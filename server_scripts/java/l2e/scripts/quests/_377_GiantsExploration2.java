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

import l2e.Config;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.util.Rnd;

/**
 * Created by LordWinter 06.08.2011
 * Based on L2J Eternity-World
 */
public class _377_GiantsExploration2 extends Quest
{
    	private static final String qn = "_377_GiantsExploration2";

    	// NPC's
    	private static final int SOBLING = 31147;

    	// Items
    	private static final int TITAN_ANCIENT_BOOK = 14847;
    	private static final int BOOK1=14842, BOOK2=14843, BOOK3=14844, BOOK4=14845, BOOK5=14846;

    	// Mobs
    	private static final int[] MOBS = {22661,22662,22663,22664,22665,22666,22667,22668,22669};

    	public _377_GiantsExploration2(int id, String name, String descr)
    	{
        	super(id, name, descr);

        	addStartNpc(SOBLING);
        	addTalkId(SOBLING);

        	for (int i : MOBS)
	        	addKillId(i);

        	questItemIds = new int[] {TITAN_ANCIENT_BOOK};
    	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31147-02.htm"))
        	{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
        	}
        	else if (event.equalsIgnoreCase("31147-quit.htm"))
        	{
			st.unset("cond");
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
        	}
        	else if (event.equalsIgnoreCase("rewardBook"))
        	{
            		if (st.getQuestItemsCount(BOOK1) >= 5 && st.getQuestItemsCount(BOOK2) >= 5 && st.getQuestItemsCount(BOOK3) >= 5 && st.getQuestItemsCount(BOOK4) >= 5 && st.getQuestItemsCount(BOOK5) >= 5)
            		{
                		st.giveItems(getRandom(100) < 50 ? 9626 : 9625, 1); // Giant's Codex - Oblivion or Giant's Codex - Discipline
                		st.takeItems(BOOK1, 5);
                		st.takeItems(BOOK2, 5);
                		st.takeItems(BOOK3, 5);
                		st.takeItems(BOOK4, 5);
                		st.takeItems(BOOK5, 5);
                		st.playSound("ItemSound.quest_finish");
                		htmltext = "31147-ok.htm";
            		}
            		else
                		htmltext = "31147-no.htm";
        	}
        	else if (event.equals("randomReward"))
        	{
            		if (st.getQuestItemsCount(BOOK1) >= 1 && st.getQuestItemsCount(BOOK2) >= 1 && st.getQuestItemsCount(BOOK3) >= 1 && st.getQuestItemsCount(BOOK4) >= 1 && st.getQuestItemsCount(BOOK5) >= 1)
            		{
                		int[][] reward = {{9628, 6},{9629,3},{9630,4}};
                		int rnd = getRandom(reward.length);
                		st.giveItems(reward[rnd][0], reward[rnd][1]);
                		st.takeItems(BOOK1, 1);
                		st.takeItems(BOOK2, 1);
                		st.takeItems(BOOK3, 1);
                		st.takeItems(BOOK4, 1);
                		st.takeItems(BOOK5, 1);
                		st.playSound("ItemSound.quest_finish");
                		htmltext = "31147-ok.htm";
            		}
            		else
                		htmltext = "31147-no.htm";
        	}
        	else if (isDigit(event))
        	{
            		if (st.getQuestItemsCount(BOOK1) >= 1 && st.getQuestItemsCount(BOOK2) >= 1 && st.getQuestItemsCount(BOOK3) >= 1 && st.getQuestItemsCount(BOOK4) >= 1 && st.getQuestItemsCount(BOOK5) >= 1)
            		{
                		int itemId = Integer.parseInt(event);
                		int count = 1;
                		if (itemId == 9628)
                    			count = 6;
                		else if (itemId == 9629)
                    			count = 3;
                		else if (itemId == 9630)
                    			count = 4;
                		st.giveItems(itemId, count);
                		st.takeItems(BOOK1, 1);
                		st.takeItems(BOOK2, 1);
                		st.takeItems(BOOK3, 1);
                		st.takeItems(BOOK4, 1);
                		st.takeItems(BOOK5, 1);
                		st.playSound("ItemSound.quest_finish");
                		htmltext = "31147-ok.htm";
            		}
            		else
                		htmltext = "31147-no.htm";
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

        	if (st.getState() == State.STARTED)
        	{
			if (st.getQuestItemsCount(BOOK1) > 0 && st.getQuestItemsCount(BOOK2) > 0 && st.getQuestItemsCount(BOOK3) > 0 && st.getQuestItemsCount(BOOK4) > 0 && st.getQuestItemsCount(BOOK5) > 0)
				htmltext = "31147-03.htm";
			else
				htmltext = "31147-02a.htm";
        	}
		else
        	{
			if (player.getLevel() >= 79)
				htmltext = "31147-01.htm";
			else
				htmltext = "31147-00.htm";
        	}
	    	return htmltext;
    	}

	@Override
    	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
    	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		if (st.getState() != State.STARTED)
			return null;

		if (st.getInt("cond") == 1 && isIntInArray(npc.getId(), MOBS))
        	{
			int chance = (int)(20* Config.RATE_QUEST_DROP);
            		int count = 1;
            		while (chance > 100)
            		{
                		chance -= 100;
                		if (chance < 20)
                    			chance = 20;
                		count++;
            		}
            		if (Rnd.getChance(chance))
            		{
				st.giveItems(TITAN_ANCIENT_BOOK,count);
				st.playSound("ItemSound.quest_itemget");
            		}
        	}
		return null;
    	}

    	public static void main(String[] args)
    	{
        	new _377_GiantsExploration2(377, qn, "");
    	}
}