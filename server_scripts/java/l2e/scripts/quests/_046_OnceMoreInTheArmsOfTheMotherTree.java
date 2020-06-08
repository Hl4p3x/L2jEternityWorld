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
public class _046_OnceMoreInTheArmsOfTheMotherTree extends Quest
{
	private static final String qn = "_046_OnceMoreInTheArmsOfTheMotherTree";

	// NPC's
	private static final int JEWELER_SANDRA_ID               = 30090;
	private static final int MAGIC_TRADER_GENTLER_ID         = 30094;
	private static final int TRADER_GALLADUCCI_ID            = 30097;
	private static final int PRIEST_DUSTIN_ID                = 30116;

	// ITEMS
	private static final int GALLADUCCIS_ORDER_DOCUMENT_ID_1 = 7563;
	private static final int GALLADUCCIS_ORDER_DOCUMENT_ID_2 = 7564;
	private static final int GALLADUCCIS_ORDER_DOCUMENT_ID_3 = 7565;
	private static final int MAGIC_SWORD_HILT_ID 		 = 7568;
	private static final int GEMSTONE_POWDER_ID 		 = 7567;
	private static final int PURIFIED_MAGIC_NECKLACE_ID 	 = 7566;
	private static final int MARK_OF_TRAVELER_ID 		 = 7570;
	private static final int SCROLL_OF_ESCAPE_SPECIAL 	 = 7555;

	// RACE
	private static final int RACE = 1;

	public _046_OnceMoreInTheArmsOfTheMotherTree(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(TRADER_GALLADUCCI_ID);
		addTalkId(TRADER_GALLADUCCI_ID);
		addTalkId(MAGIC_TRADER_GENTLER_ID);
		addTalkId(JEWELER_SANDRA_ID);
		addTalkId(PRIEST_DUSTIN_ID);

		questItemIds = new int[]
		{
			GALLADUCCIS_ORDER_DOCUMENT_ID_1,
			GALLADUCCIS_ORDER_DOCUMENT_ID_2,
			GALLADUCCIS_ORDER_DOCUMENT_ID_3,
			MAGIC_SWORD_HILT_ID,
			GEMSTONE_POWDER_ID,
			PURIFIED_MAGIC_NECKLACE_ID 
		};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("1"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(GALLADUCCIS_ORDER_DOCUMENT_ID_1, 1);
			htmltext = "30097-03.htm";
		}
		else if(event.equalsIgnoreCase("2"))
		{
			st.set("cond", "2");
			st.takeItems(GALLADUCCIS_ORDER_DOCUMENT_ID_1, 1);
			st.giveItems(MAGIC_SWORD_HILT_ID, 1);
			st.playSound("ItemSound.quest_accept");
			htmltext = "30094-02.htm";
		}
		else if(event.equalsIgnoreCase("3"))
		{
			st.set("cond", "3");
			st.takeItems(MAGIC_SWORD_HILT_ID, 1);
			st.giveItems(GALLADUCCIS_ORDER_DOCUMENT_ID_2, 1);
			st.playSound("ItemSound.quest_accept");
			htmltext = "30097-06.htm";
		}
		else if(event.equalsIgnoreCase("4"))
		{
			st.set("cond", "4");
			st.takeItems(GALLADUCCIS_ORDER_DOCUMENT_ID_2, 1);
			st.giveItems(GEMSTONE_POWDER_ID, 1);
			st.playSound("ItemSound.quest_accept");
			htmltext = "30090-02.htm";
		}
		else if(event.equalsIgnoreCase("5"))
		{
			st.set("cond", "5");
			st.takeItems(GEMSTONE_POWDER_ID, 1);
			st.giveItems(GALLADUCCIS_ORDER_DOCUMENT_ID_3, 1);
			st.playSound("ItemSound.quest_accept");
			htmltext = "30097-09.htm";
		}
		else if(event.equalsIgnoreCase("6"))
		{
			st.set("cond", "6");
			st.takeItems(GALLADUCCIS_ORDER_DOCUMENT_ID_3, 1);
			st.giveItems(PURIFIED_MAGIC_NECKLACE_ID, 1);
			st.playSound("ItemSound.quest_accept");
			htmltext = "30116-02.htm";
		}
		else if(event.equalsIgnoreCase("7"))
		{
			st.giveItems(SCROLL_OF_ESCAPE_SPECIAL, 1);
			st.takeItems(PURIFIED_MAGIC_NECKLACE_ID, 1);
			st.takeItems(MARK_OF_TRAVELER_ID,-1);
			htmltext = "30097-12.htm";
			st.unset("cond");
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
		byte id = st.getState();

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		else if(id == State.CREATED)
		{
			if(npcId == TRADER_GALLADUCCI_ID & st.getInt("cond") == 0)
			{
				if(player.getRace().ordinal() == RACE && st.getQuestItemsCount(MARK_OF_TRAVELER_ID) > 0)
					htmltext = "30097-02.htm";
				else
				{
					htmltext = "30097-01.htm";
					st.exitQuest(true);
				}
			}
		}
		else if(id == State.STARTED)
		{
			if(npcId == TRADER_GALLADUCCI_ID && st.getInt("cond") == 1)
				htmltext = "30097-04.htm";
			else if(npcId == TRADER_GALLADUCCI_ID && st.getInt("cond") == 2)
				htmltext = "30097-05.htm";
			else if(npcId == TRADER_GALLADUCCI_ID && st.getInt("cond") == 3)
				htmltext = "30097-07.htm";
			else if(npcId == TRADER_GALLADUCCI_ID && st.getInt("cond") == 4)
				htmltext = "30097-08.htm";
			else if(npcId == TRADER_GALLADUCCI_ID && st.getInt("cond") == 5)
				htmltext = "30097-10.htm";
			else if(npcId == TRADER_GALLADUCCI_ID && st.getInt("cond") == 6)
				htmltext = "30097-11.htm";
			else if(npcId == MAGIC_TRADER_GENTLER_ID && st.getInt("cond") == 1)
				htmltext = "30094-01.htm";
			else if(npcId == MAGIC_TRADER_GENTLER_ID && st.getInt("cond") == 2)
				htmltext = "30094-03.htm";
			else if(npcId == JEWELER_SANDRA_ID && st.getInt("cond") == 3)
				htmltext = "30090-01.htm";
			else if(npcId == JEWELER_SANDRA_ID && st.getInt("cond") == 4)
				htmltext = "30090-03.htm";
			else if(npcId == PRIEST_DUSTIN_ID && st.getInt("cond") == 5)
				htmltext = "30116-01.htm";
			else if(npcId == PRIEST_DUSTIN_ID && st.getInt("cond") == 6)
				htmltext = "30116-03.htm";
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _046_OnceMoreInTheArmsOfTheMotherTree(46, qn, "");    	
	}
}