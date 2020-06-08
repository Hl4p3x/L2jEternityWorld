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
public class _10291_FireDragonDestroyer extends Quest
{
	private static final String qn = "_10291_FireDragonDestroyer";

	// NPC
	private static final int Klein = 31540;
	private static final int Valakas = 29028;

	// Item
	private static final int FloatingStone = 7267;
	private static final int PoorNecklace = 15524;
	private static final int ValorNecklace = 15525;
	private static final int ValakaSlayerCirclet = 8567;

	public _10291_FireDragonDestroyer(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(Klein);
		addTalkId(Klein);
		addKillId(Valakas);
		
		questItemIds = new int[] {PoorNecklace, ValorNecklace};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31540-07.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.giveItems(PoorNecklace, 1);
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
				if (player.getLevel() >= 83 && st.getQuestItemsCount(FloatingStone) >= 1)
					htmltext = "31540-01.htm";
				else if (player.getLevel() < 83)
					htmltext = "31540-02.htm";
				else
					htmltext = "31540-04.htm";
				break;
			}
			case State.STARTED:
			{
				if (st.getInt("cond") == 1 && st.getQuestItemsCount(PoorNecklace) >= 1)
					htmltext = "31540-08.htm";
				else if (st.getInt("cond") == 1 && st.getQuestItemsCount(PoorNecklace) == 0)
				{
					st.giveItems(PoorNecklace, 1);
					htmltext = "31540-09.htm";
				}
				else if (st.getInt("cond") == 2)
				{
					st.takeItems(ValorNecklace, 1);
					st.giveItems(57, 126549);
					st.addExpAndSp(717291, 77397);
					st.giveItems(ValakaSlayerCirclet, 1);
					st.unset("cond");
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
					htmltext = "31540-10.htm";
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
			st.takeItems(PoorNecklace, 1);
			st.giveItems(ValorNecklace, 1);
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "2");
		}
	}
	
	public static void main(String[] args)
	{
		new _10291_FireDragonDestroyer(10291, qn, "");
	}
}