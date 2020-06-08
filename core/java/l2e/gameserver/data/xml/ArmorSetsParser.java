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
import l2e.gameserver.model.L2ArmorSet;
import l2e.gameserver.model.holders.SkillsHolder;

public final class ArmorSetsParser extends DocumentParser
{
	private static final Map<Integer, L2ArmorSet> _armorSets = new HashMap<>();
	
	protected ArmorSetsParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_armorSets.clear();
		parseDirectory("data/stats/armorsets");
		_log.info(getClass().getSimpleName() + ": Loaded " + _armorSets.size() + " Armor sets.");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		L2ArmorSet set;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("set".equalsIgnoreCase(d.getNodeName()))
					{
						set = new L2ArmorSet();
						for (Node a = d.getFirstChild(); a != null; a = a.getNextSibling())
						{
							attrs = a.getAttributes();
							switch (a.getNodeName())
							{
								case "chest":
								{
									set.addChest(parseInt(attrs, "id"));
									break;
								}
								case "feet":
								{
									set.addFeet(parseInt(attrs, "id"));
									break;
								}
								case "gloves":
								{
									set.addGloves(parseInt(attrs, "id"));
									break;
								}
								case "head":
								{
									set.addHead(parseInt(attrs, "id"));
									break;
								}
								case "legs":
								{
									set.addLegs(parseInt(attrs, "id"));
									break;
								}
								case "shield":
								{
									set.addShield(parseInt(attrs, "id"));
									break;
								}
								case "skill":
								{
									int skillId = parseInt(attrs, "id");
									int skillLevel = parseInt(attrs, "level");
									set.addSkill(new SkillsHolder(skillId, skillLevel));
									break;
								}
								case "shield_skill":
								{
									int skillId = parseInt(attrs, "id");
									int skillLevel = parseInt(attrs, "level");
									set.addShieldSkill(new SkillsHolder(skillId, skillLevel));
									break;
								}
								case "enchant6skill":
								{
									int skillId = parseInt(attrs, "id");
									int skillLevel = parseInt(attrs, "level");
									set.addEnchant6Skill(new SkillsHolder(skillId, skillLevel));
									break;
								}
								case "con":
								{
									set.addCon(parseInt(attrs, "val"));
									break;
								}
								case "dex":
								{
									set.addDex(parseInt(attrs, "val"));
									break;
								}
								case "str":
								{
									set.addStr(parseInt(attrs, "val"));
									break;
								}
								case "men":
								{
									set.addMen(parseInt(attrs, "val"));
									break;
								}
								case "wit":
								{
									set.addWit(parseInt(attrs, "val"));
									break;
								}
								case "int":
								{
									set.addInt(parseInt(attrs, "val"));
									break;
								}
							}
						}
						_armorSets.put(set.getChestId(), set);
					}
				}
			}
		}
	}
	
	public boolean isArmorSet(int chestId)
	{
		return _armorSets.containsKey(chestId);
	}
	
	public L2ArmorSet getSet(int chestId)
	{
		return _armorSets.get(chestId);
	}
	
	public static ArmorSetsParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ArmorSetsParser _instance = new ArmorSetsParser();
	}
}