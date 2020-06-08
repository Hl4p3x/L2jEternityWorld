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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import l2e.L2DatabaseFactory;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.model.BlockList;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExMailArrived;
import l2e.gameserver.network.serverpackets.ShowBoard;
import l2e.gameserver.network.serverpackets.SystemMessage;

/**
 * Created by LordWinter 05.07.2013 Fixed by L2J Eternity-World
 */
public class MailBBSManager extends BaseBBSManager
{
	private static Logger _log = Logger.getLogger(MailBBSManager.class.getName());
	
	private static final String SELECT_CHAR_MAILS = "SELECT * FROM character_mail WHERE charId = ? ORDER BY letterId DESC";
	private static final String COUNT_UNREAD_MAILS = "SELECT count(1) FROM character_mail WHERE charId = ? and location = 'inbox' and unread=1";
	private static final String INSERT_NEW_MAIL = "INSERT INTO character_mail (charId, senderId, location, recipientNames, subject, message, sentDate, unread) VALUES (?,?,?,?,?,?,?,?)";
	private static final String DELETE_MAIL = "DELETE FROM character_mail WHERE letterId = ?";
	private static final String MARK_MAIL_READ = "UPDATE character_mail SET unread = ? WHERE letterId = ?";
	private static final String COUNT_MAILBOX = "SELECT COUNT(1) FROM character_mail WHERE charId = ? AND location = ?";
	
	protected MailBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	protected static class UpdateMail
	{
		protected int charId;
		protected int letterId;
		protected int senderId;
		protected String location;
		protected String recipientNames;
		protected String subject;
		protected String message;
		protected Timestamp sentDate;
		protected boolean unread;
	}
	
	public ArrayList<UpdateMail> getMail(L2PcInstance activeChar)
	{
		ArrayList<UpdateMail> _letters = new ArrayList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(SELECT_CHAR_MAILS);
			statement.setInt(1, activeChar.getObjectId());
			ResultSet result = statement.executeQuery();
			while (result.next())
			{
				UpdateMail letter = new UpdateMail();
				letter.charId = result.getInt("charId");
				letter.letterId = result.getInt("letterId");
				letter.senderId = result.getInt("senderId");
				letter.location = result.getString("location");
				letter.recipientNames = result.getString("recipientNames");
				letter.subject = result.getString("subject");
				letter.message = result.getString("message");
				letter.sentDate = result.getTimestamp("sentDate");
				letter.unread = result.getInt("unread") != 0;
				_letters.add(letter);
			}
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("couldnt load mail for " + activeChar.getName());
		}
		return _letters;
	}
	
	private UpdateMail getLetter(L2PcInstance activeChar, int letterId)
	{
		UpdateMail letter = new UpdateMail();
		for (UpdateMail temp : getMail(activeChar))
		{
			letter = temp;
			if (letter.letterId == letterId)
			{
				break;
			}
		}
		return letter;
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_maillist_0_1_0_"))
		{
			showInbox(activeChar, 1);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MAILMAIN") + "");
		}
		else if (command.startsWith("_maillist_0_1_0_ "))
		{
			showInbox(activeChar, Integer.parseInt(command.substring(17)));
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MAILMAIN") + "");
		}
		else if (command.equals("_maillist_0_1_0_sentbox"))
		{
			showSentbox(activeChar, 1);
		}
		else if (command.startsWith("_maillist_0_1_0_sentbox "))
		{
			showSentbox(activeChar, Integer.parseInt(command.substring(24)));
		}
		else if (command.equals("_maillist_0_1_0_archive"))
		{
			showMailArchive(activeChar, 1);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MAILARCHIVE") + "");
		}
		else if (command.startsWith("_maillist_0_1_0_archive "))
		{
			showMailArchive(activeChar, Integer.parseInt(command.substring(24)));
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MAILARCHIVE") + "");
		}
		else if (command.equals("_maillist_0_1_0_temp_archive"))
		{
			showTempMailArchive(activeChar, 1);
		}
		else if (command.startsWith("_maillist_0_1_0_temp_archive "))
		{
			showTempMailArchive(activeChar, Integer.parseInt(command.substring(29)));
		}
		else if (command.equals("_maillist_0_1_0_write"))
		{
			showWriteView(activeChar);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MAILWRITE") + "");
		}
		else if (command.startsWith("_maillist_0_1_0_view "))
		{
			UpdateMail letter = getLetter(activeChar, Integer.parseInt(command.substring(21)));
			showLetterView(activeChar, letter);
			if (letter.unread)
			{
				setLetterToRead(letter.letterId);
			}
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MAILREAD") + "");
		}
		else if (command.startsWith("_maillist_0_1_0_reply "))
		{
			UpdateMail letter = getLetter(activeChar, Integer.parseInt(command.substring(22)));
			showWriteView(activeChar, getCharName(letter.senderId), letter);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MAILREPLY") + "");
		}
		else if (command.startsWith("_maillist_0_1_0_delete "))
		{
			UpdateMail letter = getLetter(activeChar, Integer.parseInt(command.substring(23)));
			if (letter != null)
			{
				deleteLetter(letter.letterId);
			}
			showInbox(activeChar, 1);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MAILMAIN") + "");
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
	}
	
	private String abbreviate(String s, int maxWidth)
	{
		return s.length() > maxWidth ? s.substring(0, maxWidth) : s;
	}
	
	public int checkUnreadMail(L2PcInstance activeChar)
	{
		int letters = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(COUNT_UNREAD_MAILS);
			statement.setInt(1, activeChar.getObjectId());
			ResultSet result = statement.executeQuery();
			if (result.next())
			{
				letters = result.getInt(1);
			}
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("couldnt load mail for " + activeChar.getName());
		}
		return letters;
	}
	
	private void showInbox(L2PcInstance activeChar, int page)
	{
		int index = 0, minIndex = 0, maxIndex = 0;
		maxIndex = (page == 1 ? page * 12 : (page * 13) - 1);
		minIndex = maxIndex - 12;
		final StringBuilder html = new StringBuilder();
		html.append("<html><body>");
		html.append("<center><br><br><br1><br1>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td height=20></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 width=755>");
		html.append("<tr>");
		html.append("<td width=10></td>");
		html.append("<td width=600 align=center>");
		html.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.YOUR_INBOX") + "</font><br>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td height=377>");
		html.append("<center>");
		html.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=434343>");
		html.append("<tr><td height=10></td></tr>");
		html.append("<tr>");
		html.append("<td fixWIDTH=5></td>");
		html.append("<center>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.INBOX") + " [" + countLetters(activeChar, "inbox") + "]\" action=\"bypass _maillist_0_1_0_\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.SETBOX") + " [" + countLetters(activeChar, "sentbox") + "]\" action=\"bypass _maillist_0_1_0_sentbox\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.MAIL_ARCHIVE") + " [" + countLetters(activeChar, "archive") + "]\" action=\"bypass _maillist_0_1_0_archive\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.TEMP_ARCHIVE") + " [" + countLetters(activeChar, "temparchive") + "]\" action=\"bypass _maillist_0_1_0_temp_archive\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("</center>");
		html.append("<td fixWIDTH=5></td>");
		html.append("</tr>");
		html.append("<tr><td height=10></td></tr>");
		html.append("</table>");
		if (countLetters(activeChar, "inbox") == 0)
		{
			html.append("<br><center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.INBOX_EMPTY") + "</center><br>");
		}
		else
		{
			html.append("<br>");
			html.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=434343 width=610>");
			html.append("<tr>");
			html.append("<td FIXWIDTH=5></td>");
			html.append("<td FIXWIDTH=100 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.AUTOR") + "</td>");
			html.append("<td FIXWIDTH=400 align=left>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.TITLE") + "</td>");
			html.append("<td FIXWIDTH=150 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.DATE") + "</td>");
			html.append("<td FIXWIDTH=5></td>");
			html.append("</tr></table>");
			for (UpdateMail letter : getMail(activeChar))
			{
				if ((activeChar.getObjectId() == letter.charId) && letter.location.equals("inbox"))
				{
					if (index < minIndex)
					{
						index++;
						continue;
					}
					if (index > maxIndex)
					{
						break;
					}
					String tempName = getCharName(letter.senderId);
					html.append("<table border=0 cellspacing=0 cellpadding=2 width=610>");
					html.append("<tr>");
					html.append("<td FIXWIDTH=5></td>");
					html.append("<td FIXWIDTH=100 align=center>").append(abbreviate(tempName, 6)).append("</td>");
					html.append("<td FIXWIDTH=400 align=left>");
					html.append("<a action=\"bypass _maillist_0_1_0_view ").append(letter.letterId).append("\">");
					if (letter.unread)
					{
						html.append("<font color=\"LEVEL\">");
					}
					html.append(abbreviate(letter.subject, 51));
					if (letter.unread)
					{
						html.append("</font>");
					}
					html.append("</a></td><td FIXWIDTH=150 align=center>").append(letter.sentDate).append("</td>");
					html.append("<td FIXWIDTH=5></td>");
					html.append("</tr></table>");
					html.append("<img src=\"L2UI.SquareGrey\" width=\"610\" height=\"1\">");
					index++;
				}
			}
		}
		html.append("<table width=610><tr>");
		html.append("<td align=right><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.WRITE") + "\" action=\"bypass _maillist_0_1_0_write\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("</tr></table>");
		html.append("<center><table width=610><tr>");
		html.append("<td align=right><button action=\"bypass _maillist_0_1_0_ ").append(page == 1 ? page : page - 1).append("\" width=16 height=16 back=\"L2UI_ct1.button_df_left_down\" fore=\"L2UI_ct1.button_df_left\"></td>");
		for (int i = 1; i <= 7; i++)
		{
			html.append("<td align=center fixedwidth=10><a action=\"bypass _maillist_0_1_0_ ").append(i).append("\">").append(i).append("</a></td>");
		}
		html.append("<td align=left><button action=\"bypass _maillist_0_1_0_ ").append(page + 1).append("\" width=16 height=16 back=\"L2UI_ct1.button_df_right_down\" fore=\"L2UI_ct1.button_df_right\"></td>");
		html.append("</center></tr></table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>");
		html.append("</center></center>");
		html.append("</body></html>");
		separateAndSend(html.toString(), activeChar);
	}
	
	private void showLetterView(L2PcInstance activeChar, UpdateMail letter)
	{
		final StringBuilder html = new StringBuilder();
		html.append("<html><body>");
		html.append("<center><br><br><br1><br1>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td height=20></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 width=755>");
		html.append("<tr>");
		html.append("<td width=10></td>");
		html.append("<td width=600 align=center>");
		html.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.VIEW_LETTER") + "</font><br>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td height=377>");
		html.append("<center>");
		html.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=434343>");
		html.append("<tr><td height=10></td></tr>");
		html.append("<tr>");
		html.append("<td FIXWIDTH=5 height=20></td>");
		html.append("<td FIXWIDTH=100 height=20 align=right>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.SENDER") + ":&nbsp;</td>");
		html.append("<td FIXWIDTH=360 height=20 align=left>").append(getCharName(letter.senderId)).append("</td>");
		html.append("<td FIXWIDTH=150 height=20 align=right>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.SEND_TIME") + ":&nbsp;</td>");
		html.append("<td FIXWIDTH=150 height=20 align=left>").append(letter.sentDate).append("</td>");
		html.append("<td fixWIDTH=5 height=20></td>");
		html.append("</tr><tr>");
		html.append("<td FIXWIDTH=5 height=20></td>");
		html.append("<td FIXWIDTH=100 height=20 align=right>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.RECIPIENT") + ":&nbsp;</td>");
		html.append("<td FIXWIDTH=360 height=20 align=left>").append(letter.recipientNames).append("</td>");
		html.append("<td fixWIDTH=5 height=20></td>");
		html.append("</tr><tr>");
		html.append("<td FIXWIDTH=5 height=20></td>");
		html.append("<td FIXWIDTH=100 height=20 align=right>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.TITLE") + ":&nbsp;</td>");
		html.append("<td FIXWIDTH=360 height=20 align=left>").append(letter.subject).append("</td>");
		html.append("<td FIXWIDTH=150 height=20></td>");
		html.append("<td FIXWIDTH=150 height=20></td>");
		html.append("<td fixWIDTH=5 height=20></td>");
		html.append("</tr>");
		html.append("<tr><td height=10></td></tr>");
		html.append("</table>");
		html.append("<table width=610><tr>");
		html.append("<td height=10></td>");
		html.append("<td height=10></td>");
		html.append("<td height=10></td>");
		html.append("</tr><tr>");
		html.append("<td FIXWIDTH=100></td>");
		html.append("<td FIXWIDTH=560>").append(letter.message).append("</td>");
		html.append("<td FIXWIDTH=100></td>");
		html.append("</tr></table>");
		html.append("<img src=\"L2UI.SquareGrey\" width=\"610\" height=\"1\">");
		html.append("<table width=610><tr>");
		html.append("<td align=left><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.LIST") + "\" action=\"bypass _maillist_0_1_0_\" width=76 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("<td FIXWIDTH=300></td>");
		html.append("<td align=right><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.REPLY") + "\" action=\"bypass _maillist_0_1_0_reply ").append(letter.letterId).append("\" width=76 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("<td align=right><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.DELETE") + "\" action=\"bypass _maillist_0_1_0_delete ").append(letter.letterId).append("\" width=76 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("<td align=right><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.MAIL_WRITE") + "\" action=\"bypass _maillist_0_1_0_write\" width=76 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("</tr></table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>");
		html.append("</center></center>");
		html.append("</body></html>");
		separateAndSend(html.toString(), activeChar);
	}
	
	private void showSentbox(L2PcInstance activeChar, int page)
	{
		int index = 0, minIndex = 0, maxIndex = 0;
		maxIndex = (page == 1 ? page * 12 : (page * 13) - 1);
		minIndex = maxIndex - 12;
		final StringBuilder html = new StringBuilder();
		html.append("<html><body>");
		html.append("<center><br><br><br1><br1>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td height=20></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 width=755>");
		html.append("<tr>");
		html.append("<td width=10></td>");
		html.append("<td width=600 align=center>");
		html.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.YOUR_SENTBOX") + "</font><br>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td height=377>");
		html.append("<center>");
		html.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=434343>");
		html.append("<tr><td height=10></td></tr>");
		html.append("<tr>");
		html.append("<td fixWIDTH=5></td>");
		html.append("<center>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.INBOX") + " [" + countLetters(activeChar, "inbox") + "]\" action=\"bypass _maillist_0_1_0_\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.SETBOX") + " [" + countLetters(activeChar, "sentbox") + "]\" action=\"bypass _maillist_0_1_0_sentbox\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.MAIL_ARCHIVE") + " [" + countLetters(activeChar, "archive") + "]\" action=\"bypass _maillist_0_1_0_archive\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.TEMP_ARCHIVE") + " [" + countLetters(activeChar, "temparchive") + "]\" action=\"bypass _maillist_0_1_0_temp_archive\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("</center>");
		html.append("<td fixWIDTH=5></td>");
		html.append("</tr>");
		html.append("<tr><td height=10></td></tr>");
		html.append("</table>");
		if (countLetters(activeChar, "sentbox") == 0)
		{
			html.append("<br><center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.SENTBOX_EMPTY") + "</center><br>");
		}
		else
		{
			html.append("<br>");
			html.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=434343 width=610>");
			html.append("<tr>");
			html.append("<td FIXWIDTH=5></td>");
			html.append("<td FIXWIDTH=100 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.AUTOR") + "</td>");
			html.append("<td FIXWIDTH=400 align=left>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.TITLE") + "</td>");
			html.append("<td FIXWIDTH=150 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.DATE") + "</td>");
			html.append("<td FIXWIDTH=5></td>");
			html.append("</tr></table>");
			for (UpdateMail letter : getMail(activeChar))
			{
				if ((activeChar.getObjectId() == letter.charId) && letter.location.equals("sentbox"))
				{
					if (index < minIndex)
					{
						index++;
						continue;
					}
					if (index > maxIndex)
					{
						break;
					}
					String tempName = getCharName(letter.senderId);
					html.append("<table border=0 cellspacing=0 cellpadding=2 width=610>");
					html.append("<tr>");
					html.append("<td FIXWIDTH=5></td>");
					html.append("<td FIXWIDTH=100 align=center>").append(abbreviate(tempName, 6)).append("</td>");
					html.append("<td FIXWIDTH=400 align=left><a action=\"bypass _maillist_0_1_0_view ").append(letter.letterId).append("\">").append(abbreviate(letter.subject, 51)).append("</a></td>");
					html.append("<td FIXWIDTH=150 align=center>").append(letter.sentDate).append("</td>");
					html.append("<td FIXWIDTH=5></td>");
					html.append("</tr></table>");
					html.append("<img src=\"L2UI.SquareGrey\" width=\"610\" height=\"1\">");
					index++;
				}
			}
		}
		html.append("<table width=610><tr>");
		html.append("<td align=right><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.WRITE") + "\" action=\"bypass _maillist_0_1_0_write\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("</tr></table>");
		html.append("<center><table width=610><tr>");
		html.append("<td align=right><button action=\"bypass _maillist_0_1_0_sentbox ").append(page == 1 ? page : page - 1).append("\" width=16 height=16 back=\"L2UI_ct1.button_df_left_down\" fore=\"L2UI_ct1.button_df_left\"></td>");
		for (int i = 1; i <= 7; i++)
		{
			html.append("<td align=center fixedwidth=10><a action=\"bypass _maillist_0_1_0_sentbox ").append(i).append("\">").append(i).append("</a></td>");
		}
		html.append("<td align=left><button action=\"bypass _maillist_0_1_0_sentbox ").append(page + 1).append("\" width=16 height=16 back=\"L2UI_ct1.button_df_right_down\" fore=\"L2UI_ct1.button_df_right\"></td>");
		html.append("</center></tr></table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>");
		html.append("</center></center>");
		html.append("</body></html>");
		separateAndSend(html.toString(), activeChar);
	}
	
	private void showMailArchive(L2PcInstance activeChar, int page)
	{
		int index = 0, minIndex = 0, maxIndex = 0;
		maxIndex = (page == 1 ? page * 12 : (page * 13) - 1);
		minIndex = maxIndex - 12;
		final StringBuilder html = new StringBuilder();
		html.append("<html><body>");
		html.append("<center><br><br><br1><br1>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td height=20></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 width=755>");
		html.append("<tr>");
		html.append("<td width=10></td>");
		html.append("<td width=600 align=center>");
		html.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.YOUR_MAIL_ARCHIVE") + "</font><br>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td height=377>");
		html.append("<center>");
		html.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=434343>");
		html.append("<tr><td height=10></td></tr>");
		html.append("<tr>");
		html.append("<td fixWIDTH=5></td>");
		html.append("<center>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.INBOX") + " [" + countLetters(activeChar, "inbox") + "]\" action=\"bypass _maillist_0_1_0_\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.SETBOX") + " [" + countLetters(activeChar, "sentbox") + "]\" action=\"bypass _maillist_0_1_0_sentbox\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.MAIL_ARCHIVE") + " [" + countLetters(activeChar, "archive") + "]\" action=\"bypass _maillist_0_1_0_archive\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.TEMP_ARCHIVE") + " [" + countLetters(activeChar, "temparchive") + "]\" action=\"bypass _maillist_0_1_0_temp_archive\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("</center>");
		html.append("<td fixWIDTH=5></td>");
		html.append("</tr>");
		html.append("<tr><td height=10></td></tr>");
		html.append("</table>");
		if (countLetters(activeChar, "archive") == 0)
		{
			html.append("<br><center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.ARCHIVE_EMPTY") + "</center><br>");
		}
		else
		{
			html.append("<br>");
			html.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=434343 width=610>");
			html.append("<tr>");
			html.append("<td FIXWIDTH=5></td>");
			html.append("<td FIXWIDTH=100 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.AUTOR") + "</td>");
			html.append("<td FIXWIDTH=400 align=left>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.TITLE") + "</td>");
			html.append("<td FIXWIDTH=150 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.DATE") + "</td>");
			html.append("<td FIXWIDTH=5></td>");
			html.append("</tr></table>");
			for (UpdateMail letter : getMail(activeChar))
			{
				if ((activeChar.getObjectId() == letter.charId) && letter.location.equals("archive"))
				{
					if (index < minIndex)
					{
						index++;
						continue;
					}
					if (index > maxIndex)
					{
						break;
					}
					String tempName = getCharName(letter.senderId);
					html.append("<table border=0 cellspacing=0 cellpadding=2 width=610>");
					html.append("<tr>");
					html.append("<td FIXWIDTH=5></td>");
					html.append("<td FIXWIDTH=100 align=center>").append(abbreviate(tempName, 6)).append("</td>");
					html.append("<td FIXWIDTH=400 align=left><a action=\"bypass _maillist_0_1_0_view ").append(letter.letterId).append("\">").append(abbreviate(letter.subject, 51)).append("</a></td>");
					html.append("<td FIXWIDTH=150 align=center>").append(letter.sentDate).append("</td>");
					html.append("<td FIXWIDTH=5></td>");
					html.append("</tr></table>");
					html.append("<img src=\"L2UI.SquareGrey\" width=\"610\" height=\"1\">");
					index++;
				}
			}
		}
		html.append("<table width=610><tr>");
		html.append("<td align=right><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.WRITE") + "\" action=\"bypass _maillist_0_1_0_write\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("</tr></table>");
		html.append("<center><table width=610><tr>");
		html.append("<td align=right><button action=\"bypass _maillist_0_1_0_archive ").append(page == 1 ? page : page - 1).append("\" width=16 height=16 back=\"L2UI_ct1.button_df_left_down\" fore=\"L2UI_ct1.button_df_left\"></td>");
		for (int i = 1; i <= 7; i++)
		{
			html.append("<td align=center fixedwidth=10><a action=\"bypass _maillist_0_1_0_archive ").append(i).append("\">").append(i).append("</a></td>");
		}
		html.append("<td align=left><button action=\"bypass _maillist_0_1_0_archive ").append(page + 1).append("\" width=16 height=16 back=\"L2UI_ct1.button_df_right_down\" fore=\"L2UI_ct1.button_df_right\"></td>");
		html.append("</center></tr></table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>");
		html.append("</center></center>");
		html.append("</body></html>");
		separateAndSend(html.toString(), activeChar);
	}
	
	private void showTempMailArchive(L2PcInstance activeChar, int page)
	{
		int index = 0, minIndex = 0, maxIndex = 0;
		maxIndex = (page == 1 ? page * 12 : (page * 13) - 1);
		minIndex = maxIndex - 12;
		final StringBuilder html = new StringBuilder();
		html.append("<html><body>");
		html.append("<center><br><br><br1><br1>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td height=20></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 width=755>");
		html.append("<tr>");
		html.append("<td width=10></td>");
		html.append("<td width=600 align=center>");
		html.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.YOUR_TEMP_ARCHIVE") + "</font><br>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td height=377>");
		html.append("<center>");
		html.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=434343>");
		html.append("<tr><td height=10></td></tr>");
		html.append("<tr>");
		html.append("<td fixWIDTH=5></td>");
		html.append("<center>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.INBOX") + " [" + countLetters(activeChar, "inbox") + "]\" action=\"bypass _maillist_0_1_0_\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.SETBOX") + " [" + countLetters(activeChar, "sentbox") + "]\" action=\"bypass _maillist_0_1_0_sentbox\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.MAIL_ARCHIVE") + " [" + countLetters(activeChar, "archive") + "]\" action=\"bypass _maillist_0_1_0_archive\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("<td><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.TEMP_ARCHIVE") + " [" + countLetters(activeChar, "temparchive") + "]\" action=\"bypass _maillist_0_1_0_temp_archive\" back=\"L2UI_CT1.Button_DF\" width=130 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>");
		html.append("</center>");
		html.append("<td fixWIDTH=5></td>");
		html.append("</tr>");
		html.append("<tr><td height=10></td></tr>");
		html.append("</table>");
		if (countLetters(activeChar, "temparchive") == 0)
		{
			html.append("<br><center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.TEMPMAIL_EMPTY") + "</center><br>");
		}
		else
		{
			html.append("<br>");
			html.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=434343 width=610>");
			html.append("<tr>");
			html.append("<td FIXWIDTH=5></td>");
			html.append("<td FIXWIDTH=100 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.AUTOR") + "</td>");
			html.append("<td FIXWIDTH=400 align=left>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.TITLE") + "</td>");
			html.append("<td FIXWIDTH=150 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.DATE") + "</td>");
			html.append("<td FIXWIDTH=5></td>");
			html.append("</tr></table>");
			for (UpdateMail letter : getMail(activeChar))
			{
				if ((activeChar.getObjectId() == letter.charId) && letter.location.equals("temparchive"))
				{
					if (index < minIndex)
					{
						index++;
						continue;
					}
					if (index > maxIndex)
					{
						break;
					}
					String tempName = getCharName(letter.senderId);
					html.append("<table border=0 cellspacing=0 cellpadding=2 width=610>");
					html.append("<tr>");
					html.append("<td FIXWIDTH=5></td>");
					html.append("<td FIXWIDTH=100 align=center>").append(abbreviate(tempName, 6)).append("</td>");
					html.append("<td FIXWIDTH=400 align=left><a action=\"bypass _maillist_0_1_0_view ").append(letter.letterId).append("\">").append(abbreviate(letter.subject, 51)).append("</a></td>");
					html.append("<td FIXWIDTH=150 align=center>").append(letter.sentDate).append("</td>");
					html.append("<td FIXWIDTH=5></td>");
					html.append("</tr></table>");
					html.append("<img src=\"L2UI.SquareGrey\" width=\"610\" height=\"1\">");
					index++;
				}
			}
		}
		html.append("<table width=610><tr>");
		html.append("<td align=right><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.WRITE") + "\" action=\"bypass _maillist_0_1_0_write\" width=70 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("</tr></table>");
		html.append("<center><table width=610><tr>");
		html.append("<td align=right><button action=\"bypass _maillist_0_1_0_temp_archive ").append(page == 1 ? page : page - 1).append("\" width=16 height=16 back=\"L2UI_ct1.button_df_left_down\" fore=\"L2UI_ct1.button_df_left\"></td>");
		for (int i = 1; i <= 7; i++)
		{
			html.append("<td align=center fixedwidth=10><a action=\"bypass _maillist_0_1_0_temp_archive ").append(i).append("\">").append(i).append("</a></td>");
		}
		html.append("<td align=left><button action=\"bypass _maillist_0_1_0_temp_archive ").append(page + 1).append("\" width=16 height=16 back=\"L2UI_ct1.button_df_right_down\" fore=\"L2UI_ct1.button_df_right\"></td>");
		html.append("</tr></table></center>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>");
		html.append("</center></center>");
		html.append("</body></html>");
		separateAndSend(html.toString(), activeChar);
	}
	
	private void showWriteView(L2PcInstance activeChar)
	{
		final StringBuilder html = new StringBuilder();
		html.append("<html><body>");
		html.append("<center><br><br><br1><br1>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td height=20></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 width=755>");
		html.append("<tr>");
		html.append("<td width=10></td>");
		html.append("<td width=600 align=center>");
		html.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.NEW_LETTER") + "</font><br>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td height=377>");
		html.append("<table width=610><tr>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("<td FIXWIDTH=80 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.RECIPIENT") + "</td>");
		html.append("<td FIXWIDTH=530 align=left><edit var=\"Recipients\" width=530 height=11 length=\"128\"></td>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("</tr><tr>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("<td FIXWIDTH=80 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.TITLE") + "</td>");
		html.append("<td FIXWIDTH=530 align=left><edit var=\"Title\" width=530 height=11 length=\"128\"></td>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("</tr><tr>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("<td FIXWIDTH=80 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.BODY") + "</td>");
		html.append("<td FIXWIDTH=530 align=left><MultiEdit var=\"Message\" width=530 height=200></td>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("</tr></table>");
		html.append("<table width=610><tr>");
		html.append("<td align=left></td>");
		html.append("<td FIXWIDTH=80></td>");
		html.append("<td align=left><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.SEND") + "\" action=\"Write Mail Send _ Recipients Title Message\" width=70 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("<td align=left><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.CANCEL") + "\" action=\"bypass _maillist_0_1_0_\" width=70 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("<td align=left><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.DELETE") + "\" action=\"bypass _maillist_0_1_0_delete 0\" width=70 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("<td FIXWIDTH=400></td>");
		html.append("</tr></table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>");
		html.append("</center>");
		html.append("</body></html>");
		separateAndSend(html.toString(), activeChar);
	}
	
	private void showWriteView(L2PcInstance activeChar, String parcipientName, UpdateMail letter)
	{
		final StringBuilder html = new StringBuilder();
		html.append("<html><body>");
		html.append("<center><br><br><br1><br1>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td height=20></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 width=755>");
		html.append("<tr>");
		html.append("<td width=10></td>");
		html.append("<td width=600 align=center>");
		html.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.WRITTING_LETTER") + "</font><br>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td height=377>");
		html.append("<table width=610><tr>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("<td FIXWIDTH=80 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.RECIPIENT") + "</td>");
		html.append("<td FIXWIDTH=530 align=left><combobox width=530 var=\"Recipient\" list=\"").append(parcipientName).append("\"></td>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("</tr><tr>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("<td FIXWIDTH=80 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.TITLE") + "</td>");
		html.append("<td FIXWIDTH=530 align=left><edit var=\"Title\" width=530 height=11 length=\"128\"></td>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("</tr><tr>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("<td FIXWIDTH=80 align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.BODY") + "</td>");
		html.append("<td FIXWIDTH=530 align=left><multiedit var=\"Message\" width=530 height=300 length=\"2000\"></td>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("</tr></table>");
		html.append("<table width=610><tr>");
		html.append("<td align=left></td>");
		html.append("<td FIXWIDTH=80></td>");
		html.append("<td align=left><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.SEND") + "\" action=\"Write Mail Send _ Recipient Title Message\" width=70 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("<td align=left><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.CANCEL") + "\" action=\"bypass _maillist_0_1_0_\" width=70 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("<td align=left><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.DELETE") + "\" action=\"bypass _maillist_0_1_0_delete ").append(letter.letterId).append("\" width=70 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		html.append("<td FIXWIDTH=400></td>");
		html.append("</tr></table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
		html.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>");
		html.append("</center>");
		html.append("</body></html>");
		send1001(html.toString(), activeChar);
		send1002(activeChar, " ", "Re: " + letter.subject, "0");
	}
	
	private void sendLetter(String recipients, String subject, String message, L2PcInstance activeChar)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Set<String> recipts = new HashSet<>(5);
			String[] recipAr = recipients.split(";");
			for (String r : recipAr)
			{
				recipts.add(r.trim());
			}
			message = message.replaceAll("\n", "<br1>");
			boolean sent = false;
			Timestamp ts = new Timestamp(Calendar.getInstance().getTimeInMillis() - 86400000l);
			long date = Calendar.getInstance().getTimeInMillis();
			int countRecips = 0;
			int countTodaysLetters = 0;
			if (subject.isEmpty())
			{
				subject = "(no subject)";
			}
			for (UpdateMail letter : getMail(activeChar))
			{
				if (letter.sentDate.after(ts) && letter.location.equals("sentbox"))
				{
					countTodaysLetters++;
				}
			}
			if (countTodaysLetters >= 10)
			{
				activeChar.sendPacket(SystemMessageId.NO_MORE_MESSAGES_TODAY);
				return;
			}
			PreparedStatement statement = null;
			for (String recipient : recipts)
			{
				int recipId = CharNameHolder.getInstance().getIdByName(recipient);
				if (recipId <= 0)
				{
					activeChar.sendMessage("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.CANT_FIND") + " " + recipient + ", " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.GET_MAIL") + "");
				}
				else if (isGM(recipId) && !activeChar.isGM())
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_MAIL_GM_C1).addString("a GM"));
				}
				else if (isBlocked(activeChar, recipId) && !activeChar.isGM())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_BLOCKED_YOU_CANNOT_MAIL);
					for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
					{
						if ((player.getObjectId() == recipId) && player.isOnline())
						{
							sm.addPcName(player);
						}
					}
					activeChar.sendPacket(sm);
				}
				else if (isRecipInboxFull(recipId) && !activeChar.isGM())
				{
					activeChar.sendMessage(recipient.trim() + "'s " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "MailBBS.INBOX_FULL") + "");
					activeChar.sendPacket(SystemMessageId.MESSAGE_NOT_SENT);
					for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
					{
						if ((player.getObjectId() == recipId) && player.isOnline())
						{
							player.sendPacket(SystemMessageId.MAILBOX_FULL);
						}
					}
				}
				else if (((countRecips < 5) && !activeChar.isGM()) || activeChar.isGM())
				{
					if (statement == null)
					{
						statement = con.prepareStatement(INSERT_NEW_MAIL);
						statement.setInt(2, activeChar.getObjectId());
						statement.setString(3, "inbox");
						statement.setString(4, recipients);
						statement.setString(5, abbreviate(subject, 128));
						statement.setString(6, message);
						statement.setTimestamp(7, new Timestamp(date));
						statement.setInt(8, 1);
					}
					statement.setInt(1, recipId);
					statement.execute();
					sent = true;
					countRecips++;
					for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
					{
						if ((player.getObjectId() == recipId) && player.isOnline())
						{
							player.sendPacket(SystemMessageId.NEW_MAIL);
							player.sendPacket(ExMailArrived.STATIC_PACKET);
						}
					}
				}
			}
			
			if (statement != null)
			{
				statement.setInt(1, activeChar.getObjectId());
				statement.setString(3, "sentbox");
				statement.execute();
				statement.close();
			}
			if ((countRecips > 5) && !activeChar.isGM())
			{
				activeChar.sendPacket(SystemMessageId.ONLY_FIVE_RECIPIENTS);
			}
			if (sent)
			{
				activeChar.sendPacket(SystemMessageId.SENT_MAIL);
			}
		}
		catch (Exception e)
		{
			_log.warning("couldnt send letter for " + activeChar.getName());
		}
	}
	
	private int countLetters(L2PcInstance activeChar, String location)
	{
		int count = 0;
		for (UpdateMail letter : getMail(activeChar))
		{
			if ((activeChar.getObjectId() == letter.charId) && letter.location.equals(location))
			{
				count++;
			}
		}
		return count;
	}
	
	private boolean isBlocked(L2PcInstance activeChar, int recipId)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
		{
			if (player.getObjectId() == recipId)
			{
				if (BlockList.isBlocked(player, activeChar.getObjectId()))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public void deleteLetter(int letterId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(DELETE_MAIL);
			statement.setInt(1, letterId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("couldnt delete letter " + letterId);
		}
	}
	
	private void setLetterToRead(int letterId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(MARK_MAIL_READ);
			statement.setInt(1, 0);
			statement.setInt(2, letterId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("couldnt set unread to false for " + letterId);
		}
	}
	
	private String getCharName(int charId)
	{
		if (charId == 100100)
		{
			return "Auction";
		}
		String name = CharNameHolder.getInstance().getNameById(charId);
		return name == null ? "No Name" : name;
	}
	
	private boolean isGM(int charId)
	{
		if (charId == 100100)
		{
			return false;
		}
		boolean isGM = false;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT accesslevel FROM characters WHERE charId = ?");
			statement.setInt(1, charId);
			ResultSet result = statement.executeQuery();
			result.next();
			isGM = result.getInt(1) > 0;
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(e.getMessage());
		}
		return isGM;
	}
	
	private boolean isRecipInboxFull(int charId)
	{
		boolean isFull = false;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(COUNT_MAILBOX);
			statement.setInt(1, charId);
			statement.setString(2, "inbox");
			ResultSet result = statement.executeQuery();
			result.next();
			isFull = result.getInt(1) >= 100;
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(e.getMessage());
		}
		return isFull;
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		if (ar1.equals("Send"))
		{
			sendLetter(ar3, ar4, ar5, activeChar);
			showSentbox(activeChar, 0);
		}
	}
	
	public static MailBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MailBBSManager _instance = new MailBBSManager();
	}
}