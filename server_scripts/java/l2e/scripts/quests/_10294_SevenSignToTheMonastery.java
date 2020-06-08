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
import l2e.gameserver.network.serverpackets.SocialAction;

public final class _10294_SevenSignToTheMonastery extends Quest
{
	private static final String qn = "_10294_SevenSignToTheMonastery";
	
	// NPCs
	private static int ELCARDIA = 32784;
	private static int ELCARDIA2 = 32787;
	private static int EVIL = 32792;
	private static int GUARDIAN = 32803;
	private static int WEST_WATCHER = 32804;
	private static int NORTH_WATCHER = 32805;
	private static int EAST_WATCHER = 32806;
	private static int SOUTH_WATCHER = 32807;
	private static int WEST_DESK_1 = 32821;
	private static int WEST_DESK_2 = 32822;
	private static int WEST_DESK_3 = 32823;
	private static int WEST_DESK_4 = 32824;
	private static int NORTH_DESK_1 = 32825;
	private static int NORTH_DESK_2 = 32826;
	private static int NORTH_DESK_3 = 32827;
	private static int NORTH_DESK_4 = 32828;
	private static int EAST_DESK_1 = 32829;
	private static int EAST_DESK_2 = 32830;
	private static int EAST_DESK_3 = 32831;
	private static int EAST_DESK_4 = 32832;
	private static int SOUTH_DESK_1 = 32833;
	private static int SOUTH_DESK_2 = 32834;
	private static int SOUTH_DESK_3 = 32835;
	private static int SOUTH_DESK_4 = 32836;
	
	private static final int[] NPCs =
	{
		ELCARDIA,
		EVIL,
		ELCARDIA2,
		GUARDIAN,
		WEST_WATCHER,
		NORTH_WATCHER,
		EAST_WATCHER,
		SOUTH_WATCHER,
		WEST_DESK_1,
		NORTH_DESK_1,
		EAST_DESK_1,
		SOUTH_DESK_1
	};
	private static final int[] NPC =
	{
		WEST_DESK_2,
		WEST_DESK_3,
		WEST_DESK_4,
		NORTH_DESK_2,
		NORTH_DESK_3,
		NORTH_DESK_4,
		EAST_DESK_2,
		EAST_DESK_3,
		EAST_DESK_4,
		SOUTH_DESK_2,
		SOUTH_DESK_3,
		SOUTH_DESK_4
	};
	
	private boolean isAllBooksFinded(QuestState st)
	{
		return (st.getInt("book_" + WEST_DESK_1) + st.getInt("book_" + NORTH_DESK_1) + st.getInt("book_" + EAST_DESK_1) + st.getInt("book_" + SOUTH_DESK_1)) >= 4;
	}
	
	public _10294_SevenSignToTheMonastery(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(ELCARDIA);
		
		for (int npcs : NPCs)
		{
			addTalkId(npcs);
		}
		
		for (int npc : NPC)
		{
			addFirstTalkId(npc);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		
		if (npcId == ELCARDIA)
		{
			if (event.equalsIgnoreCase("32784-03.htm"))
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (npcId == EVIL)
		{
			if (event.equalsIgnoreCase("32792-03.htm"))
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
			else if (event.equalsIgnoreCase("32792-08.htm"))
			{
				if (player.isSubClassActive())
				{
					htmltext = "32792-10.htm";
				}
				else
				{
					st.addExpAndSp(25000000, 2500000);
					player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
					st.setState(State.COMPLETED);
					st.unset("cond");
					st.unset("book_32821");
					st.unset("book_32825");
					st.unset("book_32829");
					st.unset("book_32833");
					st.unset("first");
					st.unset("second");
					st.unset("third");
					st.unset("fourth");
					st.unset("movie");
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
					htmltext = "32792-08.htm";
				}
			}
		}
		else if (npcId == WEST_DESK_1)
		{
			if (event.equalsIgnoreCase("32821-02.htm"))
			{
				st.playSound("ItemSound.quest_middle");
				st.set("book_" + npc.getId(), 1);
				st.set("first", "1");
				if (isAllBooksFinded(st))
				{
					npc.setDisplayEffect(1);
					player.showQuestMovie(25);
					st.set("movie", "1");
					return "";
				}
			}
		}
		else if (npcId == NORTH_DESK_1)
		{
			if (event.equalsIgnoreCase("32825-02.htm"))
			{
				st.playSound("ItemSound.quest_middle");
				st.set("book_" + npc.getId(), 1);
				st.set("second", "1");
				if (isAllBooksFinded(st))
				{
					npc.setDisplayEffect(1);
					player.showQuestMovie(25);
					st.set("movie", "1");
					return "";
				}
			}
		}
		else if (npcId == EAST_DESK_1)
		{
			if (event.equalsIgnoreCase("32829-02.htm"))
			{
				st.playSound("ItemSound.quest_middle");
				st.set("book_" + npc.getId(), 1);
				st.set("third", "1");
				if (isAllBooksFinded(st))
				{
					npc.setDisplayEffect(1);
					player.showQuestMovie(25);
					st.set("movie", "1");
					return "";
				}
			}
		}
		else if (npcId == SOUTH_DESK_1)
		{
			if (event.equalsIgnoreCase("32833-02.htm"))
			{
				st.playSound("ItemSound.quest_middle");
				st.set("book_" + npc.getId(), 1);
				st.set("fourth", "1");
				if (isAllBooksFinded(st))
				{
					npc.setDisplayEffect(1);
					player.showQuestMovie(25);
					st.set("movie", "1");
					return "";
				}
			}
		}
		return htmltext;
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
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		int first = st.getInt("first");
		int second = st.getInt("second");
		int third = st.getInt("third");
		int fourth = st.getInt("fourth");
		int movie = st.getInt("movie");
		
		if (st.getState() == State.CREATED)
		{
			if (npcId == ELCARDIA)
			{
				QuestState qs = player.getQuestState("_10293_SevenSignsForbiddenBook");
				if (cond == 0)
				{
					if ((player.getLevel() >= 81) && (qs != null) && qs.isCompleted())
					{
						htmltext = "32784-01.htm";
					}
					else
					{
						htmltext = "32784-00.htm";
						st.exitQuest(true);
					}
				}
			}
		}
		else if (st.getState() == State.STARTED)
		{
			if (npcId == ELCARDIA)
			{
				if (cond == 1)
				{
					htmltext = "32784-04.htm";
				}
			}
			else if ((npcId == WEST_WATCHER) && (cond == 2))
			{
				if (st.getInt("book_" + WEST_DESK_1) > 0)
				{
					htmltext = "32804-05.htm";
				}
				else
				{
					htmltext = "32804-01.htm";
				}
			}
			else if ((npcId == NORTH_WATCHER) && (cond == 2))
			{
				if (st.getInt("book_" + NORTH_DESK_1) > 0)
				{
					htmltext = "32805-05.htm";
				}
				else
				{
					htmltext = "32805-01.htm";
				}
			}
			else if ((npcId == EAST_WATCHER) && (cond == 2))
			{
				if (st.getInt("book_" + EAST_DESK_1) > 0)
				{
					htmltext = "32806-05.htm";
				}
				else
				{
					htmltext = "32806-01.htm";
				}
			}
			else if ((npcId == SOUTH_WATCHER) && (cond == 2))
			{
				if (st.getInt("book_" + SOUTH_DESK_1) > 0)
				{
					htmltext = "32807-05.htm";
				}
				else
				{
					htmltext = "32807-01.htm";
				}
			}
			else if (npcId == ELCARDIA2)
			{
				if (cond == 1)
				{
					htmltext = "32787-01.htm";
				}
				else if (cond == 2)
				{
					htmltext = "32787-02.htm";
				}
				else if (cond == 3)
				{
					htmltext = "32787-03.htm";
				}
			}
			else if (npcId == EVIL)
			{
				if (cond == 1)
				{
					htmltext = "32792-01.htm";
				}
				else if (cond == 2)
				{
					htmltext = "32792-06.htm";
				}
				else if (cond == 3)
				{
					htmltext = "32792-07.htm";
				}
			}
			else if (npcId == GUARDIAN)
			{
				if (cond == 2)
				{
					if (isAllBooksFinded(st))
					{
						htmltext = "32803-04.htm";
						st.set("cond", "3");
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						htmltext = "32803-01.htm";
					}
				}
				else if (cond == 3)
				{
					htmltext = "32803-05.htm";
				}
			}
			else if (npcId == WEST_WATCHER)
			{
				htmltext = "32804-01.htm";
			}
			else if (npcId == NORTH_WATCHER)
			{
				htmltext = "32805-01.htm";
			}
			else if (npcId == EAST_WATCHER)
			{
				htmltext = "32806-01.htm";
			}
			else if (npcId == SOUTH_WATCHER)
			{
				htmltext = "32807-01.htm";
			}
			else if (npcId == WEST_DESK_1)
			{
				if ((movie == 1) || (first == 1))
				{
					htmltext = "empty_desk.htm";
				}
				else
				{
					htmltext = "32821-01.htm";
				}
			}
			else if (npcId == NORTH_DESK_1)
			{
				if ((movie == 1) || (second == 1))
				{
					htmltext = "empty_desk.htm";
				}
				else
				{
					htmltext = "32825-01.htm";
				}
			}
			else if (npcId == EAST_DESK_1)
			{
				if ((movie == 1) || (third == 1))
				{
					htmltext = "empty_desk.htm";
				}
				else
				{
					htmltext = "32829-01.htm";
				}
			}
			else if (npcId == SOUTH_DESK_1)
			{
				if ((movie == 1) || (fourth == 1))
				{
					htmltext = "empty_desk.htm";
				}
				else
				{
					htmltext = "32833-01.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		
		int npcId = npc.getId();
		
		if ((npcId == WEST_DESK_2) || (npcId == WEST_DESK_3) || (npcId == WEST_DESK_4) || (npcId == NORTH_DESK_2) || (npcId == NORTH_DESK_3) || (npcId == NORTH_DESK_4) || (npcId == EAST_DESK_2) || (npcId == EAST_DESK_3) || (npcId == EAST_DESK_4) || (npcId == SOUTH_DESK_2) || (npcId == SOUTH_DESK_3) || (npcId == SOUTH_DESK_4))
		{
			return "empty_desk.htm";
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new _10294_SevenSignToTheMonastery(10294, qn, "");
	}
}