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

import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 25.09.2012
 * Based on L2J Eternity-World
 */
public class _417_PathToScavenger extends Quest
{
	private static final String qn = "_417_PathToScavenger";
	
	// Npcs
	private static final int PIPPI = 30524;
	private static final int RAUT = 30316;
	private static final int SHARI = 30517;
	private static final int MION = 30519;
	private static final int BRONK = 30525;
	private static final int ZIMENF = 30538;
	private static final int TOMA = 30556;
	private static final int TORAI = 30557;
	
	// Mobs
	private static final int HUNTER_TARANTULA = 20403;
	private static final int HONEY_BEAR = 27058;
	private static final int PLUNDER_TARANTULA = 20508;
	private static final int HUNTER_BEAR = 20777;
	
	// Quest items
	private static final int RING_OF_RAVEN = 1642;
	private static final int PIPIS_LETTER = 1643;
	private static final int ROUTS_TP_SCROLL = 1644;
	private static final int SUCCUBUS_UNDIES = 1645;
	private static final int MIONS_LETTER = 1646;
	private static final int BRONKS_INGOT = 1647;
	private static final int CHALIS_AXE = 1648;
	private static final int ZIMENFS_POTION = 1649;
	private static final int BRONKS_PAY = 1650;
	private static final int CHALIS_PAY = 1651;
	private static final int ZIMENFS_PAY = 1652;
	private static final int BEAR_PIC = 1653;
	private static final int TARANTULA_PIC = 1654;
	private static final int HONEY_JAR = 1655;
	private static final int BEAD = 1656;
	private static final int BEAD_PARCEL = 1657;
	
	private static final int[] QUESTITEMS =
	{
		PIPIS_LETTER,
		ROUTS_TP_SCROLL,
		SUCCUBUS_UNDIES,
		MIONS_LETTER,
		BRONKS_INGOT,
		CHALIS_AXE,
		ZIMENFS_POTION,
		BRONKS_PAY,
		CHALIS_PAY,
		ZIMENFS_PAY,
		BEAR_PIC,
		TARANTULA_PIC,
		HONEY_JAR,
		BEAD,
		BEAD_PARCEL
	};
	
	public _417_PathToScavenger(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(PIPPI);
		
		addTalkId(PIPPI);
		addTalkId(RAUT);
		addTalkId(SHARI);
		addTalkId(MION);
		addTalkId(BRONK);
		addTalkId(ZIMENF);
		addTalkId(TOMA);
		addTalkId(TORAI);
		
		addKillId(HUNTER_TARANTULA);
		addKillId(HONEY_BEAR);
		addKillId(PLUNDER_TARANTULA);
		addKillId(HUNTER_BEAR);
		
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
			if ((level >= 18) && (classId == 0x35) && (st.getQuestItemsCount(RING_OF_RAVEN) == 0))
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				st.giveItems(PIPIS_LETTER, 1);
				htmltext = "30524-05.htm";
			}
			else if (classId != 0x35)
			{
				htmltext = classId == 0x36 ? "30524-02a.htm" : "30524-08.htm";
			}
			else if ((level < 18) && (classId == 0x35))
			{
				htmltext = "30524-02.htm";
			}
			else if ((level >= 18) && (classId == 0x35) && (st.getQuestItemsCount(RING_OF_RAVEN) == 1))
			{
				htmltext = "30524-04.htm";
			}
		}
		else if (event.equalsIgnoreCase("30519_1"))
		{
			if (st.getQuestItemsCount(PIPIS_LETTER) > 0)
			{
				st.takeItems(PIPIS_LETTER, 1);
				st.set("cond", "2");
				int n = st.getRandom(3);
				if (n == 0)
				{
					st.giveItems(ZIMENFS_POTION, 1);
					htmltext = "30519-02.htm";
				}
				else if (n == 1)
				{
					st.giveItems(CHALIS_AXE, 1);
					htmltext = "30519-03.htm";
				}
				else if (n == 2)
				{
					st.giveItems(BRONKS_INGOT, 1);
					htmltext = "30519-04.htm";
				}
			}
			else
			{
				htmltext = Quest.getNoQuestMsg(player);
			}
		}
		else if (event.equalsIgnoreCase("30519_2"))
		{
			htmltext = "30519-06.htm";
		}
		else if (event.equalsIgnoreCase("30519_3"))
		{
			htmltext = "30519-07.htm";
			st.set("id", String.valueOf(st.getInt("id") + 1));
		}
		else if (event.equalsIgnoreCase("30519_4"))
		{
			htmltext = st.getRandom(2) == 0 ? "30519-06.htm" : "30519-11.htm";
		}
		else if (event.equalsIgnoreCase("30519_5"))
		{
			if ((st.getQuestItemsCount(ZIMENFS_POTION) > 0) || (st.getQuestItemsCount(CHALIS_AXE) > 0) || (st.getQuestItemsCount(BRONKS_INGOT) > 0))
			{
				if ((st.getInt("id") / 10) < 2)
				{
					st.set("id", String.valueOf(st.getInt("id") + 1));
					htmltext = "30519-07.htm";
				}
				else if (((st.getInt("id") / 10) >= 2) && (st.getInt("cond") == 0))
				{
					if ((st.getInt("id") / 10) < 3)
					{
						st.set("id", String.valueOf(st.getInt("id") + 1));
					}
					htmltext = "30519-09.htm";
				}
				else if (((st.getInt("id") / 10) >= 3) && (st.getInt("cond") > 0))
				{
					st.giveItems(MIONS_LETTER, 1);
					st.takeItems(CHALIS_AXE, 1);
					st.takeItems(ZIMENFS_POTION, 1);
					st.takeItems(BRONKS_INGOT, 1);
					htmltext = "30519-10.htm";
				}
			}
			else
			{
				htmltext = Quest.getNoQuestMsg(player);
			}
		}
		else if (event.equalsIgnoreCase("30519_6"))
		{
			if ((st.getQuestItemsCount(ZIMENFS_PAY) > 0) || (st.getQuestItemsCount(CHALIS_PAY) > 0) || (st.getQuestItemsCount(BRONKS_PAY) > 0))
			{
				int n = st.getRandom(3);
				st.takeItems(ZIMENFS_PAY, 1);
				st.takeItems(CHALIS_PAY, 1);
				st.takeItems(BRONKS_PAY, 1);
				if (n == 0)
				{
					st.giveItems(ZIMENFS_POTION, 1);
					htmltext = "30519-02.htm";
				}
				else if (n == 1)
				{
					st.giveItems(CHALIS_AXE, 1);
					htmltext = "30519-03.htm";
				}
				else if (n == 2)
				{
					st.giveItems(BRONKS_INGOT, 1);
					htmltext = "30519-04.htm";
				}
			}
			else
			{
				htmltext = Quest.getNoQuestMsg(player);
			}
		}
		else if (event.equalsIgnoreCase("30316_1"))
		{
			if (st.getQuestItemsCount(BEAD_PARCEL) > 0)
			{
				st.takeItems(BEAD_PARCEL, 1);
				st.giveItems(ROUTS_TP_SCROLL, 1);
				st.set("cond", "10");
				htmltext = "30316-02.htm";
			}
			else
			{
				htmltext = Quest.getNoQuestMsg(player);
			}
		}
		else if (event.equalsIgnoreCase("30316_2"))
		{
			if (st.getQuestItemsCount(BEAD_PARCEL) > 0)
			{
				st.takeItems(BEAD_PARCEL, 1);
				st.giveItems(ROUTS_TP_SCROLL, 1);
				st.set("cond", "10");
				htmltext = "30316-03.htm";
			}
			else
			{
				htmltext = Quest.getNoQuestMsg(player);
			}
		}
		else if (event.equalsIgnoreCase("30557_1"))
		{
			htmltext = "30557-02.htm";
		}
		else if (event.equalsIgnoreCase("30557_2"))
		{
			if (st.getQuestItemsCount(ROUTS_TP_SCROLL) > 0)
			{
				st.takeItems(ROUTS_TP_SCROLL, 1);
				st.giveItems(SUCCUBUS_UNDIES, 1);
				st.set("cond", "11");
				htmltext = "30557-03.htm";
			}
			else
			{
				htmltext = Quest.getNoQuestMsg(player);
			}
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
		int state = st.getState();
		if ((npcId != PIPPI) && (state != State.STARTED))
		{
			return htmltext;
		}
		
		int cond = st.getInt("cond");
		if ((npcId == PIPPI) && (cond == 0))
		{
			htmltext = "30524-01.htm";
		}
		else if ((npcId == PIPPI) && (cond > 0) && (st.getQuestItemsCount(PIPIS_LETTER) > 0))
		{
			htmltext = "30524-06.htm";
		}
		else if ((npcId == PIPPI) && (cond > 0) && (st.getQuestItemsCount(PIPIS_LETTER) == 0))
		{
			htmltext = "30524-07.htm";
		}
		else if ((npcId == MION) && (cond > 0) && (st.getQuestItemsCount(PIPIS_LETTER) > 0))
		{
			htmltext = "30519-01.htm";
		}
		else if ((npcId == MION) && (cond > 0) && ((st.getQuestItemsCount(CHALIS_AXE) + st.getQuestItemsCount(BRONKS_INGOT) + st.getQuestItemsCount(ZIMENFS_POTION)) == 1) && ((st.getInt("id") / 10) == 0))
		{
			htmltext = "30519-05.htm";
		}
		else if ((npcId == MION) && (cond > 0) && ((st.getQuestItemsCount(CHALIS_AXE) + st.getQuestItemsCount(BRONKS_INGOT) + st.getQuestItemsCount(ZIMENFS_POTION)) == 1) && ((st.getInt("id") / 10) > 0))
		{
			htmltext = "30519-08.htm";
		}
		else if ((npcId == MION) && (cond > 0) && ((st.getQuestItemsCount(CHALIS_PAY) + st.getQuestItemsCount(BRONKS_PAY) + st.getQuestItemsCount(ZIMENFS_PAY)) == 1) && (st.getInt("id") < 50))
		{
			htmltext = "30519-12.htm";
		}
		else if ((npcId == MION) && (cond > 0) && ((st.getQuestItemsCount(CHALIS_PAY) + st.getQuestItemsCount(BRONKS_PAY) + st.getQuestItemsCount(ZIMENFS_PAY)) == 1) && (st.getInt("id") >= 50))
		{
			st.giveItems(MIONS_LETTER, 1);
			st.takeItems(CHALIS_PAY, 1);
			st.takeItems(ZIMENFS_PAY, 1);
			st.takeItems(BRONKS_PAY, 1);
			st.set("cond", "4");
			htmltext = "30519-15.htm";
		}
		else if ((npcId == MION) && (cond > 0) && (st.getQuestItemsCount(MIONS_LETTER) > 0))
		{
			htmltext = "30519-13.htm";
		}
		else if ((npcId == MION) && (cond > 0) && ((st.getQuestItemsCount(BEAR_PIC) > 0) || (st.getQuestItemsCount(TARANTULA_PIC) > 0) || (st.getQuestItemsCount(BEAD_PARCEL) > 0) || (st.getQuestItemsCount(ROUTS_TP_SCROLL) > 0) || (st.getQuestItemsCount(SUCCUBUS_UNDIES) > 0)))
		{
			htmltext = "30519-14.htm";
		}
		else if ((npcId == SHARI) && (cond > 0) && (st.getQuestItemsCount(CHALIS_AXE) == 1) && (st.getInt("id") < 20))
		{
			st.takeItems(CHALIS_AXE, 1);
			st.giveItems(CHALIS_PAY, 1);
			if (st.getInt("id") >= 50)
			{
				st.set("cond", "3");
			}
			st.set("id", String.valueOf(st.getInt("id") + 10));
			htmltext = "30517-01.htm";
		}
		else if ((npcId == SHARI) && (cond > 0) && (st.getQuestItemsCount(CHALIS_AXE) == 1) && (st.getInt("id") >= 20))
		{
			st.takeItems(CHALIS_AXE, 1);
			st.giveItems(CHALIS_PAY, 1);
			if (st.getInt("id") >= 50)
			{
				st.set("cond", "3");
			}
			st.set("id", String.valueOf(st.getInt("id") + 10));
			htmltext = "30517-02.htm";
		}
		
		else if ((npcId == SHARI) && (cond > 0) && (st.getQuestItemsCount(CHALIS_PAY) == 1))
		{
			htmltext = "30517-03.htm";
		}
		else if ((npcId == BRONK) && (cond > 0) && (st.getQuestItemsCount(BRONKS_INGOT) == 1) && (st.getInt("id") < 20))
		{
			st.takeItems(BRONKS_INGOT, 1);
			st.giveItems(BRONKS_PAY, 1);
			if (st.getInt("id") >= 50)
			{
				st.set("cond", "3");
			}
			st.set("id", String.valueOf(st.getInt("id") + 10));
			htmltext = "30525-01.htm";
		}
		else if ((npcId == BRONK) && (cond > 0) && (st.getQuestItemsCount(BRONKS_INGOT) == 1) && (st.getInt("id") >= 20))
		{
			st.takeItems(BRONKS_INGOT, 1);
			st.giveItems(BRONKS_PAY, 1);
			if (st.getInt("id") >= 50)
			{
				st.set("cond", "3");
			}
			st.set("id", String.valueOf(st.getInt("id") + 10));
			htmltext = "30525-02.htm";
		}
		else if ((npcId == BRONK) && (cond > 0) && (st.getQuestItemsCount(BRONKS_PAY) == 1))
		{
			htmltext = "30525-03.htm";
		}
		else if ((npcId == ZIMENF) && (cond > 0) && (st.getQuestItemsCount(ZIMENFS_POTION) == 1) && (st.getInt("id") < 20))
		{
			st.takeItems(ZIMENFS_POTION, 1);
			st.giveItems(ZIMENFS_PAY, 1);
			if (st.getInt("id") >= 50)
			{
				st.set("cond", "3");
			}
			st.set("id", String.valueOf(st.getInt("id") + 10));
			htmltext = "30538-01.htm";
		}
		else if ((npcId == ZIMENF) && (cond > 0) && (st.getQuestItemsCount(ZIMENFS_POTION) == 1) && (st.getInt("id") >= 20))
		{
			st.takeItems(ZIMENFS_POTION, 1);
			st.giveItems(ZIMENFS_PAY, 1);
			if (st.getInt("id") >= 50)
			{
				st.set("cond", "3");
			}
			st.set("id", String.valueOf(st.getInt("id") + 10));
			htmltext = "30538-02.htm";
		}
		else if ((npcId == ZIMENF) && (cond > 0) && (st.getQuestItemsCount(ZIMENFS_PAY) == 1))
		{
			htmltext = "30538-03.htm";
		}
		else if ((npcId == TOMA) && (cond > 0) && (st.getQuestItemsCount(MIONS_LETTER) == 1))
		{
			st.takeItems(MIONS_LETTER, 1);
			st.giveItems(BEAR_PIC, 1);
			st.set("cond", "5");
			st.set("id", String.valueOf(0));
			htmltext = "30556-01.htm";
		}
		else if ((npcId == TOMA) && (cond > 0) && (st.getQuestItemsCount(BEAR_PIC) == 1) && (st.getQuestItemsCount(HONEY_JAR) < 5))
		{
			htmltext = "30556-02.htm";
		}
		else if ((npcId == TOMA) && (cond > 0) && (st.getQuestItemsCount(BEAR_PIC) == 1) && (st.getQuestItemsCount(HONEY_JAR) >= 5))
		{
			st.takeItems(HONEY_JAR, st.getQuestItemsCount(HONEY_JAR));
			st.takeItems(BEAR_PIC, 1);
			st.giveItems(TARANTULA_PIC, 1);
			st.set("cond", "7");
			htmltext = "30556-03.htm";
		}
		else if ((npcId == TOMA) && (cond > 0) && (st.getQuestItemsCount(TARANTULA_PIC) == 1) && (st.getQuestItemsCount(BEAD) < 20))
		{
			htmltext = "30556-04.htm";
		}
		else if ((npcId == TOMA) && (cond > 0) && (st.getQuestItemsCount(TARANTULA_PIC) == 1) && (st.getQuestItemsCount(BEAD) >= 20))
		{
			st.takeItems(BEAD, st.getQuestItemsCount(BEAD));
			st.takeItems(TARANTULA_PIC, 1);
			st.giveItems(BEAD_PARCEL, 1);
			st.set("cond", "9");
			htmltext = "30556-05.htm";
		}
		else if ((npcId == TOMA) && (cond > 0) && (st.getQuestItemsCount(BEAD_PARCEL) > 0))
		{
			htmltext = "30556-06.htm";
		}
		else if ((npcId == TOMA) && (cond > 0) && ((st.getQuestItemsCount(ROUTS_TP_SCROLL) > 0) || (st.getQuestItemsCount(SUCCUBUS_UNDIES) > 0)))
		{
			htmltext = "30556-07.htm";
		}
		else if ((npcId == RAUT) && (cond > 0) && (st.getQuestItemsCount(BEAD_PARCEL) == 1))
		{
			htmltext = "30316-01.htm";
		}
		else if ((npcId == RAUT) && (cond > 0) && (st.getQuestItemsCount(ROUTS_TP_SCROLL) == 1))
		{
			htmltext = "30316-04.htm";
		}
		else if ((npcId == RAUT) && (cond > 0) && (st.getQuestItemsCount(SUCCUBUS_UNDIES) == 1))
		{
			st.takeItems(SUCCUBUS_UNDIES, 1);
			String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
			if (isFinished.equalsIgnoreCase(""))
			{
				st.addExpAndSp(295862, 9650);
			}
			st.giveItems(RING_OF_RAVEN, 1);
			st.saveGlobalQuestVar("1ClassQuestFinished", "1");
			st.set("cond", "0");
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
			htmltext = "30316-05.htm";
		}
		else if ((npcId == 30557) && (cond > 0) && (st.getQuestItemsCount(ROUTS_TP_SCROLL) == 1))
		{
			htmltext = "30557-01.htm";
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
		
		if (npcId == HUNTER_BEAR)
		{
			if ((cond > 0) && (st.getQuestItemsCount(BEAR_PIC) == 1) && (st.getQuestItemsCount(HONEY_JAR) < 5))
			{
				if (st.getInt("id") > 20)
				{
					int n = ((st.getInt("id") - 20) * 10);
					if (st.getRandom(100) <= n)
					{
						st.addSpawn(HONEY_BEAR);
						st.set("id", "0");
					}
					else
					{
						st.set("id", String.valueOf(st.getInt("id") + 1));
					}
				}
				else
				{
					st.set("id", String.valueOf(st.getInt("id") + 1));
				}
			}
		}
		
		else if (npcId == HONEY_BEAR)
		{
			if ((cond > 0) && (st.getQuestItemsCount(BEAR_PIC) == 1) && (st.getQuestItemsCount(HONEY_JAR) < 5))
			{
				if (((L2Attackable) npc).isSpoil())
				{
					st.giveItems(HONEY_JAR, 1);
					if (st.getQuestItemsCount(HONEY_JAR) == 5)
					{
						st.playSound("ItemSound.quest_middle");
						st.set("cond", "6");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
		}
		else if (npcId == HUNTER_TARANTULA)
		{
			if ((cond > 0) && (st.getQuestItemsCount(TARANTULA_PIC) == 1) && (st.getQuestItemsCount(BEAD) < 20))
			{
				if (((L2Attackable) npc).isSpoil())
				{
					if (st.getRandom(2) == 0)
					{
						st.giveItems(BEAD, 1);
						if (st.getQuestItemsCount(BEAD) == 20)
						{
							st.playSound("ItemSound.quest_middle");
							st.set("cond", "8");
						}
						else
						{
							st.playSound("ItemSound.quest_itemget");
						}
					}
				}
			}
		}
		else if (npcId == PLUNDER_TARANTULA)
		{
			if ((cond > 0) && (st.getQuestItemsCount(TARANTULA_PIC) == 1) && (st.getQuestItemsCount(BEAD) < 20))
			{
				if (((L2Attackable) npc).isSpoil())
				{
					if (st.getRandom(10) < 6)
					{
						st.giveItems(BEAD, 1);
						if (st.getQuestItemsCount(BEAD) == 20)
						{
							st.playSound("ItemSound.quest_middle");
							st.set("cond", "8");
						}
						else
						{
							st.playSound("ItemSound.quest_itemget");
						}
					}
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _417_PathToScavenger(417, qn, "");
	}
}