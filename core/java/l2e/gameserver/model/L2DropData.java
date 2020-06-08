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

import java.util.Arrays;

public class L2DropData
{
	public static final int MAX_CHANCE = 1000000;
	
	private int _itemId;
	private int _minDrop;
	private int _maxDrop;
	private double _chance;
	private String _questID = null;
	private String[] _stateID = null;
	
	public L2DropData()
	{
	}

	public L2DropData(int id, int min, int max, double chance)
	{
		_itemId = id;
		_minDrop = min;
		_maxDrop = max;
		_chance = chance;
	}

	public int getItemId()
	{
		return _itemId;
	}
	
	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}
	
	public int getMinDrop()
	{
		return _minDrop;
	}
	
	public int getMaxDrop()
	{
		return _maxDrop;
	}
	
	public double getChance()
	{
		return _chance;
	}
	
	public void setMinDrop(int mindrop)
	{
		_minDrop = mindrop;
	}
	
	public void setMaxDrop(int maxdrop)
	{
		_maxDrop = maxdrop;
	}
	
	public void setChance(double chance)
	{
		_chance = chance;
	}

	public String[] getStateIDs()
	{
		return _stateID;
	}
	
	public void addStates(String[] list)
	{
		_stateID = list;
	}
	
	public String getQuestID()
	{
		return _questID;
	}
	
	public void setQuestID(String questID)
	{
		_questID = questID;
	}
	
	public boolean isQuestDrop()
	{
		return _questID != null && _stateID != null;
	}
	
	@Override
	public String toString()
	{
		String out = "ItemID: " + getItemId() + " Min: " + getMinDrop() +
		" Max: " + getMaxDrop() + " Chance: " + (getChance() / 10000.0) + "%";
		if (isQuestDrop())
		{
			out += " QuestID: " + getQuestID() + " StateID's: " + Arrays.toString(getStateIDs());
		}
		
		return out;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + _itemId;
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof L2DropData))
			return false;
		final L2DropData other = (L2DropData) obj;
		if (_itemId != other._itemId)
			return false;
		return true;
	}
}