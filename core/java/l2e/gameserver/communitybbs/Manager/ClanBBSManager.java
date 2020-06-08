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

import java.util.StringTokenizer;
import java.util.logging.Logger;

import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.util.StringUtil;

/**
 * Rework by LordWinter 12.07.2013 Fixed by L2J Eternity-World
 */
public class ClanBBSManager extends BaseBBSManager
{
	private final static Logger _log = Logger.getLogger(ClanBBSManager.class.getName());
	
	protected ClanBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	public static ClanBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbsclan"))
		{
			if ((activeChar.getClan() == null) || (activeChar.getClan().getLevel() < 2))
			{
				clanlist(activeChar, 1);
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.CLANLIST") + "");
			}
			else
			{
				clanhome(activeChar);
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MYCLAN") + "");
			}
		}
		else if (command.startsWith("_bbsclan_clanlist"))
		{
			if (command.equals("_bbsclan_clanlist"))
			{
				clanlist(activeChar, 1);
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.CLANLIST") + "");
			}
			else if (command.startsWith("_bbsclan_clanlist;"))
			{
				StringTokenizer st = new StringTokenizer(command, ";");
				st.nextToken();
				int index = Integer.parseInt(st.nextToken());
				clanlist(activeChar, index);
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.CLANLIST") + "");
			}
		}
		else if (command.startsWith("_bbsclan_clanhome"))
		{
			if (command.equals("_bbsclan_clanhome"))
			{
				clanhome(activeChar);
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.MYCLAN") + "");
			}
			else if (command.startsWith("_bbsclan_clanhome;"))
			{
				StringTokenizer st = new StringTokenizer(command, ";");
				st.nextToken();
				int index = Integer.parseInt(st.nextToken());
				clanhome(activeChar, index);
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.CLANPAGE") + "");
			}
		}
		else if (command.startsWith("_bbsclan_clannotice_edit;"))
		{
			clanNotice(activeChar, activeChar.getClanId());
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.NOTICECHANGE") + "");
		}
		else if (command.startsWith("_bbsclan_clannotice_enable"))
		{
			if (activeChar.getClan() != null)
			{
				activeChar.getClan().setNoticeEnabled(true);
			}
			clanNotice(activeChar, activeChar.getClanId());
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.CLANNOTICE") + "");
		}
		else if (command.startsWith("_bbsclan_clannotice_disable"))
		{
			if (activeChar.getClan() != null)
			{
				activeChar.getClan().setNoticeEnabled(false);
			}
			clanNotice(activeChar, activeChar.getClanId());
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.CLANNOTICE") + "");
		}
		else
		{
			separateAndSend("<html><body><br><br><center>Command : " + command + " needs core development</center><br><br></body></html>", activeChar);
		}
	}
	
	private void clanNotice(L2PcInstance activeChar, int clanId)
	{
		final L2Clan cl = ClanHolder.getInstance().getClan(clanId);
		if (cl != null)
		{
			if (cl.getLevel() < 2)
			{
				activeChar.sendPacket(SystemMessageId.NO_CB_IN_MY_CLAN);
				parsecmd("_bbsclan_clanlist", activeChar);
			}
			else
			{
				final StringBuilder html = StringUtil.startAppend(2000, "<html><body>" + "<center><br><br><br1><br1>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>" + "</tr>" + "<tr>" + "<td height=20></td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 width=755>" + "<tr>" + "<td width=10></td>" + "<td width=600 align=center>" + "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_NOTICE") + "</font><br>" + "</td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td height=365>" + "<center>");
				if (activeChar.isClanLeader())
				{
					StringUtil.append(html, "<br><br><center>" + "<table width=610 border=0 cellspacing=0 cellpadding=0>" + "<tr><td fixwidth=610><font color=\"AAAAAA\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.NOTICE_FOR_CL") + "</font> </td></tr>" + "<tr><td height=20></td></tr>");
					
					if (activeChar.getClan().isNoticeEnabled())
					{
						StringUtil.append(html, "<tr><td fixwidth=610>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.NOTICE_FUNCTION") + ":&nbsp;&nbsp;&nbsp;" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.ON") + "&nbsp;&nbsp;&nbsp;/&nbsp;&nbsp;&nbsp;<a action=\"bypass _bbsclan_clannotice_disable\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.OFF") + "</a>");
					}
					else
					{
						StringUtil.append(html, "<tr><td fixwidth=610>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.NOTICE_FUNCTION") + ":&nbsp;&nbsp;&nbsp;<a action=\"bypass _bbsclan_clannotice_enable\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.ON") + "</a>&nbsp;&nbsp;&nbsp;/&nbsp;&nbsp;&nbsp;" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.OFF") + "");
					}
					
					StringUtil.append(html, "</td></tr>" + "</table>" + "<br><img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">" + "<br> <br>" + "<table width=610 border=0 cellspacing=2 cellpadding=0>" + "<tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.NOTICE_EDITE") + ": </td></tr>" + "<tr><td height=5></td></tr>" + "<tr><td>" + "<MultiEdit var =\"Content\" width=610 height=100>" + "</td></tr>" + "</table>" + "<br>" + "<table width=610 border=0 cellspacing=0 cellpadding=0>" + "<tr><td height=5></td></tr>" + "<tr>" + "<td align=center FIXWIDTH=65><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CHANGE") + "\" action=\"Write Notice Set _ Content Content Content\" back=\"L2UI_CT1.Button_DF\" width=65 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>" + "<td align=center FIXWIDTH=65><button value = \"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.BACK") + "\" action=\"bypass _bbsclan_clanhome;" + String.valueOf(clanId) + "\" back=\"L2UI_CT1.Button_DF\" width=65 height=21 fore=\"L2UI_CT1.Button_DF\"></td>" + "<td align=center FIXWIDTH=500></td>" + "</tr>" + "</table>" + "</td></tr></table>" + "<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>" + "</center>" + "</center>" + "</body>" + "</html>");
					send1001(html.toString(), activeChar);
					send1002(activeChar, activeChar.getClan().getNotice(), " ", "0");
				}
				else
				{
					StringUtil.append(html, "<img src=\"L2UI.squareblank\" width=\"1\" height=\"10\">" + "<center>" + "<table border=0 cellspacing=0 cellpadding=0><tr>" + "<td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.NOTICE_NOT_CL") + "</td>" + "</tr></table>");
					if (activeChar.getClan().isNoticeEnabled())
					{
						StringUtil.append(html, "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CURRENT_NOTICE") + ":</td>" + "</tr>" + "<tr><td fixwidth=5></td>" + "<td FIXWIDTH=600 align=left>" + activeChar.getClan().getNotice() + "</td>" + "<td fixqqwidth=5></td>" + "</tr>" + "</table>");
					}
					StringUtil.append(html, "</center>" + "</td></tr></table>" + "<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>" + "</center>" + "</body>" + "</html>");
					separateAndSend(html.toString(), activeChar);
				}
			}
		}
	}
	
	private void clanlist(L2PcInstance activeChar, int index)
	{
		if (index < 1)
		{
			index = 1;
		}
		
		final StringBuilder html = StringUtil.startAppend(2000, "<html><body>" + "<center><br><br><br1><br1>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>" + "</tr>" + "<tr>" + "<td height=20></td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 width=755>" + "<tr>" + "<td width=10></td>" + "<td width=600 align=center>" + "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_COMMUNITY") + "</font><br>" + "</td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td height=365>" + "<center>" + "<table border=0 cellspacing=0 cellpadding=0 width=610>" + "<tr><td height=10></td></tr>" + "<tr>" + "<td fixWIDTH=5></td>" + "<center>" + "<td align=center><button value = \"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.MY_PAGE") + "\" action=\"bypass _bbsclan_clanhome;" + (String.valueOf((activeChar.getClan() != null) ? activeChar.getClan().getId() : 0)) + "\" back=\"L2UI_CT1.Button_DF\" width=110 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>" + "<td fixWIDTH=5></td>" + "</tr>" + "<tr><td height=10></td></tr>" + "</table>" + "<br>" + "<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>" + "<tr>" + "<td FIXWIDTH=5></td>" + "<td FIXWIDTH=200 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_NAME") + "</td>" + "<td FIXWIDTH=200 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_LEADER") + "</td>" + "<td FIXWIDTH=100 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_LEVEL") + "</td>" + "<td FIXWIDTH=100 align=center>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_MEMBERS") + "</td>" + "<td FIXWIDTH=5></td>" + "</tr>" + "</table>");
		
		int i = 0;
		for (L2Clan cl : ClanHolder.getInstance().getClans())
		{
			if (i > ((index + 1) * 7))
			{
				break;
			}
			
			if (i++ >= ((index - 1) * 7))
			{
				StringUtil.append(html, "<img src=\"L2UI.SquareBlank\" width=\"610\" height=\"3\">" + "<table border=0 cellspacing=0 cellpadding=0 width=610>" + "<tr> " + "<td FIXWIDTH=5></td>" + "<td FIXWIDTH=200 align=center><a action=\"bypass _bbsclan_clanhome;", String.valueOf(cl.getId()), "\">", cl.getName(), "</a></td>" + "<td FIXWIDTH=200 align=center>", cl.getLeaderName(), "</td>" + "<td FIXWIDTH=100 align=center>", String.valueOf(cl.getLevel()), "</td>" + "<td FIXWIDTH=100 align=center>", String.valueOf(cl.getMembersCount()), "</td>" + "<td FIXWIDTH=5></td>" + "</tr>" + "<tr><td height=5></td></tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\">");
			}
		}
		
		html.append("<img src=\"L2UI.SquareBlank\" width=\"610\" height=\"2\"><br>" // fix
			+ "<table cellpadding=0 cellspacing=2 border=0><tr>");
		
		if (index == 1)
		{
			html.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		else
		{
			StringUtil.append(html, "<td><button action=\"_bbsclan_clanlist;", String.valueOf(index - 1), "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		
		i = 0;
		int nbp;
		nbp = ClanHolder.getInstance().getClans().length / 8;
		if ((nbp * 8) != ClanHolder.getInstance().getClans().length)
		{
			nbp++;
		}
		for (i = 1; i <= nbp; i++)
		{
			if (i == index)
			{
				StringUtil.append(html, "<td> ", String.valueOf(i), " </td>");
			}
			else
			{
				StringUtil.append(html, "<td><a action=\"bypass _bbsclan_clanlist;", String.valueOf(i), "\"> ", String.valueOf(i), " </a></td>");
			}
			
		}
		if (index == nbp)
		{
			html.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		else
		{
			StringUtil.append(html, "<td><button action=\"bypass _bbsclan_clanlist;", String.valueOf(index + 1), "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		html.append("</tr></table>" + "</td></tr></table>" + "<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>" + "</center>" + "</center>" + "</body>" + "</html>");
		separateAndSend(html.toString(), activeChar);
	}
	
	private void clanhome(L2PcInstance activeChar)
	{
		clanhome(activeChar, activeChar.getClan().getId());
	}
	
	private void clanhome(L2PcInstance activeChar, int clanId)
	{
		L2Clan cl = ClanHolder.getInstance().getClan(clanId);
		if (cl != null)
		{
			if (cl.getLevel() < 2)
			{
				activeChar.sendPacket(SystemMessageId.NO_CB_IN_MY_CLAN);
				parsecmd("_bbsclan_clanlist", activeChar);
			}
			else
			{
				final String html = StringUtil.concat("<html><body>" + "<center><br><br><br1><br1>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>" + "</tr>" + "<tr>" + "<td height=20></td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 width=755>" + "<tr>" + "<td width=10></td>" + "<td width=600 align=center>" + "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_INFO") + "</font><br>" + "</td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td height=365>" + "<center>"
				
				+ "<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=434343>" + "<tr><td height=10></td></tr>" + "<tr>" + "<td fixWIDTH=5></td>" + "<center>" + "<td><button value = \"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_ANNOUNCE") + "\" action=\"bypass _bbsclan_clanhome;" + (String.valueOf(clanId)) + ";announce \" back=\"L2UI_CT1.Button_DF\" width=124 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>" + "<td><button value = \"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_BULLET") + "\" action=\"bypass _bbsclan_clanhome;" + String.valueOf(clanId) + ";cbb \" back=\"L2UI_CT1.Button_DF\" width=124 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>" + "<td><button value = \"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_MAIL") + "\" action=\"bypass _bbsclan_clanhome;" + String.valueOf(clanId) + ";cmail \" back=\"L2UI_CT1.Button_DF\" width=124 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>" + "<td><button value = \"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.NOTICE") + "\" action=\"bypass _bbsclan_clannotice_edit;" + String.valueOf(clanId) + ";cnotice \" back=\"L2UI_CT1.Button_DF\" width=124 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>" + "</center>" + "<td fixWIDTH=5></td>" + "</tr>" + "<tr><td height=10></td></tr>" + "</table>" + "<table border=0 cellspacing=0 cellpadding=0 width=610>" + "<tr><td height=10></td></tr>" + "<tr><td fixWIDTH=5></td>" + "<td fixwidth=290 valign=top>" + "</td>" + "<td fixWIDTH=5></td>" + "<td fixWIDTH=5 align=center valign=top><img src=\"l2ui.squaregray\" width=2  height=128></td>" + "<td fixWIDTH=5></td>" + "<td fixwidth=295>" + "<table border=0 cellspacing=0 cellpadding=0 width=295>" + "<tr>" + "<td fixWIDTH=100 align=left>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_NAME") + ":</td>" + "<td fixWIDTH=195 align=left>", cl.getName(), "</td>" + "</tr>" + "<tr><td height=7></td></tr>" + "<tr>" + "<td fixWIDTH=100 align=left>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_LEVEL") + ":</td>" + "<td fixWIDTH=195 align=left height=16>", String.valueOf(cl.getLevel()), "</td>" + "</tr>" + "<tr><td height=7></td></tr>" + "<tr>" + "<td fixWIDTH=100 align=left>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_MEMBERS") + ":</td>" + "<td fixWIDTH=195 align=left height=16>", String.valueOf(cl.getMembersCount()), "</td>" + "</tr>" + "<tr><td height=7></td></tr>" + "<tr>" + "<td fixWIDTH=100 align=left>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.CLAN_LEADER") + ":</td>" + "<td fixWIDTH=195 align=left height=16>", cl.getLeaderName(), "</td>" + "</tr>" + "<tr><td height=7></td></tr>" + "<tr><td height=7></td></tr>" + "<tr>" + "<td fixWIDTH=100 align=left>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.ALLIANCE") + ":</td>" + "<td fixWIDTH=195 align=left height=16>", (cl.getAllyName() != null) ? cl.getAllyName() : "", "</td>" + "</tr>" + "</table>" + "</td>" + "<td fixWIDTH=5></td>" + "</tr>" + "<tr><td height=10></td></tr>" + "</table>" + "<img src=\"L2UI.squaregray\" width=\"610\" height=\"1\">" + "<br>" + "<center><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "ClanBBS.BACK") + "\" action=\"bypass _bbsclan_clanlist\" back=\"L2UI_CT1.Button_DF\" width=65 height=21 fore=\"L2UI_CT1.Button_DF\"></center>" + "</td></tr></table>" + "<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>" + "</center>" + "</center>" + "</body>" + "</html>");
				separateAndSend(html, activeChar);
			}
		}
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		if (ar1.equals("Set"))
		{
			activeChar.getClan().setNotice(ar4);
			parsecmd("_bbsclan_clanhome;" + activeChar.getClan().getId(), activeChar);
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ClanBBSManager _instance = new ClanBBSManager();
	}
}