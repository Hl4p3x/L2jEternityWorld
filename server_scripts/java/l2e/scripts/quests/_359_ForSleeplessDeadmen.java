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
 * Created by LordWinter 13.01.2013 Based on L2J Eternity-World
 */
public class _359_ForSleeplessDeadmen extends Quest
{
	private static final String qn = "_359_ForSleeplessDeadmen";
	
	private static final int ORVEN = 30857;
	
	private static final int REMAINS = 5869;
	
	private static final int REWARD[] =
	{
		6341,
		6342,
		6343,
		6344,
		6345,
		6346,
		5494,
		5495
	};
	
	public _359_ForSleeplessDeadmen(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(ORVEN);
		addTalkId(ORVEN);
		
		addKillId(21006, 21007, 21008);

		questItemIds = new int[]
		{
			REMAINS
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30857-06.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30857-10.htm"))
		{
			st.giveItems(REWARD[getRandom(REWARD.length)], 4);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 60)
					htmltext = "30857-02.htm";
				else
				{
					htmltext = "30857-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "30857-07.htm";
				else if (cond == 2)
				{
					htmltext = "30857-08.htm";
					st.set("cond", "3");
					st.playSound("ItemSound.quest_middle");
					st.takeItems(REMAINS, -1);
				}
				else if (cond == 3)
					htmltext = "30857-09.htm";
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (st.dropItems(REMAINS, 1, 60, 100000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new _359_ForSleeplessDeadmen(359, qn, "");
	}
}