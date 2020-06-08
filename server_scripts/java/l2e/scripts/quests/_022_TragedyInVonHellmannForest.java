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

import java.util.ArrayList;
import java.util.List;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter $ Sigrlinne 14.03.2011 Based on L2J Eternity-World
 */
public class _022_TragedyInVonHellmannForest extends Quest
{
	private static final String qn = "_022_TragedyInVonHellmannForest";
	
	// Npcs
	private final static int WELL = 31527;
	private final static int TIFAREN = 31334;
	private final static int INNOCENTIN = 31328;
	private final static int SOUL_OF_WELL = 27217;
	private final static int GHOST_OF_PRIEST = 31528;
	private final static int GHOST_OF_ADVENTURER = 31529;
	
	// Items
	private final static int REPORT_BOX = 7147;
	private final static int LOST_SKULL_OF_ELF = 7142;
	private final static int CROSS_OF_EINHASAD = 7141;
	private final static int SEALED_REPORT_BOX = 7146;
	private final static int LETTER_OF_INNOCENTIN = 7143;
	private final static int JEWEL_OF_ADVENTURER_RED = 7145;
	private final static int JEWEL_OF_ADVENTURER_GREEN = 7144;
	
	// Monsters
	private static final List<Integer> MOBS = new ArrayList<>();
	static
	{
		for (int i : new int[]
		{
			21553,
			21554,
			21555,
			21556,
			21557,
			21561
		})
		{
			MOBS.add(i);
		}
	}
	
	private static L2Npc GHOST_OF_PRIESTInstance = null;
	private static L2Npc SOUL_OF_WELLInstance = null;
	
	public _022_TragedyInVonHellmannForest(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(INNOCENTIN);
		addStartNpc(TIFAREN);
		addTalkId(INNOCENTIN);
		addTalkId(TIFAREN);
		addTalkId(GHOST_OF_PRIEST);
		addTalkId(GHOST_OF_ADVENTURER);
		addTalkId(WELL);
		addAttackId(SOUL_OF_WELL);
		addKillId(SOUL_OF_WELL);
		
		for (int npcId = 21553; npcId <= 21557; npcId++)
		{
			addKillId(npcId);
		}
		
		questItemIds = new int[]
		{
			LOST_SKULL_OF_ELF,
			REPORT_BOX,
			SEALED_REPORT_BOX,
			LETTER_OF_INNOCENTIN,
			JEWEL_OF_ADVENTURER_RED,
			JEWEL_OF_ADVENTURER_GREEN
		};
		
	}
	
	private void spawnGHOST_OF_PRIEST(QuestState st)
	{
		GHOST_OF_PRIESTInstance = st.addSpawn(GHOST_OF_PRIEST, st.getPlayer().getX(), st.getPlayer().getY(), st.getPlayer().getZ(), getRandom(50, 100), true, 0);
	}
	
	private void spawnSOUL_OF_WELL(QuestState st)
	{
		SOUL_OF_WELLInstance = st.addSpawn(SOUL_OF_WELL, st.getPlayer().getX(), st.getPlayer().getY(), st.getPlayer().getZ(), getRandom(50, 100), true, 0);
	}
	
	private void despawnGHOST_OF_PRIEST()
	{
		if (GHOST_OF_PRIESTInstance != null)
		{
			GHOST_OF_PRIESTInstance.deleteMe();
		}
	}
	
	private void despawnSOUL_OF_WELL()
	{
		if (SOUL_OF_WELLInstance != null)
		{
			SOUL_OF_WELLInstance.deleteMe();
		}
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
		
		if (event.equalsIgnoreCase("31334-05.htm"))
		{
			if (st.getInt("cond") > 2)
			{
				st.takeItems(CROSS_OF_EINHASAD, 1);
			}
		}
		else if (event.equalsIgnoreCase("31334-06.htm"))
		{
			st.set("cond", "4");
		}
		else if (event.equalsIgnoreCase("31334-13.htm"))
		{
			st.set("cond", "6");
			st.takeItems(LOST_SKULL_OF_ELF, 1);
			despawnGHOST_OF_PRIEST();
			spawnGHOST_OF_PRIEST(st);
			st.playSound("AmbSound.d_horror_15");
		}
		else if (event.equalsIgnoreCase("31528-09.htm"))
		{
			despawnGHOST_OF_PRIEST();
			st.set("cond", "8");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31328-10.htm"))
		{
			st.set("cond", "9");
			st.giveItems(LETTER_OF_INNOCENTIN, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31529-03.htm"))
		{
			st.takeItems(LETTER_OF_INNOCENTIN, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31529-12.htm"))
		{
			despawnSOUL_OF_WELL();
			spawnSOUL_OF_WELL(st);
			st.set("cond", "10");
			st.giveItems(JEWEL_OF_ADVENTURER_GREEN, 1);
			htmltext = "31529-12a.htm";
			st.playSound("SkillSound3.antaras_fear");
			st.startQuestTimer("attack_timer", 90000, SOUL_OF_WELLInstance);
			((L2Attackable) SOUL_OF_WELLInstance).addDamageHate(player, 0, 99999);
			SOUL_OF_WELLInstance.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player, true);
		}
		else if (event.equalsIgnoreCase("31527-02.htm"))
		{
			despawnSOUL_OF_WELL();
			spawnSOUL_OF_WELL(st);
			st.set("cond", "12");
			st.giveItems(JEWEL_OF_ADVENTURER_GREEN, 1);
			htmltext = "31527-02a.htm";
			st.playSound("SkillSound3.antaras_fear");
			st.startQuestTimer("attack_timer1", 90000, SOUL_OF_WELLInstance);
			((L2Attackable) SOUL_OF_WELLInstance).addDamageHate(player, 0, 99999);
			SOUL_OF_WELLInstance.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player, true);
		}
		else if (event.equalsIgnoreCase("attack_timer"))
		{
			despawnSOUL_OF_WELL();
			st.giveItems(JEWEL_OF_ADVENTURER_RED, 1);
			st.takeItems(JEWEL_OF_ADVENTURER_GREEN, -1);
			st.set("cond", "11");
			st.playSound("ItemSound.quest_itemget");
		}
		else if (event.equalsIgnoreCase("attack_timer1"))
		{
			despawnSOUL_OF_WELL();
			st.giveItems(JEWEL_OF_ADVENTURER_RED, 1);
			st.takeItems(JEWEL_OF_ADVENTURER_GREEN, -1);
			st.set("cond", "13");
			st.playSound("ItemSound.quest_itemget");
		}
		else if (event.equalsIgnoreCase("31328-13.htm"))
		{
			st.startQuestTimer("wait_timer", 60000);
			st.set("cond", "15");
			st.takeItems(REPORT_BOX, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("wait_timer"))
		{
			st.set("cond", "16");
		}
		else if (event.equalsIgnoreCase("31328-21.htm"))
		{
			st.startQuestTimer("next_wait_timer", 30000);
			st.set("cond", "17");
		}
		else if (event.equalsIgnoreCase("next_wait_timer"))
		{
			st.set("cond", "18");
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
		
		final int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				final QuestState st2 = player.getQuestState("_021_HiddenTruth");
				if ((st2 != null) && (st2.getState() == State.COMPLETED))
				{
					if (player.getLevel() >= 63)
					{
						htmltext = "31328-00.htm";
					}
					else
					{
						htmltext = "31334-03.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "31328-00a.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case INNOCENTIN:
						switch (cond)
						{
							case 1:
							case 2:
								if (st.getQuestItemsCount(CROSS_OF_EINHASAD) == 0)
								{
									st.giveItems(CROSS_OF_EINHASAD, 1);
									st.set("cond", "3");
									htmltext = "31328-01.htm";
									st.playSound("ItemSound.quest_middle");
								}
								else
								{
									htmltext = "31328-02.htm";
								}
								break;
							case 3:
								htmltext = "31328-02.htm";
								break;
							case 8:
								htmltext = "31328-03.htm";
								break;
							case 9:
								htmltext = "31328-10.htm";
								break;
							case 14:
								if (st.getQuestItemsCount(REPORT_BOX) != 0)
								{
									htmltext = "31328-12.htm";
								}
								else
								{
									st.set("cond", "13");
									htmltext = "31328-12a.htm";
								}
								break;
							case 15:
								htmltext = "31328-13a.htm";
								break;
							case 16:
								htmltext = "31328-14.htm";
								break;
							case 17:
								htmltext = "31328-17a.htm";
								break;
							case 18:
								htmltext = "31328-17.htm";
								st.playSound("ItemSound.quest_finish");
								st.addExpAndSp(345966, 31578);
								st.unset("cond");
								st.setState(State.COMPLETED);
								st.exitQuest(false);
								if (player.getLevel() < 64)
								{
									htmltext = "31328-23.htm";
								}
								else
								{
									htmltext = "31328-22.htm";
								}
								break;
						}
						break;
					case TIFAREN:
						switch (cond)
						{
							case 1:
								if (st.getQuestItemsCount(CROSS_OF_EINHASAD) != 0)
								{
									st.set("cond", "1");
									st.setState(State.STARTED);
									st.playSound("ItemSound.quest_accept");
									st.takeItems(CROSS_OF_EINHASAD, 1);
									htmltext = "31334-04.htm";
								}
								else
								{
									st.set("cond", "2");
								}
								st.setState(State.STARTED);
								htmltext = "31334-04.htm";
								break;
							case 2:
								htmltext = "31334-04.htm";
								break;
							case 3:
								htmltext = "31334-05.htm";
								break;
							case 4:
								htmltext = "31334-06.htm";
								break;
							case 5:
								if (st.getQuestItemsCount(LOST_SKULL_OF_ELF) != 0)
								{
									htmltext = "31334-10.htm";
								}
								else
								{
									st.set("cond", "4");
									htmltext = "31334-06.htm";
								}
								break;
							case 6:
								despawnGHOST_OF_PRIEST();
								spawnGHOST_OF_PRIEST(st);
								htmltext = "31334-13.htm";
								break;
						}
						break;
					case GHOST_OF_PRIEST:
						switch (cond)
						{
							case 6:
								htmltext = "31528-01.htm";
								break;
							case 8:
								htmltext = "31528-08.htm";
								break;
						}
						break;
					case GHOST_OF_ADVENTURER:
						switch (cond)
						{
							case 9:
								if (st.getQuestItemsCount(LETTER_OF_INNOCENTIN) != 0)
								{
									htmltext = "31529-01.htm";
								}
								else
								{
									htmltext = "31529-01a.htm";
								}
								break;
							case 10:
								htmltext = "31529-09.htm";
								break;
							case 11:
								if (st.getQuestItemsCount(JEWEL_OF_ADVENTURER_RED) != 0)
								{
									if (st.getQuestItemsCount(SEALED_REPORT_BOX) == 0)
									{
										htmltext = "31529-17.htm";
									}
									st.takeItems(JEWEL_OF_ADVENTURER_RED, 1);
									st.set("cond", "12");
									st.playSound("ItemSound.quest_middle");
								}
								else
								{
									st.set("cond", "10");
									htmltext = "31529-09.htm";
								}
								break;
							case 13:
								if (st.getQuestItemsCount(SEALED_REPORT_BOX) != 0)
								{
									htmltext = "31529-11.htm";
									st.set("cond", "14");
									st.takeItems(SEALED_REPORT_BOX, 1);
									st.giveItems(REPORT_BOX, 1);
									st.playSound("ItemSound.quest_middle");
								}
								else
								{
									st.set("cond", "12");
									htmltext = "31529-10.htm";
								}
								break;
						}
						break;
					case WELL:
						switch (cond)
						{
							case 11:
								htmltext = "31527-01.htm";
								break;
							case 12:
								st.set("cond", "12");
								st.takeItems(JEWEL_OF_ADVENTURER_GREEN, 1);
								st.takeItems(JEWEL_OF_ADVENTURER_RED, 1);
								htmltext = "31527-01.htm";
								break;
							case 13:
								if (st.getQuestItemsCount(SEALED_REPORT_BOX) == 0)
								{
									htmltext = "31527-05.htm";
									st.takeItems(JEWEL_OF_ADVENTURER_RED, 1);
									st.giveItems(SEALED_REPORT_BOX, 1);
									st.playSound("ItemSound.quest_middle");
								}
								break;
						}
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	public String onAttack(L2Npc npc, QuestState st)
	{
		if ((st.getQuestTimer("attack_timer") != null) || (st.getQuestTimer("attack_timer1") != null))
		{
			return null;
		}
		
		if (npc.getId() == SOUL_OF_WELL)
		{
			switch (st.getInt("cond"))
			{
				case 10:
					st.startQuestTimer("attack_timer", 300000);
					break;
				case 12:
					st.startQuestTimer("attack_timer1", 300000);
					break;
			}
		}
		
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		int npcId = npc.getId();
		
		if (MOBS.contains(npcId))
		{
			if ((st.getInt("cond") == 4) && (getRandom(10) < 1) && (st.getQuestItemsCount(LOST_SKULL_OF_ELF) < 1))
			{
				st.giveItems(LOST_SKULL_OF_ELF, 1);
				st.playSound("ItemSound.quest_itemget");
				st.set("cond", "5");
			}
		}
		
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _022_TragedyInVonHellmannForest(22, qn, "");
	}
}