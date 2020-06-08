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
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public class _328_SenseForBusiness extends Quest
{
	private static final String qn = "_328_SenseForBusiness";
	
	// NPC
	private static final int SARIEN = 30436;
	
	// Items
	private static final int MONSTER_EYE_LENS = 1366;
	private static final int MONSTER_EYE_CARCASS = 1347;
	private static final int BASILISK_GIZZARD = 1348;
	
	public _328_SenseForBusiness(int questId, String name, String descr)
	{
		super(questId, name, descr);
	
		addStartNpc(SARIEN);
		addTalkId(SARIEN);

		addKillId(20055, 20059, 20067, 20068, 20070, 20072);

		questItemIds = new int[] { MONSTER_EYE_LENS, MONSTER_EYE_CARCASS, BASILISK_GIZZARD };
	}	
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30436-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if ("30436-06.htm".equals(event))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
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
				if (player.getLevel() >= 21 && player.getLevel() <= 32)
					htmltext = "30436-02.htm";
				else
				{
					htmltext = "30436-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				long carcasses = st.getQuestItemsCount(MONSTER_EYE_CARCASS);
				long lenses = st.getQuestItemsCount(MONSTER_EYE_LENS);
				long gizzards = st.getQuestItemsCount(BASILISK_GIZZARD);
				long all = carcasses + lenses + gizzards;
				
				if (all == 0)
					htmltext = "30436-04.htm";
				else
				{
					htmltext = "30436-05.htm";
					long reward = (25 * carcasses) + (1000 * lenses) + (60 * gizzards) + (all >= 10 ? 618 : 0);
					st.takeItems(MONSTER_EYE_CARCASS, -1);
					st.takeItems(MONSTER_EYE_LENS, -1);
					st.takeItems(BASILISK_GIZZARD, -1);
					st.rewardItems(57, reward);
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
		
		if (st.isStarted())
		{
			switch (npc.getId())
			{
				case 20055:
				case 20059:
				case 20067:
				case 20068:
					if (st.getRandom(100) < 2)
					{
						st.giveItems(MONSTER_EYE_LENS, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					else if (st.getRandom(100) < 33)
					{
						st.giveItems(MONSTER_EYE_CARCASS, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					break;
				case 20070:
				case 20072:
					if (st.getRandom(100) < 18)
					{
						st.giveItems(BASILISK_GIZZARD, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					break;
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _328_SenseForBusiness(328, qn, "");		
	}
}