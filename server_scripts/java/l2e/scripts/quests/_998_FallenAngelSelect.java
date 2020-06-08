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
 * Created by LordWinter 18.06.2012
 * Based on L2J Eternity-World
 */
public final class _998_FallenAngelSelect extends Quest
{
  	private static final String qn 	= "_998_FallenAngelSelect";

  	private static int NATOOLS = 30894;

  	public _998_FallenAngelSelect(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

		addStartNpc(NATOOLS);
    		addTalkId(NATOOLS);
  	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;

    		if (event.equalsIgnoreCase("dawn"))
    		{
      			Quest q1 = QuestManager.getInstance().getQuest("_142_FallenAngelRequestOfDawn");
      			QuestState qs1 = player.getQuestState("_142_FallenAngelRequestOfDawn");
      			if (q1 != null)
      			{
        			qs1 = q1.newQuestState(player);
        			qs1.setState(State.STARTED);
        			q1.notifyEvent("30894-01.htm", npc, player);
        			st.setState(State.COMPLETED);
      			}
      			return null;
    		}

    		if (event.equalsIgnoreCase("dusk"))
    		{
      			Quest q2 = QuestManager.getInstance().getQuest("_143_FallenAngelRequestOfDusk");
      			QuestState qs2 = player.getQuestState("_143_FallenAngelRequestOfDusk");
      			if (q2 != null)
      			{
        			qs2 = q2.newQuestState(player);
        			qs2.setState(State.STARTED);
        			q2.notifyEvent("30894-01.htm", npc, player);
        			st.setState(State.COMPLETED);
     			}
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
			return htmltext;

    		switch (st.getState())
    		{
    			case State.STARTED:
      				htmltext = "30894-01.htm";
    		}
    		return htmltext;
  	}

  	public static void main(String[] args)
  	{
    		new _998_FallenAngelSelect(998, qn, "");
  	}
}