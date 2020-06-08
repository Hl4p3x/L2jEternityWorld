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
import l2e.gameserver.model.quest.State;
import l2e.util.Rnd;

/**
 * Created by LordWinter 18.01.2013 Based on L2J Eternity-World
 */
public class _280_TheFoodChain extends Quest
{
	private static final String qn = "_280_TheFoodChain";

	private static int BIXON = 32175;

	private static int YOUNG_GREY_KELTIR = 22229;
	private static int GREY_KELTIR = 22230;
	private static int DOMINANT_GREY_KELTIR = 22231;
	private static int BLACK_WOLF = 22232;
	private static int DOMINANT_BLACK_WOLF = 22233;

	private static int[] REWARDS = 
	{
			28,
			35,
			116
	};

	private static int KELTIR_TOOTH = 9809;
	private static int WOLF_TOOTH = 9810;

	public _280_TheFoodChain(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(BIXON);
		addTalkId(BIXON);

		addKillId(YOUNG_GREY_KELTIR);
		addKillId(GREY_KELTIR);
		addKillId(DOMINANT_GREY_KELTIR);
		addKillId(BLACK_WOLF);
		addKillId(DOMINANT_BLACK_WOLF);

		questItemIds = new int[]
		{
			KELTIR_TOOTH,
			WOLF_TOOTH
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

		long KELTIR_TOOTH_COUNT = st.getQuestItemsCount(KELTIR_TOOTH);
		long WOLF_TOOTH_COUNT = st.getQuestItemsCount(WOLF_TOOTH);

		if(event.equalsIgnoreCase("32175-03.htm"))
		{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("ADENA"))
		{
			st.takeItems(KELTIR_TOOTH, -1);
			st.takeItems(WOLF_TOOTH, -1);
			st.giveItems(57, (KELTIR_TOOTH_COUNT + WOLF_TOOTH_COUNT) * 2);
			htmltext = "32175-06.htm";
		}
		else if(event.equalsIgnoreCase("ITEM"))
		{
       			if ((KELTIR_TOOTH_COUNT + WOLF_TOOTH_COUNT) < 25)
			{
         			htmltext = "32175-09.htm";
			}
       			else
			{
         			htmltext = "32175-06.htm";
         			if (KELTIR_TOOTH_COUNT > 25)
				{
           				st.giveItems(REWARDS[getRandom(REWARDS.length)], 1);
           				st.takeItems(KELTIR_TOOTH, 25);
				}
         			else
				{
           				st.giveItems(REWARDS[getRandom(REWARDS.length)], 1);
           				st.takeItems(KELTIR_TOOTH, KELTIR_TOOTH_COUNT);
           				st.takeItems(WOLF_TOOTH, 25 - KELTIR_TOOTH_COUNT);
				}
			}
		}
		else if(event.equalsIgnoreCase("32175-08.htm"))
		{
       			st.takeItems(KELTIR_TOOTH, -1);
       			st.takeItems(WOLF_TOOTH, -1);
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
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

		switch (st.getState())
		{
			case State.CREATED:
				if(player.getLevel() >= 3)
				{
					htmltext = "32175-01.htm";
				}
				else
				{
					htmltext = "32175-02.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				htmltext = st.getQuestItemsCount(KELTIR_TOOTH) > 0 || st.getQuestItemsCount(WOLF_TOOTH) > 0 ? "32175-05.htm" : "32175-04.htm";
				break;
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}

		int npcId = npc.getId();

		if (st.getInt("cond") == 1)
		{
			if((npcId == YOUNG_GREY_KELTIR || npcId == GREY_KELTIR || npcId == DOMINANT_GREY_KELTIR) && Rnd.chance(95))
			{
				st.giveItems(KELTIR_TOOTH, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if((npcId == BLACK_WOLF || npcId == DOMINANT_BLACK_WOLF) && Rnd.chance(75))
			{
				st.giveItems(WOLF_TOOTH, 3);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _280_TheFoodChain(280, qn, "");
	}
}