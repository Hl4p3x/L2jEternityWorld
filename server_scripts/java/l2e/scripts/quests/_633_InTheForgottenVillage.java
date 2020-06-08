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

import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 13.01.2013 Based on L2J Eternity-World
 */
public class _633_InTheForgottenVillage extends Quest
{
	private static final String qn = "_633_InTheForgottenVillage";
	
	private static final int MINA = 31388;
	
	private static final int RIB_BONE = 7544;
	private static final int ZOMBIE_LIVER = 7545;
	
	private static final Map<Integer, Integer> MOBS = new HashMap<>();
	{
		MOBS.put(21557, 328000);
		MOBS.put(21558, 328000);
		MOBS.put(21559, 337000);
		MOBS.put(21560, 337000);
		MOBS.put(21563, 342000);
		MOBS.put(21564, 348000);
		MOBS.put(21565, 351000);
		MOBS.put(21566, 359000);
		MOBS.put(21567, 359000);
		MOBS.put(21572, 365000);
		MOBS.put(21574, 383000);
		MOBS.put(21575, 383000);
		MOBS.put(21580, 385000);
		MOBS.put(21581, 395000);
		MOBS.put(21583, 397000);
		MOBS.put(21584, 401000);
	}
	
	private static final Map<Integer, Integer> UNDEADS = new HashMap<>();
	{
		UNDEADS.put(21553, 347000);
		UNDEADS.put(21554, 347000);
		UNDEADS.put(21561, 450000);
		UNDEADS.put(21578, 501000);
		UNDEADS.put(21596, 359000);
		UNDEADS.put(21597, 370000);
		UNDEADS.put(21598, 441000);
		UNDEADS.put(21599, 395000);
		UNDEADS.put(21600, 408000);
		UNDEADS.put(21601, 411000);
	}
	
	public _633_InTheForgottenVillage(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(MINA);
		addTalkId(MINA);
		
		for (int i : MOBS.keySet())
			addKillId(i);
		
		for (int i : UNDEADS.keySet())
			addKillId(i);

		questItemIds = new int[]
		{
			RIB_BONE,
			ZOMBIE_LIVER
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31388-04.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31388-10.htm"))
		{
			st.takeItems(RIB_BONE, -1);
			st.playSound("ItemSound.quest_giveup");
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("31388-09.htm"))
		{
			if (st.getQuestItemsCount(RIB_BONE) >= 200)
			{
				htmltext = "31388-08.htm";
				st.takeItems(RIB_BONE, 200);
				st.rewardItems(57, 25000);
				st.addExpAndSp(305235, 0);
				st.playSound("ItemSound.quest_finish");
			}
			st.set("cond", "1");
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
			case State.CREATED:
				if (player.getLevel() >= 65)
					htmltext = "31388-01.htm";
				else
				{
					htmltext = "31388-03.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "31388-06.htm";
				else if (cond == 2)
					htmltext = "31388-05.htm";
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		int npcId = npc.getId();
		
		if (UNDEADS.containsKey(npcId))
		{
			L2PcInstance partyMember = getRandomPartyMemberState(player, State.STARTED);
			if (partyMember == null)
				return null;
			
			partyMember.getQuestState(qn).dropItems(ZOMBIE_LIVER, 1, 0, UNDEADS.get(npcId));
		}
		else if (MOBS.containsKey(npcId))
		{
			L2PcInstance partyMember = getRandomPartyMember(player, 1);
			if (partyMember == null)
				return null;
			
			QuestState st = partyMember.getQuestState(qn);
			
			if (st.dropItems(RIB_BONE, 1, 200, MOBS.get(npcId)))
				st.set("cond", "2");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _633_InTheForgottenVillage(633, qn, "");
	}
}