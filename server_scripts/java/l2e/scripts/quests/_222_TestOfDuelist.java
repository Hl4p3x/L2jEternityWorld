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
public class _222_TestOfDuelist extends Quest
{
	private static final String qn = "_222_TestOfDuelist";
	
	private static final int Kaien = 30623;
	
	private static final int OrderGludio = 2763;
	private static final int OrderDion = 2764;
	private static final int OrderGiran = 2765;
	private static final int OrderOren = 2766;
	private static final int OrderAden = 2767;
	private static final int PunchersShard = 2768;
	private static final int NobleAntsFeeler = 2769;
	private static final int DronesChitin = 2770;
	private static final int DeadSeekerFang = 2771;
	private static final int OverlordNecklace = 2772;
	private static final int FetteredSoulsChain = 2773;
	private static final int ChiefsAmulet = 2774;
	private static final int EnchantedEyeMeat = 2775;
	private static final int TamrinOrcsRing = 2776;
	private static final int TamrinOrcsArrow = 2777;
	private static final int FinalOrder = 2778;
	private static final int ExcurosSkin = 2779;
	private static final int KratorsShard = 2780;
	private static final int GrandisSkin = 2781;
	private static final int TimakOrcsBelt = 2782;
	private static final int LakinsMace = 2783;
	
	private static final int MarkOfDuelist = 2762;
	
	private static final int Puncher = 20085;
	private static final int NobleAntLeader = 20090;
	private static final int MarshStakatoDrone = 20234;
	private static final int DeadSeeker = 20202;
	private static final int BrekaOrcOverlord = 20270;
	private static final int FetteredSoul = 20552;
	private static final int LetoLizardmanOverlord = 20582;
	private static final int EnchantedMonstereye = 20564;
	private static final int TamlinOrc = 20601;
	private static final int TamlinOrcArcher = 20602;
	private static final int Excuro = 20214;
	private static final int Krator = 20217;
	private static final int Grandis = 20554;
	private static final int TimakOrcOverlord = 20588;
	private static final int Lakin = 20604;
	
	private static final int[][] DROPLIST_COND =
	{
		{
			2,
			0,
			Puncher,
			0,
			PunchersShard,
			10,
			70,
			1
		},
		{
			2,
			0,
			NobleAntLeader,
			0,
			NobleAntsFeeler,
			10,
			70,
			1
		},
		{
			2,
			0,
			MarshStakatoDrone,
			0,
			DronesChitin,
			10,
			70,
			1
		},
		{
			2,
			0,
			DeadSeeker,
			0,
			DeadSeekerFang,
			10,
			70,
			1
		},
		{
			2,
			0,
			BrekaOrcOverlord,
			0,
			OverlordNecklace,
			10,
			70,
			1
		},
		{
			2,
			0,
			FetteredSoul,
			0,
			FetteredSoulsChain,
			10,
			70,
			1
		},
		{
			2,
			0,
			LetoLizardmanOverlord,
			0,
			ChiefsAmulet,
			10,
			70,
			1
		},
		{
			2,
			0,
			EnchantedMonstereye,
			0,
			EnchantedEyeMeat,
			10,
			70,
			1
		},
		{
			2,
			0,
			TamlinOrc,
			0,
			TamrinOrcsRing,
			10,
			70,
			1
		},
		{
			2,
			0,
			TamlinOrcArcher,
			0,
			TamrinOrcsArrow,
			10,
			70,
			1
		},
		{
			4,
			0,
			Excuro,
			0,
			ExcurosSkin,
			3,
			70,
			1
		},
		{
			4,
			0,
			Krator,
			0,
			KratorsShard,
			3,
			70,
			1
		},
		{
			4,
			0,
			Grandis,
			0,
			GrandisSkin,
			3,
			70,
			1
		},
		{
			4,
			0,
			TimakOrcOverlord,
			0,
			TimakOrcsBelt,
			3,
			70,
			1
		},
		{
			4,
			0,
			Lakin,
			0,
			LakinsMace,
			3,
			70,
			1
		}
	};
	
	public _222_TestOfDuelist(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Kaien);
		addTalkId(Kaien);
		
		for (int[] element : DROPLIST_COND)
		{
			addKillId(element[2]);
			registerQuestItems(element[4]);
		}
		questItemIds = new int[]
		{
			OrderGludio,
			OrderDion,
			OrderGiran,
			OrderOren,
			OrderAden,
			FinalOrder
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
		
		if (event.equalsIgnoreCase("30623-04.htm"))
		{
			if (player.getRace().ordinal() == 3)
			{
				htmltext = "30623-05.htm";
			}
		}
		else if (event.equalsIgnoreCase("30623-07.htm"))
		{
			st.set("cond", "2");
			st.setState(State.STARTED);
			st.giveItems(OrderGludio, 1);
			st.giveItems(OrderDion, 1);
			st.giveItems(OrderGiran, 1);
			st.giveItems(OrderOren, 1);
			st.giveItems(OrderAden, 1);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30623-16.htm"))
		{
			st.takeItems(PunchersShard, -1);
			st.takeItems(NobleAntsFeeler, -1);
			st.takeItems(DronesChitin, -1);
			st.takeItems(DeadSeekerFang, -1);
			st.takeItems(OverlordNecklace, -1);
			st.takeItems(FetteredSoulsChain, -1);
			st.takeItems(ChiefsAmulet, -1);
			st.takeItems(EnchantedEyeMeat, -1);
			st.takeItems(TamrinOrcsRing, -1);
			st.takeItems(TamrinOrcsArrow, -1);
			st.takeItems(OrderGludio, -1);
			st.takeItems(OrderDion, -1);
			st.takeItems(OrderGiran, -1);
			st.takeItems(OrderOren, -1);
			st.takeItems(OrderAden, -1);
			st.giveItems(FinalOrder, 1);
			st.set("cond", "4");
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
		
		int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.CREATED:
				if ((player.getClassId().getId() == 0x01) || (player.getClassId().getId() == 0x2f) || (player.getClassId().getId() == 0x13) || (player.getClassId().getId() == 0x20))
				{
					if (player.getLevel() >= 39)
					{
						htmltext = "30623-03.htm";
					}
					else
					{
						htmltext = "30623-01.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "30623-02.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (cond == 2)
				{
					htmltext = "30623-14.htm";
				}
				else if (cond == 3)
				{
					htmltext = "30623-13.htm";
				}
				else if (cond == 4)
				{
					htmltext = "30623-17.htm";
				}
				else if (cond == 5)
				{
					st.giveItems(MarkOfDuelist, 1);
					st.addExpAndSp(594888, 61408);
					st.giveItems(57, 161806);
					st.giveItems(7562, 72);
					st.giveItems(8870, 15);
					htmltext = "30623-18.htm";
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
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
		if ((cond == 2) && (st.getQuestItemsCount(PunchersShard) >= 10) && (st.getQuestItemsCount(NobleAntsFeeler) >= 10) && (st.getQuestItemsCount(DronesChitin) >= 10) && (st.getQuestItemsCount(DeadSeekerFang) >= 10) && (st.getQuestItemsCount(OverlordNecklace) >= 10) && (st.getQuestItemsCount(FetteredSoulsChain) >= 10) && (st.getQuestItemsCount(ChiefsAmulet) >= 10) && (st.getQuestItemsCount(EnchantedEyeMeat) >= 10) && (st.getQuestItemsCount(TamrinOrcsRing) >= 10) && (st.getQuestItemsCount(TamrinOrcsArrow) >= 10))
		{
			st.set("cond", "3");
		}
		else if ((cond == 4) && (st.getQuestItemsCount(ExcurosSkin) >= 3) && (st.getQuestItemsCount(KratorsShard) >= 3) && (st.getQuestItemsCount(LakinsMace) >= 3) && (st.getQuestItemsCount(GrandisSkin) >= 3) && (st.getQuestItemsCount(TimakOrcsBelt) >= 3))
		{
			st.set("cond", "5");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _222_TestOfDuelist(222, qn, "");
	}
}