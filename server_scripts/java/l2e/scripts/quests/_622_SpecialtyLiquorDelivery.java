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

/**
 * Created by LordWinter 13.01.2013 Based on L2J Eternity-World
 */
public class _622_SpecialtyLiquorDelivery extends Quest
{
	private final static String qn = "_622_SpecialtyLiquorDelivery";
	
	private static final int DRINK = 7197;
	private static final int FEE = 7198;
	
	private static final int JEREMY = 31521;
	private static final int PULIN = 31543;
	private static final int NAFF = 31544;
	private static final int CROCUS = 31545;
	private static final int KUBER = 31546;
	private static final int BEOLIN = 31547;
	private static final int LIETTA = 31267;
	
	private static final int ADENA = 57;
	private static final int HASTE_POT = 1062;
	private static final int[] RECIPES =
	{
		6847,
		6849,
		6851
	};
	
	public _622_SpecialtyLiquorDelivery(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(JEREMY);
		addTalkId(JEREMY, PULIN, NAFF, CROCUS, KUBER, BEOLIN, LIETTA);

		questItemIds = new int[]
		{
			DRINK,
			FEE
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31521-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.giveItems(DRINK, 5);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31547-02.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
			st.takeItems(DRINK, 1);
			st.giveItems(FEE, 1);
		}
		else if (event.equalsIgnoreCase("31546-02.htm"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
			st.takeItems(DRINK, 1);
			st.giveItems(FEE, 1);
		}
		else if (event.equalsIgnoreCase("31545-02.htm"))
		{
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
			st.takeItems(DRINK, 1);
			st.giveItems(FEE, 1);
		}
		else if (event.equalsIgnoreCase("31544-02.htm"))
		{
			st.set("cond", "5");
			st.playSound("ItemSound.quest_middle");
			st.takeItems(DRINK, 1);
			st.giveItems(FEE, 1);
		}
		else if (event.equalsIgnoreCase("31543-02.htm"))
		{
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
			st.takeItems(DRINK, 1);
			st.giveItems(FEE, 1);
		}
		else if (event.equalsIgnoreCase("31521-06.htm"))
		{
			st.set("cond", "7");
			st.playSound("ItemSound.quest_middle");
			st.takeItems(FEE, 5);
		}
		else if (event.equalsIgnoreCase("31267-02.htm"))
		{
			if (getRandom(5) < 1)
				st.giveItems(RECIPES[getRandom(RECIPES.length)], 1);
			else
			{
				st.rewardItems(ADENA, 18800);
				st.rewardItems(HASTE_POT, 1);
			}
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
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
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 68)
					htmltext = "31521-01.htm";
				else
				{
					htmltext = "31521-03.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getId())
				{
					case JEREMY:
						if (cond >= 1 && cond <= 5)
							htmltext = "31521-04.htm";
						else if (cond == 6)
							htmltext = "31521-05.htm";
						else if (cond == 7)
							htmltext = "31521-06.htm";
						break;
					case BEOLIN:
						if (cond == 1 && st.getQuestItemsCount(DRINK) == 5)
							htmltext = "31547-01.htm";
						else if (cond >= 2)
							htmltext = "31547-03.htm";
						break;
					case KUBER:
						if (cond == 2 && st.getQuestItemsCount(DRINK) == 4)
							htmltext = "31546-01.htm";
						else if (cond >= 3)
							htmltext = "31546-03.htm";
						break;
					case CROCUS:
						if (cond == 3 && st.getQuestItemsCount(DRINK) == 3)
							htmltext = "31545-01.htm";
						else if (cond >= 4)
							htmltext = "31545-03.htm";
						break;
					case NAFF:
						if (cond == 4 && st.getQuestItemsCount(DRINK) == 2)
							htmltext = "31544-01.htm";
						else if (cond >= 5)
							htmltext = "31544-03.htm";
						break;
					case PULIN:
						if (cond == 5 && st.getQuestItemsCount(DRINK) == 1)
							htmltext = "31543-01.htm";
						else if (cond >= 6)
							htmltext = "31543-03.htm";
						break;
					case LIETTA:
						if (cond == 7)
							htmltext = "31267-01.htm";
						break;
				}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new _622_SpecialtyLiquorDelivery(622, qn, "");
	}
}