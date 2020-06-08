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
public class _405_PathToCleric extends Quest
{
    	private static final String qn = "_405_PathToCleric";

    	// Npc
    	private static final int ZIGAUNT = 30022;
    	private static final int GALLINT = 30017;
    	private static final int VIVYAN = 30030;
    	private static final int SIMPLON = 30253;
    	private static final int PRAGA = 30333;
    	private static final int LIONEL = 30408;

    	private static final int[] TALKERS =
    	{
        	ZIGAUNT, GALLINT, VIVYAN, SIMPLON, PRAGA, LIONEL
    	};

    	// Mobs
    	private static final int RUIN_ZOMBIE = 20026;
    	private static final int RUIN_ZOMBIE_LEADER = 20029;

    	private static final int[] MOBS =
    	{
        	RUIN_ZOMBIE, RUIN_ZOMBIE_LEADER
    	};

    	// Quest items
    	private static final int LETTER_OF_ORDER1 = 1191;
    	private static final int LETTER_OF_ORDER2 = 1192;
    	private static final int BOOK_OF_LEMONIELL = 1193;
    	private static final int BOOK_OF_VIVI = 1194;
    	private static final int BOOK_OF_SIMLON = 1195;
    	private static final int BOOK_OF_PRAGA = 1196;
    	private static final int CERTIFICATE_OF_GALLINT = 1197;
    	private static final int PENDANT_OF_MOTHER = 1198;
    	private static final int NECKLACE_OF_MOTHER = 1199;
    	private static final int LEMONIELLS_COVENANT = 1200;

    	private static final int[] QUESTITEMS =
    	{
        	LETTER_OF_ORDER1, LETTER_OF_ORDER2, BOOK_OF_LEMONIELL, BOOK_OF_VIVI, BOOK_OF_SIMLON, BOOK_OF_PRAGA, CERTIFICATE_OF_GALLINT, PENDANT_OF_MOTHER, NECKLACE_OF_MOTHER, LEMONIELLS_COVENANT
    	};

    	// Reward
    	private static final int MARK_OF_FAITH = 1201;

    	public _405_PathToCleric(int questId, String name, String descr)
    	{
        	super(questId, name, descr);

        	addStartNpc(ZIGAUNT);

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

        	int level = player.getLevel();
        	int classId = player.getClassId().getId();

        	if (event.equalsIgnoreCase("1"))
        	{
            		st.set("id", "0");
            		if (level >= 18 && classId == 0x0a && st.getQuestItemsCount(MARK_OF_FAITH) == 0)
            		{
                		st.set("cond", "1");
                		st.setState(State.STARTED);
                		st.playSound("ItemSound.quest_accept");
                		st.giveItems(LETTER_OF_ORDER1, 1);
                		htmltext = "30022-05.htm";
            		}
            		else if (classId != 0x0a)
            		{
                		htmltext = classId == 0x0f ? "30022-02a.htm" : "30022-02.htm";
            		}
            		else if (level < 18 && classId == 0x0a)
            		{
                		htmltext = "30022-03.htm";
            		}
            		else if (level >= 18 && classId == 0x0a && st.getQuestItemsCount(MARK_OF_FAITH) == 1)
            		{
                		htmltext = "30022-04.htm";
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

        	if (npcId != ZIGAUNT && id != State.STARTED)
        	{
            		return htmltext;
        	}

        	int cond = st.getInt("cond");

        	if (npcId == ZIGAUNT && cond == 0)
        	{
            		htmltext = st.getQuestItemsCount(MARK_OF_FAITH) == 0 ? "30022-01.htm" : "30022-04.htm";
        	}
        	else if (npcId == ZIGAUNT && cond > 0 && st.getQuestItemsCount(LETTER_OF_ORDER2) == 1 && st.getQuestItemsCount(LEMONIELLS_COVENANT) == 0)
        	{
            		htmltext = "30022-07.htm";
        	}
        	else if (npcId == ZIGAUNT && cond > 0 && st.getQuestItemsCount(LETTER_OF_ORDER2) == 1 && st.getQuestItemsCount(LEMONIELLS_COVENANT) == 1)
        	{
            		st.takeItems(LETTER_OF_ORDER2, 1);
            		st.takeItems(LEMONIELLS_COVENANT, 1);
            		String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
            		if (isFinished.equalsIgnoreCase(""))
            		{
                		st.addExpAndSp(295862, 2910);
            		}
            		st.giveItems(MARK_OF_FAITH, 1);
            		st.set("cond", "0");
            		st.saveGlobalQuestVar("1ClassQuestFinished", "1");
            		st.exitQuest(false);
            		talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
            		st.playSound("ItemSound.quest_finish");
            		htmltext = "30022-09.htm";
        	}
        	else if (npcId == ZIGAUNT && cond > 0 && st.getQuestItemsCount(LETTER_OF_ORDER1) == 1)
        	{
            		if (st.getQuestItemsCount(BOOK_OF_VIVI) == 1 && st.getQuestItemsCount(BOOK_OF_SIMLON) > 0 && st.getQuestItemsCount(BOOK_OF_PRAGA) == 1)
            		{
                		st.takeItems(BOOK_OF_PRAGA, 1);
                		st.takeItems(BOOK_OF_VIVI, 1);
                		st.takeItems(BOOK_OF_SIMLON, 3);
                		st.takeItems(LETTER_OF_ORDER1, 1);
                		st.giveItems(LETTER_OF_ORDER2, 1);
                		st.set("cond", "3");
                		htmltext = "30022-08.htm";
            		}
            		else
            		{
                		htmltext = "30022-06.htm";
            		}
        	}
        	else if (npcId == SIMPLON && cond > 0 && st.getQuestItemsCount(LETTER_OF_ORDER1) == 1)
        	{
            		if (st.getQuestItemsCount(BOOK_OF_SIMLON) == 0)
            		{
                		st.giveItems(BOOK_OF_SIMLON, 3);
                		htmltext = "30253-01.htm";
            		}
            		else if (st.getQuestItemsCount(BOOK_OF_SIMLON) > 0)
            		{
                		htmltext = "30253-02.htm";
            		}
       	 	}
        	else if (npcId == VIVYAN && cond > 0 && st.getQuestItemsCount(LETTER_OF_ORDER1) == 1)
        	{
            		if (st.getQuestItemsCount(BOOK_OF_VIVI) == 0)
            		{
                		st.giveItems(BOOK_OF_VIVI, 1);
                		htmltext = "30030-01.htm";
            		}
            		else if (st.getQuestItemsCount(BOOK_OF_VIVI) == 1)
            		{
                		htmltext = "30030-02.htm";
            		}
        	}
        	else if (npcId == PRAGA && cond > 0 && st.getQuestItemsCount(LETTER_OF_ORDER1) == 1)
        	{
            		if (st.getQuestItemsCount(BOOK_OF_PRAGA) == 0 && st.getQuestItemsCount(NECKLACE_OF_MOTHER) == 0)
            		{
                		st.giveItems(NECKLACE_OF_MOTHER, 1);
                		htmltext = "30333-01.htm";
            		}
            		else if (st.getQuestItemsCount(BOOK_OF_PRAGA) == 0 && st.getQuestItemsCount(NECKLACE_OF_MOTHER) == 1 && st.getQuestItemsCount(PENDANT_OF_MOTHER) == 0)
            		{
                		htmltext = "30333-02.htm";
            		}
            		else if (st.getQuestItemsCount(BOOK_OF_PRAGA) == 0 && st.getQuestItemsCount(NECKLACE_OF_MOTHER) == 1 && st.getQuestItemsCount(PENDANT_OF_MOTHER) == 1)
            		{
                		st.takeItems(NECKLACE_OF_MOTHER, 1);
                		st.takeItems(PENDANT_OF_MOTHER, 1);
                		st.giveItems(BOOK_OF_PRAGA, 1);
                		st.set("cond", "2");
                		htmltext = "30333-03.htm";
            		}
            		else if (st.getQuestItemsCount(BOOK_OF_PRAGA) > 0)
            		{
                		htmltext = "30333-04.htm";
            		}
        	}
        	else if (npcId == LIONEL && cond > 0)
        	{
            		if (st.getQuestItemsCount(LETTER_OF_ORDER2) == 0)
            		{
                		htmltext = "30408-02.htm";
            		}
            		else if (st.getQuestItemsCount(LETTER_OF_ORDER2) == 1 && st.getQuestItemsCount(BOOK_OF_LEMONIELL) == 0 && st.getQuestItemsCount(LEMONIELLS_COVENANT) == 0 && st.getQuestItemsCount(CERTIFICATE_OF_GALLINT) == 0)
            		{
                		st.giveItems(BOOK_OF_LEMONIELL, 1);
                		st.set("cond", "4");
                		htmltext = "30408-01.htm";
            		}
            		else if (st.getQuestItemsCount(LETTER_OF_ORDER2) == 1 && st.getQuestItemsCount(BOOK_OF_LEMONIELL) == 1 && st.getQuestItemsCount(LEMONIELLS_COVENANT) == 0 && st.getQuestItemsCount(CERTIFICATE_OF_GALLINT) == 0)
            		{
                		htmltext = "30408-03.htm";
            		}
            		else if (st.getQuestItemsCount(LETTER_OF_ORDER2) == 1 && st.getQuestItemsCount(BOOK_OF_LEMONIELL) == 0 && st.getQuestItemsCount(LEMONIELLS_COVENANT) == 0 && st.getQuestItemsCount(CERTIFICATE_OF_GALLINT) == 1)
            		{
                		st.takeItems(CERTIFICATE_OF_GALLINT, 1);
                		st.giveItems(LEMONIELLS_COVENANT, 1);
                		st.set("cond", "6");
                		htmltext = "30408-04.htm";
            		}
            		else if (st.getQuestItemsCount(LETTER_OF_ORDER2) == 1 && st.getQuestItemsCount(BOOK_OF_LEMONIELL) == 0 && st.getQuestItemsCount(LEMONIELLS_COVENANT) == 1 && st.getQuestItemsCount(CERTIFICATE_OF_GALLINT) == 0)
            		{
                		htmltext = "30408-05.htm";
            		}
        	}
        	else if (npcId == GALLINT && cond > 0 && st.getQuestItemsCount(LETTER_OF_ORDER2) == 1 && st.getQuestItemsCount(LEMONIELLS_COVENANT) == 0)
        	{
            		if (st.getQuestItemsCount(BOOK_OF_LEMONIELL) == 1 && st.getQuestItemsCount(CERTIFICATE_OF_GALLINT) == 0)
            		{
                		st.takeItems(BOOK_OF_LEMONIELL, 1);
                		st.giveItems(CERTIFICATE_OF_GALLINT, 1);
                		st.set("cond", "5");
                		htmltext = "30017-01.htm";
            		}
            		else if (st.getQuestItemsCount(BOOK_OF_LEMONIELL) == 0 && st.getQuestItemsCount(CERTIFICATE_OF_GALLINT) == 1)
            		{
                		htmltext = "30017-02.htm";
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

        	if (npcId == RUIN_ZOMBIE)
        	{
            		st.set("id", "0");
            		if (st.getInt("cond") > 0 && st.getQuestItemsCount(PENDANT_OF_MOTHER) == 0)
            		{
                		st.giveItems(PENDANT_OF_MOTHER, 1);
                		st.playSound("ItemSound.quest_middle");
            		}
        	}
        	else if (npcId == RUIN_ZOMBIE_LEADER)
        	{
            		st.set("id", "0");
            		if (st.getInt("cond") > 0 && st.getQuestItemsCount(PENDANT_OF_MOTHER) == 0)
            		{
                		st.giveItems(PENDANT_OF_MOTHER, 1);
                		st.playSound("ItemSound.quest_middle");
            		}
        	}
        	return super.onKill(npc, killer, isSummon);
    	}

    	public static void main(String[] args)
    	{
        	new _405_PathToCleric(405, qn, "");
    	}
}