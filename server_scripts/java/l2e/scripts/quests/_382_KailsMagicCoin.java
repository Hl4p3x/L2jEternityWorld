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

import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 19.01.2013 Based on L2J Eternity-World
 */
public class _382_KailsMagicCoin extends Quest
{
	private static final String qn = "_382_KailsMagicCoin";

	private static int ROYAL_MEMBERSHIP = 5898;

	private static int VERGARA = 30687;

	private static final Map<Integer, int[]> MOBS = new HashMap<>();

	static
	{
		MOBS.put(21017, new int[] { 5961 });
		MOBS.put(21019, new int[] { 5962 });
		MOBS.put(21020, new int[] { 5963 });
		MOBS.put(21022, new int[] { 5961, 5962, 5963 });
	}

	public _382_KailsMagicCoin(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(VERGARA);
		addTalkId(VERGARA);

		for(int mobId : MOBS.keySet())
		{
			addKillId(mobId);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("30687-03.htm"))
		{
			if(player.getLevel() >= 55 && st.getQuestItemsCount(ROYAL_MEMBERSHIP) > 0)
			{
            			st.set("cond","1");
            			st.setState(State.STARTED);
            			st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = "30687-01.htm";
				st.exitQuest(true);
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		int cond = st.getInt("cond");

		if(st.getQuestItemsCount(ROYAL_MEMBERSHIP) == 0 || player.getLevel() < 55)
		{
			htmltext = "30687-01.htm";
			st.exitQuest(true);
		}
		else if(cond == 0)
		{
			htmltext = "30687-02.htm";
		}
		else
		{
			htmltext = "30687-04.htm";
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(qn);
		if(!st.isStarted() || st.getQuestItemsCount(ROYAL_MEMBERSHIP) == 0)
		{
			return null;
		}

		int[] droplist = MOBS.get(npc.getId());
		st.dropQuestItems(droplist[getRandom(droplist.length)], 1, 1000000, 10, true);

		return null;
	}

	public static void main(String[] args)
	{
		new _382_KailsMagicCoin(382, qn, "");
	}
}