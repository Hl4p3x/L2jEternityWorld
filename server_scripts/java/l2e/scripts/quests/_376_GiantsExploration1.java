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
import l2e.util.Rnd;

/**
 * Created by LordWinter 06.08.2011 Based on L2J Eternity-World
 */
public class _376_GiantsExploration1 extends Quest
{
	private static final String qn = "_376_GiantsExploration1";
	
	// NPC's
	private static final int SOBLING = 31147;
	
	// Items
	private static final int ANCIENT_PARCHMENT = 14841;
	private static final int BOOK1 = 14836, BOOK2 = 14837, BOOK3 = 14838, BOOK4 = 14839, BOOK5 = 14840;
	
	// Mobs
	private static final int[] MOBS =
	{
		22670,
		22671,
		22672,
		22673,
		22674,
		22675,
		22676,
		22677
	};
	
	public _376_GiantsExploration1(int id, String name, String descr)
	{
		super(id, name, descr);
		addStartNpc(SOBLING);
		addTalkId(SOBLING);
		
		for (int i : MOBS)
		{
			addKillId(i);
		}
		
		questItemIds = new int[]
		{
			ANCIENT_PARCHMENT
		};
	}
	
	private String onExchangeRequest(String event, QuestState st, long qty, long rem)
	{
		if ((st.getQuestItemsCount(BOOK1) >= rem) && (st.getQuestItemsCount(BOOK2) >= rem) && (st.getQuestItemsCount(BOOK3) >= rem) && (st.getQuestItemsCount(BOOK4) >= rem) && (st.getQuestItemsCount(BOOK5) >= rem))
		{
			st.takeItems(BOOK1, rem);
			st.takeItems(BOOK2, rem);
			st.takeItems(BOOK3, rem);
			st.takeItems(BOOK4, rem);
			st.takeItems(BOOK5, rem);
			st.giveItems(Integer.parseInt(event), qty);
			st.playSound("ItemSound.quest_finish");
			return "31147-ok.htm";
		}
		return "31147-no.htm";
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
		
		if (event.equalsIgnoreCase("31147-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31147-quit.htm"))
		{
			st.unset("cond");
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		else if (isDigit(event))
		{
			int id = Integer.parseInt(event);
			
			if (id == 9967)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if (id == 9968)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if (id == 9969)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if (id == 9970)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if (id == 9971)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if (id == 9972)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if (id == 9973)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if (id == 9974)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if (id == 9975)
			{
				htmltext = onExchangeRequest(event, st, 1, 10);
			}
			else if (id == 9628)
			{
				htmltext = onExchangeRequest(event, st, 6, 1);
			}
			else if (id == 9629)
			{
				htmltext = onExchangeRequest(event, st, 3, 1);
			}
			else if (id == 9630)
			{
				htmltext = onExchangeRequest(event, st, 4, 1);
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
		
		if (st.getState() == State.STARTED)
		{
			if ((st.getQuestItemsCount(BOOK1) > 0) && (st.getQuestItemsCount(BOOK2) > 0) && (st.getQuestItemsCount(BOOK3) > 0) && (st.getQuestItemsCount(BOOK4) > 0) && (st.getQuestItemsCount(BOOK5) > 0))
			{
				htmltext = "31147-03.htm";
			}
			else
			{
				htmltext = "31147-02a.htm";
			}
		}
		else
		{
			if (player.getLevel() >= 79)
			{
				htmltext = "31147-01.htm";
			}
			else
			{
				htmltext = "31147-00.htm";
			}
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
		
		if (st.getState() != State.STARTED)
		{
			return null;
		}
		
		if ((st.getInt("cond") == 1) && isIntInArray(npc.getId(), MOBS))
		{
			int chance = (int) (20 * Config.RATE_QUEST_DROP);
			int count = 1;
			while (chance > 100)
			{
				chance -= 100;
				if (chance < 20)
				{
					chance = 20;
				}
				count++;
			}
			if (Rnd.getChance(chance))
			{
				st.giveItems(ANCIENT_PARCHMENT, count);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _376_GiantsExploration1(376, qn, "");
	}
}