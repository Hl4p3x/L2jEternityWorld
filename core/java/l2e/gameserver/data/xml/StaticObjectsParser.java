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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2e.gameserver.model.actor.templates.L2CharTemplate;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class StaticObjectsParser extends DocumentParser
{
	private static final Map<Integer, L2StaticObjectInstance> _StaticObjectsParser = new HashMap<>();
	
	protected StaticObjectsParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_StaticObjectsParser.clear();
		parseDatapackFile("data/staticObjects.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _StaticObjectsParser.size() + " StaticObject Templates.");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node att;
		StatsSet set;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("object".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						addObject(set);
					}
				}
			}
		}
	}
	
	private void addObject(StatsSet set)
	{
		L2StaticObjectInstance obj = new L2StaticObjectInstance(IdFactory.getInstance().getNextId(), new L2CharTemplate(new StatsSet()), set.getInteger("id"));
		obj.setType(set.getInteger("type", 0));
		obj.setName(set.getString("name"));
		obj.setMap(set.getString("texture", "none"), set.getInteger("map_x", 0), set.getInteger("map_y", 0));
		obj.spawnMe(set.getInteger("x"), set.getInteger("y"), set.getInteger("z"));
		_StaticObjectsParser.put(obj.getObjectId(), obj);
	}
	
	public Collection<L2StaticObjectInstance> getStaticObjects()
	{
		return _StaticObjectsParser.values();
	}
	
	public static StaticObjectsParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final StaticObjectsParser _instance = new StaticObjectsParser();
	}
}