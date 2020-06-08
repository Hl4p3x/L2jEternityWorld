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

/**
 * Created by LordWinter 20.05.2011 Based on L2J Eternity-World
 */
public class _192_SevenSignSeriesOfDoubt extends Quest
{
	private static final String qn = "_192_SevenSignSeriesOfDoubt";
	
	// NPC
	private static final int CROOP = 30676;
	private static final int HECTOR = 30197;
	private static final int STAN = 30200;
	private static final int CORPSE = 32568;
	private static final int HOLLINT = 30191;
	
	// ITEMS
	private static final int CROOP_INTRO = 13813;
	private static final int JACOB_NECK = 13814;
	private static final int CROOP_LETTER = 13815;
	
	public _192_SevenSignSeriesOfDoubt(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(CROOP);
		addTalkId(CROOP);
		addTalkId(HECTOR);
		addTalkId(STAN);
		addTalkId(CORPSE);
		addTalkId(HOLLINT);
		
		questItemIds = new int[]
		{
			CROOP_INTRO,
			JACOB_NECK,
			CROOP_LETTER
		};
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
		if (npc.getId() == CROOP)
		{
			if (event.equalsIgnoreCase("30676-03.htm"))
			{
				st.setState((byte) 1);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else
			{
				if (event.equals("8"))
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
					player.showQuestMovie(8);
					startQuestTimer("playertele", 32000L, npc, player);
					return "";
				}
				if (event.equalsIgnoreCase("playertele"))
				{
					player.teleToLocation(0x13ef6, 54848, -1514);
					return "";
				}
				if (event.equalsIgnoreCase("30676-12.htm"))
				{
					st.set("cond", "7");
					st.takeItems(JACOB_NECK, 1L);
					st.giveItems(CROOP_LETTER, 1L);
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		else if (npc.getId() == HECTOR)
		{
			if (event.equalsIgnoreCase("30197-03.htm"))
			{
				st.set("cond", "4");
				st.takeItems(CROOP_INTRO, 1L);
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npc.getId() == STAN)
		{
			if (event.equalsIgnoreCase("30200-04.htm"))
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npc.getId() == CORPSE)
		{
			if (event.equalsIgnoreCase("32568-02.htm"))
			{
				st.set("cond", "6");
				st.giveItems(JACOB_NECK, 1L);
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if ((npc.getId() == HOLLINT) && event.equalsIgnoreCase("30191-03.htm"))
		{
			st.takeItems(CROOP_LETTER, 1L);
			st.addExpAndSp(0x17d7840, 0x2625a0);
			st.unset("cond");
			st.setState((byte) 2);
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
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
		if (npc.getId() == CROOP)
		{
			switch (st.getState())
			{
				case 0:
					if ((st.getState() == 0) && (player.getLevel() >= 79))
					{
						htmltext = "30676-01.htm";
					}
					else
					{
						htmltext = "30676-00.htm";
						st.exitQuest(true);
					}
					break;
				case 1:
					if (st.getInt("cond") == 1)
					{
						htmltext = "30676-04.htm";
					}
					else if (st.getInt("cond") == 2)
					{
						htmltext = "30676-05.htm";
						st.set("cond", "3");
						st.playSound("ItemSound.quest_middle");
						st.giveItems(CROOP_INTRO, 1L);
					}
					else if ((st.getInt("cond") >= 3) && (st.getInt("cond") <= 5))
					{
						htmltext = "30676-06.htm";
					}
					else if (st.getInt("cond") == 6)
					{
						htmltext = "30676-07.htm";
					}
					break;
				case 2:
					htmltext = "30676-13.htm";
					break;
			}
		}
		else if (npc.getId() == HECTOR)
		{
			if (st.getState() == 1)
			{
				if (st.getInt("cond") == 3)
				{
					htmltext = "30197-01.htm";
				}
				else if ((st.getInt("cond") >= 4) && (st.getInt("cond") <= 7))
				{
					htmltext = "30197-04.htm";
				}
			}
		}
		else if (npc.getId() == STAN)
		{
			if (st.getInt("cond") == 4)
			{
				htmltext = "30200-01.htm";
			}
			else if ((st.getInt("cond") >= 5) && (st.getInt("cond") <= 7))
			{
				htmltext = "30200-05.htm";
			}
		}
		else if (npc.getId() == CORPSE)
		{
			if (st.getInt("cond") == 0)
			{
				htmltext = "32568-03.htm";
			}
			else if ((st.getInt("cond") >= 1) && (st.getInt("cond") <= 4))
			{
				htmltext = "32568-04.htm";
			}
			else if (st.getInt("cond") == 5)
			{
				htmltext = "32568-01.htm";
			}
		}
		else if ((npc.getId() == HOLLINT) && (st.getInt("cond") == 7))
		{
			htmltext = "30191-01.htm";
		}
		return htmltext;
	}
	
	public static void main(String args[])
	{
		new _192_SevenSignSeriesOfDoubt(192, qn, "");
	}
}