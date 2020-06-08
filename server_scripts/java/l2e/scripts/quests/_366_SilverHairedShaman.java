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
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public class _366_SilverHairedShaman extends Quest
{
	private static final String qn = "_366_SilverHairedShaman";
	
	// NPC
	private static final int DIETER = 30111;
	
	// Item
	private static final int HAIR = 5874;
	
	public _366_SilverHairedShaman(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(DIETER);
		addTalkId(DIETER);
		
		addKillId(20986, 20987, 20988);

		questItemIds = new int[]
		{
			HAIR
		};
	}	
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30111-2.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30111-6.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 48 && player.getLevel() <= 58)
					htmltext = "30111-1.htm";
				else
				{
					htmltext = "30111-0.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				long count = st.getQuestItemsCount(HAIR);
				if (count == 0)
					htmltext = "30111-3.htm";
				else
				{
					htmltext = "30111-4.htm";
					st.takeItems(HAIR, -1);
					st.rewardItems(57, 12070 + 500 * count);
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMemberState(player, State.STARTED);
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		if (st.getRandom(100) < 55)
		{
			st.rewardItems(HAIR, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
		
	public static void main(String[] args)
	{
		new _366_SilverHairedShaman(366, qn, "");		
	}
}