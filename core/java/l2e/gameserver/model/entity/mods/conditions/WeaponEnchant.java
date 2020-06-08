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
import l2e.gameserver.model.items.instance.L2ItemInstance;

public class WeaponEnchant extends Condition
{
	public WeaponEnchant(Object value)
	{
		super(value);
		setName("Weapon Enchant");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
		{
			return false;
		}
		
		int val = Integer.parseInt(getValue().toString());
		
		L2ItemInstance weapon = player.getInventory().getPaperdollItem(5);
		
		if (weapon != null)
		{
			if (weapon.getEnchantLevel() >= val)
			{
				return true;
			}
		}
		return false;
	}
}