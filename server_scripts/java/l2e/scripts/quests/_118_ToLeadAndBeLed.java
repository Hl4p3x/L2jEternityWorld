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
import l2e.util.Rnd;

/**
 * Created by LordWinter 13.10.2012
 * Based on L2J Eternity-World
 */
public class _118_ToLeadAndBeLed extends Quest
{
	private static final String qn = "_118_ToLeadAndBeLed";

	private static int PINTER = 30298;
	private static int MAILLE_LIZARDMAN = 20919;
	private static int BLOOD_OF_MAILLE_LIZARDMAN = 8062;
	private static int KING_OF_THE_ARANEID = 20927;
	private static int KING_OF_THE_ARANEID_LEG = 8063;
	private static int D_CRY = 1458;
	private static int D_CRY_COUNT_HEAVY = 721;
	private static int D_CRY_COUNT_LIGHT_MAGIC = 604;

	private static int CLAN_OATH_HELM = 7850;

	private static int CLAN_OATH_ARMOR = 7851;
	private static int CLAN_OATH_GAUNTLETS = 7852;
	private static int CLAN_OATH_SABATON = 7853;

	private static int CLAN_OATH_BRIGANDINE = 7854;
	private static int CLAN_OATH_LEATHER_GLOVES = 7855;
	private static int CLAN_OATH_BOOTS = 7856;

	private static int CLAN_OATH_AKETON = 7857;
	private static int CLAN_OATH_PADDED_GLOVES = 7858;
	private static int CLAN_OATH_SANDALS = 7859;

	public _118_ToLeadAndBeLed(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(PINTER);
		addTalkId(PINTER);

		addKillId(MAILLE_LIZARDMAN);
		addKillId(KING_OF_THE_ARANEID);

		questItemIds = new int[] { BLOOD_OF_MAILLE_LIZARDMAN, KING_OF_THE_ARANEID_LEG };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("30298-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30298-05a.htm"))
		{
			st.set("choose", "1");
			st.set("cond", "3");
		}
		else if(event.equalsIgnoreCase("30298-05b.htm"))
		{
			st.set("choose", "2");
			st.set("cond", "4");
		}
		else if(event.equalsIgnoreCase("30298-05c.htm"))
		{
			st.set("choose", "3");
			st.set("cond", "5");
		}
		else if(event.equalsIgnoreCase("30298-08.htm"))
		{
			int choose = st.getInt("choose");
			int need_dcry = choose == 1 ? D_CRY_COUNT_HEAVY : D_CRY_COUNT_LIGHT_MAGIC;
			if(st.getQuestItemsCount(D_CRY) < need_dcry)
				htmltext = "30298-07.htm";
			st.set("cond", "7");
			st.takeItems(D_CRY, need_dcry);
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		int cond = st.getCond();

    		switch (st.getState())
    		{
    			case State.CREATED:
				if(player.getLevel() < 19)
				{
					st.exitQuest(true);
					htmltext = "30298-00.htm";
				}
				else if(player.getId() == 0)
				{
					st.exitQuest(true);
					htmltext = "30298-00a.htm";
				}
				else if(player.getSponsor() == 0)
				{
					st.exitQuest(true);
					htmltext = "30298-00b.htm";
				}
				else
					htmltext = "30298-01.htm";
       				break;
   	 		case State.STARTED:
				if(cond == 1)
					htmltext = "30298-02a.htm";
				else if(cond == 2)
				{
					if(st.getQuestItemsCount(BLOOD_OF_MAILLE_LIZARDMAN) < 10)
					{
						htmltext = "30298-02a.htm";
					}
					st.takeItems(BLOOD_OF_MAILLE_LIZARDMAN, -1);
					htmltext = "30298-04.htm";
				}
				else if(cond == 3)
					htmltext = "30298-05a.htm";
				else if(cond == 4)
					htmltext = "30298-05b.htm";
				else if(cond == 5)
					htmltext = "30298-05c.htm";
				else if(cond == 7)
					htmltext = "30298-08a.htm";
				else if(cond == 8)
				{
					if(st.getQuestItemsCount(KING_OF_THE_ARANEID_LEG) < 8)
					{
						st.set("cond","7");
						htmltext = "30298-08a.htm";
					}
					st.takeItems(KING_OF_THE_ARANEID_LEG, -1);
					st.giveItems(CLAN_OATH_HELM, 1);
					int choose = st.getInt("choose");
					if(choose == 1)
					{
						st.giveItems(CLAN_OATH_ARMOR, 1);
						st.giveItems(CLAN_OATH_GAUNTLETS, 1);
						st.giveItems(CLAN_OATH_SABATON, 1);
					}
					else if(choose == 2)
					{
						st.giveItems(CLAN_OATH_BRIGANDINE, 1);
						st.giveItems(CLAN_OATH_LEATHER_GLOVES, 1);
						st.giveItems(CLAN_OATH_BOOTS, 1);
					}
					else
					{
						st.giveItems(CLAN_OATH_AKETON, 1);
						st.giveItems(CLAN_OATH_PADDED_GLOVES, 1);
						st.giveItems(CLAN_OATH_SANDALS, 1);
					}
					st.unset("cond");
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
					htmltext = "30298-09.htm";
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

		int npcId = npc.getId();
		int cond = st.getCond();

		if(npcId == MAILLE_LIZARDMAN && st.getQuestItemsCount(BLOOD_OF_MAILLE_LIZARDMAN) < 10 && cond == 1 && Rnd.chance(50))
		{
			st.giveItems(BLOOD_OF_MAILLE_LIZARDMAN, 1);
			if(st.getQuestItemsCount(BLOOD_OF_MAILLE_LIZARDMAN) == 10)
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond","2");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		else if(npcId == KING_OF_THE_ARANEID && st.getQuestItemsCount(KING_OF_THE_ARANEID_LEG) < 8 && cond == 7 && Rnd.chance(50))
		{
			st.giveItems(KING_OF_THE_ARANEID_LEG, 1);
			if(st.getQuestItemsCount(KING_OF_THE_ARANEID_LEG) == 8)
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond","8");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

  	public static void main(String[] args)
  	{
    		new _118_ToLeadAndBeLed(118, qn, "");
  	}
}