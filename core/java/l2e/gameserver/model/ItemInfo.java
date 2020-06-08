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

import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;

public class ItemInfo
{
	private int _objectId;
	private L2Item _item;
	private int _enchant;
	private int _augmentation;
	private long _count;
	private int _price;
	private int _type1;
	private int _type2;
	private int _equipped;
	private int _change;
	
	private int _mana;
	private int _time;
	
	private int _location;
	
	private int _elemAtkType = -2;
	private int _elemAtkPower = 0;
	private int[] _elemDefAttr = {0, 0, 0, 0, 0, 0};

	private int[] _option;
	
	public ItemInfo(L2ItemInstance item)
	{
		if (item == null)
			return;

		_objectId = item.getObjectId();
		_item = item.getItem();
		_enchant = item.getEnchantLevel();

		if (item.isAugmented()) _augmentation = item.getAugmentation().getAugmentationId();
		else _augmentation = 0;
		
		_count = item.getCount();
		_type1 = item.getCustomType1();
		_type2 = item.getCustomType2();
		_equipped = item.isEquipped() ? 1 : 0;
		
		switch (item.getLastChange())
		{
			case (L2ItemInstance.ADDED): { _change = 1; break; }
			case (L2ItemInstance.MODIFIED): { _change = 2; break; }
			case (L2ItemInstance.REMOVED): { _change = 3; break;}
		}
		_mana = item.getMana();
		_time = item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -9999;
		_location = item.getLocationSlot();
		
		_elemAtkType = item.getAttackElementType();
		_elemAtkPower = item.getAttackElementPower();
		for (byte i = 0; i < 6; i++)
		{
			_elemDefAttr[i] = item.getElementDefAttr(i);
		}
		_option = item.getEnchantOptions();
	}
	
	public ItemInfo(L2ItemInstance item, int change)
	{
		if (item == null)
			return;
		
		_objectId = item.getObjectId();
		
		_item = item.getItem();
		_enchant = item.getEnchantLevel();
		if (item.isAugmented()) _augmentation = item.getAugmentation().getAugmentationId();
		else _augmentation = 0;
		
		_count = item.getCount();
		_type1 = item.getCustomType1();
		_type2 = item.getCustomType2();
		_equipped = item.isEquipped() ? 1 : 0;
		_change = change;
		_mana = item.getMana();
		_time = item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -9999;
		
		_location = item.getLocationSlot();
		
		_elemAtkType = item.getAttackElementType();
		_elemAtkPower = item.getAttackElementPower();
		for (byte i = 0; i < 6; i++)
		{
			_elemDefAttr[i] = item.getElementDefAttr(i);
		}
		_option = item.getEnchantOptions();
	}
	
	public int getObjectId(){return _objectId;}
	public L2Item getItem(){return _item;}
	public int getEnchant(){return _enchant;}
	public int getAugmentationBonus(){return _augmentation;}
	public long getCount(){return _count;}
	public int getPrice(){return _price;}
	public int getCustomType1(){return _type1;}
	public int getCustomType2(){return _type2;}
	public int getEquipped(){return _equipped;}
	public int getChange(){return _change;}
	public int getMana(){return _mana;}
	public int getTime(){return _time;}
	public int getLocation(){return _location;}
	public int getAttackElementType(){return _elemAtkType;}
	public int getAttackElementPower(){return _elemAtkPower;}
	public int getElementDefAttr(byte i){return _elemDefAttr[i];}

	public int[] getEnchantOptions()
	{
		return _option;
	}
}