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
import l2e.gameserver.network.serverpackets.MagicSkillUse;

/**
 * Created by LordWinter 06.07.2012
 * Based on L2J Eternity-World
 */
public final class _120_PavelsResearch extends Quest
{
	private static final String qn = "_120_PavelsResearch";

	// NPC
	private static final int YUMI = 32041;
	private static final int WEATHER1 = 32042;
	private static final int WEATHER2 = 32043;
	private static final int WEATHER3 = 32044;
	private static final int BOOKSHELF = 32045;
	private static final int STONES = 32046;
	private static final int WENDY = 32047;

	// ITEMS	
	private static final int EARPHOENIX = 6324;
	private static final int REPORT = 8058;
	private static final int REPORT2 = 8059;
	private static final int ENIGMA = 8060;
	private static final int FLOWER = 8290;
	private static final int HEART = 8291;
	private static final int NECKLACE = 8292;	
	
	public _120_PavelsResearch(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(STONES);
		addTalkId(STONES, BOOKSHELF, WEATHER1, WEATHER2, WEATHER3, WENDY, YUMI);

		questItemIds = new int[] { REPORT, REPORT2, ENIGMA, FLOWER, HEART, NECKLACE };
	}		
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
				
		if(event.equalsIgnoreCase("32041-03.htm"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32041-04.htm"))
		{
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32041-12.htm"))
		{
			st.set("cond", "8");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32041-16.htm"))
		{
			st.set("cond", "16");
			st.giveItems(ENIGMA, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32041-22.htm"))
		{
			st.set("cond", "17");
			st.takeItems(ENIGMA, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32041-32.htm"))
		{
			st.takeItems(NECKLACE, 1);
			st.giveItems(EARPHOENIX, 1);
			st.giveItems(57,783720);
			st.addExpAndSp(3447315, 272615);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		else if(event.equalsIgnoreCase("32042-06.htm"))
		{
			if(st.getInt("cond") == 10)
				if(st.getInt("talk") + st.getInt("talk1") == 2)
				{
					st.set("cond", "11");
					st.set("talk", "0");
					st.set("talk1", "0");
					st.playSound("ItemSound.quest_middle");
				}
				else
					htmltext = "32042-03.htm";
		}
		else if(event.equalsIgnoreCase("32042-10.htm"))
		{
			if(st.getInt("talk") + st.getInt("talk1") + st.getInt("talk2") == 3)
				htmltext = "32042-14.htm";
		}
		else if(event.equalsIgnoreCase("32042-11.htm"))
		{
			if(st.getInt("talk") == 0)
				st.set("talk", "1");
		}
		else if(event.equalsIgnoreCase("32042-12.htm"))
		{
			if(st.getInt("talk1") == 0)
				st.set("talk1", "1");
		}
		else if(event.equalsIgnoreCase("32042-13.htm"))
		{
			if(st.getInt("talk2") == 0)
				st.set("talk2", "1");
		}
		else if(event.equalsIgnoreCase("32042-15.htm"))
		{
			st.set("cond", "12");
			st.set("talk", "0");
			st.set("talk1", "0");
			st.set("talk2", "0");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32043-06.htm"))
		{
			if(st.getInt("cond") == 17)
				if(st.getInt("talk") + st.getInt("talk1") == 2)
				{
					st.set("cond", "18");
					st.set("talk", "0");
					st.set("talk1", "0");
					st.playSound("ItemSound.quest_middle");
				}
				else
					htmltext = "32043-03.htm";
		}
		else if(event.equalsIgnoreCase("32043-15.htm"))
		{
			if(st.getInt("talk") + st.getInt("talk1") == 2)
				htmltext = "32043-29.htm";
		}
		else if(event.equalsIgnoreCase("32043-18.htm"))
		{
			if(st.getInt("talk") == 1)
				htmltext = "32043-21.htm";
		}
		else if(event.equalsIgnoreCase("32043-20.htm"))
		{
			st.set("talk", "1");
			st.playSound("AmbSound.ed_drone_02");
		}
		else if(event.equalsIgnoreCase("32043-28.htm"))
			st.set("talk1", "1");
		else if(event.equalsIgnoreCase("32043-30.htm"))
		{
			st.set("cond", "19");
			st.set("talk", "0");
			st.set("talk1", "0");
		}
		else if(event.equalsIgnoreCase("32044-06.htm"))
		{
			if(st.getInt("cond") == 20)
			{
				if(st.getInt("talk") + st.getInt("talk1") == 2)
				{
					st.set("cond", "21");
					st.set("talk", "0");
					st.set("talk1", "0");
					st.playSound("ItemSound.quest_middle");
				}
				else
					htmltext = "32044-03.htm";
			}	
		}
		else if(event.equalsIgnoreCase("32044-08.htm"))
		{
			if(st.getInt("talk") + st.getInt("talk1") == 2)
				htmltext = "32044-11.htm";
		}
		else if(event.equalsIgnoreCase("32044-09.htm"))
		{
			if(st.getInt("talk") == 0)
				st.set("talk", "1");
		}
		else if(event.equalsIgnoreCase("32044-10.htm"))
		{
			if(st.getInt("talk1") == 0)
				st.set("talk1", "1");
		}
		else if(event.equalsIgnoreCase("32044-17.htm"))
		{
			st.set("cond", "22");
			st.set("talk", "0");
			st.set("talk1", "0");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32045-02.htm"))
		{
			st.set("cond", "15");
			st.playSound("ItemSound.quest_middle");
			st.giveItems(REPORT, 1);
			npc.broadcastPacket(new MagicSkillUse(npc, player, 5073, 5, 1500, 0));
		}
		else if(event.equalsIgnoreCase("32046-04.htm") || event.equalsIgnoreCase("32046-05.htm"))
			st.exitQuest(true);
		else if(event.equalsIgnoreCase("32046-06.htm"))
		{
			if(player.getLevel() >= 70)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
			else
			{
				htmltext = "32046-00.htm";
				st.exitQuest(true);
			}
		}
		else if(event.equalsIgnoreCase("32046-08.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32046-12.htm"))
		{
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
			st.giveItems(FLOWER, 1);
		}
		else if(event.equalsIgnoreCase("32046-22.htm"))
		{
			st.set("cond", "10");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32046-29.htm"))
		{
			st.set("cond", "13");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32046-35.htm"))
		{
			st.set("cond", "20");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32046-38.htm"))
		{
			st.set("cond", "23");
			st.playSound("ItemSound.quest_middle");
			st.giveItems(HEART, 1);
		}
		else if(event.equalsIgnoreCase("32047-06.htm"))
		{
			st.set("cond", "5");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32047-10.htm"))
		{
			st.set("cond", "7");
			st.playSound("ItemSound.quest_middle");
			st.takeItems(FLOWER, 1);
		}
		else if(event.equalsIgnoreCase("32047-15.htm"))
		{
			st.set("cond", "9");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32047-18.htm"))
		{
			st.set("cond", "14");
			st.playSound("ItemSound.quest_middle");
		}
		else if(event.equalsIgnoreCase("32047-26.htm"))
		{
			st.set("cond", "24");
			st.playSound("ItemSound.quest_middle");
			st.takeItems(HEART, 1);
		}
		else if(event.equalsIgnoreCase("32047-32.htm"))
		{
			st.set("cond", "25");
			st.playSound("ItemSound.quest_middle");
			st.giveItems(NECKLACE, 1);
		}
		else if(event.equalsIgnoreCase("w1_1"))
		{
			st.set("talk", "1");
			htmltext = "32042-04.htm";
		}
		else if(event.equalsIgnoreCase("w1_2"))
		{
			st.set("talk1", "1");
			htmltext = "32042-05.htm";
		}
		else if(event.equalsIgnoreCase("w2_1"))
		{
			st.set("talk", "1");
			htmltext = "32043-04.htm";
		}
		else if(event.equalsIgnoreCase("w2_2"))
		{
			st.set("talk1", "1");
			htmltext = "32043-05.htm";
		}
		else if(event.equalsIgnoreCase("w3_1"))
		{
			st.set("talk", "1");
			htmltext = "32044-04.htm";
		}
		else if(event.equalsIgnoreCase("w3_2"))
		{
			st.set("talk1", "1");
			htmltext = "32044-05.htm";
		}	
		return htmltext;
	}	

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if(st == null)
			return htmltext;

		QuestState qs = player.getQuestState("_114_ResurrectionOfAnOldManager");
		int cond = st.getInt("cond");
		int npcId = npc.getId();

		switch (st.getState())
		{		
			case State.CREATED:
				if(player.getLevel() >= 70 && qs.isCompleted())
					htmltext = "32046-01.htm";
				else
				{
					htmltext = "32046-00.htm";
					st.exitQuest(true);
				}		
				break;
			case State.STARTED:	
				if(npcId == STONES)
				{			
					if(cond == 1)
						htmltext = "32046-06.htm";
					else if(cond == 2)
						htmltext = "32046-09.htm";
					else if(cond == 5)
						htmltext = "32046-10.htm";
					else if(cond == 6)
						htmltext = "32046-13.htm";
					else if(cond == 9)
						htmltext = "32046-14.htm";
					else if(cond == 10)
						htmltext = "32046-23.htm";
					else if(cond == 12)
						htmltext = "32046-26.htm";
					else if(cond == 13)
						htmltext = "32046-30.htm";
					else if(cond == 19)
						htmltext = "32046-31.htm";
					else if(cond == 20)
						htmltext = "32046-36.htm";
					else if(cond == 22)
						htmltext = "32046-37.htm";
					else if(cond == 23)
						htmltext = "32046-39.htm";
				}
				else if(npcId == WENDY)
				{
					if(cond == 2 || cond == 3 || cond == 4)
						htmltext = "32047-01.htm";
					else if(cond == 5)
						htmltext = "32047-07.htm";
					else if(cond == 6)
						htmltext = "32047-08.htm";
					else if(cond == 7)
						htmltext = "32047-11.htm";
					else if(cond == 8)
						htmltext = "32047-12.htm";
					else if(cond == 9)
						htmltext = "32047-15.htm";
					else if(cond == 13)
						htmltext = "32047-16.htm";
					else if(cond == 14)
						htmltext = "32047-19.htm";
					else if(cond == 15)
						htmltext = "32047-20.htm";
					else if(cond == 23)
						htmltext = "32047-21.htm";
					else if(cond == 24)
						htmltext = "32047-26.htm";
					else if(cond == 25)
						htmltext = "32047-33.htm";
				}
				else if(npcId == YUMI)
				{
					if(cond == 2)
						htmltext = "32041-01.htm";
					else if(cond == 3)
						htmltext = "32041-05.htm";
					else if(cond == 4)
						htmltext = "32041-06.htm";
					else if(cond == 7)
						htmltext = "32041-07.htm";
					else if(cond == 8)
						htmltext = "32041-13.htm";
					else if(cond == 15)
						htmltext = "32041-14.htm";
					else if(cond == 16)
					{
						if(st.getQuestItemsCount(REPORT2) == 0)
							htmltext = "32041-17.htm";
						else
							htmltext = "32041-18.htm";
					}
					else if(cond == 17)
						htmltext = "32041-22.htm";
					else if(cond == 25)
						htmltext = "32041-26.htm";
				}
				else if(npcId == WEATHER1)
				{
					if(cond == 10)
						htmltext = "32042-01.htm";
					else if(cond == 11)
					{
						if(st.getInt("talk") + st.getInt("talk1") + st.getInt("talk2") == 3)
							htmltext = "32042-14.htm";
						else
							htmltext = "32042-06.htm";
					}
					else if(cond == 12)
						htmltext = "32042-15.htm";
				}
				else if(npcId == WEATHER2)
				{
					if(cond == 17)
						htmltext = "32043-01.htm";
					else if(cond == 18)
					{
						if(st.getInt("talk") + st.getInt("talk1") == 2)
							htmltext = "32043-29.htm";
						else
							htmltext = "32043-06.htm";
					}
					else if(cond == 19)
						htmltext = "32043-30.htm";
				}
				else if(npcId == WEATHER3)
				{
					if(cond == 20)
						htmltext = "32044-01.htm";
					else if(cond == 21)
						htmltext = "32044-06.htm";
					else if(cond == 22)
						htmltext = "32044-18.htm";
				}
				else if(npcId == BOOKSHELF)
					if(cond == 14)
						htmltext = "32045-01.htm";
					else if(cond == 15)
						htmltext = "32045-03.htm";	
				break;
		}
		return htmltext;
	}	
	public static void main(String[] args)
	{
		new _120_PavelsResearch(120, qn, "");
	}
}