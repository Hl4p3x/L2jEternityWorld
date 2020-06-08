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
import l2e.gameserver.model.skills.L2Skill;

public class _367_ElectrifyingRecharge extends Quest
{
	private static final String qn = "_367_ElectrifyingRecharge";
	
	private static final int LORAIN = 30673;
	
	private static final int LORAINS_LAMP = 5875;
	private static final int T_L1 = 5876;
	private static final int T_L2 = 5877;
	private static final int T_L3 = 5878;
	private static final int T_L4 = 5879;
	private static final int T_L5 = 5880;
	
	private static final int[] REWARD =
	{
		4553,
		4554,
		4555,
		4556,
		4557,
		4558,
		4559,
		4560,
		4561,
		4562,
		4563,
		4564
	};
	
	public _367_ElectrifyingRecharge(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(LORAIN);
		addTalkId(LORAIN);
		
		addSpellFinishedId(21035);
		
		questItemIds = new int[]
		{
			LORAINS_LAMP,
			T_L1,
			T_L2,
			T_L3,
			T_L4,
			T_L5
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
		
		if (event.equalsIgnoreCase("30673-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(LORAINS_LAMP, 1);
		}
		else if (event.equalsIgnoreCase("30673-09.htm"))
		{
			st.playSound("ItemSound.quest_accept");
			st.giveItems(LORAINS_LAMP, 1);
		}
		else if (event.equalsIgnoreCase("30673-08.htm"))
		{
			st.playSound("ItemSound.quest_giveup");
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30673-07.htm"))
		{
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
			st.giveItems(LORAINS_LAMP, 1);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 37)
				{
					htmltext = "30673-01.htm";
				}
				else
				{
					htmltext = "30673-02.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
				{
					if (st.hasQuestItems(T_L5))
					{
						htmltext = "30673-05.htm";
						st.takeItems(T_L5, 1);
						st.giveItems(LORAINS_LAMP, 1);
						st.playSound("ItemSound.quest_accept");
					}
					else if (st.hasQuestItems(T_L1))
					{
						htmltext = "30673-04.htm";
						st.takeItems(T_L1, 1);
					}
					else if (st.hasQuestItems(T_L2))
					{
						htmltext = "30673-04.htm";
						st.takeItems(T_L2, 1);
					}
					else if (st.hasQuestItems(T_L3))
					{
						htmltext = "30673-04.htm";
						st.takeItems(T_L3, 1);
					}
					else
					{
						htmltext = "30673-03.htm";
					}
				}
				else if ((cond == 2) && (st.hasQuestItems(T_L4)))
				{
					htmltext = "30673-06.htm";
					st.takeItems(T_L4, 1);
					st.rewardItems(REWARD[getRandom(REWARD.length)], 1);
					st.playSound("ItemSound.quest_finish");
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
		{
			return null;
		}
		
		if (skill.getId() == 4072)
		{
			if (st.hasQuestItems(LORAINS_LAMP))
			{
				int randomItem = getRandom(T_L1, T_L5);
				
				st.takeItems(LORAINS_LAMP, 1);
				st.giveItems(randomItem, 1);
				
				if (randomItem == T_L4)
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _367_ElectrifyingRecharge(367, qn, "");
	}
}