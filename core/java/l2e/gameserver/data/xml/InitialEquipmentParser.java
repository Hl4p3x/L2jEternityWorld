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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.Config;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.items.PcItemTemplate;

public final class InitialEquipmentParser extends DocumentParser
{
	private static final String NORMAL = "data/stats/initialEquipment.xml";
	private static final String EVENT = "data/stats/initialEquipmentEvent.xml";
	private final Map<ClassId, List<PcItemTemplate>> _initialEquipmentList = new HashMap<>();
	
	protected InitialEquipmentParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_initialEquipmentList.clear();
		parseDatapackFile(Config.INITIAL_EQUIPMENT_EVENT ? EVENT : NORMAL);
		_log.info(getClass().getSimpleName() + ": Loaded " + _initialEquipmentList.size() + " Initial Equipment data.");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("equipment".equalsIgnoreCase(d.getNodeName()))
					{
						parseEquipment(d);
					}
				}
			}
		}
	}
	
	private void parseEquipment(Node d)
	{
		NamedNodeMap attrs = d.getAttributes();
		Node attr;
		final ClassId classId = ClassId.getClassId(Integer.parseInt(attrs.getNamedItem("classId").getNodeValue()));
		final List<PcItemTemplate> equipList = new ArrayList<>();
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("item".equalsIgnoreCase(c.getNodeName()))
			{
				final StatsSet set = new StatsSet();
				attrs = c.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					attr = attrs.item(i);
					set.set(attr.getNodeName(), attr.getNodeValue());
				}
				equipList.add(new PcItemTemplate(set));
			}
		}
		_initialEquipmentList.put(classId, equipList);
	}
	
	public List<PcItemTemplate> getEquipmentList(ClassId cId)
	{
		return _initialEquipmentList.get(cId);
	}
	
	public List<PcItemTemplate> getEquipmentList(int cId)
	{
		return _initialEquipmentList.get(ClassId.getClassId(cId));
	}
	
	public static InitialEquipmentParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final InitialEquipmentParser _instance = new InitialEquipmentParser();
	}
}