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
 * Created by LordWinter 06.07.2012
 * Based on L2J Eternity-World
 */
public class _264_KeenClaws extends Quest
{
	private final static String qn = "_264_KeenClaws";
	
	// Item
	private static final int WOLF_CLAW = 1367;
	
	// NPC
	private static final int PAYNE = 30136;
	
	// Mobs
	private static final int GOBLIN = 20003;
	private static final int WOLF = 20456;
	
	// Rewards
	private static final int LeatherSandals = 36;
	private static final int WoodenHelmet = 43;
	private static final int Stockings = 462;
	private static final int HealingPotion = 1061;
	private static final int ShortGloves = 48;
	private static final int ClothShoes = 35;
		
	public _264_KeenClaws(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(PAYNE);
		addTalkId(PAYNE);
		
		addKillId(GOBLIN);
		addKillId(WOLF);

		questItemIds = new int[] { WOLF_CLAW };
	}	
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30136-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if(st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 3 && player.getLevel() <= 9)
					htmltext = "30136-02.htm";
				else
				{
					htmltext = "30136-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				int count = (int) st.getQuestItemsCount(WOLF_CLAW);
				
				if (count < 50)
					htmltext = "30136-04.htm";
				else
				{
					st.takeItems(WOLF_CLAW, -1);
					
					int n = st.getRandom(17);
					if (n == 0)
						st.giveItems(WoodenHelmet, 1);
					else if (n < 2)
						st.giveItems(57, 1000);
					else if (n < 5)
						st.giveItems(LeatherSandals, 1);
					else if (n < 8)
					{
						st.giveItems(Stockings, 1);
						st.giveItems(57, 50);
					}
					else if (n < 11)
						st.giveItems(HealingPotion, 1);
					else if (n < 14)
						st.giveItems(ShortGloves, 1);
					else
						st.giveItems(ClothShoes, 1);
					
					htmltext = "30136-05.htm";
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(true);
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;
		
		if (st.getInt("cond") == 1 && st.getRandom(10) < 8)
		{
			int qty = st.getRandom(8) + 1;
			int count = (int) st.getQuestItemsCount(WOLF_CLAW);
			
			if (count + qty > 50)
				qty = 50 - count;
			
			st.giveItems(WOLF_CLAW, qty);
			if (st.getQuestItemsCount(WOLF_CLAW) == 50)
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _264_KeenClaws(264, qn, "");		
	}
}