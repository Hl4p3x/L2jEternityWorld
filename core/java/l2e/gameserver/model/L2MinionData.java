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

import l2e.util.Rnd;

public class L2MinionData
{
	private int _minionId;
	private int _minionAmount;
	private int _minionAmountMin;
	private int _minionAmountMax;
	
	public void setMinionId(int id)
	{
		_minionId = id;
	}
	
	public int getMinionId()
	{
		return _minionId;
	}
	
	public void setAmountMin(int amountMin)
	{
		_minionAmountMin = amountMin;
	}
	
	public void setAmountMax(int amountMax)
	{
		_minionAmountMax = amountMax;
	}
	
	public void setAmount(int amount)
	{
		_minionAmount = amount;
	}
	
	public int getAmount()
	{
		if (_minionAmountMax > _minionAmountMin)
		{
			_minionAmount = Rnd.get(_minionAmountMin, _minionAmountMax);
			return _minionAmount;
		}
		return _minionAmountMin;
	}	
}