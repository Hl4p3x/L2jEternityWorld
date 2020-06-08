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
 * Created by LordWinter 04.10.2012
 * Based on L2J Eternity-World
 */
public class _218_TestimonyOfLife extends Quest
{
    	private static final String qn = "_218_TestimonyOfLife";

    	// Npc
    	private static final int CARDIEN = 30460;
    	private static final int ASTERIOS = 30154;
    	private static final int PUSHKIN = 30300;
    	private static final int THALIA = 30371;
    	private static final int ADONIUS = 30375;
    	private static final int ARKENIA = 30419;
    	private static final int ISAEL_SILVERSHADOW = 30655;

    	private static final int[] TALKERS =
    	{
        	CARDIEN, ASTERIOS, PUSHKIN, THALIA, ADONIUS, ARKENIA, ISAEL_SILVERSHADOW
    	};

    	// Mobs
    	private static final int HARPY = 20145;
    	private static final int WYRM = 20176;
    	private static final int MARSH_SPIDER = 20233;
    	private static final int UNICORN_OF_EVA = 27077;
    	private static final int GUARDIAN_BASILISK = 20550;
    	private static final int LETO_LIZARDMAN_SHAMAN = 20581;
    	private static final int LETO_LIZARDMAN_OVERLORD = 20582;
    	private static final int ANT_RECRUIT = 20082;
    	private static final int ANT_PATROL = 20084;
    	private static final int ANT_GUARD = 20086;
    	private static final int ANT_SOLDIER = 20087;
    	private static final int ANT_WARRIOR_CAPTAIN = 20088;

    	private static final int[] MOBS =
    	{
        	HARPY, WYRM, MARSH_SPIDER, UNICORN_OF_EVA, GUARDIAN_BASILISK, LETO_LIZARDMAN_SHAMAN,
        	LETO_LIZARDMAN_OVERLORD, ANT_RECRUIT, ANT_PATROL, ANT_GUARD, ANT_SOLDIER, ANT_WARRIOR_CAPTAIN
    	};

    	// Quest items
    	private static final int CARDIENS_LETTER = 3141;
    	private static final int CAMOMILE_CHARM = 3142;
    	private static final int HIERARCHS_LETTER = 3143;
    	private static final int MOONFLOWER_CHARM = 3144;
    	private static final int GRAIL_DIAGRAM = 3145;
    	private static final int THALIAS_LETTER1 = 3146;
    	private static final int THALIAS_LETTER2 = 3147;
    	private static final int THALIAS_INSTRUCTIONS = 3148;
    	private static final int PUSHKINS_LIST = 3149;
    	private static final int PURE_MITHRIL_CUP = 3150;
    	private static final int ARKENIAS_CONTRACT = 3151;
    	private static final int ARKENIAS_INSTRUCTIONS = 3152;
    	private static final int ADONIUS_LIST = 3153;
    	private static final int ANDARIEL_SCRIPTURE_COPY = 3154;
    	private static final int STARDUST = 3155;
    	private static final int ISAELS_INSTRUCTIONS = 3156;
    	private static final int ISAELS_LETTER = 3157;
    	private static final int GRAIL_OF_PURITY = 3158;
    	private static final int TEARS_OF_UNICORN = 3159;
    	private static final int WATER_OF_LIFE = 3160;
    	private static final int PURE_MITHRIL_ORE = 3161;
    	private static final int ANT_SOLDIER_ACID = 3162;
    	private static final int WYRMS_TALON1 = 3163;
    	private static final int SPIDER_ICHOR = 3164;
    	private static final int HARPYS_DOWN = 3165;
    	private static final int TALINS_SPEAR_BLADE = 3166;
    	private static final int TALINS_SPEAR_SHAFT = 3167;
    	private static final int TALINS_RUBY = 3168;
    	private static final int TALINS_AQUAMARINE = 3169;
    	private static final int TALINS_AMETHYST = 3170;
    	private static final int TALINS_PERIDOT = 3171;
    	private static final int TALINS_SPEAR = 3026;

    	private static final int[] QUESTITEMS =
    	{
        	CAMOMILE_CHARM, CARDIENS_LETTER, WATER_OF_LIFE, MOONFLOWER_CHARM, HIERARCHS_LETTER, STARDUST,
        	PURE_MITHRIL_CUP, THALIAS_INSTRUCTIONS, ISAELS_LETTER, TEARS_OF_UNICORN, GRAIL_DIAGRAM, PUSHKINS_LIST,
        	THALIAS_LETTER1, ARKENIAS_CONTRACT, ANDARIEL_SCRIPTURE_COPY, ARKENIAS_INSTRUCTIONS, ADONIUS_LIST,
        	THALIAS_LETTER2, TALINS_SPEAR_BLADE, TALINS_SPEAR_SHAFT, TALINS_RUBY, TALINS_AQUAMARINE, TALINS_AMETHYST,
        	TALINS_PERIDOT, ISAELS_INSTRUCTIONS, GRAIL_OF_PURITY
    	};

    	// Reward
    	private static final int MARK_OF_LIFE = 3140;

    	public _218_TestimonyOfLife(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	addStartNpc(CARDIEN);

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
            		htmltext = "30460-04.htm";
            		st.set("cond", "1");
            		st.setState(State.STARTED);
            		st.playSound("ItemSound.quest_accept");
            		st.giveItems(CARDIENS_LETTER, 1);
        	}
        	else if (event.equalsIgnoreCase("30154_1"))
        	{
            		htmltext = "30154-02.htm";
        	}
        	else if (event.equalsIgnoreCase("30154_2"))
        	{
            		htmltext = "30154-03.htm";
        	}
        	else if (event.equalsIgnoreCase("30154_3"))
        	{
            		htmltext = "30154-04.htm";
        	}
        	else if (event.equalsIgnoreCase("30154_4"))
        	{
            		htmltext = "30154-05.htm";
        	}
        	else if (event.equalsIgnoreCase("30154_5"))
        	{
            		htmltext = "30154-06.htm";
        	}
        	else if (event.equalsIgnoreCase("30154_6"))
        	{
            		htmltext = "30154-07.htm";
            		st.set("cond", "2");
            		st.takeItems(CARDIENS_LETTER, 1);
            		st.giveItems(MOONFLOWER_CHARM, 1);
            		st.giveItems(HIERARCHS_LETTER, 1);
        	}
        	else if (event.equalsIgnoreCase("30371_1"))
        	{
            		htmltext = "30371-02.htm";
        	}
        	else if (event.equalsIgnoreCase("30371_2"))
        	{
            		htmltext = "30371-03.htm";
            		st.set("cond", "3");
            		st.takeItems(HIERARCHS_LETTER, 1);
            		st.giveItems(GRAIL_DIAGRAM, 1);
        	}
        	else if (event.equalsIgnoreCase("30371_3"))
        	{
            		if (player.getLevel() < 37)
            		{
                		htmltext = "30371-10.htm";
                		st.set("cond", "13");
                		st.takeItems(STARDUST, 1);
                		st.giveItems(THALIAS_INSTRUCTIONS, 1);
            		}
            		else
            		{
                		htmltext = "30371-11.htm";
                		st.set("cond", "14");
                		st.takeItems(STARDUST, 1);
                		st.giveItems(THALIAS_LETTER2, 1);
            		}
        	}
        	else if (event.equalsIgnoreCase("30300_1"))
        	{
            		htmltext = "30300-02.htm";
        	}
        	else if (event.equalsIgnoreCase("30300_2"))
        	{
            		htmltext = "30300-03.htm";
        	}
        	else if (event.equalsIgnoreCase("30300_3"))
        	{
            		htmltext = "30300-04.htm";
        	}
        	else if (event.equalsIgnoreCase("30300_4"))
        	{
            		htmltext = "30300-05.htm";
        	}
        	else if (event.equalsIgnoreCase("30300_5"))
        	{
            		htmltext = "30300-06.htm";
            		st.set("cond", "4");
            		st.takeItems(GRAIL_DIAGRAM, 1);
            		st.giveItems(PUSHKINS_LIST, 1);
        	}
        	else if (event.equalsIgnoreCase("30300_6"))
        	{
            		htmltext = "30300-09.htm";
        	}
        	else if (event.equalsIgnoreCase("30300_7"))
        	{
            		htmltext = "30300-10.htm";
            		st.set("cond", "6");
            		st.takeItems(PURE_MITHRIL_ORE, st.getQuestItemsCount(PURE_MITHRIL_ORE));
            		st.takeItems(ANT_SOLDIER_ACID, st.getQuestItemsCount(ANT_SOLDIER_ACID));
            		st.takeItems(WYRMS_TALON1, st.getQuestItemsCount(WYRMS_TALON1));
            		st.takeItems(PUSHKINS_LIST, 1);
            		st.giveItems(PURE_MITHRIL_CUP, 1);
        	}
        	else if (event.equalsIgnoreCase("30419_1"))
        	{
            		htmltext = "30419-02.htm";
        	}
        	else if (event.equalsIgnoreCase("30419_2"))
        	{
            		htmltext = "30419-03.htm";
        	}
        	else if (event.equalsIgnoreCase("30419_3"))
        	{
            		htmltext = "30419-04.htm";
            		st.set("cond", "8");
            		st.takeItems(THALIAS_LETTER1, 1);
            		st.giveItems(ARKENIAS_CONTRACT, 1);
            		st.giveItems(ARKENIAS_INSTRUCTIONS, 1);
        	}
        	else if (event.equalsIgnoreCase("30375_1"))
        	{
            		htmltext = "30375-02.htm";
            		st.set("cond", "9");
            		st.takeItems(ARKENIAS_INSTRUCTIONS, 1);
            		st.giveItems(ADONIUS_LIST, 1);
        	}
        	else if (event.equalsIgnoreCase("30655_1"))
        	{
            		htmltext = "30655-02.htm";
            		st.set("cond", "15");
            		st.takeItems(THALIAS_LETTER2, 1);
            		st.giveItems(ISAELS_INSTRUCTIONS, 1);
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

        	if (npcId != CARDIEN && id != State.STARTED)
        	{
            		return htmltext;
        	}

        	if (npcId == CARDIEN)
        	{
            		if (st.getInt("cond") == 0)
            		{
                		if (id == State.COMPLETED)
                		{
                    			htmltext = getAlreadyCompletedMsg(talker);
                		}
                		else
                		{
                    			if (talker.getRace().ordinal() == 1)
                    			{
                        			if (talker.getLevel() < 37)
                        			{
                           		 		htmltext = "30460-02.htm";
                            				st.exitQuest(true);
                        			}
                        			else
                        			{
                            				htmltext = "30460-03.htm";
                        			}
                    			}
                    			else
                    			{
                        			htmltext = "30460-01.htm";
                    			}
                		}
            		}
            		else if (st.getQuestItemsCount(CARDIENS_LETTER) > 0)
            		{
                		htmltext = "30460-05.htm";
            		}
            		else if (st.getQuestItemsCount(MOONFLOWER_CHARM) > 0)
            		{
                		htmltext = "30460-06.htm";
            		}
            		else if (st.getQuestItemsCount(CAMOMILE_CHARM) > 0)
            		{
                		if (getGameTicks() != st.getInt("id"))
                		{
                    			htmltext = "30460-07.htm";
                    			st.set("id", String.valueOf(getGameTicks()));
                    			st.set("cond", "22");
                    			st.takeItems(CAMOMILE_CHARM, 1);
                    			st.addExpAndSp(943416, 62959);
                    			st.giveItems(57, 171144);
		    			st.giveItems(7562, 102);
                    			st.giveItems(MARK_OF_LIFE, 1);
                    			st.exitQuest(false);
                    			st.playSound("ItemSound.quest_finish");
                		}
            		}
        	}
        	else if (npcId == ASTERIOS)
        	{
            		if (st.getQuestItemsCount(CARDIENS_LETTER) > 0)
            		{
                		htmltext = "30154-01.htm";
            		}
            		else if (st.getQuestItemsCount(WATER_OF_LIFE) > 0)
            		{
                		htmltext = "30154-09.htm";
                		st.set("cond", "21");
                		st.takeItems(WATER_OF_LIFE, 1);
                		st.takeItems(MOONFLOWER_CHARM, 1);
                		st.giveItems(CAMOMILE_CHARM, 1);
            		}
            		else if (st.getQuestItemsCount(MOONFLOWER_CHARM) > 0)
            		{
                		htmltext = "30154-08.htm";
            		}
            		else if (st.getQuestItemsCount(CAMOMILE_CHARM) > 0)
            		{
                		htmltext = "30154-10.htm";
            		}
        	}
        	else if (npcId == THALIA)
        	{
            		if (st.getQuestItemsCount(HIERARCHS_LETTER) > 0)
            		{
                		htmltext = "30371-01.htm";
            		}
            		else if (st.getQuestItemsCount(GRAIL_DIAGRAM) > 0)
            		{
                		htmltext = "30371-04.htm";
            		}
            		else if (st.getQuestItemsCount(PUSHKINS_LIST) > 0)
            		{
                		htmltext = "30371-05.htm";
            		}
            		else if (st.getQuestItemsCount(PURE_MITHRIL_CUP) > 0)
            		{
                		htmltext = "30371-06.htm";
                		st.set("cond", "7");
                		st.takeItems(PURE_MITHRIL_CUP, 1);
                		st.giveItems(THALIAS_LETTER1, 1);
            		}
            		else if (st.getQuestItemsCount(THALIAS_LETTER1) > 0)
            		{
                		htmltext = "30371-07.htm";
            		}
            		else if (st.getQuestItemsCount(ARKENIAS_CONTRACT) > 0)
            		{
                		htmltext = "30371-08.htm";
            		}
            		else if (st.getQuestItemsCount(STARDUST) > 0)
            		{
                		htmltext = "30371-09.htm";
            		}
            		else if (st.getQuestItemsCount(THALIAS_INSTRUCTIONS) > 0)
            		{
                		if (talker.getLevel() < 37)
                		{
                    			htmltext = "30371-12.htm";
                    			st.set("cond", "13");
                		}
                		else
                		{
                    			st.set("cond", "14");
                    			st.takeItems(THALIAS_INSTRUCTIONS, 1);
                    			st.giveItems(THALIAS_LETTER2, 1);
                		}
            		}
            		else if (st.getQuestItemsCount(THALIAS_LETTER2) > 0)
            		{
                		htmltext = "30371-14.htm";
            		}
            		else if (st.getQuestItemsCount(ISAELS_INSTRUCTIONS) > 0)
            		{
                		htmltext = "30371-15.htm";
            		}
            		else if (st.getQuestItemsCount(ISAELS_LETTER) > 0)
            		{
                		htmltext = "30371-16.htm";
                		st.set("cond", "18");
                		st.takeItems(ISAELS_LETTER, 1);
                		st.giveItems(GRAIL_OF_PURITY, 1);
            		}
            		else if (st.getQuestItemsCount(GRAIL_OF_PURITY) > 0)
            		{
                		htmltext = "30371-17.htm";
            		}
            		else if (st.getQuestItemsCount(TEARS_OF_UNICORN) > 0)
            		{
                		htmltext = "30371-18.htm";
                		st.set("cond", "20");
                		st.takeItems(TEARS_OF_UNICORN, 1);
                		st.giveItems(WATER_OF_LIFE, 1);
            		}
            		else if (st.getQuestItemsCount(WATER_OF_LIFE) > 0)
            		{
                		htmltext = "30371-19.htm";
            		}
        	}
        	else if (npcId == PUSHKIN)
        	{
            		if (st.getQuestItemsCount(GRAIL_DIAGRAM) > 0)
            		{
                		htmltext = "30300-01.htm";
            		}
            		else if (st.getQuestItemsCount(PUSHKINS_LIST) > 0)
            		{
                		htmltext = st.getInt("cond") == 5 ? "30300-08.htm" : "30300-07.htm";
            		}
            		else if (st.getQuestItemsCount(PURE_MITHRIL_CUP) > 0)
            		{
                		htmltext = "30300-11.htm";
            		}
            		else if (st.getInt("cond") > 5)
            		{
                		htmltext = "30300-12.htm";
            		}
        	}
        	else if (npcId == ARKENIA)
        	{
            		if (st.getQuestItemsCount(THALIAS_LETTER1) > 0)
            		{
                		htmltext = "30419-01.htm";
           		}
            		else if (st.getQuestItemsCount(ARKENIAS_INSTRUCTIONS) > 0 || st.getQuestItemsCount(ADONIUS_LIST) > 0)
            		{
                		htmltext = "30419-05.htm";
            		}
            		else if (st.getQuestItemsCount(ANDARIEL_SCRIPTURE_COPY) > 0)
            		{
                		htmltext = "30419-06.htm";
                		st.set("cond", "12");
                		st.takeItems(ARKENIAS_CONTRACT, 1);
                		st.takeItems(ANDARIEL_SCRIPTURE_COPY, 1);
                		st.giveItems(STARDUST, 1);
            		}
            		else if (st.getQuestItemsCount(STARDUST) > 0)
            		{
                		htmltext = "30419-07.htm";
            		}
           		else
            		{
                		htmltext = "30419-08.htm";
            		}
        	}
        	else if (npcId == ADONIUS)
        	{
            		if (st.getQuestItemsCount(ARKENIAS_INSTRUCTIONS) > 0)
            		{
                		htmltext = "30375-01.htm";
            		}
            		else if (st.getQuestItemsCount(ADONIUS_LIST) > 0)
            		{
                		if (st.getInt("cond") == 10)
                		{
                    			htmltext = "30375-04.htm";
                    			st.set("cond", "11");
                    			st.takeItems(SPIDER_ICHOR, st.getQuestItemsCount(SPIDER_ICHOR));
                    			st.takeItems(HARPYS_DOWN, st.getQuestItemsCount(HARPYS_DOWN));
                    			st.takeItems(ADONIUS_LIST, 1);
                    			st.giveItems(ANDARIEL_SCRIPTURE_COPY, 1);
                		}
                		else
                		{
                    			htmltext = "30375-03.htm";
                		}
            		}
            		else if (st.getQuestItemsCount(ANDARIEL_SCRIPTURE_COPY) > 0)
            		{
                		htmltext = "30375-05.htm";
            		}
            		else
            		{
                		htmltext = "30375-06.htm";
            		}
        	}
        	else if (npcId == ISAEL_SILVERSHADOW)
        	{
            		if (st.getQuestItemsCount(THALIAS_LETTER2) > 0)
            		{
                		htmltext = "30655-01.htm";
            		}
            		else if (st.getQuestItemsCount(ISAELS_INSTRUCTIONS) > 0)
            		{
                		if (st.getQuestItemsCount(TALINS_SPEAR_BLADE) > 0 && st.getQuestItemsCount(TALINS_SPEAR_SHAFT) > 0 && st.getQuestItemsCount(TALINS_RUBY) > 0 && st.getQuestItemsCount(TALINS_AQUAMARINE) > 0 && st.getQuestItemsCount(TALINS_AMETHYST) > 0 && st.getQuestItemsCount(TALINS_PERIDOT) > 0)
                		{
                    			htmltext = "30655-04.htm";
                    			st.set("cond", "17");
                    			st.takeItems(TALINS_SPEAR_BLADE, 1);
                    			st.takeItems(TALINS_SPEAR_SHAFT, 1);
                    			st.takeItems(TALINS_RUBY, 1);
                    			st.takeItems(TALINS_AQUAMARINE, 1);
                    			st.takeItems(TALINS_AMETHYST, 1);
                    			st.takeItems(TALINS_PERIDOT, 1);
                    			st.takeItems(ISAELS_INSTRUCTIONS, 1);
                    			st.giveItems(ISAELS_LETTER, 1);
                    			st.giveItems(TALINS_SPEAR, 1);
                		}
                		else
                		{
                    			htmltext = "30655-03.htm";
                		}
            		}
            		else if (st.getQuestItemsCount(TALINS_SPEAR) > 0 && st.getQuestItemsCount(ISAELS_LETTER) > 0)
            		{
                		htmltext = "30655-05.htm";
            		}
            		else if (st.getQuestItemsCount(GRAIL_OF_PURITY) > 0 || st.getQuestItemsCount(CAMOMILE_CHARM) > 0)
            		{
                		htmltext = "30655-06.htm";
            		}
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

        	int npcId = npc.getId();

        	if (npcId == GUARDIAN_BASILISK)
        	{
            		if (st.getQuestItemsCount(PUSHKINS_LIST) > 0 && st.getQuestItemsCount(PURE_MITHRIL_ORE) < 10)
            		{
                		if (st.getRandom(100) < 50)
                		{
                    			st.giveItems(PURE_MITHRIL_ORE, 1);
                    			if (st.getQuestItemsCount(PURE_MITHRIL_ORE) < 10)
                    			{
                        			st.playSound("ItemSound.quest_itemget");
                    			}
                    			else
                    			{
                        			st.playSound("ItemSound.quest_middle");
                        			if (st.getQuestItemsCount(WYRMS_TALON1) >= 20 && st.getQuestItemsCount(ANT_SOLDIER_ACID) >= 20)
                        			{
                            				st.set("cond", "5");
                        			}
                    			}
                		}
            		}
        	}
        	else if (npcId == WYRM)
        	{
            		if (st.getQuestItemsCount(PUSHKINS_LIST) > 0 && st.getQuestItemsCount(WYRMS_TALON1) < 20)
            		{
                		if (st.getRandom(100) < 50)
                		{
                    			st.giveItems(WYRMS_TALON1, 1);
                    			if (st.getQuestItemsCount(WYRMS_TALON1) < 20)
                    			{
                        			st.playSound("ItemSound.quest_itemget");
                    			}
                    			else
                    			{
                        			st.playSound("ItemSound.quest_middle");
                        			if (st.getQuestItemsCount(PURE_MITHRIL_ORE) >= 10 && st.getQuestItemsCount(ANT_SOLDIER_ACID) >= 20)
                        			{
                            			st.set("cond", "5");
                        			}
                    			}
                		}
            		}
        	}
        	else if (npcId == ANT_RECRUIT || npcId == ANT_PATROL || npcId == ANT_GUARD || npcId == ANT_SOLDIER || npcId == ANT_WARRIOR_CAPTAIN)
        	{
            		if (st.getQuestItemsCount(PUSHKINS_LIST) > 0 && st.getQuestItemsCount(ANT_SOLDIER_ACID) < 20)
            		{
                		int chance = 80;
                		if (npcId == ANT_SOLDIER || npcId == ANT_WARRIOR_CAPTAIN)
                		{
                    			chance = 50;
                		}
                		if (st.getRandom(100) < chance)
                		{
                    			st.giveItems(ANT_SOLDIER_ACID, 1);
                    			if (st.getQuestItemsCount(ANT_SOLDIER_ACID) < 20)
                    			{
                        			st.playSound("ItemSound.quest_itemget");
                    			}
                    			else
                    			{
                        			st.playSound("ItemSound.quest_middle");
                        			if (st.getQuestItemsCount(PURE_MITHRIL_ORE) >= 10 && st.getQuestItemsCount(WYRMS_TALON1) >= 20)
                        			{
                            				st.set("cond", "5");
                        			}
                    			}
                		}
            		}
        	}
        	else if (npcId == MARSH_SPIDER)
        	{
            		if (st.getQuestItemsCount(ADONIUS_LIST) > 0 && st.getQuestItemsCount(SPIDER_ICHOR) < 20)
            		{
                		if (st.getRandom(100) < 50)
                		{
                    			st.giveItems(SPIDER_ICHOR, 1);
                    			if (st.getQuestItemsCount(SPIDER_ICHOR) < 20)
                    			{
                        			st.playSound("ItemSound.quest_itemget");
                    			}
                    			else
                    			{
                        			st.playSound("ItemSound.quest_middle");
                        			if (st.getQuestItemsCount(HARPYS_DOWN) >= 20)
                        			{
                            				st.set("cond", "10");
                        			}
                    			}
                		}
            		}
        	}
        	else if (npcId == HARPY)
        	{
            		if (st.getQuestItemsCount(ADONIUS_LIST) > 0 && st.getQuestItemsCount(HARPYS_DOWN) < 20)
            		{
                		if (st.getRandom(100) < 50)
                		{
                    			st.giveItems(HARPYS_DOWN, 1);
                    			if (st.getQuestItemsCount(HARPYS_DOWN) < 20)
                    			{
                        			st.playSound("ItemSound.quest_itemget");
                    			}
                    			else
                    			{
                        			st.playSound("ItemSound.quest_middle");
                        			if (st.getQuestItemsCount(SPIDER_ICHOR) >= 20)
                        			{
                            				st.set("cond", "10");
                        			}
                    			}
                		}
            		}
        	}
        	else if (npcId == UNICORN_OF_EVA)
        	{
            		if (st.getQuestItemsCount(TALINS_SPEAR) > 0 && st.getQuestItemsCount(GRAIL_OF_PURITY) > 0 && st.getQuestItemsCount(TEARS_OF_UNICORN) == 0)
            		{
                		st.takeItems(GRAIL_OF_PURITY, 1);
                		st.takeItems(TALINS_SPEAR, 1);
                		st.giveItems(TEARS_OF_UNICORN, 1);
                		st.set("cond", "19");
            		}
        	}
        	else if (npcId == LETO_LIZARDMAN_SHAMAN || npcId == LETO_LIZARDMAN_OVERLORD)
        	{
            		if (st.getQuestItemsCount(ISAELS_INSTRUCTIONS) > 0 && st.getRandom(100) < 50)
            		{
                		for (int id : new int[] { TALINS_SPEAR_BLADE, TALINS_SPEAR_SHAFT, TALINS_RUBY, TALINS_AQUAMARINE, TALINS_AMETHYST })
                		{
                    			if (st.getQuestItemsCount(id) == 0)
                    			{
                        			st.giveItems(id, 1);
                        			st.playSound("ItemSound.quest_itemget");
                        			return super.onKill(npc, killer, isSummon);
                    			}
                		}
                		if (st.getQuestItemsCount(TALINS_PERIDOT) == 0)
                		{
                    			st.giveItems(TALINS_PERIDOT, 1);
                    			st.playSound("ItemSound.quest_itemget");
                    			st.set("cond", "16");
                		}
            		}
        	}
        	return super.onKill(npc, killer, isSummon);
    	}

    	public static void main(String[] args)
    	{
        	new _218_TestimonyOfLife(218, qn, "");
    	}
}