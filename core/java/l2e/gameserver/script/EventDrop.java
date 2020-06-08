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
package l2e.gameserver.script;

public class EventDrop
{
	private final int[] _itemIdList;
	private final int _minCount;
	private final int _maxCount;
	private final int _dropChance;
	
	public EventDrop(int[] itemIdList, int minCount, int maxCount, int dropChance)
	{
		_itemIdList = itemIdList;
		_minCount = minCount;
		_maxCount = maxCount;
		_dropChance = dropChance;
	}
	
	public EventDrop(int itemId, int minCount, int maxCount, int dropChance)
	{
		_itemIdList = new int[]{ itemId };
		_minCount = minCount;
		_maxCount = maxCount;
		_dropChance = dropChance;
	}
	
	public int[] getItemIdList()
	{
		return _itemIdList;
	}
	
	public int getMinCount()
	{
		return _minCount;
	}
	
	public int getMaxCount()
	{
		return _maxCount;
	}
	
	public int getDropChance()
	{
		return _dropChance;
	}
}