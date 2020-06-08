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
 * Created by LordWinter 21.03.2011
 * Based on L2J Eternity-World
 */
public class _015_SweetWhisper extends Quest
{
	private static final String qn = "_015_SweetWhisper";

	// NPC
	private static final int VLADIMIR = 31302;
	private static final int HIERARCH = 31517;
	private static final int M_NECROMANCER = 31518;

	public _015_SweetWhisper(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(VLADIMIR);
		addTalkId(VLADIMIR);
		addTalkId(HIERARCH);
		addTalkId(M_NECROMANCER);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31302-1.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31518-1.htm"))
		{
			st.set("cond", "2");
		}
		else if (event.equalsIgnoreCase("31517-1.htm"))
		{
			st.addExpAndSp(350531,28204);
			st.playSound("ItemSound.quest_finish");
			st.unset("cond");
			st.setState(State.COMPLETED);
			st.exitQuest(false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		final int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (player.getLevel() >= 60)
					htmltext = "31302-0.htm";
				else
				{
					htmltext = "31302-0a.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case VLADIMIR:
						if (cond >= 1)
							htmltext = "31302-1a.htm";		
						break;
					case M_NECROMANCER:
						switch (cond)
						{
							case 1:
								htmltext = "31518-0.htm";
								break;
							case 2:
								htmltext = "31518-1a.htm";
								break;
						}
						break;
					case HIERARCH:
						if (cond == 2)
							htmltext = "31517-0.htm";
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _015_SweetWhisper(15, qn, "");    	
	}
}
