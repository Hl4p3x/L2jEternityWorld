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
package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.tasks.MessageDeletionTask;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Message;
import l2e.gameserver.network.serverpackets.ExNoticePostArrived;
import l2e.util.L2FastMap;

public final class MailManager
{
	protected static final Logger _log = Logger.getLogger(MailManager.class.getName());
	
	private final Map<Integer, Message> _messages = new L2FastMap<>(true);
	
	protected MailManager()
	{
		load();
	}
	
	private void load()
	{
		int count = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM messages ORDER BY expiration");
			
			ResultSet rset1 = statement.executeQuery();
			while (rset1.next())
			{
				
				final Message msg = new Message(rset1);
				
				int msgId = msg.getId();
				_messages.put(msgId, msg);
				
				count++;
				
				long expiration = msg.getExpiration();
				
				if (expiration < System.currentTimeMillis())
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msgId), 10000);
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msgId), expiration - System.currentTimeMillis());
				}
			}
			rset1.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error loading from database:" + e.getMessage(), e);
		}
		_log.info(getClass().getSimpleName() + ": Successfully loaded " + count + " messages.");
	}
	
	public final Message getMessage(int msgId)
	{
		return _messages.get(msgId);
	}
	
	public final Collection<Message> getMessages()
	{
		return _messages.values();
	}
	
	public final boolean hasUnreadPost(L2PcInstance player)
	{
		final int objectId = player.getObjectId();
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getReceiverId() == objectId) && msg.isUnread())
			{
				return true;
			}
		}
		return false;
	}
	
	public final int getInboxSize(int objectId)
	{
		int size = 0;
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getReceiverId() == objectId) && !msg.isDeletedByReceiver())
			{
				size++;
			}
		}
		return size;
	}
	
	public final int getOutboxSize(int objectId)
	{
		int size = 0;
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getSenderId() == objectId) && !msg.isDeletedBySender())
			{
				size++;
			}
		}
		return size;
	}
	
	public final List<Message> getInbox(int objectId)
	{
		final List<Message> inbox = new FastList<>();
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getReceiverId() == objectId) && !msg.isDeletedByReceiver())
			{
				inbox.add(msg);
			}
		}
		return inbox;
	}
	
	public final List<Message> getOutbox(int objectId)
	{
		final List<Message> outbox = new FastList<>();
		for (Message msg : getMessages())
		{
			if ((msg != null) && (msg.getSenderId() == objectId) && !msg.isDeletedBySender())
			{
				outbox.add(msg);
			}
		}
		return outbox;
	}
	
	public void sendMessage(Message msg)
	{
		_messages.put(msg.getId(), msg);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement stmt = Message.getStatement(msg, con);
			stmt.execute();
			stmt.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error saving message:" + e.getMessage(), e);
		}
		
		final L2PcInstance receiver = L2World.getInstance().getPlayer(msg.getReceiverId());
		if (receiver != null)
		{
			receiver.sendPacket(ExNoticePostArrived.valueOf(true));
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msg.getId()), msg.getExpiration() - System.currentTimeMillis());
	}
	
	public final void markAsReadInDb(int msgId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement stmt = con.prepareStatement("UPDATE messages SET isUnread = 'false' WHERE messageId = ?");
			stmt.setInt(1, msgId);
			stmt.execute();
			stmt.close();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error marking as read message:" + e.getMessage(), e);
		}
	}
	
	public final void markAsDeletedBySenderInDb(int msgId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE messages SET isDeletedBySender = 'true' WHERE messageId = ?"))
		{
			stmt.setInt(1, msgId);
			stmt.execute();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error marking as deleted by sender message:" + e.getMessage(), e);
		}
	}
	
	public final void markAsDeletedByReceiverInDb(int msgId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE messages SET isDeletedByReceiver = 'true' WHERE messageId = ?"))
		{
			stmt.setInt(1, msgId);
			stmt.execute();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error marking as deleted by receiver message:" + e.getMessage(), e);
		}
	}
	
	public final void removeAttachmentsInDb(int msgId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE messages SET hasAttachments = 'false' WHERE messageId = ?"))
		{
			stmt.setInt(1, msgId);
			stmt.execute();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error removing attachments in message:" + e.getMessage(), e);
		}
	}
	
	public final void deleteMessageInDb(int msgId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("DELETE FROM messages WHERE messageId = ?"))
		{
			stmt.setInt(1, msgId);
			stmt.execute();
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Error deleting message:" + e.getMessage(), e);
		}
		_messages.remove(msgId);
		IdFactory.getInstance().releaseId(msgId);
	}
	
	public static MailManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MailManager _instance = new MailManager();
	}
}