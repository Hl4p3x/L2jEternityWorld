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
 * Created by LordWinter 30.09.2012
 * Based on L2J Eternity-World
 */
public class _313_CollectSpores extends Quest
{
	private static final String qn = "_313_CollectSpores";
	
	// NPC
	private static final int Herbiel = 30150;
	// Item
	private static final int SporeSac = 1118;
	
	public _313_CollectSpores(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(Herbiel);
		addTalkId(Herbiel);
	
		addKillId(20509);

		questItemIds = new int[] { SporeSac };
	}	
			
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30150-05.htm"))
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

		int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 8 && player.getLevel() <= 13)
					htmltext = "30150-03.htm";
				else
				{
					htmltext = "30150-02.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (cond == 1)
					htmltext = "30150-06.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(SporeSac) < 10)
					{
						st.set("cond", "1");
						htmltext = "30150-06.htm";
					}
					else
					{
						htmltext = "30150-07.htm";
						st.takeItems(SporeSac, -1);
						st.rewardItems(57, 3500);
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(true);
					}
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
		
		if (st.getInt("cond") == 1 && st.dropQuestItems(SporeSac, 1, 10, 700000, true))
			st.set("cond", "2");
		
		return null;
	}

	public static void main(String[] args)
	{
		new _313_CollectSpores(313, qn, "");		
	}
}