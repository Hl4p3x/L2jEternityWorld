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
 * Created by LordWinter 12.05.2011
 * Based on L2J Eternity-World
 */
public class _10287_StoryOfThoseLeft extends Quest
{
	private static final String qn = "_10287_StoryOfThoseLeft";

	private static final int _rafforty = 32020;
	private static final int _jinia = 32760;
	private static final int _kegor = 32761;

	public _10287_StoryOfThoseLeft(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(_rafforty);
		addTalkId(_rafforty);
		addTalkId(_jinia);
		addTalkId(_kegor);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;

		if (npc.getId() == _rafforty)
		{
			if (event.equalsIgnoreCase("32020-04.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.set("progress", "1");
				st.set("Ex1", "0");
				st.set("Ex2", "0");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.startsWith("reward_") && st.getInt("progress") == 2)
			{
				try
				{
					int itemId = Integer.parseInt(event.substring(7));

					if ((itemId >= 10549 && itemId <= 10553) || itemId == 14219)
						st.giveItems(itemId, 1);
					
					st.playSound("ItemSound.quest_finished");
					st.exitQuest(false);
					htmltext = "32020-11.htm";
				}
				catch (Exception e)
				{	
				}
			}
		}
		else if (npc.getId() == _jinia)
		{
			if (event.equalsIgnoreCase("32760-03.htm") && st.getInt("progress") == 1 && st.getInt("Ex1") == 0)
			{
				st.set("Ex1", "1");
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npc.getId() == _kegor)
		{
			if (event.equalsIgnoreCase("32761-04.htm") && st.getInt("progress") == 1 && st.getInt("Ex1") == 1 && st.getInt("Ex2") == 0)
			{
				st.set("Ex2", "1");
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
			return htmltext;

		if (npc.getId() == _rafforty)
		{
			switch (st.getState())
			{
				case State.CREATED:
					QuestState _prev = player.getQuestState("_10286_ReunionWithSirra");
					if (_prev != null && _prev.getState() == State.COMPLETED && player.getLevel() >= 82)
						htmltext = "32020-01.htm";
					else
						htmltext = "32020-03.htm";
					break;
				case State.STARTED:
					if (st.getInt("progress") == 1)
						htmltext = "32020-05.htm";
					else if (st.getInt("progress") == 2)
						htmltext = "32020-09.htm";
					break;
				case State.COMPLETED:
					htmltext = "32020-02.htm";
					break;
			}
		}
		else if (npc.getId() == _jinia && st.getInt("progress") == 1)
		{
			if (st.getInt("Ex1") == 0)
				return "32760-01.htm";

			else if (st.getInt("Ex1") == 1 && st.getInt("Ex2") == 0)
				return "32760-04.htm"; 

			else if (st.getInt("Ex1") == 1 && st.getInt("Ex2") == 1)
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
				st.set("progress", "2");
				st.set("Ex1", "0");
				st.set("Ex2", "0");

				// destroy instance after 1 min
				Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
				inst.setDuration(60000);
				inst.setEmptyDestroyTime(0);
				
				return "32760-05.htm";
			} 

		}
		else if (npc.getId() == _kegor && st.getInt("progress") == 1)
		{
			if (st.getInt("Ex1") == 1 && st.getInt("Ex2") == 0)
				htmltext = "32761-01.htm";
			
			else if (st.getInt("Ex1") == 0 && st.getInt("Ex2") == 0)
				htmltext = "32761-02.htm";

			else if (st.getInt("Ex2") == 1)
				htmltext = "32761-05.htm";
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _10287_StoryOfThoseLeft(10287, qn, "");
	}
}