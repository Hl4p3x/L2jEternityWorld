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
 * Created by LordWinter 13.01.2013 Based on L2J Eternity-World
 */
public class _380_BringOutTheFlavorOfIngredients extends Quest
{
	private static final String qn = "_380_BringOutTheFlavorOfIngredients";
	
	private final static int DIRE_WOLF = 20205;
	private final static int KADIF_WEREWOLF = 20206;
	private final static int GIANT_MIST_LEECH = 20225;
	
	private final static int RITRONS_FRUIT = 5895;
	private final static int MOON_FACE_FLOWER = 5896;
	private final static int LEECH_FLUIDS = 5897;
	private final static int ANTIDOTE = 1831;
	
	private final static int RITRON_JELLY = 5960;
	private final static int JELLY_RECIPE = 5959;
	
	private final static int REC_CHANCE = 55;
	
	public _380_BringOutTheFlavorOfIngredients(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(30069);
		addTalkId(30069);
		
		addKillId(DIRE_WOLF, KADIF_WEREWOLF, GIANT_MIST_LEECH);

		questItemIds = new int[]
		{
			RITRONS_FRUIT,
			MOON_FACE_FLOWER,
			LEECH_FLUIDS
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30069-04.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30069-12.htm"))
		{
			st.giveItems(JELLY_RECIPE, 1);
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
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 24)
					htmltext = "30069-01.htm";
				else
				{
					htmltext = "30069-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "30069-06.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(ANTIDOTE) >= 2)
					{
						st.takeItems(RITRONS_FRUIT, -1);
						st.takeItems(MOON_FACE_FLOWER, -1);
						st.takeItems(LEECH_FLUIDS, -1);
						st.takeItems(ANTIDOTE, 2);
						st.set("cond", "3");
						st.playSound("ItemSound.quest_middle");
						htmltext = "30069-07.htm";
					}
					else
						htmltext = "30069-06.htm";
				}
				else if (cond == 3)
				{
					st.set("cond", "4");
					st.playSound("ItemSound.quest_middle");
					htmltext = "30069-08.htm";
				}
				else if (cond == 4)
				{
					st.set("cond", "5");
					st.playSound("ItemSound.quest_middle");
					htmltext = "30069-09.htm";
				}
				else if (cond == 5)
				{
					st.set("cond", "6");
					st.playSound("ItemSound.quest_middle");
					htmltext = "30069-10.htm";
				}
				else if (cond == 6)
				{
					st.giveItems(RITRON_JELLY, 1);
					if (getRandom(100) < REC_CHANCE)
						htmltext = "30069-11.htm";
					else
					{
						htmltext = "30069-13.htm";
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(true);
					}
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		switch (npc.getId())
		{
			case DIRE_WOLF:
				if (st.dropItems(RITRONS_FRUIT, 1, 4, 100000))
					if (st.getQuestItemsCount(MOON_FACE_FLOWER) == 20 && st.getQuestItemsCount(LEECH_FLUIDS) == 10)
						st.set("cond", "2");
				break;
			case KADIF_WEREWOLF:
				if (st.dropItems(MOON_FACE_FLOWER, 1, 20, 250000))
					if (st.getQuestItemsCount(RITRONS_FRUIT) == 4 && st.getQuestItemsCount(LEECH_FLUIDS) == 10)
						st.set("cond", "2");
				break;
			case GIANT_MIST_LEECH:
				if (st.dropItems(LEECH_FLUIDS, 1, 10, 250000))
					if (st.getQuestItemsCount(RITRONS_FRUIT) == 4 && st.getQuestItemsCount(MOON_FACE_FLOWER) == 20)
						st.set("cond", "2");
				break;
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _380_BringOutTheFlavorOfIngredients(380, qn, "");
	}
}