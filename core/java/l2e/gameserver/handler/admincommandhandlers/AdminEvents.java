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
import l2e.gameserver.instancemanager.FunEventsManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Event;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.util.StringUtil;

public class AdminEvents implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_ctf_start",
		"admin_ctf_abort",
		"admin_bw_start",
		"admin_bw_abort",
		"admin_dm_start",
		"admin_dm_abort",
		"admin_event_menu",
		"admin_event_start",
		"admin_event_stop",
		"admin_event_start_menu",
		"admin_event_stop_menu"
	};
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		String _event_name = "";
		StringTokenizer st = new StringTokenizer(command, " ");
		st.nextToken();
		if (st.hasMoreTokens())
		{
			_event_name = st.nextToken();
		}
		
		if (command.contains("_menu"))
		{
			showMenu(activeChar);
		}
		
		if (command.startsWith("admin_ctf_start"))
		{
			FunEventsManager.getInstance().startEvent("CTF");
		}
		else if (command.startsWith("admin_ctf_abort"))
		{
			FunEventsManager.getInstance().abortEvent("CTF");
		}
		else if (command.startsWith("admin_bw_start"))
		{
			FunEventsManager.getInstance().startEvent("BW");
		}
		else if (command.startsWith("admin_bw_abort"))
		{
			FunEventsManager.getInstance().abortEvent("BW");
		}
		else if (command.startsWith("admin_dm_start"))
		{
			FunEventsManager.getInstance().startEvent("DM");
		}
		else if (command.startsWith("admin_dm_abort"))
		{
			FunEventsManager.getInstance().abortEvent("DM");
		}
		else if (command.startsWith("admin_event_start"))
		{
			try
			{
				if (_event_name != null)
				{
					Event _event = (Event) QuestManager.getInstance().getQuest(_event_name);
					if (_event != null)
					{
						if (_event.eventStart())
						{
							activeChar.sendMessage("Event '" + _event_name + "' started.");
							return true;
						}
						
						activeChar.sendMessage("There is problem with starting '" + _event_name + "' event.");
						return true;
					}
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //event_start <eventname>");
				e.printStackTrace();
				return false;
			}
		}
		else if (command.startsWith("admin_event_stop"))
		{
			try
			{
				if (_event_name != null)
				{
					Event _event = (Event) QuestManager.getInstance().getQuest(_event_name);
					if (_event != null)
					{
						if (_event.eventStop())
						{
							activeChar.sendMessage("Event '" + _event_name + "' stopped.");
							return true;
						}
						
						activeChar.sendMessage("There is problem with stoping '" + _event_name + "' event.");
						return true;
					}
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //event_start <eventname>");
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	private void showMenu(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar.getLang(), "data/html/admin/gm_events.htm");
		final StringBuilder cList = new StringBuilder(500);
		for (Quest event : QuestManager.getInstance().getAllManagedScripts())
		{
			if (event instanceof Event)
			{
				StringUtil.append(cList, "<font color=\"LEVEL\">" + event.getName() + ":</font><br1>", "<table width=200><tr>", "<td><button value=\"Start\" action=\"bypass -h admin_event_start_menu " + event.getName() + "\" width=80 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>", "<td><button value=\"Stop\" action=\"bypass -h admin_event_stop_menu " + event.getName() + "\" width=80 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>", "</tr></table><br>");
			}
		}
		html.replace("%LIST%", cList.toString());
		activeChar.sendPacket(html);
	}
}