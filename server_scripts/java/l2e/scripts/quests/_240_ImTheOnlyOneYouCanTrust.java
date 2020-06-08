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
 * Created by LordWinter 15.03.2011
 * Based on L2J Eternity-World
 */
public class _240_ImTheOnlyOneYouCanTrust extends Quest
{
	private static final String qn = "_240_ImTheOnlyOneYouCanTrust";

	// QUEST ITEMS
	private final static int STAKATOFANGS = 14879;

	// NPC
	private final static int KINTAIJIN = 32640;

	// MOBS
	private final static int[] MOBS = { 22617, 22624, 22625, 22626 };

	public _240_ImTheOnlyOneYouCanTrust(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(KINTAIJIN);
		addTalkId(KINTAIJIN);
		for (int i : MOBS)
			addKillId(i);

		questItemIds = new int[] { STAKATOFANGS };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32640-3.htm"))
		{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if(st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = "32640-10.htm";
				break;
			case State.CREATED:
	            if (player.getLevel() >= 81)
	            	htmltext = "32640-1.htm";
	            else
	            {
					htmltext = "32640-0.htm";
					st.exitQuest(true);
	            }
		        break;
			case State.STARTED:
				switch (st.getInt("cond"))
				{
					case 1:
						htmltext = "32640-8.htm";
						break;
					case 2:
						st.takeItems(STAKATOFANGS,-1);
						st.addExpAndSp(589542,36800);
						st.giveItems(57, 147200);
						st.exitQuest(false);
						st.playSound("ItemSound.quest_finish");
						htmltext = "32640-9.htm";
						break;
				}
		        break;
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null || st.getState() != State.STARTED)
			return null;

		if (getRandom(100) <= 50)
		{
			st.giveItems(STAKATOFANGS,1);
			if (st.getQuestItemsCount(STAKATOFANGS) >= 25)
			{
				st.set("cond","2");
				st.playSound("ItemSound.quest_middle");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String[] args)
	{
		new _240_ImTheOnlyOneYouCanTrust(240, qn, "");
	}
}
