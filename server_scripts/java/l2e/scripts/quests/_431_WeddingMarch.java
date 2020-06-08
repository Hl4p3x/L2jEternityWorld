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
 * Created by LordWinter 25.09.2012 Based on L2J Eternity-World
 */
public class _431_WeddingMarch extends Quest
{
	private static final String qn = "_431_WeddingMarch";
	
	private static final int MELODY_MAESTRO_KANTABILON_ID = 31042;
	private static final int SILVER_CRYSTAL_ID = 7540;
	private static final int LIENRIKS_ID = 20786;
	private static final int LIENRIKS_LAD_ID = 20787;
	private static final int WEDDING_ECHO_CRYSTAL_ID = 7062;
	
	public _431_WeddingMarch(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(MELODY_MAESTRO_KANTABILON_ID);
		addTalkId(MELODY_MAESTRO_KANTABILON_ID);
		
		addKillId(LIENRIKS_ID);
		addKillId(LIENRIKS_LAD_ID);
		
		questItemIds = new int[]
		{
			SILVER_CRYSTAL_ID
		};
	}
	
	@Override
	public final String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		
		if (event.equalsIgnoreCase("1"))
		{
			htmltext = "31042-02.htm";
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("3") && (st.getQuestItemsCount(SILVER_CRYSTAL_ID) == 50))
		{
			st.giveItems(WEDDING_ECHO_CRYSTAL_ID, 25);
			st.takeItems(SILVER_CRYSTAL_ID, 50);
			htmltext = "31042-05.htm";
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = "31042-01.htm";
				break;
			case State.STARTED:
				if (cond == 1)
				{
					htmltext = "31042-03.htm";
				}
				else if (cond == 2)
				{
					htmltext = "31042-04.htm";
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
			return null;
		}
		QuestState st = partyMember.getQuestState(qn);
		if (st.getInt("cond") == 1)
		{
			if (st.dropQuestItems(SILVER_CRYSTAL_ID, 1, 1, 50, false, 100, true))
			{
				st.set("cond", "2");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _431_WeddingMarch(431, qn, "");
	}
}