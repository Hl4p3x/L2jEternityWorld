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
public class _408_PathToElvenwizard extends Quest
{
    	private static final String qn = "_408_PathToElvenwizard";

    	// Npc
    	private static final int ROSELLA = 30414;
    	private static final int GREENIS = 30157;
    	private static final int THALIA = 30371;
    	private static final int NORTHWIND = 30423;

    	private static final int[] TALKERS =
    	{
        	ROSELLA, GREENIS, THALIA, NORTHWIND
    	};

    	// Mobs
    	private static final int DRYAD_ELDER = 20019;
    	private static final int PINCER_SPIDER = 20466;
    	private static final int SUKAR_WERERAT_LEADER = 20047;

    	private static final int[] MOBS =
    	{
        	DRYAD_ELDER, PINCER_SPIDER, SUKAR_WERERAT_LEADER
    	};

    	// Quest items
    	private static final int ROGELLIAS_LETTER = 1218;
    	private static final int RED_DOWN = 1219;
    	private static final int MAGICAL_POWERS_RUBY = 1220;
    	private static final int PURE_AQUAMARINE = 1221;
    	private static final int APPETIZING_APPLE = 1222;
    	private static final int GOLD_LEAVES = 1223;
    	private static final int IMMORTAL_LOVE = 1224;
    	private static final int AMETHYST = 1225;
    	private static final int NOBILITY_AMETHYST = 1226;
    	private static final int FERTILITY_PERIDOT = 1229;
    	private static final int CHARM_OF_GRAIN = 1272;
    	private static final int SAP_OF_WORLD_TREE = 1273;
    	private static final int LUCKY_POTPOURI = 1274;

    	// Reward
    	private static final int ETERNITY_DIAMOND = 1230;

    	private static final int[] QUESTITEMS =
    	{
        	ROGELLIAS_LETTER, RED_DOWN, MAGICAL_POWERS_RUBY, PURE_AQUAMARINE, APPETIZING_APPLE, GOLD_LEAVES, IMMORTAL_LOVE, AMETHYST, NOBILITY_AMETHYST, FERTILITY_PERIDOT, CHARM_OF_GRAIN, SAP_OF_WORLD_TREE, LUCKY_POTPOURI
    	};

    	public _408_PathToElvenwizard(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	addStartNpc(ROSELLA);

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
            		if (player.getClassId().getId() != 0x19)
            		{
                		htmltext = player.getClassId().getId() == 0x1a ? "30414-02a.htm" : "30414-03.htm";
            		}
            		else
            		{
                		if (player.getLevel() < 18)
                		{
                    			htmltext = "30414-04.htm";
                		}
                		else
                		{
                    			if (st.getQuestItemsCount(ETERNITY_DIAMOND) != 0)
                    			{
                        			htmltext = "30414-05.htm";
                    			}
                    			else
                    			{
                        			st.set("cond", "1");
                        			st.setState(State.STARTED);
                        			st.playSound("ItemSound.quest_accept");
                        			if (st.getQuestItemsCount(FERTILITY_PERIDOT) == 0)
                        			{
                            				st.giveItems(FERTILITY_PERIDOT, 1);
                        			}
                        			htmltext = "30414-06.htm";
                    			}
                		}
            		}
        	}
        	else if (event.equalsIgnoreCase("408_1"))
        	{
            		if (st.getInt("cond") != 0 && st.getQuestItemsCount(MAGICAL_POWERS_RUBY) != 0)
            		{
                		htmltext = "30414-10.htm";
            		}
            		else if (st.getInt("cond") != 0 && st.getQuestItemsCount(MAGICAL_POWERS_RUBY) == 0 && st.getQuestItemsCount(FERTILITY_PERIDOT) != 0)
            		{
                		if (st.getQuestItemsCount(ROGELLIAS_LETTER) == 0)
                		{
                    			st.giveItems(ROGELLIAS_LETTER, 1);
                		}
                		htmltext = "30414-07.htm";
            		}
        	}
        	else if (event.equalsIgnoreCase("408_4"))
        	{
            		if (st.getInt("cond") != 0 && st.getQuestItemsCount(ROGELLIAS_LETTER) != 0)
            		{
                		st.takeItems(ROGELLIAS_LETTER, st.getQuestItemsCount(ROGELLIAS_LETTER));
                		if (st.getQuestItemsCount(CHARM_OF_GRAIN) == 0)
                		{
                    			st.giveItems(CHARM_OF_GRAIN, 1);
                		}
                		htmltext = "30157-02.htm";
            		}
        	}
        	else if (event.equalsIgnoreCase("408_2"))
        	{
            		if (st.getInt("cond") != 0 && st.getQuestItemsCount(PURE_AQUAMARINE) != 0)
            		{
                		htmltext = "30414-13.htm";
            		}
            		else if (st.getInt("cond") != 0 && st.getQuestItemsCount(PURE_AQUAMARINE) == 0 && st.getQuestItemsCount(FERTILITY_PERIDOT) != 0)
            		{
                		if (st.getQuestItemsCount(APPETIZING_APPLE) == 0)
                		{
                    			st.giveItems(APPETIZING_APPLE, 1);
                		}
                		htmltext = "30414-14.htm";
            		}
        	}
        	else if (event.equalsIgnoreCase("408_5"))
        	{
            		if (st.getInt("cond") != 0 && st.getQuestItemsCount(APPETIZING_APPLE) != 0)
            		{
                		st.takeItems(APPETIZING_APPLE, st.getQuestItemsCount(APPETIZING_APPLE));
                		if (st.getQuestItemsCount(SAP_OF_WORLD_TREE) == 0)
                		{
                    			st.giveItems(SAP_OF_WORLD_TREE, 1);
                		}
                		htmltext = "30371-02.htm";
            		}
        	}
        	else if (event.equalsIgnoreCase("408_3"))
        	{
            		if (st.getInt("cond") != 0 && st.getQuestItemsCount(NOBILITY_AMETHYST) != 0)
            		{
                		htmltext = "30414-17.htm";
            		}
            		else if (st.getInt("cond") != 0 && st.getQuestItemsCount(NOBILITY_AMETHYST) == 0 && st.getQuestItemsCount(FERTILITY_PERIDOT) != 0)
            		{
                		if (st.getQuestItemsCount(IMMORTAL_LOVE) == 0)
                		{
                    			st.giveItems(IMMORTAL_LOVE, 1);
                		}
                		htmltext = "30414-18.htm";
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

        	if (npcId != ROSELLA && id != State.STARTED)
        	{
            		return htmltext;
        	}

        	if (npcId == ROSELLA && st.getInt("cond") == 0)
        	{
            		htmltext = "30414-01.htm";
        	}
        	else if (npcId == ROSELLA && st.getInt("cond") != 0 && st.getQuestItemsCount(ROGELLIAS_LETTER) == 0 && st.getQuestItemsCount(APPETIZING_APPLE) == 0 && st.getQuestItemsCount(IMMORTAL_LOVE) == 0 && st.getQuestItemsCount(CHARM_OF_GRAIN) == 0 && st.getQuestItemsCount(SAP_OF_WORLD_TREE) == 0 && st.getQuestItemsCount(LUCKY_POTPOURI) == 0 && st.getQuestItemsCount(FERTILITY_PERIDOT) != 0 && (st.getQuestItemsCount(MAGICAL_POWERS_RUBY) == 0 || st.getQuestItemsCount(NOBILITY_AMETHYST) == 0 || st.getQuestItemsCount(PURE_AQUAMARINE) == 0))
        	{
            		htmltext = "30414-11.htm";
        	}
        	else if (npcId == ROSELLA && st.getInt("cond") != 0 && st.getQuestItemsCount(ROGELLIAS_LETTER) != 0)
        	{
            		htmltext = "30414-08.htm";
        	}
        	else if (npcId == GREENIS && st.getInt("cond") != 0 && st.getQuestItemsCount(ROGELLIAS_LETTER) != 0)
        	{
            		htmltext = "30157-01.htm";
        	}
        	else if (npcId == GREENIS && st.getInt("cond") != 0 && st.getQuestItemsCount(CHARM_OF_GRAIN) != 0 && st.getQuestItemsCount(RED_DOWN) < 5)
        	{
            		htmltext = "30157-03.htm";
        	}
        	else if (npcId == GREENIS && st.getInt("cond") != 0 && st.getQuestItemsCount(CHARM_OF_GRAIN) != 0 && st.getQuestItemsCount(RED_DOWN) >= 5)
        	{
            		st.takeItems(RED_DOWN, st.getQuestItemsCount(RED_DOWN));
            		st.takeItems(CHARM_OF_GRAIN, st.getQuestItemsCount(CHARM_OF_GRAIN));
            		if (st.getQuestItemsCount(MAGICAL_POWERS_RUBY) == 0)
            		{
                		st.giveItems(MAGICAL_POWERS_RUBY, 1);
            		}
            		htmltext = "30157-04.htm";
        	}
        	else if (npcId == ROSELLA && st.getInt("cond") != 0 && st.getQuestItemsCount(CHARM_OF_GRAIN) != 0 && st.getQuestItemsCount(RED_DOWN) < 5)
        	{
            		htmltext = "30414-09.htm";
        	}
        	else if (npcId == ROSELLA && st.getInt("cond") != 0 && st.getQuestItemsCount(CHARM_OF_GRAIN) != 0 && st.getQuestItemsCount(RED_DOWN) >= 5)
        	{
            		htmltext = "30414-25.htm";
        	}
        	else if (npcId == ROSELLA && st.getInt("cond") != 0 && st.getQuestItemsCount(APPETIZING_APPLE) != 0)
        	{
            		htmltext = "30414-15.htm";
        	}
        	else if (npcId == THALIA && st.getInt("cond") != 0 && st.getQuestItemsCount(APPETIZING_APPLE) != 0)
        	{
            		htmltext = "30371-01.htm";
        	}
        	else if (npcId == THALIA && st.getInt("cond") != 0 && st.getQuestItemsCount(SAP_OF_WORLD_TREE) != 0 && st.getQuestItemsCount(GOLD_LEAVES) < 5)
        	{
            		htmltext = "30371-03.htm";
        	}
        	else if (npcId == THALIA && st.getInt("cond") != 0 && st.getQuestItemsCount(SAP_OF_WORLD_TREE) != 0 && st.getQuestItemsCount(GOLD_LEAVES) >= 5)
        	{
            		st.takeItems(GOLD_LEAVES, st.getQuestItemsCount(GOLD_LEAVES));
            		st.takeItems(SAP_OF_WORLD_TREE, st.getQuestItemsCount(SAP_OF_WORLD_TREE));
            		if (st.getQuestItemsCount(PURE_AQUAMARINE) == 0)
            		{
                		st.giveItems(PURE_AQUAMARINE, 1);
            		}
            		htmltext = "30371-04.htm";
        	}
        	else if (npcId == ROSELLA && st.getInt("cond") != 0 && st.getQuestItemsCount(SAP_OF_WORLD_TREE) != 0 && st.getQuestItemsCount(GOLD_LEAVES) < 5)
        	{
            		htmltext = "30414-16.htm";
        	}
        	else if (npcId == ROSELLA && st.getInt("cond") != 0 && st.getQuestItemsCount(CHARM_OF_GRAIN) != 0 && st.getQuestItemsCount(GOLD_LEAVES) >= 5)
        	{
            		htmltext = "30414-26.htm";
        	}
        	else if (npcId == ROSELLA && st.getInt("cond") != 0 && st.getQuestItemsCount(IMMORTAL_LOVE) != 0)
        	{
            		htmltext = "30414-19.htm";
        	}
        	else if (npcId == NORTHWIND && st.getInt("cond") != 0 && st.getQuestItemsCount(IMMORTAL_LOVE) != 0)
        	{
            		st.takeItems(IMMORTAL_LOVE, st.getQuestItemsCount(IMMORTAL_LOVE));
            		if (st.getQuestItemsCount(LUCKY_POTPOURI) == 0)
            		{
                		st.giveItems(LUCKY_POTPOURI, 1);
            		}
            		htmltext = "30423-01.htm";
        	}
        	else if (npcId == NORTHWIND && st.getInt("cond") != 0 && st.getQuestItemsCount(LUCKY_POTPOURI) != 0 && st.getQuestItemsCount(AMETHYST) < 2)
        	{
           		htmltext = "30423-02.htm";
        	}
        	else if (npcId == 30423 && st.getInt("cond") != 0 && st.getQuestItemsCount(LUCKY_POTPOURI) != 0 && st.getQuestItemsCount(AMETHYST) >= 2)
        	{
            		st.takeItems(AMETHYST, st.getQuestItemsCount(AMETHYST));
            		st.takeItems(LUCKY_POTPOURI, st.getQuestItemsCount(LUCKY_POTPOURI));
            		if (st.getQuestItemsCount(NOBILITY_AMETHYST) == 0)
            		{
                		st.giveItems(NOBILITY_AMETHYST, 1);
            		}
            		htmltext = "30423-03.htm";
        	}
        	else if (npcId == ROSELLA && st.getInt("cond") != 0 && st.getQuestItemsCount(LUCKY_POTPOURI) != 0 && st.getQuestItemsCount(AMETHYST) < 2)
        	{
            		htmltext = "30414-20.htm";
        	}
        	else if (npcId == ROSELLA && st.getInt("cond") != 0 && st.getQuestItemsCount(LUCKY_POTPOURI) != 0 && st.getQuestItemsCount(AMETHYST) >= 2)
        	{
            		htmltext = "30414-27.htm";
        	}
        	else if (npcId == ROSELLA && st.getInt("cond") != 0 && st.getQuestItemsCount(ROGELLIAS_LETTER) == 0 && st.getQuestItemsCount(APPETIZING_APPLE) == 0 && st.getQuestItemsCount(IMMORTAL_LOVE) == 0 && st.getQuestItemsCount(CHARM_OF_GRAIN) == 0 && st.getQuestItemsCount(SAP_OF_WORLD_TREE) == 0 && st.getQuestItemsCount(LUCKY_POTPOURI) == 0 && st.getQuestItemsCount(FERTILITY_PERIDOT) != 0 && st.getQuestItemsCount(MAGICAL_POWERS_RUBY) != 0 && st.getQuestItemsCount(NOBILITY_AMETHYST) != 0 && st.getQuestItemsCount(PURE_AQUAMARINE) != 0)
        	{
            		st.takeItems(MAGICAL_POWERS_RUBY, st.getQuestItemsCount(MAGICAL_POWERS_RUBY));
            		st.takeItems(PURE_AQUAMARINE, st.getQuestItemsCount(PURE_AQUAMARINE));
            		st.takeItems(NOBILITY_AMETHYST, st.getQuestItemsCount(NOBILITY_AMETHYST));
            		st.takeItems(FERTILITY_PERIDOT, st.getQuestItemsCount(FERTILITY_PERIDOT));
            		String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
            		if (isFinished.equalsIgnoreCase(""))
            		{
                		st.addExpAndSp(228064, 3210);
            		}
            		if (st.getQuestItemsCount(ETERNITY_DIAMOND) == 0)
            		{
                		st.giveItems(ETERNITY_DIAMOND, 1);
            		}
            		st.saveGlobalQuestVar("1ClassQuestFinished", "1");
            		st.set("cond", "0");
            		st.exitQuest(false);
            		st.playSound("ItemSound.quest_finish");
            		htmltext = "30414-24.htm";
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

        	if (npcId == PINCER_SPIDER)
        	{
            		st.set("id", "0");
            		if (st.getInt("cond") != 0 && st.getQuestItemsCount(CHARM_OF_GRAIN) != 0 && st.getQuestItemsCount(RED_DOWN) < 5 && st.getRandom(100) < 70)
            		{
                		st.giveItems(RED_DOWN, 1);
                		st.playSound(st.getQuestItemsCount(RED_DOWN) == 5 ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            		}
        	}
        	else if (npcId == DRYAD_ELDER)
        	{
            		st.set("id", "0");
            		if (st.getInt("cond") != 0 && st.getQuestItemsCount(SAP_OF_WORLD_TREE) != 0 && st.getQuestItemsCount(GOLD_LEAVES) < 5 && st.getRandom(100) < 40)
            		{
                		st.giveItems(GOLD_LEAVES, 1);
                		st.playSound(st.getQuestItemsCount(GOLD_LEAVES) == 5 ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            		}
        	}
        	else if (npcId == SUKAR_WERERAT_LEADER)
        	{
            		st.set("id", "0");
            		if (st.getInt("cond") != 0 && st.getQuestItemsCount(LUCKY_POTPOURI) != 0 && st.getQuestItemsCount(AMETHYST) < 2 && st.getRandom(100) < 40)
            		{
                		st.giveItems(AMETHYST, 1);
                		st.playSound(st.getQuestItemsCount(AMETHYST) == 2 ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            		}
        	}
        	return super.onKill(npc, killer, isSummon);
    	}

    	public static void main(String[] args)
    	{
        	new _408_PathToElvenwizard(408, qn, "");
    	}
}