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
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public class _354_ConquestOfAlligatorIsland extends Quest
{
	private static final String qn = "_354_ConquestOfAlligatorIsland";
	
	// Items
	private static final int ALLIGATOR_TOOTH = 5863;
	private static final int TORN_MAP_FRAGMENT = 5864;
	private static final int PIRATES_TREASURE_MAP = 5915;
	
	// NPC
	private static final int KLUCK = 30895;

	// RANDOM_REWARDS
	public final int[][] RANDOM_REWARDS =
	{
		{ 736, 15 }, 	// SoE
		{ 1061, 20 }, 	// Healing Potion
		{ 734, 10 }, 	// Haste Potion
		{ 735, 5 }, 	// Alacrity Potion
		{ 1878, 25 }, 	// Braided Hemp
		{ 1875, 10 }, 	// Stone of Purity
		{ 1879, 10 }, 	// Cokes
		{ 1880, 10 }, 	// Steel
		{ 956, 1 }, 	// Enchant Armor D
		{ 955, 1 } 	// Enchant Weapon D
	};	
	
	public _354_ConquestOfAlligatorIsland(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(KLUCK);
		addTalkId(KLUCK);

		addKillId(20804, 20805, 20806, 20807, 20808, 20991);

		questItemIds = new int[] { ALLIGATOR_TOOTH, TORN_MAP_FRAGMENT };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30895-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30895-03.htm"))
		{
			if (st.getQuestItemsCount(TORN_MAP_FRAGMENT) > 0)
				htmltext = "30895-03a.htm";
		}
		else if (event.equalsIgnoreCase("30895-05.htm"))
		{
				if(st.getQuestItemsCount(ALLIGATOR_TOOTH) > 99)
				{
					st.giveItems(57, st.getQuestItemsCount(ALLIGATOR_TOOTH) * 300);
					st.takeItems(ALLIGATOR_TOOTH, -1);
					st.playSound("ItemSound.quest_itemget");
					int random = getRandom(RANDOM_REWARDS.length);
					st.giveItems(RANDOM_REWARDS[random][0], RANDOM_REWARDS[random][1]);
					htmltext = "30895-05b.htm";
				}
				else
				{
					st.giveItems(57, st.getQuestItemsCount(ALLIGATOR_TOOTH) * 100);
					st.takeItems(ALLIGATOR_TOOTH, -1);
					st.playSound("ItemSound.quest_itemget");
					htmltext = "30895-05a.htm";
				}
		}
		else if (event.equalsIgnoreCase("30895-07.htm"))
		{
			if (st.getQuestItemsCount(TORN_MAP_FRAGMENT) >= 10)
			{
				htmltext = "30895-08.htm";
				st.takeItems(TORN_MAP_FRAGMENT, 10);
				st.giveItems(PIRATES_TREASURE_MAP, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if (event.equalsIgnoreCase("30895-09.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 38 && player.getLevel() <= 49)
					htmltext = "30895-01.htm";
				else
					htmltext = "30895-00.htm";
				break;
			case State.STARTED:
				if (st.getQuestItemsCount(TORN_MAP_FRAGMENT) > 0)
					htmltext = "30895-03a.htm";
				else
					htmltext = "30895-03.htm";
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMemberState(player, State.STARTED);
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		int random = st.getRandom(100);

		if (random < 45)
		{
			st.giveItems(ALLIGATOR_TOOTH, 1);
			if (random < 10)
			{
				st.giveItems(TORN_MAP_FRAGMENT, 1);
				st.playSound("ItemSound.quest_middle");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _354_ConquestOfAlligatorIsland(354, qn, "");	
	}
}