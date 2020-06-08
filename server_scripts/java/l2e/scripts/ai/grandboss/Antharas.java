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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.instancemanager.GrandBossManager;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2GrandBossInstance;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.type.L2BossZone;
import l2e.gameserver.network.serverpackets.Earthquake;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SpecialCamera;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class Antharas extends AbstractNpcAI
{
	private static final int FWA_ACTIVITYTIMEOFANTHARAS = 120;
	private static final int FWA_INACTIVITYTIME = 900000;
	private static final boolean FWA_OLDANTHARAS = false;
	private static final boolean FWA_MOVEATRANDOM = true;
	private static final boolean FWA_DOSERVEREARTHQUAKE = true;
	private static final int FWA_LIMITOFWEAK = 45;
	private static final int FWA_LIMITOFNORMAL = 63;

	private static final int FWA_MAXMOBS = 10;
	private static final int FWA_INTERVALOFMOBSWEAK = 180000;
	private static final int FWA_INTERVALOFMOBSNORMAL = 150000;
	private static final int FWA_INTERVALOFMOBSSTRONG = 120000;
	private static final int FWA_PERCENTOFBEHEMOTH = 60;
	private static final int FWA_SELFDESTRUCTTIME = 15000;

	private final int _teleportCubeId = 31859;
	private final int _teleportCubeLocation[][] =
	{
		        {
		                        177615,
		                        114941,
		                        -7709,
		                        0
		}
	};

	protected List<L2Spawn> _teleportCubeSpawn = new FastList<>();
	protected List<L2Npc> _teleportCube = new FastList<>();

	protected Map<Integer, L2Spawn> _monsterSpawn = new FastMap<>();

	protected List<L2Npc> _monsters = new FastList<>();
	protected L2GrandBossInstance _antharas = null;

	private static final int ANTHARASOLDID = 29019;
	private static final int ANTHARASWEAKID = 29066;
	private static final int ANTHARASNORMALID = 29067;
	private static final int ANTHARASSTRONGID = 29068;

	protected ScheduledFuture<?> _cubeSpawnTask = null;
	protected ScheduledFuture<?> _monsterSpawnTask = null;
	protected ScheduledFuture<?> _activityCheckTask = null;
	protected ScheduledFuture<?> _socialTask = null;
	protected ScheduledFuture<?> _mobiliseTask = null;
	protected ScheduledFuture<?> _mobsSpawnTask = null;
	protected ScheduledFuture<?> _selfDestructionTask = null;
	protected ScheduledFuture<?> _moveAtRandomTask = null;
	protected ScheduledFuture<?> _movieTask = null;

	private static final byte DORMANT = 0;
	private static final byte WAITING = 1;
	private static final byte FIGHTING = 2;
	private static final byte DEAD = 3;

	protected static long _LastAction = 0;

	protected static L2BossZone _Zone;

	private Antharas(String name, String descr)
	{
		super(name, descr);

		int[] mob =
		{
		                ANTHARASOLDID,
		                ANTHARASWEAKID,
		                ANTHARASNORMALID,
		                ANTHARASSTRONGID,
		                29069,
		                29070,
		                29071,
		                29072,
		                29073,
		                29074,
		                29075,
		                29076
		};
		registerMobs(mob);
		init();
	}

	private void init()
	{
		try
		{
			_Zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);
			L2NpcTemplate template1;
			L2Spawn tempSpawn;

			template1 = NpcTable.getInstance().getTemplate(ANTHARASOLDID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setX(181323);
			tempSpawn.setY(114850);
			tempSpawn.setZ(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29019, tempSpawn);

			template1 = NpcTable.getInstance().getTemplate(ANTHARASWEAKID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setX(181323);
			tempSpawn.setY(114850);
			tempSpawn.setZ(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29066, tempSpawn);

			template1 = NpcTable.getInstance().getTemplate(ANTHARASNORMALID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setX(181323);
			tempSpawn.setY(114850);
			tempSpawn.setZ(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29067, tempSpawn);

			template1 = NpcTable.getInstance().getTemplate(ANTHARASSTRONGID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setX(181323);
			tempSpawn.setY(114850);
			tempSpawn.setZ(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29068, tempSpawn);
		}
		catch (Exception e)
		{
			_log.warning(e.getMessage());
		}

		try
		{
			L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(_teleportCubeId);
			L2Spawn spawnDat;
			for (int[] element : _teleportCubeLocation)
			{
				spawnDat = new L2Spawn(Cube);
				spawnDat.setAmount(1);
				spawnDat.setX(element[0]);
				spawnDat.setY(element[1]);
				spawnDat.setZ(element[2]);
				spawnDat.setHeading(element[3]);
				spawnDat.setRespawnDelay(60);
				spawnDat.setLocationId(0);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				_teleportCubeSpawn.add(spawnDat);
			}
		}
		catch (Exception e)
		{
			_log.warning(e.getMessage());
		}
		int status = GrandBossManager.getInstance().getBossStatus(ANTHARASOLDID);
		if (FWA_OLDANTHARAS || (status == WAITING))
		{
			StatsSet info = GrandBossManager.getInstance().getStatsSet(ANTHARASOLDID);
			Long respawnTime = info.getLong("respawn_time");
			if ((status == DEAD) && (respawnTime <= System.currentTimeMillis()))
			{
				GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, DORMANT);
				status = DORMANT;
			}
			else if (status == FIGHTING)
			{
				int loc_x = info.getInteger("loc_x");
				int loc_y = info.getInteger("loc_y");
				int loc_z = info.getInteger("loc_z");
				int heading = info.getInteger("heading");
				int hp = info.getInteger("currentHP");
				int mp = info.getInteger("currentMP");
				_antharas = (L2GrandBossInstance) addSpawn(ANTHARASOLDID, loc_x, loc_y, loc_z, heading, false, 0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp, mp);
				_LastAction = System.currentTimeMillis();
				_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if (status == DEAD)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(ANTHARASOLDID), respawnTime - System.currentTimeMillis());
			}
			else
			{
				setAntharasSpawnTask();
			}
		}
		else
		{
			int statusWeak = GrandBossManager.getInstance().getBossStatus(ANTHARASWEAKID);
			int statusNormal = GrandBossManager.getInstance().getBossStatus(ANTHARASNORMALID);
			int statusStrong = GrandBossManager.getInstance().getBossStatus(ANTHARASSTRONGID);
			int antharasId = 0;
			if ((statusWeak == FIGHTING) || (statusWeak == DEAD))
			{
				antharasId = ANTHARASWEAKID;
				status = statusWeak;
			}
			else if ((statusNormal == FIGHTING) || (statusNormal == DEAD))
			{
				antharasId = ANTHARASNORMALID;
				status = statusNormal;
			}
			else if ((statusStrong == FIGHTING) || (statusStrong == DEAD))
			{
				antharasId = ANTHARASSTRONGID;
				status = statusStrong;
			}
			if ((antharasId != 0) && (status == FIGHTING))
			{
				StatsSet info = GrandBossManager.getInstance().getStatsSet(antharasId);
				int loc_x = info.getInteger("loc_x");
				int loc_y = info.getInteger("loc_y");
				int loc_z = info.getInteger("loc_z");
				int heading = info.getInteger("heading");
				int hp = info.getInteger("currentHP");
				int mp = info.getInteger("currentMP");
				_antharas = (L2GrandBossInstance) addSpawn(antharasId, loc_x, loc_y, loc_z, heading, false, 0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp, mp);
				_LastAction = System.currentTimeMillis();
				_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if ((antharasId != 0) && (status == DEAD))
			{
				StatsSet info = GrandBossManager.getInstance().getStatsSet(antharasId);
				Long respawnTime = info.getLong("respawn_time");
				if (respawnTime <= System.currentTimeMillis())
				{
					GrandBossManager.getInstance().setBossStatus(antharasId, DORMANT);
					status = DORMANT;
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(antharasId), respawnTime - System.currentTimeMillis());
				}
			}
		}
	}

	public void spawnCube()
	{
		if (_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if (_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		if (_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}

		for (L2Spawn spawnDat : _teleportCubeSpawn)
		{
			_teleportCube.add(spawnDat.doSpawn());
		}
	}

	public void setAntharasSpawnTask()
	{
		if (_monsterSpawnTask == null)
		{
			synchronized (this)
			{
				if (_monsterSpawnTask == null)
				{
					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, WAITING);
					_monsterSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(1), (Config.ANTHARAS_WAIT_TIME * 60000));
				}
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("waiting"))
		{
			setAntharasSpawnTask();
		}

		return super.onAdvEvent(event, npc, player);
	}

	protected void startMinionSpawns(int antharasId)
	{
		int intervalOfMobs;

		switch (antharasId)
		{
			case ANTHARASWEAKID:
				intervalOfMobs = FWA_INTERVALOFMOBSWEAK;
				break;
			case ANTHARASNORMALID:
				intervalOfMobs = FWA_INTERVALOFMOBSNORMAL;
				break;
			default:
				intervalOfMobs = FWA_INTERVALOFMOBSSTRONG;
				break;
		}
		_mobsSpawnTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new MobsSpawn(), intervalOfMobs, intervalOfMobs);
	}

	private class AntharasSpawn implements Runnable
	{
		private int _taskId = 0;
		private final Collection<L2Character> _players = _Zone.getCharactersInside();

		public AntharasSpawn(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void run()
		{
			int npcId;
			L2Spawn antharasSpawn = null;

			switch (_taskId)
			{
				case 1:
					_monsterSpawnTask.cancel(false);
					_monsterSpawnTask = null;
					if (FWA_OLDANTHARAS)
					{
						npcId = 29019;
					}
					else if (_players.size() <= FWA_LIMITOFWEAK)
					{
						npcId = 29066;
					}
					else if (_players.size() > FWA_LIMITOFNORMAL)
					{
						npcId = 29068;
					}
					else
					{
						npcId = 29067;
					}
					antharasSpawn = _monsterSpawn.get(npcId);
					_antharas = (L2GrandBossInstance) antharasSpawn.doSpawn();
					GrandBossManager.getInstance().addBoss(_antharas);

					_monsters.add(_antharas);
					_antharas.setIsImmobilized(true);

					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, DORMANT);
					GrandBossManager.getInstance().setBossStatus(npcId, FIGHTING);
					_LastAction = System.currentTimeMillis();
					_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);

					if (!FWA_OLDANTHARAS)
					{
						startMinionSpawns(npcId);
					}

					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(2), 16);
					break;
				case 2:
					broadcastPacket(new SpecialCamera(_antharas, 700, 13, -19, 0, 10000, 20000, 0, 0, 0, 0, 0));

					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(3), 3000);
					break;

				case 3:
					broadcastPacket(new SpecialCamera(_antharas, 700, 13, 0, 6000, 10000, 20000, 0, 0, 0, 0, 0));
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(4), 10000);
					break;
				case 4:
					broadcastPacket(new SpecialCamera(_antharas, 3700, 0, -3, 0, 10000, 10000, 0, 0, 0, 0, 0));
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(5), 200);
					break;

				case 5:
					broadcastPacket(new SpecialCamera(_antharas, 1100, 0, -3, 22000, 10000, 30000, 0, 0, 0, 0, 0));
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(6), 10800);
					break;

				case 6:
					broadcastPacket(new SpecialCamera(_antharas, 1100, 0, -3, 300, 10000, 7000, 0, 0, 0, 0, 0));
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(7), 1900);
					break;

				case 7:
					_antharas.abortCast();

					_mobiliseTask = ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(_antharas), 16);

					if (FWA_MOVEATRANDOM)
					{
						Location pos = new Location(getRandom(175000, 178500), getRandom(112400, 116000), -7707, 0);
						_moveAtRandomTask = ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(_antharas, pos), 500);
					}

					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					break;
			}
		}
	}

	protected void broadcastPacket(L2GameServerPacket mov)
	{
		if (_Zone != null)
		{
			for (L2Character characters : _Zone.getCharactersInside())
			{
				if (characters.isPlayer())
				{
					characters.sendPacket(mov);
				}
			}
		}
	}

	private class MobsSpawn implements Runnable
	{
		public MobsSpawn()
		{
		}

		@Override
		public void run()
		{
			L2NpcTemplate template1;
			L2Spawn tempSpawn;
			boolean isBehemoth = getRandom(100) < FWA_PERCENTOFBEHEMOTH;
			try
			{
				int mobNumber = (isBehemoth ? 2 : 3);
				for (int i = 0; i < mobNumber; i++)
				{
					if (_monsters.size() >= FWA_MAXMOBS)
					{
						break;
					}
					int npcId;
					if (isBehemoth)
					{
						npcId = 29069;
					}
					else
					{
						npcId = getRandom(29070, 29076);
					}
					template1 = NpcTable.getInstance().getTemplate(npcId);
					tempSpawn = new L2Spawn(template1);
					int tried = 0;
					boolean notFound = true;
					int x = 175000;
					int y = 112400;
					int dt = ((_antharas.getX() - x) * (_antharas.getX() - x)) + ((_antharas.getY() - y) * (_antharas.getY() - y));
					while ((tried++ < 25) && notFound)
					{
						int rx = getRandom(175000, 179900);
						int ry = getRandom(112400, 116000);
						int rdt = ((_antharas.getX() - rx) * (_antharas.getX() - rx)) + ((_antharas.getY() - ry) * (_antharas.getY() - ry));
						if (GeoClient.getInstance().canSeeTarget(_antharas.getX(), _antharas.getY(), -7704, rx, ry, -7704))
						{
							if (rdt < dt)
							{
								x = rx;
								y = ry;
								dt = rdt;
								if (rdt <= 900000)
								{
									notFound = false;
								}
							}
						}
					}
					tempSpawn.setX(x);
					tempSpawn.setY(y);
					tempSpawn.setZ(-7704);
					tempSpawn.setHeading(0);
					tempSpawn.setAmount(1);
					tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
					SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
					_monsters.add(tempSpawn.doSpawn());
				}
			}
			catch (Exception e)
			{
				_log.warning(e.getMessage());
			}
		}
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		switch (npc.getId())
		{
			case 29070:
			case 29071:
			case 29072:
			case 29073:
			case 29074:
			case 29075:
			case 29076:
				if ((_selfDestructionTask == null) && !npc.isDead())
				{
					_selfDestructionTask = ThreadPoolManager.getInstance().scheduleGeneral(new SelfDestructionOfBomber(npc), FWA_SELFDESTRUCTTIME);
				}
				break;
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}

	private class SelfDestructionOfBomber implements Runnable
	{
		private final L2Npc _bomber;

		public SelfDestructionOfBomber(L2Npc bomber)
		{
			_bomber = bomber;
		}

		@Override
		public void run()
		{
			L2Skill skill = null;
			switch (_bomber.getId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = SkillHolder.getInstance().getInfo(5097, 1);
					break;
				case 29076:
					skill = SkillHolder.getInstance().getInfo(5094, 1);
					break;
			}

			_bomber.doCast(skill);
			if (_selfDestructionTask != null)
			{
				_selfDestructionTask.cancel(false);
				_selfDestructionTask = null;
			}
		}
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (npc.isInvul())
		{
			return null;
		}
		else if ((skill != null) && ((skill.getId() == 5097) || (skill.getId() == 5094)))
		{
			switch (npc.getId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
				case 29076:
					npc.doDie(npc);
					break;
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}

	protected class CheckActivity implements Runnable
	{
		@Override
		public void run()
		{
			Long temp = (System.currentTimeMillis() - _LastAction);
			if (temp > FWA_INACTIVITYTIME)
			{
				GrandBossManager.getInstance().setBossStatus(_antharas.getId(), DORMANT);
				setUnspawn();
			}
		}
	}

	public void setUnspawn()
	{
		_Zone.oustAllPlayers();

		if (_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(true);
			_cubeSpawnTask = null;
		}
		if (_monsterSpawnTask != null)
		{
			_monsterSpawnTask.cancel(true);
			_monsterSpawnTask = null;
		}
		if (_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}
		if (_socialTask != null)
		{
			_socialTask.cancel(true);
			_socialTask = null;
		}
		if (_mobiliseTask != null)
		{
			_mobiliseTask.cancel(true);
			_mobiliseTask = null;
		}
		if (_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if (_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		if (_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(true);
			_moveAtRandomTask = null;
		}

		for (L2Npc mob : _monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_monsters.clear();

		for (L2Npc cube : _teleportCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_teleportCube.clear();
	}

	private class CubeSpawn implements Runnable
	{
		private final int _type;

		public CubeSpawn(int type)
		{
			_type = type;
		}

		@Override
		public void run()
		{
			if (_type == 0)
			{
				spawnCube();
				_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(1), 1800000);
			}
			else
			{
				setUnspawn();
			}
		}
	}

	private static class UnlockAntharas implements Runnable
	{
		private final int _bossId;

		public UnlockAntharas(int bossId)
		{
			_bossId = bossId;
		}

		@Override
		public void run()
		{
			GrandBossManager.getInstance().setBossStatus(_bossId, DORMANT);
			if (FWA_DOSERVEREARTHQUAKE)
			{
				for (L2PcInstance p : L2World.getInstance().getAllPlayersArray())
				{
					p.broadcastPacket(new Earthquake(185708, 114298, -8221, 20, 10));
				}
			}
		}
	}

	private class SetMobilised implements Runnable
	{
		private final L2GrandBossInstance _boss;

		public SetMobilised(L2GrandBossInstance boss)
		{
			_boss = boss;
		}

		@Override
		public void run()
		{
			_boss.setIsImmobilized(false);

			if (_socialTask != null)
			{
				_socialTask.cancel(true);
				_socialTask = null;
			}
		}
	}

	private static class MoveAtRandom implements Runnable
	{
		private final L2Npc _npc;
		private final Location _loc;

		public MoveAtRandom(L2Npc npc, Location loc)
		{
			_npc = npc;
			_loc = loc;
		}

		@Override
		public void run()
		{
			_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _loc);
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if ((npc.getId() == 29019) || (npc.getId() == 29066) || (npc.getId() == 29067) || (npc.getId() == 29068))
		{
			_LastAction = System.currentTimeMillis();
			if (GrandBossManager.getInstance().getBossStatus(_antharas.getId()) != FIGHTING)
			{
				_Zone.oustAllPlayers();
			}
			else if (!FWA_OLDANTHARAS && (_mobsSpawnTask == null))
			{
				startMinionSpawns(npc.getId());
			}
		}
		else if ((npc.getId() > 29069) && (npc.getId() < 29077) && (npc.getCurrentHp() <= damage))
		{
			L2Skill skill = null;
			switch (npc.getId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = SkillHolder.getInstance().getInfo(5097, 1);
					break;
				case 29076:
					skill = SkillHolder.getInstance().getInfo(5094, 1);
					break;
			}

			npc.doCast(skill);
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if ((npc.getId() == 29019) || (npc.getId() == 29066) || (npc.getId() == 29067) || (npc.getId() == 29068))
		{
			broadcastPacket(new SpecialCamera(_antharas, 1200, 20, -10, 0, 10000, 13000, 0, 0, 0, 0, 0));
			npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(0), 10000);
			GrandBossManager.getInstance().setBossStatus(npc.getId(), DEAD);
			long respawnTime = Config.ANTHARAS_SPAWN_INTERVAL + getRandom(-Config.ANTHARAS_SPAWN_RANDOM, Config.ANTHARAS_SPAWN_RANDOM);
			respawnTime *= 3600000;
			ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(npc.getId()), respawnTime);
			StatsSet info = GrandBossManager.getInstance().getStatsSet(npc.getId());
			info.set("respawn_time", (System.currentTimeMillis() + respawnTime));
			GrandBossManager.getInstance().setStatsSet(npc.getId(), info);
		}
		else if (npc.getId() == 29069)
		{
			int countHPHerb = getRandom(6, 18);
			int countMPHerb = getRandom(6, 18);
			for (int i = 0; i < countHPHerb; i++)
			{
				((L2MonsterInstance) npc).dropItem(killer, 8602, 1);
			}
			for (int i = 0; i < countMPHerb; i++)
			{
				((L2MonsterInstance) npc).dropItem(killer, 8605, 1);
			}
		}
		if (_monsters.contains(npc))
		{
			_monsters.remove(npc);
		}
		return super.onKill(npc, killer, isSummon);
	}

	public static void main(String[] args)
	{
		new Antharas(Antharas.class.getSimpleName(), "ai");
	}
}
