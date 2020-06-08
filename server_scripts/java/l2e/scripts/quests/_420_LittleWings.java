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
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 25.09.2012
 * Based on L2J Eternity-World
 */
public class _420_LittleWings extends Quest
{
	private static final String qn = "_420_LittleWings";
	
	// Required Items
	private static final int REQUIRED_EGGS = 20;
	
	// Drop Rates
	private static final int BACK_DROP = 40;
	private static final int EGG_DROP = 50;
	
	// Quest Items
	private static final int FRY_STN = 3816;
	private static final int FRY_STN_DLX = 3817;
	private static final int FSN_LIST = 3818;
	private static final int FSN_LIST_DLX = 3819;
	private static final int TD_BCK_SKN = 3820;
	private static final int JUICE = 3821;
	private static final int SCALE_1 = 3822;
	private static final int EX_EGG = 3823;
	private static final int SCALE_2 = 3824;
	private static final int ZW_EGG = 3825;
	private static final int SCALE_3 = 3826;
	private static final int KA_EGG = 3827;
	private static final int SCALE_4 = 3828;
	private static final int SU_EGG = 3829;
	private static final int SCALE_5 = 3830;
	private static final int SH_EGG = 3831;
	private static final int FRY_DUST = 3499;
	
	private static final int[] QUESTITEMS =
	{
		FRY_STN,
		FRY_STN_DLX,
		FSN_LIST,
		FSN_LIST_DLX,
		TD_BCK_SKN,
		JUICE,
		SCALE_1,
		EX_EGG,
		SCALE_2,
		ZW_EGG,
		SCALE_3,
		KA_EGG,
		SCALE_4,
		SU_EGG,
		SCALE_5,
		SH_EGG,
		FRY_DUST
	};
	
	// Npcs
	private static final int PM_COOPER = 30829;
	private static final int SG_CRONOS = 30610;
	private static final int GD_BYRON = 30711;
	private static final int MC_MARIA = 30608;
	private static final int FR_MYMYU = 30747;
	private static final int DK_EXARION = 30748;
	private static final int DK_ZWOV = 30749;
	private static final int DK_KALIBRAN = 30750;
	private static final int WM_SUZET = 30751;
	private static final int WM_SHAMHAI = 30752;
	
	private static final int[] TALKERS =
	{
		PM_COOPER,
		SG_CRONOS,
		GD_BYRON,
		MC_MARIA,
		FR_MYMYU,
		DK_EXARION,
		DK_ZWOV,
		DK_KALIBRAN,
		WM_SUZET,
		WM_SHAMHAI
	};
	
	// Mobs
	private static final int TD_LORD = 20231;
	private static final int LO_LZRD_W = 20580;
	private static final int MS_SPIDER = 20233;
	private static final int RD_SCVNGR = 20551;
	private static final int BO_OVERLD = 20270;
	private static final int DD_SEEKER = 20202;
	private static final int FLINE = 20589;
	private static final int LIELE = 20590;
	private static final int VL_TREANT = 20591;
	private static final int SATYR = 20592;
	private static final int UNICORN = 20593;
	private static final int FR_RUNNER = 20594;
	private static final int FL_ELDER = 20595;
	private static final int LI_ELDER = 20596;
	private static final int VT_ELDER = 20597;
	private static final int ST_ELDER = 20598;
	private static final int UN_ELDER = 20599;
	private static final int SPIRIT_TIMINIEL = 21797;
	
	private static final int[] TO_KILL_ID =
	{
		TD_LORD,
		LO_LZRD_W,
		MS_SPIDER,
		RD_SCVNGR,
		BO_OVERLD,
		DD_SEEKER,
		FLINE,
		LIELE,
		VL_TREANT,
		SATYR,
		UNICORN,
		FR_RUNNER,
		FL_ELDER,
		LI_ELDER,
		VT_ELDER,
		ST_ELDER,
		UN_ELDER,
		SPIRIT_TIMINIEL
	};
	
	// Rewards
	private static final int FOOD = 4038;
	private static final int ARMOR = 3912;
	
	public _420_LittleWings(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(PM_COOPER);
		
		for (int npcTalkerId : TALKERS)
		{
			addTalkId(npcTalkerId);
		}
		
		for (int npcToKillId : TO_KILL_ID)
		{
			addKillId(npcToKillId);
		}
		
		questItemIds = QUESTITEMS;
	}
	
	private String checkEggs(QuestState st, String npc, int progress)
	{
		String htmltext = null;
		int eggs = 0;
		int whom = st.getInt("dragon");
		
		if (whom == 1)
		{
			eggs = EX_EGG;
		}
		else if (whom == 2)
		{
			eggs = ZW_EGG;
		}
		else if (whom == 3)
		{
			eggs = KA_EGG;
		}
		else if (whom == 4)
		{
			eggs = SU_EGG;
		}
		else if (whom == 5)
		{
			eggs = SH_EGG;
		}
		
		if (npc.equalsIgnoreCase("mymyu"))
		{
			if (((progress == 19) || (progress == 20)) && (st.getQuestItemsCount(eggs) == 1))
			{
				htmltext = "420_" + npc + "_10.htm";
			}
			else
			{
				if (st.getQuestItemsCount(eggs) >= 20)
				{
					htmltext = "420_" + npc + "_9.htm";
				}
				else
				{
					htmltext = "420_" + npc + "_8.htm";
				}
			}
		}
		else if (npc.equalsIgnoreCase("exarion") && (whom == 1))
		{
			if (st.getQuestItemsCount(eggs) < 20)
			{
				htmltext = "420_" + npc + "_3.htm";
			}
			else
			{
				st.takeItems(eggs, 20);
				st.takeItems(SCALE_1, 1);
				if ((progress == 14) || (progress == 21))
				{
					st.set("progress", "19");
				}
				else if ((progress == 15) || (progress == 22))
				{
					st.set("progress", "20");
				}
				st.giveItems(eggs, 1);
				st.playSound("ItemSound.quest_itemget");
				st.set("cond", "7");
				htmltext = "420_" + npc + "_4.htm";
			}
		}
		else if (npc.equalsIgnoreCase("zwov") && (whom == 2))
		{
			if (st.getQuestItemsCount(eggs) < 20)
			{
				htmltext = "420_" + npc + "_3.htm";
			}
			else
			{
				st.takeItems(eggs, 20);
				st.takeItems(SCALE_2, 1);
				if ((progress == 14) || (progress == 21))
				{
					st.set("progress", "19");
				}
				else if ((progress == 15) || (progress == 22))
				{
					st.set("progress", "20");
				}
				st.giveItems(eggs, 1);
				st.set("cond", "7");
				st.playSound("ItemSound.quest_itemget");
				htmltext = "420_" + npc + "_4.htm";
			}
		}
		else if (npc.equalsIgnoreCase("kalibran") && (whom == 3))
		{
			if (st.getQuestItemsCount(eggs) < 20)
			{
				htmltext = "420_" + npc + "_3.htm";
			}
			else
			{
				st.takeItems(eggs, 20);
				htmltext = "420_" + npc + "_4.htm";
			}
		}
		else if (npc.equalsIgnoreCase("suzet") && (whom == 4))
		{
			if (st.getQuestItemsCount(eggs) < 20)
			{
				htmltext = "420_" + npc + "_4.htm";
			}
			else
			{
				st.takeItems(eggs, 20);
				st.takeItems(SCALE_4, 1);
				if ((progress == 14) || (progress == 21))
				{
					st.set("progress", "19");
				}
				else if ((progress == 15) || (progress == 22))
				{
					st.set("progress", "20");
				}
				st.giveItems(eggs, 1);
				st.set("cond", "7");
				st.playSound("ItemSound.quest_itemget");
				htmltext = "420_" + npc + "_5.htm";
			}
		}
		else if (npc.equalsIgnoreCase("shamhai") && (whom == 5))
		{
			if (st.getQuestItemsCount(eggs) < 20)
			{
				htmltext = "420_" + npc + "_3.htm";
			}
			else
			{
				st.takeItems(eggs, 20);
				st.takeItems(SCALE_5, 1);
				if ((progress == 14) || (progress == 21))
				{
					st.set("progress", "19");
				}
				else if ((progress == 15) || (progress == 22))
				{
					st.set("progress", "20");
				}
				st.giveItems(eggs, 1);
				st.set("cond", "7");
				st.playSound("ItemSound.quest_itemget");
				htmltext = "420_" + npc + "_4.htm";
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
		{
			return null;
		}
		
		String htmltext = event;
		int state = st.getState();
		int progress = st.getInt("progress");
		int cond = st.getInt("cond");
		
		if (state == State.CREATED)
		{
			st.set("cond", "0");
			if (event.equalsIgnoreCase("ido"))
			{
				st.setState(State.STARTED);
				st.set("progress", "0");
				st.set("cond", "1");
				st.set("dragon", "0");
				st.playSound("ItemSound.quest_accept");
				htmltext = "Starting.htm";
			}
		}
		else if ((state == State.STARTED) && (cond < 5))
		{
			if (event.equalsIgnoreCase("wait"))
			{
				if ((progress == 1) || (progress == 2) || (progress == 8) || (progress == 9))
				{
					if ((progress == 1) || (progress == 8))
					{
						st.takeItems(2130, 1);
						st.takeItems(1873, 3);
						st.takeItems(TD_BCK_SKN, 10);
						st.takeItems(FSN_LIST, 1);
						st.giveItems(FRY_STN, 1);
						htmltext = "420_maria_3.htm";
					}
					else
					{
						st.takeItems(2131, 1);
						st.takeItems(1873, 5);
						st.takeItems(1875, 1);
						st.takeItems(TD_BCK_SKN, 20);
						st.takeItems(FSN_LIST_DLX, 1);
						st.giveItems(FRY_STN_DLX, 1);
						htmltext = "420_maria_5.htm";
					}
					st.takeItems(1870, 10);
					st.takeItems(1871, 10);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if (event.equalsIgnoreCase("cronos_2"))
			{
				htmltext = "420_cronos_2.htm";
			}
			else if (event.equalsIgnoreCase("cronos_3"))
			{
				htmltext = "420_cronos_3.htm";
			}
			else if (event.equalsIgnoreCase("cronos_4"))
			{
				htmltext = "420_cronos_4.htm";
			}
			else if (event.equalsIgnoreCase("fsn"))
			{
				st.set("cond", "2");
				if (progress == 0)
				{
					st.set("progress", "1");
					st.giveItems(FSN_LIST, 1);
					st.playSound("ItemSound.quest_itemget");
					htmltext = "420_cronos_5.htm";
				}
				else if (progress == 7)
				{
					st.set("progress", "8");
					st.giveItems(FSN_LIST, 1);
					st.playSound("ItemSound.quest_itemget");
					htmltext = "420_cronos_12.htm";
				}
			}
			else if (event.equalsIgnoreCase("fsn_dlx"))
			{
				st.set("cond", "2");
				if (progress == 0)
				{
					st.set("progress", "2");
					st.giveItems(FSN_LIST_DLX, 1);
					st.playSound("ItemSound.quest_itemget");
					htmltext = "420_cronos_6.htm";
				}
				else if (progress == 7)
				{
					st.set("progress", "9");
					st.giveItems(FSN_LIST_DLX, 1);
					st.playSound("ItemSound.quest_itemget");
					htmltext = "420_cronos_13.htm";
				}
			}
			else if (event.equalsIgnoreCase("showfsn"))
			{
				htmltext = "420_byron_2.htm";
			}
			else if (event.equalsIgnoreCase("askmore"))
			{
				st.set("cond", "4");
				if (progress == 3)
				{
					st.set("progress", "5");
					htmltext = "420_byron_3.htm";
				}
				else if (progress == 4)
				{
					st.set("progress", "6");
					htmltext = "420_byron_4.htm";
				}
			}
			else if (event.equalsIgnoreCase("give_fsn"))
			{
				st.takeItems(FRY_STN, 1);
				htmltext = "420_mymyu_2.htm";
			}
			else if (event.equalsIgnoreCase("give_fsn_dlx"))
			{
				st.takeItems(FRY_STN_DLX, 1);
				st.giveItems(FRY_DUST, 1);
				st.playSound("ItemSound.quest_itemget");
				htmltext = "420_mymyu_4.htm";
			}
			else if (event.equalsIgnoreCase("fry_ask"))
			{
				htmltext = "420_mymyu_5.htm";
			}
			else if (event.equalsIgnoreCase("ask_abt"))
			{
				st.set("cond", "5");
				st.giveItems(JUICE, 1);
				st.playSound("ItemSound.quest_itemget");
				htmltext = "420_mymyu_6.htm";
			}
		}
		else if ((state == State.STARTED) && (cond >= 5))
		{
			if (event.equalsIgnoreCase("exarion_1"))
			{
				st.giveItems(SCALE_1, 1);
				st.playSound("ItemSound.quest_itemget");
				st.set("dragon", "1");
				st.set("cond", "6");
				st.set("progress", String.valueOf(progress + 9));
				htmltext = "420_exarion_2.htm";
			}
			else if (event.equalsIgnoreCase("kalibran_1"))
			{
				st.set("dragon", "3");
				st.set("cond", "6");
				st.giveItems(SCALE_3, 1);
				st.playSound("ItemSound.quest_itemget");
				st.set("progress", String.valueOf(progress + 9));
				htmltext = "420_kalibran_2.htm";
			}
			else if (event.equalsIgnoreCase("kalibran_2"))
			{
				if (st.getQuestItemsCount(SCALE_3) > 0)
				{
					if ((progress == 14) || (progress == 21))
					{
						st.set("progress", "19");
					}
					else if ((progress == 15) || (progress == 22))
					{
						st.set("progress", "20");
					}
					st.takeItems(SCALE_3, 1);
					st.giveItems(KA_EGG, 1);
					st.set("cond", "7");
					st.playSound("ItemSound.quest_itemget");
					htmltext = "420_kalibran_5.htm";
				}
			}
			else if (event.equalsIgnoreCase("zwov_1"))
			{
				st.set("dragon", "2");
				st.set("cond", "6");
				st.giveItems(SCALE_2, 1);
				st.playSound("ItemSound.quest_itemget");
				st.set("progress", String.valueOf(progress + 9));
				htmltext = "420_zwov_2.htm";
			}
			else if (event.equalsIgnoreCase("shamhai_1"))
			{
				st.set("dragon", "5");
				st.set("cond", "6");
				st.giveItems(SCALE_5, 1);
				st.playSound("ItemSound.quest_itemget");
				st.set("progress", String.valueOf(progress + 9));
				htmltext = "420_shamhai_2.htm";
			}
			else if (event.equalsIgnoreCase("suzet_1"))
			{
				htmltext = "420_suzet_2.htm";
			}
			else if (event.equalsIgnoreCase("suzet_2"))
			{
				st.set("dragon", "4");
				st.set("cond", "6");
				st.giveItems(SCALE_4, 1);
				st.playSound("ItemSound.quest_itemget");
				st.set("progress", String.valueOf(progress + 9));
				htmltext = "420_suzet_3.htm";
			}
			else if (event.equalsIgnoreCase("hatch"))
			{
				int eggs = 0;
				int whom = st.getInt("dragon");
				if (whom == 1)
				{
					eggs = EX_EGG;
				}
				else if (whom == 2)
				{
					eggs = ZW_EGG;
				}
				else if (whom == 3)
				{
					eggs = KA_EGG;
				}
				else if (whom == 4)
				{
					eggs = SU_EGG;
				}
				else if (whom == 5)
				{
					eggs = SH_EGG;
				}
				if ((st.getQuestItemsCount(eggs) > 0) && ((progress == 19) || (progress == 20)))
				{
					st.takeItems(eggs, 1);
					if (progress == 19)
					{
						st.giveItems(3500 + st.getRandom(3), 1);
						st.exitQuest(true);
						st.playSound("ItemSound.quest_finish");
						htmltext = "420_mymyu_15.htm";
					}
					else if (progress == 20)
					{
						st.set("progress", "22");
						htmltext = "420_mymyu_11.htm";
					}
				}
			}
			else if (event.equalsIgnoreCase("give_dust"))
			{
				if (st.getQuestItemsCount(FRY_DUST) > 0)
				{
					st.takeItems(FRY_DUST, 1);
					int luck = st.getRandom(2);
					int extra, qty;
					if (luck == 0)
					{
						extra = ARMOR;
						qty = 1;
						htmltext = "420_mymyu_13.htm";
					}
					else
					{
						extra = FOOD;
						qty = 100;
						htmltext = "420_mymyu_14.htm";
					}
					st.giveItems(3500 + st.getRandom(3), 1);
					st.giveItems(extra, qty);
					st.exitQuest(true);
					st.playSound("ItemSound.quest_finish");
				}
				else
				{
					st.giveItems(3500 + st.getRandom(3), 1);
					st.exitQuest(true);
					st.playSound("ItemSound.quest_finish");
					htmltext = "420_mymyu_12.htm";
				}
			}
			else if (event.equalsIgnoreCase("no_dust"))
			{
				st.giveItems(3500 + st.getRandom(3), 1);
				st.exitQuest(true);
				st.playSound("ItemSound.quest_finish");
				htmltext = "420_mymyu_12.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = Quest.getNoQuestMsg(talker);
		QuestState st = talker.getQuestState(getName());
		
		if (st == null)
		{
			return htmltext;
		}
		
		int state = st.getState();
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		int progress = st.getInt("progress");
		
		long _coal = st.getQuestItemsCount(1870);
		long _char = st.getQuestItemsCount(1871);
		long _gemd = st.getQuestItemsCount(2130);
		long _gemc = st.getQuestItemsCount(2131);
		long _snug = st.getQuestItemsCount(1873);
		long _sofp = st.getQuestItemsCount(1875);
		long _tdbk = st.getQuestItemsCount(TD_BCK_SKN);
		
		if (state == State.COMPLETED)
		{
			st.setState(State.CREATED);
			state = State.CREATED;
		}
		
		if (npcId == PM_COOPER)
		{
			if (state == State.CREATED)
			{
				if (talker.getLevel() < 35)
				{
					st.exitQuest(true);
					htmltext = "420_low_level.htm";
				}
				htmltext = "Start.htm";
			}
			else if ((state == State.STARTED) && (cond < 5) && (progress == 0))
			{
				htmltext = "Starting.htm";
			}
			else
			{
				htmltext = "Started.htm";
			}
		}
		else if (npcId == SG_CRONOS)
		{
			if ((state == State.STARTED) && (cond < 5))
			{
				if (progress == 0)
				{
					htmltext = "420_cronos_1.htm";
				}
				else if ((progress == 1) || (progress == 2) || (progress == 8) || (progress == 9))
				{
					if (st.getQuestItemsCount(FRY_STN) == 1)
					{
						st.set("cond", "3");
						if (progress == 1)
						{
							st.set("progress", "3");
							htmltext = "420_cronos_8.htm";
						}
						else if (progress == 8)
						{
							st.set("progress", "10");
							htmltext = "420_cronos_14.htm";
						}
					}
					else if (st.getQuestItemsCount(FRY_STN_DLX) == 1)
					{
						if (progress == 2)
						{
							st.set("progress", "4");
							htmltext = "420_cronos_8.htm";
						}
						else if (progress == 9)
						{
							st.set("progress", "11");
							htmltext = "420_cronos_14.htm";
						}
					}
					else
					{
						htmltext = "420_cronos_7.htm";
					}
				}
				else if ((progress == 3) || (progress == 4) || (progress == 10) || (progress == 11))
				{
					htmltext = "420_cronos_9.htm";
				}
				else if ((progress == 5) || (progress == 6) || (progress == 12) || (progress == 13))
				{
					htmltext = "420_cronos_11.htm";
				}
				else if (progress == 7)
				{
					htmltext = "420_cronos_10.htm";
				}
			}
		}
		else if (npcId == MC_MARIA)
		{
			if ((state == State.STARTED) && (cond < 5))
			{
				if ((((progress == 1) || (progress == 8)) && (st.getQuestItemsCount(FSN_LIST) == 1)) || (((progress == 2) || (progress == 9)) && (st.getQuestItemsCount(FSN_LIST_DLX) == 1)))
				{
					if ((progress == 1) || (progress == 8))
					{
						htmltext = (_coal >= 10) && (_char >= 10) && (_gemd >= 1) && (_snug >= 3) && (_tdbk >= 10) ? "420_maria_2.htm" : "420_maria_1.htm";
					}
					else if ((progress == 2) || (progress == 9))
					{
						htmltext = (_coal >= 10) && (_char >= 10) && (_gemc >= 1) && (_snug >= 5) && (_sofp >= 1) && (_tdbk >= 20) ? "420_maria_4.htm" : "420_maria_1.htm";
					}
				}
				else if ((progress >= 3) && (progress <= 11))
				{
					htmltext = "420_maria_6.htm";
				}
			}
		}
		else if (npcId == GD_BYRON)
		{
			if ((state == State.STARTED) && (cond < 5))
			{
				if ((((progress == 1) || (progress == 8)) && (st.getQuestItemsCount(FSN_LIST) == 1)) || (((progress == 2) || (progress == 9)) && (st.getQuestItemsCount(FSN_LIST_DLX) == 1)))
				{
					htmltext = "420_byron_10.htm";
				}
				else if (progress == 7)
				{
					htmltext = "420_byron_9.htm";
				}
				else if (((progress == 3) && (st.getQuestItemsCount(FRY_STN) == 1)) || ((progress == 4) && (st.getQuestItemsCount(FRY_STN_DLX) == 1)))
				{
					htmltext = "420_byron_1.htm";
				}
				else if ((progress == 10) && (st.getQuestItemsCount(FRY_STN) == 1))
				{
					st.set("progress", "12");
					htmltext = "420_byron_5.htm";
				}
				else if ((progress == 11) && (st.getQuestItemsCount(FRY_STN_DLX) == 1))
				{
					st.set("progress", "13");
					htmltext = "420_byron_6.htm";
				}
				else if ((progress == 5) || (progress == 12))
				{
					htmltext = "420_byron_7.htm";
				}
				else if ((progress == 6) || (progress == 13))
				{
					htmltext = "420_byron_8.htm";
				}
			}
		}
		else if (npcId == FR_MYMYU)
		{
			if ((state == State.STARTED) && (cond < 5))
			{
				if ((progress == 5) || (progress == 12))
				{
					htmltext = st.getQuestItemsCount(FRY_STN) == 1 ? "420_mymyu_1.htm" : "420_mymyu_5.htm";
				}
				else if ((progress == 6) || (progress == 13))
				{
					htmltext = st.getQuestItemsCount(FRY_STN_DLX) == 1 ? "420_mymyu_3.htm" : "420_mymyu_5.htm";
				}
			}
			else if ((state == State.STARTED) && (cond >= 5))
			{
				if ((progress < 14) && (st.getQuestItemsCount(JUICE) == 1))
				{
					htmltext = "420_mymyu_7.htm";
				}
				else if (progress == 22)
				{
					htmltext = "420_mymyu_11.htm";
				}
				else if (progress > 13)
				{
					htmltext = checkEggs(st, "mymyu", progress);
				}
			}
		}
		else if (npcId == DK_EXARION)
		{
			if ((state == State.STARTED) && (cond >= 5))
			{
				if (((progress == 5) || (progress == 6) || (progress == 12) || (progress == 13)) && (st.getQuestItemsCount(JUICE) == 1))
				{
					st.takeItems(JUICE, 1);
					htmltext = "420_exarion_1.htm";
				}
				else if ((progress > 13) && (st.getQuestItemsCount(SCALE_1) == 1))
				{
					htmltext = checkEggs(st, "exarion", progress);
				}
				else if (((progress == 19) || (progress == 20)) && (st.getQuestItemsCount(EX_EGG) == 1))
				{
					htmltext = "420_exarion_5.htm";
				}
			}
		}
		else if (npcId == DK_ZWOV)
		{
			if ((state == State.STARTED) && (cond >= 5))
			{
				if (((progress == 5) || (progress == 6) || (progress == 12) || (progress == 13)) && (st.getQuestItemsCount(JUICE) == 1))
				{
					st.takeItems(JUICE, 1);
					htmltext = "420_zwov_1.htm";
				}
				else if ((progress > 13) && (st.getQuestItemsCount(SCALE_2) == 1))
				{
					htmltext = checkEggs(st, "zwov", progress);
				}
				else if (((progress == 19) || (progress == 20)) && (st.getQuestItemsCount(ZW_EGG) == 1))
				{
					htmltext = "420_zwov_5.htm";
				}
			}
		}
		else if (npcId == DK_KALIBRAN)
		{
			if ((state == State.STARTED) && (cond >= 5))
			{
				if (((progress == 5) || (progress == 6) || (progress == 12) || (progress == 13)) && (st.getQuestItemsCount(JUICE) == 1))
				{
					st.takeItems(JUICE, 1);
					htmltext = "420_kalibran_1.htm";
				}
				else if ((progress > 13) && (st.getQuestItemsCount(SCALE_3) == 1))
				{
					htmltext = checkEggs(st, "kalibran", progress);
				}
				else if (((progress == 19) || (progress == 20)) && (st.getQuestItemsCount(KA_EGG) == 1))
				{
					htmltext = "420_kalibran_6.htm";
				}
			}
		}
		else if (npcId == WM_SUZET)
		{
			if ((state == State.STARTED) && (cond >= 5))
			{
				if (((progress == 5) || (progress == 6) || (progress == 12) || (progress == 13)) && (st.getQuestItemsCount(JUICE) == 1))
				{
					st.takeItems(JUICE, 1);
					htmltext = "420_suzet_1.htm";
				}
				else if ((progress > 13) && (st.getQuestItemsCount(SCALE_4) == 1))
				{
					htmltext = checkEggs(st, "suzet", progress);
				}
				else if (((progress == 19) || (progress == 20)) && (st.getQuestItemsCount(SU_EGG) == 1))
				{
					htmltext = "420_suzet_6.htm";
				}
			}
		}
		else if (npcId == WM_SHAMHAI)
		{
			if ((state == State.STARTED) && (cond >= 5))
			{
				if (((progress == 5) || (progress == 6) || (progress == 12) || (progress == 13)) && (st.getQuestItemsCount(JUICE) == 1))
				{
					st.takeItems(JUICE, 1);
					htmltext = "420_shamhai_1.htm";
				}
				else if ((progress > 13) && (st.getQuestItemsCount(SCALE_5) == 1))
				{
					htmltext = checkEggs(st, "shamhai", progress);
				}
				else if (((progress == 19) || (progress == 20)) && (st.getQuestItemsCount(SH_EGG) == 1))
				{
					htmltext = "420_shamhai_5.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		QuestState st = killer.getQuestState(getName());
		
		if (st == null)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		final int state = st.getState();
		final int npcId = npc.getId();
		final int cond = st.getInt("cond");
		final int whom = st.getInt("dragon");
		final int progress = st.getInt("progress");
		long skins = st.getQuestItemsCount(TD_BCK_SKN);
		long fsn = st.getQuestItemsCount(FSN_LIST);
		int eggs = 0, eggDropper = 0, scale = 0;
		
		if (((state == State.STARTED) && (cond < 5) && ((st.getQuestItemsCount(FSN_LIST) == 1) && (skins < 10))) || ((st.getQuestItemsCount(FSN_LIST_DLX) == 1) && (skins < 20)))
		{
			if (npcId == TD_LORD)
			{
				long count = fsn == 1 ? 10 : 20;
				st.dropQuestItems(TD_BCK_SKN, 1, 1, count, false, BACK_DROP, true);
			}
		}
		else if ((state == State.STARTED) && (cond >= 5) && ((progress == 14) || (progress == 15) || (progress == 21) || (progress == 22)))
		{
			if (whom == 1)
			{
				eggs = EX_EGG;
				scale = SCALE_1;
				eggDropper = LO_LZRD_W;
			}
			else if (whom == 2)
			{
				eggs = ZW_EGG;
				scale = SCALE_2;
				eggDropper = MS_SPIDER;
			}
			else if (whom == 3)
			{
				eggs = KA_EGG;
				scale = SCALE_3;
				eggDropper = RD_SCVNGR;
			}
			else if (whom == 4)
			{
				eggs = SU_EGG;
				scale = SCALE_4;
				eggDropper = BO_OVERLD;
			}
			else if (whom == 5)
			{
				eggs = SH_EGG;
				scale = SCALE_5;
				eggDropper = DD_SEEKER;
			}
			
			long prevItems = st.getQuestItemsCount(eggs);
			if ((st.getQuestItemsCount(scale) == 1) && (prevItems < REQUIRED_EGGS))
			{
				if (npcId == eggDropper)
				{
					st.dropQuestItems(eggs, 1, 1, REQUIRED_EGGS, false, EGG_DROP, true);
					npc.broadcastNpcSay("If the eggs get taken, we're dead!");
				}
			}
		}
		else if ((state == State.STARTED) && (cond < 5) && (st.getQuestItemsCount(FRY_STN_DLX) == 1))
		{
			if (Util.contains(TO_KILL_ID, npcId) && (npcId != SPIRIT_TIMINIEL))
			{
				st.takeItems(FRY_STN_DLX, 1);
				st.set("progress", "7");
				killer.sendMessage("You lost fairy stone deluxe!");
			}
		}	
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _420_LittleWings(420, qn, "");
	}
}