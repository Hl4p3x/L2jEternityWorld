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
 * Created by LordWinter 25.06.2011 Based on L2J Eternity-World
 */
public final class _403_PathToRogue extends Quest
{
	private static final String qn = "_403_PathToRogue";
	
	// Quest NPCs
	private static final int BEZIQUE = 30379;
	private static final int NETI = 30425;
	
	// Quest items
	private static final int BEZIQUES_LETTER = 1180;
	private static final int NETIS_BOW = 1181;
	private static final int NETIS_DAGGER = 1182;
	private static final int SPARTOIS_BONES = 1183;
	private static final int SPARTOI_BONE_COUNT = 10;
	private static final int HORSESHOE_OF_LIGHT = 1184;
	private static final int WANTED_BILL = 1185;
	
	private static final int STOLEN_JEWELRY = 1186;
	private static final int STOLEN_TOMES = 1187;
	private static final int STOLEN_RING = 1188;
	private static final int STOLEN_NECKLACE = 1189;
	private static final int[] STOLEN_ITEMS =
	{
		STOLEN_JEWELRY,
		STOLEN_TOMES,
		STOLEN_RING,
		STOLEN_NECKLACE
	};
	
	private static final int BEZIQUES_RECOMMENDATION = 1190;
	
	// Quest monsters
	private static final int[] SPARTOI =
	{
		20035,
		20042,
		20045,
		20051,
		20054,
		20060
	};
	
	private static final int CATS_EYE_BANDIT = 27038;
	
	private final boolean allStolenItems(QuestState st)
	{
		return ((st.getQuestItemsCount(STOLEN_JEWELRY) + st.getQuestItemsCount(STOLEN_TOMES) + st.getQuestItemsCount(STOLEN_RING) + st.getQuestItemsCount(STOLEN_NECKLACE)) == 4);
	}
	
	private _403_PathToRogue(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(BEZIQUE);
		addTalkId(BEZIQUE);
		addTalkId(NETI);
		
		for (int mobId : SPARTOI)
		{
			addKillId(mobId);
		}
		
		addKillId(CATS_EYE_BANDIT);
		
		questItemIds = new int[]
		{
			BEZIQUES_LETTER,
			NETIS_BOW,
			NETIS_DAGGER,
			SPARTOIS_BONES,
			HORSESHOE_OF_LIGHT,
			WANTED_BILL,
			STOLEN_JEWELRY,
			STOLEN_TOMES,
			STOLEN_RING,
			STOLEN_NECKLACE,
			BEZIQUES_RECOMMENDATION
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
		
		if (event.equalsIgnoreCase("30379_2"))
		{
			if ((player.getClassId().getId() == 0x00) && !st.isCompleted())
			{
				if (player.getLevel() > 17)
				{
					if (player.getInventory().getInventoryItemCount(BEZIQUES_RECOMMENDATION, -1) != 0)
					{
						htmltext = "30379-04.htm";
					}
					else
					{
						htmltext = "30379-05.htm";
					}
				}
				else
				{
					htmltext = "30379-03.htm";
				}
			}
			else if (player.getClassId().getId() == 0x07)
			{
				htmltext = "30379-02a.htm";
			}
			else
			{
				htmltext = "30379-02.htm";
			}
		}
		else if (!st.isCompleted())
		{
			if (event.equalsIgnoreCase("1"))
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				st.giveItems(BEZIQUES_LETTER, 1);
				htmltext = "30379-06.htm";
			}
			else if (event.equalsIgnoreCase("30425_1"))
			{
				st.takeItems(BEZIQUES_LETTER, -1);
				if (st.getQuestItemsCount(NETIS_BOW) == 0)
				{
					st.giveItems(NETIS_BOW, 1);
				}
				if (st.getQuestItemsCount(NETIS_DAGGER) == 0)
				{
					st.giveItems(NETIS_DAGGER, 1);
				}
				st.set("cond", "2");
				htmltext = "30425-05.htm";
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
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		int state = st.getState();
		
		if (npcId == BEZIQUE)
		{
			if (cond == 0)
			{
				htmltext = "30379-01.htm";
			}
			else if (st.getQuestItemsCount(HORSESHOE_OF_LIGHT) == 0)
			{
				if (allStolenItems(st))
				{
					String done = st.getGlobalQuestVar("1ClassQuestFinished");
					st.set("cond", "0");
					st.exitQuest(false);
					if (done.isEmpty())
					{
						if (player.getLevel() >= 20)
						{
							st.addExpAndSp(320534, 20232);
						}
						else if (player.getLevel() == 19)
						{
							st.addExpAndSp(456128, 26930);
						}
						else
						{
							st.addExpAndSp(591724, 33628);
						}
						st.giveItems(57, 163800);
						st.giveItems(BEZIQUES_RECOMMENDATION, 1);
					}
					st.saveGlobalQuestVar("1ClassQuestFinished", "1");
					st.playSound("ItemSound.quest_finish");
					htmltext = "30379-09.htm";
				}
				else if (st.getQuestItemsCount(BEZIQUES_LETTER) != 0)
				{
					htmltext = "30379-07.htm";
				}
				else if ((st.getQuestItemsCount(NETIS_BOW) != 0) && (st.getQuestItemsCount(NETIS_DAGGER) != 0) && (st.getQuestItemsCount(WANTED_BILL) == 0))
				{
					htmltext = "30379-10.htm";
				}
				else
				{
					htmltext = "30379-11.htm";
				}
			}
			else
			{
				st.takeItems(HORSESHOE_OF_LIGHT, -1);
				st.giveItems(WANTED_BILL, 1);
				st.set("cond", "5");
				htmltext = "30379-08.htm";
			}
		}
		else
		{
			if ((state != State.STARTED) || (cond == 0))
			{
				return htmltext;
			}
			else if (st.getQuestItemsCount(BEZIQUES_LETTER) != 0)
			{
				htmltext = "30425-01.htm";
			}
			else if (st.getQuestItemsCount(HORSESHOE_OF_LIGHT) != 0)
			{
				htmltext = "30425-08.htm";
			}
			else if (st.getQuestItemsCount(WANTED_BILL) != 0)
			{
				htmltext = "30425-08.htm";
			}
			else if (st.getQuestItemsCount(SPARTOIS_BONES) >= SPARTOI_BONE_COUNT)
			{
				st.takeItems(SPARTOIS_BONES, -1);
				st.giveItems(HORSESHOE_OF_LIGHT, 1);
				st.set("cond", "4");
				htmltext = "30425-07.htm";
			}
			else
			{
				htmltext = "30425-06.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if ((st == null) || (st.getInt("cond") == 0))
		{
			return null;
		}
		
		int npcId = npc.getId();
		@SuppressWarnings("unused")
		int chance;
		
		if (npcId == CATS_EYE_BANDIT)
		{
			if (st.getQuestItemsCount(WANTED_BILL) == 0)
			{
				return null;
			}
			
			int ran = getRandom(4);
			if (st.getQuestItemsCount(STOLEN_ITEMS[ran]) == 0)
			{
				st.giveItems(STOLEN_ITEMS[ran], 1);
				if (allStolenItems(st))
				{
					st.set("cond", "6");
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else
		{
			if ((npcId == SPARTOI[0]) || (npcId == SPARTOI[2]) || (npcId == SPARTOI[3]))
			{
				chance = 200000;
			}
			else if (npcId == SPARTOI[1])
			{
				chance = 300000;
			}
			else
			{
				chance = 800000;
			}
			if (st.getQuestItemsCount(SPARTOIS_BONES) == 10)
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "3");
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _403_PathToRogue(403, qn, "");
	}
}