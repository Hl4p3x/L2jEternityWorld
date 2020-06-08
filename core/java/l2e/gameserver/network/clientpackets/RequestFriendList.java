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

import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		SystemMessage sm;
		
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_HEADER);
		
		L2PcInstance friend = null;
		for (int id : activeChar.getFriendList())
		{
			String friendName = CharNameHolder.getInstance().getNameById(id);
			
			if (friendName == null)
			{
				continue;
			}
			
			friend = L2World.getInstance().getPlayer(friendName);
			
			if ((friend == null) || !friend.isOnline())
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_OFFLINE);
				sm.addString(friendName);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ONLINE);
				sm.addString(friendName);
			}
			
			activeChar.sendPacket(sm);
		}
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
	}
}