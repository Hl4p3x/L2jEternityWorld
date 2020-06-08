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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.instancemanager.AntiFeedManager;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.L2GameClient.GameClientState;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CharSelectionInfo;
import l2e.gameserver.network.serverpackets.RestartResponse;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;
import l2e.protection.Protection;
import l2e.protection.network.ProtectionManager;

public final class RequestRestart extends L2GameClientPacket
{
	protected static final Logger _logAccounting = Logger.getLogger("accounting");
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if ((player.getActiveEnchantItemId() != L2PcInstance.ID_NONE) || (player.getActiveEnchantAttrItemId() != L2PcInstance.ID_NONE))
		{
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.isLocked())
		{
			_log.warning("Player " + player.getName() + " tried to restart during class change.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
		{
			player.sendMessage("Cannot restart while trading");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) && !(player.isGM() && Config.GM_RESTART_FIGHTING))
		{
			if (Config.DEBUG)
			{
				_log.fine("Player " + player.getName() + " tried to logout while fighting.");
			}
			
			player.sendPacket(SystemMessageId.CANT_RESTART_WHILE_FIGHTING);
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.isBlocked())
		{
			player.sendMessage("You are blocked!");
			player.sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendMessage("You cannot restart while you are a participant in a festival.");
				sendPacket(RestartResponse.valueOf(false));
				return;
			}
			
			final L2Party playerParty = player.getParty();
			
			if (playerParty != null)
			{
				player.getParty().broadcastString(player.getName() + " has been removed from the upcoming festival.");
			}
		}
		
		if (player.isBlockedFromExit())
		{
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		player.removeFromBossZone();
		
		final L2GameClient client = getClient();
		
		LogRecord record = new LogRecord(Level.INFO, "Logged out");
		record.setParameters(new Object[]
		{
			client
		});
		_logAccounting.log(record);
		
		player.setClient(null);
		
		player.deleteMe();
		
		client.setActiveChar(null);
		AntiFeedManager.getInstance().onDisconnect(client);
		
		client.setState(GameClientState.AUTHED);
		
		if (Protection.isProtectionOn())
		{
			ProtectionManager.scheduleSendPacketToClient(0, player);
			Protection.doDisconection(client);
		}
		
		sendPacket(RestartResponse.valueOf(true));
		
		final CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}