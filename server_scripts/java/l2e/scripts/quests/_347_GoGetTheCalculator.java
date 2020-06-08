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
public class _347_GoGetTheCalculator extends Quest
{
	private static final String qn = "_347_GoGetTheCalculator";
	
	// NPCs
	private static final int BRUNON = 30526;
	private static final int SILVERA = 30527;
	private static final int SPIRON = 30532;
	private static final int BALANKI = 30533;
	
	// Items
	private static final int GEMSTONE_BEAST_CRYSTAL = 4286;
	private static final int CALCULATOR_Q = 4285;
	private static final int CALCULATOR_REAL = 4393;
	
	public _347_GoGetTheCalculator(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(BRUNON);
		addTalkId(BRUNON, SILVERA, SPIRON, BALANKI);

		addKillId(20540);

		questItemIds = new int[]
		{
			4286
		};
	}	
			
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30526-05.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30533-03.htm"))
		{
			if (st.getQuestItemsCount(57) >= 100)
			{
				htmltext = "30533-02.htm";
				st.takeItems(57, 100);
				
				if (st.getInt("cond") == 3)
					st.set("cond", "4");
				else
					st.set("cond", "2");
				
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("30532-02.htm"))
		{
			if (st.getInt("cond") == 2)
				st.set("cond", "4");
			else
				st.set("cond", "3");
			
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30526-08.htm"))
		{
			st.takeItems(CALCULATOR_Q, -1);
			st.giveItems(CALCULATOR_REAL, 1);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30526-09.htm"))
		{
			st.takeItems(CALCULATOR_Q, -1);
			st.rewardItems(57, 1000);
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

		int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 12)
					htmltext = "30526-01.htm";
				else
					htmltext = "30526-00.htm";
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case BRUNON:
						if (st.getQuestItemsCount(CALCULATOR_Q) == 0)
							htmltext = "30526-06.htm";
						else
							htmltext = "30526-07.htm";
						break;
					case SPIRON:
						if (cond >= 1 && cond <= 3)
							htmltext = "30532-01.htm";
						else if (cond >= 4)
							htmltext = "30532-05.htm";
						break;
					case BALANKI:
						if (cond >= 1 && cond <= 3)
							htmltext = "30533-01.htm";
						else if (cond >= 4)
							htmltext = "30533-04.htm";
						break;
					case SILVERA:
						if (cond < 4)
							htmltext = "30527-00.htm";
						else if (cond == 4)
						{
							htmltext = "30527-01.htm";
							st.set("cond", "5");
							st.playSound("ItemSound.quest_middle");
						}
						else if (cond == 5)
						{
							if (st.getQuestItemsCount(GEMSTONE_BEAST_CRYSTAL) >= 10)
							{
								htmltext = "30527-03.htm";
								st.set("cond", "6");
								st.takeItems(GEMSTONE_BEAST_CRYSTAL, -1);
								st.giveItems(CALCULATOR_Q, 1);
								st.playSound("ItemSound.quest_middle");
							}
							else
								htmltext = "30527-02.htm";
						}
						else if (cond == 6)
							htmltext = "30527-04.htm";
						break;
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
		
		if (st.getInt("cond") == 5)
			st.dropQuestItems(GEMSTONE_BEAST_CRYSTAL, 1, 10, 500000, true);
		
		return null;
	}

	public static void main(String[] args)
	{
		new _347_GoGetTheCalculator(347, qn, "");	
	}
}