/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://eternity-world.ru/>.
 */
package l2e.gameserver.model.quest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.L2DropData;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2TrapInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.base.AcquireSkillType;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.holders.ItemsHolder;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.interfaces.IL2Procedure;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.olympiad.CompetitionType;
import l2e.gameserver.model.quest.AITasks.AggroRangeEnter;
import l2e.gameserver.model.quest.AITasks.SeeCreature;
import l2e.gameserver.model.quest.AITasks.SkillSee;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.NpcQuestHtmlMessage;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SpecialCamera;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.scripting.ManagedScript;
import l2e.gameserver.scripting.ScriptManager;
import l2e.gameserver.util.MinionList;
import l2e.util.L2FastMap;
import l2e.util.Rnd;
import l2e.util.Util;

public class Quest extends ManagedScript implements IIdentifiable
{
	public static final Logger _log = Logger.getLogger(Quest.class.getName());
	
	private static Map<String, Quest> _allEventsS = new HashMap<>();
	
	private final Map<String, List<QuestTimer>> _allEventTimers = new L2FastMap<>(true);
	private final Set<Integer> _questInvolvedNpcs = new HashSet<>();
	
	private final ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();
	private final WriteLock _writeLock = _rwLock.writeLock();
	private final ReadLock _readLock = _rwLock.readLock();
	
	private final int _questId;
	private final String _name;
	private final String _descr;
	private final byte _initialState = State.CREATED;
	protected boolean _onEnterWorld = false;
	private boolean _isCustom = false;
	private boolean _isOlympiadUse = false;
	
	public int[] questItemIds = null;
	
	boolean altMethodCall = true;
	
	private static final String DEFAULT_NO_QUEST_MSG = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
	private static final String DEFAULT_ALREADY_COMPLETED_MSG = "<html><body>This quest has already been completed.</body></html>";
	
	private static final String QUEST_DELETE_FROM_CHAR_QUERY = "DELETE FROM character_quests WHERE charId=? AND name=?";
	private static final String QUEST_DELETE_FROM_CHAR_QUERY_NON_REPEATABLE_QUERY = "DELETE FROM character_quests WHERE charId=? AND name=? AND var!=?";
	
	private static final int RESET_HOUR = 6;
	private static final int RESET_MINUTES = 30;
	
	public static enum QuestSound
	{
		ITEMSOUND_QUEST_ACCEPT(new PlaySound("ItemSound.quest_accept")),
		ITEMSOUND_QUEST_MIDDLE(new PlaySound("ItemSound.quest_middle")),
		ITEMSOUND_QUEST_FINISH(new PlaySound("ItemSound.quest_finish")),
		ITEMSOUND_QUEST_ITEMGET(new PlaySound("ItemSound.quest_itemget")),
		ITEMSOUND_QUEST_TUTORIAL(new PlaySound("ItemSound.quest_tutorial")),
		ITEMSOUND_QUEST_GIVEUP(new PlaySound("ItemSound.quest_giveup")),
		ITEMSOUND_QUEST_BEFORE_BATTLE(new PlaySound("ItemSound.quest_before_battle")),
		ITEMSOUND_QUEST_JACKPOT(new PlaySound("ItemSound.quest_jackpot")),
		ITEMSOUND_QUEST_FANFARE_1(new PlaySound("ItemSound.quest_fanfare_1")),
		ITEMSOUND_QUEST_FANFARE_2(new PlaySound("ItemSound.quest_fanfare_2")),
		
		ITEMSOUND_QUEST_FANFARE_MIDDLE(new PlaySound("ItemSound.quest_fanfare_middle")),
		ITEMSOUND_ARMOR_WOOD(new PlaySound("ItemSound.armor_wood_3")),
		ITEMSOUND_ARMOR_CLOTH(new PlaySound("ItemSound.item_drop_equip_armor_cloth")),
		AMDSOUND_ED_CHIMES(new PlaySound("AmdSound.ed_chimes_05")),
		HORROR_01(new PlaySound("horror_01")),
		
		AMBSOUND_HORROR_01(new PlaySound("AmbSound.dd_horror_01")),
		AMBSOUND_HORROR_03(new PlaySound("AmbSound.d_horror_03")),
		AMBSOUND_HORROR_15(new PlaySound("AmbSound.d_horror_15")),
		
		ITEMSOUND_ARMOR_LEATHER(new PlaySound("ItemSound.itemdrop_armor_leather")),
		ITEMSOUND_WEAPON_SPEAR(new PlaySound("ItemSound.itemdrop_weapon_spear")),
		AMBSOUND_MT_CREAK(new PlaySound("AmbSound.mt_creak01")),
		AMBSOUND_EG_DRON(new PlaySound("AmbSound.eg_dron_02")),
		SKILLSOUND_HORROR_02(new PlaySound("SkillSound5.horror_02")),
		CHRSOUND_MHFIGHTER_CRY(new PlaySound("ChrSound.MHFighter_cry")),
		
		AMDSOUND_WIND_LOOT(new PlaySound("AmdSound.d_wind_loot_02")),
		INTERFACESOUND_CHARSTAT_OPEN(new PlaySound("InterfaceSound.charstat_open_01")),
		
		AMDSOUND_HORROR_02(new PlaySound("AmdSound.dd_horror_02")),
		CHRSOUND_FDELF_CRY(new PlaySound("ChrSound.FDElf_Cry")),
		
		AMBSOUND_WINGFLAP(new PlaySound("AmbSound.t_wingflap_04")),
		AMBSOUND_THUNDER(new PlaySound("AmbSound.thunder_02")),
		
		AMBSOUND_DRONE(new PlaySound("AmbSound.ed_drone_02")),
		AMBSOUND_CRYSTAL_LOOP(new PlaySound("AmbSound.cd_crystal_loop")),
		AMBSOUND_PERCUSSION_01(new PlaySound("AmbSound.dt_percussion_01")),
		AMBSOUND_PERCUSSION_02(new PlaySound("AmbSound.ac_percussion_02")),
		
		ITEMSOUND_BROKEN_KEY(new PlaySound("ItemSound2.broken_key")),
		
		ITEMSOUND_SIREN(new PlaySound("ItemSound3.sys_siren")),
		
		ITEMSOUND_ENCHANT_SUCCESS(new PlaySound("ItemSound3.sys_enchant_success")),
		ITEMSOUND_ENCHANT_FAILED(new PlaySound("ItemSound3.sys_enchant_failed")),
		
		ITEMSOUND_SOW_SUCCESS(new PlaySound("ItemSound3.sys_sow_success")),
		
		SKILLSOUND_HORROR_1(new PlaySound("SkillSound5.horror_01")),
		
		SKILLSOUND_HORROR_2(new PlaySound("SkillSound5.horror_02")),
		
		SKILLSOUND_ANTARAS_FEAR(new PlaySound("SkillSound3.antaras_fear")),
		
		SKILLSOUND_JEWEL_CELEBRATE(new PlaySound("SkillSound2.jewel.celebrate")),
		
		SKILLSOUND_LIQUID_MIX(new PlaySound("SkillSound5.liquid_mix_01")),
		SKILLSOUND_LIQUID_SUCCESS(new PlaySound("SkillSound5.liquid_success_01")),
		SKILLSOUND_LIQUID_FAIL(new PlaySound("SkillSound5.liquid_fail_01")),
		
		ETCSOUND_ELROKI_SONG_FULL(new PlaySound("EtcSound.elcroki_song_full")),
		ETCSOUND_ELROKI_SONG_1ST(new PlaySound("EtcSound.elcroki_song_1st")),
		ETCSOUND_ELROKI_SONG_2ND(new PlaySound("EtcSound.elcroki_song_2nd")),
		ETCSOUND_ELROKI_SONG_3RD(new PlaySound("EtcSound.elcroki_song_3rd")),
		
		BS01_A(new PlaySound("BS01_A")),
		BS02_A(new PlaySound("BS02_A")),
		BS03_A(new PlaySound("BS03_A")),
		BS04_A(new PlaySound("BS04_A")),
		BS06_A(new PlaySound("BS06_A")),
		BS07_A(new PlaySound("BS07_A")),
		BS08_A(new PlaySound("BS08_A")),
		BS01_D(new PlaySound("BS01_D")),
		BS02_D(new PlaySound("BS02_D")),
		BS05_D(new PlaySound("BS05_D")),
		BS07_D(new PlaySound("BS07_D"));
		
		private final PlaySound _playSound;
		
		private static Map<String, PlaySound> soundPackets = new HashMap<>();
		
		private QuestSound(PlaySound playSound)
		{
			_playSound = playSound;
		}
		
		public static PlaySound getSound(String soundName)
		{
			if (soundPackets.containsKey(soundName))
			{
				return soundPackets.get(soundName);
			}
			
			for (QuestSound qs : QuestSound.values())
			{
				if (qs._playSound.getSoundName().equals(soundName))
				{
					soundPackets.put(soundName, qs._playSound);
					return qs._playSound;
				}
			}
			
			_log.info("Missing QuestSound enum for sound: " + soundName);
			soundPackets.put(soundName, new PlaySound(soundName));
			return soundPackets.get(soundName);
		}
		
		public String getSoundName()
		{
			return _playSound.getSoundName();
		}
		
		public PlaySound getPacket()
		{
			return _playSound;
		}
	}
	
	public int getResetHour()
	{
		return RESET_HOUR;
	}
	
	public int getResetMinutes()
	{
		return RESET_MINUTES;
	}
	
	public static Collection<Quest> findAllEvents()
	{
		return _allEventsS.values();
	}
	
	public Quest(int questId, String name, String descr)
	{
		_questId = questId;
		_name = name;
		_descr = descr;
		if (questId != 0)
		{
			QuestManager.getInstance().addQuest(this);
		}
		else
		{
			_allEventsS.put(name, this);
		}
		init_LoadGlobalData();
	}
	
	protected void init_LoadGlobalData()
	{
		
	}
	
	public void saveGlobalData()
	{
		
	}
	
	public static enum TrapAction
	{
		TRAP_TRIGGERED,
		TRAP_DETECTED,
		TRAP_DISARMED
	}
	
	public static enum QuestEventType
	{
		ON_FIRST_TALK(false),
		QUEST_START(true),
		ON_TALK(true),
		ON_ATTACK(true),
		ON_KILL(true),
		ON_SPAWN(true),
		ON_SKILL_SEE(true),
		ON_FACTION_CALL(true),
		ON_AGGRO_RANGE_ENTER(true),
		ON_SPELL_FINISHED(true),
		ON_SKILL_LEARN(false),
		ON_ENTER_ZONE(true),
		ON_EXIT_ZONE(true),
		ON_TRAP_ACTION(true),
		ON_ITEM_USE(true),
		ON_NODE_ARRIVED(true),
		ON_EVENT_RECEIVED(true),
		ON_MOVE_FINISHED(true),
		ON_SEE_CREATURE(true),
		ON_ROUTE_FINISHED(true),
		ON_CAN_SEE_ME(false);
		
		private boolean _allowMultipleRegistration;
		
		private QuestEventType(boolean allowMultipleRegistration)
		{
			_allowMultipleRegistration = allowMultipleRegistration;
		}
		
		public boolean isMultipleRegistrationAllowed()
		{
			return _allowMultipleRegistration;
		}
	}
	
	@Override
	public int getId()
	{
		return _questId;
	}
	
	public QuestState newQuestState(L2PcInstance player)
	{
		return new QuestState(this, player, getInitialState());
	}
	
	public byte getInitialState()
	{
		return _initialState;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getDescr(L2PcInstance player)
	{
		if (_descr.equals(""))
		{
			return new CustomMessage("quest." + _questId, player.getLang()).toString();
		}
		return _descr;
	}
	
	public void startQuestTimer(String name, long time, L2Npc npc, L2PcInstance player)
	{
		startQuestTimer(name, time, npc, player, false);
	}
	
	public void startQuestTimer(String name, long time, L2Npc npc, L2PcInstance player, boolean repeating)
	{
		List<QuestTimer> timers = _allEventTimers.get(name);
		
		if (timers == null)
		{
			timers = new ArrayList<>();
			timers.add(new QuestTimer(this, name, time, npc, player, repeating));
			_allEventTimers.put(name, timers);
		}
		else
		{
			if (getQuestTimer(name, npc, player) == null)
			{
				_writeLock.lock();
				try
				{
					timers.add(new QuestTimer(this, name, time, npc, player, repeating));
				}
				finally
				{
					_writeLock.unlock();
				}
			}
		}
	}
	
	public QuestTimer getQuestTimer(String name, L2Npc npc, L2PcInstance player)
	{
		final List<QuestTimer> timers = _allEventTimers.get(name);
		if (timers != null)
		{
			_readLock.lock();
			try
			{
				for (QuestTimer timer : timers)
				{
					if (timer != null)
					{
						if (timer.isMatch(this, name, npc, player))
						{
							return timer;
						}
					}
				}
			}
			finally
			{
				_readLock.unlock();
			}
		}
		return null;
	}
	
	public void cancelQuestTimers(String name)
	{
		final List<QuestTimer> timers = _allEventTimers.get(name);
		if (timers != null)
		{
			_writeLock.lock();
			try
			{
				for (QuestTimer timer : timers)
				{
					if (timer != null)
					{
						timer.cancel();
					}
				}
				timers.clear();
			}
			finally
			{
				_writeLock.unlock();
			}
		}
	}
	
	public void cancelQuestTimer(String name, L2Npc npc, L2PcInstance player)
	{
		final QuestTimer timer = getQuestTimer(name, npc, player);
		if (timer != null)
		{
			timer.cancelAndRemove();
		}
	}
	
	public void removeQuestTimer(QuestTimer timer)
	{
		if (timer != null)
		{
			final List<QuestTimer> timers = _allEventTimers.get(timer.getName());
			if (timers != null)
			{
				_writeLock.lock();
				try
				{
					timers.remove(timer);
				}
				finally
				{
					_writeLock.unlock();
				}
			}
		}
	}
	
	public Map<String, List<QuestTimer>> getQuestTimers()
	{
		return _allEventTimers;
	}
	
	public final void notifyAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onAttack(npc, attacker, damage, isSummon, skill);
		}
		catch (Exception e)
		{
			showError(attacker, e);
			return;
		}
		showResult(attacker, res);
	}
	
	public final void notifyDeath(L2Character killer, L2Character victim, QuestState qs)
	{
		String res = null;
		try
		{
			res = onDeath(killer, victim, qs);
		}
		catch (Exception e)
		{
			showError(qs.getPlayer(), e);
		}
		showResult(qs.getPlayer(), res);
	}
	
	public final void notifyItemUse(L2Item item, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onItemUse(item, player);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	public final void notifySpellFinished(L2Npc instance, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onSpellFinished(instance, player, skill);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	public final void notifyTrapAction(L2TrapInstance trap, L2Character trigger, TrapAction action)
	{
		String res = null;
		try
		{
			res = onTrapAction(trap, trigger, action);
		}
		catch (Exception e)
		{
			if (trigger.getActingPlayer() != null)
			{
				showError(trigger.getActingPlayer(), e);
			}
			_log.log(Level.WARNING, "Exception on onTrapAction() in notifyTrapAction(): " + e.getMessage(), e);
			return;
		}
		if (trigger.getActingPlayer() != null)
		{
			showResult(trigger.getActingPlayer(), res);
		}
	}
	
	public final void notifySpawn(L2Npc npc)
	{
		try
		{
			onSpawn(npc);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on onSpawn() in notifySpawn(): " + e.getMessage(), e);
		}
	}
	
	public final boolean notifyEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onAdvEvent(event, npc, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final void notifyEventReceived(String eventName, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		try
		{
			onEventReceived(eventName, sender, receiver, reference);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on onEventReceived() in notifyEventReceived(): " + e.getMessage(), e);
		}
	}
	
	public final void notifyEnterWorld(L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onEnterWorld(player);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	public final void notifyKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		String res = null;
		try
		{
			res = onKill(npc, killer, isSummon);
		}
		catch (Exception e)
		{
			showError(killer, e);
		}
		showResult(killer, res);
	}
	
	public final boolean notifyKillByMob(L2Npc npc, L2Npc killer)
	{
		try
		{
			onKillByMob(npc, killer);
		}
		catch (Exception e)
		{
			System.out.println(e);
			return false;
		}
		return true;
	}
	
	public final boolean notifyTalk(L2Npc npc, QuestState qs)
	{
		String res = null;
		try
		{
			res = onTalk(npc, qs.getPlayer());
		}
		catch (Exception e)
		{
			return showError(qs.getPlayer(), e);
		}
		qs.getPlayer().setLastQuestNpcObject(npc.getObjectId());
		return showResult(qs.getPlayer(), res);
	}
	
	public final void notifyFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onFirstTalk(npc, player);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	public final void notifyAcquireSkillList(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onAcquireSkillList(npc, player);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	public final void notifyAcquireSkillInfo(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onAcquireSkillInfo(npc, player, skill);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	public final void notifyAcquireSkill(L2Npc npc, L2PcInstance player, L2Skill skill, AcquireSkillType type)
	{
		String res = null;
		try
		{
			res = onAcquireSkill(npc, player, skill, type);
		}
		catch (Exception e)
		{
			showError(player, e);
		}
		showResult(player, res);
	}
	
	public final boolean notifyItemTalk(L2ItemInstance item, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onItemTalk(item, player);
			if (res != null)
			{
				if (res.equalsIgnoreCase("true"))
				{
					return true;
				}
				else if (res.equalsIgnoreCase("false"))
				{
					return false;
				}
			}
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public String onItemTalk(L2ItemInstance item, L2PcInstance player)
	{
		return null;
	}
	
	public final boolean notifyItemEvent(L2ItemInstance item, L2PcInstance player, String event)
	{
		String res = null;
		try
		{
			res = onItemEvent(item, player, event);
			if (res != null)
			{
				if (res.equalsIgnoreCase("true"))
				{
					return true;
				}
				else if (res.equalsIgnoreCase("false"))
				{
					return false;
				}
			}
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final void notifySkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		ThreadPoolManager.getInstance().executeAi(new SkillSee(this, npc, caster, skill, targets, isSummon));
	}
	
	public final void notifyFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isSummon)
	{
		String res = null;
		try
		{
			res = onFactionCall(npc, caller, attacker, isSummon);
		}
		catch (Exception e)
		{
			showError(attacker, e);
		}
		showResult(attacker, res);
	}
	
	public final void notifyAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		ThreadPoolManager.getInstance().executeAi(new AggroRangeEnter(this, npc, player, isSummon));
	}
	
	public final void notifySeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		ThreadPoolManager.getInstance().executeAi(new SeeCreature(this, npc, creature, isSummon));
	}
	
	public final void notifyEnterZone(L2Character character, L2ZoneType zone)
	{
		L2PcInstance player = character.getActingPlayer();
		String res = null;
		try
		{
			res = onEnterZone(character, zone);
		}
		catch (Exception e)
		{
			if (player != null)
			{
				showError(player, e);
			}
		}
		if (player != null)
		{
			showResult(player, res);
		}
	}
	
	public final void notifyExitZone(L2Character character, L2ZoneType zone)
	{
		L2PcInstance player = character.getActingPlayer();
		String res = null;
		try
		{
			res = onExitZone(character, zone);
		}
		catch (Exception e)
		{
			if (player != null)
			{
				showError(player, e);
			}
		}
		if (player != null)
		{
			showResult(player, res);
		}
	}
	
	public final void notifyOlympiadWin(L2PcInstance winner, CompetitionType type)
	{
		try
		{
			onOlympiadWin(winner, type);
		}
		catch (Exception e)
		{
			showError(winner, e);
		}
	}
	
	public final void notifyOlympiadLose(L2PcInstance loser, CompetitionType type)
	{
		try
		{
			onOlympiadLose(loser, type);
		}
		catch (Exception e)
		{
			showError(loser, e);
		}
	}
	
	public final void notifyMoveFinished(L2Npc npc)
	{
		try
		{
			onMoveFinished(npc);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on onMoveFinished() in notifyMoveFinished(): " + e.getMessage(), e);
		}
	}
	
	public final void notifyRouteFinished(L2Npc npc)
	{
		try
		{
			onRouteFinished(npc);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on onRouteFinished() in notifyRouteFinished(): " + e.getMessage(), e);
		}
	}
	
	public final boolean notifyOnCanSeeMe(L2Npc npc, L2PcInstance player)
	{
		try
		{
			return onCanSeeMe(npc, player);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on onCanSeeMe() in notifyOnCanSeeMe(): " + e.getMessage(), e);
		}
		return false;
	}
	
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		return null;
	}
	
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, L2Skill skill)
	{
		return onAttack(npc, attacker, damage, isSummon);
	}
	
	public String onDeath(L2Character killer, L2Character victim, QuestState qs)
	{
		return onAdvEvent("", ((killer instanceof L2Npc) ? ((L2Npc) killer) : null), qs.getPlayer());
	}
	
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (player != null)
		{
			final QuestState qs = player.getQuestState(getName());
			if (qs != null)
			{
				return onEvent(event, qs);
			}
		}
		return null;
	}
	
	public String onEvent(String event, QuestState qs)
	{
		return null;
	}
	
	public String onKill(L2Npc npc, QuestState qs)
	{
		return null;
	}
	
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		return null;
	}
	
	public String onKillByMob(L2Npc npc, L2Npc killer)
	{
		return null;
	}
	
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		return null;
	}
	
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	public String onItemEvent(L2ItemInstance item, L2PcInstance player, String event)
	{
		return null;
	}
	
	public String onAcquireSkillList(L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	public String onAcquireSkillInfo(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	public String onAcquireSkill(L2Npc npc, L2PcInstance player, L2Skill skill, AcquireSkillType type)
	{
		return null;
	}
	
	public String onItemUse(L2Item item, L2PcInstance player)
	{
		return null;
	}
	
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		return null;
	}
	
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	public String onTrapAction(L2TrapInstance trap, L2Character trigger, TrapAction action)
	{
		return null;
	}
	
	public String onSpawn(L2Npc npc)
	{
		return null;
	}
	
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isSummon)
	{
		return null;
	}
	
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		return null;
	}
	
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		return null;
	}
	
	public String onEnterWorld(L2PcInstance player)
	{
		return null;
	}
	
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		return null;
	}
	
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		return null;
	}
	
	public String onEventReceived(String eventName, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		return null;
	}
	
	public void onOlympiadWin(L2PcInstance winner, CompetitionType type)
	{
		
	}
	
	public void onOlympiadLose(L2PcInstance loser, CompetitionType type)
	{
		
	}
	
	public boolean onCanSeeMe(L2Npc npc, L2PcInstance player)
	{
		return false;
	}
	
	public boolean showError(L2PcInstance player, Throwable t)
	{
		_log.log(Level.WARNING, getScriptFile().getAbsolutePath(), t);
		if (t.getMessage() == null)
		{
			_log.warning(getClass().getSimpleName() + ": " + t.getMessage());
		}
		if ((player != null) && player.getAccessLevel().isGm())
		{
			String res = "<html><body><title>Script error</title>" + Util.getStackTrace(t) + "</body></html>";
			return showResult(player, res);
		}
		return false;
	}
	
	public boolean showResult(L2PcInstance player, String res)
	{
		if ((res == null) || res.isEmpty() || (player == null))
		{
			return true;
		}
		
		if (res.endsWith(".htm") || res.endsWith(".html"))
		{
			showHtmlFile(player, res);
		}
		else if (res.startsWith("<html>"))
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(res);
			npcReply.replace("%playername%", player.getName());
			player.sendPacket(npcReply);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.sendMessage(res);
		}
		return false;
	}
	
	public static final void playerEnter(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE charId = ? AND name = ?");
			PreparedStatement invalidQuestDataVar = con.prepareStatement("DELETE FROM character_quests WHERE charId = ? AND name = ? AND var = ?");
			PreparedStatement ps1 = con.prepareStatement("SELECT name, value FROM character_quests WHERE charId = ? AND var = ?"))
		{
			ps1.setInt(1, player.getObjectId());
			ps1.setString(2, "<state>");
			try (ResultSet rs = ps1.executeQuery())
			{
				while (rs.next())
				{
					String questId = rs.getString("name");
					String statename = rs.getString("value");
					
					Quest q = QuestManager.getInstance().getQuest(questId);
					if (q == null)
					{
						_log.finer("Unknown quest " + questId + " for player " + player.getName());
						if (Config.AUTODELETE_INVALID_QUEST_DATA)
						{
							invalidQuestData.setInt(1, player.getObjectId());
							invalidQuestData.setString(2, questId);
							invalidQuestData.executeUpdate();
						}
						continue;
					}
					new QuestState(q, player, State.getStateId(statename));
				}
			}
			
			try (PreparedStatement ps2 = con.prepareStatement("SELECT name, var, value FROM character_quests WHERE charId = ? AND var <> ?"))
			{
				ps2.setInt(1, player.getObjectId());
				ps2.setString(2, "<state>");
				try (ResultSet rs = ps2.executeQuery())
				{
					while (rs.next())
					{
						String questId = rs.getString("name");
						String var = rs.getString("var");
						String value = rs.getString("value");
						QuestState qs = player.getQuestState(questId);
						if (qs == null)
						{
							_log.finer("Lost variable " + var + " in quest " + questId + " for player " + player.getName());
							if (Config.AUTODELETE_INVALID_QUEST_DATA)
							{
								invalidQuestDataVar.setInt(1, player.getObjectId());
								invalidQuestDataVar.setString(2, questId);
								invalidQuestDataVar.setString(3, var);
								invalidQuestDataVar.executeUpdate();
							}
							continue;
						}
						qs.setInternal(var, value);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not insert char quest:", e);
		}
		
		for (String name : _allEventsS.keySet())
		{
			player.processQuestEvent(name, "enter");
		}
	}
	
	public final void saveGlobalQuestVar(String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO quest_global_data (quest_name,var,value) VALUES (?,?,?)"))
		{
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.setString(3, value);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not insert global quest variable:", e);
		}
	}
	
	public final String loadGlobalQuestVar(String var)
	{
		String result = "";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT value FROM quest_global_data WHERE quest_name = ? AND var = ?"))
		{
			statement.setString(1, getName());
			statement.setString(2, var);
			try (ResultSet rs = statement.executeQuery())
			{
				if (rs.first())
				{
					result = rs.getString(1);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not load global quest variable:", e);
		}
		return result;
	}
	
	public final void deleteGlobalQuestVar(String var)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ? AND var = ?"))
		{
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not delete global quest variable:", e);
		}
	}
	
	public final void deleteAllGlobalQuestVars()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ?"))
		{
			statement.setString(1, getName());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not delete global quest variables:", e);
		}
	}
	
	public static void createQuestVarInDb(QuestState qs, String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO character_quests (charId,name,var,value) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE value=?"))
		{
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.setString(5, value);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not insert char quest:", e);
		}
	}
	
	public static void updateQuestVarInDb(QuestState qs, String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE character_quests SET value=? WHERE charId=? AND name=? AND var = ?"))
		{
			statement.setString(1, value);
			statement.setInt(2, qs.getPlayer().getObjectId());
			statement.setString(3, qs.getQuestName());
			statement.setString(4, var);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not update char quest:", e);
		}
	}
	
	public static void deleteQuestVarInDb(QuestState qs, String var)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=? AND name=? AND var=?"))
		{
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not delete char quest:", e);
		}
	}
	
	public static void deleteQuestInDb(QuestState qs, boolean repeatable)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(repeatable ? QUEST_DELETE_FROM_CHAR_QUERY : QUEST_DELETE_FROM_CHAR_QUERY_NON_REPEATABLE_QUERY))
		{
			ps.setInt(1, qs.getPlayer().getObjectId());
			ps.setString(2, qs.getQuestName());
			if (!repeatable)
			{
				ps.setString(3, "<state>");
			}
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not delete char quest:", e);
		}
	}
	
	public static void createQuestInDb(QuestState qs)
	{
		createQuestVarInDb(qs, "<state>", State.getStateName(qs.getState()));
	}
	
	public static void updateQuestInDb(QuestState qs)
	{
		updateQuestVarInDb(qs, "<state>", State.getStateName(qs.getState()));
	}
	
	public static String getNoQuestMsg(L2PcInstance player)
	{
		final String result = HtmCache.getInstance().getHtm(player.getLang(), "data/html/noquest.htm");
		if ((result != null) && (result.length() > 0))
		{
			return result;
		}
		return DEFAULT_NO_QUEST_MSG;
	}
	
	public static String getAlreadyCompletedMsg(L2PcInstance player)
	{
		final String result = HtmCache.getInstance().getHtm(player.getLang(), "data/html/alreadycompleted.htm");
		if ((result != null) && (result.length() > 0))
		{
			return result;
		}
		return DEFAULT_ALREADY_COMPLETED_MSG;
	}
	
	public void addEventId(QuestEventType eventType, int... npcIds)
	{
		try
		{
			for (int npcId : npcIds)
			{
				final L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
				if (t != null)
				{
					t.addQuestEvent(eventType, this);
					_questInvolvedNpcs.add(npcId);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on addEventId(): " + e.getMessage(), e);
		}
	}
	
	public void addStartNpc(int npcId)
	{
		addEventId(QuestEventType.QUEST_START, npcId);
	}
	
	public void addFirstTalkId(int npcId)
	{
		addEventId(QuestEventType.ON_FIRST_TALK, npcId);
	}
	
	public void addTalkId(int npcId)
	{
		addEventId(QuestEventType.ON_TALK, npcId);
	}
	
	public void addKillId(int killId)
	{
		addEventId(QuestEventType.ON_KILL, killId);
	}
	
	public void addAttackId(int npcId)
	{
		addEventId(QuestEventType.ON_ATTACK, npcId);
	}
	
	public void addStartNpc(int... npcIds)
	{
		addEventId(QuestEventType.QUEST_START, npcIds);
	}
	
	public void addFirstTalkId(int... npcIds)
	{
		addEventId(QuestEventType.ON_FIRST_TALK, npcIds);
	}
	
	public void addAcquireSkillId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SKILL_LEARN, npcIds);
	}
	
	public void addAttackId(int... npcIds)
	{
		addEventId(QuestEventType.ON_ATTACK, npcIds);
	}
	
	public void addKillId(int... killIds)
	{
		addEventId(QuestEventType.ON_KILL, killIds);
	}
	
	public void addKillId(Collection<Integer> killIds)
	{
		for (int killId : killIds)
		{
			addEventId(QuestEventType.ON_KILL, killId);
		}
	}
	
	public void addTalkId(int... npcIds)
	{
		addEventId(QuestEventType.ON_TALK, npcIds);
	}
	
	public void addSpawnId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SPAWN, npcIds);
	}
	
	public void addSkillSeeId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SKILL_SEE, npcIds);
	}
	
	public void addSpellFinishedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SPELL_FINISHED, npcIds);
	}
	
	public void addTrapActionId(int... npcIds)
	{
		addEventId(QuestEventType.ON_TRAP_ACTION, npcIds);
	}
	
	public void addFactionCallId(int... npcIds)
	{
		addEventId(QuestEventType.ON_FACTION_CALL, npcIds);
	}
	
	public void addAggroRangeEnterId(int... npcIds)
	{
		addEventId(QuestEventType.ON_AGGRO_RANGE_ENTER, npcIds);
	}
	
	public void addSeeCreatureId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SEE_CREATURE, npcIds);
	}
	
	public L2ZoneType[] addEnterZoneId(int... zoneIds)
	{
		L2ZoneType[] value = new L2ZoneType[zoneIds.length];
		int i = 0;
		for (int zoneId : zoneIds)
		{
			try
			{
				L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
				if (zone != null)
				{
					zone.addQuestEvent(QuestEventType.ON_ENTER_ZONE, this);
				}
				value[i++] = zone;
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception on addEnterZoneId(): " + e.getMessage(), e);
				continue;
			}
		}
		
		return value;
	}
	
	public L2ZoneType[] addExitZoneId(int... zoneIds)
	{
		L2ZoneType[] value = new L2ZoneType[zoneIds.length];
		int i = 0;
		for (int zoneId : zoneIds)
		{
			try
			{
				L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
				if (zone != null)
				{
					zone.addQuestEvent(QuestEventType.ON_EXIT_ZONE, this);
				}
				value[i++] = zone;
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Exception on addEnterZoneId(): " + e.getMessage(), e);
				continue;
			}
		}
		
		return value;
	}
	
	public L2ZoneType addExitZoneId(int zoneId)
	{
		try
		{
			L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
			if (zone != null)
			{
				zone.addQuestEvent(QuestEventType.ON_EXIT_ZONE, this);
			}
			return zone;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on addExitZoneId(): " + e.getMessage(), e);
			return null;
		}
	}
	
	public void addEventReceivedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_EVENT_RECEIVED, npcIds);
	}
	
	public void addMoveFinishedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_MOVE_FINISHED, npcIds);
	}
	
	public void addRouteFinishedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_ROUTE_FINISHED, npcIds);
	}
	
	public void addCanSeeMeId(int... npcIds)
	{
		addEventId(QuestEventType.ON_CAN_SEE_ME, npcIds);
	}
	
	public void addCanSeeMeId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_CAN_SEE_ME, npcId);
		}
	}
	
	public L2PcInstance getRandomPartyMember(L2PcInstance player)
	{
		if (player == null)
		{
			return null;
		}
		final L2Party party = player.getParty();
		if ((party == null) || (party.getMembers().isEmpty()))
		{
			return player;
		}
		return party.getMembers().get(Rnd.get(party.getMembers().size()));
	}
	
	public L2PcInstance getRandomPartyMember(L2PcInstance player, int cond)
	{
		return getRandomPartyMember(player, "cond", String.valueOf(cond));
	}
	
	public L2PcInstance getRandomPartyMember(L2PcInstance player, String var, String value)
	{
		if (player == null)
		{
			return null;
		}
		
		if (var == null)
		{
			return getRandomPartyMember(player);
		}
		
		QuestState temp = null;
		L2Party party = player.getParty();
		
		if ((party == null) || (party.getMembers().isEmpty()))
		{
			temp = player.getQuestState(getName());
			if ((temp != null) && temp.isSet(var) && temp.get(var).equalsIgnoreCase(value))
			{
				return player;
			}
			
			return null;
		}
		
		List<L2PcInstance> candidates = new ArrayList<>();
		L2Object target = player.getTarget();
		if (target == null)
		{
			target = player;
		}
		
		for (L2PcInstance partyMember : party.getMembers())
		{
			if (partyMember == null)
			{
				continue;
			}
			temp = partyMember.getQuestState(getName());
			if ((temp != null) && (temp.get(var) != null) && (temp.get(var)).equalsIgnoreCase(value) && partyMember.isInsideRadius(target, 1500, true, false))
			{
				candidates.add(partyMember);
			}
		}
		
		if (candidates.isEmpty())
		{
			return null;
		}
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	public QuestState checkPlayerCondition(L2PcInstance player, L2Npc npc, String var, String value)
	{
		if (player == null)
		{
			return null;
		}
		
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		if ((st.get(var) == null) || (!value.equalsIgnoreCase(st.get(var))))
		{
			return null;
		}
		
		if (npc == null)
		{
			return null;
		}
		
		if (!player.isInsideRadius(npc, Config.ALT_PARTY_RANGE, true, false))
		{
			return null;
		}
		return st;
	}
	
	public List<L2PcInstance> getPartyMembers(L2PcInstance player, L2Npc npc, String var, String value)
	{
		ArrayList<L2PcInstance> candidates = new ArrayList<>();
		
		if ((player != null) && (player.isInParty()))
		{
			for (L2PcInstance partyMember : player.getParty().getMembers())
			{
				if (partyMember != null)
				{
					if (checkPlayerCondition(partyMember, npc, var, value) != null)
					{
						candidates.add(partyMember);
					}
				}
			}
		}
		else if (checkPlayerCondition(player, npc, var, value) != null)
		{
			candidates.add(player);
		}
		return candidates;
	}
	
	public L2PcInstance getRandomPartyMemberState(L2PcInstance player, byte state)
	{
		if (player == null)
		{
			return null;
		}
		
		QuestState temp = null;
		L2Party party = player.getParty();
		
		if ((party == null) || (party.getMembers().isEmpty()))
		{
			temp = player.getQuestState(getName());
			if ((temp != null) && (temp.getState() == state))
			{
				return player;
			}
			
			return null;
		}
		
		List<L2PcInstance> candidates = new ArrayList<>();
		
		L2Object target = player.getTarget();
		if (target == null)
		{
			target = player;
		}
		
		for (L2PcInstance partyMember : party.getMembers())
		{
			if (partyMember == null)
			{
				continue;
			}
			temp = partyMember.getQuestState(getName());
			if ((temp != null) && (temp.getState() == state) && partyMember.isInsideRadius(target, 1500, true, false))
			{
				candidates.add(partyMember);
			}
		}
		
		if (candidates.isEmpty())
		{
			return null;
		}
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	public static void showOnScreenMsg(L2PcInstance player, String text, int time)
	{
		player.sendPacket(new ExShowScreenMessage(text, time));
	}
	
	public static void showOnScreenMsg(L2PcInstance player, NpcStringId npcString, int position, int time, String... params)
	{
		player.sendPacket(new ExShowScreenMessage(npcString, position, time, params));
	}
	
	public static void showOnScreenMsg(L2PcInstance player, SystemMessageId systemMsg, int position, int time, String... params)
	{
		player.sendPacket(new ExShowScreenMessage(systemMsg, position, time, params));
	}
	
	public String showHtmlFile(L2PcInstance player, String fileName)
	{
		String lang = player.getLang();
		String questName = getName();
		int questId = getId();
		
		String directory = getDescr(player).toLowerCase();
		String filepath = "data/scripts/" + directory + "/" + questName + "/" + lang + "/" + fileName;
		String oriPath = filepath;
		String content = HtmCache.getInstance().getHtm(filepath);
		
		if (content == null)
		{
			filepath = "data/scripts/" + directory + "/" + questName + "/en/" + fileName;
			content = HtmCache.getInstance().getHtm(filepath);
		}
		
		if (content == null)
		{
			filepath = "data/scripts/quests/" + questName + "/" + lang + "/" + fileName;
			content = HtmCache.getInstance().getHtm(filepath);
		}
		
		if (content == null)
		{
			filepath = "data/scripts/quests/" + questName + "/en/" + fileName;
			content = HtmCache.getInstance().getHtm(filepath);
		}
		
		if (content == null)
		{
			content = "<html><body>My text is missing:<br>" + oriPath + "</body></html>";
			_log.info("Cache[HTML]: Missing HTML page: " + oriPath);
		}
		
		if (player.getTarget() != null)
		{
			content = content.replaceAll("%objectId%", String.valueOf(player.getTarget().getObjectId()));
		}
		
		if (content != null)
		{
			if ((questId > 0) && (questId < 20000))
			{
				NpcQuestHtmlMessage npcReply = new NpcQuestHtmlMessage(5, questId);
				npcReply.setHtml(content);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
			}
			else
			{
				NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
				npcReply.setHtml(content);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
			}
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		return content;
	}
	
	public String getHtm(L2PcInstance player, String lang, String fileName)
	{
		final HtmCache hc = HtmCache.getInstance();
		String content = hc.getHtm(lang, fileName.startsWith("data/") ? fileName : "data/scripts/" + getDescr(player).toLowerCase() + "/" + getName() + "/" + player.getLang() + "/" + fileName);
		if (content == null)
		{
			content = hc.getHtm(lang, "data/scripts/" + getDescr(player) + "/" + getName() + "/" + player.getLang() + "/" + fileName);
			if (content == null)
			{
				content = hc.getHtmForce(lang, "data/scripts/quests/" + getName() + "/" + player.getLang() + "/" + fileName);
			}
		}
		return content;
	}
	
	public static L2Npc addSpawn(int npcId, L2Character cha)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0, false, 0);
	}
	
	public static L2Npc addSpawn(int npcId, L2Character cha, boolean isSummonSpawn)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0, isSummonSpawn, 0);
	}
	
	public static L2Npc addSpawn(int npcId, Location loc, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId)
	{
		return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffset, despawnDelay, isSummonSpawn, instanceId);
	}
	
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffSet, long despawnDelay)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffSet, despawnDelay, false, 0);
	}
	
	public static L2Npc addSpawn(int npcId, Location loc, boolean randomOffSet, long despawnDelay)
	{
		return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffSet, despawnDelay, false, 0);
	}
	
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn, 0);
	}
	
	public static L2Npc addSpawn(int npcId, Location loc, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffset, despawnDelay, isSummonSpawn, 0);
	}
	
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId)
	{
		L2Npc result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if (template != null)
			{
				if ((x == 0) && (y == 0))
				{
					_log.log(Level.SEVERE, "Failed to adjust bad locks for quest spawn!  Spawn aborted!");
					return null;
				}
				if (randomOffset)
				{
					int offset;
					
					offset = Rnd.get(2);
					if (offset == 0)
					{
						offset = -1;
					}
					offset *= Rnd.get(50, 100);
					x += offset;
					
					offset = Rnd.get(2);
					if (offset == 0)
					{
						offset = -1;
					}
					offset *= Rnd.get(50, 100);
					y += offset;
				}
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(instanceId);
				spawn.setHeading(heading);
				spawn.setX(x);
				spawn.setY(y);
				if (template.getType().startsWith("L2Fly"))
				{
					spawn.setZ(z);
				}
				else
				{
					spawn.setZ(GeoClient.getInstance().getSpawnHeight(x, y, z));
				}
				spawn.stopRespawn();
				result = spawn.spawnOne(isSummonSpawn);
				
				if (despawnDelay > 0)
				{
					result.scheduleDespawn(despawnDelay);
				}
				
				return result;
			}
		}
		catch (Exception e1)
		{
			_log.warning("Could not spawn Npc " + npcId + " Error: " + e1.getMessage());
		}
		
		return null;
	}
	
	public L2TrapInstance addTrap(int trapId, int x, int y, int z, int heading, L2Skill skill, int instanceId)
	{
		final L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(trapId);
		L2TrapInstance trap = new L2TrapInstance(IdFactory.getInstance().getNextId(), npcTemplate, instanceId, -1);
		trap.setCurrentHp(trap.getMaxHp());
		trap.setCurrentMp(trap.getMaxMp());
		trap.setIsInvul(true);
		trap.setHeading(heading);
		trap.spawnMe(x, y, z);
		
		return trap;
	}
	
	public L2Npc addMinion(L2MonsterInstance master, int minionId)
	{
		return MinionList.spawnMinion(master, minionId);
	}
	
	public int[] getRegisteredItemIds()
	{
		return questItemIds;
	}
	
	public void registerQuestItems(int... items)
	{
		questItemIds = items;
	}
	
	@Override
	public String getScriptName()
	{
		return getName();
	}
	
	@Override
	public void setActive(boolean status)
	{
	}
	
	@Override
	public boolean reload()
	{
		unload();
		return super.reload();
	}
	
	@Override
	public boolean unload()
	{
		return unload(true);
	}
	
	public boolean unload(boolean removeFromList)
	{
		saveGlobalData();
		
		for (List<QuestTimer> timers : _allEventTimers.values())
		{
			_readLock.lock();
			try
			{
				for (QuestTimer timer : timers)
				{
					timer.cancel();
				}
			}
			finally
			{
				_readLock.unlock();
			}
			timers.clear();
		}
		_allEventTimers.clear();
		
		for (Integer npcId : _questInvolvedNpcs)
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId.intValue());
			if (template != null)
			{
				template.removeQuest(this);
			}
		}
		_questInvolvedNpcs.clear();
		
		if (removeFromList)
		{
			return QuestManager.getInstance().removeQuest(this);
		}
		return true;
	}
	
	public Set<Integer> getQuestInvolvedNpcs()
	{
		return _questInvolvedNpcs;
	}
	
	@Override
	public ScriptManager<?> getScriptManager()
	{
		return QuestManager.getInstance();
	}
	
	public void setOnEnterWorld(boolean val)
	{
		_onEnterWorld = val;
	}
	
	public boolean getOnEnterWorld()
	{
		return _onEnterWorld;
	}
	
	public void setAltMethodCall(boolean altMethodCall)
	{
		this.altMethodCall = altMethodCall;
	}
	
	public void setIsCustom(boolean val)
	{
		_isCustom = val;
	}
	
	public boolean isCustomQuest()
	{
		return _isCustom;
	}
	
	public void setOlympiadUse(boolean val)
	{
		_isOlympiadUse = val;
	}
	
	public boolean isOlympiadUse()
	{
		return _isOlympiadUse;
	}
	
	public static long getQuestItemsCount(L2PcInstance player, int itemId)
	{
		return player.getInventory().getInventoryItemCount(itemId, -1);
	}
	
	public long getQuestItemsCount(L2PcInstance player, int... itemIds)
	{
		long count = 0;
		for (L2ItemInstance item : player.getInventory().getItems())
		{
			if (item == null)
			{
				continue;
			}
			
			for (int itemId : itemIds)
			{
				if (item.getId() == itemId)
				{
					if ((count + item.getCount()) > Long.MAX_VALUE)
					{
						return Long.MAX_VALUE;
					}
					count += item.getCount();
				}
			}
		}
		return count;
	}
	
	public static boolean hasQuestItems(L2PcInstance player, int itemId)
	{
		return player.getInventory().getItemByItemId(itemId) != null;
	}
	
	public static boolean hasQuestItems(L2PcInstance player, int... itemIds)
	{
		final PcInventory inv = player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) == null)
			{
				return false;
			}
		}
		return true;
	}
	
	public static int getEnchantLevel(L2PcInstance player, int itemId)
	{
		final L2ItemInstance enchantedItem = player.getInventory().getItemByItemId(itemId);
		if (enchantedItem == null)
		{
			return 0;
		}
		return enchantedItem.getEnchantLevel();
	}
	
	public void giveAdena(L2PcInstance player, long count, boolean applyRates)
	{
		if (applyRates)
		{
			rewardItems(player, PcInventory.ADENA_ID, count);
		}
		else
		{
			giveItems(player, PcInventory.ADENA_ID, count);
		}
	}
	
	public static void rewardItems(L2PcInstance player, ItemsHolder holder)
	{
		rewardItems(player, holder.getId(), holder.getCount());
	}
	
	public static void rewardItems(L2PcInstance player, int itemId, long count)
	{
		if (count <= 0)
		{
			return;
		}
		
		final L2ItemInstance _tmpItem = ItemHolder.getInstance().createDummyItem(itemId);
		if (_tmpItem == null)
		{
			return;
		}
		
		try
		{
			if (itemId == PcInventory.ADENA_ID)
			{
				count *= Config.RATE_QUEST_REWARD_ADENA;
			}
			else if (Config.RATE_QUEST_REWARD_USE_MULTIPLIERS)
			{
				if (_tmpItem.isEtcItem())
				{
					switch (_tmpItem.getEtcItem().getItemType())
					{
						case POTION:
							count *= Config.RATE_QUEST_REWARD_POTION;
							break;
						case SCRL_ENCHANT_WP:
						case SCRL_ENCHANT_AM:
						case SCROLL:
							count *= Config.RATE_QUEST_REWARD_SCROLL;
							break;
						case RECIPE:
							count *= Config.RATE_QUEST_REWARD_RECIPE;
							break;
						case MATERIAL:
							count *= Config.RATE_QUEST_REWARD_MATERIAL;
							break;
						default:
							count *= Config.RATE_QUEST_REWARD;
					}
				}
			}
			else
			{
				count *= Config.RATE_QUEST_REWARD;
			}
		}
		catch (Exception e)
		{
			count = Long.MAX_VALUE;
		}
		
		L2ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (item == null)
		{
			return;
		}
		
		sendItemGetMessage(player, item, count);
	}
	
	private static void sendItemGetMessage(L2PcInstance player, L2ItemInstance item, long count)
	{
		if (item.getId() == PcInventory.ADENA_ID)
		{
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
			smsg.addItemNumber(count);
			player.sendPacket(smsg);
		}
		else
		{
			if (count > 1)
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(item);
				smsg.addItemNumber(count);
				player.sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				smsg.addItemName(item);
				player.sendPacket(smsg);
			}
		}
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
	
	public static void giveItems(L2PcInstance player, int itemId, long count)
	{
		giveItems(player, itemId, count, 0);
	}
	
	protected static void giveItems(L2PcInstance player, ItemsHolder holder)
	{
		giveItems(player, holder.getId(), holder.getCount());
	}
	
	public static void giveItems(L2PcInstance player, int itemId, long count, int enchantlevel)
	{
		if (count <= 0)
		{
			return;
		}
		
		final L2ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (item == null)
		{
			return;
		}
		
		if ((enchantlevel > 0) && (itemId != PcInventory.ADENA_ID))
		{
			item.setEnchantLevel(enchantlevel);
		}
		
		sendItemGetMessage(player, item, count);
	}
	
	public static void giveItems(L2PcInstance player, int itemId, long count, byte attributeId, int attributeLevel)
	{
		if (count <= 0)
		{
			return;
		}
		
		final L2ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		
		if (item == null)
		{
			return;
		}
		
		if ((attributeId >= 0) && (attributeLevel > 0))
		{
			item.setElementAttr(attributeId, attributeLevel);
			if (item.isEquipped())
			{
				item.updateElementAttrBonus(player);
			}
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(item);
			player.sendPacket(iu);
		}
		sendItemGetMessage(player, item, count);
	}
	
	public boolean dropQuestItems(L2PcInstance player, int itemId, int count, long neededCount, int dropChance, boolean sound)
	{
		return dropQuestItems(player, itemId, count, count, neededCount, dropChance, sound);
	}
	
	public static boolean dropQuestItems(L2PcInstance player, int itemId, int minCount, int maxCount, long neededCount, int dropChance, boolean sound)
	{
		dropChance *= Config.RATE_QUEST_DROP / ((player.getParty() != null) ? player.getParty().getMemberCount() : 1);
		long currentCount = getQuestItemsCount(player, itemId);
		
		if ((neededCount > 0) && (currentCount >= neededCount))
		{
			return true;
		}
		
		if (currentCount >= neededCount)
		{
			return true;
		}
		
		long itemCount = 0;
		int random = Rnd.get(L2DropData.MAX_CHANCE);
		
		while (random < dropChance)
		{
			if (minCount < maxCount)
			{
				itemCount += Rnd.get(minCount, maxCount);
			}
			else if (minCount == maxCount)
			{
				itemCount += minCount;
			}
			else
			{
				itemCount++;
			}
			dropChance -= L2DropData.MAX_CHANCE;
		}
		
		if (itemCount > 0)
		{
			if ((neededCount > 0) && ((currentCount + itemCount) > neededCount))
			{
				itemCount = neededCount - currentCount;
			}
			
			if (!player.getInventory().validateCapacityByItemId(itemId))
			{
				return false;
			}
			
			player.addItem("Quest", itemId, itemCount, player.getTarget(), true);
			
			if (sound)
			{
				playSound(player, ((currentCount + itemCount) < neededCount) ? QuestSound.ITEMSOUND_QUEST_ITEMGET : QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return ((neededCount > 0) && ((currentCount + itemCount) >= neededCount));
	}
	
	public static boolean giveItemRandomly(L2PcInstance player, int itemId, long amountToGive, long limit, double dropChance, boolean playSound)
	{
		return giveItemRandomly(player, null, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
	}
	
	public static boolean giveItemRandomly(L2PcInstance player, L2Npc npc, int itemId, long amountToGive, long limit, double dropChance, boolean playSound)
	{
		return giveItemRandomly(player, npc, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
	}
	
	public static boolean giveItemRandomly(L2PcInstance player, L2Npc npc, int itemId, long minAmount, long maxAmount, long limit, double dropChance, boolean playSound)
	{
		final long currentCount = getQuestItemsCount(player, itemId);
		
		if ((limit > 0) && (currentCount >= limit))
		{
			return true;
		}
		
		minAmount *= Config.RATE_QUEST_DROP;
		maxAmount *= Config.RATE_QUEST_DROP;
		dropChance *= Config.RATE_QUEST_DROP;
		
		if ((npc != null) && Config.CHAMPION_ENABLE && npc.isChampion())
		{
			dropChance *= Config.CHAMPION_REWARDS;
			
			if ((itemId == PcInventory.ADENA_ID) || (itemId == PcInventory.ANCIENT_ADENA_ID))
			{
				minAmount *= Config.CHAMPION_ADENAS_REWARDS;
				maxAmount *= Config.CHAMPION_ADENAS_REWARDS;
			}
			else
			{
				minAmount *= Config.CHAMPION_REWARDS;
				maxAmount *= Config.CHAMPION_REWARDS;
			}
		}
		
		long amountToGive = ((minAmount == maxAmount) ? minAmount : Rnd.get(minAmount, maxAmount));
		final double random = Rnd.nextDouble();
		
		if ((dropChance >= random) && (amountToGive > 0) && player.getInventory().validateCapacityByItemId(itemId))
		{
			if ((limit > 0) && ((currentCount + amountToGive) > limit))
			{
				amountToGive = limit - currentCount;
			}
			L2ItemInstance item = player.addItem("Quest", itemId, amountToGive, npc, true);
			
			if (item != null)
			{
				if ((currentCount + amountToGive) == limit)
				{
					if (playSound)
					{
						playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					return true;
				}
				
				if (playSound)
				{
					playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return false;
	}
	
	public static boolean takeItems(L2PcInstance player, int itemId, long amount)
	{
		final L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
		if (item == null)
		{
			return false;
		}
		
		if ((amount < 0) || (amount > item.getCount()))
		{
			amount = item.getCount();
		}
		
		if (item.isEquipped())
		{
			final L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm : unequiped)
			{
				iu.addModifiedItem(itm);
			}
			player.sendPacket(iu);
			player.broadcastUserInfo();
		}
		return player.destroyItemByItemId("Quest", itemId, amount, player, true);
	}
	
	public static long takeAllItems(L2PcInstance player, int itemId, long amount)
	{
		final L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
		if (item == null)
		{
			return 0;
		}
		
		if ((amount < 0) || (amount > item.getCount()))
		{
			amount = item.getCount();
		}
		
		if (item.isEquipped())
		{
			final L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm : unequiped)
			{
				iu.addModifiedItem(itm);
			}
			player.sendPacket(iu);
			player.broadcastUserInfo();
		}
		player.destroyItemByItemId("Quest", itemId, amount, player, true);
		
		return amount;
	}
	
	protected static boolean takeItems(L2PcInstance player, ItemsHolder holder)
	{
		return takeItems(player, holder.getId(), holder.getCount());
	}
	
	public static boolean takeItems(L2PcInstance player, int amount, int... itemIds)
	{
		boolean check = true;
		if (itemIds != null)
		{
			for (int item : itemIds)
			{
				check &= takeItems(player, item, amount);
			}
		}
		return check;
	}
	
	public void removeRegisteredQuestItems(L2PcInstance player)
	{
		takeItems(player, -1, questItemIds);
	}
	
	public static void playSound(L2PcInstance player, String sound)
	{
		player.sendPacket(QuestSound.getSound(sound));
	}
	
	public static void playSound(L2PcInstance player, QuestSound sound)
	{
		player.sendPacket(sound.getPacket());
	}
	
	public static void addExpAndSp(L2PcInstance player, long exp, int sp)
	{
		player.addExpAndSp((long) player.calcStat(Stats.EXPSP_RATE, exp * Config.RATE_QUEST_REWARD_XP, null, null), (int) player.calcStat(Stats.EXPSP_RATE, sp * Config.RATE_QUEST_REWARD_SP, null, null));
	}
	
	public static int getRandom(int max)
	{
		return Rnd.get(max);
	}
	
	public static int getRandom(int min, int max)
	{
		return Rnd.get(min, max);
	}
	
	public static boolean getRandomBoolean()
	{
		return Rnd.nextBoolean();
	}
	
	public static int getItemEquipped(L2PcInstance player, int slot)
	{
		return player.getInventory().getPaperdollItemId(slot);
	}
	
	public static int getGameTicks()
	{
		return GameTimeController.getInstance().getGameTicks();
	}
	
	public final void executeForEachPlayer(L2PcInstance player, final L2Npc npc, final boolean isSummon, boolean includeParty, boolean includeCommandChannel)
	{
		if ((includeParty || includeCommandChannel) && player.isInParty())
		{
			if (includeCommandChannel && player.getParty().isInCommandChannel())
			{
				player.getParty().getCommandChannel().forEachMember(new IL2Procedure<L2PcInstance>()
				{
					@Override
					public boolean execute(L2PcInstance member)
					{
						actionForEachPlayer(member, npc, isSummon);
						return true;
					}
				});
			}
			else if (includeParty)
			{
				player.getParty().forEachMember(new IL2Procedure<L2PcInstance>()
				{
					@Override
					public boolean execute(L2PcInstance member)
					{
						actionForEachPlayer(member, npc, isSummon);
						return true;
					}
				});
			}
		}
		else
		{
			actionForEachPlayer(player, npc, isSummon);
		}
	}
	
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
	}
	
	public void openDoor(int doorId, int instanceId)
	{
		final L2DoorInstance door = getDoor(doorId, instanceId);
		if (door == null)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": called openDoor(" + doorId + ", " + instanceId + "); but door wasnt found!", new NullPointerException());
		}
		else if (!door.getOpen())
		{
			door.openMe();
		}
	}
	
	public void closeDoor(int doorId, int instanceId)
	{
		final L2DoorInstance door = getDoor(doorId, instanceId);
		if (door == null)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": called closeDoor(" + doorId + ", " + instanceId + "); but door wasnt found!", new NullPointerException());
		}
		else if (door.getOpen())
		{
			door.closeMe();
		}
	}
	
	public L2DoorInstance getDoor(int doorId, int instanceId)
	{
		L2DoorInstance door = null;
		if (instanceId <= 0)
		{
			door = DoorParser.getInstance().getDoor(doorId);
		}
		else
		{
			final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
			if (inst != null)
			{
				door = inst.getDoor(doorId);
			}
		}
		return door;
	}
	
	public void teleportPlayer(L2PcInstance player, Location loc, int instanceId)
	{
		teleportPlayer(player, loc, instanceId, true);
	}
	
	public void teleportPlayer(L2PcInstance player, Location loc, int instanceId, boolean allowRandomOffset)
	{
		loc.setInstanceId(instanceId);
		player.teleToLocation(loc, allowRandomOffset);
	}
	
	protected static boolean isIntInArray(int i, int[] ia)
	{
		for (int v : ia)
		{
			if (i == v)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isDigit(String digit)
	{
		try
		{
			Integer.parseInt(digit);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public void addMoveFinishedId(int npcId)
	{
		addEventId(QuestEventType.ON_MOVE_FINISHED, npcId);
	}
	
	public void addNodeArrivedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_NODE_ARRIVED, npcIds);
	}
	
	public final void notifyNodeArrived(L2Npc npc)
	{
		try
		{
			onNodeArrived(npc);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on onNodeArrived() in notifyNodeArrived(): " + e.getMessage(), e);
		}
	}
	
	public void onNodeArrived(L2Npc npc)
	{
	}
	
	public void onMoveFinished(L2Npc npc)
	{
	}
	
	public void onRouteFinished(L2Npc npc)
	{
	}
	
	public L2Npc spawnNpc(int npcId, int x, int y, int z, int heading, int instId)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		Instance inst = InstanceManager.getInstance().getInstance(instId);
		try
		{
			L2Spawn npcSpawn = new L2Spawn(npcTemplate);
			npcSpawn.setX(x);
			npcSpawn.setY(y);
			npcSpawn.setZ(z);
			npcSpawn.setHeading(heading);
			npcSpawn.setAmount(1);
			npcSpawn.setInstanceId(instId);
			SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
			L2Npc npc = npcSpawn.spawnOne(false);
			inst.addNpc(npc);
			return npc;
		}
		catch (Exception ignored)
		{
		}
		return null;
	}
	
	public L2Npc spawnNpc(int npcId, Location loc, int heading, int instId)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		Instance inst = InstanceManager.getInstance().getInstance(instId);
		try
		{
			L2Spawn npcSpawn = new L2Spawn(npcTemplate);
			npcSpawn.setX(loc.getX());
			npcSpawn.setY(loc.getY());
			npcSpawn.setZ(loc.getZ());
			npcSpawn.setHeading(loc.getHeading());
			npcSpawn.setAmount(1);
			npcSpawn.setInstanceId(instId);
			SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
			L2Npc npc = npcSpawn.spawnOne(false);
			inst.addNpc(npc);
			return npc;
		}
		catch (Exception ignored)
		{
		}
		return null;
	}
	
	public L2Npc spawnNpc(int npcId, Location loc, int heading)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		try
		{
			L2Spawn npcSpawn = new L2Spawn(npcTemplate);
			npcSpawn.setX(loc.getX());
			npcSpawn.setY(loc.getY());
			npcSpawn.setZ(loc.getZ());
			npcSpawn.setHeading(loc.getHeading());
			npcSpawn.setAmount(1);
			SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
			L2Npc npc = npcSpawn.spawnOne(false);
			return npc;
		}
		catch (Exception ignored)
		{
		}
		return null;
	}
	
	public boolean hasAtLeastOneQuestItem(L2PcInstance player, int... itemIds)
	{
		final PcInventory inv = player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) != null)
			{
				return true;
			}
		}
		return false;
	}
	
	public static final void specialCamera(L2PcInstance player, L2Character creature, int force, int angle1, int angle2, int time, int range, int duration, int relYaw, int relPitch, int isWide, int relAngle)
	{
		player.sendPacket(new SpecialCamera(creature, force, angle1, angle2, time, range, duration, relYaw, relPitch, isWide, relAngle));
	}
	
	public static final void specialCameraEx(L2PcInstance player, L2Character creature, int force, int angle1, int angle2, int time, int duration, int relYaw, int relPitch, int isWide, int relAngle)
	{
		player.sendPacket(new SpecialCamera(creature, player, force, angle1, angle2, time, duration, relYaw, relPitch, isWide, relAngle));
	}
	
	public static final void specialCamera3(L2PcInstance player, L2Character creature, int force, int angle1, int angle2, int time, int range, int duration, int relYaw, int relPitch, int isWide, int relAngle, int unk)
	{
		player.sendPacket(new SpecialCamera(creature, force, angle1, angle2, time, range, duration, relYaw, relPitch, isWide, relAngle, unk));
	}
}