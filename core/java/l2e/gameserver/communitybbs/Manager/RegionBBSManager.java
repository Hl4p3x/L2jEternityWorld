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

import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.GameServer;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.xml.ClassListParser;
import l2e.gameserver.data.xml.ExperienceParser;
import l2e.gameserver.model.BlockList;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ShowBoard;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.StringUtil;
import gnu.trove.iterator.TIntObjectIterator;

public class RegionBBSManager extends BaseBBSManager
{
	private final static Logger _log = Logger.getLogger(RegionBBSManager.class.getName());
	
	private static Logger _logChat = Logger.getLogger("chat");
	
	private static final Comparator<L2PcInstance> playerNameComparator = new Comparator<L2PcInstance>()
	{
		@Override
		public int compare(L2PcInstance p1, L2PcInstance p2)
		{
			return p1.getName().compareToIgnoreCase(p2.getName());
		}
	};
	
	protected RegionBBSManager()
	{
		_log.info(getClass().getSimpleName() + ": Loading all functions.");
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbsloc"))
		{
			showOldCommunity(activeChar, 1);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.REGIONMAIN") + "");
		}
		else if (command.startsWith("_bbsloc;page;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int page = 0;
			try
			{
				page = Integer.parseInt(st.nextToken());
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.REGIONPAGE") + "");
			}
			catch (NumberFormatException nfe)
			{
			}
			
			showOldCommunity(activeChar, page);
		}
		else if (command.startsWith("_bbsloc;playerinfo;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			String name = st.nextToken();
			
			showOldCommunityPI(activeChar, name);
			activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.REGIONPLAYERINFO") + "");
		}
		else
		{
			if (Config.COMMUNITY_TYPE == 1)
			{
				showOldCommunity(activeChar, 1);
				activeChar.setSessionVar("add_fav", command + "&" + "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FavoriteBBS.REGIONMAIN") + "");
			}
			else
			{
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101", activeChar);
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
				activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
			}
		}
	}
	
	private void showOldCommunityPI(L2PcInstance activeChar, String name)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(name);
		if (player != null)
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
			htmlCode.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PLAYER_INFO") + ": " + player.getName() + "</font><br>");
			htmlCode.append("</td>");
			htmlCode.append("</tr>");
			htmlCode.append("</table>");
			htmlCode.append("<img src=\"L2UI.SquareGray\" width=770 height=1><br>");
			htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
			htmlCode.append("<tr>");
			htmlCode.append("<td height=377>");
			
			String sex = "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.MALE") + "";
			if (player.getAppearance().getSex())
			{
				sex = "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.FEMALE") + "";
			}
			String levelApprox = "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.LOW") + "";
			if (player.getLevel() >= 60)
			{
				levelApprox = "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.VERY_HIGH") + "";
			}
			else if (player.getLevel() >= 40)
			{
				levelApprox = "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.HIGH") + "";
			}
			else if (player.getLevel() >= 20)
			{
				levelApprox = "" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.MEDIUM") + "";
			}
			htmlCode.append("<table border=0><tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.NAME") + ": " + player.getName() + "</td></tr>");
			htmlCode.append("<tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SEX") + ": " + sex + "</td></tr>");
			htmlCode.append("<tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.CLASS") + ": " + ClassListParser.getInstance().getClass(player.getClassId()).getClientCode() + "</td></tr>");
			htmlCode.append("<tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.LEVEL") + ": " + levelApprox + "</td></tr>");
			
			if ((activeChar != null) && (activeChar.isGM() || (player.getObjectId() == activeChar.getObjectId()) || Config.SHOW_LEVEL_COMMUNITYBOARD))
			{
				long nextLevelExp = 0;
				long nextLevelExpNeeded = 0;
				if (player.getLevel() < (ExperienceParser.getInstance().getMaxLevel() - 1))
				{
					nextLevelExp = ExperienceParser.getInstance().getExpForLevel(player.getLevel() + 1);
					nextLevelExpNeeded = nextLevelExp - player.getExp();
				}
				htmlCode.append("<tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.LEVEL") + ": " + String.valueOf(player.getLevel()) + "</td></tr>");
				htmlCode.append("<tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.EXP") + ": " + String.valueOf(player.getExp()) + "/" + String.valueOf(nextLevelExp) + "</td></tr>");
				htmlCode.append("<tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.NEED_EXP") + ": " + String.valueOf(nextLevelExpNeeded) + "</td></tr>");
			}
			
			if (player.getClan() != null)
			{
				htmlCode.append("<tr><td>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.CLAN") + ": " + player.getClan().getName() + "</td></tr>");
				htmlCode.append("<tr><td><br></td></tr>");
			}
			htmlCode.append("<tr><td><br></td></tr>");
			htmlCode.append("<tr><td><multiedit var=\"pm\" width=240 height=40><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SEND_PM") + "\" action=\"Write Region PM " + player.getName() + " pm pm pm\" width=160 height=21 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\"></td></tr><tr><td><br><button value=\"Back\" action=\"bypass _friendlist_0_\" width=60 height=21 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table>");
			htmlCode.append("</td></tr></table>");
			htmlCode.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
			htmlCode.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font><br>");
			htmlCode.append("</center>");
			htmlCode.append("</body></html>");
			
			separateAndSend(htmlCode.toString(), activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard(StringUtil.concat("<html><body><br><br><center>No player with name ", name, "</center><br><br></body></html>"), "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
	}
	
	private void showOldCommunity(L2PcInstance activeChar, int page)
	{
		separateAndSend(getCommunityPage(page, activeChar.isGM() ? "gm" : "pl"), activeChar);
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
			htmlCode.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "FriendsBBS.STATUS") + "</font><br>");
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
				L2PcInstance receiver = L2World.getInstance().getPlayer(ar2);
				if (receiver == null)
				{
					htmlCode.append("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.NOT_FOUND") + "<br><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.BACK") + "\" action=\"bypass _bbsloc;playerinfo;" + ar2 + "\" width=60 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					htmlCode.append("</td></tr></table>");
					htmlCode.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
					htmlCode.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>");
					htmlCode.append("</center></center>");
					htmlCode.append("</body></html>");
					separateAndSend(htmlCode.toString(), activeChar);
					return;
				}
				if (Config.JAIL_DISABLE_CHAT && receiver.isJailed())
				{
					activeChar.sendMessage((new CustomMessage("RegionBBS.JAIL_MSG", activeChar.getLang())).toString());
					return;
				}
				if (receiver.isChatBanned())
				{
					activeChar.sendMessage((new CustomMessage("RegionBBS.BAN_MSG", activeChar.getLang())).toString());
					return;
				}
				if (activeChar.isJailed() && Config.JAIL_DISABLE_CHAT)
				{
					activeChar.sendMessage((new CustomMessage("RegionBBS.JAIL_MSG_1", activeChar.getLang())).toString());
					return;
				}
				if (activeChar.isChatBanned())
				{
					activeChar.sendMessage((new CustomMessage("RegionBBS.BAN_MSG_1", activeChar.getLang())).toString());
					return;
				}
				
				if (Config.LOG_CHAT)
				{
					LogRecord record = new LogRecord(Level.INFO, ar3);
					record.setLoggerName("chat");
					record.setParameters(new Object[]
					{
						"TELL",
						"[" + activeChar.getName() + " to " + receiver.getName() + "]"
					});
					_logChat.log(record);
				}
				CreatureSay cs = new CreatureSay(activeChar.getObjectId(), Say2.TELL, activeChar.getName(), ar3);
				if (!receiver.isSilenceMode(activeChar.getObjectId()) && !BlockList.isBlocked(receiver, activeChar))
				{
					receiver.sendPacket(cs);
					activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), Say2.TELL, "->" + receiver.getName(), ar3));
					htmlCode.append("" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SENT_MESSAGE") + "<br><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.BACK") + "\" action=\"bypass _bbsloc;playerinfo;" + receiver.getName() + "\" width=60 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					htmlCode.append("</td></tr></table>");
					htmlCode.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
					htmlCode.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font></center><br>");
					htmlCode.append("</center></center>");
					htmlCode.append("</body></html>");
					separateAndSend(htmlCode.toString(), activeChar);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
					activeChar.sendPacket(sm);
					parsecmd("_bbsloc;playerinfo;" + receiver.getName(), activeChar);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else
		{
			ShowBoard sb = new ShowBoard(StringUtil.concat("<html><body><br><br><center>the command: ", ar1, " is not implemented yet</center><br><br></body></html>"), "101", activeChar);
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102", activeChar));
			activeChar.sendPacket(new ShowBoard(null, "103", activeChar));
		}
		
	}
	
	private int _onlineCount = 0;
	private int _onlineCountGm = 0;
	private static FastMap<Integer, FastList<L2PcInstance>> _onlinePlayers = new FastMap<Integer, FastList<L2PcInstance>>().shared();
	private static FastMap<Integer, FastMap<String, String>> _communityPages = new FastMap<Integer, FastMap<String, String>>().shared();
	
	public static RegionBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void changeCommunityBoard(L2PcInstance activeChar)
	{
		final FastList<L2PcInstance> sortedPlayers = new FastList<>();
		final TIntObjectIterator<L2PcInstance> it = L2World.getInstance().getAllPlayers().iterator();
		while (it.hasNext())
		{
			it.advance();
			if (it.value() != null)
			{
				sortedPlayers.add(it.value());
			}
		}
		Collections.sort(sortedPlayers, playerNameComparator);
		
		_onlinePlayers.clear();
		_onlineCount = 0;
		_onlineCountGm = 0;
		
		for (L2PcInstance player : sortedPlayers)
		{
			addOnlinePlayer(player);
		}
		_communityPages.clear();
		writeCommunityPages(activeChar);
	}
	
	private void addOnlinePlayer(L2PcInstance player)
	{
		boolean added = false;
		
		for (FastList<L2PcInstance> page : _onlinePlayers.values())
		{
			if (page.size() < Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				if (!page.contains(player))
				{
					page.add(player);
					if (!player.isInvisible())
					{
						_onlineCount++;
					}
					_onlineCountGm++;
				}
				added = true;
				break;
			}
			else if (page.contains(player))
			{
				added = true;
				break;
			}
		}
		
		if (!added)
		{
			FastList<L2PcInstance> temp = new FastList<>();
			int page = _onlinePlayers.size() + 1;
			if (temp.add(player))
			{
				_onlinePlayers.put(page, temp);
				if (!player.isInvisible())
				{
					_onlineCount++;
				}
				_onlineCountGm++;
			}
		}
	}
	
	private void writeCommunityPages(L2PcInstance activeChar)
	{
		final StringBuilder htmlCode = new StringBuilder(2000);
		final String tdClose = "</td>";
		final String trClose = "</tr>";
		final String trOpen = "<tr>";
		final String colSpacer = "<td FIXWIDTH=15></td>";
		
		for (int page : _onlinePlayers.keySet())
		{
			FastMap<String, String> communityPage = new FastMap<>();
			htmlCode.setLength(0);
			
			if (Config.BBS_COUNT_PLAYERLIST)
			{
				StringUtil.append(htmlCode, "<html><body>" + "<center><br><br><br1><br1>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>" + "</tr>" + "<tr>" + "<td height=20></td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 width=755>" + "<tr>" + "<td width=10></td>" + "<td width=770 align=center>" + "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.REGION_INFO") + "</font><br>" + "</td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td height=365>" + "<table width=770>" + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SERVER_RESTART") + ": ", String.valueOf(GameServer.dateTimeServerStarted.getTime()), tdClose + trClose + "</table>" + "<table width=770>" + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.XP_RATE") + ": x", String.valueOf(Config.RATE_XP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PARTY_XP") + ": x", String.valueOf(Config.RATE_XP * Config.RATE_PARTY_XP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.EXPONENT_EXP") + ": ", String.valueOf(Config.ALT_GAME_EXPONENT_XP), tdClose + trClose + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SP_RATE") + ": x", String.valueOf(Config.RATE_SP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PARTY_SP") + ": x", String.valueOf(Config.RATE_SP * Config.RATE_PARTY_SP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.EXPONENT_SP") + ": ", String.valueOf(Config.ALT_GAME_EXPONENT_SP), tdClose + trClose + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.DROP_RATE") + ": ", String.valueOf(Config.RATE_DROP_ITEMS), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SPOIL_RATE") + ": ", String.valueOf(Config.RATE_DROP_SPOIL), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.ADENA_RATE") + ": ", String.valueOf(Config.RATE_DROP_ITEMS_ID.get(PcInventory.ADENA_ID)), tdClose + trClose + "</table>" + "<table width=770>" + trOpen + trClose + trOpen + "<td align=center valign=top>", String.valueOf(L2World.getInstance().getAllVisibleObjectsCount()), " " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.OBJ_COUNT") + "</td>" + trClose + trOpen + "<td align=center valign=top>", String.valueOf(getOnlineCount("gm")), " " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PLAYERS_ONLINE") + "</td>" + trClose + "</table>");
			}
			else
			{
				StringUtil.append(htmlCode, "<html><body>" + "<center><br><br><br1><br1>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>" + "</tr>" + "<tr>" + "<td height=20></td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 width=755>" + "<tr>" + "<td width=10></td>" + "<td width=770 align=center>" + "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.REGION_INFO") + "</font><br>" + "</td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td height=365>" + "<table width=770>" + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SERVER_RESTART") + ": ", String.valueOf(GameServer.dateTimeServerStarted.getTime()), tdClose + trClose + "</table>" + "<table width=770>" + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.XP_RATE") + ": x", String.valueOf(Config.RATE_XP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PARTY_XP") + ": x", String.valueOf(Config.RATE_XP * Config.RATE_PARTY_XP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.EXPONENT_EXP") + ": ", String.valueOf(Config.ALT_GAME_EXPONENT_XP), tdClose + trClose + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SP_RATE") + ": x", String.valueOf(Config.RATE_SP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PARTY_SP") + ": x", String.valueOf(Config.RATE_SP * Config.RATE_PARTY_SP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.EXPONENT_SP") + ": ", String.valueOf(Config.ALT_GAME_EXPONENT_SP), tdClose + trClose + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.DROP_RATE") + ": ", String.valueOf(Config.RATE_DROP_ITEMS), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SPOIL_RATE") + ": ", String.valueOf(Config.RATE_DROP_SPOIL), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.ADENA_RATE") + ": ", String.valueOf(Config.RATE_DROP_ITEMS_ID.get(PcInventory.ADENA_ID)), tdClose + trClose + "</table>");
			}
			
			int cell = 0;
			if (Config.BBS_SHOW_PLAYERLIST)
			{
				htmlCode.append("<table border=0><tr><td><table border=0>");
				
				for (L2PcInstance player : getOnlinePlayers(page))
				{
					cell++;
					
					if (cell == 1)
					{
						htmlCode.append(trOpen);
					}
					
					StringUtil.append(htmlCode, "<td align=left valign=top FIXWIDTH=110><a action=\"bypass _bbsloc;playerinfo;", player.getName(), "\">");
					
					if (player.isGM())
					{
						StringUtil.append(htmlCode, "<font color=\"LEVEL\">", player.getName(), "</font>");
					}
					else
					{
						htmlCode.append(player.getName());
					}
					
					htmlCode.append("</a></td>");
					
					if (cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						htmlCode.append(colSpacer);
					}
					
					if (cell == Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						cell = 0;
						htmlCode.append(trClose);
					}
				}
				if ((cell > 0) && (cell < Config.NAME_PER_ROW_COMMUNITYBOARD))
				{
					htmlCode.append(trClose);
				}
				
				htmlCode.append("</table><br></td></tr>" + trOpen + "<td><img src=\"sek.cbui355\" width=600 height=1><br></td>" + trClose + "</table>");
			}
			
			if (getOnlineCount("gm") > Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				htmlCode.append("<table border=0 width=600><tr>");
				if (page == 1)
				{
					htmlCode.append("<td align=right width=190><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PREV") + "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					StringUtil.append(htmlCode, "<td align=right width=190><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PREV") + "\" action=\"bypass _bbsloc;page;", String.valueOf(page - 1), "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				
				StringUtil.append(htmlCode, "<td FIXWIDTH=10></td>" + "<td align=center valign=top width=200>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.DISPLAY") + " ", String.valueOf(((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + 1), " - ", String.valueOf(((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + getOnlinePlayers(page).size()), " " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PLAYERS") + "</td>" + "<td FIXWIDTH=10></td>");
				if (getOnlineCount("gm") <= (page * Config.NAME_PAGE_SIZE_COMMUNITYBOARD))
				{
					htmlCode.append("<td width=190><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.NEXT") + "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					StringUtil.append(htmlCode, "<td width=190><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.NEXT") + "\" action=\"bypass _bbsloc;page;", String.valueOf(page + 1), "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				htmlCode.append("</tr></table>");
			}
			
			htmlCode.append("</td></tr></table>");
			htmlCode.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
			htmlCode.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font><br>");
			htmlCode.append("</center>");
			htmlCode.append("</body></html>");
			
			communityPage.put("gm", htmlCode.toString());
			
			htmlCode.setLength(0);
			
			if (Config.BBS_COUNT_PLAYERLIST)
			{
				StringUtil.append(htmlCode, "<html><body>" + "<center><br><br><br1><br1>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>" + "</tr>" + "<tr>" + "<td height=20></td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 width=755>" + "<tr>" + "<td width=10></td>" + "<td width=770 align=center>" + "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.REGION_INFO") + "</font><br>" + "</td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td height=365>" + "<table width=770>" + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SERVER_RESTART") + ": ", String.valueOf(GameServer.dateTimeServerStarted.getTime()), tdClose + trClose + "</table>" + "<table width=770>" + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.XP_RATE") + ": x", String.valueOf(Config.RATE_XP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PARTY_XP") + ": x", String.valueOf(Config.RATE_XP * Config.RATE_PARTY_XP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.EXPONENT_EXP") + ": ", String.valueOf(Config.ALT_GAME_EXPONENT_XP), tdClose + trClose + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SP_RATE") + ": x", String.valueOf(Config.RATE_SP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PARTY_SP") + ": x", String.valueOf(Config.RATE_SP * Config.RATE_PARTY_SP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.EXPONENT_SP") + ": ", String.valueOf(Config.ALT_GAME_EXPONENT_SP), tdClose + trClose + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.DROP_RATE") + ": ", String.valueOf(Config.RATE_DROP_ITEMS), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SPOIL_RATE") + ": ", String.valueOf(Config.RATE_DROP_SPOIL), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.ADENA_RATE") + ": ", String.valueOf(Config.RATE_DROP_ITEMS_ID.get(PcInventory.ADENA_ID)), tdClose + trClose + "</table>" + "<table width=770>" + trOpen + "<td align=center valign=top>", String.valueOf(getOnlineCount("pl")), " " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PLAYERS_ONLINE") + "</td>" + trClose + "</table>");
			}
			else
			{
				StringUtil.append(htmlCode, "<html><body>" + "<center><br><br><br1><br1>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td width=80><img src=\"l2ui.bbs_lineage2\" height=16 width=80></td>" + "</tr>" + "<tr>" + "<td height=20></td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 width=755>" + "<tr>" + "<td width=10></td>" + "<td width=770 align=center>" + "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.REGION_INFO") + "</font><br>" + "</td>" + "</tr>" + "</table>" + "<img src=\"L2UI.SquareGray\" width=770 height=1><br>" + "<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td height=365>" + "<table width=770>" + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SERVER_RESTART") + ": ", String.valueOf(GameServer.dateTimeServerStarted.getTime()), tdClose + trClose + "</table>" + "<table width=770>" + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.XP_RATE") + ": x", String.valueOf(Config.RATE_XP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PARTY_XP") + ": x", String.valueOf(Config.RATE_XP * Config.RATE_PARTY_XP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.EXPONENT_EXP") + ": ", String.valueOf(Config.ALT_GAME_EXPONENT_XP), tdClose + trClose + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SP_RATE") + ": x", String.valueOf(Config.RATE_SP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PARTY_SP") + ": x", String.valueOf(Config.RATE_SP * Config.RATE_PARTY_SP), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.EXPONENT_SP") + ": ", String.valueOf(Config.ALT_GAME_EXPONENT_SP), tdClose + trClose + trOpen + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.DROP_RATE") + ": ", String.valueOf(Config.RATE_DROP_ITEMS), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.SPOIL_RATE") + ": ", String.valueOf(Config.RATE_DROP_SPOIL), tdClose + colSpacer + "<td align=center valign=top>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.ADENA_RATE") + ": ", String.valueOf(Config.RATE_DROP_ITEMS_ID.get(PcInventory.ADENA_ID)), tdClose + trClose + "</table>");
			}
			
			if (Config.BBS_SHOW_PLAYERLIST)
			{
				htmlCode.append("<table border=0><tr><td><table border=0>");
				
				cell = 0;
				for (L2PcInstance player : getOnlinePlayers(page))
				{
					if ((player == null) || (player.isInvisible()))
					{
						continue;
					}
					
					cell++;
					
					if (cell == 1)
					{
						htmlCode.append(trOpen);
					}
					
					StringUtil.append(htmlCode, "<td align=left valign=top FIXWIDTH=110><a action=\"bypass _bbsloc;playerinfo;", player.getName(), "\">");
					
					if (player.isGM())
					{
						StringUtil.append(htmlCode, "<font color=\"LEVEL\">", player.getName(), "</font>");
					}
					else
					{
						htmlCode.append(player.getName());
					}
					
					htmlCode.append("</a></td>");
					
					if (cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						htmlCode.append(colSpacer);
					}
					
					if (cell == Config.NAME_PER_ROW_COMMUNITYBOARD)
					{
						cell = 0;
						htmlCode.append(trClose);
					}
				}
				if ((cell > 0) && (cell < Config.NAME_PER_ROW_COMMUNITYBOARD))
				{
					htmlCode.append(trClose);
				}
				htmlCode.append("</table><br></td></tr>" + trOpen + "<td><img src=\"sek.cbui355\" width=600 height=1><br></td>" + trClose + "</table>");
			}
			
			if (getOnlineCount("pl") > Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				htmlCode.append("<table border=0 width=600><tr>");
				
				if (page == 1)
				{
					htmlCode.append("<td align=right width=190><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PREV") + "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					StringUtil.append(htmlCode, "<td align=right width=190><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PREV") + "\" action=\"bypass _bbsloc;page;", String.valueOf(page - 1), "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				
				StringUtil.append(htmlCode, "<td FIXWIDTH=10></td>" + "<td align=center valign=top width=200>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.DISPLAY") + " ", String.valueOf(((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + 1), " - ", String.valueOf(((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + getOnlinePlayers(page).size()), " " + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.PLAYERS") + "</td>" + "<td FIXWIDTH=10></td>");
				
				if (getOnlineCount("pl") <= (page * Config.NAME_PAGE_SIZE_COMMUNITYBOARD))
				{
					htmlCode.append("<td width=190><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.NEXT") + "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					StringUtil.append(htmlCode, "<td width=190><button value=\"" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "RegionBBS.NEXT") + "\" action=\"bypass _bbsloc;page;", String.valueOf(page + 1), "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				
				htmlCode.append("</tr></table>");
			}
			htmlCode.append("</td></tr></table>");
			htmlCode.append("<br><img src=\"L2UI.SquareGray\" width=770 height=1><br>");
			htmlCode.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "Community.COPIRYTE") + "</font><br>");
			htmlCode.append("</center>");
			htmlCode.append("</body></html>");
			communityPage.put("pl", htmlCode.toString());
			_communityPages.put(page, communityPage);
		}
	}
	
	private int getOnlineCount(String type)
	{
		if (type.equalsIgnoreCase("gm"))
		{
			return _onlineCountGm;
		}
		return _onlineCount;
	}
	
	private FastList<L2PcInstance> getOnlinePlayers(int page)
	{
		return _onlinePlayers.get(page);
	}
	
	public String getCommunityPage(int page, String type)
	{
		if (_communityPages.get(page) != null)
		{
			return _communityPages.get(page).get(type);
		}
		return null;
	}
	
	private static class SingletonHolder
	{
		protected static final RegionBBSManager _instance = new RegionBBSManager();
	}
}