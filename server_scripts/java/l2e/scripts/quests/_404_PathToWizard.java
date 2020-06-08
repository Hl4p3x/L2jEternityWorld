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
public class _404_PathToWizard extends Quest
{
    	private static final String qn = "_404_PathToWizard";

    	// Npc
    	private static final int PARINA = 30391;
    	private static final int EARTH_SNAKE = 30409;
    	private static final int WASTELAND_LIZARDMAN = 30410;
    	private static final int FLAME_SALAMANDER = 30411;
    	private static final int WIND_SYLPH = 30412;
    	private static final int WATER_UNDINE = 30413;

    	private static final int[] TALKERS =
    	{
       	 	PARINA, EARTH_SNAKE, WASTELAND_LIZARDMAN, FLAME_SALAMANDER, WIND_SYLPH, WATER_UNDINE
    	};

    	// Mobs
    	private static final int RED_BEAR = 20021;
    	private static final int RATMAN_WARRIOR = 20359;
    	private static final int WATER_SEER = 27030;

    	private static final int[] MOBS =
    	{
        	RED_BEAR, RATMAN_WARRIOR, WATER_SEER
    	};

    	// Quest items
    	private static final int MAP_OF_LUSTER = 1280;
    	private static final int KEY_OF_FLAME = 1281;
    	private static final int FLAME_EARING = 1282;
    	private static final int BROKEN_BRONZE_MIRROR = 1283;
    	private static final int WIND_FEATHER = 1284;
    	private static final int WIND_BANGEL = 1285;
    	private static final int RAMAS_DIARY = 1286;
    	private static final int SPARKLE_PEBBLE = 1287;
    	private static final int WATER_NECKLACE = 1288;
    	private static final int RUST_GOLD_COIN = 1289;
    	private static final int RED_SOIL = 1290;
    	private static final int EARTH_RING = 1291;

    	private static final int[] QUESTITEMS =
    	{
        	MAP_OF_LUSTER, KEY_OF_FLAME, FLAME_EARING, BROKEN_BRONZE_MIRROR, WIND_FEATHER, WIND_BANGEL, RAMAS_DIARY, SPARKLE_PEBBLE, WATER_NECKLACE, RUST_GOLD_COIN, RED_SOIL, EARTH_RING
    	};

    	// Reward
    	private static final int BEAD_OF_SEASON = 1292;

    	public _404_PathToWizard(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	addStartNpc(PARINA);

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
            		st.set("id", "0");
            		if (player.getClassId().getId() == 0x0a)
            		{
                		if (player.getLevel() >= 18)
                		{
                    			if (st.getQuestItemsCount(BEAD_OF_SEASON) > 0)
                    			{
                        			htmltext = "30391-03.htm";
                    			}
                    			else
                    			{
                        			st.set("cond", "1");
                        			st.setState(State.STARTED);
                        			st.playSound("ItemSound.quest_accept");
                        			htmltext = "30391-08.htm";
                    			}
                		}
                		else
                		{
                    			htmltext = "30391-02.htm";
                		}
            		}
            		else
            		{
                		htmltext = player.getClassId().getId() == 0x0b ? "30391-02a.htm" : "30391-01.htm";
            		}
        	}
        	else if (event.equalsIgnoreCase("30410_1"))
        	{
            		if (st.getQuestItemsCount(WIND_FEATHER) == 0)
            		{
                		st.giveItems(WIND_FEATHER, 1);
                		st.set("cond", "6");
                		htmltext = "30410-03.htm";
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

        	if (npcId != PARINA && id != State.STARTED)
        	{
            		return htmltext;
        	}

        	if (npcId == PARINA && st.getInt("cond") == 0)
        	{
            		htmltext = "30391-04.htm";
        	}
        	else if (npcId == PARINA && st.getInt("cond") != 0 && (st.getQuestItemsCount(FLAME_EARING) == 0 || st.getQuestItemsCount(WIND_BANGEL) == 0 || st.getQuestItemsCount(WATER_NECKLACE) == 0 || st.getQuestItemsCount(EARTH_RING) == 0))
        	{
            		htmltext = "30391-05.htm";
        	}
        	else if (npcId == FLAME_SALAMANDER && st.getInt("cond") != 0 && st.getQuestItemsCount(MAP_OF_LUSTER) == 0 && st.getQuestItemsCount(FLAME_EARING) == 0)
        	{
            		if (st.getQuestItemsCount(MAP_OF_LUSTER) == 0)
            		{
                		st.giveItems(MAP_OF_LUSTER, 1);
            		}
            		st.set("cond", "2");
            		htmltext = "30411-01.htm";
        	}
        	else if (npcId == FLAME_SALAMANDER && st.getInt("cond") != 0 && st.getQuestItemsCount(MAP_OF_LUSTER) != 0 && st.getQuestItemsCount(KEY_OF_FLAME) == 0)
        	{
            		htmltext = "30411-02.htm";
        	}
        	else if (npcId == FLAME_SALAMANDER && st.getInt("cond") != 0 && st.getQuestItemsCount(MAP_OF_LUSTER) != 0 && st.getQuestItemsCount(KEY_OF_FLAME) != 0)
        	{
            		st.takeItems(KEY_OF_FLAME, st.getQuestItemsCount(KEY_OF_FLAME));
            		st.takeItems(MAP_OF_LUSTER, st.getQuestItemsCount(MAP_OF_LUSTER));
            		if (st.getQuestItemsCount(FLAME_EARING) == 0)
            		{
                		st.giveItems(FLAME_EARING, 1);
            		}
            		st.set("cond", "4");
            		htmltext = "30411-03.htm";
        	}
        	else if (npcId == FLAME_SALAMANDER && st.getInt("cond") != 0 && st.getQuestItemsCount(FLAME_EARING) != 0)
        	{
            		htmltext = "30411-04.htm";
        	}
        	else if (npcId == WIND_SYLPH && st.getInt("cond") != 0 && st.getQuestItemsCount(FLAME_EARING) != 0 && st.getQuestItemsCount(BROKEN_BRONZE_MIRROR) == 0 && st.getQuestItemsCount(WIND_BANGEL) == 0)
        	{
            		if (st.getQuestItemsCount(BROKEN_BRONZE_MIRROR) == 0)
            		{
                		st.giveItems(BROKEN_BRONZE_MIRROR, 1);
            		}
            		st.set("cond", "5");
            		htmltext = "30412-01.htm";
        	}
        	else if (npcId == WIND_SYLPH && st.getInt("cond") != 0 && st.getQuestItemsCount(BROKEN_BRONZE_MIRROR) != 0 && st.getQuestItemsCount(WIND_FEATHER) == 0)
        	{
            		htmltext = "30412-02.htm";
        	}
        	else if (npcId == WIND_SYLPH && st.getInt("cond") != 0 && st.getQuestItemsCount(BROKEN_BRONZE_MIRROR) != 0 && st.getQuestItemsCount(WIND_FEATHER) != 0)
        	{
            		st.takeItems(WIND_FEATHER, st.getQuestItemsCount(WIND_FEATHER));
            		st.takeItems(BROKEN_BRONZE_MIRROR, st.getQuestItemsCount(BROKEN_BRONZE_MIRROR));
            		if (st.getQuestItemsCount(WIND_BANGEL) == 0)
            		{
                		st.giveItems(WIND_BANGEL, 1);
            		}
            		st.set("cond", "7");
            		htmltext = "30412-03.htm";
        	}
        	else if (npcId == WIND_SYLPH && st.getInt("cond") != 0 && st.getQuestItemsCount(WIND_BANGEL) != 0)
        	{
            		htmltext = "30412-04.htm";
        	}
        	else if (npcId == WASTELAND_LIZARDMAN && st.getInt("cond") != 0 && st.getQuestItemsCount(BROKEN_BRONZE_MIRROR) != 0 && st.getQuestItemsCount(WIND_FEATHER) == 0)
        	{
            		htmltext = "30410-01.htm";
        	}
        	else if (npcId == WASTELAND_LIZARDMAN && st.getInt("cond") != 0 && st.getQuestItemsCount(BROKEN_BRONZE_MIRROR) != 0 && st.getQuestItemsCount(WIND_FEATHER) != 0)
        	{
            		htmltext = "30410-04.htm";
        	}
        	else if (npcId == WATER_UNDINE && st.getInt("cond") != 0 && st.getQuestItemsCount(WIND_BANGEL) != 0 && st.getQuestItemsCount(RAMAS_DIARY) == 0 && st.getQuestItemsCount(WATER_NECKLACE) == 0)
        	{
            		if (st.getQuestItemsCount(RAMAS_DIARY) == 0)
            		{
                		st.giveItems(RAMAS_DIARY, 1);
            		}
            		st.set("cond", "8");
            		htmltext = "30413-01.htm";
        	}
        	else if (npcId == WATER_UNDINE && st.getInt("cond") != 0 && st.getQuestItemsCount(RAMAS_DIARY) != 0 && st.getQuestItemsCount(SPARKLE_PEBBLE) < 2)
        	{
            		htmltext = "30413-02.htm";
        	}
        	else if (npcId == WATER_UNDINE && st.getInt("cond") != 0 && st.getQuestItemsCount(RAMAS_DIARY) != 0 && st.getQuestItemsCount(SPARKLE_PEBBLE) >= 2)
        	{
            		st.takeItems(SPARKLE_PEBBLE, st.getQuestItemsCount(SPARKLE_PEBBLE));
            		st.takeItems(RAMAS_DIARY, st.getQuestItemsCount(RAMAS_DIARY));
            		if (st.getQuestItemsCount(WATER_NECKLACE) == 0)
            		{
                		st.giveItems(WATER_NECKLACE, 1);
            		}
            		st.set("cond", "10");
            		htmltext = "30413-03.htm";
        	}
        	else if (npcId == WATER_UNDINE && st.getInt("cond") != 0 && st.getQuestItemsCount(WATER_NECKLACE) != 0)
        	{
            		htmltext = "30413-04.htm";
        	}
        		else if (npcId == EARTH_SNAKE && st.getInt("cond") != 0 && st.getQuestItemsCount(WATER_NECKLACE) != 0 && st.getQuestItemsCount(RUST_GOLD_COIN) == 0 && st.getQuestItemsCount(EARTH_RING) == 0)
        		{
            		if (st.getQuestItemsCount(RUST_GOLD_COIN) == 0)
            		{
                		st.giveItems(RUST_GOLD_COIN, 1);
            		}
            		st.set("cond", "11");
            		htmltext = "30409-01.htm";
        	}
        	else if (npcId == EARTH_SNAKE && st.getInt("cond") != 0 && st.getQuestItemsCount(RUST_GOLD_COIN) != 0 && st.getQuestItemsCount(RED_SOIL) == 0)
        	{
            		htmltext = "30409-02.htm";
        	}
        	else if (npcId == 30409 && st.getInt("cond") != 0 && st.getQuestItemsCount(RUST_GOLD_COIN) != 0 && st.getQuestItemsCount(RED_SOIL) != 0)
        	{
            		st.takeItems(RED_SOIL, st.getQuestItemsCount(RED_SOIL));
            		st.takeItems(RUST_GOLD_COIN, st.getQuestItemsCount(RUST_GOLD_COIN));
            		if (st.getQuestItemsCount(EARTH_RING) == 0)
            		{
                		st.giveItems(EARTH_RING, 1);
            		}
            		st.set("cond", "13");
            		htmltext = "30409-03.htm";
        	}
        	else if (npcId == EARTH_SNAKE && st.getInt("cond") != 0 && st.getQuestItemsCount(EARTH_RING) != 0)
        	{
            		htmltext = "30409-03.htm";
        	}
        	else if (npcId == PARINA && st.getInt("cond") != 0 && st.getQuestItemsCount(FLAME_EARING) != 0 && st.getQuestItemsCount(WIND_BANGEL) != 0 && st.getQuestItemsCount(WATER_NECKLACE) != 0 && st.getQuestItemsCount(EARTH_RING) != 0)
        	{
            		st.set("cond", "0");
            		st.saveGlobalQuestVar("1ClassQuestFinished", "1");
            		st.takeItems(FLAME_EARING, st.getQuestItemsCount(FLAME_EARING));
            		st.takeItems(WIND_BANGEL, st.getQuestItemsCount(WIND_BANGEL));
            		st.takeItems(WATER_NECKLACE, st.getQuestItemsCount(WATER_NECKLACE));
            		st.takeItems(EARTH_RING, st.getQuestItemsCount(EARTH_RING));
            		st.addExpAndSp(228064, 3520);
            		if (st.getQuestItemsCount(BEAD_OF_SEASON) == 0)
            		{
                		st.giveItems(BEAD_OF_SEASON, 1);
            		}
            		st.exitQuest(false);
            		st.playSound("ItemSound.quest_finish");
            		htmltext = "30391-06.htm";
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

        	if (npcId == RATMAN_WARRIOR)
        	{
            		st.set("id", "0");
            		if (st.getInt("cond") == 2)
            		{
                		st.giveItems(KEY_OF_FLAME, 1);
                		st.playSound("ItemSound.quest_middle");
                		st.set("cond", "3");
            		}
        	}
        	else if (npcId == WATER_SEER)
        	{
            		st.set("id", "0");
            		if (st.getInt("cond") == 8 && st.getQuestItemsCount(SPARKLE_PEBBLE) < 2)
            		{
                		st.giveItems(SPARKLE_PEBBLE, 1);
                		if (st.getQuestItemsCount(SPARKLE_PEBBLE) == 2)
                		{
                    			st.playSound("ItemSound.quest_middle");
                    			st.set("cond", "9");
                		}
                		else
                		{
                    			st.playSound("ItemSound.quest_itemget");
                		}
            		}
        	}
        	else if (npcId == RED_BEAR)
        	{
            		st.set("id", "0");
            		if (st.getInt("cond") == 11)
            		{
                		st.giveItems(RED_SOIL, 1);
                		st.playSound("ItemSound.quest_middle");
                		st.set("cond", "12");
            		}
        	}
        	return super.onKill(npc, killer, isSummon);
    	}

    	public static void main(String[] args)
    	{
        	new _404_PathToWizard(404, qn, "");
    	}
}