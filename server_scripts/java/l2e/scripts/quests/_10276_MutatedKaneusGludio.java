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

import javolution.util.FastList;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 31.01.2012 Based on L2J Eternity-World
 */
public class _10276_MutatedKaneusGludio extends Quest
{
	private static final String qn = "_10276_MutatedKaneusGludio";
	
	// NPCs
	private static final int BATHIS = 30332;
	private static final int ROHMER = 30344;
	private static final int TOMLAN_KAMOS = 18554;
	private static final int OL_ARIOSH = 18555;
	
	// Items
	private static final int TISSUE_TK = 13830;
	private static final int TISSUE_OA = 13831;
	
	public _10276_MutatedKaneusGludio(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(BATHIS);
		addTalkId(BATHIS, ROHMER);
		
		addKillId(TOMLAN_KAMOS, OL_ARIOSH);
		
		questItemIds = new int[]
		{
			TISSUE_TK,
			TISSUE_OA
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30332-03.htm":
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
				break;
			case "30344-03.htm":
				st.rewardItems(57, 8500);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (npc.getId())
		{
			case BATHIS:
				if (st.isCompleted())
				{
					htmltext = "30332-06.htm";
				}
				else if (st.isCreated())
				{
					htmltext = (player.getLevel() >= 18) ? "30332-01.htm" : "30332-00.htm";
				}
				else if (st.hasQuestItems(TISSUE_TK) && st.hasQuestItems(TISSUE_OA))
				{
					htmltext = "30332-05.htm";
				}
				else if (st.getInt("cond") == 1)
				{
					htmltext = "30332-04.htm";
				}
				break;
			case ROHMER:
				if (st.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				else if (st.hasQuestItems(TISSUE_TK) && st.hasQuestItems(TISSUE_OA))
				{
					htmltext = "30344-02.htm";
				}
				else
				{
					htmltext = "30344-01.htm";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		QuestState st = killer.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		final int npcId = npc.getId();
		if (killer.getParty() != null)
		{
			final FastList<QuestState> PartyMembers = new FastList<>();
			for (L2PcInstance member : killer.getParty().getMembers())
			{
				st = member.getQuestState(qn);
				if ((st != null) && st.isStarted() && (st.getInt("cond") == 1) && (((npcId == TOMLAN_KAMOS) && !st.hasQuestItems(TISSUE_TK)) || ((npcId == TISSUE_OA) && !st.hasQuestItems(TISSUE_OA))))
				{
					PartyMembers.add(st);
				}
			}
			
			if (!PartyMembers.isEmpty())
			{
				rewardItem(npcId, PartyMembers.get(getRandom(PartyMembers.size())));
			}
		}
		else
		{
			rewardItem(npcId, st);
		}
		
		return null;
	}
	
	private final void rewardItem(int npcId, QuestState st)
	{
		if ((npcId == TOMLAN_KAMOS) && !st.hasQuestItems(TISSUE_TK))
		{
			st.giveItems(TISSUE_TK, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((npcId == OL_ARIOSH) && !st.hasQuestItems(TISSUE_OA))
		{
			st.giveItems(TISSUE_OA, 1);
			st.playSound("ItemSound.quest_itemget");
		}
	}
	
	public static void main(String[] args)
	{
		new _10276_MutatedKaneusGludio(10276, qn, "");
	}
}