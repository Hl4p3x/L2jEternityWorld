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

/**
 * Created by LordWinter 06.07.2012
 * Based on L2J Eternity-World
 */
public class _122_OminousNews extends Quest
{
	private static final String qn = "_122_OminousNews";
	
	// NPCs
	private static final int MOIRA = 31979;
	private static final int KARUDA = 32017;
	
	public _122_OminousNews(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(MOIRA);
		addTalkId(MOIRA);
		addTalkId(KARUDA);
	}	
			
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31979-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32017-02.htm"))
		{
			st.giveItems(57,8923);
			st.addExpAndSp(45151,2310);
			st.playSound("ItemSound.quest_finish");
			st.unset("cond");
			st.exitQuest(false);
		}	
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if(st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 20 && player.getLevel() <= 36)
					htmltext = "31979-02.htm";
				else
				{
					htmltext = "31979-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case MOIRA:
						htmltext = "31979-03.htm";
						break;
					
					case KARUDA:
						htmltext = "32017-01.htm";
						break;
				}
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _122_OminousNews(122, qn, "");
	}
}