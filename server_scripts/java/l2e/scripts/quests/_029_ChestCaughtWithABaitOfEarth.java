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
public class _029_ChestCaughtWithABaitOfEarth extends Quest
{
	private static final String qn = "_029_ChestCaughtWithABaitOfEarth";

	// NPC
	private static final int ANABEL = 30909;
	private static final int WILLIE = 31574;

	// ITEMS
	private static final int BOX = 7627;
	private static final int CHEST = 6507;
	private static final int GLOVES = 2455;

	public _029_ChestCaughtWithABaitOfEarth(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(WILLIE);
		addTalkId(WILLIE);
		addTalkId(ANABEL);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31574-04.htm"))
		{
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31574-07.htm"))
		{
			if (st.getQuestItemsCount(CHEST) > 0)
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
				st.takeItems(CHEST, 1);
				st.giveItems(BOX, 1);
			}
			else
				htmltext = "31574-08.htm";
		}
		else if (event.equalsIgnoreCase("30909-02.htm"))
			if (st.getQuestItemsCount(BOX) == 1)
			{
				htmltext = "30909-02.htm";
				st.takeItems(BOX, -1);
				st.giveItems(GLOVES, 1);
				st.set("cond", "0");
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else
			{
				htmltext = "30909-03.htm";
				st.exitQuest(true);
			}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);

		int npcId = npc.getId();
		byte id = st.getState();
		if (id == State.CREATED)
		{
			st.setState(State.STARTED);
			st.set("cond", "0");
		}
		int cond = st.getInt("cond");
		id = State.CREATED;
		if (npcId == WILLIE)
		{
			if (cond == 0 && id == State.STARTED)
			{
				int PlayerLevel = player.getLevel();
				if (PlayerLevel < 48)
				{
					QuestState WilliesSpecialBait = player.getQuestState("_052_WilliesSpecialBait");
					if (WilliesSpecialBait != null)
					{
						if (WilliesSpecialBait.isCompleted())
							htmltext = "31574-01.htm";
						else
						{
							htmltext = "31574-02.htm";
							st.exitQuest(true);
						}
					}
					else
					{
						htmltext = "31574-03.htm";
						st.exitQuest(true);
					}
				}
			}
			else if (cond == 1)
			{
				htmltext = "31574-05.htm";
				if (st.getQuestItemsCount(CHEST) == 0)
					htmltext = "31574-06.htm";
			}
			else if (cond == 2)
				htmltext = "31574-09.htm";
		}
		else if (npcId == ANABEL)
			if (cond == 2)
				htmltext = "30909-01.htm";
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _029_ChestCaughtWithABaitOfEarth(29, qn, "");
	}
}