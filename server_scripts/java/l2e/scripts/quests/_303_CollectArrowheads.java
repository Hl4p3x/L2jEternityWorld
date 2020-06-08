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
 * Created by LordWinter 28.09.2012
 * Based on L2J Eternity-World
 */
public class _303_CollectArrowheads extends Quest
{
	private static final String qn = "_303_CollectArrowheads";
	
	// NPC
	private static final int MINIA = 30029;	

	// Item
	private static final int ORCISH_ARROWHEAD = 963;
		
	public _303_CollectArrowheads(int questId, String name, String descr)
	{
		super(questId, name, descr);
			
		addStartNpc(MINIA);
		addTalkId(MINIA);

		addKillId(20361);

		questItemIds = new int[]
		{
			ORCISH_ARROWHEAD
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30029-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}
			
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 10 && player.getLevel() <= 14)
					htmltext = "30029-02.htm";
				else
				{
					htmltext = "30029-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (st.getQuestItemsCount(ORCISH_ARROWHEAD) < 10)
					htmltext = "30029-04.htm";
				else
				{
					htmltext = "30029-05.htm";
					st.takeItems(ORCISH_ARROWHEAD, -1);
					st.rewardItems(57, 1000);
					st.addExpAndSp(2000, 0);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(true);
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;
		
		if (st.getInt("cond") == 1)
		{
			if (st.dropQuestItems(ORCISH_ARROWHEAD, 1, 10, 400000, true))
				st.set("cond", "2");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _303_CollectArrowheads(303, qn, "");	
	}
}