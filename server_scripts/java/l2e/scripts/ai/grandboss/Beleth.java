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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.instancemanager.GrandBossManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.network.serverpackets.DoorStatusUpdate;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SpecialCamera;
import l2e.gameserver.network.serverpackets.StaticObject;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.npc.AbstractNpcAI;
import l2e.util.Rnd;

/**
 * Create by LordWinter 27.03.2014 Based on L2J Eternity-World
 */
public class Beleth extends AbstractNpcAI
{
	protected static L2Npc camera1, camera2, camera3, camera4;
	protected static L2Npc beleth, vortex, priest;
	protected static L2ZoneType _zone = null;
	private static L2PcInstance beleth_killer;
	private static boolean debug = false;
	protected static boolean movie = false;
	protected static FastList<L2Npc> minions = new FastList<>();
	private static List<Location> _spawnBelethLocs = new ArrayList<>();
	private static SkillsHolder Bleed = new SkillsHolder(5495, 1);
	private static SkillsHolder FireBall = new SkillsHolder(5496, 1);
	private static SkillsHolder HornOfRising = new SkillsHolder(5497, 1);
	private static SkillsHolder Lightening = new SkillsHolder(5499, 1);
	private static SkillsHolder Direction1 = new SkillsHolder(5531, 1);
	private static SkillsHolder Direction2 = new SkillsHolder(5532, 1);
	private static SkillsHolder Direction3 = new SkillsHolder(5533, 1);

	private static final int BELETH = 29118;
	private static final int BELETH_CLONE = 29119;
	private static final int BELETH_DOOR = 20240001;

	private static final byte DORMANT = 0;
	private static final byte WAITING = 1;
	private static final byte FIGHTING = 2;
	private static final byte DEAD = 3;

	protected static Map<L2Npc, Location> _clonesLoc = new ConcurrentHashMap<>();
	protected static Map<L2Npc, Integer> _respawnTaskId = new ConcurrentHashMap<>();
	protected static Map<Integer, L2Npc> _taskId = new ConcurrentHashMap<>();
	private static Location[] _cloneLoc = new Location[56];

	private static final int controlX = 16325;
	private static final int controlY = 213135;
	private static final int controlZ = -9353;
	protected static ScheduledFuture<?>[] _cloneRespawnTask = new ScheduledFuture<?>[56];

	private Beleth(String name, String descr)
	{
		super(name, descr);

		_zone = ZoneManager.getInstance().getZoneById(12018);
		addEnterZoneId(12018);
		registerMobs(BELETH, BELETH_CLONE);
		addSpawnId(BELETH, BELETH_CLONE);
		addStartNpc(32470);
		addTalkId(32470);
		addFirstTalkId(29128);
		StatsSet info = GrandBossManager.getInstance().getStatsSet(BELETH);
		int status = GrandBossManager.getInstance().getBossStatus(BELETH);
		if (status == DEAD)
		{
			long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			if (temp > 0)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new Unlock(), temp);
				DoorParser.getInstance().getDoor(BELETH_DOOR).closeMe();
			}
			else
			{
				GrandBossManager.getInstance().setBossStatus(BELETH, DORMANT);
				DoorParser.getInstance().getDoor(BELETH_DOOR).openMe();
			}
		}
		else
		{
			GrandBossManager.getInstance().setBossStatus(BELETH, DORMANT);
			DoorParser.getInstance().getDoor(BELETH_DOOR).openMe();
		}
	}

	protected static L2Npc spawn(int npcId, Location loc)
	{
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if (template != null)
			{
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(loc.getInstanceId());
				spawn.setLocation(loc);
				spawn.setHeading(loc.getHeading());
				spawn.setAmount(1);
				spawn.setRespawnDelay(0);
				return spawn.doSpawn();
			}
		}
		catch (Exception ignored)
		{
		}
		return null;
	}

	protected static class Unlock implements Runnable
	{
		@Override
		public void run()
		{
			GrandBossManager.getInstance().setBossStatus(BELETH, DORMANT);
			DoorParser.getInstance().getDoor(BELETH_DOOR).openMe();
		}
	}

	protected static class SkillUse implements Runnable
	{
		SkillsHolder _skill;
		L2Npc _npc;

		public SkillUse(SkillsHolder skill, L2Npc npc)
		{
			_skill = skill;
			_npc = npc;
		}

		@Override
		public void run()
		{
			if ((_npc != null) && !_npc.isDead() && !_npc.isCastingNow())
			{
				_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				_npc.doCast(_skill.getSkill());
			}
		}
	}

	protected static class SpawnTask implements Runnable
	{
		private int _taskId = 0;

		public SpawnTask(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void run()
		{
			try
			{
				int instanceId = 0;
				switch (_taskId)
				{
					case 1:
						movie = true;
						for (L2Character npc : _zone.getCharactersInside())
						{
							if (npc.isNpc())
							{
								npc.deleteMe();
							}
						}
						camera1 = spawn(29120, new Location(16323, 213142, -9357, 0, instanceId));
						camera1.setIsSpecialCamera(true);
						camera2 = spawn(29121, new Location(16323, 210741, -9357, 0, instanceId));
						camera2.setIsSpecialCamera(true);
						camera3 = spawn(29122, new Location(16323, 213170, -9357, 0, instanceId));
						camera3.setIsSpecialCamera(true);
						camera4 = spawn(29123, new Location(16323, 214917, -9356, 0, instanceId));
						camera4.setIsSpecialCamera(true);
						_zone.broadcastPacket(new PlaySound(1, "BS07_A", 1, camera1.getObjectId(), camera1.getX(), camera1.getY(), camera1.getZ()));
						showSocialActionMovie(camera1, 1700, 110, 50, 0, 2600, 0, 0, 1, 0, 0);
						showSocialActionMovie(camera1, 1700, 100, 50, 0, 2600, 0, 0, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 300);
						break;
					case 2:
						showSocialActionMovie(camera1, 1800, -65, 30, 6000, 5000, 0, 0, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 4900);
						break;
					case 3:
						showSocialActionMovie(camera1, 2200, -120, 30, 6000, 5000, 0, 0, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 4900);
						break;
					case 4:
						showSocialActionMovie(camera2, 2200, 130, 20, 1000, 1500, -20, 10, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 1400);
						break;
					case 5:
						showSocialActionMovie(camera2, 2300, 100, 10, 2000, 4500, 0, 10, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 2500);
						break;
					case 6:
						L2DoorInstance door = DoorParser.getInstance().getDoor(BELETH_DOOR);
						door.closeMe();
						_zone.broadcastPacket(new StaticObject(door, false));
						_zone.broadcastPacket(new DoorStatusUpdate(door));
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 1700);
						break;
					case 7:
						showSocialActionMovie(camera4, 1500, 210, 5, 0, 1500, 0, 0, 1, 0, 0);
						showSocialActionMovie(camera4, 900, 255, 5, 5000, 6500, 0, 10, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 6000);
						break;
					case 8:
						vortex = spawn(29125, new Location(16323, 214917, -9356, 0, instanceId));
						vortex.setIsSpecialCamera(true);
						showSocialActionMovie(camera4, 900, 255, 5, 0, 1500, 0, 5, 1, 0, 0);
						beleth = spawn(BELETH, new Location(16321, 214211, -9352, 49369, instanceId));
						beleth.setIsSpecialCamera(true);
						beleth.setShowSummonAnimation(true);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 1000);
						break;
					case 9:
						showSocialActionMovie(camera4, 1100, 255, 10, 7000, 19000, 0, 20, 1, 0, 0);
						_zone.broadcastPacket(new SocialAction(beleth.getObjectId(), 1));
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 4000);
						break;
					case 10:
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 200);
						break;
					case 11:
						for (int i = 0; i < 6; i++)
						{
							int x = (int) ((150 * Math.cos(i * 1.046666667)) + 16323);
							int y = (int) ((150 * Math.sin(i * 1.046666667)) + 213059);
							L2Npc minion = spawn(BELETH_CLONE, new Location(x, y, -9357, 49152, beleth.getInstanceId()));
							minion.setShowSummonAnimation(true);
							minion.setIsSpecialCamera(true);
							minion.decayMe();
							minions.add(minion);
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 6800);
						break;
					case 12:
						ThreadPoolManager.getInstance().scheduleGeneral(new DoActionBeleth(0, Direction1.getSkill()), 1000);
						showSocialActionMovie(beleth, 0, 270, 5, 0, 6000, 0, 0, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 5500);
						break;
					case 13:
						showSocialActionMovie(beleth, 800, 270, 10, 3000, 6000, 0, 0, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 5000);
						break;
					case 14:
						showSocialActionMovie(camera3, 100, 270, 15, 0, 5000, 0, 0, 1, 0, 0);
						showSocialActionMovie(camera3, 100, 270, 15, 0, 5000, 0, 0, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 100);
						break;
					case 15:
						showSocialActionMovie(camera3, 100, 270, 15, 3000, 6000, 0, 5, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 1400);
						break;
					case 16:
						beleth.teleToLocation(16323, 213059, -9357, 49152, false);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 200);
						break;
					case 17:
						ThreadPoolManager.getInstance().scheduleGeneral(new DoActionBeleth(0, Direction2.getSkill()), 100);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 2000);
						break;
					case 18:
						showSocialActionMovie(camera3, 700, 270, 20, 1500, 8000, 0, 0, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 6900);
						break;
					case 19:
						showSocialActionMovie(camera3, 40, 260, 15, 0, 4000, 0, 0, 1, 0, 0);
						for (L2Npc blth : minions)
						{
							blth.spawnMe();
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 3000);
						break;
					case 20:
						showSocialActionMovie(camera3, 40, 280, 15, 0, 4000, 5, 0, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 3000);
						break;
					case 21:
						showSocialActionMovie(camera3, 5, 250, 15, 0, 13300, 20, 15, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 1000);
						break;
					case 22:
						_zone.broadcastPacket(new SocialAction(beleth.getObjectId(), 3));
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 4000);
						break;
					case 23:
						ThreadPoolManager.getInstance().scheduleGeneral(new DoActionBeleth(0, Direction3.getSkill()), 100);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 6800);
						break;
					case 24:
						beleth.deleteMe();
						for (L2Npc bel : minions)
						{
							bel.deleteMe();
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(26), 10);
						ThreadPoolManager.getInstance().scheduleGeneral(new ShowBeleth(), Config.BELETH_SPAWN_DELAY * 60 * 1000);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 2000);
						break;
					case 25:
						minions.clear();
						camera1.deleteMe();
						camera2.deleteMe();
						camera3.deleteMe();
						camera4.deleteMe();
						movie = false;
						for (L2Npc clones : _clonesLoc.keySet())
						{
							_zone.broadcastPacket(new SocialAction(clones.getObjectId(), 0));
						}
						break;
					case 26:
						for (int i = 0; i < 56; i++)
						{
							spawnClone(i);
						}
						break;
					case 27:
						beleth = addSpawn(BELETH, 16323, 213170, -9357, 49152, false, 0, false);
						beleth.setIsInvul(true);
						beleth.setIsSpecialCamera(true);
						beleth.setIsImmobilized(true);
						beleth.disableAllSkills();
						priest = spawn(29128, new Location(beleth));
						priest.setIsSpecialCamera(true);
						priest.setShowSummonAnimation(true);
						priest.decayMe();
						break;
					case 28:
						beleth.doDie(null);
						camera1 = spawn(29122, new Location(16323, 213170, -9357, 0, instanceId));
						camera1.setIsSpecialCamera(true);
						camera1.broadcastPacket(new PlaySound(1, "BS07_D", 1, camera1.getObjectId(), camera1.getX(), camera1.getY(), camera1.getZ()));
						showSocialActionMovie(camera1, 400, 290, 25, 0, 10000, 0, 0, 1, 0, 0);
						showSocialActionMovie(camera1, 400, 290, 25, 0, 10000, 0, 0, 1, 0, 0);
						showSocialActionMovie(camera1, 400, 110, 25, 4000, 10000, 0, 0, 1, 0, 0);
						_zone.broadcastPacket(new SocialAction(beleth.getObjectId(), 5));
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 4000);
						break;
					case 29:
						showSocialActionMovie(camera1, 400, 295, 25, 4000, 5000, 0, 0, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 4500);
						break;
					case 30:
						showSocialActionMovie(camera1, 400, 295, 10, 4000, 11000, 0, 25, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 9000);
						break;
					case 31:
						vortex.deleteMe();
						showSocialActionMovie(camera1, 250, 90, 25, 0, 1000, 0, 0, 1, 0, 0);
						showSocialActionMovie(camera1, 250, 90, 35, 0, 10000, 0, 0, 1, 0, 0);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 2000);
						break;
					case 32:
						priest.spawnMe();
						beleth.deleteMe();
						camera2 = spawn(29121, new Location(14056, 213170, -9357, 0, instanceId));
						camera2.setIsSpecialCamera(true);
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 3500);
						break;
					case 33:
						showSocialActionMovie(camera2, 800, 180, 0, 0, 4000, 0, 10, 1, 0, 0);
						showSocialActionMovie(camera2, 800, 180, 0, 0, 4000, 0, 10, 1, 0, 0);
						L2DoorInstance door2 = DoorParser.getInstance().getDoor(20240002);
						door2.openMe();
						_zone.broadcastPacket(new StaticObject(door2, false));
						_zone.broadcastPacket(new DoorStatusUpdate(door2));
						DoorParser.getInstance().getDoor(20240003).openMe();
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), 4000);
						break;
					case 34:
						camera1.deleteMe();
						camera2.deleteMe();
						movie = false;
						deleteAllClones();
						deleteBeleth();
						ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(_taskId + 1), Config.BELETH_ZONE_CLEAN_DELAY * 60 * 1000);
						break;
					case 35:
						DoorParser.getInstance().getDoor(20240002).closeMe();
						GrandBossManager.getInstance().getZone(12018).oustAllPlayers();
						break;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (((character.isPlayer()) && (GrandBossManager.getInstance().getBossStatus(BELETH) == WAITING)) || (debug && (GrandBossManager.getInstance().getBossStatus(BELETH) != FIGHTING) && (character.isPlayer())))
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(1), debug ? 10000 : 300000);
			GrandBossManager.getInstance().setBossStatus(BELETH, FIGHTING);
			initSpawnLocs();
		}
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if ((npc.getId() == BELETH) && (killer != null))
		{
			setBelethKiller(1, killer);
			GrandBossManager.getInstance().setBossStatus(BELETH, DEAD);
			long respawnTime = Config.BELETH_SPAWN_INTERVAL + getRandom(-Config.BELETH_SPAWN_RANDOM, Config.BELETH_SPAWN_RANDOM);
			respawnTime *= 3600000;
			StatsSet info = GrandBossManager.getInstance().getStatsSet(BELETH);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatsSet(BELETH, info);
			ThreadPoolManager.getInstance().scheduleGeneral(new Unlock(), respawnTime);
			deleteAllClones();
			beleth.deleteMe();
			movie = true;
			ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(27), 1000);
			spawn(32470, new Location(12470, 215607, -9381, 49152));
			ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(28), 1500);
		}

		if (npc.getId() == BELETH_CLONE)
		{
			_cloneRespawnTask[_respawnTaskId.get(npc)] = ThreadPoolManager.getInstance().scheduleGeneral(new CloneRespawnTask(npc, _respawnTaskId.get(npc)), Config.BELETH_CLONES_RESPAWN * 1000);
			_taskId.put(_respawnTaskId.get(npc), npc);
			ThreadPoolManager.getInstance().scheduleGeneral(new DeleteTask(_respawnTaskId.get(npc)), 60200);
		}
		return null;
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance player, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if ((npc != null) && !npc.isDead() && ((npc.getId() == BELETH) || (npc.getId() == BELETH_CLONE)) && !npc.isCastingNow() && skill.hasEffectType(L2EffectType.HEAL) && (getRandom(100) < 80))
		{
			npc.setTarget(player);
			npc.doCast(HornOfRising.getSkill());
		}
		return null;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if ((npc.getId() == BELETH) || (npc.getId() == BELETH_CLONE))
		{
			if (getRandom(100) < 40)
			{
				return null;
			}
			final double distance = Math.sqrt(npc.getPlanDistanceSq(attacker.getX(), attacker.getY()));
			if (((distance > 500) && (distance < 890)) || (getRandom(100) < 80))
			{
				for (L2Npc minion : minions)
				{
					if ((minion != null) && !minion.isDead() && Util.checkIfInRange(900, minion, attacker, false) && !minion.isCastingNow())
					{
						minion.setTarget(attacker);
						minion.doCast(FireBall.getSkill());
					}
				}
				if ((beleth != null) && !beleth.isDead() && Util.checkIfInRange(900, beleth, attacker, false) && !beleth.isCastingNow())
				{
					beleth.setTarget(attacker);
					beleth.doCast(FireBall.getSkill());
				}
			}
			else if (!npc.isDead() && !npc.isCastingNow())
			{
				if (!npc.getKnownList().getKnownPlayersInRadius(200).isEmpty())
				{
					npc.doCast(Lightening.getSkill());
					return null;
				}
				((L2Attackable) npc).clearAggroList();
			}
		}
		return null;
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if ((npc != null) && !npc.isDead() && ((npc.getId() == BELETH) || (npc.getId() == BELETH_CLONE)) && !npc.isCastingNow())
		{
			if ((player != null) && !player.isDead())
			{
				final double distance2 = Math.sqrt(npc.getPlanDistanceSq(player.getX(), player.getY()));
				if ((distance2 > 890) && !npc.isMovementDisabled())
				{
					npc.setTarget(player);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
					int speed = npc.isRunning() ? npc.getRunSpeed() : npc.getWalkSpeed();
					int time = (int) (((distance2 - 890) / speed) * 1000);
					ThreadPoolManager.getInstance().scheduleGeneral(new SkillUse(FireBall, npc), time);

				}
				else if (distance2 < 890)
				{
					npc.setTarget(player);
					npc.doCast(FireBall.getSkill());
				}
				return null;
			}
			if (getRandom(100) < 40)
			{
				if (!npc.getKnownList().getKnownPlayersInRadius(200).isEmpty())
				{
					npc.doCast(Lightening.getSkill());
					return null;
				}
			}
			for (L2PcInstance plr : npc.getKnownList().getKnownPlayersInRadius(950))
			{
				npc.setTarget(plr);
				npc.doCast(FireBall.getSkill());
				return null;
			}
			((L2Attackable) npc).clearAggroList();
		}
		return null;
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if ((npc != null) && !npc.isDead() && ((npc.getId() == BELETH) || (npc.getId() == BELETH_CLONE)) && !npc.isCastingNow() && !movie)
		{
			if (getRandom(100) < 40)
			{
				if (!npc.getKnownList().getKnownPlayersInRadius(200).isEmpty())
				{
					npc.doCast(Bleed.getSkill());
					return null;
				}
			}
			npc.setTarget(player);
			npc.doCast(FireBall.getSkill());
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getId() == BELETH_CLONE)
		{
			npc.setRandomAnimationEnabled(false);
			npc.setIsNoRndWalk(true);
			npc.setIsSpecialCamera(true);
			if (!movie && !npc.getKnownList().getKnownPlayersInRadius(300).isEmpty() && (getRandom(100) < 60))
			{
				npc.doCast(Bleed.getSkill());
			}
		}

		if (npc.getId() == BELETH)
		{
			npc.setRandomAnimationEnabled(true);
			npc.setIsNoRndWalk(true);
			npc.setIsSpecialCamera(true);
			if (!movie && !npc.getKnownList().getKnownPlayersInRadius(300).isEmpty() && (getRandom(100) < 60))
			{
				npc.doCast(Bleed.getSkill());
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final String html;
		if ((beleth_killer != null) && (player.getObjectId() == beleth_killer.getObjectId()))
		{
			player.addItem("Kill Beleth", 10314, 1, null, true);
			setBelethKiller(0, player);
			html = "32470a.htm";
		}
		else
		{
			html = "32470b.htm";
		}
		return HtmCache.getInstance().getHtm(player.getLang(), "data/html/default/" + html);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getId() == 29128)
		{
			player.teleToLocation(-24095, 251617, -3374);
			if (player.getSummon() != null)
			{
				player.getSummon().teleToLocation(-24095, 251617, -3374);
			}
		}
		return null;
	}

	protected static void setBelethKiller(int event, L2PcInstance killer)
	{
		if (event == 0)
		{
			beleth_killer = null;
		}
		else if (event == 1)
		{
			if (killer.getParty() != null)
			{
				if (killer.getParty().getCommandChannel() != null)
				{
					beleth_killer = killer.getParty().getCommandChannel().getLeader();
				}
				else
				{
					beleth_killer = killer.getParty().getLeader();
				}
			}
			else
			{
				beleth_killer = killer;
			}
		}
	}

	protected static void deleteAllClones()
	{
		if (_cloneRespawnTask != null)
		{
			for (int id : _taskId.keySet())
			{
				_respawnTaskId.remove(id);
				_cloneRespawnTask[id].cancel(false);
				_cloneRespawnTask = null;
				_respawnTaskId.clear();
			}
		}

		if (_clonesLoc != null)
		{
			for (L2Npc clonetodelete : _clonesLoc.keySet())
			{
				clonetodelete.abortCast();
				clonetodelete.setTarget(null);
				clonetodelete.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				clonetodelete.deleteMe();
			}
			_clonesLoc.clear();
		}
	}

	protected static void deleteBeleth()
	{
		if (beleth != null)
		{
			beleth.abortCast();
			beleth.setTarget(null);
			beleth.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			beleth.deleteMe();
		}

		if (vortex != null)
		{
			vortex.deleteMe();
		}

		if (camera1 != null)
		{
			camera1.deleteMe();
		}

		if (camera2 != null)
		{
			camera2.deleteMe();
		}

		if (camera3 != null)
		{
			camera3.deleteMe();
		}

		if (camera4 != null)
		{
			camera4.deleteMe();
		}
		_spawnBelethLocs.clear();
	}

	protected static class ShowBeleth implements Runnable
	{
		@Override
		public void run()
		{
			Location spawn = _spawnBelethLocs.get(Rnd.get(_spawnBelethLocs.size())).rnd(50, 100, true);
			beleth = addSpawn(BELETH, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading(), false, 0, false);
		}
	}

	protected static class DoActionBeleth implements Runnable
	{
		private final int _socialAction;
		private final L2Skill _skill;

		public DoActionBeleth(final int socialAction, final L2Skill skill)
		{
			_socialAction = socialAction;
			_skill = skill;
		}

		@Override
		public void run()
		{
			if (_socialAction > 0)
			{
				_zone.broadcastPacket(new SocialAction(beleth.getObjectId(), _socialAction));
			}
			if (_skill != null)
			{
				_zone.broadcastPacket(new MagicSkillUse(beleth, beleth, _skill.getId(), 1, _skill.getHitTime(), 1));
			}
		}
	}

	private static void spawnClone(int id)
	{
		L2Npc clone = spawn(BELETH_CLONE, new Location(_cloneLoc[id]._x, _cloneLoc[id]._y, controlZ, 49152));
		clone.setRandomAnimationEnabled(false);
		_zone.broadcastPacket(new SocialAction(clone.getObjectId(), 0));
		_clonesLoc.put(clone, clone.getLocation());
		_respawnTaskId.put(clone, id);
		_spawnBelethLocs.add(clone.getLocation());
	}

	protected static class CloneRespawnTask implements Runnable
	{
		private final L2Npc _npc;
		private final int _id;

		public CloneRespawnTask(L2Npc npc, int id)
		{
			_npc = npc;
			_id = id;
		}

		@Override
		public void run()
		{
			L2Npc nextclone = spawn(BELETH_CLONE, new Location(_clonesLoc.get(_npc)._x, _clonesLoc.get(_npc)._y, controlZ, 49152));
			_clonesLoc.put(nextclone, nextclone.getLocation());
			_clonesLoc.remove(_npc);
			_respawnTaskId.put(nextclone, _id);
			_respawnTaskId.remove(_npc);
		}
	}

	protected static class DeleteTask implements Runnable
	{
		private final int _id;

		public DeleteTask(int id)
		{
			_id = id;
		}

		@Override
		public void run()
		{
			_taskId.remove(_id);
		}
	}

	private static void initSpawnLocs()
	{
		double angle = Math.toRadians(22.5);
		int radius = 700;

		for (int i = 0; i < 16; i++)
		{
			if ((i % 2) == 0)
			{
				radius -= 50;
			}
			else
			{
				radius += 50;
			}
			_cloneLoc[i] = new Location(controlX + (int) (radius * Math.sin(i * angle)), controlY + (int) (radius * Math.cos(i * angle)), convertDegreeToClientHeading(270 - (i * 22.5)));
		}

		radius = 1340;
		angle = Math.asin(1 / Math.sqrt(3));
		int mulX = 1, mulY = 1, addH = 3;
		double decX = 1.0, decY = 1.0;
		for (int i = 0; i < 16; i++)
		{
			if ((i % 8) == 0)
			{
				mulX = 0;
			}
			else if (i < 8)
			{
				mulX = -1;
			}
			else
			{
				mulX = 1;
			}
			if ((i == 4) || (i == 12))
			{
				mulY = 0;
			}
			else if ((i > 4) && (i < 12))
			{
				mulY = -1;
			}
			else
			{
				mulY = 1;
			}
			if (((i % 8) == 1) || (i == 7) || (i == 15))
			{
				decX = 0.5;
			}
			else
			{
				decX = 1.0;
			}
			if (((i % 10) == 3) || (i == 5) || (i == 11))
			{
				decY = 0.5;
			}
			else
			{
				decY = 1.0;
			}
			if (((i + 2) % 4) == 0)
			{
				addH++;
			}
			_cloneLoc[i + 16] = new Location(controlX + (int) (radius * decX * mulX), controlY + (int) (radius * decY * mulY), convertDegreeToClientHeading(180 + (addH * 90)));
		}

		angle = Math.toRadians(22.5);
		radius = 1000;
		for (int i = 0; i < 16; i++)
		{
			if ((i % 2) == 0)
			{
				radius -= 70;
			}
			else
			{
				radius += 70;
			}
			_cloneLoc[i + 32] = new Location(controlX + (int) (radius * Math.sin(i * angle)), controlY + (int) (radius * Math.cos(i * angle)), _cloneLoc[i]._heading);
		}

		int order = 48;
		radius = 650;
		for (int i = 1; i < 16; i += 2)
		{
			if ((i == 1) || (i == 15))
			{
				_cloneLoc[order] = new Location(_cloneLoc[i]._x, _cloneLoc[i]._y + radius, _cloneLoc[i + 16]._heading);
			}
			else if ((i == 3) || (i == 5))
			{
				_cloneLoc[order] = new Location(_cloneLoc[i]._x + radius, _cloneLoc[i]._y, _cloneLoc[i]._heading);
			}
			else if ((i == 7) || (i == 9))
			{
				_cloneLoc[order] = new Location(_cloneLoc[i]._x, _cloneLoc[i]._y - radius, _cloneLoc[i + 16]._heading);
			}
			else if ((i == 11) || (i == 13))
			{
				_cloneLoc[order] = new Location(_cloneLoc[i]._x - radius, _cloneLoc[i]._y, _cloneLoc[i]._heading);
			}
			order++;
		}
	}

	protected static void showSocialActionMovie(final L2Character target, final int dist, final int yaw, final int pitch, final int time, final int duration, final int turn, final int rise, final int widescreen, int relAngle, final int unk)
	{
		if (target == null)
		{
			return;
		}
		final SpecialCamera movie = new SpecialCamera(target, dist, yaw, pitch, time, duration, turn, rise, widescreen, relAngle, unk);
		_zone.broadcastPacket(movie);
	}

	private static int convertDegreeToClientHeading(double degree)
	{
		if (degree < 0)
		{
			degree = 360 + degree;
		}
		return (int) (degree * 182.044444444);
	}

	public static void main(String[] args)
	{
		new Beleth(Beleth.class.getSimpleName(), "ai");
	}
}
