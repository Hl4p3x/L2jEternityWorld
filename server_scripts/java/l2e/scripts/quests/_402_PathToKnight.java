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

import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 04.10.2012
 * Based on L2J Eternity-World
 */
public class _402_PathToKnight extends Quest
{
    	private static final String qn = "_402_PathToKnight";

    	// Npc
    	private static final int SIR_KLAUS_VASPER = 30417;
    	private static final int BIOTIN = 30031;
    	private static final int LEVIAN = 30037;
    	private static final int GILBERT = 30039;
    	private static final int RAYMOND = 30289;
    	private static final int SIR_COLLIN_WINDAWOOD = 30311;
    	private static final int BATHIS = 30332;
    	private static final int BEZIQUE = 30379;
    	private static final int SIR_ARON_TANFORD = 30653;

    	private static final int[] TALKERS =
    	{
        	SIR_KLAUS_VASPER, BIOTIN, LEVIAN, GILBERT, RAYMOND, SIR_COLLIN_WINDAWOOD,
        	BATHIS, BEZIQUE, SIR_ARON_TANFORD
    	};

    	// Mobs
    	private static final int BUGBEAR_RAIDER = 20775;
    	private static final int UNDEAD_PRIEST = 27024;
    	private static final int VENOMOUS_SPIDER = 20038;
    	private static final int ARACHNID_TRACKER = 20043;
    	private static final int ARACHNID_PREDATOR = 20050;
    	private static final int LANGK_LIZARDMAN = 20030;
    	private static final int LANGK_LIZARDMAN_SCOUT = 20027;
    	private static final int LANGK_LIZARDMAN_WARRIOR = 20024;
    	private static final int GIANT_SPIDER = 20103;
    	private static final int TALON_SPIDER = 20106;
    	private static final int BLADE_SPIDER = 20108;
    	private static final int SILENT_HORROR = 20404;

    	private static final int[] MOBS =
    	{
        	BUGBEAR_RAIDER, UNDEAD_PRIEST, VENOMOUS_SPIDER, ARACHNID_TRACKER,
        	ARACHNID_PREDATOR, LANGK_LIZARDMAN, LANGK_LIZARDMAN_SCOUT, LANGK_LIZARDMAN_WARRIOR, GIANT_SPIDER,
        	TALON_SPIDER, BLADE_SPIDER, SILENT_HORROR
    	};

    	// Quest items
    	private static final int MARK_OF_ESQUIRE = 1271;
    	private static final int COIN_OF_LORDS1 = 1162;
    	private static final int COIN_OF_LORDS2 = 1163;
    	private static final int COIN_OF_LORDS3 = 1164;
    	private static final int COIN_OF_LORDS4 = 1165;
    	private static final int COIN_OF_LORDS5 = 1166;
    	private static final int COIN_OF_LORDS6 = 1167;
    	private static final int GLUDIO_GUARDS_MARK1 = 1168;
    	private static final int BUGBEAR_NECKLACE = 1169;
    	private static final int EINHASAD_CHURCH_MARK1 = 1170;
    	private static final int EINHASAD_CRUCIFIX = 1171;
    	private static final int GLUDIO_GUARDS_MARK2 = 1172;
    	private static final int POISON_SPIDER_LEG1 = 1173;
    	private static final int EINHASAD_CHURCH_MARK2 = 1174;
    	private static final int LIZARDMAN_TOTEM = 1175;
    	private static final int GLUDIO_GUARDS_MARK3 = 1176;
    	private static final int GIANT_SPIDER_HUSK = 1177;
    	private static final int EINHASAD_CHURCH_MARK3 = 1178;
    	private static final int HORRIBLE_SKULL = 1179;

    	private static final int[] QUESTITEMS =
    	{
        	COIN_OF_LORDS1, COIN_OF_LORDS2, COIN_OF_LORDS3, COIN_OF_LORDS4,
        	COIN_OF_LORDS5, COIN_OF_LORDS6, GLUDIO_GUARDS_MARK1, BUGBEAR_NECKLACE, EINHASAD_CHURCH_MARK1,
        	EINHASAD_CRUCIFIX, GLUDIO_GUARDS_MARK2, POISON_SPIDER_LEG1, EINHASAD_CHURCH_MARK2, LIZARDMAN_TOTEM,
        	GLUDIO_GUARDS_MARK3, GIANT_SPIDER_HUSK, EINHASAD_CHURCH_MARK3, HORRIBLE_SKULL
    	};

    	// Chances in %
    	private static Map<Integer, int[]> DROPLIST = new HashMap<>();

    	static
    	{
        	DROPLIST.put(BUGBEAR_RAIDER, new int[]
                {
                    	GLUDIO_GUARDS_MARK1, BUGBEAR_NECKLACE, 10, 100
                });
        	DROPLIST.put(UNDEAD_PRIEST, new int[]
                {
                    	EINHASAD_CHURCH_MARK1, EINHASAD_CRUCIFIX, 12, 100
                });
        	DROPLIST.put(VENOMOUS_SPIDER, new int[]
                {
                    	GLUDIO_GUARDS_MARK2, POISON_SPIDER_LEG1, 20, 100
                });
        	DROPLIST.put(ARACHNID_TRACKER, new int[]
                {
                    	GLUDIO_GUARDS_MARK2, POISON_SPIDER_LEG1, 20, 100
                });
        	DROPLIST.put(ARACHNID_PREDATOR, new int[]
                {
                    	GLUDIO_GUARDS_MARK2, POISON_SPIDER_LEG1, 20, 100
                });
        	DROPLIST.put(LANGK_LIZARDMAN, new int[]
                {
                    	EINHASAD_CHURCH_MARK2, LIZARDMAN_TOTEM, 20, 50
                });
        	DROPLIST.put(LANGK_LIZARDMAN_SCOUT, new int[]
                {
                    	EINHASAD_CHURCH_MARK2, LIZARDMAN_TOTEM, 20, 100
                });
        	DROPLIST.put(LANGK_LIZARDMAN_WARRIOR, new int[]
                {
                    	EINHASAD_CHURCH_MARK2, LIZARDMAN_TOTEM, 20, 100
                });
        	DROPLIST.put(GIANT_SPIDER, new int[]
                {
                    	GLUDIO_GUARDS_MARK3, GIANT_SPIDER_HUSK, 20, 40
                });
        	DROPLIST.put(TALON_SPIDER, new int[]
                {
                    	GLUDIO_GUARDS_MARK3, GIANT_SPIDER_HUSK, 20, 40
                });
        	DROPLIST.put(BLADE_SPIDER, new int[]
                {
                    	GLUDIO_GUARDS_MARK3, GIANT_SPIDER_HUSK, 20, 40
                });
        	DROPLIST.put(SILENT_HORROR, new int[]
                {
                    	EINHASAD_CHURCH_MARK3, HORRIBLE_SKULL, 10, 100
                });
    	}

    	// Reward
    	private static final int SWORD_OF_RITUAL = 1161;

    	public _402_PathToKnight(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	addStartNpc(SIR_KLAUS_VASPER);

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

        	int classId = player.getClassId().getId();
        	int level = player.getLevel();
        	long squire = st.getQuestItemsCount(MARK_OF_ESQUIRE);
        	long coin1 = st.getQuestItemsCount(COIN_OF_LORDS1);
        	long coin2 = st.getQuestItemsCount(COIN_OF_LORDS2);
        	long coin3 = st.getQuestItemsCount(COIN_OF_LORDS3);
        	long coin4 = st.getQuestItemsCount(COIN_OF_LORDS4);
        	long coin5 = st.getQuestItemsCount(COIN_OF_LORDS5);
        	long coin6 = st.getQuestItemsCount(COIN_OF_LORDS6);
        	long guards_mark1 = st.getQuestItemsCount(GLUDIO_GUARDS_MARK1);
        	long guards_mark2 = st.getQuestItemsCount(GLUDIO_GUARDS_MARK2);
        	long guards_mark3 = st.getQuestItemsCount(GLUDIO_GUARDS_MARK3);
        	long church_mark1 = st.getQuestItemsCount(EINHASAD_CHURCH_MARK1);
        	long church_mark2 = st.getQuestItemsCount(EINHASAD_CHURCH_MARK2);
        	long church_mark3 = st.getQuestItemsCount(EINHASAD_CHURCH_MARK3);

        	if (event.equalsIgnoreCase("30417-02a.htm"))
        	{
            		if (classId == 0x00)
            		{
                		if (level >= 18)
                		{
                    			htmltext = st.getQuestItemsCount(SWORD_OF_RITUAL) > 0 ? "30417-04.htm" : "30417-05.htm";
                		}
                		else
                		{
                    			htmltext = "30417-02.htm";
                    			st.exitQuest(true);
                		}
            		}
            		else if (classId != 0x04)
            		{
                		htmltext = "30417-03.htm";
                		st.exitQuest(true);
            		}
        	}
        	else if (event.equalsIgnoreCase("30417-08.htm"))
        	{
            		if (st.getInt("cond") == 0 && classId == 0x00 && level >= 18)
            		{
                		st.set("id", "0");
                		st.set("cond", "1");
                		st.setState(State.STARTED);
                		st.playSound("ItemSound.quest_accept");
                		st.giveItems(MARK_OF_ESQUIRE, 1);
            		}
            		else
            		{
                		htmltext = getNoQuestMsg(player);
            		}
        	}
        	else if (event.equalsIgnoreCase("30332-02.htm"))
        	{
            		if (squire > 0 && guards_mark1 == 0 && coin1 == 0)
            		{
                		st.giveItems(GLUDIO_GUARDS_MARK1, 1);
            		}
            		else
            		{
                		htmltext = getNoQuestMsg(player);
            		}
        	}
        	else if (event.equalsIgnoreCase("30289-03.htm"))
        	{
            		if (squire > 0 && church_mark1 == 0 && coin2 == 0)
            		{
                		st.giveItems(EINHASAD_CHURCH_MARK1, 1);
            		}
            		else
            		{
                		htmltext = getNoQuestMsg(player);
            		}
        	}
        	else if (event.equalsIgnoreCase("30379-02.htm"))
        	{
            		if (squire > 0 && guards_mark2 == 0 && coin3 == 0)
            		{
                		st.giveItems(GLUDIO_GUARDS_MARK2, 1);
            		}
            		else
            		{
                		htmltext = getNoQuestMsg(player);
            		}
        	}
        	else if (event.equalsIgnoreCase("30037-02.htm"))
        	{
            		if (squire > 0 && church_mark2 == 0 && coin4 == 0)
            		{
                		st.giveItems(EINHASAD_CHURCH_MARK2, 1);
            		}
            		else
            		{
                		htmltext = getNoQuestMsg(player);
            		}
        	}
        	else if (event.equalsIgnoreCase("30039-02.htm"))
        	{
            		if (squire > 0 && guards_mark3 == 0 && coin5 == 0)
            		{
                		st.giveItems(GLUDIO_GUARDS_MARK3, 1);
            		}
            		else
            		{
                		htmltext = getNoQuestMsg(player);
            		}
        	}
        	else if (event.equalsIgnoreCase("30031-02.htm"))
        	{
            		if (squire > 0 && church_mark3 == 0 && coin6 == 0)
            		{
                		st.giveItems(EINHASAD_CHURCH_MARK3, 1);
            		}
            		else
            		{
                		htmltext = getNoQuestMsg(player);
            		}
        	}
        	else if (event.equalsIgnoreCase("30417-13.htm"))
        	{
            		if (squire > 0 && (coin1 + coin2 + coin3 + coin4 + coin5 + coin6) >= 3)
            		{
                		st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                		st.set("cond", "0");
                		for (int item : questItemIds)
                		{
                    			st.takeItems(item, -1);
                		}
                		st.takeItems(MARK_OF_ESQUIRE, -1);
                		st.addExpAndSp(3200, 2450);
                		st.giveItems(SWORD_OF_RITUAL, 1);
                		st.exitQuest(false);
                		st.playSound("ItemSound.quest_finish");
           		}
            		else
            		{
                		htmltext = getNoQuestMsg(player);
            		}
        	}
        	else if (event.equalsIgnoreCase("30417-14.htm"))
        	{
            		if (squire > 0 && (coin1 + coin2 + coin3 + coin4 + coin5 + coin6) >= 3)
            		{
                		st.set("cond", "0");
                		for (int item : questItemIds)
                		{
                    			st.takeItems(item, -1);
                		}
                		st.takeItems(MARK_OF_ESQUIRE, -1);
                		st.addExpAndSp(3200, 2450);
                		st.giveItems(SWORD_OF_RITUAL, 1);
                		st.exitQuest(false);
                		st.playSound("ItemSound.quest_finish");
            		}
            		else
            		{
                		htmltext = getNoQuestMsg(player);
            		}
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

        	if (npcId != SIR_KLAUS_VASPER && id != State.STARTED)
        	{
            		return htmltext;
        	}

        	long squire = st.getQuestItemsCount(MARK_OF_ESQUIRE);
        	long coin1 = st.getQuestItemsCount(COIN_OF_LORDS1);
        	long coin2 = st.getQuestItemsCount(COIN_OF_LORDS2);
        	long coin3 = st.getQuestItemsCount(COIN_OF_LORDS3);
        	long coin4 = st.getQuestItemsCount(COIN_OF_LORDS4);
        	long coin5 = st.getQuestItemsCount(COIN_OF_LORDS5);
        	long coin6 = st.getQuestItemsCount(COIN_OF_LORDS6);
        	long guards_mark1 = st.getQuestItemsCount(GLUDIO_GUARDS_MARK1);
        	long guards_mark2 = st.getQuestItemsCount(GLUDIO_GUARDS_MARK2);
        	long guards_mark3 = st.getQuestItemsCount(GLUDIO_GUARDS_MARK3);
        	long church_mark1 = st.getQuestItemsCount(EINHASAD_CHURCH_MARK1);
        	long church_mark2 = st.getQuestItemsCount(EINHASAD_CHURCH_MARK2);
        	long church_mark3 = st.getQuestItemsCount(EINHASAD_CHURCH_MARK3);
        	long coin_count = coin1 + coin2 + coin3 + coin4 + coin5 + coin6;
        	int cond = st.getInt("cond");

        	if (id == State.COMPLETED)
        	{
            		htmltext = getAlreadyCompletedMsg(talker);
        	}
        	else if (npcId == SIR_KLAUS_VASPER)
        	{
            		if (cond == 0)
            		{
                		htmltext = "30417-01.htm";
            		}
            		else if (cond == 1 && squire > 0)
            		{
                		if (coin_count < 3)
                		{
                    			htmltext = "30417-09.htm";
                		}
                		else if (coin_count == 3)
                		{
                    			htmltext = "30417-10.htm";
                		}
                		else if (coin_count < 6)
                		{
                    			htmltext = "30417-11.htm";
                		}
                		else if (coin_count == 6)
                		{
                    			htmltext = "30417-12.htm";
                    			st.set("cond", "0");
                    			for (int item : questItemIds)
                    			{
                        			st.takeItems(item, -1);
                    			}
                    			st.takeItems(MARK_OF_ESQUIRE, -1);
                    			st.addExpAndSp(3200, 2450);
                    			st.giveItems(SWORD_OF_RITUAL, 1);
                    			st.exitQuest(false);
                    			st.playSound("ItemSound.quest_finish");
                		}
            		}
        	}
        	else if (npcId == BATHIS && cond == 1 && squire > 0)
        	{
            		if (guards_mark1 == 0 && coin1 == 0)
            		{
                		htmltext = "30332-01.htm";
			}
            		else if (guards_mark1 > 0)
            		{
                		if (st.getQuestItemsCount(BUGBEAR_NECKLACE) < 10)
                		{
                    			htmltext = "30332-03.htm";
                		}
                		else
                		{
                    			htmltext = "30332-04.htm";
                    			st.takeItems(BUGBEAR_NECKLACE, -1);
                    			st.takeItems(GLUDIO_GUARDS_MARK1, 1);
                    			st.giveItems(COIN_OF_LORDS1, 1);
                		}
            		}
            		else if (coin1 > 0)
            		{
                		htmltext = "30332-05.htm";
            		}
        	}
        	else if (npcId == RAYMOND && cond == 1 && squire > 0)
        	{
            		if (church_mark1 == 0 && coin2 == 0)
            		{
                		htmltext = "30289-01.htm";
            		}
            		else if (church_mark1 > 0)
            		{
                		if (st.getQuestItemsCount(EINHASAD_CRUCIFIX) < 12)
                		{
                    			htmltext = "30289-04.htm";
                		}
                		else
                		{
                    			htmltext = "30289-05.htm";
                    			st.takeItems(EINHASAD_CRUCIFIX, -1);
                    			st.takeItems(EINHASAD_CHURCH_MARK1, 1);
                    			st.giveItems(COIN_OF_LORDS2, 1);
                		}
            		}
            		else if (coin2 > 0)
            		{
                		htmltext = "30289-06.htm";
            		}
        	}
        	else if (npcId == BEZIQUE && cond == 1 && squire > 0)
        	{
            		if (coin3 == 0 && guards_mark2 == 0)
            		{
                		htmltext = "30379-01.htm";
            		}
            		else if (guards_mark2 > 0)
            		{
                		if (st.getQuestItemsCount(POISON_SPIDER_LEG1) < 20)
                		{
                    			htmltext = "30379-03.htm";
                		}
                		else
                		{
                    			htmltext = "30379-04.htm";
                    			st.takeItems(POISON_SPIDER_LEG1, -1);
                    			st.takeItems(GLUDIO_GUARDS_MARK2, 1);
                    			st.giveItems(COIN_OF_LORDS3, 1);
                		}
            		}
            		else if (coin3 > 0)
            		{
                		htmltext = "30379-05.htm";
            		}
        	}
        	else if (npcId == LEVIAN && cond == 1 && squire > 0)
        	{
            		if (coin4 == 0 && church_mark2 == 0)
            		{
                		htmltext = "30037-01.htm";
           		}
            		else if (church_mark2 > 0)
            		{
                		if (st.getQuestItemsCount(LIZARDMAN_TOTEM) < 20)
                		{
                    			htmltext = "30037-03.htm";
                		}
                		else
                		{
                    			htmltext = "30037-04.htm";
                    			st.takeItems(LIZARDMAN_TOTEM, -1);
                    			st.takeItems(EINHASAD_CHURCH_MARK2, 1);
                    			st.giveItems(COIN_OF_LORDS4, 1);
                		}
            		}
            		else if (coin4 > 0)
            		{
                		htmltext = "3007-05.htm";
            		}
        	}
        	else if (npcId == GILBERT && cond == 1 && squire > 0)
        	{
            		if (guards_mark3 == 0 && coin5 == 0)
            		{
                		htmltext = "30039-01.htm";
            		}
            		else if (guards_mark3 > 0)
            		{
                		if (st.getQuestItemsCount(GIANT_SPIDER_HUSK) < 20)
                		{
                   		 	htmltext = "30039-03.htm";
                		}
				else
                		{
                    			htmltext = "30039-04.htm";
                    			st.takeItems(GIANT_SPIDER_HUSK, -1);
                    			st.takeItems(GLUDIO_GUARDS_MARK3, 1);
                    			st.giveItems(COIN_OF_LORDS5, 1);
                		}
            		}
            		else if (coin5 > 0)
            		{
                		htmltext = "30039-05.htm";
            		}
        	}
        	else if (npcId == BIOTIN && cond == 1 && squire > 0)
        	{
            		if (church_mark3 == 0 && coin6 == 0)
            		{
                		htmltext = "30031-01.htm";
            		}
            		else if (church_mark3 > 0)
            		{
                		if (st.getQuestItemsCount(HORRIBLE_SKULL) < 10)
                		{
                    			htmltext = "30031-03.htm";
                		}
                		else
                		{
                    			htmltext = "30031-04.htm";
                    			st.takeItems(HORRIBLE_SKULL, -1);
                    			st.takeItems(EINHASAD_CHURCH_MARK3, 1);
                    			st.giveItems(COIN_OF_LORDS6, 1);
                		}
            		}
            		else if (coin6 > 0)
            		{
                		htmltext = "30031-05.htm";
            		}
        	}
        	else if (npcId == SIR_COLLIN_WINDAWOOD && cond == 1 && squire > 0)
        	{
            		htmltext = "30311-01.htm";
        	}
        	else if (npcId == SIR_ARON_TANFORD && cond == 1 && squire > 0)
        	{
            		htmltext = "30653-01.htm";
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

        	if (st.getInt("cond") > 0)
        	{
            		int npcId = npc.getId();
            		int item_required = DROPLIST.get(npcId)[0];
            		int item = DROPLIST.get(npcId)[1];
            		int max = DROPLIST.get(npcId)[2];
            		int chance = DROPLIST.get(npcId)[3];

            		if (st.getQuestItemsCount(item_required) > 0 && st.getQuestItemsCount(item) < max && st.getRandom(100) < chance)
            		{
                		st.giveItems(item, 1);
                		st.playSound(st.getQuestItemsCount(item) == max ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            		}
        	}
        	return super.onKill(npc, killer, isSummon);
    	}

    	public static void main(String[] args)
    	{
        	new _402_PathToKnight(402, qn, "");
    	}
}