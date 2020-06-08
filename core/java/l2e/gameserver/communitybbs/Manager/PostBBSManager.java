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

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.util.FastMap;
import l2e.gameserver.communitybbs.BB.Forum;
import l2e.gameserver.communitybbs.BB.Post;
import l2e.gameserver.communitybbs.BB.Post.CPost;
import l2e.gameserver.communitybbs.BB.Topic;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.ShowBoard;
import l2e.util.StringUtil;

/**
 * Rework by LordWinter 10.07.2013 Fixed by L2J Eternity-World
 */
public class PostBBSManager extends BaseBBSManager
{
	private final static Logger _log = Logger.getLogger(PostBBSManager.class.getName());
	
	private final Map<Topic, Post> _postByTopic = new FastMap<>();
	
	protected PostBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	public Post getGPosttByTopic(Topic t)
	{
		Post post = _postByTopic.get(t);
		if (post == null)
		{
			post = new Post(t);
			_postByTopic.put(t, post);
		}
		return post;
	}
	
	public void delPostByTopic(Topic t)
	{
		_postByTopic.remove(t);
	}
	
	public void addPostByTopic(Post p, Topic t)
	{
		if (_postByTopic.get(t) == null)
		{
			_postByTopic.put(t, p);
		}
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("_bbsposts;read;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			int idp = Integer.parseInt(st.nextToken());
			String index = null;
			if (st.hasMoreTokens())
			{
				index = st.nextToken();
			}
			int ind = 0;
			if (index == null)
			{
				ind = 1;
			}
			else
			{
				ind = Integer.parseInt(index);
			}
			
			showPost((TopicBBSManager.getInstance().getTopicByID(idp)), ForumsBBSManager.getInstance().getForumByID(idf), activeChar, ind);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.READPOST") + "");
		}
		else if (command.startsWith("_bbsposts;edit;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			int idt = Integer.parseInt(st.nextToken());
			int idp = Integer.parseInt(st.nextToken());
			showEditPost((TopicBBSManager.getInstance().getTopicByID(idt)), ForumsBBSManager.getInstance().getForumByID(idf), activeChar, idp);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.EDITEPOST") + "");
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
	}
	
	private void showEditPost(Topic topic, Forum forum, L2PcInstance activeChar, int idp)
	{
		Post p = getGPosttByTopic(topic);
		if ((forum == null) || (topic == null) || (p == null))
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>Error, this forum, topic or post does not exit !</center><br><br></body></html>", "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
		else
		{
			showHtmlEditPost(topic, activeChar, forum, p);
		}
	}
	
	private void showPost(Topic topic, Forum forum, L2PcInstance activeChar, int ind)
	{
		if ((forum == null) || (topic == null))
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>Error, this forum is not implemented yet</center><br><br></body></html>", "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
		else if (forum.getType() == Forum.MEMO)
		{
			showMemoPost(topic, activeChar, forum);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + forum.getName() + " is not implemented yet</center><br><br></body></html>", "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
	}
	
	private void showHtmlEditPost(Topic topic, L2PcInstance activeChar, Forum forum, Post p)
	{
		final String html = StringUtil.concat("<html><body>" + "<center><br><br><br1><br1>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>" + "</tr>" + "<tr>" + "<td height=20></td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 width=755>" + "<tr>" + "<td width=10></td>" + "<td width=600 align=center>" + "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "PostBBS.EDITE_MEMO") + ": ", topic.getName(), "</font><br>" + "</td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<center>" + "<table border=0 cellspacing=0 cellpadding=0>" + "</table>" + "<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td align=center FIXWIDTH=60 height=21>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "PostBBS.TOPIC") + "</td>" + "<td FIXWIDTH=540 height=21>", topic.getName(), "</td>" + "</tr></table>" + "<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td align=center FIXWIDTH=60 height=29 valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "PostBBS.TEXT") + "</td>" + "<td align=center FIXWIDTH=540><MultiEdit var =\"Content\" width=535 height=300></td>" + "</tr>" + "</table>" + "<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td align=center FIXWIDTH=60 height=29>&nbsp;</td>" + "<td align=center FIXWIDTH=70><br><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "PostBBS.EDITE") + "\" action=\"Write Post ", String.valueOf(forum.getID()), ";", String.valueOf(topic.getID()), ";0 _ Content Content Content\" back=\"L2UI_CT1.Button_DF\" width=65 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>" + "<td align=center FIXWIDTH=70><br><button value = \"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "PostBBS.CANCEL") + "\" action=\"bypass _bbsmemo\" back=\"L2UI_CT1.Button_DF\" width=65 height=21 fore=\"L2UI_CT1.Button_DF\"> </td>" + "<td align=center FIXWIDTH=400>&nbsp;</td>" + "</tr></table>" + "<br><br><br><br><img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font><br>" + "</center>" + "</center>" + "</body>" + "</html>");
		send1001(html, activeChar);
		send1002(activeChar, p.getCPost(0).postTxt, topic.getName(), DateFormat.getInstance().format(new Date(topic.getDate())));
	}
	
	private void showMemoPost(Topic topic, L2PcInstance activeChar, Forum forum)
	{
		Post p = getGPosttByTopic(topic);
		Locale locale = Locale.getDefault();
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
		
		String mes = p.getCPost(0).postTxt.replace(">", "&gt;");
		mes = mes.replace("<", "&lt;");
		
		final String html = StringUtil.concat("<html><body>" + "<center><br><br><br1><br1>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>" + "</tr>" + "<tr>" + "<td height=20></td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 width=755>" + "<tr>" + "<td width=10></td>" + "<td width=600 align=center>" + "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "PostBBS.VIEWING_MEMO") + "</font><br>" + "</td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td height=365>" + "<center>" + "<table border=0 cellspacing=0 cellpadding=0 bgcolor=333333>" + "<tr><td height=10></td></tr>" + "<tr>" + "<td fixWIDTH=55 align=right valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "PostBBS.TOPIC") + " : &nbsp;</td>" + "<td fixWIDTH=380 valign=top>", topic.getName(), "</td>" + "<td fixwidth=5></td>" + "<td fixwidth=50></td>" + "<td fixWIDTH=120></td>" + "</tr>" + "<tr><td height=10></td></tr>" + "<tr>" + "<td align=right><font color=\"AAAAAA\" >" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "PostBBS.AUTOR") + " : &nbsp;</font></td>" + "<td><font color=\"AAAAAA\">", topic.getOwnerName() + "</font></td>" + "<td></td>" + "<td><font color=\"AAAAAA\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "PostBBS.DATE") + " :</font></td>" + "<td><font color=\"AAAAAA\">", dateFormat.format(p.getCPost(0).postDate), "</font></td>" + "</tr>" + "<tr><td height=10></td></tr>" + "</table>" + "<br>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td fixwidth=5></td>" + "<td FIXWIDTH=600 align=left>", mes, "</td>" + "<td fixqqwidth=5></td>" + "</tr>" + "</table>" + "<br>" + "<img src=\"L2UI.squaregray\" width=\"610\" height=\"1\">" + "<table border=0 cellspacing=0 cellpadding=0 FIXWIDTH=610>" + "<tr>" + "<td width=50>" + "<button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "PostBBS.LIST") + "\" action=\"bypass _bbsmemo\" back=\"L2UI_CT1.Button_DF\" width=65 height=21 fore=\"L2UI_CT1.Button_DF\">" + "</td>" + "<td width=550 align=right><table border=0 cellspacing=0><tr>" + "<td FIXWIDTH=300></td><td><button value = \"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "PostBBS.EDITE") + "\" action=\"bypass _bbsposts;edit;", String.valueOf(forum.getID()), ";", String.valueOf(topic.getID()), ";0\" back=\"L2UI_CT1.Button_DF\" width=65 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>&nbsp;" + "<td><button value = \"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "PostBBS.DELETE") + "\" action=\"bypass _bbstopics;del;", String.valueOf(forum.getID()), ";", String.valueOf(topic.getID()), "\" back=\"L2UI_CT1.Button_DF\" width=65 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>&nbsp;" + "<td><button value = \"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "PostBBS.WRITE") + "\" action=\"bypass _bbstopics;crea;", String.valueOf(forum.getID()), "\" back=\"L2UI_CT1.Button_DF\" width=65 height=21 fore=\"L2UI_CT1.Button_DF\" ></td>&nbsp;" + "</tr></table>" + "</td>" + "</tr>" + "</table>" + "</td></tr></table>" + "<br><br><img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>" + "</center>" + "</center>" + "</body>" + "</html>");
		separateAndSend(html, activeChar);
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		
		StringTokenizer st = new StringTokenizer(ar1, ";");
		int idf = Integer.parseInt(st.nextToken());
		int idt = Integer.parseInt(st.nextToken());
		int idp = Integer.parseInt(st.nextToken());
		
		Forum f = ForumsBBSManager.getInstance().getForumByID(idf);
		if (f == null)
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + idf + " does not exist !</center><br><br></body></html>", "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
		else
		{
			Topic t = f.getTopic(idt);
			if (t == null)
			{
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>the topic: " + idt + " does not exist !</center><br><br></body></html>", "101", activeChar);
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
				activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
			}
			else
			{
				final Post p = getGPosttByTopic(t);
				if (p != null)
				{
					final CPost cp = p.getCPost(idp);
					if (cp == null)
					{
						ShowBoard sb = new ShowBoard("<html><body><br><br><center>the post: " + idp + " does not exist !</center><br><br></body></html>", "101", activeChar);
						activeChar.sendPacket(sb);
						activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
						activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
					}
					else
					{
						p.getCPost(idp).postTxt = ar4;
						p.updatetxt(idp);
						parsecmd("_bbsposts;read;" + f.getID() + ";" + t.getID(), activeChar);
					}
				}
			}
		}
	}
	
	public static PostBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PostBBSManager _instance = new PostBBSManager();
	}
}