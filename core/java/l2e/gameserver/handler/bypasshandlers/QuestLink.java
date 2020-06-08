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
package l2e.gameserver.handler.bypasshandlers;

import java.util.List;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.handler.IBypassHandler;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.Quest.QuestEventType;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.util.StringUtil;

public class QuestLink implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"Quest"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!target.isNpc())
		{
			return false;
		}
		
		String quest = "";
		try
		{
			quest = command.substring(5).trim();
		}
		catch (IndexOutOfBoundsException ioobe)
		{
		}
		if (quest.length() == 0)
		{
			showQuestWindow(activeChar, (L2Npc) target);
		}
		else
		{
			showQuestWindow(activeChar, (L2Npc) target, quest);
		}
		
		return true;
	}
	
	public static void showQuestChooseWindow(L2PcInstance player, L2Npc npc, Quest[] quests)
	{
		final StringBuilder sb = StringUtil.startAppend(150, "<html><body>");
		for (Quest q : quests)
		{
			StringUtil.append(sb, "<a action=\"bypass -h npc_", String.valueOf(npc.getObjectId()), "_Quest ", q.getName(), "\">[", q.getDescr(player));
			
			QuestState qs = player.getQuestState(q.getScriptName());
			if (qs != null)
			{
				if ((qs.getState() == State.STARTED) && (qs.getInt("cond") > 0))
				{
					sb.append((new CustomMessage("quest.progress", player.getLang())).toString());
				}
				else if (qs.getState() == State.COMPLETED)
				{
					sb.append((new CustomMessage("quest.done", player.getLang())).toString());
				}
			}
			sb.append("]</a><br>");
		}
		
		sb.append("</body></html>");
		
		npc.insertObjectIdAndShowChatWindow(player, sb.toString());
	}
	
	public static void showQuestWindow(L2PcInstance player, L2Npc npc, String questId)
	{
		String content = null;
		
		Quest q = QuestManager.getInstance().getQuest(questId);
		
		QuestState qs = player.getQuestState(questId);
		
		if (q != null)
		{
			if (((q.getId() >= 1) && (q.getId() < 20000)) && ((player.getWeightPenalty() >= 3) || !player.isInventoryUnder90(true)))
			{
				player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
				return;
			}
			
			if (qs == null)
			{
				if ((q.getId() >= 1) && (q.getId() < 20000))
				{
					if (player.getAllActiveQuests().length > 40)
					{
						final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
						html.setFile(player.getLang(), "data/html/fullquest.htm");
						player.sendPacket(html);
						return;
					}
				}
				List<Quest> qlst = npc.getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
				
				if ((qlst != null) && !qlst.isEmpty())
				{
					for (Quest temp : qlst)
					{
						if (temp == q)
						{
							qs = q.newQuestState(player);
							break;
						}
					}
				}
			}
		}
		else
		{
			content = Quest.getNoQuestMsg(player);
		}
		
		if (qs != null)
		{
			if (!qs.getQuest().notifyTalk(npc, qs))
			{
				return;
			}
			
			questId = qs.getQuest().getName();
			String stateId = State.getStateName(qs.getState());
			String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
			content = HtmCache.getInstance().getHtm(player.getLang(), path);
			
			if (Config.DEBUG)
			{
				if (content != null)
				{
					_log.fine("Showing quest window for quest " + questId + " html path: " + path);
				}
				else
				{
					_log.fine("File not exists for quest " + questId + " html path: " + path);
				}
			}
		}
		
		if (content != null)
		{
			npc.insertObjectIdAndShowChatWindow(player, content);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public static void showQuestWindow(L2PcInstance player, L2Npc npc)
	{
		List<Quest> options = new FastList<>();
		
		QuestState[] awaits = player.getQuestsForTalk(npc.getTemplate().getId());
		List<Quest> starts = npc.getTemplate().getEventQuests(QuestEventType.QUEST_START);
		
		if (awaits != null)
		{
			for (QuestState x : awaits)
			{
				if (!options.contains(x.getQuest()))
				{
					if ((x.getQuest().getId() > 0) && (x.getQuest().getId() < 20000))
					{
						options.add(x.getQuest());
					}
				}
			}
		}
		
		if (starts != null)
		{
			for (Quest x : starts)
			{
				if (!options.contains(x))
				{
					if ((x.getId() > 0) && (x.getId() < 20000))
					{
						options.add(x);
					}
				}
			}
		}
		
		if (options.size() > 1)
		{
			showQuestChooseWindow(player, npc, options.toArray(new Quest[options.size()]));
		}
		else if (options.size() == 1)
		{
			showQuestWindow(player, npc, options.get(0).getName());
		}
		else
		{
			showQuestWindow(player, npc, "");
		}
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}