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

import java.util.Calendar;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 06.08.2011 Based on L2J Eternity-World
 */
public class _451_LuciensAltar extends Quest
{
	private static final String qn = "_451_LuciensAltar";
	
	private static final int DAICHIR = 30537;
	
	private static final int REPLENISHED_BEAD = 14877;
	private static final int DISCHARGED_BEAD = 14878;
	
	private static final int[][] ALTARS =
	{
		{
			32706,
			1
		},
		{
			32707,
			2
		},
		{
			32708,
			4
		},
		{
			32709,
			8
		},
		{
			32710,
			16
		}
	};
	
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	
	public _451_LuciensAltar(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addStartNpc(DAICHIR);
		addTalkId(DAICHIR);
		
		for (int[] i : ALTARS)
		{
			addTalkId(i[0]);
		}
		
		questItemIds = new int[]
		{
			REPLENISHED_BEAD,
			DISCHARGED_BEAD
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("30537-03.htm"))
		{
			st.set("cond", "1");
			st.set("altars_state", "0");
			st.setState(State.STARTED);
			st.giveItems(REPLENISHED_BEAD, 5);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getId() == DAICHIR)
		{
			if (st.getInt("cond") == 0)
			{
				String reset = st.get("reset");
				long remain = 0;
				if ((reset != null) && isDigit(reset))
				{
					remain = Long.parseLong(reset) - System.currentTimeMillis();
				}
				if (remain <= 0)
				{
					if (player.getLevel() >= 80)
					{
						htmltext = "30537-01.htm";
					}
					else
					{
						htmltext = "30537-00.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "30537-06.htm";
				}
			}
			else if (st.getInt("cond") == 1)
			{
				if (st.getQuestItemsCount(DISCHARGED_BEAD) >= 1)
				{
					htmltext = "30537-04a.htm";
				}
				else
				{
					htmltext = "30537-04.htm";
				}
			}
			else if (st.getInt("cond") == 2)
			{
				htmltext = "30537-05.htm";
				st.giveItems(57, 127690);
				st.takeItems(DISCHARGED_BEAD, 5);
				st.setState(State.COMPLETED);
				st.unset("cond");
				st.unset("altars_state");
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
				Calendar reset = Calendar.getInstance();
				reset.set(Calendar.MINUTE, RESET_MIN);
				// if time is >= RESET_HOUR - roll to the next day
				if (reset.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
				{
					reset.add(Calendar.DATE, 1);
				}
				reset.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
				st.set("reset", String.valueOf(reset.getTimeInMillis()));
			}
		}
		else if (st.getInt("cond") == 1)
		{
			int idx = 0;
			for (int[] i : ALTARS)
			{
				if (i[0] == npc.getId())
				{
					idx = i[1];
					break;
				}
			}
			if (idx != 0)
			{
				int state = st.getInt("altars_state");
				if ((state & idx) == 0)
				{
					st.set("altars_state", String.valueOf((state | idx)));
					st.takeItems(REPLENISHED_BEAD, 1);
					st.giveItems(DISCHARGED_BEAD, 1);
					st.playSound("ItemSound.quest_itemget");
					if (st.getQuestItemsCount(DISCHARGED_BEAD) == 5)
					{
						st.set("cond", "2");
						st.playSound("ItemSound.quest_middle");
					}
					htmltext = "recharge.htm";
				}
				else
				{
					htmltext = "findother.htm";
				}
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new _451_LuciensAltar(451, qn, "");
	}
}