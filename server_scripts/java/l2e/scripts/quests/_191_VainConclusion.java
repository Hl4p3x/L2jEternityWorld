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
 * Created by LordWinter 06.07.2012
 * Based on L2J Eternity-World
 */
public final class _191_VainConclusion extends Quest
{
	private static final String qn = "_191_VainConclusion";

	private static final int Kusto = 30512;
	private static final int Lorain = 30673;
	private static final int Dorothy = 30970;
	private static final int Shegfield = 30068;

	private static final int Metal = 10371;	

	public _191_VainConclusion(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addTalkId(Kusto);
		addTalkId(Lorain);
		addTalkId(Dorothy);
		addTalkId(Shegfield);
		addFirstTalkId(Dorothy);

		questItemIds = new int[] { Metal };
	}	
		
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;				

        	if (event.equalsIgnoreCase("30970-03.htm"))
		{
            		st.set("cond","1");
			st.playSound("ItemSound.quest_accept");
            		st.giveItems(Metal,1);
		}	
        	else if(event.equalsIgnoreCase("30673-02.htm"))
		{
            		st.set("cond","2");
            		st.playSound("ItemSound.quest_middle");
            		st.takeItems(Metal,-1);
		}	
        	else if(event.equalsIgnoreCase("30068-03.htm"))
		{
            		st.set("cond","3");
            		st.playSound("ItemSound.quest_middle");
		}
        	else if(event.equalsIgnoreCase("30512-02.htm"))
		{
            		if (player.getLevel() < 50)
               			st.addExpAndSp(309467,20614);
            		st.giveItems(57,117327);
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
		if(st == null)
			return htmltext;

        	int npcId = npc.getId();
        	int cond = st.getInt("cond");

		switch (st.getState())
		{
        		case State.STARTED:
            			if (npcId == Dorothy)
				{
					if (cond == 0)
					{
                				if (player.getLevel() < 42)
							htmltext = "30970-00.htm";
						else
                    					htmltext = "30970-01.htm";
					}		
                			else if (cond == 1)
						htmltext = "30970-04.htm";
				}		
           			else if (npcId == Lorain)
				{
                			if (cond == 1)
                    				htmltext = "30673-01.htm";
                			else if (cond == 2)
                    				htmltext = "30673-03.htm";
                			else if (cond == 3)
                			{
                    				htmltext = "30673-04.htm";
                    				st.set("cond","4");
                    				st.playSound("ItemSound.quest_middle");
                			}
                			else if (cond == 4)
                    				htmltext = "30673-05.htm";
				}
           			else if (npcId == Shegfield)
				{
                			if (cond == 2)
                    				htmltext = "30068-01.htm";
                			else if (cond == 3)
                    				htmltext = "30068-04.htm";
				}
           			else if (npcId == Kusto)
				{
                			if (cond == 4)
                    				htmltext = "30512-01.htm";
				}
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
		}
		return htmltext;
	}				
		
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		QuestState qs = player.getQuestState("_188_SealRemoval");
		if (npc.getId() == Dorothy && qs != null && qs.isCompleted() && st == null) 
		{
			st = newQuestState(player);
			st.setState(State.STARTED);
		}
		else
			npc.showChatWindow(player);
		return "";
	}	

	public static void main(String[] args)
	{
		new _191_VainConclusion(191, qn, "");		
	}
}