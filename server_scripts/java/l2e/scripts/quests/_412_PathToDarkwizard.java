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
 * Created by LordWinter 26.09.2012
 * Based on L2J Eternity-World
 */
public class _412_PathToDarkwizard extends Quest
{	
	private static final String qn = "_412_PathToDarkwizard";
	
	// Npcs
	private static final int VARIKA = 30421;
	private static final int CHARKEREN = 30415;
	private static final int ANNIKA = 30418;
	private static final int ARKENIA = 30419;
	
	private static final int[] TALKERS =
	{
		VARIKA,
		CHARKEREN,
		ANNIKA,
		ARKENIA
	};
	
	// Mobs
	private static final int MARSH_ZOMBIE = 20015;
	private static final int MISERY_SKELETON = 20022;
	private static final int SKELETON_SCOUT = 20045;
	private static final int SKELETON_HUNTER = 20517;
	private static final int SKELETON_HUNTER_ARCHER = 20518;
	
	private static final int[] KILLS =
	{
		MARSH_ZOMBIE,
		MISERY_SKELETON,
		SKELETON_SCOUT,
		SKELETON_HUNTER,
		SKELETON_HUNTER_ARCHER
	};
	
	// Quest Items
	private static final int SEEDS_OF_ANGER = 1253;
	private static final int SEEDS_OF_DESPAIR = 1254;
	private static final int SEEDS_OF_HORROR = 1255;
	private static final int SEEDS_OF_LUNACY = 1256;
	private static final int FAMILYS_ASHES = 1257;
	private static final int KNEE_BONE = 1259;
	private static final int HEART_OF_LUNACY = 1260;
	private static final int LUCKY_KEY = 1277;
	private static final int CANDLE = 1278;
	private static final int HUB_SCENT = 1279;
	
	private static final int[] QUESTITEMS =
	{
		SEEDS_OF_ANGER,
		SEEDS_OF_DESPAIR,
		SEEDS_OF_HORROR,
		SEEDS_OF_LUNACY,
		FAMILYS_ASHES,
		KNEE_BONE,
		HEART_OF_LUNACY,
		LUCKY_KEY,
		CANDLE,
		HUB_SCENT
	};
	
	// Reward
	private static final int JEWEL_OF_DARKNESS = 1261;
	
	public _412_PathToDarkwizard(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(VARIKA);
		
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
			if (st.getInt("cond") == 0)
			{
				if ((level >= 18) && (classId == 0x26) && (st.getQuestItemsCount(JEWEL_OF_DARKNESS) == 0))
				{
					st.set("cond", "1");
					st.setState(State.STARTED);
					st.playSound("ItemSound.quest_accept");
					st.giveItems(SEEDS_OF_DESPAIR, 1);
					htmltext = "30421-05.htm";
				}
				else if (classId != 0x26)
				{
					htmltext = classId == 0x27 ? "30421-02a.htm" : "30421-03.htm";
				}
				else if ((level < 18) && (classId == 0x26))
				{
					htmltext = "30421-02.htm";
				}
				else if ((level >= 18) && (classId == 0x26) && (st.getQuestItemsCount(JEWEL_OF_DARKNESS) == 1))
				{
					htmltext = "30421-04.htm";
				}
			}
		}
		else if (event.equalsIgnoreCase("412_1"))
		{
			htmltext = st.getQuestItemsCount(SEEDS_OF_ANGER) > 0 ? "30421-06.htm" : "30421-07.htm";
		}
		else if (event.equalsIgnoreCase("412_2"))
		{
			htmltext = st.getQuestItemsCount(SEEDS_OF_HORROR) > 0 ? "30421-09.htm" : "30421-10.htm";
		}
		else if (event.equalsIgnoreCase("412_3"))
		{
			if (st.getQuestItemsCount(SEEDS_OF_LUNACY) > 0)
			{
				htmltext = "30421-12.htm";
			}
			else if ((st.getQuestItemsCount(SEEDS_OF_LUNACY) == 0) && (st.getQuestItemsCount(SEEDS_OF_DESPAIR) > 0))
			{
				st.giveItems(HUB_SCENT, 1);
				htmltext = "30421-13.htm";
			}
		}
		else if (event.equalsIgnoreCase("412_4"))
		{
			st.giveItems(LUCKY_KEY, 1);
			htmltext = "30415-03.htm";
		}
		else if (event.equalsIgnoreCase("30418_1"))
		{
			st.giveItems(CANDLE, 1);
			htmltext = "30418-02.htm";
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
		int cond = st.getInt("cond");
		if ((npcId != VARIKA) && (id != State.STARTED))
		{
			return htmltext;
		}
		
		if ((npcId == VARIKA) && (cond == 0))
		{
			htmltext = st.getQuestItemsCount(JEWEL_OF_DARKNESS) == 0 ? "30421-01.htm" : "30421-04.htm";
		}
		else if ((npcId == VARIKA) && (cond == 1))
		{
			if ((st.getQuestItemsCount(SEEDS_OF_DESPAIR) > 0) && (st.getQuestItemsCount(SEEDS_OF_HORROR) > 0) && (st.getQuestItemsCount(SEEDS_OF_LUNACY) > 0) && (st.getQuestItemsCount(SEEDS_OF_ANGER) > 0))
			{
				st.takeItems(SEEDS_OF_HORROR, 1);
				st.takeItems(SEEDS_OF_ANGER, 1);
				st.takeItems(SEEDS_OF_LUNACY, 1);
				st.takeItems(SEEDS_OF_DESPAIR, 1);
				String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
				if (isFinished.equalsIgnoreCase(""))
				{
					st.addExpAndSp(295862, 5210);
				}
				st.giveItems(JEWEL_OF_DARKNESS, 1);
				st.saveGlobalQuestVar("1ClassQuestFinished", "1");
				st.set("cond", "0");
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
				htmltext = "30421-16.htm";
			}
			else if ((st.getQuestItemsCount(SEEDS_OF_DESPAIR) == 1) && (st.getQuestItemsCount(FAMILYS_ASHES) == 0) && (st.getQuestItemsCount(LUCKY_KEY) == 0) && (st.getQuestItemsCount(CANDLE) == 0) && (st.getQuestItemsCount(HUB_SCENT) == 0) && (st.getQuestItemsCount(KNEE_BONE) == 0) && (st.getQuestItemsCount(HEART_OF_LUNACY) == 0))
			{
				htmltext = "30421-17.htm";
			}
			else if ((st.getQuestItemsCount(SEEDS_OF_DESPAIR) == 1) && (st.getInt("id") == 1) && (st.getQuestItemsCount(SEEDS_OF_ANGER) == 0))
			{
				htmltext = "30421-08.htm";
			}
			else if ((st.getQuestItemsCount(SEEDS_OF_DESPAIR) == 1) && (st.getInt("id") == 2) && (st.getQuestItemsCount(SEEDS_OF_HORROR) > 0))
			{
				htmltext = "30421-19.htm";
			}
			else if ((st.getQuestItemsCount(SEEDS_OF_DESPAIR) == 1) && (st.getInt("id") == 3) && (st.getQuestItemsCount(HEART_OF_LUNACY) == 0))
			{
				htmltext = "30421-13.htm";
			}
		}
		else if ((npcId == ARKENIA) && (cond == 1))
		{
			if ((st.getQuestItemsCount(HUB_SCENT) == 0) && (st.getQuestItemsCount(HEART_OF_LUNACY) == 0))
			{
				st.giveItems(HUB_SCENT, 1);
				htmltext = "30419-01.htm";
			}
			else if ((st.getQuestItemsCount(HUB_SCENT) > 0) && (st.getQuestItemsCount(HEART_OF_LUNACY) < 3))
			{
				htmltext = "30419-02.htm";
			}
			else if ((st.getQuestItemsCount(HUB_SCENT) > 0) && (st.getQuestItemsCount(HEART_OF_LUNACY) >= 3))
			{
				st.giveItems(SEEDS_OF_LUNACY, 1);
				st.takeItems(HEART_OF_LUNACY, 3);
				st.takeItems(HUB_SCENT, 1);
				htmltext = "30419-03.htm";
			}
		}
		else if ((npcId == CHARKEREN) && (cond == 1) && (st.getQuestItemsCount(SEEDS_OF_ANGER) == 0))
		{
			if ((st.getQuestItemsCount(SEEDS_OF_DESPAIR) == 1) && (st.getQuestItemsCount(FAMILYS_ASHES) == 0) && (st.getQuestItemsCount(LUCKY_KEY) == 0))
			{
				htmltext = "30415-01.htm";
			}
			else if ((st.getQuestItemsCount(SEEDS_OF_DESPAIR) == 1) && (st.getQuestItemsCount(FAMILYS_ASHES) < 3) && (st.getQuestItemsCount(LUCKY_KEY) == 1))
			{
				htmltext = "30415-04.htm";
			}
			else if ((st.getQuestItemsCount(SEEDS_OF_DESPAIR) == 1) && (st.getQuestItemsCount(FAMILYS_ASHES) >= 3) && (st.getQuestItemsCount(LUCKY_KEY) == 1))
			{
				st.giveItems(SEEDS_OF_ANGER, 1);
				st.takeItems(FAMILYS_ASHES, 3);
				st.takeItems(LUCKY_KEY, 1);
				htmltext = "30415-05.htm";
			}
		}
		else if ((npcId == CHARKEREN) && (cond == 1) && (st.getQuestItemsCount(SEEDS_OF_ANGER) == 1))
		{
			htmltext = "30415-06.htm";
		}
		else if ((npcId == ANNIKA) && (cond > 0) && (st.getQuestItemsCount(SEEDS_OF_HORROR) == 0))
		{
			if ((st.getQuestItemsCount(SEEDS_OF_DESPAIR) == 1) && (st.getQuestItemsCount(CANDLE) == 0) && (st.getQuestItemsCount(KNEE_BONE) == 0))
			{
				htmltext = "30418-01.htm";
			}
			else if ((st.getQuestItemsCount(SEEDS_OF_DESPAIR) == 1) && (st.getQuestItemsCount(CANDLE) == 1) && (st.getQuestItemsCount(KNEE_BONE) < 2))
			{
				htmltext = "30418-03.htm";
			}
			else if ((st.getQuestItemsCount(SEEDS_OF_DESPAIR) == 1) && (st.getQuestItemsCount(CANDLE) == 1) && (st.getQuestItemsCount(KNEE_BONE) >= 2))
			{
				st.giveItems(SEEDS_OF_HORROR, 1);
				st.takeItems(CANDLE, 1);
				st.takeItems(KNEE_BONE, 2);
				htmltext = "30418-04.htm";
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
		int cond = st.getInt("cond");
		if (npcId == MARSH_ZOMBIE)
		{
			st.set("id", "0");
			if ((cond == 1) && (st.getQuestItemsCount(LUCKY_KEY) == 1) && (st.getQuestItemsCount(FAMILYS_ASHES) < 3))
			{
				if (st.getRandom(2) == 0)
				{
					st.giveItems(FAMILYS_ASHES, 1);
					st.playSound(st.getQuestItemsCount(FAMILYS_ASHES) == 3 ? ("ItemSound.quest_middle") : ("ItemSound.quest_itemget"));
				}
			}
		}
		else if (npcId == SKELETON_HUNTER)
		{
			st.set("id", "0");
			if ((cond == 1) && (st.getQuestItemsCount(CANDLE) == 1) && (st.getQuestItemsCount(KNEE_BONE) < 2))
			{
				if (st.getRandom(2) == 0)
				{
					st.giveItems(KNEE_BONE, 1);
					st.playSound(st.getQuestItemsCount(KNEE_BONE) == 2 ? ("ItemSound.quest_middle") : ("ItemSound.quest_itemget"));
				}
			}
		}
		else if (npcId == SKELETON_HUNTER_ARCHER)
		{
			st.set("id", "0");
			if ((cond == 1) && (st.getQuestItemsCount(CANDLE) == 1) && (st.getQuestItemsCount(KNEE_BONE) < 2))
			{
				if (st.getRandom(2) == 0)
				{
					st.giveItems(KNEE_BONE, 1);
					st.playSound(st.getQuestItemsCount(KNEE_BONE) == 2 ? ("ItemSound.quest_middle") : ("ItemSound.quest_itemget"));
				}
			}
		}
		else if (npcId == MISERY_SKELETON)
		{
			st.set("id", "0");
			if ((cond == 1) && (st.getQuestItemsCount(CANDLE) == 1) && (st.getQuestItemsCount(KNEE_BONE) < 2))
			{
				if (st.getRandom(2) == 0)
				{
					st.giveItems(KNEE_BONE, 1);
					st.playSound(st.getQuestItemsCount(KNEE_BONE) == 2 ? ("ItemSound.quest_middle") : ("ItemSound.quest_itemget"));
				}
			}
		}
		else if (npcId == SKELETON_SCOUT)
		{
			st.set("id", "0");
			if ((cond == 1) && (st.getQuestItemsCount(HUB_SCENT) == 1) && (st.getQuestItemsCount(HEART_OF_LUNACY) < 3))
			{
				if (st.getRandom(2) == 0)
				{
					st.giveItems(HEART_OF_LUNACY, 1);
					st.playSound(st.getQuestItemsCount(HEART_OF_LUNACY) == 3 ? ("ItemSound.quest_middle") : ("ItemSound.quest_itemget"));
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _412_PathToDarkwizard(412, qn, "");
	}
}