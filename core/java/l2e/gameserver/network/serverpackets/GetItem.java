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

public final class GetItem extends L2GameServerPacket
{
	private final L2ItemInstance _item;
	private final int _playerId;
	
	public GetItem(L2ItemInstance item, int playerId)
	{
		_item = item;
		_playerId = playerId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x17);
		writeD(_playerId);
		writeD(_item.getObjectId());
		
		writeD(_item.getX());
		writeD(_item.getY());
		writeD(_item.getZ());
	}
}