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

public class L2TeleportLocation
{
	private int _teleId;
	private int _locX;
	private int _locY;
	private int _locZ;
	private int _price;
	private boolean _forNoble;
	private int _itemId;
	
	public void setTeleId(int id)
	{
		_teleId = id;
	}
	
	public void setLocX(int locX)
	{
		_locX = locX;
	}
	
	public void setLocY(int locY)
	{
		_locY = locY;
	}
	
	public void setLocZ(int locZ)
	{
		_locZ = locZ;
	}
	
	public void setPrice(int price)
	{
		_price = price;
	}
	
	public void setIsForNoble(boolean val)
	{
		_forNoble = val;
	}
	
	public void setItemId(int val)
	{
		_itemId = val;
	}
	
	public int getTeleId()
	{
		return _teleId;
	}
	
	public int getLocX()
	{
		return _locX;
	}
	
	public int getLocY()
	{
		return _locY;
	}
	
	public int getLocZ()
	{
		return _locZ;
	}
	
	public int getPrice()
	{
		return _price;
	}
	
	public boolean getIsForNoble()
	{
		return _forNoble;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
}