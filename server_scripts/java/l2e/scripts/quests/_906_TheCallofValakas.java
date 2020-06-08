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
import l2e.gameserver.model.quest.QuestState.QuestType;
import l2e.gameserver.model.quest.State;
import l2e.util.Rnd;

/**
 * Created by LordWinter 11.05.2012 Based on L2J Eternity-World
 */
public class _906_TheCallofValakas extends Quest
{
	private static final String qn = "_906_TheCallofValakas";
	
	private static final int Klein = 31540;
	private static final int LavasaurusAlphaFragment = 21993;
	private static final int ValakasMinion = 29029;
	
	public _906_TheCallofValakas(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Klein);
		addTalkId(Klein);
		
		addKillId(ValakasMinion);
		
		questItemIds = new int[]
		{
			LavasaurusAlphaFragment
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
		
		if (event.equalsIgnoreCase("31540-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31540-07.htm"))
		{
			st.takeItems(LavasaurusAlphaFragment, -1);
			st.giveItems(21895, 1);
			st.setState(State.COMPLETED);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(QuestType.DAILY);
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
		
		int cond = st.getInt("cond");
		
		if (npc.getId() == Klein)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 83)
					{
						if (st.getQuestItemsCount(7267) > 0)
						{
							htmltext = "31540-01.htm";
						}
						else
						{
							htmltext = "31540-00b.htm";
						}
					}
					else
					{
						htmltext = "31540-00.htm";
						st.exitQuest(true);
					}
					break;
				case State.STARTED:
					if (cond == 1)
					{
						htmltext = "31540-05.htm";
					}
					else if (cond == 2)
					{
						htmltext = "31540-06.htm";
					}
					break;
				case State.COMPLETED:
					if (st.isNowAvailable())
					{
						if (player.getLevel() >= 83)
						{
							if (st.getQuestItemsCount(7267) > 0)
							{
								htmltext = "31540-01.htm";
							}
							else
							{
								htmltext = "31540-00b.htm";
							}
						}
						else
						{
							htmltext = "31540-00.htm";
							st.exitQuest(true);
						}
					}
					else
					{
						htmltext = "31540-00a.htm";
					}
					break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, 1);
		
		if (partyMember == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		
		QuestState st = partyMember.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		int cond = st.getInt("cond");
		int npcId = npc.getId();
		
		if (cond == 1)
		{
			if ((npcId == ValakasMinion) && Rnd.calcChance(40))
			{
				st.giveItems(LavasaurusAlphaFragment, 1);
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
		}
		
		if (player.getParty() != null)
		{
			QuestState st2;
			for (L2PcInstance pmember : player.getParty().getMembers())
			{
				st2 = pmember.getQuestState(qn);
				
				if ((st2 != null) && (cond == 1) && (pmember.getObjectId() != partyMember.getObjectId()))
				{
					if ((npcId == ValakasMinion) && Rnd.calcChance(40))
					{
						st.giveItems(LavasaurusAlphaFragment, 1);
						st.set("cond", "2");
						st.playSound("ItemSound.quest_middle");
					}
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _906_TheCallofValakas(906, qn, "");
	}
}