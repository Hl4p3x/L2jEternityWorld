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
 * Created by LordWinter 04.10.2012
 * Based on L2J Eternity-World
 */
public class _407_PathToElvenScout extends Quest
{
    	private static final String qn = "_407_PathToElvenScout";

    	// Npc
    	private static final int REISA = 30328;
    	private static final int BABENCO = 30334;
    	private static final int MORETTI = 30337;
    	private static final int PRIAS = 30426;

    	private static final int[] TALKERS =
    	{
        	REISA, BABENCO, MORETTI, PRIAS
    	};

    	// Mobs
    	private static final int OL_MAHUM_SENTRY = 27031;
    	private static final int OL_MAHUM_PATROL = 20053;

    	private static final int[] MOBS =
    	{
        	OL_MAHUM_SENTRY, OL_MAHUM_PATROL
    	};

    	// Quest items
    	private static final int REORIA_LETTER2 = 1207;
    	private static final int PRIGUNS_TEAR_LETTER1 = 1208;
    	private static final int PRIGUNS_TEAR_LETTER2 = 1209;
    	private static final int PRIGUNS_TEAR_LETTER3 = 1210;
    	private static final int PRIGUNS_TEAR_LETTER4 = 1211;
    	private static final int MORETTIS_HERB = 1212;
    	private static final int MORETTIS_LETTER = 1214;
    	private static final int PRIGUNS_LETTER = 1215;
    	private static final int HONORARY_GUARD = 1216;
    	private static final int RUSTED_KEY = 1293;

    	// Reward
    	private static final int REORIA_RECOMMENDATION = 1217;

    	private static final int[] QUESTITEMS =
    	{
        	REORIA_LETTER2, PRIGUNS_TEAR_LETTER1, PRIGUNS_TEAR_LETTER2, PRIGUNS_TEAR_LETTER3, PRIGUNS_TEAR_LETTER4, MORETTIS_HERB, MORETTIS_LETTER, PRIGUNS_LETTER, HONORARY_GUARD, RUSTED_KEY
    	};

    	public _407_PathToElvenScout(int questId, String name, String descr)
    	{
    		super(questId, name, descr);

        	addStartNpc(REISA);

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
            		if (player.getClassId().getId() == 0x12)
            		{
                		if (player.getLevel() >= 18)
                		{
                    			if (st.getQuestItemsCount(REORIA_RECOMMENDATION) > 0)
                    			{
                        			htmltext = "30328-04.htm";
                    			}
                    			else
                    			{
                        			st.giveItems(REORIA_LETTER2, 1);
                        			st.set("cond", "1");
                        			st.setState(State.STARTED);
                        			st.playSound("ItemSound.quest_accept");
                        			htmltext = "30328-05.htm";
                    			}
                		}
                		else
                		{
                    			htmltext = "30328-03.htm";
                		}
            		}
            		else
            		{
                		htmltext = player.getClassId().getId() == 0x16 ? "30328-02a.htm" : "30328-02.htm";
            		}
        	}
        	else if (event.equalsIgnoreCase("30337_1"))
        	{
            		st.takeItems(REORIA_LETTER2, 1);
            		st.set("cond", "2");
            		st.playSound("ItemSound.quest_middle");
            		htmltext = "30337-03.htm";
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
        	int cond = st.getInt("cond");

        	if (npcId != REISA && id != State.STARTED)
        	{
            		return htmltext;
        	}

        	if (npcId == REISA && cond == 0)
        	{
            		htmltext = "30328-01.htm";
        	}
        	else if (npcId == REISA && cond > 0 && st.getQuestItemsCount(REORIA_LETTER2) > 0)
        	{
            		htmltext = "30328-06.htm";
        	}
        	else if (npcId == REISA && cond > 0 && st.getQuestItemsCount(REORIA_LETTER2) == 0 && st.getQuestItemsCount(HONORARY_GUARD) == 0)
        	{
            		htmltext = "30328-08.htm";
        	}
        	else if (npcId == MORETTI && cond > 0 && st.getQuestItemsCount(REORIA_LETTER2) > 0 && st.getQuestItemsCount(PRIGUNS_TEAR_LETTER1) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER2) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER3) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER4) == 0)
        	{
            		htmltext = "30337-01.htm";
        	}
        	else if (npcId == MORETTI && st.getQuestItemsCount(MORETTIS_LETTER) < 1 && st.getQuestItemsCount(PRIGUNS_LETTER) == 0 && st.getQuestItemsCount(HONORARY_GUARD) == 0)
        	{
            		if (st.getQuestItemsCount(PRIGUNS_TEAR_LETTER1) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER2) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER3) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER4) < 1)
            		{
                		htmltext = "30337-04.htm";
            		}
            		else if (st.getQuestItemsCount(PRIGUNS_TEAR_LETTER1) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER2) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER3) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER4) > 0 && st.getQuestItemsCount(PRIGUNS_TEAR_LETTER1) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER2) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER3) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER4) < 4)
            		{
                		htmltext = "30337-05.htm";
            		}
            		else
            		{
                		st.takeItems(PRIGUNS_TEAR_LETTER1, 1);
                		st.takeItems(PRIGUNS_TEAR_LETTER2, 1);
                		st.takeItems(PRIGUNS_TEAR_LETTER3, 1);
                		st.takeItems(PRIGUNS_TEAR_LETTER4, 1);
                		st.giveItems(MORETTIS_HERB, 1);
                		st.giveItems(MORETTIS_LETTER, 1);
                		st.set("cond", "4");
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30337-06.htm";
            		}
        	}
        	else if (npcId == BABENCO && cond > 0)
        	{
            		htmltext = "30334-01.htm";
        	}
       	 	else if (npcId == PRIAS && cond > 0 && st.getQuestItemsCount(MORETTIS_LETTER) > 0 && st.getQuestItemsCount(MORETTIS_HERB) > 0)
        	{
            		if (st.getQuestItemsCount(RUSTED_KEY) < 1)
            		{
                		st.set("cond", "5");
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30426-01.htm";
            		}
            		else
            		{
                		st.takeItems(RUSTED_KEY, 1);
                		st.takeItems(MORETTIS_HERB, 1);
                		st.takeItems(MORETTIS_LETTER, 1);
                		st.giveItems(PRIGUNS_LETTER, 1);
                		st.set("cond", "7");
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30426-02.htm";
            		}
        	}
        	else if (npcId == PRIAS && cond > 0 && st.getQuestItemsCount(PRIGUNS_LETTER) > 0)
        	{
            		htmltext = "30426-04.htm";
        	}
        	else if (npcId == MORETTI && cond > 0 && st.getQuestItemsCount(PRIGUNS_LETTER) > 0)
        	{
            		if (st.getQuestItemsCount(MORETTIS_HERB) > 0)
            		{
                		htmltext = "30337-09.htm";
            		}
            		else
            		{
                		st.takeItems(PRIGUNS_LETTER, 1);
                		st.giveItems(HONORARY_GUARD, 1);
                		st.set("cond", "8");
                		st.playSound("ItemSound.quest_middle");
                		htmltext = "30337-07.htm";
            		}
        	}
        	else if (npcId == MORETTI && cond > 0 && st.getQuestItemsCount(HONORARY_GUARD) > 0)
        	{
            		htmltext = "30337-08.htm";
        	}
        	else if (npcId == REISA && cond > 0 && st.getQuestItemsCount(HONORARY_GUARD) > 0)
        	{
            		st.takeItems(HONORARY_GUARD, 1);
            		String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
            		if (isFinished.equalsIgnoreCase(""))
            		{
                		st.addExpAndSp(160267, 1910);
            		}
            		st.giveItems(REORIA_RECOMMENDATION, 1);
            		st.saveGlobalQuestVar("1ClassQuestFinished", "1");
            		st.set("cond", "0");
            		talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
            		st.exitQuest(false);
            		st.playSound("ItemSound.quest_finish");
            		htmltext = "30328-07.htm";
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
        	int cond = st.getInt("cond");

        	if (npcId == OL_MAHUM_PATROL)
        	{
            		st.set("id", "0");
            		if (cond > 0 && st.getQuestItemsCount(PRIGUNS_TEAR_LETTER1) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER2) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER3) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER4) < 4)
            		{
                		if (st.getQuestItemsCount(PRIGUNS_TEAR_LETTER1) < 1)
                		{
                    			st.giveItems(PRIGUNS_TEAR_LETTER1, 1);
                    			if (st.getQuestItemsCount(PRIGUNS_TEAR_LETTER1) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER2) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER3) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER4) == 4)
                    			{
                        			st.playSound("ItemSound.quest_middle");
                        			st.set("cond", "3");
                    			}
                    			else
                    			{
                        			st.playSound("ItemSound.quest_itemget");
                    			}
                		}
                		else
                		{
                    			if (st.getQuestItemsCount(PRIGUNS_TEAR_LETTER2) < 1)
                    			{
                        			st.giveItems(PRIGUNS_TEAR_LETTER2, 1);
                        			if (st.getQuestItemsCount(PRIGUNS_TEAR_LETTER1) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER2) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER3) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER4) == 4)
                        			{
                            				st.playSound("ItemSound.quest_middle");
                            				st.set("cond", "3");
                        			}
                        			else
                        			{
                            				st.playSound("ItemSound.quest_itemget");
                        			}
                    			}
                    			else
                    			{
                        			if (st.getQuestItemsCount(PRIGUNS_TEAR_LETTER3) < 1)
                        			{
                            				st.giveItems(PRIGUNS_TEAR_LETTER3, 1);
                            				if (st.getQuestItemsCount(PRIGUNS_TEAR_LETTER1) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER2) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER3) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER4) == 4)
                            				{
                                				st.playSound("ItemSound.quest_middle");
                                				st.set("cond", "3");
                            				}
                            				else
                            				{
                                				st.playSound("ItemSound.quest_itemget");
                            				}
                        			}
                        			else
                        			{
                            				if (st.getQuestItemsCount(PRIGUNS_TEAR_LETTER4) < 1)
                            				{
                                				st.giveItems(PRIGUNS_TEAR_LETTER4, 1);
                                				if (st.getQuestItemsCount(PRIGUNS_TEAR_LETTER1) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER2) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER3) + st.getQuestItemsCount(PRIGUNS_TEAR_LETTER4) == 4)
                                				{
                                    					st.playSound("ItemSound.quest_middle");
                                    					st.set("cond", "3");
                                				}
                                				else
                                				{
                                    					st.playSound("ItemSound.quest_itemget");
                                				}
                            				}
                        			}
                    			}
                		}
            		}
        	}
        	else if (npcId == OL_MAHUM_SENTRY)
        	{
            		st.set("id", "0");
            		if (cond > 0 && st.getQuestItemsCount(MORETTIS_HERB) == 1 && st.getQuestItemsCount(MORETTIS_LETTER) == 1 && st.getQuestItemsCount(RUSTED_KEY) == 0 && st.getRandom(10) < 6)
            		{
                		st.giveItems(RUSTED_KEY, 1);
                		st.playSound("ItemSound.quest_middle");
                		st.set("cond", "6");
            		}
        	}
        	return super.onKill(npc, killer, isSummon);
    	}

    	public static void main(String[] args)
    	{
        	new _407_PathToElvenScout(407, qn, "");
    	}
}