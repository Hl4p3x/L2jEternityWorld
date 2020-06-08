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

import l2e.gameserver.model.items.instance.L2ItemInstance;

public class DropItem extends L2GameServerPacket
{
	private final L2ItemInstance _item;
	private final int _charObjId;
	
	public DropItem(L2ItemInstance item, int playerObjId)
	{
		_item = item;
		_charObjId = playerObjId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x16);
		writeD(_charObjId);
		writeD(_item.getObjectId());
		writeD(_item.getDisplayId());
		
		writeD(_item.getX());
		writeD(_item.getY());
		writeD(_item.getZ());

		writeD(_item.isStackable() ? 0x01 : 0x00);
		writeQ(_item.getCount());
		
		writeD(0x01);
	}
}