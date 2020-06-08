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
public class _262_TradeWithTheIvoryTower extends Quest
{
	private final static String qn = "_262_TradeWithTheIvoryTower";
	
	// NPC
	private static final int Vollodos = 30137;
	
	// Item
	private static final int FUNGUS_SAC = 707;
	
	public _262_TradeWithTheIvoryTower(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Vollodos);
		addTalkId(Vollodos);
		
		addKillId(20400, 20007);

		questItemIds = new int[] { FUNGUS_SAC };
	}	
		
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30137-03.htm"))
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
				if (player.getLevel() >= 8 && player.getLevel() <= 16)
					htmltext = "30137-02.htm";
				else
				{
					htmltext = "30137-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (st.getQuestItemsCount(FUNGUS_SAC) < 10)
					htmltext = "30137-04.htm";
				else
				{
					htmltext = "30137-05.htm";
					st.takeItems(FUNGUS_SAC, -1);
					st.rewardItems(57, 3000);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(true);
				}
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
		
		if (st.getInt("cond") == 1)
		{
			int chance = npc.getId() == 20400 ? 4 : 3;
			if (st.getRandom(10) < chance)
			{
				st.giveItems(FUNGUS_SAC, 1);
				if (st.getQuestItemsCount(FUNGUS_SAC) < 10)
					st.playSound("ItemSound.quest_itemget");
				else
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _262_TradeWithTheIvoryTower(262, qn, "");	
	}
}