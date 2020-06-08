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
public class _103_SpiritOfCraftsman extends Quest
{
	private static final String qn = "_103_SpiritOfCraftsman";
	
	// ALL ITEMS
	private static final int KAROYDS_LETTER_ID 			= 968;
	private static final int CECKTINONS_VOUCHER1_ID 		= 969;
	private static final int CECKTINONS_VOUCHER2_ID 		= 970;
	private static final int BONE_FRAGMENT1_ID 			= 1107;
	private static final int SOUL_CATCHER_ID 			= 971;
	private static final int PRESERVE_OIL_ID 			= 972;
	private static final int ZOMBIE_HEAD_ID 			= 973;
	private static final int STEELBENDERS_HEAD_ID 			= 974;
	private static final int BLOODSABER_ID 				= 975;
	
	// NEWBIE REWARD
	private static final int SPIRITSHOT_NO_GRADE_FOR_BEGINNERS 	= 5790;
	private static final int SOULSHOT_FOR_BEGINNERS 		= 5789;
	private final static int SPIRITSHOT_NO_GRADE 			= 2509;
	private final static int SOULSHOT_NO_GRADE 			= 1835;

	public _103_SpiritOfCraftsman(int id, String name, String desc)
	{
		super(id,name,desc);
		
		addStartNpc(30307);
		addTalkId(30307);
		addTalkId(30132);
		addTalkId(30144);

		addKillId(20015);
		addKillId(20020);
		addKillId(20455);
		addKillId(20517);
		addKillId(20518);

		questItemIds = new int[] { KAROYDS_LETTER_ID, CECKTINONS_VOUCHER1_ID, CECKTINONS_VOUCHER2_ID, BONE_FRAGMENT1_ID, SOUL_CATCHER_ID, PRESERVE_OIL_ID, ZOMBIE_HEAD_ID, STEELBENDERS_HEAD_ID };
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{		
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

	    	if (event.equalsIgnoreCase("30307-05.htm"))
	    	{
	        	st.set("cond","1");
	        	st.setState(State.STARTED);
	        	st.playSound("ItemSound.quest_accept");
	        	st.giveItems(KAROYDS_LETTER_ID,1);
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

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		else if(npcId == 30008 && id == State.CREATED)
		{
			if (player.getRace().ordinal() != 2)
				htmltext = "30307-00.htm";
			else if (player.getLevel() >= 10)
				htmltext = "30307-03.htm";
			else
			{
				st.exitQuest(true);
				htmltext = "30307-02.htm";
			}

		}
		else if(id == State.STARTED)
		{
			if (npcId == 30307 && st.getInt("cond") >= 1 && (st.getQuestItemsCount(KAROYDS_LETTER_ID) >=1 || st.getQuestItemsCount(CECKTINONS_VOUCHER1_ID) >=1 || st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID) >= 1))
				htmltext = "30307-06.htm";
			else if (npcId == 30132 && st.getInt("cond") == 1 && st.getQuestItemsCount(KAROYDS_LETTER_ID) == 1)
			{
            			htmltext = "30132-01.htm";
            			st.set("cond","2");
            			st.playSound("ItemSound.quest_middle") ;
            			st.takeItems(KAROYDS_LETTER_ID,1);
            			st.giveItems(CECKTINONS_VOUCHER1_ID,1);
			}
      			else if (npcId == 30132 && st.getInt("cond") >= 2 && st.getQuestItemsCount(CECKTINONS_VOUCHER1_ID) >= 1 || st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID) >= 1)
            			htmltext = "30132-02.htm";
       			else if (npcId == 30144 && st.getInt("cond") == 2 && st.getQuestItemsCount(CECKTINONS_VOUCHER1_ID) >= 1)
			{
            			htmltext = "30144-01.htm";
            			st.set("cond","3");
            			st.playSound("ItemSound.quest_middle");
            			st.takeItems(CECKTINONS_VOUCHER1_ID,1);
            			st.giveItems(CECKTINONS_VOUCHER2_ID,1);
			}
       			else if (npcId == 30144 && st.getInt("cond") == 3 && st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID) >= 1 && st.getQuestItemsCount(BONE_FRAGMENT1_ID) < 10)
            			htmltext = "30144-02.htm";
       			else if (npcId == 30144 && st.getInt("cond") == 3 && st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID) == 1 && st.getQuestItemsCount(BONE_FRAGMENT1_ID) >= 10)
			{
            			htmltext = "30144-03.htm";
            			st.set("cond","5");
            			st.playSound("ItemSound.quest_middle");
            			st.takeItems(CECKTINONS_VOUCHER2_ID,1);
            			st.takeItems(BONE_FRAGMENT1_ID,10);
            			st.giveItems(SOUL_CATCHER_ID,1);
			}
       			else if (npcId == 30144 && st.getInt("cond") == 5 && st.getQuestItemsCount(SOUL_CATCHER_ID) == 1)
            			htmltext = "30144-04.htm";
       			else if (npcId == 30132 && st.getInt("cond") == 5 && st.getQuestItemsCount(SOUL_CATCHER_ID) == 1)
			{
            			htmltext = "30132-03.htm";
            			st.set("cond","6");
            			st.playSound("ItemSound.quest_middle");
            			st.takeItems(SOUL_CATCHER_ID,1);
            			st.giveItems(PRESERVE_OIL_ID,1);
			}
       			else if (npcId == 30132 && st.getInt("cond") == 6 && st.getQuestItemsCount(PRESERVE_OIL_ID) == 1 && st.getQuestItemsCount(ZOMBIE_HEAD_ID) == 0 && st.getQuestItemsCount(STEELBENDERS_HEAD_ID) == 0)
            			htmltext = "30132-04.htm";
      			else if (npcId == 30132 && st.getInt("cond") == 6 && st.getQuestItemsCount(ZOMBIE_HEAD_ID) == 1)
			{
            			htmltext = "30132-05.htm";
            			st.set("cond","8");
            			st.playSound("ItemSound.quest_middle"); 
            			st.takeItems(ZOMBIE_HEAD_ID,1);
            			st.giveItems(STEELBENDERS_HEAD_ID,1);
			}
       			else if (npcId == 30132 && st.getInt("cond") == 8 && st.getQuestItemsCount(STEELBENDERS_HEAD_ID) == 1)
            			htmltext = "30132-06.htm";
       			else if (npcId == 30307 && st.getInt("cond") == 8 && st.getQuestItemsCount(STEELBENDERS_HEAD_ID) == 1)
			{
            			htmltext = "30307-07.htm";
            			st.giveItems(57, 19799);
	    			st.addExpAndSp(46663,3999);
            			st.takeItems(STEELBENDERS_HEAD_ID,1);
            			st.giveItems(BLOODSABER_ID, 1);
            			st.giveItems(1060, 100);
	        		st.giveItems(1060, 100);
	        		st.giveItems(4412, 10);
	        		st.giveItems(4413, 10);
	        		st.giveItems(4414, 10);
	        		st.giveItems(4415, 10);
	        		st.giveItems(4416, 10);
	        		st.giveItems(4417, 10);
            			if (!player.getClassId().isMage())
				{
                			st.playTutorialVoice("tutorial_voice_027");
                			st.giveItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS,3000);
					st.giveItems(SPIRITSHOT_NO_GRADE, 500);
				}
            			else 
				{
                			st.playTutorialVoice("tutorial_voice_026");
                			st.giveItems(SOULSHOT_FOR_BEGINNERS,6000);
					st.giveItems(SOULSHOT_NO_GRADE, 1000);
                			player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message3", player.getLang())).toString()), 3000));
				}
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
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		if (st.getState() == State.STARTED)
		{
			int npcId = npc.getId();
			if (npcId == 20517 || npcId == 20518 || npcId == 20455)
			{
				if(st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID) == 1 && st.getQuestItemsCount(BONE_FRAGMENT1_ID) < 10)
				{
         				if (getRandom(10) < 3)
					{
						st.giveItems(BONE_FRAGMENT1_ID, 1);
						if(st.getQuestItemsCount(BONE_FRAGMENT1_ID) == 10)
						{
							st.playSound("ItemSound.quest_itemget");
							st.set("cond", "4");
						}
						else
							st.playSound("ItemSound.quest_itemget");
					}
				}
			}
  			else if (npcId == 20015 || npcId == 20020)
			{
      				if (st.getQuestItemsCount(PRESERVE_OIL_ID) == 1)
				{
         				if (getRandom(10) < 3)
					{
            					st.set("cond","7");
            					st.giveItems(ZOMBIE_HEAD_ID,1);
            					st.playSound("ItemSound.quest_middle");
            					st.takeItems(PRESERVE_OIL_ID,1);
					}
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _103_SpiritOfCraftsman(103, qn, "");
	}
}