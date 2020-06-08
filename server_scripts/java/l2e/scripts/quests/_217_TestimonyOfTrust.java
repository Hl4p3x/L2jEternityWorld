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
public class _217_TestimonyOfTrust extends Quest
{
	private static final String qn = "_217_TestimonyOfTrust";

    	private static final int MARK_OF_TRUST_ID 		= 2734;
    	private static final int LETTER_TO_ELF_ID 		= 1558;
    	private static final int LETTER_TO_DARKELF_ID 		= 1556;
    	private static final int LETTER_TO_DWARF_ID 		= 2737;
    	private static final int LETTER_TO_ORC_ID 		= 2738;
    	private static final int LETTER_TO_SERESIN_ID 		= 2739;
    	private static final int SCROLL_OF_DARKELF_TRUST_ID 	= 2740;
    	private static final int SCROLL_OF_ELF_TRUST_ID 	= 2741;
    	private static final int SCROLL_OF_DWARF_TRUST_ID 	= 2742;
    	private static final int SCROLL_OF_ORC_TRUST_ID 	= 2743;
    	private static final int RECOMMENDATION_OF_HOLLIN_ID 	= 2744;
    	private static final int ORDER_OF_OZZY_ID 		= 2745;
    	private static final int BREATH_OF_WINDS_ID 		= 2746;
    	private static final int SEED_OF_VERDURE_ID 		= 2747;
    	private static final int LETTER_OF_THIFIELL_ID 		= 2748;
    	private static final int BLOOD_OF_GUARDIAN_BASILISK_ID 	= 2749;
    	private static final int GIANT_APHID_ID 		= 2750;
    	private static final int STAKATOS_FLUIDS_ID 		= 2751;
    	private static final int BASILISK_PLASMA_ID 		= 2752;
    	private static final int HONEY_DEW_ID 			= 2753;
    	private static final int STAKATO_ICHOR_ID 		= 2754;
    	private static final int ORDER_OF_CLAYTON_ID 		= 2755;
    	private static final int PARASITE_OF_LOTA_ID 		= 2756;
    	private static final int LETTER_TO_MANAKIA_ID 		= 2757;
    	private static final int LETTER_OF_MANAKIA_ID 		= 2758;
    	private static final int LETTER_TO_NICHOLA_ID 		= 2759;
    	private static final int ORDER_OF_NICHOLA_ID 		= 2760;
    	private static final int HEART_OF_PORTA_ID 		= 2761;
    	private static final int RewardExp 			= 1390298;
    	private static final int RewardSP 			= 92782;

	// NPC's
	private static final int[] NPCS = { 30191, 30031, 30154, 30358, 30464, 30515, 30531, 30565, 30621, 30657 };

	// Mob's
	private static final int[] MOBS = { 20013, 20157, 20019, 20213, 20230, 20232, 20234, 20036, 20044, 27120, 27121, 20550, 20553, 20082, 20084, 20086, 20087, 20088 };

    	public _217_TestimonyOfTrust(int questId, String name, String descr)
	{
		super(questId, name, descr);

        	addStartNpc(30191);
		for (int i : NPCS)
        		addTalkId(i);

		for (int mob : MOBS)
       			addKillId(mob);

        	questItemIds = new int[] { SCROLL_OF_DARKELF_TRUST_ID, SCROLL_OF_ELF_TRUST_ID, SCROLL_OF_DWARF_TRUST_ID, SCROLL_OF_ORC_TRUST_ID, BREATH_OF_WINDS_ID, SEED_OF_VERDURE_ID, ORDER_OF_OZZY_ID, LETTER_TO_ELF_ID, ORDER_OF_CLAYTON_ID, BASILISK_PLASMA_ID, STAKATO_ICHOR_ID, HONEY_DEW_ID, LETTER_TO_DARKELF_ID, LETTER_OF_THIFIELL_ID, LETTER_TO_SERESIN_ID, LETTER_TO_ORC_ID, LETTER_OF_MANAKIA_ID, LETTER_TO_MANAKIA_ID, PARASITE_OF_LOTA_ID, LETTER_TO_DWARF_ID, LETTER_TO_NICHOLA_ID, HEART_OF_PORTA_ID, ORDER_OF_NICHOLA_ID, RECOMMENDATION_OF_HOLLIN_ID, BLOOD_OF_GUARDIAN_BASILISK_ID, STAKATOS_FLUIDS_ID, GIANT_APHID_ID };
    	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

        	if (event.equalsIgnoreCase("30191-04.htm"))
        	{
            		htmltext = "30191-04.htm";
            		st.set("cond", "1");
            		st.set("id", "0");
            		st.setState(State.STARTED);
            		st.playSound("ItemSound.quest_accept");
            		st.giveItems(LETTER_TO_ELF_ID, 1);
            		st.giveItems(LETTER_TO_DARKELF_ID, 1);
        	}
        	else if (event.equalsIgnoreCase("30154-03.htm"))
        	{
            		st.takeItems(LETTER_TO_ELF_ID, 1);
            		st.giveItems(ORDER_OF_OZZY_ID, 1);
            		st.set("cond", "2");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30358-02.htm"))
        	{
            		st.takeItems(LETTER_TO_DARKELF_ID, 1);
            		st.giveItems(LETTER_OF_THIFIELL_ID, 1);
            		st.set("cond", "5");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30657-03.htm"))
        	{
            		if (player.getLevel() >= 38)
            		{
                		st.takeItems(LETTER_TO_SERESIN_ID, 1);
                		st.giveItems(LETTER_TO_ORC_ID, 1);
                		st.giveItems(LETTER_TO_DWARF_ID, 1);
                		st.set("cond", "12");
                		st.playSound("ItemSound.quest_middle");
            		}
            		else
                		htmltext = "30657-02.htm";
       	 	}
        	else if (event.equalsIgnoreCase("30565-02.htm"))
        	{
            		st.takeItems(LETTER_TO_ORC_ID, 1);
            		st.giveItems(LETTER_TO_MANAKIA_ID, 1);
            		st.set("cond", "13");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30515-02.htm"))
        	{
            		st.takeItems(LETTER_TO_MANAKIA_ID, 1);
            		st.set("cond", "14");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30531-02.htm"))
        	{
            		st.takeItems(LETTER_TO_DWARF_ID, 1);
            		st.giveItems(LETTER_TO_NICHOLA_ID, 1);
            		st.set("cond", "18");
            		st.playSound("ItemSound.quest_middle");
        	}
        	else if (event.equalsIgnoreCase("30621-02.htm"))
        	{
            		st.takeItems(LETTER_TO_NICHOLA_ID, 1);
            		st.giveItems(ORDER_OF_NICHOLA_ID, 1);
            		st.set("cond", "19");
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

        	if (npcId == 30191)
        	{
            		if (cond == 0)
            		{
                		if (player.getRace().ordinal() == 0)
                		{
                    			if (player.getLevel() >= 37)
                        			htmltext = "30191-03.htm";
                    			else
                    			{
                        			htmltext = "30191-01.htm";
                        			st.exitQuest(true);
                    			}
                		}
                		else
                		{
                    			htmltext = "30191-02.htm";
                    			st.exitQuest(true);
                		}
            		}
            		else if (cond == 9 && st.getQuestItemsCount(SCROLL_OF_ELF_TRUST_ID) > 0 && st.getQuestItemsCount(SCROLL_OF_DARKELF_TRUST_ID) > 0)
            		{
                		htmltext = "30191-05.htm";
                		st.takeItems(SCROLL_OF_DARKELF_TRUST_ID, 1);
                		st.takeItems(SCROLL_OF_ELF_TRUST_ID, 1);
                		st.giveItems(LETTER_TO_SERESIN_ID, 1);
                		st.set("cond", "10");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 22 && st.getQuestItemsCount(SCROLL_OF_DWARF_TRUST_ID) > 0 && st.getQuestItemsCount(SCROLL_OF_ORC_TRUST_ID) > 0)
            		{
                		htmltext = "30191-06.htm";
                		st.takeItems(SCROLL_OF_DWARF_TRUST_ID, 1);
                		st.takeItems(SCROLL_OF_ORC_TRUST_ID, 1);
                		st.giveItems(RECOMMENDATION_OF_HOLLIN_ID, 1);
                		st.set("cond", "23");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 19)
                		htmltext = "30191-07.htm";
            		else if (cond == 1)
                		htmltext = "30191-08.htm";
            		else if (cond == 8)
                		htmltext = "30191-09.htm";
        	}
        	else if (npcId == 30154)
        	{
            		if (cond == 1 && st.getQuestItemsCount(LETTER_TO_ELF_ID) > 0)
                		htmltext = "30154-01.htm";
            		else if (cond == 2 && st.getQuestItemsCount(ORDER_OF_OZZY_ID) > 0)
                		htmltext = "30154-04.htm";
            		else if (cond == 3 && st.getQuestItemsCount(BREATH_OF_WINDS_ID) > 0 && st.getQuestItemsCount(SEED_OF_VERDURE_ID) > 0)
            		{
                		htmltext = "30154-05.htm";
                		st.takeItems(BREATH_OF_WINDS_ID, 1);
                		st.takeItems(SEED_OF_VERDURE_ID, 1);
                		st.takeItems(ORDER_OF_OZZY_ID, 1);
                		st.giveItems(SCROLL_OF_ELF_TRUST_ID, 1);
                		st.set("cond", "4");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 4)
                		htmltext = "30154-06.htm";
        	}
       	 	else if (npcId == 30358)
        	{
            		if (cond == 4 && st.getQuestItemsCount(LETTER_TO_DARKELF_ID) > 0)
                		htmltext = "30358-01.htm";
            		else if (cond == 8 && st.getQuestItemsCount(STAKATO_ICHOR_ID) + st.getQuestItemsCount(HONEY_DEW_ID) + st.getQuestItemsCount(BASILISK_PLASMA_ID) == 3)
            		{
                		st.takeItems(BASILISK_PLASMA_ID, 1);
                		st.takeItems(STAKATO_ICHOR_ID, 1);
                		st.takeItems(HONEY_DEW_ID, 1);
                		st.giveItems(SCROLL_OF_DARKELF_TRUST_ID, 1);
                		st.set("cond", "9");
                		st.playSound("Itemsound.quest_middle");
                		htmltext = "30358-03.htm";
            		}
            		else if (cond == 7)
                		htmltext = "30358-04.htm";
            		else if (cond == 5)
               		 	htmltext = "30358-05.htm";
        	}
        	else if (npcId == 30464)
        	{
            		if (cond == 5 && st.getQuestItemsCount(LETTER_OF_THIFIELL_ID) > 0)
            		{
                		htmltext = "30464-01.htm";
                		st.takeItems(LETTER_OF_THIFIELL_ID, 1);
                		st.giveItems(ORDER_OF_CLAYTON_ID, 1);
                		st.set("cond", "6");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 6 && st.getQuestItemsCount(ORDER_OF_CLAYTON_ID) > 0 && st.getQuestItemsCount(STAKATO_ICHOR_ID) + st.getQuestItemsCount(HONEY_DEW_ID) + st.getQuestItemsCount(BASILISK_PLASMA_ID) < 3)
                		htmltext = "30464-02.htm";
            		else if (cond == 7 && st.getQuestItemsCount(ORDER_OF_CLAYTON_ID) > 0 && st.getQuestItemsCount(STAKATO_ICHOR_ID) + st.getQuestItemsCount(HONEY_DEW_ID) + st.getQuestItemsCount(BASILISK_PLASMA_ID) == 3)
            		{
                		st.takeItems(ORDER_OF_CLAYTON_ID, 1);
                		st.set("cond", "8");
                		st.playSound("Itemsound.quest_middle");
                		htmltext = "30464-03.htm";
            		}
        	}
        	else if (npcId == 30657)
        	{
            		if ((cond == 10 || cond == 11) && st.getQuestItemsCount(LETTER_TO_SERESIN_ID) > 0 && player.getLevel() >= 38)
                		htmltext = "30657-01.htm";
            		else if ((cond == 10 || cond == 11) && player.getLevel() < 38)
            		{
                		htmltext = "30657-02.htm";
                		if (cond == 10)
                    			st.set("cond", "11");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 18)
                		htmltext = "30657-05.htm";
        	}
        	else if (npcId == 30565)
        	{
            		if (cond == 12 && st.getQuestItemsCount(LETTER_TO_ORC_ID) > 0)
                		htmltext = "30565-01.htm";
            		else if (cond == 13)
                		htmltext = "30565-03.htm";
            		else if (cond == 16)
            		{
                		htmltext = "30565-04.htm";
                		st.takeItems(LETTER_OF_MANAKIA_ID, 1);
                		st.giveItems(SCROLL_OF_ORC_TRUST_ID, 1);
                		st.set("cond", "17");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond >= 17)
                		htmltext = "30565-05.htm";
        	}
        	else if (npcId == 30515)
        	{
            		if (cond == 13 && st.getQuestItemsCount(LETTER_TO_MANAKIA_ID) > 0)
                		htmltext = "30515-01.htm";
            		else if (cond == 14 && st.getQuestItemsCount(PARASITE_OF_LOTA_ID) < 10)
                		htmltext = "30515-03.htm";
            		else if (cond == 15 && st.getQuestItemsCount(PARASITE_OF_LOTA_ID) == 10)
            		{
                		htmltext = "30515-04.htm";
                		st.takeItems(PARASITE_OF_LOTA_ID, -1);
                		st.giveItems(LETTER_OF_MANAKIA_ID, 1);
                		st.set("cond", "16");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 16)
                		htmltext = "30515-05.htm";
        	}
        	else if (npcId == 30531)
        	{
            		if (cond == 17 && st.getQuestItemsCount(LETTER_TO_DWARF_ID) > 0)
                		htmltext = "30531-01.htm";
            		else if (cond == 18)
                		htmltext = "30531-03.htm";
            		else if (cond == 21)
            		{
                		htmltext = "30531-04.htm";
                		st.giveItems(SCROLL_OF_DWARF_TRUST_ID, 1);
                		st.set("cond", "22");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 22)
                		htmltext = "30531-05.htm";
        	}
        	else if (npcId == 30621)
        	{
            		if (cond == 18 && st.getQuestItemsCount(LETTER_TO_NICHOLA_ID) > 0)
                		htmltext = "30621-01.htm";
            		else if (cond == 19 && st.getQuestItemsCount(HEART_OF_PORTA_ID) < 1)
                		htmltext = "30621-03.htm";
            		else if (cond == 20 && st.getQuestItemsCount(HEART_OF_PORTA_ID) >= 1)
            		{
                		htmltext = "30621-04.htm";
                		st.takeItems(HEART_OF_PORTA_ID, 1);
                		st.takeItems(ORDER_OF_NICHOLA_ID, 1);
                		st.set("cond", "21");
                		st.playSound("Itemsound.quest_middle");
            		}
            		else if (cond == 21)
                		htmltext = "30621-05.htm";
        	}
        	else if (npcId == 30031 && cond == 23 && st.getQuestItemsCount(RECOMMENDATION_OF_HOLLIN_ID) > 0)
        	{
            		htmltext = "30031-01.htm";
            		st.takeItems(RECOMMENDATION_OF_HOLLIN_ID, 1);
            		st.giveItems(MARK_OF_TRUST_ID, 1);
            		st.addExpAndSp(RewardExp, RewardSP);
			st.giveItems(57,252212);
		        st.giveItems(7562,96);
            		st.playSound("ItemSound.quest_finish");
            		st.unset("cond");
            		st.setState(State.COMPLETED);
            		st.exitQuest(false);
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

        	if (npcId == 20036 || npcId == 20044)
        	{
            		if (cond == 2 && st.getQuestItemsCount(BREATH_OF_WINDS_ID) == 0)
            		{
                		st.set("id", String.valueOf(st.getInt("id") + 1));
                		if (getRandom(100) < (st.getInt("id") * 33))
                		{
                    			st.addSpawn(27120, npc.getX(), npc.getY(), npc.getZ(), 60000);
                    			st.playSound("Itemsound.quest_before_battle");
                		}
            		}
        	}
        	else if (npcId == 20013 || npcId == 20019)
        	{
            		if (cond == 2 && st.getQuestItemsCount(SEED_OF_VERDURE_ID) == 0)
            		{
                		st.set("id", String.valueOf(st.getInt("id") + 1));
                		if (getRandom(100) < (st.getInt("id") * 33))
                		{
                    			st.addSpawn(27121, npc.getX(), npc.getY(), npc.getZ(), 60000);
                    			st.playSound("Itemsound.quest_before_battle");
                		}
            		}
        	}
        	else if (npcId == 27120)
        	{
            		if (cond == 2 && st.getQuestItemsCount(BREATH_OF_WINDS_ID) == 0)
            		{
                		if (st.getQuestItemsCount(SEED_OF_VERDURE_ID) > 0)
                		{
                    			st.giveItems(BREATH_OF_WINDS_ID, 1);
                    			st.set("cond", "3");
                    			st.playSound("Itemsound.quest_middle");
                		}
                		else
                		{
                    			st.giveItems(BREATH_OF_WINDS_ID, 1);
                    			st.playSound("Itemsound.quest_itemget");
                		}
            		}
        	}
        	else if (npcId == 27121)
        	{
            		if (cond == 2 && st.getQuestItemsCount(SEED_OF_VERDURE_ID) == 0)
            		{
                		if (st.getQuestItemsCount(BREATH_OF_WINDS_ID) > 0)
                		{
                    			st.giveItems(SEED_OF_VERDURE_ID, 1);
                    			st.set("cond", "3");
                    			st.playSound("Itemsound.quest_middle");
                		}
                		else
                		{
                    			st.giveItems(SEED_OF_VERDURE_ID, 1);
                    			st.playSound("Itemsound.quest_itemget");
                		}
            		}
        	}
        	else if (npcId == 20550)
        	{
            		if (cond == 6 && st.getQuestItemsCount(BLOOD_OF_GUARDIAN_BASILISK_ID) < 10 && st.getQuestItemsCount(ORDER_OF_CLAYTON_ID) > 0 && st.getQuestItemsCount(BASILISK_PLASMA_ID) == 0)
            		{
                		if (st.getQuestItemsCount(BLOOD_OF_GUARDIAN_BASILISK_ID) == 9)
                		{
                    			st.takeItems(BLOOD_OF_GUARDIAN_BASILISK_ID, -1);
                    			st.giveItems(BASILISK_PLASMA_ID, 1);
                    			if (st.getQuestItemsCount(STAKATO_ICHOR_ID) + st.getQuestItemsCount(BASILISK_PLASMA_ID) + st.getQuestItemsCount(HONEY_DEW_ID) == 3)
                        			st.set("cond", "7");
                    			st.playSound("Itemsound.quest_middle");
                		}
                		else
                		{
                    			st.giveItems(BLOOD_OF_GUARDIAN_BASILISK_ID, 1);
                    			st.playSound("Itemsound.quest_itemget");
                		}
            		}
        	}
        	else if (npcId == 20157 || npcId == 20230 || npcId == 20232 || npcId == 20234)
        	{
            		if (cond == 6 && st.getQuestItemsCount(STAKATOS_FLUIDS_ID) < 10 && st.getQuestItemsCount(ORDER_OF_CLAYTON_ID) > 0 && st.getQuestItemsCount(STAKATO_ICHOR_ID) == 0)
            		{
                		if (st.getQuestItemsCount(STAKATOS_FLUIDS_ID) == 9)
                		{
                    			st.takeItems(STAKATOS_FLUIDS_ID, -1);
                    			st.giveItems(STAKATO_ICHOR_ID, 1);
                    			if (st.getQuestItemsCount(STAKATO_ICHOR_ID) + st.getQuestItemsCount(BASILISK_PLASMA_ID) + st.getQuestItemsCount(HONEY_DEW_ID) == 3)
                        			st.set("cond", "7");
                    			st.playSound("Itemsound.quest_middle");
                		}
                		else
                		{
                    			st.giveItems(STAKATOS_FLUIDS_ID, 1);
                    			st.playSound("Itemsound.quest_itemget");
                		}
            		}
        	}
        	else if (npcId == 20082 || npcId == 20086 || npcId == 20087 || npcId == 20084 || npcId == 20088)
        	{
            		if (cond == 6 && st.getQuestItemsCount(GIANT_APHID_ID) < 10 && st.getQuestItemsCount(ORDER_OF_CLAYTON_ID) > 0 && st.getQuestItemsCount(HONEY_DEW_ID) == 0)
            		{
                		if (st.getQuestItemsCount(GIANT_APHID_ID) == 9)
                		{
                    			st.takeItems(GIANT_APHID_ID, -1);
                    			st.giveItems(HONEY_DEW_ID, 1);
                    			if (st.getQuestItemsCount(STAKATO_ICHOR_ID) + st.getQuestItemsCount(BASILISK_PLASMA_ID) + st.getQuestItemsCount(HONEY_DEW_ID) == 3)
                        			st.set("cond", "7");
                    			st.playSound("Itemsound.quest_middle");
                		}
                		else
                		{
                    			st.giveItems(GIANT_APHID_ID, 1);
                    			st.playSound("Itemsound.quest_itemget");
                		}
            		}
        	}
        	else if (npcId == 20553)
        	{
            		if (cond == 14 && st.getQuestItemsCount(PARASITE_OF_LOTA_ID) < 10 && getRandom(100) < 50)
            		{
                		if (st.getQuestItemsCount(PARASITE_OF_LOTA_ID) == 9)
                		{
                    			st.giveItems(PARASITE_OF_LOTA_ID, 1);
                    			st.set("cond", "15");
                    			st.playSound("Itemsound.quest_middle");
                		}
                		else
                		{
                    			st.giveItems(PARASITE_OF_LOTA_ID, 1);
                    			st.playSound("Itemsound.quest_itemget");
                		}
            		}
        	}
        	else if (npcId == 20213 && cond == 19 && st.getQuestItemsCount(HEART_OF_PORTA_ID) < 1)
        	{
            		st.giveItems(HEART_OF_PORTA_ID, 1);
            		st.set("cond", "20");
            		st.playSound("Itemsound.quest_middle");
        	}
        	return null;
    	}

	public static void main(String[] args)
	{
		new _217_TestimonyOfTrust(217, qn, "");
	}
}