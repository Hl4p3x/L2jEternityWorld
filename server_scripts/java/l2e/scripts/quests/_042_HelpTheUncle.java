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
public class _042_HelpTheUncle extends Quest
{
	private static final String qn = "_042_HelpTheUncle";

	// NPC's
	private static final int WATERS = 30828;
	private static final int SOPHYA = 30735;

	// ITEMS
	private static final int TRIDENT = 291;
	private static final int MAP_PIECE = 7548;
	private static final int MAP = 7549;
	private static final int PET_TICKET = 7583;

	// MONSTERS
	private static final int MONSTER_EYE_DESTROYER = 20068;
	private static final int MONSTER_EYE_GAZER = 20266;

	// OTHER
	private static final int MAX_COUNT = 30;

	public _042_HelpTheUncle(int id, String name, String descr)
	{
		super(id, name, descr);

		addStartNpc(WATERS);

		addTalkId(WATERS);
		addTalkId(SOPHYA);

		addKillId(MONSTER_EYE_DESTROYER);
		addKillId(MONSTER_EYE_GAZER);
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
			htmltext = "30828-01.htm";
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("3") && st.getQuestItemsCount(TRIDENT) > 0)
		{
			htmltext = "30828-03.htm";
			st.takeItems(TRIDENT, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("4") && st.getQuestItemsCount(MAP_PIECE) >= MAX_COUNT)
		{
			htmltext = "30828-05.htm";
			st.takeItems(MAP_PIECE, MAX_COUNT);
			st.giveItems(MAP, 1);
			st.set("cond", "4");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("5") && st.getQuestItemsCount(MAP) > 0)
		{
			htmltext = "30735-06.htm";
			st.takeItems(MAP, 1);
			st.set("cond", "5");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("7"))
		{
			htmltext = "30828-07.htm";
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
		int cond = st.getInt("cond");

		if (st.isCompleted())
			htmltext = getAlreadyCompletedMsg(player);

		else if(id == State.CREATED)
		{
			if(player.getLevel() >= 25)
				htmltext = "30828-00.htm";
			else
			{
				htmltext = "30828-00a.htm";
				st.exitQuest(true);
			}
		}
		else if(id == State.STARTED)
		{
			if(npcId == WATERS)
			{
				if(cond == 1)
					if(st.getQuestItemsCount(TRIDENT) == 0)
						htmltext = "30828-01a.htm";
					else
						htmltext = "30828-02.htm";
				else if(cond == 2)
					htmltext = "30828-03a.htm";
				else if(cond == 3)
					htmltext = "30828-04.htm";
				else if(cond == 4)
					htmltext = "30828-05a.htm";
				else if(cond == 5)
					htmltext = "30828-06.htm";
			}
			else if(npcId == SOPHYA)
				if(cond == 4 && st.getQuestItemsCount(MAP) > 0)
					htmltext = "30735-05.htm";
				else if(cond == 5)
					htmltext = "30735-06a.htm";
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
			if(pieces < MAX_COUNT - 1)
			{
				st.giveItems(MAP_PIECE, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			else if(pieces == MAX_COUNT - 1)
			{
				st.giveItems(MAP_PIECE, 1);
				st.playSound("ItemSound.quest_middle");
				st.set("cond", "3");
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _042_HelpTheUncle(42, qn, "");
	}
}