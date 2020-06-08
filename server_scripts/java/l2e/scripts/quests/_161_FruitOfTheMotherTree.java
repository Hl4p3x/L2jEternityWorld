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

public class _161_FruitOfTheMotherTree extends Quest
{
  	private static final String qn = "_161_FruitOfTheMotherTree";

  	private static final int ANDELLIA = 30362;
  	private static final int THALIA = 30371;

  	private static final int ANDELLIA_LETTER = 1036;
  	private static final int MOTHERTREE_FRUIT = 1037;

  	public _161_FruitOfTheMotherTree(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(ANDELLIA);
    		addTalkId(ANDELLIA);
    		addTalkId(THALIA);

    		questItemIds = new int[] { ANDELLIA_LETTER, MOTHERTREE_FRUIT };
  	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

    		if (event.equalsIgnoreCase("30362-04.htm"))
    		{
      			st.set("cond", "1");
      			st.setState(State.STARTED);
      			st.giveItems(ANDELLIA_LETTER, 1);
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

      		int cond = st.getInt("cond");

    		switch (st.getState())
    		{
    			case State.CREATED:
      				if (player.getRace().ordinal() == 1)
      				{
        				if ((player.getLevel() >= 3) && (player.getLevel() <= 7))
          					htmltext = "30362-03.htm";
        				else
					{
          					htmltext = "30362-02.htm";
          					st.exitQuest(true);
        				}
      				}
      				else
      				{
        				htmltext = "30362-00.htm";
        				st.exitQuest(true);
      				}
      				break;
    			case State.STARTED:
      				switch (npc.getId())
      				{
      					case ANDELLIA:
        					if (cond == 1)
          						htmltext = "30362-05.htm";
						else if ((st.getQuestItemsCount(MOTHERTREE_FRUIT) == 1))
						{
          						htmltext = "30362-06.htm";
          						st.takeItems(MOTHERTREE_FRUIT, 1);
          						st.rewardItems(57, 1000);
          						st.addExpAndSp(1000, 0);
							st.unset("cond");
          						st.exitQuest(false);
          						st.playSound("ItemSound.quest_finish");
						}
						break;
      					case THALIA:
        					if ((cond == 1) && (st.getQuestItemsCount(ANDELLIA_LETTER) == 1))
        					{
          						htmltext = "30371-01.htm";
          						st.takeItems(ANDELLIA_LETTER, 1);
          						st.giveItems(MOTHERTREE_FRUIT, 1);
          						st.set("cond", "2");
          						st.playSound("ItemSound.quest_middle");
        					}
						else if ((cond == 2) && (st.getQuestItemsCount(MOTHERTREE_FRUIT) == 1))
          						htmltext = "30371-02.htm";
      				}
      				break;
    			case State.COMPLETED:
      				htmltext = getAlreadyCompletedMsg(player);
				break;
    		}
    		return htmltext;
  	}

  	public static void main(String[] args)
  	{
    		new _161_FruitOfTheMotherTree(161, qn, "");
  	}
}