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
public class _139_ShadowFoxPart1 extends Quest
{
    	private static final String qn = "_139_ShadowFoxPart1";

    	private static final int MIA = 30896;
    	private static final int FRAGMENT = 10345;
    	private static final int CHEST = 10346;

    	private static final int[] NPC =
    	{
        	20636, 20637, 20638, 20639
    	};

    	public _139_ShadowFoxPart1(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	addFirstTalkId(MIA);
        	addTalkId(MIA);

        	for (int mob : NPC)
        	{
            		addKillId(mob);
        	}

        	questItemIds = new int[]
        	{
            		FRAGMENT, CHEST
        	};
    	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

        	if (event.equalsIgnoreCase("30896-03.htm"))
        	{
            		st.set("cond", "1");
            		st.playSound("ItemSound.quest_accept");
        	}
        	else if (event.equalsIgnoreCase("30896-11.htm"))
        	{
            		st.set("cond", "2");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30896-14.htm"))
        	{
            		st.takeItems(FRAGMENT, -1);
            		st.takeItems(CHEST, -1);
            		st.set("talk", "1");
        	}
        	else if (event.equalsIgnoreCase("30896-16.htm"))
        	{
            		st.playSound("ItemSound.quest_finish");
            		st.unset("talk");
            		st.exitQuest(false);
            		st.giveItems(57, 14050);
            		if (player.getLevel() >= 37 && player.getLevel() <= 42)
            		{
                		st.addExpAndSp(30000, 2000);
            		}
        	}
        	return htmltext;
    	}

    	@Override
    	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
    	{
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
        	{
            		st = newQuestState(player);
        	}

        	QuestState qs = player.getQuestState("_138_TempleChampionPart2");

        	if (qs != null)
        	{
            		if (qs.getState() == State.COMPLETED && st.getState() == State.CREATED)
            		{
                		st.setState(State.STARTED);
            		}
        	}
        	npc.showChatWindow(player);
        	return null;
    	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

        	final int npcId = npc.getId();
        	final int id = st.getState();
        	final int cond = st.getInt("cond");
        	final int talk = st.getInt("talk");

        	if (id == State.CREATED)
        	{
            		return htmltext;
        	}
        	if (id == State.COMPLETED)
        	{
            		htmltext = getAlreadyCompletedMsg(player);
        	}
        	else if (npcId == MIA)
        	{
            		if (cond == 0)
            		{
                		if (player.getLevel() >= 37)
                		{
                    			htmltext = "30896-01.htm";
                		}
                		else
                		{
                    			htmltext = "30896-00.htm";
                    			st.exitQuest(true);
                		}
            		}
            		else if (cond == 1)
            		{
                		htmltext = "30896-03.htm";
            		}
            		else if (cond == 2)
            		{
                		if (st.getQuestItemsCount(FRAGMENT) >= 10 && st.getQuestItemsCount(CHEST) >= 1)
                		{
                    			htmltext = "30896-13.htm";
                		}
                		else if (cond == talk)
                		{
                    			htmltext = "30896-14.htm";
                		}
                		else
                		{
                    			htmltext = "30896-12.htm";
                		}
            		}
        	}
        	return htmltext;
    	}

    	@Override
    	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
    	{
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
        	{
            		return null;
        	}

        	if (st.getInt("cond") == 2)
        	{
            		st.playSound("ItemSound.quest_itemget");
            		st.giveItems(FRAGMENT, 1);
            		if (st.getRandom(100) <= 2)
            		{
                		st.giveItems(CHEST, 1);
            		}
        	}
        	return null;
    	}

    	public static void main(String[] args)
    	{
        	new _139_ShadowFoxPart1(139, qn, "");
    	}
}