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

import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 02.10.2012 Based on L2J Eternity-World
 */
public class _141_ShadowFoxPart3 extends Quest
{
	private static final String qn = "_141_ShadowFoxPart3";
	
	private static final int NATOOLS = 30894;
	private static final int REPORT = 10350;
	
	private static final int[] NPC =
	{
		20791,
		20792,
		20135
	};
	
	public _141_ShadowFoxPart3(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addFirstTalkId(NATOOLS);
		addTalkId(NATOOLS);
		
		for (int mob : NPC)
		{
			addKillId(mob);
		}
		
		questItemIds = new int[]
		{
			REPORT
		};
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("30894-02.htm"))
		{
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30894-04.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30894-15.htm"))
		{
			st.set("cond", "4");
			st.unset("talk");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30894-18.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.unset("talk");
			st.exitQuest(false);
			st.giveItems(57, 88888);
			if ((player.getLevel() >= 37) && (player.getLevel() <= 42))
			{
				st.addExpAndSp(278005, 17058);
			}
			QuestState qs = player.getQuestState("_998_FallenAngelSelect");
			if (qs == null)
			{
				Quest q = QuestManager.getInstance().getQuest("_998_FallenAngelSelect");
				if (q != null)
				{
					qs = q.newQuestState(player);
					qs.setState(State.STARTED);
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
		{
			QuestState qs = player.getQuestState("_140_ShadowFoxPart2");
			st = this.newQuestState(player);
			
			if (qs != null)
			{
				if ((qs.getState() == State.COMPLETED) && (st.getState() == State.CREATED))
				{
					st.setState(State.STARTED);
				}
			}
		}
		else if ((st.getState() == State.COMPLETED) && (player.getLevel() >= 38))
		{
			QuestState qs2 = player.getQuestState("_998_FallenAngelSelect");
			QuestState qs3 = player.getQuestState("142_FallenAngelRequestOfDawn");
			QuestState qs4 = player.getQuestState("143_FallenAngelRequestOfDusk");
			if (qs2 != null)
			{
				if ((qs2.getState() == State.COMPLETED) && ((qs3 == null) || (qs4 == null)))
				{
					qs2.setState(State.STARTED);
				}
			}
		}
		npc.showChatWindow(player);
		return null;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		final int id = st.getState();
		final int cond = st.getInt("cond");
		final int talk = st.getInt("talk");
		
		if (id == State.CREATED)
		{
			return htmltext;
		}
		if (id == State.COMPLETED)
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		else if (id == State.STARTED)
		{
			if (cond == 0)
			{
				if (player.getLevel() >= 37)
				{
					htmltext = "30894-01.htm";
				}
				else
				{
					htmltext = "30894-00.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				htmltext = "30894-02.htm";
			}
			else if (cond == 2)
			{
				htmltext = "30894-05.htm";
			}
			else if (cond == 3)
			{
				if (cond == talk)
				{
					htmltext = "30894-07.htm";
				}
				else
				{
					htmltext = "30894-06.htm";
					st.takeItems(REPORT, -1);
					st.set("talk", "1");
				}
			}
			else if (cond == 4)
			{
				htmltext = "30894-16.htm";
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
		
		if ((st.getInt("cond") == 2) && (st.getRandom(100) <= 80) && (st.getQuestItemsCount(REPORT) < 30))
		{
			st.giveItems(REPORT, 1);
			if (st.getQuestItemsCount(REPORT) >= 30)
			{
				st.set("cond", "3");
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
		new _141_ShadowFoxPart3(141, qn, "");
	}
}