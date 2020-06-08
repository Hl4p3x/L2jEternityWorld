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

import java.nio.BufferUnderflowException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mmocore.network.ReceivablePacket;

import l2e.Config;
import l2e.EternityWorld;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;

public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
	protected static final Logger _log = Logger.getLogger(L2GameClientPacket.class.getName());
	
	@Override
	public boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Client: " + getClient().toString() + " - Failed reading: " + getType() + " - L2J Eternity-World Server Version: " + EternityWorld._revision + " ; " + e.getMessage(), e);
			
			if (e instanceof BufferUnderflowException)
			{
				getClient().onBufferUnderflow();
			}
		}
		return false;
	}
	
	protected abstract void readImpl();
	
	@Override
	public void run()
	{
		try
		{
			runImpl();
			
			if (triggersOnActionRequest())
			{
				final L2PcInstance actor = getClient().getActiveChar();
				if ((actor != null) && (actor.isSpawnProtected() || actor.isInvul()))
				{
					actor.onActionRequest();
					if (Config.DEBUG)
					{
						_log.info("Spawn protection for player " + actor.getName() + " removed by packet: " + getType());
					}
				}
			}
		}
		catch (Throwable t)
		{
			_log.log(Level.SEVERE, "Client: " + getClient().toString() + " - Failed running: " + getType() + " - L2J Eternity-World Server Version: " + EternityWorld._revision + " ; " + t.getMessage(), t);
			
			if (this instanceof EnterWorld)
			{
				getClient().closeNow();
			}
		}
	}
	
	protected abstract void runImpl();
	
	protected final void sendPacket(L2GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}
	
	public void sendPacket(SystemMessageId id)
	{
		sendPacket(SystemMessage.getSystemMessage(id));
	}
	
	protected boolean triggersOnActionRequest()
	{
		return true;
	}
	
	protected final L2PcInstance getActiveChar()
	{
		return getClient().getActiveChar();
	}
	
	protected final void sendActionFailed()
	{
		if (getClient() != null)
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public String getType()
	{
		return "[C] " + getClass().getSimpleName();
	}
}