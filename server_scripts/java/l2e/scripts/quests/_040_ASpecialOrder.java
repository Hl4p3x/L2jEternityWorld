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
 * Created by LordWinter 06.08.2011
 * Based on L2J Eternity-World
 */
public class _040_ASpecialOrder extends Quest
{
	private static final String qn = "_040_ASpecialOrder";

	// NPC's
	static final int HELVETIA 		= 30081;
	static final int OFULLE 		= 31572;
	static final int GESTO 			= 30511;

	// Items
	static final int OrangeNimbleFish 	= 6450;
	static final int OrangeUglyFish 	= 6451;
	static final int OrangeFatFish 		= 6452;
	static final int FishChest 		= 12764;
	static final int GoldenCobol 		= 5079;
	static final int ThornCobol 		= 5082;
	static final int GreatCobol 		= 5084;
	static final int SeedJar 		= 12765;
	static final int WondrousCubic 		= 10632;

	public _040_ASpecialOrder(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(HELVETIA);
		addTalkId(HELVETIA);
		addTalkId(OFULLE);
		addTalkId(GESTO);

		questItemIds = new int[] { FishChest, SeedJar };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("30081-02.htm"))
		{
			st.set("cond", "1");
			int condition = getRandom(1, 2);
			if (condition == 1)
			{
				st.set("cond", "2");
				htmltext = "30081-02a.htm";
			}
			else
			{
				st.set("cond", "5");
				htmltext = "30081-02b.htm";
			}
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30511-03.htm"))
		{
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31572-03.htm"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30081-05a.htm"))
		{
			st.takeItems(FishChest, 1);
			st.giveItems(WondrousCubic, 1);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		else if (event.equalsIgnoreCase("30081-05b.htm"))
		{
			st.takeItems(SeedJar, 1);
			st.giveItems(WondrousCubic, 1);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
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

		int npcId = npc.getId();
		int cond = st.getInt("cond");

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		else if (npcId == HELVETIA)
		{
			if (cond == 0)
			{
				if (player.getLevel() >= 40)
					htmltext = "30081-01.htm";
				else
				{
					htmltext = "30081-00.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 2 || cond == 3)
				htmltext = "30081-03a.htm";
			else if (cond == 4)
				htmltext = "30081-04a.htm";
			else if (cond == 5 || cond == 6)
				htmltext = "30081-03b.htm";
			else if (cond == 7)
				htmltext = "30081-04b.htm";
		}
		else if (npcId == OFULLE)
		{
			if (cond == 2)
				htmltext = "31572-01.htm";
			else if (cond == 3)
			{
				if (st.getQuestItemsCount(OrangeNimbleFish) >= 10 && st.getQuestItemsCount(OrangeUglyFish) >= 10 && st.getQuestItemsCount(OrangeFatFish) >= 10)
				{
					st.set("cond", "4");
					st.takeItems(OrangeNimbleFish, 10);
					st.takeItems(OrangeUglyFish, 10);
					st.takeItems(OrangeFatFish, 10);
					st.playSound("ItemSound.quest_middle");
					st.giveItems(FishChest, 1);
					htmltext = "31572-05.htm";
				}
				else
					htmltext = "31572-04.htm";
			}
			else if (cond == 4)
				htmltext = "31572-06.htm";
		}
		else if (npcId == GESTO)
		{
			if (cond == 5)
				htmltext = "30511-01.htm";
			else if (cond == 6)
			{
				if (st.getQuestItemsCount(GoldenCobol) >= 40 && st.getQuestItemsCount(ThornCobol) >= 40 && st.getQuestItemsCount(GreatCobol) >= 40)
				{
					st.set("cond", "7");
					st.takeItems(GoldenCobol, 40);
					st.takeItems(ThornCobol, 40);
					st.takeItems(GreatCobol, 40);
					st.playSound("ItemSound.quest_middle");
					st.giveItems(SeedJar, 1);
					htmltext = "30511-05.htm";
				}
				else
					htmltext = "30511-04.htm";
			}
			else if (cond == 7)
				htmltext = "30511-06.htm";
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _040_ASpecialOrder(40, qn, "");
	}
}