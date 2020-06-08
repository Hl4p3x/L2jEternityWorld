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

import l2e.Config;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

public class _617_GatherTheFlames extends Quest
{
	final private static String qn = "_617_GatherTheFlames";
	
	// NPC's
	final private static int HILDA = 31271;
	final private static int VULCAN = 31539;
	final private static int ROONEY = 32049;
	
	// ITEMS
	final private static int TORCH = 7264;
	final private static int[][] DROPLIST =
	{
		{
			21381,
			51
		},
		{
			21653,
			51
		},
		{
			21387,
			53
		},
		{
			21655,
			53
		},
		{
			21390,
			56
		},
		{
			21656,
			69
		},
		{
			21389,
			55
		},
		{
			21388,
			53
		},
		{
			21383,
			51
		},
		{
			21392,
			56
		},
		{
			21382,
			60
		},
		{
			21654,
			52
		},
		{
			21384,
			64
		},
		{
			21394,
			51
		},
		{
			21395,
			56
		},
		{
			21385,
			52
		},
		{
			21391,
			55
		},
		{
			21393,
			58
		},
		{
			21657,
			57
		},
		{
			21386,
			52
		},
		{
			21652,
			49
		},
		{
			21378,
			49
		},
		{
			21376,
			48
		},
		{
			21377,
			48
		},
		{
			21379,
			59
		},
		{
			21380,
			49
		}
	};
	
	final private static int[] REWARDS =
	{
		6881,
		6883,
		6885,
		6887,
		6891,
		6893,
		6895,
		6897,
		6899,
		7580
	};
	final private static int[] REWARDS2 =
	{
		6882,
		6884,
		6886,
		6888,
		6892,
		6894,
		6896,
		6898,
		6900,
		7581
	};
	
	// Change this value to `true` if you wish 100% recipes, default 60%
	final private static boolean ALT_RP100 = false;
	
	public _617_GatherTheFlames(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(VULCAN);
		addStartNpc(HILDA);
		addTalkId(VULCAN);
		addTalkId(ROONEY);
		
		for (int[] dropdata : DROPLIST)
		{
			addKillId(dropdata[0]);
		}
		
		questItemIds = new int[]
		{
			TORCH
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
		
		long torches = st.getQuestItemsCount(TORCH);
		
		if (event.equalsIgnoreCase("31539-03.htm"))
		{
			if (player.getLevel() >= 74)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = "31539-02.htm";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("31271-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31539-05.htm") && (torches >= 1000))
		{
			htmltext = "31539-07.htm";
			st.takeItems(TORCH, 1000);
			if (ALT_RP100)
			{
				st.giveItems(REWARDS2[getRandom(REWARDS2.length)], 1);
			}
			else
			{
				st.giveItems(REWARDS[getRandom(REWARDS.length)], 1);
			}
		}
		else if (event.equalsIgnoreCase("31539-08.htm"))
		{
			st.takeItems(TORCH, -1);
			st.exitQuest(true);
		}
		else if (event.startsWith("reward"))
		{
			int rewardId = Integer.parseInt(event.substring(7));
			if (rewardId > 0)
			{
				if (torches >= 1200)
				{
					st.takeItems(TORCH, 1200);
					if (ALT_RP100)
					{
						st.giveItems(rewardId + 1, 1);
					}
					else
					{
						st.giveItems(rewardId, 1);
					}
					return null;
				}
				htmltext = "Incorrect item count";
			}
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
		
		int npcId = npc.getId();
		byte id = st.getState();
		long torches = st.getQuestItemsCount(TORCH);
		
		if (npcId == VULCAN)
		{
			if (id == State.CREATED)
			{
				if (player.getLevel() < 74)
				{
					st.exitQuest(true);
					htmltext = "31539-02.htm";
				}
				else
				{
					htmltext = "31539-01.htm";
				}
			}
			else if (torches < 1000)
			{
				htmltext = "31539-05.htm";
			}
			else
			{
				htmltext = "31539-04.htm";
			}
		}
		else if (npcId == HILDA)
		{
			if (id == State.CREATED)
			{
				if (player.getLevel() < 74)
				{
					st.exitQuest(true);
					htmltext = "31271-01.htm";
				}
				else
				{
					htmltext = "31271-02.htm";
				}
			}
			else
			{
				htmltext = "31271-04.htm";
			}
		}
		else if ((npcId == ROONEY) && (id == State.STARTED))
		{
			if (torches >= 1200)
			{
				htmltext = "32049-01.htm";
			}
			else
			{
				htmltext = "32049-02.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		L2PcInstance partyMember = getRandomPartyMemberState(player, State.STARTED);
		if (partyMember == null)
		{
			return super.onKill(npc, st);
		}
		st = partyMember.getQuestState(qn);
		long torches = st.getQuestItemsCount(TORCH);
		
		for (int[] dropdata : DROPLIST)
		{
			if ((npc.getId() == dropdata[0]) && (getRandom(100) < dropdata[1]))
			{
				st.giveItems(TORCH, 1 * Config.RATE_QUEST_DROP);
				if ((torches == 999) || (torches == 1199))
				{
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return super.onKill(npc, st);
	}
	
	public static void main(String[] args)
	{
		new _617_GatherTheFlames(617, qn, "");
	}
}
