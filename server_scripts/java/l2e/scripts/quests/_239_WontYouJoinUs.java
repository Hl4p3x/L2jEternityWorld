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

/**
 * Created by LordWinter 06.08.2011
 * Based on L2J Eternity-World
 */
public class _239_WontYouJoinUs extends Quest
{
    	private static final String qn = "_239_WontYouJoinUs";

    	// NPCs
    	private static final int Athenia 		= 32643;

    	// MONSTERS
    	private static final int WasteLandfillMachines 	= 18805;
    	private static final int Suppressor 		= 22656;
    	private static final int Exterminator 		= 22657;

    	// ITEMS
    	private static final int DestroyedMachinePiece 	= 14869;
    	private static final int EnchantedGolemFragment = 14870;
    	private static final int CerificateOfSupport 	= 14866;

    	public _239_WontYouJoinUs(int id, String name, String descr)
    	{
       	 	super(id, name, descr);

        	addStartNpc(Athenia);
        	addTalkId(Athenia);

        	addKillId(WasteLandfillMachines);
        	addKillId(Suppressor);
        	addKillId(Exterminator);

        	questItemIds = new int[] { DestroyedMachinePiece, EnchantedGolemFragment };
    	}

	@Override
    	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    	{
		QuestState st = player.getQuestState(qn);
        	if (st == null)
			return null;

        	if (event.equals("32643-03.htm"))
        	{
            		st.set("cond","1");
			st.setState(State.STARTED);
                	st.playSound("ItemSound.quest_accept");
        	}
        	if (event.equals("32643-07.htm"))
        	{
            		st.set("cond","3");
            		st.playSound("ItemSound.quest_middle");
        	}
        	return event;
    	}

	@Override
    	public String onTalk(L2Npc npc, L2PcInstance player)
    	{
        	String htmltext = getNoQuestMsg(player);

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		QuestState qs2 = player.getQuestState("_238_SuccesFailureOfBusiness");

        	if (npc.getId() == Athenia)
        	{
            		int cond = st.getInt("cond");

        		if (st.getState() == State.COMPLETED)
            			htmltext = "32643-11.htm";
            		else if (cond == 0)
            		{
                		if (qs2.getState() == State.COMPLETED)
				{
                			if (player.getLevel() >= 82)
                   		 		htmltext = "32643-01.htm";
                			else
                			{
                    				htmltext = "32643-00.htm";
                    				st.exitQuest(true);
					}
                		}
				else
					htmltext = "32643-00.htm";
            		}
            		else if (cond == 1)
			{
				if (st.getQuestItemsCount(DestroyedMachinePiece) >= 1)
                			htmltext = "32643-05.htm";
				else
					htmltext = "32643-04.htm";
			}
            		else if (cond == 2)
            		{
                		st.takeItems(DestroyedMachinePiece,10);
                		htmltext = "32643-06.htm";
            		}
            		else if (cond == 3)
			{
				if (st.getQuestItemsCount(EnchantedGolemFragment) >= 1)
                			htmltext = "32643-08.htm";
				else
					htmltext = "32643-09.htm";
			}
            		else if (cond == 4 && st.getQuestItemsCount(EnchantedGolemFragment) == 20)
            		{
                		htmltext = "32643-10.htm";
                		st.giveItems(57, 283346);
                		st.takeItems(CerificateOfSupport,1);
                		st.takeItems(EnchantedGolemFragment,20);
                		st.addExpAndSp(1319736, 103553);
                		st.unset("cond");
				st.setState(State.COMPLETED);
                		st.exitQuest(false);
                		st.playSound("ItemSound.quest_finish");
            		}
        	}
        	return htmltext;
    	}

	@Override
    	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
    	{
        	L2PcInstance partyMember = getRandomPartyMemberState(player, State.STARTED);
        	if (partyMember == null)
			return null;

        	QuestState st = partyMember.getQuestState(qn);
        	if (st == null)
			return null;

        	int cond = st.getInt("cond");

        	if (npc.getId() == WasteLandfillMachines)
        	{
            		if (cond == 1)
            		{
                		int count = 1;
                		int chance = (int)(5*Config.RATE_QUEST_DROP);
                		while (chance > 1000)
                		{
                    			chance -= 1000;
                    			if (chance < 5)
                        			chance = 5;
                    			count++;
                		}
                		if (getRandom(1000) <= chance)
                		{
                    			st.giveItems(DestroyedMachinePiece,count);
                    			st.playSound("ItemSound.quest_itemget");
                    			if (st.getQuestItemsCount(DestroyedMachinePiece) == 10)
                    			{
                        			st.set("cond","2");
                        			st.playSound("ItemSound.quest_middle");
                    			}
                		}
            		}
        	}
        	else if (npc.getId() == Suppressor || npc.getId() == Exterminator)
        	{
            		if (cond == 3)
            		{
                		int count = 1;
                		int chance = (int)(5*Config.RATE_QUEST_DROP);
                		while (chance > 1000)
                		{
                    			chance -= 1000;
                    			if (chance < 5)
                        			chance = 5;
                    			count++;
                		}
                		if (getRandom(1000) <= chance)
                		{
                    			st.giveItems(EnchantedGolemFragment,count);
                    			st.playSound("ItemSound.quest_itemget");
                    			if (st.getQuestItemsCount(EnchantedGolemFragment) == 20)
                    			{
                        			st.set("cond","4");
                        			st.playSound("ItemSound.quest_middle");
                    			}
                		}
            		}
        	}
        	return null;
    	}

    	public static void main(String[] args)
    	{
        	new _239_WontYouJoinUs(239, qn, "");
    	}
}