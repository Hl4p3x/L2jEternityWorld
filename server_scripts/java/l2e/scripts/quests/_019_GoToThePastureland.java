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
public class _019_GoToThePastureland extends Quest
{
	private static final String qn = "_019_GoToThePastureland";

	private final static int VLADIMIR = 31302;
	private final static int TUNATUN = 31537;
	private final static int BEAST_MEAT = 7547;

	public _019_GoToThePastureland(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(VLADIMIR);
		addTalkId(VLADIMIR);
		addTalkId(TUNATUN);

		questItemIds = new int[] {BEAST_MEAT};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31302-1.htm"))
		{
			st.giveItems(BEAST_MEAT, 1);
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("31537-1.htm"))
		{
			st.takeItems(BEAST_MEAT, 1);
			st.addExpAndSp(136766,12688);
			st.giveItems(57, 50000);
			st.unset("cond");
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
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (player.getLevel() >= 63)
					htmltext = "31302-0.htm";
				else
				{
					htmltext = "31302-0a.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case VLADIMIR:
						htmltext = "31302-2.htm";
						break;
					case TUNATUN:
						if(st.getQuestItemsCount(BEAST_MEAT) >= 1)
							htmltext = "31537-0.htm";
						else
						{
							htmltext = "31537-1.htm";
							st.exitQuest(true);
						}
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _019_GoToThePastureland(19, qn, "");    	
	}
}