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

public class _167_DwarvenKinship extends Quest
{
  	private static final String qn = "_167_DwarvenKinship";

  	private static final int CARLON = 30350;
  	private static final int NORMAN = 30210;
  	private static final int HAPROCK = 30255;

  	private static final int CARLON_LETTER = 1076;
  	private static final int NORMANS_LETTER = 1106;

  	public _167_DwarvenKinship(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(CARLON);
    		addTalkId(CARLON);
    		addTalkId(NORMAN);
    		addTalkId(HAPROCK);

    		questItemIds = new int[] { CARLON_LETTER, NORMANS_LETTER };
  	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

    		if (event.equalsIgnoreCase("30350-04.htm"))
    		{
      			st.set("cond", "1");
      			st.setState(State.STARTED);
      			st.giveItems(CARLON_LETTER, 1);
      			st.playSound("ItemSound.quest_accept");
    		}
    		else if (event.equalsIgnoreCase("30255-03.htm"))
    		{
      			st.set("cond", "2");
      			st.takeItems(CARLON_LETTER, 1);
      			st.giveItems(NORMANS_LETTER, 1);
      			st.rewardItems(57, 2000);
    		}
    		else if (event.equalsIgnoreCase("30255-04.htm"))
    		{
      			st.takeItems(CARLON_LETTER, 1);
      			st.rewardItems(57, 3000);
			st.unset("cond");
      			st.playSound("ItemSound.quest_finish");
      			st.exitQuest(false);
    		}
    		else if (event.equalsIgnoreCase("30210-02.htm"))
    		{
      			st.takeItems(NORMANS_LETTER, 1);
      			st.rewardItems(57, 20000);
			st.unset("cond");
      			st.playSound("ItemSound.quest_finish");
      			st.exitQuest(false);
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

      		int cond = st.getInt("cond");

    		switch (st.getState())
    		{
    			case State.CREATED:
      				if (player.getLevel() >= 15)
        				htmltext = "30350-03.htm";
      				else
				{
        				htmltext = "30350-02.htm";
        				st.exitQuest(true);
      				}
      				break;
    			case State.STARTED:
      				switch (npc.getId())
      				{
      					case CARLON:
        					if (cond == 1)
        						htmltext = "30350-05.htm"; 
						break;
     					case HAPROCK:
        					if (cond == 1)
          						htmltext = "30255-01.htm";
						else if (cond == 2)
          						htmltext = "30255-05.htm"; 
						break;
      					case NORMAN:
        					if (cond == 2)
       							htmltext = "30210-01.htm";
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
    		new _167_DwarvenKinship(167, qn, "");
  	}
}