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

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.TerritoryHolder;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2NpcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.interfaces.IPositionable;
import l2e.gameserver.model.zone.ZoneId;
import l2e.util.Rnd;

/**
 * Fixed by LordWinter 17.12.2012 Based on L2J Eternity-World
 */
public class L2Spawn implements IPositionable, IIdentifiable
{
	protected static final Logger _log = Logger.getLogger(L2Spawn.class.getName());
	
	private L2NpcTemplate _template;
	private int _maximumCount;
	private int _currentCount;
	protected int _scheduledCount;
	private int _locationId;
	private Location _location = new Location(0, 0, 0);
	private int _respawnMinDelay;
	private int _respawnMaxDelay;
	private int _instanceId = 0;
	private Constructor<?> _constructor;
	private boolean _doRespawn;
	private boolean _customSpawn;
	private boolean _randomSpawn;
	
	private L2Npc _lastSpawn;
	private static List<SpawnListener> _spawnListeners = new FastList<>();
	private Map<Integer, Location> _lastSpawnPoints;
	
	private boolean _isNoRndWalk = false;
	
	class SpawnTask implements Runnable
	{
		private final L2Npc _oldNpc;
		
		public SpawnTask(L2Npc pOldNpc)
		{
			_oldNpc = pOldNpc;
		}
		
		@Override
		public void run()
		{
			try
			{
				respawnNpc(_oldNpc);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "", e);
			}
			_scheduledCount--;
		}
	}
	
	public L2Spawn(L2NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException
	{
		_template = mobTemplate;
		
		if (_template == null)
		{
			return;
		}
		Class<?>[] parameters =
		{
			int.class,
			Class.forName("l2e.gameserver.model.actor.templates.L2NpcTemplate")
		};
		_constructor = Class.forName("l2e.gameserver.model.actor.instance." + _template.getType() + "Instance").getConstructor(parameters);
	}
	
	public int getAmount()
	{
		return _maximumCount;
	}
	
	public int getLocationId()
	{
		return _locationId;
	}
	
	@Override
	public Location getLocation()
	{
		return _location;
	}
	
	public Location getLocation(L2Object obj)
	{
		return ((_lastSpawnPoints == null) || (obj == null) || !_lastSpawnPoints.containsKey(obj.getObjectId())) ? _location : _lastSpawnPoints.get(obj.getObjectId());
	}
	
	@Override
	public int getX()
	{
		return _location.getX();
	}
	
	public int getX(L2Object obj)
	{
		return getLocation(obj).getX();
	}
	
	public void setX(int x)
	{
		_location.setX(x);
	}
	
	@Override
	public int getY()
	{
		return _location.getY();
	}
	
	public int getY(L2Object obj)
	{
		return getLocation(obj).getY();
	}
	
	public void setY(int y)
	{
		_location.setY(y);
	}
	
	@Override
	public int getZ()
	{
		return _location.getZ();
	}
	
	public int getZ(L2Object obj)
	{
		return getLocation(obj).getZ();
	}
	
	public void setZ(int z)
	{
		_location.setZ(z);
	}
	
	public int getHeading()
	{
		return _location.getHeading();
	}
	
	public void setHeading(int heading)
	{
		_location.setHeading(heading);
	}
	
	public void setLocation(Location loc)
	{
		_location = loc;
	}
	
	public void setLocationId(int id)
	{
		_locationId = id;
	}
	
	@Override
	public int getId()
	{
		return _template.getId();
	}
	
	public int getRespawnMinDelay()
	{
		return _respawnMinDelay;
	}
	
	public int getRespawnMaxDelay()
	{
		return _respawnMaxDelay;
	}
	
	public void setAmount(int amount)
	{
		_maximumCount = amount;
	}
	
	public void setRespawnMinDelay(int date)
	{
		_respawnMinDelay = date;
	}
	
	public void setRespawnMaxDelay(int date)
	{
		_respawnMaxDelay = date;
	}
	
	public void setCustom(boolean custom)
	{
		_customSpawn = custom;
	}
	
	public boolean isCustom()
	{
		return _customSpawn;
	}
	
	public boolean isRandom()
	{
		return _randomSpawn;
	}
	
	public void setRandom(boolean random)
	{
		_randomSpawn = random;
	}
	
	public void decreaseCount(L2Npc oldNpc)
	{
		if (_currentCount <= 0)
		{
			return;
		}
		_currentCount--;
		
		if (_lastSpawnPoints != null)
		{
			_lastSpawnPoints.remove(oldNpc.getObjectId());
		}
		
		if (_doRespawn && ((_scheduledCount + _currentCount) < _maximumCount))
		{
			_scheduledCount++;
			ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(oldNpc), hasRespawnRandom() ? Rnd.get(_respawnMinDelay, _respawnMaxDelay) : _respawnMinDelay);
		}
	}
	
	public int init()
	{
		while (_currentCount < _maximumCount)
		{
			doSpawn();
		}
		_doRespawn = _respawnMinDelay != 0;
		
		return _currentCount;
	}
	
	public L2Npc spawnOne(boolean val)
	{
		return doSpawn(val);
	}
	
	public boolean isRespawnEnabled()
	{
		return _doRespawn;
	}
	
	public void stopRespawn()
	{
		_doRespawn = false;
	}
	
	public void startRespawn()
	{
		_doRespawn = true;
	}
	
	public L2Npc doSpawn()
	{
		return doSpawn(false);
	}
	
	public L2Npc doSpawn(boolean isSummonSpawn)
	{
		L2Npc mob = null;
		try
		{
			if (_template.isType("L2Pet") || _template.isType("L2Decoy") || _template.isType("L2Trap") || _template.isType("L2EffectPoint"))
			{
				_currentCount++;
				
				return mob;
			}
			Object[] parameters =
			{
				IdFactory.getInstance().getNextId(),
				_template
			};
			Object tmp = _constructor.newInstance(parameters);
			((L2Object) tmp).setInstanceId(_instanceId);
			if (isSummonSpawn && (tmp instanceof L2Character))
			{
				((L2Character) tmp).setShowSummonAnimation(isSummonSpawn);
			}
			
			if (!(tmp instanceof L2Npc))
			{
				return mob;
			}
			mob = (L2Npc) tmp;
			return initializeNpcInstance(mob);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "NPC " + _template.getId() + " class not found", e);
		}
		return mob;
	}
	
	private L2Npc initializeNpcInstance(L2Npc mob)
	{
		int newlocx, newlocy, newlocz;
		
		if ((getX() == 0) && (getY() == 0))
		{
			if (getLocationId() == 0)
			{
				return mob;
			}
			int p[] = TerritoryHolder.getInstance().getRandomPoint(getLocationId());
			newlocx = p[0];
			newlocy = p[1];
			newlocz = GeoClient.getInstance().getSpawnHeight(newlocx, newlocy, p[2]);
		}
		else
		{
			boolean doCorrect = false;
			if (Config.GEODATA && !mob.isFlying())
			{
				switch (Config.GEO_CORRECT_Z)
				{
					case ALL:
						doCorrect = true;
						break;
					case TOWN:
						if (mob instanceof L2NpcInstance)
						{
							doCorrect = true;
						}
						break;
					case MONSTER:
						if (mob instanceof L2Attackable)
						{
							doCorrect = true;
						}
						break;
				}
			}
			
			if (_randomSpawn)
			{
				int signX = (Rnd.nextInt(2) == 0) ? -1 : 1;
				int signY = (Rnd.nextInt(2) == 0) ? -1 : 1;
				int randX = Rnd.nextInt(Config.MAX_DRIFT_RANGE);
				int randY = Rnd.nextInt(Config.MAX_DRIFT_RANGE / 2);
				
				newlocx = getX() + (signX * randX);
				newlocy = getY() + (signY * randY);
				
				if (doCorrect && !mob.isFlying() && !mob.isInsideZone(ZoneId.WATER))
				{
					newlocz = GeoClient.getInstance().getSpawnHeight(newlocx, newlocy, getZ());
				}
				else
				{
					newlocz = getZ();
				}
			}
			else
			{
				newlocx = getX();
				newlocy = getY();
				
				if (doCorrect && !mob.isFlying() && !mob.isInsideZone(ZoneId.WATER))
				{
					newlocz = GeoClient.getInstance().getSpawnHeight(newlocx, newlocy, getZ());
				}
				else
				{
					newlocz = getZ();
				}
			}
		}
		mob.stopAllEffects();
		mob.setIsDead(false);
		mob.setDecayed(false);
		mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
		if (mob.hasVariables())
		{
			mob.getVariables().getSet().clear();
		}
		mob.setIsNoRndWalk(isNoRndWalk());
		
		if (getHeading() == -1)
		{
			mob.setHeading(Rnd.nextInt(61794));
		}
		else
		{
			mob.setHeading(getHeading());
		}
		
		if (mob instanceof L2Attackable)
		{
			((L2Attackable) mob).setChampion(false);
		}
		
		if (Config.CHAMPION_ENABLE)
		{
			if (mob.isMonster() && !getTemplate().isQuestMonster() && !mob.isRaid() && !mob.isRaidMinion() && (Config.CHAMPION_FREQUENCY > 0) && (mob.getLevel() >= Config.CHAMP_MIN_LVL) && (mob.getLevel() <= Config.CHAMP_MAX_LVL) && (Config.CHAMPION_ENABLE_IN_INSTANCES || (getInstanceId() == 0)))
			{
				if (Rnd.get(100) < Config.CHAMPION_FREQUENCY)
				{
					((L2Attackable) mob).setChampion(true);
				}
			}
		}
		mob.setSpawn(this);
		mob.spawnMe(newlocx, newlocy, newlocz);
		
		L2Spawn.notifyNpcSpawned(mob);
		
		_lastSpawn = mob;
		
		if (_lastSpawnPoints != null)
		{
			_lastSpawnPoints.put(mob.getObjectId(), new Location(newlocx, newlocy, newlocz));
		}
		
		if (Config.DEBUG)
		{
			_log.finest("Spawned Mob Id: " + _template.getId() + " , at: X: " + mob.getX() + " Y: " + mob.getY() + " Z: " + mob.getZ());
		}
		_currentCount++;
		return mob;
	}
	
	public static void addSpawnListener(SpawnListener listener)
	{
		synchronized (_spawnListeners)
		{
			_spawnListeners.add(listener);
		}
	}
	
	public static void removeSpawnListener(SpawnListener listener)
	{
		synchronized (_spawnListeners)
		{
			_spawnListeners.remove(listener);
		}
	}
	
	public static void notifyNpcSpawned(L2Npc npc)
	{
		synchronized (_spawnListeners)
		{
			for (SpawnListener listener : _spawnListeners)
			{
				listener.npcSpawned(npc);
			}
		}
	}
	
	public void setRespawnDelay(int delay, int randomInterval)
	{
		if (delay != 0)
		{
			if (delay < 0)
			{
				_log.warning("respawn delay is negative for spawn:" + this);
			}
			int minDelay = delay - randomInterval;
			int maxDelay = delay + randomInterval;
			_respawnMinDelay = Math.max(10, minDelay) * 1000;
			_respawnMaxDelay = Math.max(10, maxDelay) * 1000;
		}
		else
		{
			_respawnMinDelay = 0;
			_respawnMaxDelay = 0;
		}
	}
	
	public void setRespawnDelay(int delay)
	{
		setRespawnDelay(delay, 0);
	}
	
	public int getRespawnDelay()
	{
		return (_respawnMinDelay + _respawnMaxDelay) / 2;
	}
	
	public boolean hasRespawnRandom()
	{
		return _respawnMinDelay != _respawnMaxDelay;
	}
	
	public L2Npc getLastSpawn()
	{
		return _lastSpawn;
	}
	
	public void respawnNpc(L2Npc oldNpc)
	{
		if (_doRespawn)
		{
			oldNpc.refreshID();
			initializeNpcInstance(oldNpc);
		}
	}
	
	public L2NpcTemplate getTemplate()
	{
		return _template;
	}
	
	public int getInstanceId()
	{
		return _instanceId;
	}
	
	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;
	}
	
	public final boolean isNoRndWalk()
	{
		return _isNoRndWalk;
	}
	
	public final void setIsNoRndWalk(boolean value)
	{
		_isNoRndWalk = value;
	}
	
	@Override
	public String toString()
	{
		return "L2Spawn [_template=" + getId() + ", _locX=" + getX() + ", _locY=" + getY() + ", _locZ=" + getZ() + ", _heading=" + getHeading() + "]";
	}
}