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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Fixed by L2J Etermity-World
 */
public class _10283_RequestOfIceMerchant extends Quest
{
	private static final String qn = "_10283_RequestOfIceMerchant";
	
	private static final int _rafforty = 32020;
	private static final int _kier = 32022;
	private static final int _jinia = 32760;
	
	private static final Location MOVE_TO_END = new Location(104457, -107010, -3698, 0);
	
	private boolean _jiniaOnSpawn = false;
	
	public _10283_RequestOfIceMerchant(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_rafforty);
		addTalkId(_rafforty);
		addTalkId(_kier);
		addFirstTalkId(_jinia);
		addTalkId(_jinia);
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
			if (event.equalsIgnoreCase("32020-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("32020-07.htm"))
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if ((npc.getId() == _kier) && event.equalsIgnoreCase("spawn"))
		{
			if (_jiniaOnSpawn)
			{
				htmltext = "32022-02.html";
			}
			else
			{
				addSpawn(_jinia, 104473, -107549, -3695, 44954, false, 180000);
				_jiniaOnSpawn = true;
				startQuestTimer("despawn", 180000, npc, player);
				return null;
			}
		}
		else if (event.equalsIgnoreCase("despawn"))
		{
			_jiniaOnSpawn = false;
			return null;
		}
		else if ((npc.getId() == _jinia) && event.equalsIgnoreCase("32760-04.html"))
		{
			st.giveItems(57, 190000);
			st.addExpAndSp(627000, 50300);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
			npc.setRunning();
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_END);
			npc.decayMe();
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
					QuestState _prev = player.getQuestState("_115_TheOtherSideOfTruth");
					if ((_prev != null) && (_prev.getState() == State.COMPLETED) && (player.getLevel() >= 82))
					{
						htmltext = "32020-01.htm";
					}
					else
					{
						htmltext = "32020-00.htm";
					}
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
					{
						htmltext = "32020-04.htm";
					}
					else if (st.getInt("cond") == 2)
					{
						htmltext = "32020-08.htm";
					}
					break;
				case State.COMPLETED:
					htmltext = "31350-09.htm";
					break;
			}
		}
		else if ((npc.getId() == _kier) && (st.getInt("cond") == 2))
		{
			htmltext = "32022-01.html";
		}
		else if ((npc.getId() == _jinia) && (st.getInt("cond") == 2))
		{
			htmltext = "32760-02.html";
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		if ((npc.getId() == _jinia) && (st.getInt("cond") == 2))
		{
			return "32760-01.html";
		}
		
		npc.showChatWindow(player);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new _10283_RequestOfIceMerchant(10283, qn, "");
	}
}