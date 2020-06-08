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
public class _379_FantasyWine extends Quest
{
	private static final String qn = "_379_FantasyWine";
	
	// NPCs
	private final static int HARLAN = 30074;

	// Monsters
	private final static int ENKU_CHAMPION = 20291;
	private final static int ENKU_SHAMAN = 20292;

	// Items
	private final static int LEAF = 5893;
	private final static int STONE = 5894;

	public _379_FantasyWine(int questId, String name, String descr)
	{
		super(questId, name, descr);		

		addStartNpc(HARLAN);
		addTalkId(HARLAN);

		addKillId(ENKU_CHAMPION, ENKU_SHAMAN);

		questItemIds = new int[]
		{
			LEAF,
			STONE
		};
	}	
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		long leaf = st.getQuestItemsCount(LEAF);
		long stone = st.getQuestItemsCount(STONE);
		
		if (event.equalsIgnoreCase("30074-3.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30074-6.htm"))
		{
			if (leaf == 80 && stone == 100)
			{
				st.takeItems(LEAF, 80);
				st.takeItems(STONE, 100);
				int rand = st.getRandom(100);
				
				if (rand < 25)
				{
					st.giveItems(5956, 1);
					htmltext = "30074-6.htm";
				}
				else if (rand < 50)
				{
					st.giveItems(5957, 1);
					htmltext = "30074-7.htm";
				}
				else
				{
					st.giveItems(5958, 1);
					htmltext = "30074-8.htm";
				}
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
			else
				htmltext = "30074-4.htm";
		}
		else if (event.equalsIgnoreCase("30074-2a.htm"))
		{
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 20 && player.getLevel() <= 25)
					htmltext = "30074-0.htm";
				else
				{
					htmltext = "30074-0a.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				long cond = st.getInt("cond");
				long leaf = st.getQuestItemsCount(LEAF);
				long stone = st.getQuestItemsCount(STONE);
				
				if (cond == 1)
				{
					if (leaf < 80 && stone < 100)
						htmltext = "30074-4.htm";
					else if (leaf == 80 && stone < 100)
						htmltext = "30074-4a.htm";
					else if (leaf < 80 & stone == 100)
						htmltext = "30074-4b.htm";
				}
				else if (cond == 2 && leaf == 80 && stone == 100)
					htmltext = "30074-5.htm";
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
		
		int npcId = npc.getId();

		if (st.isStarted())
		{
			if (npcId == ENKU_CHAMPION && st.getQuestItemsCount(LEAF) < 80)
				st.giveItems(LEAF, 1);
			else if (npcId == ENKU_SHAMAN && st.getQuestItemsCount(STONE) < 100)
				st.giveItems(STONE, 1);
			
			if (st.getQuestItemsCount(LEAF) >= 80 && st.getQuestItemsCount(STONE) >= 100)
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "2");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _379_FantasyWine(379, qn, "");		
	}
}