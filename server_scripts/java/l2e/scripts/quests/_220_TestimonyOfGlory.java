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
import l2e.gameserver.network.serverpackets.SocialAction;

/**
 * Created by LordWinter 15.06.2013 Based on L2J Eternity-World
 */
public class _220_TestimonyOfGlory extends Quest
{
	private static final String qn = "_220_TestimonyOfGlory";
	
	private static final int Vokian = 30514;
	private static final int Chianta = 30642;
	private static final int Manakia = 30515;
	private static final int Kasman = 30501;
	private static final int Voltar = 30615;
	private static final int Kepra = 30616;
	private static final int Burai = 30617;
	private static final int Harak = 30618;
	private static final int Driko = 30619;
	private static final int Tanapi = 30571;
	private static final int Kakai = 30565;
	
	private static final int VokiansOrder = 3204;
	private static final int ManashenShard = 3205;
	private static final int TyrantTalon = 3206;
	private static final int GuardianBasiliskFang = 3207;
	private static final int VokiansOrder2 = 3208;
	private static final int NecklaceOfAuthority = 3209;
	private static final int ChiantaOrder1st = 3210;
	private static final int ScepterOfBreka = 3211;
	private static final int ScepterOfEnku = 3212;
	private static final int ScepterOfVuku = 3213;
	private static final int ScepterOfTurek = 3214;
	private static final int ScepterOfTunath = 3215;
	private static final int ChiantasOrder2rd = 3216;
	private static final int ChiantasOrder3rd = 3217;
	private static final int TamlinOrcSkull = 3218;
	private static final int TimakOrcHead = 3219;
	private static final int ScepterBox = 3220;
	private static final int PashikasHead = 3221;
	private static final int VultusHead = 3222;
	private static final int GloveOfVoltar = 3223;
	private static final int EnkuOverlordHead = 3224;
	private static final int GloveOfKepra = 3225;
	private static final int MakumBugbearHead = 3226;
	private static final int GloveOfBurai = 3227;
	private static final int ManakiaLetter1st = 3228;
	private static final int ManakiaLetter2st = 3229;
	private static final int KasmansLetter1rd = 3230;
	private static final int KasmansLetter2rd = 3231;
	private static final int KasmansLetter3rd = 3232;
	private static final int DrikosContract = 3233;
	private static final int StakatoDroneHusk = 3234;
	private static final int TanapisOrder = 3235;
	private static final int ScepterOfTantos = 3236;
	private static final int RitualBox = 3237;
	
	private static final int MarkOfGlory = 3203;
	
	private static final int Tyrant = 20192;
	private static final int TyrantKingpin = 20193;
	private static final int GuardianBasilisk = 20550;
	private static final int ManashenGargoyle = 20563;
	private static final int MarshStakatoDrone = 20234;
	private static final int PashikasSonOfVoltarQuestMonster = 27080;
	private static final int VultusSonOfVoltarQuestMonster = 27081;
	private static final int EnkuOrcOverlordQuestMonster = 27082;
	private static final int MakumBugbearThugQuestMonster = 27083;
	private static final int TimakOrc = 20583;
	private static final int TimakOrcArcher = 20584;
	private static final int TimakOrcSoldier = 20585;
	private static final int TimakOrcWarrior = 20586;
	private static final int TimakOrcShaman = 20587;
	private static final int TimakOrcOverlord = 20588;
	private static final int TamlinOrc = 20601;
	private static final int TamlinOrcArcher = 20602;
	private static final int RagnaOrcOverlord = 20778;
	private static final int RagnaOrcSeer = 20779;
	private static final int RevenantOfTantosChief = 27086;
	
	private static final int[][] DROPLIST_COND =
	{
		{
			1,
			0,
			ManashenGargoyle,
			VokiansOrder,
			ManashenShard,
			10,
			70,
			1
		},
		{
			1,
			0,
			Tyrant,
			VokiansOrder,
			TyrantTalon,
			10,
			70,
			1
		},
		{
			1,
			0,
			TyrantKingpin,
			VokiansOrder,
			TyrantTalon,
			10,
			70,
			1
		},
		{
			1,
			0,
			GuardianBasilisk,
			VokiansOrder,
			GuardianBasiliskFang,
			10,
			70,
			1
		},
		{
			4,
			0,
			MarshStakatoDrone,
			DrikosContract,
			StakatoDroneHusk,
			30,
			70,
			1
		},
		{
			4,
			0,
			EnkuOrcOverlordQuestMonster,
			GloveOfKepra,
			EnkuOverlordHead,
			4,
			100,
			1
		},
		{
			4,
			0,
			MakumBugbearThugQuestMonster,
			GloveOfBurai,
			MakumBugbearHead,
			2,
			100,
			1
		},
		{
			6,
			0,
			TimakOrc,
			ChiantasOrder3rd,
			TimakOrcHead,
			20,
			50,
			1
		},
		{
			6,
			0,
			TimakOrcArcher,
			ChiantasOrder3rd,
			TimakOrcHead,
			20,
			60,
			1
		},
		{
			6,
			0,
			TimakOrcSoldier,
			ChiantasOrder3rd,
			TimakOrcHead,
			20,
			70,
			1
		},
		{
			6,
			0,
			TimakOrcWarrior,
			ChiantasOrder3rd,
			TimakOrcHead,
			20,
			80,
			1
		},
		{
			6,
			0,
			TimakOrcShaman,
			ChiantasOrder3rd,
			TimakOrcHead,
			20,
			90,
			1
		},
		{
			6,
			0,
			TimakOrcOverlord,
			ChiantasOrder3rd,
			TimakOrcHead,
			20,
			100,
			1
		},
		{
			6,
			0,
			TamlinOrc,
			ChiantasOrder3rd,
			TamlinOrcSkull,
			20,
			50,
			1
		},
		{
			6,
			0,
			TamlinOrcArcher,
			ChiantasOrder3rd,
			TamlinOrcSkull,
			20,
			60,
			1
		}
	};
	
	public _220_TestimonyOfGlory(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Vokian);
		addTalkId(Vokian);
		addTalkId(Chianta);
		addTalkId(Manakia);
		addTalkId(Kasman);
		addTalkId(Voltar);
		addTalkId(Kepra);
		addTalkId(Burai);
		addTalkId(Harak);
		addTalkId(Driko);
		addTalkId(Tanapi);
		addTalkId(Kakai);
		
		for (int[] element : DROPLIST_COND)
		{
			addKillId(element[2]);
		}
		
		addKillId(PashikasSonOfVoltarQuestMonster);
		addKillId(VultusSonOfVoltarQuestMonster);
		addKillId(RagnaOrcOverlord);
		addKillId(RagnaOrcSeer);
		addKillId(RevenantOfTantosChief);
		
		questItemIds = (new int[]
		{
			VokiansOrder,
			VokiansOrder2,
			NecklaceOfAuthority,
			ChiantaOrder1st,
			ManakiaLetter1st,
			ManakiaLetter2st,
			KasmansLetter1rd,
			KasmansLetter2rd,
			KasmansLetter3rd,
			ScepterOfBreka,
			PashikasHead,
			VultusHead,
			GloveOfVoltar,
			GloveOfKepra,
			ScepterOfEnku,
			ScepterOfTurek,
			GloveOfBurai,
			ScepterOfTunath,
			DrikosContract,
			ChiantasOrder2rd,
			ChiantasOrder3rd,
			ScepterBox,
			TanapisOrder,
			ScepterOfTantos,
			RitualBox,
			ManashenShard,
			TyrantTalon,
			GuardianBasiliskFang,
			StakatoDroneHusk,
			EnkuOverlordHead,
			MakumBugbearHead,
			TimakOrcHead,
			TamlinOrcSkull
		});
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
		
		if (event.equalsIgnoreCase("RETURN"))
		{
			return null;
		}
		else if (event.equalsIgnoreCase("30514-05.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(VokiansOrder, 1);
		}
		else if (event.equalsIgnoreCase("30642-03.htm"))
		{
			st.takeItems(VokiansOrder2, -1);
			st.giveItems(ChiantaOrder1st, 1);
			st.set("cond", "4");
		}
		else if (event.equalsIgnoreCase("30571-03.htm"))
		{
			st.takeItems(ScepterBox, -1);
			st.giveItems(TanapisOrder, 1);
			st.set("cond", "9");
		}
		else if (event.equalsIgnoreCase("30642-07.htm"))
		{
			st.takeItems(ScepterOfBreka, -1);
			st.takeItems(ScepterOfEnku, -1);
			st.takeItems(ScepterOfVuku, -1);
			st.takeItems(ScepterOfTurek, -1);
			st.takeItems(ScepterOfTunath, -1);
			st.takeItems(ChiantaOrder1st, -1);
			if (st.getPlayer().getLevel() >= 37)
			{
				st.giveItems(ChiantasOrder3rd, 1);
				st.set("cond", "6");
			}
			else
			{
				htmltext = "30642-06.htm";
				st.giveItems(ChiantasOrder2rd, 1);
			}
		}
		else if (event.equalsIgnoreCase("BREKA"))
		{
			if (st.getQuestItemsCount(ScepterOfBreka) > 0)
			{
				htmltext = "30515-02.htm";
			}
			else
			{
				st.addRadar(80100, 119991, -2289);
				if (st.getQuestItemsCount(ManakiaLetter1st) > 0)
				{
					htmltext = "30515-04.htm";
				}
				else
				{
					htmltext = "30515-03.htm";
					st.giveItems(ManakiaLetter1st, 1);
				}
			}
		}
		else if (event.equalsIgnoreCase("ENKU"))
		{
			if (st.getQuestItemsCount(ScepterOfEnku) > 0)
			{
				htmltext = "30515-05.htm";
			}
			else
			{
				st.addRadar(17744, 189834, -3506);
				if (st.getQuestItemsCount(ManakiaLetter2st) > 0)
				{
					htmltext = "30515-07.htm";
				}
				else
				{
					htmltext = "30515-06.htm";
					st.giveItems(ManakiaLetter2st, 1);
				}
			}
		}
		else if (event.equalsIgnoreCase("VUKU"))
		{
			if (st.getQuestItemsCount(ScepterOfVuku) > 0)
			{
				htmltext = "30501-02.htm";
			}
			else
			{
				st.addRadar(-2150, 124443, -3649);
				if (st.getQuestItemsCount(KasmansLetter1rd) > 0)
				{
					htmltext = "30501-04.htm";
				}
				else
				{
					htmltext = "30501-03.htm";
					st.giveItems(KasmansLetter1rd, 1);
				}
			}
		}
		else if (event.equalsIgnoreCase("TUREK"))
		{
			if (st.getQuestItemsCount(ScepterOfTurek) > 0)
			{
				htmltext = "30501-05.htm";
			}
			else
			{
				st.addRadar(-94294, 110818, -3488);
				if (st.getQuestItemsCount(KasmansLetter2rd) > 0)
				{
					htmltext = "30501-07.htm";
				}
				else
				{
					htmltext = "30501-06.htm";
					st.giveItems(KasmansLetter2rd, 1);
				}
			}
		}
		else if (event.equalsIgnoreCase("TUNATH"))
		{
			if (st.getQuestItemsCount(ScepterOfTunath) > 0)
			{
				htmltext = "30501-08.htm";
			}
			else
			{
				st.addRadar(-55217, 200628, -3649);
				if (st.getQuestItemsCount(KasmansLetter3rd) > 0)
				{
					htmltext = "30501-10.htm";
				}
				else
				{
					htmltext = "30501-09.htm";
					st.giveItems(KasmansLetter3rd, 1);
				}
			}
		}
		else if (event.equalsIgnoreCase("30615-04.htm"))
		{
			st.playSound("Itemsound.quest_before_battle");
			st.addSpawn(27080, 80117, 120039, -2259);
			st.addSpawn(27081, 80058, 120038, -2259);
			st.giveItems(GloveOfVoltar, 1);
			st.takeItems(ManakiaLetter1st, 1);
		}
		else if (event.equalsIgnoreCase("30616-04.htm"))
		{
			st.playSound("Itemsound.quest_before_battle");
			st.addSpawn(27082, 19456, 192245, -3730);
			st.addSpawn(27082, 19539, 192343, -3728);
			st.addSpawn(27082, 19500, 192449, -3729);
			st.addSpawn(27082, 19569, 192482, -3728);
			st.giveItems(GloveOfKepra, 1);
			st.takeItems(ManakiaLetter2st, 1);
		}
		else if (event.equalsIgnoreCase("30617-04.htm"))
		{
			st.playSound("Itemsound.quest_before_battle");
			st.addSpawn(27083, -94292, 110781, -3701);
			st.addSpawn(27083, -94293, 110861, -3701);
			st.giveItems(GloveOfBurai, 1);
			st.takeItems(KasmansLetter2rd, 1);
			
		}
		else if (event.equalsIgnoreCase("30618-03.htm"))
		{
			st.takeItems(KasmansLetter3rd, -1);
			st.giveItems(ScepterOfTunath, 1);
			if ((st.getQuestItemsCount(ScepterOfBreka) != 0) && (st.getQuestItemsCount(ScepterOfEnku) != 0) && (st.getQuestItemsCount(ScepterOfVuku) != 0) && (st.getQuestItemsCount(ScepterOfTurek) != 0) && (st.getQuestItemsCount(ScepterOfTunath) != 0))
			{
				st.set("cond", "5");
			}
		}
		else if (event.equalsIgnoreCase("30619-03.htm"))
		{
			st.takeItems(KasmansLetter1rd, -1);
			st.giveItems(DrikosContract, 1);
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
		int npcId = npc.getId();
		
		switch (st.getState())
		{
			case State.CREATED:
				if (npcId == Vokian)
				{
					if ((player.getClassId().getId() == 45) || (player.getClassId().getId() == 47) || (player.getClassId().getId() == 50))
					{
						if (player.getLevel() >= 37)
						{
							htmltext = "30514-03.htm";
						}
						else
						{
							htmltext = "30514-01.htm";
							st.exitQuest(true);
						}
					}
					else
					{
						htmltext = "30514-02.htm";
						st.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				if (npcId == Vokian)
				{
					if (cond == 1)
					{
						htmltext = "30514-06.htm";
					}
					else if (cond == 2)
					{
						st.takeItems(VokiansOrder, -1);
						st.takeItems(ManashenShard, -10);
						st.takeItems(TyrantTalon, -10);
						st.takeItems(GuardianBasiliskFang, -10);
						st.giveItems(VokiansOrder2, 1);
						st.giveItems(NecklaceOfAuthority, 1);
						htmltext = "30514-08.htm";
						st.set("cond", "3");
					}
					else if (cond == 3)
					{
						htmltext = "30514-09.htm";
					}
					else if (cond == 4)
					{
						htmltext = "30514-10.htm";
					}
				}
				else if (npcId == Chianta)
				{
					if (cond == 3)
					{
						htmltext = "30642-01.htm";
					}
					else if (cond == 4)
					{
						htmltext = "30642-04.htm";
					}
					else if (cond == 5)
					{
						if (st.getQuestItemsCount(ChiantaOrder1st) > 0)
						{
							htmltext = "30642-05.htm";
						}
						else if (st.getQuestItemsCount(ChiantasOrder2rd) > 0)
						{
							if (player.getLevel() >= 38)
							{
								st.takeItems(ChiantasOrder2rd, -1);
								st.giveItems(ChiantasOrder3rd, 1);
								htmltext = "30642-09.htm";
								st.set("cond", "6");
							}
							else
							{
								htmltext = "30642-08.htm";
							}
						}
					}
					else if (cond == 6)
					{
						htmltext = "30642-10.htm";
					}
					else if (cond == 7)
					{
						st.takeItems(NecklaceOfAuthority, -1);
						st.takeItems(ChiantasOrder3rd, -1);
						st.takeItems(TamlinOrcSkull, -1);
						st.takeItems(TimakOrcHead, -1);
						st.giveItems(ScepterBox, 1);
						htmltext = "30642-11.htm";
						st.set("cond", "8");
					}
					else if (cond == 8)
					{
						htmltext = "30642-12.htm";
					}
				}
				else if (npcId == Manakia)
				{
					if (cond == 4)
					{
						htmltext = "30515-01.htm";
					}
				}
				else if (npcId == Kasman)
				{
					if (cond == 4)
					{
						htmltext = "30501-01.htm";
					}
				}
				else if (npcId == Voltar)
				{
					if (cond == 4)
					{
						if (st.getQuestItemsCount(ManakiaLetter1st) > 0)
						{
							htmltext = "30615-02.htm";
						}
						else if ((st.getQuestItemsCount(GloveOfVoltar) > 0) && ((st.getQuestItemsCount(PashikasHead) == 0) || (st.getQuestItemsCount(VultusHead) == 0)))
						{
							htmltext = "30615-05.htm";
							st.playSound("Itemsound.quest_before_battle");
							st.addSpawn(27080, 80117, 120039, -2259);
							st.addSpawn(27081, 80058, 120038, -2259);
						}
						else if ((st.getQuestItemsCount(PashikasHead) > 0) && (st.getQuestItemsCount(VultusHead) > 0))
						{
							st.takeItems(PashikasHead, -1);
							st.takeItems(VultusHead, -1);
							st.takeItems(GloveOfVoltar, -1);
							st.giveItems(ScepterOfBreka, 1);
							htmltext = "30615-06.htm";
							if ((st.getQuestItemsCount(ScepterOfBreka) > 0) && (st.getQuestItemsCount(ScepterOfEnku) > 0) && (st.getQuestItemsCount(ScepterOfVuku) > 0) && (st.getQuestItemsCount(ScepterOfTurek) > 0) && (st.getQuestItemsCount(ScepterOfTunath) > 0))
							{
								st.set("cond", "5");
								st.playSound("Itemsound.quest_middle");
							}
							else
							{
								st.playSound("Itemsound.quest_itemget");
							}
						}
						else if (st.getQuestItemsCount(ScepterOfBreka) > 0)
						{
							htmltext = "30615-07.htm";
						}
						else
						{
							htmltext = "30615-01.htm";
						}
					}
				}
				else if (npcId == Kepra)
				{
					if (cond == 4)
					{
						if (st.getQuestItemsCount(ManakiaLetter2st) > 0)
						{
							htmltext = "30616-02.htm";
						}
						else if ((st.getQuestItemsCount(GloveOfKepra) > 0) && (st.getQuestItemsCount(EnkuOverlordHead) < 4))
						{
							htmltext = "30616-05.htm";
							st.playSound("Itemsound.quest_before_battle");
							st.addSpawn(27082, 17710, 189813, -3581);
							st.addSpawn(27082, 17674, 189798, -3581);
							st.addSpawn(27082, 17770, 189852, -3581);
							st.addSpawn(27082, 17803, 189873, -3581);
						}
						else if (st.getQuestItemsCount(EnkuOverlordHead) >= 4)
						{
							htmltext = "30616-06.htm";
							st.takeItems(EnkuOverlordHead, -1);
							st.takeItems(GloveOfKepra, -1);
							st.giveItems(ScepterOfEnku, 1);
							if ((st.getQuestItemsCount(ScepterOfBreka) > 0) && (st.getQuestItemsCount(ScepterOfEnku) > 0) && (st.getQuestItemsCount(ScepterOfVuku) > 0) && (st.getQuestItemsCount(ScepterOfTurek) > 0) && (st.getQuestItemsCount(ScepterOfTunath) > 0))
							{
								st.set("cond", "5");
								st.playSound("Itemsound.quest_middle");
							}
							else
							{
								st.playSound("Itemsound.quest_itemget");
							}
						}
						else if (st.getQuestItemsCount(ScepterOfEnku) > 0)
						{
							htmltext = "30616-07.htm";
						}
						else
						{
							htmltext = "30616-01.htm";
						}
					}
				}
				else if (npcId == Burai)
				{
					if (cond == 4)
					{
						if (st.getQuestItemsCount(KasmansLetter2rd) > 0)
						{
							htmltext = "30617-02.htm";
						}
						else if ((st.getQuestItemsCount(GloveOfBurai) > 0) && (st.getQuestItemsCount(MakumBugbearHead) < 2))
						{
							htmltext = "30617-05.htm";
							st.playSound("Itemsound.quest_before_battle");
							st.addSpawn(27083, -94292, 110781, -3701);
							st.addSpawn(27083, -94293, 110861, -3701);
						}
						else if (st.getQuestItemsCount(MakumBugbearHead) == 2)
						{
							htmltext = "30617-06.htm";
							st.takeItems(MakumBugbearHead, -1);
							st.takeItems(GloveOfBurai, -1);
							st.giveItems(ScepterOfTurek, 1);
							if ((st.getQuestItemsCount(ScepterOfBreka) > 0) && (st.getQuestItemsCount(ScepterOfEnku) > 0) && (st.getQuestItemsCount(ScepterOfVuku) > 0) && (st.getQuestItemsCount(ScepterOfTurek) > 0) && (st.getQuestItemsCount(ScepterOfTunath) > 0))
							{
								st.set("cond", "5");
								st.playSound("Itemsound.quest_middle");
							}
							else
							{
								st.playSound("Itemsound.quest_itemget");
							}
						}
						else if (st.getQuestItemsCount(ScepterOfTurek) > 0)
						{
							htmltext = "30617-07.htm";
						}
						else
						{
							htmltext = "30617-01.htm";
						}
					}
				}
				else if (npcId == Harak)
				{
					if (cond == 4)
					{
						if (st.getQuestItemsCount(KasmansLetter3rd) > 0)
						{
							htmltext = "30618-02.htm";
						}
						else if (st.getQuestItemsCount(ScepterOfTunath) > 0)
						{
							htmltext = "30618-04.htm";
						}
						else
						{
							htmltext = "30618-01.htm";
						}
					}
				}
				else if (npcId == Driko)
				{
					if (cond == 4)
					{
						if (st.getQuestItemsCount(KasmansLetter1rd) > 0)
						{
							htmltext = "30619-02.htm";
						}
						else if (st.getQuestItemsCount(DrikosContract) > 0)
						{
							if (st.getQuestItemsCount(StakatoDroneHusk) >= 30)
							{
								htmltext = "30619-05.htm";
								st.takeItems(StakatoDroneHusk, -1);
								st.takeItems(DrikosContract, -1);
								st.giveItems(ScepterOfVuku, 1);
								if ((st.getQuestItemsCount(ScepterOfBreka) > 0) && (st.getQuestItemsCount(ScepterOfEnku) > 0) && (st.getQuestItemsCount(ScepterOfVuku) > 0) && (st.getQuestItemsCount(ScepterOfTurek) > 0) && (st.getQuestItemsCount(ScepterOfTunath) > 0))
								{
									st.set("cond", "5");
									st.playSound("Itemsound.quest_middle");
								}
								else
								{
									st.playSound("Itemsound.quest_itemget");
								}
							}
							else
							{
								htmltext = "30619-04.htm";
							}
						}
						else if (st.getQuestItemsCount(ScepterOfVuku) > 0)
						{
							htmltext = "30619-06.htm";
						}
						else
						{
							htmltext = "30619-01.htm";
						}
					}
				}
				else if (npcId == Tanapi)
				{
					if (cond == 8)
					{
						htmltext = "30571-01.htm";
					}
					else if (cond == 9)
					{
						htmltext = "30571-04.htm";
					}
					else if (cond == 10)
					{
						st.takeItems(ScepterOfTantos, -1);
						st.takeItems(TanapisOrder, -1);
						st.giveItems(RitualBox, 1);
						htmltext = "30571-05.htm";
						st.set("cond", "11");
					}
					else if (cond == 11)
					{
						htmltext = "30571-06.htm";
					}
				}
				if ((npcId == Kakai) && (cond == 11))
				{
					player.sendPacket(new SocialAction(player.getObjectId(), 3));
					st.takeItems(RitualBox, -1);
					st.giveItems(MarkOfGlory, 1);
					st.addExpAndSp(1448226, 96648);
					st.giveItems(57, 262720);
					st.giveItems(7562, 109);
					htmltext = "30565-02.htm";
					st.set("cond", "0");
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
				}
				break;
			case State.COMPLETED:
				if (npcId == Vokian)
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
		if ((cond == 1) && (st.getQuestItemsCount(TyrantTalon) >= 10) && (st.getQuestItemsCount(GuardianBasiliskFang) >= 10) && (st.getQuestItemsCount(ManashenShard) >= 10))
		{
			st.set("cond", "2");
		}
		else if (cond == 4)
		{
			if (npcId == PashikasSonOfVoltarQuestMonster)
			{
				if ((st.getQuestItemsCount(GloveOfVoltar) > 0) && (st.getQuestItemsCount(PashikasHead) == 0))
				{
					st.giveItems(PashikasHead, 1);
				}
			}
			else if (npcId == VultusSonOfVoltarQuestMonster)
			{
				if ((st.getQuestItemsCount(GloveOfVoltar) > 0) && (st.getQuestItemsCount(VultusHead) == 0))
				{
					st.giveItems(VultusHead, 1);
				}
			}
		}
		else if ((cond == 6) && (st.getQuestItemsCount(TimakOrcHead) >= 20) && (st.getQuestItemsCount(TamlinOrcSkull) >= 20))
		{
			st.set("cond", "7");
		}
		else if (cond == 9)
		{
			if ((npcId == RagnaOrcOverlord) || (npcId == RagnaOrcSeer))
			{
				st.addSpawn(RevenantOfTantosChief);
				st.playSound("Itemsound.quest_before_battle");
			}
			else if (npcId == RevenantOfTantosChief)
			{
				st.giveItems(ScepterOfTantos, 1);
				st.set("cond", "10");
				st.playSound("Itemsound.quest_middle");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _220_TestimonyOfGlory(220, qn, "");
	}
}