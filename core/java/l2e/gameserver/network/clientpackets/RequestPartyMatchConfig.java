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
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.ExPartyRoomMember;
import l2e.gameserver.network.serverpackets.ListPartyWating;
import l2e.gameserver.network.serverpackets.PartyMatchDetail;

public final class RequestPartyMatchConfig extends L2GameClientPacket
{
	private int _auto, _loc, _lvl;
	
	@Override
	protected void readImpl()
	{
		_auto = readD();
		_loc = readD();
		_lvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance _activeChar = getClient().getActiveChar();
		
		if (_activeChar == null)
		{
			return;
		}
		
		if (!_activeChar.isInPartyMatchRoom() && (_activeChar.getParty() != null) && (_activeChar.getParty().getLeader() != _activeChar))
		{
			_activeChar.sendPacket(SystemMessageId.CANT_VIEW_PARTY_ROOMS);
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (_activeChar.isInPartyMatchRoom())
		{
			PartyMatchRoomList _list = PartyMatchRoomList.getInstance();
			if (_list == null)
			{
				return;
			}
			
			PartyMatchRoom _room = _list.getPlayerRoom(_activeChar);
			if (_room == null)
			{
				return;
			}
			
			_activeChar.sendPacket(new PartyMatchDetail(_activeChar, _room));
			_activeChar.sendPacket(new ExPartyRoomMember(_activeChar, _room, 2));
			
			_activeChar.setPartyRoom(_room.getId());
			_activeChar.broadcastUserInfo();
		}
		else
		{
			PartyMatchWaitingList.getInstance().addPlayer(_activeChar);
			
			ListPartyWating matchList = new ListPartyWating(_activeChar, _auto, _loc, _lvl);
			
			_activeChar.sendPacket(matchList);
		}
	}
}