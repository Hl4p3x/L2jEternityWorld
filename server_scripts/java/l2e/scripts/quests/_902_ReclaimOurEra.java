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
import l2e.gameserver.util.Util;

public class _902_ReclaimOurEra extends Quest
{
	private static final String qn = "_902_ReclaimOurEra";
	
	private static final int Mathias = 31340;
	private static final int[] Ketra_Varka = {25309, 25312, 25315, 25299, 25302, 25305};
	private static final int[] Stakato_Nest = {25667, 25668, 25669, 25670};
	private static final int Monastery_of_Silence = 29096;
	private static final int[] boss_for_kills = {25309, 25312, 25315, 25299, 25302, 25305, 25667, 25668, 25669, 25670, 29096};
	private static final int _Shattered_Bones = 21997;
	private static final int _Stakato_Leader_Claw = 21998;
	private static final int _AnaisScroll = 21999;
	private static final int _Blue_Elmore_Coin = 21750;
	
	public _902_ReclaimOurEra(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(Mathias);
		addTalkId(Mathias);
		for(int _npc : boss_for_kills)
		{
			addKillId(_npc);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;
		
		if (npc.getId() == Mathias)
		{
			if (event.equalsIgnoreCase("31340-04.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("31340-06.htm"))
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
			else if (event.equalsIgnoreCase("31340-08.htm"))
			{
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			}
			else if (event.equalsIgnoreCase("31340-10.htm"))
			{
				st.set("cond", "4");
				st.playSound("ItemSound.quest_middle");
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
			return htmltext;
		
		if (npc.getId() == Mathias)
		{
			switch(st.getState())
			{
				case State.CREATED :
					if (player.getLevel() >= 80)
						htmltext = "31340-01.htm";
					else
						htmltext = "31340-00.htm";
					break;
				case State.COMPLETED :
					htmltext = "31340-completed.htm";
					break;
				case State.STARTED :
					if (st.getInt("cond") == 2)
						htmltext = "31340-07.htm";
					else if (st.getInt("cond") == 3)
						htmltext = "31340-09.htm";
					else if (st.getInt("cond") == 4)
						htmltext = "31340-11.htm";
							
					else if (st.getInt("cond") == 5 && st.getQuestItemsCount(_Shattered_Bones) > 0)
					{
						htmltext = "31340-12.htm";
						st.takeItems(_Shattered_Bones, 1);
						st.giveItems(_Blue_Elmore_Coin, 1);
						st.giveItems(57, 134038);
						st.unset("cond");
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
					}
					else if (st.getInt("cond") == 5 && st.getQuestItemsCount(_Stakato_Leader_Claw) > 0)
					{
						htmltext = "31340-12.htm";
						st.takeItems(_Stakato_Leader_Claw, 1);
						st.giveItems(_Blue_Elmore_Coin, 3);
						st.giveItems(57, 210119);
						st.unset("cond");
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
					}
					else if (st.getInt("cond") == 5 && st.getQuestItemsCount(_AnaisScroll) > 0)
					{
						htmltext = "31340-12.htm";
						st.takeItems(_AnaisScroll, 1);
						st.giveItems(_Blue_Elmore_Coin, 3);
						st.giveItems(57, 348155);
						st.unset("cond");
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
					}
					else
						htmltext = "31340-05.htm";
					break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (killer.isInParty())
		{
			for(L2PcInstance player : killer.getParty().getMembers())
				rewardPlayer(npc, player);
		}
		else
			rewardPlayer(npc, killer);
		return super.onKill(npc, killer, isSummon);
	}
	
	private void rewardPlayer(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st != null && st.getState() == State.STARTED && player.isInsideRadius(npc, 2000, false, false))
		{
			if (st.getInt("cond") == 2 && Util.contains(Ketra_Varka, npc.getId()))
			{
				st.giveItems(_Shattered_Bones, 1);
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "5");
			}
			else if (st.getInt("cond") == 3 && Util.contains(Stakato_Nest, npc.getId()))
			{
				st.giveItems(_Stakato_Leader_Claw, 1);
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "5");
			}
			else if (st.getInt("cond") == 4 && npc.getId() == Monastery_of_Silence)
			{
				st.giveItems(_AnaisScroll, 1);
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "5");
			}		
		}
	}

	public static void main(String[] args)
	{
		new _902_ReclaimOurEra(902, qn, "");
	}
}