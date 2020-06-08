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

import java.util.Calendar;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.util.Util;

/**
 * Fixed by L2J Etermity-World
 */
public class _453_NotStrongEnoughAlone extends Quest
{
	private static final String qn = "_453_NotStrongEnoughAlone";

	// NPC
	private static final int Klemis = 32734;
	private static final int[] Monsters1 = { 22746, 22747, 22748, 22749, 22750, 22751, 22752, 22753 };
	private static final int[] Monsters2 = { 22754, 22755, 22756, 22757, 22758, 22759 };
	private static final int[] Monsters3 = { 22760, 22761, 22762, 22763, 22764, 22765 };
	
	// Restart Time
	private static final int ResetHour = 6;
	private static final int ResetMin = 30;

	public _453_NotStrongEnoughAlone(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(Klemis);
		addTalkId(Klemis);
		
		for (int i : Monsters1)
			addKillId(i);

		for (int i : Monsters2)
			addKillId(i);

		for (int i : Monsters3)
			addKillId(i);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32734-06.html"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32734-07.html"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32734-08.html"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32734-09.html"))
		{
			st.set("cond", "4");
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
			return htmltext;

		QuestState prev = player.getQuestState("_10282_ToTheSeedOfAnnihilation");
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 84 && prev != null && prev.getState() == State.COMPLETED)
					htmltext = "32734-01.html";
				else
					htmltext = "32734-03.html";
				break;
			case State.STARTED:
				if (st.getInt("cond") == 1)
					htmltext = "32734-10.html";
				else if (st.getInt("cond") == 2)
					htmltext = "32734-11.html";
				else if (st.getInt("cond") == 3)
					htmltext = "32734-12.html";
				else if (st.getInt("cond") == 4)
					htmltext = "32734-13.html";
				else if (st.getInt("cond") == 5)
				{
					boolean i1 = getRandomBoolean();
					int i0 = getRandom(100);
					if (i1)
					{
						if (i0 < 9)
							st.giveItems(15815, 1);
						else if (i0 < 18)
							st.giveItems(15816, 1);
						else if (i0 < 27)
							st.giveItems(15817, 1);
						else if (i0 < 36)
							st.giveItems(15818, 1);
						else if (i0 < 47)
							st.giveItems(15819, 1);
						else if (i0 < 56)
							st.giveItems(15820, 1);
						else if (i0 < 65)
							st.giveItems(15821, 1);
						else if (i0 < 74)
							st.giveItems(15822, 1);
						else if (i0 < 83)
							st.giveItems(15823, 1);
						else if (i0 < 92)
							st.giveItems(15824, 1);
						else
							st.giveItems(15825, 1);
					}
					else
					{
						if (i0 < 9)
							st.giveItems(15634, 1);
						else if (i0 < 18)
							st.giveItems(15635, 1);
						else if (i0 < 27)
							st.giveItems(15636, 1);
						else if (i0 < 36)
							st.giveItems(15637, 1);
						else if (i0 < 47)
							st.giveItems(15638, 1);
						else if (i0 < 56)
							st.giveItems(15639, 1);
						else if (i0 < 65)
							st.giveItems(15640, 1);
						else if (i0 < 74)
							st.giveItems(15641, 1);
						else if (i0 < 83)
							st.giveItems(15642, 1);
						else if (i0 < 92)
							st.giveItems(15643, 1);
						else
							st.giveItems(15644, 1);
					}
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
					htmltext = "32734-14.html";
					
					Calendar reset = Calendar.getInstance();
					reset.set(Calendar.MINUTE, ResetMin);
					if (reset.get(Calendar.HOUR_OF_DAY) >= ResetHour)
						reset.add(Calendar.DATE, 1);
					reset.set(Calendar.HOUR_OF_DAY, ResetHour);
					st.set("reset", String.valueOf(reset.getTimeInMillis()));
				}
				break;
			case State.COMPLETED:
				if (Long.parseLong(st.get("reset")) > System.currentTimeMillis())
					htmltext = "32734-02.html";
				else
				{
					st.setState(State.CREATED);
					if (player.getLevel() >= 84 && prev != null && prev.getState() == State.COMPLETED)
						htmltext = "32734-01.html";
					else
						htmltext = "32734-03.html";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (player.getParty() != null)
		{
			for (L2PcInstance member : player.getParty().getMembers())
			{
				increaseNpcKill(member, npc);
			}
		}
		else
			increaseNpcKill(player, npc);

		return null;
	}
	
	private void increaseNpcKill(L2PcInstance player, L2Npc npc)
	{
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return;
		
		if (Util.contains(Monsters1, npc.getId()) && st.getInt("cond") == 2)
		{
			int val = 0;
			
			if (npc.getId() == Monsters1[0] || npc.getId() == Monsters1[4])
				val = Monsters1[0];
			else if (npc.getId() == Monsters1[1] || npc.getId() == Monsters1[5])
				val = Monsters1[1];
			else if (npc.getId() == Monsters1[2] || npc.getId() == Monsters1[6])
				val = Monsters1[2];
			else if (npc.getId() == Monsters1[3] || npc.getId() == Monsters1[7])
				val = Monsters1[3];
			
			int i = st.getInt(String.valueOf(val));
			if (i < 15)
				st.set(String.valueOf(val), String.valueOf(i + 1));
			
			if (st.getInt(String.valueOf(Monsters1[0])) >= 15 && st.getInt(String.valueOf(Monsters1[1])) >= 15 && st.getInt(String.valueOf(Monsters1[2])) >= 15 && st.getInt(String.valueOf(Monsters1[3])) >= 15)
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		else if (Util.contains(Monsters2, npc.getId()) && st.getInt("cond") == 3)
		{
			int val = 0;
			
			if (npc.getId() == Monsters2[0] || npc.getId() == Monsters2[3])
				val = Monsters2[0];
			else if (npc.getId() == Monsters2[1] || npc.getId() == Monsters2[4])
				val = Monsters2[1];
			else if (npc.getId() == Monsters2[2] || npc.getId() == Monsters2[5])
				val = Monsters2[2];
			
			int i = st.getInt(String.valueOf(val));
			if (i < 20)
				st.set(String.valueOf(val), String.valueOf(i + 1));
			
			if (st.getInt(String.valueOf(Monsters2[0])) >= 20 && st.getInt(String.valueOf(Monsters2[1])) >= 20 && st.getInt(String.valueOf(Monsters2[2])) >= 20)
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		else if (Util.contains(Monsters3, npc.getId()) && st.getInt("cond") == 4)
		{
			int val = 0;
			
			if (npc.getId() == Monsters3[0] || npc.getId() == Monsters3[3])
				val = Monsters3[0];
			else if (npc.getId() == Monsters3[1] || npc.getId() == Monsters3[4])
				val = Monsters3[1];
			else if (npc.getId() == Monsters3[2] || npc.getId() == Monsters3[5])
				val = Monsters3[2];
			
			int i = st.getInt(String.valueOf(val));
			if (i < 20)
				st.set(String.valueOf(val), String.valueOf(i + 1));
			
			if (st.getInt(String.valueOf(Monsters3[0])) >= 20 && st.getInt(String.valueOf(Monsters3[1])) >= 20 && st.getInt(String.valueOf(Monsters3[2])) >= 20)
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
	}
	
	public static void main(String[] args)
	{
		new _453_NotStrongEnoughAlone(453, qn, "");
	}
}