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

public class L2PhantomLoc
{
	protected int _x;
	protected int _y;
	protected int _z;
	protected int _grade;
	protected String _town;
	
	public void setLocX(int x)
	{
		_x = x;
	}
	
	public void setLocY(int y)
	{
		_y = y;
	}
	
	public void setLocZ(int z)
	{
		_z = z;
	}
	
	public void setGrade(int grade)
	{
		_grade = grade;
	}
	
	public void setTown(String town)
	{
		_town = town;
	}
	
	public int getLocX()
	{
		return _x;
	}
	
	public int getLocY()
	{
		return _y;
	}
	
	public int getLocZ()
	{
		return _z;
	}
	
	public int getGrade()
	{
		return _grade;
	}
	
	public String getTown()
	{
		return _town;
	}
}