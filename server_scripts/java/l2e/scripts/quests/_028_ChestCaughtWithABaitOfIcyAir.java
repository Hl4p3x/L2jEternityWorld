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
public class _028_ChestCaughtWithABaitOfIcyAir extends Quest
{
	private static final String qn = "_028_ChestCaughtWithABaitOfIcyAir";

	// NPC
	private static final int KIKI = 31442;
	private static final int OFULLE = 31572;

	// ITEMS
	private static final int LETTER = 7626;
	private static final int CHEST = 6503;
	private static final int RING = 881;

	public _028_ChestCaughtWithABaitOfIcyAir(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(OFULLE);
		addTalkId(OFULLE);
		addTalkId(KIKI);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31572-04.htm"))
		{
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31572-07.htm"))
		{
			if (st.getQuestItemsCount(CHEST) > 0)
			{
				st.set("cond", "2");
				st.takeItems(CHEST, 1);
				st.giveItems(LETTER, 1);
				st.playSound("ItemSound.quest_middle");
			}
			else
				htmltext = "31572-08.htm";
		}
		else if (event.equalsIgnoreCase("31442-02.htm"))
			if (st.getQuestItemsCount(LETTER) == 1)
			{
				htmltext = "31442-02.htm";
				st.takeItems(LETTER, -1);
				st.giveItems(RING, 1);
				st.set("cond", "0");
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else
			{
				htmltext = "31442-03.htm";
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
		if (npcId == OFULLE)
		{
			if (cond == 0 && id == State.STARTED)
			{
				int PlayerLevel = player.getLevel();
				if (PlayerLevel < 36)
				{
					QuestState OFullesSpecialBait = player.getQuestState("_051_OFullesSpecialBait");
					if (OFullesSpecialBait != null)
					{
						if (OFullesSpecialBait.isCompleted())
							htmltext = "31572-01.htm";
						else
						{
							htmltext = "31572-02.htm";
							st.exitQuest(true);
						}
					}
					else
					{
						htmltext = "31572-02.htm";
						st.exitQuest(true);
					}
				}
				else
					htmltext = "31572-01.htm";
			}
			else if (cond == 1)
			{
				htmltext = "31572-05.htm";
				if (st.getQuestItemsCount(CHEST) == 0)
					htmltext = "31572-06.htm";
			}
			else if (cond == 2)
				htmltext = "31572-09.htm";
		}
		else if (npcId == KIKI)
			if (cond == 2)
				htmltext = "31442-01.htm";
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _028_ChestCaughtWithABaitOfIcyAir(28, qn, "");
	}
}