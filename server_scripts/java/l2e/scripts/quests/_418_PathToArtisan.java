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
 * Created by LordWinter 25.09.2012
 * Based on L2J Eternity-World
 */
public class _418_PathToArtisan extends Quest
{
	private static final String qn = "_418_PathToArtisan";
	
	// Npcs
	private static final int SILVERA = 30527;
	private static final int PINTER = 30298;
	private static final int KLUTO = 30317;
	
	// Mobs
	private static final int VUKU_FIGHTER = 20017;
	private static final int BOOGLE_RATMAN = 20389;
	private static final int BOOGLE_RATMAN_LEADER = 20390;
	
	// Quest Items
	private static final int SILVERYS_RING = 1632;
	private static final int PASS_1ST = 1633;
	private static final int PASS_2ND = 1634;
	private static final int PASS_FINAL = 1635;
	private static final int RATMAN_TOOTH = 1636;
	private static final int BIG_RATMAN_TOOTH = 1637;
	private static final int KLUTOS_LETTER = 1638;
	private static final int FOOTPRINT = 1639;
	private static final int SECRET_BOX1 = 1640;
	private static final int SECRET_BOX2 = 1641;
	private static final int TOTEM_SPIRIT_CLAW = 1622;
	private static final int TATARUS_LETTER = 1623;
	
	private static final int[] QUESTITEMS =
	{
		SILVERYS_RING,
		PASS_1ST,
		PASS_2ND,
		RATMAN_TOOTH,
		BIG_RATMAN_TOOTH,
		KLUTOS_LETTER,
		FOOTPRINT,
		SECRET_BOX1,
		SECRET_BOX2,
		TOTEM_SPIRIT_CLAW,
		TATARUS_LETTER
	};
	
	public _418_PathToArtisan(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(SILVERA);
		
		addTalkId(SILVERA);
		addTalkId(PINTER);
		addTalkId(KLUTO);
		
		addKillId(VUKU_FIGHTER);
		addKillId(BOOGLE_RATMAN);
		addKillId(BOOGLE_RATMAN_LEADER);
		
		questItemIds = QUESTITEMS;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			return super.onAdvEvent(event, npc, player);
		}
		
		if (event.equalsIgnoreCase("30527_1"))
		{
			if (player.getClassId().getId() != 0x35)
			{
				htmltext = player.getClassId().getId() == 0x38 ? "30527-02a.htm" : "30527-02.htm";
			}
			else
			{
				if (player.getLevel() < 18)
				{
					htmltext = "30527-03.htm";
				}
				else
				{
					htmltext = st.getQuestItemsCount(PASS_FINAL) != 0 ? "30527-04.htm" : "30527-05.htm";
				}
			}
		}
		else if (event.equalsIgnoreCase("30527_2"))
		{
			st.takeItems(TOTEM_SPIRIT_CLAW, 1);
			st.giveItems(TATARUS_LETTER, 1);
			htmltext = "30527-11.htm";
		}
		else if (event.equalsIgnoreCase("1"))
		{
			st.set("id", "0");
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(SILVERYS_RING, 1);
			htmltext = "30527-06.htm";
		}
		else if (event.equalsIgnoreCase("30317_1"))
		{
			htmltext = "30317-02.htm";
		}
		else if (event.equalsIgnoreCase("30317_2"))
		{
			htmltext = "30317-05.htm";
		}
		else if (event.equalsIgnoreCase("30317_3"))
		{
			htmltext = "30317-03.htm";
		}
		else if (event.equalsIgnoreCase("30317_4"))
		{
			st.giveItems(KLUTOS_LETTER, 1);
			st.set("cond", "4");
			htmltext = "30317-04.htm";
		}
		else if (event.equalsIgnoreCase("30317_5"))
		{
			htmltext = "30317-06.htm";
		}
		else if (event.equalsIgnoreCase("30317_6"))
		{
			st.giveItems(KLUTOS_LETTER, 1);
			st.set("cond", "4");
			htmltext = "30317-07.htm";
		}
		else if (event.equalsIgnoreCase("30317_7"))
		{
			if ((st.getQuestItemsCount(PASS_1ST) > 0) && (st.getQuestItemsCount(PASS_2ND) > 0) && (st.getQuestItemsCount(SECRET_BOX2) > 0))
			{
				st.takeItems(PASS_1ST, 1);
				st.takeItems(PASS_2ND, 1);
				st.takeItems(SECRET_BOX2, 1);
				String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
				if (isFinished.equalsIgnoreCase(""))
				{
					st.addExpAndSp(160267, 3670);
				}
				st.giveItems(PASS_FINAL, 1);
				st.saveGlobalQuestVar("1ClassQuestFinished", "1");
				st.set("cond", "0");
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
				htmltext = "30317-10.htm";
			}
			else
			{
				htmltext = "30317-08.htm";
			}
		}
		else if (event.equalsIgnoreCase("30317_8"))
		{
			htmltext = "30317-11.htm";
		}
		else if (event.equalsIgnoreCase("30317_9"))
		{
			if ((st.getQuestItemsCount(PASS_1ST) > 0) && (st.getQuestItemsCount(PASS_2ND) > 0) && (st.getQuestItemsCount(SECRET_BOX2) > 0))
			{
				st.set("cond", "0");
				st.takeItems(PASS_1ST, 1);
				st.takeItems(PASS_2ND, 1);
				st.takeItems(SECRET_BOX2, 1);
				st.addExpAndSp(228064, 3670);
				st.giveItems(PASS_FINAL, 1);
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
				htmltext = "30317-12.htm";
			}
			else
			{
				htmltext = "30317-08.htm";
			}
		}
		else if (event.equalsIgnoreCase("30298_1"))
		{
			htmltext = "30298-02.htm";
		}
		else if (event.equalsIgnoreCase("30298_2"))
		{
			st.takeItems(KLUTOS_LETTER, 1);
			st.giveItems(FOOTPRINT, 1);
			st.set("cond", "5");
			htmltext = "30298-03.htm";
		}
		else if (event.equalsIgnoreCase("30298_3"))
		{
			st.takeItems(SECRET_BOX1, 1);
			st.takeItems(FOOTPRINT, 1);
			st.giveItems(SECRET_BOX2, 1);
			st.giveItems(PASS_2ND, 1);
			st.set("cond", "7");
			htmltext = "30298-06.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = Quest.getNoQuestMsg(talker);
		QuestState st = talker.getQuestState(getName());
		
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		int id = st.getState();
		int cond = st.getInt("cond");
		
		if ((npcId != SILVERA) && (id != State.STARTED))
		{
			return htmltext;
		}
		
		if ((npcId == SILVERA) && (cond == 0))
		{
			htmltext = "30527-01.htm";
		}
		else if ((npcId == SILVERA) && (cond > 0) && (st.getQuestItemsCount(SILVERYS_RING) == 1) && ((st.getQuestItemsCount(RATMAN_TOOTH) + st.getQuestItemsCount(BIG_RATMAN_TOOTH)) < 12))
		{
			htmltext = "30527-07.htm";
		}
		else if ((npcId == SILVERA) && (cond > 0) && (st.getQuestItemsCount(SILVERYS_RING) == 1) && (st.getQuestItemsCount(RATMAN_TOOTH) >= 10) && (st.getQuestItemsCount(BIG_RATMAN_TOOTH) >= 2))
		{
			st.takeItems(SILVERYS_RING, st.getQuestItemsCount(SILVERYS_RING));
			st.takeItems(RATMAN_TOOTH, st.getQuestItemsCount(RATMAN_TOOTH));
			st.takeItems(BIG_RATMAN_TOOTH, st.getQuestItemsCount(BIG_RATMAN_TOOTH));
			st.giveItems(PASS_1ST, 1);
			st.set("cond", "3");
			htmltext = "30527-08.htm";
		}
		else if ((npcId == SILVERA) && (cond > 0) && (st.getQuestItemsCount(PASS_1ST) == 1))
		{
			htmltext = "30527-09.htm";
		}
		else if ((npcId == 30317) && (cond > 0) && (st.getQuestItemsCount(KLUTOS_LETTER) == 0) && (st.getQuestItemsCount(FOOTPRINT) == 0) && (st.getQuestItemsCount(PASS_1ST) > 0) && (st.getQuestItemsCount(PASS_2ND) == 0) && (st.getQuestItemsCount(SECRET_BOX2) == 0))
		{
			htmltext = "30317-01.htm";
		}
		else if ((npcId == 30317) && (cond > 0) && (st.getQuestItemsCount(PASS_1ST) > 0) && ((st.getQuestItemsCount(KLUTOS_LETTER) > 0) || (st.getQuestItemsCount(FOOTPRINT) > 0)))
		{
			htmltext = "30317-08.htm";
		}
		else if ((npcId == 30317) && (cond > 0) && (st.getQuestItemsCount(PASS_1ST) > 0) && (st.getQuestItemsCount(PASS_2ND) > 0) && (st.getQuestItemsCount(SECRET_BOX2) > 0))
		{
			htmltext = "30317-09.htm";
		}
		else if ((npcId == 30298) && (cond > 0) && (st.getQuestItemsCount(PASS_1ST) > 0) && (st.getQuestItemsCount(KLUTOS_LETTER) > 0))
		{
			htmltext = "30298-01.htm";
		}
		else if ((npcId == 30298) && (cond > 0) && (st.getQuestItemsCount(PASS_1ST) > 0) && (st.getQuestItemsCount(FOOTPRINT) > 0) && (st.getQuestItemsCount(SECRET_BOX1) == 0))
		{
			htmltext = "30298-04.htm";
		}
		else if ((npcId == 30298) && (cond > 0) && (st.getQuestItemsCount(PASS_1ST) > 0) && (st.getQuestItemsCount(FOOTPRINT) > 0) && (st.getQuestItemsCount(SECRET_BOX1) > 0))
		{
			htmltext = "30298-05.htm";
		}
		else if ((npcId == 30298) && (cond > 0) && (st.getQuestItemsCount(PASS_1ST) > 0) && (st.getQuestItemsCount(PASS_2ND) > 0) && (st.getQuestItemsCount(SECRET_BOX2) > 0))
		{
			htmltext = "30298-07.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		QuestState st = killer.getQuestState(getName());
		
		if (st == null)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		if (st.getState() != State.STARTED)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		
		if (npcId == BOOGLE_RATMAN)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(SILVERYS_RING) == 1) && (st.getQuestItemsCount(RATMAN_TOOTH) < 10))
			{
				if (st.getRandom(10) < 7)
				{
					if ((st.getQuestItemsCount(RATMAN_TOOTH) == 9) && (st.getQuestItemsCount(BIG_RATMAN_TOOTH) == 2))
					{
						st.giveItems(RATMAN_TOOTH, 1);
						st.playSound("ItemSound.quest_middle");
						st.set("cond", "2");
					}
					else
					{
						st.giveItems(RATMAN_TOOTH, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
		}
		else if (npcId == BOOGLE_RATMAN_LEADER)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(SILVERYS_RING) == 1) && (st.getQuestItemsCount(BIG_RATMAN_TOOTH) < 2))
			{
				if (st.getRandom(10) < 5)
				{
					if ((st.getQuestItemsCount(BIG_RATMAN_TOOTH) == 1) && (st.getQuestItemsCount(RATMAN_TOOTH) == 10))
					{
						st.giveItems(BIG_RATMAN_TOOTH, 1);
						st.playSound("ItemSound.quest_middle");
						st.set("cond", "2");
					}
					else
					{
						st.giveItems(BIG_RATMAN_TOOTH, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
		}
		else if (npcId == VUKU_FIGHTER)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(FOOTPRINT) == 1) && (st.getQuestItemsCount(SECRET_BOX1) < 1))
			{
				if (st.getRandom(10) < 2)
				{
					st.giveItems(SECRET_BOX1, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "6");
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _418_PathToArtisan(418, qn, "");
	}
}