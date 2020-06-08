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
package l2e.gameserver.instancemanager.tasks;

import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Message;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class MessageDeletionTask implements Runnable
{
	private static final Logger _log = Logger.getLogger(MessageDeletionTask.class.getName());
	
	final int _msgId;
	
	public MessageDeletionTask(int msgId)
	{
		_msgId = msgId;
	}
	
	@Override
	public void run()
	{
		final Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
		{
			return;
		}
		
		if (msg.hasAttachments())
		{
			try
			{
				final L2PcInstance sender = L2World.getInstance().getPlayer(msg.getSenderId());
				if (sender != null)
				{
					msg.getAttachments().returnToWh(sender.getWarehouse());
					sender.sendPacket(SystemMessageId.MAIL_RETURNED);
				}
				else
				{
					msg.getAttachments().returnToWh(null);
				}
				
				msg.getAttachments().deleteMe();
				msg.removeAttachments();
				
				final L2PcInstance receiver = L2World.getInstance().getPlayer(msg.getReceiverId());
				if (receiver != null)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.MAIL_RETURNED);
					receiver.sendPacket(sm);
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": Error returning items:" + e.getMessage(), e);
			}
		}
		MailManager.getInstance().deleteMessageInDb(msg.getId());
	}
}