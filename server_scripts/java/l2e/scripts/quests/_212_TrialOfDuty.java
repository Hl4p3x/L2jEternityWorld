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
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public class _212_TrialOfDuty extends Quest
{
	private static final String qn = "_212_TrialOfDuty";

	// Npc
	private static final int HANNAVALT = 30109;
	private static final int DUSTIN = 30116;
	private static final int SIR_COLLIN_WINDAWOOD = 30311;
	private static final int SIR_ARON_TANFORD = 30653;
	private static final int SIR_KIEL_NIGHTHAWK = 30654;
	private static final int ISAEL_SILVERSHADOW = 30655;
	private static final int SPIRIT_OF_SIR_TALIANUS = 30656;

	private static final int[] TALKERS =
	{
		HANNAVALT, DUSTIN, SIR_COLLIN_WINDAWOOD, SIR_ARON_TANFORD, SIR_KIEL_NIGHTHAWK, ISAEL_SILVERSHADOW, SPIRIT_OF_SIR_TALIANUS
	};

	// Mobs
	private static final int HANGMAN_TREE = 20144;
	private static final int SKELETON_MARAUDER = 20190;
	private static final int SKELETON_RAIDER = 20191;
	private static final int STRAIN = 20200;
	private static final int GHOUL = 20201;
	private static final int BREKA_ORC_OVERLORD = 20270;
	private static final int SPIRIT_OF_SIR_HEROD = 27119;
	private static final int LETO_LIZARDMAN = 20577;
	private static final int LETO_LIZARDMAN_ARCHER = 20578;
	private static final int LETO_LIZARDMAN_SOLDIER = 20579;
	private static final int LETO_LIZARDMAN_WARRIOR = 20580;
	private static final int LETO_LIZARDMAN_SHAMAN = 20581;
	private static final int LETO_LIZARDMAN_OVERLORD = 20582;

	private static final int[] MOBS =
	{
		HANGMAN_TREE, SKELETON_MARAUDER, SKELETON_RAIDER, STRAIN, GHOUL, BREKA_ORC_OVERLORD, SPIRIT_OF_SIR_HEROD,
		LETO_LIZARDMAN, LETO_LIZARDMAN_ARCHER, LETO_LIZARDMAN_SOLDIER, LETO_LIZARDMAN_WARRIOR, LETO_LIZARDMAN_SHAMAN, LETO_LIZARDMAN_OVERLORD
	};

	// Quest items
	private static final int LETTER_OF_DUSTIN = 2634;
	private static final int KNIGHTS_TEAR = 2635;
	private static final int MIRROR_OF_ORPIC = 2636;
	private static final int TEAR_OF_CONFESSION = 2637;
	private static final int REPORT_PIECE = 2638;
	private static final int TALIANUSS_REPORT = 2639;
	private static final int TEAR_OF_LOYALTY = 2640;
	private static final int MILITAS_ARTICLE = 2641;
	private static final int SAINTS_ASHES_URN = 2642;
	private static final int ATEBALTS_SKULL = 2643;
	private static final int ATEBALTS_RIBS = 2644;
	private static final int ATEBALTS_SHIN = 2645;
	private static final int LETTER_OF_WINDAWOOD = 2646;
	private static final int OLD_KNIGHT_SWORD = 3027;

	private static final int[] QUESTITEMS =
	{
		LETTER_OF_DUSTIN, KNIGHTS_TEAR, MIRROR_OF_ORPIC, TEAR_OF_CONFESSION, REPORT_PIECE, TALIANUSS_REPORT,
		TEAR_OF_LOYALTY, MILITAS_ARTICLE, SAINTS_ASHES_URN, ATEBALTS_SKULL, ATEBALTS_RIBS, ATEBALTS_SHIN, LETTER_OF_WINDAWOOD, OLD_KNIGHT_SWORD
	};

	// Reward
	private static final int MARK_OF_DUTY = 2633;

	// Allowed classes
	private static final int[] CLASSES = { 0x04, 0x13, 0x20 };

	public _212_TrialOfDuty(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(HANNAVALT);

		for (int talkId : TALKERS)
			addTalkId(talkId);

		for (int mobId : MOBS)
			addKillId(mobId);

		questItemIds = QUESTITEMS;
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("1"))
		{
			htmltext = "30109-04.htm";
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.set("cond", "1");
		}
		else if (event.equalsIgnoreCase("30116_1"))
		{
			htmltext = "30116-02.htm";
		}
		else if (event.equalsIgnoreCase("30116_2"))
		{
			htmltext = "30116-03.htm";
		}
		else if (event.equalsIgnoreCase("30116_3"))
		{
			htmltext = "30116-04.htm";
		}
		else if (event.equalsIgnoreCase("30116_4"))
		{
			htmltext = "30116-05.htm";
			st.takeItems(TEAR_OF_LOYALTY, 1);
			st.set("cond", "14");
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = getNoQuestMsg(talker);
		QuestState st = talker.getQuestState(qn);
		if (st == null)
			return htmltext;

		int cond = st.getInt("cond");
		int npcId = npc.getId();
		int id = st.getState();

		if (npcId != HANNAVALT && id != State.STARTED)
			return htmltext;

		if (id == State.CREATED)
		{
			st.set("cond", "0");
			st.set("onlyone", "0");
			st.set("id", "0");
		}
		if (npcId == HANNAVALT && cond == 0 && st.getInt("onlyone") == 0)
		{
			if (Util.contains(CLASSES, talker.getClassId().getId()))
			{
				if (talker.getLevel() >= 35)
					htmltext = "30109-03.htm";
				else
				{
					htmltext = "30109-01.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				htmltext = "30109-02.htm";
				st.exitQuest(true);
			}
		}
		else if (npcId == HANNAVALT && cond == 0 && st.getInt("onlyone") == 1)
		{
			htmltext = Quest.getAlreadyCompletedMsg(talker);
		}
		else if (npcId == HANNAVALT && cond == 18 && st.getQuestItemsCount(LETTER_OF_DUSTIN) > 0)
		{
			htmltext = "30109-05.htm";
			st.set("onlyone", "1");
			st.set("cond", "0");
			st.takeItems(LETTER_OF_DUSTIN, 1);
			st.addExpAndSp(381288, 24729);
			st.giveItems(57, 69484);
			st.giveItems(7562, 61);
			st.giveItems(MARK_OF_DUTY, 1);
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		else if (npcId == HANNAVALT && cond == 1)
		{
			htmltext = "30109-04.htm";
		}
		else if (npcId == SIR_ARON_TANFORD && cond == 1)
		{
			htmltext = "30653-01.htm";
			if (st.getQuestItemsCount(OLD_KNIGHT_SWORD) == 0)
				st.giveItems(OLD_KNIGHT_SWORD, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == SIR_ARON_TANFORD && cond == 2 && st.getQuestItemsCount(KNIGHTS_TEAR) == 0)
		{
			htmltext = "30653-02.htm";
		}
		else if (npcId == SIR_ARON_TANFORD && cond == 3 && st.getQuestItemsCount(KNIGHTS_TEAR) > 0)
		{
			htmltext = "30653-03.htm";
			st.takeItems(KNIGHTS_TEAR, 1);
			st.takeItems(OLD_KNIGHT_SWORD, 1);
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == SIR_ARON_TANFORD && cond == 4)
		{
			htmltext = "30653-04.htm";
		}
		else if (npcId == SIR_KIEL_NIGHTHAWK && cond == 4)
		{
			htmltext = "30654-01.htm";
			st.set("cond", "5");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == SIR_KIEL_NIGHTHAWK && cond == 5 && st.getQuestItemsCount(TALIANUSS_REPORT) == 0)
		{
			htmltext = "30654-02.htm";
		}
		else if (npcId == SIR_KIEL_NIGHTHAWK && cond == 6 && st.getQuestItemsCount(TALIANUSS_REPORT) > 0)
		{
			htmltext = "30654-03.htm";
			st.set("cond", "7");
			st.playSound("ItemSound.quest_middle");
			st.giveItems(MIRROR_OF_ORPIC, 1);
		}
		else if (npcId == SIR_KIEL_NIGHTHAWK && cond == 7)
		{
			htmltext = "30654-04.htm";
		}
		else if (npcId == 30654 && cond == 9 && st.getQuestItemsCount(TEAR_OF_CONFESSION) > 0)
		{
			htmltext = "30654-05.htm";
			st.takeItems(TEAR_OF_CONFESSION, 1);
			st.set("cond", "10");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == SIR_KIEL_NIGHTHAWK && cond == 10)
		{
			htmltext = "30654-06.htm";
		}
		else if (npcId == SPIRIT_OF_SIR_TALIANUS && cond == 8 && st.getQuestItemsCount(MIRROR_OF_ORPIC) > 0)
		{
			htmltext = "30656-01.htm";
			st.takeItems(MIRROR_OF_ORPIC, 1);
			st.takeItems(TALIANUSS_REPORT, 1);
			st.giveItems(TEAR_OF_CONFESSION, 1);
			st.set("cond", "9");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == ISAEL_SILVERSHADOW && cond == 10)
		{
			if (talker.getLevel() >= 35)
			{
				htmltext = "30655-02.htm";
				st.set("cond", "11");
				st.playSound("ItemSound.quest_middle");
			}
			else
				htmltext = "30655-01.htm";
		}
		else if (npcId == ISAEL_SILVERSHADOW && cond == 11)
		{
			htmltext = "30655-03.htm";
		}
		else if (npcId == ISAEL_SILVERSHADOW && cond == 12)
		{
			htmltext = "30655-04.htm";
			st.takeItems(MILITAS_ARTICLE, st.getQuestItemsCount(MILITAS_ARTICLE));
			st.giveItems(TEAR_OF_LOYALTY, 1);
			st.set("cond", "13");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == ISAEL_SILVERSHADOW && cond == 13)
		{
			htmltext = "30655-05.htm";
		}
		else if (npcId == DUSTIN && cond == 13 && st.getQuestItemsCount(TEAR_OF_LOYALTY) > 0)
		{
			htmltext = "30116-01.htm";
		}
		else if (npcId == DUSTIN && cond == 14)
		{
			htmltext = "30116-06.htm";
		}
		else if (npcId == DUSTIN && cond == 15)
		{
			htmltext = "30116-07.htm";
			st.takeItems(ATEBALTS_SKULL, 1);
			st.takeItems(ATEBALTS_RIBS, 1);
			st.takeItems(ATEBALTS_SHIN, 1);
			st.giveItems(SAINTS_ASHES_URN, 1);
			st.set("cond", "16");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == DUSTIN && cond == 17)
		{
			htmltext = "30116-08.htm";
			st.takeItems(LETTER_OF_WINDAWOOD, 1);
			st.giveItems(LETTER_OF_DUSTIN, 1);
			st.set("cond", "18");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == DUSTIN && cond == 16)
		{
			htmltext = "30116-09.htm";
		}
		else if (npcId == DUSTIN && cond == 18)
		{
			htmltext = "30116-10.htm";
		}
		else if (npcId == SIR_COLLIN_WINDAWOOD && cond == 16 && st.getQuestItemsCount(SAINTS_ASHES_URN) > 0)
		{
			htmltext = "30311-01.htm";
			st.takeItems(SAINTS_ASHES_URN, 1);
			st.giveItems(LETTER_OF_WINDAWOOD, 1);
			st.set("cond", "17");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == SIR_COLLIN_WINDAWOOD && cond == 14)
		{
			htmltext = "30311-02.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		QuestState st = killer.getQuestState(qn);
		if (st == null)
			return null;

		int cond = st.getInt("cond");
		int npcId = npc.getId();

		if (npcId == SKELETON_MARAUDER || npcId == SKELETON_RAIDER)
		{
			if (cond == 2)
			{
				if (st.getRandom(50) < 2)
				{
					st.addSpawn(SPIRIT_OF_SIR_HEROD, npc, true, 0);
					st.playSound("Itemsound.quest_before_battle");
				}
			}
		}
		else if (npcId == SPIRIT_OF_SIR_HEROD)
		{
			if (cond == 2 && st.getQuestItemsCount(OLD_KNIGHT_SWORD) > 0)
			{
				st.giveItems(KNIGHTS_TEAR, 1);
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "3");
			}
		}
		else if (npcId == STRAIN)
		{
			if (cond == 5 && st.getQuestItemsCount(REPORT_PIECE) < 10 && st.getQuestItemsCount(TALIANUSS_REPORT) == 0)
			{
				if (st.getQuestItemsCount(REPORT_PIECE) == 9)
				{
					if (st.getRandom(2) == 1)
					{
						st.takeItems(REPORT_PIECE, st.getQuestItemsCount(REPORT_PIECE));
						st.giveItems(TALIANUSS_REPORT, 1);
						st.playSound("ItemSound.quest_middle");
						st.set("cond", "6");
					}
				}
				else if (st.getRandom(2) == 1)
				{
					st.giveItems(REPORT_PIECE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == GHOUL)
		{
			if (cond == 5 && st.getQuestItemsCount(REPORT_PIECE) < 10 && st.getQuestItemsCount(TALIANUSS_REPORT) == 0)
			{
				if (st.getQuestItemsCount(REPORT_PIECE) == 9)
				{
					if (st.getRandom(2) == 1)
					{
						st.takeItems(REPORT_PIECE, st.getQuestItemsCount(REPORT_PIECE));
						st.giveItems(TALIANUSS_REPORT, 1);
						st.playSound("ItemSound.quest_middle");
						st.set("cond", "6");
					}
				}
				else if (st.getRandom(2) == 1)
				{
					st.giveItems(REPORT_PIECE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == HANGMAN_TREE)
		{
			if (cond == 7)
			{
				if (st.getRandom(100) < 33)
				{
					st.addSpawn(SPIRIT_OF_SIR_TALIANUS, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "8");
				}
			}
		}
		else if (npcId == LETO_LIZARDMAN)
		{
			if (cond == 11 && st.getQuestItemsCount(MILITAS_ARTICLE) < 20)
			{
				if (st.getQuestItemsCount(MILITAS_ARTICLE) == 19)
				{
					st.giveItems(MILITAS_ARTICLE, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "12");
				}
				else
				{
					st.giveItems(MILITAS_ARTICLE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == LETO_LIZARDMAN_ARCHER)
		{
			if (cond == 11 && st.getQuestItemsCount(MILITAS_ARTICLE) < 20)
			{
				if (st.getQuestItemsCount(MILITAS_ARTICLE) == 19)
				{
					st.giveItems(MILITAS_ARTICLE, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "12");
				}
				else
				{
					st.giveItems(MILITAS_ARTICLE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == LETO_LIZARDMAN_SOLDIER)
		{
			if (cond == 11 && st.getQuestItemsCount(MILITAS_ARTICLE) < 20)
			{
				if (st.getQuestItemsCount(MILITAS_ARTICLE) == 19)
				{
					st.giveItems(MILITAS_ARTICLE, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "12");
				}
				else
				{
					st.giveItems(MILITAS_ARTICLE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == LETO_LIZARDMAN_WARRIOR)
		{
			if (cond == 11 && st.getQuestItemsCount(MILITAS_ARTICLE) < 20)
			{
				if (st.getQuestItemsCount(MILITAS_ARTICLE) == 19)
				{
					st.giveItems(MILITAS_ARTICLE, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "12");
				}
				else
				{
					st.giveItems(MILITAS_ARTICLE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == LETO_LIZARDMAN_SHAMAN)
		{
			if (cond == 11 && st.getQuestItemsCount(MILITAS_ARTICLE) < 20)
			{
				if (st.getQuestItemsCount(MILITAS_ARTICLE) == 19)
				{
					st.giveItems(MILITAS_ARTICLE, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "12");
				}
				else
				{
					st.giveItems(MILITAS_ARTICLE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == LETO_LIZARDMAN_OVERLORD)
		{
			if (cond == 11 && st.getQuestItemsCount(MILITAS_ARTICLE) < 20)
			{
				if (st.getQuestItemsCount(MILITAS_ARTICLE) == 19)
				{
					st.giveItems(MILITAS_ARTICLE, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "12");
				}
				else
				{
					st.giveItems(MILITAS_ARTICLE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == BREKA_ORC_OVERLORD)
		{
			if (cond == 14)
			{
				if (st.getRandom(2) == 1)
				{
					if (st.getQuestItemsCount(ATEBALTS_SKULL) == 0)
					{
						st.giveItems(ATEBALTS_SKULL, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					else if (st.getQuestItemsCount(ATEBALTS_RIBS) == 0)
					{
						st.giveItems(ATEBALTS_RIBS, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					else if (st.getQuestItemsCount(ATEBALTS_SHIN) == 0)
					{
						st.giveItems(ATEBALTS_SHIN, 1);
						st.set("cond", "15");
						st.playSound("ItemSound.quest_middle");
					}
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}

	public static void main(String[] args)
	{
		new _212_TrialOfDuty(212, qn, "");
	}
}