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
import l2e.util.Rnd;

/**
 * Created by LordWinter 16.06.2013 Based on L2J Eternity-World
 */
public class _226_TestOfHealer extends Quest
{
	private static final String qn = "_226_TestOfHealer";
	
	private static final int Bandellos = 30473;
	private static final int Perrin = 30428;
	private static final int OrphanGirl = 30659;
	private static final int Allana = 30424;
	private static final int FatherGupu = 30658;
	private static final int Windy = 30660;
	private static final int Sorius = 30327;
	private static final int Daurin = 30674;
	private static final int Piper = 30662;
	private static final int Slein = 30663;
	private static final int Kein = 30664;
	private static final int MysteryDarkElf = 30661;
	private static final int Kristina = 30665;
	
	private static final int REPORT_OF_PERRIN_ID = 2810;
	private static final int CRISTINAS_LETTER_ID = 2811;
	private static final int PICTURE_OF_WINDY_ID = 2812;
	private static final int GOLDEN_STATUE_ID = 2813;
	private static final int WINDYS_PEBBLES_ID = 2814;
	private static final int ORDER_OF_SORIUS_ID = 2815;
	private static final int SECRET_LETTER1_ID = 2816;
	private static final int SECRET_LETTER2_ID = 2817;
	private static final int SECRET_LETTER3_ID = 2818;
	private static final int SECRET_LETTER4_ID = 2819;
	private static final int MARK_OF_HEALER_ID = 2820;
	
	private static Map<Integer, Integer[]> DROPLIST = new HashMap<>();
	
	static
	{
		DROPLIST.put(27134, new Integer[]
		{
			2,
			3,
			0
		});
		DROPLIST.put(27123, new Integer[]
		{
			11,
			12,
			SECRET_LETTER1_ID
		});
		DROPLIST.put(27124, new Integer[]
		{
			14,
			15,
			SECRET_LETTER2_ID
		});
		DROPLIST.put(27125, new Integer[]
		{
			16,
			17,
			SECRET_LETTER3_ID
		});
		DROPLIST.put(27127, new Integer[]
		{
			18,
			19,
			SECRET_LETTER4_ID
		});
	}
	
	public _226_TestOfHealer(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(30473);
		
		addTalkId(30327, 30424, 30428, 30473, 30658, 30659, 30660, 30661, 30662, 30663, 30664, 30665, 30674);
		
		addKillId(20150, 27123, 27124, 27125, 27127, 27134);
		
		questItemIds = new int[]
		{
			REPORT_OF_PERRIN_ID,
			CRISTINAS_LETTER_ID,
			PICTURE_OF_WINDY_ID,
			GOLDEN_STATUE_ID, //
			WINDYS_PEBBLES_ID,
			ORDER_OF_SORIUS_ID,
			SECRET_LETTER1_ID,
			SECRET_LETTER2_ID,
			SECRET_LETTER3_ID,
			SECRET_LETTER4_ID
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
		
		if (event.equalsIgnoreCase("1"))
		{
			htmltext = "30473-04.htm";
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(REPORT_OF_PERRIN_ID, 1);
		}
		else if (event.equalsIgnoreCase("30473_1"))
		{
			htmltext = "30473-08.htm";
		}
		else if (event.equalsIgnoreCase("30473_2"))
		{
			htmltext = "30473-09.htm";
			st.takeItems(GOLDEN_STATUE_ID, -1);
			st.giveItems(MARK_OF_HEALER_ID, 1);
			st.addExpAndSp(1476566, 101324);
			st.giveItems(57, 266980);
			st.giveItems(7562, 60);
			st.set("cond", "0");
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		else if (event.equalsIgnoreCase("30428_1"))
		{
			htmltext = "30428-02.htm";
			st.set("cond", "2");
			st.addSpawn(27134);
		}
		else if (event.equalsIgnoreCase("30658_1"))
		{
			if (st.getQuestItemsCount(57) >= 100000)
			{
				htmltext = "30658-02.htm";
				st.takeItems(57, 100000);
				st.giveItems(PICTURE_OF_WINDY_ID, 1);
				st.set("cond", "7");
			}
			else
			{
				htmltext = "30658-05.htm";
			}
		}
		else if (event.equalsIgnoreCase("30658_2"))
		{
			st.set("cond", "6");
			htmltext = "30658-03.htm";
		}
		else if (event.equalsIgnoreCase("30660-03.htm"))
		{
			st.takeItems(PICTURE_OF_WINDY_ID, 1);
			st.giveItems(WINDYS_PEBBLES_ID, 1);
			st.set("cond", "8");
		}
		else if (event.equalsIgnoreCase("30674_1"))
		{
			htmltext = "30674-02.htm";
			st.takeItems(ORDER_OF_SORIUS_ID, 1);
			st.addSpawn(27122);
			st.addSpawn(27122);
			st.addSpawn(27123);
			st.set("cond", "11");
			st.playSound("Itemsound.quest_before_battle");
		}
		else if (event.equalsIgnoreCase("30665_1"))
		{
			htmltext = "30665-02.htm";
			st.takeItems(SECRET_LETTER1_ID, 1);
			st.takeItems(SECRET_LETTER2_ID, 1);
			st.takeItems(SECRET_LETTER3_ID, 1);
			st.takeItems(SECRET_LETTER4_ID, 1);
			st.giveItems(CRISTINAS_LETTER_ID, 1);
			st.set("cond", "22");
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
				if (npcId == Bandellos)
				{
					if ((player.getClassId().getId() == 0x04) || (player.getClassId().getId() == 0x0f) || (player.getClassId().getId() == 0x1d) || (player.getClassId().getId() == 0x13))
					{
						if (player.getLevel() > 38)
						{
							htmltext = "30473-03.htm";
						}
						else
						{
							htmltext = "30473-01.htm";
						}
					}
					else
					{
						htmltext = "30473-02.htm";
						st.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				if (npcId == Bandellos)
				{
					if (cond == 23)
					{
						if (st.getQuestItemsCount(GOLDEN_STATUE_ID) == 0)
						{
							htmltext = "30473-06.htm";
							st.giveItems(MARK_OF_HEALER_ID, 1);
							htmltext = "30690-08.htm";
							st.addExpAndSp(32000, 4100);
							st.set("cond", "0");
							st.exitQuest(false);
							st.playSound("ItemSound.quest_finish");
						}
						else
						{
							htmltext = "30473-07.htm";
						}
					}
					else
					{
						htmltext = "30473-05.htm";
					}
				}
				else if (npcId == Perrin)
				{
					if (cond == 1)
					{
						htmltext = "30428-01.htm";
					}
					else if (cond == 3)
					{
						htmltext = "30428-03.htm";
						st.takeItems(REPORT_OF_PERRIN_ID, 1);
						st.set("cond", "4");
					}
					else if (cond != 2)
					{
						htmltext = "30428-04.htm";
					}
				}
				else if (npcId == OrphanGirl)
				{
					int n = Rnd.get(5);
					if (n == 0)
					{
						htmltext = "30659-01.htm";
					}
					else if (n == 1)
					{
						htmltext = "30659-02.htm";
					}
					else if (n == 2)
					{
						htmltext = "30659-03.htm";
					}
					else if (n == 3)
					{
						htmltext = "30659-04.htm";
					}
					else if (n == 4)
					{
						htmltext = "30659-05.htm";
					}
				}
				else if (npcId == Allana)
				{
					if (cond == 4)
					{
						htmltext = "30424-01.htm";
						st.set("cond", "5");
					}
					else
					{
						htmltext = "30424-02.htm";
					}
				}
				else if (npcId == FatherGupu)
				{
					if (cond == 5)
					{
						htmltext = "30658-01.htm";
					}
					else if (cond == 7)
					{
						htmltext = "30658-04.htm";
					}
					else if (cond == 8)
					{
						htmltext = "30658-06.htm";
						st.giveItems(GOLDEN_STATUE_ID, 1);
						st.takeItems(WINDYS_PEBBLES_ID, 1);
						st.set("cond", "9");
					}
					else if (cond == 6)
					{
						st.set("cond", "9");
						htmltext = "30658-07.htm";
					}
					else if (cond == 9)
					{
						htmltext = "30658-07.htm";
					}
				}
				else if (npcId == Windy)
				{
					if (cond == 7)
					{
						htmltext = "30660-01.htm";
					}
					else if (cond == 8)
					{
						htmltext = "30660-04.htm";
					}
				}
				else if (npcId == Sorius)
				{
					if (cond == 9)
					{
						htmltext = "30327-01.htm";
						st.giveItems(ORDER_OF_SORIUS_ID, 1);
						st.set("cond", "10");
					}
					else if ((cond > 9) && (cond < 22))
					{
						htmltext = "30327-02.htm";
					}
					else if (cond == 22)
					{
						htmltext = "30327-03.htm";
						st.takeItems(CRISTINAS_LETTER_ID, 1);
						st.set("cond", "23");
					}
				}
				else if (npcId == Daurin)
				{
					if ((cond == 10) && (st.getQuestItemsCount(ORDER_OF_SORIUS_ID) > 0))
					{
						htmltext = "30674-01.htm";
					}
					else if ((cond == 12) && (st.getQuestItemsCount(SECRET_LETTER1_ID) > 0))
					{
						htmltext = "30674-03.htm";
						st.set("cond", "13");
					}
				}
				else if ((npcId == Piper) || (npcId == Slein) || (npcId == Kein))
				{
					if (cond == 13)
					{
						htmltext = npcId + "-01.htm";
					}
					else if (cond == 15)
					{
						htmltext = npcId + "-02.htm";
					}
					else if (cond == 20)
					{
						st.set("cond", "21");
						htmltext = npcId + "-03.htm";
					}
					else if (cond == 21)
					{
						htmltext = npcId + "-04.htm";
					}
				}
				else if (npcId == MysteryDarkElf)
				{
					if (cond == 13)
					{
						htmltext = "30661-01.htm";
						st.addSpawn(27124);
						st.addSpawn(27124);
						st.addSpawn(27124);
						st.playSound("Itemsound.quest_before_battle");
						st.set("cond", "14");
					}
					else if (cond == 15)
					{
						htmltext = "30661-02.htm";
						st.addSpawn(27125);
						st.addSpawn(27125);
						st.addSpawn(27125);
						st.playSound("Itemsound.quest_before_battle");
						st.set("cond", "16");
					}
					else if (cond == 17)
					{
						htmltext = "30661-03.htm";
						st.addSpawn(27126);
						st.addSpawn(27126);
						st.addSpawn(27127);
						st.playSound("Itemsound.quest_before_battle");
						st.set("cond", "18");
					}
					else if (cond == 19)
					{
						htmltext = "30661-04.htm";
						st.set("cond", "20");
					}
				}
				else if (npcId == Kristina)
				{
					if ((cond == 20) || (cond == 21))
					{
						htmltext = "30665-01.htm";
					}
					else
					{
						htmltext = "30665-03.htm";
					}
				}
				break;
			case State.COMPLETED:
				if (npcId == Bandellos)
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
		
		Integer[] d = DROPLIST.get(npc.getId());
		if ((st.getCond() == d[0]) && ((d[2] == 0) || (st.getQuestItemsCount(d[2]) == 0)))
		{
			if (d[2] != 0)
			{
				st.giveItems(d[2], 1);
			}
			st.setCond(d[1]);
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _226_TestOfHealer(226, qn, "");
	}
}