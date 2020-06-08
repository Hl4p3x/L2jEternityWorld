
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
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

/**
 * Created by LordWinter $ Sigrlinne 14.03.2011
 * Based on L2J Eternity-World
 */
public class _023_LidiasHeart extends Quest
{
	private static final String qn = "_023_LidiasHeart";

	// NPCs
	private final static int INNOCENTIN = 31328;
	private final static int BROKEN_BOOK_SHELF = 31526;
	private final static int GHOST_OF_VON_HELLMAN_ID = 31524;
	private final static int TOMBSTONE = 31523;
	private final static int VIOLET = 31386;
	private final static int BOX = 31530;

	// Items
	private final static int MAP_FOREST_OF_DEADMAN = 7063;
	private final static int SILVER_KEY = 7149;
	private final static int LIDIA_HAIR_PIN = 7148;
	private final static int LIDIA_DIARY = 7064;
	private final static int SILVER_SPEAR = 7150;
	private final static int ADENA = 57;

	public L2Npc GHOST_OF_VON_HELLMANN;

	public _023_LidiasHeart(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(INNOCENTIN);
		addTalkId(INNOCENTIN);
		addTalkId(BROKEN_BOOK_SHELF);
		addTalkId(GHOST_OF_VON_HELLMAN_ID);
		addTalkId(TOMBSTONE);
		addTalkId(VIOLET);
		addTalkId(BOX);

		questItemIds = new int[] {MAP_FOREST_OF_DEADMAN, SILVER_KEY, LIDIA_HAIR_PIN, LIDIA_DIARY, SILVER_SPEAR};
	}

	private void spawnGHOST_OF_VON_HELLMANN(QuestState st)
	{
		GHOST_OF_VON_HELLMANN = st.addSpawn(GHOST_OF_VON_HELLMAN_ID ,51432, -54570, -3136, getRandom(0, 20), false, 180000);
		GHOST_OF_VON_HELLMANN.broadcastPacket(new NpcSay(GHOST_OF_VON_HELLMANN.getObjectId(), 0, GHOST_OF_VON_HELLMANN.getId(), NpcStringId.WHO_AWOKE_ME));
	}

	private void despawnGHOST_OF_VON_HELLMANN(QuestState st)
	{
		if (GHOST_OF_VON_HELLMANN != null)
			GHOST_OF_VON_HELLMANN.deleteMe();
		GHOST_OF_VON_HELLMANN = null;
	}	
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31328-02.htm"))
		{
			st.giveItems(MAP_FOREST_OF_DEADMAN, 1);
			st.giveItems(SILVER_KEY, 1);
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31328-03.htm"))
		{
			st.set("cond", "2");
		}
		else if (event.equalsIgnoreCase("31526-01.htm"))
		{
			st.set("cond", "3");
		}
		else if (event.equalsIgnoreCase("31526-05.htm"))
		{
			st.giveItems(LIDIA_HAIR_PIN, 1);
			if (st.getQuestItemsCount(LIDIA_DIARY) != 0)
				st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31526-11.htm"))
		{
			st.giveItems(LIDIA_DIARY, 1);
			if (st.getQuestItemsCount(LIDIA_HAIR_PIN) != 0)
				st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31328-19.htm"))
		{
			st.set("cond", "6");
		}
		else if (event.equalsIgnoreCase("31524-04.htm"))
		{
			st.set("cond", "7");
			st.takeItems(LIDIA_DIARY, -1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31523-02.htm"))
		{
			despawnGHOST_OF_VON_HELLMANN(st);
			spawnGHOST_OF_VON_HELLMANN(st);
			st.playSound("SkillSound5.horror_02");			
		}
		else if (event.equalsIgnoreCase("31523-05.htm"))
		{
			st.startQuestTimer("viwer_timer", 10000);
		}
		else if (event.equalsIgnoreCase("viwer_timer"))
		{
			st.set("cond", "8");
			htmltext = "31523-06.htm";
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31530-02.htm"))
		{
			st.set("cond", "10");
			st.takeItems(SILVER_KEY, -1);
			st.giveItems(SILVER_SPEAR, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("i7064-02.htm"))
		{
			htmltext = "i7064-02.htm";
		}
		else if (event.equalsIgnoreCase("31526-13.htm"))
		{
			st.startQuestTimer("read_book", 120000);
		}
		else if (event.equalsIgnoreCase("read_book"))
		{
			htmltext = "i7064.htm";
		}
		
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		final int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.COMPLETED :
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED :
				final QuestState qs = player.getQuestState("_022_TragedyInVonHellmannForest");
				if (qs != null && qs.isCompleted())
					htmltext = "31328-01.htm";
				else
				{
					htmltext = "31328-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED :
				switch (npc.getId())
				{
					case INNOCENTIN:
						switch (cond)
						{
							case 1:
								htmltext = "31328-03.htm";
								break;
							case 2:
								htmltext = "31328-07.htm";
								break;
							case 4:
								htmltext = "31328-08.htm";
								break;
							case 6:
								htmltext = "31328-19.htm";
								break;
						}		
						break;
					case BROKEN_BOOK_SHELF:
						switch (cond)
						{
							case 2:
								if (st.getQuestItemsCount(SILVER_KEY) != 0)
									htmltext = "31526-00.htm";
								break;
							case 3:
								if (st.getQuestItemsCount(LIDIA_HAIR_PIN) == 0)
								{
									if (st.getQuestItemsCount(LIDIA_DIARY) == 0)
										htmltext = "31526-02.htm";
									else
										htmltext = "31526-12.htm";
								}
								else if (st.getQuestItemsCount(LIDIA_DIARY) == 0)
									htmltext = "31526-06.htm";
								break;
							case 4:
								htmltext = "31526-13.htm";
								break;
						}		
						break;
					case GHOST_OF_VON_HELLMAN_ID:
						switch (cond)
						{
							case 6:
								htmltext = "31524-01.htm";
								break;
							case 7:
								htmltext = "31524-05.htm";
								break;
						}		
						break;
					case TOMBSTONE:
						switch (cond)
						{
							case 6:
								if (st.getQuestTimer("spawn_timer") != null)
									htmltext = "31523-03.htm";
								else
									htmltext = "31523-01.htm";
								break;
							case 7:
								htmltext = "31523-04.htm";
								break;
							case 8:
								htmltext = "31523-06.htm";
								break;
						}		
						break;
					case VIOLET:
						switch (cond)
						{
							case 8:
								htmltext = "31386-01.htm";
								st.set("cond", "9");
								break;
							case 9:
								htmltext = "31386-02.htm";
								break;
							case 10:
								if (st.getQuestItemsCount(SILVER_SPEAR) != 0)
								{
									htmltext = "31386-03.htm";
									st.takeItems(SILVER_SPEAR, -1);
									st.addExpAndSp(456893,42112);
									st.giveItems(ADENA, 300000);
									st.playSound("ItemSound.quest_finish");
									st.unset("cond");
									st.setState(State.COMPLETED);
									st.exitQuest(false);
								}
								else
									htmltext = "31386-03a.htm";
								break;
						}		
						break;
					case BOX:
						switch (cond)
						{
							case 9:
								if (st.getQuestItemsCount(SILVER_KEY) != 0)
									htmltext = "31530-01.htm";
								else
									htmltext = "31530-01a.htm";
								break;
							case 10:
								htmltext = "31386-03.htm";
								break;
						}		
						break;
				}
				break;
		}
		
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _023_LidiasHeart(23, qn, "");    	
	}
}