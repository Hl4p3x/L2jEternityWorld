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
public class _308_ReedFieldMaintenance extends Quest
{
    	private static final String qn = "_308_ReedFieldMaintenance";

    	//NPC
    	private static final int Katensa 		= 32646;

    	//MOBS
    	private static final int[] Mucrokians 		= {22650,22651,22652,22653};
    	private static final int ContaminatedMucrokian 	= 22654;
    	private static final int ChangedMucrokian 	= 22655;

    	//ITEMS
    	private static final int MucrokianHide 		= 14871;
    	private static final int AwakenedMucrokianHide 	= 14872;

    	public _308_ReedFieldMaintenance(int id, String name, String descr)
    	{
        	super(id, name, descr);

        	addStartNpc(Katensa);
        	addTalkId(Katensa);

        	addKillId(ChangedMucrokian);
        	addKillId(ContaminatedMucrokian);
        	for (int i : Mucrokians)
            		addKillId(i);

        	questItemIds = new int[] { MucrokianHide, AwakenedMucrokianHide };
    	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

        	else if (event.equals("32646-3.htm"))
        	{
            		st.set("cond","1");
            		st.setState(State.STARTED);
            		st.playSound("ItemSound.quest_accept");
        	}
        	else if (event.equals("32646-5.htm"))
        	{
            		if (st.getQuestItemsCount(14866) > 0)
                		htmltext = "32646-5b.htm";
        	}
        	else if (event.equals("32646-8.htm"))
        	{
            		if (st.getQuestItemsCount(AwakenedMucrokianHide) > 0)
            		{
                		long Hides = st.getQuestItemsCount(AwakenedMucrokianHide);
                		int Reward = (int)(Hides*2);
                		st.giveItems(MucrokianHide,Reward);
                		st.takeItems(AwakenedMucrokianHide, -1);
            		}
        	}
        	else if (event.equals("32646-8a.htm"))
        	{
            		if (st.getQuestItemsCount(AwakenedMucrokianHide) > 0)
            		{
                		long Hides = st.getQuestItemsCount(AwakenedMucrokianHide);
                		int Reward = (int)(Hides*2);
                		st.giveItems(MucrokianHide,Reward);
                		st.takeItems(AwakenedMucrokianHide, -1);
            		}
        	}
        	else if (event.equals("32646-10.htm"))
        	{
            		st.exitQuest(true);
            		st.playSound("ItemSound.quest_finish");
        	}
        	else if (event.equals("32646-11.htm"))
        	{
            		if (st.getQuestItemsCount(MucrokianHide) >= 346)
            		{
                		st.takeItems(MucrokianHide,346);
                		st.giveItems(9985,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "32646-11.htm";
            		}
            		else
                		htmltext = "32646-8no.htm";
        	}
        	else if (event.equals("32646-12.htm"))
        	{
            		if (st.getQuestItemsCount(MucrokianHide) >= 462)
            		{
                		st.takeItems(MucrokianHide,462);
                		st.giveItems(9986,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "32646-11.htm";
            		}
            		else
                		htmltext = "32646-8no.htm";
        	}
        	else if (event.equals("32646-13.htm"))
        	{
            		if (st.getQuestItemsCount(MucrokianHide) >= 232)
            		{
                		st.takeItems(MucrokianHide,232);
                		st.giveItems(9987,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "32646-11.htm";
            		}
            		else
                		htmltext = "32646-8no.htm";
        	}
        	else if (event.equals("32646-14.htm"))
        	{
            		if (st.getQuestItemsCount(MucrokianHide) >= 372)
            		{
                		st.takeItems(MucrokianHide,372);
                		st.giveItems(10115,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "32646-11.htm";
            		}
            		else
                		htmltext = "32646-8no.htm";
        	}
        	else if (event.equals("32646-15.htm"))
        	{
            		if (st.getQuestItemsCount(MucrokianHide) >= 288)
            		{
                		st.takeItems(MucrokianHide,288);
                		st.giveItems(9985,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "32646-11.htm";
            		}
            		else
               		 	htmltext = "32646-8no.htm";
        	}
        	else if (event.equals("32646-16.htm"))
        	{
            		if (st.getQuestItemsCount(MucrokianHide) >= 384)
            		{
                		st.takeItems(MucrokianHide,384);
                		st.giveItems(9986,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "32646-11.htm";
            		}
            		else
                		htmltext = "32646-8no.htm";
        	}
        	else if (event.equals("32646-17.htm"))
        	{
            		if (st.getQuestItemsCount(MucrokianHide) >= 192)
            		{
                		st.takeItems(MucrokianHide,192);
                		st.giveItems(9987,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "32646-11.htm";
            		}
            		else
                		htmltext = "32646-8no.htm";
        	}
        	else if (event.equals("32646-18.htm"))
        	{
            		if (st.getQuestItemsCount(MucrokianHide) >= 310)
            		{
                		st.takeItems(MucrokianHide,310);
                		st.giveItems(10115,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "32646-11.htm";
            		}
            		else
                		htmltext = "32646-8no.htm";
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

        	QuestState st1 = player.getQuestState("_309_ForAGoodCause");
        	QuestState st2 = player.getQuestState("_238_SuccesFailureOfBusiness");

        	int cond = st.getInt("cond");

        	if (npc.getId() == Katensa)
        	{
             		if (cond == 0)
             		{
                		if (st1 != null)
                		{
                    			if (st1.getInt("cond") >= 1)
                        			return "32646-0b.htm";
                		}
                		if ((st1 == null || st1.getInt("cond") == 0) && player.getLevel() >= 82)
                    			htmltext = "32646-0.htm";
                		else
                		{
                    			htmltext = "32646-0a.htm";
                    			st.exitQuest(true);
                		}
             		}
             		else if (cond == 1 && st.getQuestItemsCount(MucrokianHide) == 0)
                		htmltext = "32646-3a.htm";
             		else if (cond == 1 && st.getQuestItemsCount(MucrokianHide) > 0)
             		{
                		if (st2 != null)
                		{
                    			if (st2.getState() == State.COMPLETED)
                        			return "32646-4a.htm";
                    			htmltext = "32646-4.htm";
                		}
                		else
                    			htmltext = "32646-4.htm";
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
        	if (st == null || st.getInt("cond") != 1)
			return null;

        	if (isIntInArray(npc.getId(), Mucrokians))
        	{
            		int count = 1;
            		int chance = (int)(5*Config.RATE_QUEST_DROP);
            		while(chance > 1000)
            		{
                		chance -= 1000;
                		if (chance < 5)
                    			chance = 5;
                		count++;
            		}
            		if (getRandom(1000) <= chance)
            		{
                		st.giveItems(MucrokianHide,count);
                		st.playSound("ItemSound.quest_itemget");
            		}
        	}
        	else if (npc.getId() == ContaminatedMucrokian || npc.getId() == ChangedMucrokian)
        	{
            		int count = 1;
            		int chance = (int)(5*Config.RATE_QUEST_DROP);
            		while(chance > 1000)
            		{
                		chance -= 1000;
                		if (chance < 5)
                    			chance = 5;
                		count++;
            		}
            		if (getRandom(1000) <= chance)
            		{
                		st.giveItems(AwakenedMucrokianHide,count);
                		st.playSound("ItemSound.quest_itemget");
            		}
        	}
        	return null;
    	}

    	public static void main(String[] args)
    	{
        	new _308_ReedFieldMaintenance(308, qn, "");
    	}
}