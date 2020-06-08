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

import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 22.01.2011 Based on L2J Eternity-World
 */
public final class _643_RiseandFalloftheElrokiTribe extends Quest
{
	private static final String qn = "_643_RiseandFalloftheElrokiTribe";
	
	// NPCs
	private static final int SINGSING = 32106;
	private static final int SHAMAN_KARAKAWEI = 32117;
	
	// MOBs
	private static final int[] PLAIN_DINOSAURS =
	{
		22201,
		22202,
		22204,
		22205,
		22209,
		22210,
		22212,
		22213,
		22219,
		22220,
		22221,
		22222,
		22224,
		22225,
		22742,
		22743,
		22744,
		22745
	};
	
	// Quest Item
	private static final int BONES_OF_A_PLAINS_DINOSAUR = 8776;
	
	// Chance (100% = 1000)
	private static final int DROP_CHANCE = 750;
	
	// Rewards
	private static final int[] REWARDS =
	{
		8712,
		8713,
		8714,
		8715,
		8716,
		8717,
		8718,
		8719,
		8720,
		8721,
		8722
	};
	
	private static final FastMap<String, int[]> REWARDS_DYNA = new FastMap<>();
	
	static
	{
		REWARDS_DYNA.put("1", new int[]
		{
			9492,
			400
		});
		REWARDS_DYNA.put("2", new int[]
		{
			9493,
			250
		});
		REWARDS_DYNA.put("3", new int[]
		{
			9494,
			200
		});
		REWARDS_DYNA.put("4", new int[]
		{
			9495,
			134
		});
		REWARDS_DYNA.put("5", new int[]
		{
			9496,
			134
		});
		REWARDS_DYNA.put("6", new int[]
		{
			10115,
			287
		});
	}
	
	public _643_RiseandFalloftheElrokiTribe(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(SINGSING);
		
		addTalkId(SINGSING);
		addTalkId(SHAMAN_KARAKAWEI);
		
		for (int i : PLAIN_DINOSAURS)
		{
			addKillId(i);
		}
		
		questItemIds = new int[]
		{
			BONES_OF_A_PLAINS_DINOSAUR
		};
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(qn);
		
		if (st == null)
		{
			return htmltext;
		}
		
		long count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR);
		
		if (event.equalsIgnoreCase("32106-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32117-03.htm"))
		{
			if (count >= 300)
			{
				st.takeItems(BONES_OF_A_PLAINS_DINOSAUR, 300);
				st.rewardItems(REWARDS[getRandom(REWARDS.length - 1)], 5);
			}
			else
			{
				htmltext = "32117-04.htm";
			}
		}
		else if (REWARDS_DYNA.containsKey(event))
		{
			if (count >= REWARDS_DYNA.get(event)[1])
			{
				st.takeItems(BONES_OF_A_PLAINS_DINOSAUR, REWARDS_DYNA.get(event)[1]);
				st.rewardItems(REWARDS_DYNA.get(event)[0], 1);
				htmltext = "32117-06.htm";
			}
			else
			{
				htmltext = "32117-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("32106-07.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		
		if (st == null)
		{
			return htmltext;
		}
		
		int id = st.getState();
		final int cond = st.getInt("cond");
		int npcId = npc.getId();
		long count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR);
		
		switch (st.getState())
		{
			case State.CREATED:
				if (npcId == SINGSING)
				{
					if ((id == State.CREATED) && (cond == 0))
					{
						if (player.getLevel() >= 75)
						{
							htmltext = "32106-01.htm";
						}
						else
						{
							htmltext = "32106-00.htm";
							st.exitQuest(true);
						}
					}
				}
				break;
			case State.STARTED:
				if (npcId == SINGSING)
				{
					if (cond == 1)
					{
						if (count == 0)
						{
							htmltext = "32106-05.htm";
						}
						else
						{
							htmltext = "32106-05a.htm";
							st.takeItems(BONES_OF_A_PLAINS_DINOSAUR, -1);
							st.giveItems(57, count * 1374);
						}
					}
				}
				else if (npcId == SHAMAN_KARAKAWEI)
				{
					if (cond == 1)
					{
						htmltext = "32117-01.htm";
					}
					else
					{
						st.exitQuest(true);
					}
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
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
		
		final int cond = st.getInt("cond");
		long count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR);
		
		if (cond == 1)
		{
			int chance = (int) (DROP_CHANCE * Config.RATE_QUEST_DROP);
			int numItems = (chance / 1000);
			chance = chance % 1000;
			if (getRandom(1000) < chance)
			{
				numItems++;
			}
			if (numItems > 0)
			{
				if (((count + numItems) / 300) > (count / 300))
				{
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
				st.giveItems(BONES_OF_A_PLAINS_DINOSAUR, numItems);
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _643_RiseandFalloftheElrokiTribe(643, qn, "");
	}
}