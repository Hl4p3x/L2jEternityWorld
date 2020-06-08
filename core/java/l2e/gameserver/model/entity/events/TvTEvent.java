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
package l2e.gameserver.model.entity.events;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.instancemanager.AntiFeedManager;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance.ConfirmDialogScripts;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.actor.instance.L2ServitorInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.ConfirmDlg;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.scripting.scriptengine.events.TvtKillEvent;
import l2e.gameserver.scripting.scriptengine.impl.L2Script.EventStage;
import l2e.gameserver.scripting.scriptengine.listeners.events.TvTListener;
import l2e.util.Rnd;
import l2e.util.StringUtil;

public class TvTEvent
{
	enum EventState
	{
		INACTIVE,
		INACTIVATING,
		PARTICIPATING,
		STARTING,
		STARTED,
		REWARDING
	}
	
	protected static final Logger _log = Logger.getLogger(TvTEvent.class.getName());
	
	private static final String htmlPath = "data/html/mods/TvTEvent/";
	public static TvTEventTeam[] _teams = new TvTEventTeam[2];
	private static EventState _state = EventState.INACTIVE;
	private static L2Spawn _npcSpawn = null;
	private static L2Npc _lastNpcSpawn = null;
	private static int _TvTEventInstance = 0;
	
	private static FastList<TvTListener> tvtListeners = new FastList<TvTListener>().shared();
	
	private TvTEvent()
	{
	}
	
	public static void init()
	{
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.TVT_ID);
		_teams[0] = new TvTEventTeam(Config.TVT_EVENT_TEAM_1_NAME, Config.TVT_EVENT_TEAM_1_COORDINATES);
		_teams[1] = new TvTEventTeam(Config.TVT_EVENT_TEAM_2_NAME, Config.TVT_EVENT_TEAM_2_COORDINATES);
	}
	
	public static boolean startParticipation()
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(Config.TVT_EVENT_PARTICIPATION_NPC_ID);
		
		if (tmpl == null)
		{
			_log.warning("TvTEventEngine[TvTEvent.startParticipation()]: L2NpcTemplate is a NullPointer -> Invalid npc id in configs?");
			return false;
		}
		
		try
		{
			_npcSpawn = new L2Spawn(tmpl);
			
			_npcSpawn.setX(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0]);
			_npcSpawn.setY(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1]);
			_npcSpawn.setZ(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3]);
			_npcSpawn.setRespawnDelay(1);
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			_npcSpawn.init();
			_lastNpcSpawn = _npcSpawn.getLastSpawn();
			_lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
			_lastNpcSpawn.setTitle("TvT Event Participation");
			_lastNpcSpawn.isAggressive();
			_lastNpcSpawn.decayMe();
			_lastNpcSpawn.spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_lastNpcSpawn, _lastNpcSpawn, 1034, 1, 1, 1));
			
			for (L2PcInstance pl : L2World.getInstance().getAllPlayersArray())
			{
				if ((pl != null) && pl.isOnline())
				{
					ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1.getId());
					confirm.addString("" + LocalizationStorage.getInstance().getString(pl.getLang(), "TvT.WANT_TO_REG") + "");
					confirm.addTime(30000);
					pl.CurrentConfirmDialog = ConfirmDialogScripts.Tvt;
					pl.sendPacket(confirm);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "TvTEventEngine[TvTEvent.startParticipation()]: exception: " + e.getMessage(), e);
			return false;
		}
		
		setState(EventState.PARTICIPATING);
		fireTvtEventListeners(EventStage.REGISTRATION_BEGIN);
		
		return true;
	}
	
	private static int highestLevelPcInstanceOf(Map<Integer, L2PcInstance> players)
	{
		int maxLevel = Integer.MIN_VALUE, maxLevelId = -1;
		for (L2PcInstance player : players.values())
		{
			if (player.getLevel() >= maxLevel)
			{
				maxLevel = player.getLevel();
				maxLevelId = player.getObjectId();
			}
		}
		return maxLevelId;
	}
	
	public static boolean startFight()
	{
		setState(EventState.STARTING);
		
		Map<Integer, L2PcInstance> allParticipants = new FastMap<>();
		allParticipants.putAll(_teams[0].getParticipatedPlayers());
		allParticipants.putAll(_teams[1].getParticipatedPlayers());
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		
		L2PcInstance player;
		Iterator<L2PcInstance> iter;
		if (needParticipationFee())
		{
			iter = allParticipants.values().iterator();
			while (iter.hasNext())
			{
				player = iter.next();
				if (!hasParticipationFee(player))
				{
					iter.remove();
				}
			}
		}
		
		int balance[] =
		{
			0,
			0
		}, priority = 0, highestLevelPlayerId;
		L2PcInstance highestLevelPlayer;
		
		while (!allParticipants.isEmpty())
		{
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			if (allParticipants.isEmpty())
			{
				break;
			}
			priority = 1 - priority;
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			priority = balance[0] > balance[1] ? 1 : 0;
		}
		
		if ((_teams[0].getParticipatedPlayerCount() < Config.TVT_EVENT_MIN_PLAYERS_IN_TEAMS) || (_teams[1].getParticipatedPlayerCount() < Config.TVT_EVENT_MIN_PLAYERS_IN_TEAMS))
		{
			setState(EventState.INACTIVE);
			_teams[0].cleanMe();
			_teams[1].cleanMe();
			unSpawnNpc();
			AntiFeedManager.getInstance().clear(AntiFeedManager.TVT_ID);
			return false;
		}
		
		if (needParticipationFee())
		{
			iter = _teams[0].getParticipatedPlayers().values().iterator();
			while (iter.hasNext())
			{
				player = iter.next();
				if (!payParticipationFee(player))
				{
					iter.remove();
				}
			}
			iter = _teams[1].getParticipatedPlayers().values().iterator();
			while (iter.hasNext())
			{
				player = iter.next();
				if (!payParticipationFee(player))
				{
					iter.remove();
				}
			}
		}
		
		if (Config.TVT_EVENT_IN_INSTANCE)
		{
			try
			{
				_TvTEventInstance = InstanceManager.getInstance().createDynamicInstance(Config.TVT_EVENT_INSTANCE_FILE);
				InstanceManager.getInstance().getInstance(_TvTEventInstance).setAllowSummon(false);
				InstanceManager.getInstance().getInstance(_TvTEventInstance).setPvPInstance(true);
				InstanceManager.getInstance().getInstance(_TvTEventInstance).setEmptyDestroyTime((Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY * 1000) + 60000L);
			}
			catch (Exception e)
			{
				_TvTEventInstance = 0;
				_log.log(Level.WARNING, "TvTEventEngine[TvTEvent.createDynamicInstance]: exception: " + e.getMessage(), e);
			}
		}
		openDoors(Config.TVT_DOORS_IDS_TO_OPEN);
		closeDoors(Config.TVT_DOORS_IDS_TO_CLOSE);
		setState(EventState.STARTED);
		
		for (TvTEventTeam team : _teams)
		{
			for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
			{
				if (playerInstance != null)
				{
					new TvTEventTeleporter(playerInstance, team.getCoordinates(), false, false);
				}
			}
		}
		fireTvtEventListeners(EventStage.START);
		
		return true;
	}
	
	public static String calculateRewards()
	{
		if (_teams[0].getPoints() == _teams[1].getPoints())
		{
			if ((_teams[0].getParticipatedPlayerCount() == 0) || (_teams[1].getParticipatedPlayerCount() == 0))
			{
				setState(EventState.REWARDING);
				CustomMessage msg = new CustomMessage("TVTEvent.INACTIVITY", true);
				return msg.toString();
			}
			
			CustomMessage msg1 = new CustomMessage("TVTEvent.TIED", true);
			sysMsgToAllParticipants(msg1.toString());
			
			if (Config.TVT_REWARD_TEAM_TIE)
			{
				rewardTeam(_teams[0]);
				rewardTeam(_teams[1]);
				CustomMessage msg = new CustomMessage("TVTEvent.TYING", true);
				return msg.toString();
			}
			
			CustomMessage msg2 = new CustomMessage("TVTEvent.TYING", true);
			return msg2.toString();
		}
		setState(EventState.REWARDING);
		
		TvTEventTeam team = _teams[_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1];
		rewardTeam(team);
		fireTvtEventListeners(EventStage.END);
		
		CustomMessage msg2 = new CustomMessage("TVTEvent.NORMAL_FINISH", true);
		msg2.add(team.getName());
		msg2.add(team.getPoints());
		
		return msg2.toString();
	}
	
	private static void rewardTeam(TvTEventTeam team)
	{
		for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
		{
			if (playerInstance == null)
			{
				continue;
			}
			
			SystemMessage systemMessage = null;
			
			for (int[] reward : Config.TVT_EVENT_REWARDS)
			{
				PcInventory inv = playerInstance.getInventory();
				
				if (ItemHolder.getInstance().createDummyItem(reward[0]).isStackable())
				{
					inv.addItem("TvT Event", reward[0], reward[1], playerInstance, playerInstance);
					
					if (reward[1] > 1)
					{
						systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
						systemMessage.addItemName(reward[0]);
						systemMessage.addItemNumber(reward[1]);
					}
					else
					{
						systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						systemMessage.addItemName(reward[0]);
					}
					
					playerInstance.sendPacket(systemMessage);
				}
				else
				{
					for (int i = 0; i < reward[1]; ++i)
					{
						inv.addItem("TvT Event", reward[0], 1, playerInstance, playerInstance);
						systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						systemMessage.addItemName(reward[0]);
						playerInstance.sendPacket(systemMessage);
					}
				}
			}
			StatusUpdate statusUpdate = new StatusUpdate(playerInstance);
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			
			statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, playerInstance.getCurrentLoad());
			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Reward.htm"));
			playerInstance.sendPacket(statusUpdate);
			playerInstance.sendPacket(npcHtmlMessage);
		}
	}
	
	public static void stopFight()
	{
		setState(EventState.INACTIVATING);
		unSpawnNpc();
		openDoors(Config.TVT_DOORS_IDS_TO_CLOSE);
		closeDoors(Config.TVT_DOORS_IDS_TO_OPEN);
		
		for (TvTEventTeam team : _teams)
		{
			for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
			{
				if (playerInstance != null)
				{
					new TvTEventTeleporter(playerInstance, Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES, false, false);
				}
			}
		}
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		setState(EventState.INACTIVE);
		AntiFeedManager.getInstance().clear(AntiFeedManager.TVT_ID);
	}
	
	public static synchronized boolean addParticipant(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
		{
			return false;
		}
		byte teamId = 0;
		
		if (_teams[0].getParticipatedPlayerCount() == _teams[1].getParticipatedPlayerCount())
		{
			teamId = (byte) (Rnd.get(2));
		}
		else
		{
			teamId = (byte) (_teams[0].getParticipatedPlayerCount() > _teams[1].getParticipatedPlayerCount() ? 1 : 0);
		}
		playerInstance.addEventListener(new TvTEventListener(playerInstance));
		return _teams[teamId].addPlayer(playerInstance);
	}
	
	public static boolean removeParticipant(int playerObjectId)
	{
		byte teamId = getParticipantTeamId(playerObjectId);
		
		if (teamId != -1)
		{
			_teams[teamId].removePlayer(playerObjectId);
			final L2PcInstance player = L2World.getInstance().getPlayer(playerObjectId);
			if (player != null)
			{
				player.removeEventListener(TvTEventListener.class);
			}
			return true;
		}
		
		return false;
	}
	
	public static boolean needParticipationFee()
	{
		return (Config.TVT_EVENT_PARTICIPATION_FEE[0] != 0) && (Config.TVT_EVENT_PARTICIPATION_FEE[1] != 0);
	}
	
	public static boolean hasParticipationFee(L2PcInstance playerInstance)
	{
		return playerInstance.getInventory().getInventoryItemCount(Config.TVT_EVENT_PARTICIPATION_FEE[0], -1) >= Config.TVT_EVENT_PARTICIPATION_FEE[1];
	}
	
	public static boolean payParticipationFee(L2PcInstance playerInstance)
	{
		return playerInstance.destroyItemByItemId("TvT Participation Fee", Config.TVT_EVENT_PARTICIPATION_FEE[0], Config.TVT_EVENT_PARTICIPATION_FEE[1], _lastNpcSpawn, true);
	}
	
	public static String getParticipationFee()
	{
		int itemId = Config.TVT_EVENT_PARTICIPATION_FEE[0];
		int itemNum = Config.TVT_EVENT_PARTICIPATION_FEE[1];
		
		if ((itemId == 0) || (itemNum == 0))
		{
			return "-";
		}
		
		return StringUtil.concat(String.valueOf(itemNum), " ", ItemHolder.getInstance().getTemplate(itemId).getName());
	}
	
	public static void sysMsgToAllParticipants(String message)
	{
		for (L2PcInstance playerInstance : _teams[0].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
			{
				playerInstance.sendMessage(message);
			}
		}
		
		for (L2PcInstance playerInstance : _teams[1].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
			{
				playerInstance.sendMessage(message);
			}
		}
	}
	
	private static void closeDoors(List<Integer> doors)
	{
		for (int doorId : doors)
		{
			L2DoorInstance doorInstance = DoorParser.getInstance().getDoor(doorId);
			
			if (doorInstance != null)
			{
				doorInstance.closeMe();
			}
		}
	}
	
	private static void openDoors(List<Integer> doors)
	{
		for (int doorId : doors)
		{
			L2DoorInstance doorInstance = DoorParser.getInstance().getDoor(doorId);
			
			if (doorInstance != null)
			{
				doorInstance.openMe();
			}
		}
	}
	
	private static void unSpawnNpc()
	{
		_lastNpcSpawn.deleteMe();
		SpawnTable.getInstance().deleteSpawn(_lastNpcSpawn.getSpawn(), false);
		_npcSpawn.stopRespawn();
		_npcSpawn = null;
		_lastNpcSpawn = null;
	}
	
	public static void onLogin(L2PcInstance playerInstance)
	{
		if ((playerInstance == null) || (!isStarting() && !isStarted()))
		{
			return;
		}
		
		byte teamId = getParticipantTeamId(playerInstance.getObjectId());
		
		if (teamId == -1)
		{
			return;
		}
		
		_teams[teamId].addPlayer(playerInstance);
		new TvTEventTeleporter(playerInstance, _teams[teamId].getCoordinates(), true, false);
	}
	
	public static void onLogout(L2PcInstance playerInstance)
	{
		if ((playerInstance != null) && (isStarting() || isStarted() || isParticipating()))
		{
			if (removeParticipant(playerInstance.getObjectId()))
			{
				playerInstance.setXYZInvisible((Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0] + Rnd.get(101)) - 50, (Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1] + Rnd.get(101)) - 50, Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			}
		}
	}
	
	public static synchronized void onBypass(String command, L2PcInstance playerInstance)
	{
		if ((playerInstance == null) || !isParticipating())
		{
			return;
		}
		
		final String htmContent;
		
		if (command.equals("tvt_event_participation"))
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			int playerLevel = playerInstance.getLevel();
			
			if (playerInstance.isCursedWeaponEquipped())
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "CursedWeaponEquipped.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if (OlympiadManager.getInstance().isRegistered(playerInstance))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Olympiad.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if (playerInstance.getKarma() > 0)
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Karma.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if ((playerLevel < Config.TVT_EVENT_MIN_LVL) || (playerLevel > Config.TVT_EVENT_MAX_LVL))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Level.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%min%", String.valueOf(Config.TVT_EVENT_MIN_LVL));
					npcHtmlMessage.replace("%max%", String.valueOf(Config.TVT_EVENT_MAX_LVL));
				}
			}
			else if ((_teams[0].getParticipatedPlayerCount() == Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS) && (_teams[1].getParticipatedPlayerCount() == Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "TeamsFull.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%max%", String.valueOf(Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS));
				}
			}
			else if ((Config.TVT_EVENT_MAX_PARTICIPANTS_PER_IP > 0) && !AntiFeedManager.getInstance().tryAddPlayer(AntiFeedManager.TVT_ID, playerInstance, Config.TVT_EVENT_MAX_PARTICIPANTS_PER_IP))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "IPRestriction.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%max%", String.valueOf(AntiFeedManager.getInstance().getLimit(playerInstance, Config.TVT_EVENT_MAX_PARTICIPANTS_PER_IP)));
				}
			}
			else if (needParticipationFee() && !hasParticipationFee(playerInstance))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "ParticipationFee.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%fee%", getParticipationFee());
				}
			}
			else if (addParticipant(playerInstance))
			{
				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Registered.htm"));
			}
			else
			{
				return;
			}
			
			playerInstance.sendPacket(npcHtmlMessage);
		}
		else if (command.equals("tvt_event_remove_participation"))
		{
			removeParticipant(playerInstance.getObjectId());
			if (Config.TVT_EVENT_MAX_PARTICIPANTS_PER_IP > 0)
			{
				AntiFeedManager.getInstance().removePlayer(AntiFeedManager.TVT_ID, playerInstance);
			}
			
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			
			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Unregistered.htm"));
			playerInstance.sendPacket(npcHtmlMessage);
		}
	}
	
	public static boolean onAction(L2PcInstance playerInstance, int targetedPlayerObjectId)
	{
		if ((playerInstance == null) || !isStarted())
		{
			return true;
		}
		
		if (playerInstance.isGM())
		{
			return true;
		}
		
		byte playerTeamId = getParticipantTeamId(playerInstance.getObjectId());
		byte targetedPlayerTeamId = getParticipantTeamId(targetedPlayerObjectId);
		
		if (((playerTeamId != -1) && (targetedPlayerTeamId == -1)) || ((playerTeamId == -1) && (targetedPlayerTeamId != -1)))
		{
			return false;
		}
		
		if ((playerTeamId != -1) && (targetedPlayerTeamId != -1) && (playerTeamId == targetedPlayerTeamId) && (playerInstance.getObjectId() != targetedPlayerObjectId) && !Config.TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED)
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean onScrollUse(int playerObjectId)
	{
		if (!isStarted())
		{
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_EVENT_SCROLL_ALLOWED)
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean onPotionUse(int playerObjectId)
	{
		if (!isStarted())
		{
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_EVENT_POTIONS_ALLOWED)
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean onEscapeUse(int playerObjectId)
	{
		if (!isStarted())
		{
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId))
		{
			return false;
		}
		
		return true;
	}
	
	public static boolean onItemSummon(int playerObjectId)
	{
		if (!isStarted())
		{
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_EVENT_SUMMON_BY_ITEM_ALLOWED)
		{
			return false;
		}
		
		return true;
	}
	
	public static void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance)
	{
		if ((killedPlayerInstance == null) || !isStarted())
		{
			return;
		}
		
		byte killedTeamId = getParticipantTeamId(killedPlayerInstance.getObjectId());
		
		if (killedTeamId == -1)
		{
			return;
		}
		
		new TvTEventTeleporter(killedPlayerInstance, _teams[killedTeamId].getCoordinates(), false, false);
		
		if (killerCharacter == null)
		{
			return;
		}
		
		L2PcInstance killerPlayerInstance = null;
		
		if ((killerCharacter instanceof L2PetInstance) || (killerCharacter instanceof L2ServitorInstance))
		{
			killerPlayerInstance = ((L2Summon) killerCharacter).getOwner();
			
			if (killerPlayerInstance == null)
			{
				return;
			}
		}
		else if (killerCharacter.isPlayer())
		{
			killerPlayerInstance = (L2PcInstance) killerCharacter;
		}
		else
		{
			return;
		}
		
		byte killerTeamId = getParticipantTeamId(killerPlayerInstance.getObjectId());
		
		if ((killerTeamId != -1) && (killedTeamId != -1) && (killerTeamId != killedTeamId))
		{
			TvTEventTeam killerTeam = _teams[killerTeamId];
			
			killerTeam.increasePoints();
			
			CreatureSay cs = new CreatureSay(killerPlayerInstance.getObjectId(), Say2.TELL, killerPlayerInstance.getName(), "I have killed " + killedPlayerInstance.getName() + "!");
			
			for (L2PcInstance playerInstance : _teams[killerTeamId].getParticipatedPlayers().values())
			{
				if (playerInstance != null)
				{
					playerInstance.sendPacket(cs);
				}
			}
			fireTvtKillListeners(killerPlayerInstance, killedPlayerInstance, killerTeam);
		}
	}
	
	public static void onTeleported(L2PcInstance playerInstance)
	{
		if (!isStarted() || (playerInstance == null) || !isPlayerParticipant(playerInstance.getObjectId()))
		{
			return;
		}
		
		if (playerInstance.isMageClass())
		{
			if ((Config.TVT_EVENT_MAGE_BUFFS != null) && !Config.TVT_EVENT_MAGE_BUFFS.isEmpty())
			{
				for (Entry<Integer, Integer> e : Config.TVT_EVENT_MAGE_BUFFS.entrySet())
				{
					L2Skill skill = SkillHolder.getInstance().getInfo(e.getKey(), e.getValue());
					if (skill != null)
					{
						skill.getEffects(playerInstance, playerInstance);
					}
				}
			}
		}
		else
		{
			if ((Config.TVT_EVENT_FIGHTER_BUFFS != null) && !Config.TVT_EVENT_FIGHTER_BUFFS.isEmpty())
			{
				for (Entry<Integer, Integer> e : Config.TVT_EVENT_FIGHTER_BUFFS.entrySet())
				{
					L2Skill skill = SkillHolder.getInstance().getInfo(e.getKey(), e.getValue());
					if (skill != null)
					{
						skill.getEffects(playerInstance, playerInstance);
					}
				}
			}
		}
	}
	
	public static final boolean checkForTvTSkill(L2PcInstance source, L2PcInstance target, L2Skill skill)
	{
		if (!isStarted())
		{
			return true;
		}
		
		final int sourcePlayerId = source.getObjectId();
		final int targetPlayerId = target.getObjectId();
		final boolean isSourceParticipant = isPlayerParticipant(sourcePlayerId);
		final boolean isTargetParticipant = isPlayerParticipant(targetPlayerId);
		
		if (!isSourceParticipant && !isTargetParticipant)
		{
			return true;
		}
		
		if (!(isSourceParticipant && isTargetParticipant))
		{
			return false;
		}
		
		if (getParticipantTeamId(sourcePlayerId) != getParticipantTeamId(targetPlayerId))
		{
			if (!skill.isOffensive())
			{
				return false;
			}
		}
		return true;
	}
	
	private static void setState(EventState state)
	{
		synchronized (_state)
		{
			_state = state;
		}
	}
	
	public static boolean isInactive()
	{
		boolean isInactive;
		
		synchronized (_state)
		{
			isInactive = _state == EventState.INACTIVE;
		}
		
		return isInactive;
	}
	
	public static boolean isInactivating()
	{
		boolean isInactivating;
		
		synchronized (_state)
		{
			isInactivating = _state == EventState.INACTIVATING;
		}
		
		return isInactivating;
	}
	
	public static boolean isParticipating()
	{
		boolean isParticipating;
		
		synchronized (_state)
		{
			isParticipating = _state == EventState.PARTICIPATING;
		}
		
		return isParticipating;
	}
	
	public static boolean isStarting()
	{
		boolean isStarting;
		
		synchronized (_state)
		{
			isStarting = _state == EventState.STARTING;
		}
		
		return isStarting;
	}
	
	public static boolean isStarted()
	{
		boolean isStarted;
		
		synchronized (_state)
		{
			isStarted = _state == EventState.STARTED;
		}
		
		return isStarted;
	}
	
	public static boolean isRewarding()
	{
		boolean isRewarding;
		
		synchronized (_state)
		{
			isRewarding = _state == EventState.REWARDING;
		}
		
		return isRewarding;
	}
	
	public static byte getParticipantTeamId(int playerObjectId)
	{
		return (byte) (_teams[0].containsPlayer(playerObjectId) ? 0 : (_teams[1].containsPlayer(playerObjectId) ? 1 : -1));
	}
	
	public static TvTEventTeam getParticipantTeam(int playerObjectId)
	{
		return (_teams[0].containsPlayer(playerObjectId) ? _teams[0] : (_teams[1].containsPlayer(playerObjectId) ? _teams[1] : null));
	}
	
	public static TvTEventTeam getParticipantEnemyTeam(int playerObjectId)
	{
		return (_teams[0].containsPlayer(playerObjectId) ? _teams[1] : (_teams[1].containsPlayer(playerObjectId) ? _teams[0] : null));
	}
	
	public static int[] getParticipantTeamCoordinates(int playerObjectId)
	{
		return _teams[0].containsPlayer(playerObjectId) ? _teams[0].getCoordinates() : (_teams[1].containsPlayer(playerObjectId) ? _teams[1].getCoordinates() : null);
	}
	
	public static boolean isPlayerParticipant(int playerObjectId)
	{
		if (!isParticipating() && !isStarting() && !isStarted())
		{
			return false;
		}
		
		return _teams[0].containsPlayer(playerObjectId) || _teams[1].containsPlayer(playerObjectId);
	}
	
	public static int getParticipatedPlayersCount()
	{
		if (!isParticipating() && !isStarting() && !isStarted())
		{
			return 0;
		}
		
		return _teams[0].getParticipatedPlayerCount() + _teams[1].getParticipatedPlayerCount();
	}
	
	public static String[] getTeamNames()
	{
		return new String[]
		{
			_teams[0].getName(),
			_teams[1].getName()
		};
	}
	
	public static int[] getTeamsPlayerCounts()
	{
		return new int[]
		{
			_teams[0].getParticipatedPlayerCount(),
			_teams[1].getParticipatedPlayerCount()
		};
	}
	
	public static int[] getTeamsPoints()
	{
		return new int[]
		{
			_teams[0].getPoints(),
			_teams[1].getPoints()
		};
	}
	
	public static int getTvTEventInstance()
	{
		return _TvTEventInstance;
	}
	
	private static void fireTvtKillListeners(L2PcInstance killer, L2PcInstance victim, TvTEventTeam killerTeam)
	{
		if (!tvtListeners.isEmpty() && (killer != null) && (victim != null) && (killerTeam != null))
		{
			TvtKillEvent event = new TvtKillEvent();
			event.setKiller(killer);
			event.setVictim(victim);
			event.setKillerTeam(killerTeam);
			for (TvTListener listener : tvtListeners)
			{
				listener.onKill(event);
			}
		}
	}
	
	private static void fireTvtEventListeners(EventStage stage)
	{
		if (!tvtListeners.isEmpty())
		{
			switch (stage)
			{
				case REGISTRATION_BEGIN:
				{
					for (TvTListener listener : tvtListeners)
					{
						listener.onRegistrationStart();
					}
					break;
				}
				case START:
				{
					for (TvTListener listener : tvtListeners)
					{
						listener.onBegin();
					}
					break;
				}
				case END:
				{
					for (TvTListener listener : tvtListeners)
					{
						listener.onEnd();
					}
					break;
				}
			}
		}
	}
	
	public static void addTvTListener(TvTListener listener)
	{
		if (!tvtListeners.contains(listener))
		{
			tvtListeners.add(listener);
		}
	}
	
	public static void removeTvtListener(TvTListener listener)
	{
		tvtListeners.remove(listener);
	}
}