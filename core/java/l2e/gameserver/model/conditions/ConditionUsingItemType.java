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
package l2e.gameserver.model.conditions;

import l2e.gameserver.model.itemcontainer.Inventory;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.type.L2ArmorType;
import l2e.gameserver.model.stats.Env;

public final class ConditionUsingItemType extends Condition
{
	private final boolean _armor;
	private final int _mask;
	
	public ConditionUsingItemType(int mask)
	{
		_mask = mask;
		_armor = (_mask & (L2ArmorType.MAGIC.mask() | L2ArmorType.LIGHT.mask() | L2ArmorType.HEAVY.mask())) != 0;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getCharacter() == null || !env.getCharacter().isPlayer())
		{
			return false;
		}
		
		final Inventory inv = env.getPlayer().getInventory();

		if (_armor)
		{
			L2ItemInstance chest = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if (chest == null)
			{
				return false;
			}
			int chestMask = chest.getItem().getItemMask();
			
			if ((_mask & chestMask) == 0)
			{
				return false;
			}
			
			int chestBodyPart = chest.getItem().getBodyPart();

			if (chestBodyPart == L2Item.SLOT_FULL_ARMOR)
			{
				return true;
			}
			L2ItemInstance legs = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			if (legs == null)
			{
				return false;
			}
			int legMask = legs.getItem().getItemMask();
			return (_mask & legMask) != 0;
		}
		return (_mask & inv.getWearedMask()) != 0;
	}
}