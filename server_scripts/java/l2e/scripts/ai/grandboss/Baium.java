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
package l2e.scripts.ai.grandboss;

import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import javolution.util.FastList;
import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.instancemanager.GrandBossManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.MountType;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2DecoyInstance;
import l2e.gameserver.model.actor.instance.L2GrandBossInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.quest.QuestTimer;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.type.L2BossZone;
import l2e.gameserver.network.serverpackets.Earthquake;
import l2e.gameserver.network.serverpackets.MoveToPawn;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.util.Util;

public class Baium extends AbstractNpcAI
{
	private static final int STONE_BAIUM = 29025;
	private static final int ANGELIC_VORTEX = 31862;
	private static final int LIVE_BAIUM = 29020;
	private static final int ARCHANGEL = 29021;
	private static final int TELEPORT_CUBIC = 31842;

	private static final int BLOODED_FABRIC = 4295;

	private static final byte ASLEEP = 0;
	private static final byte AWAKE = 1;
	private static final byte DEAD = 2;

	private static final Location[] ANGEL_LOCATION =
	{
		new Location(114239, 17168, 10080, 63544),
		new Location(115780, 15564, 10080, 13620),
		new Location(114880, 16236, 10080, 5400),
		new Location(115168, 17200, 10080, 0),
		new Location(115792, 16608, 10080, 0)
	};
	
	private static final SkillsHolder GENERAL_ATTACK = new SkillsHolder(4127, 1);
	private static final SkillsHolder WIND_OF_FORCE = new SkillsHolder(4128, 1);
	private static final SkillsHolder EARTHQUAKE = new SkillsHolder(4129, 1);
	private static final SkillsHolder STRIKING_OF_THUNDERBOLT = new SkillsHolder(4130, 1);
	private static final SkillsHolder STUN = new SkillsHolder(4131, 1);
	private static final SkillsHolder BAIUM_HEAL = new SkillsHolder(4135, 1);
	private static final SkillsHolder HINDER_STRIDER = new SkillsHolder(4258, 1);
	
	private long _LastAttackVsBaiumTime = 0;
	protected final List<L2Npc> _Minions = new ArrayList<>(5);
	private L2BossZone _Zone;
	
	private L2Character _target;
	private SkillsHolder _skill;
	
	private Baium(String name, String descr)
	{
		super(name, descr);

		registerMobs(LIVE_BAIUM);
		
		addStartNpc(STONE_BAIUM, ANGELIC_VORTEX, TELEPORT_CUBIC);
		addTalkId(STONE_BAIUM, ANGELIC_VORTEX, TELEPORT_CUBIC);
		
		_Zone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
		StatsSet info = GrandBossManager.getInstance().getStatsSet(LIVE_BAIUM);
		int status = GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM);
		if (status == DEAD)
		{
			long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			if (temp > 0)
			{
				startQuestTimer("baium_unlock", temp, null, null);
			}
			else
			{
				addSpawn(STONE_BAIUM, 116033, 17447, 10107, -25348, false, 0);
				GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, ASLEEP);
			}
		}
		else if (status == AWAKE)
		{
			int loc_x = info.getInteger("loc_x");
			int loc_y = info.getInteger("loc_y");
			int loc_z = info.getInteger("loc_z");
			int heading = info.getInteger("heading");
			final int hp = info.getInteger("currentHP");
			final int mp = info.getInteger("currentMP");
			L2GrandBossInstance baium = (L2GrandBossInstance) addSpawn(LIVE_BAIUM, loc_x, loc_y, loc_z, heading, false, 0);
			GrandBossManager.getInstance().addBoss(baium);
			final L2Npc _baium = baium;
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						_baium.setCurrentHpMp(hp, mp);
						_baium.setIsInvul(true);
						_baium.setIsImmobilized(true);
						_baium.setRunning();
						_baium.broadcastSocialAction(2);
						startQuestTimer("baium_wakeup", 15000, _baium, null);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}, 100L);
		}
		else
		{
			addSpawn(STONE_BAIUM, 116033, 17447, 10107, -25348, false, 0);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "baium_unlock":
			{
				GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, ASLEEP);
				addSpawn(STONE_BAIUM, 116033, 17447, 10107, -25348, false, 0);
				break;
			}
			case "skill_range":
			{
				if (npc != null)
				{
					callSkillAI(npc);
				}
				break;
			}
			case "clean_player":
			{
				_target = getRandomTarget(npc);
				break;
			}
			case "baium_wakeup":
			{
				if ((npc != null) && (npc.getId() == LIVE_BAIUM))
				{
					npc.broadcastSocialAction(1);
					npc.broadcastPacket(new Earthquake(npc.getX(), npc.getY(), npc.getZ(), 40, 5));
					npc.broadcastPacket(new PlaySound(1, "BS02_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));

					_LastAttackVsBaiumTime = System.currentTimeMillis();
					startQuestTimer("baium_despawn", 60000, npc, null, true);
					startQuestTimer("skill_range", 500, npc, null, true);
					final L2Npc baium = npc;
					ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								baium.setIsInvul(false);
								baium.setIsImmobilized(false);
								for (L2Npc minion : _Minions)
								{
									minion.setShowSummonAnimation(false);
								}
							}
							catch (Exception e)
							{
								_log.log(Level.WARNING, "", e);
							}
						}
					}, 11100L);
					
					for (Location loc : ANGEL_LOCATION)
					{
						L2Npc angel = addSpawn(ARCHANGEL, loc, false, 0, true);
						angel.setIsInvul(true);
						_Minions.add(angel);
					}
				}
				break;
			}
			case "baium_despawn":
			{
				if ((npc != null) && (npc.getId() == LIVE_BAIUM))
				{
					if (_Zone == null)
					{
						_Zone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
					}
					if ((_LastAttackVsBaiumTime + 1800000) < System.currentTimeMillis())
					{
						npc.deleteMe();
						for (L2Npc minion : _Minions)
						{
							if (minion != null)
							{
								minion.getSpawn().stopRespawn();
								minion.deleteMe();
							}
						}
						_Minions.clear();
						addSpawn(STONE_BAIUM, 116033, 17447, 10107, -25348, false, 0);
						GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, ASLEEP);
						_Zone.oustAllPlayers();
						cancelQuestTimer("baium_despawn", npc, null);
					}
					else if (((_LastAttackVsBaiumTime + 300000) < System.currentTimeMillis()) && (npc.getCurrentHp() < ((npc.getMaxHp() * 3) / 4.0)))
					{
						npc.setIsCastingNow(false);
						npc.setTarget(npc);
						if (npc.isPhysicalMuted())
						{
							return super.onAdvEvent(event, npc, player);
						}
						npc.doCast(BAIUM_HEAL.getSkill());
						npc.setIsCastingNow(true);
					}
					else if (!_Zone.isInsideZone(npc))
					{
						npc.teleToLocation(116033, 17447, 10104);
					}
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		if (_Zone == null)
		{
			_Zone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
		}
		if (_Zone == null)
		{
			return "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "Baium.ZONE_NULL") + "</body></html>";
		}
		
		switch (npc.getId())
		{
			case STONE_BAIUM:
			{
				if (GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM) == ASLEEP)
				{
					if (_Zone.isPlayerAllowed(player))
					{
						GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, AWAKE);
						npc.deleteMe();
						L2GrandBossInstance baium = (L2GrandBossInstance) addSpawn(LIVE_BAIUM, npc, true);
						GrandBossManager.getInstance().addBoss(baium);
						final L2Npc _baium = baium;
						ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
						{
							@Override
							public void run()
							{
								try
								{
									_baium.setIsInvul(true);
									_baium.setRunning();
									_baium.broadcastSocialAction(2);
									startQuestTimer("baium_wakeup", 15000, _baium, null);
									_baium.setShowSummonAnimation(false);
								}
								catch (Throwable e)
								{
									_log.log(Level.WARNING, "", e);
								}
							}
						}, 100L);
					}
					else
					{
						htmltext = "Conditions are not right to wake up Baium";
					}
				}
				break;
			}
			case ANGELIC_VORTEX:
			{
				if (player.isFlying())
				{
					return "<html><body>" + LocalizationStorage.getInstance().getString(player.getLang(), "Baium.PLAYER_FLY") + "</body></html>";
				}
				
				if ((GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM) == ASLEEP) && hasQuestItems(player, BLOODED_FABRIC))
				{
					takeItems(player, BLOODED_FABRIC, 1);
					_Zone.allowPlayerEntry(player, 30);
					player.teleToLocation(113100, 14500, 10077);
				}
				else
				{
					npc.showChatWindow(player, 1);
				}
				break;
			}
			case TELEPORT_CUBIC:
			{
				int x, y, z;
				switch (getRandom(3))
				{
					case 0:
						x = 108784 + getRandom(100);
						y = 16000 + getRandom(100);
						z = -4928;
						break;
					case 1:
						x = 113824 + getRandom(100);
						y = 10448 + getRandom(100);
						z = -5164;
						break;
					default:
						x = 115488 + getRandom(100);
						y = 22096 + getRandom(100);
						z = -5168;
						break;
				}
				player.teleToLocation(x, y, z);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return null;
		}
		callSkillAI(npc);
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.disableCoreAI(true);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (!_Zone.isInsideZone(attacker))
		{
			attacker.reduceCurrentHp(attacker.getCurrentHp(), attacker, false, false, null);
			return super.onAttack(npc, attacker, damage, isSummon);
		}
		if (npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return super.onAttack(npc, attacker, damage, isSummon);
		}
		
		if (attacker.getMountType() == MountType.STRIDER)
		{
			boolean hasStriderDebuff = false;
			for (L2Effect e : attacker.getAllEffects())
			{
				if (e.getSkill().getId() == HINDER_STRIDER.getSkillId())
				{
					hasStriderDebuff = true;
				}
			}
			if (!hasStriderDebuff)
			{
				npc.setTarget(attacker);
				if (npc.isMuted())
				{
					return super.onAttack(npc, attacker, damage, isSummon);
				}
				npc.doCast(HINDER_STRIDER.getSkill());
			}
		}
		_LastAttackVsBaiumTime = System.currentTimeMillis();
		callSkillAI(npc);
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		cancelQuestTimer("baium_despawn", npc, null);
		npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		addSpawn(TELEPORT_CUBIC, 115017, 15549, 10090, 0, false, 900000);
		long respawnTime = Config.BAIUM_SPAWN_INTERVAL + getRandom(-Config.BAIUM_SPAWN_RANDOM, Config.BAIUM_SPAWN_RANDOM);
		respawnTime *= 3600000;
		
		GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, DEAD);
		startQuestTimer("baium_unlock", respawnTime, null, null);
		StatsSet info = GrandBossManager.getInstance().getStatsSet(LIVE_BAIUM);
		info.set("respawn_time", (System.currentTimeMillis()) + respawnTime);
		GrandBossManager.getInstance().setStatsSet(LIVE_BAIUM, info);
		for (L2Npc minion : _Minions)
		{
			if (minion != null)
			{
				minion.getSpawn().stopRespawn();
				minion.deleteMe();
			}
		}
		_Minions.clear();
		final QuestTimer timer = getQuestTimer("skill_range", npc, null);
		if (timer != null)
		{
			timer.cancelAndRemove();
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public L2Character getRandomTarget(L2Npc npc)
	{
		FastList<L2Character> result = FastList.newInstance();
		Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
		{
			for (L2Object obj : objs)
			{
				if (obj.isPlayable() || (obj instanceof L2DecoyInstance))
				{
					if (obj.isPlayer())
					{
						if (obj.getActingPlayer().isInvisible())
						{
							continue;
						}
					}
					
					if (((((L2Character) obj).getZ() < (npc.getZ() - 100)) && (((L2Character) obj).getZ() > (npc.getZ() + 100))) || !(GeoClient.getInstance().canSeeTarget(((L2Character) obj).getX(), ((L2Character) obj).getY(), ((L2Character) obj).getZ(), npc.getX(), npc.getY(), npc.getZ())))
					{
						continue;
					}
				}
				if (obj.isPlayable() || (obj instanceof L2DecoyInstance))
				{
					if (Util.checkIfInRange(9000, npc, obj, true) && !((L2Character) obj).isDead())
					{
						result.add((L2Character) obj);
					}
				}
			}
		}
		if (result.isEmpty())
		{
			for (L2Npc minion : _Minions)
			{
				if (minion != null)
				{
					result.add(minion);
				}
			}
		}
		
		if (result.isEmpty())
		{
			FastList.recycle(result);
			return null;
		}
		
		Object[] characters = result.toArray();
		QuestTimer timer = getQuestTimer("clean_player", npc, null);
		if (timer != null)
		{
			timer.cancelAndRemove();
		}
		startQuestTimer("clean_player", 20000, npc, null);
		L2Character target = (L2Character) characters[getRandom(characters.length)];
		FastList.recycle(result);
		return target;
		
	}
	
	public synchronized void callSkillAI(L2Npc npc)
	{
		if (npc.isInvul() || npc.isCastingNow())
		{
			return;
		}
		
		if ((_target == null) || _target.isDead() || !(_Zone.isInsideZone(_target)))
		{
			_target = getRandomTarget(npc);
			if (_target != null)
			{
				_skill = getRandomSkill(npc);
			}
		}
		
		L2Character target = _target;
		SkillsHolder skill = _skill;
		if (skill == null)
		{
			skill = (getRandomSkill(npc));
		}
		
		if (npc.isPhysicalMuted())
		{
			return;
		}
		
		if ((target == null) || target.isDead() || !(_Zone.isInsideZone(target)))
		{
			npc.setIsCastingNow(false);
			return;
		}
		
		if (Util.checkIfInRange(skill.getSkill().getCastRange(), npc, target, true))
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			npc.setTarget(target);
			npc.setIsCastingNow(true);
			_target = null;
			_skill = null;
			if (getDist(skill.getSkill().getCastRange()) > 0)
			{
				npc.broadcastPacket(new MoveToPawn(npc, target, getDist(skill.getSkill().getCastRange())));
			}
			try
			{
				Thread.sleep(1000);
				npc.stopMove(null);
				npc.doCast(skill.getSkill());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			npc.getAI().setIntention(AI_INTENTION_FOLLOW, target, null);
			npc.setIsCastingNow(false);
		}
	}
	
	public SkillsHolder getRandomSkill(L2Npc npc)
	{
		SkillsHolder skill;
		if (npc.getCurrentHp() > ((npc.getMaxHp() * 3) / 4.0))
		{
			if (getRandom(100) < 10)
			{
				skill = WIND_OF_FORCE;
			}
			else if (getRandom(100) < 10)
			{
				skill = EARTHQUAKE;
			}
			else
			{
				skill = GENERAL_ATTACK;
			}
		}
		else if (npc.getCurrentHp() > ((npc.getMaxHp() * 2) / 4.0))
		{
			if (getRandom(100) < 10)
			{
				skill = STUN;
			}
			else if (getRandom(100) < 10)
			{
				skill = WIND_OF_FORCE;
			}
			else if (getRandom(100) < 10)
			{
				skill = EARTHQUAKE;
			}
			else
			{
				skill = GENERAL_ATTACK;
			}
		}
		else if (npc.getCurrentHp() > (npc.getMaxHp() / 4.0))
		{
			if (getRandom(100) < 10)
			{
				skill = STRIKING_OF_THUNDERBOLT;
			}
			else if (getRandom(100) < 10)
			{
				skill = STUN;
			}
			else if (getRandom(100) < 10)
			{
				skill = WIND_OF_FORCE;
			}
			else if (getRandom(100) < 10)
			{
				skill = EARTHQUAKE;
			}
			else
			{
				skill = GENERAL_ATTACK;
			}
		}
		else if (getRandom(100) < 10)
		{
			skill = STRIKING_OF_THUNDERBOLT;
		}
		else if (getRandom(100) < 10)
		{
			skill = STUN;
		}
		else if (getRandom(100) < 10)
		{
			skill = WIND_OF_FORCE;
		}
		else if (getRandom(100) < 10)
		{
			skill = EARTHQUAKE;
		}
		else
		{
			skill = GENERAL_ATTACK;
		}
		return skill;
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return null;
		}
		npc.setTarget(caster);
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
	
	public int getDist(int range)
	{
		int dist = 0;
		switch (range)
		{
			case -1:
				break;
			case 100:
				dist = 85;
				break;
			default:
				dist = range - 85;
				break;
		}
		return dist;
	}
	
	public static void main(String[] args)
	{
		new Baium(Baium.class.getSimpleName(), "ai");
	}
}