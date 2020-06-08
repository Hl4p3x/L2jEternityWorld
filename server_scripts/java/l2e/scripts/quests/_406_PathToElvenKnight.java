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
public class _406_PathToElvenKnight extends Quest
{
    	private static final String qn = "_406_PathToElvenKnight";

    	// Npc
    	private static final int SORIUS = 30327;
    	private static final int KLUTO = 30317;

    	private static final int[] TALKERS =
    	{
        	SORIUS, KLUTO
    	};

    	// Mobs
    	private static final int TRACKER_SKELETON = 20035;
    	private static final int TRACKER_SKELETON_LEADER = 20042;
    	private static final int SKELETON_SCOUT = 20045;
    	private static final int SKELETON_BOWMAN = 20051;
    	private static final int RUIN_SPARTOI = 20054;
    	private static final int RAGING_SPARTOI = 20060;
    	private static final int OL_MAHUM_NOVICE = 20782;

    	private static final int[] MOBS =
    	{
        	TRACKER_SKELETON, TRACKER_SKELETON_LEADER, SKELETON_SCOUT, SKELETON_BOWMAN, RUIN_SPARTOI, RAGING_SPARTOI, OL_MAHUM_NOVICE
    	};

    	// Quest items
    	private static final int SORIUS_LETTER1 = 1202;
    	private static final int KLUTO_BOX = 1203;
    	private static final int TOPAZ_PIECE = 1205;
    	private static final int EMERALD_PIECE = 1206;
    	private static final int KLUTO_MEMO = 1276;

    	private static final int[] QUESTITEMS =
    	{
        	SORIUS_LETTER1, KLUTO_BOX, TOPAZ_PIECE, EMERALD_PIECE, KLUTO_MEMO
    	};

    	// Reward
    	private static final int ELVEN_KNIGHT_BROOCH = 1204;

    	public _406_PathToElvenKnight(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	addStartNpc(SORIUS);

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

        	if (event.equalsIgnoreCase("30327-05.htm"))
        	{
            		if (player.getClassId().getId() != 0x12)
            		{
                		if (player.getClassId().getId() == 0x13)
                		{
                    			htmltext = "30327-02a.htm";
                		}
                		else
                		{
                    			st.exitQuest(true);
                    			htmltext = "30327-02.htm";
                		}
            		}
            		else
            		{
                		if (player.getLevel() < 18)
                		{
                    			st.exitQuest(true);
                    			htmltext = "30327-03.htm";
                		}
                		else
                		{
                    			if (st.getQuestItemsCount(ELVEN_KNIGHT_BROOCH) > 0)
                    			{
                        			htmltext = "30327-04.htm";
                    			}
                		}
            		}
        	}
        	else if (event.equalsIgnoreCase("30327-06.htm"))
        	{
            		st.set("cond", "1");
            		st.setState(State.STARTED);
            		st.playSound("ItemSound.quest_accept");
        	}
        	else if (event.equalsIgnoreCase("30317-02.htm"))
        	{
            		if (st.getInt("cond") == 3)
            		{
                		st.takeItems(SORIUS_LETTER1, -1);
                		if (st.getQuestItemsCount(KLUTO_MEMO) == 0)
                		{
                    			st.giveItems(KLUTO_MEMO, 1);
                    			st.set("cond", "4");
                		}
                		else
                		{
                    			htmltext = getNoQuestMsg(player);
                		}
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
        	int cond = st.getInt("cond");

        	if (npcId != SORIUS && id != State.STARTED)
        	{
            		return htmltext;
        	}

        	if (npcId == SORIUS)
        	{
            		if (cond == 0)
            		{
                		htmltext = "30327-01.htm";
            		}
            		else if (cond == 1)
            		{
                		htmltext = st.getQuestItemsCount(TOPAZ_PIECE) == 0 ? "30327-07.htm" : "30327-08.htm";
            		}
            		else if (cond == 2)
            		{
                		if (st.getQuestItemsCount(SORIUS_LETTER1) == 0)
                		{
                    			st.giveItems(SORIUS_LETTER1, 1);
                		}
                		st.set("cond", "3");
                		htmltext = "30327-09.htm";
            		}
            		else if (cond == 3 || cond == 4 || cond == 5)
            		{
                		htmltext = "30327-11.htm";
            		}
            		else if (cond == 6)
            		{
                		st.takeItems(KLUTO_BOX, -1);
                		String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
                		if (isFinished.equalsIgnoreCase(""))
                		{
                    			st.addExpAndSp(228064, 3520);
                		}
                		if (st.getQuestItemsCount(ELVEN_KNIGHT_BROOCH) == 0)
                		{
                    			st.giveItems(ELVEN_KNIGHT_BROOCH, 1);
                		}
                		st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                		st.set("cond", "0");
                		talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
                		st.exitQuest(false);
                		st.playSound("ItemSound.quest_finish");
                		htmltext = "30327-10.htm";
            		}
        	}
        	else if (npcId == KLUTO)
        	{
            		if (cond == 3)
            		{
                		htmltext = "30317-01.htm";
            		}
            		else if (cond == 4)
            		{
               		 	htmltext = st.getQuestItemsCount(EMERALD_PIECE) == 0 ? "30317-03.htm" : "30317-04.htm";
            		}
            		else if (cond == 5)
            		{
                		st.takeItems(EMERALD_PIECE, -1);
                		st.takeItems(TOPAZ_PIECE, -1);
                		if (st.getQuestItemsCount(KLUTO_BOX) == 0)
                		{
                    			st.giveItems(KLUTO_BOX, 1);
                		}
                		st.takeItems(KLUTO_MEMO, -1);
                		st.set("cond", "6");
                		htmltext = "30317-05.htm";
            		}
            		else if (cond == 6)
            		{
                		htmltext = "30317-06.htm";
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
        	int cond = st.getInt("cond");

        	if (npcId != OL_MAHUM_NOVICE)
        	{
            		if (cond == 1 && st.getQuestItemsCount(TOPAZ_PIECE) < 20 && st.getRandom(100) < 70)
            		{
                		st.giveItems(TOPAZ_PIECE, 1);
                		if (st.getQuestItemsCount(TOPAZ_PIECE) == 20)
                		{
                    			st.playSound("ItemSound.quest_middle");
                    			st.set("cond", "2");
                		}
                		else
                		{
                    			st.playSound("ItemSound.quest_itemget");
                		}
            		}
        	}
        	else
        	{
            		if (cond == 4 && st.getQuestItemsCount(EMERALD_PIECE) < 20 && st.getRandom(100) < 50)
            		{
                		st.giveItems(EMERALD_PIECE, 1);
                		if (st.getQuestItemsCount(EMERALD_PIECE) == 20)
                		{
                    			st.playSound("ItemSound.quest_middle");
                    			st.set("cond", "5");
                		}
                		else
                		{
                    			st.playSound("ItemSound.quest_itemget");
                		}
            		}
        	}
        	return super.onKill(npc, killer, isSummon);
    	}

    	public static void main(String[] args)
    	{
        	new _406_PathToElvenKnight(406, qn, "");
    	}
}