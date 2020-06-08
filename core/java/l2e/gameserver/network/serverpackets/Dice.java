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
package l2e.gameserver.network.serverpackets;

public class Dice extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _itemId;
	private final int _number;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public Dice(int charObjId, int itemId, int number, int x , int y , int z)
	{
		_charObjId = charObjId;
		_itemId = itemId;
		_number = number;
		_x =x;
		_y =y;
		_z =z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xda);
		writeD(_charObjId);
		writeD(_itemId);
		writeD(_number);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}