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
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 12.01.2013 Based on L2J Eternity-World
 */
public class _614_SlayTheEnemyCommander extends Quest
{
  	private static final String qn = "_614_SlayTheEnemyCommander";

  	private static final int TAYR_HEAD = 7241;
  	private static final int WISDOM_FEATHER = 7230;
  	private static final int VARKA_ALLIANCE_FOUR = 7224;

  	public _614_SlayTheEnemyCommander(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(31377);
    		addTalkId(31377);

    		addKillId(25302);

		questItemIds = new int[] { TAYR_HEAD };
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

    		if (event.equalsIgnoreCase("31377-04.htm"))
    		{
          		st.startQuest();
    		}
    		else if (event.equalsIgnoreCase("31377-07.htm"))
    		{
      			if (st.getQuestItemsCount(TAYR_HEAD) == 1)
      			{
        			st.takeItems(TAYR_HEAD, -1);
        			st.giveItems(WISDOM_FEATHER, 1);
        			st.addExpAndSp(10000, 0);
        			st.playSound("ItemSound.quest_finish");
        			st.exitQuest(true);
      			}
      			else
      			{
        			htmltext = "31377-06.htm";
        			st.set("cond", "1");
        			st.playSound("ItemSound.quest_accept");
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
		{
			return htmltext;
		}

    		switch (st.getState())
    		{
    			case State.CREATED:
				htmltext = (player.getLevel() >= 75) ? (st.hasQuestItems(VARKA_ALLIANCE_FOUR)) ? "31377-01.htm" : "31377-02.htm" : "31377-03.htm";
      				break;
    			case State.STARTED:
      				if (st.getQuestItemsCount(TAYR_HEAD) > 0)
      				{
        				htmltext = "31377-05.htm";
      				}
      				else
      				{
        				htmltext = "31377-06.htm";
      				}
      				break;
    		}
    		return htmltext;
  	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, false);
		return super.onKill(npc, player, isSummon);
  	}

	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		final QuestState st = player.getQuestState(qn);
		if ((st != null) && st.isCond(1) && Util.checkIfInRange(1500, npc, player, false))
		{
			st.giveItems(TAYR_HEAD, 1);
			st.setCond(2, true);
		}
	}

  	public static void main(String[] args)
  	{
    		new _614_SlayTheEnemyCommander(614, qn, "");
  	}
}