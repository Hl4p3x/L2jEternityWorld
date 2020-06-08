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
 * Created by Sigrlinne
 * Based on L2J Eternity-World
 */
public class _252_ItSmellsDelicious extends Quest
{
	private static final String qn = "_252_ItSmellsDelicious";

	//NPC
	private static final int STAN = 30200;
	
	//MOB
	private static final int COOK = 18908;
	private static final int[] MOB = { 22786, 22787, 22788 };
	
	//Item
	private static final int diary = 15500;
	private static final int page = 15501;
	
	public _252_ItSmellsDelicious(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(STAN);
		addTalkId(STAN);
		for(int npcId : MOB)
			addKillId(npcId);
		addKillId(COOK);

        	questItemIds = new int[] { diary, page };
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
			
		if (event.equalsIgnoreCase("30200-05.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		
		if (event.equalsIgnoreCase("30200-08.htm"))
		{
			st.unset("cond");
			st.giveItems(57, 313355);
			st.addExpAndSp(56787, 160578);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		int id = st.getState();
		int npcId = npc.getId();
		final int cond = st.getInt("cond");
		String htmltext = getNoQuestMsg(player);
		
		if(npcId == STAN)
		{
			if(id == State.CREATED && cond == 0)
			{
				if (player.getLevel()  >= 82)
				{
					htmltext = "30200-01.htm";
				}
				else
				{
					htmltext = "30200-02.htm";
					st.exitQuest(true);
				}
			}
			else if(id == State.STARTED && cond == 1)
			{
				htmltext = "30200-06.htm";
			}
			else if(id == State.STARTED && cond == 2)
			{
				htmltext = "30200-07.htm";
			}
			else if(id == State.COMPLETED)
			{
				htmltext = "30200-03.htm";
			}
		}
        	return htmltext;
    	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		int npcId = npc.getId();
		if (st.getState() == State.STARTED)
		{
            		if (Integer.parseInt(st.get("cond")) == 1)
            		{
				long random = getRandom(10);

                		if((npcId == COOK) && st.getQuestItemsCount(page) < 5 && (random < 3))
                		{
                    			st.giveItems(page, 1);
                    			st.playSound("ItemSound.quest_itemget");
                		}
                		else if(((npcId == MOB[0]) || (npcId == MOB[1]) || (npcId == MOB[2])) && st.getQuestItemsCount(diary) < 10 && (random >= 4))
                		{
                    			st.giveItems(diary, 1);
                    			st.playSound("ItemSound.quest_itemget");
                		}
                		else if(st.getQuestItemsCount(page) == 5 && st.getQuestItemsCount(diary) == 10)
                		{
                    			st.set("cond", "2");
                    			st.playSound("ItemSound.quest_middle");
                		}
            		}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _252_ItSmellsDelicious(252, qn, "");
	}
}