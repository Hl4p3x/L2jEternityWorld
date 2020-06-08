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
public class _014_WhereaboutsOfTheArchaeologist extends Quest
{
	private static final String qn = "_014_WhereaboutsOfTheArchaeologist";

	// NPC
	private static final int LIESEL = 31263;
	private static final int GHOST_OF_ADVENTURER = 31538;
	// QUEST ITEM
	private static final int LETTER_TO_ARCHAEOLOGIST = 7253;

	public _014_WhereaboutsOfTheArchaeologist(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(LIESEL);
		addTalkId(LIESEL);
		addTalkId(GHOST_OF_ADVENTURER);
		
		questItemIds = new int[] {LETTER_TO_ARCHAEOLOGIST};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31263-2.htm"))
		{
			st.set("cond", "1");
			st.giveItems(LETTER_TO_ARCHAEOLOGIST, 1);
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31538-1.htm"))
		{
			st.takeItems(LETTER_TO_ARCHAEOLOGIST, 1);
			st.addExpAndSp(325881, 32524);
			st.giveItems(57, 136928);
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
				if(player.getLevel() >= 74)
					htmltext = "31263-0.htm";
				else
				{
					htmltext = "31263-1.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case LIESEL:
						if (st.getInt("cond") == 1)
							htmltext = "31263-2.htm";		
						break;
					case GHOST_OF_ADVENTURER:
						if (st.getInt("cond") == 1 && st.getQuestItemsCount(LETTER_TO_ARCHAEOLOGIST) == 1)
							htmltext = "31538-0.htm";
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _014_WhereaboutsOfTheArchaeologist(14, qn, "");    	
	}
}
