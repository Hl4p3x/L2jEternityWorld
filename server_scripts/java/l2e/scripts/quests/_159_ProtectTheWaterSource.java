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
 * Created by LordWinter 30.06.2012
 * Based on L2J Eternity-World
 */
public class _159_ProtectTheWaterSource extends Quest
{
  	private static final String qn = "_159_ProtectTheWaterSource";

  	private static final int ASTERIOS = 30154;
  	private static final int PLAGUE_ZOMBIE = 27017;

  	private static final int HYACINTH_CHARM1 = 1071;
  	private static final int HYACINTH_CHARM2 = 1072;
  	private static final int PLAGUE_DUST = 1035;

  	public _159_ProtectTheWaterSource(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(ASTERIOS);
    		addTalkId(ASTERIOS);

    		addKillId(PLAGUE_ZOMBIE);

    		questItemIds = new int[] { PLAGUE_DUST, HYACINTH_CHARM1, HYACINTH_CHARM2 };
  	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

    		if (event.equalsIgnoreCase("30154-04.htm"))
    		{
      			st.set("cond", "1");
      			st.setState(State.STARTED);
      			st.playSound("ItemSound.quest_accept");
      			st.giveItems(HYACINTH_CHARM1, 1);
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
        				if ((player.getLevel() >= 12) && (player.getLevel() <= 18))
          					htmltext = "30154-03.htm";
        				else
					{
          					htmltext = "30154-02.htm";
          					st.exitQuest(true);
        				}
      				}
      				else
      				{
        				htmltext = "30154-00.htm";
        				st.exitQuest(true);
      				}
      				break;
    			case State.STARTED:
      				if (cond == 1)
        				htmltext = "30154-05.htm";
				else if (cond == 2)
      				{
        				st.set("cond", "3");
        				htmltext = "30154-06.htm";
        				st.takeItems(PLAGUE_DUST, -1);
        				st.takeItems(HYACINTH_CHARM1, -1);
        				st.giveItems(HYACINTH_CHARM2, 1);
        				st.playSound("ItemSound.quest_middle");
     				}
      				else if (cond == 3)
        				htmltext = "30154-07.htm";
				else if (cond == 4)
				{
        				htmltext = "30154-08.htm";
        				st.takeItems(PLAGUE_DUST, -1);
        				st.takeItems(HYACINTH_CHARM2, -1);
        				st.rewardItems(57, 18250L);
        				st.playSound("ItemSound.quest_finish");
					st.unset("cond");
        				st.exitQuest(false);
				}
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

    		int cond = st.getInt("cond");
    		int count = (int)st.getQuestItemsCount(PLAGUE_DUST);

    		if ((cond == 1) && (st.getRandom(10) < 4))
    		{
      			st.set("cond", "2");
      			st.playSound("ItemSound.quest_middle");
      			st.giveItems(PLAGUE_DUST, 1);
    		}
    		else if ((cond == 3) && (st.getRandom(10) < 4) && (count < 5))
    		{
      			if (count == 4)
      			{
        			st.playSound("ItemSound.quest_middle");
        			st.set("cond", "4");
     			}
      			else
        			st.playSound("ItemSound.quest_itemget");

      			st.giveItems(PLAGUE_DUST, 1);
    		}
    		return null;
  	}

  	public static void main(String[] args)
  	{
    		new _159_ProtectTheWaterSource(159, qn, "");
  	}
}