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

/**
 * Created by LordWinter 30.04.2012 Based on L2J Eternity-World
 */
public class _10505_JewelOfValakas extends Quest
{
	private static final String qn = "_10505_JewelOfValakas";
	
	// NPC's
	private static final int KLEIN = 31540;
	private static final int VALAKAS = 29028;
	
	// Item's
	private static final int EMPTY_CRYSTAL = 21906;
	private static final int FILLED_CRYSTAL_VALAKAS = 21908;
	private static final int VACUALITE_FLOATING_STONE = 7267;
	private static final int JEWEL_OF_VALAKAS = 21896;
	
	public _10505_JewelOfValakas(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(KLEIN);
		addTalkId(KLEIN);
		
		addKillId(VALAKAS);
		
		questItemIds = new int[]
		{
			EMPTY_CRYSTAL,
			FILLED_CRYSTAL_VALAKAS
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
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(EMPTY_CRYSTAL, 1);
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
		int id = st.getState();
		
		if (st.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		
		if ((id == State.CREATED) && (cond == 0))
		{
			if (npcId == KLEIN)
			{
				if (player.getLevel() < 84)
				{
					htmltext = "31540-00.htm";
				}
				else if (st.getQuestItemsCount(VACUALITE_FLOATING_STONE) < 1)
				{
					htmltext = "31540-00a.htm";
				}
				else
				{
					htmltext = "31540-01.htm";
				}
			}
		}
		else if (id == State.STARTED)
		{
			if (npcId == KLEIN)
			{
				if (cond == 1)
				{
					if (st.getQuestItemsCount(EMPTY_CRYSTAL) < 1)
					{
						htmltext = "31540-08.htm";
						st.giveItems(EMPTY_CRYSTAL, 1);
					}
					else
					{
						htmltext = "31540-05.htm";
					}
				}
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(FILLED_CRYSTAL_VALAKAS) >= 1)
					{
						htmltext = "31540-07.htm";
						st.takeItems(FILLED_CRYSTAL_VALAKAS, -1);
						st.giveItems(JEWEL_OF_VALAKAS, 1);
						st.playSound("ItemSound.quest_finish");
						st.setState(State.COMPLETED);
						st.exitQuest(QuestType.DAILY);
					}
					else
					{
						htmltext = "31540-06.htm";
					}
				}
			}
		}
		else if (id == State.COMPLETED)
		{
			if (npcId == KLEIN)
			{
				if (st.isNowAvailable())
				{
					if (player.getLevel() < 84)
					{
						htmltext = "31540-00.htm";
					}
					else if (st.getQuestItemsCount(VACUALITE_FLOATING_STONE) < 1)
					{
						htmltext = "31540-00a.htm";
					}
					else
					{
						htmltext = "31540-01.htm";
					}
				}
				else
				{
					htmltext = "31540-09.htm";
				}
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
			return super.onKill(npc, player, isSummon);
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		
		if ((cond == 1) && (npcId == VALAKAS))
		{
			st.takeItems(EMPTY_CRYSTAL, -1);
			st.giveItems(FILLED_CRYSTAL_VALAKAS, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		
		if (player.getParty() != null)
		{
			QuestState st2;
			for (L2PcInstance pmember : player.getParty().getMembers())
			{
				st2 = pmember.getQuestState(qn);
				
				if ((st2 != null) && (st2.getInt("cond") == 1) && (pmember.getObjectId() != partyMember.getObjectId()))
				{
					if (npcId == VALAKAS)
					{
						st.takeItems(EMPTY_CRYSTAL, -1);
						st.giveItems(FILLED_CRYSTAL_VALAKAS, 1);
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
		new _10505_JewelOfValakas(10505, qn, "");
	}
}