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
 * Created by LordWinter 09.08.2011
 * Based on L2J Eternity-World
 */
public class _219_TestimonyOfFate extends Quest
{
	private static final String qn = "_219_TestimonyOfFate";

    	// NPC's
    	private static final int Kaira = 30476;
    	private static final int Metheus = 30614;
    	private static final int Ixia = 30463;
    	private static final int AldersSpirit = 30613;
    	private static final int Roa = 30114;
    	private static final int Norman = 30210;
    	private static final int Thifiell = 30358;
    	private static final int Arkenia = 30419;
    	private static final int BloodyPixy = 31845;
    	private static final int BlightTreant = 31850;
	private static final int[] NPCS = { Kaira, Metheus, Ixia, AldersSpirit, Roa, Norman, Thifiell, Arkenia, BloodyPixy, BlightTreant };

    	// QuestItem
    	private static final int KairasLetter = 3173;
    	private static final int MetheussFuneralJar = 3174;
    	private static final int KasandrasRemains = 3175;
    	private static final int HerbalismTextbook = 3176;
    	private static final int IxiasList = 3177;
    	private static final int MedusasIchor = 3178;
    	private static final int MarshSpiderFluids = 3179;
    	private static final int DeadSeekerDung = 3180;
    	private static final int TyrantsBlood = 3181;
    	private static final int NightshadeRoot = 3182;
    	private static final int Belladonna = 3183;
    	private static final int AldersSkull1 = 3184;
    	private static final int AldersSkull2 = 3185;
    	private static final int AldersReceipt = 3186;
    	private static final int RevelationsManuscript = 3187;
    	private static final int KairasRecommendation = 3189;
    	private static final int KairasInstructions = 3188;
    	private static final int PalusCharm = 3190;
    	private static final int ThifiellsLetter = 3191;
    	private static final int ArkeniasNote = 3192;
    	private static final int PixyGarnet = 3193;
    	private static final int BlightTreantSeed = 3199;
    	private static final int GrandissSkull = 3194;
    	private static final int KarulBugbearSkull = 3195;
    	private static final int BrekaOverlordSkull = 3196;
    	private static final int LetoOverlordSkull = 3197;
    	private static final int BlackWillowLeaf = 3200;
    	private static final int RedFairyDust = 3198;
    	private static final int BlightTreantSap = 3201;
    	private static final int ArkeniasLetter = 1246;

    	// Items
    	private static final int MarkofFate = 3172;

    	// MOBs
    	private static final int HangmanTree = 20144;
    	private static final int Medusa = 20158;
    	private static final int MarshSpider = 20233;
    	private static final int DeadSeeker = 20202;
    	private static final int Tyrant = 20192;
    	private static final int TyrantKingpin = 20193;
    	private static final int MarshStakatoWorker = 20230;
    	private static final int MarshStakato = 20157;
    	private static final int MarshStakatoSoldier = 20232;
    	private static final int MarshStakatoDrone = 20234;
    	private static final int Grandis = 20554;
    	private static final int KarulBugbear = 20600;
    	private static final int BrekaOrcOverlord = 20270;
    	private static final int LetoLizardmanOverlord = 20582;
    	private static final int BlackWillowLurker = 27079;

    	//Drop Cond
    	private static final int[][] DROPLIST_COND =
	{
		{ 6, 0, Medusa, IxiasList, MedusasIchor, 1, 10, 200000 },
		{ 6, 0, MarshSpider, IxiasList, MarshSpiderFluids, 1, 10, 200000 },
		{ 6, 0, DeadSeeker, IxiasList, DeadSeekerDung, 1, 10, 200000 },
		{ 6, 0, Tyrant, IxiasList, TyrantsBlood, 1, 10, 200000 },
		{ 6, 0, TyrantKingpin, IxiasList, TyrantsBlood, 1, 10, 600000 },
		{ 6, 0, MarshStakatoWorker, IxiasList, NightshadeRoot, 1, 10, 300000 },
		{ 6, 0, MarshStakato, IxiasList, NightshadeRoot, 1, 10, 400000 },
		{ 6, 0, MarshStakatoSoldier, IxiasList, NightshadeRoot, 1, 10, 500000 },
		{ 6, 0, MarshStakatoDrone, IxiasList, NightshadeRoot, 1, 10, 600000 },
		{ 17, 0, Grandis, PixyGarnet, GrandissSkull, 1, 10, 1000000 },
		{ 17, 0, KarulBugbear, PixyGarnet, KarulBugbearSkull, 1, 10, 1000000 },
		{ 17, 0, BrekaOrcOverlord, PixyGarnet, BrekaOverlordSkull, 1, 10, 1000000 },
		{ 17, 0, LetoLizardmanOverlord, PixyGarnet, LetoOverlordSkull, 1, 10, 1000000 },
		{ 17, 0, BlackWillowLurker, BlightTreantSeed, BlackWillowLeaf, 1, 10, 1000000 }
	};

    	public _219_TestimonyOfFate(int questId, String name, String descr)
	{
		super(questId, name, descr);

        	addStartNpc(Kaira);
        	for (int i : NPCS)
        		addTalkId(i);

        	for (int i = 0; i < DROPLIST_COND.length; i++)
            		addKillId(DROPLIST_COND[i][2]);
        	addKillId(HangmanTree);

        	questItemIds = (new int[] { KairasLetter, MetheussFuneralJar, KasandrasRemains, IxiasList, Belladonna, AldersSkull1, AldersSkull2, AldersReceipt, RevelationsManuscript, KairasRecommendation, KairasInstructions, ThifiellsLetter, PalusCharm, ArkeniasNote, PixyGarnet, BlightTreantSeed, RedFairyDust, BlightTreantSap, ArkeniasLetter, MedusasIchor, MarshSpiderFluids, DeadSeekerDung, TyrantsBlood, NightshadeRoot, GrandissSkull, KarulBugbearSkull, BrekaOverlordSkull, LetoOverlordSkull, BlackWillowLeaf });
    	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

        	if (event.equalsIgnoreCase("1"))
        	{
            		htmltext = "30476-05.htm";
            		st.set("cond", "1");
            		st.setState(State.STARTED);
            		st.playSound("ItemSound.quest_accept");
            		st.giveItems(KairasLetter, 1);
        	}
        	else if (event.equalsIgnoreCase("30476_1"))
            		htmltext = "30476-04.htm";
        	else if (event.equalsIgnoreCase("30476_2"))
        	{
            		if (player.getLevel() >= 38)
            		{
                		st.set("cond", "15");
                		htmltext = "30476-12.htm";
                		st.giveItems(KairasRecommendation, 1);
                		st.takeItems(RevelationsManuscript, 1);
            		}
            		else
            		{
                		st.set("cond", "14");
                		htmltext = "30476-13.htm";
                		st.giveItems(KairasInstructions, 1);
                		st.takeItems(RevelationsManuscript, 1);
            		}
        	}
        	else if (event.equalsIgnoreCase("30114_1"))
            		htmltext = "30114-02.htm";
        	else if (event.equalsIgnoreCase("30114_2"))
            		htmltext = "30114-03.htm";
        	else if (event.equalsIgnoreCase("30114_3"))
        	{
            		htmltext = "30114-04.htm";
            		st.takeItems(AldersSkull2, 1);
            		st.giveItems(AldersReceipt, 1);
            		st.set("cond", "12");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30419_1"))
        	{
            		htmltext = "30419-02.htm";
            		st.takeItems(ThifiellsLetter, 1);
            		st.giveItems(ArkeniasNote, 1);
            		st.set("cond", "17");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("31845_1"))
        	{
            		htmltext = "31845-02.htm";
            		st.giveItems(PixyGarnet, 1);
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("31850_1"))
        	{
            		htmltext = "31850-02.htm";
            		st.giveItems(BlightTreantSeed, 1);
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30419_2"))
        	{
            		htmltext = "30419-05.htm";
            		st.takeItems(ArkeniasNote, 1);
            		st.takeItems(RedFairyDust, 1);
            		st.takeItems(BlightTreantSap, 1);
            		st.giveItems(ArkeniasLetter, 1);
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
			return htmltext;

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

        	int cond = st.getInt("cond");
        	int npcId = npc.getId();

        	if (npcId == Kaira)
        	{
            		if (st.getQuestItemsCount(MarkofFate) != 0)
                		return htmltext;
            		else if (cond == 0)
            		{
                		if (player.getRace().ordinal() == 2 && player.getLevel() >= 37)
                    			htmltext = "30476-03.htm";
                		else if (player.getRace().ordinal() == 2)
                		{
                    			htmltext = "30476-02.htm";
                    			st.exitQuest(true);
                		}
                		else
                		{
                    			htmltext = "30476-01.htm";
                    			st.exitQuest(true);
                		}
            		}
            		else if (cond == 2 && st.getQuestItemsCount(KairasLetter) == 1)
                		htmltext = "30476-06.htm";
            		else if (cond == 9 || cond == 10)
            		{
               		 	htmltext = "30476-09.htm";
                		st.takeItems(AldersSkull1, 1);
                		if (st.getQuestItemsCount(AldersSkull2) == 0)
                    			st.giveItems(AldersSkull2, 1);
                		st.set("cond", "10");
                		st.playSound("Itemsound.quest_middle");
                		st.addSpawn(AldersSpirit, 78977, 149036, -3597, 30000);
            		}
            		else if (cond == 13)
                		htmltext = "30476-11.htm";
            		else if (cond == 14)
            		{
                		if (st.getQuestItemsCount(KairasInstructions) != 0 && player.getLevel() < 38)
                    			htmltext = "30476-14.htm";
               			else if (st.getQuestItemsCount(KairasInstructions) != 0 && player.getLevel() >= 38)
                		{
                    			st.giveItems(KairasRecommendation, 1);
                    			st.takeItems(KairasInstructions, 1);
                    			htmltext = "30476-15.htm";
                    			st.set("cond", "15");
                    			st.playSound("Itemsound.quest_middle");
                		}
            		}
            		else if (cond == 15)
                		htmltext = "30476-16.htm";
            		else if (cond == 16 || cond == 17)
                		htmltext = "30476-17.htm";
            		else if (st.getQuestItemsCount(MetheussFuneralJar) > 0 || st.getQuestItemsCount(KasandrasRemains) > 0)
                		htmltext = "30476-07.htm";
            		else if (st.getQuestItemsCount(HerbalismTextbook) > 0 || st.getQuestItemsCount(IxiasList) > 0)
                		htmltext = "30476-08.htm";
            		else if (st.getQuestItemsCount(AldersSkull2) > 0 || st.getQuestItemsCount(AldersReceipt) > 0)
                		htmltext = "30476-10.htm";
        	}
        	else if (npcId == Metheus)
        	{
            		if (cond == 1)
            		{
                		htmltext = "30614-01.htm";
                		st.takeItems(KairasLetter, 1);
                		st.giveItems(MetheussFuneralJar, 1);
                		st.set("cond", "2");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 2)
                		htmltext = "30614-02.htm";
            		else if (cond == 3)
            		{
                		st.takeItems(KasandrasRemains, 1);
                		st.giveItems(HerbalismTextbook, 1);
                		htmltext = "30614-03.htm";
                		st.set("cond", "5");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 8)
            		{
                		st.takeItems(Belladonna, 1);
                		st.giveItems(AldersSkull1, 1);
                		htmltext = "30614-05.htm";
                		st.set("cond", "9");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (st.getQuestItemsCount(HerbalismTextbook) > 0 || st.getQuestItemsCount(IxiasList) > 0)
                		htmltext = "30614-04.htm";
            		else if (st.getQuestItemsCount(AldersSkull1) > 0 || st.getQuestItemsCount(AldersSkull2) > 0 || st.getQuestItemsCount(AldersReceipt) > 0 || st.getQuestItemsCount(RevelationsManuscript) > 0 || st.getQuestItemsCount(KairasInstructions) > 0 || st.getQuestItemsCount(KairasRecommendation) > 0)
                		htmltext = "30614-06.htm";
        	}
        	else if (npcId == Ixia)
        	{
            		if (cond == 5)
            		{
                		st.takeItems(HerbalismTextbook, 1);
                		st.giveItems(IxiasList, 1);
                		htmltext = "30463-01.htm";
                		st.set("cond", "6");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 6)
                		htmltext = "30463-02.htm";
            		else if (cond == 7 && st.getQuestItemsCount(MedusasIchor) >= 10 && st.getQuestItemsCount(MarshSpiderFluids) >= 10 && st.getQuestItemsCount(DeadSeekerDung) >= 10 && st.getQuestItemsCount(TyrantsBlood) >= 10 && st.getQuestItemsCount(NightshadeRoot) >= 10)
            		{
                		st.takeItems(MedusasIchor, st.getQuestItemsCount(MedusasIchor));
                		st.takeItems(MarshSpiderFluids, st.getQuestItemsCount(MarshSpiderFluids));
                		st.takeItems(DeadSeekerDung, st.getQuestItemsCount(DeadSeekerDung));
                		st.takeItems(TyrantsBlood, st.getQuestItemsCount(TyrantsBlood));
                		st.takeItems(NightshadeRoot, st.getQuestItemsCount(NightshadeRoot));
                		st.takeItems(IxiasList, 1);
                		st.giveItems(Belladonna, 1);
                		htmltext = "30463-03.htm";
                		st.set("cond", "8");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 7)
            		{
                		htmltext = "30463-02.htm";
                		st.set("cond", "6");
            		}
            		else if (cond == 8)
                		htmltext = "30463-04.htm";
            		else if (st.getQuestItemsCount(AldersSkull1) > 0 || st.getQuestItemsCount(AldersSkull2) > 0 || st.getQuestItemsCount(AldersReceipt) > 0 || st.getQuestItemsCount(RevelationsManuscript) > 0 || st.getQuestItemsCount(KairasInstructions) > 0 || st.getQuestItemsCount(KairasRecommendation) > 0)
                		htmltext = "30614-06.htm";
        	}
        	else if (npcId == AldersSpirit)
        	{
            		htmltext = "30613-02.htm";
            		st.set("cond", "11");
        	}
        	else if (npcId == Roa)
        	{
            		if (cond == 11)
                		htmltext = "30114-01.htm";
            		else if (cond == 12)
                		htmltext = "30114-05.htm";
            		else if (st.getQuestItemsCount(RevelationsManuscript) > 0 || st.getQuestItemsCount(KairasInstructions) > 0 || st.getQuestItemsCount(KairasRecommendation) > 0)
                		htmltext = "30114-06.htm";
        	}
        	else if (npcId == Norman)
        	{
           		if (cond == 12)
            		{
                		st.takeItems(AldersReceipt, 1);
                		st.giveItems(RevelationsManuscript, 1);
                		htmltext = "30210-01.htm";
                		st.set("cond", "13");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 13)
                		htmltext = "30210-02.htm";
        	}
        	else if (npcId == Thifiell)
        	{
            		if (cond == 15)
            		{
                		st.takeItems(KairasRecommendation, 1);
                		st.giveItems(ThifiellsLetter, 1);
                		st.giveItems(PalusCharm, 1);
                		htmltext = "30358-01.htm";
                		st.set("cond", "16");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 16)
                		htmltext = "30358-02.htm";
            		else if (cond == 17)
                		htmltext = "30358-03.htm";
            		else if (cond == 18)
            		{
                		st.takeItems(ArkeniasLetter, 1);
                		st.takeItems(PalusCharm, 1);
                		st.giveItems(MarkofFate, 1);
                		st.addExpAndSp(1365470,91124);
				st.giveItems(57,247708);
                		st.giveItems(7562, 16);
                		htmltext = "30358-04.htm";
                		st.playSound("ItemSound.quest_finish");
                		st.unset("cond");
                		st.setState(State.COMPLETED);
                		st.exitQuest(false);
            		}
        	}
        	else if (npcId == Arkenia)
        	{
            		if (cond == 16)
                		htmltext = "30419-01.htm";
            		else if (cond == 17)
            		{
                		if (st.getQuestItemsCount(RedFairyDust) < 1 || st.getQuestItemsCount(BlightTreantSap) < 1)
                    			htmltext = "30419-03.htm";
                		else if (st.getQuestItemsCount(RedFairyDust) >= 1 && st.getQuestItemsCount(BlightTreantSap) >= 1)
                    			htmltext = "30419-04.htm";
            		}
            		else if (cond == 18)
                		htmltext = "30419-06.htm";
        	}
        	else if (npcId == BloodyPixy && cond == 17)
        	{
            		if (st.getQuestItemsCount(RedFairyDust) == 0 && st.getQuestItemsCount(PixyGarnet) == 0)
                		htmltext = "31845-01.htm";
            		else if (st.getQuestItemsCount(RedFairyDust) == 0 && st.getQuestItemsCount(PixyGarnet) > 0 && (st.getQuestItemsCount(GrandissSkull) < 10 || st.getQuestItemsCount(KarulBugbearSkull) < 10 || st.getQuestItemsCount(BrekaOverlordSkull) < 10 || st.getQuestItemsCount(LetoOverlordSkull) < 10))
                		htmltext = "31845-03.htm";
            		else if (st.getQuestItemsCount(RedFairyDust) == 0 && st.getQuestItemsCount(PixyGarnet) > 0 && st.getQuestItemsCount(GrandissSkull) >= 10 && st.getQuestItemsCount(KarulBugbearSkull) >= 10 && st.getQuestItemsCount(BrekaOverlordSkull) >= 10 && st.getQuestItemsCount(LetoOverlordSkull) >= 10)
            		{
                		st.takeItems(GrandissSkull, st.getQuestItemsCount(GrandissSkull));
                		st.takeItems(KarulBugbearSkull, st.getQuestItemsCount(KarulBugbearSkull));
                		st.takeItems(BrekaOverlordSkull, st.getQuestItemsCount(BrekaOverlordSkull));
                		st.takeItems(LetoOverlordSkull, st.getQuestItemsCount(LetoOverlordSkull));
                		st.takeItems(PixyGarnet, 1);
                	st.giveItems(RedFairyDust, 1);
                			htmltext = "31845-04.htm";
            		}
            		else if (st.getQuestItemsCount(RedFairyDust) != 0)
                		htmltext = "31845-05.htm";
        	}
        	else if (npcId == BlightTreant && cond == 17)
        	{
            		if (st.getQuestItemsCount(BlightTreantSap) == 0 && st.getQuestItemsCount(BlightTreantSeed) == 0)
                		htmltext = "31850-01.htm";
            		else if (st.getQuestItemsCount(BlightTreantSap) == 0 && st.getQuestItemsCount(BlightTreantSeed) > 0 && st.getQuestItemsCount(BlackWillowLeaf) == 0)
                		htmltext = "31850-03.htm";
            		else if (st.getQuestItemsCount(BlightTreantSap) == 0 && st.getQuestItemsCount(BlightTreantSeed) > 0 && st.getQuestItemsCount(BlackWillowLeaf) > 0)
            		{
                		st.takeItems(BlackWillowLeaf, st.getQuestItemsCount(BlackWillowLeaf));
                		st.takeItems(BlightTreantSeed, 1);
                		st.giveItems(BlightTreantSap, 1);
                		htmltext = "31850-04.htm";
            		}
            		else if (st.getQuestItemsCount(BlightTreantSap) > 0)
                		htmltext = "31850-05.htm";
        	}
        	return htmltext;
    	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
        	if (st == null)
			return null;

        	int npcId = npc.getId();
        	int cond = st.getInt("cond");

        	for (int i = 0; i < DROPLIST_COND.length; i++)
        	{
            		if (cond == DROPLIST_COND[i][0] && npcId == DROPLIST_COND[i][2])
            		{
                		if (DROPLIST_COND[i][3] == 0 || st.getQuestItemsCount(DROPLIST_COND[i][3]) > 0)
                		{
                    			if (DROPLIST_COND[i][4] == 0)
                        			st.dropQuestItems(DROPLIST_COND[i][4], DROPLIST_COND[i][5], DROPLIST_COND[i][5], DROPLIST_COND[i][6], DROPLIST_COND[i][7], true);
                    			else if (st.dropQuestItems(DROPLIST_COND[i][4], DROPLIST_COND[i][5], DROPLIST_COND[i][5], DROPLIST_COND[i][6], DROPLIST_COND[i][7], true))
                    			{
                        			if (DROPLIST_COND[i][1] != cond && DROPLIST_COND[i][1] != 0)
                            				st.set("cond", String.valueOf(DROPLIST_COND[i][1]));
                    			}
                		}
            		}
        	}

        	if (cond == 2 && npcId == HangmanTree)
        	{
            		st.takeItems(MetheussFuneralJar, 1);
            		st.giveItems(KasandrasRemains, 1);
            		st.playSound("Itemsound.quest_middle");
            		st.set("cond", "3");
        	}
        	else if (cond == 6 && st.getQuestItemsCount(MedusasIchor) >= 10 && st.getQuestItemsCount(MarshSpiderFluids) >= 10 && st.getQuestItemsCount(DeadSeekerDung) >= 10 && st.getQuestItemsCount(TyrantsBlood) >= 10 && st.getQuestItemsCount(NightshadeRoot) >= 10)
            		st.set("cond", "7");

        	return null;
    	}

	public static void main(String[] args)
	{
		new _219_TestimonyOfFate(219, qn, "");
	}
}
