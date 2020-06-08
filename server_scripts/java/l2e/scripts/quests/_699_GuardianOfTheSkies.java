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

import org.apache.commons.lang.ArrayUtils;

import l2e.Config;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 24.06.2011 Based on L2J Eternity-World
 */
public final class _699_GuardianOfTheSkies extends Quest
{
	private static final String qn = "_699_GuardianOfTheSkies";
	
	// NPCs
	private static final int LEKON = 32557;
	
	// Quet Item
	private static final int GOLDEN_FEATHER = 13871;
	
	// MOBs
	private static final int[] MOBS =
	{
		22614,
		22615,
		25623,
		25633
	};
	
	// Chance
	private static final int DROP_CHANCE = 80;
	
	public _699_GuardianOfTheSkies(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(LEKON);
		addTalkId(LEKON);
		
		for (int i : MOBS)
		{
			addKillId(i);
		}
		
		questItemIds = new int[]
		{
			GOLDEN_FEATHER
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("32557-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32557-quit.htm"))
		{
			st.unset("cond");
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
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
		
		if (npc.getId() == LEKON)
		{
			QuestState first = player.getQuestState("_10273_GoodDayToFly");
			if ((first != null) && (first.getState() == State.COMPLETED) && (st.getState() == State.CREATED) && (player.getLevel() >= 75))
			{
				htmltext = "32557-01.htm";
			}
			else
			{
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = "32557-00.htm";
						break;
					case State.STARTED:
						long count = st.getQuestItemsCount(GOLDEN_FEATHER);
						if (count > 0)
						{
							st.takeItems(GOLDEN_FEATHER, -1);
							st.giveItems(57, count * 2300);
							st.playSound("ItemSound.quest_itemget");
							htmltext = "32557-06.htm";
						}
						else
						{
							htmltext = "32557-04.htm";
						}
						break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		if ((st.getInt("cond") == 1) && ArrayUtils.contains(MOBS, npc.getId()))
		{
			int chance = (int) (DROP_CHANCE * Config.RATE_QUEST_DROP);
			int numItems = (chance / 100);
			chance = chance % 100;
			if (st.getRandom(100) < chance)
			{
				numItems++;
			}
			if (numItems > 0)
			{
				st.giveItems(GOLDEN_FEATHER, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _699_GuardianOfTheSkies(699, qn, "");
	}
}