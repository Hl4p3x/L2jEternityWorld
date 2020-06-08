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
 * Created by LordWinter 28.09.2012
 * Based on L2J Eternity-World
 */
public class _300_HuntingLetoLizardman extends Quest
{
	private static final String qn = "_300_HuntingLetoLizardman";
	
	// NPC
	private static final int RATH = 30126;

	// Item
	private static final int BRACELET = 7139;
	
	public _300_HuntingLetoLizardman(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(RATH);
		addTalkId(RATH);

		addKillId(20577, 20578, 20579, 20580, 20582);

		questItemIds = new int[] { BRACELET };
	}	
			
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30126-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30126-05.htm"))
		{
			if (st.getQuestItemsCount(BRACELET) >= 60)
			{
				htmltext = "30126-06.htm";
				int luck = st.getRandom(3);
				st.takeItems(BRACELET, -1);
				if (luck == 0)
					st.rewardItems(57, 30000);
				else if (luck == 1)
					st.rewardItems(1867, 50);
				else if (luck == 2)
					st.rewardItems(1872, 50);			
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
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
				if (player.getLevel() >= 34 && player.getLevel() <= 39)
					htmltext = "30126-02.htm";
				else
				{
					htmltext = "30126-01.htm";
					st.exitQuest(true);
				}
				break;
			
			case State.STARTED:
				if (st.getQuestItemsCount(BRACELET) >= 60)
					htmltext = "30126-04.htm";
				else
					htmltext = "30126-04a.htm";
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		if (st.getRandom(100) < 33)
		{
			st.giveItems(BRACELET, 1);
			if (st.getQuestItemsCount(BRACELET) == 60)
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _300_HuntingLetoLizardman(300, qn, "");	
	}
}