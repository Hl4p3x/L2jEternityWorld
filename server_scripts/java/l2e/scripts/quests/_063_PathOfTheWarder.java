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
public final class _063_PathOfTheWarder extends Quest
{
    	private static final String qn = "_063_PathOfTheWarder";

    	// NPC
    	private static final int SIONE = 32195;
    	private static final int GOBIE = 32198;
    	private static final int BATHIS = 30332;
    	private static final int TOBIAS = 30297;

    	// MOBS
    	private static final int OL_MAHUM_NOVICE = 20782;
    	private static final int OL_MAHUM_PATROL = 20053;
    	private static final int MAILLE_LIZARDMAN = 20919;
    	private static final int OL_MAHUM_OFFICER_TAK = 27337;

    	// ITEMS
    	private static final int ORDERS = 9762;
    	private static final int ORGANIZATION_CHART = 9763;
    	private static final int GOBIES_ORDERS = 9764;
    	private static final int LETTER_TO_HUMANS = 9765;
    	private static final int REPLAY_HUMANS = 9766;
    	private static final int LETTER_TO_DARKELVES = 9767;
    	private static final int REPLAY_DARKELVES = 9768;
    	private static final int REPORT_TO_SIONE = 9769;
    	private static final int EMPTY_SOUL_CRYSTAL = 9770;
    	private static final int TAKS_CAPTURED_SOUL = 9771;
    	private static final int STEELRAZOR_EVALUTION = 9772;

    	private static final int[] QUESTITEMS =
    	{
        	ORDERS, ORGANIZATION_CHART, GOBIES_ORDERS, LETTER_TO_HUMANS, REPLAY_HUMANS, LETTER_TO_DARKELVES,
        	REPLAY_DARKELVES, REPORT_TO_SIONE, EMPTY_SOUL_CRYSTAL, TAKS_CAPTURED_SOUL,
    	};

    	public _063_PathOfTheWarder(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	addStartNpc(SIONE);
        	addTalkId(SIONE);
        	addTalkId(GOBIE);
        	addTalkId(BATHIS);
        	addTalkId(TOBIAS);

        	addKillId(OL_MAHUM_NOVICE);
        	addKillId(OL_MAHUM_PATROL);
        	addKillId(MAILLE_LIZARDMAN);
        	addKillId(OL_MAHUM_OFFICER_TAK);

        	questItemIds = QUESTITEMS;
    	}

    	@Override
    	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

        	if (event.equalsIgnoreCase("32195-02.htm"))
        	{
            		st.set("cond", "1");
            		st.playSound("ItemSound.quest_accept");
            		st.setState(State.STARTED);
        	}
        	else if (event.equalsIgnoreCase("32195-04.htm"))
        	{
            		st.set("cond", "2");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("32198-02.htm"))
        	{
            		st.set("cond", "5");
            		st.playSound("ItemSound.quest_middle");
            		st.takeItems(GOBIES_ORDERS, -1);
           	 	st.giveItems(LETTER_TO_HUMANS, 1);
        	}
        	else if (event.equalsIgnoreCase("30332-01.htm"))
        	{
            		st.giveItems(REPLAY_HUMANS, 1);
            		st.takeItems(LETTER_TO_HUMANS, -1);
        	}
        	else if (event.equalsIgnoreCase("30332-03.htm"))
        	{
            		st.set("cond", "6");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("32198-06.htm"))
        	{
            		st.giveItems(LETTER_TO_DARKELVES, 1);
            		st.set("cond", "7");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30297-04.htm"))
        	{
            		st.giveItems(REPLAY_DARKELVES, 1);
            		st.set("cond", "8");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("32198-09.htm"))
        	{
            		st.giveItems(REPORT_TO_SIONE, 1);
            		st.set("cond", "9");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("32198-13.htm"))
        	{
            		st.giveItems(EMPTY_SOUL_CRYSTAL, 1);
            		st.set("cond", "11");
            		st.playSound("ItemSound.quest_middle");
        	}
        	return htmltext;
    	}

    	@Override
    	public final String onTalk(L2Npc npc, L2PcInstance player)
    	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

        	int npcId = npc.getId();
        	int cond = st.getInt("cond");

        	if (st.getState() == State.COMPLETED)
        	{
            		if (npcId == GOBIE)
            		{
                		htmltext = "32198-16.htm";
            		}
            		else
            		{
                		htmltext = getAlreadyCompletedMsg(player);
            		}
        	}
        	else if (npcId == SIONE)
        	{
            		if (player.getClassId().getId() != 0x7C || player.getLevel() < 18)
            		{
                		htmltext = "32195-00.htm";
                		st.exitQuest(true);
            		}
            		else if (st.getState() == State.CREATED)
            		{
                		htmltext = "32195-01.htm";
            		}
            		else if (cond == 1)
            		{
                		htmltext = "32195-03.htm";
           	 	}
            		else if (cond == 2)
            		{
                		htmltext = "32195-05.htm";
            		}
            		else if (cond == 3)
            		{
                		htmltext = "32195-06.htm";
                		st.set("cond", "4");
                		st.playSound("ItemSound.quest_middle");
                		st.giveItems(GOBIES_ORDERS, 1);
                		st.takeItems(ORDERS, -1);
                		st.takeItems(ORGANIZATION_CHART, -1);
            		}
            		else if (cond >= 4 && cond < 9)
            		{
                		htmltext = "32195-07.htm";
            		}
           	 	else if (cond == 9)
            		{
                		htmltext = "32195-08.htm";
                		st.set("cond", "10");
                		st.playSound("ItemSound.quest_middle");
                		st.takeItems(REPORT_TO_SIONE, -1);
            		}
            		else if (cond == 10)
            		{
                		htmltext = "32195-09.htm";
            		}
        	}
        	else if (npcId == GOBIE)
        	{
            		if (cond == 4)
            		{
                		htmltext = "32198-01.htm";
            		}
            		else if (cond == 5)
            		{
                		htmltext = "32198-03.htm";
            		}
            		else if (cond == 6)
            		{
                		if (st.getQuestItemsCount(REPLAY_HUMANS) == 1)
                		{
                    			st.takeItems(REPLAY_HUMANS, -1);
                    			htmltext = "32198-04.htm";
                		}
                		else
                		{
                    			htmltext = "32198-05.htm";
                		}
            		}
            		else if (cond == 7)
            		{
                		htmltext = "32198-07.htm";
            		}
            		else if (cond == 8)
            		{
                		if (st.getQuestItemsCount(LETTER_TO_DARKELVES) == 1)
                		{
                    			htmltext = "32198-08.htm";
                    			st.takeItems(REPLAY_DARKELVES, -1);
                		}
                		else
                		{
                    			htmltext = "32198-09.htm";
                    			st.giveItems(REPORT_TO_SIONE, 1);
                    			st.set("cond", "9");
                    			st.playSound("ItemSound.quest_middle");
                		}
            		}
            		else if (cond == 9)
            		{
                		htmltext = "32198-10.htm";
            		}
            		else if (cond == 10)
            		{
                		htmltext = "32198-11.htm";
            		}
            		else if (cond == 11)
            		{
                		htmltext = "32198-14.htm";
            		}
            		else if (cond == 12)
            		{
                		htmltext = "32198-15.htm";
                		st.takeItems(TAKS_CAPTURED_SOUL, -1);
                		st.giveItems(STEELRAZOR_EVALUTION, 1);
                		String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
                		if (isFinished.equalsIgnoreCase(""))
                		{
                    			st.addExpAndSp(160267, 2967);
                		}
                		st.playSound("ItemSound.quest_finish");
                		st.exitQuest(false);
                		st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                		st.unset("cond");
            		}
        	}
        	else if (npcId == BATHIS)
        	{
            		if (cond == 5)
            		{
                		if (st.getQuestItemsCount(REPLAY_HUMANS) == 1)
                		{
                    			htmltext = "30332-02.htm";
                		}
                		else
                		{
                    			htmltext = "30332-00.htm";
                		}
            		}
            		else if (cond > 5)
            		{
                		htmltext = "30332-04.htm";
            		}
        	}
        	else if (npcId == TOBIAS)
        	{
            		if (cond == 7)
           		{
                		if (st.getQuestItemsCount(LETTER_TO_DARKELVES) == 1)
                		{
                    			htmltext = "30297-01.htm";
                    			st.takeItems(LETTER_TO_DARKELVES, -1);
                		}
                		else
                		{
                    			htmltext = "30297-04.htm";
                    			st.giveItems(REPLAY_DARKELVES, 1);
                    			st.set("cond", "8");
                    			st.playSound("ItemSound.quest_middle");
                		}
            		}
            		else if (cond == 8)
            		{
                		htmltext = "30297-05.htm";
            		}
        	}
        	return htmltext;
    	}

    	@Override
    	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
    	{
       		QuestState st = player.getQuestState(getName());
        	if (st == null)
        	{
            		return null;
        	}

        	int npcId = npc.getId();
        	int cond = st.getInt("cond");

        	if (npcId == OL_MAHUM_NOVICE)
        	{
            		if (st.getQuestItemsCount(ORDERS) < 10 && cond == 2)
            		{
                		st.giveItems(ORDERS, 1);
                		if (st.getQuestItemsCount(ORDERS) == 10)
                		{
                    			if (st.getQuestItemsCount(ORGANIZATION_CHART) == 5)
                    			{
                        			st.playSound("ItemSound.quest_middle");
                        			st.set("cond", "3");
                    			}
                		}
                		else
                		{
                    			st.playSound("ItemSound.quest_itemget");
                		}
            		}
        	}
        	else if (npcId == OL_MAHUM_PATROL)
        	{
            		if (st.getQuestItemsCount(ORGANIZATION_CHART) < 5 && cond == 2)
            		{
                		st.giveItems(ORGANIZATION_CHART, 1);
                		if (st.getQuestItemsCount(ORGANIZATION_CHART) == 5)
                		{
                    			if (st.getQuestItemsCount(ORDERS) == 10)
                    			{
                        			st.playSound("ItemSound.quest_middle");
                        			st.set("cond", "3");
                    			}
                		}
                		else
                		{
                		    	st.playSound("ItemSound.quest_itemget");
                		}
            		}
        	}
        	else if (npcId == MAILLE_LIZARDMAN)
        	{
           		if (st.getQuestItemsCount(TAKS_CAPTURED_SOUL) == 0 && st.getRandom(10) < 2 && cond == 11)
            		{
                		npc = st.addSpawn(OL_MAHUM_OFFICER_TAK, 180000);
            		}
        	}
        	else if (npcId == OL_MAHUM_OFFICER_TAK)
        	{
            		if (st.getQuestItemsCount(TAKS_CAPTURED_SOUL) == 0 && cond == 11)
            		{
                		st.playSound("ItemSound.quest_middle");
                		st.takeItems(EMPTY_SOUL_CRYSTAL, -1);
                		st.giveItems(TAKS_CAPTURED_SOUL, 1);
                		st.set("cond", "12");
            		}
        	}
        	return null;
    	}

    	public static void main(String[] args)
    	{
       	 	new _063_PathOfTheWarder(63, qn, "");
    	}
}