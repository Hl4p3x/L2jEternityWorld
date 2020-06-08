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
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public final class _329_CuriosityOfDwarf extends Quest
{
	private static final String qn = "_329_CuriosityOfDwarf";

	private static int GOLEM_HEARTSTONE = 1346;
	private static int BROKEN_HEARTSTONE = 1365;
	
	public _329_CuriosityOfDwarf(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(30437);
		addTalkId(30437);

		addKillId(20083,20085);

		questItemIds = new int[] { BROKEN_HEARTSTONE, GOLEM_HEARTSTONE };
	}	
		
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
			
		if (event.equalsIgnoreCase("30437-03.htm"))
		{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if("30437-06.htm".equals(event))
		{
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
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
		
		switch (st.getState())
		{		
			case State.CREATED:
				if (player.getLevel() >= 33)
					htmltext = "30437-02.htm";
				else
				{
					htmltext = "30437-01.htm";
					st.exitQuest(true);
				}		
				break;
			case State.STARTED:
				int heart = (int)st.getQuestItemsCount(GOLEM_HEARTSTONE);
				int broken = (int)st.getQuestItemsCount(BROKEN_HEARTSTONE);
				if (broken + heart > 0)
				{
					st.giveItems(57,50 * broken + 1000 * heart);
					st.takeItems(BROKEN_HEARTSTONE,-1);
					st.takeItems(GOLEM_HEARTSTONE,-1);
					htmltext = "30437-05.htm";
				}	
				else
					htmltext = "30437-04.htm";			
				break;	
		}
		return htmltext;
	}	

    	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null) 
			return null;

		int npcId = npc.getId();
		int n = st.getRandom(100);

		if (st.isStarted())
		{
			if (npcId == 20085)
			{
				if (n < 5)
				{
					st.giveItems(GOLEM_HEARTSTONE,1);
					st.playSound("ItemSound.quest_itemget");
				}   
				else if(n < 58)
				{
					st.giveItems(BROKEN_HEARTSTONE,1);
					st.playSound("ItemSound.quest_itemget");
				}   
			}      
			else if(npcId == 20083)
			{
				if(n < 6)
				{
					st.giveItems(GOLEM_HEARTSTONE,1);
					st.playSound("ItemSound.quest_itemget");
				}
				if(n < 56)
				{
					st.giveItems(BROKEN_HEARTSTONE,1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}          
      		return null;
	}	

	public static void main(String[] args)
	{
		new _329_CuriosityOfDwarf(329, qn, "");
	}
}