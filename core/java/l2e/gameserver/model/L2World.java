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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastTable;
import l2e.Config;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.data.xml.AdminParser;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.util.L2TIntObjectHashMap;
import l2e.gameserver.util.Point3D;
import l2e.util.StringUtil;
import gnu.trove.procedure.TObjectProcedure;

public final class L2World
{
	private static Logger _log = Logger.getLogger(L2World.class.getName());
	
	public static final int GRACIA_MAX_X = -166168;
	public static final int GRACIA_MAX_Z = 6105;
	public static final int GRACIA_MIN_Z = -895;
	
	public static final int SHIFT_BY = 12;
	
	public static final int MAP_MIN_X = -327680;
	public static final int MAP_MAX_X = 229376;
	public static final int MAP_MIN_Y = -262144;
	public static final int MAP_MAX_Y = 294912;
	
	public static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
	
	private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
	private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
	
	private final L2TIntObjectHashMap<L2PcInstance> _allPlayers;
	private final L2TIntObjectHashMap<L2Object> _allObjects;
	private final L2TIntObjectHashMap<String> _allObjectsDebug;
	private final L2TIntObjectHashMap<L2PetInstance> _petsInstance;
	
	private L2WorldRegion[][] _worldRegions;
	
	protected L2World()
	{
		_allPlayers = new L2TIntObjectHashMap<>();
		_allObjects = new L2TIntObjectHashMap<>();
		_allObjectsDebug = new L2TIntObjectHashMap<>();
		_petsInstance = new L2TIntObjectHashMap<>();
		
		initRegions();
	}
	
	public static L2World getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void storeObject(L2Object object)
	{
		if (_allObjects.containsKey(object.getObjectId()))
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Current object: " + object + " already exist in OID map!");
			_log.log(Level.WARNING, StringUtil.getTraceString(Thread.currentThread().getStackTrace()));
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Previous object: " + _allObjects.get(object.getObjectId()) + " already exist in OID map!");
			_log.log(Level.WARNING, _allObjectsDebug.get(object.getObjectId()));
			_log.log(Level.WARNING, "---------------------- End ---------------------");
			return;
		}
		_allObjects.put(object.getObjectId(), object);
		_allObjectsDebug.put(object.getObjectId(), StringUtil.getTraceString(Thread.currentThread().getStackTrace()));
	}
	
	public void removeObject(L2Object object)
	{
		_allObjects.remove(object.getObjectId());
		_allObjectsDebug.remove(object.getObjectId());
	}
	
	public L2Object findObject(int objectId)
	{
		return _allObjects.get(objectId);
	}
	
	public final L2Object[] getAllVisibleObjectsArray()
	{
		return _allObjects.values(new L2Object[0]);
	}
	
	public final boolean forEachObject(final TObjectProcedure<L2Object> proc)
	{
		return _allObjects.forEachValue(proc);
	}
	
	public final int getAllVisibleObjectsCount()
	{
		return _allObjects.size();
	}
	
	public List<L2PcInstance> getAllGMs()
	{
		return AdminParser.getInstance().getAllGms(true);
	}
	
	public final L2PcInstance[] getAllPlayersArray()
	{
		return _allPlayers.values(new L2PcInstance[0]);
	}
	
	public L2TIntObjectHashMap<L2PcInstance> getAllPlayers()
	{
		return _allPlayers;
	}
	
	public final boolean forEachPlayer(final TObjectProcedure<L2PcInstance> proc)
	{
		return _allPlayers.forEachValue(proc);
	}
	
	public int getAllPlayersCount()
	{
		return _allPlayers.size();
	}
	
	public L2PcInstance getPlayer(String name)
	{
		return getPlayer(CharNameHolder.getInstance().getIdByName(name));
	}
	
	public L2PcInstance getPlayer(int objectId)
	{
		return _allPlayers.get(objectId);
	}
	
	public L2PetInstance getPet(int ownerId)
	{
		return _petsInstance.get(ownerId);
	}
	
	public L2PetInstance addPet(int ownerId, L2PetInstance pet)
	{
		return _petsInstance.put(ownerId, pet);
	}
	
	public void removePet(int ownerId)
	{
		_petsInstance.remove(ownerId);
	}
	
	public void removePet(L2PetInstance pet)
	{
		_petsInstance.remove(pet.getOwner().getObjectId());
	}
	
	public void addVisibleObject(L2Object object, L2WorldRegion newRegion)
	{
		if (object.isPlayer())
		{
			L2PcInstance player = object.getActingPlayer();
			
			if (!player.isTeleporting())
			{
				L2PcInstance tmp = getPlayer(player.getObjectId());
				if (tmp != null)
				{
					_log.warning("Duplicate character!? Closing both characters (" + player.getName() + ")");
					player.logout();
					tmp.logout();
					return;
				}
				addToAllPlayers(player);
			}
		}
		
		if (!newRegion.isActive())
		{
			return;
		}
		
		List<L2Object> visibles = getVisibleObjects(object, 2000);
		if (Config.DEBUG)
		{
			_log.finest("objects in range:" + visibles.size());
		}
		
		for (L2Object visible : visibles)
		{
			if (visible == null)
			{
				continue;
			}
			
			visible.getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(visible);
		}
	}
	
	public void addToAllPlayers(L2PcInstance cha)
	{
		_allPlayers.put(cha.getObjectId(), cha);
	}
	
	public void removeFromAllPlayers(L2PcInstance cha)
	{
		_allPlayers.remove(cha.getObjectId());
	}
	
	public void removeVisibleObject(L2Object object, L2WorldRegion oldRegion)
	{
		if (object == null)
		{
			return;
		}
		
		if (oldRegion != null)
		{
			oldRegion.removeVisibleObject(object);
			
			for (L2WorldRegion reg : oldRegion.getSurroundingRegions())
			{
				final Collection<L2Object> vObj = reg.getVisibleObjects().values();
				for (L2Object obj : vObj)
				{
					if (obj != null)
					{
						obj.getKnownList().removeKnownObject(object);
					}
				}
			}
			object.getKnownList().removeAllKnownObjects();
			
			if (object.isPlayer())
			{
				final L2PcInstance player = object.getActingPlayer();
				if (!player.isTeleporting())
				{
					removeFromAllPlayers(player);
				}
			}
			
		}
	}
	
	public List<L2Object> getVisibleObjects(L2Object object)
	{
		L2WorldRegion reg = object.getWorldRegion();
		
		if (reg == null)
		{
			return null;
		}
		
		List<L2Object> result = new ArrayList<>();
		
		for (L2WorldRegion regi : reg.getSurroundingRegions())
		{
			Collection<L2Object> vObj = regi.getVisibleObjects().values();
			for (L2Object _object : vObj)
			{
				if ((_object == null) || _object.equals(object))
				{
					continue;
				}
				else if (!_object.isVisible())
				{
					continue;
				}
				result.add(_object);
			}
		}
		
		return result;
	}
	
	public List<L2Object> getVisibleObjects(L2Object object, int radius)
	{
		if ((object == null) || !object.isVisible())
		{
			return new ArrayList<>();
		}
		
		int x = object.getX();
		int y = object.getY();
		int sqRadius = radius * radius;
		
		List<L2Object> result = new ArrayList<>();
		
		for (L2WorldRegion regi : object.getWorldRegion().getSurroundingRegions())
		{
			Collection<L2Object> vObj = regi.getVisibleObjects().values();
			for (L2Object _object : vObj)
			{
				if ((_object == null) || _object.equals(object))
				{
					continue;
				}
				
				int x1 = _object.getX();
				int y1 = _object.getY();
				
				double dx = x1 - x;
				double dy = y1 - y;
				
				if (((dx * dx) + (dy * dy)) < sqRadius)
				{
					result.add(_object);
				}
			}
		}
		
		return result;
	}
	
	public List<L2Object> getVisibleObjects3D(L2Object object, int radius)
	{
		if ((object == null) || !object.isVisible())
		{
			return new ArrayList<>();
		}
		
		int x = object.getX();
		int y = object.getY();
		int z = object.getZ();
		int sqRadius = radius * radius;
		
		List<L2Object> result = new ArrayList<>();
		
		for (L2WorldRegion regi : object.getWorldRegion().getSurroundingRegions())
		{
			Collection<L2Object> vObj = regi.getVisibleObjects().values();
			for (L2Object _object : vObj)
			{
				if ((_object == null) || _object.equals(object))
				{
					continue;
				}
				
				int x1 = _object.getX();
				int y1 = _object.getY();
				int z1 = _object.getZ();
				
				long dx = x1 - x;
				long dy = y1 - y;
				long dz = z1 - z;
				
				if (((dx * dx) + (dy * dy) + (dz * dz)) < sqRadius)
				{
					result.add(_object);
				}
			}
		}
		
		return result;
	}
	
	public List<L2Playable> getVisiblePlayable(L2Object object)
	{
		L2WorldRegion reg = object.getWorldRegion();
		
		if (reg == null)
		{
			return null;
		}
		
		List<L2Playable> result = new ArrayList<>();
		
		for (L2WorldRegion regi : reg.getSurroundingRegions())
		{
			Map<Integer, L2Playable> _allpls = regi.getVisiblePlayable();
			Collection<L2Playable> _playables = _allpls.values();
			
			for (L2Playable _object : _playables)
			{
				if ((_object == null) || _object.equals(object))
				{
					continue;
				}
				
				if (!_object.isVisible())
				{
					continue;
				}
				
				result.add(_object);
			}
		}
		return result;
	}
	
	public L2WorldRegion getRegion(Point3D point)
	{
		return _worldRegions[(point.getX() >> SHIFT_BY) + OFFSET_X][(point.getY() >> SHIFT_BY) + OFFSET_Y];
	}
	
	public L2WorldRegion getRegion(int x, int y)
	{
		return _worldRegions[(x >> SHIFT_BY) + OFFSET_X][(y >> SHIFT_BY) + OFFSET_Y];
	}
	
	public L2WorldRegion[][] getAllWorldRegions()
	{
		return _worldRegions;
	}
	
	private boolean validRegion(int x, int y)
	{
		return ((x >= 0) && (x <= REGIONS_X) && (y >= 0) && (y <= REGIONS_Y));
	}
	
	private void initRegions()
	{
		_worldRegions = new L2WorldRegion[REGIONS_X + 1][REGIONS_Y + 1];
		
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				_worldRegions[i][j] = new L2WorldRegion(i, j);
			}
		}
		
		for (int x = 0; x <= REGIONS_X; x++)
		{
			for (int y = 0; y <= REGIONS_Y; y++)
			{
				for (int a = -1; a <= 1; a++)
				{
					for (int b = -1; b <= 1; b++)
					{
						if (validRegion(x + a, y + b))
						{
							_worldRegions[x + a][y + b].addSurroundingRegion(_worldRegions[x][y]);
						}
					}
				}
			}
		}
		_log.info("L2World: (" + REGIONS_X + " by " + REGIONS_Y + ") World Region Grid set up.");
		
	}
	
	public void deleteVisibleNpcSpawns()
	{
		_log.info("Deleting all visible NPC's.");
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				_worldRegions[i][j].deleteVisibleNpcSpawns();
			}
		}
		_log.info("All visible NPC's deleted.");
	}
	
	public L2PcInstance findPlayer(int objectId)
	{
		L2Object obj = _allObjects.get(objectId);
		
		if (obj.isPlayer())
		{
			return (L2PcInstance) obj;
		}
		
		return null;
	}
	
	public static Collection<L2DoorInstance> getVisibleDoors(L2Object obj)
	{
		if ((obj == null) || !obj.isVisible())
		{
			return null;
		}
		
		if (obj instanceof L2Playable)
		{
			FastTable<L2DoorInstance> r = new FastTable<>();
			
			L2WorldRegion reg = obj.getWorldRegion();
			r.addAll(reg.getDoors());
			for (L2WorldRegion regS : reg.getSurroundingRegions())
			{
				if (regS == reg)
				{
					continue;
				}
				r.addAll(regS.getDoors());
			}
			return r;
		}
		return obj.getWorldRegion().getDoors();
	}
	
	private static class SingletonHolder
	{
		protected static final L2World _instance = new L2World();
	}
}