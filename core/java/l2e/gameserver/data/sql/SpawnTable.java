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
package l2e.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastSet;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.instancemanager.DayNightSpawnManager;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;

public class SpawnTable
{
	private static Logger _log = Logger.getLogger(SpawnTable.class.getName());
	
	private final FastSet<L2Spawn> _spawntable = new FastSet<>();
	private int _npcSpawnCount;
	private int _customSpawnCount;
	
	protected SpawnTable()
	{
		_spawntable.shared();
		if (!Config.ALT_DEV_NO_SPAWNS)
		{
			fillSpawnTable();
		}
	}
	
	public FastSet<L2Spawn> getSpawnTable()
	{
		return _spawntable;
	}
	
	private void fillSpawnTable()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT count, npc_templateid, locx, locy, locz, heading, respawn_delay, respawn_random, loc_id, periodOfDay FROM spawnlist");
			ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					if (template1.isType("L2SiegeGuard"))
					{
					}
					else if (template1.isType("L2RaidBoss"))
					{
					}
					else if (!Config.ALLOW_CLASS_MASTERS && template1.isType("L2ClassMaster"))
					{
					}
					else
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setAmount(rset.getInt("count"));
						spawnDat.setX(rset.getInt("locx"));
						spawnDat.setY(rset.getInt("locy"));
						spawnDat.setZ(rset.getInt("locz"));
						spawnDat.setHeading(rset.getInt("heading"));
						spawnDat.setRespawnDelay(rset.getInt("respawn_delay"), rset.getInt("respawn_random"));
						int loc_id = rset.getInt("loc_id");
						spawnDat.setLocationId(loc_id);
						
						switch (rset.getInt("periodOfDay"))
						{
							case 0:
								_npcSpawnCount += spawnDat.init();
								break;
							case 1:
								DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
								_npcSpawnCount++;
								break;
							case 2:
								DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
								_npcSpawnCount++;
								break;
						}
						
						_spawntable.add(spawnDat);
					}
				}
				else
				{
					_log.warning(getClass().getSimpleName() + ": Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Spawn could not be initialized: " + e.getMessage(), e);
		}
		_log.info(getClass().getSimpleName() + ": Loaded " + _spawntable.size() + " npc spawns from database.");
		
		if (Config.CUSTOM_SPAWNLIST_TABLE)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("SELECT count, npc_templateid, locx, locy, locz, heading, respawn_delay, respawn_random, loc_id, periodOfDay FROM custom_spawnlist");
				ResultSet rset = statement.executeQuery();
				
				L2Spawn spawnDat;
				L2NpcTemplate template1;
				
				while (rset.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
					if (template1 != null)
					{
						if (template1.isType("L2SiegeGuard"))
						{
						}
						else if (template1.isType("L2RaidBoss"))
						{
						}
						else if (!Config.ALLOW_CLASS_MASTERS && template1.isType("L2ClassMaster"))
						{
						}
						else
						{
							spawnDat = new L2Spawn(template1);
							spawnDat.setAmount(rset.getInt("count"));
							spawnDat.setX(rset.getInt("locx"));
							spawnDat.setY(rset.getInt("locy"));
							spawnDat.setZ(rset.getInt("locz"));
							spawnDat.setHeading(rset.getInt("heading"));
							spawnDat.setRespawnDelay(rset.getInt("respawn_delay"), rset.getInt("respawn_random"));
							spawnDat.setCustom(true);
							int loc_id = rset.getInt("loc_id");
							spawnDat.setLocationId(loc_id);
							
							switch (rset.getInt("periodOfDay"))
							{
								case 0:
									_customSpawnCount += spawnDat.init();
									break;
								case 1:
									DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
									_customSpawnCount++;
									break;
								case 2:
									DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
									_customSpawnCount++;
									break;
							}
							
							_spawntable.add(spawnDat);
						}
					}
					else
					{
						_log.warning(getClass().getSimpleName() + ": Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
					}
				}
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "CustomSpawnTable: Spawn could not be initialized: " + e.getMessage(), e);
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + _customSpawnCount + " custom npc spawns.");
		}
		
		if (Config.DEBUG)
		{
			_log.fine(getClass().getSimpleName() + ": Spawning completed, total number of NPCs in the world: " + (_npcSpawnCount + _customSpawnCount));
		}
	}
	
	public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
	{
		_spawntable.add(spawn);
		
		if (storeInDb)
		{
			String spawnTable;
			if (spawn.isCustom() && Config.CUSTOM_SPAWNLIST_TABLE)
			{
				spawnTable = "custom_spawnlist";
			}
			else
			{
				spawnTable = "spawnlist";
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement insert = con.prepareStatement("INSERT INTO " + spawnTable + "(count,npc_templateid,locx,locy,locz,heading,respawn_delay,respawn_random,loc_id) values(?,?,?,?,?,?,?,?,?)"))
			{
				insert.setInt(1, spawn.getAmount());
				insert.setInt(2, spawn.getId());
				insert.setInt(3, spawn.getX());
				insert.setInt(4, spawn.getY());
				insert.setInt(5, spawn.getZ());
				insert.setInt(6, spawn.getHeading());
				insert.setInt(7, spawn.getRespawnDelay() / 1000);
				insert.setInt(8, spawn.getRespawnMaxDelay() - spawn.getRespawnMinDelay());
				insert.setInt(9, spawn.getLocationId());
				insert.execute();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": Could not store spawn in the DB:" + e.getMessage(), e);
			}
		}
	}
	
	public void deleteSpawn(L2Spawn spawn, boolean updateDb)
	{
		if (!_spawntable.remove(spawn))
		{
			return;
		}
		
		if (updateDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement delete = con.prepareStatement("DELETE FROM " + (spawn.isCustom() ? "custom_spawnlist" : "spawnlist") + " WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?"))
			{
				delete.setInt(1, spawn.getX());
				delete.setInt(2, spawn.getY());
				delete.setInt(3, spawn.getZ());
				delete.setInt(4, spawn.getId());
				delete.setInt(5, spawn.getHeading());
				delete.execute();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": Spawn " + spawn + " could not be removed from DB: " + e.getMessage(), e);
			}
		}
	}
	
	public void reloadAll()
	{
		fillSpawnTable();
	}
	
	public Collection<L2Spawn> getAllSpawns()
	{
		return _spawntable;
	}
	
	public void findNPCInstances(L2PcInstance activeChar, int npcId, int teleportIndex, boolean showposition)
	{
		int index = 0;
		for (L2Spawn spawn : _spawntable)
		{
			
			if (npcId == spawn.getId())
			{
				index++;
				L2Npc _npc = spawn.getLastSpawn();
				if (teleportIndex > -1)
				{
					if (teleportIndex == index)
					{
						if (showposition && (_npc != null))
						{
							activeChar.teleToLocation(_npc.getX(), _npc.getY(), _npc.getZ(), true);
						}
						else
						{
							activeChar.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ(), true);
						}
					}
				}
				else
				{
					if (showposition && (_npc != null))
					{
						activeChar.sendMessage(index + " - " + spawn.getTemplate().getName() + " (" + spawn + "): " + _npc.getX() + " " + _npc.getY() + " " + _npc.getZ());
					}
					else
					{
						activeChar.sendMessage(index + " - " + spawn.getTemplate().getName() + " (" + spawn + "): " + spawn.getX() + " " + spawn.getY() + " " + spawn.getZ());
					}
				}
			}
		}
		
		if (index == 0)
		{
			activeChar.sendMessage(getClass().getSimpleName() + ": No current spawns found.");
		}
	}
	
	public static SpawnTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SpawnTable _instance = new SpawnTable();
	}
}