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
 * Created by LordWinter 06.07.2012
 * Based on L2J Eternity-World
 */
public class _117_TheOceanOfDistantStars extends Quest
{
	private static final String qn = "_117_TheOceanOfDistantStars";
	
	// NPCs
	private static final int ABEY = 32053;
	private static final int GHOST = 32055;
	private static final int GHOST_F = 32054;
	private static final int OBI = 32052;
	private static final int BOX = 32076;
	
	// Items
	private static final int GREY_STAR = 8495;
	private static final int ENGRAVED_HAMMER = 8488;
	
	// Monsters
	private static final int BANDIT_WARRIOR = 22023;
	private static final int BANDIT_INSPECTOR = 22024;

	public _117_TheOceanOfDistantStars(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(ABEY);
		addTalkId(ABEY, GHOST, GHOST_F, OBI, BOX);

		addKillId(BANDIT_WARRIOR, BANDIT_INSPECTOR);

		questItemIds = new int[] { GREY_STAR, ENGRAVED_HAMMER };
	}
			
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32053-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32055-02.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32052-02.htm"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32053-04.htm"))
		{
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32076-02.htm"))
		{
			st.set("cond", "5");
			st.giveItems(ENGRAVED_HAMMER, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32053-06.htm") && st.getQuestItemsCount(ENGRAVED_HAMMER) == 1)
		{
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32052-04.htm") && st.getQuestItemsCount(ENGRAVED_HAMMER) == 1)
		{
			st.set("cond", "7");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32052-06.htm") && st.getQuestItemsCount(GREY_STAR) == 1)
		{
			st.set("cond", "9");
			st.takeItems(GREY_STAR, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32055-04.htm") && st.getQuestItemsCount(ENGRAVED_HAMMER) == 1)
		{
			st.set("cond", "10");
			st.takeItems(ENGRAVED_HAMMER, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32054-03.htm"))
		{
			st.giveItems(57,17647);
			st.addExpAndSp(107387,7369);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
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
			case State.CREATED:
				if (player.getLevel() >= 39)
					htmltext = "32053-01.htm";
				else
				{
					htmltext = "32053-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getId())
				{
					case GHOST:
						if (cond == 1)
							htmltext = "32055-01.htm";
						else if (cond >= 2 && cond <= 8)
							htmltext = "32055-02.htm";
						else if (cond == 9 && st.getQuestItemsCount(ENGRAVED_HAMMER) == 1)
							htmltext = "32055-03.htm";
						else if (cond >= 10)
							htmltext = "32055-05.htm";
						break;
					case OBI:
						if (cond == 2)
							htmltext = "32052-01.htm";
						else if (cond >= 2 && cond <= 5)
							htmltext = "32052-02.htm";
						else if (cond == 6 && st.getQuestItemsCount(ENGRAVED_HAMMER) == 1)
							htmltext = "32052-03.htm";
						else if (cond == 7 && st.getQuestItemsCount(ENGRAVED_HAMMER) == 1)
							htmltext = "32052-04.htm";
						else if (cond == 8 && st.getQuestItemsCount(GREY_STAR) == 1)
							htmltext = "32052-05.htm";
						else if (cond >= 9)
							htmltext = "32052-06.htm";
						break;
					case ABEY:
						if (cond == 1 || cond == 2)
							htmltext = "32053-02.htm";
						else if (cond == 3)
							htmltext = "32053-03.htm";
						else if (cond == 4)
							htmltext = "32053-04.htm";
						else if (cond == 5 && st.getQuestItemsCount(ENGRAVED_HAMMER) == 1)
							htmltext = "32053-05.htm";
						else if (cond >= 6 && st.getQuestItemsCount(ENGRAVED_HAMMER) == 1)
							htmltext = "32053-06.htm";
						break;
					case BOX:
						if (cond == 4)
							htmltext = "32076-01.htm";
						else if (cond >= 5)
							htmltext = "32076-03.htm";
						break;
					case GHOST_F:
						if (cond == 10)
							htmltext = "32054-01.htm";
						break;
				}
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;
		
		if (st.getInt("cond") == 7 && st.getRandom(10) < 2)
		{
			st.set("cond", "8");
			st.giveItems(GREY_STAR, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _117_TheOceanOfDistantStars(117, qn, "");		
	}
}