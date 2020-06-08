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
package l2e.gameserver.handler.voicedcommandhandlers;

import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.handler.IVoicedCommandHandler;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.protection.ConfigProtection;
import l2e.protection.hwidmanager.HWIDInfoList;
import l2e.protection.hwidmanager.HWIDManager;

public class Security implements IVoicedCommandHandler
{
	private final String[] _commandList =
	{
		"lock",
		"unlock",
		"lockplayer",
		"lockaccount"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());

		if (command.equalsIgnoreCase("lock"))
		{
			html.setFile(activeChar.getLang(), "data/html/security.htm");
			html.replace("%hwid_val%", HwidBlockBy());
			html.replace("%curIP%", activeChar.getClient().getConnection().getInetAddress().getHostAddress());
			activeChar.sendPacket(html);
		}
		else if (command.equalsIgnoreCase("unlock"))
		{
			updateLockPlayer(activeChar.getClient(), 3);
			activeChar.sendMessage((new CustomMessage("Security.UNLOCK", activeChar.getLang())).toString());
		}
		else if (command.startsWith("unlock"))
		{
			updateLockPlayer(activeChar.getClient(), 3);
			activeChar.sendMessage((new CustomMessage("Security.UNLOCK", activeChar.getLang())).toString());
			html.setFile(activeChar.getLang(), "data/html/security.htm");
			html.replace("%hwid_val%", HwidBlockBy());
			html.replace("%curIP%", activeChar.getClient().getConnection().getInetAddress().getHostAddress());
			activeChar.sendPacket(html);
		}
		else if (command.equalsIgnoreCase("lockplayer"))
		{
			updateLockPlayer(activeChar.getClient(), 1);
			activeChar.sendMessage((new CustomMessage("Security.LOCK_PLAYER", activeChar.getLang())).toString());
		}
		else if (command.startsWith("lockplayer"))
		{
			updateLockPlayer(activeChar.getClient(), 1);
			activeChar.sendMessage((new CustomMessage("Security.LOCK_PLAYER", activeChar.getLang())).toString());
			html.setFile(activeChar.getLang(), "data/html/security.htm");
			html.replace("%hwid_val%", HwidBlockBy());
			html.replace("%curIP%", activeChar.getClient().getConnection().getInetAddress().getHostAddress());
			activeChar.sendPacket(html);
		}
		else if (command.equalsIgnoreCase("lockaccount"))
		{
			updateLockPlayer(activeChar.getClient(), 2);
			activeChar.sendMessage((new CustomMessage("Security.LOCK_ACCOUNT", activeChar.getLang())).toString());
		}
		else if (command.startsWith("lockaccount"))
		{
			updateLockPlayer(activeChar.getClient(), 2);
			activeChar.sendMessage((new CustomMessage("Security.LOCK_ACCOUNT", activeChar.getLang())).toString());
			html.setFile(activeChar.getLang(), "data/html/security.htm");
			html.replace("%hwid_val%", HwidBlockBy());
			html.replace("%curIP%", activeChar.getClient().getConnection().getInetAddress().getHostAddress());
			activeChar.sendPacket(html);
		}
		return true;
	}

	private String HwidBlockBy()
	{
		String result = "(CPU/HDD)";

		switch(ConfigProtection.GET_CLIENT_HWID)
		{
			case 1:
				result = "(HDD)";
				break;
			case 2:
				result = "(MAC)";
				break;
			case 3:
				result = "(CPU)";
				break;
			default:
				result = "(unknown)";

		}
		return result;
	}
	
	public void updateLockPlayer(L2GameClient client, int Type)
	{
		if (Type == 1)
		{
			HWIDManager.updateHWIDInfo(client, HWIDInfoList.LockType.PLAYER_LOCK);
		}
		else if (Type == 2)
		{
			HWIDManager.updateHWIDInfo(client, HWIDInfoList.LockType.ACCOUNT_LOCK);
		}
		else if (Type == 3)
		{
			HWIDManager.updateHWIDInfo(client, HWIDInfoList.LockType.NONE);
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}