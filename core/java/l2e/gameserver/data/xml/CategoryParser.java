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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.CategoryType;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class CategoryParser extends DocumentParser
{
	private static final Logger _log = Logger.getLogger(CategoryParser.class.getName());
	
	private final Map<CategoryType, Set<Integer>> _categories = new HashMap<>();
	
	protected CategoryParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/stats/categoryData.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _categories.size() + " Categories.");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node node = getCurrentDocument().getFirstChild(); node != null; node = node.getNextSibling())
		{
			if ("list".equalsIgnoreCase(node.getNodeName()))
			{
				for (Node list_node = node.getFirstChild(); list_node != null; list_node = list_node.getNextSibling())
				{
					if ("category".equalsIgnoreCase(list_node.getNodeName()))
					{
						final NamedNodeMap attrs = list_node.getAttributes();
						final CategoryType categoryType = CategoryType.findByName(attrs.getNamedItem("name").getNodeValue());
						if (categoryType == null)
						{
							_log.log(Level.WARNING, getClass().getSimpleName() + ": Can't find category by name :" + attrs.getNamedItem("name").getNodeValue());
							continue;
						}
						
						final Set<Integer> ids = new HashSet<>();
						for (Node category_node = list_node.getFirstChild(); category_node != null; category_node = category_node.getNextSibling())
						{
							if ("id".equalsIgnoreCase(category_node.getNodeName()))
							{
								ids.add(Integer.parseInt(category_node.getTextContent()));
							}
						}
						_categories.put(categoryType, ids);
					}
				}
			}
		}
	}
	
	public boolean isInCategory(CategoryType type, int id)
	{
		final Set<Integer> category = getCategoryByType(type);
		if (category == null)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Can't find category type :" + type);
			return false;
		}
		return category.contains(id);
	}
	
	public Set<Integer> getCategoryByType(CategoryType type)
	{
		return _categories.get(type);
	}
	
	public static CategoryParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected final static CategoryParser _instance = new CategoryParser();
	}
}