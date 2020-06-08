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
public class _211_TrialOfChallenger extends Quest
{
    	private static final String qn = "_211_TrialOfChallenger";

    	// Npc
    	private static final int FILAUR = 30535;
    	private static final int KASH = 30644;
    	private static final int MARTIEN = 30645;
    	private static final int RALDO = 30646;
    	private static final int CHEST_OF_SHYSLASSYS = 30647;

    	private static final int[] TALKERS =
    	{
        	FILAUR, KASH, MARTIEN, RALDO, CHEST_OF_SHYSLASSYS
    	};

    	// Mobs
    	private static final int SHYSLASSYS = 27110;
    	private static final int GORR = 27112;
    	private static final int BARAHAM = 27113;
    	private static final int SUCCUBUS_QUEEN = 27114;

    	private static final int[] MOBS =
    	{
        	SHYSLASSYS, GORR, BARAHAM, SUCCUBUS_QUEEN
    	};

    	// Quest items
    	private static final int LETTER_OF_KASH = 2628;
    	private static final int SCROLL_OF_SHYSLASSY = 2631;
    	private static final int WATCHERS_EYE1 = 2629;
    	private static final int BROKEN_KEY = 2632;
    	private static final int MITHRIL_SCALE_GAITERS_MATERIAL = 2918;
    	private static final int BRIGANDINE_GAUNTLET_PATTERN = 2927;
    	private static final int MANTICOR_SKIN_GAITERS_PATTERN = 1943;
    	private static final int GAUNTLET_OF_REPOSE_OF_THE_SOUL_PATTERN = 1946;
    	private static final int IRON_BOOTS_DESIGN = 1940;
    	private static final int TOME_OF_BLOOD_PAGE = 2030;
    	private static final int ELVEN_NECKLACE_BEADS = 1904;
    	private static final int WHITE_TUNIC_PATTERN = 1936;
    	private static final int WATCHERS_EYE2 = 2630;

    	private static final int[] QUESTITEMS =
    	{
        	SCROLL_OF_SHYSLASSY, LETTER_OF_KASH, WATCHERS_EYE1, BROKEN_KEY, WATCHERS_EYE2
    	};

    	// Reward
    	private static final int MARK_OF_CHALLENGER = 2627;

    	// Allowed classes
    	private static final int[] CLASSES =
    	{
        	0x01, 0x13, 0x20, 0x2d, 0x2f
    	};

    	public _211_TrialOfChallenger(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	addStartNpc(KASH);
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
            		htmltext = "30644-05.htm";
            		st.set("cond", "1");
            		st.setState(State.STARTED);
            		st.playSound("ItemSound.quest_accept");
        	}
        	else if (event.equalsIgnoreCase("30644_1"))
        	{
            		htmltext = "30644-04.htm";
        	}
        	else if (event.equalsIgnoreCase("30645_1"))
        	{
            		htmltext = "30645-02.htm";
            		st.takeItems(LETTER_OF_KASH, 1);
            		st.set("cond", "4");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30647_1"))
        	{
            		if (st.getQuestItemsCount(BROKEN_KEY) == 1)
            		{
                		st.giveItems(SCROLL_OF_SHYSLASSY, 1);
                		st.playSound("ItemSound.quest_middle");
                		if (st.getRandom(10) < 2)
                		{
                    			htmltext = "30647-03.htm";
                    			st.takeItems(BROKEN_KEY, 1);
                    			st.playSound("ItemSound.quest_jackpot");
                    			int n = st.getRandom(100);
                    			if (n > 90)
                    			{
                        			st.giveItems(MITHRIL_SCALE_GAITERS_MATERIAL, 1);
                        			st.giveItems(BRIGANDINE_GAUNTLET_PATTERN, 1);
                        			st.giveItems(MANTICOR_SKIN_GAITERS_PATTERN, 1);
                        			st.giveItems(GAUNTLET_OF_REPOSE_OF_THE_SOUL_PATTERN, 1);
                        			st.giveItems(IRON_BOOTS_DESIGN, 1);
                        			st.playSound("ItemSound.quest_middle");
                    			}
                    			else if (n > 70)
                    			{
                        			st.giveItems(TOME_OF_BLOOD_PAGE, 1);
                        			st.giveItems(ELVEN_NECKLACE_BEADS, 1);
                        			st.playSound("ItemSound.quest_middle");
                    			}
                    			else if (n > 40)
                    			{
                        			st.giveItems(WHITE_TUNIC_PATTERN, 1);
                        			st.playSound("ItemSound.quest_middle");
                    			}
                    			else
                    			{
                        			st.giveItems(IRON_BOOTS_DESIGN, 1);
                        			st.playSound("ItemSound.quest_middle");
                    			}
                		}
                		else
                		{
                    			htmltext = "30647-02.htm";
                    			st.takeItems(BROKEN_KEY, 1);
                    			st.giveItems(57, st.getRandom(1000) + 1);
                    			st.playSound("ItemSound.quest_middle");
                		}
            		}
            		else
            		{
                		htmltext = "30647-04.htm";
                		st.takeItems(BROKEN_KEY, 1);
            		}
        	}
        	else if (event.equalsIgnoreCase("30646_1"))
        	{
            		htmltext = "30646-02.htm";
        	}
        	else if (event.equalsIgnoreCase("30646_2"))
        	{
            		htmltext = "30646-03.htm";
        	}
        	else if (event.equalsIgnoreCase("30646_3"))
        	{
            		htmltext = "30646-04.htm";
            		st.set("cond", "8");
            		st.takeItems(WATCHERS_EYE2, 1);
        	}
        	else if (event.equalsIgnoreCase("30646_4"))
        	{
            		htmltext = "30646-06.htm";
            		st.set("cond", "8");
            		st.takeItems(WATCHERS_EYE2, 1);
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

        	int npcId = npc.getId();
        	int id = st.getState();

        	if (npcId != KASH && id != State.STARTED)
        	{
            		return htmltext;
        	}

        	int cond = st.getInt("cond");

        	if (id == State.CREATED)
        	{
            		if (npcId == KASH)
            		{
                		if (Util.contains(CLASSES, talker.getClassId().ordinal()))
                		{
                    			if (talker.getLevel() >= 35)
                    			{
                        			htmltext = "30644-03.htm";
                    			}
                    			else
                    			{
                        			htmltext = "30644-01.htm";
                        			st.exitQuest(true);
                    			}
                		}
                		else
                		{
                    			htmltext = "30644-02.htm";
                    			st.exitQuest(true);
                		}
            		}
        	}
        	else if (npcId == KASH && id == State.COMPLETED)
        	{
            		htmltext = Quest.getAlreadyCompletedMsg(talker);
        	}
        	else if (npcId == KASH && cond == 1)
        	{
            		htmltext = "30644-06.htm";
        	}
        	else if (npcId == KASH && cond == 2 && st.getQuestItemsCount(SCROLL_OF_SHYSLASSY) == 1)
        	{
            		htmltext = "30644-07.htm";
            		st.takeItems(SCROLL_OF_SHYSLASSY, 1);
            		st.giveItems(LETTER_OF_KASH, 1);
            		st.set("cond", "3");
            		st.playSound("ItemSound.quest_middle");
       	 	}
        	else if (npcId == KASH && cond == 1 && st.getQuestItemsCount(LETTER_OF_KASH) == 1)
        	{
            		htmltext = "30644-08.htm";
        	}
        	else if (npcId == KASH && cond >= 7)
        	{
            		htmltext = "30644-09.htm";
        	}
        	else if (npcId == MARTIEN && cond == 3 && st.getQuestItemsCount(LETTER_OF_KASH) == 1)
        	{
            		htmltext = "30645-01.htm";
        	}
        	else if (npcId == MARTIEN && cond == 4 && st.getQuestItemsCount(WATCHERS_EYE1) == 0)
        	{
            		htmltext = "30645-03.htm";
        	}
        	else if (npcId == MARTIEN && cond == 5 && st.getQuestItemsCount(WATCHERS_EYE1) > 0)
        	{
            		htmltext = "30645-04.htm";
            		st.takeItems(WATCHERS_EYE1, 1);
            		st.set("cond", "6");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (npcId == MARTIEN && cond == 6)
        	{
            		htmltext = "30645-05.htm";
        	}
        	else if (npcId == MARTIEN && cond >= 7)
        	{
            		htmltext = "30645-06.htm";
        	}
        	else if (npcId == CHEST_OF_SHYSLASSYS && cond == 2)
        	{
            		htmltext = "30647-01.htm";
        	}
        	else if (npcId == RALDO && cond == 7 && st.getQuestItemsCount(WATCHERS_EYE2) > 0)
        	{
            		htmltext = "30646-01.htm";
        	}
        	else if (npcId == RALDO && cond == 7)
        	{
            		htmltext = "30646-06a.htm";
        	}
        	else if (npcId == RALDO && cond == 10)
        	{
            		htmltext = "30646-07.htm";
            		st.set("cond", "0");
            		st.takeItems(BROKEN_KEY, 1);
            		st.addExpAndSp(533803, 34621);
            		st.giveItems(57, 97278);
			st.giveItems(7562, 61);
            		st.giveItems(MARK_OF_CHALLENGER, 1);
            		st.exitQuest(false);
            		st.playSound("ItemSound.quest_finish");
        	}
        	else if (npcId == FILAUR && cond == 7)
        	{
            		if (talker.getLevel() >= 35)
            		{
                		htmltext = "30535-01.htm";
                		st.addRadar(176560, -184969, -3729);
                		st.set("cond", "8");
                		st.playSound("ItemSound.quest_middle");
            		}
            		else
            		{
                		htmltext = "30535-03.htm";
            		}
        	}
        	else if (npcId == FILAUR && cond == 8)
        	{
            		htmltext = "30535-02.htm";
            		st.addRadar(176560, -184969, -3729);
            		st.set("cond", "9");
            		st.playSound("ItemSound.quest_middle");
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

        	if (npcId == SHYSLASSYS && cond == 1 && st.getQuestItemsCount(BROKEN_KEY) == 0)
        	{
            		st.giveItems(BROKEN_KEY, 1);
            		st.addSpawn(CHEST_OF_SHYSLASSYS, npc, true, 0);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "2");
        	}
        	else if (npcId == GORR && cond == 4 && st.getQuestItemsCount(WATCHERS_EYE1) == 0)
        	{
            		st.giveItems(WATCHERS_EYE1, 1);
            		st.set("cond", "5");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (npcId == BARAHAM && cond == 6 && st.getQuestItemsCount(WATCHERS_EYE2) == 0)
        	{
            		st.giveItems(WATCHERS_EYE2, 1);
            		st.playSound("ItemSound.quest_middle");
            		st.set("cond", "7");
            		st.addSpawn(RALDO, npc, false, 300000);
        	}
        	else if (npcId == SUCCUBUS_QUEEN && cond == 9)
        	{
            		st.set("cond", "10");
            		st.playSound("ItemSound.quest_middle");
            		st.addSpawn(RALDO, npc, false, 300000);
        	}
        	return super.onKill(npc, killer, isSummon);
    	}

    	public static void main(String[] args)
    	{
        	new _211_TrialOfChallenger(211, qn, "");
    	}
}