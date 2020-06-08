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

import l2e.Config;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;


/**
 * Created by LordWinter 20.05.2011
 * Based on L2J Eternity-World
 */
public class _10272_LightFragment extends Quest
{
	private static final String qn = "_10272_LightFragment";

	// NPC
	private static final int ORBYU = 32560;
	private static final int ARTIUS = 32559;
  	private static final int GINBY = 32566;
  	private static final int LELRIKIA = 32567;
  	private static final int LEKON = 32557;

	// MONSTERS
  	private static final int[] Monsters = {22536, 22537, 22538, 22539, 22540, 22541, 22542, 22543, 22544, 22547, 22550, 22551, 22552, 22596};

	// ITEMS
  	private static final int FRAGMENT_POWDER = 13853;
  	private static final int LIGHT_FRAGMENT_POWDER = 13854;
  	private static final int ADENA = 57;

	//DROP CHANCE
  	private static final double DROP_CHANCE = 90;

	public _10272_LightFragment(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(ORBYU);
		addTalkId(ORBYU);
    		addTalkId(ARTIUS);
    		addTalkId(GINBY);
    		addTalkId(LELRIKIA);
    		addTalkId(LEKON);

    		for (int i : Monsters)
			addKillId(i);
		questItemIds = new int[] {FRAGMENT_POWDER,LIGHT_FRAGMENT_POWDER};
	}

   	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32560-06.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
	  	else if(event.equalsIgnoreCase("32559-03.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
	  	else if(event.equalsIgnoreCase("32559-07.htm"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle"); 
       		}
    		else if(event.equalsIgnoreCase("pay"))
		{
        		if (st.getQuestItemsCount(ADENA) >= 10000)
              		st.takeItems(ADENA, 10000);
              		htmltext = "32566-05.htm";
        		if (st.getQuestItemsCount(ADENA) < 10000)
              			htmltext = "32566-04a.htm";
       		}
	  	else if(event.equalsIgnoreCase("32567-04.htm"))
		{
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle"); 
        	}
	  	else if(event.equalsIgnoreCase("32559-12.htm"))
		{ 
			st.set("cond", "5");
			st.playSound("ItemSound.quest_middle"); 
        	}
		return htmltext;
  	}

   	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
  	{
		String htmltext = getNoQuestMsg(player);
		QuestState st =  player.getQuestState(qn);
    		if (st == null)
			return htmltext;

    		if (npc.getId() == ORBYU)
    		{
     			switch (st.getState())
      			{
	      			case State.CREATED:
      					QuestState _prev = player.getQuestState("_10271_TheEnvelopingDarkness");
      					if ((_prev != null) && (_prev.getState() == State.COMPLETED) && (player.getLevel() >= 75))
						htmltext = "32560-01.htm";
        				else 
          					htmltext = "32560-02.htm"; 
     					if (player.getLevel() <= 75)
          					htmltext = "32560-03.htm"; 
		        		break;
      
				case State.STARTED:
					htmltext = "32560-06.htm";
          				break;
      				case State.COMPLETED:
					htmltext = "32560-04.htm";
          				break;   
      			}
      
     			if (st.getInt("cond") == 2)
         		{
      				htmltext = "32560-06.htm";
         		}         
     		}
     		else if (npc.getId() == ARTIUS)
       		{ 
       			switch (st.getState())
      			{  
       				case State.COMPLETED:
					htmltext = "32559-19.htm";
          				break;   
      			}
			if (st.getInt("cond") == 1)
        		{
        		  	htmltext = "32559-01.htm";
        		}
			if (st.getInt("cond") == 2)
        		{
        		  	htmltext = "32559-04.htm";
        		}
			if (st.getInt("cond") == 3)
        		{
        		  	htmltext = "32559-08.htm";
        		}
			else if (st.getInt("cond") == 4)
        		{
        		  htmltext = "32559-10.htm";
        		}
			else if (st.getInt("cond") == 5)
        		{
            			if (st.getQuestItemsCount(FRAGMENT_POWDER) >= 100)
              			{
        		  		htmltext = "32559-15.htm";
              				st.set("cond", "6");
              			}
            			else if (st.getQuestItemsCount(FRAGMENT_POWDER) >= 1)
              			{
              				htmltext = "32559-14.htm";
              			}
           			else if (st.getQuestItemsCount(FRAGMENT_POWDER) < 1)
               			{
              				htmltext = "32559-13.htm"; 
        		   	}
             		}  
			else if (st.getInt("cond") == 6)
        		{
        		  	htmltext = "32559-16.htm";
        		}
		}  
         	else if (npc.getId() == GINBY)
       		{ 
       			switch (st.getState())
        		{  
       				case State.COMPLETED:
					htmltext = "32559-19.htm";
          				break;   
         		}
			if (st.getInt("cond") == 1)
        		{
        		  	htmltext = "32566-02.htm";
        		}
			else if (st.getInt("cond") == 2)
        		{
        		  	htmltext = "32566-02.htm";
        		}
			else if (st.getInt("cond") == 3)
        		{
        		  	htmltext = "32566-01.htm";
        		}
			else if (st.getInt("cond") == 4)
        		{
        		  	htmltext = "32566-09.htm";
        		} 
			else if (st.getInt("cond") == 5)
        		{ 
        		  	htmltext = "32566-10.htm";
        		}
             		else if (st.getInt("cond") == 6)
        		{ 
        		  	htmltext = "32566-10.htm";
        		}
            
		}        
        	else if (npc.getId() == LELRIKIA)
         	{ 
			if (st.getInt("cond") == 3)
        		{
        		  	htmltext = "32567-01.htm";
        		} 
			else if (st.getInt("cond") == 4)
        		{
        		  	htmltext = "32567-05.htm";
        		}
		}                    
       		return htmltext;
  	}
            
  	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{

		final QuestState st = player.getQuestState(qn);
		if ((st != null) && st.getInt("cond") == 5)
		{
			final long count = st.getQuestItemsCount(FRAGMENT_POWDER);
			if (count < 100)
			{ 
				int chance = (int)(Config.RATE_QUEST_DROP * DROP_CHANCE);
				int numItems = chance / 100;
				chance = chance % 100;
				if (getRandom(100) < chance)
					numItems++;
				if (numItems > 0)
				{ 
					if (count + numItems >= 100)
					{
						numItems = 100 - (int)count;
						st.playSound("ItemSound.quest_middle");
					}
					else
						st.playSound("ItemSound.quest_itemget");
						st.giveItems(FRAGMENT_POWDER, numItems);
				}
			}
		}
		return null;
	} 
  
	public static void main(String[] args)
	{
		new _10272_LightFragment(10272, qn, "");
	}
}