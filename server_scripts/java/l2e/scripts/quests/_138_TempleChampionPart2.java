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
public class _138_TempleChampionPart2 extends Quest
{
	private static final String qn = "_138_TempleChampionPart2";
	
	// NPCs
	private static final int SYLVAIN = 30070;
	private static final int PUPINA = 30118;
	private static final int ANGUS = 30474;
	private static final int SLA = 30666;
	
	private static final int MOBS[] =
	{
		20176, // Wyrm
		20550, // Guardian Basilisk
		20551, // Road Scavenger
		20552, // Fettered Soul
	};
	
	// Items
	private static final int MANIFESTO = 10340;
	private static final int RELIC = 10340;
	private static final int ANGUS_REC = 10343;
	private static final int PUPINA_REC = 10344;

	public _138_TempleChampionPart2(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(SYLVAIN);
		addTalkId(SYLVAIN, PUPINA, ANGUS, SLA);

		addKillId(MOBS);

		questItemIds = new int[] { MANIFESTO, RELIC, ANGUS_REC, PUPINA_REC };
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
				st.giveItems(MANIFESTO, 1);
				break;
			case "30070-05.htm":
				st.giveAdena(84593, true);
				if ((player.getLevel() < 42))
				{
					st.addExpAndSp(187062, 11307);
				}
				st.exitQuest(false, true);
				break;
			case "30070-03.htm":
				st.setCond(2, true);
				break;
			case "30118-06.htm":
				st.setCond(3, true);
				break;
			case "30118-09.htm":
				st.setCond(6, true);
				st.giveItems(PUPINA_REC, 1);
				break;
			case "30474-02.htm":
				st.setCond(4, true);
				break;
			case "30666-02.htm":
				if (st.hasQuestItems(PUPINA_REC))
				{
					st.set("talk", "1");
					st.takeItems(PUPINA_REC, -1);
				}
				break;
			case "30666-03.htm":
				if (st.hasQuestItems(MANIFESTO))
				{
					st.set("talk", "2");
					st.takeItems(MANIFESTO, -1);
				}
				break;
			case "30666-08.htm":
				st.setCond(7, true);
				st.unset("talk");
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

		final int cond = st.getInt("cond");
		switch (npc.getId())
		{
			case SYLVAIN:
				switch (cond)
				{
					case 1:
						htmltext = "30070-02.htm";
						break;
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
						htmltext = "30070-03.htm";
						break;
					case 7:
						htmltext = "30070-04.htm";
						break;
					default:
						if (st.isCompleted())
						{
							return getAlreadyCompletedMsg(player);
						}
						htmltext = (player.getLevel() >= 36) ? "30070-01.htm" : "30070-00.htm";
						break;
				}
				break;
			case PUPINA:
				switch (cond)
				{
					case 2:
						htmltext = "30118-01.htm";
						break;
					case 3:
					case 4:
						htmltext = "30118-07.htm";
						break;
					case 5:
						htmltext = "30118-08.htm";
						if (st.hasQuestItems(ANGUS_REC))
						{
							st.takeItems(ANGUS_REC, -1);
						}
						break;
					case 6:
						htmltext = "30118-10.htm";
						break;
				}
				break;
			case ANGUS:
				switch (cond)
				{
					case 3:
						htmltext = "30474-01.htm";
						break;
					case 4:
						if (st.getQuestItemsCount(RELIC) >= 10)
						{
							st.takeItems(RELIC, -1);
							st.giveItems(ANGUS_REC, 1);
							st.setCond(5, true);
							htmltext = "30474-04.htm";
						}
						else
							htmltext = "30474-03.htm";
						break;
					case 5:
						htmltext = "30474-05.htm";
						break;
				}
				break;
			case SLA:
				switch (cond)
				{
					case 6:
						switch (st.getInt("talk"))
						{
							case 1:
								htmltext = "30666-02.htm";
								break;
							case 2:
								htmltext = "30666-03.htm";
								break;
							default:
								htmltext = "30666-01.htm";
								break;
						}
						break;
					case 7:
						htmltext = "30666-09.htm";
						break;
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = player.getQuestState(qn);
		if ((st != null) && st.isStarted() && st.isCond(4) && (st.getQuestItemsCount(RELIC) < 10))
		{
			st.giveItems(RELIC, 1);
			if (st.getQuestItemsCount(RELIC) >= 10)
				st.playSound("ItemSound.quest_middle");
			else
				st.playSound("ItemSound.quest_itemget");
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _138_TempleChampionPart2(138, qn, "");
	}
}