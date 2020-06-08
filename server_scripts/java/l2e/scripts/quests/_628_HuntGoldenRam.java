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

import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 12.01.2013 Based on L2J Eternity-World
 */
public class _628_HuntGoldenRam extends Quest
{
	private static final String qn = "_628_HuntGoldenRam";
	
	private static final int KAHMAN = 31554;
	private static final int CHITIN = 7248;
	private static final int CHITIN2 = 7249;
	private static final int RECRUIT = 7246;
	private static final int SOLDIER = 7247;
	
	private static final Map<Integer, Integer> chances = new HashMap<>();
	
	public _628_HuntGoldenRam(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(KAHMAN);
		addTalkId(KAHMAN);
		
		addKillId(21508, 21509, 21510, 21511, 21512, 21513, 21514, 21515, 21516, 21517);
		
		chances.put(Integer.valueOf(21508), Integer.valueOf(250000));
		chances.put(Integer.valueOf(21509), Integer.valueOf(210000));
		chances.put(Integer.valueOf(21510), Integer.valueOf(260000));
		chances.put(Integer.valueOf(21511), Integer.valueOf(260000));
		chances.put(Integer.valueOf(21512), Integer.valueOf(370000));
		
		chances.put(Integer.valueOf(21513), Integer.valueOf(250000));
		chances.put(Integer.valueOf(21514), Integer.valueOf(210000));
		chances.put(Integer.valueOf(21515), Integer.valueOf(250000));
		chances.put(Integer.valueOf(21516), Integer.valueOf(260000));
		chances.put(Integer.valueOf(21517), Integer.valueOf(370000));
		
		questItemIds = new int[]
		{
			CHITIN,
			CHITIN2,
			RECRUIT,
			SOLDIER
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
		
		if (event.equalsIgnoreCase("31554-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31554-03a.htm"))
		{
			if ((st.getQuestItemsCount(CHITIN) >= 100) && (st.getInt("cond") == 1))
			{
				st.set("cond", "2");
				st.takeItems(CHITIN, -1);
				st.giveItems(RECRUIT, 1);
				htmltext = "31554-04.htm";
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("31554-07.htm"))
		{
			st.playSound("ItemSound.quest_giveup");
			st.exitQuest(true);
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
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 66)
				{
					htmltext = "31554-01.htm";
				}
				else
				{
					htmltext = "31554-01a.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
				{
					if (st.getQuestItemsCount(CHITIN) >= 100)
					{
						htmltext = "31554-03.htm";
					}
					else
					{
						htmltext = "31554-03a.htm";
					}
				}
				else if (cond == 2)
				{
					if ((st.getQuestItemsCount(CHITIN) >= 100) && (st.getQuestItemsCount(CHITIN2) >= 100))
					{
						htmltext = "31554-05.htm";
						st.takeItems(CHITIN, -1);
						st.takeItems(CHITIN2, -1);
						st.takeItems(RECRUIT, 1);
						st.giveItems(SOLDIER, 1);
						st.set("cond", "3");
						st.playSound("ItemSound.quest_finish");
					}
					else if ((!st.hasQuestItems(CHITIN)) && (!st.hasQuestItems(CHITIN2)))
					{
						htmltext = "31554-04b.htm";
					}
					else
					{
						htmltext = "31554-04a.htm";
					}
				}
				else if (cond == 3)
				{
					htmltext = "31554-05a.htm";
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
		{
			return null;
		}
		
		QuestState st = partyMember.getQuestState(qn);
		
		int cond = st.getInt("cond");
		int npcId = npc.getId();
		switch (npcId)
		{
			case 21508:
			case 21509:
			case 21510:
			case 21511:
			case 21512:
				if ((cond == 1) || (cond == 2))
				{
					st.dropItems(CHITIN, 1, 100, chances.get(Integer.valueOf(npcId)).intValue());
				}
				break;
			case 21513:
			case 21514:
			case 21515:
			case 21516:
			case 21517:
				if (cond == 2)
				{
					st.dropItems(CHITIN2, 1, 100, chances.get(Integer.valueOf(npcId)).intValue());
				}
				break;
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _628_HuntGoldenRam(628, qn, "");
	}
}