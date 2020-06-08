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
 * Created by LordWinter 24.09.2012 Based on L2J Eternity-World
 */
public final class _654_JourneyToASettlement extends Quest
{
	private static final String qn = "_654_JourneyToASettlement";
	
	// NPC
	private static final int SPIRIT = 31453;
	
	// MOBs
	private static final int[] MOBS =
	{
		21294,
		21295
	};
	
	// QUEST ITEMS
	private static final int ANTELOPE_SKIN = 8072;
	private static final int FRINTEZZA_FORCE_SCROLL = 8073;
	
	public _654_JourneyToASettlement(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(SPIRIT);
		addTalkId(SPIRIT);
		
		for (int i : MOBS)
		{
			addKillId(i);
		}
		
		questItemIds = new int[]
		{
			ANTELOPE_SKIN,
			FRINTEZZA_FORCE_SCROLL
		};
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
		
		if (event.equalsIgnoreCase("31453-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31453-03.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31453-05.htm") && st.hasQuestItems(ANTELOPE_SKIN))
		{
			st.takeItems(ANTELOPE_SKIN, 1);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
			st.giveItems(FRINTEZZA_FORCE_SCROLL, 1);
			st.unset("cond");
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.CREATED:
				QuestState qs = player.getQuestState("_119_LastImperialPrince");
				if (player.getLevel() < 74)
				{
					htmltext = "31453-06.htm";
					st.exitQuest(true);
				}
				else if ((qs == null) || !qs.isCompleted())
				{
					htmltext = "31453-07.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "31453-01.htm";
				}
				break;
			case State.STARTED:
				if ((npcId == SPIRIT) && (cond != 3))
				{
					htmltext = "31453-02.htm";
				}
				else if ((npcId == SPIRIT) && (cond == 3))
				{
					htmltext = "31453-04.htm";
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
		
		if (st.getInt("cond") == 2)
		{
			st.dropQuestItems(ANTELOPE_SKIN, 1, 1, 1, false, 5, true);
			if (st.hasQuestItems(ANTELOPE_SKIN))
			{
				st.set("cond", "3");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _654_JourneyToASettlement(654, qn, "");
	}
}