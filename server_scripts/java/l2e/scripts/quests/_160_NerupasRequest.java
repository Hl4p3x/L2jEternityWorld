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

public class _160_NerupasRequest extends Quest
{
  	private static final String qn = "_160_NerupasRequest";

  	private static final int NERUPA = 30370;
  	private static final int UNOREN = 30147;
  	private static final int CREAMEES = 30149;
  	private static final int JULIA = 30152;

  	private static final int SILVERY_SPIDERSILK = 1026;
  	private static final int UNOS_RECEIPT = 1027;
  	private static final int CELS_TICKET = 1028;
  	private static final int NIGHTSHADE_LEAF = 1029;
  	private static final int LESSER_HEALING_POTION = 1060;

  	public _160_NerupasRequest(int questId, String name, String descr)
  	{
    		super(questId, name, descr);

    		addStartNpc(NERUPA);
    		addTalkId(NERUPA);
    		addTalkId(UNOREN);
    		addTalkId(CREAMEES);
    		addTalkId(JULIA);

    		questItemIds = new int[] { SILVERY_SPIDERSILK, UNOS_RECEIPT, CELS_TICKET, NIGHTSHADE_LEAF };
  	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

    		if (event.equalsIgnoreCase("30370-04.htm"))
    		{
      			st.set("cond", "1");
      			st.setState(State.STARTED);
      			st.playSound("ItemSound.quest_accept");
      			st.giveItems(SILVERY_SPIDERSILK, 1);
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
      				if (player.getRace().ordinal() == 1)
      				{
        				if ((player.getLevel() >= 3) && (player.getLevel() <= 7))
          					htmltext = "30370-03.htm";
        				else
					{
          					htmltext = "30370-02.htm";
          					st.exitQuest(true);
        				}
      				}
      				else
      				{
        				htmltext = "30370-00.htm";
        				st.exitQuest(true);
      				}
      				break;
    			case State.STARTED:
      				switch (npc.getId())
      				{
      					case NERUPA:
        					if ((cond >= 1) && (cond <= 3))
          						htmltext = "30370-05.htm";
						else if((cond == 4) && (st.getQuestItemsCount(NIGHTSHADE_LEAF) == 1))
						{
          						htmltext = "30370-06.htm";
          						st.playSound("ItemSound.quest_finish");
          						st.takeItems(NIGHTSHADE_LEAF, 1);
          						st.rewardItems(LESSER_HEALING_POTION, 5);
          						st.addExpAndSp(1000, 0);
							st.unset("cond");
          						st.exitQuest(false);
						}
						break;
      					case UNOREN:
        					if (cond == 1)
        					{
          						st.set("cond", "2");
          						htmltext = "30147-01.htm";
          						st.playSound("ItemSound.quest_middle");
          						st.takeItems(SILVERY_SPIDERSILK, 1);
          						st.giveItems(UNOS_RECEIPT, 1);
        					}
        					else if (cond == 2)
          						htmltext = "30147-02.htm";
						else if (cond == 4)
          						htmltext = "30147-03.htm";
						break;
      					case CREAMEES:
        					if (cond == 2)
        					{
          						st.set("cond", "3");
          						htmltext = "30149-01.htm";
          						st.takeItems(UNOS_RECEIPT, 1);
          						st.giveItems(CELS_TICKET, 1);
          						st.playSound("ItemSound.quest_middle");
        					}
        					else if (cond == 3)
          						htmltext = "30149-02.htm";
						else if (cond == 4)
          						htmltext = "30149-03.htm";
						break;
      					case JULIA:
        					if (cond == 3)
        					{
          						st.set("cond", "4");
          						htmltext = "30152-01.htm";
          						st.takeItems(CELS_TICKET, -1);
          						st.giveItems(NIGHTSHADE_LEAF, 1);
          						st.playSound("ItemSound.quest_middle");
        					}
						else if (cond == 4)
          						htmltext = "30152-02.htm";
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
    		new _160_NerupasRequest(160, qn, "");
  	}
}