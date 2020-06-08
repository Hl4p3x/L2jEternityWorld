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
public class _106_ForgottenTruth extends Quest
{
	private static final String qn = "_106_ForgottenTruth";

	// QuestItem
	private static final int ONYX_TALISMAN1 	= 984;
	private static final int ONYX_TALISMAN2 	= 985;
	private static final int ANCIENT_SCROLL 	= 986;
	private static final int ANCIENT_CLAY_TABLET 	= 987;
	private static final int KARTAS_TRANSLATION 	= 988;
	private static final int ELDRITCH_DAGGER 	= 989;

	// REWARDS
	private final static int SPIRITSHOT_FOR_BEGINNERS = 5790;
	private final static int SOULSHOT_FOR_BEGINNERS   = 5789;

	public _106_ForgottenTruth(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(30358);
		addTalkId(30358);
		addTalkId(30133);

                addKillId(27070);

		questItemIds = new int[] { ONYX_TALISMAN1, ONYX_TALISMAN2, ANCIENT_SCROLL, ANCIENT_CLAY_TABLET, KARTAS_TRANSLATION };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{		
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("30358-05.htm"))
		{
        		st.giveItems(ONYX_TALISMAN1,1);
        		st.set("cond","1");
        		st.setState(State.STARTED);
        		st.playSound("ItemSound.quest_accept");
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

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		int npcId = npc.getId();
		int cond = st.getInt("cond");
		int id = st.getState();

   		if (id == State.CREATED)
		{
     			if (player.getRace().ordinal() == 2)
			{
       				if (player.getLevel() >= 10)
         				htmltext = "30358-03.htm";
       				else
				{
         				htmltext = "30358-02.htm";
         				st.exitQuest(true);
				}
			}
     			else
			{
       				htmltext = "30358-00.htm";
       				st.exitQuest(true);
			}
		}
   		else if (id == State.STARTED)
		{
     			if (cond == 1)
			{
       				if (npcId == 30358)
         				htmltext = "30358-06.htm";
	
       				else if (npcId == 30133 && st.getQuestItemsCount(ONYX_TALISMAN1) > 0)
				{
         				htmltext = "30133-01.htm";
         				st.takeItems(ONYX_TALISMAN1,1);
         				st.giveItems(ONYX_TALISMAN2,1);
         				st.set("cond","2");
         				st.playSound("ItemSound.quest_middle");
				}
			}
     			else if (cond == 2)
			{
				if (npcId == 30358)
         				htmltext = "30358-06.htm";
       				else if (npcId == 30133)
         				htmltext = "30133-02.htm";
			}
     			else if (cond == 3)
			{
				if (npcId == 30358)
         				htmltext = "30358-06.htm";
       				else if (npcId == 30133 && st.getQuestItemsCount(ANCIENT_SCROLL) > 0 && st.getQuestItemsCount(ANCIENT_CLAY_TABLET) > 0)
				{
         				htmltext = "30133-03.htm";
                  			st.takeItems(ONYX_TALISMAN2,1);
                  			st.takeItems(ANCIENT_SCROLL,1);
                  			st.takeItems(ANCIENT_CLAY_TABLET,1);
                  			st.giveItems(KARTAS_TRANSLATION,1);
                  			st.set("cond","4");
                  			st.playSound("ItemSound.quest_middle");
				}
			} 
     			else if (cond == 4)
			{
				if (npcId == 30358 && st.getQuestItemsCount(KARTAS_TRANSLATION) > 0)
				{
         				htmltext = "30358-07.htm";
         				st.takeItems(KARTAS_TRANSLATION,1);
         				st.giveItems(ELDRITCH_DAGGER, 1);
         				st.giveItems(1060, 100);
         				st.addExpAndSp(24195, 2074);
         				for(int ECHO_CHRYTSAL = 4412; ECHO_CHRYTSAL <= 4417; ECHO_CHRYTSAL++)
						st.giveItems(ECHO_CHRYTSAL, 10);
         				if (!player.getClassId().isMage())
					{
               					st.playTutorialVoice("tutorial_voice_027");
               					st.giveItems(SPIRITSHOT_FOR_BEGINNERS,3000);
            					st.giveItems(2509, 500);
						player.sendPacket(new ExShowScreenMessage(((new CustomMessage("Newbie.Message3", player.getLang())).toString()), 3000));
					}
         				else
					{
               					st.playTutorialVoice("tutorial_voice_026");
            					st.giveItems(1835, 1000);
               					st.giveItems(SOULSHOT_FOR_BEGINNERS,6000);
					}
         				st.unset("cond");
         				st.exitQuest(false);
         				st.playSound("ItemSound.quest_finish");
				}
       				else if (npcId == 30133)
         				htmltext = "30133-04.htm";
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

		int cond = st.getInt("cond");

   		if (cond == 2)
		{
     			if (getRandom(100) < 20)
			{
       				if (st.getQuestItemsCount(ANCIENT_SCROLL) == 0)
				{
         				st.giveItems(ANCIENT_SCROLL,1);
         				st.playSound("Itemsound.quest_itemget");
				}
       				else if (st.getQuestItemsCount(ANCIENT_CLAY_TABLET) == 0)
				{
         				st.giveItems(ANCIENT_CLAY_TABLET,1);
         				st.playSound("ItemSound.quest_middle");
         				st.set("cond","3");
				}
			}
		}
		return null;
	}

	public static void main(String[] args)
    	{
    		new _106_ForgottenTruth(106, qn, "");
    	}
}