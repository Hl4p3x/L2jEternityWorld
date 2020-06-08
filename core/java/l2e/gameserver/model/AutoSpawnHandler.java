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
package l2e.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.L2DatabaseFactory;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.util.Rnd;

public class AutoSpawnHandler
{
	protected static final Logger _log = Logger.getLogger(AutoSpawnHandler.class.getName());
	
	private static final int DEFAULT_INITIAL_SPAWN = 30000;
	private static final int DEFAULT_RESPAWN = 3600000;
	private static final int DEFAULT_DESPAWN = 3600000;
	
	protected Map<Integer, AutoSpawnInstance> _registeredSpawns;
	protected Map<Integer, ScheduledFuture<?>> _runningSpawns;
	
	protected boolean _activeState = true;
	
	protected AutoSpawnHandler()
	{
		_registeredSpawns = new FastMap<>();
		_runningSpawns = new FastMap<>();
		
		restoreSpawnData();
	}
	
	public static AutoSpawnHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public final int size()
	{
		return _registeredSpawns.size();
	}
	
	public void reload()
	{
		for (ScheduledFuture<?> sf : _runningSpawns.values())
		{
			if (sf != null)
			{
				sf.cancel(true);
			}
		}
		
		for (AutoSpawnInstance asi : _registeredSpawns.values())
		{
			if (asi != null)
			{
				this.removeSpawn(asi);
			}
		}
		_registeredSpawns = new FastMap<>();
		_runningSpawns = new FastMap<>();
		
		restoreSpawnData();
	}
	
	private void restoreSpawnData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM random_spawn ORDER BY groupId ASC");
			PreparedStatement ps = con.prepareStatement("SELECT * FROM random_spawn_loc WHERE groupId=?"))
		{
			while (rs.next())
			{
				AutoSpawnInstance spawnInst = registerSpawn(rs.getInt("npcId"), rs.getInt("initialDelay"), rs.getInt("respawnDelay"), rs.getInt("despawnDelay"));
				
				spawnInst.setSpawnCount(rs.getInt("count"));
				spawnInst.setBroadcast(rs.getBoolean("broadcastSpawn"));
				spawnInst.setRandomSpawn(rs.getBoolean("randomSpawn"));
				
				ps.setInt(1, rs.getInt("groupId"));
				try (ResultSet rs2 = ps.executeQuery())
				{
					ps.clearParameters();
					
					while (rs2.next())
					{
						spawnInst.addSpawnLocation(rs2.getInt("x"), rs2.getInt("y"), rs2.getInt("z"), rs2.getInt("heading"));
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "AutoSpawnHandler: Could not restore spawn data: " + e.getMessage(), e);
		}
	}
	
	public AutoSpawnInstance registerSpawn(int npcId, int[][] spawnPoints, int initialDelay, int respawnDelay, int despawnDelay)
	{
		if (initialDelay < 0)
		{
			initialDelay = DEFAULT_INITIAL_SPAWN;
		}
		
		if (respawnDelay < 0)
		{
			respawnDelay = DEFAULT_RESPAWN;
		}
		
		if (despawnDelay < 0)
		{
			despawnDelay = DEFAULT_DESPAWN;
		}
		
		AutoSpawnInstance newSpawn = new AutoSpawnInstance(npcId, initialDelay, respawnDelay, despawnDelay);
		
		if (spawnPoints != null)
		{
			for (int[] spawnPoint : spawnPoints)
			{
				newSpawn.addSpawnLocation(spawnPoint);
			}
		}
		
		int newId = IdFactory.getInstance().getNextId();
		newSpawn._objectId = newId;
		_registeredSpawns.put(newId, newSpawn);
		
		setSpawnActive(newSpawn, true);
		
		return newSpawn;
	}
	
	public AutoSpawnInstance registerSpawn(int npcId, int initialDelay, int respawnDelay, int despawnDelay)
	{
		return registerSpawn(npcId, null, initialDelay, respawnDelay, despawnDelay);
	}
	
	public boolean removeSpawn(AutoSpawnInstance spawnInst)
	{
		if (!isSpawnRegistered(spawnInst))
		{
			return false;
		}
		
		try
		{
			_registeredSpawns.remove(spawnInst.getId());
			
			ScheduledFuture<?> respawnTask = _runningSpawns.remove(spawnInst._objectId);
			respawnTask.cancel(false);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "AutoSpawnHandler: Could not auto spawn for NPC ID " + spawnInst._npcId + " (Object ID = " + spawnInst._objectId + "): " + e.getMessage(), e);
			return false;
		}
		
		return true;
	}
	
	public void removeSpawn(int objectId)
	{
		removeSpawn(_registeredSpawns.get(objectId));
	}
	
	public void setSpawnActive(AutoSpawnInstance spawnInst, boolean isActive)
	{
		if (spawnInst == null)
		{
			return;
		}
		
		int objectId = spawnInst._objectId;
		
		if (isSpawnRegistered(objectId))
		{
			ScheduledFuture<?> spawnTask = null;
			
			if (isActive)
			{
				AutoSpawner rs = new AutoSpawner(objectId);
				
				if (spawnInst._desDelay > 0)
				{
					spawnTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(rs, spawnInst._initDelay, spawnInst._resDelay);
				}
				else
				{
					spawnTask = ThreadPoolManager.getInstance().scheduleEffect(rs, spawnInst._initDelay);
				}
				
				_runningSpawns.put(objectId, spawnTask);
			}
			else
			{
				AutoDespawner rd = new AutoDespawner(objectId);
				spawnTask = _runningSpawns.remove(objectId);
				
				if (spawnTask != null)
				{
					spawnTask.cancel(false);
				}
				
				ThreadPoolManager.getInstance().scheduleEffect(rd, 0);
			}
			
			spawnInst.setSpawnActive(isActive);
		}
	}
	
	public void setAllActive(boolean isActive)
	{
		if (_activeState == isActive)
		{
			return;
		}
		
		for (AutoSpawnInstance spawnInst : _registeredSpawns.values())
		{
			setSpawnActive(spawnInst, isActive);
		}
		
		_activeState = isActive;
	}
	
	public final long getTimeToNextSpawn(AutoSpawnInstance spawnInst)
	{
		int objectId = spawnInst.getObjectId();
		
		if (!isSpawnRegistered(objectId))
		{
			return -1;
		}
		
		return _runningSpawns.get(objectId).getDelay(TimeUnit.MILLISECONDS);
	}
	
	public final AutoSpawnInstance getAutoSpawnInstance(int id, boolean isObjectId)
	{
		if (isObjectId)
		{
			if (isSpawnRegistered(id))
			{
				return _registeredSpawns.get(id);
			}
		}
		else
		{
			for (AutoSpawnInstance spawnInst : _registeredSpawns.values())
			{
				if (spawnInst.getId() == id)
				{
					return spawnInst;
				}
			}
		}
		return null;
	}
	
	public Map<Integer, AutoSpawnInstance> getAutoSpawnInstances(int npcId)
	{
		Map<Integer, AutoSpawnInstance> spawnInstList = new FastMap<>();
		
		for (AutoSpawnInstance spawnInst : _registeredSpawns.values())
		{
			if (spawnInst.getId() == npcId)
			{
				spawnInstList.put(spawnInst.getObjectId(), spawnInst);
			}
		}
		
		return spawnInstList;
	}
	
	public final boolean isSpawnRegistered(int objectId)
	{
		return _registeredSpawns.containsKey(objectId);
	}
	
	public final boolean isSpawnRegistered(AutoSpawnInstance spawnInst)
	{
		return _registeredSpawns.containsValue(spawnInst);
	}
	
	private class AutoSpawner implements Runnable
	{
		private final int _objectId;
		
		protected AutoSpawner(int objectId)
		{
			_objectId = objectId;
		}
		
		@Override
		public void run()
		{
			try
			{
				AutoSpawnInstance spawnInst = _registeredSpawns.get(_objectId);
				
				if (!spawnInst.isSpawnActive())
				{
					return;
				}
				
				Location[] locationList = spawnInst.getLocationList();
				
				if (locationList.length == 0)
				{
					_log.info("AutoSpawnHandler: No location co-ords specified for spawn instance (Object ID = " + _objectId + ").");
					return;
				}
				
				int locationCount = locationList.length;
				int locationIndex = Rnd.nextInt(locationCount);
				
				if (!spawnInst.isRandomSpawn())
				{
					locationIndex = spawnInst._lastLocIndex + 1;
					
					if (locationIndex == locationCount)
					{
						locationIndex = 0;
					}
					
					spawnInst._lastLocIndex = locationIndex;
				}
				
				final int x = locationList[locationIndex].getX();
				final int y = locationList[locationIndex].getY();
				final int z = locationList[locationIndex].getZ();
				final int heading = locationList[locationIndex].getHeading();
				
				L2NpcTemplate npcTemp = NpcTable.getInstance().getTemplate(spawnInst.getId());
				if (npcTemp == null)
				{
					_log.warning("Couldnt find NPC id" + spawnInst.getId() + " Try to update your DP");
					return;
				}
				L2Spawn newSpawn = new L2Spawn(npcTemp);
				
				newSpawn.setX(x);
				newSpawn.setY(y);
				newSpawn.setZ(z);
				if (heading != -1)
				{
					newSpawn.setHeading(heading);
				}
				newSpawn.setAmount(spawnInst.getSpawnCount());
				if (spawnInst._desDelay == 0)
				{
					newSpawn.setRespawnDelay(spawnInst._resDelay);
				}
				
				SpawnTable.getInstance().addNewSpawn(newSpawn, false);
				L2Npc npcInst = null;
				
				if (spawnInst._spawnCount == 1)
				{
					npcInst = newSpawn.doSpawn();
					npcInst.setXYZ(npcInst.getX(), npcInst.getY(), npcInst.getZ());
					spawnInst.addNpcInstance(npcInst);
				}
				else
				{
					for (int i = 0; i < spawnInst._spawnCount; i++)
					{
						npcInst = newSpawn.doSpawn();
						npcInst.setXYZ(npcInst.getX() + Rnd.nextInt(50), npcInst.getY() + Rnd.nextInt(50), npcInst.getZ());
						spawnInst.addNpcInstance(npcInst);
					}
				}
				
				String nearestTown = MapRegionManager.getInstance().getClosestTownName(npcInst);
				
				if (spawnInst.isBroadcasting() && (npcInst != null))
				{
					Announcements.getInstance().announceToAll("The " + npcInst.getName() + " has spawned near " + nearestTown + "!");
				}
				
				if (spawnInst.getDespawnDelay() > 0)
				{
					AutoDespawner rd = new AutoDespawner(_objectId);
					ThreadPoolManager.getInstance().scheduleAi(rd, spawnInst.getDespawnDelay() - 1000);
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "AutoSpawnHandler: An error occurred while initializing spawn instance (Object ID = " + _objectId + "): " + e.getMessage(), e);
			}
		}
	}
	
	private class AutoDespawner implements Runnable
	{
		private final int _objectId;
		
		protected AutoDespawner(int objectId)
		{
			_objectId = objectId;
		}
		
		@Override
		public void run()
		{
			try
			{
				AutoSpawnInstance spawnInst = _registeredSpawns.get(_objectId);
				
				if (spawnInst == null)
				{
					_log.info("AutoSpawnHandler: No spawn registered for object ID = " + _objectId + ".");
					return;
				}
				
				for (L2Npc npcInst : spawnInst.getNPCInstanceList())
				{
					if (npcInst == null)
					{
						continue;
					}
					
					npcInst.deleteMe();
					SpawnTable.getInstance().deleteSpawn(npcInst.getSpawn(), false);
					spawnInst.removeNpcInstance(npcInst);
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "AutoSpawnHandler: An error occurred while despawning spawn (Object ID = " + _objectId + "): " + e.getMessage(), e);
			}
		}
	}
	
	public static class AutoSpawnInstance implements IIdentifiable
	{
		protected int _objectId;
		
		protected int _spawnIndex;
		
		protected int _npcId;
		
		protected int _initDelay;
		
		protected int _resDelay;
		
		protected int _desDelay;
		
		protected int _spawnCount = 1;
		
		protected int _lastLocIndex = -1;
		
		private final List<L2Npc> _npcList = new FastList<>();
		
		private final List<Location> _locList = new FastList<>();
		
		private boolean _spawnActive;
		
		private boolean _randomSpawn = false;
		
		private boolean _broadcastAnnouncement = false;
		
		protected AutoSpawnInstance(int npcId, int initDelay, int respawnDelay, int despawnDelay)
		{
			_npcId = npcId;
			_initDelay = initDelay;
			_resDelay = respawnDelay;
			_desDelay = despawnDelay;
		}
		
		protected void setSpawnActive(boolean activeValue)
		{
			_spawnActive = activeValue;
		}
		
		protected boolean addNpcInstance(L2Npc npcInst)
		{
			return _npcList.add(npcInst);
		}
		
		protected boolean removeNpcInstance(L2Npc npcInst)
		{
			return _npcList.remove(npcInst);
		}
		
		public int getObjectId()
		{
			return _objectId;
		}
		
		public int getInitialDelay()
		{
			return _initDelay;
		}
		
		public int getRespawnDelay()
		{
			return _resDelay;
		}
		
		public int getDespawnDelay()
		{
			return _desDelay;
		}
		
		@Override
		public int getId()
		{
			return _npcId;
		}
		
		public int getSpawnCount()
		{
			return _spawnCount;
		}
		
		public Location[] getLocationList()
		{
			return _locList.toArray(new Location[_locList.size()]);
		}
		
		public L2Npc[] getNPCInstanceList()
		{
			L2Npc[] ret;
			synchronized (_npcList)
			{
				ret = new L2Npc[_npcList.size()];
				_npcList.toArray(ret);
			}
			
			return ret;
		}
		
		public L2Spawn[] getSpawns()
		{
			List<L2Spawn> npcSpawns = new FastList<>();
			
			for (L2Npc npcInst : _npcList)
			{
				npcSpawns.add(npcInst.getSpawn());
			}
			
			return npcSpawns.toArray(new L2Spawn[npcSpawns.size()]);
		}
		
		public void setSpawnCount(int spawnCount)
		{
			_spawnCount = spawnCount;
		}
		
		public void setRandomSpawn(boolean randValue)
		{
			_randomSpawn = randValue;
		}
		
		public void setBroadcast(boolean broadcastValue)
		{
			_broadcastAnnouncement = broadcastValue;
		}
		
		public boolean isSpawnActive()
		{
			return _spawnActive;
		}
		
		public boolean isRandomSpawn()
		{
			return _randomSpawn;
		}
		
		public boolean isBroadcasting()
		{
			return _broadcastAnnouncement;
		}
		
		public boolean addSpawnLocation(int x, int y, int z, int heading)
		{
			return _locList.add(new Location(x, y, z, heading));
		}
		
		public boolean addSpawnLocation(int[] spawnLoc)
		{
			if (spawnLoc.length != 3)
			{
				return false;
			}
			
			return addSpawnLocation(spawnLoc[0], spawnLoc[1], spawnLoc[2], -1);
		}
		
		public Location removeSpawnLocation(int locIndex)
		{
			try
			{
				return _locList.remove(locIndex);
			}
			catch (IndexOutOfBoundsException e)
			{
				return null;
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final AutoSpawnHandler _instance = new AutoSpawnHandler();
	}
}