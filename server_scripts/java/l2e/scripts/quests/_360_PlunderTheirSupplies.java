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
public class _360_PlunderTheirSupplies extends Quest
{
	private static final String qn = "_360_PlunderTheirSupplies";
	
	// NPC
	private static final int COLEMAN = 30873;
	
	// Items
	private static final int SUPPLY_ITEM = 5872;
	private static final int SUSPICIOUS_DOCUMENT = 5871;
	private static final int RECIPE_OF_SUPPLY = 5870;
	
	public _360_PlunderTheirSupplies(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(COLEMAN);
		addTalkId(COLEMAN);

		addKillId(20666, 20669);

		questItemIds = new int[]
		{
			RECIPE_OF_SUPPLY,
			SUPPLY_ITEM,
			SUSPICIOUS_DOCUMENT
		};
	}
		
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30873-2.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30873-6.htm"))
		{
			st.takeItems(SUPPLY_ITEM, -1);
			st.takeItems(SUSPICIOUS_DOCUMENT, -1);
			st.takeItems(RECIPE_OF_SUPPLY, -1);
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
				if (player.getLevel() >= 52 && player.getLevel() <= 59)
					htmltext = "30873-0.htm";
				else
				{
					htmltext = "30873-0a.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (st.getQuestItemsCount(SUPPLY_ITEM) == 0)
					htmltext = "30873-3.htm";
				else
				{
					htmltext = "30873-5.htm";	
					long reward = 6000 + st.getQuestItemsCount(SUPPLY_ITEM) * 100 + st.getQuestItemsCount(RECIPE_OF_SUPPLY) * 6000;
					st.takeItems(SUPPLY_ITEM, -1);
					st.takeItems(RECIPE_OF_SUPPLY, -1);
					st.rewardItems(57, reward);
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
			return null;
		
		int chance = st.getRandom(10);

		if (chance == 9)
		{
			st.giveItems(SUSPICIOUS_DOCUMENT, 1);
			
			if (st.getQuestItemsCount(SUSPICIOUS_DOCUMENT) == 5)
			{
				st.takeItems(SUSPICIOUS_DOCUMENT, 5);
				st.giveItems(RECIPE_OF_SUPPLY, 1);
				st.playSound("ItemSound.quest_middle");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		else if (chance < 6)
		{
			st.giveItems(SUPPLY_ITEM, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _360_PlunderTheirSupplies(360, qn, "");		
	}
}