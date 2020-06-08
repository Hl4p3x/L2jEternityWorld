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
 * Created by LordWinter 02.10.2012
 * Based on L2J Eternity-World
 */
public final class _064_CertifiedBerserker extends Quest
{
	private static final String qn = "_064_CertifiedBerserker";

	// NPC
	private static final int ORKURUS = 32207;
	private static final int TENAIN = 32215;
	private static final int GORT = 32252;
	private static final int ENTIEN = 32200;
	private static final int HARKILGAMED = 32253;

	// MOBS
	private static final int BREKA_ORC = 20267;
	private static final int BREKA_ORC_ARCHER = 20268;
	private static final int BREKA_ORC_SHAMAN = 20269;
	private static final int BREKA_ORC_OVERLORD = 20270;
	private static final int BREKA_ORC_WARRIOR = 20271;
	private static final int ROAD_SCAVENGER = 20551;
	private static final int DEAD_SEEKER = 20202;
	private static final int MARSH_STAKATO_DRONE = 20234;
	private static final int DIVINE_EMISSARY = 27323;

	// ITEMS
	private static final int BREKA_ORC_HEAD = 9754;
	private static final int MESSAGE_PLATE = 9755;
	private static final int REPORT_EAST = 9756;
	private static final int REPORT_NORTH = 9757;
	private static final int HARKILGAMEDS_LETTER = 9758;
	private static final int TENAINS_RECOMMENDATION = 9759;
	private static final int ORKURUS_RECOMMENDATION = 9760;

	private static boolean _isSpawned = false;

	private static final int[] QUESTITEMS = 
	{
		BREKA_ORC_HEAD, MESSAGE_PLATE, REPORT_EAST, REPORT_NORTH,
		HARKILGAMEDS_LETTER, TENAINS_RECOMMENDATION, ORKURUS_RECOMMENDATION
	};
	
	public _064_CertifiedBerserker(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(ORKURUS);
		addTalkId(ORKURUS);
		addTalkId(TENAIN);
		addTalkId(GORT);
		addTalkId(ENTIEN);
		addTalkId(HARKILGAMED);

		addKillId(BREKA_ORC);
		addKillId(BREKA_ORC_ARCHER);
		addKillId(BREKA_ORC_SHAMAN);
		addKillId(BREKA_ORC_OVERLORD);
		addKillId(BREKA_ORC_WARRIOR);
		addKillId(ROAD_SCAVENGER);
		addKillId(DEAD_SEEKER);
		addKillId(MARSH_STAKATO_DRONE);
		addKillId(DIVINE_EMISSARY);

		questItemIds = QUESTITEMS;
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32207-02.htm"))
		{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32215-02.htm"))
			st.set("cond","2");
		else if (event.equalsIgnoreCase("32252-02.htm"))
			st.set("cond","5");
		else if (event.equalsIgnoreCase("32215-08.htm"))
			st.takeItems(MESSAGE_PLATE, -1);
		else if (event.equalsIgnoreCase("32215-10.htm"))
			st.set("cond","8");
		else if (event.equalsIgnoreCase("Despawn_Harkilgamed"))
			_isSpawned = false;
		else if (event.equalsIgnoreCase("32236-02.htm"))
		{
			st.set("cond","13");
			st.giveItems(HARKILGAMEDS_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("32215-15.htm"))
		{
			st.takeItems(HARKILGAMEDS_LETTER, -1);
			st.giveItems(TENAINS_RECOMMENDATION, 1);
			st.set("cond","14");
		}
		else if (event.equalsIgnoreCase("32207-05.htm"))
		{
			st.unset("cond");
			st.unset("kills");
			st.unset("spawned");
			st.takeItems(TENAINS_RECOMMENDATION, -1);
			st.addExpAndSp(174503, 11974);
			st.giveItems(57, 31552);
			st.giveItems(ORKURUS_RECOMMENDATION, 1);
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		int npcId = npc.getId();
		int cond = st.getInt("cond");

		if (st.getState() == State.COMPLETED)
			htmltext = Quest.getAlreadyCompletedMsg(player);

		else if (npcId == ORKURUS)
		{
			if (player.getClassId().getId() != 0x7D || player.getLevel() < 39)
			{
				htmltext = "32207-00.htm";
				st.exitQuest(true);
			}
			else if (st.getState() == State.CREATED)
				htmltext = "32207-01.htm";
			else if (cond == 1)
				htmltext = "32207-03.htm";
			else if (cond == 14)
				htmltext = "32207-04.htm";
		}
		else if (npcId == TENAIN)
		{
			if (cond == 1)
				htmltext = "32215-01.htm";
			else if (cond == 2)
				htmltext = "32215-03.htm";
			else if (cond == 3)
			{
				htmltext = "32215-04.htm";
				st.takeItems(BREKA_ORC_HEAD, -1);
				st.set("cond","4");
			}
			else if (cond == 4)
				htmltext = "32215-05.htm";
			else if (cond == 7)
				htmltext = "32215-06.htm";
			else if (cond == 8)
				htmltext = "32215-11.htm";
			else if (cond == 11)
			{
				htmltext = "32215-12.htm";
				st.set("cond","12");
				st.set("kills","0");
				st.set("spawned","0");
			}
			else if (cond == 12)
				htmltext = "32215-13.htm";
			else if (cond == 13)
				htmltext = "32215-14.htm";
		}
		else if (npcId == GORT)
		{
			if (cond == 4)
				htmltext = "32252-01.htm";
			else if (cond == 5)
				htmltext = "32252-03.htm";
			else if (cond == 6)
			{
				htmltext = "32252-04.htm";
				st.set("cond","7");
			}
			else if (cond == 7)
				htmltext = "32252-05.htm";
		}
		else if (npcId == ENTIEN)
		{
			if (cond == 8)
			{
				htmltext = "32200-01.htm";
				st.set("cond","9");
			}
			else if (cond == 9)
				htmltext = "32200-02.htm";
			else if (cond == 10)
			{
				htmltext = "32200-03.htm";
				st.takeItems(REPORT_EAST, -1);
				st.takeItems(REPORT_NORTH, -1);
				st.set("cond","11");
			}
			else if (cond == 11)
				htmltext = "32200-04.htm";
		}
		else if (npcId == HARKILGAMED)
		{
			if (cond == 12)
				htmltext = "32236-01.htm";
			else if (cond == 13)
				htmltext = "32236-03.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(getName());
		if(st == null)
			return null;

		int npcId = npc.getId();
		int cond = st.getInt("cond");

		if(npcId == BREKA_ORC || npcId == BREKA_ORC_ARCHER || npcId == BREKA_ORC_SHAMAN || npcId == BREKA_ORC_OVERLORD || npcId == BREKA_ORC_WARRIOR)
		{
			if (st.getQuestItemsCount(BREKA_ORC_HEAD) < 20 && cond == 2)
			{
				st.giveItems(BREKA_ORC_HEAD, 1);
				if (st.getQuestItemsCount(BREKA_ORC_HEAD) == 20)
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond","3");
				}
				else
					st.playSound("ItemSound.quest_itemget");
			}
		}
		else if(npcId == ROAD_SCAVENGER)
		{
			if (st.getQuestItemsCount(MESSAGE_PLATE) == 0 && st.getRandom(20) == 1 && cond == 5)
			{
				st.giveItems(MESSAGE_PLATE, 1);
				st.playSound("ItemSound.quest_middle");
				st.set("cond","6");
			}
		}
		else if(npcId == DEAD_SEEKER)
		{
			if (st.getQuestItemsCount(REPORT_EAST) == 0 && st.getRandom(30) == 1 && cond == 9)
			{
				st.giveItems(REPORT_EAST, 1);
				st.playSound("ItemSound.quest_middle");
				if (st.getQuestItemsCount(REPORT_NORTH) > 0)
					st.set("cond","10");
			}
		}
		else if(npcId == MARSH_STAKATO_DRONE)
		{
			if (st.getQuestItemsCount(REPORT_NORTH) == 0 && st.getRandom(30) == 1 && cond == 9)
			{
				st.giveItems(REPORT_NORTH, 1);
				st.playSound("ItemSound.quest_middle");
				if (st.getQuestItemsCount(REPORT_EAST) > 0)
					st.set("cond","10");
			}
		}
		else if(npcId == DIVINE_EMISSARY)
		{
			if (cond == 12 && _isSpawned == false)
			{
				if (st.getInt("kills") < 5)
					st.set("kills",String.valueOf(st.getInt("kills")+1));
				else
				{
					st.addSpawn(HARKILGAMED, 120000);
					st.set("kills","0");
					_isSpawned = true;
					st.startQuestTimer("Despawn_Harkilgamed", 120000);
				}
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _064_CertifiedBerserker(64, qn, "");
	}
}