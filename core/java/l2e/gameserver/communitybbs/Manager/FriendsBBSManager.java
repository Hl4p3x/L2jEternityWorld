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
import l2e.L2DatabaseFactory;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.data.xml.ClassListParser;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.FriendPacket;
import l2e.gameserver.network.serverpackets.ShowBoard;
import l2e.gameserver.network.serverpackets.SystemMessage;

/**
 * Created by LordWinter 05.07.2013 Fixed by L2J Eternity-World
 */
public class FriendsBBSManager extends BaseBBSManager
{
	private static Logger _log = Logger.getLogger(FriendsBBSManager.class.getName());
	
	protected FriendsBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	public static FriendsBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_friendlist_0_"))
		{
			showFriendsList(activeChar);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.FRIENDMAIN") + "");
		}
		else if (command.startsWith("_friendlist_0_;playerdelete;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			String name = st.nextToken();
			deleteFriend(activeChar, name);
			showFriendsList(activeChar);
		}
		else if (command.startsWith("_friendlist_0_;playerinfo;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			String name = st.nextToken();
			showFriendsInfo(activeChar, name);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.FRIENDINFO") + "");
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
	}
	
	private void deleteFriend(L2PcInstance activeChar, String name)
	{
		int id = CharNameHolder.getInstance().getIdByName(name);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE (charId=? AND friendId=?) OR (charId=? AND friendId=?)"))
		{
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, id);
			statement.setInt(3, id);
			statement.setInt(4, activeChar.getObjectId());
			statement.execute();
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST);
			sm.addString(name);
			activeChar.sendPacket(sm);
			
			activeChar.getFriendList().remove(Integer.valueOf(id));
			activeChar.sendPacket(new FriendPacket(false, id));
		}
		catch (Exception e)
		{
			_log.warning("could not del friend objectid: " + e.getMessage());
		}
	}
	
	private void showFriendsInfo(L2PcInstance activeChar, String name)
	{
		TextBuilder htmlCode = new TextBuilder("<html><body>");
		htmlCode.append("<center><br><br><br1><br1>");
		htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
		htmlCode.append("<tr>");
		htmlCode.append("<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>");
		htmlCode.append("</tr>");
		htmlCode.append("<tr>");
		htmlCode.append("<td height=20></td>");
		htmlCode.append("</tr>");
		htmlCode.append("</table>");
		htmlCode.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		htmlCode.append("<table border=0 width=755>");
		htmlCode.append("<tr>");
		htmlCode.append("<td width=10></td>");
		htmlCode.append("<td width=600 align=center>");
		htmlCode.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.FRIEND_INFO") + "</font><br>");
		htmlCode.append("</td>");
		htmlCode.append("</tr>");
		htmlCode.append("</table>");
		htmlCode.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
		htmlCode.append("<tr>");
		htmlCode.append("<td height=377>");
		
		L2PcInstance player = L2World.getInstance().getPlayer(name);
		if (player != null)
		{
			String sex = "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.MALE") + "";
			if (player.getAppearance().getSex())
			{
				sex = "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.FEMALE") + "";
			}
			String levelApprox = "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.LOW") + "";
			if (player.getLevel() >= 60)
			{
				levelApprox = "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.VERY_HIGH") + "";
			}
			else if (player.getLevel() >= 40)
			{
				levelApprox = "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.HIGH") + "";
			}
			else if (player.getLevel() >= 20)
			{
				levelApprox = "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.MEDIUM") + "";
			}
			htmlCode.append("<table border=0><tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.NAME") + ": " + player.getName() + "</td></tr>");
			htmlCode.append("<tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.SEX") + ": " + sex + "</td></tr>");
			htmlCode.append("<tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.CLASS") + ": " + ClassListParser.getInstance().getClass(player.getClassId()).getClientCode() + "</td></tr>");
			htmlCode.append("<tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.LEVEL") + ": " + levelApprox + "</td></tr>");
			if (player.getClan() != null)
			{
				htmlCode.append("<tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.CLAN") + ": " + player.getClan().getName() + "</td></tr>");
				htmlCode.append("<tr><td><br></td></tr>");
			}
			htmlCode.append("<tr><td><br></td></tr>");
			htmlCode.append("<tr><td><multiedit var=\"pm\" width=240 height=40><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.SEND_PM") + "\" action=\"Write Friends PM " + player.getName() + " pm pm pm\" width=160 height=21 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\"></td></tr><tr><td><br><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.BACK") + "\" action=\"bypass _friendlist_0_\" width=60 height=21 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>");
			htmlCode.append("</td></tr></table>");
			htmlCode.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
			htmlCode.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font><br>");
			htmlCode.append("</center>");
			htmlCode.append("</body></html>");
			
			separateAndSend(htmlCode.toString(), activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>No player with name " + name + "</center><br><br></body></html>", "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
	}
	
	private void showFriendsList(L2PcInstance activeChar)
	{
		TextBuilder htmlCode = new TextBuilder("<html><body>");
		htmlCode.append("<center><br><br><br1><br1>");
		htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
		htmlCode.append("<tr>");
		htmlCode.append("<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>");
		htmlCode.append("</tr>");
		htmlCode.append("<tr>");
		htmlCode.append("<td height=20></td>");
		htmlCode.append("</tr>");
		htmlCode.append("</table>");
		htmlCode.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		htmlCode.append("<table border=0 width=755>");
		htmlCode.append("<tr>");
		htmlCode.append("<td width=10></td>");
		htmlCode.append("<td width=600 align=center>");
		htmlCode.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.FRIEND_LIST") + "</font><br>");
		htmlCode.append("</td>");
		htmlCode.append("</tr>");
		htmlCode.append("</table>");
		htmlCode.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
		htmlCode.append("<tr>");
		htmlCode.append("<td height=377>");
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT friendId FROM character_friends WHERE charId=?"))
		{
			statement.setInt(1, activeChar.getObjectId());
			ResultSet rset = statement.executeQuery();
			
			htmlCode.append("<table border=0>");
			htmlCode.append("<tr><td width=770 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.TO_ADD") + " '<font color=\"LEVEL\">/friendinvite " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.FRIEND_NAME") + "</font>'</td></tr>");
			htmlCode.append("<tr><td width=770 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.CAN_REMOVE") + "</td></tr>");
			htmlCode.append("</table><br>");
			htmlCode.append("<table border=0>");
			htmlCode.append("<tr><td width=770 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.YOUR_FRIEND") + ":</td></tr>");
			htmlCode.append("</table>");
			htmlCode.append("<table width=770>");
			while (rset.next())
			{
				int friendId = rset.getInt("friendId");
				String friendName = CharNameHolder.getInstance().getNameById(friendId);
				L2PcInstance friend = friendId != 0 ? (L2PcInstance) L2World.getInstance().findObject(friendId) : L2World.getInstance().getPlayer(friendName);
				if (friend == null)
				{
					htmlCode.append("<tr>");
					htmlCode.append("<center>");
					htmlCode.append("<td FIXWIDTH=55>&nbsp;</td>");
					htmlCode.append("<td>" + friendName + "</td>");
					htmlCode.append("<td>&nbsp;</td>");
					htmlCode.append("<td><font color=\"D70000\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.OFF") + "</font></td>");
					htmlCode.append("<td>&nbsp;</td>");
					htmlCode.append("</center>");
					htmlCode.append("</tr>");
				}
				else
				{
					htmlCode.append("<tr>");
					htmlCode.append("<center>");
					htmlCode.append("<td FIXWIDTH=55>&nbsp;</td>");
					htmlCode.append("<td><a action=\"bypass _friendlist_0_;playerinfo;" + friendName + "\">" + friendName + "</a></td>");
					htmlCode.append("<td>&nbsp;</td>");
					htmlCode.append("<td><font color=\"00CC00\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.ON") + "</font></td>");
					htmlCode.append("<td>&nbsp;</td>");
					htmlCode.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.DELETE") + "\" action=\"bypass _friendlist_0_;playerdelete;" + friendName + "\" width=60 height=21 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\"></td>");
					htmlCode.append("</center>");
					htmlCode.append("</tr>");
				}
			}
			htmlCode.append("</table>");
			htmlCode.append("</td>");
			htmlCode.append("</tr>");
			htmlCode.append("</table>");
			htmlCode.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
			htmlCode.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font><br>");
			htmlCode.append("</center>");
			htmlCode.append("</body></html>");
		}
		catch (Exception e)
		{
			htmlCode.append("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.CAN_T_SHOW") + "</body></html>");
		}
		separateAndSend(htmlCode.toString(), activeChar);
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		if (ar1.equals("PM"))
		{
			TextBuilder htmlCode = new TextBuilder("<html><body>");
			htmlCode.append("<center><br><br><br1><br1>");
			htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
			htmlCode.append("<tr>");
			htmlCode.append("<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>");
			htmlCode.append("</tr>");
			htmlCode.append("<tr>");
			htmlCode.append("<td height=20></td>");
			htmlCode.append("</tr>");
			htmlCode.append("</table>");
			htmlCode.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
			htmlCode.append("<table border=0 width=755>");
			htmlCode.append("<tr>");
			htmlCode.append("<td width=10></td>");
			htmlCode.append("<td width=600 align=center>");
			htmlCode.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.STATUS") + "</font><br>");
			htmlCode.append("</td>");
			htmlCode.append("</tr>");
			htmlCode.append("</table>");
			htmlCode.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
			htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
			htmlCode.append("<tr>");
			htmlCode.append("<td height=377>");
			htmlCode.append("<center>");
			try
			{
				L2PcInstance reciever = L2World.getInstance().getPlayer(ar2);
				if (reciever == null)
				{
					htmlCode.append("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.NOT_FOUND") + "<br><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.BACK") + "\" action=\"bypass _friendlist_0_;playerinfo;" + ar2 + "\" width=60 height=21 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					htmlCode.append("</td></tr></table>");
					htmlCode.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
					htmlCode.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>");
					htmlCode.append("</center></center>");
					htmlCode.append("</body></html>");
					separateAndSend(htmlCode.toString(), activeChar);
					return;
				}
				if (activeChar.isChatBanned())
				{
					htmlCode.append("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.BANNED") + "<br><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.BACK") + "\" action=\"bypass _friendlist_0_;playerinfo;" + reciever.getName() + "\" width=60 height=21 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					htmlCode.append("</td></tr></table>");
					htmlCode.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
					htmlCode.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>");
					htmlCode.append("</center></center>");
					htmlCode.append("</body></html>");
					separateAndSend(htmlCode.toString(), activeChar);
					return;
				}
				if (!reciever.getMessageRefusal())
				{
					reciever.sendPacket(new CreatureSay(0, Say2.TELL, activeChar.getName(), ar3));
					activeChar.sendPacket(new CreatureSay(0, Say2.TELL, activeChar.getName(), ar3));
					htmlCode.append("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.SENT") + "<br><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.BACK") + "\" action=\"bypass _friendlist_0_;playerinfo;" + reciever.getName() + "\" width=60 height=21 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\">");
					htmlCode.append("</td></tr></table>");
					htmlCode.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
					htmlCode.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>");
					htmlCode.append("</center></center>");
					htmlCode.append("</body></html>");
					separateAndSend(htmlCode.toString(), activeChar);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
					parsecmd("_friendlist_0_;playerinfo;" + reciever.getName(), activeChar);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + ar1 + " is not implemented yet</center><br><br></body></html>", "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
	}
	
	private static class SingletonHolder
	{
		protected static final FriendsBBSManager _instance = new FriendsBBSManager();
	}
}