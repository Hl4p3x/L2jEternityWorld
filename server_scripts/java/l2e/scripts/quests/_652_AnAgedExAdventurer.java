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
 * Created by LordWinter 02.04.2011
 * Based on L2J Eternity-World
 */
public class _652_AnAgedExAdventurer extends Quest
{
	private static String qn = "_652_AnAgedExAdventurer";

	//NPC
	private static final int Tantan = 32012;
	private static final int Sara = 30180;

	//Item
	private static final int SSC = 1464;
	private static final int EAD = 956;

	public _652_AnAgedExAdventurer(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(Tantan);
		addTalkId(Sara);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("32012-02.htm") && st.getQuestItemsCount(SSC) >= 100)
		{
			if (st.getQuestItemsCount(SSC) >= 100)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.takeItems(SSC, 100);
				st.playSound("ItemSound.quest_accept");
				npc.deleteMe();
			}
			else
				htmltext = "32012-02a.htm";
		}
		else if(event.equalsIgnoreCase("32012-03.htm"))
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
			if(npcId == Tantan & cond == 0)
				if(player.getLevel() < 46)
				{
					htmltext = "32012-00.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "32012-01.htm";
		}
		else if(id == State.STARTED)
		{
			if(npcId == Sara && cond == 1)
			{
				htmltext = "30180-01.htm";
				st.giveItems(57, 10000);
				if(getRandom(100) < (50))
					st.giveItems(EAD, 1);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _652_AnAgedExAdventurer(652, qn, "");
	}
}