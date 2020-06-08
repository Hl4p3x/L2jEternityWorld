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
 * Created by LordWinter 06.08.2011
 * Based on L2J Eternity-World
 */
public class _044_HelpTheSon extends Quest
{
	private static final String qn = "_044_HelpTheSon";

	//NPC's
	private static final int LUNDY 		  = 30827;
	private static final int DRIKUS 	  = 30505;

	// ITEMS
	private static final int WORK_HAMMER 	  = 168;
	private static final int GEMSTONE_FRAGMENT = 7552;
	private static final int GEMSTONE 	  = 7553;
	private static final int PET_TICKET 	  = 7585;

	// MONSTERS
	private static final int MAILLE_GUARD 	  = 20921;
	private static final int MAILLE_SCOUT 	  = 20920;
	private static final int MAILLE_LIZARDMAN = 20919;

	public _044_HelpTheSon(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(LUNDY);
		addTalkId(LUNDY);
		addTalkId(DRIKUS);

		addKillId(MAILLE_GUARD);
		addKillId(MAILLE_SCOUT);
		addKillId(MAILLE_LIZARDMAN);

		questItemIds = new int[] { GEMSTONE_FRAGMENT };
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("1"))
		{
			htmltext = "30827-01.htm";
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("3") && st.getQuestItemsCount(WORK_HAMMER) > 0)
		{
			htmltext = "30827-03.htm";
			st.takeItems(WORK_HAMMER, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("4") && st.getQuestItemsCount(GEMSTONE_FRAGMENT) >= 30)
		{
			htmltext = "30827-05.htm";
			st.takeItems(GEMSTONE_FRAGMENT, 30);
			st.giveItems(GEMSTONE, 1);
			st.set("cond", "4");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("5") && st.getQuestItemsCount(GEMSTONE) > 0)
		{
			htmltext = "30505-06.htm";
			st.takeItems(GEMSTONE, 1);
			st.set("cond", "5");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("7"))
		{
			htmltext = "30827-07.htm";
			st.giveItems(PET_TICKET, 1);
			st.unset("cond");
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
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
		byte id = st.getState();

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		else if(id == State.CREATED)
		{
			if(player.getLevel() >= 24)
				htmltext = "30827-00.htm";
			else
			{
				st.exitQuest(true);
				htmltext = "30827-00a.htm";
			}
		}
		else if(id == State.STARTED)
		{
			int cond = st.getInt("cond");
			if(npcId == LUNDY)
			{
				if(cond == 1)
				{
					if(st.getQuestItemsCount(WORK_HAMMER) == 0)
						htmltext = "30827-01a.htm";
					else
						htmltext = "30827-02.htm";
				}
				else if(cond == 2)
					htmltext = "30827-03a.htm";
				else if(cond == 3)
					htmltext = "30827-04.htm";
				else if(cond == 4)
					htmltext = "30827-05a.htm";
				else if(cond == 5)
					htmltext = "30827-06.htm";
			}
			else if(npcId == DRIKUS)
				if(cond == 4 && st.getQuestItemsCount(GEMSTONE) > 0)
					htmltext = "30505-05.htm";
				else if(cond == 5)
					htmltext = "30505-06a.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
        	if (st == null)
			return null;

		int cond = st.getInt("cond");

		if(cond == 2 && st.getQuestItemsCount(GEMSTONE_FRAGMENT) < 30)
		{
			st.giveItems(GEMSTONE_FRAGMENT, 1);
			if(st.getQuestItemsCount(GEMSTONE_FRAGMENT) >= 30)
			{
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "3");
				st.playSound("ItemSound.quest_itemget");
			}
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _044_HelpTheSon(44, qn, "");
	}
}