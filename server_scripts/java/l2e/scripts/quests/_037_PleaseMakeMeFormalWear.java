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
 * Created by LordWinter 24.05.2011
 * Based on L2J Eternity-World
 */
public class _037_PleaseMakeMeFormalWear extends Quest
{
	private static final String qn = "_037_PleaseMakeMeFormalWear";

	// ITEMS
	private static final int BOX_OF_COOKIES 	= 7159;
	private static final int DRESS_SHOES_BOX 	= 7113;
	private static final int FORMAL_WEAR 		= 6408;
	private static final int ICE_WINE 		= 7160;
	private static final int JEWEL_BOX 		= 7077;
	private static final int MYSTERIOUS_CLOTH 	= 7076;
	private static final int SEWING_KIT 		= 7078;
	private static final int SIGNET_RING 		= 7164;

	public _037_PleaseMakeMeFormalWear(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(30842);
		addTalkId(30842);
		addTalkId(31520);
		addTalkId(31521);
		addTalkId(31627);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("30842-1.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31520-1.htm"))
		{
			st.giveItems(SIGNET_RING, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31521-1.htm"))
		{
			st.giveItems(ICE_WINE, 1);
			st.set("cond", "3");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31627-1.htm"))
		{
			if (st.getQuestItemsCount(ICE_WINE) > 0)
			{
				st.takeItems(ICE_WINE, 1);
				st.set("cond", "4");
				st.playSound("ItemSound.quest_accept");
			}
			else
				htmltext = "no_items.htm";
		}
		else if (event.equalsIgnoreCase("31521-3.htm"))
		{
			st.giveItems(BOX_OF_COOKIES, 1);
			st.set("cond", "5");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31520-3.htm"))
		{
			st.set("cond", "6");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31520-5.htm"))
		{
			if (st.getQuestItemsCount(MYSTERIOUS_CLOTH) > 0 && st.getQuestItemsCount(JEWEL_BOX) > 0 && st.getQuestItemsCount(SEWING_KIT) > 0)
			{
				st.takeItems(MYSTERIOUS_CLOTH, 1);
				st.takeItems(JEWEL_BOX, 1);
				st.takeItems(SEWING_KIT, 1);
				st.set("cond", "7");
				st.playSound("ItemSound.quest_accept");
			}
			else
				htmltext = "no_items.htm";
		}
		else if (event.equalsIgnoreCase("31520-7.htm"))
			if (st.getQuestItemsCount(DRESS_SHOES_BOX) > 0)
			{
				st.takeItems(DRESS_SHOES_BOX, 1);
				st.giveItems(FORMAL_WEAR, 1);
				st.unset("cond");
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else
				htmltext = "no_items.htm";
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);

		int npcId = npc.getId();
		int cond = st.getInt("cond");

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		if (npcId == 30842)
		{
			if (cond == 0)
			{
				if (player.getLevel() >= 60)
					htmltext = "30842-0.htm";
				else
				{
					htmltext = "30842-2.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
				htmltext = "30842-2a.htm";
		}
		else if (npcId == 31520)
		{
			if (cond == 1)
				htmltext = "31520-0.htm";
			else if (cond == 2 || cond == 3 || cond == 4 || cond == 5 )
			{
				if (st.getQuestItemsCount(BOX_OF_COOKIES) > 0)
				{
					st.takeItems(BOX_OF_COOKIES,1);
					htmltext = "31520-2.htm";
				}
				else
					htmltext = "31520-1a.htm";
			}
			else if (cond == 6)
			{
				if (st.getQuestItemsCount(MYSTERIOUS_CLOTH) > 0 && st.getQuestItemsCount(JEWEL_BOX) > 0 && st.getQuestItemsCount(SEWING_KIT) > 0)
					htmltext = "31520-4.htm";
				else
					htmltext = "31520-3a.htm";
			}
			else if (cond == 7)
				if (st.getQuestItemsCount(DRESS_SHOES_BOX) > 0)
					htmltext = "31520-6.htm";
				else
					htmltext = "31520-5a.htm";
		}
		else if (npcId == 31521)
		{
			if (st.getQuestItemsCount(SIGNET_RING) > 0)
			{
				st.takeItems(SIGNET_RING,1);
				htmltext = "31521-0.htm";
			}
			else if (cond == 3)
				htmltext = "31521-1a.htm";
			else if (cond == 4)
				htmltext = "31521-2.htm";
			else if (cond == 5)
				htmltext = "31521-3a.htm";
		}
		else if (npcId == 31627)
		{
			if (st.getQuestItemsCount(ICE_WINE) > 0)
				htmltext = "31627-0.htm";
			if (cond == 4)
				htmltext = "31627-1a.htm";
		}				
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _037_PleaseMakeMeFormalWear(37, qn, "");
	}
}