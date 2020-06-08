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

/**
 * Created by LordWinter 22.07.2012
 * Based on L2J Eternity-World
 */
public class _137_TempleChampionPart1 extends Quest
{
	private static final String qn = "_137_TempleChampionPart1";
	
	// NPCs
	private static final int SYLVAIN = 30070;
	
	private static final int MOBS[] =
	{
		20083, // Granite Golem
		20144, // Hangman Tree
		20199, // Amber Basilisk
		20200, // Strain
		20201, // Ghoul
		20202, // Dead Seeker
	};
	
	// Items
	private static final int FRAGMENT = 10340;
	private static final int EXECUTOR = 10334;
	private static final int MISSIONARY = 10339;

	public _137_TempleChampionPart1(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(SYLVAIN);
		addTalkId(SYLVAIN);

		addKillId(MOBS);

		questItemIds = new int[] { FRAGMENT };
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return getNoQuestMsg(player);

		switch (event)
		{
			case "30070-02.htm":
				st.startQuest();
				break;
			case "30070-05.htm":
				st.set("talk", "1");
				break;
			case "30070-06.htm":
				st.set("talk", "2");
				break;
			case "30070-08.htm":
				st.unset("talk");
				st.setCond(2, true);
				break;
			case "30070-16.htm":
				if (st.isCond(2) && (st.hasQuestItems(EXECUTOR) && st.hasQuestItems(MISSIONARY)))
				{
					st.takeItems(EXECUTOR, -1);
					st.takeItems(MISSIONARY, -1);
					st.giveAdena(69146, true);
					if (player.getLevel() < 41)
					{
						st.addExpAndSp(219975, 13047);
					}
					st.exitQuest(false, true);
				}
				break;
		}
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;

		if (st.isCompleted())
			return getAlreadyCompletedMsg(player);

		switch (st.getInt("cond"))
		{
			case 1:
				switch (st.getInt("talk"))
				{
					case 1:
						htmltext = "30070-05.htm";
						break;
					case 2:
						htmltext = "30070-06.htm";
						break;
					default:
						htmltext = "30070-03.htm";
						break;
				}
				break;
			case 2:
				htmltext = "30070-08.htm";
				break;
			case 3:
				if (st.getInt("talk") == 1)
					htmltext = "30070-10.htm";
				else if (st.getQuestItemsCount(FRAGMENT) >= 30)
				{
					st.set("talk", "1");
					htmltext = "30070-09.htm";
					st.takeItems(FRAGMENT, -1);
				}
				break;
			default:
				htmltext = ((player.getLevel() >= 35) && st.hasQuestItems(EXECUTOR) && st.hasQuestItems(MISSIONARY)) ? "30070-01.htm" : "30070-00.htm";
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(qn);
		if ((st != null) && st.isStarted() && st.isCond(2) && (st.getQuestItemsCount(FRAGMENT) < 30))
		{
			st.giveItems(FRAGMENT, 1);
			if (st.getQuestItemsCount(FRAGMENT) >= 30)
				st.setCond(3, true);
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _137_TempleChampionPart1(137, qn, "");
	}
}
