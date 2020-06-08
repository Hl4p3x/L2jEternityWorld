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
public class _370_AnElderSowsSeeds extends Quest
{
	private static final String qn = "_370_AnElderSowsSeeds";
	
	// NPC
	private static final int CASIAN = 30612;
	
	// Items
	private static final int SPELLBOOK_PAGE = 5916;
	private static final int CHAPTER_OF_FIRE = 5917;
	private static final int CHAPTER_OF_WATER = 5918;
	private static final int CHAPTER_OF_WIND = 5919;
	private static final int CHAPTER_OF_EARTH = 5920;

	public _370_AnElderSowsSeeds(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(CASIAN);
		addTalkId(CASIAN);
		
		addKillId(20082, 20084, 20086, 20089, 20090);

		questItemIds = new int[]
		{
			SPELLBOOK_PAGE,
			CHAPTER_OF_FIRE,
			CHAPTER_OF_WATER,
			CHAPTER_OF_WIND,
			CHAPTER_OF_EARTH
		};
	}	
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30612-3.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30612-6.htm"))
		{
			if (st.getQuestItemsCount(CHAPTER_OF_FIRE) > 0 && st.getQuestItemsCount(CHAPTER_OF_WATER) > 0 && st.getQuestItemsCount(CHAPTER_OF_WIND) > 0 && st.getQuestItemsCount(CHAPTER_OF_EARTH) > 0)
			{
				htmltext = "30612-8.htm";
				st.takeItems(CHAPTER_OF_FIRE, 1);
				st.takeItems(CHAPTER_OF_WATER, 1);
				st.takeItems(CHAPTER_OF_WIND, 1);
				st.takeItems(CHAPTER_OF_EARTH, 1);
				st.rewardItems(57, 3600);
			}
		}
		else if (event.equalsIgnoreCase("30612-9.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 28 && player.getLevel() <= 42)
					htmltext = "30612-0.htm";
				else
				{
					htmltext = "30612-0a.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				htmltext = "30612-4.htm";
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
		
		if (st.isStarted())
		{
			st.giveItems(SPELLBOOK_PAGE, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _370_AnElderSowsSeeds(370, qn, "");	
	}
}