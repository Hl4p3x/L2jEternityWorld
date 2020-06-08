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
 * Created by LordWinter 05.08.2011
 * Based on L2J Eternity-World
 */
public class _039_RedEyedInvaders extends Quest
{
	private static final String qn = "_039_RedEyedInvaders";

 	// NPC's
	public final int BABENCO = 30334;
	public final int BATHIS  = 30332;

	// Mobs
	public final int M_LIZARDMAN       = 20919;
	public final int M_LIZARDMAN_SCOUT = 20920;
	public final int M_LIZARDMAN_GUARD = 20921;
	public final int ARANEID           = 20925;

	// Quest Items
	public final int BBN 	= 7178;
	public final int RBN 	= 7179;
	public final int IP 	= 7180;
	public final int GML 	= 7181;
	public final int[] REW 	= { 6521, 6529, 6535 };

	public _039_RedEyedInvaders(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(BABENCO);
		addTalkId(BABENCO);
		addTalkId(BATHIS);

		addKillId(M_LIZARDMAN);
		addKillId(M_LIZARDMAN_SCOUT);
		addKillId(M_LIZARDMAN_GUARD);
		addKillId(ARANEID);

		questItemIds = new int[] { BBN, IP, RBN, GML };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("30334-1.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30332-1.htm"))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30332-3a.htm"))
		{
			if(st.getQuestItemsCount(BBN) == 100 && st.getQuestItemsCount(RBN) == 100)
			{
				st.set("cond", "4");
				st.takeItems(BBN, -1);
				st.takeItems(RBN, -1);
				st.playSound("ItemSound.quest_accept");
			}
			else
				htmltext = "no_items.htm";
		}
		else if(event.equalsIgnoreCase("30332-5.htm"))
		{
			if(st.getQuestItemsCount(IP) == 30 && st.getQuestItemsCount(GML) == 30)
			{
				st.takeItems(IP, -1);
				st.takeItems(GML, -1);
				st.giveItems(REW[0], 60);
				st.giveItems(REW[1], 1);
				st.giveItems(REW[2], 500);
				st.set("cond", "0");
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else
				htmltext = "no_items.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		int npcId = npc.getId();
		int cond = st.getInt("cond");

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		if(npcId == BABENCO)
		{
			if(cond == 0)
			{
				if(player.getLevel() < 20)
				{
					htmltext = "30334-2.htm";
					st.exitQuest(true);
				}
				else if(player.getLevel() >= 20)
					htmltext = "30334-0.htm";
			}
			else if(cond == 1)
				htmltext = "30334-3.htm";
		}
		else if(npcId == BATHIS)
		{
			if(cond == 1)
				htmltext = "30332-0.htm";
			else if(cond == 2 && (st.getQuestItemsCount(BBN) < 100 || st.getQuestItemsCount(RBN) < 100))
				htmltext = "30332-2.htm";
			else if(cond == 3 && st.getQuestItemsCount(BBN) == 100 && st.getQuestItemsCount(RBN) == 100)
				htmltext = "30332-3.htm";
			else if(cond == 4 && (st.getQuestItemsCount(IP) < 30 || st.getQuestItemsCount(GML) < 30))
				htmltext = "30332-3b.htm";
			else if(cond == 5 && st.getQuestItemsCount(IP) == 30 && st.getQuestItemsCount(GML) == 30)
				htmltext = "30332-4.htm";
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
		int cond = st.getInt("cond");

		if(cond == 2 && Rnd.calcChance(60))
		{
			if((npcId == 20919 || npcId == 20920) && st.getQuestItemsCount(BBN) <= 99)
				st.giveItems(BBN, 1);
			else if(npcId == 20921 && st.getQuestItemsCount(RBN) <= 99)
				st.giveItems(RBN, 1);
			st.playSound("ItemSound.quest_itemget");
			if(st.getQuestItemsCount(BBN) + st.getQuestItemsCount(RBN) == 200)
			{
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			}
		}

		if(cond == 4 && Rnd.calcChance(60))
		{
			if((npcId == 20920 || npcId == 20921) && st.getQuestItemsCount(IP) <= 29)
				st.giveItems(IP, 1);
			else if(npcId == 20925 && st.getQuestItemsCount(GML) <= 29)
				st.giveItems(GML, 1);
			st.playSound("ItemSound.quest_itemget");
			if(st.getQuestItemsCount(IP) + st.getQuestItemsCount(GML) == 60)
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _039_RedEyedInvaders(39, qn, "");
	}
}