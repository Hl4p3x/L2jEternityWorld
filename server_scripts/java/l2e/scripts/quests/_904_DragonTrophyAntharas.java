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
 * Created by LordWinter 11.05.2012
 * Based on L2J Eternity-World
 */
public class _904_DragonTrophyAntharas extends Quest
{
	private static final String qn = "_904_DragonTrophyAntharas";

	private static final int Theodric = 30755;
	private static final int AntharasMax = 29068;
	private static final int MedalofGlory = 21874;

	public _904_DragonTrophyAntharas(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(Theodric);
		addTalkId(Theodric);

		addKillId(AntharasMax);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("30755-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30755-07.htm"))
		{
			st.giveItems(MedalofGlory, 3000);
			st.setState(State.COMPLETED);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
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

		int cond = st.getInt("cond");

		if(npc.getId() == Theodric)
		{
			switch(st.getState())
			{
				case State.CREATED:
					if(player.getLevel() >= 84)
					{
						if(st.getQuestItemsCount(3865) > 0)
							htmltext = "30755-01.htm";
						else
							htmltext = "30755-00b.htm";
					}
					else
					{
						htmltext = "30755-00.htm";
						st.exitQuest(true);
					}
					break;
				case State.STARTED:
					if(cond == 1)
						htmltext = "30755-05.htm";
					else if(cond == 2)
						htmltext = "30755-06.htm";
					break;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (player.getParty() != null)
		{
			for (L2PcInstance partyMember : player.getParty().getMembers())
				rewardPlayer(partyMember);
		}
		else
			rewardPlayer(player);
		return null;
	}

	private void rewardPlayer(L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);

		if (st != null && st.getInt("cond") == 1)
		{
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "2");
		}
	}

	public static void main(String[] args)
	{
		new _904_DragonTrophyAntharas(904, qn, "");
	}
}