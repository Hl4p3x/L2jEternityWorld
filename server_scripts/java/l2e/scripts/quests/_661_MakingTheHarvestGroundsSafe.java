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
 * Created by LordWinter 18.06.2012 Based on L2J Eternity-World
 */
public class _661_MakingTheHarvestGroundsSafe extends Quest
{
	private static final String qn = "_661_MakingTheHarvestGroundsSafe";
	
	public _661_MakingTheHarvestGroundsSafe(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(30210);
		addTalkId(30210);
		
		addKillId(new int[]
		{
			21095,
			21096,
			21097
		});
		
		questItemIds = new int[]
		{
			8283,
			8284,
			8285
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("30210-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30210-04.htm"))
		{
			int item1 = (int) st.getQuestItemsCount(8283);
			int item2 = (int) st.getQuestItemsCount(8284);
			int item3 = (int) st.getQuestItemsCount(8285);
			int sum = 0;
			
			sum = (item1 * 57) + (item2 * 56) + (item3 * 60);
			
			if ((item1 + item2 + item3) >= 10)
			{
				sum += 2871;
			}
			st.takeItems(8283, item1);
			st.takeItems(8284, item2);
			st.takeItems(8285, item3);
			st.rewardItems(57, sum);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30210-06.htm"))
		{
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 21)
				{
					htmltext = "30210-01.htm";
				}
				else
				{
					htmltext = "30210-01a.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if ((st.getQuestItemsCount(8283) >= 1L) || (st.getQuestItemsCount(8284) >= 1L) || (st.getQuestItemsCount(8285) >= 1L))
				{
					htmltext = "30210-03.htm";
				}
				else
				{
					htmltext = "30210-05.htm";
				}
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		if ((st.isStarted()) && (st.getRandom(10) < 5))
		{
			switch (npc.getId())
			{
				case 21095:
					st.giveItems(8283, 1L);
					break;
				case 21096:
					st.giveItems(8284, 1L);
					break;
				case 21097:
					st.giveItems(8285, 1L);
					break;
			}
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _661_MakingTheHarvestGroundsSafe(661, qn, "");
	}
}