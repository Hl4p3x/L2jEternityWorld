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
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.util.Util;

public class AdminPcCondOverride implements IAdminCommandHandler
{
	private static final String[] COMMANDS =
	{
		"admin_exceptions",
		"admin_set_exception",
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		if (st.hasMoreTokens())
		{
			switch (st.nextToken())
			{
				case "admin_exceptions":
				{
					NpcHtmlMessage msg = new NpcHtmlMessage(5, 1);
					msg.setFile(activeChar.getLang(), "data/html/admin/cond_override.htm");
					StringBuilder sb = new StringBuilder();
					for (PcCondOverride ex : PcCondOverride.values())
					{
						sb.append("<tr><td fixwidth=\"180\">" + ex.getDescription() + ":</td><td><a action=\"bypass -h admin_set_exception " + ex.ordinal() + "\">" + (activeChar.canOverrideCond(ex) ? "Disable" : "Enable") + "</a></td></tr>");	
					}
					msg.replace("%cond_table%", sb.toString());
					activeChar.sendPacket(msg);
					break;
				}
				case "admin_set_exception":
				{
					if (st.hasMoreTokens())
					{
						String token = st.nextToken();
						if (Util.isDigit(token))
						{
							PcCondOverride ex = PcCondOverride.getCondOverride(Integer.valueOf(token));
							if (ex != null)
							{
								if (activeChar.canOverrideCond(ex))
								{
									activeChar.removeOverridedCond(ex);
									activeChar.sendMessage("You've disabled " + ex.getDescription());
								}
								else
								{
									activeChar.addOverrideCond(ex);
									activeChar.sendMessage("You've enabled " + ex.getDescription());
								}
							}
						}
						else
						{
							switch (token)
							{
								case "enable_all":
								{
									for (PcCondOverride ex : PcCondOverride.values())
									{
										if (!activeChar.canOverrideCond(ex))
										{
											activeChar.addOverrideCond(ex);
										}
									}
									activeChar.sendMessage("All condition exceptions have been enabled.");
									break;
								}
								case "disable_all":
								{
									for (PcCondOverride ex : PcCondOverride.values())
									{
										if (activeChar.canOverrideCond(ex))
										{
											activeChar.removeOverridedCond(ex);
										}
									}
									activeChar.sendMessage("All condition exceptions have been disabled.");
									break;
								}
							}
						}
						useAdminCommand(COMMANDS[0], activeChar);
					}
					break;
				}
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return COMMANDS;
	}
}