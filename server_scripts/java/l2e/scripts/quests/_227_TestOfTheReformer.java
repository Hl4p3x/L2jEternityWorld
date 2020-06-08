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
public class _227_TestOfTheReformer extends Quest
{
	private static final String qn = "_227_TestOfTheReformer";
	
	private static final int Pupina = 30118;
	private static final int Sla = 30666;
	private static final int Katari = 30668;
	private static final int OlMahumPilgrimNPC = 30732;
	private static final int Kakan = 30669;
	private static final int Nyakuri = 30670;
	private static final int Ramus = 30667;
	
	private static final int BookOfReform = 2822;
	private static final int LetterOfIntroduction = 2823;
	private static final int SlasLetter = 2824;
	private static final int Greetings = 2825;
	private static final int OlMahumMoney = 2826;
	private static final int KatarisLetter = 2827;
	private static final int NyakurisLetter = 2828;
	private static final int KakansLetter = 3037;
	private static final int UndeadList = 2829;
	private static final int RamussLetter = 2830;
	private static final int RippedDiary = 2831;
	private static final int HugeNail = 2832;
	private static final int LetterOfBetrayer = 2833;
	private static final int BoneFragment1 = 2834;
	private static final int BoneFragment2 = 2835;
	private static final int BoneFragment3 = 2836;
	private static final int BoneFragment4 = 2837;
	private static final int BoneFragment5 = 2838;
	
	private static final int MarkOfReformer = 2821;
	
	private static final int NamelessRevenant = 27099;
	private static final int Aruraune = 27128;
	private static final int OlMahumInspector = 27129;
	private static final int OlMahumBetrayer = 27130;
	private static final int CrimsonWerewolf = 27131;
	private static final int KrudelLizardman = 27132;
	private static final int SilentHorror = 20404;
	private static final int SkeletonLord = 20104;
	private static final int SkeletonMarksman = 20102;
	private static final int MiserySkeleton = 20022;
	private static final int SkeletonArcher = 20100;
	
	public final int[][] DROPLIST_COND =
	{
		{
			18,
			0,
			SilentHorror,
			0,
			BoneFragment1,
			1,
			70,
			1
		},
		{
			18,
			0,
			SkeletonLord,
			0,
			BoneFragment2,
			1,
			70,
			1
		},
		{
			18,
			0,
			SkeletonMarksman,
			0,
			BoneFragment3,
			1,
			70,
			1
		},
		{
			18,
			0,
			MiserySkeleton,
			0,
			BoneFragment4,
			1,
			70,
			1
		},
		{
			18,
			0,
			SkeletonArcher,
			0,
			BoneFragment5,
			1,
			70,
			1
		}
	};
	
	public _227_TestOfTheReformer(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Pupina);
		addTalkId(Pupina);
		addTalkId(Sla);
		addTalkId(Katari);
		addTalkId(OlMahumPilgrimNPC);
		addTalkId(Kakan);
		addTalkId(Nyakuri);
		addTalkId(Ramus);
		
		addKillId(NamelessRevenant);
		addKillId(Aruraune);
		addKillId(OlMahumInspector);
		addKillId(OlMahumBetrayer);
		addKillId(CrimsonWerewolf);
		addKillId(KrudelLizardman);
		for (int[] element : DROPLIST_COND)
		{
			addKillId(element[2]);
			registerQuestItems(element[4]);
		}
		
		questItemIds = new int[]
		{
			BookOfReform,
			HugeNail,
			LetterOfIntroduction,
			SlasLetter,
			KatarisLetter,
			LetterOfBetrayer,
			OlMahumMoney,
			NyakurisLetter,
			UndeadList,
			Greetings,
			KakansLetter,
			RamussLetter,
			RippedDiary
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
			htmltext = "30118-04.htm";
			st.giveItems(BookOfReform, 1);
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30118_1"))
		{
			htmltext = "30118-06.htm";
			st.takeItems(HugeNail, -1);
			st.takeItems(BookOfReform, -1);
			st.giveItems(LetterOfIntroduction, 1);
			st.set("cond", "4");
		}
		else if (event.equalsIgnoreCase("30666_2"))
		{
			htmltext = "30666-02.htm";
		}
		else if (event.equalsIgnoreCase("30666_3"))
		{
			htmltext = "30666-04.htm";
			st.takeItems(LetterOfIntroduction, -1);
			st.giveItems(SlasLetter, 1);
			st.set("cond", "5");
		}
		else if (event.equalsIgnoreCase("30666_4"))
		{
			htmltext = "30666-02.htm";
		}
		else if (event.equalsIgnoreCase("30669_1"))
		{
			htmltext = "30669-02.htm";
		}
		else if (event.equalsIgnoreCase("30669_2"))
		{
			htmltext = "30669-03.htm";
			st.addSpawn(CrimsonWerewolf, -9382, -89852, -2333);
			st.set("cond", "12");
		}
		else if (event.equalsIgnoreCase("30669_3"))
		{
			htmltext = "30669-05.htm";
		}
		else if (event.equalsIgnoreCase("30670_1"))
		{
			htmltext = "30670-03.htm";
			st.addSpawn(KrudelLizardman, 126019, -179983, -1781);
			st.set("cond", "15");
		}
		else if (event.equalsIgnoreCase("30670_2"))
		{
			htmltext = "30670-02.htm";
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
				if (npcId == Pupina)
				{
					if ((player.getClassId().getId() == 0x0f) || (player.getClassId().getId() == 0x2a))
					{
						if (player.getLevel() >= 39)
						{
							htmltext = "30118-03.htm";
						}
						else
						{
							htmltext = "30118-01.htm";
							st.exitQuest(true);
						}
					}
					else
					{
						htmltext = "30118-02.htm";
						st.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				if (npcId == Pupina)
				{
					if (cond == 3)
					{
						htmltext = "30118-05.htm";
					}
					else if (cond >= 4)
					{
						htmltext = "30118-07.htm";
					}
				}
				else if (npcId == Sla)
				{
					if (cond == 4)
					{
						htmltext = "30666-01.htm";
					}
					else if (cond == 5)
					{
						htmltext = "30666-05.htm";
					}
					else if (cond == 10)
					{
						st.takeItems(OlMahumMoney, -1);
						st.giveItems(Greetings, 3);
						htmltext = "30666-06.htm";
						st.set("cond", "11");
					}
					else if (cond == 20)
					{
						st.takeItems(KatarisLetter, -1);
						st.takeItems(KakansLetter, -1);
						st.takeItems(NyakurisLetter, -1);
						st.takeItems(RamussLetter, -1);
						st.giveItems(MarkOfReformer, 1);
						st.addExpAndSp(1252844, 85972);
						st.giveItems(57, 226528);
						st.giveItems(7562, 60);
						htmltext = "30666-07.htm";
						st.set("cond", "0");
						st.exitQuest(false);
						st.playSound("ItemSound.quest_finish");
					}
				}
				else if (npcId == Katari)
				{
					if ((cond == 5) || (cond == 6))
					{
						st.takeItems(SlasLetter, -1);
						htmltext = "30668-01.htm";
						st.set("cond", "6");
						st.addSpawn(OlMahumPilgrimNPC, -4015, 40141, -3664);
						st.addSpawn(OlMahumInspector, -4034, 40201, -3665);
					}
					else if (cond == 8)
					{
						htmltext = "30668-02.htm";
						st.addSpawn(OlMahumBetrayer, -4106, 40174, -3660);
					}
					else if (cond == 9)
					{
						st.takeItems(LetterOfBetrayer, -1);
						st.giveItems(KatarisLetter, 1);
						htmltext = "30668-03.htm";
						st.set("cond", "10");
					}
				}
				else if (npcId == OlMahumPilgrimNPC)
				{
					if (cond == 7)
					{
						st.giveItems(OlMahumMoney, 1);
						htmltext = "30732-01.htm";
						st.set("cond", "8");
					}
				}
				else if (npcId == Kakan)
				{
					if ((cond == 11) || (cond == 12))
					{
						htmltext = "30669-01.htm";
					}
					else if (cond == 13)
					{
						st.takeItems(Greetings, 1);
						st.giveItems(KakansLetter, 1);
						htmltext = "30669-04.htm";
						st.set("cond", "14");
					}
				}
				else if (npcId == Nyakuri)
				{
					if ((cond == 14) || (cond == 15))
					{
						htmltext = "30670-01.htm";
					}
					else if (cond == 16)
					{
						st.takeItems(Greetings, 1);
						st.giveItems(NyakurisLetter, 1);
						htmltext = "30670-04.htm";
						st.set("cond", "17");
					}
				}
				else if (npcId == Ramus)
				{
					if (cond == 17)
					{
						st.takeItems(Greetings, -1);
						st.giveItems(UndeadList, 1);
						htmltext = "30667-01.htm";
						st.set("cond", "18");
					}
					else if (cond == 19)
					{
						st.takeItems(BoneFragment1, -1);
						st.takeItems(BoneFragment2, -1);
						st.takeItems(BoneFragment3, -1);
						st.takeItems(BoneFragment4, -1);
						st.takeItems(BoneFragment5, -1);
						st.takeItems(UndeadList, -1);
						st.giveItems(RamussLetter, 1);
						htmltext = "30667-03.htm";
						st.set("cond", "20");
					}
				}
				break;
			case State.COMPLETED:
				if (npcId == Pupina)
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
							st.playSound("ItemSound.quest_middle");
						}
					}
				}
			}
		}
		if ((cond == 18) && (st.getQuestItemsCount(BoneFragment1) != 0) && (st.getQuestItemsCount(BoneFragment2) != 0) && (st.getQuestItemsCount(BoneFragment3) != 0) && (st.getQuestItemsCount(BoneFragment4) != 0) && (st.getQuestItemsCount(BoneFragment5) != 0))
		{
			st.setCond(19);
			st.playSound("ItemSound.quest_middle");
		}
		else if ((npcId == NamelessRevenant) && ((cond == 1) || (cond == 2)))
		{
			if (st.getQuestItemsCount(RippedDiary) < 6)
			{
				st.giveItems(RippedDiary, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				st.takeItems(RippedDiary, -1);
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
				st.addSpawn(Aruraune);
			}
		}
		else if (npcId == Aruraune)
		{
			if (cond == 2)
			{
				if (st.getQuestItemsCount(HugeNail) == 0)
				{
					st.giveItems(HugeNail, 1);
				}
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "3");
			}
		}
		else if (npcId == OlMahumInspector)
		{
			if (cond == 6)
			{
				st.set("cond", "7");
			}
		}
		else if (npcId == OlMahumBetrayer)
		{
			if (cond == 8)
			{
				if (st.getQuestItemsCount(LetterOfBetrayer) == 0)
				{
					st.giveItems(LetterOfBetrayer, 1);
				}
				st.set("cond", "9");
			}
		}
		else if (npcId == CrimsonWerewolf)
		{
			if (cond == 12)
			{
				st.set("cond", "13");
			}
		}
		else if (npcId == KrudelLizardman)
		{
			if (cond == 15)
			{
				st.set("cond", "16");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _227_TestOfTheReformer(227, qn, "");
	}
}