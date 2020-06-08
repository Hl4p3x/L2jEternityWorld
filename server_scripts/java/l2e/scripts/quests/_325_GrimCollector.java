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
 * Created by LordWinter 30.09.2012
 * Based on L2J Eternity-World
 */
public class _325_GrimCollector extends Quest
{
	private static final String qn = "_325_GrimCollector";
	
	// Items
	private static final int ANATOMY_DIAGRAM = 1349;
	private static final int ZOMBIE_HEAD = 1350;
	private static final int ZOMBIE_HEART = 1351;
	private static final int ZOMBIE_LIVER = 1352;
	private static final int SKULL = 1353;
	private static final int RIB_BONE = 1354;
	private static final int SPINE = 1355;
	private static final int ARM_BONE = 1356;
	private static final int THIGH_BONE = 1357;
	private static final int COMPLETE_SKELETON = 1358;
	
	// NPCs
	private static final int CURTIS = 30336;
	private static final int VARSAK = 30342;
	private static final int SAMED = 30434;
		
	private int getNumberOfPieces(QuestState st)
	{
		return (int) (st.getQuestItemsCount(ZOMBIE_HEAD) + st.getQuestItemsCount(SPINE) + st.getQuestItemsCount(ARM_BONE) + st.getQuestItemsCount(ZOMBIE_HEART) + st.getQuestItemsCount(ZOMBIE_LIVER) + st.getQuestItemsCount(SKULL) + st.getQuestItemsCount(RIB_BONE) + st.getQuestItemsCount(THIGH_BONE) + st.getQuestItemsCount(COMPLETE_SKELETON));
	}
	
	private void payback(QuestState st)
	{
		int count = getNumberOfPieces(st);
		if (count > 0)
		{
			int reward = (int) (30 * st.getQuestItemsCount(ZOMBIE_HEAD) + 20 * st.getQuestItemsCount(ZOMBIE_HEART) + 20 * st.getQuestItemsCount(ZOMBIE_LIVER) + 100 * st.getQuestItemsCount(SKULL) + 40 * st.getQuestItemsCount(RIB_BONE) + 14 * st.getQuestItemsCount(SPINE) + 14 * st.getQuestItemsCount(ARM_BONE) + 14 * st.getQuestItemsCount(THIGH_BONE) + 341 * st.getQuestItemsCount(COMPLETE_SKELETON));
			if (count > 10)
				reward += 1629;
			
			if (st.getQuestItemsCount(COMPLETE_SKELETON) > 0)
				reward += 543;
			
			st.takeItems(ZOMBIE_HEAD, -1);
			st.takeItems(ZOMBIE_HEART, -1);
			st.takeItems(ZOMBIE_LIVER, -1);
			st.takeItems(SKULL, -1);
			st.takeItems(RIB_BONE, -1);
			st.takeItems(SPINE, -1);
			st.takeItems(ARM_BONE, -1);
			st.takeItems(THIGH_BONE, -1);
			st.takeItems(COMPLETE_SKELETON, -1);
			st.rewardItems(57, reward);
		}
	}

	public _325_GrimCollector(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(CURTIS);
		addTalkId(CURTIS, VARSAK, SAMED);
		
		addKillId(20026, 20029, 20035, 20042, 20045, 20457, 20458, 20051, 20514, 20515);

		questItemIds = new int[]
		{
			ZOMBIE_HEAD,
			ZOMBIE_HEART,
			ZOMBIE_LIVER,
			SKULL,
			RIB_BONE,
			SPINE,
			ARM_BONE,
			THIGH_BONE,
			COMPLETE_SKELETON,
			ANATOMY_DIAGRAM
		};
	}	
		
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30336-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30434-03.htm"))
		{
			st.giveItems(ANATOMY_DIAGRAM, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if (event.equalsIgnoreCase("30434-06.htm"))
		{
			st.takeItems(ANATOMY_DIAGRAM, -1);
			payback(st);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30434-07.htm"))
		{
			payback(st);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30434-09.htm"))
		{
			int skeletons = (int) st.getQuestItemsCount(COMPLETE_SKELETON);
			if (skeletons > 0)
			{
				st.takeItems(COMPLETE_SKELETON, -1);
				st.playSound("ItemSound.quest_middle");
				st.rewardItems(57, 543 + 341 * skeletons);
			}
		}
		else if (event.equalsIgnoreCase("30342-03.htm"))
		{
			if (st.getQuestItemsCount(SPINE) > 0 && st.getQuestItemsCount(ARM_BONE) > 0 && st.getQuestItemsCount(SKULL) > 0 && st.getQuestItemsCount(RIB_BONE) > 0 && st.getQuestItemsCount(THIGH_BONE) > 0)
			{
				st.takeItems(SPINE, 1);
				st.takeItems(SKULL, 1);
				st.takeItems(ARM_BONE, 1);
				st.takeItems(RIB_BONE, 1);
				st.takeItems(THIGH_BONE, 1);
				
				if (st.getRandom(10) < 9)
				{
					st.giveItems(COMPLETE_SKELETON, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else
					htmltext = "30342-04.htm";
			}
			else
				htmltext = "30342-02.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg(player);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 15 && player.getLevel() <= 26)
					htmltext = "30336-02.htm";
				else
				{
					htmltext = "30336-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case CURTIS:
						htmltext = (st.getQuestItemsCount(ANATOMY_DIAGRAM) < 1) ? "30336-04.htm" : "30336-05.htm";
						break;
					case SAMED:
						if (st.getQuestItemsCount(ANATOMY_DIAGRAM) == 0)
							htmltext = "30434-01.htm";
						else
						{
							if (getNumberOfPieces(st) == 0)
								htmltext = "30434-04.htm";
							else
								htmltext = (st.getQuestItemsCount(COMPLETE_SKELETON) == 0) ? "30434-05.htm" : "30434-08.htm";
						}
						break;
					case VARSAK:
						htmltext = "30342-01.htm";
						break;
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;
		
		if (st.isStarted() && st.getQuestItemsCount(ANATOMY_DIAGRAM) > 0)
		{
			int n = st.getRandom(100);
			switch (npc.getId())
			{
				case 20026:
					if (n <= 90)
					{
						st.playSound("ItemSound.quest_itemget");
						if (n <= 40)
							st.giveItems(ZOMBIE_HEAD, 1);
						else if (n <= 60)
							st.giveItems(ZOMBIE_HEART, 1);
						else
							st.giveItems(ZOMBIE_LIVER, 1);
					}
					break;
				case 20029:
					st.playSound("ItemSound.quest_itemget");
					if (n <= 44)
						st.giveItems(ZOMBIE_HEAD, 1);
					else if (n <= 66)
						st.giveItems(ZOMBIE_HEART, 1);
					else
						st.giveItems(ZOMBIE_LIVER, 1);
					break;
				case 20035:
					if (n <= 79)
					{
						st.playSound("ItemSound.quest_itemget");
						if (n <= 5)
							st.giveItems(SKULL, 1);
						else if (n <= 15)
							st.giveItems(RIB_BONE, 1);
						else if (n <= 29)
							st.giveItems(SPINE, 1);
						else
							st.giveItems(THIGH_BONE, 1);
					}
					break;
				case 20042:
					if (n <= 86)
					{
						st.playSound("ItemSound.quest_itemget");
						if (n <= 6)
							st.giveItems(SKULL, 1);
						else if (n <= 19)
							st.giveItems(RIB_BONE, 1);
						else if (n <= 69)
							st.giveItems(ARM_BONE, 1);
						else
							st.giveItems(THIGH_BONE, 1);
					}
					break;
				case 20045:
					if (n <= 97)
					{
						st.playSound("ItemSound.quest_itemget");
						if (n <= 9)
							st.giveItems(SKULL, 1);
						else if (n <= 59)
							st.giveItems(SPINE, 1);
						else if (n <= 77)
							st.giveItems(ARM_BONE, 1);
						else
							st.giveItems(THIGH_BONE, 1);
					}
					break;
				case 20051:
					if (n <= 99)
					{
						st.playSound("ItemSound.quest_itemget");
						if (n <= 9)
							st.giveItems(SKULL, 1);
						else if (n <= 59)
							st.giveItems(RIB_BONE, 1);
						else if (n <= 79)
							st.giveItems(SPINE, 1);
						else
							st.giveItems(ARM_BONE, 1);
					}
					break;
				case 20514:
					if (n <= 51)
					{
						st.playSound("ItemSound.quest_itemget");
						if (n <= 2)
							st.giveItems(SKULL, 1);
						else if (n <= 8)
							st.giveItems(RIB_BONE, 1);
						else if (n <= 17)
							st.giveItems(SPINE, 1);
						else if (n <= 18)
							st.giveItems(ARM_BONE, 1);
						else
							st.giveItems(THIGH_BONE, 1);
					}
					break;
				case 20515:
					if (n <= 60)
					{
						st.playSound("ItemSound.quest_itemget");
						if (n <= 3)
							st.giveItems(SKULL, 1);
						else if (n <= 11)
							st.giveItems(RIB_BONE, 1);
						else if (n <= 22)
							st.giveItems(SPINE, 1);
						else if (n <= 24)
							st.giveItems(ARM_BONE, 1);
						else
							st.giveItems(THIGH_BONE, 1);
					}
					break;
				case 20457:
				case 20458:
					st.playSound("ItemSound.quest_itemget");
					if (n <= 42)
						st.giveItems(ZOMBIE_HEAD, 1);
					else if (n <= 67)
						st.giveItems(ZOMBIE_HEART, 1);
					else
						st.giveItems(ZOMBIE_LIVER, 1);
					break;
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _325_GrimCollector(325, qn, "");	
	}
}