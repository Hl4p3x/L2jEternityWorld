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
 * Created by LordWinter 25.06.2011 Based on L2J Eternity-World
 */
public final class _229_TestOfWitchcraft extends Quest
{
	private static final String qn = "_229_TestOfWitchcraft";
	
	// NPCs
	private static final int ORIM = 30630;
	private static final int ALEXANDRIA = 30098;
	private static final int IKER = 30110;
	private static final int KAIRA = 30476;
	private static final int LARA = 30063;
	private static final int RODERIK = 30631;
	private static final int NESTLE = 30314;
	private static final int LEOPOLD = 30435;
	private static final int VASPER = 30417;
	private static final int VADIN = 30188;
	private static final int EVERT = 30633;
	private static final int ENDRIGO = 30632;
	
	// ITEMs
	private static final int DIMENSION_DIAMONS = 7562;
	
	// QUEST ITEMs
	private static final int MARK_OF_WITCH_CRAFT = 3307;
	private static final int ORIMS_DIAGRAM = 3308;
	private static final int ALEXANDRIAS_BOOK = 3309;
	private static final int IKERS_LIST = 3310;
	private static final int DIRE_WYRM_FANG = 3311;
	private static final int LETO_LIZARDMAN_CHARM = 3312;
	private static final int ENCHANTED_GOLEM_HEARTSTONE = 3313;
	private static final int LARAS_MEMO = 3314;
	private static final int NESTLES_MEMO = 3315;
	private static final int LEOPOLDS_JOURNAL = 3316;
	private static final int AKLANTOOTH_1STGEM = 3317;
	private static final int AKLANTOOTH_2NDGEM = 3318;
	private static final int AKLANTOOTH_3RDGEM = 3319;
	private static final int AKLANTOOTH_4THGEM = 3320;
	private static final int AKLANTOOTH_5THGEM = 3321;
	private static final int AKLANTOOTH_6THGEM = 3322;
	private static final int BRIMSTONE_1ST = 3323;
	private static final int ORIMS_INSTRUCTIONS = 3324;
	private static final int ORIMS_1STLETTER = 3325;
	private static final int ORIMS_2NDLETTER = 3326;
	private static final int SIR_VASPERS_LETTER = 3327;
	private static final int VADINS_CRUCIFIX = 3328;
	private static final int TAMLIN_ORC_AMULET = 3329;
	private static final int VADINS_SANCTIONS = 3330;
	private static final int IKERS_AMULET = 3331;
	private static final int SOULTRAP_CRYSTAL = 3332;
	private static final int PURGATORY_KEY = 3333;
	private static final int ZERUEL_BIND_CRYSTAL = 3334;
	private static final int BRIMSTONE_2ND = 3335;
	private static final int SWORD_OF_BINDING = 3029;
	
	// MOBs
	private static final int DIRE_WYRM = 20557;
	private static final int ENCHANTED_STONE_GOLEM = 20565;
	private static final int LETO_LIZARDMAN = 20577;
	private static final int LETO_LIZARDMAN_ARCHER = 20578;
	private static final int LETO_LIZARDMAN_SOLDIER = 20579;
	private static final int LETO_LIZARDMAN_WARRIOR = 20580;
	private static final int LETO_LIZARDMAN_SHAMAN = 20581;
	private static final int LETO_LIZARDMAN_OVERLORD = 20582;
	private static final int NAMELESS_REVENANT = 27099;
	private static final int SKELETON_MERCENARY = 27100;
	private static final int DREVANUL_PRINCE_ZERUEL = 27101;
	private static final int TAMLIN_ORC = 20601;
	private static final int TAMLIN_ORC_ARCHER = 20602;
	
	public _229_TestOfWitchcraft(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(ORIM);
		addTalkId(ORIM);
		addTalkId(ALEXANDRIA);
		addTalkId(IKER);
		addTalkId(KAIRA);
		addTalkId(LARA);
		addTalkId(RODERIK);
		addTalkId(NESTLE);
		addTalkId(LEOPOLD);
		addTalkId(VASPER);
		addTalkId(VADIN);
		addTalkId(EVERT);
		addTalkId(ENDRIGO);
		
		addKillId(SKELETON_MERCENARY);
		addKillId(DREVANUL_PRINCE_ZERUEL);
		addKillId(DIRE_WYRM);
		addKillId(ENCHANTED_STONE_GOLEM);
		addKillId(LETO_LIZARDMAN);
		addKillId(LETO_LIZARDMAN_ARCHER);
		addKillId(LETO_LIZARDMAN_SOLDIER);
		addKillId(LETO_LIZARDMAN_WARRIOR);
		addKillId(LETO_LIZARDMAN_SHAMAN);
		addKillId(LETO_LIZARDMAN_OVERLORD);
		addKillId(NAMELESS_REVENANT);
		addKillId(SKELETON_MERCENARY);
		addKillId(DREVANUL_PRINCE_ZERUEL);
		addKillId(TAMLIN_ORC);
		addKillId(TAMLIN_ORC_ARCHER);
		
		questItemIds = new int[]
		{
			ORIMS_DIAGRAM,
			ORIMS_INSTRUCTIONS,
			ORIMS_1STLETTER,
			ORIMS_2NDLETTER,
			BRIMSTONE_1ST,
			ALEXANDRIAS_BOOK,
			IKERS_LIST,
			AKLANTOOTH_1STGEM,
			SOULTRAP_CRYSTAL,
			IKERS_AMULET,
			AKLANTOOTH_2NDGEM,
			LARAS_MEMO,
			NESTLES_MEMO,
			LEOPOLDS_JOURNAL,
			AKLANTOOTH_4THGEM,
			AKLANTOOTH_5THGEM,
			AKLANTOOTH_6THGEM,
			SIR_VASPERS_LETTER,
			SWORD_OF_BINDING,
			VADINS_CRUCIFIX,
			VADINS_SANCTIONS,
			BRIMSTONE_2ND,
			PURGATORY_KEY,
			ZERUEL_BIND_CRYSTAL,
			DIRE_WYRM_FANG,
			ENCHANTED_GOLEM_HEARTSTONE,
			LETO_LIZARDMAN_CHARM,
			AKLANTOOTH_3RDGEM,
			TAMLIN_ORC_AMULET,
			DIMENSION_DIAMONS
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
		
		if (event.equalsIgnoreCase("30630-08.htm"))
		{
			st.giveItems(ORIMS_DIAGRAM, 1);
			st.giveItems(DIMENSION_DIAMONS, 104);
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30098-03.htm"))
		{
			st.giveItems(ALEXANDRIAS_BOOK, 1);
			st.takeItems(ORIMS_DIAGRAM, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30110-03.htm"))
		{
			st.giveItems(IKERS_LIST, 1);
		}
		else if (event.equalsIgnoreCase("30476-02.htm"))
		{
			st.giveItems(AKLANTOOTH_2NDGEM, 1);
		}
		else if (event.equalsIgnoreCase("30063-02.htm"))
		{
			if (st.getQuestItemsCount(LARAS_MEMO) < 1)
			{
				st.giveItems(LARAS_MEMO, 1);
			}
		}
		else if (event.equalsIgnoreCase("30314-02.htm"))
		{
			if (st.getQuestItemsCount(NESTLES_MEMO) < 1)
			{
				st.giveItems(NESTLES_MEMO, 1);
			}
		}
		else if (event.equalsIgnoreCase("30435-02.htm"))
		{
			st.takeItems(NESTLES_MEMO, 1);
			st.giveItems(LEOPOLDS_JOURNAL, 1);
		}
		else if (event.equalsIgnoreCase("30630-14.htm"))
		{
			if (st.getQuestItemsCount(BRIMSTONE_1ST) == 0)
			{
				st.takeItems(ALEXANDRIAS_BOOK, 1);
				st.takeItems(AKLANTOOTH_1STGEM, 1);
				st.takeItems(AKLANTOOTH_2NDGEM, 1);
				st.takeItems(AKLANTOOTH_3RDGEM, 1);
				st.takeItems(AKLANTOOTH_4THGEM, 1);
				st.takeItems(AKLANTOOTH_5THGEM, 1);
				st.takeItems(AKLANTOOTH_6THGEM, 1);
				st.giveItems(BRIMSTONE_1ST, 1);
				st.set("cond", "4");
				st.playSound("ItemSound.quest_middle");
				st.set("id", "1");
				st.addSpawn(DREVANUL_PRINCE_ZERUEL);
			}
		}
		else if (event.equalsIgnoreCase("30630-16.htm"))
		{
			htmltext = "30630-16.htm";
			st.takeItems(BRIMSTONE_1ST, 1);
			st.giveItems(ORIMS_INSTRUCTIONS, 1);
			st.giveItems(ORIMS_1STLETTER, 1);
			st.giveItems(ORIMS_2NDLETTER, 1);
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30110-08.htm"))
		{
			if (st.getQuestItemsCount(ORIMS_2NDLETTER) > 0)
			{
				st.takeItems(ORIMS_2NDLETTER, 1);
				st.giveItems(SOULTRAP_CRYSTAL, 1);
				st.giveItems(IKERS_AMULET, 1);
				if (st.getQuestItemsCount(SWORD_OF_BINDING) > 0)
				{
					st.set("cond", "7");
				}
			}
		}
		else if (event.equalsIgnoreCase("30417-03.htm"))
		{
			st.takeItems(ORIMS_1STLETTER, 1);
			st.giveItems(SIR_VASPERS_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("30633-02.htm"))
		{
			
			st.set("id", "2");
			st.set("cond", "9");
			if (st.getQuestItemsCount(BRIMSTONE_2ND) == 0)
			{
				st.giveItems(BRIMSTONE_2ND, 1);
			}
			st.addSpawn(DREVANUL_PRINCE_ZERUEL);
		}
		else if (event.equalsIgnoreCase("30630-20.htm"))
		{
			st.takeItems(ZERUEL_BIND_CRYSTAL, 1);
		}
		else if (event.equalsIgnoreCase("30630-21.htm"))
		{
			st.takeItems(PURGATORY_KEY, 1);
		}
		else if (event.equalsIgnoreCase("30630-22.htm"))
		{
			st.takeItems(SWORD_OF_BINDING, 1);
			st.takeItems(IKERS_AMULET, 1);
			st.takeItems(ORIMS_INSTRUCTIONS, 1);
			st.addExpAndSp(2058244, 141240);
			st.giveItems(57, 372154);
			st.giveItems(MARK_OF_WITCH_CRAFT, 1);
			st.exitQuest(true);
			st.setState(State.COMPLETED);
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		
		switch (npcId)
		{
			case ORIM:
				if (st.getQuestItemsCount(MARK_OF_WITCH_CRAFT) != 0)
				{
					st.exitQuest(true);
					return htmltext;
				}
				switch (cond)
				{
					case 0:
						if ((player.getClassId().getId() == 0x0b) || (player.getClassId().getId() == 0x04) || (player.getClassId().getId() == 0x20))
						{
							if (player.getLevel() < 39)
							{
								htmltext = "30630-02.htm";
								st.exitQuest(true);
							}
							else if (player.getClassId().getId() == 0x0b)
							{
								htmltext = "30630-03.htm";
							}
							else
							{
								htmltext = "30630-05.htm";
							}
						}
						else
						{
							htmltext = "30630-01.htm";
							st.exitQuest(true);
						}
						break;
					case 1:
						htmltext = "30630-09.htm";
						break;
					case 2:
						htmltext = "30630-10.htm";
						break;
					case 3:
						if (st.getInt("id") == 1)
						{
							htmltext = "30630-11.htm";
						}
						break;
					case 5:
						htmltext = "30630-15.htm";
						break;
					case 6:
						htmltext = "30630-17.htm";
						break;
					case 7:
						htmltext = "30630-18.htm";
						st.set("cond", "8");
						break;
					case 10:
						if (st.getQuestItemsCount(ZERUEL_BIND_CRYSTAL) != 0)
						{
							htmltext = "30630-19.htm";
						}
						else if (st.getQuestItemsCount(PURGATORY_KEY) != 0)
						{
							htmltext = "30630-20.htm";
						}
						else
						{
							htmltext = "30630-21.htm";
						}
						break;
				}
				break;
			case ALEXANDRIA:
				switch (cond)
				{
					case 1:
						htmltext = "30098-01.htm";
						break;
					case 2:
						htmltext = "30098-04.htm";
						break;
					default:
						htmltext = "30098-05.htm";
						break;
				}
				break;
			case IKER:
				if (cond == 2)
				{
					if ((st.getQuestItemsCount(AKLANTOOTH_1STGEM) == 0) && (st.getQuestItemsCount(IKERS_LIST) == 0))
					{
						htmltext = "30110-01.htm";
					}
					else if ((st.getQuestItemsCount(IKERS_LIST) > 0) && ((st.getQuestItemsCount(DIRE_WYRM_FANG) < 20) || (st.getQuestItemsCount(LETO_LIZARDMAN_CHARM) < 20) || (st.getQuestItemsCount(ENCHANTED_GOLEM_HEARTSTONE) < 20)))
					{
						htmltext = "30110-04.htm";
					}
					else if ((st.getQuestItemsCount(AKLANTOOTH_1STGEM) == 0) && (st.getQuestItemsCount(IKERS_LIST) > 0))
					{
						st.takeItems(IKERS_LIST, 1);
						st.takeItems(DIRE_WYRM_FANG, 20);
						st.takeItems(LETO_LIZARDMAN_CHARM, 20);
						st.takeItems(ENCHANTED_GOLEM_HEARTSTONE, 20);
						st.giveItems(AKLANTOOTH_1STGEM, 1);
						htmltext = "30110-05.htm";
					}
					else if (st.getQuestItemsCount(AKLANTOOTH_1STGEM) == 1)
					{
						htmltext = "30110-06.htm";
					}
				}
				else if (cond == 6)
				{
					htmltext = "30110-07.htm";
				}
				else if (cond == 10)
				{
					htmltext = "30110-10.htm";
				}
				else
				{
					htmltext = "30110-09.htm";
				}
				break;
			case KAIRA:
				if (cond == 2)
				{
					if (st.getQuestItemsCount(AKLANTOOTH_2NDGEM) == 0)
					{
						htmltext = "30476-01.htm";
					}
					else
					{
						htmltext = "30476-03.htm";
					}
				}
				else if (cond > 2)
				{
					htmltext = "30476-04.htm";
				}
				break;
			case LARA:
				if (cond == 2)
				{
					if ((st.getQuestItemsCount(LARAS_MEMO) == 0) && (st.getQuestItemsCount(AKLANTOOTH_3RDGEM) == 0))
					{
						htmltext = "30063-01.htm";
					}
					else if ((st.getQuestItemsCount(LARAS_MEMO) == 1) && (st.getQuestItemsCount(AKLANTOOTH_3RDGEM) == 0))
					{
						htmltext = "30063-03.htm";
					}
					else if (st.getQuestItemsCount(AKLANTOOTH_3RDGEM) == 1)
					{
						htmltext = "30063-04.htm";
					}
				}
				else if (cond > 2)
				{
					htmltext = "30063-05.htm";
				}
				break;
			case RODERIK:
				if ((cond == 2) && (st.getQuestItemsCount(LARAS_MEMO) > 0))
				{
					htmltext = "30631-01.htm";
				}
				break;
			case NESTLE:
				if (cond == 2)
				{
					if ((st.getQuestItemsCount(AKLANTOOTH_1STGEM) > 0) && (st.getQuestItemsCount(AKLANTOOTH_2NDGEM) > 0) && (st.getQuestItemsCount(AKLANTOOTH_3RDGEM) > 0))
					{
						htmltext = "30314-01.htm";
					}
					else
					{
						htmltext = "30314-04.htm";
					}
				}
				break;
			case LEOPOLD:
				if ((cond == 2) && (st.getQuestItemsCount(NESTLES_MEMO) > 0))
				{
					if ((st.getQuestItemsCount(AKLANTOOTH_4THGEM) + st.getQuestItemsCount(AKLANTOOTH_5THGEM) + st.getQuestItemsCount(AKLANTOOTH_6THGEM)) == 0)
					{
						htmltext = "30435-01.htm";
					}
					else
					{
						htmltext = "30435-04.htm";
					}
				}
				else
				{
					htmltext = "30435-05.htm";
				}
				break;
			case VASPER:
				if (cond == 6)
				{
					if ((st.getQuestItemsCount(SIR_VASPERS_LETTER) > 0) || (st.getQuestItemsCount(VADINS_CRUCIFIX) > 0))
					{
						htmltext = "30417-04.htm";
					}
					else if (st.getQuestItemsCount(VADINS_SANCTIONS) == 0)
					{
						htmltext = "30417-01.htm";
					}
					else if (st.getQuestItemsCount(VADINS_SANCTIONS) != 0)
					{
						htmltext = "30417-05.htm";
						st.takeItems(VADINS_SANCTIONS, 1);
						st.giveItems(SWORD_OF_BINDING, 1);
						if (st.getQuestItemsCount(SOULTRAP_CRYSTAL) > 0)
						{
							st.set("cond", "7");
							st.playSound("ItemSound.quest_middle");
						}
					}
				}
				else if (cond == 7)
				{
					htmltext = "30417-06.htm";
				}
				break;
			case VADIN:
				if (cond == 6)
				{
					if (st.getQuestItemsCount(SIR_VASPERS_LETTER) != 0)
					{
						htmltext = "30188-01.htm";
						st.takeItems(SIR_VASPERS_LETTER, 1);
						st.giveItems(VADINS_CRUCIFIX, 1);
					}
					else if ((st.getQuestItemsCount(VADINS_CRUCIFIX) > 0) && (st.getQuestItemsCount(TAMLIN_ORC_AMULET) < 20))
					{
						htmltext = "30188-02.htm";
					}
					else if (st.getQuestItemsCount(TAMLIN_ORC_AMULET) >= 20)
					{
						htmltext = "30188-03.htm";
						st.takeItems(TAMLIN_ORC_AMULET, 20);
						st.takeItems(VADINS_CRUCIFIX, 1);
						st.giveItems(VADINS_SANCTIONS, 1);
					}
					else if (st.getQuestItemsCount(VADINS_SANCTIONS) > 0)
					{
						htmltext = "30188-04.htm";
					}
				}
				else if (cond == 7)
				{
					htmltext = "30188-05.htm";
				}
				break;
			case EVERT:
				if ((st.getInt("id") == 2) || ((cond == 8) && (st.getQuestItemsCount(BRIMSTONE_2ND) == 0)))
				{
					htmltext = "30633-01.htm";
				}
				else
				{
					htmltext = "30633-03.htm";
				}
				break;
			case ENDRIGO:
				if (cond == 2)
				{
					htmltext = "30632-01.htm";
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
		{
			return null;
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		
		if ((npcId == DIRE_WYRM) && (cond == 2) && (st.getQuestItemsCount(DIRE_WYRM_FANG) < 20) && (st.getQuestItemsCount(IKERS_LIST) > 0))
		{
			st.giveItems(DIRE_WYRM_FANG, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((npcId == ENCHANTED_STONE_GOLEM) && (80 >= getRandom(100)) && (cond == 2) && (st.getQuestItemsCount(ENCHANTED_GOLEM_HEARTSTONE) < 20) && (st.getQuestItemsCount(IKERS_LIST) > 0))
		{
			st.giveItems(ENCHANTED_GOLEM_HEARTSTONE, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((npcId == LETO_LIZARDMAN) && (50 >= getRandom(100)) && (cond == 2) && (st.getQuestItemsCount(LETO_LIZARDMAN_CHARM) < 20) && (st.getQuestItemsCount(IKERS_LIST) > 0))
		{
			st.giveItems(LETO_LIZARDMAN_CHARM, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((npcId == LETO_LIZARDMAN_ARCHER) && (50 >= getRandom(100)) && (cond == 2) && (st.getQuestItemsCount(LETO_LIZARDMAN_CHARM) < 20) && (st.getQuestItemsCount(IKERS_LIST) > 0))
		{
			st.giveItems(LETO_LIZARDMAN_CHARM, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((npcId == LETO_LIZARDMAN_SOLDIER) && (60 >= getRandom(100)) && (cond == 2) && (st.getQuestItemsCount(LETO_LIZARDMAN_CHARM) < 20) && (st.getQuestItemsCount(IKERS_LIST) > 0))
		{
			st.giveItems(LETO_LIZARDMAN_CHARM, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((npcId == LETO_LIZARDMAN_WARRIOR) && (65 >= getRandom(100)) && (cond == 2) && (st.getQuestItemsCount(LETO_LIZARDMAN_CHARM) < 20) && (st.getQuestItemsCount(IKERS_LIST) > 0))
		{
			st.giveItems(LETO_LIZARDMAN_CHARM, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((npcId == LETO_LIZARDMAN_SHAMAN) && (70 >= getRandom(100)) && (cond == 2) && (st.getQuestItemsCount(LETO_LIZARDMAN_CHARM) < 20) && (st.getQuestItemsCount(IKERS_LIST) > 0))
		{
			st.giveItems(LETO_LIZARDMAN_CHARM, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((npcId == LETO_LIZARDMAN_OVERLORD) && (70 >= getRandom(100)) && (cond == 2) && (st.getQuestItemsCount(LETO_LIZARDMAN_CHARM) < 20) && (st.getQuestItemsCount(IKERS_LIST) > 0))
		{
			st.giveItems(LETO_LIZARDMAN_CHARM, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((npcId == NAMELESS_REVENANT) && (cond == 2) && (st.getQuestItemsCount(AKLANTOOTH_3RDGEM) < 1) && (st.getQuestItemsCount(LARAS_MEMO) > 0))
		{
			st.giveItems(AKLANTOOTH_3RDGEM, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((npcId == TAMLIN_ORC) && (50 >= getRandom(100)) && (cond == 6) && (st.getQuestItemsCount(TAMLIN_ORC_AMULET) < 20) && (st.getQuestItemsCount(VADINS_CRUCIFIX) > 0))
		{
			st.giveItems(TAMLIN_ORC_AMULET, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((npcId == TAMLIN_ORC_ARCHER) && (55 >= getRandom(100)) && (cond == 6) && (st.getQuestItemsCount(TAMLIN_ORC_AMULET) < 20) && (st.getQuestItemsCount(VADINS_CRUCIFIX) > 0))
		{
			st.giveItems(TAMLIN_ORC_AMULET, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((cond == 2) && (st.getQuestItemsCount(LEOPOLDS_JOURNAL) > 0) && (npcId == SKELETON_MERCENARY))
		{
			if ((st.getQuestItemsCount(AKLANTOOTH_4THGEM) == 0) && (50 >= getRandom(100)))
			{
				st.giveItems(AKLANTOOTH_4THGEM, 1);
			}
			if ((st.getQuestItemsCount(AKLANTOOTH_5THGEM) == 0) && (50 >= getRandom(100)))
			{
				st.giveItems(AKLANTOOTH_5THGEM, 1);
			}
			if ((st.getQuestItemsCount(AKLANTOOTH_6THGEM) == 0) && (50 >= getRandom(100)))
			{
				st.giveItems(AKLANTOOTH_6THGEM, 1);
			}
			if ((st.getQuestItemsCount(AKLANTOOTH_4THGEM) != 0) && (st.getQuestItemsCount(AKLANTOOTH_5THGEM) != 0) && (st.getQuestItemsCount(AKLANTOOTH_6THGEM) != 0))
			{
				st.takeItems(LEOPOLDS_JOURNAL, 1);
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if ((cond == 4) && (npcId == DREVANUL_PRINCE_ZERUEL))
		{
			st.set("cond", "5");
			st.unset("id");
			st.playSound("ItemSound.quest_middle");
		}
		else if ((cond == 9) && (npcId == DREVANUL_PRINCE_ZERUEL))
		{
			if (player.getActiveWeaponItem().getId() == SWORD_OF_BINDING)
			{
				st.takeItems(BRIMSTONE_2ND, 1);
				st.takeItems(SOULTRAP_CRYSTAL, 1);
				st.giveItems(PURGATORY_KEY, 1);
				st.giveItems(ZERUEL_BIND_CRYSTAL, 1);
				st.unset("id");
				st.set("cond", "10");
				st.playSound("ItemSound.quest_middle");
				return "You have trapped the Soul of Drevanul Prince Zeruel";
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _229_TestOfWitchcraft(229, qn, "");
	}
}