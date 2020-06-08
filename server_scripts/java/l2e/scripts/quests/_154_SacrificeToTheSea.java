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
public class _154_SacrificeToTheSea extends Quest
{
	private final static String qn = "_154_SacrificeToTheSea";
	
	// NPCs
	private static final int ROCKSWELL = 30312;
	private static final int CRISTEL = 30051;
	private static final int ROLFE = 30055;
	
	// Items
	private static final int FOX_FUR = 1032;
	private static final int FOX_FUR_YARN = 1033;
	private static final int MAIDEN_DOLL = 1034;
	
	// Reward
	private static final int EARING = 113;

	public _154_SacrificeToTheSea(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(ROCKSWELL);
		addTalkId(ROCKSWELL, CRISTEL, ROLFE);
		
		addKillId(20481, 20544, 20545);

		questItemIds = new int[] { FOX_FUR, FOX_FUR_YARN, MAIDEN_DOLL };
	}	
			
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30312-04.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
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
				if (player.getLevel() >= 2 && player.getLevel() <= 7)
					htmltext = "30312-03.htm";
				else
				{
					htmltext = "30312-02.htm";
					st.exitQuest(true);
				}
				break;
			
			case State.STARTED:
				switch (npc.getId())
				{
					case ROCKSWELL:
						if (cond == 1)
							htmltext = "30312-05.htm";
						else if (cond == 2 && st.getQuestItemsCount(FOX_FUR) >= 10)
							htmltext = "30312-08.htm";
						else if (cond == 3 && st.getQuestItemsCount(FOX_FUR_YARN) >= 1)
							htmltext = "30312-06.htm";
						else if (cond == 4 && st.getQuestItemsCount(MAIDEN_DOLL) >= 1)
						{
							htmltext = "30312-07.htm";
							st.giveItems(EARING, 1);
							st.takeItems(MAIDEN_DOLL, -1);
							st.addExpAndSp(100, 0);
							st.playSound("ItemSound.quest_finish");
							st.exitQuest(false);
						}
						break;
					
					case CRISTEL:
						if (cond == 1)
						{
							if (st.getQuestItemsCount(FOX_FUR) > 0)
								htmltext = "30051-01.htm";
							else
								htmltext = "30051-01a.htm";
						}
						else if (cond == 2 && st.getQuestItemsCount(FOX_FUR) >= 10)
						{
							htmltext = "30051-02.htm";
							st.giveItems(FOX_FUR_YARN, 1);
							st.takeItems(FOX_FUR, -1);
							st.set("cond", "3");
							st.playSound("ItemSound.quest_middle");
						}
						else if (cond == 3 && st.getQuestItemsCount(FOX_FUR_YARN) >= 1)
							htmltext = "30051-03.htm";
						else if (cond == 4 && st.getQuestItemsCount(MAIDEN_DOLL) >= 1)
							htmltext = "30051-04.htm";
						break;
					
					case ROLFE:
						if (cond == 3 && st.getQuestItemsCount(FOX_FUR_YARN) >= 1)
						{
							htmltext = "30055-01.htm";
							st.giveItems(MAIDEN_DOLL, 1);
							st.takeItems(FOX_FUR_YARN, -1);
							st.set("cond", "4");
							st.playSound("ItemSound.quest_middle");
						}
						else if (cond == 4 && st.getQuestItemsCount(MAIDEN_DOLL) >= 1)
							htmltext = "30055-02.htm";
						else if (cond >= 1 && cond <= 2)
							htmltext = "30055-03.htm";
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
		
		if (st.getInt("cond") == 1 && st.getRandom(10) < 4)
		{
			st.giveItems(FOX_FUR, 1);
			if (st.getQuestItemsCount(FOX_FUR) == 10)
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "2");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _154_SacrificeToTheSea(154, qn, "");	
	}
}