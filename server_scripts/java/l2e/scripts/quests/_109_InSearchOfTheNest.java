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
 * Created by LordWinter 06.08.2011
 * Based on L2J Eternity-World
 */
public class _109_InSearchOfTheNest extends Quest
{
	private static final String qn = "_109_InSearchOfTheNest";

	//NPC
	private static final int PIERCE 		= 31553;
	private static final int CORPSE 		= 32015;
	private static final int KAHMAN 		= 31554;

	//QUEST ITEMS
	private static final int MEMO 			= 8083;
	private static final int GOLDEN_BADGE_RECRUIT 	= 7246;
	private static final int GOLDEN_BADGE_SOLDIER 	= 7247;

	public _109_InSearchOfTheNest(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(PIERCE);
		addTalkId(PIERCE);
		addTalkId(CORPSE);
		addTalkId(KAHMAN);

		questItemIds = new int[] { MEMO };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		int cond = st.getInt("cond");

		if(event.equalsIgnoreCase("Memo") && cond == 1)
		{
			st.giveItems(MEMO, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_itemget");
			htmltext = "You've find something...";
		}
		else if(event.equalsIgnoreCase("31553-02.htm") && cond == 2)
		{
			st.takeItems(MEMO, -1);
			st.set("cond", "3");
            		st.playSound("ItemSound.quest_middle");
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

		int npcId = npc.getId();
		int id = st.getState();
		int cond = st.getInt("cond");

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		else if(id == State.CREATED)
		{
			if(player.getLevel() >= 66 && npcId == PIERCE && (st.getQuestItemsCount(GOLDEN_BADGE_RECRUIT) > 0 || st.getQuestItemsCount(GOLDEN_BADGE_SOLDIER) > 0))
			{
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				st.set("cond", "1");
				htmltext = "31553-00a.htm";
			}
			else
			{
				htmltext = "31553-00.htm";
				st.exitQuest(true);
			}
		}
		else if(id == State.STARTED)
		{
			if(npcId == CORPSE)
			{
				if(cond == 1)
					htmltext = "32015-01.htm";
				else if(cond == 2)
					htmltext = "32015-01a.htm";
			}
			else if(npcId == PIERCE)
			{
				if(cond == 1)
					htmltext = "31553-01a.htm";
				else if(cond == 2)
					htmltext = "31553-01.htm";
				else if(cond == 3)
					htmltext = "31553-01b.htm";
			}
			else if(npcId == KAHMAN && cond == 3)
			{
				htmltext = "31554-01.htm";
				st.addExpAndSp(701500, 50000);
				st.giveItems(57, 161500);
				st.unset("cond");
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _109_InSearchOfTheNest(109, qn, "");
	}
}