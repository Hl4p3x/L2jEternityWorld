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
public final class _186_ContractExecution extends Quest
{
	private static final String qn = "_186_ContractExecution";
	
	private static int Nikola = 30621;
	private static int Lorain = 30673;
	private static int Luka = 31437;

	private static int Certificate = 10362;
	private static int MetalReport = 10366;
	private static int Accessory = 10367;
	
	public _186_ContractExecution(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(Lorain);
		addTalkId(Nikola);
		addTalkId(Lorain);
		addTalkId(Luka);

		for(int mobs = 20577; mobs <= 20583; mobs++)
			addKillId(mobs);

		questItemIds = new int[] { Certificate, MetalReport, Accessory };
	}	
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
				
        	if(event.equalsIgnoreCase("30673-02.htm"))
		{
            		st.set("cond","1");
			st.playSound("ItemSound.quest_accept");
            		st.takeItems(Certificate,-1);
            		st.giveItems(MetalReport,1);
		}	
        	else if(event.equalsIgnoreCase("30621-03.htm"))
		{
            		st.set("cond","2");
            		st.playSound("ItemSound.quest_middle");
		}	
        	else if(event.equalsIgnoreCase("31437-05.htm"))
		{
            		if (player.getLevel() < 50)
               			st.addExpAndSp(285935,18711);
            		st.giveItems(57,105083);
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
		int npcId = npc.getId();
		int cond = st.getInt("cond");		
		switch (st.getState())
		{		
			case State.STARTED:
				if (npcId == Lorain)
				{
					if(cond == 0)
					{
						if (player.getLevel() < 41)
							htmltext = "30673-00.htm";	
						else 
							htmltext = "30673-01.htm";
					}	
					else if(cond == 1)
						htmltext = "30673-03.htm";
				}
				else if (npcId == Nikola)
				{
					if(cond == 1)
						htmltext = "30621-01.htm";
					else if(cond == 2)
						htmltext = "30621-04.htm";
				}		
				else if(npcId == Luka)
				{
					if(st.getQuestItemsCount(Accessory) == 0)
						htmltext = "31437-01.htm";
					else
						htmltext = "31437-02.htm";	
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
		QuestState qs = player.getQuestState("_184_NikolasCooperationContract");
		QuestState qs1 = player.getQuestState("_188_SealRemoval");
		QuestState qs2 = player.getQuestState("_187_NikolasHeart");

		if (npc.getId() == Lorain && qs != null && qs.isCompleted() && qs1 == null && qs2 == null && st == null)
		{
			st = newQuestState(player);
			st.setState(State.STARTED);
		}
		else
			npc.showChatWindow(player);
		return "";
	}	
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null) 
			return null;
		
        	if(st.getQuestItemsCount(Accessory) == 0 && st.getInt("cond") == 2)
		{
            		if (getRandom(5) == 0)
			{
                		st.playSound("ItemSound.quest_middle");
                		st.giveItems(Accessory,1);
			}
		}	
		return null;	          
	} 

	public static void main(String[] args)
	{
		new _186_ContractExecution(186, qn, "");
	}
}