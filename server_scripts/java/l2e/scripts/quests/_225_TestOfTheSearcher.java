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
 * Created by LordWinter 16.06.2013 Based on L2J Eternity-World
 */
public class _225_TestOfTheSearcher extends Quest
{
	private static final String qn = "_225_TestOfTheSearcher";
	
	private static final int Luther = 30690;
	private static final int Alex = 30291;
	private static final int Tyra = 30420;
	private static final int Chest = 30628;
	private static final int Leirynn = 30728;
	private static final int Borys = 30729;
	private static final int Jax = 30730;
	private static final int Tree = 30627;
	
	private static final int LuthersLetter = 2784;
	private static final int AlexsWarrant = 2785;
	private static final int Leirynns1stOrder = 2786;
	private static final int DeluTotem = 2787;
	private static final int Leirynns2ndOrder = 2788;
	private static final int ChiefKalkisFang = 2789;
	private static final int AlexsRecommend = 2808;
	private static final int LambertsMap = 2792;
	private static final int LeirynnsReport = 2790;
	private static final int AlexsLetter = 2793;
	private static final int StrangeMap = 2791;
	private static final int AlexsOrder = 2794;
	private static final int CombinedMap = 2805;
	private static final int GoldBar = 2807;
	private static final int WineCatalog = 2795;
	private static final int OldOrder = 2799;
	private static final int MalrukianWine = 2798;
	private static final int TyrasContract = 2796;
	private static final int RedSporeDust = 2797;
	private static final int JaxsDiary = 2800;
	private static final int SoltsMap = 2803;
	private static final int MakelsMap = 2804;
	private static final int RustedKey = 2806;
	private static final int TornMapPiece1st = 2801;
	private static final int TornMapPiece2st = 2802;
	
	private static final int MarkOfSearcher = 2809;
	
	private static final int DeluLizardmanShaman = 20781;
	private static final int DeluLizardmanAssassin = 27094;
	private static final int DeluChiefKalkis = 27093;
	private static final int GiantFungus = 20555;
	private static final int RoadScavenger = 20551;
	private static final int HangmanTree = 20144;
	
	private static final int[][] DROPLIST_COND =
	{
		{
			3,
			4,
			DeluLizardmanShaman,
			0,
			DeluTotem,
			10,
			100,
			1
		},
		{
			3,
			4,
			DeluLizardmanAssassin,
			0,
			DeluTotem,
			10,
			100,
			1
		},
		{
			10,
			11,
			GiantFungus,
			0,
			RedSporeDust,
			10,
			100,
			1
		}
	};
	
	public _225_TestOfTheSearcher(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Luther);
		addTalkId(Luther);
		addTalkId(Alex);
		addTalkId(Leirynn);
		addTalkId(Borys);
		addTalkId(Tyra);
		addTalkId(Jax);
		addTalkId(Tree);
		addTalkId(Chest);
		
		addKillId(DeluChiefKalkis);
		addKillId(RoadScavenger);
		addKillId(HangmanTree);
		for (int[] element : DROPLIST_COND)
		{
			addKillId(element[2]);
		}
		
		questItemIds = new int[]
		{
			DeluTotem,
			RedSporeDust,
			LuthersLetter,
			AlexsWarrant,
			Leirynns1stOrder,
			Leirynns2ndOrder,
			LeirynnsReport,
			ChiefKalkisFang,
			StrangeMap,
			LambertsMap,
			AlexsLetter,
			AlexsOrder,
			WineCatalog,
			TyrasContract,
			OldOrder,
			MalrukianWine,
			JaxsDiary,
			TornMapPiece1st,
			TornMapPiece2st,
			SoltsMap,
			MakelsMap,
			RustedKey,
			CombinedMap
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
		
		if (event.equalsIgnoreCase("30690-05.htm"))
		{
			st.giveItems(LuthersLetter, 1);
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30291-07.htm"))
		{
			st.takeItems(LeirynnsReport, -1);
			st.takeItems(StrangeMap, -1);
			st.giveItems(LambertsMap, 1);
			st.giveItems(AlexsLetter, 1);
			st.giveItems(AlexsOrder, 1);
			st.set("cond", "8");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30420-01a.htm"))
		{
			st.takeItems(WineCatalog, -1);
			st.giveItems(TyrasContract, 1);
			st.set("cond", "10");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30730-01d.htm"))
		{
			st.takeItems(OldOrder, -1);
			st.giveItems(JaxsDiary, 1);
			st.set("cond", "14");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30627-01a.htm"))
		{
			if (st.getQuestItemsCount(RustedKey) == 0)
			{
				st.giveItems(RustedKey, 1);
			}
			st.addSpawn(Chest, 10098, 157287, -2406, 300000);
			st.set("cond", "17");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30628-01a.htm"))
		{
			st.takeItems(RustedKey, -1);
			st.giveItems(GoldBar, 20);
			st.set("cond", "18");
			st.playSound("ItemSound.quest_middle");
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
		
		switch (st.getState())
		{
			case State.CREATED:
				if (npcId == Luther)
				{
					if ((player.getClassId().getId() == 0x07) || (player.getClassId().getId() == 0x16) || (player.getClassId().getId() == 0x23) || (player.getClassId().getId() == 0x36))
					{
						if (player.getLevel() >= 39)
						{
							if (player.getClassId().getId() == 0x36)
							{
								htmltext = "30690-04.htm";
							}
							else
							{
								htmltext = "30690-03.htm";
							}
						}
						else
						{
							htmltext = "30690-02.htm";
							st.exitQuest(true);
						}
					}
					else
					{
						htmltext = "30690-01.htm";
						st.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				if (npcId == Luther)
				{
					if (cond == 1)
					{
						htmltext = "30690-06.htm";
					}
					else if ((cond > 1) && (cond < 16))
					{
						htmltext = "30623-17.htm";
					}
					else if (cond == 19)
					{
						htmltext = "30690-08.htm";
						st.addExpAndSp(894888, 61408);
						st.giveItems(57, 161806);
						st.giveItems(7562, 82);
						st.takeItems(AlexsRecommend, -1);
						st.giveItems(MarkOfSearcher, 1);
						st.set("cond", "0");
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
					}
				}
				else if (npcId == Alex)
				{
					if (cond == 1)
					{
						htmltext = "30291-01.htm";
						st.takeItems(LuthersLetter, -1);
						st.giveItems(AlexsWarrant, 1);
						st.set("cond", "2");
						st.playSound("ItemSound.quest_middle");
					}
					else if (cond == 2)
					{
						htmltext = "30291-02.htm";
					}
					else if ((cond > 2) && (cond < 7))
					{
						htmltext = "30291-03.htm";
					}
					else if (cond == 7)
					{
						htmltext = "30291-04.htm";
					}
					else if (cond == 8)
					{
						htmltext = "30291-08.htm";
					}
					else if ((cond == 13) || (cond == 14))
					{
						htmltext = "30291-09.htm";
					}
					else if (cond == 18)
					{
						st.takeItems(AlexsOrder, -1);
						st.takeItems(CombinedMap, -1);
						st.takeItems(GoldBar, -1);
						st.giveItems(AlexsRecommend, 1);
						htmltext = "30291-11.htm";
						st.set("cond", "19");
						st.playSound("ItemSound.quest_middle");
					}
					else if (cond == 19)
					{
						htmltext = "30291-12.htm";
					}
					
				}
				else if (npcId == Leirynn)
				{
					if (cond == 2)
					{
						htmltext = "30728-01.htm";
						st.takeItems(AlexsWarrant, -1);
						st.giveItems(Leirynns1stOrder, 1);
						st.set("cond", "3");
						st.playSound("ItemSound.quest_middle");
					}
					else if (cond == 3)
					{
						htmltext = "30728-02.htm";
					}
					else if (cond == 4)
					{
						htmltext = "30728-03.htm";
						st.takeItems(DeluTotem, -1);
						st.takeItems(Leirynns1stOrder, -1);
						st.giveItems(Leirynns2ndOrder, 1);
						st.set("cond", "5");
						st.playSound("ItemSound.quest_middle");
					}
					else if (cond == 5)
					{
						htmltext = "30728-04.htm";
					}
					else if (cond == 6)
					{
						st.takeItems(ChiefKalkisFang, -1);
						st.takeItems(Leirynns2ndOrder, -1);
						st.giveItems(LeirynnsReport, 1);
						htmltext = "30728-05.htm";
						st.set("cond", "7");
						st.playSound("ItemSound.quest_middle");
					}
					else if (cond == 7)
					{
						htmltext = "30728-06.htm";
					}
					else if (cond == 8)
					{
						htmltext = "30728-07.htm";
					}
				}
				else if (npcId == Borys)
				{
					if (cond == 8)
					{
						st.takeItems(AlexsLetter, -1);
						st.giveItems(WineCatalog, 1);
						htmltext = "30729-01.htm";
						st.set("cond", "9");
						st.playSound("ItemSound.quest_middle");
					}
					else if (cond == 9)
					{
						htmltext = "30729-02.htm";
					}
					else if (cond == 12)
					{
						st.takeItems(WineCatalog, -1);
						st.takeItems(MalrukianWine, -1);
						st.giveItems(OldOrder, 1);
						htmltext = "30729-03.htm";
						st.set("cond", "13");
						st.playSound("ItemSound.quest_middle");
					}
					else if (cond == 13)
					{
						htmltext = "30729-04.htm";
					}
					else if ((cond >= 8) && (cond <= 14))
					{
						htmltext = "30729-05.htm";
					}
				}
				else if (npcId == Tyra)
				{
					if (cond == 9)
					{
						htmltext = "30420-01.htm";
					}
					else if (cond == 10)
					{
						htmltext = "30420-02.htm";
					}
					else if (cond == 11)
					{
						st.takeItems(TyrasContract, -1);
						st.takeItems(RedSporeDust, -1);
						st.giveItems(MalrukianWine, 1);
						htmltext = "30420-03.htm";
						st.set("cond", "12");
						st.playSound("ItemSound.quest_middle");
					}
					else if ((cond == 12) || (cond == 13))
					{
						htmltext = "30420-04.htm";
					}
				}
				else if (npcId == Jax)
				{
					if (cond == 13)
					{
						htmltext = "30730-01.htm";
					}
					else if (cond == 14)
					{
						htmltext = "30730-02.htm";
					}
					else if (cond == 15)
					{
						st.takeItems(SoltsMap, -1);
						st.takeItems(MakelsMap, -1);
						st.takeItems(LambertsMap, -1);
						st.takeItems(JaxsDiary, -1);
						st.giveItems(CombinedMap, 1);
						htmltext = "30730-03.htm";
						st.set("cond", "16");
					}
					else if (cond == 16)
					{
						htmltext = "30730-04.htm";
					}
				}
				else if (npcId == Tree)
				{
					if ((cond == 16) || (cond == 17))
					{
						htmltext = "30627-01.htm";
					}
				}
				else if (npcId == Chest)
				{
					if (cond == 17)
					{
						htmltext = "30628-01.htm";
					}
					else
					{
						htmltext = "30628-02.htm";
					}
				}
				break;
			case State.COMPLETED:
				if (npcId == Luther)
				{
					htmltext = getAlreadyCompletedMsg(player);
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
		
		for (int[] element : DROPLIST_COND)
		{
			if ((cond == element[0]) && (npcId == element[2]))
			{
				if ((element[3] == 0) || (st.getQuestItemsCount(element[3]) > 0))
				{
					if (element[5] == 0)
					{
						st.rollAndGive(element[4], element[7], element[6]);
					}
					else if (st.rollAndGive(element[4], element[7], element[7], element[5], element[6]))
					{
						if ((element[1] != cond) && (element[1] != 0))
						{
							st.setCond(Integer.valueOf(element[1]));
							st.playSound("Itemsound.quest_itemget");
						}
					}
				}
			}
		}
		if ((cond == 5) && (npcId == DeluChiefKalkis))
		{
			if (st.getQuestItemsCount(StrangeMap) == 0)
			{
				st.giveItems(StrangeMap, 1);
			}
			if (st.getQuestItemsCount(ChiefKalkisFang) == 0)
			{
				st.giveItems(ChiefKalkisFang, 1);
			}
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "6");
		}
		else if (cond == 14)
		{
			if ((npcId == RoadScavenger) && (st.getQuestItemsCount(SoltsMap) == 0))
			{
				st.giveItems(TornMapPiece1st, 1);
				if (st.getQuestItemsCount(TornMapPiece1st) >= 4)
				{
					st.takeItems(TornMapPiece1st, -1);
					st.giveItems(SoltsMap, 1);
				}
			}
			else if ((npcId == HangmanTree) && (st.getQuestItemsCount(MakelsMap) == 0))
			{
				st.giveItems(TornMapPiece2st, 1);
				if (st.getQuestItemsCount(TornMapPiece2st) >= 4)
				{
					st.takeItems(TornMapPiece2st, -1);
					st.giveItems(MakelsMap, 1);
				}
			}
			if ((st.getQuestItemsCount(SoltsMap) != 0) && (st.getQuestItemsCount(MakelsMap) != 0))
			{
				st.set("cond", "15");
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _225_TestOfTheSearcher(225, qn, "");
	}
}