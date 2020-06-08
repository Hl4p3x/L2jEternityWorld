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
import l2e.gameserver.network.serverpackets.MagicSkillUse;

/**
 * Created by LordWinter 18.06.2012 Based on L2J Eternity-World
 */
public class _653_WildMaiden extends Quest
{
	private static final String qn = "_653_WildMaiden";
	
	private static final int[][] spawns =
	{
		{
			66578,
			72351,
			-3731,
			0
		},
		{
			77189,
			73610,
			-3708,
			2555
		},
		{
			71809,
			67377,
			-3675,
			29130
		},
		{
			69166,
			88825,
			-3447,
			43886
		}
	};
	
	private int _currentPosition = 0;
	
	public _653_WildMaiden(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(32013);
		addTalkId(new int[]
		{
			32013,
			30181
		});
		
		addSpawn(32013, 66578, 72351, -3731, 0, false, 0L);
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
		
		if (event.equalsIgnoreCase("32013-03.htm"))
		{
			if (st.getQuestItemsCount(736) >= 1L)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.takeItems(736, 1L);
				st.playSound("ItemSound.quest_accept");
				
				npc.broadcastPacket(new MagicSkillUse(npc, npc, 2013, 1, 3500, 0));
				startQuestTimer("apparition_npc", 4000L, npc, player);
			}
			else
			{
				htmltext = "32013-03a.htm";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("apparition_npc"))
		{
			int chance = st.getRandom(4);
			
			while (chance == this._currentPosition)
			{
				chance = st.getRandom(4);
			}
			_currentPosition = chance;
			
			npc.deleteMe();
			addSpawn(32013, spawns[chance][0], spawns[chance][1], spawns[chance][2], spawns[chance][3], false, 0L);
			return null;
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
				if (player.getLevel() >= 26)
				{
					htmltext = "32013-02.htm";
				}
				else
				{
					htmltext = "32013-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case 30181:
						htmltext = "30181-01.htm";
						st.rewardItems(57, 2883L);
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(true);
						break;
					case 32013:
						htmltext = "32013-04a.htm";
				}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new _653_WildMaiden(653, qn, "");
	}
}