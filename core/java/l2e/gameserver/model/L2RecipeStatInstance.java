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

public class L2RecipeStatInstance
{
	private StatType _type;
	private int _value;

	public static enum StatType
	{
		HP,
		MP,
		XP,
		SP,
		GIM
	}
	
	public L2RecipeStatInstance(String type, int value)
	{
		_type = Enum.valueOf(StatType.class, type);
		_value = value;
	}
	
	public StatType getType()
	{
		return _type;
	}
	
	public int getValue()
	{
		return _value;
	}	
}