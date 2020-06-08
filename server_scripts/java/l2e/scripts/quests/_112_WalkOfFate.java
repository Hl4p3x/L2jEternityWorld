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
public class _112_WalkOfFate extends Quest
{
    	private static final String qn = "_112_WalkOfFate";

	// Livina, Karuda
	private static final int LIVINA = 30572;
	private static final int KARUDA = 32017;

	// Enchant D
	private final static int QUEST_REWARD[] = { 956 };
	
	public _112_WalkOfFate(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(LIVINA);
		addTalkId(LIVINA);
		addTalkId(KARUDA);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
	    	if (event.equalsIgnoreCase("32017-02.htm"))
	    	{
	    		if (st.getInt("cond") == 1)
	    		{
		    		st.takeItems(57, 22308);
		    		st.takeItems(QUEST_REWARD[0], 1);
		    		st.addExpAndSp(112876, 5774);
		    		st.exitQuest(false);
		    		st.playSound("ItemSound.quest_finish");
	    		}
	    	}
	    	else if (event.equalsIgnoreCase("30572-02.htm"))
	    	{
	        	st.playSound("ItemSound.quest_accept");
	        	st.setState(State.STARTED);
	        	st.set("cond","1");
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
		        	if (npcId == LIVINA)
		        	{
		            		if (player.getLevel() >= 20)
		               			htmltext = "30572-01.htm";
		            		else
		            		{
		               			htmltext = "30572-00.htm";
		               			st.exitQuest(true);
		            		}
		        	}
		        	break;
			case State.STARTED:
		        	if (npcId == LIVINA)
		            		htmltext = "30572-03.htm";
		        	else if (npcId == KARUDA)
		            		htmltext = "32017-01.htm";
		        		break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new _112_WalkOfFate(112, qn, "");
	}	
}
