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
 * Created by LordWinter 03.08.2011 Based on L2J Eternity-World
 */
public class _247_PossessorOfAPreciousSoul_4 extends Quest
{
	private static String qn = "_247_PossessorOfAPreciousSoul_4";
	
	// Npc
	private static final int CARADINE = 31740;
	private static final int LADY_OF_LAKE = 31745;
	
	// Quest Items
	private static final int CARADINE_LETTER_LAST = 7679;
	private static final int NOBLESS_TIARA = 7694;
	
	public _247_PossessorOfAPreciousSoul_4(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addStartNpc(CARADINE);
		addTalkId(CARADINE);
		addTalkId(LADY_OF_LAKE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		int cond = st.getInt("cond");
		
		if (event.equals("31740-3.htm"))
		{
			if (cond == 0)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equals("31740-5.htm"))
		{
			if (cond == 1)
			{
				st.set("cond", "2");
				st.takeItems(CARADINE_LETTER_LAST, 1);
				player.teleToLocation(143209, 43968, -3038);
			}
		}
		else if (event.equals("31745-5.htm"))
		{
			if (cond == 2)
			{
				st.set("cond", "0");
				player.setNoble(true);
				st.giveItems(NOBLESS_TIARA, 1);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if ((npc.getId() != CARADINE) && (st.getState() != State.STARTED))
		{
			return htmltext;
		}
		int cond = st.getInt("cond");
		if (st.getState() == State.CREATED)
		{
			st.set("cond", "0");
		}
		if (player.isSubClassActive())
		{
			if (npc.getId() == CARADINE)
			{
				if (st.getQuestItemsCount(CARADINE_LETTER_LAST) >= 1)
				{
					if ((cond == 0) || (cond == 1))
					{
						if (st.getState() == State.COMPLETED)
						{
							htmltext = getAlreadyCompletedMsg(player);
						}
						else if (player.getLevel() < 75)
						{
							htmltext = "31740-2.htm";
							st.exitQuest(true);
						}
						else if (player.getLevel() >= 75)
						{
							htmltext = "31740-1.htm";
						}
					}
				}
				else if (cond == 2)
				{
					htmltext = "31740-6.htm";
				}
			}
			else if ((npc.getId() == LADY_OF_LAKE) && (cond == 2))
			{
				htmltext = "31745-1.htm";
			}
		}
		else
		{
			htmltext = "31740-0.htm";
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new _247_PossessorOfAPreciousSoul_4(247, qn, "");
	}
}