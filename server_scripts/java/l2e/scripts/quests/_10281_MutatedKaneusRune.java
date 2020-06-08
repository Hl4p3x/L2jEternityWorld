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
public class _10281_MutatedKaneusRune extends Quest
{
	private static final String qn = "_10281_MutatedKaneusRune";
	
	// NPCs
	private static final int MATHIAS = 31340;
	private static final int KAYAN = 31335;
	private static final int WHITE_ALLOSCE = 18577;
	
	// Items
	private static final int TISSUE_WA = 13840;
	
	public _10281_MutatedKaneusRune(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(MATHIAS);
		addTalkId(MATHIAS);
		addTalkId(KAYAN);
		
		addKillId(WHITE_ALLOSCE);
		
		questItemIds = new int[]
		{
			TISSUE_WA
		};
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
		
		switch (event)
		{
			case "31340-03.htm":
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
				break;
			case "31335-03.htm":
				st.rewardItems(57, 360000);
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
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
			case MATHIAS:
				if (st.isCompleted())
				{
					htmltext = "31340-06.htm";
				}
				else if (st.isCreated())
				{
					htmltext = (player.getLevel() >= 68) ? "31340-01.htm" : "31340-00.htm";
				}
				else if (st.hasQuestItems(TISSUE_WA))
				{
					htmltext = "31340-05.htm";
				}
				else if (st.getInt("cond") == 1)
				{
					htmltext = "31340-04.htm";
				}
				break;
			case KAYAN:
				if (st.isCompleted())
				{
					htmltext = Quest.getAlreadyCompletedMsg(player);
				}
				else if (st.hasQuestItems(TISSUE_WA))
				{
					htmltext = "31335-02.htm";
				}
				else
				{
					htmltext = "31335-01.htm";
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
				if ((st != null) && st.isStarted() && (st.getInt("cond") == 1))
				{
					if ((npcId == WHITE_ALLOSCE) && (!st.hasQuestItems(TISSUE_WA)))
					{
						PartyMembers.add(st);
					}
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
		if ((npcId == WHITE_ALLOSCE) && !st.hasQuestItems(TISSUE_WA))
		{
			st.giveItems(TISSUE_WA, 1);
			st.playSound("ItemSound.quest_itemget");
		}
	}
	
	public static void main(String[] args)
	{
		new _10281_MutatedKaneusRune(10281, qn, "");
	}
}