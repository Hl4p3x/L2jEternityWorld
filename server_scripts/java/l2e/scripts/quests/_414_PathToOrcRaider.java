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
import l2e.gameserver.network.serverpackets.SocialAction;

/**
 * Created by LordWinter 26.09.2012
 * Based on L2J Eternity-World
 */
public class _414_PathToOrcRaider extends Quest
{
	private static final String qn = "_414_PathToOrcRaider";
	
	// Npcs
	private static final int KARUKIA = 30570;
	private static final int KASMAN = 30501;
	private static final int TAZEER = 31978;
	
	private static final int[] TALKERS =
	{
		KARUKIA,
		KASMAN,
		TAZEER
	};
	
	// Mobs
	private static final int GOBLIN_TOMB_RAIDER_LEADER = 20320;
	private static final int KURUKA_RATMAN_LEADER = 27045;
	private static final int UMBAR_ORC = 27054;
	private static final int TIMORA_ORC = 27320;
	
	private static final int[] KILLS =
	{
		GOBLIN_TOMB_RAIDER_LEADER,
		KURUKA_RATMAN_LEADER,
		UMBAR_ORC,
		TIMORA_ORC
	};
	
	// Quest Items
	private static final int GREEN_BLOOD = 1578;
	private static final int GOBLIN_DWELLING_MAP = 1579;
	private static final int KURUKA_RATMAN_TOOTH = 1580;
	private static final int BETRAYER_UMBAR_REPORT = 1589;
	private static final int HEAD_OF_BETRAYER = 1591;
	private static final int TIMORA_ORC_HEAD = 8544;
	
	private static final int[] QUESTITEMS =
	{
		GREEN_BLOOD,
		GOBLIN_DWELLING_MAP,
		KURUKA_RATMAN_TOOTH,
		BETRAYER_UMBAR_REPORT,
		HEAD_OF_BETRAYER,
		TIMORA_ORC_HEAD
	};
	
	// Reward
	private static final int MARK_OF_RAIDER = 1592;
	
	public _414_PathToOrcRaider(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(KARUKIA);
		
		for (int talkId : TALKERS)
		{
			addTalkId(talkId);
		}
		
		for (int killId : KILLS)
		{
			addKillId(killId);
		}
		
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
		
		if (event.equalsIgnoreCase("30570-05.htm"))
		{
			st.set("id", "1");
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.giveItems(GOBLIN_DWELLING_MAP, 1);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30570-07a.htm"))
		{
			st.takeItems(KURUKA_RATMAN_TOOTH, -1);
			st.takeItems(GOBLIN_DWELLING_MAP, -1);
			st.takeItems(GREEN_BLOOD, -1);
			st.giveItems(BETRAYER_UMBAR_REPORT, 1);
			st.set("id", "3");
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30570-07b.htm"))
		{
			st.takeItems(KURUKA_RATMAN_TOOTH, -1);
			st.takeItems(GOBLIN_DWELLING_MAP, -1);
			st.takeItems(GREEN_BLOOD, -1);
			st.set("cond", "5");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31978-03.htm"))
		{
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = Quest.getNoQuestMsg(talker);
		QuestState st = talker.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		int id = st.getState();
		if ((npcId != KARUKIA) && (id != State.STARTED))
		{
			return htmltext;
		}
		
		int playerClassID = talker.getClassId().getId();
		int playerLvl = talker.getLevel();
		int cond = st.getInt("cond");
		
		if ((npcId == KARUKIA) && (cond == 0))
		{
			if ((playerLvl >= 18) && (playerClassID == 0x2c) && (st.getQuestItemsCount(MARK_OF_RAIDER) == 0) && (st.getQuestItemsCount(GOBLIN_DWELLING_MAP) == 0))
			{
				htmltext = "30570-01.htm";
			}
			else if (playerClassID != 0x2c)
			{
				htmltext = playerClassID == 0x2d ? "30570-02a.htm" : "30570-03.htm";
			}
			else if ((playerLvl < 18) && (playerClassID == 0x2c))
			{
				htmltext = "30570-02.htm";
			}
			else if ((playerLvl >= 18) && (playerClassID == 0x2c) && (st.getQuestItemsCount(MARK_OF_RAIDER) == 1))
			{
				htmltext = "30570-04.htm";
			}
			else
			{
				htmltext = "30570-02.htm";
			}
		}
		else if ((npcId == KARUKIA) && (cond > 0) && (st.getQuestItemsCount(GOBLIN_DWELLING_MAP) == 1) && (st.getQuestItemsCount(KURUKA_RATMAN_TOOTH) < 10))
		{
			htmltext = "30570-06.htm";
		}
		else if ((npcId == KARUKIA) && (cond > 0) && (st.getQuestItemsCount(GOBLIN_DWELLING_MAP) == 1) && (st.getQuestItemsCount(KURUKA_RATMAN_TOOTH) >= 10) && (st.getQuestItemsCount(BETRAYER_UMBAR_REPORT) == 0))
		{
			htmltext = "30570-07.htm";
		}
		else if ((npcId == KARUKIA) && (cond > 5))
		{
			htmltext = "30570-07b.htm";
		}
		else if ((npcId == KARUKIA) && (cond > 0) && (st.getQuestItemsCount(BETRAYER_UMBAR_REPORT) > 0) && (st.getQuestItemsCount(HEAD_OF_BETRAYER) < 2))
		{
			htmltext = "30570-08.htm";
		}
		else if ((npcId == KARUKIA) && (cond > 0) && (st.getQuestItemsCount(BETRAYER_UMBAR_REPORT) > 0) && (st.getQuestItemsCount(HEAD_OF_BETRAYER) == 2))
		{
			htmltext = "30570-09.htm";
		}
		else if ((npcId == KASMAN) && (cond > 0) && (st.getQuestItemsCount(BETRAYER_UMBAR_REPORT) > 0) && (st.getQuestItemsCount(HEAD_OF_BETRAYER) == 0))
		{
			htmltext = "30501-01.htm";
		}
		else if ((npcId == KASMAN) && (cond > 0) && (st.getQuestItemsCount(HEAD_OF_BETRAYER) > 0) && (st.getQuestItemsCount(HEAD_OF_BETRAYER) < 2))
		{
			htmltext = "30501-02.htm";
		}
		else if ((npcId == KASMAN) && (cond > 0) && (st.getQuestItemsCount(HEAD_OF_BETRAYER) == 2))
		{
			htmltext = "30501-03.htm";
			st.takeItems(HEAD_OF_BETRAYER, -1);
			st.takeItems(BETRAYER_UMBAR_REPORT, -1);
			String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
			if (isFinished.equalsIgnoreCase(""))
			{
				st.addExpAndSp(295862, 2600);
			}
			st.giveItems(MARK_OF_RAIDER, 1);
			st.saveGlobalQuestVar("1ClassQuestFinished", "1");
			st.unset("cond");
			talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		else if (npcId == TAZEER)
		{
			if (cond == 5)
			{
				htmltext = "31978-01.htm";
			}
			else if (cond == 6)
			{
				htmltext = "31978-04.htm";
			}
			else if (cond == 7)
			{
				htmltext = "31978-05.htm";
				st.unset("cond");
				st.takeItems(TIMORA_ORC_HEAD, -1);
				st.addExpAndSp(160267, 1300);
				st.giveItems(MARK_OF_RAIDER, 1);
				talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		QuestState st = killer.getQuestState(qn);
		
		if (st == null)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		if (st.getState() != State.STARTED)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		int cond = st.getInt("cond");
		int npcId = npc.getId();
		int x = killer.getX();
		int y = killer.getY();
		int z = killer.getZ();
		if (npcId == GOBLIN_TOMB_RAIDER_LEADER)
		{
			if ((cond > 0) && (st.getQuestItemsCount(GOBLIN_DWELLING_MAP) == 1) && (st.getQuestItemsCount(KURUKA_RATMAN_TOOTH) < 10) && (st.getQuestItemsCount(GREEN_BLOOD) < 40))
			{
				if (st.getQuestItemsCount(GREEN_BLOOD) > 1)
				{
					if (st.getRandom(100) < ((st.getQuestItemsCount(GREEN_BLOOD)) * 10))
					{
						st.takeItems(GREEN_BLOOD, -1);
						st.addSpawn(KURUKA_RATMAN_LEADER, x, y, z);
					}
					else
					{
						st.giveItems(GREEN_BLOOD, 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
				else
				{
					st.giveItems(GREEN_BLOOD, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == KURUKA_RATMAN_LEADER)
		{
			if ((cond > 0) && (st.getQuestItemsCount(GOBLIN_DWELLING_MAP) == 1) && (st.getQuestItemsCount(KURUKA_RATMAN_TOOTH) < 10))
			{
				st.takeItems(GREEN_BLOOD, -1);
				if (st.getQuestItemsCount(KURUKA_RATMAN_TOOTH) == 9)
				{
					st.giveItems(KURUKA_RATMAN_TOOTH, 1);
					st.set("id", "2");
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.giveItems(KURUKA_RATMAN_TOOTH, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == UMBAR_ORC)
		{
			if ((cond > 0) && (st.getQuestItemsCount(BETRAYER_UMBAR_REPORT) > 0) && (st.getQuestItemsCount(HEAD_OF_BETRAYER) < 2))
			{
				st.giveItems(HEAD_OF_BETRAYER, 1);
				if (st.getQuestItemsCount(HEAD_OF_BETRAYER) > 1)
				{
					st.set("id", "4");
					st.set("cond", "4");
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == TIMORA_ORC)
		{
			if (cond == 6)
			{
				st.set("cond", "7");
				st.playSound("ItemSound.quest_middle");
				st.giveItems(TIMORA_ORC_HEAD, 1);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _414_PathToOrcRaider(414, qn, "");
	}
}