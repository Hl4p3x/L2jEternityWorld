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
package l2e.gameserver.model.quest;

import org.apache.commons.lang.ArrayUtils;

public class QuestDrop
{
	public final int condition;
	public final int maxcount;
	public final int chance;
	
	public int[] itemList = ArrayUtils.EMPTY_INT_ARRAY;
	
	public QuestDrop(int condition, int maxcount, int chance)
	{
		this.condition = condition;
		this.maxcount = maxcount;
		this.chance = chance;
	}
	
	public QuestDrop addItem(int item)
	{
		itemList = ArrayUtils.add(itemList, item);
		return this;
	}
}