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
 * Created by LordWinter 09.09.2011
 * Based on L2J Eternity-World
 */
public class _660_AidingtheFloranVillage extends Quest
{
	private static final String qn = "_660_AidingtheFloranVillage";

	// NPC
	public final int MARIA 				= 30608;
	public final int ALEX 				= 30291;

	// MOBS
	public final int CARSED_SEER 			= 21106;
	public final int PLAIN_WATCMAN 			= 21102;
	public final int ROUGH_HEWN_ROCK_GOLEM 		= 21103;
	public final int DELU_LIZARDMAN_SHAMAN 		= 20781;
	public final int DELU_LIZARDMAN_SAPPLIER 	= 21104;
	public final int DELU_LIZARDMAN_COMMANDER 	= 21107;
	public final int DELU_LIZARDMAN_SPESIAL_AGENT 	= 21105;

	// QUEST ITEMS
	public final int WATCHING_EYES 			= 8074;
	public final int ROUGHLY_HEWN_ROCK_GOLEM_SHARD 	= 8075;
	public final int DELU_LIZARDMAN_SCALE 		= 8076;

	// REWARDS
	public final int SCROLL_ENCANT_ARMOR 		= 956;
	public final int SCROLL_ENCHANT_WEAPON 		= 955;

	public _660_AidingtheFloranVillage(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(MARIA);

		addTalkId(MARIA);
		addTalkId(ALEX);

		addKillId(CARSED_SEER);
		addKillId(PLAIN_WATCMAN);
		addKillId(ROUGH_HEWN_ROCK_GOLEM);
		addKillId(DELU_LIZARDMAN_SHAMAN);
		addKillId(DELU_LIZARDMAN_SAPPLIER);
		addKillId(DELU_LIZARDMAN_COMMANDER);
		addKillId(DELU_LIZARDMAN_SPESIAL_AGENT);

		questItemIds = new int[] { WATCHING_EYES, DELU_LIZARDMAN_SCALE, ROUGHLY_HEWN_ROCK_GOLEM_SHARD };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;

		long EYES = st.getQuestItemsCount(WATCHING_EYES);
		long SCALE = st.getQuestItemsCount(DELU_LIZARDMAN_SCALE);
		long SHARD = st.getQuestItemsCount(ROUGHLY_HEWN_ROCK_GOLEM_SHARD);

		if(event.equalsIgnoreCase("30608-04.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30291-05.htm"))
		{
			if(EYES + SCALE + SHARD >= 45)
			{
				st.giveItems(57, EYES * 100 + SCALE * 100 + SHARD * 100 + 9000);
				st.takeItems(WATCHING_EYES, -1);
				st.takeItems(DELU_LIZARDMAN_SCALE, -1);
				st.takeItems(ROUGHLY_HEWN_ROCK_GOLEM_SHARD, -1);
			}
			else
			{
				st.giveItems(57, EYES * 100 + SCALE * 100 + SHARD * 100);
				st.takeItems(WATCHING_EYES, -1);
				st.takeItems(DELU_LIZARDMAN_SCALE, -1);
				st.takeItems(ROUGHLY_HEWN_ROCK_GOLEM_SHARD, -1);
			}
			st.playSound("ItemSound.quest_finish");
		}
		else if(event.equalsIgnoreCase("30291-11.htm"))
		{
			if(EYES + SCALE + SHARD >= 99)
			{
				long n = 100 - EYES;
				long t = 100 - SCALE - EYES;
				if(EYES >= 100)
					st.takeItems(WATCHING_EYES, 100);
				else
				{
					st.takeItems(WATCHING_EYES, -1);
					if(SCALE >= n)
						st.takeItems(DELU_LIZARDMAN_SCALE, n);
					else
					{
						st.takeItems(DELU_LIZARDMAN_SCALE, -1);
						st.takeItems(ROUGHLY_HEWN_ROCK_GOLEM_SHARD, t);
					}
				}
				if (getRandom(10) < 8)
				{
					st.giveItems(57, 13000);
					st.giveItems(SCROLL_ENCANT_ARMOR, 1);
				}
				else
					st.giveItems(57, 1000);
				st.playSound("ItemSound.quest_finish");
			}
			else
				htmltext = "30291-14.htm";
		}
		else if(event.equalsIgnoreCase("30291-12.htm"))
		{
			if(EYES + SCALE + SHARD >= 199)
			{
				long n = 200 - EYES;
				long t = 200 - SCALE - EYES;
				int luck = getRandom(15);
				if(EYES >= 200)
					st.takeItems(WATCHING_EYES, 200);
				else
					st.takeItems(WATCHING_EYES, -1);
				if(SCALE >= n)
					st.takeItems(DELU_LIZARDMAN_SCALE, n);
				else
					st.takeItems(DELU_LIZARDMAN_SCALE, -1);
				st.takeItems(ROUGHLY_HEWN_ROCK_GOLEM_SHARD, t);
				if(luck < 9)
				{
					st.giveItems(57, 20000);
					st.giveItems(SCROLL_ENCANT_ARMOR, 1);
				}
				else if(luck > 8 && luck < 12)
					st.giveItems(SCROLL_ENCHANT_WEAPON, 1);
				else
					st.giveItems(57, 2000);
				st.playSound("ItemSound.quest_finish");
			}
			else
				htmltext = "30291-14.htm";
		}
		else if(event.equalsIgnoreCase("30291-13.htm"))
		{
			if(EYES + SCALE + SHARD >= 499)
			{
				long n = 500 - EYES;
				long t = 500 - SCALE - EYES;
				if(EYES >= 500)
					st.takeItems(WATCHING_EYES, 500);
				else
					st.takeItems(WATCHING_EYES, -1);
				if(SCALE >= n)
					st.takeItems(DELU_LIZARDMAN_SCALE, n);
				else
				{
					st.takeItems(DELU_LIZARDMAN_SCALE, -1);
					st.takeItems(ROUGHLY_HEWN_ROCK_GOLEM_SHARD, t);
				}
				if (getRandom(10) < 8)
				{
					st.giveItems(57, 45000);
					st.giveItems(SCROLL_ENCHANT_WEAPON, 1);
				}
				else
					st.giveItems(57, 5000);
				st.playSound("ItemSound.quest_finish");
			}
			else
				htmltext = "30291-14.htm";
		}
    		else if(event.equalsIgnoreCase("30291-15.htm"))
      			st.playSound("ItemSound.quest_middle");
		else if(event.equalsIgnoreCase("30291-06.htm"))
		{
       			st.unset("cond");
       			st.exitQuest(true);
       			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;

		int npcId = npc.getId();
		int cond = st.getInt("cond");
		int id = st.getState();

		if(id == State.CREATED)
		{
			if(npcId == MARIA && cond == 0)
			{
				if(player.getLevel() < 30)
				{
					htmltext = "30608-01.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "30608-02.htm";
			}
		}
		else if(id == State.STARTED)
		{
			if(npcId == MARIA && cond == 1)
				htmltext = "30608-06.htm";
			else if(npcId == ALEX && cond == 1)
			{
				htmltext = "30291-01.htm";
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "2");
			}
			else if(npcId == ALEX && cond == 2)
			{
				if(st.getQuestItemsCount(WATCHING_EYES) + st.getQuestItemsCount(DELU_LIZARDMAN_SCALE) + st.getQuestItemsCount(ROUGHLY_HEWN_ROCK_GOLEM_SHARD) == 0)
					htmltext = "30291-02.htm";
				else
					htmltext = "30291-03.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		int npcId = npc.getId();
		int chance = getRandom(100) + 1;
		if(st.getInt("cond") == 2)
		{
			if(npcId == 21106 | npcId == 21102 && chance < 79)
			{
				st.giveItems(WATCHING_EYES, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(npcId == ROUGH_HEWN_ROCK_GOLEM && chance < 75)
			{
				st.giveItems(ROUGHLY_HEWN_ROCK_GOLEM_SHARD, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(npcId == 20781 | npcId == 21104 | npcId == 21107 | npcId == 21105 && chance < 67)
			{
				st.giveItems(DELU_LIZARDMAN_SCALE, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _660_AidingtheFloranVillage(660, qn, "");
	}
}