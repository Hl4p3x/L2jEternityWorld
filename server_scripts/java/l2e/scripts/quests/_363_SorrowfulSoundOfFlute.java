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
 * Created by LordWinter 13.01.2013 Based on L2J Eternity-World
 */
public class _363_SorrowfulSoundOfFlute extends Quest
{
	private static final String qn = "_363_SorrowfulSoundOfFlute";
	
	private static final int NANARIN = 30956;
	private static final int OPIX = 30595;
	private static final int ALDO = 30057;
	private static final int RANSPO = 30594;
	private static final int HOLVAS = 30058;
	private static final int BARBADO = 30959;
	private static final int POITAN = 30458;
	
	private static final int NANARINS_FLUTE = 4319;
	private static final int BLACK_BEER = 4320;
	private static final int CLOTHES = 4318;
	
	private static final int THEME_OF_SOLITUDE = 4420;
	
	public _363_SorrowfulSoundOfFlute(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(NANARIN);
		addTalkId(NANARIN, OPIX, ALDO, RANSPO, HOLVAS, BARBADO, POITAN);

		questItemIds = new int[]
		{
			NANARINS_FLUTE,
			BLACK_BEER,
			CLOTHES
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30956-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30956-05.htm"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
			st.giveItems(CLOTHES, 1);
		}
		else if (event.equalsIgnoreCase("30956-06.htm"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
			st.giveItems(NANARINS_FLUTE, 1);
		}
		else if (event.equalsIgnoreCase("30956-07.htm"))
		{
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
			st.giveItems(BLACK_BEER, 1);
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
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() < 15)
				{
					htmltext = "30956-03.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "30956-01.htm";
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getId())
				{
					case NANARIN:
						if (cond == 1)
							htmltext = "30956-02.htm";
						else if (cond == 2)
							htmltext = "30956-04.htm";
						else if (cond == 3)
							htmltext = "30956-08.htm";
						else if (cond == 4)
						{
							if (st.getInt("success") == 1)
							{
								htmltext = "30956-09.htm";
								st.giveItems(THEME_OF_SOLITUDE, 1);
								st.playSound("ItemSound.quest_finish");
							}
							else
							{
								htmltext = "30956-10.htm";
								st.playSound("ItemSound.quest_giveup");
							}
							st.exitQuest(true);
						}
						break;
					
					case OPIX:
					case POITAN:
					case ALDO:
					case RANSPO:
					case HOLVAS:
						htmltext = npc.getId() + "-01.htm";
						if (cond == 1)
						{
							st.set("cond", "2");
							st.playSound("ItemSound.quest_middle");
						}
						break;
					
					case BARBADO:
						if (cond == 3)
						{
							st.set("cond", "4");
							st.playSound("ItemSound.quest_middle");
							
							if (st.hasQuestItems(NANARINS_FLUTE))
							{
								htmltext = "30959-02.htm";
								st.set("success", "1");
							}
							else
								htmltext = "30959-01.htm";
							
							st.takeItems(BLACK_BEER, -1);
							st.takeItems(CLOTHES, -1);
							st.takeItems(NANARINS_FLUTE, -1);
						}
						else if (cond == 4)
							htmltext = "30959-03.htm";
						break;
				}
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new _363_SorrowfulSoundOfFlute(363, qn, "");
	}
}