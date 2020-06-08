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
 * Created by LordWinter 15.06.2013 Based on L2J Eternity-World
 */
public class _231_TestOfTheMaestro extends Quest
{
	private static final String qn = "_231_TestOfTheMaestro";
	
	private static final int Lockirin = 30531;
	private static final int Balanki = 30533;
	private static final int Arin = 30536;
	private static final int Filaur = 30535;
	private static final int Spiron = 30532;
	private static final int Croto = 30671;
	private static final int Kamur = 30675;
	private static final int Dubabah = 30672;
	private static final int Toma = 30556;
	private static final int Lorain = 30673;
	
	private static final int RecommendationOfBalanki = 2864;
	private static final int RecommendationOfFilaur = 2865;
	private static final int RecommendationOfArin = 2866;
	private static final int LetterOfSolderDetachment = 2868;
	private static final int PaintOfKamuru = 2869;
	private static final int NecklaceOfKamuru = 2870;
	private static final int PaintOfTeleportDevice = 2871;
	private static final int TeleportDevice = 2872;
	private static final int ArchitectureOfCruma = 2873;
	private static final int ReportOfCruma = 2874;
	private static final int IngredientsOfAntidote = 2875;
	private static final int StingerWaspNeedle = 2876;
	private static final int MarshSpidersWeb = 2877;
	private static final int BloodOfLeech = 2878;
	private static final int BrokenTeleportDevice = 2916;
	
	private static final int MarkOfMaestro = 2867;
	
	private static final int QuestMonsterEvilEyeLord = 27133;
	private static final int GiantMistLeech = 20225;
	private static final int StingerWasp = 20229;
	private static final int MarshSpider = 20233;
	
	private static final int[][] DROPLIST_COND =
	{
		{
			4,
			5,
			QuestMonsterEvilEyeLord,
			0,
			NecklaceOfKamuru,
			1,
			100,
			1
		},
		{
			13,
			0,
			GiantMistLeech,
			0,
			BloodOfLeech,
			10,
			100,
			1
		},
		{
			13,
			0,
			StingerWasp,
			0,
			StingerWaspNeedle,
			10,
			100,
			1
		},
		{
			13,
			0,
			MarshSpider,
			0,
			MarshSpidersWeb,
			10,
			100,
			1
		}
	};
	
	public _231_TestOfTheMaestro(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Lockirin);
		addTalkId(Lockirin);
		addTalkId(Balanki);
		addTalkId(Arin);
		addTalkId(Filaur);
		addTalkId(Spiron);
		addTalkId(Croto);
		addTalkId(Kamur);
		addTalkId(Dubabah);
		addTalkId(Toma);
		addTalkId(Lorain);
		
		for (int[] element : DROPLIST_COND)
		{
			addKillId(element[2]);
			registerQuestItems(element[4]);
		}
		
		questItemIds = new int[]
		{
			PaintOfKamuru,
			LetterOfSolderDetachment,
			PaintOfTeleportDevice,
			BrokenTeleportDevice,
			TeleportDevice,
			ArchitectureOfCruma,
			IngredientsOfAntidote,
			RecommendationOfBalanki,
			RecommendationOfFilaur,
			RecommendationOfArin,
			ReportOfCruma
		};
	}
	
	public void recommendationCount(QuestState st)
	{
		if ((st.getQuestItemsCount(RecommendationOfArin) != 0) && (st.getQuestItemsCount(RecommendationOfFilaur) != 0) && (st.getQuestItemsCount(RecommendationOfBalanki) != 0))
		{
			st.setCond(17);
		}
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
		
		if (event.equalsIgnoreCase("1"))
		{
			htmltext = "30531-04.htm";
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.set("cond", "1");
		}
		else if (event.equalsIgnoreCase("30533_1"))
		{
			htmltext = "30533-02.htm";
			st.set("cond", "2");
		}
		else if (event.equalsIgnoreCase("30671_1"))
		{
			htmltext = "30671-02.htm";
			st.giveItems(PaintOfKamuru, 1);
			st.set("cond", "3");
		}
		else if (event.equalsIgnoreCase("30556_1"))
		{
			htmltext = "30556-02.htm";
		}
		else if (event.equalsIgnoreCase("30556_2"))
		{
			htmltext = "30556-03.htm";
		}
		else if (event.equalsIgnoreCase("30556_3"))
		{
			htmltext = "30556-05.htm";
			st.takeItems(PaintOfTeleportDevice, -1);
			st.giveItems(BrokenTeleportDevice, 1);
			st.set("cond", "9");
			st.getPlayer().teleToLocation(140352, -194133, -2028);
		}
		else if (event.equalsIgnoreCase("30556_4"))
		{
			htmltext = "30556-04.htm";
		}
		else if (event.equalsIgnoreCase("30673_1"))
		{
			htmltext = "30673-04.htm";
			st.takeItems(BloodOfLeech, -1);
			st.takeItems(StingerWaspNeedle, -1);
			st.takeItems(MarshSpidersWeb, -1);
			st.takeItems(IngredientsOfAntidote, -1);
			st.giveItems(ReportOfCruma, 1);
			st.set("cond", "15");
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
				if (npcId == Lockirin)
				{
					if (player.getClassId().getId() == 0x38)
					{
						if (player.getLevel() > 38)
						{
							htmltext = "30531-03.htm";
						}
						else
						{
							htmltext = "30531-01.htm";
							st.exitQuest(true);
						}
					}
					else
					{
						htmltext = "30531-02.htm";
						st.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				if (npcId == Lockirin)
				{
					if ((cond >= 1) && (cond <= 16))
					{
						htmltext = "30531-05.htm";
					}
					else if (cond == 17)
					{
						st.addExpAndSp(2058244, 141240);
						st.giveItems(57, 372154);
						st.giveItems(7562, 23);
						htmltext = "30531-06.htm";
						st.takeItems(RecommendationOfBalanki, -1);
						st.takeItems(RecommendationOfFilaur, -1);
						st.takeItems(RecommendationOfArin, -1);
						st.giveItems(MarkOfMaestro, 1);
						st.unset("cond");
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
					}
				}
				else if (npcId == Balanki)
				{
					if (((cond == 1) || (cond == 11) || (cond == 16)) && (st.getQuestItemsCount(RecommendationOfBalanki) == 0))
					{
						htmltext = "30533-01.htm";
					}
					else if (cond == 2)
					{
						htmltext = "30533-03.htm";
					}
					else if (cond == 6)
					{
						st.takeItems(LetterOfSolderDetachment, -1);
						st.giveItems(RecommendationOfBalanki, 1);
						htmltext = "30533-04.htm";
						st.set("cond", "7");
						recommendationCount(st);
					}
					else if ((cond == 7) || (cond == 17))
					{
						htmltext = "30533-05.htm";
					}
				}
				else if (npcId == Arin)
				{
					if (((cond == 1) || (cond == 7) || (cond == 16)) && (st.getQuestItemsCount(RecommendationOfArin) == 0))
					{
						st.giveItems(PaintOfTeleportDevice, 1);
						htmltext = "30536-01.htm";
						st.set("cond", "8");
					}
					else if (cond == 8)
					{
						htmltext = "30536-02.htm";
					}
					else if (cond == 10)
					{
						st.takeItems(TeleportDevice, -1);
						st.giveItems(RecommendationOfArin, 1);
						htmltext = "30536-03.htm";
						st.set("cond", "11");
						recommendationCount(st);
					}
					else if ((cond == 11) || (cond == 17))
					{
						htmltext = "30536-04.htm";
					}
				}
				else if (npcId == Filaur)
				{
					if (((cond == 1) || (cond == 7) || (cond == 11)) && (st.getQuestItemsCount(RecommendationOfFilaur) == 0))
					{
						st.giveItems(ArchitectureOfCruma, 1);
						htmltext = "30535-01.htm";
						st.set("cond", "12");
					}
					else if (cond == 12)
					{
						htmltext = "30535-02.htm";
					}
					else if (cond == 15)
					{
						st.takeItems(ReportOfCruma, 1);
						st.giveItems(RecommendationOfFilaur, 1);
						st.set("cond", "16");
						htmltext = "30535-03.htm";
						recommendationCount(st);
					}
					else if (cond > 15)
					{
						htmltext = "30535-04.htm";
					}
				}
				else if (npcId == Croto)
				{
					if (cond == 2)
					{
						htmltext = "30671-01.htm";
					}
					else if (cond == 3)
					{
						htmltext = "30671-03.htm";
					}
					else if (cond == 5)
					{
						st.takeItems(NecklaceOfKamuru, -1);
						st.takeItems(PaintOfKamuru, -1);
						st.giveItems(LetterOfSolderDetachment, 1);
						htmltext = "30671-04.htm";
						st.set("cond", "6");
					}
					else if (cond == 6)
					{
						htmltext = "30671-05.htm";
					}
				}
				else if ((npcId == Dubabah) && (cond == 3))
				{
					htmltext = "30672-01.htm";
				}
				else if ((npcId == Kamur) && (cond == 3))
				{
					htmltext = "30675-01.htm";
					st.set("cond", "4");
				}
				else if (npcId == Toma)
				{
					if (cond == 8)
					{
						htmltext = "30556-01.htm";
					}
					else if (cond == 9)
					{
						st.takeItems(BrokenTeleportDevice, -1);
						st.giveItems(TeleportDevice, 5);
						htmltext = "30556-06.htm";
						st.set("cond", "10");
					}
					else if (cond == 10)
					{
						htmltext = "30556-07.htm";
					}
				}
				else if (npcId == Lorain)
				{
					if (cond == 12)
					{
						st.takeItems(ArchitectureOfCruma, -1);
						st.giveItems(IngredientsOfAntidote, 1);
						st.set("cond", "13");
						htmltext = "30673-01.htm";
					}
					else if (cond == 13)
					{
						htmltext = "30673-02.htm";
					}
					else if (cond == 14)
					{
						htmltext = "30673-03.htm";
					}
					else if (cond == 15)
					{
						htmltext = "30673-05.htm";
					}
				}
				else if ((npcId == Spiron) && ((cond == 1) || (cond == 7) || (cond == 11) || (cond == 16)))
				{
					htmltext = "30532-01.htm";
				}
				break;
			case State.COMPLETED:
				if (npcId == Lockirin)
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
						}
					}
				}
			}
		}
		if ((cond == 13) && (st.getQuestItemsCount(BloodOfLeech) >= 10) && (st.getQuestItemsCount(StingerWaspNeedle) >= 10) && (st.getQuestItemsCount(MarshSpidersWeb) >= 10))
		{
			st.set("cond", "14");
			st.playSound("Itemsound.quest_middle");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _231_TestOfTheMaestro(231, qn, "");
	}
}