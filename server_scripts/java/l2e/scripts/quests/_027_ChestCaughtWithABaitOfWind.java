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
public class _027_ChestCaughtWithABaitOfWind extends Quest
{
	private static final String qn = "_027_ChestCaughtWithABaitOfWind";

	// NPCs
	private static final int LANOSCO = 31570;
	private static final int SHALING = 31434;
	
	// Quest Items
	private final static int STRANGE_GOLEM_BLUEPRINT = 7625;
	
	// Items
	private static final int BIG_BLUE_TREASURE_CHEST = 6500;
	private static final int BLACK_PEARL_RING = 880;
	
	public _027_ChestCaughtWithABaitOfWind(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(LANOSCO);
		addTalkId(LANOSCO);
		addTalkId(SHALING);
		
		questItemIds = new int[] {STRANGE_GOLEM_BLUEPRINT};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31570-04.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31570-07.htm"))
		{
			if (st.getQuestItemsCount(BIG_BLUE_TREASURE_CHEST) > 0)
			{
				st.takeItems(BIG_BLUE_TREASURE_CHEST, 1);
				st.giveItems(STRANGE_GOLEM_BLUEPRINT, 1);
				st.set("cond", "2");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_middle");
			}
			else
				htmltext = "31570-08.htm";
		}
		else if (event.equalsIgnoreCase("31434-02.htm"))
		{
			if (st.getQuestItemsCount(STRANGE_GOLEM_BLUEPRINT) == 1)
			{
				st.takeItems(STRANGE_GOLEM_BLUEPRINT, -1);
				st.giveItems(BLACK_PEARL_RING, 1);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else
			{
				htmltext = "31434-03.htm";
				st.exitQuest(true);
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
			return htmltext;
		
		final int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.COMPLETED :
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED :
				final QuestState qs = player.getQuestState("50_LanoscosSpecialBait");
				if (qs != null && qs.isCompleted() && player.getLevel() >= 27)
					htmltext = "31570-01.htm";
				else
				{
					htmltext = "31570-02.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED :
				switch (npc.getId())
				{
					case LANOSCO:
						switch (cond)
						{
							case 1:								
								if (st.getQuestItemsCount(BIG_BLUE_TREASURE_CHEST) == 0)
									htmltext = "31570-06.htm";
								else
									htmltext = "31570-05.htm";
								break;
							case 2:
								htmltext = "31570-09.htm";
								break;
						}		
						break;
					case SHALING:
						switch (cond)
						{
							case 2:
								htmltext = "31434-01.htm";
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
		new _027_ChestCaughtWithABaitOfWind(27, qn, "");    	
	}
}
