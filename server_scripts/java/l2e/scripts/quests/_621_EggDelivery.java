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

/**
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public class _621_EggDelivery extends Quest
{
	private static final String qn = "_621_EggDelivery";
	
	// Chance to get an S-grade random recipe
	private final int RPCHANCE = 10;
	
	// NPC
	private final int JEREMY = 31521;
	private final int PULIN = 31543;
	private final int NAFF = 31544;
	private final int CROCUS = 31545;
	private final int KUBER = 31546;
	private final int BEORIN = 31547;
	private final int VALENTINE = 31584;
	private final int[] NPCS = new int[] { PULIN, NAFF, CROCUS, KUBER, BEORIN, JEREMY, VALENTINE };
	
	// QUEST ITEMS
	private final int BOILED_EGGS = 7195;
	private final int FEE_OF_EGGS = 7196;
	private final int[] ITEMS = new int[] { 6847, 6849, 6851 };
	
	// REWARDS
	private final int HASTE_POTION = 734;
	
	public _621_EggDelivery(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(JEREMY);
		
		for (int i = 0; i < NPCS.length; i++)
		{
			addTalkId(NPCS[i]);
		}
		
		questItemIds = new int[] { BOILED_EGGS, FEE_OF_EGGS };
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		int cond = st.getInt("cond");

		if (event.equalsIgnoreCase("31521-1.htm"))
		{
			if (cond == 0)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.giveItems(BOILED_EGGS, 5);
				st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = getNoQuestMsg(player);
			}
		}
		else if (event.equalsIgnoreCase("31543-1.htm"))
		{
			if (st.getQuestItemsCount(BOILED_EGGS) > 0)
			{
				if (cond == 1)
				{
					st.takeItems(BOILED_EGGS, 1);
					st.giveItems(FEE_OF_EGGS, 1);
					st.set("cond", "2");
				}
				else
				{
					htmltext = getNoQuestMsg(player);
				}
			}
			else
			{
				htmltext = "LMFAO!";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("31544-1.htm"))
		{
			if (st.getQuestItemsCount(BOILED_EGGS) > 0)
			{
				if (cond == 2)
				{
					st.takeItems(BOILED_EGGS, 1);
					st.giveItems(FEE_OF_EGGS, 1);
					st.set("cond", "3");
				}
				else
				{
					htmltext = getNoQuestMsg(player);
				}
			}
			else
			{
				htmltext = "LMFAO!";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("31545-1.htm"))
		{
			if (st.getQuestItemsCount(BOILED_EGGS) > 0)
			{
				if (cond == 3)
				{
					st.takeItems(BOILED_EGGS, 1);
					st.giveItems(FEE_OF_EGGS, 1);
					st.set("cond", "4");
				}
				else
				{
					htmltext = getNoQuestMsg(player);
				}
			}
			else
			{
				htmltext = "LMFAO!";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("31546-1.htm"))
		{
			if (st.getQuestItemsCount(BOILED_EGGS) > 0)
			{
				if (cond == 4)
				{
					st.takeItems(BOILED_EGGS, 1);
					st.giveItems(FEE_OF_EGGS, 1);
					st.set("cond", "5");
				}
				else
				{
					htmltext = getNoQuestMsg(player);
				}
			}
			else
			{
				htmltext = "LMFAO!";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("31547-1.htm"))
		{
			if (st.getQuestItemsCount(BOILED_EGGS) > 0)
			{
				if (cond == 5)
				{
					st.takeItems(BOILED_EGGS, 1);
					st.giveItems(FEE_OF_EGGS, 1);
					st.set("cond", "6");
				}
				else
				{
					htmltext = getNoQuestMsg(player);
				}
			}
			else
			{
				htmltext = "LMFAO!";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("31521-3.htm"))
		{
			st.set("cond", "7");
		}
		else if (event.equalsIgnoreCase("31584-2.htm"))
		{
			if (st.getQuestItemsCount(FEE_OF_EGGS) == 5)
			{
				st.takeItems(FEE_OF_EGGS, 5);
				if (st.getRandom(100) < RPCHANCE)
				{
					st.giveItems(ITEMS[st.getRandom(3)], 1);
				}
				else
				{
					st.giveAdena(18800, true);
					st.giveItems(HASTE_POTION, (int) Config.RATE_QUEST_REWARD);
				}
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
			else
			{
				htmltext = getNoQuestMsg(player);
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st != null)
		{
			int npcId = npc.getId();
			byte id = st.getState();
			if (id == State.CREATED)
			{
				st.set("cond", "0");
			}
			int cond = st.getInt("cond");
			if (npcId == JEREMY)
			{
					if (cond == 0)
					{
						if (player.getLevel() >= 68)
						{
							htmltext = "31521-0.htm";
						}
						else
						{
							st.exitQuest(true);
						}
					}
					else if ((cond == 6) && (st.getQuestItemsCount(FEE_OF_EGGS) == 5))
					{
						htmltext = "31521-2.htm";
					}
					else if ((cond == 7) && (st.getQuestItemsCount(FEE_OF_EGGS) == 5))
					{
						htmltext = "31521-4.htm";
					}
					
			}
			else if ((id == State.STARTED) && (st.getQuestItemsCount(BOILED_EGGS) > 0))
			{
				if ((npcId == PULIN) && (cond == 1))
				{
					htmltext = "31543-0.htm";
				}
				else if ((npcId == NAFF) && (cond == 2))
				{
					htmltext = "31544-0.htm";
				}
				else if ((npcId == CROCUS) && (cond == 3))
				{
					htmltext = "31545-0.htm";
				}
				else if ((npcId == KUBER) && (cond == 4))
				{
					htmltext = "31546-0.htm";
				}
				else if ((npcId == BEORIN) && (cond == 5))
				{
					htmltext = "31547-0.htm";
				}
			}
			else if ((npcId == VALENTINE) && (cond == 7) && (st.getQuestItemsCount(FEE_OF_EGGS) == 5))
			{
				htmltext = "31584-1.htm";
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _621_EggDelivery(621, qn, "");			
	}
}