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
package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2e.L2DatabaseFactory;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2GrandBossInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.zone.type.L2BossZone;
import l2e.util.L2FastList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class GrandBossManager
{
	private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";
	
	private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";
	
	private static final String UPDATE_GRAND_BOSS_DATA = "UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?";
	
	private static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data set status = ? where boss_id = ?";
	
	protected static Logger _log = Logger.getLogger(GrandBossManager.class.getName());
	
	protected static Map<Integer, L2GrandBossInstance> _bosses;
	
	protected static TIntObjectHashMap<StatsSet> _storedInfo;
	
	private TIntIntHashMap _bossStatus;
	
	private L2FastList<L2BossZone> _zones;
	
	public static GrandBossManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected GrandBossManager()
	{
		init();
	}
	
	private void init()
	{
		_zones = new L2FastList<>();
		
		_bosses = new FastMap<>();
		_storedInfo = new TIntObjectHashMap<>();
		_bossStatus = new TIntIntHashMap();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * from grandboss_data ORDER BY boss_id");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				StatsSet info = new StatsSet();
				int bossId = rset.getInt("boss_id");
				info.set("loc_x", rset.getInt("loc_x"));
				info.set("loc_y", rset.getInt("loc_y"));
				info.set("loc_z", rset.getInt("loc_z"));
				info.set("heading", rset.getInt("heading"));
				info.set("respawn_time", rset.getLong("respawn_time"));
				double HP = rset.getDouble("currentHP");
				int true_HP = (int) HP;
				info.set("currentHP", true_HP);
				double MP = rset.getDouble("currentMP");
				int true_MP = (int) MP;
				info.set("currentMP", true_MP);
				int status = rset.getInt("status");
				_bossStatus.put(bossId, status);
				_storedInfo.put(bossId, info);
				_log.info("GrandBossManager: " + NpcTable.getInstance().getTemplate(bossId).getName() + "(" + bossId + ") status is " + status + ".");
				if (status > 0)
				{
					_log.info("GrandBossManager: Next spawn date of " + NpcTable.getInstance().getTemplate(bossId).getName() + " is " + new Date(info.getLong("respawn_time")) + ".");
				}
				
				info = null;
			}
			_log.info("GrandBossManager: Loaded " + _storedInfo.size() + " Instances");
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "GrandBossManager: Could not load grandboss_data table: " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error while initializing GrandBossManager: " + e.getMessage(), e);
		}
	}
	
	public void initZones()
	{
		FastMap<Integer, L2FastList<Integer>> zones = new FastMap<>();
		
		if (_zones == null)
		{
			_log.warning("GrandBossManager: Could not read Grand Boss zone data");
			return;
		}
		
		for (L2BossZone zone : _zones)
		{
			if (zone == null)
			{
				continue;
			}
			zones.put(zone.getId(), new L2FastList<Integer>());
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * from grandboss_list ORDER BY player_id");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int id = rset.getInt("player_id");
				int zone_id = rset.getInt("zone");
				zones.get(zone_id).add(id);
			}
			
			rset.close();
			statement.close();
			
			_log.info("GrandBossManager: Initialized " + _zones.size() + " Grand Boss Zones");
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "GrandBossManager: Could not load grandboss_list table: " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error while initializing GrandBoss zones: " + e.getMessage(), e);
		}
		
		for (L2BossZone zone : _zones)
		{
			if (zone == null)
			{
				continue;
			}
			zone.setAllowedPlayers(zones.get(zone.getId()));
		}
		
		zones.clear();
	}
	
	public void addZone(L2BossZone zone)
	{
		if (_zones != null)
		{
			_zones.add(zone);
		}
	}
	
	public final L2BossZone getZone(int zoneId)
	{
		if (_zones != null)
		{
			for (L2BossZone temp : _zones)
			{
				if (temp.getId() == zoneId)
				{
					return temp;
				}
			}
		}
		return null;
	}
	
	public final L2BossZone getZone(L2Character character)
	{
		if (_zones != null)
		{
			for (L2BossZone temp : _zones)
			{
				if (temp.isCharacterInZone(character))
				{
					return temp;
				}
			}
		}
		return null;
	}
	
	public final L2BossZone getZone(Location loc)
	{
		return getZone(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public final L2BossZone getZone(int x, int y, int z)
	{
		if (_zones != null)
		{
			for (L2BossZone temp : _zones)
			{
				if (temp.isInsideZone(x, y, z))
				{
					return temp;
				}
			}
		}
		return null;
	}
	
	public boolean checkIfInZone(String zoneType, L2Object obj)
	{
		L2BossZone temp = getZone(obj.getX(), obj.getY(), obj.getZ());
		if (temp == null)
		{
			return false;
		}
		
		return temp.getName().equalsIgnoreCase(zoneType);
	}
	
	public boolean checkIfInZone(L2PcInstance player)
	{
		if (player == null)
		{
			return false;
		}
		L2BossZone temp = getZone(player.getX(), player.getY(), player.getZ());
		if (temp == null)
		{
			return false;
		}
		
		return true;
	}
	
	public int getBossStatus(int bossId)
	{
		return _bossStatus.get(bossId);
	}
	
	public void setBossStatus(int bossId, int status)
	{
		_bossStatus.put(bossId, status);
		_log.info(getClass().getSimpleName() + ": Updated " + NpcTable.getInstance().getTemplate(bossId).getName() + "(" + bossId + ") status to " + status);
		updateDb(bossId, true);
	}
	
	public void addBoss(L2GrandBossInstance boss)
	{
		if (boss != null)
		{
			_bosses.put(boss.getId(), boss);
		}
	}
	
	public L2GrandBossInstance getBoss(int bossId)
	{
		return _bosses.get(bossId);
	}
	
	public StatsSet getStatsSet(int bossId)
	{
		return _storedInfo.get(bossId);
	}
	
	public void setStatsSet(int bossId, StatsSet info)
	{
		_storedInfo.put(bossId, info);
		updateDb(bossId, false);
	}
	
	private void storeToDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement delete = con.prepareStatement(DELETE_GRAND_BOSS_LIST))
		{
			delete.executeUpdate();
			
			try (PreparedStatement insert = con.prepareStatement(INSERT_GRAND_BOSS_LIST))
			{
				for (L2BossZone zone : _zones)
				{
					if (zone == null)
					{
						continue;
					}
					Integer id = zone.getId();
					L2FastList<Integer> list = zone.getAllowedPlayers();
					if ((list == null) || list.isEmpty())
					{
						continue;
					}
					for (Integer player : list)
					{
						insert.setInt(1, player);
						insert.setInt(2, id);
						insert.executeUpdate();
						insert.clearParameters();
					}
				}
			}
			for (Integer bossId : _storedInfo.keys())
			{
				final L2GrandBossInstance boss = _bosses.get(bossId);
				StatsSet info = _storedInfo.get(bossId);
				if ((boss == null) || (info == null))
				{
					try (PreparedStatement update = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2))
					{
						update.setInt(1, _bossStatus.get(bossId));
						update.setInt(2, bossId);
						update.executeUpdate();
						update.clearParameters();
					}
				}
				else
				{
					try (PreparedStatement update = con.prepareStatement(UPDATE_GRAND_BOSS_DATA))
					{
						update.setInt(1, boss.getX());
						update.setInt(2, boss.getY());
						update.setInt(3, boss.getZ());
						update.setInt(4, boss.getHeading());
						update.setLong(5, info.getLong("respawn_time"));
						double hp = boss.getCurrentHp();
						double mp = boss.getCurrentMp();
						if (boss.isDead())
						{
							hp = boss.getMaxHp();
							mp = boss.getMaxMp();
						}
						update.setDouble(6, hp);
						update.setDouble(7, mp);
						update.setInt(8, _bossStatus.get(bossId));
						update.setInt(9, bossId);
						update.executeUpdate();
						update.clearParameters();
					}
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "GrandBossManager: Couldn't store grandbosses to database:" + e.getMessage(), e);
		}
	}
	
	private void updateDb(int bossId, boolean statusOnly)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			L2GrandBossInstance boss = _bosses.get(bossId);
			StatsSet info = _storedInfo.get(bossId);
			
			if (statusOnly || (boss == null) || (info == null))
			{
				try (PreparedStatement statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2))
				{
					statement.setInt(1, _bossStatus.get(bossId));
					statement.setInt(2, bossId);
					statement.executeUpdate();
				}
			}
			else
			{
				try (PreparedStatement statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA))
				{
					statement.setInt(1, boss.getX());
					statement.setInt(2, boss.getY());
					statement.setInt(3, boss.getZ());
					statement.setInt(4, boss.getHeading());
					statement.setLong(5, info.getLong("respawn_time"));
					double hp = boss.getCurrentHp();
					double mp = boss.getCurrentMp();
					if (boss.isDead())
					{
						hp = boss.getMaxHp();
						mp = boss.getMaxMp();
					}
					statement.setDouble(6, hp);
					statement.setDouble(7, mp);
					statement.setInt(8, _bossStatus.get(bossId));
					statement.setInt(9, bossId);
					statement.executeUpdate();
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "GrandBossManager: Couldn't update grandbosses to database:" + e.getMessage(), e);
		}
	}
	
	public void cleanUp()
	{
		storeToDb();
		
		_bosses.clear();
		_storedInfo.clear();
		_bossStatus.clear();
		_zones.clear();
	}
	
	public L2FastList<L2BossZone> getZones()
	{
		return _zones;
	}
	
	private static class SingletonHolder
	{
		protected static final GrandBossManager _instance = new GrandBossManager();
	}
}