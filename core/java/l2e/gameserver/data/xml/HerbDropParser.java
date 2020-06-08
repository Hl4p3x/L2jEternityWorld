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
import java.util.logging.Logger;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.L2DropCategory;
import l2e.gameserver.model.L2DropData;

public class HerbDropParser extends DocumentParser
{
	private static Logger _log = Logger.getLogger(HerbDropParser.class.getName());
	
	private final Map<Integer, List<L2DropCategory>> _herbGroups = new HashMap<>();
	
	public static HerbDropParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected HerbDropParser()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_herbGroups.clear();
		parseDatapackFile("data/herbsDroplist.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _herbGroups.size() + " herbs groups.");
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
					if ("group".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						int groupId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
						
						List<L2DropCategory> category;
						if (_herbGroups.containsKey(groupId))
						{
							category = _herbGroups.get(groupId);
						}
						else
						{
							category = new ArrayList<>();
							_herbGroups.put(groupId, category);
						}
						
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							L2DropData dropDat = new L2DropData();
							if ("item".equalsIgnoreCase(cd.getNodeName()))
							{
								attrs = cd.getAttributes();
								int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
								int categoryType = Integer.parseInt(attrs.getNamedItem("category").getNodeValue());
								int chance = Integer.parseInt(attrs.getNamedItem("chance").getNodeValue());
								
								dropDat.setItemId(id);
								dropDat.setMinDrop(1);
								dropDat.setMaxDrop(1);
								dropDat.setChance(chance);
								
								if (ItemHolder.getInstance().getTemplate(dropDat.getItemId()) == null)
								{
									_log.warning(getClass().getSimpleName() + ": Herb data for undefined item template! GroupId: " + groupId + ", itemId: " + dropDat.getItemId());
									continue;
								}
								
								boolean catExists = false;
								for (L2DropCategory cat : category)
								{
									if (cat.getCategoryType() == categoryType)
									{
										cat.addDropData(dropDat, false);
										catExists = true;
										break;
									}
								}
								
								if (!catExists)
								{
									L2DropCategory cat = new L2DropCategory(categoryType);
									cat.addDropData(dropDat, false);
									category.add(cat);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public List<L2DropCategory> getHerbDroplist(int groupId)
	{
		return _herbGroups.get(groupId);
	}
	
	private static class SingletonHolder
	{
		protected static final HerbDropParser _instance = new HerbDropParser();
	}
}