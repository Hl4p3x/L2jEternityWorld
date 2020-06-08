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

public class _603_DaimontheWhiteEyedPart1 extends Quest
{
	private static final String qn = "_603_DaimontheWhiteEyedPart1";
	
	private static final int EYE_OF_ARGOS = 31683;
	private static final int MYSTERIOUS_TABLET_1 = 31548;
	private static final int MYSTERIOUS_TABLET_2 = 31549;
	private static final int MYSTERIOUS_TABLET_3 = 31550;
	private static final int MYSTERIOUS_TABLET_4 = 31551;
	private static final int MYSTERIOUS_TABLET_5 = 31552;
	
	private static final int CANYON_BANDERSNATCH_SLAVE = 21297;
	private static final int BUFFALO_SLAVE = 21299;
	private static final int GRENDEL_SLAVE = 21304;
	
	private static final int EVIL_SPIRIT_BEADS = 7190;
	private static final int BROKEN_CRYSTAL = 7191;
	private static final int UNFINISHED_SUMMON_CRYSTAL = 7192;
	
	public _603_DaimontheWhiteEyedPart1(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(EYE_OF_ARGOS);
		addTalkId(EYE_OF_ARGOS, MYSTERIOUS_TABLET_1, MYSTERIOUS_TABLET_2, MYSTERIOUS_TABLET_3, MYSTERIOUS_TABLET_4, MYSTERIOUS_TABLET_5);
		
		addKillId(BUFFALO_SLAVE, GRENDEL_SLAVE, CANYON_BANDERSNATCH_SLAVE);
		
		questItemIds = new int[]
		{
			EVIL_SPIRIT_BEADS,
			BROKEN_CRYSTAL
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
		
		if (event.equalsIgnoreCase("31683-03.htm"))
		{
			st.set("cond", "1");
			st.setState((byte) 1);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31683-06.htm"))
		{
			if (st.getQuestItemsCount(BROKEN_CRYSTAL) > 4)
			{
				st.set("cond", "7");
				st.takeItems(BROKEN_CRYSTAL, -1);
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = "31683-07.htm";
			}
		}
		else if (event.equalsIgnoreCase("31683-10.htm"))
		{
			if (st.getQuestItemsCount(EVIL_SPIRIT_BEADS) > 199)
			{
				st.takeItems(EVIL_SPIRIT_BEADS, -1);
				st.giveItems(UNFINISHED_SUMMON_CRYSTAL, 1);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
			else
			{
				st.set("cond", "7");
				htmltext = "31683-11.htm";
			}
		}
		else if (event.equalsIgnoreCase("31548-02.htm"))
		{
			st.set("cond", "2");
			st.giveItems(BROKEN_CRYSTAL, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31549-02.htm"))
		{
			st.set("cond", "3");
			st.giveItems(BROKEN_CRYSTAL, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31550-02.htm"))
		{
			st.set("cond", "4");
			st.giveItems(BROKEN_CRYSTAL, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31551-02.htm"))
		{
			st.set("cond", "5");
			st.giveItems(BROKEN_CRYSTAL, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31552-02.htm"))
		{
			st.set("cond", "6");
			st.giveItems(BROKEN_CRYSTAL, 1);
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case 0:
				if (player.getLevel() < 73)
				{
					htmltext = "31683-02.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "31683-01.htm";
				}
				break;
			case 1:
				int cond = st.getInt("cond");
				switch (npc.getId())
				{
					case EYE_OF_ARGOS:
						if ((cond >= 1) && (cond <= 5))
						{
							htmltext = "31683-04.htm";
						}
						else if (cond == 6)
						{
							htmltext = "31683-05.htm";
						}
						else if (cond == 7)
						{
							htmltext = "31683-08.htm";
						}
						else if (cond == 8)
						{
							htmltext = "31683-09.htm";
						}
						break;
					case MYSTERIOUS_TABLET_1:
						if (cond == 1)
						{
							htmltext = "31548-01.htm";
						}
						else if (cond >= 2)
						{
							htmltext = "31548-03.htm";
						}
						break;
					case MYSTERIOUS_TABLET_2:
						if (cond == 2)
						{
							htmltext = "31549-01.htm";
						}
						else if (cond >= 3)
						{
							htmltext = "31549-03.htm";
						}
						break;
					case MYSTERIOUS_TABLET_3:
						if (cond == 3)
						{
							htmltext = "31550-01.htm";
						}
						else if (cond >= 4)
						{
							htmltext = "31550-03.htm";
						}
						break;
					case MYSTERIOUS_TABLET_4:
						if (cond == 4)
						{
							htmltext = "31551-01.htm";
						}
						else if (cond >= 5)
						{
							htmltext = "31551-03.htm";
						}
						break;
					case MYSTERIOUS_TABLET_5:
						if (cond == 5)
						{
							htmltext = "31552-01.htm";
						}
						else if (cond >= 6)
						{
							htmltext = "31552-03.htm";
						}
						break;
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, 7);
		if (partyMember == null)
		{
			return null;
		}
		
		QuestState st = partyMember.getQuestState(qn);
		
		if (st.dropQuestItems(EVIL_SPIRIT_BEADS, 1, 200, 800000, true))
		{
			st.set("cond", "8");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _603_DaimontheWhiteEyedPart1(603, qn, "");
	}
}