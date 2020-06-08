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

public class L2ExtractableProduct
{
	private final int _id;
	private final int _min;
	private final int _max;
	private final int _chance;
	
	public L2ExtractableProduct(int id, int min, int max, double chance)
	{
		_id = id;
		_min = min;
		_max = max;
		_chance = (int) (chance * 1000);
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getMin()
	{
		return _min;
	}
	
	public int getMax()
	{
		return _max;
	}
	
	public int getChance()
	{
		return _chance;
	}
}