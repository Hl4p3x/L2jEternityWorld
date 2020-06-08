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

import java.util.List;
import java.util.StringTokenizer;

import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.util.StringUtil;

public class AdminFortSiege implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_fortsiege",
		"admin_add_fortattacker",
		"admin_list_fortsiege_clans",
		"admin_clear_fortsiege_list",
		"admin_spawn_fortdoors",
		"admin_endfortsiege",
		"admin_startfortsiege",
		"admin_setfort",
		"admin_removefort"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken();
		
		Fort fort = null;
		int fortId = 0;
		if (st.hasMoreTokens())
		{
			fortId = Integer.parseInt(st.nextToken());
			fort = FortManager.getInstance().getFortById(fortId);
		}
		
		if (((fort == null) || (fortId == 0)))
		{
			showFortSelectPage(activeChar);
		}
		else
		{
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if (target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			
			if (command.equalsIgnoreCase("admin_add_fortattacker"))
			{
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				}
				else
				{
					if (fort.getSiege().checkIfCanRegister(player))
					{
						fort.getSiege().registerAttacker(player, true);
					}
				}
			}
			else if (command.equalsIgnoreCase("admin_clear_fortsiege_list"))
			{
				fort.getSiege().clearSiegeClan();
			}
			else if (command.equalsIgnoreCase("admin_endfortsiege"))
			{
				fort.getSiege().endSiege();
			}
			else if (command.equalsIgnoreCase("admin_list_fortsiege_clans"))
			{
				activeChar.sendMessage("Not implemented yet.");
			}
			else if (command.equalsIgnoreCase("admin_setfort"))
			{
				if ((player == null) || (player.getClan() == null))
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				}
				else
				{
					fort.setOwner(player.getClan(), false);
				}
			}
			else if (command.equalsIgnoreCase("admin_removefort"))
			{
				L2Clan clan = fort.getOwnerClan();
				if (clan != null)
				{
					fort.removeOwner(true);
				}
				else
				{
					activeChar.sendMessage("Unable to remove fort");
				}
			}
			else if (command.equalsIgnoreCase("admin_spawn_fortdoors"))
			{
				fort.resetDoors();
			}
			else if (command.equalsIgnoreCase("admin_startfortsiege"))
			{
				fort.getSiege().startSiege();
			}
			
			showFortSiegePage(activeChar, fort);
		}
		return true;
	}
	
	private void showFortSelectPage(L2PcInstance activeChar)
	{
		int i = 0;
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "data/html/admin/forts.htm");
		
		final List<Fort> forts = FortManager.getInstance().getForts();
		final StringBuilder cList = new StringBuilder(forts.size() * 100);
		
		for (Fort fort : forts)
		{
			if (fort != null)
			{
				StringUtil.append(cList, "<td fixwidth=90><a action=\"bypass -h admin_fortsiege ", String.valueOf(fort.getId()), "\">", fort.getName(), " id: ", String.valueOf(fort.getId()), "</a></td>");
				i++;
			}
			
			if (i > 2)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		adminReply.replace("%forts%", cList.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showFortSiegePage(L2PcInstance activeChar, Fort fort)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "data/html/admin/fort.htm");
		adminReply.replace("%fortName%", fort.getName());
		adminReply.replace("%fortId%", String.valueOf(fort.getId()));
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}