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
 * Created by LordWinter 15.03.2011
 * Based on L2J Eternity-World
 */
public class _113_StatusOfTheBeaconTower extends Quest
{
    	private static final String qn = "_113_StatusOfTheBeaconTower";

	// Moira, Torrant
	private final static int QUEST_NPC[] = {31979, 32016};

	// Box
	private final static int QUEST_ITEMS[] = { 8086 };

	// Adena
	private final static int QUEST_REWARD[] = { 57 };

	public _113_StatusOfTheBeaconTower(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(QUEST_NPC[0]);
		for (final int i: QUEST_NPC)
			addTalkId(i);
		
		questItemIds = QUEST_ITEMS;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event == "31979-02.htm")
		{
			st.set("cond","1");
			st.giveItems(QUEST_ITEMS[0], 1);
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event == "32016-02.htm")
		{
			if (st.getInt("cond") == 1)
			{
				st.giveItems(QUEST_REWARD[0], 154800);
				st.addExpAndSp(619300, 44200);
				st.takeItems(QUEST_ITEMS[0], 1);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
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
		
		final int npcId = npc.getId();
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
		        if (npcId == QUEST_NPC[0])
		        {
		            if (player.getLevel() >= 80)
		            	htmltext = "31979-01.htm";
		            else
		            {
		            	htmltext = "31979-00.htm";
		            	st.exitQuest(true);
		            }
		        }
		        break;
			case State.STARTED:
		        if (npcId == QUEST_NPC[0])
		        	htmltext = "31979-03.htm";
		        else if (npcId == QUEST_NPC[1])
		        {
		        	if (st.getQuestItemsCount(QUEST_ITEMS[0]) == 1)
		        		htmltext = "32016-01.htm";
		        }
		        break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _113_StatusOfTheBeaconTower(113, qn, "");
	}
}
