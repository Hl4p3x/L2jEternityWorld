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
 * Created by LordWinter 12.01.2013 Based on L2J Eternity-World
 */
public class _619_RelicsOfTheOldEmpire extends Quest
{
  	private static final String qn = "_619_RelicsOfTheOldEmpire";

  	private static int GHOST_OF_ADVENTURER = 31538;

  	private static int RELICS = 7254;
  	private static int ENTRANCE = 7075;

  	private static int[] RCP_REWARDS = 
	{
		6881, 6883, 6885, 6887, 6891, 6893, 6895, 6897, 6899, 7580
	};

  	public _619_RelicsOfTheOldEmpire(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(GHOST_OF_ADVENTURER);
    		addTalkId(GHOST_OF_ADVENTURER);

    		for (int id = 21396; id <= 21434; id++)
    		{
      			addKillId(id);
    		}

    		addKillId(21798, 21799, 21800);

    		for (int id = 18120; id <= 18256; id++)
    		{
      			addKillId(id);
    		}

		questItemIds = new int[] { RELICS };
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

    		if (event.equalsIgnoreCase("31538-03.htm"))
    		{
      			st.set("cond", "1");
      			st.setState(State.STARTED);
      			st.playSound("ItemSound.quest_accept");
    		}
    		else if (event.equalsIgnoreCase("31538-09.htm"))
    		{
      			if (st.getQuestItemsCount(RELICS) >= 1000)
      			{
        			htmltext = "31538-09.htm";
        			st.takeItems(RELICS, 1000);
        			st.giveItems(RCP_REWARDS[getRandom(RCP_REWARDS.length)], 1);
      			}
      			else
      			{
        			htmltext = "31538-06.htm";
      			}
    		}
    		else if (event.equalsIgnoreCase("31538-10.htm"))
    		{
      			st.playSound("ItemSound.quest_finish");
      			st.exitQuest(true);
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
      				if (player.getLevel() < 74)
      				{
        				htmltext = "31538-02.htm";
        				st.exitQuest(true);
      				}
      				else
      				{
        				htmltext = "31538-01.htm";
      				}
      				break;
    			case State.STARTED:
      				if (st.getQuestItemsCount(RELICS) >= 1000)
      				{
        				htmltext = "31538-04.htm";
      				}
      				else if (st.getQuestItemsCount(ENTRANCE) >= 1)
      				{
        				htmltext = "31538-06.htm";
      				}
      				else
      				{
        				htmltext = "31538-07.htm";
      				}
      				break;
    		}
    		return htmltext;
  	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
    		L2PcInstance partyMember = getRandomPartyMemberState(player, State.STARTED);
    		if (partyMember == null)
    		{	
      			return null;
    		}

    		QuestState st = partyMember.getQuestState(qn);

    		st.dropItemsAlways(RELICS, 1, -1);
    		st.dropItems(ENTRANCE, 1, -1, 50000);

    		return null;
  	}

  	public static void main(String[] args)
  	{
    		new _619_RelicsOfTheOldEmpire(619, qn, "");
  	}
}