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
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.QuestState.QuestType;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 04.01.2013 Based on L2J Eternity-World
 */
public class _464_Oath extends Quest
{
	private static final int[][] NPC =
	{
		{32596,	0, 0, 0},
		{30657,	15449, 17696, 42910},
		{30839, 189377, 21692, 52599},
		{30899,	249180, 28542, 69210},
		{31350, 249180, 28542, 69210},
		{30539,	19408, 47062, 169442},
		{30297,	24146, 58551, 210806},
		{31960,	15449, 17696, 42910},
		{31588,	15449, 17696, 42910}
	};
	
	private static final int STRONGBOX = 15537;
	private static final int BOOK = 15538;
	private static final int BOOK2 = 15539;

	private static final int MIN_LEVEL = 82;
	
	private static final Map<Integer, Integer> MOBS = new HashMap<>();
	
	static
	{
		MOBS.put(22799, 9);
		MOBS.put(22794, 6);
		MOBS.put(22800, 10);
		MOBS.put(22796, 9);
		MOBS.put(22798, 9);
		MOBS.put(22795, 8);
		MOBS.put(22797, 7);
		MOBS.put(22789, 5);
		MOBS.put(22791, 4);
		MOBS.put(22790, 5);
		MOBS.put(22792, 4);
		MOBS.put(22793, 5);
	}

	public _464_Oath(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int[] npc : NPC)
		{
			addTalkId(npc[0]);
		}

		addKillId(MOBS.keySet());

		registerQuestItems(BOOK, BOOK2);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		String htmltext = event;
		switch (event)
		{
			case "32596-04.htm":
				if (!st.hasQuestItems(BOOK))
				{
					return getNoQuestMsg(player);
				}
				
				int cond = getRandom(2, 9);
				st.set("npc", String.valueOf(NPC[cond - 1][0]));
				st.setCond(cond, true);
				st.takeItems(BOOK, 1);
				st.giveItems(BOOK2, 1);
				switch (cond)
				{
					case 2:
						htmltext = "32596-04.htm";
						break;
					case 3:
						htmltext = "32596-04a.htm";
						break;
					case 4:
						htmltext = "32596-04b.htm";
						break;
					case 5:
						htmltext = "32596-04c.htm";
						break;
					case 6:
						htmltext = "32596-04d.htm";
						break;
					case 7:
						htmltext = "32596-04e.htm";
						break;
					case 8:
						htmltext = "32596-04f.htm";
						break;
					case 9:
						htmltext = "32596-04g.htm";
						break;
				}
				break;
			case "end_quest":
				if (!st.hasQuestItems(BOOK2))
				{
					return getNoQuestMsg(player);
				}
				
				int i = st.getCond() - 1;
				st.addExpAndSp(NPC[i][1], NPC[i][2]);
				st.giveAdena(NPC[i][3], true);
				st.exitQuest(QuestType.DAILY, true);
				htmltext = npc.getId() + "-02.htm";
				break;
			case "32596-02.htm":
			case "32596-03.htm":
				break;
			default:
				htmltext = null;
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		
		if ((st != null) && st.isStarted())
		{
			int npcId = npc.getId();
			
			if (npcId == NPC[0][0])
			{
				switch (st.getCond())
				{
					case 1:
						htmltext = "32596-01.htm";
						break;
					case 2:
						htmltext = "32596-05.htm";
						break;
					case 3:
						htmltext = "32596-05a.htm";
						break;
					case 4:
						htmltext = "32596-05b.htm";
						break;
					case 5:
						htmltext = "32596-05c.htm";
						break;
					case 6:
						htmltext = "32596-05d.htm";
						break;
					case 7:
						htmltext = "32596-05e.htm";
						break;
					case 8:
						htmltext = "32596-05f.htm";
						break;
					case 9:
						htmltext = "32596-05g.htm";
						break;
				}
			}
			else if ((st.getCond() > 1) && (st.getInt("npc") == npcId))
			{
				htmltext = npcId + "-01.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onItemTalk(L2ItemInstance item, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		boolean startQuest = false;
		switch (st.getState())
		{
			case State.CREATED:
				startQuest = true;
				break;
			case State.STARTED:
				htmltext = "strongbox-02.htm";
				break;
			case State.COMPLETED:
				if (st.isNowAvailable())
				{
					st.setState(State.CREATED);
					startQuest = true;
				}
				else
				{
					htmltext = "strongbox-03.htm";
				}
				break;
		}
		
		if (startQuest)
		{
			if (player.getLevel() >= MIN_LEVEL)
			{
				st.startQuest();
				st.takeItems(STRONGBOX, 1);
				st.giveItems(BOOK, 1);
				htmltext = "strongbox-01.htm";
			}
			else
			{
				htmltext = "strongbox-00.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (getRandom(1000) < MOBS.get(npc.getId()))
		{
			((L2MonsterInstance) npc).dropItem(killer, STRONGBOX, 1);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _464_Oath(464, _464_Oath.class.getSimpleName(), "");
	}
}