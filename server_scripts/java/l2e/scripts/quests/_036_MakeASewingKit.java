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
 * Created by LordWinter 24.05.2011
 * Based on L2J Eternity-World
 */
public class _036_MakeASewingKit extends Quest
{
	private static final String qn = "_036_MakeASewingKit";

	// ITEMS
	private static final int ARTISANS_FRAME 	= 1891;
	private static final int ORIHARUKON 		= 1893;
	private static final int REINFORCED_STEEL 	= 7163;
	private static final int SEWING_KIT 		= 7078;

	public _036_MakeASewingKit(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(30847);
		addTalkId(30847);
		addKillId(20566);
		questItemIds = new int[] { REINFORCED_STEEL };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		int cond = st.getInt("cond");
		if (event.equalsIgnoreCase("30847-1.htm") && cond == 0)
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30847-3.htm") && cond == 2)
		{
			st.takeItems(REINFORCED_STEEL, 5);
			st.set("cond", "3");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30847-4a.htm"))
		{
			st.takeItems(ORIHARUKON, 10);
			st.takeItems(ARTISANS_FRAME, 10);
			st.giveItems(SEWING_KIT, 1);
			st.playSound("ItemSound.quest_finish");
			st.unset("cond");
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);

		int cond = st.getInt("cond");

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		if (cond == 0 && st.getQuestItemsCount(SEWING_KIT) == 0)
		{
			if (player.getLevel() >= 60)
			{
				QuestState fwear = player.getQuestState("_037_PleaseMakeMeFormalWear");
				if (fwear != null && fwear.getState() == State.STARTED)
				{
					if (fwear.get("cond").equals("6"))
						htmltext = "30847-0.htm";
					else
					{
						htmltext = "30847-5.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "30847-5.htm";
					st.exitQuest(true);
				}
			}
			else
				htmltext = "30847-5.htm";
		}
		else if (cond == 1 && st.getQuestItemsCount(REINFORCED_STEEL) < 5)
			htmltext = "30847-1a.htm";
		else if (cond == 2 && st.getQuestItemsCount(REINFORCED_STEEL) == 5)
			htmltext = "30847-2.htm";
		else if (cond == 3 && st.getQuestItemsCount(ORIHARUKON) >= 10 && st.getQuestItemsCount(ARTISANS_FRAME) >= 10)
			htmltext = "30847-4.htm";
		else
			htmltext = "30847-3a.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);

		if (st.getQuestItemsCount(REINFORCED_STEEL) < 5)
		{
			st.giveItems(REINFORCED_STEEL, 1);
			if (st.getQuestItemsCount(REINFORCED_STEEL) == 5)
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "2");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _036_MakeASewingKit(36, qn, "");
	}
}