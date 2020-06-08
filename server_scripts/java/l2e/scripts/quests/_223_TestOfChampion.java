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
public class _223_TestOfChampion extends Quest
{
	private static final String qn = "_223_TestOfChampion";
	
	private static final int MARK_OF_CHAMPION = 3276;
	private static final int ASCALONS_LETTER1 = 3277;
	private static final int MASONS_LETTER = 3278;
	private static final int IRON_ROSE_RING = 3279;
	private static final int ASCALONS_LETTER2 = 3280;
	private static final int WHITE_ROSE_INSIGNIA = 3281;
	private static final int GROOTS_LETTER = 3282;
	private static final int ASCALONS_LETTER3 = 3283;
	private static final int MOUENS_ORDER1 = 3284;
	private static final int MOUENS_ORDER2 = 3285;
	private static final int MOUENS_LETTER = 3286;
	private static final int HARPYS_EGG = 3287;
	private static final int MEDUSA_VENOM = 3288;
	private static final int WINDSUS_BILE = 3289;
	private static final int BLOODY_AXE_HEAD = 3290;
	private static final int ROAD_RATMAN_HEAD = 3291;
	private static final int LETO_LIZARDMAN_FANG = 3292;
	
	private static final int Ascalon = 30624;
	private static final int Groot = 30093;
	private static final int Mouen = 30196;
	private static final int Mason = 30625;
	
	private static final int Harpy = 20145;
	private static final int HarpyMatriarch = 27088;
	private static final int Medusa = 20158;
	private static final int Windsus = 20553;
	private static final int RoadScavenger = 20551;
	private static final int LetoLizardman = 20577;
	private static final int LetoLizardmanArcher = 20578;
	private static final int LetoLizardmanSoldier = 20579;
	private static final int LetoLizardmanWarrior = 20580;
	private static final int LetoLizardmanShaman = 20581;
	private static final int LetoLizardmanOverlord = 20582;
	private static final int BloodyAxeElite = 20780;
	
	private static final int[][] DROPLIST =
	{
		{
			2,
			3,
			BloodyAxeElite,
			BLOODY_AXE_HEAD,
			20,
			10
		},
		{
			6,
			7,
			Harpy,
			HARPYS_EGG,
			100,
			30
		},
		{
			6,
			7,
			HarpyMatriarch,
			HARPYS_EGG,
			100,
			30
		},
		{
			6,
			7,
			Medusa,
			MEDUSA_VENOM,
			50,
			30
		},
		{
			6,
			7,
			Windsus,
			WINDSUS_BILE,
			50,
			30
		},
		{
			10,
			11,
			RoadScavenger,
			ROAD_RATMAN_HEAD,
			20,
			10
		},
		{
			12,
			13,
			LetoLizardman,
			LETO_LIZARDMAN_FANG,
			20,
			10
		},
		{
			12,
			13,
			LetoLizardmanArcher,
			LETO_LIZARDMAN_FANG,
			22,
			10
		},
		{
			12,
			13,
			LetoLizardmanSoldier,
			LETO_LIZARDMAN_FANG,
			24,
			10
		},
		{
			12,
			13,
			LetoLizardmanWarrior,
			LETO_LIZARDMAN_FANG,
			26,
			10
		},
		{
			12,
			13,
			LetoLizardmanShaman,
			LETO_LIZARDMAN_FANG,
			28,
			10
		},
		{
			12,
			13,
			LetoLizardmanOverlord,
			LETO_LIZARDMAN_FANG,
			30,
			10
		},
	};
	
	public _223_TestOfChampion(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Ascalon);
		addTalkId(Ascalon);
		addTalkId(Groot);
		addTalkId(Mouen);
		addTalkId(Mason);
		
		addKillId(Harpy, Medusa, HarpyMatriarch, RoadScavenger, Windsus, LetoLizardman, LetoLizardmanArcher, LetoLizardmanSoldier, LetoLizardmanWarrior, LetoLizardmanShaman, LetoLizardmanOverlord, BloodyAxeElite);
		
		questItemIds = new int[]
		{
			MASONS_LETTER,
			MEDUSA_VENOM,
			WINDSUS_BILE,
			WHITE_ROSE_INSIGNIA,
			HARPYS_EGG,
			GROOTS_LETTER,
			MOUENS_LETTER,
			ASCALONS_LETTER1,
			IRON_ROSE_RING,
			BLOODY_AXE_HEAD,
			ASCALONS_LETTER2,
			ASCALONS_LETTER3,
			MOUENS_ORDER1,
			ROAD_RATMAN_HEAD,
			MOUENS_ORDER2,
			LETO_LIZARDMAN_FANG
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
		
		if (event.equals("1"))
		{
			htmltext = "30624-06.htm";
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(ASCALONS_LETTER1, 1);
		}
		else if (event.equals("30624_1"))
		{
			htmltext = "30624-05.htm";
		}
		else if (event.equals("30624_2"))
		{
			htmltext = "30624-10.htm";
			st.playSound("Itemsound.quest_middle");
			st.set("cond", "5");
			st.takeItems(MASONS_LETTER, -1);
			st.giveItems(ASCALONS_LETTER2, 1);
		}
		else if (event.equals("30624_3"))
		{
			htmltext = "30624-14.htm";
			st.playSound("Itemsound.quest_middle");
			st.set("cond", "9");
			st.takeItems(GROOTS_LETTER, -1);
			st.giveItems(ASCALONS_LETTER3, 1);
		}
		else if (event.equals("30625_1"))
		{
			htmltext = "30625-02.htm";
		}
		else if (event.equals("30625_2"))
		{
			htmltext = "30625-03.htm";
			st.playSound("Itemsound.quest_middle");
			st.set("cond", "2");
			st.takeItems(ASCALONS_LETTER1, -1);
			st.giveItems(IRON_ROSE_RING, 1);
		}
		else if (event.equals("30093_1"))
		{
			htmltext = "30093-02.htm";
			st.playSound("Itemsound.quest_middle");
			st.set("cond", "6");
			st.takeItems(ASCALONS_LETTER2, -1);
			st.giveItems(WHITE_ROSE_INSIGNIA, 1);
		}
		else if (event.equals("30196_1"))
		{
			htmltext = "30196-02.htm";
		}
		else if (event.equals("30196_2"))
		{
			htmltext = "30196-03.htm";
			st.playSound("Itemsound.quest_middle");
			st.set("cond", "10");
			st.takeItems(ASCALONS_LETTER3, -1);
			st.giveItems(MOUENS_ORDER1, 1);
		}
		else if (event.equals("30196_3"))
		{
			htmltext = "30196-06.htm";
			st.playSound("Itemsound.quest_middle");
			st.set("cond", "12");
			st.takeItems(MOUENS_ORDER1, -1);
			st.takeItems(ROAD_RATMAN_HEAD, -1);
			st.giveItems(MOUENS_ORDER2, 1);
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
				if (npcId == Ascalon)
				{
					int class_id = player.getClassId().getId();
					if ((class_id != 0x01) && (class_id != 0x2d))
					{
						st.exitQuest(true);
						return "30624-01.htm";
					}
					if (st.getPlayer().getLevel() < 39)
					{
						st.exitQuest(true);
						return "30624-02.htm";
					}
					return class_id == 0x01 ? "30624-03.htm" : "30624-04.htm";
				}
				break;
			case State.STARTED:
				if (npcId == Ascalon)
				{
					if (cond == 1)
					{
						htmltext = "30624-07.htm";
					}
					else if ((cond == 2) || (cond == 3))
					{
						htmltext = "30624-08.htm";
					}
					else if (cond == 4)
					{
						htmltext = "30624-09.htm";
					}
					else if (cond == 5)
					{
						htmltext = "30624-11.htm";
					}
					else if ((cond == 6) || (cond == 7))
					{
						htmltext = "30624-12.htm";
					}
					else if (cond == 8)
					{
						htmltext = "30624-13.htm";
					}
					else if (cond == 9)
					{
						htmltext = "30624-15.htm";
					}
					else if ((cond > 9) && (cond < 14))
					{
						htmltext = "30624-16.htm";
					}
					else if (cond == 14)
					{
						htmltext = "30624-17.htm";
						st.takeItems(MOUENS_LETTER, -1);
						st.giveItems(MARK_OF_CHAMPION, 1);
						st.addExpAndSp(1270742, 87200);
						st.giveItems(7562, 72);
						st.giveItems(57, 229764);
						st.giveItems(8870, 15);
						st.set("cond", "0");
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
					}
				}
				else if (npcId == Mason)
				{
					if (cond == 1)
					{
						htmltext = "30625-01.htm";
					}
					else if (cond == 2)
					{
						htmltext = "30625-04.htm";
					}
					else if (cond == 3)
					{
						htmltext = "30625-05.htm";
						st.takeItems(BLOODY_AXE_HEAD, -1);
						st.takeItems(IRON_ROSE_RING, -1);
						st.giveItems(MASONS_LETTER, 1);
						st.playSound("Itemsound.quest_middle");
						st.set("cond", "4");
					}
					else if (cond == 4)
					{
						htmltext = "30625-06.htm";
					}
					else
					{
						htmltext = "30625-07.htm";
					}
				}
				else if (npcId == Groot)
				{
					if (cond == 5)
					{
						htmltext = "30093-01.htm";
					}
					else if (cond == 6)
					{
						htmltext = "30093-03.htm";
					}
					else if (cond == 7)
					{
						htmltext = "30093-04.htm";
						st.takeItems(WHITE_ROSE_INSIGNIA, -1);
						st.takeItems(HARPYS_EGG, -1);
						st.takeItems(MEDUSA_VENOM, -1);
						st.takeItems(WINDSUS_BILE, -1);
						st.giveItems(GROOTS_LETTER, 1);
						st.playSound("Itemsound.quest_middle");
						st.set("cond", "8");
					}
					else if (cond == 8)
					{
						htmltext = "30093-05.htm";
					}
					else if (cond > 8)
					{
						htmltext = "30093-06.htm";
					}
				}
				else if (npcId == Mouen)
				{
					if (cond == 9)
					{
						htmltext = "30196-01.htm";
					}
					else if (cond == 10)
					{
						htmltext = "30196-04.htm";
					}
					else if (cond == 11)
					{
						htmltext = "30196-05.htm";
					}
					else if (cond == 12)
					{
						htmltext = "30196-07.htm";
					}
					else if (cond == 13)
					{
						htmltext = "30196-08.htm";
						st.takeItems(MOUENS_ORDER2, -1);
						st.takeItems(LETO_LIZARDMAN_FANG, -1);
						st.giveItems(MOUENS_LETTER, 1);
						st.playSound("Itemsound.quest_middle");
						st.set("cond", "14");
					}
					else if (cond == 14)
					{
						htmltext = "30196-09.htm";
					}
				}
				break;
			case State.COMPLETED:
				if (npcId == Ascalon)
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
		
		int cond = st.getInt("cond");
		
		int npcId = npc.getId();
		for (int[] drop : DROPLIST)
		{
			if ((drop[2] == npcId) && (drop[0] == cond))
			{
				st.rollAndGive(drop[3], 1, 1, drop[5], drop[4]);
				
				for (int[] drop2 : DROPLIST)
				{
					if ((drop2[0] == cond) && (st.getQuestItemsCount(drop2[3]) < drop2[5]))
					{
						return null;
					}
				}
				
				st.setCond(cond + 1);
				return null;
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _223_TestOfChampion(223, qn, "");
	}
}