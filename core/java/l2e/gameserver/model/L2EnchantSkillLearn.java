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
package l2e.gameserver.model;

import java.util.Set;
import java.util.TreeMap;

import l2e.gameserver.data.xml.EnchantSkillGroupsParser;
import l2e.gameserver.model.L2EnchantSkillGroup.EnchantSkillsHolder;

public final class L2EnchantSkillLearn
{
	private final int _id;
	private final int _baseLvl;
	private final TreeMap<Integer, Integer> _enchantRoutes = new TreeMap<>();
	
	public L2EnchantSkillLearn(int id, int baseLvl)
	{
		_id = id;
		_baseLvl = baseLvl;
	}
	
	public void addNewEnchantRoute(int route, int group)
	{
		_enchantRoutes.put(route, group);
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getBaseLevel()
	{
		return _baseLvl;
	}
	
	public static int getEnchantRoute(int level)
	{
		return (int) Math.floor(level / 100);
	}
	
	public static int getEnchantIndex(int level)
	{
		return (level % 100) - 1;
	}
	
	public static int getEnchantType(int level)
	{
		return ((level - 1) / 100) - 1;
	}
	
	public L2EnchantSkillGroup getFirstRouteGroup()
	{
		return EnchantSkillGroupsParser.getInstance().getEnchantSkillGroupById(_enchantRoutes.firstEntry().getValue());
	}
	
	public Set<Integer> getAllRoutes()
	{
		return _enchantRoutes.keySet();
	}
	
	public int getMinSkillLevel(int level)
	{
		if ((level % 100) == 1)
		{
			return _baseLvl;
		}
		return level - 1;
	}
	
	public boolean isMaxEnchant(int level)
	{
		int enchantType = L2EnchantSkillLearn.getEnchantRoute(level);
		if ((enchantType < 1) || !_enchantRoutes.containsKey(enchantType))
		{
			return false;
		}
		int index = L2EnchantSkillLearn.getEnchantIndex(level);
		
		if ((index + 1) >= EnchantSkillGroupsParser.getInstance().getEnchantSkillGroupById(_enchantRoutes.get(enchantType)).getEnchantGroupDetails().size())
		{
			return true;
		}
		return false;
	}
	
	public EnchantSkillsHolder getEnchantSkillsHolder(int level)
	{
		int enchantType = L2EnchantSkillLearn.getEnchantRoute(level);
		if ((enchantType < 1) || !_enchantRoutes.containsKey(enchantType))
		{
			return null;
		}
		int index = L2EnchantSkillLearn.getEnchantIndex(level);
		L2EnchantSkillGroup group = EnchantSkillGroupsParser.getInstance().getEnchantSkillGroupById(_enchantRoutes.get(enchantType));
		
		if (index < 0)
		{
			return group.getEnchantGroupDetails().get(0);
		}
		else if (index >= group.getEnchantGroupDetails().size())
		{
			return group.getEnchantGroupDetails().get(EnchantSkillGroupsParser.getInstance().getEnchantSkillGroupById(_enchantRoutes.get(enchantType)).getEnchantGroupDetails().size() - 1);
		}
		return group.getEnchantGroupDetails().get(index);
	}
}