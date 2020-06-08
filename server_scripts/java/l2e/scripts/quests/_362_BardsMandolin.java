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
public class _362_BardsMandolin extends Quest
{
	private final static String qn = "_362_BardsMandolin";
	
	// Items
	private static final int SWAN_FLUTE = 4316;
	private static final int SWAN_LETTER = 4317;
	
	// NPCs
	private static final int SWAN = 30957;
	private static final int NANARIN = 30956;
	private static final int GALION = 30958;
	private static final int WOODROW = 30837;

	public _362_BardsMandolin(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(SWAN);
		addTalkId(SWAN, NANARIN, GALION, WOODROW);

		questItemIds = new int[]
		{
			SWAN_FLUTE,
			SWAN_LETTER
		};
	}	
			
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30957-3.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30957-7.htm") || event.equalsIgnoreCase("30957-8.htm"))
		{
			st.rewardItems(57, 10000);
			st.giveItems(4410, 1);
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

		int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 15)
					htmltext = "30957-1.htm";
				else
				{
					htmltext = "30957-2.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case SWAN:
						if (cond == 1 || cond == 2)
							htmltext = "30957-4.htm";
						else if (cond == 3)
						{
							htmltext = "30957-5.htm";
							st.set("cond", "4");
							st.giveItems(SWAN_LETTER, 1);
							st.playSound("ItemSound.quest_middle");
						}
						else if (cond == 4)
							htmltext = "30957-5a.htm";
						else if (cond == 5)
							htmltext = "30957-6.htm";
						break;
					case WOODROW:
						if (cond == 1)
						{
							htmltext = "30837-1.htm";
							st.set("cond", "2");
							st.playSound("ItemSound.quest_middle");
						}
						else if (cond == 2)
							htmltext = "30837-2.htm";
						else if (cond > 2)
							htmltext = "30837-3.htm";
						break;
					case GALION:
						if (cond == 2)
						{
							htmltext = "30958-1.htm";
							st.set("cond", "3");
							st.giveItems(SWAN_FLUTE, 1);
							st.playSound("ItemSound.quest_itemget");
						}
						else if (cond >= 3)
							htmltext = "30958-2.htm";
						break;
					case NANARIN:
						if (cond == 4)
						{
							htmltext = "30956-1.htm";
							st.set("cond", "5");
							st.takeItems(SWAN_FLUTE, 1);
							st.takeItems(SWAN_LETTER, 1);
							st.playSound("ItemSound.quest_middle");
						}
						else if (cond == 5)
							htmltext = "30956-2.htm";
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _362_BardsMandolin(362, qn, "");	
	}
}