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
package l2e.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastMap;
import l2e.L2DatabaseFactory;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Created by LordWinter 25.02.2011 Fixed by L2J Eternity-World
 */
public class StateBBSManager extends BaseBBSManager
{
	private final static Logger _log = Logger.getLogger(StateBBSManager.class.getName());
	
	protected StateBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	private int _posId;
	
	public class CBStatMan
	{
		public int PlayerId = 0;
		public String ChName = "";
		public int ChGameTime = 0;
		public int ChPk = 0;
		public int ChPvP = 0;
		public int ChPcBangPoint = 0;
		public String ChClanName = "";
		public int ChClanLevel = 0;
		public int ChClanRep = 0;
		public String ChClanAlly = "";
		public int ChOnOff = 0;
		public int ChSex = 0;
		public int Npcid = 0;
		public int Status = 0;
		public int ChrClass = 0;
		public int Count = 0;
		public int Played = 0;
		public String Owner = "";
		public int Tax = 0;
		public int HasCastle = 0;
		long SomeLong = 0;
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance player)
	{
		if (command.equals("_bbsstat;"))
		{
			showPvp(player);
			player.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FavoriteBBS.PVP") + "");
		}
		else if (command.startsWith("_bbsstat;pk"))
		{
			showPK(player);
			player.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FavoriteBBS.PK") + "");
		}
		else if (command.startsWith("_bbsstat;clan"))
		{
			showClan(player);
			player.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FavoriteBBS.CLAN") + "");
		}
		else if (command.startsWith("_bbsstat;pcbang"))
		{
			showPcBang(player);
			player.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FavoriteBBS.PCBANG") + "");
		}
		else if (command.startsWith("_bbsstat;grandlist"))
		{
			showGrandList(player);
			player.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FavoriteBBS.GRAND") + "");
		}
		else if (command.startsWith("_bbsstat;herolist"))
		{
			showHeroList(player);
			player.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FavoriteBBS.HERO") + "");
		}
		else if (command.startsWith("_bbsstat;castlelist"))
		{
			showCastleList(player);
			player.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(player.getLang(), "FavoriteBBS.CASTLE") + "");
		}
		else
		{
			separateAndSend("<html><body><br><br><center>In bbsstat function: " + command + " is not implemented yet.</center><br><br></body></html>", player);
		}
	}
	
	private void showPvp(L2PcInstance player)
	{
		String lang = player.getLang();
		CBStatMan tp;
		int pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE accesslevel = '0' ORDER BY pvpkills DESC LIMIT 20;"))
		{
			ResultSet rs = statement.executeQuery();
			
			TextBuilder html = new TextBuilder();
			html.append("<center>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.TOP_PVP") + "</center>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700 bgcolor=CCCCCC>");
			html.append("<tr>");
			html.append("<td width=50>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.NUMBER") + "</td>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.CHAR") + "</td>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.CLASS") + "</td>");
			html.append("<td width=50>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.SEX") + "</td>");
			html.append("<td width=100>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.GAME_TIME") + "</td>");
			html.append("<td width=50>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.PK") + "</td>");
			html.append("<td width=50><font color=00CC00>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.PVP") + "</font></td>");
			html.append("<td width=60>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.STATUS") + "</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700>");
			while (rs.next())
			{
				pos++;
				tp = new CBStatMan();
				tp.PlayerId = rs.getInt("charId");
				tp.ChName = rs.getString("char_name");
				tp.ChrClass = rs.getInt("base_class");
				tp.ChSex = rs.getInt("sex");
				tp.ChGameTime = rs.getInt("onlinetime");
				tp.ChPk = rs.getInt("pkkills");
				tp.ChPvP = rs.getInt("pvpkills");
				tp.ChOnOff = rs.getInt("online");
				String OnOff;
				String color;
				String sex;
				sex = tp.ChSex == 1 ? "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.FEMALE") + "" : "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.MEN") + "";
				if (tp.ChOnOff == 1)
				{
					OnOff = "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.ONLINE") + "";
					color = "00CC00";
				}
				else
				{
					OnOff = "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.OFFLINE") + "";
					color = "D70000";
				}
				html.append("<tr>");
				html.append("<td width=50>" + pos + "</td>");
				html.append("<td width=70>" + tp.ChName + "</td>");
				html.append("<td width=70>" + className(player, tp.ChrClass) + "</td>");
				html.append("<td width=50>" + sex + "</td>");
				html.append("<td width=100>" + OnlineTime(player, tp.ChGameTime) + "</td>");
				html.append("<td width=50>" + tp.ChPk + "</td>");
				html.append("<td width=50><font color=00CC00>" + tp.ChPvP + "</font></td>");
				html.append("<td width=60><font color=" + color + ">" + OnOff + "</font></td>");
				html.append("</tr>");
			}
			html.append("</table>");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(player.getLang(), "data/html/CommunityBoard/11.htm");
			adminReply.replace("%stat%", html.toString());
			separateAndSend(adminReply.getHtm(), player);
			
			rs.close();
			
			return;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void showPK(L2PcInstance player)
	{
		String lang = player.getLang();
		CBStatMan tp;
		int pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE accesslevel = '0' ORDER BY pkkills DESC LIMIT 20;"))
		{
			ResultSet rs = statement.executeQuery();
			
			TextBuilder html = new TextBuilder();
			html.append("<center>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.TOP_PK") + "</center>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700 bgcolor=CCCCCC>");
			html.append("<tr>");
			html.append("<td width=50>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.NUMBER") + "</td>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.CHAR") + "</td>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.CLASS") + "</td>");
			html.append("<td width=50>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.SEX") + "</td>");
			html.append("<td width=100>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.GAME_TIME") + "</td>");
			html.append("<td width=50><font color=00CC00>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.PK") + "</font></td>");
			html.append("<td width=50>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.PVP") + "</td>");
			html.append("<td width=60>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.STATUS") + "</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700>");
			while (rs.next())
			{
				pos++;
				tp = new CBStatMan();
				tp.PlayerId = rs.getInt("charId");
				tp.ChName = rs.getString("char_name");
				tp.ChrClass = rs.getInt("base_class");
				tp.ChSex = rs.getInt("sex");
				tp.ChGameTime = rs.getInt("onlinetime");
				tp.ChPk = rs.getInt("pkkills");
				tp.ChPvP = rs.getInt("pvpkills");
				tp.ChOnOff = rs.getInt("online");
				String OnOff;
				String color;
				String sex;
				sex = tp.ChSex == 1 ? "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.FEMALE") + "" : "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.MEN") + "";
				if (tp.ChOnOff == 1)
				{
					OnOff = "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.ONLINE") + "";
					color = "00CC00";
				}
				else
				{
					OnOff = "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.OFFLINE") + "";
					color = "D70000";
				}
				html.append("<tr>");
				html.append("<td width=50>" + pos + "</td>");
				html.append("<td width=70>" + tp.ChName + "</td>");
				html.append("<td width=70>" + className(player, tp.ChrClass) + "</td>");
				html.append("<td width=50>" + sex + "</td>");
				html.append("<td width=100>" + OnlineTime(player, tp.ChGameTime) + "</td>");
				html.append("<td width=50><font color=00CC00>" + tp.ChPk + "</font></td>");
				html.append("<td width=50>" + tp.ChPvP + "</td>");
				html.append("<td width=60><font color=" + color + ">" + OnOff + "</font></td>");
				html.append("</tr>");
			}
			html.append("</table>");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(player.getLang(), "data/html/CommunityBoard/11.htm");
			adminReply.replace("%stat%", html.toString());
			separateAndSend(adminReply.getHtm(), player);
			
			rs.close();
			
			return;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void showPcBang(L2PcInstance player)
	{
		String lang = player.getLang();
		CBStatMan tp;
		int pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE accesslevel = '0' ORDER BY pccafe_points DESC LIMIT 20;"))
		{
			ResultSet rs = statement.executeQuery();
			
			TextBuilder html = new TextBuilder();
			html.append("<center>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.TOP_PC") + "</center>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700 bgcolor=CCCCCC>");
			html.append("<tr>");
			html.append("<td width=50>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.NUMBER") + "</td>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.CHAR") + "</td>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.CLASS") + "</td>");
			html.append("<td width=50>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.SEX") + "</td>");
			html.append("<td width=100>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.GAME_TIME") + "</td>");
			html.append("<td width=70><font color=00CC00>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.PC") + "</font></td>");
			html.append("<td width=60>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.STATUS") + "</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700>");
			while (rs.next())
			{
				pos++;
				tp = new CBStatMan();
				tp.PlayerId = rs.getInt("charId");
				tp.ChName = rs.getString("char_name");
				tp.ChrClass = rs.getInt("base_class");
				tp.ChSex = rs.getInt("sex");
				tp.ChGameTime = rs.getInt("onlinetime");
				tp.ChPcBangPoint = rs.getInt("pccafe_points");
				tp.ChOnOff = rs.getInt("online");
				String OnOff;
				String color;
				String sex;
				sex = tp.ChSex == 1 ? "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.FEMALE") + "" : "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.MEN") + "";
				if (tp.ChOnOff == 1)
				{
					OnOff = "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.ONLINE") + "";
					color = "00CC00";
				}
				else
				{
					OnOff = "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.OFFLINE") + "";
					color = "D70000";
				}
				html.append("<tr>");
				html.append("<td width=50>" + pos + "</td>");
				html.append("<td width=70>" + tp.ChName + "</td>");
				html.append("<td width=70>" + className(player, tp.ChrClass) + "</td>");
				html.append("<td width=50>" + sex + "</td>");
				html.append("<td width=100>" + OnlineTime(player, tp.ChGameTime) + "</td>");
				html.append("<td width=70><font color=00CC00>" + tp.ChPcBangPoint + "</font></td>");
				html.append("<td width=60><font color=" + color + ">" + OnOff + "</font></td>");
				html.append("</tr>");
			}
			html.append("</table>");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(player.getLang(), "data/html/CommunityBoard/11.htm");
			adminReply.replace("%stat%", html.toString());
			separateAndSend(adminReply.getHtm(), player);
			
			rs.close();
			
			return;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void showClan(L2PcInstance player)
	{
		String lang = player.getLang();
		CBStatMan tp;
		int pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT clan_name,clan_level,reputation_score,ally_name FROM clan_data WHERE clan_level>0 order by clan_level desc limit 20;"))
		{
			ResultSet rs = statement.executeQuery();
			
			TextBuilder html = new TextBuilder();
			html.append("<center>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.TOP_CLAN") + "</center>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700 bgcolor=CCCCCC>");
			html.append("<tr>");
			html.append("<td width=50>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.NUMBER") + "</td>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.CLAN") + "</td>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.ALLY") + "</td>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.REPUTATION") + "</td>");
			html.append("<td width=50>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.LVL") + "</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700>");
			while (rs.next())
			{
				pos++;
				tp = new CBStatMan();
				tp.ChClanName = rs.getString("clan_name");
				tp.ChClanAlly = rs.getString("ally_name");
				tp.ChClanRep = rs.getInt("reputation_score");
				tp.ChClanLevel = rs.getInt("clan_level");
				String ClanAlly;
				if (tp.ChClanAlly == null)
				{
					ClanAlly = "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.NO_ALLY") + "";
				}
				else
				{
					ClanAlly = tp.ChClanAlly;
				}
				
				html.append("<tr>");
				html.append("<td width=50>" + pos + "</td>");
				html.append("<td width=70>" + tp.ChClanName + "</td>");
				html.append("<td width=70>" + ClanAlly + "</td>");
				html.append("<td width=70>" + tp.ChClanRep + "</td>");
				html.append("<td width=50>" + tp.ChClanLevel + "</td>");
				html.append("</tr>");
			}
			html.append("</table>");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(player.getLang(), "data/html/CommunityBoard/11.htm");
			adminReply.replace("%stat%", html.toString());
			separateAndSend(adminReply.getHtm(), player);
			
			rs.close();
			
			return;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void showGrandList(L2PcInstance player)
	{
		String lang = player.getLang();
		CBStatMan tp;
		int pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT boss_id, status FROM grandboss_data"))
		{
			ResultSet rs = statement.executeQuery();
			
			TextBuilder html = new TextBuilder();
			html.append("<center>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.GRANDBOSS_STATUS") + "</center>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700 bgcolor=CCCCCC>");
			html.append("<tr>");
			html.append("<td width=50>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.NUMBER") + "</td>");
			html.append("<td width=100>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.GRANDBOSS") + "</td>");
			html.append("<td width=100>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.STATUS") + "</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700>");
			while (rs.next())
			{
				tp = new CBStatMan();
				tp.Npcid = rs.getInt("boss_id");
				tp.Status = rs.getInt("status");
				
				PreparedStatement statement2 = con.prepareStatement("SELECT name FROM npc WHERE id=" + tp.Npcid);
				ResultSet rs2 = statement2.executeQuery();
				
				while (rs2.next())
				{
					pos++;
					boolean rstatus = false;
					if (tp.Status == 0)
					{
						rstatus = true;
					}
					String npcname = rs2.getString("name");
					
					html.append("<tr>");
					html.append("<td width=50>" + pos + "</td>");
					html.append("<td width=100>" + npcname + "</td>");
					html.append("<td width=100>" + ((rstatus) ? "<font color=99FF00>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.ALIVE") + "</font>" : "<font color=CC0000>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.DEAD") + "</font>") + "</td>");
					html.append("</tr>");
				}
				rs2.close();
				statement2.close();
			}
			html.append("</table>");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(player.getLang(), "data/html/CommunityBoard/11.htm");
			adminReply.replace("%stat%", html.toString());
			separateAndSend(adminReply.getHtm(), player);
			
			rs.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void showHeroList(L2PcInstance player)
	{
		String lang = player.getLang();
		CBStatMan tp;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT h.count, h.played, ch.char_name, ch.base_class, ch.online, cl.clan_name, cl.ally_name FROM heroes h LEFT JOIN characters ch ON ch.charId=h.charId LEFT OUTER JOIN clan_data cl ON cl.clan_id=ch.clanid ORDER BY h.count DESC, ch.char_name ASC LIMIT 20"))
		{
			_posId = 0;
			ResultSet rs = statement.executeQuery();
			
			TextBuilder html = new TextBuilder();
			html.append("<center>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.TOP_HEROES") + "</center>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700 bgcolor=CCCCCC>");
			html.append("<tr>");
			html.append("<td width=50>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.NUMBER") + "</td>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.CHAR") + "</td>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.CLASS") + "</td>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.COUNT") + "</td>");
			html.append("<td width=100>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.CLAN") + "</td>");
			html.append("<td width=100>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.ALLY") + "</td>");
			html.append("<td width=60>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.STATUS") + "</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700>");
			while (rs.next())
			{
				_posId = _posId + 1;
				
				tp = new CBStatMan();
				tp.ChName = rs.getString("char_name");
				tp.ChrClass = rs.getInt("base_class");
				tp.Count = rs.getInt("count");
				tp.ChClanName = rs.getString("clan_name");
				tp.ChClanAlly = rs.getString("ally_name");
				tp.ChOnOff = rs.getInt("online");
				String OnOff;
				String color;
				if (tp.ChOnOff == 1)
				{
					OnOff = "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.ONLINE") + "";
					color = "00CC00";
				}
				else
				{
					OnOff = "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.OFFLINE") + "";
					color = "D70000";
				}
				
				String ClanAlly;
				if (tp.ChClanAlly == null)
				{
					ClanAlly = "" + LocalizationStorage.getInstance().getString(lang, "StateBBS.NO_ALLY") + "";
				}
				else
				{
					ClanAlly = tp.ChClanAlly;
				}
				
				html.append("<tr>");
				html.append("<td width=50>" + _posId + "</td>");
				html.append("<td width=70>" + tp.ChName + "</td>");
				html.append("<td width=70>" + className(player, tp.ChrClass) + "</td>");
				html.append("<td width=70>" + tp.Count + "</td>");
				html.append("<td width=100>" + tp.ChClanName + "</td>");
				html.append("<td width=100>" + ClanAlly + "</td>");
				html.append("<td width=60><font color=" + color + ">" + OnOff + "</font></td>");
				html.append("</tr>");
			}
			html.append("</table>");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(player.getLang(), "data/html/CommunityBoard/11.htm");
			adminReply.replace("%stat%", html.toString());
			separateAndSend(adminReply.getHtm(), player);
			
			rs.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void showCastleList(L2PcInstance player)
	{
		String lang = player.getLang();
		CBStatMan tp;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT clan_name, clan_level, hasCastle FROM clan_data WHERE clan_level > '0';"))
		{
			ResultSet rs = statement.executeQuery();
			
			TextBuilder html = new TextBuilder();
			html.append("<center>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.TOP_CASTLES") + "</center>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700 bgcolor=CCCCCC>");
			html.append("<tr>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.CASTLE") + "</td>");
			html.append("<td width=70>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.OWNER") + "</td>");
			html.append("<td width=60>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.LVL") + "</td>");
			html.append("<td width=60>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.TAXE") + "</td>");
			html.append("<td width=250>" + LocalizationStorage.getInstance().getString(lang, "StateBBS.SIEGE_DATE") + "</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<img src=L2UI.SquareWhite width=700 height=1>");
			html.append("<table width=700>");
			while (rs.next())
			{
				tp = new CBStatMan();
				tp.Owner = rs.getString("clan_name");
				tp.ChClanLevel = rs.getInt("clan_level");
				tp.HasCastle = rs.getInt("hasCastle");
				
				PreparedStatement statement2 = con.prepareStatement("SELECT name, siegeDate, taxPercent FROM castle WHERE id = " + tp.HasCastle);
				ResultSet rs2 = statement2.executeQuery();
				
				while (rs2.next())
				{
					tp.ChClanName = rs2.getString("name");
					tp.SomeLong = rs2.getLong("siegeDate");
					tp.Tax = rs2.getInt("taxPercent");
					Date anotherDate = new Date(tp.SomeLong);
					
					html.append("<tr>");
					html.append("<td width=70>" + tp.ChClanName + "</td>");
					html.append("<td width=70>" + tp.Owner + "</td>");
					html.append("<td width=60>" + tp.ChClanLevel + "</td>");
					html.append("<td width=60>" + tp.Tax + "%</td>");
					html.append("<td width=250>" + anotherDate + "</td>");
					html.append("</tr>");
				}
				rs2.close();
				statement2.close();
			}
			html.append("</table>");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(player.getLang(), "data/html/CommunityBoard/11.htm");
			adminReply.replace("%stat%", html.toString());
			separateAndSend(adminReply.getHtm(), player);
			
			rs.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	String OnlineTime(L2PcInstance player, int time)
	{
		String lang = player.getLang();
		long onlinetimeH;
		int onlinetimeM;
		if (((time / 60 / 60) - 0.5) <= 0)
		{
			onlinetimeH = 0;
		}
		else
		{
			onlinetimeH = Math.round((time / 60 / 60) - 0.5);
		}
		onlinetimeM = Math.round(((time / 60 / 60) - onlinetimeH) * 60);
		return "" + onlinetimeH + " " + LocalizationStorage.getInstance().getString(lang, "StateBBS.HOUR") + " " + onlinetimeM + " " + LocalizationStorage.getInstance().getString(lang, "StateBBS.MIN") + "";
	}
	
	public final static String className(L2PcInstance player, int classId)
	{
		String lang = player.getLang();
		Map<Integer, String> classList;
		
		classList = new FastMap<>();
		classList.put(0, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.0") + "");
		classList.put(1, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.1") + "");
		classList.put(2, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.2") + "");
		classList.put(3, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.3") + "");
		classList.put(4, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.4") + "");
		classList.put(5, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.5") + "");
		classList.put(6, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.6") + "");
		classList.put(7, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.7") + "");
		classList.put(8, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.8") + "");
		classList.put(9, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.9") + "");
		classList.put(10, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.10") + "");
		classList.put(11, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.11") + "");
		classList.put(12, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.12") + "");
		classList.put(13, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.13") + "");
		classList.put(14, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.14") + "");
		classList.put(15, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.15") + "");
		classList.put(16, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.16") + "");
		classList.put(17, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.17") + "");
		classList.put(18, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.18") + "");
		classList.put(19, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.19") + "");
		classList.put(20, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.20") + "");
		classList.put(21, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.21") + "");
		classList.put(22, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.22") + "");
		classList.put(23, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.23") + "");
		classList.put(24, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.24") + "");
		classList.put(25, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.25") + "");
		classList.put(26, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.26") + "");
		classList.put(27, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.27") + "");
		classList.put(28, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.28") + "");
		classList.put(29, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.29") + "");
		classList.put(30, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.30") + "");
		classList.put(31, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.31") + "");
		classList.put(32, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.32") + "");
		classList.put(33, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.33") + "");
		classList.put(34, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.34") + "");
		classList.put(35, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.35") + "");
		classList.put(36, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.36") + "");
		classList.put(37, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.37") + "");
		classList.put(38, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.38") + "");
		classList.put(39, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.39") + "");
		classList.put(40, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.40") + "");
		classList.put(41, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.41") + "");
		classList.put(42, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.42") + "");
		classList.put(43, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.43") + "");
		classList.put(44, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.44") + "");
		classList.put(45, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.45") + "");
		classList.put(46, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.46") + "");
		classList.put(47, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.47") + "");
		classList.put(48, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.48") + "");
		classList.put(49, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.49") + "");
		classList.put(50, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.50") + "");
		classList.put(51, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.51") + "");
		classList.put(52, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.52") + "");
		classList.put(53, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.53") + "");
		classList.put(54, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.54") + "");
		classList.put(55, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.55") + "");
		classList.put(56, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.56") + "");
		classList.put(57, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.57") + "");
		classList.put(88, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.88") + "");
		classList.put(89, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.89") + "");
		classList.put(90, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.90") + "");
		classList.put(91, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.91") + "");
		classList.put(92, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.92") + "");
		classList.put(93, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.93") + "");
		classList.put(94, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.94") + "");
		classList.put(95, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.95") + "");
		classList.put(96, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.96") + "");
		classList.put(97, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.97") + "");
		classList.put(98, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.98") + "");
		classList.put(99, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.99") + "");
		classList.put(100, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.100") + "");
		classList.put(101, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.101") + "");
		classList.put(102, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.102") + "");
		classList.put(103, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.103") + "");
		classList.put(104, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.104") + "");
		classList.put(105, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.105") + "");
		classList.put(106, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.106") + "");
		classList.put(107, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.107") + "");
		classList.put(108, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.108") + "");
		classList.put(109, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.109") + "");
		classList.put(110, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.110") + "");
		classList.put(111, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.111") + "");
		classList.put(112, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.112") + "");
		classList.put(113, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.113") + "");
		classList.put(114, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.114") + "");
		classList.put(115, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.115") + "");
		classList.put(116, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.116") + "");
		classList.put(117, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.117") + "");
		classList.put(118, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.118") + "");
		classList.put(123, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.123") + "");
		classList.put(124, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.124") + "");
		classList.put(125, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.125") + "");
		classList.put(126, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.126") + "");
		classList.put(127, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.127") + "");
		classList.put(128, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.128") + "");
		classList.put(129, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.129") + "");
		classList.put(130, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.130") + "");
		classList.put(131, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.131") + "");
		classList.put(132, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.132") + "");
		classList.put(133, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.133") + "");
		classList.put(134, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.134") + "");
		classList.put(135, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.135") + "");
		classList.put(136, "" + LocalizationStorage.getInstance().getString(lang, "ClassName.136") + "");
		
		return classList.get(classId);
	}
	
	public static StateBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance player)
	{
	}
	
	private static class SingletonHolder
	{
		protected static final StateBBSManager _instance = new StateBBSManager();
	}
}