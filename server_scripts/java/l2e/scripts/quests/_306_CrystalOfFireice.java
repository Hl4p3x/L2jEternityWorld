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
 * Created by LordWinter 28.09.2012
 * Based on L2J Eternity-World
 */
public final class _306_CrystalOfFireice extends Quest
{
	private static final String qn = "_306_CrystalOfFireice";

	private static int FLAME_SHARD = 1020;
	private static int ICE_SHARD = 1021;
	private static int ADENA = 57;
	
	Map<Integer, int[]> droplist = new HashMap<>();
	{
		droplist.put(20109, new int[]
		{
			30,
			FLAME_SHARD,
		});
		droplist.put(20110, new int[]
		{
			30,
			ICE_SHARD,
		});
		droplist.put(20112, new int[]
		{
			40,
			FLAME_SHARD,
		});
		droplist.put(20113, new int[]
		{
			40,
			ICE_SHARD,
		});	
		droplist.put(20114, new int[]
		{
			50,
			FLAME_SHARD,
		});
		droplist.put(20114, new int[]
		{
			50,
			ICE_SHARD,
		});
	}			
	
	public _306_CrystalOfFireice(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(30004);
		addTalkId(30004);

		addKillId(20109,20110,20112,20113,20114,20115);

		questItemIds = new int[] { FLAME_SHARD, ICE_SHARD };
	}	
		
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
				
		if(event.equalsIgnoreCase("30004-04.htm"))
		{
		  	st.set("cond","1");
		  	st.setState(State.STARTED);
		  	st.playSound("ItemSound.quest_accept");
		} 
		else if(event.equalsIgnoreCase("30004-08.htm"))
		{
		  	st.exitQuest(true);
		  	st.playSound("ItemSound.quest_finish");
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
				if (player.getLevel() >= 17)
				   	htmltext = "30004-03.htm";
				else
				{
					htmltext = "30004-02.htm";
				   	st.exitQuest(true);
				}		
				break;
			case State.STARTED:
				long Shrads_count = st.getQuestItemsCount(FLAME_SHARD) + st.getQuestItemsCount(ICE_SHARD);
				long Reward = Shrads_count * 30 + (Shrads_count >= 10 ? 5000 : 0);				
				if (Reward > 0)
				{
					st.giveItems(ADENA, Reward);
					st.takeItems(FLAME_SHARD,-1);
					st.takeItems(ICE_SHARD,-1);
					htmltext = "30004-07.htm";
				}			
				else
					htmltext = "30004-05.htm";			   
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

		int npcId = npc.getId();

		if (droplist.containsKey(npcId))
		{			
			int chance = droplist.get(npcId)[0];
			int item = droplist.get(npcId)[1];		
			if (st.getRandom(100) < chance)
			{
				st.giveItems(item,1);
				st.playSound("ItemSound.quest_itemget");
			}				
		}
		return null;
	}	

	public static void main(String[] args)
	{
		new _306_CrystalOfFireice(306, qn, "");
	}
}