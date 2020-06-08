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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.L2WorldRegion;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.zone.AbstractZoneSettings;
import l2e.gameserver.model.zone.L2ZoneRespawn;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.model.zone.form.ZoneCuboid;
import l2e.gameserver.model.zone.form.ZoneCylinder;
import l2e.gameserver.model.zone.form.ZoneNPoly;
import l2e.gameserver.model.zone.type.L2ArenaZone;
import l2e.gameserver.model.zone.type.L2OlympiadStadiumZone;
import l2e.gameserver.model.zone.type.L2RespawnZone;

public class ZoneManager extends DocumentParser
{
	private static final Map<String, AbstractZoneSettings> _settings = new HashMap<>();
	
	private final Map<Class<? extends L2ZoneType>, Map<Integer, ? extends L2ZoneType>> _classZones = new HashMap<>();
	private int _lastDynamicId = 300000;
	private List<L2ItemInstance> _debugItems;
	
	protected ZoneManager()
	{
		load();
	}
	
	public void reload()
	{
		int count = 0;
		L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		
		for (Map<Integer, ? extends L2ZoneType> map : _classZones.values())
		{
			for (L2ZoneType zone : map.values())
			{
				if (zone.getSettings() != null)
				{
					_settings.put(zone.getName(), zone.getSettings());
				}
			}
		}
		
		for (L2WorldRegion[] worldRegion : worldRegions)
		{
			for (L2WorldRegion element : worldRegion)
			{
				element.getZones().clear();
				count++;
			}
		}
		GrandBossManager.getInstance().getZones().clear();
		_log.info(getClass().getSimpleName() + ": Removed zones in " + count + " regions.");
		
		load();
		for (L2Object obj : L2World.getInstance().getAllVisibleObjectsArray())
		{
			if (obj instanceof L2Character)
			{
				((L2Character) obj).revalidateZone(true);
			}
		}
		_settings.clear();
	}
	
	@Override
	protected void parseDocument()
	{
		L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		NamedNodeMap attrs;
		Node attribute;
		String zoneName;
		int[][] coords;
		int zoneId, minZ, maxZ;
		String zoneType, zoneShape;
		List<int[]> rs = new ArrayList<>();
		
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				attrs = n.getAttributes();
				attribute = attrs.getNamedItem("enabled");
				if ((attribute != null) && !Boolean.parseBoolean(attribute.getNodeValue()))
				{
					continue;
				}
				
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("zone".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						
						attribute = attrs.getNamedItem("id");
						if (attribute != null)
						{
							zoneId = Integer.parseInt(attribute.getNodeValue());
						}
						else
						{
							zoneId = _lastDynamicId++;
						}
						
						attribute = attrs.getNamedItem("name");
						if (attribute != null)
						{
							zoneName = attribute.getNodeValue();
						}
						else
						{
							zoneName = null;
						}
						
						minZ = parseInt(attrs, "minZ");
						maxZ = parseInt(attrs, "maxZ");
						
						zoneType = attrs.getNamedItem("type").getNodeValue();
						zoneShape = attrs.getNamedItem("shape").getNodeValue();
						
						Class<?> newZone = null;
						Constructor<?> zoneConstructor = null;
						L2ZoneType temp;
						try
						{
							newZone = Class.forName("l2e.gameserver.model.zone.type.L2" + zoneType);
							zoneConstructor = newZone.getConstructor(int.class);
							temp = (L2ZoneType) zoneConstructor.newInstance(zoneId);
						}
						catch (Exception e)
						{
							_log.warning("ZoneData: No such zone type: " + zoneType + " in file: " + getCurrentFile().getName());
							continue;
						}
						
						try
						{
							coords = null;
							int[] point;
							rs.clear();
							
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("node".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									point = new int[2];
									point[0] = parseInt(attrs, "X");
									point[1] = parseInt(attrs, "Y");
									rs.add(point);
								}
							}
							
							coords = rs.toArray(new int[rs.size()][2]);
							
							if ((coords == null) || (coords.length == 0))
							{
								_log.warning("ZoneData: missing data for zone: " + zoneId + " XML file: " + getCurrentFile().getName());
								continue;
							}
							
							if (zoneShape.equalsIgnoreCase("Cuboid"))
							{
								if (coords.length == 2)
								{
									temp.setZone(new ZoneCuboid(coords[0][0], coords[1][0], coords[0][1], coords[1][1], minZ, maxZ));
								}
								else
								{
									_log.warning("ZoneData: Missing cuboid vertex in sql data for zone: " + zoneId + " in file: " + getCurrentFile().getName());
									continue;
								}
							}
							else if (zoneShape.equalsIgnoreCase("NPoly"))
							{
								if (coords.length > 2)
								{
									final int[] aX = new int[coords.length];
									final int[] aY = new int[coords.length];
									for (int i = 0; i < coords.length; i++)
									{
										aX[i] = coords[i][0];
										aY[i] = coords[i][1];
									}
									temp.setZone(new ZoneNPoly(aX, aY, minZ, maxZ));
								}
								else
								{
									_log.warning("ZoneData: Bad data for zone: " + zoneId + " in file: " + getCurrentFile().getName());
									continue;
								}
							}
							else if (zoneShape.equalsIgnoreCase("Cylinder"))
							{
								attrs = d.getAttributes();
								final int zoneRad = Integer.parseInt(attrs.getNamedItem("rad").getNodeValue());
								if ((coords.length == 1) && (zoneRad > 0))
								{
									temp.setZone(new ZoneCylinder(coords[0][0], coords[0][1], minZ, maxZ, zoneRad));
								}
								else
								{
									_log.warning("ZoneData: Bad data for zone: " + zoneId + " in file: " + getCurrentFile().getName());
									continue;
								}
							}
						}
						catch (Exception e)
						{
							_log.log(Level.WARNING, "ZoneData: Failed to load zone " + zoneId + " coordinates: " + e.getMessage(), e);
						}
						
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("stat".equalsIgnoreCase(cd.getNodeName()))
							{
								attrs = cd.getAttributes();
								String name = attrs.getNamedItem("name").getNodeValue();
								String val = attrs.getNamedItem("val").getNodeValue();
								
								temp.setParameter(name, val);
							}
							else if ("spawn".equalsIgnoreCase(cd.getNodeName()) && (temp instanceof L2ZoneRespawn))
							{
								attrs = cd.getAttributes();
								int spawnX = Integer.parseInt(attrs.getNamedItem("X").getNodeValue());
								int spawnY = Integer.parseInt(attrs.getNamedItem("Y").getNodeValue());
								int spawnZ = Integer.parseInt(attrs.getNamedItem("Z").getNodeValue());
								Node val = attrs.getNamedItem("type");
								((L2ZoneRespawn) temp).parseLoc(spawnX, spawnY, spawnZ, val == null ? null : val.getNodeValue());
							}
							else if ("race".equalsIgnoreCase(cd.getNodeName()) && (temp instanceof L2RespawnZone))
							{
								attrs = cd.getAttributes();
								String race = attrs.getNamedItem("name").getNodeValue();
								String point = attrs.getNamedItem("point").getNodeValue();
								
								((L2RespawnZone) temp).addRaceRespawnPoint(race, point);
							}
						}
						if (checkId(zoneId))
						{
							_log.config("Caution: Zone (" + zoneId + ") from file: " + getCurrentFile().getName() + " overrides previos definition.");
						}
						
						if ((zoneName != null) && !zoneName.isEmpty())
						{
							temp.setName(zoneName);
						}
						
						addZone(zoneId, temp);
						
						int ax, ay, bx, by;
						for (int x = 0; x < worldRegions.length; x++)
						{
							for (int y = 0; y < worldRegions[x].length; y++)
							{
								ax = (x - L2World.OFFSET_X) << L2World.SHIFT_BY;
								bx = ((x + 1) - L2World.OFFSET_X) << L2World.SHIFT_BY;
								ay = (y - L2World.OFFSET_Y) << L2World.SHIFT_BY;
								by = ((y + 1) - L2World.OFFSET_Y) << L2World.SHIFT_BY;
								
								if (temp.getZone().intersectsRectangle(ax, bx, ay, by))
								{
									worldRegions[x][y].addZone(temp);
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public final void load()
	{
		_log.info(getClass().getSimpleName() + ": Loading zones...");
		_classZones.clear();
		
		long started = System.currentTimeMillis();
		
		parseDirectory("data/zones");
		
		started = System.currentTimeMillis() - started;
		_log.info(getClass().getSimpleName() + ": Loaded " + _classZones.size() + " zone classes and " + getSize() + " zones in " + (started / 1000) + " seconds.");
	}
	
	public int getSize()
	{
		int i = 0;
		for (Map<Integer, ? extends L2ZoneType> map : _classZones.values())
		{
			i += map.size();
		}
		return i;
	}
	
	public boolean checkId(int id)
	{
		for (Map<Integer, ? extends L2ZoneType> map : _classZones.values())
		{
			if (map.containsKey(id))
			{
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> void addZone(Integer id, T zone)
	{
		Map<Integer, T> map = (Map<Integer, T>) _classZones.get(zone.getClass());
		if (map == null)
		{
			map = new HashMap<>();
			map.put(id, zone);
			_classZones.put(zone.getClass(), map);
		}
		else
		{
			map.put(id, zone);
		}
	}
	
	@Deprecated
	public Collection<L2ZoneType> getAllZones()
	{
		List<L2ZoneType> zones = new ArrayList<>();
		for (Map<Integer, ? extends L2ZoneType> map : _classZones.values())
		{
			zones.addAll(map.values());
		}
		return zones;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> Collection<T> getAllZones(Class<T> zoneType)
	{
		return (Collection<T>) _classZones.get(zoneType).values();
	}
	
	public L2ZoneType getZoneById(int id)
	{
		for (Map<Integer, ? extends L2ZoneType> map : _classZones.values())
		{
			if (map.containsKey(id))
			{
				return map.get(id);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> T getZoneById(int id, Class<T> zoneType)
	{
		return (T) _classZones.get(zoneType).get(id);
	}
	
	public List<L2ZoneType> getZones(L2Object object)
	{
		return getZones(object.getX(), object.getY(), object.getZ());
	}
	
	public <T extends L2ZoneType> T getZone(L2Object object, Class<T> type)
	{
		if (object == null)
		{
			return null;
		}
		return getZone(object.getX(), object.getY(), object.getZ(), type);
	}
	
	public List<L2ZoneType> getZones(int x, int y)
	{
		L2WorldRegion region = L2World.getInstance().getRegion(x, y);
		List<L2ZoneType> temp = new ArrayList<>();
		for (L2ZoneType zone : region.getZones())
		{
			if (zone.isInsideZone(x, y))
			{
				temp.add(zone);
			}
		}
		return temp;
	}
	
	public List<L2ZoneType> getZones(int x, int y, int z)
	{
		L2WorldRegion region = L2World.getInstance().getRegion(x, y);
		List<L2ZoneType> temp = new ArrayList<>();
		for (L2ZoneType zone : region.getZones())
		{
			if (zone.isInsideZone(x, y, z))
			{
				temp.add(zone);
			}
		}
		return temp;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> T getZone(int x, int y, int z, Class<T> type)
	{
		L2WorldRegion region = L2World.getInstance().getRegion(x, y);
		for (L2ZoneType zone : region.getZones())
		{
			if (zone.isInsideZone(x, y, z) && type.isInstance(zone))
			{
				return (T) zone;
			}
		}
		return null;
	}
	
	public final L2ArenaZone getArena(L2Character character)
	{
		if (character == null)
		{
			return null;
		}
		
		for (L2ZoneType temp : ZoneManager.getInstance().getZones(character.getX(), character.getY(), character.getZ()))
		{
			if ((temp instanceof L2ArenaZone) && temp.isCharacterInZone(character))
			{
				return ((L2ArenaZone) temp);
			}
		}
		
		return null;
	}
	
	public final L2OlympiadStadiumZone getOlympiadStadium(L2Character character)
	{
		if (character == null)
		{
			return null;
		}
		
		for (L2ZoneType temp : ZoneManager.getInstance().getZones(character.getX(), character.getY(), character.getZ()))
		{
			if ((temp instanceof L2OlympiadStadiumZone) && temp.isCharacterInZone(character))
			{
				return ((L2OlympiadStadiumZone) temp);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> T getClosestZone(L2Object obj, Class<T> type)
	{
		T zone = getZone(obj, type);
		if (zone == null)
		{
			double closestdis = Double.MAX_VALUE;
			for (T temp : (Collection<T>) _classZones.get(type).values())
			{
				double distance = temp.getDistanceToZone(obj);
				if (distance < closestdis)
				{
					closestdis = distance;
					zone = temp;
				}
			}
		}
		return zone;
	}
	
	public List<L2ItemInstance> getDebugItems()
	{
		if (_debugItems == null)
		{
			_debugItems = new ArrayList<>();
		}
		return _debugItems;
	}
	
	public void clearDebugItems()
	{
		if (_debugItems != null)
		{
			Iterator<L2ItemInstance> it = _debugItems.iterator();
			while (it.hasNext())
			{
				L2ItemInstance item = it.next();
				if (item != null)
				{
					item.decayMe();
				}
				it.remove();
			}
		}
	}
	
	public static AbstractZoneSettings getSettings(String name)
	{
		return _settings.get(name);
	}
	
	public static final ZoneManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ZoneManager _instance = new ZoneManager();
	}
}