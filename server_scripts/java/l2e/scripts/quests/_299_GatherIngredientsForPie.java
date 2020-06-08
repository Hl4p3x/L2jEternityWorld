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
 * Created by LordWinter 08.07.2012
 * Based on L2J Eternity-World
 */
public class _299_GatherIngredientsForPie extends Quest
{
	private static final String qn = "_299_GatherIngredientsForPie";
	
	// NPCs
	private static final int LARA = 30063;
	private static final int BRIGHT = 30466;
	private static final int EMILY = 30620;
	
	// Items
	private static final int FRUIT_BASKET = 7136;
	private static final int AVELLAN_SPICE = 7137;
	private static final int HONEY_POUCH = 7138;
	
	// Monsters
	private static final int WASP_WORKER = 20934;
	private static final int WASP_LEADER = 20935;
	
	public _299_GatherIngredientsForPie(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(EMILY);
		addTalkId(EMILY);
		addTalkId(LARA);
		addTalkId(BRIGHT);
		
		addKillId(WASP_WORKER, WASP_LEADER);

		questItemIds = new int[] { FRUIT_BASKET, AVELLAN_SPICE, HONEY_POUCH };
	}	
			
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30620-1.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30620-3.htm"))
		{
			st.set("cond", "3");
			st.takeItems(HONEY_POUCH, -1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30063-1.htm"))
		{
			st.set("cond", "4");
			st.giveItems(AVELLAN_SPICE, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30620-5.htm"))
		{
			st.set("cond", "5");
			st.takeItems(AVELLAN_SPICE, -1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30466-1.htm"))
		{
			st.set("cond", "6");
			st.giveItems(FRUIT_BASKET, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30620-7a.htm"))
		{
			if (st.getQuestItemsCount(FRUIT_BASKET) >= 1)
			{
				st.takeItems(FRUIT_BASKET, -1);
				st.rewardItems(57, 25000);
				st.giveItems(1865,50);
				st.unset("cond");
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if(st == null)
			return htmltext;

		int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 34 && player.getLevel() <= 40)
					htmltext = "30620-0.htm";
				else
				{
					htmltext = "30620-0a.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case EMILY:
						if (cond == 1)
							htmltext = "30620-1a.htm";
						else if (cond == 2)
						{
							if (st.getQuestItemsCount(HONEY_POUCH) >= 100)
								htmltext = "30620-2.htm";
							else
							{
								htmltext = "30620-2a.htm";
								st.exitQuest(true);
							}
						}
						else if (cond == 3)
							htmltext = "30620-3a.htm";
						else if (cond == 4)
						{
							if (st.getQuestItemsCount(AVELLAN_SPICE) >= 1)
								htmltext = "30620-4.htm";
							else
							{
								htmltext = "30620-4a.htm";
								st.exitQuest(true);
							}
						}
						else if (cond == 5)
							htmltext = "30620-5a.htm";
						else if (cond == 6)
							htmltext = "30620-6.htm";
						break;
					case LARA:
						if (cond == 3)
							htmltext = "30063-0.htm";
						else if (cond >= 4)
							htmltext = "30063-1a.htm";
						break;
					case BRIGHT:
						if (cond == 5)
							htmltext = "30466-0.htm";
						else if (cond >= 6)
							htmltext = "30466-1a.htm";
						break;
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		if (st.getRandom(100) < 50)
		{
			st.giveItems(HONEY_POUCH, 1);
			if (st.getQuestItemsCount(HONEY_POUCH) == 100)
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _299_GatherIngredientsForPie(299, qn, "");	
	}
}