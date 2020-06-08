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

import l2e.gameserver.model.items.instance.L2ItemInstance;

public final class TempItem
{
	private int _itemId;
	private int _quantity;
	private int _referencePrice;
	private String _itemName;
	
	public TempItem(L2ItemInstance item, int quantity)
	{
		super();
		_itemId = item.getId();
		_quantity = quantity;
		_itemName = item.getItem().getName();
		_referencePrice = item.getReferencePrice();
	}
	
	public int getQuantity()
	{
		return _quantity;
	}
	
	public void setQuantity(int quantity)
	{
		_quantity = quantity;
	}
	
	public int getReferencePrice()
	{
		return _referencePrice;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public String getItemName()
	{
		return _itemName;
	}
}