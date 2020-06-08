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
public class _364_JovialAccordion extends Quest
{
	private static final String qn = "_364_JovialAccordion";
	
	// NPCs
	private final static int BARBADO = 30959;
	private final static int SWAN = 30957;
	private final static int SABRIN = 30060;
	private final static int XABER = 30075;
	private final static int CLOTH_CHEST = 30961;
	private final static int BEER_CHEST = 30960;
	
	// Items
	private final static int KEY_1 = 4323;
	private final static int KEY_2 = 4324;
	private final static int STOLEN_BEER = 4321;
	private final static int STOLEN_CLOTHES = 4322;
	private final static int ECHO = 4421;
	
	public _364_JovialAccordion(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(BARBADO);
		addTalkId(BARBADO, SWAN, SABRIN, XABER, CLOTH_CHEST, BEER_CHEST);

		questItemIds = new int[]
		{
			KEY_1,
			KEY_2,
			STOLEN_BEER,
			STOLEN_CLOTHES
		};
	}	
		
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30959-02.htm"))
		{
			st.set("cond", "1");
			st.set("items", "0");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30957-02.htm"))
		{
			st.set("cond", "2");
			st.giveItems(KEY_1, 1);
			st.giveItems(KEY_2, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30960-04.htm"))
		{
			if (st.getQuestItemsCount(KEY_2) == 1)
			{
				st.takeItems(KEY_2, 1);
				if (st.getRandom(10) < 5)
				{
					htmltext = "30960-02.htm";
					st.giveItems(STOLEN_BEER, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (event.equalsIgnoreCase("30961-04.htm"))
		{
			if (st.getQuestItemsCount(KEY_1) == 1)
			{
				st.takeItems(KEY_1, 1);
				if (st.getRandom(10) < 5)
				{
					htmltext = "30961-02.htm";
					st.giveItems(STOLEN_CLOTHES, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
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
				if (player.getLevel() >= 15)
					htmltext = "30959-01.htm";
				else
				{
					htmltext = "30959-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				int stolenItems = st.getInt("items");
				
				switch (npc.getId())
				{
					case BARBADO:
						if (cond == 1 || cond == 2)
							htmltext = "30959-03.htm";
						else if (cond == 3)
						{
							htmltext = "30959-04.htm";
							st.giveItems(ECHO, 1);
							st.playSound("ItemSound.quest_finish");
							st.exitQuest(true);
						}
						break;
					case SWAN:
						if (cond == 1)
							htmltext = "30957-01.htm";
						else if (cond == 2)
						{
							if (stolenItems > 0)
							{
								st.set("cond", "3");
								st.playSound("ItemSound.quest_middle");
								if (stolenItems == 2)
								{
									htmltext = "30957-04.htm";
									st.rewardItems(57, 100);
								}
								else
									htmltext = "30957-05.htm";
							}
							else
							{
								if (st.getQuestItemsCount(KEY_1) == 0 && st.getQuestItemsCount(KEY_2) == 0)
								{
									htmltext = "30957-06.htm";
									st.playSound("ItemSound.quest_finish");
									st.exitQuest(true);
								}
								else
									htmltext = "30957-03.htm";
							}
						}
						else if (cond == 3)
							htmltext = "30957-07.htm";
						break;
					case BEER_CHEST:
						htmltext = "30960-03.htm";
						if (cond == 2 && st.getQuestItemsCount(KEY_2) == 1)
							htmltext = "30960-01.htm";
						break;
					case CLOTH_CHEST:
						htmltext = "30961-03.htm";
						if (cond == 2 && st.getQuestItemsCount(KEY_1) == 1)
							htmltext = "30961-01.htm";
						break;
					case SABRIN:
						if (st.getQuestItemsCount(STOLEN_BEER) == 1)
						{
							htmltext = "30060-01.htm";
							st.takeItems(STOLEN_BEER, 1);
							st.playSound("ItemSound.quest_itemget");
							st.set("items", String.valueOf(stolenItems + 1));
						}
						else
							htmltext = "30060-02.htm";
						break;
					case XABER:
						if (st.getQuestItemsCount(STOLEN_CLOTHES) == 1)
						{
							htmltext = "30075-01.htm";
							st.takeItems(STOLEN_CLOTHES, 1);
							st.playSound("ItemSound.quest_itemget");
							st.set("items", String.valueOf(stolenItems + 1));
						}
						else
							htmltext = "30075-02.htm";
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _364_JovialAccordion(364, qn, "");		
	}
}