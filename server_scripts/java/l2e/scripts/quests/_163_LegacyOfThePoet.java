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
 * Created by LordWinter 28.06.2012
 * Based on L2J Eternity-World
 */
public class _163_LegacyOfThePoet extends Quest
{
  	private static final String qn = "_163_LegacyOfThePoet";

  	private static final int STARDEN = 30220;

  	private static final int RUMIELS_POEM_1 = 1038;
  	private static final int RUMIELS_POEM_2 = 1039;
  	private static final int RUMIELS_POEM_3 = 1040;
  	private static final int RUMIELS_POEM_4 = 1041;

  	public _163_LegacyOfThePoet(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(STARDEN);
    		addTalkId(STARDEN);

    		addKillId(new int[] { 20372, 20373 });

    		questItemIds = new int[] { RUMIELS_POEM_1, RUMIELS_POEM_2, RUMIELS_POEM_3, RUMIELS_POEM_4 };
  	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

    		if (event.equalsIgnoreCase("30220-07.htm"))
    		{
      			st.set("cond", "1");
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

    		switch (st.getState())
    		{
    			case State.CREATED:
      				if (player.getRace().ordinal() == 2)
      				{
        				htmltext = "30220-00.htm";
        				st.exitQuest(true);
      				}
      				else if ((player.getLevel() >= 11) && (player.getLevel() <= 15))
        				htmltext = "30220-03.htm";
      				else
				{
        				htmltext = "30220-02.htm";
        				st.exitQuest(true);
      				}
      				break;
    			case State.STARTED:
      				if ((st.getQuestItemsCount(RUMIELS_POEM_1) == 1) && (st.getQuestItemsCount(RUMIELS_POEM_2) == 1) && (st.getQuestItemsCount(RUMIELS_POEM_3) == 1) && (st.getQuestItemsCount(RUMIELS_POEM_4) == 1))
      				{
        				htmltext = "30220-09.htm";
        				st.takeItems(RUMIELS_POEM_1, 1);
        				st.takeItems(RUMIELS_POEM_2, 1);
        				st.takeItems(RUMIELS_POEM_3, 1);
        				st.takeItems(RUMIELS_POEM_4, 1);
        				st.rewardItems(57, 13890);
        				st.addExpAndSp(21643, 943);
        				st.exitQuest(false);
        				st.playSound("ItemSound.quest_finish");
      				}
      				else
        				htmltext = "30220-08.htm";
     				break;
    			case State.COMPLETED:
      				htmltext = getAlreadyCompletedMsg(player);
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

    		if (st.getInt("cond") == 1)
    		{
			if ((st.getRandom(10) == 0) && (st.getQuestItemsCount(RUMIELS_POEM_1) == 0))
      			{
        			st.giveItems(RUMIELS_POEM_1, 1);
        			st.playSound("ItemSound.quest_itemget");
      			}
      			else if ((st.getRandom(10) > 7) && (st.getQuestItemsCount(RUMIELS_POEM_2) == 0))
      			{
        			st.giveItems(RUMIELS_POEM_2, 1);
        			st.playSound("ItemSound.quest_itemget");
      			}
      			else if ((st.getRandom(10) > 7) && (st.getQuestItemsCount(RUMIELS_POEM_3) == 0))
      			{
        			st.giveItems(RUMIELS_POEM_3, 1);
        			st.playSound("ItemSound.quest_itemget");
      			}
      			else if ((st.getRandom(10) > 5) && (st.getQuestItemsCount(RUMIELS_POEM_4) == 0))
      			{
        			st.giveItems(RUMIELS_POEM_4, 1);
        			st.playSound("ItemSound.quest_itemget");
      			}

      			if (st.getQuestItemsCount(RUMIELS_POEM_1) + st.getQuestItemsCount(RUMIELS_POEM_2) + st.getQuestItemsCount(RUMIELS_POEM_3) + st.getQuestItemsCount(RUMIELS_POEM_4) == 4)
      			{
        			st.set("cond", "2");
        			st.playSound("ItemSound.quest_middle");
      			}
    		}
    		return null;
  	}

  	public static void main(String[] args)
  	{
    		new _163_LegacyOfThePoet(163, qn, "");
  	}
}