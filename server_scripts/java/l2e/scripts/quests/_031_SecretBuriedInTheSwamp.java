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

public class _031_SecretBuriedInTheSwamp extends Quest
{
	private static final String qn = "_031_SecretBuriedInTheSwamp";

	// NPC
	private static final int ABERCROMBIE = 31555;
	private static final int FORGOTTEN_MONUMENT_1 = 31661;
	private static final int FORGOTTEN_MONUMENT_2 = 31662;
	private static final int FORGOTTEN_MONUMENT_3 = 31663;
	private static final int FORGOTTEN_MONUMENT_4 = 31664;
	private static final int CORPSE_OF_DWARF = 31665;

	// Quest Item
	private static final int KRORINS_JOURNAL = 7252;

	public _031_SecretBuriedInTheSwamp(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(ABERCROMBIE);

		for(int i = 31661; i <= 31665; i++)
			addTalkId(i);

		questItemIds = new int[] {KRORINS_JOURNAL};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		int cond = st.getInt("cond");

		if(event.equalsIgnoreCase("31555-1.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("31665-1.htm") && cond == 1)
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_itemget");
			st.giveItems(KRORINS_JOURNAL, 1);
		}
		else if(event.equalsIgnoreCase("31555-4.htm") && cond == 2)
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("31661-1.htm") && cond == 3)
		{
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("31662-1.htm") && cond == 4)
		{
			st.set("cond", "5");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("31663-1.htm") && cond == 5)
		{
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("31664-1.htm") && cond == 6)
		{
			st.set("cond", "7");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("31555-7.htm") && cond == 7)
		{
			st.takeItems(KRORINS_JOURNAL, -1);
			st.addExpAndSp(130000, 0);
			st.giveItems(57, 40000);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
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

		if (st.isCompleted())
			return htmltext = getAlreadyCompletedMsg(player);

		int npcId = npc.getId();
		int cond = st.getInt("cond");
		if(npcId == ABERCROMBIE)
		{
			if(cond == 0)
			{
				if(player.getLevel() >= 66)
					htmltext = "31555-0.htm";
				else
				{
					htmltext = "31555-0a.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1)
				htmltext = "31555-2.htm";
			else if(cond == 2)
				htmltext = "31555-3.htm";
			else if(cond == 3)
				htmltext = "31555-5.htm";
			else if(cond == 7)
				htmltext = "31555-6.htm";
		}
		else if(npcId == CORPSE_OF_DWARF)
		{
			if(cond == 1)
				htmltext = "31665-0.htm";
			else if(cond == 2)
				htmltext = "31665-2.htm";
		}
		else if(npcId == FORGOTTEN_MONUMENT_1)
		{
			if(cond == 3)
				htmltext = "31661-0.htm";
			else if(cond > 3)
				htmltext = "31661-2.htm";
		}
		else if(npcId == FORGOTTEN_MONUMENT_2)
		{
			if(cond == 4)
				htmltext = "31662-0.htm";
			else if(cond > 4)
				htmltext = "31662-2.htm";
		}
		else if(npcId == FORGOTTEN_MONUMENT_3)
		{
			if(cond == 5)
				htmltext = "31663-0.htm";
			else if(cond > 5)
				htmltext = "31663-2.htm";
		}
		else if(npcId == FORGOTTEN_MONUMENT_4)
			if(cond == 6)
				htmltext = "31664-0.htm";
			else if(cond > 6)
				htmltext = "31664-2.htm";
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _031_SecretBuriedInTheSwamp(31, qn, "");    	
	}
}