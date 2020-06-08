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
 * Created by LordWinter 24.05.2011
 * Based on L2J Eternity-World
 */
public class _035_FindGlitteringJewelry extends Quest
{
	private static final String qn = "_035_FindGlitteringJewelry";
	
	// ITEMS
	private static final int JEWEL_BOX 	= 7077;
	private static final int ORIHARUKON 	= 1893;
	private static final int ROUGH_JEWEL 	= 7162;
	private static final int SILVER_NUGGET 	= 1873;
	private static final int THONS 		= 4044;

	public _035_FindGlitteringJewelry(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(30091);
		addTalkId(30091);
		addTalkId(30879);
		addKillId(20135);
		questItemIds = new int[] { ROUGH_JEWEL };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		int cond = st.getInt("cond");
		if (event.equalsIgnoreCase("30091-1.htm") && cond == 0)
		{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		if (event.equalsIgnoreCase("30879-1.htm") && cond == 1)
		{
			st.set("cond","2");
			st.playSound("ItemSound.quest_accept");
		}
		if (event.equalsIgnoreCase("30091-3.htm") && cond == 3)
		{
			st.takeItems(ROUGH_JEWEL,10);
			st.set("cond","4");
			st.playSound("ItemSound.quest_accept");
		}
		if (event.equalsIgnoreCase("30091-5.htm") && cond == 4)
			if (st.getQuestItemsCount(ORIHARUKON) >= 5 && st.getQuestItemsCount(SILVER_NUGGET) >= 500 && st.getQuestItemsCount(THONS) >= 150)
			{
				st.takeItems(ORIHARUKON,5);
				st.takeItems(SILVER_NUGGET,500);
				st.takeItems(THONS,150);
				st.giveItems(JEWEL_BOX,1);
				st.playSound("ItemSound.quest_finish");
				st.unset("cond");
				st.exitQuest(false);
			}
			else
				return "no_items.htm";
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		int cond = st.getInt("cond");

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		else if (npc.getId() == 30091 && cond == 0 && st.getQuestItemsCount(JEWEL_BOX) == 0)
		{
			QuestState fwear = player.getQuestState("_037_PleaseMakeMeFormalWear");
			if (fwear != null)
				if (fwear.get("cond") == "6")
					htmltext = "30091-0.htm";
				else
				{
					htmltext = "30091-6.htm";
					st.exitQuest(true);
				}
			else
			{
				htmltext = "30091-6.htm";
				st.exitQuest(true);
			}	
			st.exitQuest(true);
		}
		else if (npc.getId() == 30879 && cond == 1)
			htmltext = "30879-0.htm";
		else if (npc.getId() == 30879 && cond == 2)
			htmltext = "30879-1a.htm";
		else if (npc.getId() == 30879 && cond == 3)
			htmltext = "30879-1a.htm";
		else if (st.getState() == State.STARTED)
			if (npc.getId() == 30091 && st.getQuestItemsCount(ROUGH_JEWEL) == 10)
				htmltext = "30091-2.htm";
			else
				htmltext = "30091-1a.htm";
			else if (npc.getId() == 30091 && cond == 4 && st.getQuestItemsCount(ORIHARUKON) >= 5 && st.getQuestItemsCount(SILVER_NUGGET) >= 500 && st.getQuestItemsCount(THONS) >= 150)
				htmltext = "30091-4.htm";
			else
				htmltext = "30091-3a.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, 1);
		L2PcInstance partyMember1 = getRandomPartyMember(player, 2);
		if (partyMember == null && partyMember1 == null)
			return null;
		else if (partyMember == null)
			partyMember = partyMember1;
		else if (partyMember1 != null && Rnd.getChance(50))
			partyMember = partyMember1;

		if (partyMember == null)
			return null;
		QuestState st = partyMember.getQuestState(qn);
		if (st == null || st.getState() != State.STARTED)
			return null;
		long count = st.getQuestItemsCount(ROUGH_JEWEL);
		if (count < 10)
		{
			st.giveItems(ROUGH_JEWEL,1);
			if (count == 9)
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond","3");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _035_FindGlitteringJewelry(35, qn, "");
	}
}