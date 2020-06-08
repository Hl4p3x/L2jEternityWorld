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
import l2e.util.Rnd;

/**
 * Created by LordWinter 06.04.2013 Based on L2J Eternity-World
 */
public class _343_UndertheShadowoftheIvoryTower extends Quest
{
	private static final String qn = "_343_UndertheShadowoftheIvoryTower";
	
	public final int CEMA = 30834;
	public final int ICARUS = 30835;
	public final int MARSHA = 30934;
	public final int TRUMPIN = 30935;
	
	public final int[] MOBS =
	{
		20563,
		20564,
		20565,
		20566
	};
	
	public final int ORB = 4364;
	public final int ECTOPLASM = 4365;
	
	public final int[] AllowClass =
	{
		0xb,
		0xc,
		0xd,
		0xe,
		0x1a,
		0x1b,
		0x1c,
		0x27,
		0x28,
		0x29
	};
	
	public final int CHANCE = 50;
	
	public _343_UndertheShadowoftheIvoryTower(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(CEMA);
		addTalkId(CEMA);
		addTalkId(ICARUS);
		addTalkId(MARSHA);
		addTalkId(TRUMPIN);
		
		for (int i : MOBS)
		{
			addKillId(i);
		}
		
		questItemIds = new int[]
		{
			ORB
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		String htmltext = event;
		int random1 = getRandom(3);
		int random2 = getRandom(2);
		long orbs = st.getQuestItemsCount(ORB);
		if (event.equalsIgnoreCase("30834-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30834-08.htm"))
		{
			if (orbs > 0)
			{
				st.giveAdena(orbs * 120, true);
				st.takeItems(ORB, -1);
			}
			else
			{
				htmltext = "30834-08.htm";
			}
		}
		else if (event.equalsIgnoreCase("30834-09.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30934-02.htm") || event.equalsIgnoreCase("30934-03.htm"))
		{
			if (orbs < 10)
			{
				htmltext = "noorbs.htm";
			}
			else if (event.equalsIgnoreCase("30934-03.htm"))
			{
				if (orbs >= 10)
				{
					st.takeItems(ORB, 10);
					st.set("playing", "1");
				}
				else
				{
					htmltext = "noorbs.htm";
				}
			}
		}
		else if (event.equalsIgnoreCase("30934-04.htm"))
		{
			if (st.getInt("playing") > 0)
			{
				if (random1 == 0)
				{
					htmltext = "30934-05.htm";
					st.giveItems(ORB, 10);
				}
				else if (random1 == 1)
				{
					htmltext = "30934-06.htm";
				}
				else
				{
					htmltext = "30934-04.htm";
					st.giveItems(ORB, 20);
				}
				st.unset("playing");
			}
			else
			{
				htmltext = "Player is cheating";
				st.takeItems(ORB, -1);
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("30934-05.htm"))
		{
			if (st.getInt("playing") > 0)
			{
				if (random1 == 0)
				{
					htmltext = "30934-04.htm";
					st.giveItems(ORB, 20);
				}
				else if (random1 == 1)
				{
					htmltext = "30934-05.htm";
					st.giveItems(ORB, 10);
				}
				else
				{
					htmltext = "30934-06.htm";
				}
				st.unset("playing");
			}
			else
			{
				htmltext = "Player is cheating";
				st.takeItems(ORB, -1);
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("30934-06.htm"))
		{
			if (st.getInt("playing") > 0)
			{
				if (random1 == 0)
				{
					htmltext = "30934-04.htm";
					st.giveItems(ORB, 20);
				}
				else if (random1 == 1)
				{
					htmltext = "30934-06.htm";
				}
				else
				{
					htmltext = "30934-05.htm";
					st.giveItems(ORB, 10);
				}
				st.unset("playing");
			}
			else
			{
				htmltext = "Player is cheating";
				st.takeItems(ORB, -1);
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("30935-02.htm") || event.equalsIgnoreCase("30935-03.htm"))
		{
			st.unset("toss");
			if (orbs < 10)
			{
				htmltext = "noorbs.htm";
			}
		}
		else if (event.equalsIgnoreCase("30935-05.htm"))
		{
			if (orbs >= 10)
			{
				if (random2 == 0)
				{
					int toss = st.getInt("toss");
					if (toss == 4)
					{
						st.unset("toss");
						st.giveItems(ORB, 150);
						htmltext = "30935-07.htm";
					}
					else
					{
						st.set("toss", String.valueOf(toss + 1));
						htmltext = "30935-04.htm";
					}
				}
				else
				{
					st.unset("toss");
					st.takeItems(ORB, 10);
				}
			}
			else
			{
				htmltext = "noorbs.htm";
			}
		}
		else if (event.equalsIgnoreCase("30935-06.htm"))
		{
			if (orbs >= 10)
			{
				int toss = st.getInt("toss");
				st.unset("toss");
				if (toss == 1)
				{
					st.giveItems(ORB, 10);
				}
				else if (toss == 2)
				{
					st.giveItems(ORB, 30);
				}
				else if (toss == 3)
				{
					st.giveItems(ORB, 70);
				}
				else if (toss == 4)
				{
					st.giveItems(ORB, 150);
				}
			}
			else
			{
				htmltext = "noorbs.htm";
			}
		}
		else if (event.equalsIgnoreCase("30835-02.htm"))
		{
			if (st.getQuestItemsCount(ECTOPLASM) > 0)
			{
				st.takeItems(ECTOPLASM, 1);
				int random = getRandom(1000);
				if (random <= 119)
				{
					st.giveItems(955, 1);
				}
				else if (random <= 169)
				{
					st.giveItems(951, 1);
				}
				else if (random <= 329)
				{
					st.giveItems(2511, getRandom(200) + 401);
				}
				else if (random <= 559)
				{
					st.giveItems(2510, getRandom(200) + 401);
				}
				else if (random <= 561)
				{
					st.giveItems(316, 1);
				}
				else if (random <= 578)
				{
					st.giveItems(630, 1);
				}
				else if (random <= 579)
				{
					st.giveItems(188, 1);
				}
				else if (random <= 581)
				{
					st.giveItems(885, 1);
				}
				else if (random <= 582)
				{
					st.giveItems(103, 1);
				}
				else if (random <= 584)
				{
					st.giveItems(917, 1);
				}
				else
				{
					st.giveItems(736, 1);
				}
			}
			else
			{
				htmltext = "30835-03.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		int npcId = npc.getId();
		String htmltext = getNoQuestMsg(player);
		int id = st.getState();
		if (npcId == CEMA)
		{
			if (id != State.STARTED)
			{
				for (int i : AllowClass)
				{
					if ((player.getClassId().getId() == i) && (player.getLevel() >= 40))
					{
						htmltext = "30834-01.htm";
					}
				}
				if (!htmltext.equals("30834-01.htm"))
				{
					htmltext = "30834-07.htm";
					st.exitQuest(true);
				}
			}
			else if (st.getQuestItemsCount(ORB) > 0)
			{
				htmltext = "30834-06.htm";
			}
			else
			{
				htmltext = "30834-05.htm";
			}
		}
		else if (npcId == ICARUS)
		{
			htmltext = "30835-01.htm";
		}
		else if (npcId == MARSHA)
		{
			htmltext = "30934-01.htm";
		}
		else if (npcId == TRUMPIN)
		{
			htmltext = "30935-01.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		if (st.getState() != State.STARTED)
		{
			return null;
		}
		
		if (Rnd.chance(CHANCE))
		{
			st.giveItems(ORB, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _343_UndertheShadowoftheIvoryTower(343, qn, "");
	}
}