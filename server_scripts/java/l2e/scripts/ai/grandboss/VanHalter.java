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

import gnu.trove.map.hash.TIntObjectHashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.instancemanager.GrandBossManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2GrandBossInstance;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.type.L2BossZone;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public class VanHalter extends Quest
{
	protected static final Logger _log = Logger.getLogger(VanHalter.class.getName());

	protected TIntObjectHashMap<ArrayList<L2PcInstance>> _bleedingPlayers = new TIntObjectHashMap<>();

	protected ArrayList<L2Spawn> _royalGuardSpawn = new ArrayList<>();
	protected ArrayList<L2Spawn> _royalGuardCaptainSpawn = new ArrayList<>();
	protected ArrayList<L2Spawn> _royalGuardHelperSpawn = new ArrayList<>();
	protected ArrayList<L2Spawn> _triolRevelationSpawn = new ArrayList<>();
	protected ArrayList<L2Spawn> _triolRevelationAlive = new ArrayList<>();
	protected ArrayList<L2Spawn> _guardOfAltarSpawn = new ArrayList<>();
	protected TIntObjectHashMap<L2Spawn> _cameraMarkerSpawn = new TIntObjectHashMap<>();
	protected L2Spawn _ritualOfferingSpawn = null;
	protected L2Spawn _ritualSacrificeSpawn = null;
	protected L2Spawn _vanHalterSpawn = null;

	protected ArrayList<L2Npc> _royalGuard = new ArrayList<>();
	protected ArrayList<L2Npc> _royalGuardCaptain = new ArrayList<>();
	protected ArrayList<L2Npc> _royalGuardHepler = new ArrayList<>();
	protected ArrayList<L2Npc> _triolRevelation = new ArrayList<>();
	protected ArrayList<L2Npc> _guardOfAltar = new ArrayList<>();
	protected TIntObjectHashMap<L2Npc> _cameraMarker = new TIntObjectHashMap<>();
	protected ArrayList<L2DoorInstance> _doorOfAltar = new ArrayList<>();
	protected ArrayList<L2DoorInstance> _doorOfSacrifice = new ArrayList<>();
	protected L2Npc _ritualOffering = null;
	protected L2Npc _ritualSacrifice = null;
	protected L2GrandBossInstance _vanHalter = null;

	protected ScheduledFuture<?> _movieTask = null;
	protected ScheduledFuture<?> _closeDoorOfAltarTask = null;
	protected ScheduledFuture<?> _openDoorOfAltarTask = null;
	protected ScheduledFuture<?> _lockUpDoorOfAltarTask = null;
	protected ScheduledFuture<?> _callRoyalGuardHelperTask = null;
	protected ScheduledFuture<?> _timeUpTask = null;
	protected ScheduledFuture<?> _intervalTask = null;
	protected ScheduledFuture<?> _halterEscapeTask = null;
	protected ScheduledFuture<?> _setBleedTask = null;

	boolean _isLocked = false;
	boolean _isHalterSpawned = false;
	boolean _isSacrificeSpawned = false;
	boolean _isCaptainSpawned = false;
	boolean _isHelperCalled = false;

	private static final int ANDREAS_VAN_HALTER = 29062;
	private static final int ANDREAS_CAPTAIN = 22188;

	private static final int[] TRIOLS =
	{
	                32058,
	                32059,
	                32060,
	                32061,
	                32062,
	                32063,
	                32064,
	                32065,
	                32066
	};

	private static L2BossZone _zone;
	private static int _state = 0;
	private static final byte INTERVAL = 0;
	private static final byte ALIVE = 1;

	private static final String[] StrStatus =
	{
	                "INTERVAL",
	                "ALIVE"
	};

	public static void main(String[] args)
	{
		new VanHalter();
	}

	public VanHalter()
	{
		super(-1, "Andreas van Halter", "ai");

		addAttackId(ANDREAS_VAN_HALTER);

		addKillId(ANDREAS_VAN_HALTER);
		addKillId(ANDREAS_CAPTAIN);
		for (int Triol : TRIOLS)
		{
			addKillId(Triol);
		}
		init();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		int npcId = npc.getId();
		if (npcId == ANDREAS_VAN_HALTER)
		{
			int maxHp = npc.getMaxHp();
			double curHp = npc.getStatus().getCurrentHp();
			if (((curHp / maxHp) * 100) <= 20)
			{
				callRoyalGuardHelper();
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		int npcId = npc.getId();

		if (isIntInArray(npcId, TRIOLS))
		{
			removeBleeding(npcId);
			checkTriolRevelationDestroy();
		}

		if (npcId == ANDREAS_CAPTAIN)
		{
			checkRoyalGuardCaptainDestroy();
		}

		if (npcId == ANDREAS_VAN_HALTER)
		{
			StatsSet info = GrandBossManager.getInstance().getStatsSet(ANDREAS_VAN_HALTER);
			int interval = getRandom(Config.HPH_FIXINTERVALOFHALTER, Config.HPH_FIXINTERVALOFHALTER + Config.HPH_RANDOMINTERVALOFHALTER);
			info.set("respawn_time", (System.currentTimeMillis() + interval));
			GrandBossManager.getInstance().setStatsSet(ANDREAS_VAN_HALTER, info);
			GrandBossManager.getInstance().setBossStatus(ANDREAS_VAN_HALTER, INTERVAL);
			enterInterval();
		}
		return super.onKill(npc, killer, isSummon);
	}

	private String getState()
	{
		return StrStatus[_state];
	}

	public void init()
	{
		_zone = (L2BossZone) ZoneManager.getInstance().getZoneById(12014);
		_state = GrandBossManager.getInstance().getBossStatus(ANDREAS_VAN_HALTER);
		StatsSet info = GrandBossManager.getInstance().getStatsSet(ANDREAS_VAN_HALTER);

		_isLocked = false;
		_isCaptainSpawned = false;
		_isHelperCalled = false;
		_isHalterSpawned = false;

		_doorOfAltar.add(DoorParser.getInstance().getDoor(19160014));
		_doorOfAltar.add(DoorParser.getInstance().getDoor(19160015));
		openDoorOfAltar(true);
		_doorOfSacrifice.add(DoorParser.getInstance().getDoor(19160016));
		_doorOfSacrifice.add(DoorParser.getInstance().getDoor(19160017));
		closeDoorOfSacrifice();

		loadRoyalGuard();
		loadTriolRevelation();
		loadRoyalGuardCaptain();
		loadRoyalGuardHelper();
		loadGuardOfAltar();
		loadVanHalter();
		loadRitualOffering();
		loadRitualSacrifice();

		_cameraMarkerSpawn.clear();
		try
		{
			L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(13014);
			L2Spawn tempSpawn;

			tempSpawn = new L2Spawn(template1);
			tempSpawn.setX(-16397);
			tempSpawn.setY(-55200);
			tempSpawn.setZ(-10449);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(1, tempSpawn);

			tempSpawn = new L2Spawn(template1);
			tempSpawn.setX(-16397);
			tempSpawn.setY(-55200);
			tempSpawn.setZ(-10051);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(2, tempSpawn);

			tempSpawn = new L2Spawn(template1);
			tempSpawn.setX(-16397);
			tempSpawn.setY(-55200);
			tempSpawn.setZ(-9741);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(3, tempSpawn);

			tempSpawn = new L2Spawn(template1);
			tempSpawn.setX(-16397);
			tempSpawn.setY(-55200);
			tempSpawn.setZ(-9394);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(4, tempSpawn);

			tempSpawn = new L2Spawn(template1);
			tempSpawn.setX(-16397);
			tempSpawn.setY(-55197);
			tempSpawn.setZ(-8739);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(5, tempSpawn);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "VanHalterManager: Error in spawning mobs." + e.getMessage(), e);
		}

		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), Config.HPH_ACTIVITYTIMEOFHALTER);

		if (_setBleedTask != null)
		{
			_setBleedTask.cancel(false);
		}
		_setBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(), 2000);

		if (Config.DEBUG)
		{
			_log.info("VanHalterManager : State of High Priestess van Halter is " + getState() + ".");
		}
		if (_state == INTERVAL)
		{
			enterInterval();
		}
		else if (_state == ALIVE)
		{
			setupAltar();
			_vanHalter.setCurrentHpMp(info.getInteger("currentHP"), info.getInteger("currentMP"));
		}
	}

	protected void loadRoyalGuard()
	{
		_royalGuardSpawn.clear();

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
			statement.setInt(1, 22175);
			statement.setInt(2, 22176);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_royalGuardSpawn.add(spawnDat);
				}
				else
				{
					_log.warning("VanHalterManager.loadRoyalGuard: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadRoyalGuard: Loaded " + _royalGuardSpawn.size() + " Royal Guard spawn locations.");
			}
		}
		catch (Exception e)
		{
			_log.warning("VanHalterManager.loadRoyalGuard: Spawn could not be initialized: " + e);
		}
	}

	protected void spawnRoyalGuard()
	{
		if (!_royalGuard.isEmpty())
		{
			deleteRoyalGuard();
		}

		for (L2Spawn rgs : _royalGuardSpawn)
		{
			rgs.startRespawn();
			_royalGuard.add(rgs.doSpawn());
		}
	}

	protected void deleteRoyalGuard()
	{
		for (L2Npc rg : _royalGuard)
		{
			rg.getSpawn().stopRespawn();
			rg.deleteMe();
		}

		_royalGuard.clear();
	}

	protected void loadTriolRevelation()
	{
		_triolRevelationSpawn.clear();

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
			statement.setInt(1, 32058);
			statement.setInt(2, 32068);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_triolRevelationSpawn.add(spawnDat);
				}
				else
				{
					_log.warning("VanHalterManager.loadTriolRevelation: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();

			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadTriolRevelation: Loaded " + _triolRevelationSpawn.size() + " Triol's Revelation spawn locations.");
			}
		}
		catch (Exception e)
		{
			_log.warning("VanHalterManager.loadTriolRevelation: Spawn could not be initialized: " + e);
		}
	}

	protected void spawnTriolRevelation()
	{
		if (!_triolRevelation.isEmpty())
		{
			deleteTriolRevelation();
		}

		for (L2Spawn trs : _triolRevelationSpawn)
		{
			L2MonsterInstance triol = (L2MonsterInstance) trs.doSpawn();
			triol.setIsParalyzed(true);
			_triolRevelation.add(triol);
			if ((trs.getId() != 32067) && (trs.getId() != 32068))
			{
				_triolRevelationAlive.add(trs);
			}
		}
	}

	protected void deleteTriolRevelation()
	{
		for (L2Npc tr : _triolRevelation)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		_triolRevelation.clear();
		_bleedingPlayers.clear();
	}

	protected void loadRoyalGuardCaptain()
	{
		_royalGuardCaptainSpawn.clear();

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22188);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_royalGuardCaptainSpawn.add(spawnDat);
				}
				else
				{
					_log.warning("VanHalterManager.loadRoyalGuardCaptain: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();

			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadRoyalGuardCaptain: Loaded " + _royalGuardCaptainSpawn.size() + " Royal Guard Captain spawn locations.");
			}
		}
		catch (Exception e)
		{
			_log.warning("VanHalterManager.loadRoyalGuardCaptain: Spawn could not be initialized: " + e);
		}
	}

	protected void spawnRoyalGuardCaptain()
	{
		if (!_royalGuardCaptain.isEmpty())
		{
			deleteRoyalGuardCaptain();
		}

		for (L2Spawn trs : _royalGuardCaptainSpawn)
		{
			trs.startRespawn();
			_royalGuardCaptain.add(trs.doSpawn());
		}
		_isCaptainSpawned = true;
	}

	protected void deleteRoyalGuardCaptain()
	{
		for (L2Npc tr : _royalGuardCaptain)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}

		_royalGuardCaptain.clear();
	}

	protected void loadRoyalGuardHelper()
	{
		_royalGuardHelperSpawn.clear();

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22191);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_royalGuardHelperSpawn.add(spawnDat);
				}
				else
				{
					_log.warning("VanHalterManager.loadRoyalGuardHelper: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();

			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadRoyalGuardHelper: Loaded " + _royalGuardHelperSpawn.size() + " Royal Guard Helper spawn locations.");
			}
		}
		catch (Exception e)
		{
			_log.warning("VanHalterManager.loadRoyalGuardHelper: Spawn could not be initialized: " + e);
		}
	}

	protected void spawnRoyalGuardHepler()
	{
		for (L2Spawn trs : _royalGuardHelperSpawn)
		{
			trs.startRespawn();
			_royalGuardHepler.add(trs.doSpawn());
		}
	}

	protected void deleteRoyalGuardHepler()
	{
		for (L2Npc tr : _royalGuardHepler)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		_royalGuardHepler.clear();
	}

	protected void loadGuardOfAltar()
	{
		_guardOfAltarSpawn.clear();

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 32051);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_guardOfAltarSpawn.add(spawnDat);
				}
				else
				{
					_log.warning("VanHalterManager.loadGuardOfAltar: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();

			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadGuardOfAltar: Loaded " + _guardOfAltarSpawn.size() + " Guard Of Altar spawn locations.");
			}
		}
		catch (Exception e)
		{
			_log.warning("VanHalterManager.loadGuardOfAltar: Spawn could not be initialized: " + e);
		}
	}

	protected void spawnGuardOfAltar()
	{
		if (!_guardOfAltar.isEmpty())
		{
			deleteGuardOfAltar();
		}

		for (L2Spawn trs : _guardOfAltarSpawn)
		{
			trs.startRespawn();
			_guardOfAltar.add(trs.doSpawn());
		}
	}

	protected void deleteGuardOfAltar()
	{
		for (L2Npc tr : _guardOfAltar)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}

		_guardOfAltar.clear();
	}

	protected void loadVanHalter()
	{
		_vanHalterSpawn = null;

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 29062);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_vanHalterSpawn = spawnDat;
				}
				else
				{
					_log.warning("VanHalterManager.loadVanHalter: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();

			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadVanHalter: Loaded High Priestess van Halter spawn locations.");
			}
		}
		catch (Exception e)
		{
			_log.warning("VanHalterManager.loadVanHalter: Spawn could not be initialized: " + e);
		}
	}

	protected void spawnVanHalter()
	{
		_vanHalter = (L2GrandBossInstance) _vanHalterSpawn.doSpawn();
		_vanHalter.setIsImmobilized(true);
		_vanHalter.setIsInvul(true);
		_isHalterSpawned = true;
		GrandBossManager.getInstance().addBoss(_vanHalter);
	}

	protected void deleteVanHalter()
	{
		if (_vanHalter != null)
		{
			_vanHalter.setIsImmobilized(false);
			_vanHalter.setIsInvul(false);
			_vanHalter.getSpawn().stopRespawn();
			_vanHalter.deleteMe();
		}
	}

	protected void loadRitualOffering()
	{
		_ritualOfferingSpawn = null;

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 32038);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_ritualOfferingSpawn = spawnDat;
				}
				else
				{
					_log.warning("VanHalterManager.loadRitualOffering: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();

			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadRitualOffering: Loaded Ritual Offering spawn locations.");
			}
		}
		catch (Exception e)
		{
			_log.warning("VanHalterManager.loadRitualOffering: Spawn could not be initialized: " + e);
		}
	}

	protected void spawnRitualOffering()
	{
		_ritualOffering = _ritualOfferingSpawn.doSpawn();
		_ritualOffering.setIsImmobilized(true);
		_ritualOffering.setIsInvul(true);
		_ritualOffering.setIsParalyzed(true);
	}

	protected void deleteRitualOffering()
	{
		if (_ritualOffering != null)
		{
			_ritualOffering.setIsImmobilized(false);
			_ritualOffering.setIsInvul(false);
			_ritualOffering.setIsParalyzed(false);
			_ritualOffering.getSpawn().stopRespawn();
			_ritualOffering.deleteMe();
		}
	}

	protected void loadRitualSacrifice()
	{
		_ritualSacrificeSpawn = null;

		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22195);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_ritualSacrificeSpawn = spawnDat;
				}
				else
				{
					_log.warning("VanHalterManager.loadRitualSacrifice: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();

			if (Config.DEBUG)
			{
				_log.info("VanHalterManager.loadRitualSacrifice: Loaded Ritual Sacrifice spawn locations.");
			}
		}
		catch (Exception e)
		{
			_log.warning("VanHalterManager.loadRitualSacrifice: Spawn could not be initialized: " + e);
		}
	}

	protected void spawnRitualSacrifice()
	{
		_ritualSacrifice = _ritualSacrificeSpawn.doSpawn();
		_ritualSacrifice.setIsImmobilized(true);
		_ritualSacrifice.setIsInvul(true);
		_isSacrificeSpawned = true;
	}

	protected void deleteRitualSacrifice()
	{
		if (!_isSacrificeSpawned)
		{
			return;
		}

		_ritualSacrifice.getSpawn().stopRespawn();
		_ritualSacrifice.deleteMe();
		_isSacrificeSpawned = false;
	}

	protected void spawnCameraMarker()
	{
		_cameraMarker.clear();
		for (int i = 1; i <= _cameraMarkerSpawn.size(); i++)
		{
			_cameraMarker.put(i, _cameraMarkerSpawn.get(i).doSpawn());
			_cameraMarker.get(i).getSpawn().stopRespawn();
			_cameraMarker.get(i).setIsImmobilized(true);
		}
	}

	protected void deleteCameraMarker()
	{
		if (_cameraMarker.isEmpty())
		{
			return;
		}

		for (int i = 1; i <= _cameraMarker.size(); i++)
		{
			_cameraMarker.get(i).deleteMe();
		}
		_cameraMarker.clear();
	}

	protected void openDoorOfAltar(boolean loop)
	{
		for (L2DoorInstance door : _doorOfAltar)
		{
			try
			{
				door.openMe();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, e.getMessage(), e);
			}
		}

		if (loop)
		{
			_isLocked = false;

			if (_closeDoorOfAltarTask != null)
			{
				_closeDoorOfAltarTask.cancel(false);
			}
			_closeDoorOfAltarTask = null;
			_closeDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new CloseDoorOfAltar(), Config.HPH_INTERVALOFDOOROFALTER);
		}
		else
		{
			if (_closeDoorOfAltarTask != null)
			{
				_closeDoorOfAltarTask.cancel(false);
			}
			_closeDoorOfAltarTask = null;
		}
	}

	protected class OpenDoorOfAltar implements Runnable
	{
		@Override
		public void run()
		{
			openDoorOfAltar(true);
		}
	}

	protected void closeDoorOfAltar(boolean loop)
	{
		for (L2DoorInstance door : _doorOfAltar)
		{
			door.closeMe();
		}

		if (loop)
		{
			if (_openDoorOfAltarTask != null)
			{
				_openDoorOfAltarTask.cancel(false);
			}

			_openDoorOfAltarTask = null;
			_openDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new OpenDoorOfAltar(), Config.HPH_INTERVALOFDOOROFALTER);
		}
		else
		{
			if (_openDoorOfAltarTask != null)
			{
				_openDoorOfAltarTask.cancel(false);
			}
			_openDoorOfAltarTask = null;
		}
	}

	protected class CloseDoorOfAltar implements Runnable
	{
		@Override
		public void run()
		{
			closeDoorOfAltar(true);
		}
	}

	protected void openDoorOfSacrifice()
	{
		for (L2DoorInstance door : _doorOfSacrifice)
		{
			try
			{
				door.openMe();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	protected void closeDoorOfSacrifice()
	{
		for (L2DoorInstance door : _doorOfSacrifice)
		{
			try
			{
				door.closeMe();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	public void checkTriolRevelationDestroy()
	{
		if (_isCaptainSpawned)
		{
			return;
		}

		boolean isTriolRevelationDestroyed = true;
		for (L2Spawn tra : _triolRevelationAlive)
		{
			if (!tra.getLastSpawn().isDead())
			{
				isTriolRevelationDestroyed = false;
			}
		}

		if (isTriolRevelationDestroyed)
		{
			spawnRoyalGuardCaptain();
		}
	}

	public void checkRoyalGuardCaptainDestroy()
	{
		if (!_isHalterSpawned)
		{
			return;
		}

		deleteRoyalGuard();
		deleteRoyalGuardCaptain();
		spawnGuardOfAltar();
		openDoorOfSacrifice();

		CreatureSay cs = new CreatureSay(0, Say2.ALLIANCE, "", NpcStringId.THE_DOOR_TO_THE_3RD_FLOOR_OF_THE_ALTAR_IS_NOW_OPEN);
		for (L2PcInstance pc : getPlayersInside())
		{
			pc.sendPacket(cs);
		}

		_vanHalter.setIsImmobilized(true);
		_vanHalter.setIsInvul(true);
		spawnCameraMarker();

		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = null;

		_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(1), Config.HPH_APPTIMEOFHALTER);
	}

	protected void combatBeginning()
	{
		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), Config.HPH_FIGHTTIMEOFHALTER);

		TIntObjectHashMap<L2PcInstance> _targets = new TIntObjectHashMap<>();
		int i = 0;

		for (L2PcInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
		{
			i++;
			_targets.put(i, pc);
		}
		_vanHalter.reduceCurrentHp(1, _targets.get(getRandom(1, i)), null);
	}

	public void callRoyalGuardHelper()
	{
		if (!_isHelperCalled)
		{
			_isHelperCalled = true;
			_halterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(), 500);
			_callRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(), 1000);
		}
	}

	protected class CallRoyalGuardHelper implements Runnable
	{
		@Override
		public void run()
		{
			spawnRoyalGuardHepler();

			if ((_royalGuardHepler.size() <= Config.HPH_CALLROYALGUARDHELPERCOUNT) && !_vanHalter.isDead())
			{
				if (_callRoyalGuardHelperTask != null)
				{
					_callRoyalGuardHelperTask.cancel(false);
				}
				_callRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(), Config.HPH_CALLROYALGUARDHELPERINTERVAL);
			}
			else
			{
				if (_callRoyalGuardHelperTask != null)
				{
					_callRoyalGuardHelperTask.cancel(false);
				}
				_callRoyalGuardHelperTask = null;
			}
		}
	}

	protected class HalterEscape implements Runnable
	{
		@Override
		public void run()
		{
			if ((_royalGuardHepler.size() <= Config.HPH_CALLROYALGUARDHELPERCOUNT) && !_vanHalter.isDead())
			{
				if (!_vanHalter.isAfraid())
				{
					_vanHalter.getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
					if (_vanHalter.getZ() >= -10476)
					{
						Location pos = new Location(-16397, -53308, -10448, 0);
						if (!((_vanHalter.getX() == pos.getX()) && (_vanHalter.getY() == pos.getY())))
						{
							_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
						}
					}
					else if (_vanHalter.getX() >= -16397)
					{
						Location pos = new Location(-15548, -54830, -10475, 0);
						_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
					}
					else
					{
						Location pos = new Location(-17248, -54830, -10475, 0);
						_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
					}
				}
				if (_halterEscapeTask != null)
				{
					_halterEscapeTask.cancel(false);
				}
				_halterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(), 5000);
			}
			else
			{
				if (_halterEscapeTask != null)
				{
					_halterEscapeTask.cancel(false);
				}
				_halterEscapeTask = null;
			}
		}
	}

	protected void addBleeding()
	{
		L2Skill bleed = SkillHolder.getInstance().getInfo(4615, 12);

		for (L2Npc tr : _triolRevelation)
		{
			if ((tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()).size() == 0) || tr.isDead())
			{
				continue;
			}

			ArrayList<L2PcInstance> bpc = new ArrayList<>();

			for (L2PcInstance pc : tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()))
			{
				if (pc.getFirstEffect(bleed) == null)
				{
					bleed.getEffects(tr, pc);
					tr.broadcastPacket(new MagicSkillUse(tr, pc, bleed.getId(), 12, 1, 1));
				}

				bpc.add(pc);
			}
			_bleedingPlayers.remove(tr.getId());
			_bleedingPlayers.put(tr.getId(), bpc);
		}
	}

	public void removeBleeding(int npcId)
	{
		if (_bleedingPlayers.get(npcId) == null)
		{
			return;
		}
		for (L2PcInstance pc : _bleedingPlayers.get(npcId))
		{
			if (pc.getFirstEffect(L2EffectType.DMG_OVER_TIME) != null)
			{
				pc.stopEffects(L2EffectType.DMG_OVER_TIME);
			}
		}
		_bleedingPlayers.remove(npcId);
	}

	protected class Bleeding implements Runnable
	{
		@Override
		public void run()
		{
			addBleeding();

			if (_setBleedTask != null)
			{
				_setBleedTask.cancel(false);
			}
			_setBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(), 2000);
		}
	}

	public void enterInterval()
	{
		StatsSet info = GrandBossManager.getInstance().getStatsSet(ANDREAS_VAN_HALTER);

		if (_callRoyalGuardHelperTask != null)
		{
			_callRoyalGuardHelperTask.cancel(false);
		}
		_callRoyalGuardHelperTask = null;

		if (_closeDoorOfAltarTask != null)
		{
			_closeDoorOfAltarTask.cancel(false);
		}
		_closeDoorOfAltarTask = null;

		if (_halterEscapeTask != null)
		{
			_halterEscapeTask.cancel(false);
		}
		_halterEscapeTask = null;

		if (_intervalTask != null)
		{
			_intervalTask.cancel(false);
		}
		_intervalTask = null;

		if (_lockUpDoorOfAltarTask != null)
		{
			_lockUpDoorOfAltarTask.cancel(false);
		}
		_lockUpDoorOfAltarTask = null;

		if (_movieTask != null)
		{
			_movieTask.cancel(false);
		}
		_movieTask = null;

		if (_openDoorOfAltarTask != null)
		{
			_openDoorOfAltarTask.cancel(false);
		}
		_openDoorOfAltarTask = null;

		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = null;

		deleteVanHalter();
		deleteRoyalGuardHepler();
		deleteRoyalGuardCaptain();
		deleteRoyalGuard();
		deleteRitualOffering();
		deleteRitualSacrifice();
		deleteGuardOfAltar();

		if (_intervalTask != null)
		{
			_intervalTask.cancel(false);
		}

		if (_state != INTERVAL)
		{
			GrandBossManager.getInstance().setBossStatus(ANDREAS_VAN_HALTER, INTERVAL);
		}

		long respawntime = info.getLong("respawn_time");
		long currenttime = System.currentTimeMillis();
		if (respawntime > currenttime)
		{
			_intervalTask = ThreadPoolManager.getInstance().scheduleGeneral(new Interval(), respawntime - currenttime);

			Date dt = new Date(respawntime);
			_log.info("VanHalterManager : Next spawn date of High Priestess van Halter is " + dt + ".");
		}
		else
		{
			setupAltar();
		}
	}

	protected class Interval implements Runnable
	{
		@Override
		public void run()
		{
			setupAltar();
		}
	}

	public void setupAltar()
	{
		if (_callRoyalGuardHelperTask != null)
		{
			_callRoyalGuardHelperTask.cancel(false);
		}
		_callRoyalGuardHelperTask = null;

		if (_closeDoorOfAltarTask != null)
		{
			_closeDoorOfAltarTask.cancel(false);
		}
		_closeDoorOfAltarTask = null;

		if (_halterEscapeTask != null)
		{
			_halterEscapeTask.cancel(false);
		}
		_halterEscapeTask = null;

		if (_intervalTask != null)
		{
			_intervalTask.cancel(false);
		}
		_intervalTask = null;

		if (_lockUpDoorOfAltarTask != null)
		{
			_lockUpDoorOfAltarTask.cancel(false);
		}
		_lockUpDoorOfAltarTask = null;

		if (_movieTask != null)
		{
			_movieTask.cancel(false);
		}
		_movieTask = null;

		if (_openDoorOfAltarTask != null)
		{
			_openDoorOfAltarTask.cancel(false);
		}
		_openDoorOfAltarTask = null;

		if (_timeUpTask != null)
		{
			_timeUpTask.cancel(false);
		}
		_timeUpTask = null;

		deleteVanHalter();
		deleteTriolRevelation();
		deleteRoyalGuardHepler();
		deleteRoyalGuardCaptain();
		deleteRoyalGuard();
		deleteRitualSacrifice();
		deleteRitualOffering();
		deleteGuardOfAltar();
		deleteCameraMarker();

		_isLocked = false;
		_isCaptainSpawned = false;
		_isHelperCalled = false;
		_isHalterSpawned = false;

		_zone.oustAllPlayers();

		closeDoorOfSacrifice();
		openDoorOfAltar(true);

		spawnTriolRevelation();
		spawnRoyalGuard();
		spawnRitualOffering();
		spawnVanHalter();

		GrandBossManager.getInstance().setBossStatus(ANDREAS_VAN_HALTER, ALIVE);

		_timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), Config.HPH_ACTIVITYTIMEOFHALTER);
	}

	protected class TimeUp implements Runnable
	{
		@Override
		public void run()
		{
			enterInterval();
		}
	}

	private class Movie implements Runnable
	{
		private final int _distance = 6502500;
		private final int _taskId;
		private final List<L2PcInstance> _players = getPlayersInside();

		public Movie(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void run()
		{
			_vanHalter.setHeading(16384);
			_vanHalter.setTarget(_ritualOffering);

			switch (_taskId)
			{
				case 1:
					GrandBossManager.getInstance().setBossStatus(ANDREAS_VAN_HALTER, ALIVE);

					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 50, 90, 0, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(2), 16);

					break;

				case 2:
					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(5)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(5), 1842, 100, -3, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(3), 1);

					break;

				case 3:
					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(5)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(5), 1861, 97, -10, 1500, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(4), 1500);

					break;

				case 4:
					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(4)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(4), 1876, 97, 12, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(5), 1);

					break;

				case 5:
					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(4)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(4), 1839, 94, 0, 1500, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(6), 1500);

					break;

				case 6:
					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(3)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(3), 1872, 94, 15, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(7), 1);

					break;

				case 7:
					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(3)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(3), 1839, 92, 0, 1500, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(8), 1500);

					break;

				case 8:
					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(2)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(2), 1872, 92, 15, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(9), 1);

					break;

				case 9:
					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(2)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(2), 1839, 90, 5, 1500, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(10), 1500);

					break;

				case 10:
					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(1)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(1), 1872, 90, 5, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(11), 1);

					break;

				case 11:
					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_cameraMarker.get(1)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_cameraMarker.get(1), 2002, 90, 2, 1500, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(12), 2000);

					break;

				case 12:
					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 50, 90, 10, 0, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(13), 1000);

					break;

				case 13:
					L2Skill skill = SkillHolder.getInstance().getInfo(1168, 7);
					_ritualOffering.setIsInvul(false);
					_vanHalter.setTarget(_ritualOffering);
					_vanHalter.setIsImmobilized(false);
					_vanHalter.doCast(skill);
					_vanHalter.setIsImmobilized(true);

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(14), 4700);

					break;

				case 14:
					_ritualOffering.setIsInvul(false);
					_ritualOffering.reduceCurrentHp(_ritualOffering.getMaxHp() + 1, _vanHalter, null);

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(15), 4300);

					break;

				case 15:
					spawnRitualSacrifice();
					deleteRitualOffering();

					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 100, 90, 15, 1500, 15000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(16), 2000);

					break;

				case 16:
					for (L2PcInstance pc : _players)
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 5200, 90, -10, 9500, 6000);
						}
						else
						{
							pc.leaveMovieMode();
						}
					}

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(17), 6000);

					break;

				case 17:
					for (L2PcInstance pc : _players)
					{
						pc.leaveMovieMode();
					}
					deleteRitualSacrifice();
					deleteCameraMarker();
					_vanHalter.setIsImmobilized(false);
					_vanHalter.setIsInvul(false);

					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(18), 1000);

					break;

				case 18:
					combatBeginning();
					if (_movieTask != null)
					{
						_movieTask.cancel(false);
					}
					_movieTask = null;
			}
		}
	}

	protected List<L2PcInstance> getPlayersInside()
	{
		List<L2PcInstance> lst = new FastList<>();
		L2BossZone _Zone = GrandBossManager.getInstance().getZone(-16373, -53562, -10300);
		if (_Zone != null)
		{
			for (L2Character cha : _Zone.getCharactersInside())
			{
				if (cha.isPlayer())
				{
					lst.add((L2PcInstance) cha);
				}
			}
			return lst;
		}
		_log.warning("VanHalterManager: Zone for Van Halter is missing.");
		return null;
	}
}
