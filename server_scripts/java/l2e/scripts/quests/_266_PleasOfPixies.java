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
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 01.01.2013
 * Based on L2J Eternity-World
 */
public class _266_PleasOfPixies extends Quest
{
  	private static final String qn = "_266_PleasOfPixies";

  	private static final int PREDATORS_FANG = 1334;
  	private static final int GLASS_SHARD = 1336;
  	private static final int EMERALD = 1337;
  	private static final int BLUE_ONYX = 1338;
  	private static final int ONYX = 1339;

  	public _266_PleasOfPixies(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(31852);
    		addTalkId(31852);

    		addKillId(20525, 20530, 20534, 20537);

   		questItemIds = new int[] { PREDATORS_FANG };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}

    		if (event.equalsIgnoreCase("31852-03.htm"))
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
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
		{
			return htmltext;
		}

    		switch (st.getState())
    		{
    			case State.CREATED:
      				if (player.getRace() != Race.Elf)
      				{
        				htmltext = "31852-00.htm";
        				st.exitQuest(true);
      				}
      				else if (player.getLevel() >= 3)
      				{
        				htmltext = "31852-02.htm";
      				}
      				else
      				{
        				htmltext = "31852-01.htm";
        				st.exitQuest(true);
      				}
      				break;
    			case State.STARTED:
      				if (st.getQuestItemsCount(PREDATORS_FANG) < 100)
      				{
        				htmltext = "31852-04.htm";
      				}
      				else
      				{
        				htmltext = "31852-05.htm";
        				st.takeItems(PREDATORS_FANG, -1);

        				int n = getRandom(100);
        				if (n < 10)
        				{
          					st.rewardItems(EMERALD, 1);
          					st.playSound("ItemSound.quest_jackpot");
        				}
        				else if (n < 30)
        				{
          					st.rewardItems(BLUE_ONYX, 1);
        				}
        				else if (n < 60)
        				{
          					st.rewardItems(ONYX, 1);
        				}
        				else
        				{
          					st.rewardItems(GLASS_SHARD, 1);
        				}
        				st.playSound("ItemSound.quest_finish");
        				st.exitQuest(true);
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
		{
			return null;
		}

		if ((st.getInt("cond") == 1) && (st.dropQuestItems(PREDATORS_FANG, getRandom(1, 3), 100, 900000, true)))
    		{
      			st.set("cond", "2");
    		}
    		return null;
  	}

  	public static void main(String[] args)
  	{
    		new _266_PleasOfPixies(266, qn, "");
  	}
}