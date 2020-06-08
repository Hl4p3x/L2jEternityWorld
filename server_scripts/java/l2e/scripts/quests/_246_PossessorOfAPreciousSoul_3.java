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
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

public class _246_PossessorOfAPreciousSoul_3 extends Quest
{
	private static final String qn = "_246_PossessorOfAPreciousSoul_3";
	
	// NPCs
	private static final int LADD = 30721;
	private static final int CARADINE = 31740;
	private static final int OSSIAN = 31741;
	
	private final int NPCS[] =
	{
		LADD,
		CARADINE,
		OSSIAN
	};
	
	// Quest Items
	private static final int CARADINE_LETTER = 7678;
	private static final int CARADINE_LETTER_LAST = 7679;
	private static final int WATERBINDER = 7591;
	private static final int EVERGREEN = 7592;
	private static final int RAIN_SONG = 7593;
	private static final int RAIN_SONG_FRAGMENT = 21725;
	private static final int RELIC_BOX = 7594;
	
	// Chances
	private static final int CHANCE_FOR_DROP = (int) (35 * Config.RATE_QUEST_DROP);
	
	// Mobs
	private static final int PILGRIM_OF_SPLENDOR = 21541;
	private static final int JUDGE_OF_SPLENDOR = 21544;
	private static final int BARAKIEL = 25325;
	
	private static final int WAILING_OF_SPLENDOR = 21539;
	private static final int FANG_OF_SPLENDOR = 21537;
	private static final int CROWN_OF_SPLENDOR = 21536;
	private static final int SHOUT_OF_SPLENDOR = 21532;
	
	private final int MOBS[] =
	{
		PILGRIM_OF_SPLENDOR,
		JUDGE_OF_SPLENDOR,
		BARAKIEL,
		WAILING_OF_SPLENDOR,
		FANG_OF_SPLENDOR,
		CROWN_OF_SPLENDOR,
		SHOUT_OF_SPLENDOR
	};
	
	public _246_PossessorOfAPreciousSoul_3(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(CARADINE);
		
		for (int npcId : NPCS)
		{
			addTalkId(npcId);
		}
		
		for (int npcId : MOBS)
		{
			addKillId(npcId);
		}
		
		questItemIds = new int[5];
		questItemIds[0] = WATERBINDER;
		questItemIds[1] = EVERGREEN;
		questItemIds[2] = RAIN_SONG;
		questItemIds[3] = RELIC_BOX;
		questItemIds[4] = RAIN_SONG_FRAGMENT;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
		{
			return null;
		}
		
		int cond = st.getInt("cond");
		
		if (event.equalsIgnoreCase("31740-4.htm"))
		{
			if (cond == 0)
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("31741-2.htm"))
		{
			if (cond == 1)
			{
				st.set("cond", "2");
				st.set("awaitsWaterbinder", "1");
				st.set("awaitsEvergreen", "1");
				st.takeItems(CARADINE_LETTER, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("31744-2.htm"))
		{
			if (cond == 2)
			{
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("31741-5.htm"))
		{
			if (cond == 3)
			{
				st.set("cond", "4");
				st.takeItems(WATERBINDER, 1);
				st.takeItems(EVERGREEN, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("31741-9.htm"))
		{
			if (cond == 5)
			{
				st.set("cond", "6");
				st.takeItems(RAIN_SONG, 1);
				st.takeItems(RAIN_SONG_FRAGMENT, -1);
				st.giveItems(RELIC_BOX, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("30721-2.htm"))
		{
			if (cond == 6)
			{
				st.set("cond", "0");
				st.takeItems(RELIC_BOX, 1);
				st.giveItems(CARADINE_LETTER_LAST, 1);
				st.addExpAndSp(719843, 0);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = getNoQuestMsg(talker);
		
		QuestState st = talker.getQuestState(qn);
		
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		byte id = st.getState();
		
		if ((npcId != CARADINE) && (id != State.STARTED))
		{
			return htmltext;
		}
		
		int cond = st.getInt("cond");
		
		if (talker.isSubClassActive())
		{
			switch (npcId)
			{
				case CARADINE:
					if ((cond == 0) && (st.getQuestItemsCount(CARADINE_LETTER) == 1))
					{
						if (id == State.COMPLETED)
						{
							htmltext = getAlreadyCompletedMsg(talker);
						}
						else if (talker.getLevel() < 65)
						{
							htmltext = "31740-2.htm";
							st.exitQuest(true);
						}
						else if (talker.getLevel() >= 65)
						{
							htmltext = "31740-1.htm";
						}
					}
					else if (cond == 1)
					{
						htmltext = "31740-5.htm";
					}
					break;
				case OSSIAN:
					if (cond == 1)
					{
						htmltext = "31741-1.htm";
					}
					else if (cond == 2)
					{
						htmltext = "31741-4.htm";
					}
					else if ((cond == 3) && (st.getQuestItemsCount(WATERBINDER) == 1) && (st.getQuestItemsCount(EVERGREEN) == 1))
					{
						htmltext = "31741-3.htm";
					}
					else if (cond == 4)
					{
						htmltext = "31741-8.htm";
					}
					else if ((cond == 5) && ((st.getQuestItemsCount(RAIN_SONG) == 1) || (st.getQuestItemsCount(RAIN_SONG_FRAGMENT) == 100)))
					{
						htmltext = "31741-7.htm";
					}
					else if ((cond == 6) && (st.getQuestItemsCount(RELIC_BOX) == 1))
					{
						htmltext = "31741-11.htm";
					}
					break;
				case LADD:
					if (cond == 6)
					{
						htmltext = "30721-1.htm";
					}
					break;
			}
		}
		else
		{
			htmltext = "sub.htm";
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		int npcId = npc.getId();
		
		QuestState st = killer.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		L2PcInstance partyMember;
		switch (npcId)
		{
			case PILGRIM_OF_SPLENDOR:
				// get a random party member who is doing this quest and needs this drop
				partyMember = getRandomPartyMember(killer, "awaitsWaterbinder", "1");
				if (partyMember != null)
				{
					st = partyMember.getQuestState(qn);
					if (st.getQuestItemsCount(WATERBINDER) < 1)
					{
						if (getRandom(100) < CHANCE_FOR_DROP)
						{
							st.giveItems(WATERBINDER, 1);
							st.unset("awaitsWaterbinder");
							if (st.getQuestItemsCount(EVERGREEN) < 1)
							{
								st.playSound("ItemSound.quest_itemget");
							}
							else
							{
								st.playSound("ItemSound.quest_middle");
								st.set("cond", "3");
							}
						}
					}
				}
				break;
			case JUDGE_OF_SPLENDOR:
				// get a random party member who is doing this quest and needs this drop
				partyMember = getRandomPartyMember(killer, "awaitsEvergreen", "1");
				if (partyMember != null)
				{
					st = partyMember.getQuestState(qn);
					int cond = st.getInt("cond");
					if ((cond == 2) && (st.getQuestItemsCount(EVERGREEN) < 1))
					{
						if (getRandom(100) < CHANCE_FOR_DROP)
						{
							st.giveItems(EVERGREEN, 1);
							st.unset("awaitsEvergreen");
							if (st.getQuestItemsCount(WATERBINDER) < 1)
							{
								st.playSound("ItemSound.quest_itemget");
							}
							else
							{
								st.playSound("ItemSound.quest_middle");
								st.set("cond", "3");
							}
						}
					}
				}
				break;
			case BARAKIEL:
				// give the quest item and update variables for ALL PARTY MEMBERS who are doing the quest,
				// so long as they each qualify for the drop (cond == 4 and item not in inventory)
				// note: the killer WILL participate in the loop as a party member (no need to handle separately)
				L2Party party = killer.getParty();
				if (party != null)
				{
					for (L2PcInstance pm : party.getMembers())
					{
						QuestState pst = pm.getQuestState(qn);
						if ((pst != null) && (pst.getInt("cond") == 4) && (pst.getQuestItemsCount(RAIN_SONG) < 1) && (pst.getQuestItemsCount(RAIN_SONG_FRAGMENT) < 100))
						{
							pst.giveItems(RAIN_SONG, 1);
							pst.playSound("ItemSound.quest_middle");
							pst.set("cond", "5");
						}
					}
				}
				else
				{
					QuestState pst = killer.getQuestState(qn);
					if (pst != null)
					{
						if ((pst.getInt("cond") == 4) && (pst.getQuestItemsCount(RAIN_SONG) < 1) && (pst.getQuestItemsCount(RAIN_SONG_FRAGMENT) < 100))
						{
							pst.giveItems(RAIN_SONG, 1);
							pst.playSound("ItemSound.quest_middle");
							pst.set("cond", "5");
						}
					}
				}
				break;
			case WAILING_OF_SPLENDOR:
			case FANG_OF_SPLENDOR:
			case CROWN_OF_SPLENDOR:
			case SHOUT_OF_SPLENDOR:
			{
				if ((st.getInt("cond") == 4) && (st.getQuestItemsCount(RAIN_SONG) < 1) && (st.getQuestItemsCount(RAIN_SONG_FRAGMENT) < 100))
				{
					if (getRandom(100) < CHANCE_FOR_DROP)
					{
						st.giveItems(RAIN_SONG_FRAGMENT, 1);
						if (st.getQuestItemsCount(RAIN_SONG_FRAGMENT) == 100)
						{
							st.playSound("ItemSound.quest_middle");
							st.set("cond", "5");
						}
						else
						{
							st.playSound("ItemSound.quest_itemget");
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
		new _246_PossessorOfAPreciousSoul_3(246, qn, "");
	}
}