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

import java.util.List;

import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.entity.Message;

public class ExShowSentPostList extends L2GameServerPacket
{
	private final List<Message> _outbox;
	
	public ExShowSentPostList(int objectId)
	{
		_outbox = MailManager.getInstance().getOutbox(objectId);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xAC);
		writeD((int) (System.currentTimeMillis() / 1000));
		if (_outbox != null && _outbox.size() > 0)
		{
			writeD(_outbox.size());
			for (Message msg : _outbox)
			{
				writeD(msg.getId());
				writeS(msg.getSubject());
				writeS(msg.getReceiverName());
				writeD(msg.isLocked() ? 0x01 : 0x00);
				writeD(msg.getExpirationSeconds());
				writeD(msg.isUnread() ? 0x01 : 0x00);
				writeD(0x01);
				writeD(msg.hasAttachments() ? 0x01 : 0x00);
			}
		}
		else
		{
			writeD(0x00);
		}
	}
}