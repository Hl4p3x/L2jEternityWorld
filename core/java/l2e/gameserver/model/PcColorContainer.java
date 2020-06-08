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

public class PcColorContainer
{
	private final int _color;
	private final long _regTime;
	private final long _time;
	
	public PcColorContainer(int color, long regTime, long time)
	{
		_color = color;
		_regTime = regTime;
		_time = time;
	}
	
	public int getColor()
	{
		return _color;
	}
	
	public long getRegTime()
	{
		return _regTime;
	}
	
	public long getTime()
	{
		return _time;
	}
}