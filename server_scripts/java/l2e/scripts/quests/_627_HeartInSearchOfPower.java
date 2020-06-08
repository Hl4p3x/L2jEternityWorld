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

import l2e.Config;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 03.04.2011
 * Based on L2J Eternity-World
 */
public class _627_HeartInSearchOfPower extends Quest
{
	private static final String qn = "_627_HeartInSearchOfPower";

	// NPC
	private static final int NECROMANCER = 31518;
	private static final int ENFEUX = 31519;

	private static final int SEAL_OF_LIGHT = 7170;
	private static final int GEM_OF_SUBMISSION = 7171;
	private static final int GEM_OF_SAINTS = 7172;

	// REWARDS
	private static final int MOLD_HARDENER = 4041;
	private static final int ENRIA = 4042;
	private static final int ASOFE = 4043;
	private static final int THONS = 4044;

	public _627_HeartInSearchOfPower(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NECROMANCER);
		addTalkId(NECROMANCER);
		addTalkId(ENFEUX);

		for(int mobs = 21520; mobs <= 21541; mobs++)
			addKillId(mobs);

		questItemIds = new int[] { GEM_OF_SUBMISSION };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("31518-1.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("31518-3.htm"))
		{
			st.takeItems(GEM_OF_SUBMISSION, 300);
			st.giveItems(SEAL_OF_LIGHT, 1);
			st.set("cond", "3");
		}
		else if(event.equalsIgnoreCase("31519-1.htm"))
		{
			st.takeItems(SEAL_OF_LIGHT, 1);
			st.giveItems(GEM_OF_SAINTS, 1);
			st.set("cond", "4");
		}
		else if(event.equalsIgnoreCase("31518-5.htm") && st.getQuestItemsCount(GEM_OF_SAINTS) == 1)
		{
			st.takeItems(GEM_OF_SAINTS, 1);
			st.set("cond","5");
		}
		else
		{
			if(event.equalsIgnoreCase("31518-6.htm"))
				st.giveItems(57, 100000);
			else if(event.equalsIgnoreCase("31518-7.htm"))
			{
				st.giveItems(ASOFE, 13);
				st.giveItems(57, 6400);
			}
			else if(event.equalsIgnoreCase("31518-8.htm"))
			{
				st.giveItems(THONS, 13);
				st.giveItems(57, 6400);
			}
			else if(event.equalsIgnoreCase("31518-9.htm"))
			{
				st.giveItems(ENRIA, 6);
				st.giveItems(57, 13600);
			}
			else if(event.equalsIgnoreCase("31518-10.htm"))
			{
				st.giveItems(MOLD_HARDENER, 3);
				st.giveItems(57, 17200);
			}
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
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

		int npcId = npc.getId();
		int cond = st.getInt("cond");

    		switch (st.getState())
    		{
    			case State.CREATED:
				if(npcId == NECROMANCER)
				{
					if(player.getLevel() >= 60)
						htmltext = "31518-0.htm";
					else
					{
						htmltext = "31518-0a.htm";
						st.exitQuest(true);
					}
				}
      				break;
    			case State.STARTED:
				if(npcId == NECROMANCER)
				{
					if(cond == 1)
						htmltext = "31518-1a.htm";
					else if(st.getQuestItemsCount(GEM_OF_SUBMISSION) == 300)
						htmltext = "31518-2.htm";
					else if(st.getQuestItemsCount(GEM_OF_SAINTS) > 0)
						htmltext = "31518-4.htm";
					else if(cond == 5)
						htmltext = "31518-5.htm";
				}
				else if(npcId == ENFEUX && st.getQuestItemsCount(SEAL_OF_LIGHT) > 0)
					htmltext = "31519-0.htm";
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		long count = st.getQuestItemsCount(GEM_OF_SUBMISSION);
		if(st.getInt("cond") == 1 && count < 300)
		{
			st.giveItems(GEM_OF_SUBMISSION, 1);
			count += 1 * Config.RATE_QUEST_DROP;
			if(count > 299)
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "2");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _627_HeartInSearchOfPower(627, qn, "");
	}
}