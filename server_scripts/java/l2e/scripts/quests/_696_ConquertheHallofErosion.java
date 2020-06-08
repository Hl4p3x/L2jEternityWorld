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
 * Rework by LordWinter 12.09.2012
 * Based on L2J Eternity-World
 */
public final class _696_ConquertheHallofErosion extends Quest
{
	private static final String qn = "_696_ConquertheHallofErosion";

	// NPCs
	private static final int TEPIOS	= 32603;
	private static final int COHEMENES = 25634;

	// Quest Item
	private static final int MARK_OF_KEUCEREUS_STAGE_1 = 13691;
	private static final int MARK_OF_KEUCEREUS_STAGE_2 = 13692;

	public _696_ConquertheHallofErosion(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(TEPIOS);
		addTalkId(TEPIOS);

		addKillId(COHEMENES);

		questItemIds = new int[] { MARK_OF_KEUCEREUS_STAGE_1, MARK_OF_KEUCEREUS_STAGE_2 };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32603-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
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
				if (player.getLevel() >= 75)
				{
					if(st.getQuestItemsCount(MARK_OF_KEUCEREUS_STAGE_1) > 0 || st.getQuestItemsCount(MARK_OF_KEUCEREUS_STAGE_2) > 0)
					{
						htmltext = "32603-01.htm";
					}
					else
					{
						htmltext = "32603-05.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "32603-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if(st.getInt("cohemenesDone") != 0)
				{
					if(st.getQuestItemsCount(MARK_OF_KEUCEREUS_STAGE_2) < 1)
					{
						st.takeItems(MARK_OF_KEUCEREUS_STAGE_1, 1);
						st.giveItems(MARK_OF_KEUCEREUS_STAGE_2, 1);
					}
					htmltext = "32603-04.htm";
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(true);
				}
				else
				{
					htmltext = "32603-01a.htm";
				}
				break;
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, 1);
		
		if (partyMember == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		
		QuestState st = partyMember.getQuestState(qn);
		if (st == null)
		{
			return null;
		}

		int cond = st.getInt("cond");

    		if (cond == 1)
    		{
			st.set("cohemenesDone", 1);
    		}

		if (player.getParty() != null)
		{
			QuestState st2;
			for (L2PcInstance pmember : player.getParty().getMembers())
			{
				st2 = pmember.getQuestState(qn);
				if ((st2 != null) && (cond == 1) && (pmember.getObjectId() != partyMember.getObjectId()))
				{
					st.set("cohemenesDone", 1);
				}
			}
		}
    		return super.onKill(npc, player, isSummon);
  	}

	public static void main(String[] args)
	{
		new _696_ConquertheHallofErosion(696, qn, "");
	}
}