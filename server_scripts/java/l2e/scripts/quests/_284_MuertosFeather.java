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
 * Created by LordWinter 18.01.2013 Based on L2J Eternity-World
 */
public class _284_MuertosFeather extends Quest
{
	private static final String qn = "_284_MuertosFeather";

	private static final int TREVOR = 32166;

	private static final int[] MOBS = 
	{
		22239, 22240, 22242, 22243, 22245, 22246
	};

	private static final int FEATHER = 9748;

	public _284_MuertosFeather(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(TREVOR);
		addTalkId(TREVOR);

		for (int mob : MOBS)
		{
			addKillId(mob);
		}

		questItemIds = new int[]
		{
			FEATHER
		};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("32166-03.htm"))
		{
       			st.set("cond", "1");
       			st.setState(State.STARTED);
       			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("32166-06.htm"))
		{
			long counts = st.getQuestItemsCount(FEATHER) * 45;
			st.takeItems(FEATHER, -1);
			st.giveItems(57, counts);
		}
		else if(event.equalsIgnoreCase("32166-08.htm"))
		{
       			st.takeItems(FEATHER, -1);
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
				if(player.getLevel() < 11)
				{
					htmltext = "32166-02.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "32166-01.htm";
				}
				break;
			case State.STARTED:
				if(st.getQuestItemsCount(FEATHER) == 0)
				{
					htmltext = "32166-04.htm";
				}
				else
				{
					htmltext = "32166-05.htm";
				}
				break;
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}

       		int chance = st.getRandom(100);

		if (st.getInt("cond") == 1)
		{
       			if (chance < 70)
			{
         			st.giveItems(FEATHER, 1);
         			st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _284_MuertosFeather(284, qn, "");
	}
}