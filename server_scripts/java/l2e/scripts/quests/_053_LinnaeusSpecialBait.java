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

public class _053_LinnaeusSpecialBait extends Quest
{
	private static final String qn = "_053_LinnaeusSpecialBait";

	// NPC
	private static final int Linnaeu = 31577;

	// REWARDS
	private static final int FlameFishingLure = 7613;

	// ITEMS
	private static final int HeartOfCrimsonDrake = 7624;

	// MONSTERS
	private static final int CrimsonDrake = 20670;

	public _053_LinnaeusSpecialBait(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(Linnaeu);
		addTalkId(Linnaeu);
		addKillId(CrimsonDrake);

		questItemIds = new int[] { HeartOfCrimsonDrake };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31577-1.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31577-3.htm"))
		{
			if (st.getQuestItemsCount(HeartOfCrimsonDrake) < 100)
				htmltext = "31577-4.htm";
			else
			{
				htmltext = "31577-3.htm";
				st.unset("cond");
				st.takeItems(HeartOfCrimsonDrake, -1);
				st.giveItems(FlameFishingLure, 4);
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
			return htmltext;

		int npcId = npc.getId();
		int cond = st.getInt("cond");
		
		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		if (npcId == Linnaeu)
			if (cond == 1)
				htmltext = "31577-04.htm";
			else if (cond == 2)
				htmltext = "31577-05.htm";
			else if (cond == 0)
				if (player.getLevel() > 59 && player.getLevel() < 63)
					htmltext = "31577-01.htm";
				else
				{
					htmltext = "31577-02.htm";
					st.exitQuest(true);
				}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null) 
			return null;

		int npcId = npc.getId();

		if (npcId == CrimsonDrake && st.getInt("cond") == 1)
		{
			if (st.getQuestItemsCount(HeartOfCrimsonDrake) < 100 && getRandom(100) < 30)
			{
				st.giveItems(HeartOfCrimsonDrake, 1);
				if (st.getQuestItemsCount(HeartOfCrimsonDrake) == 100)
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "2");
				}
				else
					st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _053_LinnaeusSpecialBait(53, qn, "");
	}
}