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
 * Created by LordWinter 05.08.2011
 * Based on L2J Eternity-World
 */
public class _10271_TheEnvelopingDarkness extends Quest
{
	private static final String qn = "_10271_TheEnvelopingDarkness";

	// NPC's
	private static final int ORBYU 		  = 32560;
	private static final int EL 		  = 32556;
  	private static final int MEDIBAL_CORPSE   = 32528;

  	// ITEM
  	private static final int MEDIBAL_DOCUMENT = 13852;

	public _10271_TheEnvelopingDarkness(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(ORBYU);
		addTalkId(ORBYU);
    		addTalkId(EL);
    		addTalkId(MEDIBAL_CORPSE);

    		questItemIds = new int[] {MEDIBAL_DOCUMENT};
	}

   	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32560-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
	  	else if (event.equalsIgnoreCase("32556-02.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
       		else if (event.equalsIgnoreCase("32556-05.htm"))
		{
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
      			st.takeItems(MEDIBAL_DOCUMENT, 1);
      
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

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

    		else if (npc.getId() == ORBYU)
    		{
     			if (st.getInt("cond") == 0)
			{
      				QuestState _prev = player.getQuestState("_10269_ToTheSeedOfDestruction");
      				if ((_prev != null) && (_prev.getState() == State.COMPLETED) && (player.getLevel() >= 75))
					htmltext = "32560-01.htm";
        			else 
          				htmltext = "32560-00.htm"; 
      			} 
    			else if (st.getInt("cond") >= 1 && st.getInt("cond") < 4)
         		{
      				htmltext = "32560-03.htm";
         		}
     			else if (st.getInt("cond") == 4)
         		{
      				htmltext = "32560-04.htm";
      				st.unset("cond");
      				st.setState(State.COMPLETED);
      				st.giveItems(57, 62516);
      				st.addExpAndSp(377403, 37867);
      				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
         		}         
         
     		}
     		else if (npc.getId() == EL)
       		{  
			if (st.getInt("cond") == 1)
        		{
        		  	htmltext = "32556-01.htm";
        		} 
        		else if (st.getInt("cond") == 2)
        		{
				htmltext = "32556-03.htm";
        		}
            		else if (st.getInt("cond") == 3)
        		{
        		  	htmltext = "32556-04.htm";
        		}
            		else if (st.getInt("cond") == 4)
        		{
        		  	htmltext = "32556-06.htm";
        		}
		}
       		else if (npc.getId() == MEDIBAL_CORPSE)
       		{   
       			if (st.getInt("cond") == 2)
        		{
        		   	htmltext = "32528-01.htm";
               			st.playSound("ItemSound.quest_middle");
               			st.set("cond", "3");
               			st.giveItems(MEDIBAL_DOCUMENT, 1);
        		}    
			else if (st.getInt("cond") == 3)
        		{
        		  	htmltext = "32528-02.htm";
        		}  
	 	}
       		return htmltext;
  	} 
  
	public static void main(String[] args)
	{
		new _10271_TheEnvelopingDarkness(10271, qn, "");
	}
}