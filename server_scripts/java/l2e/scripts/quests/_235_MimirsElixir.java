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

import java.util.Map;

import javolution.util.FastMap;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 06.07.2012 Based on L2J Eternity-World
 */
public class _235_MimirsElixir extends Quest
{
	private final static String qn = "_235_MimirsElixir";
	
	// NPCs
	private static final int JOAN = 30718;
	private static final int LADD = 30721;
	private static final int MIXING_URN = 31149;
	
	// Items
	private static final int STAR_OF_DESTINY = 5011;
	private static final int PURE_SILVER = 6320;
	private static final int TRUE_GOLD = 6321;
	private static final int SAGES_STONE = 6322;
	private static final int BLOOD_FIRE = 6318;
	private static final int MIMIRS_ELIXIR = 6319;
	private static final int MAGISTER_MIXING_STONE = 5905;
	
	// Reward
	private static final int SCROLL_ENCHANT_WEAPON_A = 729;
	
	// Droplist
	Map<Integer, int[]> droplist = new FastMap<>();
	
	public _235_MimirsElixir(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(LADD);
		addTalkId(LADD);
		addTalkId(JOAN);
		addTalkId(MIXING_URN);
		
		droplist.put(20965, new int[]
		{
			3,
			SAGES_STONE
		});
		droplist.put(21090, new int[]
		{
			6,
			BLOOD_FIRE
		});
		
		addKillId(20965, 21090);
		
		questItemIds = new int[]
		{
			PURE_SILVER,
			TRUE_GOLD,
			SAGES_STONE,
			BLOOD_FIRE,
			MAGISTER_MIXING_STONE,
			MIMIRS_ELIXIR
		};
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
		
		switch (npc.getId())
		{
			case LADD:
				if (event.equalsIgnoreCase("30721-06.htm"))
				{
					st.set("cond", "1");
					st.setState(State.STARTED);
					st.playSound("ItemSound.quest_accept");
				}
				else if (event.equalsIgnoreCase("30721-12.htm") && (st.getQuestItemsCount(TRUE_GOLD) >= 1))
				{
					st.set("cond", "6");
					st.playSound("ItemSound.quest_middle");
					st.giveItems(MAGISTER_MIXING_STONE, 1);
				}
				else if (event.equalsIgnoreCase("30721-16.htm") && (st.getQuestItemsCount(MIMIRS_ELIXIR) >= 1))
				{
					st.giveItems(SCROLL_ENCHANT_WEAPON_A, 1);
					st.takeItems(STAR_OF_DESTINY, -1);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
				}
				break;
			
			case JOAN:
				if (event.equalsIgnoreCase("30718-03.htm"))
				{
					st.set("cond", "3");
					st.playSound("ItemSound.quest_middle");
				}
				break;
			
			case MIXING_URN:
				if (event.equalsIgnoreCase("31149-02.htm"))
				{
					if (st.getQuestItemsCount(MAGISTER_MIXING_STONE) == 0)
					{
						htmltext = "31149-havent.htm";
					}
				}
				else if (event.equalsIgnoreCase("31149-03.htm"))
				{
					if ((st.getQuestItemsCount(MAGISTER_MIXING_STONE) == 0) || (st.getQuestItemsCount(PURE_SILVER) == 0))
					{
						htmltext = "31149-havent.htm";
					}
				}
				else if (event.equalsIgnoreCase("31149-05.htm"))
				{
					if ((st.getQuestItemsCount(MAGISTER_MIXING_STONE) == 0) || (st.getQuestItemsCount(PURE_SILVER) == 0) || (st.getQuestItemsCount(TRUE_GOLD) == 0))
					{
						htmltext = "31149-havent.htm";
					}
				}
				else if (event.equalsIgnoreCase("31149-07.htm"))
				{
					if ((st.getQuestItemsCount(MAGISTER_MIXING_STONE) == 0) || (st.getQuestItemsCount(PURE_SILVER) == 0) || (st.getQuestItemsCount(TRUE_GOLD) == 0) || (st.getQuestItemsCount(BLOOD_FIRE) == 0))
					{
						htmltext = "31149-havent.htm";
					}
				}
				else if (event.equalsIgnoreCase("31149-success.htm"))
				{
					if ((st.getQuestItemsCount(MAGISTER_MIXING_STONE) == 0) || (st.getQuestItemsCount(PURE_SILVER) == 0) || (st.getQuestItemsCount(TRUE_GOLD) == 0) || (st.getQuestItemsCount(BLOOD_FIRE) == 0))
					{
						htmltext = "31149-havent.htm";
					}
					else
					{
						st.set("cond", "8");
						st.playSound("ItemSound.quest_middle");
						st.takeItems(PURE_SILVER, -1);
						st.takeItems(TRUE_GOLD, -1);
						st.takeItems(BLOOD_FIRE, -1);
						st.giveItems(MIMIRS_ELIXIR, 1);
					}
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() < 75)
				{
					htmltext = "30721-01b.htm";
					st.exitQuest(true);
				}
				else if (st.getQuestItemsCount(STAR_OF_DESTINY) == 0)
				{
					htmltext = "30721-01a.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "30721-01.htm";
				}
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getId())
				{
					case LADD:
						if (cond == 1)
						{
							if (st.getQuestItemsCount(PURE_SILVER) >= 1)
							{
								st.set("cond", "2");
								htmltext = "30721-08.htm";
								st.playSound("ItemSound.quest_middle");
							}
							else
							{
								htmltext = "30721-07.htm";
							}
						}
						else if ((cond > 1) && (cond < 5))
						{
							htmltext = "30721-10.htm";
						}
						else if ((cond == 5) && (st.getQuestItemsCount(TRUE_GOLD) >= 1))
						{
							htmltext = "30721-11.htm";
						}
						else if ((cond == 6) || (cond == 7))
						{
							htmltext = "30721-13.htm";
						}
						else if ((cond == 8) && (st.getQuestItemsCount(MIMIRS_ELIXIR) >= 1))
						{
							htmltext = "30721-14.htm";
						}
						break;
					case JOAN:
						if (cond == 2)
						{
							htmltext = "30718-01.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30718-04.htm";
						}
						else if ((cond == 4) && (st.getQuestItemsCount(SAGES_STONE) >= 1))
						{
							htmltext = "30718-05.htm";
							st.takeItems(SAGES_STONE, -1);
							st.giveItems(TRUE_GOLD, 1);
							st.set("cond", "5");
							st.playSound("ItemSound.quest_middle");
						}
						else if (cond >= 5)
						{
							htmltext = "30718-06.htm";
						}
						break;
					case MIXING_URN:
						htmltext = "31149-01.htm";
						break;
				}
				break;
			
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
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
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		
		if (st.isStarted())
		{
			if (droplist.containsKey(npcId))
			{
				int neededCond = droplist.get(npcId)[0];
				int item = droplist.get(npcId)[1];
				
				if ((st.getRandom(100) < 20) && (cond == neededCond))
				{
					if (st.getQuestItemsCount(item) == 0)
					{
						st.giveItems(item, 1);
						st.playSound("ItemSound.quest_itemget");
						st.set("cond", String.valueOf(cond + 1));
					}
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _235_MimirsElixir(235, qn, "");
	}
}