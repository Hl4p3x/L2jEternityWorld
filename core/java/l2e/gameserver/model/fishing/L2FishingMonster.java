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
package l2e.gameserver.model.fishing;

import l2e.gameserver.model.StatsSet;

public class L2FishingMonster
{
	private final int _userMinLevel;
	private final int _userMaxLevel;
	private final int _fishingMonsterId;
	private final int _probability;
	
	public L2FishingMonster(StatsSet set)
	{
		_userMinLevel = set.getInteger("userMinLevel");
		_userMaxLevel = set.getInteger("userMaxLevel");
		_fishingMonsterId = set.getInteger("fishingMonsterId");
		_probability = set.getInteger("probability");
	}
	
	public int getUserMinLevel()
	{
		return _userMinLevel;
	}
	
	public int getUserMaxLevel()
	{
		return _userMaxLevel;
	}
	
	public int getFishingMonsterId()
	{
		return _fishingMonsterId;
	}
	
	public int getProbability()
	{
		return _probability;
	}
}