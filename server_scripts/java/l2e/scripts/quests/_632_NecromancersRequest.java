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
public class _632_NecromancersRequest extends Quest
{
  	private static final String qn = "_632_NecromancersRequest";

  	private static final int[] VAMPIRES =
	{
		21568, 21573, 21582, 21585, 21586, 21587, 21588, 21589, 21590, 21591, 21592, 21593, 21594, 21595
	};

  	private static final int[] UNDEADS =
	{
		21547, 21548, 21549, 21551, 21552, 21555, 21556, 21562, 21571, 21576, 21577, 21579
	};

  	private static final int VAMPIRE_HEART = 7542;
  	private static final int ZOMBIE_BRAIN = 7543;

  	public _632_NecromancersRequest(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(31522);
    		addTalkId(31522);

    		addKillId(VAMPIRES);
    		addKillId(UNDEADS);

		questItemIds = new int[] { VAMPIRE_HEART, ZOMBIE_BRAIN };
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

    		if (event.equalsIgnoreCase("31522-03.htm"))
    		{
      			st.set("cond", "1");
      			st.setState(State.STARTED);
      			st.playSound("ItemSound.quest_accept");
    		}
    		else if (event.equalsIgnoreCase("31522-06.htm"))
    		{
      			if (st.getQuestItemsCount(VAMPIRE_HEART) > 199)
      			{
        			st.takeItems(VAMPIRE_HEART, -1);
        			st.rewardItems(57, 120000);
        			st.set("cond", "1");
        			st.playSound("ItemSound.quest_middle");
      			}
      			else
      			{
        			htmltext = "31522-09.htm";
      			}
    		}
    		else if (event.equalsIgnoreCase("31522-08.htm"))
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
      				if (player.getLevel() < 63)
      				{
        				st.exitQuest(true);
        				htmltext = "31522-01.htm";
      				}
      				else
      				{
        				htmltext = "31522-02.htm";
      				}
      				break;
    			case State.STARTED:
      				htmltext = st.getQuestItemsCount(VAMPIRE_HEART) >= 200 ? "31522-05.htm" : "31522-04.htm";
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

    		int npcId = npc.getId();
    		for (int undead : UNDEADS)
    		{
      			if (undead == npcId)
      			{
        			st.dropItems(ZOMBIE_BRAIN, 1, -1, 330000);
        			return null;
      			}
    		}

    		if (st.getInt("cond") == 1)
    		{
      			if (st.dropItems(VAMPIRE_HEART, 1, 200, 500000))
      			{
        			st.set("cond", "2");
      			}
    		}
    		return null;
  	}

  	public static void main(String[] args)
  	{
    		new _632_NecromancersRequest(632, qn, "");
  	}
}