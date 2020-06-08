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
 * Created by LordWinter 18.06.2012
 * Based on L2J Eternity-World
 */
public class _650_ABrokenDream extends Quest
{
  	private static final String qn = "_650_ABrokenDream";

  	private static final int GHOST = 32054;
  	private static final int DREAM_FRAGMENT = 8514;
 	private static final int CREWMAN = 22027;
  	private static final int VAGABOND = 22028;

  	public _650_ABrokenDream(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(GHOST);
    		addTalkId(GHOST);

    		addKillId(new int[] { CREWMAN, VAGABOND });

   		questItemIds = new int[] { DREAM_FRAGMENT };
  	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;

    		if (event.equalsIgnoreCase("32054-01a.htm"))
    		{
      			st.set("cond", "1");
      			st.setState(State.STARTED);
      			st.playSound("ItemSound.quest_accept");
    		}
    		else if (event.equalsIgnoreCase("32054-03.htm"))
    		{
      			if (st.getQuestItemsCount(8514) == 0L)
        			htmltext = "32054-04.htm";
    		}
    		else if (event.equalsIgnoreCase("32054-05.htm"))
    		{
      			st.exitQuest(true);
      			st.playSound("ItemSound.quest_giveup");
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
      				QuestState st2 = player.getQuestState("_117_TheOceanOfDistantStars");
      				if ((st2 != null) && (st2.isCompleted()) && (player.getLevel() >= 39))
				{
        				htmltext = "32054-01.htm";
      				}
      				else
				{
        				htmltext = "32054-00.htm";
        				st.exitQuest(true);
      				}
      				break;
    			case State.STARTED:
      				htmltext = "32054-02.htm";
      				break;
    		}
    		return htmltext;
  	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

    		if ((st.isStarted()) && (st.getRandom(100) < 25))
    		{
      			st.giveItems(DREAM_FRAGMENT, 1L);
      			st.playSound("ItemSound.quest_itemget");
    		}
    		return null;
  	}

  	public static void main(String[] args)
  	{
    		new _650_ABrokenDream(650, qn, "");
  	}
}