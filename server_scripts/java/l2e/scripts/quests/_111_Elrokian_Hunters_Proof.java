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

import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 15.03.2011
 * Based on L2J Eternity-World
 */
public class _111_Elrokian_Hunters_Proof extends Quest
{
    	private static final String qn = "_111_Elrokian_Hunters_Proof";

	// NPCs: MARQUEZ,MUSHIKA,ASHAMAH,KIRIKASHIN
	private final static int QUEST_NPC[] = 
	{
		32113, 32114, 32115, 32116, 32117
	};

	// Fragment
	private final static int QUEST_ITEM[] = { 8768 };
		
	private final static int QUEST_MONSTERS1[] = { 22196, 22197, 22198 , 22218 };
	private final static int QUEST_MONSTERS2[] = { 22200, 22201, 22202, 22219 };
	private final static int QUEST_MONSTERS3[] = { 22208, 22209, 22210, 22221 };
	private final static int QUEST_MONSTERS4[] = { 22203, 22204, 22205, 22220 };

	public _111_Elrokian_Hunters_Proof(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(QUEST_NPC[0]);
		for (final int i : QUEST_NPC)
			addTalkId(i);

		for (final int i : QUEST_MONSTERS1)
			addKillId(i);
		for (final int i : QUEST_MONSTERS2)
			addKillId(i);
		for (final int i : QUEST_MONSTERS3)
			addKillId(i);
		for (final int i : QUEST_MONSTERS4)
			addKillId(i);	
	
		questItemIds = QUEST_ITEM;
	}	
	
	private boolean checkPartyCondition(QuestState st, L2PcInstance leader)
	{		
		if (leader == null)
			return false;
		
		final L2Party party = leader.getParty();
		if (party == null)
			return false;
		
		if (party.getLeader() != leader)
			return false;
		
		for (L2PcInstance player: party.getMembers())
		{
			if (player.getLevel() < 75)
				return false;
		}		
		return true;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		final int cond = st.getInt("cond");
		final int npcId = npc.getId();
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (npcId == QUEST_NPC[0])
				{
					if (checkPartyCondition(st, player))
					{
						st.set("cond","1");
						st.playSound("ItemSound.quest_accept");
						st.setState(State.STARTED);
						htmltext = "32113-1.htm";
					}
					else
					{
				        	st.exitQuest(true);
				        	htmltext = "32113-0.htm";
					}
				}
				break;
			case State.STARTED:
				if (npcId == QUEST_NPC[0])
				{
					switch (cond)
					{
						case 3:
							st.set("cond","4");
							st.playSound("ItemSound.quest_middle");
							htmltext = "32113-2.htm";
							break;
						case 5:
							if(st.getQuestItemsCount(QUEST_ITEM[0]) >= 50)
							{
								st.takeItems(QUEST_ITEM[0], -1);
								st.set("cond", "6");
								st.playSound("ItemSound.quest_middle");
								htmltext = "32113-3.htm";
							}
							break;
					}
				}
				else if (npcId == QUEST_NPC[1])
				{
					if (cond == 1)
					{
						st.set("cond","2");
						st.playSound("ItemSound.quest_middle");
						htmltext = "32114-1.htm";
					}
				}
				else if (npcId == QUEST_NPC[2])
				{
					switch (cond)
					{
						case 2:
							st.set("cond","3");
							st.playSound("ItemSound.quest_middle");
							htmltext = "32115-1.htm";
							break;
						case 8:
							st.set("cond","9");
							st.playSound("ItemSound.quest_middle");
							htmltext = "32115-2.htm";
							break;
						case 9:
							st.set("cond","10");
							st.playSound("ItemSound.quest_middle");
							htmltext = "32115-3.htm";
							break;
						case 11:
							st.set("cond","12");
							st.playSound("ItemSound.quest_middle");
							st.giveItems(8773,1);
							htmltext = "32115-5.htm";
							break;
					}
				}
				else if (npcId == QUEST_NPC[3])
				{
					switch (cond)
					{
						case 6:
							st.set("cond","8");
							st.playSound("EtcSound.elcroki_song_full");
							htmltext = "32116-1.htm";
							break;
						case 12:
							if (st.getQuestItemsCount(8773) >= 1)
							{
								st.takeItems(8773,1);
								st.giveItems(8763,1);
								st.giveItems(8764,100);
								st.giveItems(57,1022636);
								st.playSound("ItemSound.quest_finish");
								st.exitQuest(false);
								htmltext = "32116-2.htm";	
							}
							break;
					}
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isSummon)
	{		
		if (player == null || player.getParty() == null)
			return super.onKill(npc, player, isSummon);
		
		final QuestState st = player.getParty().getLeader().getQuestState(qn);
		if (st == null || st.getState() != State.STARTED)
			return super.onKill(npc, player, isSummon);
		
		final int cond = st.getInt("cond");
		final int npcId = npc.getId();
		
		switch (cond)
		{
			case 4:
				if (Util.contains(QUEST_MONSTERS1, npcId))
				{
					if (getRandom(100) < 25)
					{
						st.giveItems(QUEST_ITEM[0], 1);
						if (st.getQuestItemsCount(QUEST_ITEM[0]) <= 49)
							st.playSound("ItemSound.quest_itemget");
						else
						{
							st.set("cond","5");
							st.playSound("ItemSound.quest_middle");
						}
					}
				}
				break;
			case 10:
				if (Util.contains(QUEST_MONSTERS2, npcId))
				{
					if (getRandom(100) < 75)
					{
						st.giveItems(8770,1);
						if (st.getQuestItemsCount(8770) <= 9)
							st.playSound("ItemSound.quest_itemget");
					}
				}
				else if (Util.contains(QUEST_MONSTERS3, npcId))
				{
					if (getRandom(100) < 75)
					{
						st.giveItems(8772,1);
						if (st.getQuestItemsCount(8771) <= 9)
							st.playSound("ItemSound.quest_itemget");
					}
				}
				else if (Util.contains(QUEST_MONSTERS4, npcId))
				{
					if (getRandom(100) < 75)
					{
						st.giveItems(8771,1);
						if (st.getQuestItemsCount(8772) <= 9)
							st.playSound("ItemSound.quest_itemget");
					}
				}

				if (st.getQuestItemsCount(8770) >= 10 && st.getQuestItemsCount(8771) >= 10 && st.getQuestItemsCount(8772) >= 10)
				{
					st.set("cond","11");
					st.playSound("ItemSound.quest_middle");
				}
				break;				
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _111_Elrokian_Hunters_Proof(111, qn, "");
	}	
}