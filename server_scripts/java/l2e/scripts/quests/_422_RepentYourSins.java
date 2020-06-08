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
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 25.09.2012
 * Based on L2J Eternity-World
 */
public class _422_RepentYourSins extends Quest
{
	private static final String qn = "_422_RepentYourSins";
	
	// Items
	private static final int SCAVENGER_WERERAT_SKULL = 4326;
	private static final int TUREK_WARHOUND_TAIL = 4327;
	private static final int TYRANT_KINGPIN_HEART = 4328;
	private static final int TRISALIM_TARANTULAS_VENOM_SAC = 4329;
	private static final int MANUAL_OF_MANACLES = 4331;
	private static final int PENITENTS_MANACLES = 4425;
	private static final int PENITENTS_MANACLES1 = 4330;
	private static final int PENITENTS_MANACLES2 = 4426;
	private static final int SILVER_NUGGET = 1873;
	private static final int ADAMANTINE_NUGGET = 1877;
	private static final int BLACKSMITHS_FRAME = 1892;
	private static final int COKES = 1879;
	private static final int STEEL = 1880;
	
	// NPC
	private static final int BLACK_JUDGE = 30981;
	private static final int KATARI = 30668;
	private static final int PIOTUR = 30597;
	private static final int CASIAN = 30612;
	private static final int JOAN = 30718;
	private static final int PUSHKIN = 30300;
	
	// Mobs
	private static final int SCAVENGER_WERERAT = 20039;
	private static final int TUREK_WARHOUND = 20494;
	private static final int TYRANT_KINGPIN = 20193;
	private static final int TRISALIM_TARANTULA = 20561;

	public _422_RepentYourSins(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(BLACK_JUDGE);
		addTalkId(BLACK_JUDGE);
		addTalkId(KATARI);
		addTalkId(PIOTUR);
		addTalkId(CASIAN);
		addTalkId(JOAN);
		addTalkId(PUSHKIN);
		
		addKillId(SCAVENGER_WERERAT);
		addKillId(TUREK_WARHOUND);
		addKillId(TYRANT_KINGPIN);
		addKillId(TRISALIM_TARANTULA);

		questItemIds = new int[]
		{
			SCAVENGER_WERERAT_SKULL,
			TUREK_WARHOUND_TAIL,
			TYRANT_KINGPIN_HEART,
			TRISALIM_TARANTULAS_VENOM_SAC,
			MANUAL_OF_MANACLES,
			PENITENTS_MANACLES,
			PENITENTS_MANACLES1
		};
	}
	
	private int findPetLvl(L2PcInstance player, int itemId)
	{
		L2Summon pet = player.getSummon();
		int level = 0;
		if (pet != null)
		{
			if (pet.getId() == 12564)
			{
				level = pet.getStat().getLevel();
			}
			else
			{
				L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
				if (item != null)
				{
					level = item.getEnchantLevel();
				}
			}
		}
		else
		{
			L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
			if (item != null)
			{
				level = item.getEnchantLevel();
			}
		}
		return level;
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		if (event.equalsIgnoreCase("Start"))
		{
			st.playSound("ItemSound.quest_accept");
			st.setState(State.STARTED);
			if (player.getLevel() <= 20)
			{
				htmltext = "30981-03.htm";
				st.set("cond", "1");
				st.set("cond", "2");
			}
			else if (player.getLevel() <= 30)
			{
				htmltext = "30981-04.htm";
				st.set("cond", "3");
			}
			else if (player.getLevel() <= 40)
			{
				htmltext = "30981-05.htm";
				st.set("cond", "4");
			}
			else
			{
				htmltext = "30981-06.htm";
				st.set("cond", "5");
			}
		}
		else if (event.equalsIgnoreCase("1"))
		{
			if (st.getQuestItemsCount(PENITENTS_MANACLES1) >= 1)
			{
				st.takeItems(PENITENTS_MANACLES1, -1);
			}
			if (st.getQuestItemsCount(PENITENTS_MANACLES2) >= 1)
			{
				st.takeItems(PENITENTS_MANACLES2, -1);
			}
			if (st.getQuestItemsCount(PENITENTS_MANACLES) >= 1)
			{
				st.takeItems(PENITENTS_MANACLES, -1);
			}
			htmltext = "30981-11.htm";
			st.set("cond", "16");
			if (player.getLevel() < 85)
			{
				st.set("level", String.valueOf(player.getLevel()));
				st.giveItems(PENITENTS_MANACLES, 1);
			}
			else
			{
				st.set("level", String.valueOf(84));
				st.giveItems(PENITENTS_MANACLES, 1);
			}
		}
		else if (event.equalsIgnoreCase("2"))
		{
			htmltext = "30981-14.htm";
		}
		else if (event.equalsIgnoreCase("3"))
		{
			int pLevel = findPetLvl(player, PENITENTS_MANACLES);
			int level = player.getLevel();
			int oLevel = st.getInt("level");
			L2Summon pet = player.getSummon();
			if ((pet != null) && (pet.getId() == 12564))
			{
				htmltext = "30981-16.htm";
			}
			else
			{
				int pkRemove = 0;
				if (level > oLevel)
				{
					pkRemove = pLevel - level;
				}
				else
				{
					pkRemove = pLevel - oLevel;
				}
				if (pkRemove < 0)
				{
					pkRemove = 0;
				}
				pkRemove = st.getRandom(10 + pkRemove) + 1;
				if (player.getPkKills() <= pkRemove)
				{
					st.giveItems(PENITENTS_MANACLES2, 1);
					st.takeItems(PENITENTS_MANACLES, 1);
					htmltext = "30981-15.htm";
					player.setPkKills(0);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(true);
				}
				else
				{
					st.giveItems(PENITENTS_MANACLES2, 1);
					st.takeItems(PENITENTS_MANACLES, 1);
					htmltext = "30981-17.htm";
					int newPkCount = player.getPkKills() - pkRemove;
					player.setPkKills(newPkCount);
					st.set("level", "0");
				}
			}
		}
		else if (event.equalsIgnoreCase("4"))
		{
			htmltext = "30981-19.htm";
		}
		else if (event.equalsIgnoreCase("Quit"))
		{
			htmltext = "30981-20.htm";
			st.playSound("ItemSound.quest_finish");
			st.takeItems(SCAVENGER_WERERAT_SKULL, -1);
			st.takeItems(TUREK_WARHOUND_TAIL, -1);
			st.takeItems(TYRANT_KINGPIN_HEART, -1);
			st.takeItems(TRISALIM_TARANTULAS_VENOM_SAC, -1);
			st.takeItems(PENITENTS_MANACLES1, -1);
			st.takeItems(MANUAL_OF_MANACLES, -1);
			st.takeItems(PENITENTS_MANACLES, -1);
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		int condition = st.getInt("cond");
		int npcId = npc.getId();
		int id = st.getState();
		switch (npcId)
		{
			case BLACK_JUDGE:
				if (id == State.CREATED)
				{
					if (player.getPkKills() >= 1)
					{
						htmltext = "30981-02.htm";
					}
					else
					{
						htmltext = "30981-01.htm";
					}
					st.exitQuest(true);
				}
				else if (condition <= 9)
				{
					htmltext = "30981-07.htm";
				}
				else if ((condition == 13) && (st.getQuestItemsCount(PENITENTS_MANACLES2) > 0))
				{
					htmltext = "30981-10.htm";
				}
				else if ((condition <= 13) && (condition > 9) && (st.getQuestItemsCount(MANUAL_OF_MANACLES) == 0))
				{
					htmltext = "30981-08.htm";
					st.set("cond", "14");
					st.giveItems(MANUAL_OF_MANACLES, 1);
				}
				else if ((condition == 14) && (st.getQuestItemsCount(MANUAL_OF_MANACLES) > 0))
				{
					htmltext = "30981-09.htm";
				}
				else if ((condition == 15) && (st.getQuestItemsCount(PENITENTS_MANACLES1) > 0))
				{
					htmltext = "30981-10.htm";
				}
				else if (condition >= 16)
				{
					if (st.getQuestItemsCount(PENITENTS_MANACLES) > 0)
					{
						int plevel = findPetLvl(player, PENITENTS_MANACLES);
						int level = player.getLevel();
						if (st.getInt("level") > level)
						{
							level = st.getInt("level");
						}
						if (plevel > 0)
						{
							if (plevel > level)
							{
								htmltext = "30981-13.htm";
							}
							else
							{
								htmltext = "30981-12.htm";
							}
						}
						else
						{
							htmltext = "30981-12.htm";
						}
					}
					else
					{
						htmltext = "30981-18.htm";
					}
				}
				break;
			case KATARI:
				if (condition == 2)
				{
					st.set("cond", "6");
					htmltext = "30668-01.htm";
				}
				else if (condition == 6)
				{
					if (st.getQuestItemsCount(SCAVENGER_WERERAT_SKULL) < 10)
					{
						htmltext = "30668-02.htm";
					}
					else
					{
						st.set("cond", "10");
						htmltext = "30668-03.htm";
						st.takeItems(SCAVENGER_WERERAT_SKULL, -1);
					}
				}
				else if (condition == 10)
				{
					htmltext = "30668-04.htm";
				}
				break;
			case PIOTUR:
				if (condition == 3)
				{
					st.set("cond", "7");
					htmltext = "30597-01.htm";
				}
				else if (condition == 7)
				{
					if (st.getQuestItemsCount(TUREK_WARHOUND_TAIL) < 10)
					{
						htmltext = "30597-02.htm";
					}
					else
					{
						st.set("cond", "11");
						htmltext = "30597-03.htm";
						st.takeItems(TUREK_WARHOUND_TAIL, -1);
					}
				}
				else if (condition == 11)
				{
					htmltext = "30597-04.htm";
				}
				break;
			case CASIAN:
				if (condition == 4)
				{
					st.set("cond", "8");
					htmltext = "30612-01.htm";
				}
				else if (condition == 8)
				{
					if (st.getQuestItemsCount(TYRANT_KINGPIN_HEART) < 1)
					{
						htmltext = "30612-02.htm";
					}
					else
					{
						st.set("cond", "12");
						htmltext = "30612-03.htm";
						st.takeItems(TYRANT_KINGPIN_HEART, -1);
					}
				}
				else if (condition == 12)
				{
					htmltext = "30612-04.htm";
				}
				break;
			case JOAN:
				if (condition == 5)
				{
					st.set("cond", "9");
					htmltext = "30718-01.htm";
				}
				else if (condition == 9)
				{
					if (st.getQuestItemsCount(TRISALIM_TARANTULAS_VENOM_SAC) < 3)
					{
						htmltext = "30718-02.htm";
					}
					else if (st.getQuestItemsCount(TRISALIM_TARANTULAS_VENOM_SAC) >= 3)
					{
						st.set("cond", "13");
						htmltext = "30718-03.htm";
						st.takeItems(TRISALIM_TARANTULAS_VENOM_SAC, -1);
					}
				}
				else if (condition == 13)
				{
					htmltext = "30718-04.htm";
				}
				break;
			case PUSHKIN:
				if (condition >= 14)
				{
					if (st.getQuestItemsCount(MANUAL_OF_MANACLES) == 1)
					{
						if ((st.getQuestItemsCount(SILVER_NUGGET) < 10) || (st.getQuestItemsCount(STEEL) < 5) || (st.getQuestItemsCount(ADAMANTINE_NUGGET) < 2) || (st.getQuestItemsCount(COKES) < 10) || (st.getQuestItemsCount(BLACKSMITHS_FRAME) < 1))
						{
							htmltext = "30300-02.htm";
						}
						else if ((st.getQuestItemsCount(SILVER_NUGGET) >= 10) && (st.getQuestItemsCount(STEEL) >= 5) && (st.getQuestItemsCount(ADAMANTINE_NUGGET) >= 2) && (st.getQuestItemsCount(COKES) >= 10) && (st.getQuestItemsCount(BLACKSMITHS_FRAME) >= 1))
						{
							htmltext = "30300-02.htm";
							st.set("cond", "15");
							st.takeItems(MANUAL_OF_MANACLES, 1);
							st.takeItems(SILVER_NUGGET, 10);
							st.takeItems(ADAMANTINE_NUGGET, 2);
							st.takeItems(COKES, 10);
							st.takeItems(STEEL, 5);
							st.takeItems(BLACKSMITHS_FRAME, 1);
							st.giveItems(PENITENTS_MANACLES1, 1);
							st.playSound("ItemSound.quest_middle");
						}
					}
					else if ((st.getQuestItemsCount(PENITENTS_MANACLES1) > 0) || (st.getQuestItemsCount(PENITENTS_MANACLES) > 0) || (st.getQuestItemsCount(PENITENTS_MANACLES2) > 0))
					{
						htmltext = "30300-03.htm";
					}
				}
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		if (st.getState() != State.STARTED)
		{
			return null;
		}
		final int condition = st.getInt("cond");
		final int npcId = npc.getId();
		long skulls = st.getQuestItemsCount(SCAVENGER_WERERAT_SKULL);
		long tails = st.getQuestItemsCount(TUREK_WARHOUND_TAIL);
		long heart = st.getQuestItemsCount(TYRANT_KINGPIN_HEART);
		long sacs = st.getQuestItemsCount(TRISALIM_TARANTULAS_VENOM_SAC);
		switch (npcId)
		{
			case SCAVENGER_WERERAT:
				if ((condition == 6) && (skulls < 10))
				{
					st.giveItems(SCAVENGER_WERERAT_SKULL, 1);
					if (st.getQuestItemsCount(SCAVENGER_WERERAT_SKULL) == 10)
					{
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
				break;
			case TUREK_WARHOUND:
				if ((condition == 7) && (tails < 10))
				{
					st.giveItems(TUREK_WARHOUND_TAIL, 1);
					if (st.getQuestItemsCount(TUREK_WARHOUND_TAIL) == 10)
					{
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
				break;
			case TYRANT_KINGPIN:
				if ((condition == 8) && (heart < 1))
				{
					st.giveItems(TYRANT_KINGPIN_HEART, 1);
					st.playSound("ItemSound.quest_middle");
				}
				break;
			case TRISALIM_TARANTULA:
				if ((condition == 9) && (sacs < 3))
				{
					st.giveItems(TRISALIM_TARANTULAS_VENOM_SAC, 1);
					if (st.getQuestItemsCount(TRISALIM_TARANTULAS_VENOM_SAC) == 3)
					{
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _422_RepentYourSins(422, qn, "");
	}
}