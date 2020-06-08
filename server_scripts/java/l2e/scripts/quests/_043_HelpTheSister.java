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
public class _043_HelpTheSister extends Quest
{
	private static final String qn = "_043_HelpTheSister";

	// NPS's
	private static final int COOPER 	= 30829;
	private static final int GALLADUCCI 	= 30097;

	// ITEMS
	private static final int CRAFTED_DAGGER = 220;
	private static final int MAP_PIECE 	= 7550;
	private static final int MAP 		= 7551;
	private static final int PET_TICKET 	= 7584;

	// MONSTERS
	private static final int SPECTER 	= 20171;
	private static final int SORROW_MAIDEN 	= 20197;

	// OTHER
	private static final int MAX_COUNT = 30;

	public _043_HelpTheSister(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(COOPER);
		addTalkId(COOPER);
		addTalkId(GALLADUCCI);

		addKillId(SPECTER);
		addKillId(SORROW_MAIDEN);
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
			htmltext = "30829-01.htm";
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("3") && st.getQuestItemsCount(CRAFTED_DAGGER) > 0)
		{
			htmltext = "30829-03.htm";
			st.takeItems(CRAFTED_DAGGER, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("4") && st.getQuestItemsCount(MAP_PIECE) >= MAX_COUNT)
		{
			htmltext = "30829-05.htm";
			st.takeItems(MAP_PIECE, MAX_COUNT);
			st.giveItems(MAP, 1);
			st.set("cond", "4");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("5") && st.getQuestItemsCount(MAP) > 0)
		{
			htmltext = "30097-06.htm";
			st.takeItems(MAP, 1);
			st.set("cond", "5");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("7"))
		{
			htmltext = "30829-07.htm";
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
			if(player.getLevel() >= 26)
				htmltext = "30829-00.htm";
			else
			{
				st.exitQuest(true);
				htmltext = "30829-00a.htm";
			}
		}
		else if(id == State.STARTED)
		{
			int cond = st.getInt("cond");
			if(npcId == COOPER)
			{
				if(cond == 1)
				{
					if(st.getQuestItemsCount(CRAFTED_DAGGER) == 0)
						htmltext = "30829-01a.htm";
					else
						htmltext = "30829-02.htm";
				}
				else if(cond == 2)
					htmltext = "30829-03a.htm";
				else if(cond == 3)
					htmltext = "30829-04.htm";
				else if(cond == 4)
					htmltext = "30829-05a.htm";
				else if(cond == 5)
					htmltext = "30829-06.htm";
			}
			else if(npcId == GALLADUCCI)
				if(cond == 4 && st.getQuestItemsCount(MAP) > 0)
					htmltext = "30097-05.htm";
				else if(cond == 5)
					htmltext = "30097-06a.htm";
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
		if(cond == 2)
		{
			long pieces = st.getQuestItemsCount(MAP_PIECE);
			if(pieces < MAX_COUNT)
			{
				st.giveItems(MAP_PIECE, 1);
				if(pieces < MAX_COUNT - 1)
					st.playSound("ItemSound.quest_itemget");
				else
				{
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "3");
				}
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _043_HelpTheSister(43, qn, "");
	}
}