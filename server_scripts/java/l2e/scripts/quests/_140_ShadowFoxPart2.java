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
public class _140_ShadowFoxPart2 extends Quest
{
    	private static final String qn = "_140_ShadowFoxPart2";

    	private static final int KLUCK = 30895;
    	private static final int XENOVIA = 30912;
    	private static final int CRYSTAL = 10347;
    	private static final int OXYDE = 10348;
    	private static final int CRYPT = 10349;

    	private static final int[] NPC =
    	{
        	20789, 20790, 20791, 20792
    	};

    	public _140_ShadowFoxPart2(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	addFirstTalkId(KLUCK);
        	addTalkId(KLUCK);
        	addTalkId(XENOVIA);

        	for (int mob : NPC)
        	{
            		addKillId(mob);
        	}

        	questItemIds = new int[]
        	{
            		CRYSTAL, OXYDE, CRYPT
        	};
    	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

        	if (event.equalsIgnoreCase("30895-02.htm"))
        	{
            		st.set("cond", "1");
            		st.playSound("ItemSound.quest_accept");
        	}
        	else if (event.equalsIgnoreCase("30895-05.htm"))
        	{
            		st.set("cond", "2");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30895-09.htm"))
        	{
            		st.playSound("ItemSound.quest_finish");
            		st.unset("talk");
            		st.exitQuest(false);
            		st.giveItems(57, 18775);
            		if (player.getLevel() >= 37 && player.getLevel() <= 42)
            		{
                		st.addExpAndSp(30000, 2000);
            		}
        	}
        	else if (event.equalsIgnoreCase("30912-07.htm"))
        	{
            		st.set("cond", "3");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30912-09.htm"))
        	{
            		st.takeItems(CRYSTAL, 5);
            		if (st.getRandom(100) <= 60)
            		{
                		st.giveItems(OXYDE, 1);
                		if (st.getQuestItemsCount(OXYDE) >= 3)
                		{
                    			htmltext = "30912-09b.htm";
                    			st.set("cond", "4");
                    			st.playSound("ItemSound.quest_middle");
                    			st.takeItems(CRYSTAL, -1);
                    			st.takeItems(OXYDE, -1);
                    			st.giveItems(CRYPT, 1);
                		}
            		}
            		else
            		{
                		htmltext = "30912-09a.htm";
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

        	QuestState qs = player.getQuestState("_139_ShadowFoxPart1");

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
        	else if (npcId == KLUCK)
        	{
            		if (cond == 0)
            		{
                		if (player.getLevel() >= 37)
                		{
                    			htmltext = "30895-01.htm";
                		}
                		else
                		{
                    			htmltext = "30895-00.htm";
                    			st.exitQuest(true);
                		}
            		}
            		else if (cond == 1)
            		{
                			htmltext = "30895-02.htm";
            		}
            		else if (cond == 2 || cond == 3)
            		{
                		htmltext = "30895-06.htm";
            		}
            		else if (cond == 4)
            		{
                		if (cond == talk)
                		{
                    			htmltext = "30895-08.htm";
                		}
                		else
                		{
                    			htmltext = "30895-07.htm";
                    			st.takeItems(CRYPT, -1);
                    			st.set("talk", "1");
                		}
            		}
        	}
        	else if (npcId == XENOVIA)
        	{
            		if (cond == 2)
            		{
                		htmltext = "30912-01.htm";
            		}
            		else if (cond == 3)
            		{
                		if (st.getQuestItemsCount(CRYSTAL) >= 5)
                		{
                    			htmltext = "30912-08.htm";
                		}
                		else
                		{
                    			htmltext = "30912-07.htm";
                		}
            		}
            		else if (cond == 4)
            		{
                		htmltext = "30912-10.htm";
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

        	if (st.getInt("cond") == 3 && st.getRandom(100) <= 80)
        	{
            		st.playSound("ItemSound.quest_itemget");
            		st.giveItems(CRYSTAL, 1);
        	}
        	return null;
    	}

    	public static void main(String[] args)
    	{
        	new _140_ShadowFoxPart2(140, qn, "");
    	}
}
