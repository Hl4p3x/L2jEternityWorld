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
 * Created by LordWinter 13.01.2013 Based on L2J Eternity-World
 */
public class _372_LegacyOfInsolence extends Quest
{
	private static final String qn = "_372_LegacyOfInsolence";
	
	private static final int WALDERAL = 30844;
	private static final int PATRIN = 30929;
	private static final int HOLLY = 30839;
	private static final int CLAUDIA = 31001;
	private static final int DESMOND = 30855;
	
	private static final int[][] MONSTERS_DROPS =
	{
		{
			20817,
			20821,
			20825,
			20829,
			21069,
			21063
		},
		{
			5966,
			5966,
			5966,
			5967,
			5968,
			5969
		},
		{
			30,
			40,
			46,
			40,
			25,
			25
		}
	};
	
	private static final int[][] SCROLLS =
	{
		{
			5989,
			6001
		},
		{
			5984,
			5988
		},
		{
			5979,
			5983
		},
		{
			5972,
			5978
		},
		{
			5972,
			5978
		}
	};
	
	private static final int[][][] REWARDS_MATRICE =
	{
		{
			{
				13,
				5496
			},
			{
				26,
				5508
			},
			{
				40,
				5525
			},
			{
				58,
				5368
			},
			{
				76,
				5392
			},
			{
				100,
				5426
			}
		},
		{
			{
				13,
				5497
			},
			{
				26,
				5509
			},
			{
				40,
				5526
			},
			{
				58,
				5370
			},
			{
				76,
				5394
			},
			{
				100,
				5428
			}
		},
		{
			{
				20,
				5502
			},
			{
				40,
				5514
			},
			{
				58,
				5527
			},
			{
				73,
				5380
			},
			{
				87,
				5404
			},
			{
				100,
				5430
			}
		},
		{
			{
				20,
				5503
			},
			{
				40,
				5515
			},
			{
				58,
				5528
			},
			{
				73,
				5382
			},
			{
				87,
				5406
			},
			{
				100,
				5432
			}
		},
		{
			{
				33,
				5496
			},
			{
				66,
				5508
			},
			{
				89,
				5525
			},
			{
				100,
				57
			}
		},
		{
			{
				33,
				5497
			},
			{
				66,
				5509
			},
			{
				89,
				5526
			},
			{
				100,
				57
			}
		},
		{
			{
				35,
				5502
			},
			{
				70,
				5514
			},
			{
				87,
				5527
			},
			{
				100,
				57
			}
		},
		{
			{
				35,
				5503
			},
			{
				70,
				5515
			},
			{
				87,
				5528
			},
			{
				100,
				57
			}
		}
	};
	
	public _372_LegacyOfInsolence(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(WALDERAL);
		addTalkId(WALDERAL, PATRIN, HOLLY, CLAUDIA, DESMOND);
		
		addKillId(MONSTERS_DROPS[0]);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30844-04.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30844-05b.htm"))
		{
			if (st.getInt("cond") == 1)
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("30844-07.htm"))
		{
			for (int blueprint = 5989; blueprint <= 6001; blueprint++)
			{
				if (!st.hasQuestItems(blueprint))
				{
					htmltext = "30844-06.htm";
					break;
				}
			}
		}
		else if (event.startsWith("30844-07-"))
		{
			checkAndRewardItems(st, 0, Integer.parseInt(event.substring(9, 10)), WALDERAL);
		}
		else if (event.equalsIgnoreCase("30844-09.htm"))
		{
			st.playSound("ItemSound.quest_finish");
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
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() < 59)
				{
					htmltext = "30844-01.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "30844-02.htm";
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case WALDERAL:
						htmltext = "30844-05.htm";
						break;
					
					case HOLLY:
						htmltext = checkAndRewardItems(st, 1, 4, HOLLY);
						break;
					
					case PATRIN:
						htmltext = checkAndRewardItems(st, 2, 5, PATRIN);
						break;
					
					case CLAUDIA:
						htmltext = checkAndRewardItems(st, 3, 6, CLAUDIA);
						break;
					
					case DESMOND:
						htmltext = checkAndRewardItems(st, 4, 7, DESMOND);
						break;
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
			return null;
		
		final int npcId = npc.getId();
		for (int index = 0; index < MONSTERS_DROPS[0].length; index++)
		{
			if (MONSTERS_DROPS[0][index] == npcId)
			{
				if (getRandom(100) < MONSTERS_DROPS[2][index])
				{
					QuestState st = partyMember.getQuestState(qn);
					
					st.rewardItems(MONSTERS_DROPS[1][index], 1);
					st.playSound("ItemSound.quest_itemget");
				}
				break;
			}
		}
		return null;
	}
	
	private static String checkAndRewardItems(QuestState st, int itemType, int rewardType, int npcId)
	{
		final int[] itemsToCheck = SCROLLS[itemType];
		
		for (int item = itemsToCheck[0]; item <= itemsToCheck[1]; item++)
			if (!st.hasQuestItems(item))
				return npcId + ((npcId == WALDERAL) ? "-07a.htm" : "-01.htm");
		
		for (int item = itemsToCheck[0]; item <= itemsToCheck[1]; item++)
			st.takeItems(item, 1);
		
		final int[][] rewards = REWARDS_MATRICE[rewardType];
		final int chance = getRandom(100);
		
		for (int[] reward : rewards)
		{
			if (chance < reward[0])
			{
				st.rewardItems(reward[1], 1);
				return npcId + "-02.htm";
			}
		}
		return npcId + ((npcId == WALDERAL) ? "-07a.htm" : "-01.htm");
	}
	
	public static void main(String[] args)
	{
		new _372_LegacyOfInsolence(372, qn, "");
	}
}