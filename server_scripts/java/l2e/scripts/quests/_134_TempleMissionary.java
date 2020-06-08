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
import l2e.util.Rnd;

/**
 * Created by LordWinter 13.10.2012
 * Based on L2J Eternity-World
 */
public final class _134_TempleMissionary extends Quest
{
	private static final String qn = "_134_TempleMissionary";

	// NPCs
	private final static int GLYVKA = 30067;
	private final static int ROUKE = 31418;

	// Mobs
	private final static int MARSHLANDS_TRAITOR = 27339;
	private final static int[] mobs =
	{
			20157,
			20229,
			20230,
			20231,
			20232,
			20233,
			20234
	};

	// Quest Items
	private final static int FRAGMENT = 10335;
	private final static int GIANTS_TOOL = 10336;
	private final static int REPORT = 10337;
	private final static int REPORT2 = 10338;

	// Items
	private final static int BADGE = 10339;

	// Chances
	private final static int FRAGMENT_CHANCE = 66;

	public _134_TempleMissionary(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(GLYVKA);
		addTalkId(GLYVKA);
		addTalkId(ROUKE);

        	for (int mob : mobs)
        	{
            		addKillId(mob);
        	}
		addKillId(MARSHLANDS_TRAITOR);

		questItemIds = new int[] { FRAGMENT, GIANTS_TOOL, REPORT, REPORT2 };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("30067-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30067-04.htm"))
		{
            		st.set("cond", "2");
            		st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("30067-08.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.unset("Report");
			st.giveItems(57, 15100);
			st.giveItems(BADGE, 1);
			st.addExpAndSp(30000, 2000);
			st.exitQuest(false);
		}
		else if(event.equalsIgnoreCase("31418-02.htm"))
		{
            		st.set("cond", "3");
            		st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("31418-07.htm"))
		{
            		st.set("cond", "5");
            		st.playSound("ItemSound.quest_middle");
			st.giveItems(REPORT2, 1);
			st.unset("Report");
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		int cond = st.getCond();
		int npcId = npc.getId();

    		switch (st.getState())
    		{
    			case State.CREATED:
				if(player.getLevel() >= 35)
					htmltext = "30067-01.htm";
				else
				{
					htmltext = "30067-00.htm";
					st.exitQuest(true);
				}
       				break;
   	 		case State.STARTED:
				if(npcId == GLYVKA)
				{
					if(cond == 1)
						return "30067-02.htm";
					else if((cond == 2) || (cond == 3) || (cond == 4))
						htmltext = "30067-05.htm";
					else if(cond == 5)
					{
						if(st.getInt("Report") == 1)
							htmltext = "30067-07.htm";
						if(st.getQuestItemsCount(REPORT2) > 0)
						{
							st.takeItems(REPORT2, -1);
							st.set("Report", "1");
							htmltext = "30067-06.htm";
						}
					}
				}

				if(npcId == ROUKE)
				{
					if(cond == 2)
						htmltext = "31418-01.htm";
					else if(cond == 3)
					{
						long Tools = st.getQuestItemsCount(FRAGMENT) / 10;
						if(Tools < 1)
							htmltext = "31418-03.htm";
						st.takeItems(FRAGMENT, Tools * 10);
						st.giveItems(GIANTS_TOOL, Tools);
						htmltext = "31418-04.htm";
					}
					else if(cond == 4)
					{
						if(st.getInt("Report") == 1)
							htmltext = "31418-06.htm";
						if(st.getQuestItemsCount(REPORT) > 2)
						{
							st.takeItems(FRAGMENT, -1);
							st.takeItems(GIANTS_TOOL, -1);
							st.takeItems(REPORT, -1);
							st.set("Report", "1");
							htmltext = "31418-05.htm";
						}
					}
					else if(cond == 5)
						htmltext = "31418-08.htm";
				}
				break;
    			case State.COMPLETED:
      				htmltext = getAlreadyCompletedMsg(player);
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

		if(st.getCond() == 3)
		{
			if(npc.getId() == MARSHLANDS_TRAITOR)
			{
				st.giveItems(REPORT, 1);
				if(st.getQuestItemsCount(REPORT) < 3)
					st.playSound("ItemSound.quest_itemget");
				else
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond","4");
				}
			}
			else if(st.getQuestItemsCount(GIANTS_TOOL) < 1)
			{
				if(Rnd.chance(FRAGMENT_CHANCE))
					st.giveItems(FRAGMENT, 1);
			}
			else
			{
				st.takeItems(GIANTS_TOOL, 1);
				if(Rnd.chance(45))
					st.addSpawn(MARSHLANDS_TRAITOR, npc, true, 900000);
			}
		}
		return null;
	}

  	public static void main(String[] args)
  	{
    		new _134_TempleMissionary(134, qn, "");
  	}
}