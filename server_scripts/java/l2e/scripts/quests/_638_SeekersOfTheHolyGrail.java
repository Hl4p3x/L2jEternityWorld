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
 * Created by LordWinter 24.09.2012
 * Based on L2J Eternity-World
 */
public final class _638_SeekersOfTheHolyGrail extends Quest
{
    	private static final String qn = "_638_SeekersOfTheHolyGrail";

    	private static final int DROP_CHANCE = 30;
    	private static final int INNOCENTIN = 31328;

    	private static final int[] MOBS =
	{
		22138, 22139, 22140, 22142, 22143, 22144, 22145, 22146,
        	22147, 22148, 22149, 22150, 22151, 22152, 22153, 22154,
        	22154, 22155, 22156, 22157, 22158, 22159, 22160, 22161,
        	22161, 22162, 22163, 22164, 22165, 22166, 22167, 22168,
        	22169, 22170, 22171, 22172, 22173, 22174, 22175
	};

    	// Items
    	private static final int TOTEM = 8068;
    	private static final int ANTEROOMKEY = 8273;
    	private static final int CHAPELKEY = 8274;
    	private static final int KEYOFDARKNESS = 8275;

    	// Mobs/raids that drop keys
    	private static final int RitualOffering = 22149;
    	private static final int ZombieWorker = 22140;
    	private static final int TriolsBeliever = 22143;
    	private static final int TriolsLayperson = 22142;
    	private static final int TriolsPriest2 = 22151;
    	private static final int TriolsPriest3 = 22146;

    	public _638_SeekersOfTheHolyGrail(int questId, String name, String descr)
    	{
        	super(questId, name, descr);
        
        	addStartNpc(INNOCENTIN);
        	addTalkId(INNOCENTIN);

        	for (int npcId : MOBS)
            		addKillId(npcId);

        	questItemIds = new int[] { TOTEM };
    	}

    	@Override
    	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;

        	if(event.equalsIgnoreCase("31328-02.htm"))
        	{
             		st.set("cond","1");
             		st.setState(State.STARTED);
             		st.playSound("ItemSound.quest_accept");
        	}
        	else if (event.equalsIgnoreCase("31328-06.htm"))
        	{
            		st.playSound("ItemSound.quest_finish");
            		st.exitQuest(true);
        	}
        	return htmltext;
    	}

    	@Override
    	public final String onTalk(L2Npc npc, L2PcInstance player)
    	{
        	String htmltext = Quest.getNoQuestMsg(player);
        	final QuestState st = player.getQuestState(qn);
        	if (st == null)
            		return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 73)
					htmltext = "31328-01.htm";
				else
				{
					htmltext = "31328-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
            			if(st.getQuestItemsCount(TOTEM) >= 2000)
            			{
                			int rr = st.getRandom(3);

                			if(rr == 0)
                			{
                   				st.takeItems(TOTEM,2000);
                    				st.giveItems(959, st.getRandom(4) + 3);
                   				st.playSound("ItemSound.quest_middle");
                			}
                			if(rr == 1)
                			{
                    				st.takeItems(TOTEM,2000);
                    				st.giveItems(57, 3576000);
                    				st.playSound("ItemSound.quest_middle");
                			}
                			if (rr == 2)
                			{
                     				st.takeItems(TOTEM,2000);
                     				st.giveItems(960, st.getRandom(4) + 3);
                     				st.playSound("ItemSound.quest_middle");
                			}
                			htmltext = "31328-03.htm";
            			}
            			else
            			{
                			htmltext = "31328-04.htm";
            			}
				break;
		}
        	return htmltext;
    	}

	@Override
    	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
    	{
        	L2PcInstance partyMember = getRandomPartyMember(player, 1);
        	if (partyMember == null)
            		return null;

        	QuestState st = partyMember.getQuestState(getName());
        	if (st == null)
            		return null;

        	final int npcId = npc.getId();

        	if(st.getInt("cond") == 1)
        	{
            		st.dropQuestItems(TOTEM, 1, 1, 0, true, DROP_CHANCE, true);
            		if (npcId == RitualOffering || npcId == ZombieWorker)
            		{
                		st.giveItems(ANTEROOMKEY, 6);
           		}
            		else if (npcId == TriolsBeliever || npcId == TriolsLayperson)
            		{
                 		st.giveItems(CHAPELKEY, 1);
            		}
            		else if ((npcId == TriolsPriest2 || npcId == TriolsPriest3) && getRandom(100) < 10)
            		{
                		st.giveItems(KEYOFDARKNESS, 1);
            		}
        	}
        	return null;
    	}

    	public static void main(String[] args)
    	{
        	new _638_SeekersOfTheHolyGrail(638, qn, "");
    	}
}