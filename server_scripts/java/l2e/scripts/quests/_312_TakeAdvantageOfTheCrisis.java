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
 * Created by LordWinter 04.08.2011
 * Based on L2J Eternity-World
 */
public class _312_TakeAdvantageOfTheCrisis extends Quest
{
    	private static final String qn = "_312_TakeAdvantageOfTheCrisis";

    	// NPC's
    	private static final int Filaur = 30535;

    	// ITEMS
    	private static final int MineralFragment = 14875;

    	// MONSTERS
    	private static final int[] GraveRobberList = {22678,22679,22680,22681,22682};
    	private static final int[] Others = {22683,22684,22685,22686,22687,22688,22689,22690};

    	public _312_TakeAdvantageOfTheCrisis(int id, String name, String descr)
    	{
        	super(id, name, descr);

        	addStartNpc(Filaur);
        	addTalkId(Filaur);

        	for (int i : GraveRobberList)
        	    addKillId(i);
        	for (int i : Others)
        	    addKillId(i);

        	questItemIds = new int[] { MineralFragment };
    	}

   	@Override
    	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

        	if (event.equalsIgnoreCase("30535-25.htm"))
        	{
            		st.exitQuest(true);
            		st.playSound("ItemSound.quest_finish");
        	}
        	else if (event.equalsIgnoreCase("30535-6.htm"))
        	{
            		st.set("cond","1");
            		st.setState(State.STARTED);
            		st.playSound("ItemSound.quest_accept");
        	}
        	else if (event.equalsIgnoreCase("30535-14.htm"))
        	{
            		if (st.getQuestItemsCount(MineralFragment) >= 366)
            		{
                		st.takeItems(MineralFragment,366);
                		st.giveItems(9487,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30535-14.htm";
            		}
            		else
                		htmltext = "30535-14no.htm";
        	}
        	else if (event.equalsIgnoreCase("30535-15.htm"))
        	{
            		if (st.getQuestItemsCount(MineralFragment) >= 299)
            		{
                		st.takeItems(MineralFragment,299);
                		st.giveItems(9488,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30535-14.htm";
            		}
            		else
                		htmltext = "30535-14no.htm";
        	}
        	else if (event.equalsIgnoreCase("30535-16.htm"))
        	{
            		if (st.getQuestItemsCount(MineralFragment) >= 183)
            		{
                		st.takeItems(MineralFragment,183);
                		st.giveItems(9489,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30535-14.htm";
            		}
            		else
                		htmltext = "30535-14no.htm";
        	}
        	else if (event.equalsIgnoreCase("30535-17.htm"))
        	{
            		if (st.getQuestItemsCount(MineralFragment) >= 122)
            		{
                		st.takeItems(MineralFragment,122);
                		st.giveItems(9490,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30535-14.htm";
            		}
            		else
                		htmltext = "30535-14no.htm";
        	}
        	else if (event.equalsIgnoreCase("30535-18.htm"))
        	{
            		if (st.getQuestItemsCount(MineralFragment) >= 122)
            		{
                		st.takeItems(MineralFragment,122);
                		st.giveItems(9491,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30535-14.htm";
            		}
            		else
                		htmltext = "30535-14no.htm";
        	}
        	else if (event.equalsIgnoreCase("30535-19.htm"))
        	{
            		if (st.getQuestItemsCount(MineralFragment) >= 129)
            		{
                		st.takeItems(MineralFragment,129);
                		st.giveItems(9497,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30535-14.htm";
            		}
            		else
                		htmltext = "30535-14no.htm";
        	}
        	else if (event.equalsIgnoreCase("30535-20.htm"))
        	{
            		if (st.getQuestItemsCount(MineralFragment) >= 667)
            		{
                		st.takeItems(MineralFragment,667);
                		st.giveItems(9625,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30535-14.htm";
            		}
            		else
                		htmltext = "30535-14no.htm";
        	}
        	else if (event.equalsIgnoreCase("30535-21.htm"))
        	{
            		if (st.getQuestItemsCount(MineralFragment) >= 1000)
            		{
                		st.takeItems(MineralFragment,1000);
                		st.giveItems(9626,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30535-.htm";
            		}
            		else
                		htmltext = "30535-14no.htm";
        	}
        	else if (event.equalsIgnoreCase("30535-22.htm"))
        	{
            		if (st.getQuestItemsCount(MineralFragment) >= 24)
            		{
                		st.takeItems(MineralFragment,24);
                		st.giveItems(9628,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30535-14.htm";
            		}
            		else
                		htmltext = "30535-14no.htm";
        	}
        	else if (event.equalsIgnoreCase("30535-23.htm"))
        	{
            		if (st.getQuestItemsCount(MineralFragment) >= 24)
            		{
                		st.takeItems(MineralFragment,24);
                		st.giveItems(9629,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30535-14.htm";
            		}
            		else
                		htmltext = "30535-14no.htm";
        	}
        	else if (event.equalsIgnoreCase("30535-24.htm"))
        	{
            		if (st.getQuestItemsCount(MineralFragment) >= 36)
            		{
                		st.takeItems(MineralFragment,36);
                		st.giveItems(9630,1);
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30535-14.htm";
            		}
            		else
                		htmltext = "30535-14no.htm";
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

		int cond = st.getInt("cond");
		int npcId = npc.getId();
		byte id = st.getState();

		if(npcId == Filaur)
		{
        		if(id == State.CREATED)
			{
        			if (cond == 0)
        			{
            				if (player.getLevel() >= 80)
                				htmltext = "30535-0.htm";
            				else
            				{
                				st.exitQuest(true);
                				htmltext = "30535-0a.htm";
            				}
        			}
			}
			else if(id == State.CREATED)
			{
        			if (cond == 1 && st.getQuestItemsCount(MineralFragment) == 0)
            				htmltext = "30535-6.htm";
        			else if (cond == 1 && st.getQuestItemsCount(MineralFragment) > 0)
            				htmltext = "30535-7.htm";
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

        	if (isIntInArray(npc.getId(), GraveRobberList))
        	{
            		int count = 1;
            		int chance = (int)(30 * Config.RATE_QUEST_DROP);
            		while (chance > 100)
            		{
                		chance -= 100;
                		if (chance < 30)
                    			chance = 30;
                		count++;
            		}
            		if (Rnd.getChance(chance))
            		{
                		st.giveItems(MineralFragment,count);
                		st.playSound("ItemSound.quest_itemget");
            		}
        	}
        	else if (isIntInArray(npc.getId(), Others))
        	{
            		int count = 1;
            		int chance = (int)(20 * Config.RATE_QUEST_DROP);
            		while (chance > 100)
            		{
                		chance -= 100;
                		if (chance < 20)
                    			chance = 20;
                		count++;
            		}

            		if (Rnd.getChance(chance))
            		{
                		st.giveItems(MineralFragment,count);
                		st.playSound("ItemSound.quest_itemget");
            		}
        	}
        	return null;
    	}

    	public static void main(String[] args)
    	{
        	new _312_TakeAdvantageOfTheCrisis(312, qn, "");
    	}
}