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

import java.util.StringTokenizer;

import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.model.entity.events.MonsterRush;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class AdminMonsterRush implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = 
	{ 
		"admin_mrush_start", 
		"admin_mrush_teleport", 
		"admin_mrush_abort" 
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null)
			return false;
		
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		
		if (actualCommand.equalsIgnoreCase("admin_mrush_start"))
			MonsterRush.startRegister();
		else if (actualCommand.equalsIgnoreCase("admin_mrush_teleport"))
			MonsterRush.startEvent();
		else if (actualCommand.equalsIgnoreCase("admin_mrush_abort"))
			MonsterRush.abortEvent();
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}	
}