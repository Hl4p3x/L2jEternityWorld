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
public final class _187_NikolasHeart extends Quest
{
	private static final String qn = "_187_NikolasHeart";

	private static int Kusto = 30512;
	private static int Nikola = 30621;
	private static int Lorain = 30673;

	private static int Certificate = 10362;
	private static int Metal = 10368;
	
	public _187_NikolasHeart(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(Lorain);
		addTalkId(Lorain);
		addTalkId(Kusto);
		addTalkId(Nikola);

		questItemIds = new int[] { Certificate, Metal };
	}	
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
				
        	if (event.equalsIgnoreCase("30673-02.htm"))
		{
			st.set("cond","1");	
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
            		st.takeItems(Certificate,-1);
            		st.giveItems(Metal,1);
		}	
        	else if(event.equalsIgnoreCase("30621-03.htm"))
		{
            		st.set("cond","2");
            		st.playSound("ItemSound.quest_middle");
		}	
        	else if(event.equalsIgnoreCase("30512-03.htm"))
		{
            		if (player.getLevel() < 50)
               			st.addExpAndSp(285935,18711);
            		st.giveItems(57,93383);
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
			case State.CREATED:
				QuestState qs = player.getQuestState("_185_NikolasCooperationConsideration");
				QuestState qs1 = player.getQuestState("_186_ContractExecution");			
				if(npcId == Lorain && (qs.getState() == State.COMPLETED) && qs1 == null )
				{
					if (player.getLevel() < 41)
						htmltext = "30673-00.htm";
					else				
						htmltext = "30673-01.htm";				
				}
				break;
			case State.STARTED:
				if(npcId == Lorain)
				{						
					if(cond == 1)
						htmltext = "30673-03.htm";
				}
				else if(npcId == Nikola)
				{
					if (cond == 1)
						htmltext = "30621-01.htm";
					else if(cond == 2)
						htmltext = "30621-04.htm";
				}
				else if(npcId == Kusto)
				{
					if (cond == 2)
						htmltext = "30512-01.htm";
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
		new _187_NikolasHeart(187, qn, "");
	}
}