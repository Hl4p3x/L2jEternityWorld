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
public class _251_NoSecrets extends Quest
{
	private static final String qn = "_251_NoSecrets";

	//Npc
    	private static final int PINAPS = 30201;

    	//Mobs
    	private static final int[] MOB = { 22775, 22777 };
    	private static final int[] MOB1 = { 22781, 22783, 22780, 22782, 22784 };

    	//Items
    	private static final int diary = 15508;
    	private static final int timetable = 15509;
	
	public _251_NoSecrets(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(PINAPS);
		addTalkId(PINAPS);
		for(int npcId : MOB)
			addKillId(npcId);
		for(int npcId : MOB1)
			addKillId(npcId);

        	questItemIds = new int[] { diary, timetable };
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
			
		if (event.equalsIgnoreCase("30201-05.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
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
		
		if(npcId == PINAPS)
		{
			if(id == State.CREATED && cond == 0)
			{
				if (player.getLevel()  >= 82)
				{
					htmltext = "30201-01.htm";
				}
				else
				{
					htmltext = "30201-02.htm";
					st.exitQuest(true);
				}
			}
			else if(id == State.STARTED && cond == 1)
			{
				htmltext = "30201-06.htm";
			}
			else if(id == State.STARTED && cond == 2)
			{
				htmltext = "30201-07.htm";
				st.unset("cond");
				st.giveItems(57, 313355);
				st.addExpAndSp(56787, 160578);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else if(id == State.COMPLETED)
			{
				htmltext = "30201-03.htm";
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

                		if(((npcId == MOB[0]) || (npcId == MOB[1])) && st.getQuestItemsCount(timetable) < 5 && (random < 3))
                		{
                    			st.giveItems(timetable, 1);
                    			st.playSound("ItemSound.quest_itemget");
                		}
                		else if(((npcId == MOB1[0]) || (npcId == MOB1[1]) || (npcId == MOB1[2]) || (npcId == MOB1[3]) || (npcId == MOB1[4])) && st.getQuestItemsCount(diary) < 10 && (random >= 4))
                		{
                    			st.giveItems(diary, 1);
                    			st.playSound("ItemSound.quest_itemget");
                		}
                		else if(st.getQuestItemsCount(timetable) == 5 && st.getQuestItemsCount(diary) == 10)
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
		new _251_NoSecrets(251, qn, "");
	}
}	