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
package l2e.gameserver.model.entity.mods.aio.dymanicHtmls;

import java.util.logging.Logger;

import javolution.text.TextBuilder;
import l2e.Config;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.xml.AIOItemParser;
import l2e.gameserver.instancemanager.GrandBossManager;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.mods.AchievementsManager;
import l2e.gameserver.model.entity.mods.aio.main.PlayersTopData;
import l2e.gameserver.model.entity.mods.base.Achievement;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import l2e.gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import l2e.gameserver.network.serverpackets.WareHouseWithdrawalList;

public class GenerateHtmls
{
	private static final Logger _log = Logger.getLogger(GenerateHtmls.class.getName());
	private static final int[] BOSSES =
	{
		29001,
		29006,
		29014,
		29019,
		29020,
		29022,
		29028,
		29118
	};
	
	public static void sendPacket(L2PcInstance player, String html)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setFile(player.getLang(), "/data/html/AioItemNpcs/" + html);
		player.sendPacket(msg);
	}
	
	public static final void showRbInfo(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(5);
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Rb Info</title><body>");
		tb.append("<br><br>");
		tb.append("<font color=00FFFF>Grand Boss Info</font>");
		tb.append("<center>");
		tb.append("<img src=L2UI.SquareGray width=280 height=1>");
		tb.append("<br><br>");
		tb.append("<table width = 280>");
		for (int boss : BOSSES)
		{
			String name = NpcTable.getInstance().getTemplate(boss).getName();
			long delay = GrandBossManager.getInstance().getStatsSet(boss).getLong("respawn_time");
			if (delay <= System.currentTimeMillis())
			{
				tb.append("<tr>");
				tb.append("<td><font color=\"00C3FF\">" + name + "</color>:</td> " + "<td><font color=\"9CC300\">Is Alive</color></td>" + "<br1>");
				tb.append("</tr>");
			}
			else
			{
				int hours = (int) ((delay - System.currentTimeMillis()) / 1000 / 60 / 60);
				int mins = (int) (((delay - (hours * 60 * 60 * 1000)) - System.currentTimeMillis()) / 1000 / 60);
				int seconts = (int) (((delay - ((hours * 60 * 60 * 1000) + (mins * 60 * 1000))) - System.currentTimeMillis()) / 1000);
				tb.append("<tr>");
				tb.append("<td><font color=\"00C3FF\">" + name + "</color></td>" + "<td><font color=\"FFFFFF\">" + " " + "Respawn in :</color></td>" + " " + "<td><font color=\"32C332\">" + hours + " : " + mins + " : " + seconts + "</color></td><br1>");
				tb.append("</tr>");
			}
		}
		tb.append("</table>");
		tb.append("<br><br>");
		tb.append("<img src=L2UI.SquareWhite width=280 height=1>");
		tb.append("<td><button value=\"Back\" action=\"bypass -h Aioitem_Chat_service/services.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		tb.append("</center>");
		tb.append("</body></html>");
		html.setHtml(tb.toString());
		player.sendPacket(html);
	}
	
	public static void showAchievementMain(L2PcInstance player, int val)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Hello <font color=\"LEVEL\">" + player.getName() + "</font><br>Are you looking for challenge?");
		tb.append("<br><img src=\"L2UI.SquareWhite\" width=\"280\" height=\"1\"><br><br>");
		tb.append("<button value=\"My Achievements\" action=\"bypass -h Aioitem_showMyAchievements\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<button value=\"Statistics\" action=\"bypass -h Aioitem_showAchievementStats\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<button value=\"Help\" action=\"bypass -h Aioitem_showAchievementHelp\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<br><br><img src=\"L2UI.SquareWhite\" width=\"280\" height=\"1\">");
		tb.append("<td><button value=\"Back\" action=\"bypass -h Aioitem_Chat_service/services.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setHtml(tb.toString());
		
		player.sendPacket(msg);
	}
	
	public static void showMyAchievements(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<center><font color=\"LEVEL\">My achievements</font>:</center><br>");
		
		if (AchievementsManager.getInstance().getAchievementList().isEmpty())
		{
			tb.append("There are no Achievements created yet!");
		}
		else
		{
			int i = 0;
			
			tb.append("<table width=280 border=0 bgcolor=\"33FF33\">");
			tb.append("<tr><td width=115 align=\"left\">Name:</td><td width=50 align=\"center\">Info:</td><td width=115 align=\"center\">Status:</td></tr></table>");
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"280\" height=\"1\"><br>");
			
			for (Achievement a : AchievementsManager.getInstance().getAchievementList().values())
			{
				tb.append(getTableColor(i));
				tb.append("<tr><td width=115 align=\"left\">" + a.getName() + "</td><td width=50 align=\"center\"><a action=\"bypass -h Aioitem_showAchievementInfo " + a.getID() + "\">info</a></td><td width=115 align=\"center\">" + getStatusString(a.getID(), player) + "</td></tr></table>");
				i++;
			}
			
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"280\" height=\"1s\"><br>");
			tb.append("<center><button value=\"Back\" action=\"bypass -h Aioitem_showAchievementMain\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		}
		
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setHtml(tb.toString());
		
		player.sendPacket(msg);
	}
	
	public static void showAchievementInfo(int achievementID, L2PcInstance player)
	{
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<center><table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">" + a.getName() + "</td></tr></table><br>");
		tb.append("Status: " + getStatusString(achievementID, player));
		
		if (a.meetAchievementRequirements(player) && !player.getCompletedAchievements().contains(achievementID))
		{
			tb.append("<button value=\"Receive Reward!\" action=\"bypass -h Aioitem_achievementGetReward " + a.getID() + "\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		}
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">Description</td></tr></table><br>");
		tb.append(a.getDescription());
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=280 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=120 align=\"left\">Condition To Meet:</td><td width=55 align=\"center\">Value:</td><td width=95 align=\"center\">Status:</td></tr></table>");
		tb.append(getConditionsStatus(achievementID, player));
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h Aioitem_showMyAchievements\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setHtml(tb.toString());
		
		player.sendPacket(msg);
	}
	
	public static void showAchievementStats(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Check your <font color=\"LEVEL\">Achievements </font>statistics:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		player.getAchievemntData();
		int completedCount = player.getCompletedAchievements().size();
		
		tb.append("You have completed: " + completedCount + "/<font color=\"LEVEL\">" + AchievementsManager.getInstance().getAchievementList().size() + "</font>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h Aioitem_showAchievementMain\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setHtml(tb.toString());
		
		player.sendPacket(msg);
	}
	
	public static void showAchievementHelp(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Achievements  <font color=\"LEVEL\">Help </font>page:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		tb.append("You can check status of your achievements, receive reward if every condition of achievement is meet, if not you can check which condition is still not meet, by using info button");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<font color=\"FF0000\">Not Completed</font> - you did not meet the achivement requirements.<br>");
		tb.append("<font color=\"LEVEL\">Get Reward</font> - you may receive reward, click info.<br>");
		tb.append("<font color=\"5EA82E\">Completed</font> - achievement completed, reward received.<br>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h Aioitem_showAchievementMain\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setHtml(tb.toString());
		
		player.sendPacket(msg);
	}
	
	public static final void showCWithdrawWindow(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
			return;
		}
		
		player.setActiveWarehouse(player.getClan().getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		
		if (itemtype != null)
		{
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN, itemtype, sortorder));
		}
		else
		{
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
		}
		
		if (Config.DEBUG)
		{
			_log.info("Source: L2WarehouseInstance.java; Player: " + player.getName() + "; Command: showRetrieveWindowClan; Message: Showing stored items.");
		}
	}
	
	public static final void showPWithdrawWindow(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		
		if (itemtype != null)
		{
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE, itemtype, sortorder));
		}
		else
		{
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
		}
		
		if (Config.DEBUG)
		{
			_log.info("Source: L2WarehouseInstance.java; Player: " + player.getName() + "; Command: showRetrieveWindow; Message: Showing stored items.");
		}
	}
	
	public static void showTopPvp(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(5);
		TextBuilder sb = new TextBuilder();
		sb.append("<html><title>Top PvP</title><body><center><br>");
		sb.append("<table border=1 width = 280>");
		sb.append("<tr>");
		sb.append("<td><font color=FFD700>No</font></td><td><font color=FFD700>Character Name:</font></td><td><font color=FFD700>Clan Name:</font></td><td><font color=FFD700>PvP Kills:</font></td>");
		sb.append("</tr>");
		int count = 1;
		for (PlayersTopData playerData : AIOItemParser.getInstance().getTopPvp())
		{
			String name = playerData.getCharName();
			String cName = playerData.getClanName();
			int pvp = playerData.getPvp();
			
			sb.append("<tr>");
			sb.append("<td align=center>" + count + "</td><td>" + name + "</td><td align=center>" + cName + "</td><td align=center>" + pvp + "</td>");
			sb.append("</tr>");
			sb.append("<br>");
			count = count + 1;
		}
		sb.append("</table>");
		sb.append("<br><center>");
		sb.append("<br><img src=L2UI.SquareWhite width=280 height=1>");
		sb.append("<td><button value=\"Back\" action=\"bypass -h Aioitem_Chat_service/toplists.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</center>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	public static void showTopPk(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(5);
		TextBuilder sb = new TextBuilder();
		sb.append("<html><title>Top Pk</title><body><center><br>");
		sb.append("<table border=1 width = 280>");
		sb.append("<tr>");
		sb.append("<td><font color=FFD700>No</font></td><td><font color=FFD700>Character Name:</font></td><td><font color=FFD700>Clan Name:</font></td><td><font color=FFD700>Pk Kills:</font></td>");
		sb.append("</tr>");
		int count = 1;
		for (PlayersTopData playerData : AIOItemParser.getInstance().getTopPk())
		{
			String name = playerData.getCharName();
			String cName = playerData.getClanName();
			int pk = playerData.getPk();
			
			sb.append("<tr>");
			sb.append("<td align=center>" + count + "</td><td>" + name + "</td><td align=center>" + cName + "</td><td align=center>" + pk + "</td>");
			sb.append("</tr>");
			sb.append("<br>");
			count = count + 1;
		}
		sb.append("</table>");
		sb.append("<br><center>");
		sb.append("<br><img src=L2UI.SquareWhite width=280 height=1>");
		sb.append("<td><button value=\"Back\" action=\"bypass -h Aioitem_Chat_service/toplists.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</center>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	public static void showTopClan(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(5);
		TextBuilder sb = new TextBuilder();
		sb.append("<html><title>Top Clan</title><body><center><br>");
		sb.append("<table border=1 width = 280>");
		sb.append("<tr>");
		sb.append("<td><font color=FFD700>No</font></td><td><font color=FFD700>Leader Name:</font></td><td><font color=FFD700>Clan Name:</font></td><td><font color=FFD700>Clan Level:</font></td>");
		sb.append("</tr>");
		int count = 1;
		for (PlayersTopData playerData : AIOItemParser.getInstance().getTopClan())
		{
			String name = playerData.getCharName();
			String cName = playerData.getClanName();
			int cLevel = playerData.getClanLevel();
			
			sb.append("<tr>");
			sb.append("<td align=center>" + count + "</td><td>" + name + "</td><td align=center>" + cName + "</td><td align=center>" + cLevel + "</td>");
			sb.append("</tr>");
			sb.append("<br>");
			count = count + 1;
		}
		sb.append("</table>");
		sb.append("<br><center>");
		sb.append("<br><img src=L2UI.SquareWhite width=280 height=1>");
		sb.append("<td><button value=\"Back\" action=\"bypass -h Aioitem_Chat_service/toplists.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</center>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	private static String getStatusString(int achievementID, L2PcInstance player)
	{
		if (player.getCompletedAchievements().contains(achievementID))
		{
			return "<font color=\"5EA82E\">Completed</font>";
		}
		if (AchievementsManager.getInstance().getAchievementList().get(achievementID).meetAchievementRequirements(player))
		{
			return "<font color=\"LEVEL\">Get Reward</font>";
		}
		return "<font color=\"FF0000\">Not Completed</font>";
	}
	
	private static String getTableColor(int i)
	{
		if ((i % 2) == 0)
		{
			return "<table width=280 border=0 bgcolor=\"444444\">";
		}
		return "<table width=280 border=0>";
	}
	
	private static String getConditionsStatus(int achievementID, L2PcInstance player)
	{
		if (player.getCompletedAchievements().contains(Integer.valueOf(achievementID)))
		{
			return "<font color=\"5EA82E\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.COMPLETED") + "</font>";
		}
		
		if (AchievementsManager.getInstance().getAchievementList().get(Integer.valueOf(achievementID)).meetAchievementRequirements(player))
		{
			return "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.GET_REWARD") + "</font>";
		}
		
		return "<font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.NOT_COMPLETE") + "</font>";
	}
}