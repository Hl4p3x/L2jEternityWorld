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
 * Created by LordWinter 21.03.2011
 * Based on L2J Eternity-World
 */
public class _017_LightAndDarkness extends Quest
{
	private static final String qn = "_017_LightAndDarkness";

	//NPC
	private static final int HIERARCH = 31517;
	private static final int SAINT_ALTAR_1 = 31508;
	private static final int SAINT_ALTAR_2 = 31509;
	private static final int SAINT_ALTAR_3 = 31510;
	private static final int SAINT_ALTAR_4 = 31511;
	//ITEMS
	private static final int BLOOD_OF_SAINT = 7168;

	public _017_LightAndDarkness(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(HIERARCH);
		addTalkId(HIERARCH);
		addTalkId(SAINT_ALTAR_1);
		addTalkId(SAINT_ALTAR_2);
		addTalkId(SAINT_ALTAR_3);
		addTalkId(SAINT_ALTAR_4);
		
		questItemIds = new int[] {BLOOD_OF_SAINT};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31517-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.giveItems(BLOOD_OF_SAINT, 4);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31508-02.htm"))
		{
			st.takeItems(BLOOD_OF_SAINT, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31509-02.htm"))
		{
			st.takeItems(BLOOD_OF_SAINT, 1);
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31510-02.htm"))
		{
			st.takeItems(BLOOD_OF_SAINT, 1);
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31511-02.htm"))
		{
			st.takeItems(BLOOD_OF_SAINT, 1);
			st.set("cond", "5");
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		final int cond = st.getInt("cond");		
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				QuestState st2 = player.getQuestState("_015_SweetWhisper");
				if (st2 != null && st2.getState() == State.COMPLETED)
				{
					if(player.getLevel() >= 61)					
						htmltext = "31517-00.htm";					
					else
					{
						htmltext = "31517-02a.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "31517-02b.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case HIERARCH:
						if (cond > 0 && cond < 5)
						{
							if (st.getQuestItemsCount(BLOOD_OF_SAINT) > 0)
								htmltext = "31517-04.htm";
							else
							{
								htmltext = "31517-05.htm";
								st.exitQuest(true);
								st.playSound("ItemSound.quest_giveup");
							}
						}
						else if (cond == 5 && st.getQuestItemsCount(BLOOD_OF_SAINT) == 0)
						{
							htmltext = "31517-03.htm";
							st.addExpAndSp(697040,54887);
							st.unset("cond");
							st.setState(State.COMPLETED);
							st.playSound("ItemSound.quest_finish");
							st.exitQuest(false);
						}
						break;
					case SAINT_ALTAR_1:
						switch (cond)
						{
							case 1:
								if (st.getQuestItemsCount(BLOOD_OF_SAINT) != 0)
									htmltext = "31508-00.htm";
								else
									htmltext = "31508-02.htm";
								break;
							case 2:
								htmltext = "31508-03.htm";
								break;
						}
						break;
					case SAINT_ALTAR_2:
						switch (cond)
						{
							case 2:
								if (st.getQuestItemsCount(BLOOD_OF_SAINT) != 0)
									htmltext = "31509-00.htm";
								else
									htmltext = "31509-02.htm";
								break;
							case 3:
								htmltext = "31509-03.htm";
								break;
						}
						break;
					case SAINT_ALTAR_3:
						switch (cond)
						{
							case 3:
								if (st.getQuestItemsCount(BLOOD_OF_SAINT) != 0)
									htmltext = "31510-00.htm";
								else
									htmltext = "31510-02.htm";
								break;
							case 4:
								htmltext = "31510-03.htm";
								break;
						}
						break;
					case SAINT_ALTAR_4:
						switch (cond)
						{
							case 4:
								if (st.getQuestItemsCount(BLOOD_OF_SAINT) != 0)
									htmltext = "31511-00.htm";
								else
									htmltext = "31511-02.htm";
								break;
							case 5:
								htmltext = "31511-03.htm";
								break;
						}
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _017_LightAndDarkness(17, qn, "");    	
	}
}