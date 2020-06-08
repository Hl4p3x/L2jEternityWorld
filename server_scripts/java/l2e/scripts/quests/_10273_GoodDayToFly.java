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

import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.skills.L2Skill;

/**
 * Created by LordWinter 31.01.2012 Based on L2J Eternity-World
 */
public class _10273_GoodDayToFly extends Quest
{
	private static final String qn = "_10273_GoodDayToFly";

	// NPCs
	private static final int LEKON = 32557;

	private static final int[] MOBS =
	{
	                22614, 22615
	};

	// Items
	private static final int MARK = 13856;

	// Skills
	private static final L2Skill AuraBirdFalcon = SkillHolder.getInstance().getInfo(5982, 1);
	private static final L2Skill AuraBirdOwl = SkillHolder.getInstance().getInfo(5983, 1);

	public _10273_GoodDayToFly(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(LEKON);
		addTalkId(LEKON);
		addKillId(MOBS);

		questItemIds = new int[]
		{
			MARK
		};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}

		switch (event)
		{
			case "32557-06.htm":
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				break;
			case "32557-09.htm":
				st.set("transform", "1");
				AuraBirdFalcon.getEffects(player, player);
				break;
			case "32557-10.htm":
				st.set("transform", "2");
				AuraBirdOwl.getEffects(player, player);
				break;
			case "32557-13.htm":
				if (st.getInt("transform") == 1)
				{
					AuraBirdFalcon.getEffects(player, player);
				}
				else if (st.getInt("transform") == 2)
				{
					AuraBirdOwl.getEffects(player, player);
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}

		final int transform = st.getInt("transform");
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = "32557-0a.htm";
				break;
			case State.CREATED:
				htmltext = (player.getLevel() < 75) ? "32557-00.htm" : "32557-01.htm";
				break;
			default:
				if (st.getQuestItemsCount(MARK) >= 5)
				{
					htmltext = "32557-14.htm";
					if (transform == 1)
					{
						st.giveItems(13553, 1);
					}
					else if (transform == 2)
					{
						st.giveItems(13554, 1);
					}
					st.giveItems(13857, 1);
					st.addExpAndSp(25160, 2525);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
				}
				else if (transform == 0)
				{
					htmltext = "32557-07.htm";
				}
				else
				{
					htmltext = "32557-11.htm";
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState st = killer.getQuestState(qn);
		if ((st == null) || !st.isStarted())
		{
			return null;
		}

		final long count = st.getQuestItemsCount(MARK);
		if ((st.getInt("cond") == 1) && (count < 5))
		{
			st.giveItems(MARK, 1);
			if (count == 4)
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "2");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _10273_GoodDayToFly(10273, qn, "");
	}
}
