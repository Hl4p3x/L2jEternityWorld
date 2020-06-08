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
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public class _214_TrialOfScholar extends Quest
{
    	private static final String qn = "_214_TrialOfScholar";

    	// Npc
    	private static final int MIRIEN = 30461;
    	private static final int SYLVAIN = 30070;
    	private static final int LUCAS = 30071;
    	private static final int VALKON = 30103;
    	private static final int DIETER = 30111;
    	private static final int JUREK = 30115;
    	private static final int EDROC = 30230;
    	private static final int RAUT = 30316;
    	private static final int POITAN = 30458;
    	private static final int MARIA = 30608;
    	private static final int CRETA = 30609;
    	private static final int CRONOS = 30610;
    	private static final int TRIFF = 30611;
    	private static final int CASIAN = 30612;

    	private static final int[] TALKERS =
    	{
        	MIRIEN, SYLVAIN, LUCAS, VALKON, DIETER, JUREK, EDROC, RAUT, POITAN, MARIA, CRETA, CRONOS, TRIFF, CASIAN
    	};

    	// Mobs
    	private static final int MEDUSA = 20158;
    	private static final int GHOUL = 20201;
    	private static final int SHACKLE = 20235;
    	private static final int BREKA_ORC_SHAMAN = 20269;
    	private static final int FETTERED_SOUL = 20552;
    	private static final int GRANDIS = 20554;
    	private static final int ENCHANTED_GARGOYLE = 20567;
    	private static final int LETO_LIZARDMAN_WARRIOR = 20580;
    	private static final int MONSTER_EYE_DESTROYER = 20068;

    	private static final int[] MOBS =
    	{
        	MEDUSA, GHOUL, SHACKLE, BREKA_ORC_SHAMAN, FETTERED_SOUL, GRANDIS, ENCHANTED_GARGOYLE, LETO_LIZARDMAN_WARRIOR, MONSTER_EYE_DESTROYER
    	};

    	// Quest items
    	private static final int MIRIENS_SIGIL1 = 2675;
    	private static final int MIRIENS_SIGIL2 = 2676;
    	private static final int MIRIENS_SIGIL3 = 2677;
    	private static final int MIRIENS_INSTRUCTION = 2678;
    	private static final int MARIAS_LETTER1 = 2679;
    	private static final int MARIAS_LETTER2 = 2680;
    	private static final int LUKAS_LETTER = 2681;
    	private static final int LUCILLAS_HANDBAG = 2682;
    	private static final int CRETAS_LETTER1 = 2683;
    	private static final int CRETAS_PAINTING1 = 2684;
    	private static final int CRETAS_PAINTING2 = 2685;
    	private static final int CRETAS_PAINTING3 = 2686;
    	private static final int BROWN_SCROLL_SCRAP = 2687;
    	private static final int CRYSTAL_OF_PURITY1 = 2688;
    	private static final int HIGHPRIESTS_SIGIL = 2689;
    	private static final int GMAGISTERS_SIGIL = 2690;
    	private static final int CRONOS_SIGIL = 2691;
    	private static final int SYLVAINS_LETTER = 2692;
    	private static final int SYMBOL_OF_SYLVAIN = 2693;
    	private static final int JUREKS_LIST = 2694;
    	private static final int MEYEDESTROYERS_SKIN = 2695;
    	private static final int SHAMANS_NECKLACE = 2696;
    	private static final int SHACKLES_SCALP = 2697;
    	private static final int SYMBOL_OF_JUREK = 2698;
    	private static final int CRONOS_LETTER = 2699;
    	private static final int DIETERS_KEY = 2700;
    	private static final int CRETAS_LETTER2 = 2701;
    	private static final int DIETERS_LETTER = 2702;
    	private static final int DIETERS_DIARY = 2703;
    	private static final int RAUTS_LETTER_ENVELOPE = 2704;
    	private static final int TRIFFS_RING = 2705;
    	private static final int SCRIPTURE_CHAPTER_1 = 2706;
    	private static final int SCRIPTURE_CHAPTER_2 = 2707;
    	private static final int SCRIPTURE_CHAPTER_3 = 2708;
    	private static final int SCRIPTURE_CHAPTER_4 = 2709;
    	private static final int VALKONS_REQUEST = 2710;
    	private static final int POITANS_NOTES = 2711;
    	private static final int STRONG_LIQUOR = 2713;
    	private static final int CRYSTAL_OF_PURITY2 = 2714;
    	private static final int CASIANS_LIST = 2715;
    	private static final int GHOULS_SKIN = 2716;
    	private static final int MEDUSAS_BLOOD = 2717;
    	private static final int FETTEREDSOULS_ICHOR = 2718;
    	private static final int ENCHT_GARGOYLES_NAIL = 2719;
    	private static final int SYMBOL_OF_CRONOS = 2720;

    	private static final int[] QUESTITEMS =
    	{
        	CRYSTAL_OF_PURITY2, CASIANS_LIST, GHOULS_SKIN, MEDUSAS_BLOOD, FETTEREDSOULS_ICHOR, ENCHT_GARGOYLES_NAIL, SYMBOL_OF_CRONOS
    	};

    	// Reward
    	private static final int MARK_OF_SCHOLAR = 2674;

    	// Allowed classes
    	private static final int[] CLASSES =
    	{
        	0x0b, 0x1a, 0x27
    	};

    	public _214_TrialOfScholar(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	addStartNpc(MIRIEN);

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
			return htmltext;

        	if (event.equalsIgnoreCase("1"))
        	{
            		htmltext = "30461-04.htm";
            		st.set("cond", "1");
            		st.setState(State.STARTED);
            		st.playSound("ItemSound.quest_accept");
            		st.giveItems(MIRIENS_SIGIL1, 1);
        	}
        	else if (event.equalsIgnoreCase("30461_1"))
        	{
            		if (player.getLevel() < 35)
            		{
                		htmltext = "30461-09.htm";
                		st.takeItems(SYMBOL_OF_JUREK, -1);
                		st.takeItems(MIRIENS_SIGIL2, -1);
                		st.giveItems(MIRIENS_INSTRUCTION, 1);
            		}
            		else
            		{
                		htmltext = "30461-10.htm";
                		st.takeItems(SYMBOL_OF_JUREK, -1);
                		st.takeItems(MIRIENS_SIGIL2, -1);
                		st.giveItems(MIRIENS_SIGIL3, 1);
                		st.playSound("ItemSound.quest_middle");
                		st.set("cond", "19");
            		}
        	}
        	else if (event.equalsIgnoreCase("30070_1"))
        	{
            		htmltext = "30070-02.htm";
            		st.giveItems(HIGHPRIESTS_SIGIL, 1);
            		st.giveItems(SYLVAINS_LETTER, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "2");
        	}
        	else if (event.equalsIgnoreCase("30608_1"))
        	{
            		htmltext = "30608-02.htm";
            		st.takeItems(SYLVAINS_LETTER, -1);
            		st.giveItems(MARIAS_LETTER1, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "3");
        	}
        	else if (event.equalsIgnoreCase("30608_2"))
        	{
            		htmltext = "30608-07.htm";
        	}
        	else if (event.equalsIgnoreCase("30608_3"))
        	{
            		htmltext = "30608-08.htm";
            		st.takeItems(CRETAS_LETTER1, -1);
            		st.giveItems(LUCILLAS_HANDBAG, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "7");
        	}
        	else if (event.equalsIgnoreCase("30608_4"))
        	{
            		htmltext = "30608-14.htm";
            		st.takeItems(BROWN_SCROLL_SCRAP, -1);
            		st.takeItems(CRETAS_PAINTING3, -1);
            		st.giveItems(CRYSTAL_OF_PURITY1, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "13");
        	}
        	else if (event.equalsIgnoreCase("30115_1"))
        	{
            		htmltext = "30115-02.htm";
        	}
        	else if (event.equalsIgnoreCase("30115_2"))
        	{
            		htmltext = "30115-03.htm";
            		st.giveItems(JUREKS_LIST, 1);
            		st.giveItems(GMAGISTERS_SIGIL, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "16");
        	}
        	else if (event.equalsIgnoreCase("30071_1"))
        	{
            		htmltext = "30071-04.htm";
            		st.takeItems(CRETAS_PAINTING2, -1);
            		st.giveItems(CRETAS_PAINTING3, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "10");
        	}
        	else if (event.equalsIgnoreCase("30609_1"))
        	{
            		htmltext = "30609-02.htm";
        	}
        	else if (event.equalsIgnoreCase("30609_2"))
        	{
            		htmltext = "30609-03.htm";
        	}
        	else if (event.equalsIgnoreCase("30609_3"))
        	{
            		htmltext = "30609-04.htm";
        	}
        	else if (event.equalsIgnoreCase("30609_4"))
        	{
            		htmltext = "30609-05.htm";
            		st.takeItems(MARIAS_LETTER2, -1);
            		st.giveItems(CRETAS_LETTER1, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "6");
        	}
        	else if (event.equalsIgnoreCase("30609_5"))
        	{
            		htmltext = "30609-08.htm";
        	}
        	else if (event.equalsIgnoreCase("30609_6"))
        	{
            		htmltext = "30609-09.htm";
            		st.takeItems(LUCILLAS_HANDBAG, -1);
            		st.giveItems(CRETAS_PAINTING1, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "8");
        	}
        	else if (event.equalsIgnoreCase("30609_7"))
        	{
            		htmltext = "30609-13.htm";
        	}
        	else if (event.equalsIgnoreCase("30609_8"))
        	{
            		htmltext = "30609-14.htm";
            		st.takeItems(DIETERS_KEY, -1);
            		st.giveItems(CRETAS_LETTER2, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "22");
        	}
        	else if (event.equalsIgnoreCase("30610_1"))
        	{
            		htmltext = "30610-02.htm";
        	}
        	else if (event.equalsIgnoreCase("30610_2"))
        	{
            		htmltext = "30610-03.htm";
        	}
        	else if (event.equalsIgnoreCase("30610_3"))
        	{
            		htmltext = "30610-04.htm";
        	}
        	else if (event.equalsIgnoreCase("30610_4"))
        	{
            		htmltext = "30610-05.htm";
        	}
        	else if (event.equalsIgnoreCase("30610_5"))
        	{
            		htmltext = "30610-06.htm";
        	}
        	else if (event.equalsIgnoreCase("30610_6"))
        	{
            		htmltext = "30610-07.htm";
        	}
        	else if (event.equalsIgnoreCase("30610_7"))
        	{
            		htmltext = "30610-08.htm";
        	}
        	else if (event.equalsIgnoreCase("30610_8"))
        	{
            		htmltext = "30610-09.htm";
        	}
        	else if (event.equalsIgnoreCase("30610_9"))
        	{
            		htmltext = "30610-10.htm";
            		st.giveItems(CRONOS_SIGIL, 1);
            		st.giveItems(CRONOS_LETTER, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "20");
        	}
        	else if (event.equalsIgnoreCase("30610_10"))
        	{
            		htmltext = "30610-13.htm";
        	}
        	else if (event.equalsIgnoreCase("30610_11"))
        	{
            		htmltext = "30610-14.htm";
            		st.takeItems(SCRIPTURE_CHAPTER_1, -1);
            		st.takeItems(SCRIPTURE_CHAPTER_2, -1);
            		st.takeItems(SCRIPTURE_CHAPTER_3, -1);
            		st.takeItems(SCRIPTURE_CHAPTER_4, -1);
            		st.takeItems(CRONOS_SIGIL, -1);
            		st.takeItems(TRIFFS_RING, -1);
            		st.takeItems(DIETERS_DIARY, -1);
            		st.giveItems(SYMBOL_OF_CRONOS, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "31");
        	}
        	else if (event.equalsIgnoreCase("30111_1"))
        	{
            		htmltext = "30111-02.htm";
        	}
        	else if (event.equalsIgnoreCase("30111_2"))
        	{
            		htmltext = "30111-03.htm";
        	}
        	else if (event.equalsIgnoreCase("30111_3"))
        	{
            		htmltext = "30111-04.htm";
        	}
        	else if (event.equalsIgnoreCase("30111_4"))
        	{
            		htmltext = "30111-05.htm";
            		st.takeItems(CRONOS_LETTER, -1);
            		st.giveItems(DIETERS_KEY, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "21");
        	}
        	else if (event.equalsIgnoreCase("30111_5"))
        	{
            		htmltext = "30111-08.htm";
        	}
        	else if (event.equalsIgnoreCase("30111_6"))
        	{
            		htmltext = "30111-09.htm";
            		st.takeItems(CRETAS_LETTER2, -1);
            		st.giveItems(DIETERS_LETTER, 1);
            		st.giveItems(DIETERS_DIARY, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "23");
        	}
        	else if (event.equalsIgnoreCase("30230_1"))
        	{
            		htmltext = "30230-02.htm";
            		st.takeItems(DIETERS_LETTER, -1);
            		st.giveItems(RAUTS_LETTER_ENVELOPE, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "24");
        	}
        	else if (event.equalsIgnoreCase("30316_1"))
        	{
            		htmltext = "30316-02.htm";
            		st.takeItems(RAUTS_LETTER_ENVELOPE, -1);
            		st.giveItems(SCRIPTURE_CHAPTER_1, 1);
            		st.giveItems(STRONG_LIQUOR, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "25");
        	}
        	else if (event.equalsIgnoreCase("30611_1"))
        	{
            		htmltext = "30611-02.htm";
        	}
        	else if (event.equalsIgnoreCase("30611_2"))
        	{
            		htmltext = "30611-03.htm";
        	}
        	else if (event.equalsIgnoreCase("30611_3"))
        	{
            		htmltext = "30611-04.htm";
            		st.takeItems(STRONG_LIQUOR, -1);
            		st.giveItems(TRIFFS_RING, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "26");
        	}
        	else if (event.equalsIgnoreCase("30103_1"))
        	{
            		htmltext = "30103-02.htm";
        	}
        	else if (event.equalsIgnoreCase("30103_2"))
        	{
            		htmltext = "30103-03.htm";
        	}
        	else if (event.equalsIgnoreCase("30103_3"))
        	{
            		htmltext = "30103-04.htm";
            		st.giveItems(VALKONS_REQUEST, 1);
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30612_1"))
        	{
            		htmltext = "30612-03.htm";
        	}
        	else if (event.equalsIgnoreCase("30612_2"))
        	{
            		htmltext = "30612-04.htm";
            		st.giveItems(CASIANS_LIST, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "28");
        	}
        	else if (event.equalsIgnoreCase("30612_3"))
        	{
            		htmltext = "30612-07.htm";
            		st.giveItems(SCRIPTURE_CHAPTER_4, 1);
            		st.takeItems(CASIANS_LIST, -1);
            		st.takeItems(GHOULS_SKIN, -1);
            		st.takeItems(MEDUSAS_BLOOD, -1);
            		st.takeItems(FETTEREDSOULS_ICHOR, -1);
            		st.takeItems(ENCHT_GARGOYLES_NAIL, -1);
            		st.takeItems(POITANS_NOTES, -1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "30");
        	}
        	return htmltext;
    	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = getNoQuestMsg(talker);
		QuestState st = talker.getQuestState(qn);
		if (st == null)
			return htmltext;

        	int cond = st.getInt("cond");
        	int npcId = npc.getId();
        	int id = st.getState();

        	if (npcId != MIRIEN && id != State.STARTED)
        	{
        	    	return htmltext;
        	}

        	if (npcId == MIRIEN && cond == 0)
        	{
            		if (st.getInt("onlyone") == 0)
            		{
                		if (Util.contains(CLASSES, talker.getClassId().getId()))
                		{
                    			if (talker.getLevel() >= 35)
                    			{
                        			htmltext = "30461-03.htm";
                    			}
                    			else
                    			{
                        			htmltext = "30461-02.htm";
                        			st.exitQuest(true);
                    			}
                		}
                		else
                		{
                    			htmltext = "30461-01.htm";
                    			st.exitQuest(true);
                		}
            		}
            		else
            		{
                		htmltext = getAlreadyCompletedMsg(talker);
            		}
        	}
        	else if (npcId == MIRIEN && cond == 1)
        	{
            		htmltext = "30461-05.htm";
        	}
        	else if (npcId == MIRIEN && cond == 14)
        	{
            		htmltext = "30461-06.htm";
            		st.takeItems(SYMBOL_OF_SYLVAIN, -1);
            		st.takeItems(MIRIENS_SIGIL1, -1);
            		st.giveItems(MIRIENS_SIGIL2, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "15");
        	}
        	else if (npcId == MIRIEN && (cond > 14 && cond < 18))
        	{
            		htmltext = "30461-07.htm";
        	}
        	else if (npcId == MIRIEN && cond == 18 && st.getQuestItemsCount(MIRIENS_INSTRUCTION) >= 1)
        	{
            		if (talker.getLevel() < 35)
            		{
                		htmltext = "30461-11.htm";
            		}
            		else
            		{
                		htmltext = "30461-12.htm";
                		st.giveItems(MIRIENS_SIGIL3, 1);
                		st.takeItems(MIRIENS_INSTRUCTION, -1);
                		st.playSound("ItemSound.quest_middle");
                		st.set("cond", "19");
            		}
        	}
        	else if (npcId == MIRIEN && cond == 18)
        	{
            		htmltext = "30461-08.htm";
        	}
        	else if (npcId == MIRIEN && cond == 19)
        	{
            		htmltext = "30461-13.htm";
        	}
        	else if (npcId == MIRIEN && cond == 31 && st.getQuestItemsCount(SYMBOL_OF_CRONOS) >= 1)
        	{
            		htmltext = "30461-14.htm";
            		st.set("cond", "0");
            		st.set("onlyone", "1");
            		st.takeItems(MIRIENS_SIGIL3, -1);
            		st.takeItems(SYMBOL_OF_CRONOS, -1);
            		st.addExpAndSp(876963, 56877);
            		st.giveItems(57, 159814);
	    		st.giveItems(7562, 168);
            		st.giveItems(MARK_OF_SCHOLAR, 1);
            		st.exitQuest(false);
            		st.playSound("ItemSound.quest_finish");
        	}
        	else if (npcId == SYLVAIN && cond == 1)
        	{
            		htmltext = "30070-01.htm";
        	}
        	else if (npcId == SYLVAIN && cond == 2)
        	{
            		htmltext = "30070-03.htm";
        	}
        	else if (npcId == SYLVAIN && cond == 13)
        	{
            		htmltext = "30070-04.htm";
            		st.giveItems(SYMBOL_OF_SYLVAIN, 1);
            		st.takeItems(HIGHPRIESTS_SIGIL, -1);
            		st.takeItems(CRYSTAL_OF_PURITY1, -1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "14");
        	}
        	else if (npcId == SYLVAIN && cond == 14)
        	{
            		htmltext = "30070-05.htm";
        	}
        	else if (npcId == SYLVAIN && cond > 14)
        	{
            		htmltext = "30070-06.htm";
        	}
        	else if (npcId == MARIA && cond == 2)
        	{
            		htmltext = "30608-01.htm";
        	}
        	else if (npcId == MARIA && cond == 3)
        	{
            		htmltext = "30608-03.htm";
        	}
        	else if (npcId == MARIA && cond == 4)
        	{
            		htmltext = "30608-04.htm";
            		st.giveItems(MARIAS_LETTER2, 1);
            		st.takeItems(LUKAS_LETTER, -1);
            		st.set("cond", "5");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (npcId == MARIA && cond == 5)
        	{
            		htmltext = "30608-05.htm";
        	}
        	else if (npcId == MARIA && cond == 6)
        	{
            		htmltext = "30608-06.htm";
        	}
        	else if (npcId == MARIA && cond == 7)
        	{
            		htmltext = "30608-09.htm";
        	}
        	else if (npcId == MARIA && cond == 8)
        	{
            		htmltext = "30608-10.htm";
            		st.giveItems(CRETAS_PAINTING2, 1);
            		st.takeItems(CRETAS_PAINTING1, -1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "9");
        	}
        	else if (npcId == MARIA && cond == 9)
        	{
            		htmltext = "30608-11.htm";
        	}
        	else if (npcId == MARIA && cond == 10)
        	{
            		htmltext = "30608-12.htm";
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "11");
        	}
        	else if (npcId == MARIA && cond == 12)
        	{
            		htmltext = "30608-13.htm";
        	}
        	else if (npcId == MARIA && cond == 13)
        	{
            		htmltext = "30608-15.htm";
        	}
        	else if (npcId == MARIA && (st.getQuestItemsCount(SYMBOL_OF_SYLVAIN) > 0 || st.getQuestItemsCount(MIRIENS_SIGIL2) > 0))
        	{
            		htmltext = "30608-16.htm";
        	}
        	else if (npcId == MARIA && st.getQuestItemsCount(MIRIENS_SIGIL3) >= 1 && st.getQuestItemsCount(VALKONS_REQUEST) == 0)
        	{
            		htmltext = "30608-17.htm";
        	}
        	else if (npcId == MARIA && cond == 26 && st.getQuestItemsCount(VALKONS_REQUEST) >= 1)
        	{
            		htmltext = "30608-18.htm";
            		st.giveItems(CRYSTAL_OF_PURITY2, 1);
            		st.takeItems(VALKONS_REQUEST, -1);
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (npcId == JUREK && cond == 15)
        	{
            		htmltext = "30115-01.htm";
        	}
        	else if (npcId == JUREK && cond == 16)
        	{
            		htmltext = "30115-04.htm";
        	}
        	else if (npcId == JUREK && cond == 17)
        	{
            		htmltext = "30115-05.htm";
            		st.takeItems(JUREKS_LIST, -1);
            		st.takeItems(MEYEDESTROYERS_SKIN, -1);
            		st.takeItems(SHAMANS_NECKLACE, -1);
            		st.takeItems(SHACKLES_SCALP, -1);
            		st.takeItems(GMAGISTERS_SIGIL, -1);
            		st.giveItems(SYMBOL_OF_JUREK, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "18");
        	}
        	else if (npcId == JUREK && cond == 18)
        	{
            		htmltext = "30115-06.htm";
        	}
        	else if (npcId == JUREK && cond > 18)
        	{
            		htmltext = "30115-07.htm";
        	}
        	else if (npcId == LUCAS && cond == 3)
        	{
            		htmltext = "30071-01.htm";
            		st.set("cond", "4");
            		st.giveItems(LUKAS_LETTER, 1);
            		st.takeItems(MARIAS_LETTER1, -1);
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (npcId == LUCAS && cond == 4)
        	{
            		htmltext = "30071-02.htm";
        	}
        	else if (npcId == LUCAS && cond == 9)
        	{
            		htmltext = "30071-03.htm";
        	}
        	else if (npcId == LUCAS && cond == 10)
        	{
            		htmltext = st.getQuestItemsCount(BROWN_SCROLL_SCRAP) < 5 ? "30071-05.htm" : "30071-06.htm";
        	}
       	 	else if (npcId == LUCAS && cond < 10)
        	{
            		htmltext = "30071-07.htm";
        	}
        	else if (npcId == CRETA && cond == 5)
        	{
            		htmltext = "30609-01.htm";
        	}
        	else if (npcId == CRETA && cond == 6)
        	{
            		htmltext = "30609-06.htm";
        	}
        	else if (npcId == CRETA && cond == 7)
        	{
            		htmltext = "30609-07.htm";
        	}
        	else if (npcId == CRETA && cond == 8)
        	{
            		htmltext = "30609-10.htm";
        	}
        	else if (npcId == CRETA && (cond > 9 && cond < 21))
        	{
            		htmltext = "30609-11.htm";
        	}
        	else if (npcId == CRETA && cond == 21)
        	{
            		htmltext = "30609-12.htm";
        	}
        	else if (npcId == CRETA && cond == 22)
        	{
            		htmltext = "30609-14.htm";
        	}
        	else if (npcId == CRETA && cond > 22)
        	{
            		htmltext = "30609-15.htm";
        	}
        	else if (npcId == CRONOS && cond == 19)
        	{
            		htmltext = "30610-01.htm";
        	}
        	else if (npcId == CRONOS && cond == 20)
        	{
            		htmltext = "30610-11.htm";
        	}
        	else if (npcId == CRONOS && cond == 30)
        	{
            		htmltext = "30610-12.htm";
        	}
        	else if (npcId == CRONOS && cond == 31)
        	{
            		htmltext = "30610-15.htm";
        	}
        	else if (npcId == DIETER && cond == 20)
        	{
            		htmltext = "30111-01.htm";
        	}
        	else if (npcId == DIETER && cond == 21)
        	{
            		htmltext = "30111-06.htm";
        	}
        	else if (npcId == DIETER && cond == 22)
        	{
            		htmltext = "30111-07.htm";
        	}
        	else if (npcId == DIETER && cond == 23)
        	{
            		htmltext = "30111-10.htm";
        	}
        	else if (npcId == DIETER && st.getQuestItemsCount(MIRIENS_SIGIL3) > 0 && st.getQuestItemsCount(CRONOS_SIGIL) > 0 && st.getQuestItemsCount(DIETERS_DIARY) > 0 && st.getQuestItemsCount(RAUTS_LETTER_ENVELOPE) > 0)
        	{
            		htmltext = "30111-11.htm";
        	}
        	else if (npcId == DIETER && st.getQuestItemsCount(MIRIENS_SIGIL3) > 0 && st.getQuestItemsCount(CRONOS_SIGIL) > 0 && st.getQuestItemsCount(DIETERS_DIARY) > 0 && st.getQuestItemsCount(DIETERS_LETTER) == 0 && st.getQuestItemsCount(RAUTS_LETTER_ENVELOPE) == 0)
        	{
            		htmltext = st.getQuestItemsCount(SCRIPTURE_CHAPTER_1) > 0 && st.getQuestItemsCount(SCRIPTURE_CHAPTER_2) > 0 && st.getQuestItemsCount(SCRIPTURE_CHAPTER_3) > 0 && st.getQuestItemsCount(SCRIPTURE_CHAPTER_4) > 0 ? "30111-13.htm" : "30111-12.htm";
        	}
        	else if (npcId == DIETER && st.getQuestItemsCount(SYMBOL_OF_CRONOS) >= 1)
        	{
            		htmltext = "30111-15.htm";
        	}
        	else if (npcId == EDROC && cond == 23)
        	{
            		htmltext = "30230-01.htm";
        	}
        	else if (npcId == EDROC && cond == 24)
        	{
            		htmltext = "30230-03.htm";
        	}
        	else if (npcId == EDROC && st.getQuestItemsCount(DIETERS_DIARY) >= 1 && (st.getQuestItemsCount(STRONG_LIQUOR) > 0 || st.getQuestItemsCount(TRIFFS_RING) > 0))
        	{
            		htmltext = "30230-04.htm";
        	}
        	else if (npcId == RAUT && cond == 24)
        	{
            		htmltext = "30316-01.htm";
        	}
        	else if (npcId == RAUT && cond == 25)
        	{
            		htmltext = "30316-04.htm";
        	}
        	else if (npcId == RAUT && st.getQuestItemsCount(DIETERS_DIARY) > 0 && st.getQuestItemsCount(SCRIPTURE_CHAPTER_1) > 0 && st.getQuestItemsCount(TRIFFS_RING) > 0)
        	{
            		htmltext = "30316-05.htm";
        	}
        	else if (npcId == TRIFF && cond == 25)
        	{
            		htmltext = "30611-01.htm";
        	}
        	else if (npcId == TRIFF && cond > 25)
        	{
            		htmltext = "30611-05.htm";
        	}
        	else if (npcId == VALKON && cond == 26 && st.getQuestItemsCount(CRYSTAL_OF_PURITY2) >= 1)
        	{
            		htmltext = "30103-06.htm";
            		st.giveItems(SCRIPTURE_CHAPTER_2, 1);
            		st.takeItems(CRYSTAL_OF_PURITY2, -1);
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (npcId == VALKON && cond == 26 && st.getQuestItemsCount(VALKONS_REQUEST) >= 1)
        	{
            		htmltext = "30103-05.htm";
        	}
        	else if (npcId == VALKON && cond == 26 && st.getQuestItemsCount(SCRIPTURE_CHAPTER_2) == 0)
        	{
            		htmltext = "30103-01.htm";
        	}
        	else if (npcId == VALKON && st.getQuestItemsCount(SCRIPTURE_CHAPTER_2) >= 1)
        	{
            		htmltext = "30103-07.htm";
        	}
        	else if (npcId == POITAN && cond == 26 && st.getQuestItemsCount(POITANS_NOTES) == 0)
        	{
            		htmltext = "30458-01.htm";
            		st.giveItems(POITANS_NOTES, 1);
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (npcId == POITAN && cond == 26 && st.getQuestItemsCount(POITANS_NOTES) > 0 && st.getQuestItemsCount(CASIANS_LIST) == 0)
        	{
            		htmltext = "30458-02.htm";
        	}
        	else if (npcId == POITAN && cond == 27 && st.getQuestItemsCount(POITANS_NOTES) > 0 && st.getQuestItemsCount(CASIANS_LIST) > 0)
        	{
            		htmltext = "30458-03.htm";
        	}
        	else if (npcId == POITAN && cond >= 28)
        	{
            		htmltext = "30458-04.htm";
        	}
        	else if (npcId == CASIAN && cond == 26)
        	{
            		htmltext = st.getQuestItemsCount(SCRIPTURE_CHAPTER_1) > 0 && st.getQuestItemsCount(SCRIPTURE_CHAPTER_2) > 0 && st.getQuestItemsCount(SCRIPTURE_CHAPTER_3) > 0 ? "30612-02.htm" : "30612-01.htm";
        	}
        	else if (npcId == CASIAN && cond == 28)
        	{
            		if (st.getQuestItemsCount(GHOULS_SKIN) + st.getQuestItemsCount(MEDUSAS_BLOOD) + st.getQuestItemsCount(FETTEREDSOULS_ICHOR) + st.getQuestItemsCount(ENCHT_GARGOYLES_NAIL) < 32)
            		{
                		htmltext = "30612-05.htm";
            		}
        	}
        	else if (npcId == CASIAN && cond == 29)
        	{
            		htmltext = "30612-06.htm";
        	}
        	else if (npcId == CASIAN && cond >= 30)
        	{
            		htmltext = "30612-08.htm";
        	}
        	return htmltext;
    	}

    	@Override
    	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
    	{
        	QuestState st = killer.getQuestState(qn);
        	if (st == null)
        	{
            		return null;
        	}

        	final int npcId = npc.getId();
        	final int cond = st.getInt("cond");

        	if (npcId == LETO_LIZARDMAN_WARRIOR && cond == 11)
        	{
            		if (st.getQuestItemsCount(BROWN_SCROLL_SCRAP) < 5)
            		{
                		if (st.getRandom(100) < 50)
                		{
                    			st.giveItems(BROWN_SCROLL_SCRAP, 1);
                    			if (st.getQuestItemsCount(BROWN_SCROLL_SCRAP) < 5)
                    			{
                        			st.playSound("ItemSound.quest_itemget");
                    			}
                    			else
                    			{
                        			st.playSound("ItemSound.quest_middle");
                        			st.set("cond", "12");
                    			}
                		}
            		}
        	}
        	if (cond == 16)
        	{
            		if (npcId == MONSTER_EYE_DESTROYER)
            		{
                		if (st.getQuestItemsCount(MEYEDESTROYERS_SKIN) < 5)
                		{
                    			if (st.getRandom(100) < 50)
                    			{
                        			st.giveItems(MEYEDESTROYERS_SKIN, 1);
                        			if (st.getQuestItemsCount(MEYEDESTROYERS_SKIN) < 5)
                        			{
                            				st.playSound("ItemSound.quest_itemget");
                        			}
                        			else
                        			{
                            				st.playSound("ItemSound.quest_middle");
                            				if (st.getQuestItemsCount(SHACKLES_SCALP) == 2 && st.getQuestItemsCount(SHAMANS_NECKLACE) == 5 && st.getQuestItemsCount(MEYEDESTROYERS_SKIN) == 5)
                            				{
                                				st.set("cond", "17");
                            				}
                        			}
                    			}
                		}
            		}
            		else if (npcId == BREKA_ORC_SHAMAN)
            		{
                		if (st.getQuestItemsCount(SHAMANS_NECKLACE) < 5)
                		{
                    			if (st.getRandom(100) < 50)
                    			{
                        			st.giveItems(SHAMANS_NECKLACE, 1);
                        			if (st.getQuestItemsCount(SHAMANS_NECKLACE) < 5)
                        			{
                            			st.playSound("ItemSound.quest_itemget");
                        			}
                        			else
                        			{
                            				st.playSound("ItemSound.quest_middle");
                            				if (st.getQuestItemsCount(SHACKLES_SCALP) == 2 && st.getQuestItemsCount(SHAMANS_NECKLACE) == 5 && st.getQuestItemsCount(MEYEDESTROYERS_SKIN) == 5)
                            				{
                                				st.set("cond", "17");
                            				}
                        			}
                    			}
                		}
            		}
            		else if (npcId == SHACKLE)
            		{
                		if (st.getQuestItemsCount(SHACKLES_SCALP) < 2)
                		{
                    			st.giveItems(SHACKLES_SCALP, 1);
                    			if (st.getQuestItemsCount(SHACKLES_SCALP) < 2)
                    			{
                        			st.playSound("ItemSound.quest_itemget");
                    			}
                    			else
                    			{
                        			st.playSound("ItemSound.quest_middle");
                        			if (st.getQuestItemsCount(SHACKLES_SCALP) == 2 && st.getQuestItemsCount(SHAMANS_NECKLACE) == 5 && st.getQuestItemsCount(MEYEDESTROYERS_SKIN) == 5)
                        			{
                            				st.set("cond", "17");
                        			}
                    			}
                		}
            		}
        	}
        	else if (npcId == GRANDIS && (st.getInt("cond") == 26 || st.getInt("cond") == 27) && st.getQuestItemsCount(SCRIPTURE_CHAPTER_3) == 0)
        	{
            		if (st.getRandom(100) < 30)
            		{
                		st.giveItems(SCRIPTURE_CHAPTER_3, 1);
                		st.playSound("ItemSound.quest_middle");
            		}
        	}
        	else if (npcId == GHOUL)
        	{
            		if (cond == 28 && st.getQuestItemsCount(GHOULS_SKIN) < 10)
            		{
                		st.giveItems(GHOULS_SKIN, 1);
                		if (st.getQuestItemsCount(GHOULS_SKIN) < 10)
                		{
                    			st.playSound("ItemSound.quest_itemget");
                		}
                		else
                		{
                    			st.playSound("ItemSound.quest_middle");
                    			if (st.getQuestItemsCount(GHOULS_SKIN) == 10 && st.getQuestItemsCount(MEDUSAS_BLOOD) == 12 && st.getQuestItemsCount(FETTEREDSOULS_ICHOR) == 5 && st.getQuestItemsCount(ENCHT_GARGOYLES_NAIL) == 5)
                    			{
                        			st.set("cond", "29");
                    			}
                		}
            		}
        	}
        	else if (npcId == MEDUSA)
        	{
            		if (cond == 28 && st.getQuestItemsCount(MEDUSAS_BLOOD) < 12)
            		{
                		st.giveItems(MEDUSAS_BLOOD, 1);
                		if (st.getQuestItemsCount(MEDUSAS_BLOOD) < 12)
                		{
                    			st.playSound("ItemSound.quest_itemget");
                		}
                		else
                		{
                    			st.playSound("ItemSound.quest_middle");
                    			if (st.getQuestItemsCount(GHOULS_SKIN) == 10 && st.getQuestItemsCount(MEDUSAS_BLOOD) == 12 && st.getQuestItemsCount(FETTEREDSOULS_ICHOR) == 5 && st.getQuestItemsCount(ENCHT_GARGOYLES_NAIL) == 5)
                    			{
                        			st.set("cond", "29");
                    			}
                		}
            		}
        	}
        	else if (npcId == FETTERED_SOUL)
        	{
            		if (cond == 28 && st.getQuestItemsCount(FETTEREDSOULS_ICHOR) < 5)
            		{
                		st.giveItems(FETTEREDSOULS_ICHOR, 1);
                		if (st.getQuestItemsCount(FETTEREDSOULS_ICHOR) < 5)
                		{
                    			st.playSound("ItemSound.quest_itemget");
                		}
                		else
                		{
                   			st.playSound("ItemSound.quest_middle");
                    			if (st.getQuestItemsCount(GHOULS_SKIN) == 10 && st.getQuestItemsCount(MEDUSAS_BLOOD) == 12 && st.getQuestItemsCount(FETTEREDSOULS_ICHOR) == 5 && st.getQuestItemsCount(ENCHT_GARGOYLES_NAIL) == 5)
                    			{
                        			st.set("cond", "29");
                    			}
                		}
            		}
        	}
        	else if (npcId == ENCHANTED_GARGOYLE)
        	{
            		if (cond == 28 && st.getQuestItemsCount(ENCHT_GARGOYLES_NAIL) < 5)
            		{
                		st.giveItems(ENCHT_GARGOYLES_NAIL, 1);
                		if (st.getQuestItemsCount(ENCHT_GARGOYLES_NAIL) < 5)
                		{
                    			st.playSound("ItemSound.quest_itemget");
                		}
                		else
                		{
                    			st.playSound("ItemSound.quest_middle");
                    			if (st.getQuestItemsCount(GHOULS_SKIN) == 10 && st.getQuestItemsCount(MEDUSAS_BLOOD) == 12 && st.getQuestItemsCount(FETTEREDSOULS_ICHOR) == 5 && st.getQuestItemsCount(ENCHT_GARGOYLES_NAIL) == 5)
                    			{
                        			st.set("cond", "29");
                    			}
                		}
            		}
        	}
        	return null;
    	}

    	public static void main(String[] args)
    	{
        	new _214_TrialOfScholar(214, qn, "");
    	}
}