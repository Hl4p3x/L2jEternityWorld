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
import l2e.gameserver.model.items.type.L2WeaponType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.serverpackets.SocialAction;

/**
 * Created by LordWinter 26.09.2012
 * Based on L2J Eternity-World
 */
public class _415_PathToOrcMonk extends Quest
{
	private static final String qn = "_415_PathToOrcMonk";
	
	// Npc
	private static final int URUTU = 30587;
	private static final int KASMAN = 30501;
	private static final int ROSHEEK = 30590;
	private static final int TORUKU = 30591;
	private static final int MOIRA = 31979;
	private static final int AREN = 32056;
	
	private static final int[] TALKERS =
	{
		30587,
		30501,
		30590,
		30591,
		31979,
		32056
	};
	
	// Quest items
	private static final int POMEGRANATE = 1593;
	private static final int LEATHER_POUCH1 = 1594;
	private static final int LEATHER_POUCH2 = 1595;
	private static final int LEATHER_POUCH3 = 1596;
	private static final int LEATHER_POUCH1FULL = 1597;
	private static final int LEATHER_POUCH2FULL = 1598;
	private static final int LEATHER_POUCH3FULL = 1599;
	private static final int KASHA_BEAR_CLAW = 1600;
	private static final int KASHA_BSPIDER_TALON = 1601;
	private static final int S_SALAMANDER_SCALE = 1602;
	private static final int SCROLL_FIERY_SPIRIT = 1603;
	private static final int ROSHEEKS_LETTER = 1604;
	private static final int GANTAKIS_LETTER = 1605;
	private static final int FIG = 1606;
	private static final int LEATHER_PURSE4 = 1607;
	private static final int LEATHER_POUCH4FULL = 1608;
	private static final int VUKU_TUSK = 1609;
	private static final int RATMAN_FANG = 1610;
	private static final int LANGK_TOOTH = 1611;
	private static final int FELIM_TOOTH = 1612;
	private static final int SCROLL_IRON_WILL = 1613;
	private static final int TORUKUS_LETTER = 1614;
	private static final int KHAVATARI_TOTEM = 1615;
	private static final int SPIDER_TOOTH = 8545;
	private static final int HORN_BAAR = 8546;
	
	private static final int[] QUESTITEMS =
	{
		POMEGRANATE,
		LEATHER_POUCH1,
		LEATHER_POUCH2,
		LEATHER_POUCH3,
		LEATHER_POUCH1FULL,
		LEATHER_POUCH2FULL,
		LEATHER_POUCH3FULL,
		KASHA_BEAR_CLAW,
		KASHA_BSPIDER_TALON,
		S_SALAMANDER_SCALE,
		SCROLL_FIERY_SPIRIT,
		ROSHEEKS_LETTER,
		GANTAKIS_LETTER,
		FIG,
		LEATHER_PURSE4,
		LEATHER_POUCH4FULL,
		VUKU_TUSK,
		RATMAN_FANG,
		LANGK_TOOTH,
		FELIM_TOOTH,
		SCROLL_IRON_WILL,
		TORUKUS_LETTER,
		SPIDER_TOOTH,
		HORN_BAAR
	};
	
	// Mobs
	private static final int F_LIZZARDMAN_WARRIOR = 20014;
	private static final int ORC_FIGHTER = 20017;
	private static final int L_LIZZARDMAN_WARRIOR = 20024;
	private static final int RATMAN_WARRIOR = 20359;
	private static final int SALAMANDER = 20415;
	private static final int TIMBER_SPIDER = 20476;
	private static final int BLADE_SPIDER = 20478;
	private static final int BEAR = 20479;
	private static final int VANUL = 21118;
	
	private static final int[] KILLS =
	{
		F_LIZZARDMAN_WARRIOR,
		ORC_FIGHTER,
		L_LIZZARDMAN_WARRIOR,
		RATMAN_WARRIOR,
		SALAMANDER,
		TIMBER_SPIDER,
		BLADE_SPIDER,
		BEAR,
		VANUL
	};
	
	public _415_PathToOrcMonk(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(URUTU);
		
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
		
		if (event.equalsIgnoreCase("30587_1"))
		{
			if (player.getClassId().getId() != 0x2c)
			{
				if (player.getClassId().getId() == 0x2f)
				{
					htmltext = "30587-02a.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "30587-02.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				if (player.getLevel() < 18)
				{
					htmltext = "30587-03.htm";
				}
				else
				{
					htmltext = st.getQuestItemsCount(KHAVATARI_TOTEM) != 0 ? "30587-04.htm" : "30587-05.htm";
				}
			}
		}
		else if (event.equalsIgnoreCase("1"))
		{
			st.set("id", "0");
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(POMEGRANATE, 1);
			htmltext = "30587-06.htm";
		}
		else if (event.equalsIgnoreCase("30587-09a.htm"))
		{
			st.takeItems(ROSHEEKS_LETTER, 1);
			st.giveItems(GANTAKIS_LETTER, 1);
			st.set("cond", "9");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30587-09b.htm"))
		{
			st.takeItems(ROSHEEKS_LETTER, 1);
			st.giveItems(GANTAKIS_LETTER, 1);
			st.set("cond", "14");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32056-03.htm"))
		{
			st.set("cond", "15");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("32056-08.htm"))
		{
			st.set("cond", "19");
			st.playSound("ItemSound.quest_middle");
			st.takeItems(8546, -1);
		}
		else if (event.equalsIgnoreCase("31979-03.htm"))
		{
			st.takeItems(SCROLL_FIERY_SPIRIT, 1);
			String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
			if (isFinished.equalsIgnoreCase(""))
			{
				st.addExpAndSp(295862, 4590);
			}
			st.giveItems(KHAVATARI_TOTEM, 1);
			st.saveGlobalQuestVar("1ClassQuestFinished", "1");
			st.set("cond", "0");
			st.set("onlyone", "1");
			player.sendPacket(new SocialAction(player.getObjectId(), 3));
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
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
		int state = st.getState();
		int cond = st.getInt("cond");
		
		if ((npcId != URUTU) && (state != State.STARTED))
		{
			return htmltext;
		}
		
		if ((npcId == URUTU) && (cond == 0) && (st.getInt("onlyone") == 0))
		{
			htmltext = "30587-01.htm";
		}
		else if ((npcId == URUTU) && (cond == 0) && (st.getInt("onlyone") == 1))
		{
			htmltext = "30587-04.htm";
		}
		else if ((npcId == URUTU) && (cond > 0) && (st.getQuestItemsCount(SCROLL_FIERY_SPIRIT) == 0) && (st.getQuestItemsCount(POMEGRANATE) == 1) && (st.getQuestItemsCount(GANTAKIS_LETTER) == 0) && (st.getQuestItemsCount(ROSHEEKS_LETTER) == 0) && ((st.getQuestItemsCount(LEATHER_POUCH1) + st.getQuestItemsCount(LEATHER_POUCH2) + st.getQuestItemsCount(LEATHER_POUCH3) + st.getQuestItemsCount(LEATHER_POUCH1FULL) + st.getQuestItemsCount(LEATHER_POUCH2FULL) + st.getQuestItemsCount(LEATHER_POUCH3FULL)) == 0))
		{
			htmltext = "30587-07.htm";
		}
		else if ((npcId == URUTU) && (cond > 0) && (st.getQuestItemsCount(SCROLL_FIERY_SPIRIT) == 0) && (st.getQuestItemsCount(POMEGRANATE) == 0) && (st.getQuestItemsCount(GANTAKIS_LETTER) == 0) && (st.getQuestItemsCount(ROSHEEKS_LETTER) == 0) && ((st.getQuestItemsCount(LEATHER_POUCH1) + st.getQuestItemsCount(LEATHER_POUCH2) + st.getQuestItemsCount(LEATHER_POUCH3) + st.getQuestItemsCount(LEATHER_POUCH1FULL) + st.getQuestItemsCount(LEATHER_POUCH2FULL) + st.getQuestItemsCount(LEATHER_POUCH3FULL)) == 1))
		{
			htmltext = "30587-08.htm";
		}
		else if ((npcId == URUTU) && (cond > 0) && (st.getQuestItemsCount(SCROLL_FIERY_SPIRIT) == 1) && (st.getQuestItemsCount(POMEGRANATE) == 0) && (st.getQuestItemsCount(GANTAKIS_LETTER) == 0) && (st.getQuestItemsCount(ROSHEEKS_LETTER) == 1) && ((st.getQuestItemsCount(LEATHER_POUCH1) + st.getQuestItemsCount(LEATHER_POUCH2) + st.getQuestItemsCount(LEATHER_POUCH3) + st.getQuestItemsCount(LEATHER_POUCH1FULL) + st.getQuestItemsCount(LEATHER_POUCH2FULL) + st.getQuestItemsCount(LEATHER_POUCH3FULL)) == 0))
		{
			htmltext = "30587-09.htm";
		}
		else if ((npcId == 30587) && (cond >= 14))
		{
			htmltext = "30587-09b.htm";
		}
		else if ((npcId == URUTU) && (cond > 0) && (st.getQuestItemsCount(SCROLL_FIERY_SPIRIT) == 1) && (st.getQuestItemsCount(POMEGRANATE) == 0) && (st.getQuestItemsCount(GANTAKIS_LETTER) == 1) && (st.getQuestItemsCount(ROSHEEKS_LETTER) == 0) && ((st.getQuestItemsCount(LEATHER_POUCH1) + st.getQuestItemsCount(LEATHER_POUCH2) + st.getQuestItemsCount(LEATHER_POUCH3) + st.getQuestItemsCount(LEATHER_POUCH1FULL) + st.getQuestItemsCount(LEATHER_POUCH2FULL) + st.getQuestItemsCount(LEATHER_POUCH3FULL)) == 0))
		{
			htmltext = "30587-10.htm";
		}
		else if ((npcId == URUTU) && (cond > 0) && (st.getQuestItemsCount(SCROLL_FIERY_SPIRIT) == 1) && (st.getQuestItemsCount(POMEGRANATE) == 0) && (st.getQuestItemsCount(GANTAKIS_LETTER) == 0) && (st.getQuestItemsCount(ROSHEEKS_LETTER) == 0) && ((st.getQuestItemsCount(LEATHER_POUCH1) + st.getQuestItemsCount(LEATHER_POUCH2) + st.getQuestItemsCount(LEATHER_POUCH3) + st.getQuestItemsCount(LEATHER_POUCH1FULL) + st.getQuestItemsCount(LEATHER_POUCH2FULL) + st.getQuestItemsCount(LEATHER_POUCH3FULL)) == 0))
		{
			htmltext = "30587-11.htm";
		}
		else if ((npcId == ROSHEEK) && (cond > 0) && (st.getQuestItemsCount(POMEGRANATE) > 0))
		{
			st.takeItems(POMEGRANATE, 1);
			st.giveItems(LEATHER_POUCH1, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30590-01.htm";
		}
		else if ((npcId == ROSHEEK) && (cond > 0) && (st.getQuestItemsCount(LEATHER_POUCH1) > 0) && (st.getQuestItemsCount(LEATHER_POUCH1FULL) == 0))
		{
			htmltext = "30590-02.htm";
		}
		else if ((npcId == ROSHEEK) && (cond > 0) && (st.getQuestItemsCount(LEATHER_POUCH1) == 0) && (st.getQuestItemsCount(LEATHER_POUCH1FULL) > 0))
		{
			st.takeItems(LEATHER_POUCH1FULL, 1);
			st.giveItems(LEATHER_POUCH2, 1);
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30590-03.htm";
		}
		else if ((npcId == ROSHEEK) && (cond > 0) && (st.getQuestItemsCount(LEATHER_POUCH2) == 1) && (st.getQuestItemsCount(LEATHER_POUCH2FULL) == 0))
		{
			htmltext = "30590-04.htm";
		}
		else if ((npcId == ROSHEEK) && (cond > 0) && (st.getQuestItemsCount(LEATHER_POUCH2) == 0) && (st.getQuestItemsCount(LEATHER_POUCH2FULL) == 1))
		{
			st.takeItems(LEATHER_POUCH2FULL, 1);
			st.giveItems(LEATHER_POUCH3, 1);
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30590-05.htm";
		}
		else if ((npcId == ROSHEEK) && (cond > 0) && (st.getQuestItemsCount(LEATHER_POUCH3) == 1) && (st.getQuestItemsCount(LEATHER_POUCH3FULL) == 0))
		{
			htmltext = "30590-06.htm";
		}
		else if ((npcId == ROSHEEK) && (cond > 0) && (st.getQuestItemsCount(LEATHER_POUCH3) == 0) && (st.getQuestItemsCount(LEATHER_POUCH3FULL) == 1))
		{
			st.takeItems(LEATHER_POUCH3FULL, 1);
			st.giveItems(SCROLL_FIERY_SPIRIT, 1);
			st.giveItems(ROSHEEKS_LETTER, 1);
			st.set("cond", "8");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30590-07.htm";
		}
		else if ((npcId == ROSHEEK) && (cond > 0) && (st.getQuestItemsCount(ROSHEEKS_LETTER) == 1) && (st.getQuestItemsCount(SCROLL_FIERY_SPIRIT) == 1))
		{
			htmltext = "30590-08.htm";
		}
		else if ((npcId == ROSHEEK) && (cond > 0) && (st.getQuestItemsCount(ROSHEEKS_LETTER) == 0) && (st.getQuestItemsCount(SCROLL_FIERY_SPIRIT) == 1))
		{
			htmltext = "30590-09.htm";
		}
		else if ((npcId == KASMAN) && (cond > 0) && (st.getQuestItemsCount(GANTAKIS_LETTER) > 0))
		{
			st.takeItems(GANTAKIS_LETTER, 1);
			st.giveItems(FIG, 1);
			st.set("cond", "10");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30501-01.htm";
		}
		else if ((npcId == KASMAN) && (cond > 0) && (st.getQuestItemsCount(FIG) > 0) && ((st.getQuestItemsCount(LEATHER_PURSE4) == 0) || (st.getQuestItemsCount(LEATHER_POUCH4FULL) == 0)))
		{
			htmltext = "30501-02.htm";
		}
		else if ((npcId == KASMAN) && (cond > 0) && (st.getQuestItemsCount(FIG) == 0) && ((st.getQuestItemsCount(LEATHER_PURSE4) == 1) || (st.getQuestItemsCount(LEATHER_POUCH4FULL) == 1)))
		{
			htmltext = "30501-03.htm";
		}
		else if ((npcId == KASMAN) && (cond > 0) && (st.getQuestItemsCount(SCROLL_IRON_WILL) > 0))
		{
			st.takeItems(SCROLL_IRON_WILL, 1);
			st.takeItems(SCROLL_FIERY_SPIRIT, 1);
			st.takeItems(TORUKUS_LETTER, 1);
			st.giveItems(KHAVATARI_TOTEM, 1);
			st.giveItems(57, 81900);
			st.addExpAndSp(295862, 19344);
			talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
			st.set("cond", "0");
			st.set("onlyone", "1");
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
			htmltext = "30501-04.htm";
		}
		else if ((npcId == TORUKU) && (cond > 0) && (st.getQuestItemsCount(FIG) > 0))
		{
			st.takeItems(FIG, 1);
			st.giveItems(LEATHER_PURSE4, 1);
			st.set("cond", "11");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30591-01.htm";
		}
		else if ((npcId == TORUKU) && (cond > 0) && (st.getQuestItemsCount(LEATHER_PURSE4) > 0) && (st.getQuestItemsCount(LEATHER_POUCH4FULL) == 0))
		{
			htmltext = "30591-02.htm";
		}
		else if ((npcId == TORUKU) && (cond > 0) && (st.getQuestItemsCount(LEATHER_PURSE4) == 0) && (st.getQuestItemsCount(LEATHER_POUCH4FULL) == 1))
		{
			st.takeItems(LEATHER_POUCH4FULL, 1);
			st.giveItems(SCROLL_IRON_WILL, 1);
			st.giveItems(TORUKUS_LETTER, 1);
			st.set("cond", "13");
			st.playSound("ItemSound.quest_middle");
			htmltext = "30591-03.htm";
		}
		else if ((npcId == TORUKU) && (cond > 0) && (st.getQuestItemsCount(SCROLL_IRON_WILL) == 1) && (st.getQuestItemsCount(TORUKUS_LETTER) == 1))
		{
			htmltext = "30591-04.htm";
		}
		else if (npcId == AREN)
		{
			if (cond == 14)
			{
				htmltext = "32056-01.htm";
			}
			else if (cond == 15)
			{
				htmltext = "32056-04.htm";
			}
			else if (cond == 16)
			{
				st.set("cond", "17");
				st.playSound("ItemSound.quest_middle");
				st.takeItems(8545, -1);
				htmltext = "32056-05.htm";
			}
			else if (cond == 17)
			{
				htmltext = "32056-06.htm";
			}
			else if (cond == 18)
			{
				htmltext = "32056-07.htm";
			}
			else if (cond == 19)
			{
				htmltext = "32056-09.htm";
			}
		}
		else if (npcId == MOIRA)
		{
			if (cond == 19)
			{
				htmltext = "31979-01.htm";
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
		
		if (killer.getActiveWeaponItem() != null)
		{
			if ((killer.getActiveWeaponItem().getItemType() != L2WeaponType.FIST) && (killer.getActiveWeaponItem().getItemType() != L2WeaponType.DUALFIST))
			{
				return super.onKill(npc, killer, isSummon);
			}
		}
		
		int npcId = npc.getId();
		int cond = st.getInt("cond");
		if (npcId == BEAR)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(LEATHER_POUCH1) == 1))
			{
				if (st.getQuestItemsCount(KASHA_BEAR_CLAW) == 4)
				{
					st.takeItems(KASHA_BEAR_CLAW, st.getQuestItemsCount(KASHA_BEAR_CLAW));
					st.takeItems(LEATHER_POUCH1, st.getQuestItemsCount(LEATHER_POUCH1));
					st.giveItems(LEATHER_POUCH1FULL, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "3");
				}
				else
				{
					st.giveItems(KASHA_BEAR_CLAW, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == SALAMANDER)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(LEATHER_POUCH3) == 1))
			{
				if (st.getQuestItemsCount(S_SALAMANDER_SCALE) == 4)
				{
					st.takeItems(S_SALAMANDER_SCALE, st.getQuestItemsCount(S_SALAMANDER_SCALE));
					st.takeItems(LEATHER_POUCH3, st.getQuestItemsCount(LEATHER_POUCH3));
					st.giveItems(LEATHER_POUCH3FULL, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "7");
				}
				else
				{
					st.giveItems(S_SALAMANDER_SCALE, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if ((npcId == TIMBER_SPIDER) && (cond == 15))
		{
			if ((st.getQuestItemsCount(8545) < 6) && (st.getRandom(100) <= 50))
			{
				if (st.getQuestItemsCount(8545) == 5)
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "16");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
					st.giveItems(8545, 1);
				}
			}
		}
		else if (npcId == BLADE_SPIDER)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(LEATHER_POUCH2) == 1))
			{
				if (st.getQuestItemsCount(KASHA_BSPIDER_TALON) == 4)
				{
					st.takeItems(KASHA_BSPIDER_TALON, st.getQuestItemsCount(KASHA_BSPIDER_TALON));
					st.takeItems(LEATHER_POUCH2, st.getQuestItemsCount(LEATHER_POUCH2));
					st.giveItems(LEATHER_POUCH2FULL, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "5");
				}
				else
				{
					st.giveItems(KASHA_BSPIDER_TALON, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if (cond == 15)
			{
				if ((st.getQuestItemsCount(8545) < 6) && (st.getRandom(100) <= 50))
				{
					if (st.getQuestItemsCount(8545) == 5)
					{
						st.playSound("ItemSound.quest_middle");
						st.set("cond", "16");
					}
					else
					{
						st.playSound("ItemSound.quest_itemget");
						st.giveItems(8545, 1);
					}
				}
			}
		}
		else if (npcId == ORC_FIGHTER)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(LEATHER_PURSE4) == 1) && (st.getQuestItemsCount(VUKU_TUSK) < 3))
			{
				if ((st.getQuestItemsCount(RATMAN_FANG) + st.getQuestItemsCount(LANGK_TOOTH) + st.getQuestItemsCount(FELIM_TOOTH) + st.getQuestItemsCount(VUKU_TUSK)) >= 11)
				{
					st.takeItems(VUKU_TUSK, st.getQuestItemsCount(VUKU_TUSK));
					st.takeItems(RATMAN_FANG, st.getQuestItemsCount(RATMAN_FANG));
					st.takeItems(LANGK_TOOTH, st.getQuestItemsCount(LANGK_TOOTH));
					st.takeItems(FELIM_TOOTH, st.getQuestItemsCount(FELIM_TOOTH));
					st.takeItems(LEATHER_PURSE4, 1);
					st.giveItems(LEATHER_POUCH4FULL, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "12");
				}
				else
				{
					st.giveItems(VUKU_TUSK, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == RATMAN_WARRIOR)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(LEATHER_PURSE4) == 1) && (st.getQuestItemsCount(RATMAN_FANG) < 3))
			{
				if ((st.getQuestItemsCount(RATMAN_FANG) + st.getQuestItemsCount(LANGK_TOOTH) + st.getQuestItemsCount(FELIM_TOOTH) + st.getQuestItemsCount(VUKU_TUSK)) >= 11)
				{
					st.takeItems(VUKU_TUSK, st.getQuestItemsCount(VUKU_TUSK));
					st.takeItems(RATMAN_FANG, st.getQuestItemsCount(RATMAN_FANG));
					st.takeItems(LANGK_TOOTH, st.getQuestItemsCount(LANGK_TOOTH));
					st.takeItems(FELIM_TOOTH, st.getQuestItemsCount(FELIM_TOOTH));
					st.takeItems(LEATHER_PURSE4, 1);
					st.giveItems(LEATHER_POUCH4FULL, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "12");
				}
				else
				{
					st.giveItems(RATMAN_FANG, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == L_LIZZARDMAN_WARRIOR)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(LEATHER_PURSE4) == 1) && (st.getQuestItemsCount(LANGK_TOOTH) < 3))
			{
				if ((st.getQuestItemsCount(RATMAN_FANG) + st.getQuestItemsCount(LANGK_TOOTH) + st.getQuestItemsCount(FELIM_TOOTH) + st.getQuestItemsCount(VUKU_TUSK)) >= 11)
				{
					st.takeItems(VUKU_TUSK, st.getQuestItemsCount(VUKU_TUSK));
					st.takeItems(RATMAN_FANG, st.getQuestItemsCount(RATMAN_FANG));
					st.takeItems(LANGK_TOOTH, st.getQuestItemsCount(LANGK_TOOTH));
					st.takeItems(FELIM_TOOTH, st.getQuestItemsCount(FELIM_TOOTH));
					st.takeItems(LEATHER_PURSE4, 1);
					st.giveItems(LEATHER_POUCH4FULL, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "12");
				}
				else
				{
					st.giveItems(LANGK_TOOTH, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (npcId == F_LIZZARDMAN_WARRIOR)
		{
			st.set("id", "0");
			if ((cond > 0) && (st.getQuestItemsCount(LEATHER_PURSE4) == 1) && (st.getQuestItemsCount(FELIM_TOOTH) < 3))
			{
				if ((st.getQuestItemsCount(RATMAN_FANG) + st.getQuestItemsCount(LANGK_TOOTH) + st.getQuestItemsCount(FELIM_TOOTH) + st.getQuestItemsCount(VUKU_TUSK)) >= 11)
				{
					st.takeItems(VUKU_TUSK, st.getQuestItemsCount(VUKU_TUSK));
					st.takeItems(RATMAN_FANG, st.getQuestItemsCount(RATMAN_FANG));
					st.takeItems(LANGK_TOOTH, st.getQuestItemsCount(LANGK_TOOTH));
					st.takeItems(FELIM_TOOTH, st.getQuestItemsCount(FELIM_TOOTH));
					st.takeItems(LEATHER_PURSE4, 1);
					st.giveItems(LEATHER_POUCH4FULL, 1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "12");
				}
				else
				{
					st.giveItems(FELIM_TOOTH, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if ((npcId == VANUL) && (cond == 17))
		{
			st.giveItems(8546, 1);
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "18");
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _415_PathToOrcMonk(415, qn, "");
	}
}