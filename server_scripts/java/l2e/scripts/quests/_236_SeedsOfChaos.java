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

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.util.Rnd;

/**
 * Created by LordWinter 22.01.2013 Based on L2J Eternity-World
 */
public class _236_SeedsOfChaos extends Quest
{
	private final static String qn = "_236_SeedsOfChaos";
	
	private final static int KEKROPUS = 32138;
	private final static int WIZARD = 31522;
	private final static int KATENAR = 32333;
	private final static int ROCK = 32238;
	private final static int HARKILGAMED = 32236;
	private final static int MAO = 32190;
	private final static int RODENPICULA = 32237;
	private final static int NORNIL = 32239;
	
	private final static int[] NEEDLE_STAKATO_DRONES =
	{
		21516,
		21517
	};
	
	private final static int[] SPLENDOR_MOBS =
	{
		21520,
		21521,
		21522,
		21523,
		21524,
		21525,
		21526,
		21527,
		21528,
		21529,
		21530,
		21531,
		21532,
		21533,
		21534,
		21535,
		21536,
		21537,
		21538,
		21539,
		21540,
		21541
	};
	
	private final static int STAR_OF_DESTINY = 5011;
	private final static int SCROLL_ENCHANT_WEAPON_A = 729;
	
	private final static int SHINING_MEDALLION = 9743;
	private final static int BLACK_ECHO_CRYSTAL = 9745;
	
	protected static boolean KATENAR_SPAWNED = false;
	protected static boolean HARKILGAMED_SPAWNED = false;
	
	public _236_SeedsOfChaos(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(KEKROPUS);
		addTalkId(KEKROPUS);
		addTalkId(WIZARD);
		addTalkId(KATENAR);
		addTalkId(ROCK);
		addTalkId(HARKILGAMED);
		addTalkId(MAO);
		addTalkId(RODENPICULA);
		addTalkId(NORNIL);
		
		for (int kill_id : NEEDLE_STAKATO_DRONES)
		{
			addKillId(kill_id);
		}
		
		for (int kill_id : SPLENDOR_MOBS)
		{
			addKillId(kill_id);
		}
		
		questItemIds = new int[]
		{
			SHINING_MEDALLION,
			BLACK_ECHO_CRYSTAL
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("1"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
			htmltext = "32138_02b.htm";
		}
		else if (event.equalsIgnoreCase("1_yes"))
		{
			htmltext = "31522_01c.htm";
		}
		else if (event.equalsIgnoreCase("1_no"))
		{
			htmltext = "31522_01no.htm";
		}
		else if (event.equalsIgnoreCase("2"))
		{
			st.set("cond", "2");
			htmltext = "31522_02.htm";
		}
		else if (event.equalsIgnoreCase("31522_03b.htm") && (st.getQuestItemsCount(BLACK_ECHO_CRYSTAL) > 0))
		{
			st.takeItems(BLACK_ECHO_CRYSTAL, -1);
			htmltext = event + ".htm";
		}
		else if (event.equalsIgnoreCase("4"))
		{
			st.set("cond", "4");
			if (!KATENAR_SPAWNED)
			{
				st.addSpawn(KATENAR, 120000);
				ThreadPoolManager.getInstance().scheduleGeneral(new OnDespawn(true), 120000);
				KATENAR_SPAWNED = true;
			}
			return null;
		}
		else if (event.equalsIgnoreCase("5"))
		{
			st.set("cond", "5");
			htmltext = "32235_02.htm";
		}
		else if (event.equalsIgnoreCase("spawn_harkil"))
		{
			if (!HARKILGAMED_SPAWNED)
			{
				st.addSpawn(HARKILGAMED, 120000);
				ThreadPoolManager.getInstance().scheduleGeneral(new OnDespawn(false), 120000);
				HARKILGAMED_SPAWNED = true;
			}
			return null;
		}
		else if (event.equalsIgnoreCase("6"))
		{
			st.set("cond", "12");
			htmltext = "32236_06.htm";
		}
		else if (event.equalsIgnoreCase("8"))
		{
			st.set("cond", "14");
			htmltext = "32236_08.htm";
		}
		else if (event.equalsIgnoreCase("9"))
		{
			st.set("cond", "15");
			htmltext = "32138_09.htm";
		}
		else if (event.equalsIgnoreCase("10"))
		{
			st.set("cond", "16");
			player.teleToLocation(-119534, 87176, -12593);
			htmltext = "32190_02.htm";
		}
		else if (event.equalsIgnoreCase("11"))
		{
			st.set("cond", "17");
			htmltext = "32237_11.htm";
		}
		else if (event.equalsIgnoreCase("12"))
		{
			st.set("cond", "18");
			htmltext = "32239_12.htm";
		}
		else if (event.equalsIgnoreCase("13"))
		{
			st.set("cond", "19");
			htmltext = "32237_13.htm";
		}
		else if (event.equalsIgnoreCase("14"))
		{
			st.set("cond", "20");
			htmltext = "32239_14.htm";
		}
		else if (event.equalsIgnoreCase("15"))
		{
			st.giveItems(SCROLL_ENCHANT_WEAPON_A, 1, true);
			st.setState(State.COMPLETED);
			htmltext = "32237_15.htm";
		}
		else
		{
			htmltext = event + ".htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.CREATED:
				if (npcId == KEKROPUS)
				{
					if (player.getRace() != Race.Kamael)
					{
						st.exitQuest(true);
						htmltext = "32138_00.htm";
					}
					else if (player.getLevel() < 75)
					{
						st.exitQuest(true);
						htmltext = "32138_01.htm";
					}
					else if (st.getQuestItemsCount(STAR_OF_DESTINY) < 1)
					{
						htmltext = "32138_01a.htm";
						st.exitQuest(true);
					}
					else
					{
						htmltext = "32138_02.htm";
					}
				}
				break;
			case State.STARTED:
				if (npcId == KEKROPUS)
				{
					if (cond < 14)
					{
						htmltext = "32138_02c.htm";
					}
					else if (cond == 14)
					{
						htmltext = "32138_08.htm";
					}
					else
					{
						htmltext = "32138_10.htm";
					}
				}
				else if (npcId == WIZARD)
				{
					if (cond == 1)
					{
						htmltext = "31522_01.htm";
					}
					else if (cond == 2)
					{
						htmltext = "31522_02a.htm";
					}
					else if ((cond == 3) || ((cond == 4) && !KATENAR_SPAWNED))
					{
						htmltext = "31522_03.htm";
					}
					else
					{
						htmltext = "31522_04.htm";
					}
				}
				else if (npcId == KATENAR)
				{
					if (cond == 4)
					{
						htmltext = "32235_01.htm";
					}
					else if (cond >= 5)
					{
						htmltext = "32235_02.htm";
					}
				}
				else if (npcId == ROCK)
				{
					if ((cond == 5) || (cond == 13))
					{
						htmltext = "32238-01.htm";
					}
					else
					{
						htmltext = "32238-00.htm";
					}
				}
				else if (npcId == HARKILGAMED)
				{
					if (cond == 5)
					{
						htmltext = "32236_05.htm";
					}
					else if (cond == 12)
					{
						htmltext = "32236_06.htm";
					}
					else if (cond == 13)
					{
						st.takeItems(SHINING_MEDALLION, -1);
						htmltext = "32236_07.htm";
					}
					else if (cond > 13)
					{
						htmltext = "32236_09.htm";
					}
				}
				else if (npcId == MAO)
				{
					if ((cond == 15) || (cond == 16))
					{
						htmltext = "32190_01.htm";
					}
				}
				else if (npcId == RODENPICULA)
				{
					if (cond == 16)
					{
						htmltext = "32237_10.htm";
					}
					else if (cond == 17)
					{
						htmltext = "32237_11.htm";
					}
					else if (cond == 18)
					{
						htmltext = "32237_12.htm";
					}
					else if (cond == 19)
					{
						htmltext = "32237_13.htm";
					}
					else if (cond == 20)
					{
						htmltext = "32237_14.htm";
					}
				}
				else if (npcId == NORNIL)
				{
					if (cond == 17)
					{
						htmltext = "32239_11.htm";
					}
					else if (cond == 18)
					{
						htmltext = "32239_12.htm";
					}
					else if (cond == 19)
					{
						htmltext = "32239_13.htm";
					}
					else if (cond == 20)
					{
						htmltext = "32239_14.htm";
					}
				}
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		
		if (IsInIntArray(npcId, NEEDLE_STAKATO_DRONES))
		{
			if ((cond == 2) && (st.getQuestItemsCount(BLACK_ECHO_CRYSTAL) == 0) && Rnd.chance((int) (15 * Config.RATE_QUEST_DROP)))
			{
				st.giveItems(BLACK_ECHO_CRYSTAL, 1);
				st.set("cond", "3");
				st.playSound("Itemsound.quest_middle");
			}
		}
		else if (IsInIntArray(npcId, SPLENDOR_MOBS))
		{
			if ((cond == 12) && (st.getQuestItemsCount(SHINING_MEDALLION) < 62) && Rnd.chance((int) (20 * Config.RATE_QUEST_DROP)))
			{
				st.giveItems(SHINING_MEDALLION, 1);
				if (st.getQuestItemsCount(SHINING_MEDALLION) < 62)
				{
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					st.set("cond", "13");
					st.playSound("Itemsound.quest_middle");
				}
			}
		}
		return null;
	}
	
	private static boolean IsInIntArray(int i, int[] a)
	{
		for (int _i : a)
		{
			if (_i == i)
			{
				return true;
			}
		}
		return false;
	}
	
	private static class OnDespawn implements Runnable
	{
		private final boolean _SUBJ_KATENAR;
		
		public OnDespawn(boolean SUBJ_KATENAR)
		{
			_SUBJ_KATENAR = SUBJ_KATENAR;
		}
		
		@Override
		public void run()
		{
			if (_SUBJ_KATENAR)
			{
				KATENAR_SPAWNED = false;
			}
			else
			{
				HARKILGAMED_SPAWNED = false;
			}
		}
	}
	
	public static void main(String[] args)
	{
		new _236_SeedsOfChaos(236, qn, "");
	}
}