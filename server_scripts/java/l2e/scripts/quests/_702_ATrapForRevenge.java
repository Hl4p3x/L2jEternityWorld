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
 * Created by LordWinter 16.05.2011
 * Based on L2J Eternity-World
 */
public class _702_ATrapForRevenge extends Quest
{
	private static final String qn = "_702_ATrapForRevenge";

	// NPC
	private static final int Plenos = 32563;
	private static final int Lekon = 32557;
	private static final int Tenius = 32555;
	private static final int[] Monsters = { 22612, 22613, 25632, 22610, 22611, 25631, 25626 };

	// Items
	private static final int DrakeFlesh = 13877;
	private static final int RottenBlood = 13878;
	private static final int BaitForDrakes = 13879;
	private static final int VariantDrakeWingHorns = 13880;
	private static final int ExtractedRedStarStone = 14009;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return getNoQuestMsg(player);
		
		if (event.equalsIgnoreCase("32563-04.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32563-07.htm"))
		{
			if (st.hasQuestItems(DrakeFlesh))
				htmltext = "32563-08.htm";
			else
				htmltext = "32563-07.htm";
		}
		else if (event.equalsIgnoreCase("32563-09.htm"))
		{
			long count = st.getQuestItemsCount(DrakeFlesh);
			st.giveItems(57, count * 100);
			st.takeItems(DrakeFlesh, count);
		}
		else if (event.equalsIgnoreCase("32563-11.htm"))
		{
			if (st.hasQuestItems(VariantDrakeWingHorns))
			{
				long count = st.getQuestItemsCount(VariantDrakeWingHorns);
				st.giveItems(57, count * 200000);
				st.takeItems(VariantDrakeWingHorns, count);
				htmltext = "32563-12.htm";
			}
			else
				htmltext = "32563-11.htm";
		}
		else if (event.equalsIgnoreCase("32563-14.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("32557-03.htm"))
		{
			if (!st.hasQuestItems(RottenBlood) && st.getQuestItemsCount(ExtractedRedStarStone) < 100)
				htmltext = "32557-03.htm";
			else if (st.hasQuestItems(RottenBlood) && st.getQuestItemsCount(ExtractedRedStarStone) < 100)
				htmltext = "32557-04.htm";
			else if (!st.hasQuestItems(RottenBlood) && st.getQuestItemsCount(ExtractedRedStarStone) >= 100)
				htmltext = "32557-05.htm";
			else if (st.hasQuestItems(RottenBlood) && st.getQuestItemsCount(ExtractedRedStarStone) >= 100)
			{
				st.giveItems(BaitForDrakes, 1);
				st.takeItems(RottenBlood, 1);
				st.takeItems(ExtractedRedStarStone, 100);
				htmltext = "32557-06.htm";
			}
		}
		else if (event.equalsIgnoreCase("32555-03.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32555-05.htm"))
		{
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
		}
		else if (event.equalsIgnoreCase("32555-06.htm"))
		{
			if (st.getQuestItemsCount(DrakeFlesh) < 100)
				htmltext = "32555-06.htm";
			else
				htmltext = "32555-07.htm";
		}
		else if (event.equalsIgnoreCase("32555-08.htm"))
		{
			st.giveItems(RottenBlood, 1);
			st.takeItems(DrakeFlesh, 100);
		}
		else if (event.equalsIgnoreCase("32555-10.htm"))
		{
			if (st.hasQuestItems(VariantDrakeWingHorns))
				htmltext = "32555-11.htm";
			else
				htmltext = "32555-10.htm";
		}
		else if (event.equalsIgnoreCase("32555-15.htm"))
		{
			int i0 = getRandom(1000);
			int i1 = getRandom(1000);
			
			if (i0 >= 500 && i1 >= 600)
			{
				st.giveItems(57, getRandom(49917) + 125000);
				if (i1 < 720)
				{
					st.giveItems(9628, getRandom(3) + 1);
					st.giveItems(9629, getRandom(3) + 1);
				}
				else if (i1 < 840)
				{
					st.giveItems(9629, getRandom(3) + 1);
					st.giveItems(9630, getRandom(3) + 1);
				}
				else if (i1 < 960)
				{
					st.giveItems(9628, getRandom(3) + 1);
					st.giveItems(9630, getRandom(3) + 1);
				}
				else if (i1 < 1000)
				{
					st.giveItems(9628, getRandom(3) + 1);
					st.giveItems(9629, getRandom(3) + 1);
					st.giveItems(9630, getRandom(3) + 1);
				}
				htmltext = "32555-15.htm";
			}
			else if (i0 >= 500 && i1 < 600)
			{
				st.giveItems(57, getRandom(49917) + 125000);
				if (i1 < 210)
				{
				}
				else if (i1 < 340)
					st.giveItems(9628, getRandom(3) + 1);
				else if (i1 < 470)
					st.giveItems(9629, getRandom(3) + 1);
				else if (i1 < 600)
					st.giveItems(9630, getRandom(3) + 1);
				
				htmltext = "32555-16.htm";
			}
			else if (i0 < 500 && i1 >= 600)
			{
				st.giveItems(57, getRandom(49917) + 25000);
				if (i1 < 720)
				{
					st.giveItems(9628, getRandom(3) + 1);
					st.giveItems(9629, getRandom(3) + 1);
				}
				else if (i1 < 840)
				{
					st.giveItems(9629, getRandom(3) + 1);
					st.giveItems(9630, getRandom(3) + 1);
				}
				else if (i1 < 960)
				{
					st.giveItems(9628, getRandom(3) + 1);
					st.giveItems(9630, getRandom(3) + 1);
				}
				else if (i1 < 1000)
				{
					st.giveItems(9628, getRandom(3) + 1);
					st.giveItems(9629, getRandom(3) + 1);
					st.giveItems(9630, getRandom(3) + 1);
				}
				htmltext = "32555-17.htm";
			}
			else if (i0 < 500 && i1 < 600)
			{
				st.giveItems(57, getRandom(49917) + 25000);
				if (i1 < 210)
				{
				}
				else if (i1 < 340)
					st.giveItems(9628, getRandom(3) + 1);
				else if (i1 < 470)
					st.giveItems(9629, getRandom(3) + 1);
				else if (i1 < 600)
					st.giveItems(9630, getRandom(3) + 1);
				
				htmltext = "32555-18.htm";
			}
			st.takeItems(VariantDrakeWingHorns, 1);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);

		QuestState st = player.getQuestState(qn);
		QuestState prev = player.getQuestState("_10273_GoodDayToFly");
		
		if (st == null)
			return htmltext;
		
		if (npc.getId() == Plenos)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (prev != null && prev.getState() == State.COMPLETED && player.getLevel() >= 78)
						htmltext = "32563-01.htm";
					else
						htmltext = "32563-02.htm";
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
						htmltext = "32563-05.htm";
					else
						htmltext = "32563-06.htm";
					break;
			}
		}

		if (st.getState() == State.STARTED)
		{
			if (npc.getId() == Lekon)
			{
				switch (st.getInt("cond"))
				{
					case 1:
						htmltext = "32557-01.htm";
						break;
					case 2:
						htmltext = "32557-02.htm";
						break;
				}
			}
			else if (npc.getId() == Tenius)
			{
				switch (st.getInt("cond"))
				{
					case 1:
						htmltext = "32555-01.htm";
						break;
					case 2:
						htmltext = "32555-04.htm";
						break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, 2);
		if (partyMember == null)
			return null;
		final QuestState st = partyMember.getQuestState(qn);
		int chance = getRandom(1000);
		
		switch (npc.getId())
		{
			case 22612:
				if (chance < 413)
					st.giveItems(DrakeFlesh, 2);
				else
					st.giveItems(DrakeFlesh, 1);
				break;
			case 22613:
				if (chance < 440)
					st.giveItems(DrakeFlesh, 2);
				else
					st.giveItems(DrakeFlesh, 1);
				break;
			case 25632:
				if (chance < 996)
					st.giveItems(DrakeFlesh, 1);
				break;
			case 22610:
				if (chance < 485)
					st.giveItems(DrakeFlesh, 2);
				else
					st.giveItems(DrakeFlesh, 1);
				break;
			case 22611:
				if (chance < 451)
					st.giveItems(DrakeFlesh, 2);
				else
					st.giveItems(DrakeFlesh, 1);
				break;
			case 25631:
				if (chance < 485)
					st.giveItems(DrakeFlesh, 2);
				else
					st.giveItems(DrakeFlesh, 1);
				break;
			case 25626:
				if (chance < 708)
					st.giveItems(VariantDrakeWingHorns, getRandom(2) + 1);
				else if (chance < 978)
					st.giveItems(VariantDrakeWingHorns, getRandom(3) + 3);
				else if (chance < 994)
					st.giveItems(VariantDrakeWingHorns, getRandom(4) + 6);
				else if (chance < 998)
					st.giveItems(VariantDrakeWingHorns, getRandom(4) + 10);
				else if (chance < 1000)
					st.giveItems(VariantDrakeWingHorns, getRandom(5) + 14);
				break;
		}
		st.playSound("ItemSound.quest_itemget");
		return null;
	}
	
	public _702_ATrapForRevenge(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(Plenos);
		addTalkId(Plenos);
		addTalkId(Lekon);
		addTalkId(Tenius);

		for (int i : Monsters)
		{
			addKillId(i);
		}
	}
	
	public static void main(String[] args)
	{
		new _702_ATrapForRevenge(702, qn, "");
	}
}