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
 * Created by LordWinter 24.06.2011 Based on L2J Eternity-World
 */
public final class _409_PathToOracle extends Quest
{
	private static final String qn = "_409_PathToOracle";
	
	// Quest NPCs
	private static final int MANUEL = 30293;
	private static final int ALLANA = 30424;
	private static final int PERRIN = 30428;
	
	// Quest items
	private static final int CRYSTAL_MEDALLION = 1231;
	private static final int SWINDLERS_MONEY = 1232;
	private static final int ALLANAS_DIARY = 1233;
	private static final int LIZARD_CAPTAIN_ORDER = 1234;
	private static final int LEAF_OF_ORACLE = 1235;
	private static final int HALF_OF_DIARY = 1236;
	private static final int TAMILS_NECKLACE = 1275;
	
	// Quest monsters
	private static final int LIZARDMAN_WARRIOR = 27032;
	private static final int LIZARDMAN_SCOUT = 27033;
	private static final int LIZARDMAN = 27034;
	private static final int TAMIL = 27035;
	
	private _409_PathToOracle(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(MANUEL);
		addTalkId(MANUEL);
		addTalkId(ALLANA);
		addTalkId(PERRIN);
		
		addKillId(LIZARDMAN_WARRIOR);
		addKillId(LIZARDMAN_SCOUT);
		addKillId(LIZARDMAN);
		addKillId(TAMIL);
		
		questItemIds = new int[]
		{
			CRYSTAL_MEDALLION,
			SWINDLERS_MONEY,
			ALLANAS_DIARY,
			LIZARD_CAPTAIN_ORDER,
			LEAF_OF_ORACLE,
			HALF_OF_DIARY,
			TAMILS_NECKLACE
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
			if ((player.getClassId().getId() == 0x19) && !st.isCompleted())
			{
				if (player.getLevel() > 17)
				{
					if (player.getInventory().getInventoryItemCount(LEAF_OF_ORACLE, -1) == 0)
					{
						st.setState(State.STARTED);
						st.set("cond", "1");
						st.playSound("ItemSound.quest_accept");
						st.giveItems(CRYSTAL_MEDALLION, 1);
						htmltext = "30293-05.htm";
					}
					else
					{
						htmltext = "30293-04.htm";
					}
				}
				else
				{
					htmltext = "30293-03.htm";
				}
			}
			else if (player.getClassId().getId() == 0x1d)
			{
				htmltext = "30293-02a.htm";
			}
			else
			{
				htmltext = "30293-02.htm";
			}
		}
		else if (!st.isCompleted())
		{
			if (event.equalsIgnoreCase("30424_1"))
			{
				st.set("cond", "2");
				st.addSpawn(LIZARDMAN_WARRIOR);
				st.addSpawn(LIZARDMAN_SCOUT);
				st.addSpawn(LIZARDMAN);
				return null;
			}
			else if (event.equalsIgnoreCase("30428_1"))
			{
				htmltext = "30428-02.htm";
			}
			else if (event.equalsIgnoreCase("30428_2"))
			{
				htmltext = "30428-03.htm";
			}
			else if (event.equalsIgnoreCase("30428_3"))
			{
				st.addSpawn(TAMIL);
				return null;
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
		
		if (npcId == MANUEL)
		{
			if (st.getQuestItemsCount(CRYSTAL_MEDALLION) != 0)
			{
				if ((st.getQuestItemsCount(ALLANAS_DIARY) == 0) && (st.getQuestItemsCount(LIZARD_CAPTAIN_ORDER) == 0) && (st.getQuestItemsCount(SWINDLERS_MONEY) == 0) && (st.getQuestItemsCount(HALF_OF_DIARY) == 0))
				{
					if (cond == 0)
					{
						htmltext = "30293-06.htm";
					}
					else
					{
						htmltext = "30293-09.htm";
					}
				}
				else
				{
					if ((st.getQuestItemsCount(ALLANAS_DIARY) != 0) && (st.getQuestItemsCount(LIZARD_CAPTAIN_ORDER) != 0) && (st.getQuestItemsCount(SWINDLERS_MONEY) != 0) && (st.getQuestItemsCount(HALF_OF_DIARY) == 0))
					{
						st.takeItems(SWINDLERS_MONEY, -1);
						st.takeItems(ALLANAS_DIARY, -1);
						st.takeItems(LIZARD_CAPTAIN_ORDER, -1);
						st.takeItems(CRYSTAL_MEDALLION, -1);
						String done = st.getGlobalQuestVar("1ClassQuestFinished");
						st.set("cond", "0");
						st.exitQuest(false);
						if (done.isEmpty())
						{
							if (player.getLevel() >= 20)
							{
								st.addExpAndSp(320534, 20392);
							}
							else if (player.getLevel() == 19)
							{
								st.addExpAndSp(456128, 27090);
							}
							else
							{
								st.addExpAndSp(591724, 33788);
							}
							st.giveItems(57, 163800);
						}
						st.giveItems(LEAF_OF_ORACLE, 1);
						st.saveGlobalQuestVar("1ClassQuestFinished", "1");
						st.playSound("ItemSound.quest_finish");
						player.sendPacket(new SocialAction(player.getObjectId(), 3));
						htmltext = "30293-08.htm";
					}
					else
					{
						htmltext = "30293-07.htm";
					}
				}
			}
			else if (cond == 0)
			{
				if (st.getQuestItemsCount(LEAF_OF_ORACLE) == 0)
				{
					htmltext = "30293-01.htm";
				}
				else
				{
					htmltext = "30293-04.htm";
				}
			}
		}
		else if ((cond != 0) && (st.getQuestItemsCount(CRYSTAL_MEDALLION) != 0))
		{
			if (npcId == ALLANA)
			{
				if ((st.getQuestItemsCount(ALLANAS_DIARY) == 0) && (st.getQuestItemsCount(LIZARD_CAPTAIN_ORDER) == 0) && (st.getQuestItemsCount(SWINDLERS_MONEY) == 0) && (st.getQuestItemsCount(HALF_OF_DIARY) == 0))
				{
					if (cond > 2)
					{
						htmltext = "30424-05.htm";
					}
					else
					{
						htmltext = "30424-01.htm";
					}
				}
				else if ((st.getQuestItemsCount(ALLANAS_DIARY) == 0) && (st.getQuestItemsCount(LIZARD_CAPTAIN_ORDER) != 0) && (st.getQuestItemsCount(SWINDLERS_MONEY) == 0) && (st.getQuestItemsCount(HALF_OF_DIARY) == 0))
				{
					st.giveItems(HALF_OF_DIARY, 1);
					st.set("cond", "4");
					htmltext = "30424-02.htm";
				}
				else if ((st.getQuestItemsCount(ALLANAS_DIARY) == 0) && (st.getQuestItemsCount(LIZARD_CAPTAIN_ORDER) != 0) && (st.getQuestItemsCount(SWINDLERS_MONEY) == 0) && (st.getQuestItemsCount(HALF_OF_DIARY) != 0))
				{
					if (st.getQuestItemsCount(TAMILS_NECKLACE) == 0)
					{
						htmltext = "30424-06.htm";
					}
					else
					{
						htmltext = "30424-03.htm";
					}
				}
				else if ((st.getQuestItemsCount(ALLANAS_DIARY) == 0) && (st.getQuestItemsCount(LIZARD_CAPTAIN_ORDER) != 0) && (st.getQuestItemsCount(SWINDLERS_MONEY) != 0) && (st.getQuestItemsCount(HALF_OF_DIARY) != 0))
				{
					st.takeItems(HALF_OF_DIARY, -1);
					st.giveItems(ALLANAS_DIARY, 1);
					st.set("cond", "7");
					htmltext = "30424-04.htm";
				}
				else if ((st.getQuestItemsCount(ALLANAS_DIARY) != 0) && (st.getQuestItemsCount(LIZARD_CAPTAIN_ORDER) != 0) && (st.getQuestItemsCount(SWINDLERS_MONEY) != 0) && (st.getQuestItemsCount(HALF_OF_DIARY) == 0))
				{
					htmltext = "30424-05.htm";
				}
			}
			else if (st.getQuestItemsCount(LIZARD_CAPTAIN_ORDER) != 0)
			{
				if (st.getQuestItemsCount(TAMILS_NECKLACE) != 0)
				{
					st.takeItems(TAMILS_NECKLACE, -1);
					st.giveItems(SWINDLERS_MONEY, 1);
					st.set("cond", "6");
					htmltext = "30428-04.htm";
				}
				else
				{
					if (st.getQuestItemsCount(SWINDLERS_MONEY) == 0)
					{
						if (cond > 4)
						{
							htmltext = "30428-06.htm";
						}
						else
						{
							htmltext = "30428-01.htm";
						}
					}
					else
					{
						htmltext = "30428-05.htm";
					}
				}
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
		
		if (npcId == TAMIL)
		{
			if (st.getQuestItemsCount(TAMILS_NECKLACE) == 0)
			{
				st.giveItems(TAMILS_NECKLACE, 1);
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "5");
			}
		}
		else if (st.getQuestItemsCount(LIZARD_CAPTAIN_ORDER) == 0)
		{
			st.giveItems(LIZARD_CAPTAIN_ORDER, 1);
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "3");
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _409_PathToOracle(409, qn, "");
	}
}