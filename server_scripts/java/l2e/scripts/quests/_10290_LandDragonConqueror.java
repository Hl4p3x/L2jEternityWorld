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
 * Fixed by L2J Etermity-World
 */
public class _10290_LandDragonConqueror extends Quest
{
	private static final String qn = "_10290_LandDragonConqueror";

	// NPC
	private static final int Theodoric = 30755;
	private static final int[] Antharas = {29019,29066,29067,29068};

	// Item
	private static final int PortalStone = 3865;
	private static final int ShabbyNecklace = 15522;
	private static final int MiracleNecklace = 15523;
	private static final int AntharaSlayerCirclet = 8568;

	public _10290_LandDragonConqueror(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(Theodoric);
		addTalkId(Theodoric);
		for (int i : Antharas)
			addKillId(i);
		
		questItemIds = new int[] {MiracleNecklace, ShabbyNecklace};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30755-07.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.giveItems(ShabbyNecklace, 1);
			st.playSound("ItemSound.quest_accept");
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
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				if (player.getLevel() >= 83 && st.getQuestItemsCount(PortalStone) >= 1)
					htmltext = "30755-01.htm";
				else if (player.getLevel() < 83)
					htmltext = "30755-02.htm";
				else
					htmltext = "30755-04.htm";
				break;
			}
			case State.STARTED:
			{
				if (st.getInt("cond") == 1 && st.getQuestItemsCount(ShabbyNecklace) >= 1)
					htmltext = "30755-08.htm";
				else if (st.getInt("cond") == 1 && st.getQuestItemsCount(ShabbyNecklace) == 0)
				{
					st.giveItems(ShabbyNecklace, 1);
					htmltext = "30755-09.htm";
				}
				else if (st.getInt("cond") == 2)
				{
					st.takeItems(MiracleNecklace, 1);
					st.giveItems(57, 131236);
					st.addExpAndSp(702557, 76334);
					st.giveItems(AntharaSlayerCirclet, 1);
					st.unset("cond");
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
					htmltext = "30755-10.htm";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = "31540-03.htm";
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
			st.takeItems(ShabbyNecklace, 1);
			st.giveItems(MiracleNecklace, 1);
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "2");
		}
	}
	
	public static void main(String[] args)
	{
		new _10290_LandDragonConqueror(10290, qn, "");
	}
}