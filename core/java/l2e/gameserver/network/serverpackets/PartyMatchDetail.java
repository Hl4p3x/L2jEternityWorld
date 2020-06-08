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

import l2e.gameserver.model.PartyMatchRoom;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class PartyMatchDetail extends L2GameServerPacket
{
	private final PartyMatchRoom _room;
	
	public PartyMatchDetail(L2PcInstance player, PartyMatchRoom room)
	{
		_room = room;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9d);
		writeD(_room.getId());
		writeD(_room.getMaxMembers());
		writeD(_room.getMinLvl());
		writeD(_room.getMaxLvl());
		writeD(_room.getLootType());
		writeD(_room.getLocation());
		writeS(_room.getTitle());
		writeH(0x00);
	}
}