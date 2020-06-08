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
public final class _066_CertifiedArbalester extends Quest
{
	private static final String qn = "_066_CertifiedArbalester";

	// NPC
	private static final int RINDY = 32201;
	private static final int CLAYTON = 30464;
	private static final int POITAN = 30458;
	private static final int HOLVAS = 30058;
	private static final int MELDINA = 32214;
	private static final int SELSIA = 32220;
	private static final int GAIUS = 30171;
	private static final int GAUEN = 30717;
	private static final int KAIENA = 30720;
	private static final int[] _talkNpc = { RINDY, CLAYTON, POITAN, HOLVAS, MELDINA, SELSIA, GAIUS, GAUEN, KAIENA };

	// MOBS
	private static final int[] _FloranMobs = { 21102, 21103, 21104, 21105, 21106, 21107, 21108, 20781 };
	private static final int[] _EgMobs = { 20199, 20200, 20201, 20202, 20203, 20083, 20144 };
	private static final int GRANDIS = 20554;
	private static final int GARGOYLE = 20563;
	private static final int[] _TimakMobs = { 20584, 20585 };
	private static final int LADY = 27336;

	// ITEMS
	private static final int ENMITY_CRYSTAL = 9773;
	private static final int ENMITY_CRYSTAL_CORE = 9774;
	private static final int MANUSCRIPT_PAGE = 9775;
	private static final int ENCODED_PAGE = 9776;
	private static final int KAMAEL_INQUISITOR_TRAIN_MARK = 9777;
	private static final int FRAGMENT_ATTACK_ORDERS = 9778;
	private static final int GRANDIS_ATTACK_ORDERS = 9779;
	private static final int MANASHENS_TALISMAN = 9780;
	private static final int RESEARCH_OF_THE_GIANTS = 9781;
	private static final int KAMAEL_INQUISITOR_MARK = 9782;

	private static boolean _isSpawnedCrimsonLady = false;
	private static final int[] QUESTITEMS =
	{
		ENMITY_CRYSTAL, ENMITY_CRYSTAL_CORE, MANUSCRIPT_PAGE,
		ENCODED_PAGE, KAMAEL_INQUISITOR_TRAIN_MARK, FRAGMENT_ATTACK_ORDERS,
		GRANDIS_ATTACK_ORDERS, MANASHENS_TALISMAN, RESEARCH_OF_THE_GIANTS
	};
	
	public _066_CertifiedArbalester(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(RINDY);
		for (int npcId : _talkNpc)
			addTalkId(npcId);

		for (int npcId : _FloranMobs)
			addKillId(npcId);
		for (int npcId : _EgMobs)
			addKillId(npcId);
		for (int npcId : _TimakMobs)
			addKillId(npcId);
		addKillId(GRANDIS);
		addKillId(GARGOYLE);
		addKillId(LADY);

		questItemIds = QUESTITEMS;
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32201-02.htm"))
		{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32201-03.htm"))
		{
			st.set("cond","2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30464-05.htm"))
		{
			st.set("cond","3");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30464-08.htm"))
			st.takeItems(ENMITY_CRYSTAL, -1);
		else if (event.equalsIgnoreCase("30464-09.htm"))
		{
			st.giveItems(ENMITY_CRYSTAL_CORE, 1);
			st.set("cond","5");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30458-03.htm"))
			st.takeItems(ENMITY_CRYSTAL_CORE, -1);
		else if (event.equalsIgnoreCase("30458-07.htm"))
		{
			st.set("cond","6");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30058-04.htm"))
		{
			st.set("cond","7");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30058-07.htm"))
		{
			st.set("cond","9");
			st.playSound("ItemSound.quest_middle");
			st.giveItems(ENCODED_PAGE, 1);
		}
		else if (event.equalsIgnoreCase("32214-03.htm"))
		{
			st.set("cond","10");
			st.playSound("ItemSound.quest_middle");
			st.takeItems(ENCODED_PAGE, -1);
			st.giveItems(KAMAEL_INQUISITOR_TRAIN_MARK, 1);
		}
		else if (event.equalsIgnoreCase("32220-11.htm"))
		{
			st.set("cond","11");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30171-02.htm"))
			st.takeItems(GRANDIS_ATTACK_ORDERS, -1);
		else if (event.equalsIgnoreCase("30171-05.htm"))
		{
			st.set("cond","14");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30717-02.htm"))
			st.takeItems(MANASHENS_TALISMAN, -1);
		else if (event.equalsIgnoreCase("30717-07.htm"))
		{
			st.set("cond","17");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30720-03.htm"))
		{
			st.set("cond","18");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32220-19.htm"))
		{
			st.set("cond","19");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("Despawn_Crimson_Lady"))
		{
			_isSpawnedCrimsonLady = false;
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
			htmltext = getAlreadyCompletedMsg(player);
		else if (npcId == RINDY)
		{
			if (player.getClassId().getId() != 0x7E || player.getLevel() < 39)
			{
				htmltext = "32201-00.htm";
				st.exitQuest(true);
			}
			else if (st.getState() == State.CREATED)
				htmltext = "32201-01.htm";
			else if (cond == 1)
				htmltext = "32201-03.htm";
			else if (cond == 2)
				htmltext = "32201-04.htm";
		}
		else if (npcId == CLAYTON)
		{
			if (cond == 2)
				htmltext = "30464-01.htm";
			else if (cond == 3)
				htmltext = "30464-06.htm";
			else if (cond == 4)
				htmltext = "30464-07.htm";
			else if (cond == 5)
				htmltext = "30464-09.htm";
		}
		else if (npcId == POITAN)
		{
			if (cond == 5)
				htmltext = "30458-01.htm";
			else if (cond == 6)
				htmltext = "30458-08.htm";
		}
		else if (npcId == HOLVAS)
		{
			if (cond == 6)
				htmltext = "30058-01.htm";
			else if (cond == 7)
				htmltext = "30058-05.htm";
			else if (cond == 8)
			{
				htmltext = "30058-06.htm";
				st.takeItems(MANUSCRIPT_PAGE, -1);
			}
			else if (cond == 9)
				htmltext = "30058-08.htm";
		}
		else if (npcId == MELDINA)
		{
			if (cond == 9)
				htmltext = "32214-01.htm";
			else if (cond == 10)
				htmltext = "32214-04.htm";
		}
		else if (npcId == SELSIA)
		{
			if (cond == 10)
				htmltext = "32220-01.htm";
			else if (cond == 11)
				htmltext = "32220-11.htm";
			else if (cond == 18)
				htmltext = "32220-12.htm";
			else if (cond == 19)
				htmltext = "32220-19.htm";
			else if (cond == 20)
			{
				htmltext = "32220-20.htm";
				st.unset("cond");
				st.takeItems(RESEARCH_OF_THE_GIANTS, -1);
				st.addExpAndSp(214773, 14738);
				st.giveItems(57, 38833);
				st.giveItems(KAMAEL_INQUISITOR_MARK, 1);
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
			}
		}
		else if (npcId == GAIUS)
		{
			if (cond == 13)
				htmltext = "30171-01.htm";
			else if (cond == 14)
				htmltext = "30171-06.htm";
			else if (cond == 16)
				htmltext = "30171-07.htm";
		}
		else if (npcId == GAUEN)
		{
			if (cond == 16)
				htmltext = "30717-01.htm";
			else if (cond == 17)
				htmltext = "30717-08.htm";
		}
		else if (npcId == KAIENA)
		{
			if (cond == 17)
				htmltext = "30720-01.htm";
			else if (cond == 18)
				htmltext = "30720-04.htm";
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

		if (npcId == GRANDIS)
		{
			if (st.getQuestItemsCount(FRAGMENT_ATTACK_ORDERS) < 10 && cond == 11 || cond == 12)
			{
				if (st.getQuestItemsCount(FRAGMENT_ATTACK_ORDERS) == 9)
				{
					st.playSound("ItemSound.quest_middle");
					st.takeItems(FRAGMENT_ATTACK_ORDERS, -1);
					st.giveItems(GRANDIS_ATTACK_ORDERS, 1);
					st.set("cond","13");
				}
				else
				{
					st.giveItems(FRAGMENT_ATTACK_ORDERS, 1);
					st.playSound("ItemSound.quest_itemget");
					if (st.getQuestItemsCount(FRAGMENT_ATTACK_ORDERS) == 0)
						st.set("cond","12");
				}
			}
		}
		else if (npcId == GARGOYLE)
		{
			if (st.getQuestItemsCount(MANASHENS_TALISMAN) < 10 && cond == 14 || cond == 15)
			{
				st.giveItems(MANASHENS_TALISMAN, 1);
				if (st.getQuestItemsCount(MANASHENS_TALISMAN) == 9)
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond","16");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
					if (st.getQuestItemsCount(MANASHENS_TALISMAN) == 0)
						st.set("cond","15");
				}
			}
		}
		else if (npcId == LADY)
		{
			if (cond == 19 && st.getQuestItemsCount(RESEARCH_OF_THE_GIANTS) == 0)
			{
				st.giveItems(RESEARCH_OF_THE_GIANTS, 1);
				st.set("cond","20");
				_isSpawnedCrimsonLady = false;
			}
		}
		else if (Util.contains(_FloranMobs, npc.getId()))
		{
			if (st.getQuestItemsCount(ENMITY_CRYSTAL) < 30 && cond == 3)
			{
				st.giveItems(ENMITY_CRYSTAL, 1);
				if (st.getQuestItemsCount(ENMITY_CRYSTAL) == 30)
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond","4");
				}
				else
					st.playSound("ItemSound.quest_itemget");
			}
		}
		else if (Util.contains(_EgMobs, npc.getId()))
		{
			if (st.getQuestItemsCount(MANUSCRIPT_PAGE) < 30 && cond == 7)
			{
				st.giveItems(MANUSCRIPT_PAGE, 1);
				if (st.getQuestItemsCount(MANUSCRIPT_PAGE) == 30)
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond","8");
				}
				else
					st.playSound("ItemSound.quest_itemget");
			}
		}
		else if (Util.contains(_TimakMobs, npc.getId()))
		{
			if (st.getRandom(40) < 1 && cond == 19 && _isSpawnedCrimsonLady == false)
			{
				st.addSpawn(LADY, 180000);
				_isSpawnedCrimsonLady = true;
				st.startQuestTimer("Despawn_Crimson_Lady", 180000);
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _066_CertifiedArbalester(66, qn, "");
	}
}