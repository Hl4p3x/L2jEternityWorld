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
 * Created by LordWinter 23.05.2012 Based on L2J Eternity-World
 */
public class _10296_SevenSignsPoweroftheSeal extends Quest
{
	private static final String qn = "_10296_SevenSignsPoweroftheSeal";
	
	// NPC`s
	private static final int EVIL = 32792;
	private static final int ELCARDIA1 = 32787;
	private static final int ELCARDIA = 32784;
	private static final int HARDIN = 30832;
	private static final int WOOD = 32593;
	private static final int FRANZ = 32597;
	
	private static final int[] NPCs =
	{
		EVIL,
		ELCARDIA1,
		ELCARDIA,
		HARDIN,
		WOOD,
		FRANZ
	};
	
	// MOB`s
	private static final int ETISETINA = 18949;
	
	public _10296_SevenSignsPoweroftheSeal(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(EVIL);
		
		for (int id : NPCs)
		{
			addTalkId(id);
		}
		
		addKillId(ETISETINA);
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
		
		if (event.equalsIgnoreCase("32792-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32784-03.htm"))
		{
			st.set("cond", "4");
		}
		else if (event.equalsIgnoreCase("30832-03.htm"))
		{
			st.set("cond", "5");
		}
		else if (event.equalsIgnoreCase("32597-03.htm"))
		{
			if (player.getLevel() >= 81)
			{
				st.addExpAndSp(125000000, 12500000);
				st.giveItems(17265, 1);
				st.setState(State.COMPLETED);
				st.unset("cond");
				st.unset("EtisKilled");
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else
			{
				htmltext = "32597-00.htm";
			}
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
		int EtisKilled = st.getInt("EtisKilled");
		
		if (player.isSubClassActive())
		{
			return "no_subclass-allowed.htm";
		}
		
		if (npcId == EVIL)
		{
			if (cond == 0)
			{
				QuestState qs = player.getQuestState("_10295_SevenSignsSolinasTomb");
				if ((player.getLevel() >= 81) && (qs != null) && qs.isCompleted())
				{
					htmltext = "32792-01.htm";
				}
				else
				{
					htmltext = "32792-00.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				htmltext = "32792-04.htm";
			}
			else if (cond == 2)
			{
				htmltext = "32792-05.htm";
			}
			else if (cond >= 3)
			{
				htmltext = "32792-06.htm";
			}
		}
		else if (npcId == ELCARDIA1)
		{
			if (cond == 1)
			{
				htmltext = "32787-01.htm";
			}
			else if (cond == 2)
			{
				if (EtisKilled == 0)
				{
					htmltext = "32787-01.htm";
				}
				else
				{
					st.set("cond", "3");
					htmltext = "32787-02.htm";
				}
			}
			else if (cond >= 3)
			{
				htmltext = "32787-04.htm";
			}
		}
		else if (npcId == ELCARDIA)
		{
			if (cond == 3)
			{
				htmltext = "32784-01.htm";
			}
			else if (cond >= 4)
			{
				htmltext = "32784-03.htm";
			}
		}
		else if (npcId == HARDIN)
		{
			if (cond == 4)
			{
				htmltext = "30832-01.htm";
			}
			else if (cond == 5)
			{
				htmltext = "30832-04.htm";
			}
		}
		else if (npcId == WOOD)
		{
			if (cond == 5)
			{
				htmltext = "32593-01.htm";
			}
		}
		else if (npcId == FRANZ)
		{
			if (cond == 5)
			{
				htmltext = "32597-01.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		
		int npcId = npc.getId();
		
		if (npcId == ETISETINA)
		{
			st.set("EtisKilled", 1);
			player.showQuestMovie(30);
			
		}
		return null;
	}
	
	public static void main(String args[])
	{
		new _10296_SevenSignsPoweroftheSeal(10296, qn, "");
	}
}