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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.GMViewPledgeInfo;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class AdminPledge implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_pledge"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			showMainPage(activeChar);
			return false;
		}
		String name = player.getName();
		if (command.startsWith("admin_pledge"))
		{
			String action = null;
			String parameter = null;
			StringTokenizer st = new StringTokenizer(command);
			try
			{
				st.nextToken();
				action = st.nextToken();
				parameter = st.nextToken();
			}
			catch (NoSuchElementException nse)
			{
				return false;
			}
			if (action.equals("create"))
			{
				long cet = player.getClanCreateExpiryTime();
				player.setClanCreateExpiryTime(0);
				L2Clan clan = ClanHolder.getInstance().createClan(player, parameter);
				if (clan != null)
				{
					activeChar.sendMessage("Clan " + parameter + " created. Leader: " + player.getName());
				}
				else
				{
					player.setClanCreateExpiryTime(cet);
					activeChar.sendMessage("There was a problem while creating the clan.");
				}
			}
			else if (!player.isClanLeader())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
				sm.addString(name);
				activeChar.sendPacket(sm);
				showMainPage(activeChar);
				return false;
			}
			else if (action.equals("dismiss"))
			{
				ClanHolder.getInstance().destroyClan(player.getClanId());
				L2Clan clan = player.getClan();
				if (clan == null)
				{
					activeChar.sendMessage("Clan disbanded.");
				}
				else
				{
					activeChar.sendMessage("There was a problem while destroying the clan.");
				}
			}
			else if (action.equals("info"))
			{
				activeChar.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
			}
			else if (parameter == null)
			{
				activeChar.sendMessage("Usage: //pledge <setlevel|rep> <number>");
			}
			else if (action.equals("setlevel"))
			{
				int level = Integer.parseInt(parameter);
				if ((level >= 0) && (level < 12))
				{
					player.getClan().changeLevel(level);
					activeChar.sendMessage("You set level " + level + " for clan " + player.getClan().getName());
				}
				else
				{
					activeChar.sendMessage("Level incorrect.");
				}
			}
			else if (action.startsWith("rep"))
			{
				try
				{
					int points = Integer.parseInt(parameter);
					L2Clan clan = player.getClan();
					if (clan.getLevel() < 5)
					{
						activeChar.sendMessage("Only clans of level 5 or above may receive reputation points.");
						showMainPage(activeChar);
						return false;
					}
					clan.addReputationScore(points, true);
					activeChar.sendMessage("You " + (points > 0 ? "add " : "remove ") + Math.abs(points) + " points " + (points > 0 ? "to " : "from ") + clan.getName() + "'s reputation. Their current score is " + clan.getReputationScore());
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Usage: //pledge <rep> <number>");
				}
			}
		}
		showMainPage(activeChar);
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showMainPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
		adminhtm.setFile(activeChar.getLang(), "data/html/admin/game_menu.htm");
		activeChar.sendPacket(adminhtm);
	}
}