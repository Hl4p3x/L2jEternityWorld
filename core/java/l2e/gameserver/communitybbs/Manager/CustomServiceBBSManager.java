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
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javolution.text.TextBuilder;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.CharColorHolder;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

public class CustomServiceBBSManager extends BaseBBSManager
{
	private final static Logger _log = Logger.getLogger(ServiceBBSManager.class.getName());
	
	protected CustomServiceBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("_bbs_service;nickname"))
		{
			changeNameHtml(activeChar);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.NAME_CHANGE_SERVICES") + "");
		}
		else if (command.startsWith("_bbs_service;nickcolor"))
		{
			changeNameColorHtml(activeChar);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.COLOR_NAME_SERVICES") + "");
		}
		else if (command.startsWith("_bbs_service;titlecolor"))
		{
			changeTitleColorHtml(activeChar);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.COLOR_TITLE_SERVICES") + "");
		}
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String curCommand = st.nextToken();
		
		if (curCommand.startsWith("_bbs_service;changenickname"))
		{
			String name = null;
			try
			{
				name = st.nextToken();
			}
			catch (Exception e)
			{
			}
			
			if (name != null)
			{
				playerSetNickName(activeChar, name);
			}
			else
			{
				activeChar.sendMessage("You didn't enter name.");
			}
		}
		else if (curCommand.startsWith("_bbs_service;changenickcolor"))
		{
			playerSetColor(activeChar, st.nextToken(), 1);
		}
		else if (curCommand.startsWith("_bbs_service;changetitlecolor"))
		{
			playerSetColor(activeChar, st.nextToken(), 2);
		}
	}
	
	private void playerSetColor(L2PcInstance activeChar, String color, int type)
	{
		String colorh = new String("FFFFFF");
		if (color.equalsIgnoreCase("Green"))
		{
			colorh = "00FF00";
		}
		else if (color.equalsIgnoreCase("Yellow"))
		{
			colorh = "00FFFF";
		}
		else if (color.equalsIgnoreCase("Orange"))
		{
			colorh = "0099FF";
		}
		else if (color.equalsIgnoreCase("Blue"))
		{
			colorh = "FF0000";
		}
		else if (color.equalsIgnoreCase("Black"))
		{
			colorh = "000000";
		}
		else if (color.equalsIgnoreCase("Brown"))
		{
			colorh = "006699";
		}
		else if (color.equalsIgnoreCase("Light-Pink"))
		{
			colorh = "FF66FF";
		}
		else if (color.equalsIgnoreCase("Pink"))
		{
			colorh = "FF00FF";
		}
		else if (color.equalsIgnoreCase("Light-Blue"))
		{
			colorh = "FFFF66";
		}
		else if (color.equalsIgnoreCase("Turquoise"))
		{
			colorh = "999900";
		}
		else if (color.equalsIgnoreCase("Lime"))
		{
			colorh = "99FF99";
		}
		else if (color.equalsIgnoreCase("Gray"))
		{
			colorh = "999999";
		}
		else if (color.equalsIgnoreCase("Dark-Green"))
		{
			colorh = "339900";
		}
		else if (color.equalsIgnoreCase("Purple"))
		{
			colorh = "FF3399";
		}
		
		if (type == 1)
		{
			if ((activeChar.getInventory().getItemByItemId(Config.CHANGE_NICK_COLOR_ITEM) != null) && ((activeChar.getInventory().getItemByItemId(Config.CHANGE_NICK_COLOR_ITEM).getCount() >= Config.CHANGE_NICK_COLOR_ITEM_COUNT)) && (colorh != "FFFFFF"))
			{
				activeChar.destroyItemByItemId("BBSColorName", Config.CHANGE_NICK_COLOR_ITEM, Config.CHANGE_NICK_COLOR_ITEM_COUNT, activeChar, false);
				
				L2Object target = activeChar;
				activeChar.setTarget(activeChar);
				int curColor = Integer.decode("0x" + colorh);
				CharColorHolder.getInstance().add((L2PcInstance) target, curColor, System.currentTimeMillis(), (30 * 24 * 60 * 60 * 1000));
				activeChar.broadcastUserInfo();
				activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.CHANGE_NAME") + " " + color);
				changeNameColorHtml(activeChar);
			}
			else
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
				changeNameColorHtml(activeChar);
			}
		}
		else if (type == 2)
		{
			if ((activeChar.getInventory().getItemByItemId(Config.CHANGE_TITLE_COLOR_ITEM) != null) && ((activeChar.getInventory().getItemByItemId(Config.CHANGE_TITLE_COLOR_ITEM).getCount() >= Config.CHANGE_TITLE_COLOR_ITEM_COUNT)))
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement("UPDATE characters SET title_color = ? WHERE charId = ?");
					statement.setString(1, colorh);
					statement.setInt(2, activeChar.getObjectId());
					
					activeChar.destroyItemByItemId("BBSColorTitle", Config.CHANGE_TITLE_COLOR_ITEM, Config.CHANGE_TITLE_COLOR_ITEM_COUNT, activeChar, false);
					activeChar.getAppearance().setTitleColor(Integer.decode("0x" + colorh).intValue());
					activeChar.broadcastUserInfo();
					activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.CHANGE_TITLE_COLOR") + " " + color);
					changeTitleColorHtml(activeChar);
				}
				catch (SQLException e)
				{
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
				changeTitleColorHtml(activeChar);
			}
		}
	}
	
	private void playerSetNickName(L2PcInstance activeChar, String name)
	{
		if ((name.length() < 3) || (name.length() > 16) || (!(Util.isAlphaNumeric(name))) || (!(isValidName(name))))
		{
			activeChar.sendMessage((new CustomMessage("ServiceBBS.CHANGE_NAME_COLOR", activeChar.getLang())).toString());
			changeNameHtml(activeChar);
		}
		else if ((activeChar.getInventory().getItemByItemId(Config.NICK_NAME_CHANGE_ITEM) != null) && ((activeChar.getInventory().getItemByItemId(Config.NICK_NAME_CHANGE_ITEM).getCount() >= Config.NICK_NAME_CHANGE_ITEM_COUNT)))
		{
			int existing = 0;
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE char_name=?");
				statement.setString(1, name);
				ResultSet rset = statement.executeQuery();
				
				while (rset.next())
				{
					existing = rset.getInt("charId");
				}
			}
			catch (Exception e)
			{
				System.out.println("Error in check nick " + e);
			}
			
			if (existing == 0)
			{
				activeChar.setName(name);
				activeChar.destroyItemByItemId("BBSChangeName", Config.NICK_NAME_CHANGE_ITEM, Config.NICK_NAME_CHANGE_ITEM_COUNT, activeChar, false);
				activeChar.broadcastUserInfo();
				activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.YOUR_NAME") + " " + name);
				activeChar.store();
				changeNameHtml(activeChar);
			}
			else
			{
				activeChar.sendMessage((new CustomMessage("ServiceBBS.ALREADY_USE", activeChar.getLang())).toString());
				changeNameHtml(activeChar);
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			changeNameHtml(activeChar);
		}
	}
	
	private void changeNameHtml(L2PcInstance activeChar)
	{
		TextBuilder html = new TextBuilder("");
		html.append("<html>");
		html.append("<body><center>");
		html.append("<br><br>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.ENTER_NAME") + "<br> <edit var=\"name\" width=120 height=15><br>");
		html.append("<button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BUTTON_NAME") + "\" action=\"bypass -h _bbs_service;changenickname $name\" width=160 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br><br>");
		html.append("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.PRICE") + " " + Config.NICK_NAME_CHANGE_ITEM_COUNT + " <font color=\"LEVEL\">" + ItemHolder.getInstance().getTemplate(Config.NICK_NAME_CHANGE_ITEM).getName() + "</font><br><br>");
		html.append("<button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BACK") + "\" action=\"bypass -h _bbsservice\" width=120 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br>");
		html.append("</center></body></html>");
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "data/html/CommunityBoard/47.htm");
		adminReply.replace("%service%", html.toString());
		separateAndSend(adminReply.getHtm(), activeChar);
	}
	
	private void changeNameColorHtml(L2PcInstance activeChar)
	{
		TextBuilder html = new TextBuilder("");
		html.append("<html><body><center>");
		html.append("<br>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.NAME_MESSAGE") + "<br>");
		html.append("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.CHOOSE") + "<br> <combobox var=\"scolor\" width=\"100\" List=\"Green;Yellow;Orange;Blue;Black;Brown;Light-Pink;Pink;Light-Blue;Turquoise;Lime;Gray;Dark-Green;Purple\"><br>");
		html.append("<button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BUTTON_NAME_COLOR") + "\" action=\"bypass -h _bbs_service;changenickcolor $scolor\" width=160 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br><br>");
		html.append("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.PRICE") + " " + Config.CHANGE_NICK_COLOR_ITEM_COUNT + " <font color=\"LEVEL\">" + ItemHolder.getInstance().getTemplate(Config.CHANGE_NICK_COLOR_ITEM).getName() + "</font><br>");
		html.append("<br>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.AVAILABLE") + "<br>");
		html.append("<table><center>");
		html.append("<tr>");
		html.append("<td><font color=00FF00>Green</font></td>");
		html.append("<td><font color=FFFF00>Yellow</font></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=FF9900>Orange</font></td>");
		html.append("<td><font color=0000FF>Blue</font></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=000000>Black</font></td>");
		html.append("<td><font color=996600>Brown</font></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=FF66FF>Light Pink</font></td>");
		html.append("<td><font color=FF00FF>Pink</font></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=66FFFF>Light Blue</font></td>");
		html.append("<td><font color=009999>Turquoise</font></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=99FF99>Lime</font></td>");
		html.append("<td><font color=999999>Gray</font></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=009933>Dark Green</font></td>");
		html.append("<td><font color=9933FF>Purple</font></td>");
		html.append("</tr>");
		html.append("</center></table><br><br>");
		html.append("<button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BACK") + "\" action=\"bypass -h _bbsservice\" width=120 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		html.append("</center></body></html>");
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "data/html/CommunityBoard/47.htm");
		adminReply.replace("%service%", html.toString());
		separateAndSend(adminReply.getHtm(), activeChar);
	}
	
	private void changeTitleColorHtml(L2PcInstance activeChar)
	{
		TextBuilder html = new TextBuilder("");
		html.append("<html><body><center>");
		html.append("<br>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.TITLE_MESSAGE") + "<br>");
		html.append("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.CHOOSE") + "<br> <combobox var=\"scolor\" width=\"100\" List=\"Green;Yellow;Orange;Blue;Black;Brown;Light-Pink;Pink;Light-Blue;Turquoise;Lime;Gray;Dark-Green;Purple\"><br>");
		html.append("<button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BUTTON_TITLE") + "\" action=\"bypass -h _bbs_service;changetitlecolor $scolor\" width=200 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"><br><br>");
		html.append("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.PRICE") + " " + Config.CHANGE_TITLE_COLOR_ITEM_COUNT + " <font color=\"LEVEL\">" + ItemHolder.getInstance().getTemplate(Config.CHANGE_TITLE_COLOR_ITEM).getName() + "</font><br>");
		html.append("<br>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.AVAILABLE") + "<br>");
		html.append("<table><center>");
		html.append("<tr>");
		html.append("<td><font color=00FF00>Green</font></td>");
		html.append("<td><font color=FFFF00>Yellow</font></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=FF9900>Orange</font></td>");
		html.append("<td><font color=0000FF>Blue</font></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=000000>Black</font></td>");
		html.append("<td><font color=996600>Brown</font></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=FF66FF>LightPink</font></td>");
		html.append("<td><font color=FF00FF>Pink</font></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=66FFFF>LightBlue</font></td>");
		html.append("<td><font color=009999>Turquoise</font></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=99FF99>Lime</font></td>");
		html.append("<td><font color=999999>Gray</font></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=009933>DarkGreen</font></td>");
		html.append("<td><font color=9933FF>Purple</font></td>");
		html.append("</tr>");
		html.append("</center></table><br><br>");
		html.append("<button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.BACK") + "\" action=\"bypass -h _bbsservice\" width=120 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		html.append("</center></body></html>");
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getLang(), "data/html/CommunityBoard/47.htm");
		adminReply.replace("%service%", html.toString());
		separateAndSend(adminReply.getHtm(), activeChar);
	}
	
	private boolean isValidName(String text)
	{
		boolean result = true;
		String test = text;
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CNAME_TEMPLATE);
		}
		catch (PatternSyntaxException e)
		{
			_log.warning("ERROR : Character name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(test);
		if (!regexp.matches())
		{
			result = false;
		}
		return result;
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
	
	public static CustomServiceBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CustomServiceBBSManager _instance = new CustomServiceBBSManager();
	}
}