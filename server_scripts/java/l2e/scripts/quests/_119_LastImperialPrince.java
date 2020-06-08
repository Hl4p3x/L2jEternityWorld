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
public final class _119_LastImperialPrince extends Quest
{
	private static final String qn = "_119_LastImperialPrince";

	// NPC
	private static int SPIRIT   = 31453;
	private static int DEVORIN  = 32009;

	// ITEM
	private static int BROOCH   = 7262;
	
	public _119_LastImperialPrince(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(SPIRIT);
		addTalkId(SPIRIT);
		addTalkId(DEVORIN);
	}	
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;				

		if(event.equalsIgnoreCase("31453-4.htm"))
		{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}   
		else if(event.equalsIgnoreCase("32009-2.htm"))
		{
			if(st.getQuestItemsCount(BROOCH) < 1)
			{
				htmltext = "32009-2a.htm";
				st.exitQuest(true);
			}
		}	  
		else if(event.equalsIgnoreCase("32009-3.htm"))
		{
			st.set("cond","2");
			st.playSound("ItemSound.quest_middle");
		}   
		else if(event.equalsIgnoreCase("31453-7.htm"))
		{	
			st.giveItems(57,150292);
			st.addExpAndSp(902439,90067);
			st.setState(State.COMPLETED);
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

		int cond = st.getInt("cond");
		int npcId = npc.getId();

		switch (st.getState())
		{		
			case State.CREATED:
					if(player.getLevel() < 74)
					{
						htmltext = "31453-0.htm";
						st.exitQuest(true);
					}	
					else
						htmltext = "31453-1.htm";			
					break;
			case State.STARTED:		
					if(npcId == SPIRIT)
					{
						if(cond == 1)
							htmltext = "31453-4.htm";
						else if(cond == 2)
							htmltext = "31453-5.htm";
					}	  
					else if(npcId == DEVORIN)
					{
						if(cond == 1)
							htmltext = "32009-1.htm";
						else if(cond == 2)
							htmltext = "32009-3.htm";	
					}		  
					break;
			case State.COMPLETED:
					htmltext = getAlreadyCompletedMsg(player);
					break;
		}
		return htmltext;
	}	
	
	public static void main(String[] args)
	{
		new _119_LastImperialPrince(119, qn, "");
	}
}