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
 * Created by LordWinter 30.09.2012
 * Based on L2J Eternity-World
 */
public final class _324_SweetestVenom extends Quest
{
	private static final String qn = "_324_SweetestVenom";

	private static int VENOM_SAC = 1077;	
	
	public _324_SweetestVenom(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(30351);
		addTalkId(30351);

		addKillId(20034,20038,20043);

		questItemIds = new int[] { VENOM_SAC };
	}	
		
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;				

		if (event.equalsIgnoreCase("30351-04.htm"))
		{
			st.set("cond","1");
			st.setState(State.STARTED);
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
			return htmltext;
		
		switch (st.getState())
		{		
			case State.CREATED:
				if(player.getLevel() >= 18)
					htmltext = "30351-03.htm";
				else
				{
					htmltext = "30351-02.htm";
					st.exitQuest(true);
				}			
				break;
			case State.STARTED:
				if (st.getQuestItemsCount(VENOM_SAC) < 10)
					htmltext = "30351-05.htm";
				else
				{
					st.takeItems(VENOM_SAC,-1);
					st.giveItems(57,5810);
					st.exitQuest(true);
					st.playSound("ItemSound.quest_finish");
					htmltext = "30351-06.htm";
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

		int chance = 22 + (((npc.getId() - 20000) ^ 34) / 4);
		int count = (int) st.getQuestItemsCount(VENOM_SAC);
	
		if (count < 10 && st.getRandom(100) < chance)
		{
			st.giveItems(VENOM_SAC,1);
			if (count == 9)
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond","2");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}				
		return null;
	}	

	public static void main(String[] args)
	{
		new _324_SweetestVenom(324, qn, "");
	}
}