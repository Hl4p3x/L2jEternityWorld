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
import l2e.util.Rnd;

/**
 * Created by LordWinter 19.01.2013 Based on L2J Eternity-World
 */
public class _381_LetsBecomeARoyalMember extends Quest
{
	private static final String qn = "_381_LetsBecomeARoyalMember";

	private static int SORINT = 30232;
	private static int SANDRA = 30090;

	private static int KAILS_COIN = 5899;
	private static int COIN_ALBUM = 5900;
	private static int MEMBERSHIP_1 = 3813;
	private static int CLOVER_COIN = 7569;
	private static int MEMBERSHIP = 5898;

	private static int GARGOYLE = 21018;
	private static int VEGUS = 27316;

	public _381_LetsBecomeARoyalMember(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(SORINT);
		addTalkId(SORINT);
		addTalkId(SANDRA);

		addKillId(GARGOYLE);
		addKillId(VEGUS);

		questItemIds = new int[]
		{	
			KAILS_COIN,
			COIN_ALBUM,
			CLOVER_COIN
		};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("30232-02.htm"))
		{
			if(player.getLevel() >= 55 && st.getQuestItemsCount(MEMBERSHIP_1) > 0)
			{
            			st.set("cond","1");
            			st.setState(State.STARTED);
            			st.playSound("ItemSound.quest_accept");
				htmltext = "30232-03.htm";
			}
			else
			{
				st.exitQuest(true);
			}
		}
		else if(event.equalsIgnoreCase("30090-02.htm"))
		{
			if(st.getInt("cond") == 1)
			{
				st.set("id", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		int cond = st.getInt("cond");
		int npcId = npc.getId();
		long album = st.getQuestItemsCount(COIN_ALBUM);

		switch (st.getState())
		{
			case State.CREATED:
				if(npcId == SORINT)
				{
					htmltext = "30232-01.htm";
				}
				break;
			case State.STARTED:
				if(npcId == SORINT)
				{
					if(cond == 1)
					{
						long coin = st.getQuestItemsCount(KAILS_COIN);
						if(coin > 0 && album > 0)
						{
							st.takeItems(KAILS_COIN, -1);
							st.takeItems(COIN_ALBUM, -1);
							st.giveItems(MEMBERSHIP, 1);
							st.playSound("ItemSound.quest_finish");
							st.exitQuest(true);
							htmltext = "30232-06.htm";
						}
						else if(album == 0)
						{
							htmltext = "30232-05.htm";
						}
						else if(coin == 0)
						{
							htmltext = "30232-04.htm";
						}
					}
				}
				else if(npcId == SANDRA)
				{
					long clover = st.getQuestItemsCount(CLOVER_COIN);
					if(album > 0)
					{
						htmltext = "30090-05.htm";
					}
					else if(clover > 0)
					{
						st.takeItems(CLOVER_COIN, -1);
						st.giveItems(COIN_ALBUM, 1);
						st.playSound("ItemSound.quest_itemget");
						htmltext = "30090-04.htm";
					}
					else if(st.getInt("id") == 0)
					{
						htmltext = "30090-01.htm";
					}
					else
					{
						htmltext = "30090-03.htm";
					}
				}
				break;
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null || !st.isStarted())
		{
			return null;
		}

		int npcId = npc.getId();

		long album = st.getQuestItemsCount(COIN_ALBUM);
		long coin = st.getQuestItemsCount(KAILS_COIN);
		long clover = st.getQuestItemsCount(CLOVER_COIN);

		if(npcId == GARGOYLE && coin == 0)
		{
			if(Rnd.chance(5 * Config.RATE_QUEST_DROP))
			{
				st.giveItems(KAILS_COIN, 1);
				if(album > 0 || clover > 0)
				{
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if(npcId == VEGUS && clover + album == 0 && st.getInt("id") != 0)
		{
			if(Rnd.chance(100 * Config.RATE_QUEST_DROP))
			{
				st.giveItems(CLOVER_COIN, 1);
				if(coin > 0)
				{
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _381_LetsBecomeARoyalMember(381, qn, "");
	}
}