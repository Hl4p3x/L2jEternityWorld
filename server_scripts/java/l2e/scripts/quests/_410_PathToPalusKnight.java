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

/**
 * Created by LordWinter 26.09.2012
 * Based on L2J Eternity-World
 */
public class _410_PathToPalusKnight extends Quest
{
	private static final String qn = "_410_PathToPalusKnight";

	// Npcs
	private static final int VIRGIL = 30329;
	private static final int KALINTA = 30422;

	private static final int[] TALKERS =
	{
		VIRGIL,
		KALINTA
	};

	// Mobs
	private static final int VENOMOUS_SPIDER = 20038;
	private static final int ARACHNID_TRACKER = 20043;
	private static final int LYCANTHROPE = 20049;

	private static final int[] KILLS =
	{
		VENOMOUS_SPIDER,
		ARACHNID_TRACKER,
		LYCANTHROPE
	};

	// Quest Items
	private static final int PALLUS_TALISMAN = 1237;
	private static final int LYCANTHROPE_SKULL = 1238;
	private static final int VIRGILS_LETTER = 1239;
	private static final int MORTE_TALISMAN = 1240;
	private static final int PREDATOR_CARAPACE = 1241;
	private static final int TRIMDEN_SILK = 1242;
	private static final int COFFIN_ETERNAL_REST = 1243;

	private static final int[] QUESTITEMS =
	{
		PALLUS_TALISMAN,
		LYCANTHROPE_SKULL,
		VIRGILS_LETTER,
		MORTE_TALISMAN,
		PREDATOR_CARAPACE,
		TRIMDEN_SILK,
		COFFIN_ETERNAL_REST
	};

	// Reward
	private static final int GAZE_OF_ABYSS = 1244;
	
	public _410_PathToPalusKnight(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(VIRGIL);
		
		for (int talkId : TALKERS)
		{
			addTalkId(talkId);
		}
		
		for (int killId : KILLS)
		{
			addKillId(killId);
		}
		
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
		
		int level = player.getLevel();
		int classId = player.getClassId().getId();
		if (event.equalsIgnoreCase("1"))
		{
			st.set("id", "0");
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(PALLUS_TALISMAN, 1);
			htmltext = "30329-06.htm";
		}
		else if (event.equalsIgnoreCase("410_1"))
		{
			if ((level >= 18) && (classId == 0x1f) && (st.getQuestItemsCount(GAZE_OF_ABYSS) == 0))
			{
				htmltext = "30329-05.htm";
			}
			else if (classId != 0x1f)
			{
				htmltext = classId == 0x20 ? "30329-02a.htm" : "30329-03.htm";
			}
			else if ((level < 18) && (classId == 0x1f))
			{
				htmltext = "30329-02.htm";
			}
			else if ((level >= 18) && (classId == 0x1f) && (st.getQuestItemsCount(GAZE_OF_ABYSS) == 1))
			{
				htmltext = "30329-04.htm";
			}
		}
		else if (event.equalsIgnoreCase("30329_2"))
		{
			st.takeItems(PALLUS_TALISMAN, 1);
			st.takeItems(LYCANTHROPE_SKULL, st.getQuestItemsCount(LYCANTHROPE_SKULL));
			st.giveItems(VIRGILS_LETTER, 1);
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30329-10.htm";
		}
		else if (event.equalsIgnoreCase("30422_1"))
		{
			st.takeItems(VIRGILS_LETTER, 1);
			st.giveItems(MORTE_TALISMAN, 1);
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30422-02.htm";
		}
		else if (event.equalsIgnoreCase("30422_2"))
		{
			st.takeItems(MORTE_TALISMAN, 1);
			st.takeItems(TRIMDEN_SILK, st.getQuestItemsCount(TRIMDEN_SILK));
			st.takeItems(PREDATOR_CARAPACE, st.getQuestItemsCount(PREDATOR_CARAPACE));
			st.giveItems(COFFIN_ETERNAL_REST, 1);
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30422-06.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = Quest.getNoQuestMsg(talker);
		QuestState st = talker.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		int id = st.getState();
		if ((npcId != VIRGIL) && (id != State.STARTED))
		{
			return htmltext;
		}
		
		if ((npcId == VIRGIL) && (st.getInt("cond") == 0))
		{
			htmltext = "30329-01.htm";
		}
		else if ((npcId == VIRGIL) && (st.getInt("cond") > 0))
		{
			if ((st.getQuestItemsCount(PALLUS_TALISMAN) == 1) && (st.getQuestItemsCount(LYCANTHROPE_SKULL) == 0))
			{
				htmltext = "30329-07.htm";
			}
			else if ((st.getQuestItemsCount(PALLUS_TALISMAN) == 1) && (st.getQuestItemsCount(LYCANTHROPE_SKULL) > 0) && (st.getQuestItemsCount(LYCANTHROPE_SKULL) < 13))
			{
				htmltext = "30329-08.htm";
			}
			else if ((st.getQuestItemsCount(PALLUS_TALISMAN) == 1) && (st.getQuestItemsCount(LYCANTHROPE_SKULL) >= 13))
			{
				htmltext = "30329-09.htm";
			}
			else if (st.getQuestItemsCount(COFFIN_ETERNAL_REST) == 1)
			{
				st.takeItems(COFFIN_ETERNAL_REST, 1);
				String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
				if (isFinished.equalsIgnoreCase(""))
				{
					st.addExpAndSp(295862, 5050);
				}
				st.giveItems(GAZE_OF_ABYSS, 1);
				st.saveGlobalQuestVar("1ClassQuestFinished", "1");
				st.set("cond", "0");
				talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
				htmltext = "30329-11.htm";
			}
			else if ((st.getQuestItemsCount(MORTE_TALISMAN) > 0) || (st.getQuestItemsCount(VIRGILS_LETTER) > 0))
			{
				htmltext = "30329-12.htm";
			}
		}
		else if ((npcId == KALINTA) && (st.getInt("cond") > 0))
		{
			if (st.getQuestItemsCount(VIRGILS_LETTER) > 0)
			{
				htmltext = "30422-01.htm";
			}
			else if ((st.getQuestItemsCount(MORTE_TALISMAN) > 0) && (st.getQuestItemsCount(TRIMDEN_SILK) == 0) && (st.getQuestItemsCount(PREDATOR_CARAPACE) == 0))
			{
				htmltext = "30422-03.htm";
			}
			else if ((st.getQuestItemsCount(MORTE_TALISMAN) > 0) && (st.getQuestItemsCount(TRIMDEN_SILK) > 0) && (st.getQuestItemsCount(PREDATOR_CARAPACE) == 0))
			{
				htmltext = "30422-04.htm";
			}
			else if ((st.getQuestItemsCount(MORTE_TALISMAN) > 0) && (st.getQuestItemsCount(TRIMDEN_SILK) == 0) && (st.getQuestItemsCount(PREDATOR_CARAPACE) > 0))
			{
				htmltext = "30422-04.htm";
			}
			else if ((st.getQuestItemsCount(MORTE_TALISMAN) > 0) && (st.getQuestItemsCount(TRIMDEN_SILK) >= 5) && (st.getQuestItemsCount(PREDATOR_CARAPACE) > 0))
			{
				htmltext = "30422-05.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		QuestState st = killer.getQuestState(qn);
		
		if (st == null)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		if (st.getState() != State.STARTED)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		int npcId = npc.getId();
		if (npcId == LYCANTHROPE)
		{
			st.set("id", "0");
			if ((st.getInt("cond") > 0) && (st.getQuestItemsCount(PALLUS_TALISMAN) == 1) && (st.getQuestItemsCount(LYCANTHROPE_SKULL) < 13))
			{
				st.giveItems(LYCANTHROPE_SKULL, 1);
				if (st.getQuestItemsCount(LYCANTHROPE_SKULL) == 13)
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "2");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == VENOMOUS_SPIDER)
		{
			st.set("id", "0");
			if ((st.getInt("cond") > 0) && (st.getQuestItemsCount(MORTE_TALISMAN) == 1) && (st.getQuestItemsCount(PREDATOR_CARAPACE) < 1))
			{
				st.giveItems(PREDATOR_CARAPACE, 1);
				st.playSound("ItemSound.quest_middle");
				if ((st.getQuestItemsCount(TRIMDEN_SILK) >= 5) && (st.getQuestItemsCount(PREDATOR_CARAPACE) > 0))
				{
					st.set("cond", "5");
				}
			}
		}
		else if (npcId == ARACHNID_TRACKER)
		{
			st.set("id", "0");
			if ((st.getInt("cond") > 0) && (st.getQuestItemsCount(MORTE_TALISMAN) == 1) && (st.getQuestItemsCount(TRIMDEN_SILK) < 5))
			{
				st.giveItems(TRIMDEN_SILK, 1);
				if (st.getQuestItemsCount(TRIMDEN_SILK) == 5)
				{
					st.playSound("ItemSound.quest_middle");
					if ((st.getQuestItemsCount(TRIMDEN_SILK) >= 5) && (st.getQuestItemsCount(PREDATOR_CARAPACE) > 0))
					{
						st.set("cond", "5");
					}
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _410_PathToPalusKnight(410, qn, "");
	}
}