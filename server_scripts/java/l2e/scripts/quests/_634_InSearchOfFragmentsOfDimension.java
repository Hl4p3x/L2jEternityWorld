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

import l2e.Config;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public class _634_InSearchOfFragmentsOfDimension extends Quest
{
	private final static String qn = "_634_InSearchOfFragmentsOfDimension";
	
	// Items
	private static final int DIMENSION_FRAGMENT = 7079;

	private static final int[] NPCs = 
	{
		31494,
		31495,
		31496,
		31497,
		31498,
		31499,
		31500,
		31501,
		31502,
		31503,
		31504,
		31505,
		31506,
		31507	
	};	
	
	public _634_InSearchOfFragmentsOfDimension(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for(int npcId : NPCs)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}	

		for (int mobs = 21208; mobs < 21256; mobs++)
		{
			addKillId(mobs);
		}	
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
		
		if (event.equalsIgnoreCase("02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("05.htm"))
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
				if (player.getLevel() < 20)
				{
					htmltext = "01a.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "01.htm";
				}
				break;	
			case State.STARTED:
				htmltext = "03.htm";
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
		
		int itemMultiplier = (int) (80 * Config.RATE_QUEST_DROP) / 1000;
		int chance = (int) (80 * Config.RATE_QUEST_DROP) % 1000;
		
		if (st.getRandom(1000) < chance)
		{
			itemMultiplier++;
		}
		
		int numItems = (int) (itemMultiplier * (npc.getLevel() * 0.15 + 1.6));
		if (numItems > 0)
		{
			st.giveItems(DIMENSION_FRAGMENT, numItems);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _634_InSearchOfFragmentsOfDimension(634, qn, "");		
	}
}