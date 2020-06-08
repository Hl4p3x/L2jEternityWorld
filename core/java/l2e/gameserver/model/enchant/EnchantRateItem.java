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
package l2e.gameserver.model.enchant;

import l2e.gameserver.model.items.L2Item;

public final class EnchantRateItem
{
	private final String _name;
	private int _itemId;
	private int _slot;
	private Boolean _isMagicWeapon = null;
	
	public EnchantRateItem(String name)
	{
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setItemId(int id)
	{
		_itemId = id;
	}
	
	public void addSlot(int slot)
	{
		_slot |= slot;
	}
	
	public void setMagicWeapon(boolean magicWeapon)
	{
		_isMagicWeapon = magicWeapon;
	}
	
	public boolean validate(L2Item item)
	{
		if ((_itemId != 0) && (_itemId != item.getId()))
		{
			return false;
		}
		else if ((_slot != 0) && ((item.getBodyPart() & _slot) == 0))
		{
			return false;
		}
		else if ((_isMagicWeapon != null) && (item.isMagicWeapon() != _isMagicWeapon))
		{
			return false;
		}
		return true;
	}
}