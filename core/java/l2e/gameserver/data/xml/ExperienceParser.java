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

public final class ExperienceParser extends DocumentParser
{
	private final Map<Integer, Long> _expTable = new HashMap<>();
	
	private byte MAX_LEVEL;
	private byte MAX_PET_LEVEL;
	
	protected ExperienceParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_expTable.clear();
		parseDatapackFile("data/stats/experience.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _expTable.size() + " levels.");
		_log.info(getClass().getSimpleName() + ": Max Player Level is: " + (MAX_LEVEL - 1));
		_log.info(getClass().getSimpleName() + ": Max Pet Level is: " + (MAX_PET_LEVEL - 1));
	}
	
	@Override
	protected void parseDocument()
	{
		final Node table = getCurrentDocument().getFirstChild();
		final NamedNodeMap tableAttr = table.getAttributes();
		
		MAX_LEVEL = (byte) (Byte.parseByte(tableAttr.getNamedItem("maxLevel").getNodeValue()) + 1);
		MAX_PET_LEVEL = (byte) (Byte.parseByte(tableAttr.getNamedItem("maxPetLevel").getNodeValue()) + 1);
		
		NamedNodeMap attrs;
		for (Node n = table.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("experience".equals(n.getNodeName()))
			{
				attrs = n.getAttributes();
				_expTable.put(parseInteger(attrs, "level"), parseLong(attrs, "tolevel"));
			}
		}
	}
	
	public long getExpForLevel(int level)
	{
		return _expTable.get(level);
	}
	
	public byte getMaxLevel()
	{
		return MAX_LEVEL;
	}
	
	public byte getMaxPetLevel()
	{
		return MAX_PET_LEVEL;
	}
	
	public static ExperienceParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ExperienceParser _instance = new ExperienceParser();
	}
}