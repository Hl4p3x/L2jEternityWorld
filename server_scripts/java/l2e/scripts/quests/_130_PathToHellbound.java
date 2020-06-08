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
public class _130_PathToHellbound extends Quest
{
    	private static final String qn = "_130_PathToHellbound";

    	// NPCs
    	public static final int GALATE = 32292;
    	public static final int CASIAN = 30612;

    	// Items
    	public static final int CASIAN_BLUE_CRY = 12823;

	public _130_PathToHellbound (int id, String name, String descr)
    	{
        	super(id, name, descr);

        	addStartNpc(CASIAN);
        	addTalkId(CASIAN);
        	addTalkId(GALATE);

        	questItemIds = new int[] {CASIAN_BLUE_CRY};
    	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

        	if (event.equalsIgnoreCase("30612-03.htm"))
        	{
            		st.set("cond","1");
            		st.setState(State.STARTED);
        	}
        	else if (event.equalsIgnoreCase("32292-03.htm"))
        	{
            		st.set("cond","2");
        	}
        	else if (event.equalsIgnoreCase("30612-05.htm"))
        	{
           	 	st.set("cond","3");
            		st.giveItems(CASIAN_BLUE_CRY,1);
        	}
        	else if (event.equalsIgnoreCase("32292-06.htm"))
        	{
            		st.takeItems(CASIAN_BLUE_CRY,-1);
            		st.playSound("ItemSound.quest_finish");
            		st.exitQuest(false);
        	}
        	return htmltext;
    	}

    	@Override
	public String onTalk (L2Npc npc, L2PcInstance player)
    	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		final int npcId = npc.getId();
		final int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
		        	if (npcId == CASIAN)
		        	{
					if (player.getLevel() >= 78)
	                			htmltext = "30612-01.htm";
	                		else
	                		{
	                    			st.exitQuest(true);
	                    			htmltext = "30612-00.htm";
	                		}
				}
		        	break;
			case State.STARTED:
		        	switch (npcId)
		        	{
		        		case CASIAN:
		        			switch (cond)
		        			{
		        				case 1:
		        					htmltext = "30612-03a.htm";
		        					break;
		        				case 2:
		        					htmltext = "30612-04.htm";
		        					break;
		        				case 3:
		        					htmltext = "30612-05a.htm";
		        					break;
		        			}
		        			break;
		        		case GALATE:
		        			switch (cond)
		        			{
		        				case 1:
		        					htmltext = "32292-01.htm";
		        					break;
		        				case 2:
		        					htmltext = "32292-03a.htm";
		        					break;
		        				case 3:
		                        			if (st.getQuestItemsCount(CASIAN_BLUE_CRY) == 1)
		                        				htmltext = "32292-04.htm";
		                        			else
		                        				htmltext = "Incorrect item count";
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
        new _130_PathToHellbound(130, qn, "");
    }
}
