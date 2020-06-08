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

import org.w3c.dom.Node;

import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.base.ClassId;

public final class SkillLearnParser extends DocumentParser
{
	private final Map<Integer, List<ClassId>> _skillLearn = new HashMap<>();
	
	protected SkillLearnParser()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		parseDatapackFile("data/stats/chars/pcSkillLearn.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _skillLearn.size() + " Skill Learn data.");
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
					if ("npc".equalsIgnoreCase(list_node.getNodeName()))
					{
						final List<ClassId> classIds = new ArrayList<>();
						for (Node c = list_node.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if ("classId".equalsIgnoreCase(c.getNodeName()))
							{
								classIds.add(ClassId.getClassId(Integer.parseInt(c.getTextContent())));
							}
						}
						_skillLearn.put(parseInteger(list_node.getAttributes(), "id"), classIds);
					}
				}
			}
		}
	}
	
	public List<ClassId> getSkillLearnData(int npcId)
	{
		return _skillLearn.get(npcId);
	}
	
	public static SkillLearnParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected final static SkillLearnParser _instance = new SkillLearnParser();
	}
}