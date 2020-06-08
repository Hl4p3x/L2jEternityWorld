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
public class _275_DarkWingedSpies extends Quest
{
	private final static String qn = "_275_DarkWingedSpies";
	
	// NPC
	private static final int TANTUS = 30567;
	
	// Monsters
	private static final int DARKWING_BAT = 20316;
	private static final int VARANGKA_TRACKER = 27043;
	
	// Items
	private static final int DARKWING_BAT_FANG = 1478;
	private static final int VARANGKAS_PARASITE = 1479;

	public _275_DarkWingedSpies(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(TANTUS);
		addTalkId(TANTUS);
		
		addKillId(DARKWING_BAT, VARANGKA_TRACKER);

		questItemIds = new int[] { DARKWING_BAT_FANG, VARANGKAS_PARASITE };
	}
			
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30567-03.htm"))
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
				if (player.getRace().ordinal() == 3)
				{
					if (player.getLevel() >= 11 && player.getLevel() <= 15)
						htmltext = "30567-02.htm";
					else
					{
						htmltext = "30567-01.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "30567-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (st.getQuestItemsCount(DARKWING_BAT_FANG) < 70)
					htmltext = "30567-04.htm";
				else
				{
					htmltext = "30567-05.htm";
					st.takeItems(DARKWING_BAT_FANG, -1);
					st.takeItems(VARANGKAS_PARASITE, -1);
					st.rewardItems(57, 4550);
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
		
		if (st.isStarted())
		{
			switch (npc.getId())
			{
				case DARKWING_BAT:
					if (st.getQuestItemsCount(DARKWING_BAT_FANG) < 70)
					{
						st.giveItems(DARKWING_BAT_FANG, 1);
						
						if (st.getQuestItemsCount(DARKWING_BAT_FANG) < 69)
							st.playSound("ItemSound.quest_itemget");
						else
						{
							st.playSound("ItemSound.quest_middle");
							st.set("cond", "2");
						}
						
						if (st.getQuestItemsCount(DARKWING_BAT_FANG) < 66 && st.getRandom(100) < 10)
						{
							st.addSpawn(VARANGKA_TRACKER, npc);
							st.giveItems(VARANGKAS_PARASITE, 1);
						}
					}
					break;
				case VARANGKA_TRACKER:
					if (st.getQuestItemsCount(DARKWING_BAT_FANG) < 66 && st.getQuestItemsCount(VARANGKAS_PARASITE) == 1)
					{
						st.takeItems(VARANGKAS_PARASITE, -1);
						st.giveItems(DARKWING_BAT_FANG, 5);
						
						if (st.getQuestItemsCount(DARKWING_BAT_FANG) < 65)
							st.playSound("ItemSound.quest_itemget");
						else
						{
							st.playSound("ItemSound.quest_middle");
							st.set("cond", "2");
						}
					}
					break;
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _275_DarkWingedSpies(275, qn, "");	
	}
}