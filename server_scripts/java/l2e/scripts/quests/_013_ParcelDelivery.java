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
 * Created by LordWinter 21.03.2011
 * Based on L2J Eternity-World
 */
public class _013_ParcelDelivery extends Quest
{
	private static final String qn = "_013_ParcelDelivery";

	//NPC
	private static final int FUNDIN = 31274;
	private static final int VULCAN = 31539;
	private static final int PACKAGE = 7263;

	public _013_ParcelDelivery(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(FUNDIN);
		addTalkId(FUNDIN);
		addTalkId(VULCAN);
		
		questItemIds = new int[] {PACKAGE};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31274-2.htm"))
		{
			st.set("cond", "1");
			st.giveItems(PACKAGE, 1);
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31539-1.htm"))
		{
			st.takeItems(PACKAGE, 1);
			st.addExpAndSp(589092, 58794);
			st.giveItems(57, 157834);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (player.getLevel() >= 74)
					htmltext = "31274-0.htm";
				else
				{
					htmltext = "31274-1.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case FUNDIN:
						if (st.getInt("cond") == 1)
							htmltext = "31274-2.htm";		
						break;
					case VULCAN:
						if (st.getInt("cond") == 1 && st.getQuestItemsCount(PACKAGE) == 1)
							htmltext = "31539-0.htm";	
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _013_ParcelDelivery(13, qn, "");    	
	}
}
