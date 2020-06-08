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

import l2e.gameserver.model.L2World;
import l2e.gameserver.model.PartyMatchRoom;
import l2e.gameserver.model.PartyMatchRoomList;
import l2e.gameserver.model.PartyMatchWaitingList;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExClosePartyRoom;
import l2e.gameserver.network.serverpackets.ListPartyWating;

public final class RequestOustFromPartyRoom extends L2GameClientPacket
{
	private int _charid;
	
	@Override
	protected void readImpl()
	{
		_charid = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getActiveChar();
		if (player == null)
		{
			return;
		}
		
		L2PcInstance member = L2World.getInstance().getPlayer(_charid);
		if (member == null)
		{
			return;
		}
		
		PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(member);
		if ((room == null) || (room.getOwner() != player))
		{
			return;
		}
		
		if (player.isInParty() && member.isInParty() && (player.getParty().getLeaderObjectId() == member.getParty().getLeaderObjectId()))
		{
			player.sendPacket(SystemMessageId.CANNOT_DISMISS_PARTY_MEMBER);
		}
		else
		{
			room.deleteMember(member);
			member.setPartyRoom(0);
			
			member.sendPacket(new ExClosePartyRoom());
			
			PartyMatchWaitingList.getInstance().addPlayer(member);
			
			int loc = 0;
			member.sendPacket(new ListPartyWating(member, 0, loc, member.getLevel()));
			
			member.broadcastUserInfo();
			member.sendPacket(SystemMessageId.OUSTED_FROM_PARTY_ROOM);
		}
	}
}