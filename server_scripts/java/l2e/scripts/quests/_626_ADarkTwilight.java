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
 * Created by LordWinter 20.06.2012 Based on L2J Eternity-World
 */
public class _626_ADarkTwilight extends Quest
{
	private static final String qn = "_626_ADarkTwilight";
	
	private static final int BloodOfSaint = 7169;
	private static final int Hierarch = 31517;
	
	public _626_ADarkTwilight(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Hierarch);
		addTalkId(Hierarch);
		
		addKillId(new int[]
		{
			21520,
			21523,
			21524,
			21526,
			21529,
			21530,
			21531,
			21532,
			21535,
			21536,
			21539,
			21540
		});
		
		questItemIds = new int[]
		{
			BloodOfSaint
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
		
		if (event.equalsIgnoreCase("31517-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("reward1"))
		{
			if (st.getQuestItemsCount(BloodOfSaint) == 300L)
			{
				htmltext = "31517-07.htm";
				st.takeItems(BloodOfSaint, 300L);
				st.addExpAndSp(162773, 12500);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
			else
			{
				htmltext = "31517-08.htm";
			}
		}
		else if (event.equalsIgnoreCase("reward2"))
		{
			if (st.getQuestItemsCount(BloodOfSaint) == 300L)
			{
				htmltext = "31517-07.htm";
				st.takeItems(BloodOfSaint, 300L);
				st.rewardItems(57, 100000L);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
			else
			{
				htmltext = "31517-08.htm";
			}
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
		
		switch (st.getState())
		{
			case State.CREATED:
				if ((player.getLevel() >= 60) && (player.getLevel() <= 71))
				{
					htmltext = "31517-01.htm";
				}
				else
				{
					htmltext = "31517-02.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				if ((cond == 1) && (st.getQuestItemsCount(BloodOfSaint) < 300L))
				{
					htmltext = "31517-05.htm";
				}
				else
				{
					if (cond != 2)
					{
						break;
					}
					htmltext = "31517-04.htm";
				}
				break;
			case State.COMPLETED:
				htmltext = Quest.getAlreadyCompletedMsg(player);
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		if ((st.getInt("cond") == 1) && (st.getQuestItemsCount(BloodOfSaint) < 300L))
		{
			st.giveItems(BloodOfSaint, 1L);
			if (st.getQuestItemsCount(BloodOfSaint) == 300L)
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _626_ADarkTwilight(626, qn, "");
	}
}