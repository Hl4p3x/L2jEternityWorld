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
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.util.Util;

/**
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public class _215_TrialOfPilgrim extends Quest
{
	private static final String qn = "_215_TrialOfPilgrim";

	// Npc
	private static final int SANTIAGO = 30648;
	private static final int PETRON = 30036;
	private static final int PRIMOS = 30117;
	private static final int ANDELLIA = 30362;
	private static final int GAURI_TWINKLEROCK = 30550;
	private static final int TANAPI = 30571;
	private static final int CASIAN = 30612;
	private static final int ANCESTOR_MARTANKUS = 30649;
	private static final int GERALD = 30650;
	private static final int DORF = 30651;
	private static final int URUHA = 30652;

	private static final int[] TALKERS =
	{
		SANTIAGO, PETRON, PRIMOS, ANDELLIA, GAURI_TWINKLEROCK, TANAPI, CASIAN, ANCESTOR_MARTANKUS, GERALD, DORF, URUHA
	};

	// Mobs
	private static final int LAVA_SALAMANDER = 27116;
	private static final int NAHIR = 27117;
	private static final int BLACK_WILLOW = 27118;

	private static final int[] MOBS = { LAVA_SALAMANDER, NAHIR, BLACK_WILLOW };

	// Quest items
	private static final int BOOK_OF_SAGE = 2722;
	private static final int VOUCHER_OF_TRIAL = 2723;
	private static final int SPIRIT_OF_FLAME = 2724;
	private static final int ESSENSE_OF_FLAME = 2725;
	private static final int BOOK_OF_GERALD = 2726;
	private static final int GREY_BADGE = 2727;
	private static final int PICTURE_OF_NAHIR = 2728;
	private static final int HAIR_OF_NAHIR = 2729;
	private static final int STATUE_OF_EINHASAD = 2730;
	private static final int BOOK_OF_DARKNESS = 2731;
	private static final int DEBRIS_OF_WILLOW = 2732;
	private static final int TAG_OF_RUMOR = 2733;

	private static final int[] QUESTITEMS =
	{
		BOOK_OF_SAGE, VOUCHER_OF_TRIAL, SPIRIT_OF_FLAME, ESSENSE_OF_FLAME, BOOK_OF_GERALD, GREY_BADGE,
		PICTURE_OF_NAHIR, HAIR_OF_NAHIR, STATUE_OF_EINHASAD, BOOK_OF_DARKNESS, DEBRIS_OF_WILLOW, TAG_OF_RUMOR
	};

	// Reward
	private static final int MARK_OF_PILGRIM = 2721;

	// Allowed classes
	private static final int[] CLASSES = { 0x0f, 0x1d, 0x2a, 0x32 };

	public _215_TrialOfPilgrim(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(SANTIAGO);

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
			htmltext = "30648-04.htm";
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(VOUCHER_OF_TRIAL, 1);
		}
		else if (event.equalsIgnoreCase("30648_1"))
		{
			htmltext = "30648-05.htm";
		}
		else if (event.equalsIgnoreCase("30648_2"))
		{
			htmltext = "30648-06.htm";
		}
		else if (event.equalsIgnoreCase("30648_3"))
		{
			htmltext = "30648-07.htm";
		}
		else if (event.equalsIgnoreCase("30648_4"))
		{
			htmltext = "30648-08.htm";
		}
		else if (event.equalsIgnoreCase("30648_5"))
		{
			htmltext = "30648-05.htm";
		}
		else if (event.equalsIgnoreCase("30649_1"))
		{
			htmltext = "30649-04.htm";
			st.giveItems(SPIRIT_OF_FLAME, 1);
			st.takeItems(ESSENSE_OF_FLAME, 1);
			st.set("cond", "5");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30650_1"))
		{
			if (st.getQuestItemsCount(57) >= 100000)
			{
				htmltext = "30650-02.htm";
				st.giveItems(BOOK_OF_GERALD, 1);
				st.takeItems(57, 100000);
				st.set("cond", "8");
				st.playSound("ItemSound.quest_middle");
			}
			else
				htmltext = "30650-03.htm";
		}
		else if (event.equalsIgnoreCase("30650_2"))
		{
			htmltext = "30650-03.htm";
		}
		else if (event.equalsIgnoreCase("30362_1"))
		{
			htmltext = "30362-05.htm";
			st.takeItems(BOOK_OF_DARKNESS, 1);
			st.set("cond", "16");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30362_2"))
		{
			htmltext = "30362-04.htm";
			st.set("cond", "16");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30652_1"))
		{
			htmltext = "30652-02.htm";
			st.giveItems(BOOK_OF_DARKNESS, 1);
			st.takeItems(DEBRIS_OF_WILLOW, 1);
			st.set("cond", "15");
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

		int npcId = npc.getId();
		int id = st.getState();

		if (npcId != SANTIAGO && id != State.STARTED)
			return htmltext;

		int cond = st.getInt("cond");
		if (npcId == SANTIAGO && cond == 0 && id == State.CREATED)
		{
			if (Util.contains(CLASSES, talker.getClassId().getId()))
			{
				if (talker.getLevel() >= 35)
					htmltext = "30648-03.htm";
				else
				{
					htmltext = "30648-01.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				htmltext = "30648-02.htm";
				st.exitQuest(true);
			}
		}
		else if (npcId == SANTIAGO && cond == 0 && id == State.COMPLETED)
		{
			htmltext = Quest.getAlreadyCompletedMsg(talker);
		}
		else if (npcId == SANTIAGO && cond == 1 && st.getQuestItemsCount(VOUCHER_OF_TRIAL) > 0)
		{
			htmltext = "30648-09.htm";
		}
		else if (npcId == SANTIAGO && cond == 17 && st.getQuestItemsCount(BOOK_OF_SAGE) > 0)
		{
			htmltext = "30648-10.htm";
			st.unset("cond");
			st.takeItems(BOOK_OF_SAGE, 1);
			st.addExpAndSp(629125, 40803);
			st.giveItems(57, 114649);
			st.giveItems(7562, 49);
			st.giveItems(MARK_OF_PILGRIM, 1);
			talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		else if (npcId == TANAPI && cond == 1 && st.getQuestItemsCount(VOUCHER_OF_TRIAL) > 0)
		{
			htmltext = "30571-01.htm";
			st.takeItems(VOUCHER_OF_TRIAL, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == TANAPI && cond == 2)
		{
			htmltext = "30571-02.htm";
		}
		else if (npcId == TANAPI && (cond == 5 || cond == 6) && st.getQuestItemsCount(SPIRIT_OF_FLAME) > 0)
		{
			htmltext = "30571-03.htm";
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == ANCESTOR_MARTANKUS && cond == 2)
		{
			htmltext = "30649-01.htm";
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == ANCESTOR_MARTANKUS && cond == 3)
		{
			htmltext = "30649-02.htm";
		}
		else if (npcId == ANCESTOR_MARTANKUS && cond == 4 && st.getQuestItemsCount(ESSENSE_OF_FLAME) > 0)
		{
			htmltext = "30649-03.htm";
		}
		else if (npcId == GAURI_TWINKLEROCK && cond == 6 && st.getQuestItemsCount(SPIRIT_OF_FLAME) > 0)
		{
			htmltext = "30550-01.htm";
			st.giveItems(TAG_OF_RUMOR, 1);
			st.set("cond", "7");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == GAURI_TWINKLEROCK && cond == 7)
		{
			htmltext = "30550-02.htm";
		}
		else if (npcId == GERALD && cond == 7 && st.getQuestItemsCount(TAG_OF_RUMOR) > 0)
		{
			htmltext = st.showHtmlFile("30650-01.htm").replace("RequiredAdena", String.valueOf(100000));
		}
		else if (npcId == GERALD && cond >= 9 && st.getQuestItemsCount(GREY_BADGE) > 0 && st.getQuestItemsCount(BOOK_OF_GERALD) > 0)
		{
			htmltext = "30650-04.htm";
			st.giveItems(57, 100000);
			st.takeItems(BOOK_OF_GERALD, 1);
		}
		else if (npcId == DORF && cond == 7 && st.getQuestItemsCount(TAG_OF_RUMOR) > 0)
		{
			htmltext = "30651-01.htm";
			st.giveItems(GREY_BADGE, 1);
			st.takeItems(TAG_OF_RUMOR, 1);
			st.set("cond", "9");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == DORF && cond == 8 && st.getQuestItemsCount(TAG_OF_RUMOR) > 0)
		{
			htmltext = "30651-02.htm";
			st.giveItems(GREY_BADGE, 1);
			st.takeItems(TAG_OF_RUMOR, 1);
			st.set("cond", "9");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == DORF && cond == 9)
		{
			htmltext = "30651-03.htm";
		}
		else if (npcId == PRIMOS && cond == 8)
		{
			htmltext = "30117-01.htm";
			st.set("cond", "9");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == PRIMOS && cond == 9)
		{
			htmltext = "30117-02.htm";
		}
		else if (npcId == PETRON && cond == 9)
		{
			htmltext = "30036-01.htm";
			st.giveItems(PICTURE_OF_NAHIR, 1);
			st.set("cond", "10");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == PETRON && cond == 10)
		{
			htmltext = "30036-02.htm";
		}
		else if (npcId == PETRON && cond == 11)
		{
			htmltext = "30036-03.htm";
			st.giveItems(STATUE_OF_EINHASAD, 1);
			st.takeItems(PICTURE_OF_NAHIR, 1);
			st.takeItems(HAIR_OF_NAHIR, 1);
			st.set("cond", "12");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == PETRON && cond == 12 && st.getQuestItemsCount(STATUE_OF_EINHASAD) > 0)
		{
			htmltext = "30036-04.htm";
		}
		else if (npcId == ANDELLIA && cond == 12)
		{
			htmltext = "30362-01.htm";
			st.set("cond", "13");
			st.playSound("ItemSound.quest_middle");
		}
		else if (npcId == ANDELLIA && cond == 13)
		{
			htmltext = "30362-02.htm";
		}
		else if (npcId == ANDELLIA && cond == 15 && st.getQuestItemsCount(BOOK_OF_DARKNESS) > 0)
		{
			htmltext = "30362-03.htm";
		}
		else if (npcId == ANDELLIA && cond == 16)
		{
			htmltext = "30362-06.htm";
		}
		else if (npcId == ANDELLIA && cond == 15 && st.getQuestItemsCount(BOOK_OF_DARKNESS) == 0)
		{
			htmltext = "30362-07.htm";
		}
		else if (npcId == URUHA && cond == 14 && st.getQuestItemsCount(DEBRIS_OF_WILLOW) > 0)
		{
			htmltext = "30652-01.htm";
		}
		else if (npcId == URUHA && cond == 15 && st.getQuestItemsCount(BOOK_OF_DARKNESS) > 0)
		{
			htmltext = "30652-03.htm";
		}
		else if (npcId == CASIAN && cond == 16)
		{
			htmltext = "30612-01.htm";
			st.giveItems(BOOK_OF_SAGE, 1);
			if (st.getQuestItemsCount(BOOK_OF_DARKNESS) > 0)
				st.takeItems(BOOK_OF_DARKNESS, 1);
			st.set("cond", "17");
			st.playSound("ItemSound.quest_middle");
			st.takeItems(GREY_BADGE, 1);
			st.takeItems(SPIRIT_OF_FLAME, 1);
			st.takeItems(STATUE_OF_EINHASAD, 1);
		}
		else if (npcId == CASIAN && cond == 17)
		{
			htmltext = "30612-02.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		QuestState st = killer.getQuestState(qn);
		if (st == null)
			return null;

		int npcId = npc.getId();
		int cond = st.getInt("cond");

		if (npcId == LAVA_SALAMANDER)
		{
			if (cond == 3 && st.getQuestItemsCount(ESSENSE_OF_FLAME) == 0)
			{
				if (st.getRandom(5) == 0)
				{
					st.giveItems(ESSENSE_OF_FLAME, 1);
					st.set("cond", "4");
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		else if (npcId == NAHIR)
		{
			if (cond == 10 && st.getQuestItemsCount(HAIR_OF_NAHIR) == 0)
			{
				st.giveItems(HAIR_OF_NAHIR, 1);
				st.set("cond", "11");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npcId == BLACK_WILLOW)
		{
			if (cond == 13 && st.getQuestItemsCount(DEBRIS_OF_WILLOW) == 0)
			{
				if (st.getRandom(5) == 0)
				{
					st.giveItems(DEBRIS_OF_WILLOW, 1);
					st.set("cond", "14");
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}

	public static void main(String[] args)
	{
		new _215_TrialOfPilgrim(215, qn, "");
	}
}