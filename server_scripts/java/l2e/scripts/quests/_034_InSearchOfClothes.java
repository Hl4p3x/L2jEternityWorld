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
package l2e.scripts.quests;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 24.05.2011
 * Based on L2J Eternity-World
 */
public class _034_InSearchOfClothes extends Quest
{
	private static final String qn = "_034_InSearchOfClothes";

	// ITEMS
	private static final int MYSTERIOUS_CLOTH 	= 7076;
	private static final int SPIDERSILK 		= 1493;
	private static final int SPINNERET 		= 7528;
	private static final int SUEDE 			= 1866;
	private static final int THREAD 		= 1868;

	public _034_InSearchOfClothes(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(30088);
		addTalkId(30088);
		addTalkId(30165);
		addTalkId(30294);
		addKillId(20560);
		questItemIds = new int[] { SPINNERET, SPIDERSILK };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		int cond = st.getInt("cond");
		if (event.equalsIgnoreCase("30088-1.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30294-1.htm") && cond == 1)
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30088-3.htm") && cond == 2)
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30165-1.htm") && cond == 3)
		{
			st.set("cond", "4");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30165-3.htm") && cond == 5)
		{
			if (st.getQuestItemsCount(SPINNERET) == 10)
			{
				st.takeItems(SPINNERET, 10);
				st.giveItems(SPIDERSILK, 1);
				st.set("cond", "6");
				st.playSound("ItemSound.quest_accept");
			}
			else
				htmltext = "30165-1a.htm";
		}
		else if (event.equalsIgnoreCase("30088-5.htm") && cond == 6)
			if (st.getQuestItemsCount(SUEDE) >= 3000 && st.getQuestItemsCount(THREAD) >= 5000 && st.getQuestItemsCount(SPIDERSILK) == 1)
			{
				st.takeItems(SUEDE, 3000);
				st.takeItems(THREAD, 5000);
				st.takeItems(SPIDERSILK, 1);
				st.giveItems(MYSTERIOUS_CLOTH, 1);
				st.playSound("ItemSound.quest_finish");
				st.unset("cond");
				st.exitQuest(true);
			}
			else
				htmltext = "30088-4a.htm";
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);

		int npcId = npc.getId();
		int cond = st.getInt("cond");

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		if (npcId == 30088)
		{
			if (cond == 0 && st.getQuestItemsCount(MYSTERIOUS_CLOTH) == 0)
			{
				if (player.getLevel() >= 60)
				{
					QuestState fwear = player.getQuestState("_037_PleaseMakeMeFormalWear");
					if (fwear != null && fwear.getInt("cond") == 6)
						htmltext = "30088-0.htm";
					else
					{
						htmltext = "30088-6.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "30088-6.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
				htmltext = "30088-1a.htm";
			else if (cond == 2)
				htmltext = "30088-2.htm";
			else if (cond == 3)
				htmltext = "30088-3a.htm";
			else if (cond == 4)
				htmltext = "30088-3a.htm";
			else if (cond == 5)
				htmltext = "30088-3a.htm";
			else if (cond == 6)
				htmltext = "30088-4.htm";
		}
		else if (npcId == 30294)
		{
			if (cond == 1)
				htmltext = "30294-0.htm";
			else if (cond == 2)
				htmltext = "30294-1a.htm";
		}
		else if (npcId == 30165)
			if (cond == 3)
				htmltext = "30165-0.htm";
			else if (cond == 4 && st.getQuestItemsCount(SPINNERET) < 10)
				htmltext = "30165-1a.htm";
			else if (cond == 5)
				htmltext = "30165-2.htm";
			else if (cond == 6 && (st.getQuestItemsCount(SUEDE) < 3000 || st.getQuestItemsCount(THREAD) < 5000 || st.getQuestItemsCount(SPIDERSILK) < 1))
				htmltext = "30165-3a.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		if (st.getQuestItemsCount(SPINNERET) < 10)
		{
			st.giveItems(SPINNERET, 1);
			if (st.getQuestItemsCount(SPINNERET) == 10)
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "5");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _034_InSearchOfClothes(34, qn, "");
	}
}