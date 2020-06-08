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

import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 02.10.2012 Based on L2J Eternity-World
 */
public class _213_TrialOfSeeker extends Quest
{
	private static final String qn = "_213_TrialOfSeeker";
	
	// Npc
	private static final int DUFNER = 30106;
	private static final int TERRY = 30064;
	private static final int BRUNON = 30526;
	private static final int VIKTOR = 30684;
	private static final int MARINA = 30715;
	
	private static final int[] TALKERS =
	{
		DUFNER,
		TERRY,
		BRUNON,
		VIKTOR,
		MARINA
	};
	
	// Mobs
	private static final int NEER_GHOUL_BERSERKER = 20198;
	private static final int OL_MAHUM_CAPTAIN = 20211;
	private static final int TUREK_ORC_WARLORD = 20495;
	private static final int ANT_CAPTAIN = 20080;
	private static final int TURAK_BUGBEAR_WARRIOR = 20249;
	private static final int MARSH_STAKATO_DRONE = 20234;
	private static final int BREKA_ORC_OVERLORD = 20270;
	private static final int ANT_WARRIOR_CAPTAIN = 20088;
	private static final int LETO_LIZARDMAN_WARRIOR = 20580;
	private static final int MEDUSA = 20158;
	
	private static final int[] MOBS =
	{
		NEER_GHOUL_BERSERKER,
		OL_MAHUM_CAPTAIN,
		TUREK_ORC_WARLORD,
		ANT_CAPTAIN,
		TURAK_BUGBEAR_WARRIOR,
		MARSH_STAKATO_DRONE,
		BREKA_ORC_OVERLORD,
		ANT_WARRIOR_CAPTAIN,
		LETO_LIZARDMAN_WARRIOR,
		MEDUSA
	};
	
	// Quest items
	private static final int DUFNERS_LETTER = 2647;
	private static final int TERYS_ORDER1 = 2648;
	private static final int TERYS_ORDER2 = 2649;
	private static final int TERYS_LETTER = 2650;
	private static final int VIKTORS_LETTER = 2651;
	private static final int HAWKEYES_LETTER = 2652;
	private static final int MYSTERIOUS_RUNESTONE = 2653;
	private static final int OL_MAHUM_RUNESTONE = 2654;
	private static final int TUREK_RUNESTONE = 2655;
	private static final int ANT_RUNESTONE = 2656;
	private static final int TURAK_BUGBEAR_RUNESTONE = 2657;
	private static final int TERYS_BOX = 2658;
	private static final int VIKTORS_REQUEST = 2659;
	private static final int MEDUSAS_SCALES = 2660;
	private static final int SILENS_RUNESTONE = 2661;
	private static final int ANALYSIS_REQUEST = 2662;
	private static final int MARINAS_LETTER = 2663;
	private static final int EXPERIMENT_TOOLS = 2664;
	private static final int ANALYSIS_RESULT = 2665;
	private static final int TERYS_ORDER3 = 2666;
	private static final int LIST_OF_HOST = 2667;
	private static final int ABYSS_RUNESTONE1 = 2668;
	private static final int ABYSS_RUNESTONE2 = 2669;
	private static final int ABYSS_RUNESTONE3 = 2670;
	private static final int ABYSS_RUNESTONE4 = 2671;
	private static final int TERYS_REPORT = 2672;
	
	private static final int[] QUESTITEMS =
	{
		DUFNERS_LETTER,
		TERYS_ORDER1,
		TERYS_ORDER2,
		TERYS_LETTER,
		VIKTORS_LETTER,
		HAWKEYES_LETTER,
		MYSTERIOUS_RUNESTONE,
		OL_MAHUM_RUNESTONE,
		TUREK_RUNESTONE,
		ANT_RUNESTONE,
		TURAK_BUGBEAR_RUNESTONE,
		TERYS_BOX,
		VIKTORS_REQUEST,
		MEDUSAS_SCALES,
		SILENS_RUNESTONE,
		ANALYSIS_REQUEST,
		MARINAS_LETTER,
		EXPERIMENT_TOOLS,
		ANALYSIS_RESULT,
		TERYS_ORDER3,
		LIST_OF_HOST,
		ABYSS_RUNESTONE1,
		ABYSS_RUNESTONE2,
		ABYSS_RUNESTONE3,
		ABYSS_RUNESTONE4,
		TERYS_REPORT
	};
	
	// Reward
	private static final int MARK_OF_SEEKER = 2673;
	
	// Chances in %
	private static Map<Integer, int[]> DROPLIST = new HashMap<>();
	
	static
	{
		DROPLIST.put(NEER_GHOUL_BERSERKER, new int[]
		{
			TERYS_ORDER1,
			MYSTERIOUS_RUNESTONE,
			10,
			1
		});
		DROPLIST.put(OL_MAHUM_CAPTAIN, new int[]
		{
			TERYS_ORDER2,
			OL_MAHUM_RUNESTONE,
			25,
			1
		});
		DROPLIST.put(TUREK_ORC_WARLORD, new int[]
		{
			TERYS_ORDER2,
			TUREK_RUNESTONE,
			25,
			1
		});
		DROPLIST.put(ANT_CAPTAIN, new int[]
		{
			TERYS_ORDER2,
			ANT_RUNESTONE,
			25,
			1
		});
		DROPLIST.put(TURAK_BUGBEAR_WARRIOR, new int[]
		{
			TERYS_ORDER2,
			TURAK_BUGBEAR_RUNESTONE,
			25,
			1
		});
		DROPLIST.put(MARSH_STAKATO_DRONE, new int[]
		{
			LIST_OF_HOST,
			ABYSS_RUNESTONE1,
			25,
			1
		});
		DROPLIST.put(BREKA_ORC_OVERLORD, new int[]
		{
			LIST_OF_HOST,
			ABYSS_RUNESTONE2,
			25,
			1
		});
		DROPLIST.put(ANT_WARRIOR_CAPTAIN, new int[]
		{
			LIST_OF_HOST,
			ABYSS_RUNESTONE3,
			25,
			1
		});
		DROPLIST.put(LETO_LIZARDMAN_WARRIOR, new int[]
		{
			LIST_OF_HOST,
			ABYSS_RUNESTONE4,
			25,
			1
		});
		DROPLIST.put(MEDUSA, new int[]
		{
			VIKTORS_REQUEST,
			MEDUSAS_SCALES,
			30,
			10
		});
	}
	
	// Allowed classes
	private static final int[] CLASSES =
	{
		0x07,
		0x16,
		0x23
	};
	
	public _213_TrialOfSeeker(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(DUFNER);
		
		for (int talkId : TALKERS)
		{
			addTalkId(talkId);
		}
		
		for (int mobId : MOBS)
		{
			addKillId(mobId);
		}
		
		questItemIds = QUESTITEMS;
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("30106-05.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(DUFNERS_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("30064-03.htm"))
		{
			st.takeItems(DUFNERS_LETTER, 1);
			st.giveItems(TERYS_ORDER1, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30064-06.htm"))
		{
			st.takeItems(MYSTERIOUS_RUNESTONE, 1);
			st.takeItems(TERYS_ORDER1, 1);
			st.giveItems(TERYS_ORDER2, 1);
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30064-10.htm"))
		{
			st.takeItems(OL_MAHUM_RUNESTONE, 1);
			st.takeItems(TUREK_RUNESTONE, 1);
			st.takeItems(ANT_RUNESTONE, 1);
			st.takeItems(TURAK_BUGBEAR_RUNESTONE, 1);
			st.takeItems(TERYS_ORDER2, 1);
			st.giveItems(TERYS_LETTER, 1);
			st.giveItems(TERYS_BOX, 1);
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30064-18.htm"))
		{
			if (player.getLevel() < 35)
			{
				htmltext = "30064-17.htm";
				st.giveItems(TERYS_ORDER3, 1);
				st.takeItems(ANALYSIS_RESULT, 1);
			}
			else
			{
				st.giveItems(LIST_OF_HOST, 1);
				st.takeItems(ANALYSIS_RESULT, 1);
				st.set("cond", "16");
			}
		}
		else if (event.equalsIgnoreCase("30684-05.htm"))
		{
			st.giveItems(VIKTORS_LETTER, 1);
			st.takeItems(TERYS_LETTER, 1);
			st.set("cond", "7");
		}
		else if (event.equalsIgnoreCase("30684-11.htm"))
		{
			st.takeItems(TERYS_LETTER, 1);
			st.takeItems(TERYS_BOX, 1);
			st.takeItems(HAWKEYES_LETTER, 1);
			st.takeItems(VIKTORS_LETTER, st.getQuestItemsCount(VIKTORS_LETTER));
			st.giveItems(VIKTORS_REQUEST, 1);
			st.set("cond", "9");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30684-15.htm"))
		{
			st.takeItems(VIKTORS_REQUEST, 1);
			st.takeItems(MEDUSAS_SCALES, st.getQuestItemsCount(MEDUSAS_SCALES));
			st.giveItems(SILENS_RUNESTONE, 1);
			st.giveItems(ANALYSIS_REQUEST, 1);
			st.set("cond", "11");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30715-02.htm"))
		{
			st.takeItems(SILENS_RUNESTONE, 1);
			st.takeItems(ANALYSIS_REQUEST, 1);
			st.giveItems(MARINAS_LETTER, 1);
			st.set("cond", "12");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30715-05.htm"))
		{
			st.takeItems(EXPERIMENT_TOOLS, 1);
			st.giveItems(ANALYSIS_RESULT, 1);
			st.set("cond", "14");
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = getNoQuestMsg(talker);
		QuestState st = talker.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		int cond = st.getInt("cond");
		int npcId = npc.getId();
		int id = st.getState();
		
		if ((npcId != DUFNER) && (id != State.STARTED))
		{
			return htmltext;
		}
		
		if (id == State.COMPLETED)
		{
			htmltext = Quest.getAlreadyCompletedMsg(talker);
		}
		else if (id == State.CREATED)
		{
			st.set("cond", "0");
			st.set("id", "0");
			st.set("onlyone", "0");
		}
		
		if ((npcId == DUFNER) && (st.getInt("cond") == 0) && (st.getInt("onlyone") == 0))
		{
			if (Util.contains(CLASSES, talker.getClassId().getId()))
			{
				if (talker.getLevel() >= 35)
				{
					htmltext = "30106-03.htm";
				}
				else
				{
					htmltext = "30106-02.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				htmltext = "30106-00.htm";
				st.exitQuest(true);
			}
		}
		else if (npcId == DUFNER)
		{
			if (cond == 1)
			{
				htmltext = "30106-06.htm";
			}
			else if ((cond >= 1) && (st.getInt("id") != 18))
			{
				htmltext = "30106-07.htm";
			}
			else if ((cond == 17) && (st.getInt("id") == 18))
			{
				htmltext = "30106-08.htm";
				st.set("cond", "0");
				st.set("onlyone", "1");
				st.set("id", "0");
				st.takeItems(TERYS_REPORT, 1);
				st.addExpAndSp(514739, 33384);
				st.giveItems(57, 93803);
				st.giveItems(7562, 128);
				st.giveItems(MARK_OF_SEEKER, 1);
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
			}
		}
		else if ((npcId == TERRY) && (st.getQuestItemsCount(TERYS_ORDER3) == 1))
		{
			if (talker.getLevel() < 35)
			{
				htmltext = "30064-20.htm";
			}
			else
			{
				htmltext = "30064-21.htm";
				st.giveItems(LIST_OF_HOST, 1);
				st.takeItems(TERYS_ORDER3, 1);
				st.set("cond", "16");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if ((npcId == TERRY) && (cond == 1))
		{
			htmltext = "30064-01.htm";
		}
		else if ((npcId == TERRY) && (cond == 2))
		{
			htmltext = "30064-04.htm";
		}
		else if ((npcId == TERRY) && (cond == 3))
		{
			htmltext = "30064-05.htm";
		}
		else if ((npcId == TERRY) && (cond == 4))
		{
			htmltext = "30064-08.htm";
		}
		else if ((npcId == TERRY) && (cond == 5))
		{
			htmltext = "30064-09.htm";
		}
		else if ((npcId == TERRY) && (cond == 6))
		{
			htmltext = "30064-11.htm";
		}
		else if ((npcId == TERRY) && (cond == 7))
		{
			htmltext = "30064-12.htm";
			st.takeItems(VIKTORS_LETTER, 1);
			st.giveItems(HAWKEYES_LETTER, 1);
			st.set("cond", "8");
			st.playSound("ItemSound.quest_middle");
		}
		else if ((npcId == TERRY) && (cond == 8))
		{
			htmltext = "30064-13.htm";
		}
		else if ((npcId == TERRY) && ((cond > 8) && (cond < 14)))
		{
			htmltext = "30064-14.htm";
		}
		else if ((npcId == TERRY) && (cond == 14))
		{
			htmltext = "30064-15.htm";
		}
		else if ((npcId == TERRY) && (cond == 16))
		{
			htmltext = "30064-22.htm";
		}
		else if ((npcId == TERRY) && (cond == 17) && (st.getInt("id") != 18))
		{
			htmltext = "30064-23.htm";
			st.takeItems(LIST_OF_HOST, 1);
			st.takeItems(ABYSS_RUNESTONE1, 1);
			st.takeItems(ABYSS_RUNESTONE2, 1);
			st.takeItems(ABYSS_RUNESTONE3, 1);
			st.takeItems(ABYSS_RUNESTONE4, 1);
			st.giveItems(TERYS_REPORT, 1);
			st.set("id", "18");
			st.playSound("ItemSound.quest_middle");
		}
		else if ((npcId == TERRY) && (cond == 17) && (st.getInt("id") == 18))
		{
			htmltext = "30064-24.htm";
		}
		else if ((npcId == VIKTOR) && (cond == 6))
		{
			htmltext = "30684-01.htm";
		}
		else if ((npcId == VIKTOR) && (cond == 7))
		{
			htmltext = "30684-05.htm";
		}
		else if ((npcId == VIKTOR) && (cond == 8))
		{
			htmltext = "30684-12.htm";
		}
		else if ((npcId == VIKTOR) && (cond == 9))
		{
			htmltext = "30684-13.htm";
		}
		else if ((npcId == VIKTOR) && (cond == 10))
		{
			htmltext = "30684-14.htm";
		}
		else if ((npcId == VIKTOR) && (cond == 11))
		{
			htmltext = "30684-16.htm";
		}
		else if ((npcId == VIKTOR) && (cond == 14))
		{
			htmltext = "30684-17.htm";
		}
		else if ((npcId == MARINA) && (cond == 11))
		{
			htmltext = "30715-01.htm";
		}
		else if ((npcId == MARINA) && (cond == 12))
		{
			htmltext = "30715-03.htm";
		}
		else if ((npcId == MARINA) && (cond == 13))
		{
			htmltext = "30715-04.htm";
		}
		else if ((npcId == MARINA) && (cond == 14))
		{
			htmltext = "30715-06.htm";
		}
		else if ((npcId == BRUNON) && (cond == 12))
		{
			htmltext = "30526-01.htm";
			st.takeItems(MARINAS_LETTER, 1);
			st.giveItems(EXPERIMENT_TOOLS, 1);
			st.set("cond", "13");
		}
		else if ((npcId == BRUNON) && (cond == 13))
		{
			htmltext = "30526-02.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		QuestState st = killer.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		int cond = st.getInt("cond");
		int npcId = npc.getId();
		int required = DROPLIST.get(npcId)[0];
		int item = DROPLIST.get(npcId)[1];
		int chance = DROPLIST.get(npcId)[2];
		int maxqty = DROPLIST.get(npcId)[3];
		long count = st.getQuestItemsCount(item);
		
		if ((st.getQuestItemsCount(required) > 0) && (count < maxqty))
		{
			if (st.getRandom(100) < chance)
			{
				st.giveItems(item, 1);
				if ((count + 1) == maxqty)
				{
					st.playSound("ItemSound.quest_middle");
					if (cond == 4)
					{
						if ((st.getQuestItemsCount(OL_MAHUM_RUNESTONE) + st.getQuestItemsCount(TUREK_RUNESTONE) + st.getQuestItemsCount(ANT_RUNESTONE) + st.getQuestItemsCount(TURAK_BUGBEAR_RUNESTONE)) == 4)
						{
							st.set("cond", String.valueOf(cond + 1));
						}
					}
					else if (cond == 16)
					{
						if ((st.getQuestItemsCount(ABYSS_RUNESTONE1) + st.getQuestItemsCount(ABYSS_RUNESTONE2) + st.getQuestItemsCount(ABYSS_RUNESTONE3) + st.getQuestItemsCount(ABYSS_RUNESTONE4)) == 4)
						{
							st.set("cond", String.valueOf(cond + 1));
						}
					}
					else
					{
						st.set("cond", String.valueOf(cond + 1));
					}
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _213_TrialOfSeeker(213, qn, "");
	}
}