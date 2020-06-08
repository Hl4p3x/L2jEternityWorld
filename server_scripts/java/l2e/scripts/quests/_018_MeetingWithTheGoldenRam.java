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
public class _018_MeetingWithTheGoldenRam extends Quest
{
	private static final String qn = "_018_MeetingWithTheGoldenRam";

	// NPC
	private static final int DONAL = 31314;
	private static final int DAISY = 31315;
	private static final int ABERCROMBIE = 31555;
	// ITEM
	private static final int SUPPLY_BOX = 7245;

	public _018_MeetingWithTheGoldenRam(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(DONAL);
		addTalkId(DONAL);
		addTalkId(DAISY);
		addTalkId(ABERCROMBIE);
		
		questItemIds = new int[] {SUPPLY_BOX};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31314-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31315-02.htm"))
		{
			st.set("cond", "2");
			st.giveItems(SUPPLY_BOX, 1);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31555-02.htm"))
		{
			st.takeItems(SUPPLY_BOX, 1);
			st.addExpAndSp(126668,11731);
			st.giveItems(57,40000);
			st.unset("cond");
			st.setState(State.COMPLETED);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
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
				if (player.getLevel() >= 66)
					htmltext = "31314-01.htm";
				else
				{
					htmltext = "31314-02.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case DONAL:
						if (cond == 1)
							htmltext = "31314-04.htm";
						break;
					case DAISY:
						switch (cond)
						{
							case 1:
								htmltext = "31315-01.htm";
								break;
							case 2:
								htmltext = "31315-03.htm";
								break;
						}
						break;
					case ABERCROMBIE:
						if (cond == 2 && st.getQuestItemsCount(SUPPLY_BOX) == 1)
							htmltext = "31555-01.htm";
						break;
				}
				break;
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		new _018_MeetingWithTheGoldenRam(18, qn, "");    	
	}
}
