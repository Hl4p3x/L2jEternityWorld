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

import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 12.05.2011 Based on L2J Eternity-World
 */
public class _10286_ReunionWithSirra extends Quest
{
	private static final String qn = "_10286_ReunionWithSirra";
	
	private static final int _rafforty = 32020;
	private static final int _jinia = 32760;
	private static final int _sirra = 32762;
	private static final int _jinia2 = 32781;
	
	private static final int _blackCore = 15470;
	
	public _10286_ReunionWithSirra(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_rafforty);
		addTalkId(_rafforty);
		addTalkId(_jinia);
		addTalkId(_jinia2);
		addTalkId(_sirra);
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
		
		if (npc.getId() == _rafforty)
		{
			if (event.equalsIgnoreCase("32020-04.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.set("progress", "1");
				st.playSound("ItemSound.quest_accept");
			}
			
			else if (event.equalsIgnoreCase("32020-05.htm") && (st.getInt("progress") == 1))
			{
				st.set("Ex", "0");
			}
		}
		else if (npc.getId() == _jinia)
		{
			if (event.equalsIgnoreCase("32760-07.htm"))
			{
				addSpawn(_sirra, -23905, -8790, -5384, 56238, false, 0, false, npc.getInstanceId());
				st.set("Ex", "1");
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			}
			
			else if (event.equalsIgnoreCase("32760-09.htm") && (st.getInt("progress") == 1) && (st.getInt("Ex") == 2))
			{
				st.set("progress", "2");
				Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
				inst.setDuration(60000);
				inst.setEmptyDestroyTime(0);
			}
		}
		else if (npc.getId() == _sirra)
		{
			if (event.equalsIgnoreCase("32762-04.htm") && (st.getInt("progress") == 1) && (st.getInt("Ex") == 1))
			{
				if (st.getQuestItemsCount(_blackCore) == 0)
				{
					st.giveItems(_blackCore, 5);
				}
				
				st.set("Ex", "2");
				st.set("cond", "4");
				st.playSound("ItemSound.quest_middle");
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
		
		if (npc.getId() == _rafforty)
		{
			switch (st.getState())
			{
				case State.CREATED:
					QuestState _prev = player.getQuestState("_10285_MeetingSirra");
					if ((_prev != null) && (_prev.getState() == State.COMPLETED) && (player.getLevel() >= 82))
					{
						htmltext = "32020-01.htm";
					}
					else
					{
						htmltext = "32020-03.htm";
					}
					break;
				case State.STARTED:
					if (st.getInt("progress") == 1)
					{
						htmltext = "32020-06.htm";
					}
					else if (st.getInt("progress") == 2)
					{
						htmltext = "32020-09.htm";
					}
					break;
				case State.COMPLETED:
					htmltext = "32020-02.htm";
					break;
			}
		}
		else if ((npc.getId() == _jinia) && (st.getInt("progress") == 1))
		{
			switch (st.getInt("Ex"))
			{
				case 0:
					return "32760-01.htm";
				case 1:
					return "32760-07.htm";
				case 2:
					return "32760-08.htm";
			}
		}
		else if ((npc.getId() == _sirra) && (st.getInt("progress") == 1))
		{
			switch (st.getInt("Ex"))
			{
				case 1:
					return "32762-01.htm";
				case 2:
					return "32762-05.htm";
			}
		}
		else if (npc.getId() == _jinia2)
		{
			if (st.getInt("progress") == 2)
			{
				htmltext = "32781-01.htm";
			}
			else if (st.getInt("progress") == 3)
			{
				st.addExpAndSp(2152200, 181070);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
				htmltext = "32781-08.htm";
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new _10286_ReunionWithSirra(10286, qn, "");
	}
}