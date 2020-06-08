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
 * Created by LordWinter 03.04.2011
 * Based on L2J Eternity-World
 */
public class _651_RunawayYouth extends Quest
{
	private static String qn = "_651_RunawayYouth";

	//Npc
	private static int IVAN = 32014;
	private static int BATIDAE = 31989;
	protected L2Npc _npc;

	//Items
	private static int SOE = 736;

	public _651_RunawayYouth(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(IVAN);
		addTalkId(BATIDAE);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("32014-03.htm"))
		{
			if(st.getQuestItemsCount(SOE) > 0)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				st.takeItems(SOE, 1);
				npc.deleteMe();
			}
			else
				htmltext = "32014-04.htm";
		}
		else if(event.equalsIgnoreCase("32014-04a.htm"))
		{
			st.exitQuest(true);
			st.playSound("ItemSound.quest_giveup");
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
		int id = st.getState();
		int cond = st.getInt("cond");

		if(id == State.CREATED)
		{
			if(npcId == IVAN && cond == 0)
			{
				if(player.getLevel() >= 26)
					htmltext = "32014-02.htm";
				else
				{
					htmltext = "32014-01.htm";
					st.exitQuest(true);
				}
			}
		}
		else if(id == State.STARTED)
		{
			if(npcId == BATIDAE && cond == 1)
			{
				htmltext = "31989-01.htm";
				st.giveItems(57, 2883);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _651_RunawayYouth(651, qn, "");
	}
}