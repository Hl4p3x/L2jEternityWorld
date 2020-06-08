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

import java.util.Map;

import javolution.util.FastMap;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 02.10.2012 Based on L2J Eternity-World
 */
public class _369_CollectorOfJewels extends Quest
{
	private final static String qn = "_369_CollectorOfJewels";
	
	// NPC
	private static final int NELL = 30376;
	
	// Items
	private static final int FLARE_SHARD = 5882;
	private static final int FREEZING_SHARD = 5883;
	
	// Droplists
	private static Map<Integer, Integer> DROPLIST_FREEZE = new FastMap<>();
	private static Map<Integer, Integer> DROPLIST_FLARE = new FastMap<>();
	
	public _369_CollectorOfJewels(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(NELL);
		addTalkId(NELL);
		
		DROPLIST_FREEZE.put(20747, 85);
		DROPLIST_FREEZE.put(20619, 73);
		DROPLIST_FREEZE.put(20616, 60);
		
		DROPLIST_FLARE.put(20612, 77);
		DROPLIST_FLARE.put(20609, 77);
		DROPLIST_FLARE.put(20749, 85);
		
		for (int mob : DROPLIST_FREEZE.keySet())
		{
			addKillId(mob);
		}
		
		for (int mob : DROPLIST_FLARE.keySet())
		{
			addKillId(mob);
		}
		
		questItemIds = new int[]
		{
			FLARE_SHARD,
			FREEZING_SHARD
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
		
		if (event.equalsIgnoreCase("30376-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.set("awaitsFreezing", "1");
			st.set("awaitsFlare", "1");
		}
		else if (event.equalsIgnoreCase("30376-07.htm"))
		{
			st.playSound("ItemSound.quest_itemget");
		}
		else if (event.equalsIgnoreCase("30376-08.htm"))
		{
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if ((player.getLevel() >= 25) && (player.getLevel() <= 37))
				{
					htmltext = "30376-02.htm";
				}
				else
				{
					htmltext = "30376-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				long flare = st.getQuestItemsCount(FLARE_SHARD);
				long freezing = st.getQuestItemsCount(FREEZING_SHARD);
				
				if (cond == 1)
				{
					htmltext = "30376-04.htm";
				}
				else if ((cond == 2) && (flare >= 50) && (freezing >= 50))
				{
					htmltext = "30376-05.htm";
					st.set("cond", "3");
					st.rewardItems(57, 12500);
					st.takeItems(FLARE_SHARD, -1);
					st.takeItems(FREEZING_SHARD, -1);
					st.set("awaitsFreezing", "1");
					st.set("awaitsFlare", "1");
					st.playSound("ItemSound.quest_middle");
				}
				else if (cond == 3)
				{
					htmltext = "30376-09.htm";
				}
				else if ((cond == 4) && (flare >= 200) && (freezing >= 200))
				{
					htmltext = "30376-10.htm";
					st.playSound("ItemSound.quest_finish");
					st.rewardItems(57, 63500);
					st.takeItems(FLARE_SHARD, -1);
					st.takeItems(FREEZING_SHARD, -1);
					st.exitQuest(true);
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		int npcId = npc.getId();
		L2PcInstance partymember = null;
		int item = 0, chance = 0;
		
		if (DROPLIST_FREEZE.containsKey(npcId))
		{
			partymember = getRandomPartyMember(player, "awaitsFreezing", "1");
			if (partymember == null)
			{
				return null;
			}
			
			item = FREEZING_SHARD;
			chance = DROPLIST_FREEZE.get(npcId);
		}
		else if (DROPLIST_FLARE.containsKey(npcId))
		{
			partymember = getRandomPartyMember(player, "awaitsFlare", "1");
			if (partymember == null)
			{
				return null;
			}
			
			item = FLARE_SHARD;
			chance = DROPLIST_FLARE.get(npcId);
		}
		
		QuestState st = partymember.getQuestState(qn);
		
		int cond = st.getInt("cond");
		
		if ((cond >= 1) && (cond <= 3))
		{
			int max = 0;
			
			if (cond == 1)
			{
				max = 50;
			}
			else if (cond == 3)
			{
				max = 200;
			}
			
			if ((st.getRandom(100) < chance) && (st.getQuestItemsCount(item) <= max))
			{
				st.giveItems(item, 1);
				
				if (st.getQuestItemsCount(FREEZING_SHARD) == max)
				{
					st.unset("awaitsFreezing");
				}
				else if (st.getQuestItemsCount(FLARE_SHARD) == max)
				{
					st.unset("awaitsFlare");
				}
				
				if ((st.getQuestItemsCount(FLARE_SHARD) == max) && (st.getQuestItemsCount(FREEZING_SHARD) == max))
				{
					st.set("cond", String.valueOf(cond + 1));
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _369_CollectorOfJewels(369, qn, "");
	}
}