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
 * Created by LordWinter 06.04.2011
 * Based on L2J Eternity-World
 */
public class _10282_ToTheSeedOfAnnihilation extends Quest
{
    	private static final String qn = "_10282_ToTheSeedOfAnnihilation";

    	// NPC
    	private static final int KBALDIR = 32733;
    	private static final int KLEMIS = 32734;

    	// ITEMS
    	private static final int SOA_ORDERS = 15512;

    	public _10282_ToTheSeedOfAnnihilation(int id, String name, String descr)
    	{
        	super(id, name, descr);
        	addStartNpc(KBALDIR);
        	addTalkId(KBALDIR);
        	addTalkId(KLEMIS);
        	questItemIds = new int[] {SOA_ORDERS};
    	}

    	@Override
		public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32733-07.htm"))
        	{
			st.setState(State.STARTED);
			st.set("cond","1");
			st.giveItems(SOA_ORDERS,1);
			st.playSound("ItemSound.quest_accept");
        	}
		else if (event.equalsIgnoreCase("32734-02.htm"))
        	{
			st.unset("cond");
			st.addExpAndSp(1148480,99110);
			st.takeItems(SOA_ORDERS,-1);
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
        	{
			if (npc.getId() == KBALDIR)
				htmltext = "32733-09.htm";
			else if (npc.getId() == KLEMIS)
				htmltext = "32734-03.htm";
        	}
		else if (st.getState() == State.CREATED)
        	{
			if (player.getLevel() >= 84)
				htmltext = "32733-01.htm";
			else
				htmltext = "32733-00.htm";
        	}
		else
        	{
			if (st.getInt("cond") == 1)
            		{
				if (npc.getId() == KBALDIR)
					htmltext = "32733-08.htm";
				else if (npc.getId() == KLEMIS)
					htmltext = "32734-01.htm";
            		}
        	}
		return htmltext;
    	}

    	public static void main(String[] args)
    	{
        	new _10282_ToTheSeedOfAnnihilation(10282, qn, "");
    	}
}