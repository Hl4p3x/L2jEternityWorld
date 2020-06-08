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
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public class _612_WarWithKetraOrcs extends Quest
{
	private static final String qn = "_612_WarWithKetraOrcs";
		
	// NPC
	private final int ASHAS = 31377;
	
	// Mobs
	private final int[] VARKA_MOBS = { 21350, 21351, 21353, 21354, 21355, 21357, 21358, 21360, 21361, 21362, 21369, 21370, 21364, 21365, 21366, 21368, 21371, 21372, 21373, 21374, 21375 };
	private final int[] KETRA_ORCS = { 21324, 21327, 21328, 21329, 21331, 21332, 21334, 21336, 21338, 21339, 21340, 21342, 21343, 21345, 21347 };
	
	private final int[][] CHANCE = { { 21324, 500 }, { 21327, 510 }, { 21328, 522 }, { 21329, 519 }, { 21331, 529 }, { 21332, 664 }, { 21334, 539 }, { 21336, 529 }, { 21338, 558 }, { 21339, 568 }, { 21340, 568 }, { 21342, 578 }, { 21343, 548 }, { 21345, 713 }, { 21347, 738 } };
	
	// Items
	private final int SEED = 7187;
	private final int MOLAR = 7234;
	
	public _612_WarWithKetraOrcs(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(ASHAS);
		addTalkId(ASHAS);
		
		for (int i = 0; i < KETRA_ORCS.length; i++)
		{
			addKillId(KETRA_ORCS[i]);
		}

		for (int i = 0; i < VARKA_MOBS.length; i++)
		{
			addKillId(VARKA_MOBS[i]);
		}
		
		questItemIds = new int[] { MOLAR };
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		long molars = st.getQuestItemsCount(MOLAR);

		if (event.equalsIgnoreCase("31377-03.htm"))
		{
			st.set("cond", "1");
			st.set("id", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31377-06.htm"))
		{
			htmltext = "31377-06.htm";
		}
		else if (event.equalsIgnoreCase("31377-07.htm"))
		{
			if (molars >= 100)
			{
				htmltext = "31377-07.htm";
				st.takeItems(MOLAR, 100);
				st.giveItems(SEED, 20);
			}
			else
			{
				htmltext = "31377-08.htm";
			}
		}
		else if (event.equalsIgnoreCase("31377-09.htm"))
		{
			htmltext = "31377-09.htm";
			st.unset("id");
			st.takeItems(MOLAR, -1);
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = (player.getLevel() >= 75) ? "31377-01.htm" : "31377-02.htm";
				break;
			case State.STARTED:
				htmltext = (st.hasQuestItems(MOLAR)) ? "31377-04.htm" : "31377-05.htm";
				break;
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMemberState(player, State.STARTED);
		if (partyMember == null)
		{
			return null;
		}

		final QuestState st = partyMember.getQuestState(qn);
		if (st == null)
			return null;

		int npcId = npc.getId();
		long count = st.getQuestItemsCount(MOLAR);
		final QuestState st2 = partyMember.getQuestState("_605_AllianceWithKetraOrcs");

		if (checkArray(KETRA_ORCS, npcId))
		{
			if (st2 == null)
			{
				int chance = (int) (Config.RATE_QUEST_DROP * getValue(CHANCE, npcId));
				int numItems = chance / 100;
				if (st.getRandom(1000) < chance)
				{
					numItems++;
				}
				if (numItems != 0)
				{
					if (((count + numItems) / 100) > (count / 100))
					{
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
					st.giveItems(MOLAR, numItems);
				}
			}
		}
		else if (checkArray(VARKA_MOBS, npcId))
		{
			st.unset("id");
			st.takeItems(MOLAR, -1);
			st.exitQuest(true);
		}
		return null;
	}
	
	private int getValue(int[][] array, int value)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i][0] == value)
			{
				return array[i][1];
			}
		}
		return 0;
	}
	
	private boolean checkArray(int[] array, int value)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == value)
			{
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		new _612_WarWithKetraOrcs(612, qn, "");		
	}	
}