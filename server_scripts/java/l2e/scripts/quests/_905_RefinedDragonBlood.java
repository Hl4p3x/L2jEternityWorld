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
import l2e.gameserver.util.Util;

public class _905_RefinedDragonBlood extends Quest
{
	private static final String qn = "_905_RefinedDragonBlood";

	private static final int[] SEPARATED_SOUL =
	{
		32864, 32865, 32866, 32867, 32868, 32869, 32870, 32891
	};

	private static final int[] BLUE =
	{ 
		22852, 22853, 22844, 22845, 22846, 22847
	};
	
	private static final int[] RED =
	{ 
		22848, 22849, 22850, 22851
	};
	
	private static final int UNREFINED_RED_DRAGON_BLOOD = 21913;
	private static final int UNREFINED_BLUE_DRAGON_BLOOD = 21914;
	
	private static final int REFINED_RED_DRAGON_BLOOD = 21903;
	private static final int REFINED_BLUE_DRAGON_BLOOD = 21904;

	private static final int DROP_CHANCE = 20;
	
	public _905_RefinedDragonBlood(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for(int npc : SEPARATED_SOUL)
		{
			addStartNpc(npc);
			addTalkId(npc);
		}

		for(int first_group : BLUE)
		{
			addKillId(first_group);
		}

		for(int second_group : RED)
		{
			addKillId(second_group);
		}

		questItemIds = new int[] { UNREFINED_BLUE_DRAGON_BLOOD, UNREFINED_RED_DRAGON_BLOOD };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;
		
		if (Util.contains(SEPARATED_SOUL, npc.getId()))
		{
			if (event.equalsIgnoreCase("accept"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
				htmltext = "RefinedDragonBlood-05.htm";
			}
			else if (event.equalsIgnoreCase("RefinedDragonBlood-12.htm"))
			{
				st.takeItems(UNREFINED_BLUE_DRAGON_BLOOD, -1);
				st.takeItems(UNREFINED_RED_DRAGON_BLOOD, -1);
				st.giveItems(REFINED_RED_DRAGON_BLOOD, 1);
				st.exitQuest(QuestType.DAILY);
				st.playSound("ItemSound.quest_finish");
			}
			else if (event.equalsIgnoreCase("RefinedDragonBlood-13.htm"))
			{
				st.takeItems(UNREFINED_BLUE_DRAGON_BLOOD, -1);
				st.takeItems(UNREFINED_RED_DRAGON_BLOOD, -1);
				st.giveItems(REFINED_BLUE_DRAGON_BLOOD, 1);
				st.exitQuest(QuestType.DAILY);
				st.playSound("ItemSound.quest_finish");
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
			return htmltext;
		
		if (Util.contains(SEPARATED_SOUL, npc.getId()))
		{
			switch(st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 83)
						htmltext = "RefinedDragonBlood-01.htm";
					else
						htmltext = "RefinedDragonBlood-03.htm";
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
						htmltext = "RefinedDragonBlood-06.htm";
					else if (st.getInt("cond") == 2)
						htmltext = "RefinedDragonBlood-08.htm";
					break;
				case State.COMPLETED:
					if (st.isNowAvailable())
					{
						if (player.getLevel() >= 83)
							htmltext = "RefinedDragonBlood-01.htm";
						else
							htmltext = "RefinedDragonBlood-03.htm";
					}
					else
						htmltext = "RefinedDragonBlood-02.htm";
					break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{	
		QuestState st = player.getQuestState(qn);
		if (st != null && player.isInsideRadius(npc, 2000, false, false))
		{
			if (Util.contains(BLUE, npc.getId()) && getRandom(100) < DROP_CHANCE && st.getQuestItemsCount(UNREFINED_BLUE_DRAGON_BLOOD) < 10)
			{
				st.giveItems(UNREFINED_BLUE_DRAGON_BLOOD, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if (Util.contains(RED, npc.getId()) && getRandom(100) < DROP_CHANCE && st.getQuestItemsCount(UNREFINED_RED_DRAGON_BLOOD) < 10)
			{
				st.giveItems(UNREFINED_RED_DRAGON_BLOOD, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		
		if (st != null && st.getQuestItemsCount(UNREFINED_BLUE_DRAGON_BLOOD) >= 10 && st.getQuestItemsCount(UNREFINED_RED_DRAGON_BLOOD) >= 10)
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String[] args)
	{
		new _905_RefinedDragonBlood(905, qn, "");
	}
}