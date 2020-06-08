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
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import l2e.L2DatabaseFactory;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Created by LordWinter 05.07.2013 Fixed by L2J Eternity-World
 */
public class FavoritesBBSManager extends BaseBBSManager
{
	private final static Logger _log = Logger.getLogger(EventBBSManager.class.getName());
	
	protected FavoritesBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	public class CBFavorite
	{
		public int fav_id = 0;
		public int object_id = 0;
		public String bypass = "";
		public String title = "";
		public Long date;
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, ";");
		String cmd = st.nextToken();
		
		if (cmd.equalsIgnoreCase("_bbsgetfav"))
		{
			getFavoriteList(activeChar);
		}
		else if (cmd.equalsIgnoreCase("_bbsaddfav"))
		{
			String fav = activeChar.getSessionVar("add_fav");
			if (fav != null)
			{
				String favs[] = fav.split("&");
				if (favs.length > 1)
				{
					try (Connection con = L2DatabaseFactory.getInstance().getConnection())
					{
						PreparedStatement statement = con.prepareStatement("INSERT INTO character_favorites(object_id, fav_bypass, fav_title, add_date) VALUES(?, ?, ?, ?)");
						statement.setInt(1, activeChar.getObjectId());
						statement.setString(2, favs[0]);
						statement.setString(3, favs[1]);
						statement.setLong(4, Calendar.getInstance().getTimeInMillis());
						statement.execute();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			getFavoriteList(activeChar);
		}
		else if (cmd.equalsIgnoreCase("_bbsdelfav"))
		{
			int favId = Integer.parseInt(st.nextToken());
			
			delFavorite(activeChar, favId);
			getFavoriteList(activeChar);
		}
	}
	
	private void getFavoriteList(L2PcInstance activeChar)
	{
		CBFavorite add;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM character_favorites WHERE object_id = ? ORDER BY add_date DESC");
			statement.setInt(1, activeChar.getObjectId());
			ResultSet rset = statement.executeQuery();
			
			TextBuilder html = new TextBuilder();
			html.append("<table border=0 cellspacing=0 cellpadding=2 width=755>");
			while (rset.next())
			{
				add = new CBFavorite();
				add.fav_id = rset.getInt("fav_id");
				add.object_id = rset.getInt("object_id");
				add.bypass = rset.getString("fav_bypass");
				add.title = rset.getString("fav_title");
				add.date = rset.getLong("add_date");
				
				final DateFormat dateFormat = DateFormat.getInstance();
				
				html.append("<tr>");
				html.append("<td FIXWIDTH=60>&nbsp;</td>");
				html.append("<td FIXWIDTH=400 align=left><a action=\"bypass " + add.bypass + "\">" + add.title + "</a></td>");
				html.append("<td FIXWIDTH=150 align=center>" + dateFormat.format(new Date(add.date)) + "</td>");
				html.append("<td FIXWIDTH=100 align=center><button action=\"bypass -h _bbsdelfav;" + add.fav_id + "\" back=\"L2UI_CT1.Button_DF_Delete_Down\" width=15 height=15 fore=\"L2UI_CT1.Button_DF_Delete\" ></td>");
				html.append("</tr>");
			}
			html.append("</table>");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(activeChar.getLang(), "data/html/CommunityBoard/45.htm");
			adminReply.replace("%FAV_LIST%", html.toString());
			separateAndSend(adminReply.getHtm(), activeChar);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void delFavorite(L2PcInstance activeChar, int favId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_favorites WHERE fav_id = ? AND object_id = ?");
			statement.setInt(1, favId);
			statement.setInt(2, activeChar.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
	
	public static FavoritesBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FavoritesBBSManager _instance = new FavoritesBBSManager();
	}
}