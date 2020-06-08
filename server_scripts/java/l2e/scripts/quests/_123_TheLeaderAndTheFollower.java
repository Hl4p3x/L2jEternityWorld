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

import l2e.gameserver.model.L2ClanMember;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 29.12.2012 Based on L2J Eternity-World
 */
public class _123_TheLeaderAndTheFollower extends Quest
{
	private static final String qn = "_123_TheLeaderAndTheFollower";
	
	// NPC
	private static final int NEWYEAR = 31961;
	
	// ITEMS
	private static final int BLOOD = 8549;
	private static final int LEG = 8550;
	
	// MOBS
	private static final int BRUIN_LIZARDMAN = 27321;
	private static final int PICOT_ARANEID = 27322;
	
	public _123_TheLeaderAndTheFollower(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(NEWYEAR);
		addTalkId(NEWYEAR);
		
		addKillId(BRUIN_LIZARDMAN, PICOT_ARANEID);
		
		questItemIds = new int[]
		{
			BLOOD,
			LEG
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
		
		if (event.equalsIgnoreCase("31961-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31961-05a.htm"))
		{
			if (st.getQuestItemsCount(BLOOD) >= 10)
			{
				st.takeItems(BLOOD, -1);
				st.set("cond", "3");
				st.set("settype", "1");
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = "no_items.htm";
			}
		}
		else if (event.equalsIgnoreCase("31961-05b.htm"))
		{
			if (st.getQuestItemsCount(BLOOD) >= 10)
			{
				st.takeItems(BLOOD, -1);
				st.set("cond", "4");
				st.set("settype", "2");
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = "no_items.htm";
			}
		}
		else if (event.equalsIgnoreCase("31961-05c.htm"))
		{
			if (st.getQuestItemsCount(BLOOD) >= 10)
			{
				st.takeItems(BLOOD, -1);
				st.set("cond", "5");
				st.set("settype", "3");
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = "no_items.htm";
			}
		}
		else if (event.equalsIgnoreCase("31961-09.htm"))
		{
			L2ClanMember cm_apprentice = player.getClan().getClanMember(player.getApprentice());
			if (cm_apprentice.isOnline())
			{
				L2PcInstance apprentice = cm_apprentice.getPlayerInstance();
				if (apprentice != null)
				{
					QuestState apQuest = apprentice.getQuestState(qn);
					if (apQuest != null)
					{
						int crystals = apQuest.getInt("cond") == 3 ? 922 : 771;
						
						if (st.getQuestItemsCount(1458) >= crystals)
						{
							htmltext = "31961-10.htm";
							st.takeItems(1458, crystals);
							st.playSound("ItemSound.quest_finish");
							apQuest.set("cond", "6");
							apQuest.playSound("ItemSound.quest_middle");
						}
					}
				}
			}
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getSponsor() > 0)
				{
					if ((player.getLevel() > 19) && (player.getPledgeType() == -1))
					{
						return "31961-01.htm";
					}
					
				}
				else if (player.getApprentice() > 0)
				{
					L2ClanMember cm = player.getClan().getClanMember(player.getApprentice());
					if ((cm != null) && (cm.isOnline()))
					{
						L2PcInstance apprentice = cm.getPlayerInstance();
						if (apprentice != null)
						{
							QuestState apQuest = apprentice.getQuestState(qn);
							if (apQuest != null)
							{
								int apCond = apQuest.getInt("cond");
								if (apCond == 3)
								{
									return "31961-09a.htm";
								}
								if (apCond == 4)
								{
									return "31961-09b.htm";
								}
								if (apCond == 5)
								{
									return "31961-09c.htm";
								}
							}
						}
					}
				}
				htmltext = "31961-00.htm";
				st.exitQuest(true);
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				
				if (player.getSponsor() > 0)
				{
					if (cond == 1)
					{
						htmltext = "31961-03.htm";
					}
					else if (cond == 2)
					{
						htmltext = "31961-04.htm";
					}
					else if (cond == 3)
					{
						htmltext = "31961-05d.htm";
					}
					else if (cond == 4)
					{
						htmltext = "31961-05e.htm";
					}
					else if (cond == 5)
					{
						htmltext = "31961-05f.htm";
					}
					else if (cond == 6)
					{
						htmltext = "31961-06.htm";
						st.set("cond", "7");
						st.playSound("ItemSound.quest_middle");
					}
					else if (cond == 7)
					{
						htmltext = "31961-07.htm";
					}
					else if (cond == 8)
					{
						if (st.getQuestItemsCount(LEG) == 8)
						{
							htmltext = "31961-08.htm";
							
							st.takeItems(LEG, -1);
							st.giveItems(7850, 1);
							
							switch (st.getInt("settype"))
							{
								case 1:
									st.giveItems(7851, 1);
									st.giveItems(7852, 1);
									st.giveItems(7853, 1);
									break;
								case 2:
									st.giveItems(7854, 1);
									st.giveItems(7855, 1);
									st.giveItems(7856, 1);
									break;
								case 3:
									st.giveItems(7857, 1);
									st.giveItems(7858, 1);
									st.giveItems(7859, 1);
							}
							st.playSound("ItemSound.quest_finish");
							st.exitQuest(false);
						}
					}
				}
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		int sponsor = player.getSponsor();
		if (sponsor == 0)
		{
			st.exitQuest(true);
			return null;
		}
		
		switch (npc.getId())
		{
			case BRUIN_LIZARDMAN:
				if ((st.getInt("cond") == 1) && (st.dropQuestItems(BLOOD, 1, 10, 600000, true)))
				{
					st.set("cond", "2");
				}
				break;
			case PICOT_ARANEID:
				L2ClanMember cmSponsor = player.getClan().getClanMember(sponsor);
				if ((cmSponsor != null) && (cmSponsor.isOnline()))
				{
					L2PcInstance sponsorHelper = cmSponsor.getPlayerInstance();
					if ((sponsorHelper != null) && (player.isInsideRadius(sponsorHelper, 1100, true, false)))
					{
						if ((st.getInt("cond") == 7) && (st.dropQuestItems(LEG, 1, 8, 700000, true)))
						{
							st.set("cond", "8");
						}
					}
				}
				break;
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _123_TheLeaderAndTheFollower(123, qn, "");
	}
}