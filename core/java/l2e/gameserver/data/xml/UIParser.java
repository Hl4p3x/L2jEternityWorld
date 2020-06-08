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

import org.w3c.dom.Node;

import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.ActionKey;

public class UIParser extends DocumentParser
{
	private static final Logger _log = Logger.getLogger(UIParser.class.getName());
	
	private final Map<Integer, List<ActionKey>> _storedKeys = new HashMap<>();
	private final Map<Integer, List<Integer>> _storedCategories = new HashMap<>();
	
	protected UIParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_storedKeys.clear();
		_storedCategories.clear();
		parseDatapackFile("data/stats/chars/pcUi.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _storedKeys.size() + " keys " + _storedCategories.size() + " categories.");
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
					if ("category".equalsIgnoreCase(d.getNodeName()))
					{
						parseCategory(d);
					}
				}
			}
		}
	}
	
	private void parseCategory(Node n)
	{
		final int cat = parseInt(n.getAttributes(), "id");
		for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			if ("commands".equalsIgnoreCase(d.getNodeName()))
			{
				parseCommands(cat, d);
			}
			else if ("keys".equalsIgnoreCase(d.getNodeName()))
			{
				parseKeys(cat, d);
			}
		}
	}
	
	private void parseCommands(int cat, Node d)
	{
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("cmd".equalsIgnoreCase(c.getNodeName()))
			{
				addCategory(_storedCategories, cat, Integer.parseInt(c.getTextContent()));
			}
		}
	}
	
	private void parseKeys(int cat, Node d)
	{
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("key".equalsIgnoreCase(c.getNodeName()))
			{
				final ActionKey akey = new ActionKey(cat);
				for (int i = 0; i < c.getAttributes().getLength(); i++)
				{
					final Node att = c.getAttributes().item(i);
					final int val = Integer.parseInt(att.getNodeValue());
					switch (att.getNodeName())
					{
						case "cmd":
						{
							akey.setCommandId(val);
							break;
						}
						case "key":
						{
							akey.setKeyId(val);
							break;
						}
						case "toggleKey1":
						{
							akey.setToogleKey1(val);
							break;
						}
						case "toggleKey2":
						{
							akey.setToogleKey2(val);
							break;
						}
						case "showType":
						{
							akey.setShowStatus(val);
							break;
						}
					}
				}
				addKey(_storedKeys, cat, akey);
			}
		}
	}
	
	public static void addCategory(Map<Integer, List<Integer>> map, int cat, int cmd)
	{
		if (!map.containsKey(cat))
		{
			map.put(cat, new ArrayList<Integer>());
		}
		map.get(cat).add(cmd);
	}
	
	public static void addKey(Map<Integer, List<ActionKey>> map, int cat, ActionKey akey)
	{
		if (!map.containsKey(cat))
		{
			map.put(cat, new ArrayList<ActionKey>());
		}
		map.get(cat).add(akey);
	}
	
	public Map<Integer, List<Integer>> getCategories()
	{
		return _storedCategories;
	}
	
	public Map<Integer, List<ActionKey>> getKeys()
	{
		return _storedKeys;
	}
	
	public static UIParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final UIParser _instance = new UIParser();
	}
}