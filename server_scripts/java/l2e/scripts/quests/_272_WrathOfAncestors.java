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
 * Created by LordWinter 06.07.2012
 * Based on L2J Eternity-World
 */
public class _272_WrathOfAncestors extends Quest
{
	private static final String qn = "_272_WrathOfAncestors";
	
	// NPCs
	private static final int LIVINA = 30572;
	
	// Monsters
	private static final int GOBLIN_GRAVE_ROBBER = 20319;
	private static final int GOBLIN_TOMB_RAIDER_LEADER = 20320;
	
	// Item
	private static final int GRAVE_ROBBERS_HEAD = 1474;

	public _272_WrathOfAncestors(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(LIVINA);
		addTalkId(LIVINA);
		
		addKillId(GOBLIN_GRAVE_ROBBER);
		addKillId(GOBLIN_TOMB_RAIDER_LEADER);

		questItemIds = new int[] { GRAVE_ROBBERS_HEAD };
	}		
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30572-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if(st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getRace().ordinal() == 3)
				{
					if (player.getLevel() >= 5 && player.getLevel() <= 16)
						htmltext = "30572-02.htm";
					else
					{
						htmltext = "30572-01.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "30572-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (st.getQuestItemsCount(GRAVE_ROBBERS_HEAD) < 50)
					htmltext = "30572-04.htm";
				else
				{
					htmltext = "30572-05.htm";
					st.takeItems(GRAVE_ROBBERS_HEAD, -1);
					st.rewardItems(57, 1500);
					st.exitQuest(true);
					st.playSound("ItemSound.quest_finish");
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
		
		if (st.getInt("cond") == 1 && st.getQuestItemsCount(GRAVE_ROBBERS_HEAD) < 50)
		{
			st.giveItems(GRAVE_ROBBERS_HEAD, 1);
			if (st.getQuestItemsCount(GRAVE_ROBBERS_HEAD) < 49)
				st.playSound("ItemSound.quest_itemget");
			else
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "2");
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _272_WrathOfAncestors(272, qn, "");	
	}
}