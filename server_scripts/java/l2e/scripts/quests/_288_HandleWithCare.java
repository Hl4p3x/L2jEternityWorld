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

public class _288_HandleWithCare extends Quest
{
	private static final String qn = "_288_HandleWithCare";

	private static final int _ankumi = 32741;

	private static final int _mid_scale = 15498;
	private static final int _high_scale = 15497;

	private static final int[][] _rewards =
	{
		{ 959, 1 },
		{ 960, 3 },
		{ 9557, 1 }
	};

	public _288_HandleWithCare(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(_ankumi);
		addTalkId(_ankumi);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;

		if (npc.getId() == _ankumi)
		{
			if (event.equalsIgnoreCase("32741-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("32741-07.htm"))
			{
				if (st.hasQuestItems(_mid_scale))
				{
					st.takeItems(_mid_scale, 1);
					int[] _rnd = _rewards[getRandom(0,_rewards.length-1)];
					st.giveItems(_rnd[0], getRandom(1, _rnd[1]));
				}
				else if (st.hasQuestItems(_high_scale))
				{
					st.takeItems(_high_scale, 1);
					int[] _rnd = _rewards[getRandom(0,_rewards.length-2)];
					st.giveItems(_rnd[0], getRandom(1, _rnd[1]));
					st.giveItems(_rewards[2][0], 1);
				}
				st.unset("cond");
				st.unset("drop");
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
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
			return htmltext;

		if (npc.getId() == _ankumi)
		{
			switch(st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 82)
						htmltext = "32741-01.htm";
					else
						htmltext = "32741-00.htm";
					break;
				case State.STARTED:
					if (st.getInt("cond") == 2 && st.hasQuestItems(_mid_scale))
						htmltext = "32741-05.htm";
					else if (st.getInt("cond") == 3 && st.hasQuestItems(_high_scale))
						htmltext = "32741-06.htm";
					else
						htmltext = "32741-04.htm";
					break;
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _288_HandleWithCare(288, qn, "");
	}
}