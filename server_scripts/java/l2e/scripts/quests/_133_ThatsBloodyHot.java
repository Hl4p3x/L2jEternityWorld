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
 * Created by LordWinter 24.06.2011
 * Based on L2J Eternity-World
 */
public final class _133_ThatsBloodyHot extends Quest
{
	private static final String qn = "_133_ThatsBloodyHot";

	// NPC's
	private static final int KANIS		= 32264;
	private static final int GALATE		= 32292;

	// ITEMS
	private static final int CRYSTAL_SAMPLE	= 9785;

	public _133_ThatsBloodyHot(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(KANIS);
		addTalkId(KANIS);
		addTalkId(GALATE);

		questItemIds = new int[] { CRYSTAL_SAMPLE };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32264-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32264-07.htm"))
		{
			st.set("cond", "2");
			st.giveItems(CRYSTAL_SAMPLE, 1);
		}
		else if (event.equals("32292-04.htm"))
		{
			st.takeItems(CRYSTAL_SAMPLE, 1);
			st.giveItems(57, 254247);
			st.addExpAndSp(331457, 32524);
			st.setState(State.COMPLETED);
			st.exitQuest(false);
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

		final int npcId = npc.getId();
		final int cond = st.getInt("cond");
		QuestState qs131 = player.getQuestState("_131_BirdInACage");

		if (st.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}

		switch (npcId)
		{
			case KANIS:
				switch (cond)
				{
					case 0:
						if (qs131 != null && qs131.isCompleted() && player.getLevel() >= 78)
							htmltext = "32264-01.htm";
						else
							htmltext = "32264-00.htm";
						break;
					case 1:
						htmltext = "32264-02.htm";
						break;
					case 2:
						htmltext = "32264-07.htm";
						break;
				}
				break;
			case GALATE:
				switch (cond)
				{
					case 2:
						htmltext = "32292-01.htm";
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _133_ThatsBloodyHot(133, qn, "");
	}
}