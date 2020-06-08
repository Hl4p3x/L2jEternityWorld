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

import l2e.Config;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 18.06.2012 Based on L2J Eternity-World
 */
public final class _648_AnIceMerchantsDream extends Quest
{
	private static final String qn = "_648_AnIceMerchantsDream";
	
	private static final int RAFFORTY = 32020;
	private static final int ICE_SHELF = 32023;
	private static final int HEMOCYTE = 8057;
	private static final int SILVER_ICE = 8077;
	private static final int BLACK_ICE = 8078;
	
	public _648_AnIceMerchantsDream(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(RAFFORTY);
		addStartNpc(ICE_SHELF);
		addTalkId(RAFFORTY);
		addTalkId(ICE_SHELF);
		
		for (int i = 22080; i <= 22098; i++)
		{
			if (i != 22095)
			{
				addKillId(i);
			}
		}
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
		
		if (event.equalsIgnoreCase("32020-02.htm"))
		{
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.set("cond", "1");
		}
		else if (event.equalsIgnoreCase("32020-07.htm"))
		{
			int silver = (int) st.getQuestItemsCount(SILVER_ICE);
			int black = (int) st.getQuestItemsCount(BLACK_ICE);
			int r1 = silver * 300;
			int r2 = black * 1200;
			int reward = r1 + r2;
			st.rewardItems(57, reward);
			st.takeItems(SILVER_ICE, silver);
			st.takeItems(BLACK_ICE, black);
		}
		else if (event.equalsIgnoreCase("32020-09.htm"))
		{
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		else if (event.equalsIgnoreCase("32023-04.htm"))
		{
			st.playSound("ItemSound2.broken_key");
			st.takeItems(SILVER_ICE, 1L);
		}
		else if (event.equalsIgnoreCase("32023-05.htm"))
		{
			if (st.getRandom(100) <= 25)
			{
				st.giveItems(BLACK_ICE, 1L);
				st.playSound("ItemSound3.sys_enchant_sucess");
			}
			else
			{
				htmltext = "32023-06.htm";
				st.playSound("ItemSound3.sys_enchant_failed");
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
		{
			return htmltext;
		}
		
		int cond = st.getInt("cond");
		int npcId = npc.getId();
		int silver = (int) st.getQuestItemsCount(SILVER_ICE);
		int black = (int) st.getQuestItemsCount(BLACK_ICE);
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 53)
				{
					htmltext = "32020-01.htm";
				}
				else
				{
					htmltext = "32020-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (npcId == RAFFORTY)
				{
					if (cond == 1)
					{
						if ((silver > 0) || (black > 0))
						{
							QuestState st2 = player.getQuestState("_115_TheOtherSideOfTruth");
							htmltext = "32020-05.htm";
							if (st2 != null && st2.isCompleted())
							{
								htmltext = "32020-10.htm";
								st.playSound("ItemSound.quest_middle");
								st.set("cond", "2");
							}
						}
						else
						{
							htmltext = "32020-04.htm";
						}
					}
					else if (cond == 2)
					{
						if ((silver > 0) || (black > 0))
						{
							htmltext = "32020-10.htm";
						}
						else
						{
							htmltext = "32020-04a.htm";
						}
					}
				}
				else if (npcId == ICE_SHELF)
				{
					if (st.getState() == 0)
					{
						htmltext = "32023-00.htm";
					}
					else if (silver > 0)
					{
						htmltext = "32023-02.htm";
					}
					else
					{
						htmltext = "32023-01.htm";
					}
				}
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		L2PcInstance partyMember = getRandomPartyMemberState(player, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}
		
		QuestState stp = partyMember.getQuestState(qn);
		int cond = st.getInt("cond");
		
		if (stp != null)
		{
			int chance = (int) (60.0F * Config.RATE_QUEST_DROP);
			int numItems = chance / 100;
			chance %= 100;
			if (st.getRandom(100) < chance)
			{
				numItems++;
			}
			if (numItems > 0)
			{
				st.playSound("ItemSound.quest_itemget");
				st.giveItems(SILVER_ICE, numItems);
			}
		}
		
		int random = st.getRandom(100);
		if ((cond == 2) && (random <= 10))
		{
			st.giveItems(HEMOCYTE, 1L);
		}
		st.playSound("ItemSound.quest_itemget");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new _648_AnIceMerchantsDream(648, qn, "");
	}
}