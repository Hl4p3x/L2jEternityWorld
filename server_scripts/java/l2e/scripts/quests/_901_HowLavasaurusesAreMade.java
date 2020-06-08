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

import org.apache.commons.lang.ArrayUtils;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.QuestState.QuestType;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 15.05.2012 Based on L2J Eternity-World
 */
public class _901_HowLavasaurusesAreMade extends Quest
{
	private static final String qn = "_901_HowLavasaurusesAreMade";
	
	// NPC's
	private static final int ROONEY = 32049;
	
	// Item's
	private static final int TOTEM_OF_BODY = 21899;
	private static final int TOTEM_OF_SPIRIT = 21900;
	private static final int TOTEM_OF_COURAGE = 21901;
	private static final int TOTEM_OF_FORTITUDE = 21902;
	
	// Quest Item's
	private static final int LAVASAURUS_STONE_FRAGMENT = 21909;
	private static final int LAVASAURUS_HEAD_FRAGMENT = 21910;
	private static final int LAVASAURUS_BODY_FRAGMENT = 21911;
	private static final int LAVASAURUS_HORN_FRAGMENT = 21912;
	
	// Monster's
	private static final int[] KILLING_MONSTERS = new int[]
	{
		18799,
		18800,
		18801,
		18802,
		18803
	};
	
	// Chance's
	private static final int DROP_CHANCE = 5;
	
	public _901_HowLavasaurusesAreMade(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(ROONEY);
		addTalkId(ROONEY);
		
		for (int mobs : KILLING_MONSTERS)
		{
			addKillId(mobs);
		}
		
		questItemIds = new int[]
		{
			LAVASAURUS_STONE_FRAGMENT,
			LAVASAURUS_HEAD_FRAGMENT,
			LAVASAURUS_BODY_FRAGMENT,
			LAVASAURUS_HORN_FRAGMENT
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
		
		if (event.equalsIgnoreCase("32049-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32049-12a.htm"))
		{
			st.giveItems(TOTEM_OF_BODY, 1);
			st.playSound("ItemSound.quest_finish");
			st.setState(State.COMPLETED);
			st.exitQuest(QuestType.DAILY);
		}
		else if (event.equalsIgnoreCase("32049-12b.htm"))
		{
			st.giveItems(TOTEM_OF_SPIRIT, 1);
			st.playSound("ItemSound.quest_finish");
			st.setState(State.COMPLETED);
			st.exitQuest(QuestType.DAILY);
		}
		else if (event.equalsIgnoreCase("32049-12c.htm"))
		{
			st.giveItems(TOTEM_OF_FORTITUDE, 1);
			st.playSound("ItemSound.quest_finish");
			st.setState(State.COMPLETED);
			st.exitQuest(QuestType.DAILY);
		}
		else if (event.equalsIgnoreCase("32049-12d.htm"))
		{
			st.giveItems(TOTEM_OF_COURAGE, 1);
			st.playSound("ItemSound.quest_finish");
			st.setState(State.COMPLETED);
			st.exitQuest(QuestType.DAILY);
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
		
		if (npcId == ROONEY)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 76)
					{
						htmltext = "32049-01.htm";
					}
					else
					{
						htmltext = "32049-00.htm";
					}
					break;
				case State.STARTED:
					if (cond == 1)
					{
						htmltext = "32049-04.htm";
					}
					else if (cond == 2)
					{
						if (st.getInt("collect") == 1)
						{
							htmltext = "32049-07.htm";
						}
						else
						{
							if ((st.getQuestItemsCount(LAVASAURUS_STONE_FRAGMENT) >= 10) && (st.getQuestItemsCount(LAVASAURUS_HEAD_FRAGMENT) >= 10) && (st.getQuestItemsCount(LAVASAURUS_BODY_FRAGMENT) >= 10) && (st.getQuestItemsCount(LAVASAURUS_HORN_FRAGMENT) >= 10))
							{
								htmltext = "32049-05.htm";
								st.takeItems(LAVASAURUS_STONE_FRAGMENT, -1);
								st.takeItems(LAVASAURUS_HEAD_FRAGMENT, -1);
								st.takeItems(LAVASAURUS_BODY_FRAGMENT, -1);
								st.takeItems(LAVASAURUS_HORN_FRAGMENT, -1);
								st.set("collect", "1");
							}
							else
							{
								htmltext = "32049-06.htm";
							}
						}
					}
					break;
				case State.COMPLETED:
					if (st.isNowAvailable())
					{
						if (player.getLevel() >= 76)
						{
							htmltext = "32049-01.htm";
						}
						else
						{
							htmltext = "32049-00.htm";
						}
					}
					else
					{
						htmltext = "32049-01a.htm";
					}
					break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
		{
			return null;
		}
		
		QuestState st = partyMember.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		int id = st.getState();
		final int cond = st.getInt("cond");
		
		if (id == State.STARTED)
		{
			if (cond == 1)
			{
				if (ArrayUtils.contains(KILLING_MONSTERS, npc.getId()))
				{
					if ((getRandom(100) < DROP_CHANCE) && (st.getQuestItemsCount(LAVASAURUS_STONE_FRAGMENT) < 10))
					{
						st.giveItems(LAVASAURUS_STONE_FRAGMENT, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					else if ((getRandom(100) < DROP_CHANCE) && (st.getQuestItemsCount(LAVASAURUS_HEAD_FRAGMENT) < 10))
					{
						st.giveItems(LAVASAURUS_HEAD_FRAGMENT, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					else if ((getRandom(100) < DROP_CHANCE) && (st.getQuestItemsCount(LAVASAURUS_BODY_FRAGMENT) < 10))
					{
						st.giveItems(LAVASAURUS_BODY_FRAGMENT, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					else if ((getRandom(100) < DROP_CHANCE) && (st.getQuestItemsCount(LAVASAURUS_HORN_FRAGMENT) < 10))
					{
						st.giveItems(LAVASAURUS_HORN_FRAGMENT, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
				if ((st.getQuestItemsCount(LAVASAURUS_STONE_FRAGMENT) >= 10) && (st.getQuestItemsCount(LAVASAURUS_HEAD_FRAGMENT) >= 10) && (st.getQuestItemsCount(LAVASAURUS_BODY_FRAGMENT) >= 10) && (st.getQuestItemsCount(LAVASAURUS_HORN_FRAGMENT) >= 10))
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _901_HowLavasaurusesAreMade(901, qn, "");
	}
}