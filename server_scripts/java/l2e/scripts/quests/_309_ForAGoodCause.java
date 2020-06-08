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
 * Created by LordWinter 15.03.2011
 * Based on L2J Eternity-World
 */
public final class _309_ForAGoodCause extends Quest
{
    	private static final String qn = "_309_ForAGoodCause";
    
    	// Quest NPC
    	private static final int ATRA = 32647;
    
    	// Quest Monsters
    	private static final int[] MUCROKIANS = 
    	{
       		22650,
        	22651,
        	22652,
        	22653,
        	22654
    	};
    
    	private static final int CHANGED_MUCROKIAN = 22655;

    	// Quest Items
    	private static final int MUCROKIAN_HIDE = 14873;
    	private static final int FALLEN_MUCROKIAN_HIDE = 14874;
    
    	// Base drop chance of quest items 
    	private static final int MUCROKIAN_HIDE_CHANCE = 100;
    	private static final int FALLEN_HIDE_CHANCE = 100;
    
    	// Rewards
    	private static final int EXCHANGE_REC_MOIRAI_MAGE_MUCROKIAN_HIDE_COUNT = 240;
    	private static final int REC_MOIRAI_MAGE_REWARD_COUNT = 6;

    	private static final int[] REC_MOIRAI_MAGE =
    	{
        	15777,
        	15780,
        	15783,
        	15786,
        	15789,
        	15790
    	};

    	private static final int EXCHANGE_PART_MOIRAI_MAGE_MUCROKIAN_HIDE_COUNT = 180;
    	private static final int PART_MOIRAI_MAGE_REWARD_COUNT = 6;
    	private static final int PART_MOIRAI_MAGE_MIN_REWARD_ITEM_COUNT = 3;
    	private static final int PART_MOIRAI_MAGE_MAX_REWARD_ITEM_COUNT = 9;

    	private static final int[] PART_MOIRAI_MAGE =
    	{
        	15647,
        	15650,
        	15653,
        	15656,
        	15659,
        	15692
    	};

    	public _309_ForAGoodCause(int questID, String name, String description)
    	{
        	super(questID, name, description);

        	addStartNpc(ATRA);
        	addTalkId(ATRA);
        
        	for (int currentNPCID : MUCROKIANS)
            		addKillId(currentNPCID);

        	addKillId(CHANGED_MUCROKIAN);
    	}
    
    	private String onExchangeRequest(QuestState questState, int exchangeID)
    	{
        	String resultHtmlText = "32647-13.htm";
        
        	long fallenMucrokianHideCount = questState.getQuestItemsCount(FALLEN_MUCROKIAN_HIDE);
        	if (fallenMucrokianHideCount > 0)
        	{
            		questState.takeItems(FALLEN_MUCROKIAN_HIDE, fallenMucrokianHideCount);
            		questState.giveItems(MUCROKIAN_HIDE, fallenMucrokianHideCount * 2);
            		fallenMucrokianHideCount = 0;
        	}
        
        	long mucrokianHideCount = questState.getQuestItemsCount(MUCROKIAN_HIDE);
        	if (exchangeID == EXCHANGE_REC_MOIRAI_MAGE_MUCROKIAN_HIDE_COUNT && mucrokianHideCount >= EXCHANGE_REC_MOIRAI_MAGE_MUCROKIAN_HIDE_COUNT)
        	{
            		int currentRecipeIndex = getRandom(REC_MOIRAI_MAGE_REWARD_COUNT);
            
            		questState.takeItems(MUCROKIAN_HIDE, EXCHANGE_REC_MOIRAI_MAGE_MUCROKIAN_HIDE_COUNT);
            		questState.giveItems(REC_MOIRAI_MAGE[currentRecipeIndex], (1 * (int)Config.RATE_QUEST_REWARD_RECIPE));
            		questState.playSound("ItemSound.quest_finish");
            
            		resultHtmlText = "32647-14.htm";
        	}
        	else if (exchangeID == EXCHANGE_PART_MOIRAI_MAGE_MUCROKIAN_HIDE_COUNT && mucrokianHideCount >= EXCHANGE_PART_MOIRAI_MAGE_MUCROKIAN_HIDE_COUNT)
        	{
            		int currentPartIndex = getRandom(PART_MOIRAI_MAGE_REWARD_COUNT);
            
            		int minCountWithQuestRewardMultiplier = PART_MOIRAI_MAGE_MIN_REWARD_ITEM_COUNT * (int)Config.RATE_QUEST_REWARD_MATERIAL;
            		int maxCountWithQuestRewardMultiplier = PART_MOIRAI_MAGE_MAX_REWARD_ITEM_COUNT * (int)Config.RATE_QUEST_REWARD_MATERIAL;
            		int currentPartCount = getRandom(minCountWithQuestRewardMultiplier, maxCountWithQuestRewardMultiplier);
            
            		questState.takeItems(MUCROKIAN_HIDE, EXCHANGE_PART_MOIRAI_MAGE_MUCROKIAN_HIDE_COUNT);
            		questState.giveItems(PART_MOIRAI_MAGE[currentPartIndex], currentPartCount);
            		questState.playSound("ItemSound.quest_finish");
            
            		resultHtmlText = "32647-14.htm";
        	}
        
        	return resultHtmlText;
    	}

   	@Override
    	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
         	
             	if (event.equalsIgnoreCase("32647-05.htm"))
            	{
                	st.set("cond", "1");
                	st.setState(State.STARTED);
                	st.playSound("ItemSound.quest_accept");
            	}
            	else if (event.equalsIgnoreCase("32647-12.htm") || event.equalsIgnoreCase("32647-07.htm"))
            	{
               	 	st.exitQuest(true);
                	st.playSound("ItemSound.quest_finish");
            	}
            	else if (event.equalsIgnoreCase("claimreward"))
            	{
                	htmltext = "32647-09.htm";
            	}
            	else
            	{
                	int exchangeID = 0;
               		try
                	{
                    		exchangeID = Integer.parseInt(event);
                	}
                	catch (Exception e)
                	{
                    		exchangeID = 0;
                	}
                
                	if (exchangeID > 0)
                    		htmltext = onExchangeRequest(st, exchangeID);
        	}
        	return htmltext;
   	}

    	@Override
    	public String onTalk(L2Npc npc, L2PcInstance talker)
    	{
        	String htmltext = getNoQuestMsg(talker);
        	QuestState questState = talker.getQuestState(qn);
        	if (questState != null)
        	{
            		int currentQuestCondition = questState.getInt("cond");

            		QuestState reedFieldMaintenanceState = talker.getQuestState("_308_ReedFieldMaintenance");
            		if (reedFieldMaintenanceState != null && reedFieldMaintenanceState.getState() == State.STARTED)
            		{
                		htmltext = "32647-15.htm";
            		}
            		else if (currentQuestCondition == 0)
            		{
                		if (talker.getLevel() < 82)
                		{
                   		 	htmltext = "32647-00.htm";
                    			questState.exitQuest(true);                    
                		}
                		else
                		{
                    			htmltext = "32647-01.htm";
                		}
            		}
            		else if (State.STARTED == questState.getState())
            		{
                		if (questState.getQuestItemsCount(MUCROKIAN_HIDE) >= 1 || questState.getQuestItemsCount(FALLEN_MUCROKIAN_HIDE) >= 1)
                    			htmltext = "32647-08.htm";
                		else
                    			htmltext = "32647-06.htm";
            		}
        	}
        	return htmltext;
    	}

    	@Override
    	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
    	{
        	L2PcInstance partyMember = getRandomPartyMember(player, 1);
        	if (null == partyMember)
            		return null;
            
        	QuestState questState = partyMember.getQuestState(qn);
        	if (null == questState)
            		return null;

        	int killedNPCID = npc.getId();

        	int itemIDToGive = 0;
        	int itemCountToGive = 1;
        	if (CHANGED_MUCROKIAN == killedNPCID && getRandom(100) < (FALLEN_HIDE_CHANCE * Config.RATE_QUEST_DROP))
        	{
            		itemIDToGive = FALLEN_MUCROKIAN_HIDE;
        	}
        	else
        	{
            		boolean containsKilledNPC = false;
            		for (int currentNPCID : MUCROKIANS)
            		{
                		if (currentNPCID == killedNPCID)
                		{
                    			containsKilledNPC = true;
                    			break;
                		}
            		}

            		if (containsKilledNPC && getRandom(100) < (MUCROKIAN_HIDE_CHANCE * Config.RATE_QUEST_DROP))
                		itemIDToGive = MUCROKIAN_HIDE;
        	}
            
        	if (itemIDToGive > 0)
        	{
            		questState.giveItems(itemIDToGive, itemCountToGive);
            		questState.playSound("ItemSound.quest_itemget");
        	}
        	return null;
    	}

    	public static void main(String[] args)
	{
        	new _309_ForAGoodCause(309, qn, "");
    	}
}