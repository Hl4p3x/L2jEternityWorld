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

import l2e.Config;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Message;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExChangePostState;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

public final class RequestRejectPostAttachment extends L2GameClientPacket
{
	private int _msgId;
	
	@Override
	protected void readImpl()
	{
		_msgId = readD();
	}
	
	@Override
	public void runImpl()
	{
		if (!Config.ALLOW_MAIL || !Config.ALLOW_ATTACHMENTS)
		{
			return;
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("rejectattach"))
		{
			return;
		}
		
		if (!activeChar.isInsideZone(ZoneId.PEACE))
		{
			activeChar.sendPacket(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE);
			return;
		}
		
		Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
		{
			return;
		}
		
		if (msg.getReceiverId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to reject not own attachment!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!msg.hasAttachments() || (msg.getSendBySystem() != 0))
		{
			return;
		}
		
		MailManager.getInstance().sendMessage(new Message(msg));
		
		activeChar.sendPacket(SystemMessageId.MAIL_SUCCESSFULLY_RETURNED);
		activeChar.sendPacket(new ExChangePostState(true, _msgId, Message.REJECTED));
		
		final L2PcInstance sender = L2World.getInstance().getPlayer(msg.getSenderId());
		if (sender != null)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RETURNED_MAIL);
			sm.addCharName(activeChar);
			sender.sendPacket(sm);
		}
	}
}