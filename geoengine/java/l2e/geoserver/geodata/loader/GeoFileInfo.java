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
package l2e.geoserver.geodata.loader;

/**
 * Created by LordWinter 10.09.2013 Based on L2J Eternity-World
 */
public class GeoFileInfo
{
	private int _x;
	
	private int _y;
	
	private byte[][] data;
	
	public GeoFileInfo()
	{
	}
	
	public GeoFileInfo(int x, int y)
	{
		_x = x;
		_y = y;
	}
	
	public int getX()
	{
		return _x;
	}
	
	public void setX(int x)
	{
		_x = x;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public void setY(int y)
	{
		_y = y;
	}
	
	public byte[][] getData()
	{
		return data;
	}
	
	public void setData(byte[][] data)
	{
		this.data = data;
	}
}