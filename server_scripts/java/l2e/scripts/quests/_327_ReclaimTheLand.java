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

import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.util.Rnd;

/**
 * Created by LordWinter 20.01.2013 Based on L2J Eternity-World
 */
public class _327_ReclaimTheLand extends Quest
{
	private static final String qn = "_327_ReclaimTheLand";

	private static int PIOTUR = 30597;
	private static int IRIS = 30034;
	private static int ASHA = 30313;

	private static int TUREK_DOGTAG = 1846;
	private static int TUREK_MEDALLION = 1847;
	private static int CLAY_URN_FRAGMENT = 1848;
	private static int BRASS_TRINKET_PIECE = 1849;
	private static int BRONZE_MIRROR_PIECE = 1850;
	private static int JADE_NECKLACE_BEAD = 1851;
	private static int ANCIENT_CLAY_URN = 1852;
	private static int ANCIENT_BRASS_TIARA = 1853;
	private static int ANCIENT_BRONZE_MIRROR = 1854;
	private static int ANCIENT_JADE_NECKLACE = 1855;

	private static Map<Integer, int[]> DROPLIST = new HashMap<>();
	private static Map<Integer, Integer> EXP = new HashMap<>();

	public _327_ReclaimTheLand(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(PIOTUR);
		addTalkId(PIOTUR);
		addTalkId(IRIS);
		addTalkId(ASHA);

		DROPLIST.put(20495, new int[]{ TUREK_MEDALLION, 13 });
		DROPLIST.put(20496, new int[]{ TUREK_DOGTAG, 9 });
		DROPLIST.put(20497, new int[]{ TUREK_MEDALLION, 11 });
		DROPLIST.put(20498, new int[]{ TUREK_DOGTAG, 10 });
		DROPLIST.put(20499, new int[]{ TUREK_DOGTAG, 8 });
		DROPLIST.put(20500, new int[]{ TUREK_DOGTAG, 7 });
		DROPLIST.put(20501, new int[]{ TUREK_MEDALLION, 12 });

		EXP.put(ANCIENT_CLAY_URN, 913);
		EXP.put(ANCIENT_BRASS_TIARA, 1065);
		EXP.put(ANCIENT_BRONZE_MIRROR, 1065);
		EXP.put(ANCIENT_JADE_NECKLACE, 1294);

		for(int kill_id : DROPLIST.keySet())
		{
			addKillId(kill_id);
		}

		questItemIds = new int[]
		{
			TUREK_MEDALLION,
			TUREK_DOGTAG
		};
	}

	private static boolean ExpReward(QuestState st, int item_id)
	{
		Integer exp = EXP.get(item_id);
		if(exp == null)
			exp = 182;

		long exp_reward = st.getQuestItemsCount(item_id * exp);

		if(exp_reward == 0)
			return false;

		st.takeItems(item_id, -1);
		st.addExpAndSp((int)exp_reward, 0);
		st.playSound("ItemSound.quest_itemget");

		return true;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("30597-03.htm"))
		{
      			st.set("cond","1");
      			st.setState(State.STARTED);
      			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30597-06.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		else if(event.equalsIgnoreCase("30313-02.htm") && st.getQuestItemsCount(CLAY_URN_FRAGMENT) >= 5)
		{
			st.takeItems(CLAY_URN_FRAGMENT, 5);
			if(!Rnd.chance(80))
			{
				htmltext = "30313-10.htm";
			}
			st.giveItems(ANCIENT_CLAY_URN, 1);
			htmltext = "30313-03.htm";
		}
		else if(event.equalsIgnoreCase("30313-04.htm") && st.getQuestItemsCount(BRASS_TRINKET_PIECE) >= 5)
		{
			st.takeItems(BRASS_TRINKET_PIECE, 5);
			if(!Rnd.chance(80))
			{
				htmltext = "30313-10.htm";
			}
			st.giveItems(ANCIENT_BRASS_TIARA, 1);
			htmltext = "30313-05.htm";
		}
		else if(event.equalsIgnoreCase("30313-06.htm") && st.getQuestItemsCount(BRONZE_MIRROR_PIECE) >= 5)
		{
			st.takeItems(BRONZE_MIRROR_PIECE, 5);
			if(!Rnd.chance(80))
			{
				htmltext = "30313-10.htm";
			}
			st.giveItems(ANCIENT_BRONZE_MIRROR, 1);
			htmltext = "30313-07.htm";
		}
		else if(event.equalsIgnoreCase("30313-08.htm") && st.getQuestItemsCount(JADE_NECKLACE_BEAD) >= 5)
		{
			st.takeItems(JADE_NECKLACE_BEAD, 5);
			if(!Rnd.chance(80))
			{
				htmltext = "30313-10.htm";
			}
			st.giveItems(ANCIENT_JADE_NECKLACE, 1);
			htmltext = "30313-09.htm";
		}
		else if(event.equalsIgnoreCase("30034-03.htm"))
		{
			if(!ExpReward(st, CLAY_URN_FRAGMENT))
			{
        			htmltext = "30034-02.htm";
			}
		}
		else if(event.equalsIgnoreCase("30034-04.htm"))
		{
        		if(!ExpReward(st, BRASS_TRINKET_PIECE))
			{
       			 	htmltext = "30034-02.htm";
			}
		}
		else if(event.equalsIgnoreCase("30034-05.htm"))
		{
			if(!ExpReward(st, BRONZE_MIRROR_PIECE))
			{
        			htmltext = "30034-02.htm";
			}
		}
		else if(event.equalsIgnoreCase("30034-06.htm"))
		{
			if(!ExpReward(st, JADE_NECKLACE_BEAD))
			{
        			htmltext = "30034-02.htm";
			}
		}
		else if(event.equalsIgnoreCase("30034-07.htm"))
		{
			if(!(ExpReward(st, ANCIENT_CLAY_URN) || ExpReward(st, ANCIENT_BRASS_TIARA) || ExpReward(st, ANCIENT_BRONZE_MIRROR) || ExpReward(st, ANCIENT_JADE_NECKLACE)))
			{
				htmltext = "30034-02.htm";
			}
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

		int npcId = npc.getId();

		switch (st.getState())
		{
			case State.CREATED:
				if(player.getLevel() < 25)
				{
					st.exitQuest(true);
					htmltext = "30597-01.htm";
				}
				else
				{
					htmltext = "30597-02.htm";
				}
				break;
			case State.STARTED:
				if(npcId == PIOTUR)
				{
					long reward = st.getQuestItemsCount(TUREK_DOGTAG) * 40 + st.getQuestItemsCount(TUREK_MEDALLION) * 50;
					if(reward == 0)
					{
						htmltext = "30597-04.htm";
					}
					st.takeItems(TUREK_DOGTAG, -1);
					st.takeItems(TUREK_MEDALLION, -1);
					st.giveItems(57, reward);
					htmltext = "30597-05.htm";
				}
				else
				{
					htmltext = npcId +"-01.htm";
				}
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

		int chance = DROPLIST.get(npcId)[1];
		int item = DROPLIST.get(npcId)[0];

		if(getRandom(100) < chance)
		{
			int n = getRandom(100);
			if(n < 25)
			{
				st.giveItems(CLAY_URN_FRAGMENT, 1);
			}
			else if(n < 50)
			{
				st.giveItems(BRASS_TRINKET_PIECE, 1);
			}
			else if(n < 75)
			{
				st.giveItems(BRONZE_MIRROR_PIECE, 1);
			}
			else
			{
				st.giveItems(JADE_NECKLACE_BEAD, 1);
			}
		}
   		st.giveItems(item, 1);
   		st.playSound("ItemSound.quest_itemget");

		return null;
	}
	
	public static void main(String[] args)
	{
		new _327_ReclaimTheLand(327, qn, "");
	}
}