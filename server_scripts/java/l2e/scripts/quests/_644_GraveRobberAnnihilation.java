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
 * Created by LordWinter 20.06.2012 Based on L2J Eternity-World
 */
public class _644_GraveRobberAnnihilation extends Quest
{
	private static final String qn = "_644_GraveRobberAnnihilation";
	
	Map<String, int[]> Rewards = new HashMap<>();
	
	private static final int KARUDA = 32017;
	private static final int GOODS = 8088;
	
	public _644_GraveRobberAnnihilation(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(KARUDA);
		addTalkId(KARUDA);
		
		addKillId(new int[]
		{
			22003,
			22004,
			22005,
			22006,
			22008
		});
		
		Rewards.put("var", new int[]
		{
			1865,
			30
		});
		Rewards.put("ask", new int[]
		{
			1867,
			40
		});
		Rewards.put("ior", new int[]
		{
			1869,
			30
		});
		Rewards.put("coa", new int[]
		{
			1870,
			30
		});
		Rewards.put("cha", new int[]
		{
			1871,
			30
		});
		Rewards.put("abo", new int[]
		{
			1872,
			40
		});
		
		questItemIds = new int[]
		{
			GOODS
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
		
		if (event.equalsIgnoreCase("32017-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (Rewards.containsKey(event))
		{
			if (st.getQuestItemsCount(GOODS) == 120L)
			{
				htmltext = "32017-04.htm";
				st.takeItems(GOODS, -1L);
				st.rewardItems((Rewards.get(event))[0], (Rewards.get(event))[1]);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
			else
			{
				htmltext = "32017-07.htm";
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
		
		switch (st.getState())
		{
			case State.CREATED:
				if ((player.getLevel() >= 20) && (player.getLevel() <= 33))
				{
					htmltext = "32017-01.htm";
				}
				else
				{
					htmltext = "32017-06.htm";
				}
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				
				if (cond == 1)
				{
					htmltext = "32017-05.htm";
				}
				else
				{
					if (cond != 2)
					{
						break;
					}
					if (st.getQuestItemsCount(GOODS) == 120L)
					{
						htmltext = "32017-03.htm";
					}
					else
					{
						htmltext = "32017-07.htm";
					}
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
		
		L2PcInstance partyMember = getRandomPartyMemberState(player, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		
		if ((st.getQuestItemsCount(GOODS) < 120L) && (st.getRandom(10) < 5))
		{
			st.giveItems(GOODS, 1L);
			if (st.getQuestItemsCount(GOODS) == 120L)
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _644_GraveRobberAnnihilation(644, qn, "");
	}
}