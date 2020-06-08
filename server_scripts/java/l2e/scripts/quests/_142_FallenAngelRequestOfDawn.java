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
 * Created by LordWinter 16.01.2013 Based on L2J Eternity-World
 */
public class _142_FallenAngelRequestOfDawn extends Quest
{
	private static final String qn = "_142_FallenAngelRequestOfDawn";
	
	private final static int NATOOLS = 30894;
	private final static int RAYMOND = 30289;
	private final static int CASIAN = 30612;
	private final static int ROCK = 32368;
	
	private final static int CRYPT = 10351;
	private final static int FRAGMENT = 10352;
	private final static int BLOOD = 10353;
	
	private static final int[] MOBs =
	{
		20079,
		20080,
		20081,
		20082,
		20084,
		20086,
		20087,
		20088,
		20089,
		20090,
		27338
	};
	
	private int isAngelSpawned = 0;
	
	public _142_FallenAngelRequestOfDawn(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addTalkId(NATOOLS, RAYMOND, CASIAN, ROCK);
		
		for (int mob : MOBs)
		{
			addKillId(mob);
		}
		
		questItemIds = new int[]
		{
			CRYPT,
			FRAGMENT,
			BLOOD
		};
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("30894-01.htm"))
		{
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30894-03.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
			st.giveItems(CRYPT, 1);
		}
		else if (event.equalsIgnoreCase("30289-04.htm"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30612-07.htm"))
		{
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32368-02.htm"))
		{
			if (isAngelSpawned == 0)
			{
				addSpawn(27338, -21882, 186730, -4320, 0, false, 900000);
				isAngelSpawned = 1;
				startQuestTimer("angel_cleanup", 900000, null, player);
			}
		}
		else if (event.equalsIgnoreCase("angel_cleanup"))
		{
			if (isAngelSpawned == 1)
			{
				isAngelSpawned = 0;
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		final int cond = st.getInt("cond");
		final int npcId = npc.getId();
		final int id = st.getState();
		
		if (id == State.CREATED)
		{
			return htmltext;
		}
		
		if (id == State.COMPLETED)
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		else if (npcId == NATOOLS)
		{
			if (cond == 1)
			{
				htmltext = "30894-01.htm";
			}
			else if (cond == 2)
			{
				htmltext = "30894-04.htm";
			}
		}
		else if (npcId == RAYMOND)
		{
			if (cond == 2)
			{
				if (st.getInt("talk") == 1)
				{
					htmltext = "30289-02.htm";
				}
				else
				{
					htmltext = "30289-01.htm";
					st.takeItems(CRYPT, -1);
					st.set("talk", "1");
				}
			}
			else if (cond == 3)
			{
				htmltext = "30289-05.htm";
			}
			else if (cond == 6)
			{
				htmltext = "30289-06.htm";
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
				st.giveItems(57, 92676);
				st.takeItems(BLOOD, -1);
				if ((player.getLevel() >= 38) && (player.getLevel() <= 43))
				{
					st.addExpAndSp(223036, 13091);
				}
			}
		}
		else if (npcId == CASIAN)
		{
			if (cond == 3)
			{
				htmltext = "30612-01.htm";
			}
			else if (cond == 4)
			{
				htmltext = "30612-07.htm";
			}
		}
		else if (npcId == ROCK)
		{
			if (cond == 5)
			{
				htmltext = "32368-01.htm";
			}
			if (st.getInt("talk") != 1)
			{
				st.takeItems(BLOOD, -1);
				st.set("talk", "1");
			}
			else if (cond == 6)
			{
				htmltext = "32368-03.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		int cond = st.getInt("cond");
		
		if (npc.getId() == 27338)
		{
			if (cond == 5)
			{
				st.set("cond", "6");
				st.playSound("ItemSound.quest_middle");
				st.giveItems(BLOOD, 1);
				isAngelSpawned = 0;
			}
		}
		else if ((cond == 4) && (st.getQuestItemsCount(FRAGMENT) < 30))
		{
			st.dropQuestItems(FRAGMENT, 1, 30, 20, true);
			if (st.getQuestItemsCount(FRAGMENT) >= 30)
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _142_FallenAngelRequestOfDawn(142, qn, "");
	}
}