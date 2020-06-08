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
 * Created by LordWinter 06.07.2012
 * Based on L2J Eternity-World
 */
public class _156_MillenniumLove extends Quest
{
	private final static String qn = "_156_MillenniumLove";
	
	// NPCs
	private static final int LILITH = 30368;
	private static final int BAENEDES = 30369;

	// Items
	private static final int RYLITHS_LETTER = 1022;
	private static final int THEONS_DIARY = 1023;
		
	public _156_MillenniumLove(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(LILITH);
		addTalkId(LILITH);
		addTalkId(BAENEDES);

		questItemIds = new int[] { RYLITHS_LETTER, THEONS_DIARY };
	}	
		
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30368-04.htm"))
		{
			st.set("cond", "1");
			st.giveItems(RYLITHS_LETTER, 1);
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30369-02.htm"))
		{
			st.set("cond", "2");
			st.takeItems(RYLITHS_LETTER, -1);
			st.giveItems(THEONS_DIARY, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30369-03.htm"))
		{
			st.takeItems(RYLITHS_LETTER, -1);
			st.addExpAndSp(3000, 0);
			st.playSound("ItemSound.quest_finish");
			st.unset("cond");
			st.exitQuest(false);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if(st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 15 && player.getLevel() <= 19)
					htmltext = "30368-01.htm";
				else
				{
					htmltext = "30368-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case LILITH:
						if (st.getQuestItemsCount(RYLITHS_LETTER) == 1)
							htmltext = "30368-05.htm";
						else if (st.getQuestItemsCount(THEONS_DIARY) == 1)
						{
							htmltext = "30368-06.htm";
							st.takeItems(THEONS_DIARY, -1);
							st.giveItems(5250, 1);
							st.addExpAndSp(3000, 0);
							st.playSound("ItemSound.quest_finish");
							st.unset("cond");
							st.exitQuest(false);
						}
						break;
					
					case BAENEDES:
						if (st.getQuestItemsCount(RYLITHS_LETTER) == 1)
							htmltext = "30369-01.htm";
						else if (st.getQuestItemsCount(THEONS_DIARY) == 1)
							htmltext = "30369-04.htm";
						break;
				}
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _156_MillenniumLove(156, qn, "");		
	}
}