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

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.entity.mods.AchievementsManager;
import l2e.gameserver.model.entity.mods.base.Achievement;
import l2e.gameserver.model.entity.mods.base.Condition;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Created by LordWinter 15.06.2013 Fixed by L2J Eternity-World
 */
public class L2AchievementsInstance extends L2NpcInstance
{
	private boolean first = true;
	
	public L2AchievementsInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player == null)
		{
			return;
		}
		if (command.startsWith("showMyAchievements"))
		{
			player.getAchievemntData();
			showMyAchievements(player);
		}
		else if (command.startsWith("achievementInfo"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			
			showAchievementInfo(id, player);
		}
		else if (command.startsWith("topList"))
		{
			showTopListWindow(player);
		}
		else if (command.startsWith("showMainWindow"))
		{
			showChatWindow(player, 0);
		}
		else if (command.startsWith("getReward"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			if (id == 10)
			{
				player.destroyItemByItemId("", 8787, 200L, this, true);
				AchievementsManager.getInstance().rewardForAchievement(id, player);
			}
			else if ((id == 4) || (id == 19))
			{
				L2ItemInstance weapon = player.getInventory().getPaperdollItem(5);
				if (weapon != null)
				{
					int objid = weapon.getObjectId();
					if (AchievementsManager.getInstance().getAchievementList().get(Integer.valueOf(id)).meetAchievementRequirements(player))
					{
						if (!AchievementsManager.getInstance().isBinded(objid, id))
						{
							AchievementsManager.getInstance().getBinded().add(objid + "@" + id);
							player.saveAchievementData(id, objid);
							AchievementsManager.getInstance().rewardForAchievement(id, player);
						}
						else
						{
							player.sendMessage((new CustomMessage("L2Achievements.MESSAGE_1", player.getLang())).toString());
						}
					}
					else
					{
						player.sendMessage((new CustomMessage("L2Achievements.MESSAGE_2", player.getLang())).toString());
					}
				}
				else
				{
					player.sendMessage((new CustomMessage("L2Achievements.MESSAGE_3", player.getLang())).toString());
				}
			}
			else if ((id == 6) || (id == 18))
			{
				int clid = player.getClan().getId();
				
				if (!AchievementsManager.getInstance().isBinded(clid, id))
				{
					AchievementsManager.getInstance().getBinded().add(clid + "@" + id);
					player.saveAchievementData(id, clid);
					AchievementsManager.getInstance().rewardForAchievement(id, player);
				}
				else
				{
					player.sendMessage((new CustomMessage("L2Achievements.MESSAGE_4", player.getLang())).toString());
				}
			}
			else
			{
				player.saveAchievementData(id, 0);
				AchievementsManager.getInstance().rewardForAchievement(id, player);
			}
			showMyAchievements(player);
		}
		else if (command.startsWith("showMyStats"))
		{
			showMyStatsWindow(player);
		}
		else if (command.startsWith("showHelpWindow"))
		{
			showHelpWindow(player);
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		if (first)
		{
			AchievementsManager.getInstance().loadUsed();
			first = false;
		}
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.TITLE") + "</title><body><center><br>");
		tb.append("" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.HELLO") + " <font color=\"LEVEL\">" + player.getName() + "</font><br>" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.LOOKING") + "");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		tb.append("<button value=\"" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.MY_ACVS") + "\" action=\"bypass -h npc_%objectId%_showMyAchievements\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=115 height=21>");
		tb.append("<button value=\"" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.STATS") + "\" action=\"bypass -h npc_%objectId%_showMyStats\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=115 height=21>");
		tb.append("<button value=\"" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.HELP") + "\" action=\"bypass -h npc_%objectId%_showHelpWindow\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=115 height=21>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showMyAchievements(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.TITLE") + "</title><body><br>");
		
		tb.append("<center><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.MY_ACVS") + "</font>:</center><br>");
		
		if (AchievementsManager.getInstance().getAchievementList().isEmpty())
		{
			tb.append("" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.NO_ACVS") + "");
		}
		else
		{
			int i = 0;
			
			tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
			tb.append("<tr><td width=230 align=\"left\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.NAME") + ":</td><td width=80 align=\"right\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.INFO") + ":</td><td width=210 align=\"center\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.STATUS") + ":</td></tr></table>");
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
			
			for (Achievement a : AchievementsManager.getInstance().getAchievementList().values())
			{
				tb.append(getTableColor(i));
				tb.append("<tr><td width=230 align=\"left\">" + a.getName() + "</td><td width=80 align=\"right\"><a action=\"bypass -h npc_%objectId%_achievementInfo " + a.getID() + "\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.ACV_INFO") + "</a></td><td width=210 align=\"center\">" + getStatusString(a.getID(), player) + "</td></tr></table>");
				i++;
			}
			
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
			tb.append("<center><button value=\"" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.BACK") + "\" action=\"bypass -h npc_%objectId%_showMainWindow\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=95 height=21></center>");
		}
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showAchievementInfo(int achievementID, L2PcInstance player)
	{
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(Integer.valueOf(achievementID));
		
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.TITLE") + "</title><body><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">" + a.getName() + "</td></tr></table><br>");
		tb.append("<center>" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.STATUS") + ": " + getStatusString(achievementID, player));
		
		if ((a.meetAchievementRequirements(player)) && (!player.getCompletedAchievements().contains(Integer.valueOf(achievementID))))
		{
			tb.append("<button value=\"" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.REWARD") + "\" action=\"bypass -h npc_%objectId%_getReward " + a.getID() + "\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=115 height=21>");
		}
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.DESCRIPTION") + "</td></tr></table><br>");
		tb.append(a.getDescription());
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=230 align=\"left\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.COND") + ":</td><td width=120 align=\"left\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.VALUE") + ":</td><td width=220 align=\"center\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.STATUS") + ":</td></tr></table>");
		tb.append(getConditionsStatus(achievementID, player));
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.BACK") + "\" action=\"bypass -h npc_%objectId%_showMyAchievements\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=95 height=21></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showMyStatsWindow(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.TITLE") + "</title><body><center><br>");
		tb.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.ACV_STATISTIC") + "</font>:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		player.getAchievemntData();
		int completedCount = player.getCompletedAchievements().size();
		
		tb.append("" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.COMPLETE") + ": " + completedCount + " / <font color=\"LEVEL\">" + AchievementsManager.getInstance().getAchievementList().size() + "</font>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.BACK") + "\" action=\"bypass -h npc_%objectId%_showMainWindow\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=95 height=21></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showTopListWindow(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.TITLE") + "</title><body><center><br>");
		tb.append("" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.CHECK") + " <font color=\"LEVEL\">Achievements </font>" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.TOP") + ":");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		tb.append("" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.NO_IMPL") + "");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.BACK") + "\" action=\"bypass -h npc_%objectId%_showMainWindow\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=95 height=21></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showHelpWindow(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.TITLE") + "</title><body><center><br>");
		tb.append("<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.HELP_PAGE") + "</font>:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		tb.append("<table><tr><td>" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.YOU_CHECK") + ",</td></tr><tr><td>" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.REWARD_IF") + ",</td></tr><tr><td>" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.IF_NOT") + "</td></tr></table>");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<table><tr><td><font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.NOT_COMPLETE") + "</font> - " + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.YOU_DID") + ".</td></tr>");
		tb.append("<tr><td><font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.GET_REWARD") + "</font> - " + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.YOU_MAY") + ".</td></tr>");
		tb.append("<tr><td><font color=\"5EA82E\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.COMPLETED") + "</font> - " + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.RECEIVE_REWARD") + ".</td></tr></table>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.BACK") + "\" action=\"bypass -h npc_%objectId%_showMainWindow\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" width=95 height=21></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private String getStatusString(int achievementID, L2PcInstance player)
	{
		if (player.getCompletedAchievements().contains(Integer.valueOf(achievementID)))
		{
			return "<font color=\"5EA82E\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.COMPLETED") + "</font>";
		}
		
		if (AchievementsManager.getInstance().getAchievementList().get(Integer.valueOf(achievementID)).meetAchievementRequirements(player))
		{
			return "<font color=\"LEVEL\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.GET_REWARD") + "</font>";
		}
		
		return "<font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.NOT_COMPLETE") + "</font>";
	}
	
	private String getTableColor(int i)
	{
		if ((i % 2) == 0)
		{
			return "<table width=270 border=0 bgcolor=\"444444\">";
		}
		
		return "<table width=270 border=0>";
	}
	
	private String getConditionsStatus(int achievementID, L2PcInstance player)
	{
		int i = 0;
		String s = "</center>";
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(Integer.valueOf(achievementID));
		String completed = "<font color=\"5EA82E\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.COMPLETED") + "</font></td></tr></table>";
		String notcompleted = "<font color=\"FF0000\">" + LocalizationStorage.getInstance().getString(player.getLang(), "L2Achievements.NOT_COMPLETE") + "</font></td></tr></table>";
		
		for (Condition c : a.getConditions())
		{
			s = s + getTableColor(i);
			s = s + "<tr><td width=250 align=\"left\">" + c.getName() + "</td><td width=110 align=\"left\">" + c.getValue() + "</td><td width=210 align=\"center\">";
			i++;
			
			if (c.meetConditionRequirements(player))
			{
				s = s + completed;
			}
			else
			{
				s = s + notcompleted;
			}
		}
		return s;
	}
}