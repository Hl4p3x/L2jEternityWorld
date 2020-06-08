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

import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Created by LordWinter 09.08.2011
 * Based on L2J Eternity-World
 */
public class _101_SwordOfSolidarity extends Quest
{
	private static final String qn = "_101_SwordOfSolidarity";
	
	// ALL ITEMS
	private static final int ROIENS_LETTER 		= 796;
	private static final int HOWTOGO_RUINS 		= 937;
	private static final int BROKEN_SWORD_HANDLE 	= 739;
	private static final int BROKEN_BLADE_BOTTOM 	= 740;
	private static final int BROKEN_BLADE_TOP 	= 741;
	private static final int ALLTRANS_NOTE 		= 742;
	private static final int SWORD_OF_SOLIDARITY 	= 738;
	
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;

	public _101_SwordOfSolidarity(int id, String name, String desc)
	{
		super(id,name,desc);
		
		addStartNpc(30008);
		addTalkId(30008);
		addTalkId(30283);

		addKillId(20361);
		addKillId(20362);

		questItemIds = new int[] { ROIENS_LETTER, HOWTOGO_RUINS, BROKEN_SWORD_HANDLE, BROKEN_BLADE_BOTTOM, BROKEN_BLADE_TOP, ALLTRANS_NOTE };
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{		
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

	    	if (event.equalsIgnoreCase("30008-04.htm"))
	    	{
	        	st.set("cond","1");
	        	st.setState(State.STARTED);
	        	st.playSound("ItemSound.quest_accept");
	        	st.giveItems(ROIENS_LETTER,1);
	    	}
	    	else if (event.equalsIgnoreCase("30283-02.htm"))
	    	{
	        	st.set("cond","2");
	        	st.playSound("ItemSound.quest_middle");
	        	st.takeItems(ROIENS_LETTER,st.getQuestItemsCount(ROIENS_LETTER));
	        	st.giveItems(HOWTOGO_RUINS,1);
	    	}
	    	else if (event.equalsIgnoreCase("30283-07.htm"))
	    	{
	        	st.giveItems(57, 10981);
	        	st.takeItems(BROKEN_SWORD_HANDLE,-1);
	        	st.giveItems(SWORD_OF_SOLIDARITY,1);
	        	st.giveItems(1060, 100);
	        	st.giveItems(4412, 10);
	        	st.giveItems(4413, 10);
	        	st.giveItems(4414, 10);
	        	st.giveItems(4415, 10);
	        	st.giveItems(4416, 10);
	        	st.giveItems(4417, 10);
	        	st.addExpAndSp(25747, 2171);
	        	st.unset("cond");
	        	st.exitQuest(false);
	        	st.playSound("ItemSound.quest_finish");
	        	if (!player.getClassId().isMage())
	        	{
	            		st.giveItems(SOULSHOT_FOR_BEGINNERS, 7000);
	            		st.playTutorialVoice("tutorial_voice_026");
	        	}
			else
			{
	            		st.giveItems(SOULSHOT_FOR_BEGINNERS, 7000);
	            		st.playTutorialVoice("tutorial_voice_026");
	            		player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message3", player.getLang())).toString()), 3000));
			}
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

		int npcId = npc.getId();
		int id = st.getState();

		switch (id)
		{
			case State.COMPLETED: 
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:	
				if (npcId == 30008)
				{
					if (player.getRace().ordinal() != 0)
				        	htmltext = "30008-00.htm";
					else if (player.getLevel() >= 9)
				        	htmltext = "30008-02.htm";
					else
					{
				        	htmltext = "30008-08.htm";
				        	st.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				if (npcId == 30008)
				{
					if (st.getInt("cond") == 1 && st.getQuestItemsCount(ROIENS_LETTER) ==1 )
						htmltext = "30008-05.htm";
					else if (st.getInt("cond") >= 2 && st.getQuestItemsCount(ROIENS_LETTER)==0 && st.getQuestItemsCount(ALLTRANS_NOTE)==0)
					{
						if (st.getQuestItemsCount(BROKEN_BLADE_TOP) > 0 && st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) > 0)
							htmltext = "30008-12.htm";
						else if (st.getQuestItemsCount(BROKEN_BLADE_TOP) + st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) <= 1)
							htmltext = "30008-11.htm";
						else if (st.getQuestItemsCount(BROKEN_SWORD_HANDLE) > 0)
							htmltext = "30008-07.htm";
						else if (st.getQuestItemsCount(HOWTOGO_RUINS) == 1)
		            				htmltext = "30008-10.htm";
					}
					else if (st.getInt("cond") == 4 && st.getQuestItemsCount(ROIENS_LETTER) == 0 && st.getQuestItemsCount(ALLTRANS_NOTE) > 0)
					{
						htmltext = "30008-06.htm";
						st.set("cond","5");
						st.playSound("ItemSound.quest_middle");
						st.takeItems(ALLTRANS_NOTE,st.getQuestItemsCount(ALLTRANS_NOTE));
						st.giveItems(BROKEN_SWORD_HANDLE,1);
					}
				}
				else if (npcId == 30283)
				{
					if (st.getInt("cond")==1 && st.getQuestItemsCount(ROIENS_LETTER) > 0)
						htmltext = "30283-01.htm";
					else if (st.getInt("cond") >= 2 && st.getQuestItemsCount(ROIENS_LETTER) == 0 && st.getQuestItemsCount(HOWTOGO_RUINS) > 0)
					{
						if (st.getQuestItemsCount(BROKEN_BLADE_TOP) + st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) == 1)
							htmltext = "30283-08.htm";
						else if (st.getQuestItemsCount(BROKEN_BLADE_TOP) + st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) == 0)
							htmltext = "30283-03.htm";
						else if (st.getQuestItemsCount(BROKEN_BLADE_TOP) > 0 && st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) > 0 )
						{
							htmltext = "30283-04.htm";
							st.set("cond","4");
							st.playSound("ItemSound.quest_middle");
							st.takeItems(HOWTOGO_RUINS,st.getQuestItemsCount(HOWTOGO_RUINS));
							st.takeItems(BROKEN_BLADE_TOP,st.getQuestItemsCount(BROKEN_BLADE_TOP));
							st.takeItems(BROKEN_BLADE_BOTTOM,st.getQuestItemsCount(BROKEN_BLADE_BOTTOM));
							st.giveItems(ALLTRANS_NOTE,1);
						}
					}
					else if (st.getInt("cond") == 4 && st.getQuestItemsCount(ALLTRANS_NOTE) > 0)
						htmltext = "30283-05.htm";
					else if (st.getInt("cond") == 5 && st.getQuestItemsCount(BROKEN_SWORD_HANDLE) > 0)
						htmltext = "30283-06.htm";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		if (st.getState() == State.STARTED)
		{
			int npcId = npc.getId();
			if (npcId == 20361 || npcId == 20362)
			{
				if (st.getQuestItemsCount(HOWTOGO_RUINS) > 0)
				{
		             		if (st.getQuestItemsCount(BROKEN_BLADE_TOP) == 0)
		             		{
		            	 		if (getRandom(5) == 0)
		            	 		{
		            		 		st.giveItems(BROKEN_BLADE_TOP,1);
		            		 		st.playSound("ItemSound.quest_itemget");
		            	 		}
		             		}
		             		else if (st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) == 0)
		             		{
		                		if (getRandom(5) == 0)
		                		{
		                   			st.giveItems(BROKEN_BLADE_BOTTOM,1);
		                   			st.playSound("ItemSound.quest_itemget");
		                		}
		             		}
				}
				if (st.getQuestItemsCount(BROKEN_BLADE_TOP) > 0 && st.getQuestItemsCount(BROKEN_BLADE_BOTTOM) > 0)
				{
		             		st.set("cond","3");
		             		st.playSound("ItemSound.quest_middle");
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _101_SwordOfSolidarity(101, qn, "");
	}
}