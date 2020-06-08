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
package l2e.gameserver.data.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.templates.L2DoorTemplate;

public class DoorParser extends DocumentParser
{
	private final Map<Integer, L2DoorInstance> _doors = new HashMap<>();
	private static final Map<String, Set<Integer>> _groups = new HashMap<>();
	private final Map<Integer, List<L2DoorInstance>> _regions = new HashMap<>();
	
	protected DoorParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_doors.clear();
		_groups.clear();
		_regions.clear();
		parseDatapackFile("data/doors.xml");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node att;
		StatsSet set;
		for (Node a = getCurrentDocument().getFirstChild(); a != null; a = a.getNextSibling())
		{
			if ("list".equalsIgnoreCase(a.getNodeName()))
			{
				for (Node b = a.getFirstChild(); b != null; b = b.getNextSibling())
				{
					if ("door".equalsIgnoreCase(b.getNodeName()))
					{
						attrs = b.getAttributes();
						set = new StatsSet();
						set.set("baseHpMax", 1);
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						makeDoor(set);
					}
				}
			}
		}
		_log.info(getClass().getSimpleName() + ": Loaded " + _doors.size() + " Door Templates for " + _regions.size() + " regions.");
	}
	
	public void insertCollisionData(StatsSet set)
	{
		int posX, posY, nodeX, nodeY, height;
		height = set.getInteger("height");
		String[] pos = set.getString("node1").split(",");
		nodeX = Integer.parseInt(pos[0]);
		nodeY = Integer.parseInt(pos[1]);
		pos = set.getString("node2").split(",");
		posX = Integer.parseInt(pos[0]);
		posY = Integer.parseInt(pos[1]);
		int collisionRadius;
		collisionRadius = Math.min(Math.abs(nodeX - posX), Math.abs(nodeY - posY));
		if (collisionRadius < 20)
		{
			collisionRadius = 20;
		}
		set.set("collision_radius", collisionRadius);
		set.set("collision_height", height / 4);
	}
	
	private void makeDoor(StatsSet set)
	{
		insertCollisionData(set);
		
		L2DoorTemplate template = new L2DoorTemplate(set);
		L2DoorInstance door = new L2DoorInstance(IdFactory.getInstance().getNextId(), template, set);
		door.setCurrentHp(door.getMaxHp());
		
		int x = template.posX;
		int y = template.posY;
		int z = template.posZ;
		
		int xa = (template.getRange().getPoints()[0].getX() + template.getRange().getPoints()[1].getX() + template.getRange().getPoints()[2].getX() + template.getRange().getPoints()[3].getX()) / 4;
		int ya = (template.getRange().getPoints()[0].getY() + template.getRange().getPoints()[1].getY() + template.getRange().getPoints()[2].getY() + template.getRange().getPoints()[3].getY()) / 4;
		
		x = xa;
		y = ya;
		
		int gz = GeoClient.getInstance().getHeight(x, y, z);
		if (Math.abs(gz - z) > 32)
		{
			z = gz;
		}
		door.setRange(template.getRange());
		
		if ((door.getDoorId() == 20250777) || (door.getDoorId() == 20250778))
		{
			door.setIsGeoReverted(true);
		}
		door.spawnMe(x, y, z);
		putDoor(door, MapRegionManager.getInstance().getMapRegionLocId(door));
	}
	
	public L2DoorTemplate getDoorTemplate(int doorId)
	{
		return _doors.get(doorId).getTemplate();
	}
	
	public L2DoorInstance getDoor(int doorId)
	{
		return _doors.get(doorId);
	}
	
	public void putDoor(L2DoorInstance door, int region)
	{
		_doors.put(door.getDoorId(), door);
		
		if (!_regions.containsKey(region))
		{
			_regions.put(region, new ArrayList<L2DoorInstance>());
		}
		_regions.get(region).add(door);
	}
	
	public static void addDoorGroup(String groupName, int doorId)
	{
		Set<Integer> set = _groups.get(groupName);
		if (set == null)
		{
			set = new HashSet<>();
			_groups.put(groupName, set);
		}
		set.add(doorId);
	}
	
	public static Set<Integer> getDoorsByGroup(String groupName)
	{
		return _groups.get(groupName);
	}
	
	public Collection<L2DoorInstance> getDoors()
	{
		return _doors.values();
	}
	
	public static DoorParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DoorParser _instance = new DoorParser();
	}
}