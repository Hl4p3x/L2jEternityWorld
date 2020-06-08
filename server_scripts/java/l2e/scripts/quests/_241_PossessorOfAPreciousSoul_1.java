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

public class _241_PossessorOfAPreciousSoul_1 extends Quest
{
	private static final String qn = "_241_PossessorOfAPreciousSoul_1";
	
	// NPCs
	private static final int _stedmiel = 30692;
	private static final int _gabrielle = 30753;
	private static final int _gilmore = 30754;
	private static final int _kantabilon = 31042;
	private static final int _noel = 31272;
	private static final int _rahorakti = 31336;
	private static final int _talien = 31739;
	private static final int _caradine = 31740;
	private static final int _virgil = 31742;
	private static final int _kassandra = 31743;
	private static final int _ogmar = 31744;
	
	// Quest Items
	private static final int LEGEND_OF_SEVENTEEN = 7587;
	private static final int MALRUK_SUCCUBUS_CLAW = 7597;
	private static final int ECHO_CRYSTAL = 7589;
	private static final int POETRY_BOOK = 7588;
	private static final int CRIMSON_MOSS = 7598;
	private static final int RAHORAKTIS_MEDICINE = 7599;
	private static final int LUNARGENT = 6029;
	private static final int HELLFIRE_OIL = 6033;
	private static final int VIRGILS_LETTER = 7677;
	
	// Chances
	private static final int CRIMSON_MOSS_CHANCE = (10 * (int) Config.RATE_QUEST_DROP);
	private static final int MALRUK_SUCCUBUS_CLAW_CHANCE = (15 * (int) Config.RATE_QUEST_DROP); // Guessed Chance! 
	
	private static final int _baraham = 27113;
	
	private final int MOBS[] =
	{
		20244, 20245, 20283, 21508, 21509, 21510, 21511, 21512, _baraham, 20669
	};
	
	private final int NPCS[] =
	{
		_stedmiel, _gabrielle, _gilmore, _kantabilon, _noel, _rahorakti, _talien, _caradine, _virgil, _kassandra, _ogmar
	};
	
	public _241_PossessorOfAPreciousSoul_1(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_talien);
		
		for (int npcId : NPCS)
			addTalkId(npcId);
		
		for (int npcId : MOBS)
			addKillId(npcId);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return null;
		
		int cond = st.getInt("cond");
		
		if (!player.isSubClassActive())
			return null;
		
		if (event.equalsIgnoreCase("31739-4.htm"))
		{
			if (cond == 0)
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("30753-2.htm"))
		{
			if (cond == 1)
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("30754-2.htm"))
		{
			if (cond == 2)
			{
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("31739-8.htm"))
		{
			if (cond == 4 && st.getQuestItemsCount(LEGEND_OF_SEVENTEEN) > 0)
			{
				st.set("cond", "5");
				st.takeItems(LEGEND_OF_SEVENTEEN, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("31042-2.htm"))
		{
			if (cond == 5)
			{
				st.set("cond", "6");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("31042-5.htm"))
		{
			if (cond == 7 && st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW) >= 10)
			{
				st.set("cond", "8");
				st.takeItems(MALRUK_SUCCUBUS_CLAW, 10);
				st.giveItems(ECHO_CRYSTAL, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("31739-12.htm"))
		{
			if (cond == 8 && st.getQuestItemsCount(ECHO_CRYSTAL) > 0)
			{
				st.set("cond", "9");
				st.takeItems(ECHO_CRYSTAL, 1);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("30692-2.htm"))
		{
			if (cond == 9 && !(st.getQuestItemsCount(POETRY_BOOK) > 0))
			{
				st.set("cond", "10");
				st.giveItems(POETRY_BOOK, 1);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("31739-15.htm"))
		{
			if (cond == 10 && st.getQuestItemsCount(POETRY_BOOK) > 0)
			{
				st.set("cond", "11");
				st.takeItems(POETRY_BOOK, 1);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("31742-2.htm"))
		{
			if (cond == 11)
			{
				st.set("cond", "12");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("31744-2.htm"))
		{
			if (cond == 12)
			{
				st.set("cond", "13");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("31336-2.htm"))
		{
			if (cond == 13)
			{
				st.set("cond", "14");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("31336-5.htm"))
		{
			if (cond == 15 && st.getQuestItemsCount(CRIMSON_MOSS) > 0)
			{
				st.set("cond", "16");
				st.takeItems(CRIMSON_MOSS, 5);
				st.giveItems(RAHORAKTIS_MEDICINE, 1);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("31743-2.htm"))
		{
			if (cond == 16 && st.getQuestItemsCount(RAHORAKTIS_MEDICINE) > 0)
			{
				st.set("cond", "17");
				st.takeItems(RAHORAKTIS_MEDICINE, 1);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("31742-5.htm"))
		{
			if (cond == 17)
			{
				st.set("cond", "18");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("31740-2.htm"))
		{
			if (cond == 18)
			{
				st.set("cond", "19");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("31272-2.htm"))
		{
			if (cond == 19)
			{
				st.set("cond", "20");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("31272-5.htm"))
		{
			if (cond == 20 && st.getQuestItemsCount(LUNARGENT) >= 5 && st.getQuestItemsCount(HELLFIRE_OIL) > 0)
			{
				st.takeItems(LUNARGENT, 5);
				st.takeItems(HELLFIRE_OIL, 1);
				st.set("cond", "21");
				st.playSound("ItemSound.quest_accept");
			}
			else
				htmltext = "31272-4.htm";
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
		
		if (npcId != _talien && id != State.STARTED)
			return htmltext;
		
		int cond = st.getInt("cond");
		
		if (npcId == _talien)
		{
			if (cond == 0)
			{
				if (id == State.COMPLETED)
					htmltext = getAlreadyCompletedMsg(player); 
				else if (player.getLevel() >= 50 && player.isSubClassActive())
					htmltext = "31739-1.htm";
				else
				{
					htmltext = "31739-2.htm";
					st.exitQuest(true);
				}
			}
			if (!player.isSubClassActive())
				htmltext = "sub.htm";
			else
			{
				switch (cond)
				{
					case 1:
						htmltext = "31739-5.htm";
						break;
					case 4:
						if (st.getQuestItemsCount(LEGEND_OF_SEVENTEEN) == 1)
							htmltext = "31739-6.htm";
						break;
					case 5:
						htmltext = "31739-9.htm";
						break;
					case 8:
						if (st.getQuestItemsCount(ECHO_CRYSTAL) == 1)
							htmltext = "31739-11.htm";
						break;
					case 9:
						htmltext = "31739-13.htm";
						break;
					case 10:
						if (st.getQuestItemsCount(POETRY_BOOK) == 1)
							htmltext = "31739-14.htm";
						break;
					case 11:
						htmltext = "31739-16.htm";
						break;
				}
			}
		}
		else if (player.isSubClassActive())
		{
			switch (npcId)
			{
				case _gabrielle:
				{
					switch (cond)
					{
						case 1:
							htmltext = "30753-1.htm";
							break;
						case 2:
							htmltext = "30753-3.htm";
							break;
					}
					break;
				}
				case _gilmore:
				{
					switch (cond)
					{
						case 2:
							htmltext = "30754-1.htm";
							break;
						case 3:
							htmltext = "30754-3.htm";
							break;
					}
					break;
				}
				case _kantabilon:
				{
					switch (cond)
					{
						case 5:
							htmltext = "31042-1.htm";
							break;
						case 6:
							htmltext = "31042-4.htm";
							break;
						case 7:
							if (st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW) == 10)
								htmltext = "31042-3.htm";
							break;
						case 8:
							htmltext = "31042-6.htm";
							break;
					}
					break;
				}
				case _stedmiel:
				{
					switch (cond)
					{
						case 9:
							htmltext = "30692-1.htm";
							break;
						case 10:
							htmltext = "30692-3.htm";
							break;
					}
					break;
				}
				case _virgil:
				{
					switch (cond)
					{
						case 11:
							htmltext = "31742-1.htm";
							break;
						case 12:
							htmltext = "31742-3.htm";
							break;
						case 17:
							htmltext = "31742-4.htm";
							break;
						case 18:
						case 19:
						case 20:
						case 21:
							htmltext = "31742-6.htm";
							break;
					}
					break;
				}
				case _ogmar:
				{
					switch (cond)
					{
						case 12:
							htmltext = "31744-1.htm";
							break;
						case 13:
							htmltext = "31744-3.htm";
							break;
					}
					break;
				}
				case _rahorakti:
				{
					switch (cond)
					{
						case 13:
							htmltext = "31336-1.htm";
							break;
						case 14:
							htmltext = "31336-4.htm";
							break;
						case 15:
							if (st.getQuestItemsCount(CRIMSON_MOSS) == 5)
								htmltext = "31336-3.htm";
							break;
						case 16:
							htmltext = "31336-6.htm";
							break;
					}
					break;
				}
				case _kassandra:
				{
					switch (cond)
					{
						case 16:
							if (st.getQuestItemsCount(RAHORAKTIS_MEDICINE) == 1)
								htmltext = "31743-1.htm";
							break;
						case 17:
							htmltext = "31743-3.htm";
							break;
					}
					break;
				}
				case _caradine:
				{
					switch (cond)
					{
						case 18:
						case 19:
						case 20:
						case 21:
							st.giveItems(VIRGILS_LETTER, 1);
							st.addExpAndSp(263043, 0);
							st.set("cond", "0");
							st.playSound("ItemSound.quest_finish");
							st.exitQuest(false);
							htmltext = "31740-5.htm";
							break;
					}
					break;
				}
				case _noel:
				{
					switch (cond)
					{
						case 18:
						case 19:
						case 20:
						case 21:
							htmltext = "31272-7.htm";
							break;
					}
					break;
				}
			}
		}
		else
			htmltext = "sub.htm";
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		switch (npc.getId())
		{
			case _baraham:
			{
				// get a random party member who is doing this quest and is at cond == 3  
				L2PcInstance ptMember = getRandomPartyMember(killer, 3);
				if (ptMember != null)
				{
					QuestState st = ptMember.getQuestState(qn);
					st.set("cond", "4");
					st.giveItems(LEGEND_OF_SEVENTEEN, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				break;
			}
			case 20244:
			case 20245:
			case 20283:
			case 20284:
			{
				// get a random party member who is doing this quest and is at cond == 6
				L2PcInstance ptMember = getRandomPartyMember(killer, 6);
				if (ptMember != null)
				{
					QuestState st = ptMember.getQuestState(qn);
					int chance = getRandom(100);
					if (MALRUK_SUCCUBUS_CLAW_CHANCE >= chance && st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW) < 10)
					{
						st.giveItems(MALRUK_SUCCUBUS_CLAW, 1);
						st.playSound("ItemSound.quest_itemget");
						if (st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW) == 10)
						{
							st.set("cond", "7");
							st.playSound("ItemSound.quest_middle");
						}
					}
				}
				break;
			}
			case 20669:
			{
				// get a random party member who is doing this quest and is at cond == 14
				L2PcInstance ptMember = getRandomPartyMember(killer, 14);
				if (ptMember != null)
				{
					QuestState st = ptMember.getQuestState(qn);
					int chance = getRandom(100);
					if (CRIMSON_MOSS_CHANCE >= chance && st.getQuestItemsCount(CRIMSON_MOSS) < 5)
					{
						st.giveItems(CRIMSON_MOSS, 1);
						st.playSound("ItemSound.quest_itemget");
						if (st.getQuestItemsCount(CRIMSON_MOSS) == 5)
						{
							st.set("cond", "15");
							st.playSound("ItemSound.quest_middle");
						}
					}
				}
				break;
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _241_PossessorOfAPreciousSoul_1(241, qn, "");
	}
}