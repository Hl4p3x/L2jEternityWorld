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
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.gameserver.engines.DocumentParser;

/**
 * Rework by LordWinter 04.07.2013 Fixed by L2J Eternity-World
 */
public class ItemIconsParser extends DocumentParser
{
	private final static Map<Integer, String> _icon = new HashMap<>();
	
	protected ItemIconsParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_icon.clear();
		parseDatapackFile("data/itemIcons.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _icon.size() + " item icons.");
	}
	
	@Override
	protected void parseDocument()
	{
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equals(n.getNodeName()))
			{
				NamedNodeMap attrs;
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equals(d.getNodeName()))
					{
						attrs = d.getAttributes();
						_icon.put(parseInteger(attrs, "id"), attrs.getNamedItem("icon").getNodeValue());
					}
				}
			}
		}
	}
	
	public static String getIcon(int itemId)
	{
		return _icon.get(itemId);
	}
	
	public static ItemIconsParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemIconsParser _instance = new ItemIconsParser();
	}
}