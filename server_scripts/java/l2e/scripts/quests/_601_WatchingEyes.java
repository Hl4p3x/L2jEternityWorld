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
 * Created by LordWinter 03.08.2011
 * Based on L2J Eternity-World
 */
public class _601_WatchingEyes extends Quest
{
	private static final String qn 		= "_601_WatchingEyes";

	//NPC
	private static int EYE_OF_ARGOS 	= 31683;

	//ITEMS
	private static int PROOF_OF_AVENGER 	= 7188;

	//CHANCE
	private static int DROP_CHANCE 		= 50;

	//MOBS
	private static int[] MOBS 		= { 21306, 21308, 21309, 21310, 21311 };
	private static int[][] REWARDS 		= { { 6699, 90000, 0, 19 }, { 6698, 80000, 20, 39 }, { 6700, 40000, 40, 49 }, { 0, 230000, 50, 100 } };

	public _601_WatchingEyes(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(EYE_OF_ARGOS);
        	addTalkId(EYE_OF_ARGOS);

		for(int MOB : MOBS)
			addKillId(MOB);

		questItemIds = new int[] {PROOF_OF_AVENGER};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("31683-1.htm"))
		{
			if(player.getLevel() < 71)
			{
				htmltext = "31683-0a.htm";
				st.exitQuest(true);
			}
			else
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if(event.equalsIgnoreCase("31683-4.htm"))
		{
			int random = getRandom(101);
			int i = 0;
			int item = 0;
			int adena = 0;
			while(i < REWARDS.length)
			{
				item = REWARDS[i][0];
				adena = REWARDS[i][1];
				if(REWARDS[i][2] <= random && random <= REWARDS[i][3])
					break;
				i++;
			}

			st.giveItems(57,adena);
			if(item != 0)
			{
				st.giveItems(item,5);
				st.addExpAndSp(120000,10000);
			}
			st.takeItems(PROOF_OF_AVENGER, -1);
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

		int cond = st.getInt("cond");

		if(cond == 0)
			htmltext = "31683-0.htm";
		else if(cond == 1)
			htmltext = "31683-2.htm";
		else if(cond == 2 && st.getQuestItemsCount(PROOF_OF_AVENGER) == 100)
			htmltext = "31683-3.htm";
		else
		{
			htmltext = "31683-4a.htm";
			st.set("cond", "1");
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		if(st.getInt("cond") == 1)
		{
			long count = st.getQuestItemsCount(PROOF_OF_AVENGER);
			if(count < 100 && getRandom(100) < DROP_CHANCE)
			{
				st.giveItems(PROOF_OF_AVENGER, 1);
				if(count == 99)
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
				else
					st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _601_WatchingEyes(601, qn, "");
	}
}