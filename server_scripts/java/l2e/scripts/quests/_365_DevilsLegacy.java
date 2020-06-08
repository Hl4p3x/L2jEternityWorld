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
import l2e.util.Rnd;

/**
 * Created by LordWinter 19.01.2013 Based on L2J Eternity-World
 */
public class _365_DevilsLegacy extends Quest
{
	private static final String qn = "_365_DevilsLegacy";

	private static final int RANDOLF = 30095;

	private static final int[] MOBS = 
	{
		20836, 29027, 20845, 21629, 21630, 29026
	};

	private static final int TREASURE_CHEST = 5873;

	public _365_DevilsLegacy(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(RANDOLF);
		addTalkId(RANDOLF);

		for (int mob : MOBS)
		{
			addKillId(mob);
		}

		questItemIds = new int[]
		{
			TREASURE_CHEST
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

		if(event.equalsIgnoreCase("30095-01.htm"))
		{
     			st.set("cond","1");
     			st.setState(State.STARTED);
     			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30095-05.htm"))
		{
			long count = st.getQuestItemsCount(TREASURE_CHEST);
			if(count > 0)
			{
				long reward = count * 5070;
				st.takeItems(TREASURE_CHEST, -1);
				st.giveItems(57, reward);
			}
			else
			{
				htmltext = "30095-07.htm";
			}
		}
		else if(event.equalsIgnoreCase("30095-6.htm"))
		{
			st.playSound("ItemSound.quest_finish");
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

		int cond = st.getCond();

		switch (st.getState())
		{
			case State.CREATED:
				if(player.getLevel() >= 39)
					htmltext = "30095-00.htm";
				else
				{
					htmltext = "30095-00a.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if(cond == 1)
				{
					if(st.getQuestItemsCount(TREASURE_CHEST) == 0)
					{
						htmltext = "30095-02.htm";
					}
					else
					{
						htmltext = "30095-04.htm";
					}
				}
				break;
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null || !st.isStarted())
		{
			return null;
		}

		if(Rnd.chance(25))
		{
			st.giveItems(TREASURE_CHEST, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _365_DevilsLegacy(365, qn, "");
	}
}