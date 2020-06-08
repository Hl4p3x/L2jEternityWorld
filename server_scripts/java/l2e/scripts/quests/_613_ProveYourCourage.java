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
public class _613_ProveYourCourage extends Quest
{
  	private static final String qn = "_613_ProveYourCourage";

  	private static final int HEKATON_HEAD = 7240;
  	private static final int VALOR_FEATHER = 7229;
  	private static final int VARKA_ALLIANCE_THREE = 7223;

  	public _613_ProveYourCourage(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(31377);
    		addTalkId(31377);

    		addKillId(25299);

		questItemIds = new int[] { HEKATON_HEAD };
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
      			if (st.getQuestItemsCount(HEKATON_HEAD) == 1)
      			{
        			st.takeItems(HEKATON_HEAD, -1);
        			st.giveItems(VALOR_FEATHER, 1);
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
				htmltext = (player.getLevel() >= 75) ? (st.hasQuestItems(VARKA_ALLIANCE_THREE)) ? "31377-01.htm" : "31377-02.htm" : "31377-03.htm";
      				break;
    			case State.STARTED:
      				if (st.getQuestItemsCount(HEKATON_HEAD) == 1)
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
		final QuestState st = player.getQuestState(getName());
		if ((st != null) && st.isCond(1) && Util.checkIfInRange(1500, npc, player, false))
		{
			st.giveItems(HEKATON_HEAD, 1);
			st.setCond(2, true);
		}
	}

  	public static void main(String[] args)
  	{
    		new _613_ProveYourCourage(613, qn, "");
  	}
}