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
 * Created by LordWinter 06.08.2011 Based on L2J Eternity-World
 */
public class _124_MeetingTheElroki extends Quest 
{
    	private static final String qn = "_124_MeetingTheElroki";

	private final int MARQUEZ 	= 32113;
	private final int MUSHIKA	= 32114;
	private final int ASAMAH 	= 32115;
	private final int KARAKAWEI 	= 32117;
	private final int MANTARASA 	= 32118;

	private final int MUSHIKA_EGG 	= 8778;

	public _124_MeetingTheElroki(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(MARQUEZ);
		addTalkId(MARQUEZ);
		addTalkId(MUSHIKA);
		addTalkId(ASAMAH);
		addTalkId(KARAKAWEI);
		addTalkId(MANTARASA);
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

		int cond = st.getInt("cond");

		if(event.equalsIgnoreCase("32113-02.htm"))
		{
			st.setState(State.STARTED);
		}
		else if(event.equalsIgnoreCase("32113-03.htm"))
		{
			if(cond == 0)
			{
         			st.set("cond","1");
         			st.playSound("ItemSound.quest_accept");
			}
		}
		else if(event.equalsIgnoreCase("32113-04.htm"))
		{
			if(cond == 1)
			{
				st.set("cond", "2");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if(event.equalsIgnoreCase("32114-02.htm"))
		{
			if(cond == 2)
			{
				st.set("cond", "3");
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(event.equalsIgnoreCase("32115-04.htm"))
		{
			if(cond == 3)
			{
				st.set("cond", "4");
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(event.equalsIgnoreCase("32117-02.htm"))
		{
			if(cond == 4)
				st.set("progress","1");
		}
		else if(event.equalsIgnoreCase("32117-03.htm"))
		{
			if(cond == 4)
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(event.equalsIgnoreCase("32118-02.htm"))
		{
			st.giveItems(MUSHIKA_EGG, 1);
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
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

		int npcId = npc.getId();
		int cond = st.getInt("cond");

		switch (st.getState())
		{
			case State.CREATED:
				if(npcId == MARQUEZ)
				{
					if(player.getLevel() < 75)
					{
						htmltext = "32113-01a.htm";
						st.exitQuest(false);
					}
					else
					{
						htmltext = "32113-01.htm";
					}
				}
				break;
			case State.STARTED:
				if(npcId == MARQUEZ)
				{
					if(cond == 1)
					{
						htmltext = "32113-03.htm";
					}
					else if(cond == 2)
					{
						htmltext = "32113-04a.htm";
					}
				}
				else if(npcId == MUSHIKA)
				{
					if(cond == 2)
					{
						htmltext = "32114-01.htm";
					}
				}
				else if(npcId == ASAMAH)
				{
					if(cond == 3)
					{
						htmltext = "32115-01.htm";
					}
					else if(cond == 6)
					{
						htmltext = "32115-05.htm";
						st.takeItems(MUSHIKA_EGG, 1);
						st.giveItems(57, 100013);
						st.unset("cond");
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
					}
				}
				else if(npcId == KARAKAWEI)
				{
					if(cond == 4)
					{
						htmltext = "32117-01.htm";
						if(st.getInt("progress") == 1)
						{
								htmltext = "32117-02.htm";
						}
					}
					else if(cond == 5)
					{
						htmltext = "32117-04.htm";
					}
				}
				else if(npcId == MANTARASA)
				{
					if(cond == 5)
					{
						htmltext = "32118-01.htm";
					}
				}
				break;
			case State.COMPLETED:
                		htmltext = Quest.getAlreadyCompletedMsg(player);
                		break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _124_MeetingTheElroki(124, qn, "");
	}
}