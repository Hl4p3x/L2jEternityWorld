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
 * Based on L2J Eternity-World
 */
public class _182_NewRecruits extends Quest
{
	private static final String qn = "_182_NewRecruits";
	// NPC's
	private static final int _kekropus = 32138;
	private static final int _nornil = 32258;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getId() == _kekropus)
		{
			if (event.equalsIgnoreCase("32138-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (npc.getId() == _nornil)
		{
			if (event.equalsIgnoreCase("32258-04.htm"))
			{
				st.giveItems(847, 2);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else if (event.equalsIgnoreCase("32258-05.htm"))
			{
				st.giveItems(890, 2);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
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
		
		if (player.getRace().ordinal() == 5)
		{
			return "32138-00.htm";
		}
		
		if (player.getLevel() < 17)
		{
			return "32138-00a.htm";
		}
		
		if (npc.getId() == _kekropus)
		{
			switch (st.getState())
			{
				case State.CREATED:
					htmltext = "32138-01.htm";
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
					{
						htmltext = "32138-04.htm";
					}
					break;
				case State.COMPLETED:
					htmltext = getAlreadyCompletedMsg(player);
					break;
			}
		}
		else if ((npc.getId() == _nornil) && (st.getState() == State.STARTED))
		{
			htmltext = "32258-01.htm";
		}
		else if ((npc.getId() == _nornil) && (st.getState() == State.COMPLETED))
		{
			htmltext = "32258-exit.htm";
		}
		return htmltext;
	}
	
	public _182_NewRecruits(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_kekropus);
		addTalkId(_kekropus);
		addTalkId(_nornil);
	}
	
	public static void main(String[] args)
	{
		new _182_NewRecruits(182, qn, "");
	}
}