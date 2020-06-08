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
public class _238_SuccesFailureOfBusiness extends Quest
{
    	private static final String qn = "_238_SuccesFailureOfBusiness";

    	// NPC's
    	private static final int DrHelvetica			= 32641;

    	// MONSTERS
    	private static final int BrazierOfPurity 		= 18806;
    	private static final int GuardianSpiritsOfMagicForce 	= 22659;
    	private static final int EvilSpiritsInMagicForce 	= 22658;

    	// ITEMS
    	private static final int BrokenPieveOfMagicForce 	= 14867;
    	private static final int GuardianSpiritFragment 	= 14868;
    	private static final int VicinityOfTheFieldOfSilenceResearchCenter = 14865;

    	public _238_SuccesFailureOfBusiness(int id, String name, String descr)
    	{
        	super(id, name, descr);

        	addStartNpc(DrHelvetica);
        	addTalkId(DrHelvetica);

        	addKillId(BrazierOfPurity);
        	addKillId(GuardianSpiritsOfMagicForce);
        	addKillId(EvilSpiritsInMagicForce);

        	questItemIds = new int[] { BrokenPieveOfMagicForce, GuardianSpiritFragment };
    	}

	@Override
    	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    	{
		QuestState st = player.getQuestState(qn);
        	if (st == null)
			return null;

        	if (event.equals("32461-03.htm"))
        	{
            		st.set("cond","1");
			st.setState(State.STARTED);
                	st.playSound("ItemSound.quest_accept");
        	}
        	else if (event.equals("32461-06.htm"))
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

        	int cond = st.getInt("cond");

		QuestState qs = player.getQuestState("_237_WindsOfChange");
		QuestState qs2 = player.getQuestState("_239_WontYouJoinUs");

        	if (npc.getId() == DrHelvetica)
        	{
        		if (st.getState() == State.COMPLETED)
            			htmltext = "32461-09.htm";
            		else if (cond == 0)
            		{
                		if (qs2.getState() == State.COMPLETED)
                    			htmltext = "32461-10.htm";
				else if (qs.getState() == State.COMPLETED)
				{
                			if (player.getLevel() >= 82)
                    				htmltext = "32461-01.htm";
                			else
                			{
                    				htmltext = "32461-00.htm";
                    				st.exitQuest(true);
                			}
				}
            		}
            		else if (cond == 1)
                		htmltext = "32461-04.htm";
            		else if (cond == 2)
            		{
                		st.takeItems(BrokenPieveOfMagicForce,10);
                		htmltext = "32461-05.htm";
            		}
            		else if (cond == 3)
                		htmltext = "32461-07.htm";
            		else if (cond == 4 && st.getQuestItemsCount(GuardianSpiritFragment) >= 20)
            		{
                		htmltext = "32461-08.htm";
                		st.giveItems(57, 283346);
                		st.takeItems(VicinityOfTheFieldOfSilenceResearchCenter,-1);
                		st.takeItems(GuardianSpiritFragment,20);
                		st.addExpAndSp(1319736, 103553);
				st.setState(State.COMPLETED);
                		st.unset("cond");
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

        	if (npc.getId() == BrazierOfPurity)
        	{
            		if (cond == 1)
            		{
                		int count = 1;
                		int chance = (int)(5* Config.RATE_QUEST_DROP);
                		while (chance > 1000)
                		{
                    			chance -= 1000;
                    			count++;
                		}
                		if (getRandom(1000) <= chance)
                		{
                    			st.giveItems(BrokenPieveOfMagicForce,count);
                    			st.playSound("ItemSound.quest_itemget");
                    			if (st.getQuestItemsCount(BrokenPieveOfMagicForce) == 10)
                    			{
                        			st.set("cond","2");
                        			st.playSound("ItemSound.quest_middle");
                    			}
                		}
            		}
        	}
        	else if (npc.getId() == GuardianSpiritsOfMagicForce || npc.getId() == EvilSpiritsInMagicForce)
        	{
            		if (cond == 3)
            		{
                		int count = 1;
                		int chance = (int)(5* Config.RATE_QUEST_DROP);
                		while (chance > 1000)
                		{
                    			chance -= 1000;
                    			count++;
                		}
                		if (getRandom(1000) <= chance)
                		{
                    			st.giveItems(GuardianSpiritFragment,count);
                    			st.playSound("ItemSound.quest_itemget");
                    			if (st.getQuestItemsCount(GuardianSpiritFragment) == 20)
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
        	new _238_SuccesFailureOfBusiness(238, qn, "");
    	}
}