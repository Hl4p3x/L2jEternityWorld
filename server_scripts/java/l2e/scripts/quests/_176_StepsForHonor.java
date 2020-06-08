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

import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 15.03.2011
 * Based on L2J Eternity-World
 */
public class _176_StepsForHonor extends Quest
{
    	private static final String qn = "_176_StepsForHonor";

	public static final int RAPIDUS = 36479;
	
	public _176_StepsForHonor(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(RAPIDUS);
		addTalkId(RAPIDUS);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if(st == null)
			return htmltext;
		
		final int count = st.getInt("count");
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
	            		if (player.getLevel() >= 80)
	            			htmltext = "36479-1.htm";
	            		else
	            		{
	    				htmltext = "36479-low.htm";
	    				st.exitQuest(true);
	            		}
		        	break;

			case State.STARTED:
		        	switch(st.getInt("cond"))
		        	{
		        		case 1:
						htmltext = "36479-1.htm";
						if (count >= 9)
						{
							st.set("count", String.valueOf(0));
							htmltext = "36479-2.htm";
							st.set("cond", "3");
						}
						else
							htmltext = "36479-1fail.htm";
						break;
		        		case 3:
						htmltext = "36479-2.htm";
						if (count >= 18)
						{
							st.set("count", String.valueOf(0));
							htmltext = "36479-3.htm";
							st.set("cond", "5");
						}
						else
							htmltext = "36479-2fail.htm";
						break;
		        		case 5:
						htmltext = "36479-3.htm";
						if (count >= 27)
						{
							st.set("count", String.valueOf(0));
							htmltext = "36479-4.htm";
							st.set("cond", "7");
						}
						else
							htmltext = "36479-3fail.htm";
						break;
		        		case 7:
						htmltext = "36479-4.htm";
						if (count < 36)
							htmltext = "36479-4fail.htm";
						break;
		        		case 8:
						st.set("count", String.valueOf(0));
						htmltext = "36479-end.htm";
						st.giveItems(14603, 1);
						st.setState(State.COMPLETED);
						st.playSound("ItemSound.quest_finish");
						break;
		        	}
		        	break;
		}
		return htmltext;
	}

	@Override
	public String onDeath(L2Character killer, L2Character victim, QuestState st)
	{
		if (!(killer.isPlayer()) && !(victim.isPlayer()))
			return "";
		
		if (victim.getLevel() < 61)
			return "";

		final L2PcInstance killerPl = (L2PcInstance) killer;
		final L2PcInstance victimPl = (L2PcInstance) victim;
		
		if (victimPl.getSiegeSide() == killerPl.getSiegeSide())
			return "";
		
		int count = st.getInt("count");
		switch (st.getInt("cond"))
		{
			case 1:
				if (count < 9)
				{
					count += 1;
					st.set("count", String.valueOf(count));
					if (count == 9)
					{
						st.playSound("ItemSound.quest_middle");
						st.set("cond", "2");
					}
				}
				break;
			case 3:
				if (count < 18)
				{
					count += 1;
					st.set("count", String.valueOf(count));
					if (count == 18)
					{
						st.playSound("ItemSound.quest_middle");
						st.set("cond", "4");
					}
				}
				break;
			case 5:
				if (count < 27)
				{
					count += 1;
					st.set("count", String.valueOf(count));
					if (count == 27)
					{
						st.playSound("ItemSound.quest_middle");
						st.set("cond", "6");
					}
				}
				break;
			case 7:
				if (count < 36)
				{
					count += 1;
					st.set("count", String.valueOf(count));
					if (count == 36)
					{
						st.playSound("ItemSound.quest_middle");
						st.set("cond", "8");
					}
				}
				break;					
		}
		return "";
	}

	public static void main(String[] args)
	{
		new _176_StepsForHonor(176, qn, "");
	}
}
