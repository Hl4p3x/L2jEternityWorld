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
 * Created by LordWinter 21.03.2011
 * Based on L2J Eternity-World
 */
public class _045_ToTalkingIsland extends Quest
{
	private static final String qn = "_045_ToTalkingIsland";

	protected static final int GALLADUCCIS_ORDER_DOCUMENT_ID_1 = 7563;
	protected static final int GALLADUCCIS_ORDER_DOCUMENT_ID_2 = 7564;
	protected static final int GALLADUCCIS_ORDER_DOCUMENT_ID_3 = 7565;
	protected static final int MAGIC_SWORD_HILT_ID = 7568;
	protected static final int GEMSTONE_POWDER_ID = 7567;
	protected static final int PURIFIED_MAGIC_NECKLACE_ID = 7566;
	protected static final int MARK_OF_TRAVELER_ID = 7570;
	protected static final int SCROLL_OF_ESCAPE_SPECIAL = 7554;	

	// NPC
	protected static final int GALLADUCCI = 30097;
	protected static final int GENTLER = 30094;
	protected static final int SANDRA = 30090;
	protected static final int DUSTIN = 30116;

	protected static final int RACE = 0;

	public _045_ToTalkingIsland(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(GALLADUCCI);
		addTalkId(GALLADUCCI);
		addTalkId(GENTLER);
		addTalkId(SANDRA);
		addTalkId(DUSTIN);
		questItemIds = new int[] {GALLADUCCIS_ORDER_DOCUMENT_ID_1, GALLADUCCIS_ORDER_DOCUMENT_ID_2,
				GALLADUCCIS_ORDER_DOCUMENT_ID_3, MAGIC_SWORD_HILT_ID,GEMSTONE_POWDER_ID,
				PURIFIED_MAGIC_NECKLACE_ID };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("1"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(GALLADUCCIS_ORDER_DOCUMENT_ID_1, 1);
			htmltext = "30097-03.htm";
		}
		else if (event.equalsIgnoreCase("2"))
		{
			st.set("cond", "2");
			st.takeItems(GALLADUCCIS_ORDER_DOCUMENT_ID_1, 1);
			st.giveItems(MAGIC_SWORD_HILT_ID, 1);
			htmltext = "30094-02.htm";
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("3"))
		{
			st.set("cond", "3");
			st.takeItems(MAGIC_SWORD_HILT_ID, 1);
			st.giveItems(GALLADUCCIS_ORDER_DOCUMENT_ID_2, 1);
			htmltext = "30097-06.htm";
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("4"))
		{
			st.set("cond", "4");
			st.takeItems(GALLADUCCIS_ORDER_DOCUMENT_ID_2, 1);
			st.giveItems(GEMSTONE_POWDER_ID, 1);
			htmltext = "30090-02.htm";
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("5"))
		{
			st.set("cond", "5");
			st.takeItems(GEMSTONE_POWDER_ID, 1);
			st.giveItems(GALLADUCCIS_ORDER_DOCUMENT_ID_3, 1);
			htmltext = "30097-09.htm";
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("6"))
		{
			st.set("cond", "6");
			st.takeItems(GALLADUCCIS_ORDER_DOCUMENT_ID_3, 1);
			st.giveItems(PURIFIED_MAGIC_NECKLACE_ID, 1);
			htmltext = "30116-02.htm";
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("7"))
		{
			st.giveItems(SCROLL_OF_ESCAPE_SPECIAL, 1);
			st.takeItems(PURIFIED_MAGIC_NECKLACE_ID, 1);
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
		
		final int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.COMPLETED :
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED :
				if (player.getRace().ordinal() == RACE && st.getQuestItemsCount(MARK_OF_TRAVELER_ID) > 0)
					htmltext = "30097-02.htm";
				else
				{
					htmltext = "30097-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED :
				switch (npc.getId())
				{
					case GALLADUCCI:
						switch (cond)
						{
							case 1:
								htmltext = "30097-04.htm";
								break;
							case 2:
								htmltext = "30097-05.htm";
								break;
							case 3:
								htmltext = "30097-07.htm";
								break;
							case 4:
								htmltext = "30097-08.htm";
								break;
							case 5:
								htmltext = "30097-10.htm";
								break;
							case 6:
								htmltext = "30097-11.htm";
								break;
						}
						break;
					case GENTLER:
						switch (cond)
						{
							case 1:
								htmltext = "30094-01.htm";
								break;
							case 2:
								htmltext = "30094-03.htm";
								break;
						}
						break;
					case SANDRA:
						switch (cond)
						{
							case 3:
								htmltext = "30090-01.htm";
								break;
							case 4:
								htmltext = "30090-03.htm";
								break;
						}
						break;
					case DUSTIN:
						switch (cond)
						{
							case 5:
								htmltext = "30116-01.htm";
								break;
							case 6:
								htmltext = "30116-03.htm";
								break;
						}
						break;
				}
				break;
		}
		
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _045_ToTalkingIsland(45, qn, "");    	
	}
}
