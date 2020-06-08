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
import l2e.gameserver.model.PartyMatchWaitingList;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExManagePartyRoomMember;
import l2e.gameserver.network.serverpackets.ExPartyRoomMember;
import l2e.gameserver.network.serverpackets.PartyMatchDetail;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestPartyMatchDetail extends L2GameClientPacket
{
	private int _roomid;
	protected int _unk1;
	protected int _unk2;
	protected int _unk3;
	
	@Override
	protected void readImpl()
	{
		_roomid = readD();
		_unk1 = readD();
		_unk2 = readD();
		_unk3 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance _activeChar = getClient().getActiveChar();
		if (_activeChar == null)
		{
			return;
		}
		
		PartyMatchRoom _room = PartyMatchRoomList.getInstance().getRoom(_roomid);
		if (_room == null)
		{
			return;
		}
		
		if ((_activeChar.getLevel() >= _room.getMinLvl()) && (_activeChar.getLevel() <= _room.getMaxLvl()))
		{
			PartyMatchWaitingList.getInstance().removePlayer(_activeChar);
			
			_activeChar.setPartyRoom(_roomid);
			
			_activeChar.sendPacket(new PartyMatchDetail(_activeChar, _room));
			_activeChar.sendPacket(new ExPartyRoomMember(_activeChar, _room, 0));
			
			for (L2PcInstance _member : _room.getPartyMembers())
			{
				if (_member == null)
				{
					continue;
				}
				
				_member.sendPacket(new ExManagePartyRoomMember(_activeChar, _room, 0));
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ENTERED_PARTY_ROOM);
				sm.addCharName(_activeChar);
				_member.sendPacket(sm);
			}
			_room.addMember(_activeChar);
			
			_activeChar.broadcastUserInfo();
		}
		else
		{
			_activeChar.sendPacket(SystemMessageId.CANT_ENTER_PARTY_ROOM);
		}
	}
}