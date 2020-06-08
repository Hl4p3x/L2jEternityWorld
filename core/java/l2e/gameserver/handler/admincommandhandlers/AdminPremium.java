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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Logger;

import l2e.L2DatabaseFactory;
import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminPremium implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_premium_menu",
		"admin_premium_add1",
		"admin_premium_add3",
		"admin_premium_add7",
		"admin_premium_add14",
		"admin_premium_add30"
	};
	
	private static final String UPDATE_PREMIUMSERVICE = "UPDATE character_premium SET premium_service=?,enddate=? WHERE account_name=?";
	private static final Logger _log = Logger.getLogger(AdminPremium.class.getName());
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_premium_menu"))
		{
			NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
			adminhtm.setFile(activeChar.getLang(), "data/html/admin/premium_menu.htm");
			activeChar.sendPacket(adminhtm);
		}
		else if (command.startsWith("admin_premium_add1"))
		{
			try
			{
				String val = command.substring(19);
				addPremiumServices(1, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Err");
			}
		}
		else if (command.startsWith("admin_premium_add3"))
		{
			try
			{
				String val = command.substring(19);
				addPremiumServices(3, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Err");
			}
		}
		else if (command.startsWith("admin_premium_add7"))
		{
			try
			{
				String val = command.substring(19);
				addPremiumServices(7, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Err");
			}
		}
		else if (command.startsWith("admin_premium_add14"))
		{
			try
			{
				String val = command.substring(19);
				addPremiumServices(14, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Err");
			}
		}
		else if (command.startsWith("admin_premium_add30"))
		{
			try
			{
				String val = command.substring(19);
				addPremiumServices(30, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Err");
			}
		}
		return true;
	}
	
	private void addPremiumServices(int Days, String AccName)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(System.currentTimeMillis());
			finishtime.set(Calendar.SECOND, 0);
			finishtime.add(Calendar.DAY_OF_YEAR, Days);
			
			PreparedStatement statement = con.prepareStatement(UPDATE_PREMIUMSERVICE);
			statement.setInt(1, 1);
			statement.setLong(2, finishtime.getTimeInMillis());
			statement.setString(3, AccName);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.info("PremiumService:  Could not increase data");
		}	
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}