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
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.L2Vehicle;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.model.zone.type.L2PeaceZone;

public final class L2WorldRegion
{
	private static Logger _log = Logger.getLogger(L2WorldRegion.class.getName());
	
	private final Map<Integer, L2Playable> _allPlayable;
	private final Map<Integer, L2Object> _visibleObjects;
	
	private final List<L2WorldRegion> _surroundingRegions;
	private final int _tileX, _tileY;
	private boolean _active = false;
	private ScheduledFuture<?> _neighborsTask = null;
	private final List<L2ZoneType> _zones;
	private final Map<Integer, L2DoorInstance> _doors = new FastMap<Integer, L2DoorInstance>().shared();
	
	public L2WorldRegion(int pTileX, int pTileY)
	{
		_allPlayable = new FastMap<Integer, L2Playable>().shared();
		_visibleObjects = new FastMap<Integer, L2Object>().shared();
		_surroundingRegions = new ArrayList<>();
		
		_tileX = pTileX;
		_tileY = pTileY;
		
		if (Config.GRIDS_ALWAYS_ON)
		{
			_active = true;
		}
		else
		{
			_active = false;
		}
		_zones = new FastList<>();
	}
	
	public List<L2ZoneType> getZones()
	{
		return _zones;
	}
	
	public void addZone(L2ZoneType zone)
	{
		_zones.add(zone);
	}
	
	public void removeZone(L2ZoneType zone)
	{
		_zones.remove(zone);
	}
	
	public void revalidateZones(L2Character character)
	{
		if (character.isTeleporting())
		{
			return;
		}
		
		for (L2ZoneType z : getZones())
		{
			if (z != null)
			{
				z.revalidateInZone(character);
			}
		}
	}
	
	public void removeFromZones(L2Character character)
	{
		for (L2ZoneType z : getZones())
		{
			if (z != null)
			{
				z.removeCharacter(character);
			}
		}
	}
	
	public boolean containsZone(int zoneId)
	{
		for (L2ZoneType z : getZones())
		{
			if (z.getId() == zoneId)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean checkEffectRangeInsidePeaceZone(L2Skill skill, final int x, final int y, final int z)
	{
		final int range = skill.getEffectRange();
		final int up = y + range;
		final int down = y - range;
		final int left = x + range;
		final int right = x - range;
		
		for (L2ZoneType e : getZones())
		{
			if (e instanceof L2PeaceZone)
			{
				if (e.isInsideZone(x, up, z))
				{
					return false;
				}
				
				if (e.isInsideZone(x, down, z))
				{
					return false;
				}
				
				if (e.isInsideZone(left, y, z))
				{
					return false;
				}
				
				if (e.isInsideZone(right, y, z))
				{
					return false;
				}
				
				if (e.isInsideZone(x, y, z))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public void onDeath(L2Character character)
	{
		for (L2ZoneType z : getZones())
		{
			if (z != null)
			{
				z.onDieInside(character);
			}
		}
	}
	
	public void onRevive(L2Character character)
	{
		for (L2ZoneType z : getZones())
		{
			if (z != null)
			{
				z.onReviveInside(character);
			}
		}
	}
	
	public class NeighborsTask implements Runnable
	{
		private final boolean _isActivating;
		
		public NeighborsTask(boolean isActivating)
		{
			_isActivating = isActivating;
		}
		
		@Override
		public void run()
		{
			if (_isActivating)
			{
				for (L2WorldRegion neighbor : getSurroundingRegions())
				{
					neighbor.setActive(true);
				}
			}
			else
			{
				if (areNeighborsEmpty())
				{
					setActive(false);
				}
				
				for (L2WorldRegion neighbor : getSurroundingRegions())
				{
					if (neighbor.areNeighborsEmpty())
					{
						neighbor.setActive(false);
					}
				}
			}
		}
	}
	
	private void switchAI(boolean isOn)
	{
		int c = 0;
		if (!isOn)
		{
			Collection<L2Object> vObj = _visibleObjects.values();
			for (L2Object o : vObj)
			{
				if (o instanceof L2Attackable)
				{
					c++;
					L2Attackable mob = (L2Attackable) o;
					
					mob.setTarget(null);
					mob.stopMove(null);
					mob.stopAllEffects();
					mob.clearAggroList();
					mob.getAttackByList().clear();
					mob.getKnownList().removeAllKnownObjects();
					
					if (mob.hasAI())
					{
						mob.getAI().setIntention(l2e.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE);
						mob.getAI().stopAITask();
					}
				}
				else if (o instanceof L2Vehicle)
				{
					c++;
					((L2Vehicle) o).getKnownList().removeAllKnownObjects();
				}
			}
			_log.fine(c + " mobs were turned off");
		}
		else
		{
			Collection<L2Object> vObj = _visibleObjects.values();
			
			for (L2Object o : vObj)
			{
				if (o instanceof L2Attackable)
				{
					c++;
					((L2Attackable) o).getStatus().startHpMpRegeneration();
				}
				else if (o instanceof L2Npc)
				{
					((L2Npc) o).startRandomAnimationTimer();
				}
			}
			_log.fine(c + " mobs were turned on");
			
		}
		
	}
	
	public boolean isActive()
	{
		return _active;
	}
	
	public boolean areNeighborsEmpty()
	{
		if (isActive() && !_allPlayable.isEmpty())
		{
			return false;
		}
		
		for (L2WorldRegion neighbor : _surroundingRegions)
		{
			if (neighbor.isActive() && !neighbor._allPlayable.isEmpty())
			{
				return false;
			}
		}
		
		return true;
	}
	
	public void setActive(boolean value)
	{
		if (_active == value)
		{
			return;
		}
		
		_active = value;
		
		switchAI(value);
		
		if (value)
		{
			_log.fine("Starting Grid " + _tileX + "," + _tileY);
		}
		else
		{
			_log.fine("Stoping Grid " + _tileX + "," + _tileY);
		}
	}
	
	private void startActivation()
	{
		setActive(true);
		
		synchronized (this)
		{
			if (_neighborsTask != null)
			{
				_neighborsTask.cancel(true);
				_neighborsTask = null;
			}
			_neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(true), 1000 * Config.GRID_NEIGHBOR_TURNON_TIME);
		}
	}
	
	private void startDeactivation()
	{
		synchronized (this)
		{
			if (_neighborsTask != null)
			{
				_neighborsTask.cancel(true);
				_neighborsTask = null;
			}
			_neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(false), 1000 * Config.GRID_NEIGHBOR_TURNOFF_TIME);
		}
	}
	
	public void addVisibleObject(L2Object object)
	{
		if (object == null)
		{
			return;
		}
		
		assert object.getWorldRegion() == this;
		
		_visibleObjects.put(object.getObjectId(), object);
		
		if (object instanceof L2DoorInstance)
		{
			_doors.put(object.getObjectId(), (L2DoorInstance) object);
		}
		
		if (object instanceof L2Playable)
		{
			_allPlayable.put(object.getObjectId(), (L2Playable) object);
			
			if ((_allPlayable.size() == 1) && (!Config.GRIDS_ALWAYS_ON))
			{
				startActivation();
			}
		}
	}
	
	public void removeVisibleObject(L2Object object)
	{
		if (object == null)
		{
			return;
		}
		
		assert (object.getWorldRegion() == this) || (object.getWorldRegion() == null);
		
		_visibleObjects.remove(object.getObjectId());
		
		if (object instanceof L2DoorInstance)
		{
			_doors.remove(object.getObjectId());
		}
		
		if (object instanceof L2Playable)
		{
			_allPlayable.remove(object.getObjectId());
			
			if (_allPlayable.isEmpty() && !Config.GRIDS_ALWAYS_ON)
			{
				startDeactivation();
			}
		}
	}
	
	public void addSurroundingRegion(L2WorldRegion region)
	{
		_surroundingRegions.add(region);
	}
	
	public List<L2WorldRegion> getSurroundingRegions()
	{
		return _surroundingRegions;
	}
	
	public Map<Integer, L2Playable> getVisiblePlayable()
	{
		return _allPlayable;
	}
	
	public Map<Integer, L2Object> getVisibleObjects()
	{
		return _visibleObjects;
	}
	
	public String getName()
	{
		return "(" + _tileX + ", " + _tileY + ")";
	}
	
	public void deleteVisibleNpcSpawns()
	{
		_log.fine("Deleting all visible NPC's in Region: " + getName());
		Collection<L2Object> vNPC = _visibleObjects.values();
		for (L2Object obj : vNPC)
		{
			if (obj instanceof L2Npc)
			{
				L2Npc target = (L2Npc) obj;
				target.deleteMe();
				L2Spawn spawn = target.getSpawn();
				if (spawn != null)
				{
					spawn.stopRespawn();
					SpawnTable.getInstance().deleteSpawn(spawn, false);
				}
				_log.finest("Removed NPC " + target.getObjectId());
			}
		}
		_log.info("All visible NPC's deleted in Region: " + getName());
	}
	
	private int _key;
	
	public int getKey()
	{
		return _key;
	}
	
	public Collection<L2DoorInstance> getDoors()
	{
		return _doors.values();
	}
}