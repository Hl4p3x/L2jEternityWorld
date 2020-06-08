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
package l2e.gameserver.model.entity.mods.conditions;

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.mods.base.Condition;
import l2e.gameserver.model.skills.L2Skill;

public class SkillEnchant extends Condition
{
	public SkillEnchant(Object value)
	{
		super(value);
		setName("Skill Enchant");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
		{
			return false;
		}
		
		int val = Integer.parseInt(getValue().toString());
		
		for (L2Skill s : player.getAllSkills())
		{
			String lvl = String.valueOf(s.getLevel());
			if (lvl.length() > 2)
			{
				int sklvl = Integer.parseInt(lvl.substring(1));
				if (sklvl >= val)
				{
					return true;
				}
			}
		}
		return false;
	}
}