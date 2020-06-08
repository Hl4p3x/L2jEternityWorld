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

public class _642_APowerfulPrimevalCreature extends Quest
{
	private static final String qn = "_642_APowerfulPrimevalCreature";
	
	// NPC
	private final static int DINN = 32105;
	
	// Quest Item
	private static final int DINOSAUR_TISSUE = 8774;
	private static final int DINOSAUR_EGG = 8775;
	
	// Mobs
	private static final int[] DINOSAURS =
	{
		22196,
		22197,
		22198,
		22199,
		22215,
		22216,
		22217,
		22218,
		22223,
		18344
	};
	
	// Rewards
	private static final int[] REWARDS =
	{
		8690,
		8692,
		8694,
		8696,
		8698,
		8700,
		8702,
		8704,
		8706,
		8708,
		8710
	};
	private static final int[] REWARDS_S80 =
	{
		9967,
		9968,
		9969,
		9970,
		9971,
		9972,
		9973,
		9974,
		9975
	};
	
	public _642_APowerfulPrimevalCreature(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addStartNpc(DINN);
		addTalkId(DINN);
		
		for (int i : DINOSAURS)
		{
			addKillId(i);
		}
		
		questItemIds = new int[]
		{
			DINOSAUR_TISSUE,
			DINOSAUR_EGG
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
		
		long count_tissue = st.getQuestItemsCount(DINOSAUR_TISSUE);
		long count_egg = st.getQuestItemsCount(DINOSAUR_EGG);
		
		if (event.equalsIgnoreCase("32105-04.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32105-06a.htm"))
		{
			st.takeItems(DINOSAUR_TISSUE, -1);
			st.giveItems(57, count_tissue * 5000);
		}
		else if (event.equalsIgnoreCase("32105-07.htm"))
		{
			if ((count_tissue < 150) || (count_egg == 0))
			{
				htmltext = "32105-07a.htm";
			}
		}
		else if (isDigit(event) && isIntInArray(Integer.parseInt(event), REWARDS))
		{
			if ((count_tissue >= 150) && (count_egg >= 1))
			{
				htmltext = "32105-08.htm";
				st.takeItems(DINOSAUR_TISSUE, 150);
				st.takeItems(DINOSAUR_EGG, 1);
				st.giveItems(57, 44000);
				st.giveItems(Integer.parseInt(event), 1);
			}
			else
			{
				htmltext = "32105-07a.htm";
			}
		}
		else if (event.equalsIgnoreCase("32105-10.htm"))
		{
			if (count_tissue >= 450)
			{
				htmltext = "32105-10.htm";
			}
			else
			{
				htmltext = "32105-11.htm";
			}
		}
		else if (event.equalsIgnoreCase("32105-09.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		else if (isDigit(event) && isIntInArray(Integer.parseInt(event), REWARDS_S80))
		{
			if (count_tissue >= 450)
			{
				htmltext = "32105-10.htm";
				st.takeItems(DINOSAUR_TISSUE, 450);
				st.giveItems(Integer.parseInt(event), 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else
			{
				htmltext = "32105-11.htm";
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
		
		long count = st.getQuestItemsCount(DINOSAUR_TISSUE);
		int npcId = npc.getId();
		int id = st.getState();
		int cond = st.getInt("cond");
		
		if (id == State.CREATED)
		{
			if ((npcId == DINN) & (cond == 0))
			{
				if (player.getLevel() >= 75)
				{
					htmltext = "32105-01.htm";
				}
				else
				{
					htmltext = "32105-00.htm";
					st.exitQuest(true);
				}
			}
		}
		else if (id == State.STARTED)
		{
			if ((npcId == DINN) & (cond == 1))
			{
				if (count == 0)
				{
					htmltext = "32105-05.htm";
				}
				else
				{
					htmltext = "32105-06.htm";
				}
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
		
		if (id == State.STARTED)
		{
			int cond = st.getInt("cond");
			if (cond == 1)
			{
				int chance;
				int count;
				if (npc.getId() == 18344)
				{
					chance = (int) (10 * Config.RATE_QUEST_DROP);
					count = 1;
					while (chance > 1000)
					{
						chance -= 1000;
						if (chance < 50)
						{
							chance = 50;
						}
						count++;
					}
					if (getRandom(1000) <= chance)
					{
						st.giveItems(DINOSAUR_EGG, count);
						st.playSound("ItemSound.quest_itemget");
					}
				}
				else
				{
					chance = (int) (50 * Config.RATE_QUEST_DROP);
					count = 1;
					while (chance > 100)
					{
						chance -= 100;
						if (chance < 10)
						{
							chance = 10;
						}
						count++;
					}
					if (getRandom(100) <= chance)
					{
						st.giveItems(DINOSAUR_TISSUE, count);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _642_APowerfulPrimevalCreature(642, qn, "");
	}
}