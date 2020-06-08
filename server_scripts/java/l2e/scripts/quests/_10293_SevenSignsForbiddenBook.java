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

public class _10293_SevenSignsForbiddenBook extends Quest
{
	private static final String qn = "_10293_SevenSignsForbiddenBook";

	// NPC
	private static final int Sophia1 = 32596;
	private static final int Elcadia = 32784;
	private static final int Elcadia_Support = 32785;
	private static final int Books = 32809;
	private static final int Books1 = 32810;
	private static final int Books2 = 32811;
	private static final int Books3 = 32812;
	private static final int Books4 = 32813;
	private static final int Sophia2 = 32861;
	private static final int Sophia3 = 32863;

	// Item
	private static final int SolinasBiography = 17213;
	
	public _10293_SevenSignsForbiddenBook(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Elcadia);
		addTalkId(Elcadia);
		addTalkId(Sophia1);
		addTalkId(Elcadia_Support);
		addTalkId(Books);
		addTalkId(Books1);
		addTalkId(Books2);
		addTalkId(Books3);
		addTalkId(Books4);
		addTalkId(Sophia2);
		addTalkId(Sophia3);
		addStartNpc(Sophia3);
		addFirstTalkId(Sophia3);
		
		questItemIds = new int[] { SolinasBiography };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (npc.getId() == Elcadia)
		{
			if (event.equalsIgnoreCase("32784-04.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("32784-09.htm"))
			{
				if (player.isSubClassActive())
				{
					htmltext = "32784-10.htm";
				}
				else
				{
					st.playSound("ItemSound.quest_finish");
					st.addExpAndSp(15000000, 1500000);
					st.exitQuest(false);
					htmltext = "32784-09.htm";
				}
			}
		}
		else if (npc.getId() == Sophia2)
		{
			if (event.equalsIgnoreCase("32861-04.htm"))
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
			if (event.equalsIgnoreCase("32861-08.htm"))
			{
				st.set("cond", "4");
				st.playSound("ItemSound.quest_middle");
			}
			if (event.equalsIgnoreCase("32861-11.htm"))
			{
				st.set("cond", "6");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npc.getId() == Elcadia_Support)
		{
			if (event.equalsIgnoreCase("32785-07.htm"))
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npc.getId() == Books)
		{
			if (event.equalsIgnoreCase("32809-02.htm"))
			{
				st.set("cond", "7");
				st.giveItems(SolinasBiography, 1);
				st.playSound("ItemSound.quest_middle");
			}
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
		else if (npc.getId() == Elcadia)
		{
			if (st.getState() == State.COMPLETED)
				htmltext = "32784-02.htm";
			else if (player.getLevel() < 81)
				htmltext = "32784-11.htm";
			else if (player.getQuestState("_10292_SevenSignsGirlofDoubt") == null || player.getQuestState("_10292_SevenSignsGirlofDoubt").getState() != State.COMPLETED)
				htmltext = "32784-11.htm";
			else if (st.getState() == State.CREATED)
				htmltext = "32784-01.htm";
			else if (st.getInt("cond") == 1)
				htmltext = "32784-06.htm";
			else if (st.getInt("cond") >= 8)
				htmltext = "32784-07.htm";
		}
		else if (npc.getId() == Elcadia_Support)
		{
			switch (st.getInt("cond"))
			{
				case 1:
					htmltext = "32785-01.htm";
					break;
				case 2:
					htmltext = "32785-04.htm";
					st.set("cond", "3");
					st.playSound("ItemSound.quest_middle");
					break;
				case 3:
					htmltext = "32785-05.htm";
					break;
				case 4:
					htmltext = "32785-06.htm";
					break;
				case 5:
					htmltext = "32785-08.htm";
					break;
				case 6:
					htmltext = "32785-09.htm";
					break;
				case 7:
					htmltext = "32785-11.htm";
					st.set("cond", "8");
					st.playSound("ItemSound.quest_middle");
					break;
				case 8:
					htmltext = "32785-12.htm";
					break;
			}
		}
		else if (npc.getId() == Sophia1)
		{
			switch (st.getInt("cond"))
			{
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					htmltext = "32596-01.htm";
					break;
				case 8:
					htmltext = "32596-05.htm";
					break;
			}
		}
		else if (npc.getId() == Sophia2)
		{
			switch (st.getInt("cond"))
			{
				case 1:
					htmltext = "32861-01.htm";
					break;
				case 2:
					htmltext = "32861-05.htm";
					break;
				case 3:
					htmltext = "32861-06.htm";
					break;
				case 4:
					htmltext = "32861-09.htm";
					break;
				case 5:
					htmltext = "32861-10.htm";
					break;
				case 6:
				case 7:
					htmltext = "32861-12.htm";
					break;
				case 8:
					htmltext = "32861-14.htm";
					break;
			}
		}
		else if (npc.getId() == Books)
		{
			if (st.getInt("cond") == 6)
				htmltext = "32809-01.htm";
		}
		else if (npc.getId() == Books1)
		{
			if (st.getInt("cond") == 6)
				htmltext = "32810-01.htm";
		}
		else if (npc.getId() == Books2)
		{
			if (st.getInt("cond") == 6)
				htmltext = "32811-01.htm";
		}
		else if (npc.getId() == Books3)
		{
			if (st.getInt("cond") == 6)
				htmltext = "32812-01.htm";
		}
		else if (npc.getId() == Books4)
		{
			if (st.getInt("cond") == 6)
				htmltext = "32813-01.htm";
		}
		return htmltext;
	}
	
	@Override
	public final String onFirstTalk (L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (npc.getId() == Sophia3)
		{
			switch (st.getInt("cond"))
			{
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					htmltext = "32863-01.htm";
					break;
				case 8:
					htmltext = "32863-04.htm";
					break;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new _10293_SevenSignsForbiddenBook(10293, qn, "");
	}
}