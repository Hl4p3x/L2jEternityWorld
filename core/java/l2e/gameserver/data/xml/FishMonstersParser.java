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
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.fishing.L2FishingMonster;

public final class FishMonstersParser extends DocumentParser
{
	private static final Map<Integer, L2FishingMonster> _fishingMonstersData = new HashMap<>();
	
	protected FishMonstersParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_fishingMonstersData.clear();
		parseDatapackFile("data/stats/fishing/fishingMonsters.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _fishingMonstersData.size() + " Fishing Monsters.");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node att;
		L2FishingMonster fishingMonster;
		StatsSet set;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("fishingMonster".equalsIgnoreCase(d.getNodeName()))
					{
						
						attrs = d.getAttributes();
						
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						fishingMonster = new L2FishingMonster(set);
						_fishingMonstersData.put(fishingMonster.getFishingMonsterId(), fishingMonster);
					}
				}
			}
		}
	}
	
	public L2FishingMonster getFishingMonster(int lvl)
	{
		for (L2FishingMonster fishingMonster : _fishingMonstersData.values())
		{
			if ((lvl >= fishingMonster.getUserMinLevel()) && (lvl <= fishingMonster.getUserMaxLevel()))
			{
				return fishingMonster;
			}
		}
		return null;
	}
	
	public L2FishingMonster getFishingMonsterById(int id)
	{
		if (_fishingMonstersData.containsKey(id))
		{
			return _fishingMonstersData.get(id);
		}
		return null;
	}
	
	public static FishMonstersParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FishMonstersParser _instance = new FishMonstersParser();
	}
}