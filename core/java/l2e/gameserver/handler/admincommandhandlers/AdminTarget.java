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
package l2e.gameserver.handler.admincommandhandlers;

import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class AdminTarget implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_target"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_target"))
			handleTarget(command, activeChar);
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleTarget(String command, L2PcInstance activeChar)
	{
		try
		{
			String targetName = command.substring(13);
			L2PcInstance player = L2World.getInstance().getPlayer(targetName);
			if (player != null)
			{
				player.onAction(activeChar);
			}
			else
			{
				activeChar.sendMessage("Player " + targetName + " not found");
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			activeChar.sendMessage("Please specify correct name.");
		}
	}
}
