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
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.ShowBoard;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 25.02.2011 Fixed by L2J Eternity-World
 */
public class TeleportBBSManager extends BaseBBSManager
{
	private final static Logger _log = Logger.getLogger(TeleportBBSManager.class.getName());
	
	protected TeleportBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	public class CBteleport
	{
		public int TpId = 0;
		public String TpName = "";
		public int PlayerId = 0;
		public int xC = 0;
		public int yC = 0;
		public int zC = 0;
	}
	
	public String points[][];
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbsteleport;"))
		{
			showTp(activeChar);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE8") + "");
		}
		else if (command.startsWith("_bbsteleport;delete;"))
		{
			StringTokenizer stDell = new StringTokenizer(command, ";");
			stDell.nextToken();
			stDell.nextToken();
			int TpNameDell = Integer.parseInt(stDell.nextToken());
			delTp(activeChar, TpNameDell);
			showTp(activeChar);
		}
		else if (command.startsWith("_bbsteleport;save;"))
		{
			String TpNameAdd = null;
			StringTokenizer stAdd = new StringTokenizer(command, ";");
			stAdd.nextToken();
			stAdd.nextToken();
			try
			{
				TpNameAdd = stAdd.nextToken();
			}
			catch (Exception e)
			{
			}
			
			if (TpNameAdd != null)
			{
				AddTp(activeChar, TpNameAdd);
			}
			else
			{
				activeChar.sendMessage((new CustomMessage("TeleportBBS.MSG_5", activeChar.getLang())).toString());
			}
			showTp(activeChar);
		}
		else if (command.startsWith("_bbsteleport;teleport;"))
		{
			StringTokenizer stGoTp = new StringTokenizer(command, " ");
			stGoTp.nextToken();
			int xTp = Integer.parseInt(stGoTp.nextToken());
			int yTp = Integer.parseInt(stGoTp.nextToken());
			int zTp = Integer.parseInt(stGoTp.nextToken());
			int priceTp = Integer.parseInt(stGoTp.nextToken());
			goTp(activeChar, xTp, yTp, zTp, priceTp);
			showTp(activeChar);
		}
		else if (command.startsWith("_bbsteleport;page;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int page = Integer.parseInt(st.nextToken());
			
			if (page == 9)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/9.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE9") + "");
			}
			else if (page == 10)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/10.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE10") + "");
			}
			else if (page == 12)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/12.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE12") + "");
			}
			else if (page == 13)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/13.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE13") + "");
			}
			else if (page == 14)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/14.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE14") + "");
			}
			else if (page == 15)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/15.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE15") + "");
			}
			else if (page == 16)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/16.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE16") + "");
			}
			else if (page == 17)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/17.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE17") + "");
			}
			else if (page == 18)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/18.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE18") + "");
			}
			else if (page == 19)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/19.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE19") + "");
			}
			else if (page == 20)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/20.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE20") + "");
			}
			else if (page == 21)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/21.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE21") + "");
			}
			else if (page == 22)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/22.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE22") + "");
			}
			else if (page == 23)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/23.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE23") + "");
			}
			else if (page == 24)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/24.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE24") + "");
			}
			else if (page == 25)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/25.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE25") + "");
			}
			else if (page == 26)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/26.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE26") + "");
			}
			else if (page == 27)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/27.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE27") + "");
			}
			else if (page == 28)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/28.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE28") + "");
			}
			else if (page == 29)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/29.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE29") + "");
			}
			else if (page == 30)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/30.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE30") + "");
			}
			else if (page == 31)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/31.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE31") + "");
			}
			else if (page == 32)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/32.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE32") + "");
			}
			else if (page == 33)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/33.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE33") + "");
			}
			else if (page == 34)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/34.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE34") + "");
			}
			else if (page == 35)
			{
				sendHtm(activeChar, "data/html/CommunityBoard/35.htm");
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.TELEPAGE35") + "");
			}
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
	}
	
	private void goTp(L2PcInstance activeChar, int xTp, int yTp, int zTp, int priceTp)
	{
		if (activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isInCombat() || activeChar.isAttackingNow() || activeChar.isInOlympiadMode() || activeChar.isJailed() || activeChar.isFlying() || (activeChar.getKarma() > 0) || activeChar.isInDuel())
		{
			activeChar.sendMessage((new CustomMessage("TeleportBBS.MSG_1", activeChar.getLang())).toString());
			return;
		}
		
		if ((priceTp > 0) && (activeChar.getAdena() < priceTp))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		
		if (priceTp > 0)
		{
			activeChar.reduceAdena("Teleport", priceTp, activeChar, true);
			
		}
		activeChar.teleToLocation(xTp, yTp, zTp);
	}
	
	private void showTp(L2PcInstance activeChar)
	{
		String lang = activeChar.getLang();
		CBteleport tp;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement st = con.prepareStatement("SELECT * FROM character_teleport WHERE charId=?;");
			st.setLong(1, activeChar.getObjectId());
			ResultSet rs = st.executeQuery();
			TextBuilder html = new TextBuilder();
			html.append("<table width=220>");
			while (rs.next())
			{
				
				tp = new CBteleport();
				tp.TpId = rs.getInt("TpId");
				tp.TpName = rs.getString("name");
				tp.PlayerId = rs.getInt("charId");
				tp.xC = rs.getInt("xPos");
				tp.yC = rs.getInt("yPos");
				tp.zC = rs.getInt("zPos");
				html.append("<tr>");
				html.append("<td>");
				html.append("<button value=\"" + tp.TpName + "\" action=\"bypass -h _bbsteleport;teleport; " + tp.xC + " " + tp.yC + " " + tp.zC + " " + 100000 + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("<td>");
				html.append("<button value=" + LocalizationStorage.getInstance().getString(lang, "TeleportBBS.DELETE") + " action=\"bypass -h _bbsteleport;delete;" + tp.TpId + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("</tr>");
			}
			html.append("</table>");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(activeChar.getLang(), "data/html/CommunityBoard/8.htm");
			adminReply.replace("%tp%", html.toString());
			separateAndSend(adminReply.getHtm(), activeChar);
			return;
		}
		catch (Exception e)
		{
		}
	}
	
	private void delTp(L2PcInstance activeChar, int TpNameDell)
	{
		try (Connection conDel = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement stDel = conDel.prepareStatement("DELETE FROM character_teleport WHERE charId=? AND TpId=?;");
			stDel.setInt(1, activeChar.getObjectId());
			stDel.setInt(2, TpNameDell);
			stDel.execute();
		}
		catch (Exception e)
		{
		}
	}
	
	private void AddTp(L2PcInstance activeChar, String TpNameAdd)
	{
		if (activeChar.isDead() || activeChar.isAlikeDead() || activeChar.isCastingNow() || activeChar.isAttackingNow())
		{
			activeChar.sendMessage((new CustomMessage("TeleportBBS.MSG_2", activeChar.getLang())).toString());
			return;
		}
		
		if (activeChar.isInCombat())
		{
			activeChar.sendMessage((new CustomMessage("TeleportBBS.MSG_3", activeChar.getLang())).toString());
			return;
		}
		
		if (activeChar.isInsideZone(ZoneId.SWAMP) || activeChar.isInsideZone(ZoneId.LANDING) || activeChar.isInsideZone(ZoneId.MONSTER_TRACK) || activeChar.isInsideZone(ZoneId.CASTLE) || activeChar.isInsideZone(ZoneId.MOTHER_TREE) || activeChar.isInsideZone(ZoneId.SCRIPT) || activeChar.isInsideZone(ZoneId.JAIL) || activeChar.isFlying())
		{
			activeChar.sendMessage((new CustomMessage("TeleportBBS.MSG_4", activeChar.getLang())).toString());
			return;
		}
		
		if (!Util.isMatchingRegexp(TpNameAdd, Config.CNAME_TEMPLATE))
		{
			activeChar.sendMessage((new CustomMessage("TeleportBBS.MSG_7", activeChar.getLang())).toString());
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM character_teleport WHERE charId=?;");
			st.setLong(1, activeChar.getObjectId());
			ResultSet rs = st.executeQuery();
			rs.next();
			if (rs.getInt(1) <= 9)
			{
				PreparedStatement st1 = con.prepareStatement("SELECT COUNT(*) FROM character_teleport WHERE charId=? AND name=?;");
				st1.setLong(1, activeChar.getObjectId());
				st1.setString(2, TpNameAdd);
				ResultSet rs1 = st1.executeQuery();
				rs1.next();
				if (rs1.getInt(1) == 0)
				{
					PreparedStatement stAdd = con.prepareStatement("INSERT INTO character_teleport (charId,xPos,yPos,zPos,name) VALUES(?,?,?,?,?)");
					stAdd.setInt(1, activeChar.getObjectId());
					stAdd.setInt(2, activeChar.getX());
					stAdd.setInt(3, activeChar.getY());
					stAdd.setInt(4, activeChar.getZ());
					stAdd.setString(5, TpNameAdd);
					stAdd.execute();
				}
				else
				{
					PreparedStatement stAdd = con.prepareStatement("UPDATE character_teleport SET xPos=?, yPos=?, zPos=? WHERE charId=? AND name=?;");
					stAdd.setInt(1, activeChar.getObjectId());
					stAdd.setInt(2, activeChar.getX());
					stAdd.setInt(3, activeChar.getY());
					stAdd.setInt(4, activeChar.getZ());
					stAdd.setString(5, TpNameAdd);
					stAdd.execute();
				}
			}
			else
			{
				activeChar.sendMessage((new CustomMessage("TeleportBBS.MSG_6", activeChar.getLang())).toString());
			}
		}
		catch (Exception e)
		{
		}
	}
	
	private boolean sendHtm(L2PcInstance player, String path)
	{
		String oriPath = path;
		if ((player.getLang() != null) && !player.getLang().equalsIgnoreCase("en"))
		{
			if (path.contains("html/"))
			{
				path = path.replace("html/", "html-" + player.getLang() + "/");
			}
		}
		String content = HtmCache.getInstance().getHtm(path);
		if ((content == null) && !oriPath.equals(path))
		{
			content = HtmCache.getInstance().getHtm(oriPath);
		}
		if (content == null)
		{
			return false;
		}
		
		separateAndSend(content, player);
		return true;
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
	
	public static TeleportBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TeleportBBSManager _instance = new TeleportBBSManager();
	}
}