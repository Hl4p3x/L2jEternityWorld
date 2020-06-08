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
 * Created by LordWinter 06.07.2012
 * Based on L2J Eternity-World
 */
public class _258_BringWolfPelts extends Quest
{
	private final static String qn = "_258_BringWolfPelts";
	
	// NPC
	private static final int LECTOR = 30001;
	
	// Monsters
	private static final int WOLF = 20120;
	private static final int ELDER_WOLF = 20442;
	
	// Item
	private static final int WOLF_PELT = 702;
	
	// Rewards
	private static final int Cotton_Shirt = 390;
	private static final int Leather_Pants = 29;
	private static final int Leather_Shirt = 22;
	private static final int Short_Leather_Gloves = 1119;
	private static final int Tunic = 426;
	
	public _258_BringWolfPelts(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(LECTOR);
		addTalkId(LECTOR);
		
		addKillId(WOLF, ELDER_WOLF);

		questItemIds = new int[] { WOLF_PELT };
	}	
			
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30001-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
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
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 3 && player.getLevel() <= 9)
					htmltext = "30001-02.htm";
				else
				{
					htmltext = "30001-01.htm";
					st.exitQuest(true);
				}
				break;
			
			case State.STARTED:
				if (st.getQuestItemsCount(WOLF_PELT) < 40)
					htmltext = "30001-05.htm";
				else
				{
					st.takeItems(WOLF_PELT, 40);
					int randomNumber = st.getRandom(16);
					
					if (randomNumber == 0)
						st.giveItems(Cotton_Shirt, 1);
					else if (randomNumber < 6)
						st.giveItems(Leather_Pants, 1);
					else if (randomNumber < 9)
						st.giveItems(Leather_Shirt, 1);
					else if (randomNumber < 13)
						st.giveItems(Short_Leather_Gloves, 1);
					else
						st.giveItems(Tunic, 1);
					
					htmltext = "30001-06.htm";
					
					if (randomNumber == 0)
						st.playSound("ItemSound.quest_jackpot");
					else
						st.playSound("ItemSound.quest_finish");
					
					st.exitQuest(true);
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
		
		if (st.getInt("cond") == 1 && st.getQuestItemsCount(WOLF_PELT) < 40)
		{
			st.giveItems(WOLF_PELT, 1);
			if (st.getQuestItemsCount(WOLF_PELT) == 40)
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
		new _258_BringWolfPelts(258, qn, "");	
	}
}