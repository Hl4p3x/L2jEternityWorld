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
public class _10504_JewelOfAntharas extends Quest
{
	private static final String qn = "_10504_JewelOfAntharas";
	
	// NPC's
	private static final int THEODRIC = 30755;
	private static final int ULTIMATE_ANTHARAS = 29068;
	
	// Item's
	private static final int CLEAR_CRYSTAL = 21905;
	private static final int FILLED_CRYSTAL_ANTHARAS = 21907;
	private static final int PORTAL_STONE = 3865;
	private static final int JEWEL_OF_ANTHARAS = 21898;
	
	public _10504_JewelOfAntharas(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(THEODRIC);
		addTalkId(THEODRIC);
		
		addKillId(ULTIMATE_ANTHARAS);
		
		questItemIds = new int[]
		{
			CLEAR_CRYSTAL,
			FILLED_CRYSTAL_ANTHARAS
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
		
		if (event.equalsIgnoreCase("30755-04.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(CLEAR_CRYSTAL, 1);
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
		
		if ((id == State.CREATED) && (cond == 0))
		{
			if (npcId == THEODRIC)
			{
				if (player.getLevel() < 84)
				{
					htmltext = "30755-00.htm";
				}
				else if (st.getQuestItemsCount(PORTAL_STONE) < 1)
				{
					htmltext = "30755-00a.htm";
				}
				else
				{
					htmltext = "30755-01.htm";
				}
				
			}
		}
		else if (id == State.STARTED)
		{
			if (npcId == THEODRIC)
			{
				if (cond == 1)
				{
					if (st.getQuestItemsCount(CLEAR_CRYSTAL) < 1)
					{
						htmltext = "30755-08.htm";
						st.giveItems(CLEAR_CRYSTAL, 1);
					}
					else
					{
						htmltext = "30755-05.htm";
					}
				}
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(FILLED_CRYSTAL_ANTHARAS) >= 1)
					{
						htmltext = "30755-07.htm";
						st.takeItems(FILLED_CRYSTAL_ANTHARAS, -1);
						st.giveItems(JEWEL_OF_ANTHARAS, 1);
						st.playSound("ItemSound.quest_finish");
						st.setState(State.COMPLETED);
						st.exitQuest(QuestType.DAILY);
					}
					else
					{
						htmltext = "30755-06.htm";
					}
				}
			}
		}
		else if (id == State.COMPLETED)
		{
			if (npcId == THEODRIC)
			{
				if (st.isNowAvailable())
				{
					if (player.getLevel() < 84)
					{
						htmltext = "30755-00.htm";
					}
					else if (st.getQuestItemsCount(PORTAL_STONE) < 1)
					{
						htmltext = "30755-00a.htm";
					}
					else
					{
						htmltext = "30755-01.htm";
					}
				}
				else
				{
					htmltext = "30755-09.htm";
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
		
		if ((cond == 1) && (npcId == ULTIMATE_ANTHARAS))
		{
			st.takeItems(CLEAR_CRYSTAL, -1);
			st.giveItems(FILLED_CRYSTAL_ANTHARAS, 1);
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
					if (npcId == ULTIMATE_ANTHARAS)
					{
						st.takeItems(CLEAR_CRYSTAL, -1);
						st.giveItems(FILLED_CRYSTAL_ANTHARAS, 1);
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
		new _10504_JewelOfAntharas(10504, qn, "");
	}
}