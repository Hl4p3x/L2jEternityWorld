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
public class _411_PathToAssassin extends Quest
{
	private static final String qn = "_411_PathToAssassin";

	// Npcs
	private static final int TRISKEL = 30416;
	private static final int LEIKAN = 30382;
	private static final int ARKENIA = 30419;

	private static final int[] TALKERS =
	{
		TRISKEL,
		LEIKAN,
		ARKENIA
	};

	// Mobs
	private static final int MARSH_ZOMBIE = 20369;
	private static final int MISERY_SKELETON = 27036;

	private static final int[] KILLS =
	{
		MARSH_ZOMBIE,
		MISERY_SKELETON
	};

	// Quest Items
	private static final int SHILENS_CALL = 1245;
	private static final int ARKENIAS_LETTER = 1246;
	private static final int LEIKANS_NOTE = 1247;
	private static final int ONYX_BEASTS_MOLAR = 1248;
	private static final int SHILENS_TEARS = 1250;
	private static final int ARKENIA_RECOMMEND = 1251;

	private static final int[] QUESTITEMS =
	{
		SHILENS_CALL,
		ARKENIAS_LETTER,
		LEIKANS_NOTE,
		ONYX_BEASTS_MOLAR,
		SHILENS_TEARS,
		ARKENIA_RECOMMEND
	};

	// Reward
	private static final int IRON_HEART = 1252;
	
	public _411_PathToAssassin(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(TRISKEL);
		
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
			if ((level >= 18) && (classId == 0x1f) && (st.getQuestItemsCount(IRON_HEART) == 0))
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				st.giveItems(SHILENS_CALL, 1);
				htmltext = "30416-05.htm";
			}
			else if (classId != 0x1f)
			{
				if (classId == 0x23)
				{
					htmltext = "30416-02a.htm";
				}
				else
				{
					st.exitQuest(true);
					htmltext = "30416-02.htm";
				}
			}
			else if ((level < 18) && (classId == 0x1f))
			{
				st.exitQuest(true);
				htmltext = "30416-03.htm";
			}
			else if ((level >= 18) && (classId == 0x1f) && (st.getQuestItemsCount(IRON_HEART) == 1))
			{
				htmltext = "30416-04.htm";
			}
		}
		else if (event.equalsIgnoreCase("30419_1"))
		{
			st.giveItems(ARKENIAS_LETTER, 1);
			st.takeItems(SHILENS_CALL, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30419-05.htm";
		}
		else if (event.equalsIgnoreCase("30382_1"))
		{
			st.giveItems(LEIKANS_NOTE, 1);
			st.takeItems(ARKENIAS_LETTER, 1);
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30382-03.htm";
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
		if ((npcId != TRISKEL) && (id != State.STARTED))
		{
			return htmltext;
		}
		
		if (id == State.CREATED)
		{
			st.set("cond", "0");
			st.set("onlyone", "0");
		}
		if ((npcId == TRISKEL) && (st.getInt("cond") == 0))
		{
			htmltext = st.getQuestItemsCount(IRON_HEART) == 0 ? "30416-01.htm" : "30416-04.htm";
		}
		else if ((npcId == TRISKEL) && (st.getInt("cond") >= 1))
		{
			if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 0) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 1) && (st.getQuestItemsCount(IRON_HEART) == 0))
			{
				st.takeItems(ARKENIA_RECOMMEND, 1);
				String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
				if (isFinished.equalsIgnoreCase(""))
				{
					st.addExpAndSp(295862, 6510);
				}
				st.giveItems(IRON_HEART, 1);
				st.saveGlobalQuestVar("1ClassQuestFinished", "1");
				st.set("cond", "0");
				talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
				htmltext = "30416-06.htm";
			}
			else if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 1) && (st.getQuestItemsCount(LEIKANS_NOTE) == 0) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0))
			{
				htmltext = "30416-07.htm";
			}
			else if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 1) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0))
			{
				htmltext = "30416-08.htm";
			}
			else if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 0) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0))
			{
				htmltext = "30416-09.htm";
			}
			else if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 0) && (st.getQuestItemsCount(SHILENS_TEARS) == 1) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0))
			{
				htmltext = "30416-10.htm";
			}
			else if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 0) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 1))
			{
				htmltext = "30416-11.htm";
			}
		}
		else if ((npcId == ARKENIA) && (st.getInt("cond") >= 1))
		{
			if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 0) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 1))
			{
				htmltext = "30419-01.htm";
			}
			else if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 1) && (st.getQuestItemsCount(LEIKANS_NOTE) == 0) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0))
			{
				htmltext = "30419-07.htm";
			}
			else if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 0) && (st.getQuestItemsCount(SHILENS_TEARS) == 1) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0))
			{
				st.giveItems(ARKENIA_RECOMMEND, 1);
				st.takeItems(SHILENS_TEARS, 1);
				st.set("cond", "7");
				st.playSound("ItemSound.quest_middle");
				htmltext = "30419-08.htm";
			}
			else if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 0) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 1) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0))
			{
				htmltext = "30419-09.htm";
			}
			else if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 1) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0))
			{
				htmltext = "30419-10.htm";
			}
			else if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 0) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0))
			{
				htmltext = "30419-11.htm";
			}
		}
		else if ((npcId == LEIKAN) && (st.getInt("cond") >= 1))
		{
			if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 1) && (st.getQuestItemsCount(LEIKANS_NOTE) == 0) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0) && (st.getQuestItemsCount(ONYX_BEASTS_MOLAR) == 0))
			{
				htmltext = "30382-01.htm";
			}
			else if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 1) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0) && (st.getQuestItemsCount(ONYX_BEASTS_MOLAR) == 0))
			{
				htmltext = "30382-05.htm";
			}
			else if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 1) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0) && (st.getQuestItemsCount(ONYX_BEASTS_MOLAR) < 10))
			{
				htmltext = "30382-06.htm";
			}
			else if ((st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 1) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0) && (st.getQuestItemsCount(ONYX_BEASTS_MOLAR) >= 10))
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
				st.takeItems(ONYX_BEASTS_MOLAR, 10);
				st.takeItems(LEIKANS_NOTE, 1);
				htmltext = "30382-07.htm";
			}
			else if (st.getQuestItemsCount(SHILENS_TEARS) == 1)
			{
				htmltext = "30382-08.htm";
			}
			else if ((st.getInt("cond") >= 1) && (st.getQuestItemsCount(ARKENIAS_LETTER) == 0) && (st.getQuestItemsCount(LEIKANS_NOTE) == 0) && (st.getQuestItemsCount(SHILENS_TEARS) == 0) && (st.getQuestItemsCount(ARKENIA_RECOMMEND) == 0) && (st.getQuestItemsCount(IRON_HEART) == 0) && (st.getQuestItemsCount(SHILENS_CALL) == 0) && (st.getQuestItemsCount(ONYX_BEASTS_MOLAR) == 0))
			{
				htmltext = "30382-09.htm";
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
		if (npcId == MISERY_SKELETON)
		{
			if ((st.getInt("cond") >= 1) && (st.getQuestItemsCount(SHILENS_TEARS) == 0))
			{
				st.giveItems(SHILENS_TEARS, 1);
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "6");
			}
		}
		else if (npcId == MARSH_ZOMBIE)
		{
			if ((st.getInt("cond") >= 1) && (st.getQuestItemsCount(LEIKANS_NOTE) == 1) && (st.getQuestItemsCount(ONYX_BEASTS_MOLAR) < 10))
			{
				st.giveItems(ONYX_BEASTS_MOLAR, 1);
				if (st.getQuestItemsCount(ONYX_BEASTS_MOLAR) == 10)
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "4");
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
		new _411_PathToAssassin(411, qn, "");
	}
}