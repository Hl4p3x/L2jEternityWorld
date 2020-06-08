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
public class _116_BeyondTheHillsOfWinter extends Quest
{
	private static final String qn = "_116_BeyondTheHillsOfWinter";
	
	// NPCs
	private static final int FILAUR = 30535;
	private static final int OBI = 32052;
	
	// Items
	private static final int BANDAGE = 1833;
	private static final int ENERGY_STONE = 5589;
	private static final int THIEF_KEY = 1661;
	private static final int GOODS = 8098;
	
	// Reward
	private static final int SSD = 1463;
	
	public _116_BeyondTheHillsOfWinter(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(FILAUR);
		addTalkId(FILAUR);
		addTalkId(OBI);

		questItemIds = new int[] { GOODS };
	}	
			
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30535-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30535-05.htm"))
		{
			st.set("cond", "3");
			st.giveItems(GOODS, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("materials"))
		{
			htmltext = "32052-02.htm";
			st.takeItems(GOODS, -1);
			st.rewardItems(SSD, 1650);
			st.addExpAndSp(82792,4981);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		else if (event.equalsIgnoreCase("adena"))
		{
			htmltext = "32052-02.htm";
			st.takeItems(GOODS, -1);
			st.giveItems(57, 16500);
			st.addExpAndSp(82792,4981);
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

		int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 30 && player.getRace().ordinal() == 4)
					htmltext = "30535-01.htm";
				else
				{
					htmltext = "30535-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case FILAUR:
						if (cond == 1)
						{
							if (st.getQuestItemsCount(BANDAGE) >= 20 && st.getQuestItemsCount(ENERGY_STONE) >= 5 && st.getQuestItemsCount(THIEF_KEY) >= 10)
							{
								htmltext = "30535-03.htm";
								st.set("cond","2");
								st.takeItems(BANDAGE, 20);
								st.takeItems(ENERGY_STONE, 5);
								st.takeItems(THIEF_KEY, 10);
							}
							else
								htmltext = "30535-04.htm";
						}
						else if (cond == 2)
							htmltext = "30535-03.htm";
						else if (cond == 3)
							htmltext = "30535-05.htm";
						break;
					
					case OBI:
						if (cond == 3 && st.getQuestItemsCount(GOODS) == 1)
							htmltext = "32052-00.htm";
						break;
				}
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _116_BeyondTheHillsOfWinter(116, qn, "");		
	}
}