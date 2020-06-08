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

import java.util.List;

import l2e.gameserver.model.holders.ItemsHolder;

public class L2ExtractableProductItem
{
	private final List<ItemsHolder> _items;
	private final double _chance;
	
	public L2ExtractableProductItem(List<ItemsHolder> items, double chance)
	{
		_items = items;
		_chance = chance;
	}
	
	public List<ItemsHolder> getItems()
	{
		return _items;
	}
	
	public double getChance()
	{
		return _chance;
	}
}