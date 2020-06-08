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
package l2e.gameserver.model.items;

import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.type.L2ItemType;

public class L2WarehouseItem
{
	private final L2Item _item;
	private final int _object;
	private final long _count;
	private final int _owner;
	private final int _locationSlot;
	private final int _enchant;
	private final int _grade;
	private boolean _isAugmented;
	private int _augmentationId;
	private final int _customType1;
	private final int _customType2;
	private final int _mana;
	
	private int _elemAtkType = -2;
	private int _elemAtkPower = 0;
	private final int[] _elemDefAttr =
	{
		0,
		0,
		0,
		0,
		0,
		0
	};
	
	private final int[] _enchantOptions;
	
	private final int _time;
	
	public L2WarehouseItem(L2ItemInstance item)
	{
		_item = item.getItem();
		_object = item.getObjectId();
		_count = item.getCount();
		_owner = item.getOwnerId();
		_locationSlot = item.getLocationSlot();
		_enchant = item.getEnchantLevel();
		_customType1 = item.getCustomType1();
		_customType2 = item.getCustomType2();
		_grade = item.getItem().getItemGrade();
		if (item.isAugmented())
		{
			_isAugmented = true;
			_augmentationId = item.getAugmentation().getAugmentationId();
		}
		else
		{
			_isAugmented = false;
		}
		_mana = item.getMana();
		_time = item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -1;
		
		_elemAtkType = item.getAttackElementType();
		_elemAtkPower = item.getAttackElementPower();
		for (byte i = 0; i < 6; i++)
		{
			_elemDefAttr[i] = item.getElementDefAttr(i);
		}
		_enchantOptions = item.getEnchantOptions();
	}
	
	public L2Item getItem()
	{
		return _item;
	}
	
	public final int getObjectId()
	{
		return _object;
	}
	
	public final int getOwnerId()
	{
		return _owner;
	}
	
	public final int getLocationSlot()
	{
		return _locationSlot;
	}
	
	public final long getCount()
	{
		return _count;
	}
	
	public final int getType1()
	{
		return _item.getType1();
	}
	
	public final int getType2()
	{
		return _item.getType2();
	}
	
	public final L2ItemType getItemType()
	{
		return _item.getItemType();
	}
	
	public final int getItemId()
	{
		return _item.getId();
	}
	
	public final int getBodyPart()
	{
		return _item.getBodyPart();
	}
	
	public final int getEnchantLevel()
	{
		return _enchant;
	}
	
	public final int getItemGrade()
	{
		return _grade;
	}
	
	public final boolean isWeapon()
	{
		return (_item instanceof L2Weapon);
	}
	
	public final boolean isArmor()
	{
		return (_item instanceof L2Armor);
	}
	
	public final boolean isEtcItem()
	{
		return (_item instanceof L2EtcItem);
	}
	
	public String getItemName()
	{
		return _item.getName();
	}
	
	public boolean isAugmented()
	{
		return _isAugmented;
	}
	
	public int getAugmentationId()
	{
		return _augmentationId;
	}
	
	public String getName()
	{
		return _item.getName();
	}
	
	public final int getCustomType1()
	{
		return _customType1;
	}
	
	public final int getCustomType2()
	{
		return _customType2;
	}
	
	public final int getMana()
	{
		return _mana;
	}
	
	public int getAttackElementType()
	{
		return _elemAtkType;
	}
	
	public int getAttackElementPower()
	{
		return _elemAtkPower;
	}
	
	public int getElementDefAttr(byte i)
	{
		return _elemDefAttr[i];
	}
	
	public int[] getEnchantOptions()
	{
		return _enchantOptions;
	}
	
	public int getTime()
	{
		return _time;
	}
	
	@Override
	public String toString()
	{
		return _item.toString();
	}
}