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

import l2e.gameserver.handler.bypasshandlers.QuestLink;

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
public class _10285_MeetingSirra extends Quest
{
	private static final String qn = "_10285_MeetingSirra";
	// NPC's
	private static final int _rafforty = 32020;
	private static final int _steward = 32029;
	private static final int _jinia = 32760;
	private static final int _kegor = 32761;
	private static final int _sirra = 32762;
	private static final int _jinia2 = 32781;

	public _10285_MeetingSirra(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_rafforty);
		addFirstTalkId(_sirra);
		addTalkId(_rafforty);
		addTalkId(_jinia);
		addTalkId(_jinia2);
		addTalkId(_kegor);
		addTalkId(_sirra);
		addTalkId(_steward);
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
			if (event.equalsIgnoreCase("32020-05.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.set("progress", "1");
				st.set("Ex", "0");
				st.playSound("ItemSound.quest_accept");
			}
		}	
		else if (npc.getId() == _jinia)
		{
			if (event.equalsIgnoreCase("32760-02.htm"))
			{
				st.set("Ex", "1");
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			}

			else if (event.equalsIgnoreCase("32760-06.htm"))
			{
				st.set("Ex", "3");
				addSpawn(_sirra, -23905,-8790,-5384,56238, false, 0, false, npc.getInstanceId());
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}

			else if (event.equalsIgnoreCase("32760-12.htm"))
			{
				st.set("Ex", "5");
				st.set("cond", "7");
				st.playSound("ItemSound.quest_middle");
			}

			else if (event.equalsIgnoreCase("32760-14.htm"))
			{
				st.set("Ex", "0");
				st.set("progress", "2");
				st.playSound("ItemSound.quest_middle");

				// destroy instance after 1 min
				Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
				inst.setDuration(60000);
				inst.setEmptyDestroyTime(0);
			}
		}
		else if (npc.getId() == _kegor)
		{
			if (event.equalsIgnoreCase("32761-02.htm"))
			{
				st.set("Ex", "2");
				st.set("cond", "4");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npc.getId() == _sirra)
		{
			if (event.equalsIgnoreCase("32762-08.htm"))
			{
				st.set("Ex", "4");
				st.set("cond", "6");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npc.getId() == _steward)
		{
			if (event.equalsIgnoreCase("go"))
			{
				if (player.getLevel() >= 82)
				{
					player.teleToLocation(103045,-124361,-2768);
					htmltext = "";
				}
				else
					htmltext = "32029-01a.htm";
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
					QuestState _prev = player.getQuestState("_10284_AcquisitionOfDivineSword");
					if ((_prev != null) && (_prev.getState() == State.COMPLETED) && (player.getLevel() >= 82))
						htmltext = "32020-01.htm";
					else
						htmltext = "32020-03.htm";
					break;
				case State.STARTED:
					if (st.getInt("progress") == 1)
						htmltext = "32020-06.htm";
					else if (st.getInt("progress") == 2)
						htmltext = "32020-09.htm";
					else if (st.getInt("progress") == 3)
					{
						st.giveItems(57, 283425);
						st.addExpAndSp(939075, 83855);
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
						htmltext = "32020-10.htm";
					}
					break;
				case State.COMPLETED:
					htmltext = "32020-02.htm";
					break;
			}
		}
		else if (npc.getId() == _jinia && st.getInt("progress") == 1)
		{
			switch (st.getInt("Ex"))
			{
				case 0:
					return "32760-01.htm";
				case 1:
					return "32760-03.htm";
				case 2:
					return "32760-04.htm";
				case 3:
					return "32760-07.htm";
				case 4:
					return "32760-08.htm";
				case 5:
					return "32760-13.htm";
			}
		}
		else if (npc.getId() == _kegor && st.getInt("progress") == 1)
		{
			switch (st.getInt("Ex"))
			{
				case 1:
					return "32761-01.htm";
				case 2:
					return "32761-03.htm";
				case 3:
					return "32761-04.htm";
			}
		}
		else if (npc.getId() == _sirra && st.getInt("progress") == 1)
		{
			switch (st.getInt("Ex"))
			{
				case 3:
					return "32762-01.htm";
				case 4:
					return "32762-09.htm";
			}
		}
		else if (npc.getId() == _steward && st.getInt("progress") == 2)
		{
			htmltext = "32029-01.htm";
			st.set("cond", "8");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npc.getId() == _jinia2 && st.getInt("progress") == 2)
		{
			htmltext = "32781-01.htm";
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getId() == _sirra)
			QuestLink.showQuestWindow(player, npc);
		
		return null;
	}

	public static void main(String[] args)
	{
		new _10285_MeetingSirra(10285, qn, "");
	}
}