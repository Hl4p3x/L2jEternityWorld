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
public class _416_PathToOrcShaman extends Quest
{	
	private static final String qn = "_416_PathToOrcShaman";
	
	// Npc
	private static final int TATARU_HESTUI = 30585;
	private static final int UMOS = 30502;
	private static final int H_TOTEM_SPIRIT = 30592;
	private static final int DM_TOTEM_SPIRIT = 30593;
	private static final int MOIRA = 31979;
	private static final int G_TOTEM_SPIRIT = 32057;
	private static final int CARCASS = 32090;
	
	private static final int[] TALKERS =
	{
		TATARU_HESTUI,
		UMOS,
		H_TOTEM_SPIRIT,
		DM_TOTEM_SPIRIT,
		MOIRA,
		G_TOTEM_SPIRIT,
		CARCASS
	};
	
	// Quest items
	private static final int FIRE_CHARM = 1616;
	private static final int KASHA_BEAR_PELT = 1617;
	private static final int KASHA_BSPIDER_HUSK = 1618;
	private static final int FIERY_EGG1 = 1619;
	private static final int HESTUI_MASK = 1620;
	private static final int FIERY_EGG2 = 1621;
	private static final int TOTEM_SPIRIT_CLAW = 1622;
	private static final int TATARUS_LETTER = 1623;
	private static final int FLAME_CHARM = 1624;
	private static final int GRIZZLY_BLOOD = 1625;
	private static final int BLOOD_CAULDRON = 1626;
	private static final int SPIRIT_NET = 1627;
	private static final int BOUND_DURKA_SPIRIT = 1628;
	private static final int DURKA_PARASITE = 1629;
	private static final int TOTEM_SPIRIT_BLOOD = 1630;
	private static final int MASK_OF_MEDIUM = 1631;
	
	private static final int[] QUESTITEMS =
	{
		FIRE_CHARM,
		KASHA_BEAR_PELT,
		KASHA_BSPIDER_HUSK,
		FIERY_EGG1,
		HESTUI_MASK,
		FIERY_EGG2,
		TOTEM_SPIRIT_CLAW,
		TATARUS_LETTER,
		FLAME_CHARM,
		GRIZZLY_BLOOD,
		BLOOD_CAULDRON,
		SPIRIT_NET,
		BOUND_DURKA_SPIRIT,
		DURKA_PARASITE,
		TOTEM_SPIRIT_BLOOD
	};
	
	// Mobs
	private static final int BEAR = 20335;
	private static final int SPIDER = 20038;
	private static final int SALAMANDER = 20415;
	private static final int TRACKER = 20043;
	private static final int KASHA_SPIDER = 20478;
	private static final int KASHA_BEAR = 20479;
	private static final int SPIRIT = 27056;
	private static final int LEOPARD = 27319;
	
	private static final int[] MOBS =
	{
		BEAR,
		SPIDER,
		SALAMANDER,
		TRACKER,
		KASHA_SPIDER,
		KASHA_BEAR,
		SPIRIT,
		LEOPARD
	};
	
	public _416_PathToOrcShaman(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(TATARU_HESTUI);
		
		for (int talkId : TALKERS)
		{
			addTalkId(talkId);
		}
		
		for (int mobId : MOBS)
		{
			addKillId(mobId);
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
		
		if (event.equalsIgnoreCase("1"))
		{
			st.set("id", "0");
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(FIRE_CHARM, 1);
			htmltext = "30585-06.htm";
		}
		else if (event.equalsIgnoreCase("32057_1"))
		{
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "14");
			htmltext = "32057-02.htm";
		}
		else if (event.equalsIgnoreCase("32057_2"))
		{
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "21");
			htmltext = "32057-05.htm";
		}
		else if (event.equalsIgnoreCase("32090_1"))
		{
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "18");
			htmltext = "32090-04.htm";
		}
		else if (event.equalsIgnoreCase("30585_1"))
		{
			if (player.getClassId().getId() != 0x31)
			{
				htmltext = player.getClassId().getId() == 0x32 ? "30585-02a.htm" : "30585-02.htm";
			}
			else
			{
				if (player.getLevel() < 18)
				{
					htmltext = "30585-03.htm";
				}
				else
				{
					htmltext = st.getQuestItemsCount(MASK_OF_MEDIUM) != 0 ? "30585-04.htm" : "30585-05.htm";
				}
			}
		}
		else if (event.equalsIgnoreCase("30585_1a"))
		{
			htmltext = "30585-10a.htm";
		}
		else if (event.equalsIgnoreCase("30585_2"))
		{
			st.takeItems(TOTEM_SPIRIT_CLAW, 1);
			st.giveItems(TATARUS_LETTER, 1);
			st.set("cond", "5");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30585-11.htm";
		}
		else if (event.equalsIgnoreCase("30585_3"))
		{
			st.takeItems(TOTEM_SPIRIT_CLAW, 1);
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "12");
			htmltext = "30585-11a.htm";
		}
		else if (event.equalsIgnoreCase("30592_1"))
		{
			htmltext = "30592-02.htm";
		}
		else if (event.equalsIgnoreCase("30592_2"))
		{
			st.takeItems(HESTUI_MASK, 1);
			st.takeItems(FIERY_EGG2, 1);
			st.giveItems(TOTEM_SPIRIT_CLAW, 1);
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30592-03.htm";
		}
		else if (event.equalsIgnoreCase("30502_2"))
		{
			st.takeItems(TOTEM_SPIRIT_BLOOD, st.getQuestItemsCount(TOTEM_SPIRIT_BLOOD));
			String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
			if (isFinished.equalsIgnoreCase(""))
			{
				st.addExpAndSp(295862, 3440);
			}
			st.giveItems(MASK_OF_MEDIUM, 1);
			st.saveGlobalQuestVar("1ClassQuestFinished", "1");
			st.set("cond", "0");
			player.sendPacket(new SocialAction(player.getObjectId(), 3));
			player.sendPacket(new SocialAction(player.getObjectId(), 15));
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
			htmltext = "30502-07.htm";
		}
		else if (event.equalsIgnoreCase("30593_1"))
		{
			htmltext = "30593-02.htm";
		}
		else if (event.equalsIgnoreCase("30593_2"))
		{
			st.takeItems(BLOOD_CAULDRON, 1);
			st.giveItems(SPIRIT_NET, 1);
			st.set("cond", "9");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30593-03.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = Quest.getNoQuestMsg(talker);
		QuestState st = talker.getQuestState(getName());
		
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		int state = st.getState();
		int cond = st.getInt("cond");
		if ((npcId != TATARU_HESTUI) && (state != State.STARTED))
		{
			return htmltext;
		}
		
		if ((npcId == TATARU_HESTUI) && (cond == 0))
		{
			htmltext = "30585-01.htm";
		}
		else if ((npcId == TATARU_HESTUI) && (cond == 12))
		{
			htmltext = "30585-11a.htm";
		}
		else if ((npcId == MOIRA) && (cond == 12))
		{
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "13");
			htmltext = "31979-01.htm";
		}
		else if ((npcId == MOIRA) && (cond == 21))
		{
			st.giveItems(MASK_OF_MEDIUM, 1);
			st.giveItems(57, 81900);
			st.addExpAndSp(295862, 18194);
			talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
			talker.sendPacket(new SocialAction(talker.getObjectId(), 15));
			st.set("cond", "0");
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
			htmltext = "31979-03.htm";
		}
		else if ((npcId == MOIRA) && (cond == 13))
		{
			htmltext = "31979-02.htm";
		}
		else if ((npcId == G_TOTEM_SPIRIT) && (cond == 13))
		{
			htmltext = "32057-01.htm";
		}
		else if ((npcId == G_TOTEM_SPIRIT) && (cond == 14))
		{
			htmltext = "32057-03.htm";
		}
		else if ((npcId == G_TOTEM_SPIRIT) && (cond == 20))
		{
			htmltext = "32057-04.htm";
		}
		else if ((npcId == G_TOTEM_SPIRIT) && (cond == 21))
		{
			htmltext = "32057-05.htm";
		}
		else if ((npcId == CARCASS) && (cond == 15))
		{
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "16");
			htmltext = "32090-01.htm";
		}
		else if ((npcId == CARCASS) && (cond == 16))
		{
			htmltext = "32090-01.htm";
		}
		else if ((npcId == CARCASS) && (cond == 17))
		{
			htmltext = "32090-02.htm";
		}
		else if ((npcId == CARCASS) && (cond == 18))
		{
			htmltext = "32090-05.htm";
		}
		else if ((npcId == CARCASS) && (cond == 19))
		{
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "20");
			htmltext = "32090-06.htm";
		}
		else if ((npcId == CARCASS) && (cond == 20))
		{
			htmltext = "32090-06.htm";
		}
		else if ((npcId == TATARU_HESTUI) && (cond > 0) && (st.getQuestItemsCount(FIRE_CHARM) == 1) && ((st.getQuestItemsCount(KASHA_BEAR_PELT) + st.getQuestItemsCount(KASHA_BSPIDER_HUSK) + st.getQuestItemsCount(FIERY_EGG1)) < 3))
		{
			htmltext = "30585-07.htm";
		}
		else if ((npcId == TATARU_HESTUI) && (cond > 0) && (st.getQuestItemsCount(FIRE_CHARM) == 1) && ((st.getQuestItemsCount(KASHA_BEAR_PELT) + st.getQuestItemsCount(KASHA_BSPIDER_HUSK) + st.getQuestItemsCount(FIERY_EGG1)) >= 3))
		{
			st.takeItems(FIRE_CHARM, 1);
			st.takeItems(KASHA_BEAR_PELT, 1);
			st.takeItems(KASHA_BSPIDER_HUSK, 1);
			st.takeItems(FIERY_EGG1, 1);
			st.giveItems(HESTUI_MASK, 1);
			st.giveItems(FIERY_EGG2, 1);
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30585-08.htm";
		}
		else if ((npcId == TATARU_HESTUI) && (cond > 0) && (st.getQuestItemsCount(HESTUI_MASK) == 1) && (st.getQuestItemsCount(FIERY_EGG2) == 1))
		{
			htmltext = "30585-09.htm";
		}
		else if ((npcId == TATARU_HESTUI) && (cond > 0) && (st.getQuestItemsCount(TOTEM_SPIRIT_CLAW) == 1))
		{
			htmltext = "30585-10.htm";
		}
		else if ((npcId == TATARU_HESTUI) && (cond > 0) && (st.getQuestItemsCount(TATARUS_LETTER) == 1))
		{
			htmltext = "30585-12.htm";
		}
		else if ((npcId == TATARU_HESTUI) && (cond > 0) && ((st.getQuestItemsCount(GRIZZLY_BLOOD) > 0) || (st.getQuestItemsCount(FLAME_CHARM) > 0) || (st.getQuestItemsCount(BLOOD_CAULDRON) > 0) || (st.getQuestItemsCount(SPIRIT_NET) > 0) || (st.getQuestItemsCount(BOUND_DURKA_SPIRIT) > 0) || (st.getQuestItemsCount(TOTEM_SPIRIT_BLOOD) > 0)))
		{
			htmltext = "30585-13.htm";
		}
		else if ((npcId == H_TOTEM_SPIRIT) && (cond > 0) && (st.getQuestItemsCount(HESTUI_MASK) > 0) && (st.getQuestItemsCount(FIERY_EGG2) > 0))
		{
			htmltext = "30592-01.htm";
		}
		else if ((npcId == H_TOTEM_SPIRIT) && (cond > 0) && (st.getQuestItemsCount(TOTEM_SPIRIT_CLAW) > 0))
		{
			htmltext = "30592-04.htm";
		}
		else if ((npcId == H_TOTEM_SPIRIT) && (cond > 0) && ((st.getQuestItemsCount(GRIZZLY_BLOOD) > 0) || (st.getQuestItemsCount(FLAME_CHARM) > 0) || (st.getQuestItemsCount(BLOOD_CAULDRON) > 0) || (st.getQuestItemsCount(SPIRIT_NET) > 0) || (st.getQuestItemsCount(BOUND_DURKA_SPIRIT) > 0) || (st.getQuestItemsCount(TOTEM_SPIRIT_BLOOD) > 0) || (st.getQuestItemsCount(TATARUS_LETTER) > 0)))
		{
			htmltext = "30592-05.htm";
		}
		else if ((npcId == UMOS) && (cond > 0) && (st.getQuestItemsCount(TATARUS_LETTER) > 0))
		{
			st.giveItems(FLAME_CHARM, 1);
			st.takeItems(TATARUS_LETTER, 1);
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30502-01.htm";
		}
		else if ((npcId == UMOS) && (cond > 0) && (st.getQuestItemsCount(FLAME_CHARM) == 1) && (st.getQuestItemsCount(GRIZZLY_BLOOD) < 3))
		{
			htmltext = "30502-02.htm";
		}
		else if ((npcId == UMOS) && (cond > 0) && (st.getQuestItemsCount(FLAME_CHARM) == 1) && (st.getQuestItemsCount(GRIZZLY_BLOOD) >= 3))
		{
			st.takeItems(FLAME_CHARM, 1);
			st.takeItems(GRIZZLY_BLOOD, st.getQuestItemsCount(GRIZZLY_BLOOD));
			st.giveItems(BLOOD_CAULDRON, 1);
			st.set("cond", "8");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30502-03.htm";
		}
		else if ((npcId == UMOS) && (cond > 0) && (st.getQuestItemsCount(BLOOD_CAULDRON) == 1))
		{
			htmltext = "30502-04.htm";
		}
		else if ((npcId == UMOS) && (cond > 0) && ((st.getQuestItemsCount(BOUND_DURKA_SPIRIT) == 1) || (st.getQuestItemsCount(SPIRIT_NET) == 1)))
		{
			htmltext = "30502-05.htm";
		}
		else if ((npcId == UMOS) && (cond > 0) && (st.getQuestItemsCount(TOTEM_SPIRIT_BLOOD) == 1))
		{
			htmltext = "30502-06.htm";
		}
		else if ((npcId == DM_TOTEM_SPIRIT) && (cond > 0) && (st.getQuestItemsCount(BLOOD_CAULDRON) > 0))
		{
			htmltext = "30593-01.htm";
		}
		else if ((npcId == DM_TOTEM_SPIRIT) && (cond > 0) && (st.getQuestItemsCount(SPIRIT_NET) > 0) && (st.getQuestItemsCount(BOUND_DURKA_SPIRIT) == 0))
		{
			htmltext = "30593-04.htm";
		}
		else if ((npcId == DM_TOTEM_SPIRIT) && (cond > 0) && (st.getQuestItemsCount(SPIRIT_NET) == 0) && (st.getQuestItemsCount(BOUND_DURKA_SPIRIT) > 0))
		{
			st.takeItems(BOUND_DURKA_SPIRIT, 1);
			st.giveItems(TOTEM_SPIRIT_BLOOD, 1);
			st.set("cond", "11");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30593-05.htm";
		}
		else if ((npcId == DM_TOTEM_SPIRIT) && (cond == 1) && (st.getQuestItemsCount(TOTEM_SPIRIT_BLOOD) > 0))
		{
			htmltext = "30593-06.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		QuestState st = killer.getQuestState(getName());
		
		if (st == null)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		if (st.getState() != State.STARTED)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		if (npcId == KASHA_BEAR)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(FIRE_CHARM) == 1) && (st.getQuestItemsCount(KASHA_BEAR_PELT) < 1))
			{
				if ((st.getQuestItemsCount(KASHA_BEAR_PELT) + st.getQuestItemsCount(KASHA_BSPIDER_HUSK) + st.getQuestItemsCount(FIERY_EGG1)) == 2)
				{
					st.giveItems(KASHA_BEAR_PELT, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "2");
				}
				else
				{
					st.giveItems(KASHA_BEAR_PELT, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == LEOPARD)
		{
			if (cond == 14)
			{
				st.set("cond", "15");
				st.playSound("ItemSound.quest_middle");
				npc.broadcastNpcSay("My dear friend of " + killer.getName() + ", who has gone on ahead of me!");
			}
			
			else if (cond == 16)
			{
				st.set("cond", "17");
				st.playSound("ItemSound.quest_middle");
				npc.broadcastNpcSay("Listen to Tejakar Gandi, young Oroka! The spirit of the slain leopard is calling you, " + killer.getName() + "!");
			}
			else if (cond == 18)
			{
				st.set("cond", "19");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npcId == KASHA_SPIDER)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(FIRE_CHARM) == 1) && (st.getQuestItemsCount(KASHA_BSPIDER_HUSK) < 1))
			{
				if ((st.getQuestItemsCount(KASHA_BEAR_PELT) + st.getQuestItemsCount(KASHA_BSPIDER_HUSK) + st.getQuestItemsCount(FIERY_EGG1)) == 2)
				{
					st.giveItems(KASHA_BSPIDER_HUSK, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "2");
				}
				else
				{
					st.giveItems(KASHA_BSPIDER_HUSK, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == SALAMANDER)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(FIRE_CHARM) == 1) && (st.getQuestItemsCount(FIERY_EGG1) < 1))
			{
				if ((st.getQuestItemsCount(KASHA_BEAR_PELT) + st.getQuestItemsCount(KASHA_BSPIDER_HUSK) + st.getQuestItemsCount(FIERY_EGG1)) == 2)
				{
					st.giveItems(FIERY_EGG1, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "2");
				}
				else
				{
					st.giveItems(FIERY_EGG1, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == BEAR)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(FLAME_CHARM) == 1) && (st.getQuestItemsCount(GRIZZLY_BLOOD) < 3))
			{
				if (st.getQuestItemsCount(GRIZZLY_BLOOD) == 2)
				{
					st.giveItems(GRIZZLY_BLOOD, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "7");
				}
				else
				{
					st.giveItems(GRIZZLY_BLOOD, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == SPIDER)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(SPIRIT_NET) == 1) && (st.getQuestItemsCount(BOUND_DURKA_SPIRIT) == 0) && (st.getQuestItemsCount(DURKA_PARASITE) < 8))
			{
				int n = st.getRandom(10);
				if ((st.getQuestItemsCount(DURKA_PARASITE) == 5) && (n < 1))
				{
					st.takeItems(DURKA_PARASITE, st.getQuestItemsCount(DURKA_PARASITE));
					st.addSpawn(SPIRIT);
					st.playSound("ItemSound.quest_itemget");
				}
				else if ((st.getQuestItemsCount(DURKA_PARASITE) == 6) && (n < 2))
				{
					st.takeItems(DURKA_PARASITE, st.getQuestItemsCount(DURKA_PARASITE));
					st.playSound("ItemSound.quest_itemget");
					st.addSpawn(SPIRIT);
				}
				else if ((st.getQuestItemsCount(DURKA_PARASITE) == 7) && (n < 2))
				{
					st.takeItems(DURKA_PARASITE, st.getQuestItemsCount(DURKA_PARASITE));
					st.playSound("ItemSound.quest_itemget");
					st.addSpawn(SPIRIT);
				}
				else if (st.getQuestItemsCount(DURKA_PARASITE) >= 7)
				{
					st.addSpawn(SPIRIT);
					st.playSound("ItemSound.quest_itemget");
					st.takeItems(DURKA_PARASITE, st.getQuestItemsCount(DURKA_PARASITE));
				}
				else
				{
					st.giveItems(DURKA_PARASITE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == TRACKER)
		{
			st.set("id", "0");
			if ((st.getInt("cond") > 0) && (st.getQuestItemsCount(SPIRIT_NET) == 1) && (st.getQuestItemsCount(BOUND_DURKA_SPIRIT) == 0) && (st.getQuestItemsCount(DURKA_PARASITE) < 8))
			{
				int n = st.getRandom(10);
				if ((st.getQuestItemsCount(DURKA_PARASITE) == 5) && (n < 1))
				{
					st.takeItems(DURKA_PARASITE, st.getQuestItemsCount(DURKA_PARASITE));
					st.addSpawn(SPIRIT);
					st.playSound("ItemSound.quest_itemget");
				}
				else if ((st.getQuestItemsCount(DURKA_PARASITE) == 6) && (n < 2))
				{
					st.takeItems(DURKA_PARASITE, st.getQuestItemsCount(DURKA_PARASITE));
					st.addSpawn(SPIRIT);
					st.playSound("ItemSound.quest_itemget");
				}
				else if ((st.getQuestItemsCount(DURKA_PARASITE) == 7) && (n < 2))
				{
					st.takeItems(DURKA_PARASITE, st.getQuestItemsCount(DURKA_PARASITE));
					st.addSpawn(SPIRIT);
					st.playSound("ItemSound.quest_itemget");
				}
				else if (st.getQuestItemsCount(DURKA_PARASITE) >= 7)
				{
					st.takeItems(DURKA_PARASITE, st.getQuestItemsCount(DURKA_PARASITE));
					st.addSpawn(SPIRIT);
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					st.giveItems(DURKA_PARASITE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == SPIRIT)
		{
			st.set("id", "0");
			if ((st.getInt("cond") > 0) && (st.getQuestItemsCount(SPIRIT_NET) == 1) && (st.getQuestItemsCount(BOUND_DURKA_SPIRIT) == 0))
			{
				st.giveItems(BOUND_DURKA_SPIRIT, 1);
				st.takeItems(SPIRIT_NET, 1);
				st.takeItems(DURKA_PARASITE, st.getQuestItemsCount(DURKA_PARASITE));
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "10");
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _416_PathToOrcShaman(416, qn, "");
	}
}