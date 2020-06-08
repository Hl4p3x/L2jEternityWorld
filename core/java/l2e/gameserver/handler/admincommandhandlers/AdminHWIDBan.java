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
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.protection.hwidmanager.HWIDBan;

public class AdminHWIDBan implements IAdminCommandHandler
{
	private static String[] _adminCommands =
	{
		"admin_hwid_ban"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance player)
	{
		if (command.startsWith("admin_hwid_ban"))
		{
			L2Object playerTarger = player.getTarget();
			if ((playerTarger == null) && !(playerTarger instanceof L2PcInstance))
			{
				player.sendMessage("Target is empty");
				return false;
			}
			L2PcInstance target = (L2PcInstance) playerTarger;
			if (target != null)
			{
				HWIDBan.addHWIDBan(target.getClient());
				player.sendMessage(target.getName() + " banned in HWID");
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}