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
package l2e.gameserver.model.actor.instance;

import java.io.File;

import javolution.text.TextBuilder;
import l2e.Config;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.entity.mods.VoteManager;
import l2e.gameserver.network.serverpackets.MyTargetSelectedForVote;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.PledgeCrestVote;
import l2e.gameserver.network.serverpackets.ValidateLocation;
import gov.nasa.worldwind.formats.dds.DDSConverter;

public class L2VoteManagerInstance extends L2Npc
{
	public L2VoteManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, String command)
	{
		if (player == null)
		{
			return;
		}
		
		if (command.startsWith("votehopzone"))
		{
			VoteManager.hopvote(player);
		}
		
		if (command.startsWith("votetopzone"))
		{
			VoteManager.topvote(player);
		}
		
		if (command.startsWith("rewards"))
		{
			showRewardsHtml(player);
		}
		
		if (command.startsWith("givereward"))
		{
			player.getInventory().addItem("givereward", Config.VOTE_REWARD_ID, Config.VOTE_REWARD_AMOUNT, player, null);
			player.sendMessage((new CustomMessage("L2VoteManager.REWARD_MSG", player.getLang())).toString());
			VoteManager.setHasNotVotedHop(player);
			VoteManager.setHasNotVotedTop(player);
			VoteManager.setTries(player, VoteManager.getTries(player) + 1);
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			
			player.sendPacket(new MyTargetSelectedForVote(getObjectId(), 0));
			
			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player))
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
		}
		else
		{
			showHtmlWindow(player);
		}
	}
	
	public void showHtmlWindow(L2PcInstance activeChar)
	{
		generateLogo(activeChar, 1821);
		generateLogo(activeChar, 11888);
		generateLogo(activeChar, 65531);
		generateLogo(activeChar, 65532);
		generateLogo(activeChar, 65533);
		VoteManager.hasVotedHop(activeChar);
		VoteManager.hasVotedTop(activeChar);
		
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		TextBuilder tb = new TextBuilder("");
		
		tb.append("<html><head><title>" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "L2VoteManager.VOTE_REWARD_PANEL") + "</title></head><body>");
		tb.append("<center>");
		tb.append("<table><tr><td align=\"center\"><font color=\"00ff00\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "L2VoteManager.VOTTING") + " </font>" + VoteManager.whosVoting() + "</td></tr>");
		tb.append("<tr><td align=\"center\"><font color=\"00ff00\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "L2VoteManager.TRIES_LEFT") + " </font>" + VoteManager.getTries(activeChar) + "</td></tr></table>");
		tb.append("</center>");
		if (!VoteManager.hasVotedHop() || !VoteManager.hasVotedTop())
		{
			tb.append("<table width=\"250\" cellpadding=\"5\">");
			tb.append("<tr>");
			tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><button action=\"bypass -h npc_" + getObjectId() + "_votehopzone\" width=256 height=64 back=\"Crest.crest_" + Config.SERVER_ID + "_" + 1821 + "\" fore=\"Crest.crest_" + Config.SERVER_ID + "_" + 1821 + "\"></td>");
			tb.append("</tr>");
			tb.append("</table>");
			tb.append("<table width=\"250\" cellpadding=\"5\" >");
			tb.append("<tr>");
			tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><button action=\"bypass -h npc_" + getObjectId() + "_votetopzone\" width=256 height=64 back=\"Crest.crest_" + Config.SERVER_ID + "_" + 11888 + "\" fore=\"Crest.crest_" + Config.SERVER_ID + "_" + 11888 + "\"></td>");
			tb.append("</tr>");
			tb.append("</table>");
		}
		if (VoteManager.hasVotedHop() && VoteManager.hasVotedTop())
		{
			tb.append("<table width=\"250\" cellpadding=\"5\" >");
			tb.append("<tr>");
			tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><button action=\"bypass -h npc_" + getObjectId() + "_rewards\" width=256 height=64 back=\"Crest.crest_" + Config.SERVER_ID + "_" + 65531 + "\" fore=\"Crest.crest_" + Config.SERVER_ID + "_" + 65531 + "\"></td>");
			tb.append("</tr>");
			tb.append("</table>");
		}
		tb.append("<center><table width=\"250\" cellpadding=\"5\" >");
		if (!VoteManager.hasVotedHop())
		{
			tb.append("<tr><td width=\"45\" valign=\"top\" align=\"center\"><font color=\"FF00FF\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "L2VoteManager.HOPZONE_STATUS") + " </font><img src=\"Crest.crest_" + Config.SERVER_ID + "_" + 65533 + "\" width=32 height=32>");
		}
		if (VoteManager.hasVotedHop())
		{
			tb.append("<tr><td width=\"45\" valign=\"top\" align=\"center\"><font color=\"FF00FF\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "L2VoteManager.HOPZONE_STATUS") + " </font><img src=\"Crest.crest_" + Config.SERVER_ID + "_" + 65532 + "\" width=32 height=32>");
		}
		if (!VoteManager.hasVotedTop())
		{
			tb.append("<br1><font color=\"FF00FF\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "L2VoteManager.TOPZONE_STATUS") + " </font><img src=\"Crest.crest_" + Config.SERVER_ID + "_" + 65533 + "\" width=32 height=32></td></tr>");
		}
		if (VoteManager.hasVotedTop())
		{
			tb.append("<br1><font color=\"FF00FF\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "L2VoteManager.TOPZONE_STATUS") + " </font><img src=\"Crest.crest_" + Config.SERVER_ID + "_" + 65532 + "\" width=32 height=32></td></tr>");
		}
		tb.append("</table></center>");
		tb.append("<center>");
		tb.append("<table><tr><td align=\"center\"><font color=\"00ff00\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "L2VoteManager.YOUR_VOTE_MONTH") + " </font>" + VoteManager.getMonthVotes(activeChar) + "</td></tr>");
		tb.append("<tr><td align=\"center\"><font color=\"00ff00\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "L2VoteManager.YOUR_TOTAL_VOTE") + " </font>" + VoteManager.getTotalVotes(activeChar) + "</td></tr>");
		tb.append("<tr><td align=\"center\"><font color=\"00ff00\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "L2VoteManager.PLAYERS_VOTE_MONTH") + " </font>" + VoteManager.getBigMonthVotes(activeChar) + "</td></tr>");
		tb.append("<tr><td align=\"center\"><font color=\"00ff00\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "L2VoteManager.PLAYERS_TOTAL_VOTE") + " </font>" + VoteManager.getBigTotalVotes(activeChar) + "</td></tr>");
		tb.append("<tr><td align=\"center\"><font color=\"FF00FF\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "L2VoteManager.VOTE_IN_HOPZONE") + " " + VoteManager.hopCd(activeChar) + "</font></td></tr>");
		tb.append("<tr><td align=\"center\"><font color=\"FF00FF\">" + LocalizationStorage.getInstance().getString(activeChar.getLang(), "L2VoteManager.VOTE_IN_TOPZONE") + " " + VoteManager.topCd(activeChar) + "</font></td></tr></table>");
		tb.append("</center>");
		tb.append("</body></html>");
		
		nhm.setHtml(tb.toString());
		activeChar.sendPacket(nhm);
	}
	
	public static void generateLogo(L2PcInstance activeChar, int imgId)
	{
		try
		{
			if (imgId == 1821)
			{
				File captcha = new File("data/images/hz.png");
				PledgeCrestVote packet = new PledgeCrestVote(imgId, DDSConverter.convertToDDS(captcha).array());
				activeChar.sendPacket(packet);
			}
			
			if (imgId == 11888)
			{
				File captcha = new File("data/images/tz.png");
				PledgeCrestVote packet = new PledgeCrestVote(imgId, DDSConverter.convertToDDS(captcha).array());
				activeChar.sendPacket(packet);
			}
			
			if (imgId == 65531)
			{
				File captcha = new File("data/images/reward.png");
				PledgeCrestVote packet = new PledgeCrestVote(imgId, DDSConverter.convertToDDS(captcha).array());
				activeChar.sendPacket(packet);
			}
			
			if (imgId == 65532)
			{
				File captcha = new File("data/images/yes.png");
				PledgeCrestVote packet = new PledgeCrestVote(imgId, DDSConverter.convertToDDS(captcha).array());
				activeChar.sendPacket(packet);
			}
			
			if (imgId == 65533)
			{
				File captcha = new File("data/images/no.png");
				PledgeCrestVote packet = new PledgeCrestVote(imgId, DDSConverter.convertToDDS(captcha).array());
				activeChar.sendPacket(packet);
			}
		}
		catch (Exception e)
		{
		}
		
	}
	
	public void showRewardsHtml(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		
		tb.append("<html><head><title>" + LocalizationStorage.getInstance().getString(player.getLang(), "L2VoteManager.VOTE_REWARD_PANEL") + "</title></head><body>");
		tb.append("<center>");
		tb.append("<table width=\"250\" cellpadding=\"5\" bgcolor=\"000000\">");
		tb.append("<tr>");
		tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><img src=\"L2ui_ch3.menubutton4\" width=\"38\" height=\"38\"></td>");
		tb.append("<td valign=\"top\"><font color=\"FF6600\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2VoteManager.VOTE_PANEL") + "</font>");
		tb.append("<br1><font color=\"00FF00\">" + player.getName() + "</font>, " + LocalizationStorage.getInstance().getString(player.getLang(), "L2VoteManager.GET_REWARD") + "</td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("</center>");
		tb.append("<center>");
		tb.append("<td valign=\"top\"><font color=\"FF6600\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2VoteManager.CHOOSE_REWARD") + " " + player.getName() + ".</font>");
		tb.append("<button value=\"" + LocalizationStorage.getInstance().getString(player.getLang(), "L2VoteManager.GIVE_ME") + " " + Config.VOTE_REWARD_AMOUNT + " " + ItemHolder.getInstance().getTemplate(Config.VOTE_REWARD_ID).getName() + "\" action=\"bypass -h npc_" + getObjectId() + "_givereward\" width=204 height=28 back=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_HeroConfirm\">");
		tb.append("</center>");
		
		tb.append("</body></html>");
		
		html.setHtml(tb.toString());
		player.sendPacket(html);
	}
}