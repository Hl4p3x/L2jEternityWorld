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
package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.PartyMatchRoom;
import l2e.gameserver.model.PartyMatchRoomList;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExClosePartyRoom;

public final class RequestWithdrawPartyRoom extends L2GameClientPacket
{
	private int _roomid;
	protected int _unk1;
	
	@Override
	protected void readImpl()
	{
		_roomid = readD();
		_unk1 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance _activeChar = getClient().getActiveChar();
		
		if (_activeChar == null)
		{
			return;
		}
		
		PartyMatchRoom _room = PartyMatchRoomList.getInstance().getRoom(_roomid);
		if (_room == null)
		{
			return;
		}
		
		if ((_activeChar.isInParty() && _room.getOwner().isInParty()) && (_activeChar.getParty().getLeaderObjectId() == _room.getOwner().getParty().getLeaderObjectId()))
		{
			_activeChar.broadcastUserInfo();
		}
		else
		{
			_room.deleteMember(_activeChar);
			
			_activeChar.setPartyRoom(0);
			
			_activeChar.sendPacket(new ExClosePartyRoom());
			_activeChar.sendPacket(SystemMessageId.PARTY_ROOM_EXITED);
		}
	}
}