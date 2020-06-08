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
public class _10268_ToTheSeedOfInfinity extends Quest
{
	private static final String qn = "_10268_ToTheSeedOfInfinity";

    	//NPCs
    	private static final int Keucereus = 32548;
    	private static final int Tepios = 32603;

    	//Items
    	private static final int Introduction = 13811;

    	public _10268_ToTheSeedOfInfinity(int id, String name, String descr)
    	{
        	super(id, name, descr);
        	addStartNpc(Keucereus);
        	addTalkId(Keucereus);
        	addTalkId(Tepios);
        	questItemIds = new int[] {Introduction};
    	}

    	@Override
		public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

        	else if (event.equalsIgnoreCase("32548-05.htm"))
        	{
            		st.set("cond","1");
            		st.setState(State.STARTED);
            		st.playSound("ItemSound.quest_accept");
            		st.giveItems(Introduction,1);
        	}
        	return htmltext;
    	}

    	@Override
		public final String onTalk(L2Npc npc, L2PcInstance player)
    	{
        	String htmltext = getNoQuestMsg(player);
        	QuestState st = player.getQuestState(qn);
        	if (st == null) 
			return htmltext;

        	if (st.isCompleted())
        	{
            		if (npc.getId() == Tepios)
                		htmltext = "32530-02.htm";
            		else
                		htmltext = "32548-0a.htm";
        	}
        	else if (st.getState() == State.CREATED && npc.getId() == Keucereus)
        	{
            		if (player.getLevel() < 75)
                		htmltext = "32548-00.htm";
            		else
                		htmltext = "32548-01.htm";
        	}
        	else if (st.getState() == State.STARTED && npc.getId() == Keucereus)
            		htmltext = "32548-06.htm";
        	else if (st.getState() == State.STARTED && npc.getId() == Tepios)
        	{
            		htmltext = "32530-01.htm";
            		st.giveItems(57,16671);
            		st.addExpAndSp(100640,10098);
            		st.unset("cond");
            		st.exitQuest(false);
            		st.playSound("ItemSound.quest_finish");
        	}
        	return htmltext;
    	}

    	public static void main(String[] args)
    	{
        	new _10268_ToTheSeedOfInfinity(10268, qn, "");
    	}
}