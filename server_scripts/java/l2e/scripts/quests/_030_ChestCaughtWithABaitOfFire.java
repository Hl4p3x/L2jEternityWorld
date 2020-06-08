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
public class _030_ChestCaughtWithABaitOfFire extends Quest
{
	private static final String qn = "_030_ChestCaughtWithABaitOfFire";

	// NPC
	private static final int LINNAEUS = 31577;
	private static final int RUKAL = 30629;

	// ITEMS
	private static final int NECKLACE = 916;
	private static final int SCORE = 7628;
	private static final int CHEST = 6511;

	public _030_ChestCaughtWithABaitOfFire(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(LINNAEUS);
		addTalkId(RUKAL);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31577-04.htm"))
		{
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31577-07.htm"))
		{
			if (st.getQuestItemsCount(CHEST) > 0)
			{
				st.takeItems(CHEST, 1);
				st.giveItems(SCORE, 1);
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
			else
				htmltext = "31577-08.htm";
		}
		else if (event.equalsIgnoreCase("30629-02.htm"))
			if (st.getQuestItemsCount(SCORE) == 1)
			{
				st.takeItems(SCORE, -1);
				st.giveItems(NECKLACE, 1);
				st.set("cond", "0");
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else
			{
				htmltext = "30629-03.htm";
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
		id = st.getState();
		int cond = st.getInt("cond");
		if (npcId == LINNAEUS)
		{
			if (cond == 0 && id == State.STARTED)
			{
				int PLevel = player.getLevel();
				if (PLevel < 60)
				{
					QuestState LinnaeusSpecialBait = player.getQuestState("_053_LinnaeusSpecialBait");
					if (LinnaeusSpecialBait != null)
					{
						if (LinnaeusSpecialBait.isCompleted())
							htmltext = "31577-01.htm";
						else
						{
							htmltext = "31577-02.htm";
							st.exitQuest(true);
						}
					}
					else
					{
						htmltext = "31577-03.htm";
						st.exitQuest(true);
					}
				}
				else
					htmltext = "31577-01.htm";
			}
			else if (cond == 1)
			{
				htmltext = "31577-05.htm";
				if (st.getQuestItemsCount(CHEST) == 0)
					htmltext = "31577-06.htm";
			}
			else if (cond == 2)
				htmltext = "31577-09.htm";
		}
		else if (npcId == RUKAL)
			if (cond == 2)
				htmltext = "30629-01.htm";
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _030_ChestCaughtWithABaitOfFire(30, qn, "");
	}
}