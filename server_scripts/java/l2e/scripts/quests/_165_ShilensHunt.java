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

public class _165_ShilensHunt extends Quest
{
  	private static final String qn = "_165_ShilensHunt";

  	private static final int NELSYA = 30348;

  	private static final int DARK_BEZOAR = 1160;
  	private static final int LESSER_HEALING_POTION = 1060;

  	public _165_ShilensHunt(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(NELSYA);
    		addTalkId(NELSYA);

    		addKillId(new int[] { 20456, 20529, 20532, 20536 });

    		questItemIds = new int[] { DARK_BEZOAR };
  	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

    		if (event.equalsIgnoreCase("30348-03.htm"))
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
        				if ((player.getLevel() >= 3) && (player.getLevel() <= 7))
          					htmltext = "30348-02.htm";
        				else
					{
         	 				htmltext = "30348-01.htm";
          					st.exitQuest(true);
        				}
      				}
      				else
      				{
        				htmltext = "30348-00.htm";
        				st.exitQuest(true);
      				}
      				break;
    			case State.STARTED:
      				if (st.getQuestItemsCount(DARK_BEZOAR) >= 13)
      				{
        				htmltext = "30348-05.htm";
        				st.takeItems(DARK_BEZOAR, -1);
        				st.rewardItems(LESSER_HEALING_POTION, 5);
        				st.addExpAndSp(1000, 0);
        				st.exitQuest(false);
        				st.playSound("ItemSound.quest_finish");
      				}
      				else
        				htmltext = "30348-04.htm";
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

    		if ((st.getInt("cond") == 1) && (st.getRandom(10) < 2))
    		{
      			st.giveItems(DARK_BEZOAR, 1);
      			if (st.getQuestItemsCount(DARK_BEZOAR) == 13)
      			{
        			st.set("cond", "2");
        			st.playSound("ItemSound.quest_middle");
      			}
      			else
        			st.playSound("ItemSound.quest_itemget");
    		}
    		return null;
  	}

  	public static void main(String[] args)
  	{
    		new _165_ShilensHunt(165, qn, "");
  	}
}