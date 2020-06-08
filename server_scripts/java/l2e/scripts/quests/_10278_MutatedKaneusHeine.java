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
public class _10278_MutatedKaneusHeine extends Quest
{
	private static final String qn = "_10278_MutatedKaneusHeine";
	
	// NPCs
	private static final int GOSTA = 30916;
	private static final int MINEVIA = 30907;
	private static final int BLADE_OTIS = 18562;
	private static final int WEIRD_BUNEI = 18564;
	
	// Items
	private static final int TISSUE_BO = 13834;
	private static final int TISSUE_WB = 13835;
	
	public _10278_MutatedKaneusHeine(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(GOSTA);
		addTalkId(MINEVIA);
		addTalkId(MINEVIA);
		
		addKillId(BLADE_OTIS);
		addKillId(WEIRD_BUNEI);
		
		questItemIds = new int[]
		{
			TISSUE_BO,
			TISSUE_WB
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
			case "30916-03.htm":
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
				break;
			case "30907-03.htm":
				st.rewardItems(57, 50000);
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
			case GOSTA:
				if (st.isCompleted())
				{
					htmltext = "30916-06.htm";
				}
				else if (st.isCreated())
				{
					htmltext = (player.getLevel() >= 38) ? "30916-01.htm" : "30916-00.htm";
				}
				else if (st.hasQuestItems(TISSUE_BO) && st.hasQuestItems(TISSUE_WB))
				{
					htmltext = "30916-05.htm";
				}
				else if (st.getInt("cond") == 1)
				{
					htmltext = "30916-04.htm";
				}
				break;
			case MINEVIA:
				if (st.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				else if (st.hasQuestItems(TISSUE_BO) && !st.hasQuestItems(TISSUE_WB))
				{
					htmltext = "30907-02.htm";
				}
				else
				{
					htmltext = "30907-01.htm";
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
				if ((st != null) && st.isStarted() && (st.getInt("cond") == 1) && (((npcId == BLADE_OTIS) && !st.hasQuestItems(TISSUE_BO)) || ((npcId == WEIRD_BUNEI) && !st.hasQuestItems(TISSUE_WB))))
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
		if ((npcId == BLADE_OTIS) && !st.hasQuestItems(TISSUE_BO))
		{
			st.giveItems(TISSUE_BO, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if ((npcId == WEIRD_BUNEI) && !st.hasQuestItems(TISSUE_WB))
		{
			st.giveItems(TISSUE_WB, 1);
			st.playSound("ItemSound.quest_itemget");
		}
	}
	
	public static void main(String[] args)
	{
		new _10278_MutatedKaneusHeine(10278, qn, "");
	}
}