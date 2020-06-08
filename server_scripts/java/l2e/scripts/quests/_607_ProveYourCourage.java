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
 * Created by LordWinter 03.08.2011 Based on L2J Eternity-World
 */
public class _607_ProveYourCourage extends Quest
{
	private static final String qn = "_607_ProveYourCourage";
	
	// NPC
	private final static int KADUN_ZU_KETRA = 31370;
	private final static int VARKAS_HERO_SHADITH = 25309;
	
	// Quest items
	private final static int HEAD_OF_SHADITH = 7235;
	private final static int TOTEM_OF_VALOR = 7219;
	
	// Etc
	private final static int MARK_OF_KETRA_ALLIANCE3 = 7213;
	private final static int MARK_OF_KETRA_ALLIANCE4 = 7214;
	private final static int MARK_OF_KETRA_ALLIANCE5 = 7215;
	
	public _607_ProveYourCourage(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(KADUN_ZU_KETRA);
		addTalkId(KADUN_ZU_KETRA);
		
		addKillId(VARKAS_HERO_SHADITH);
		
		questItemIds = new int[]
		{
			HEAD_OF_SHADITH
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("31370-2.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31370-4.htm"))
		{
			if (st.getQuestItemsCount(HEAD_OF_SHADITH) >= 1)
			{
				st.takeItems(HEAD_OF_SHADITH, -1);
				st.giveItems(TOTEM_OF_VALOR, 1);
				st.addExpAndSp(0, 10000);
				st.unset("cond");
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
			else
			{
				htmltext = "31370-2r.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		int cond = st.getInt("cond");
		
		if (cond == 0)
		{
			if (player.getLevel() >= 75)
			{
				if ((st.getQuestItemsCount(MARK_OF_KETRA_ALLIANCE3) == 1) || (st.getQuestItemsCount(MARK_OF_KETRA_ALLIANCE4) == 1) || (st.getQuestItemsCount(MARK_OF_KETRA_ALLIANCE5) == 1))
				{
					htmltext = "31370-1.htm";
				}
				else
				{
					htmltext = "31370-00.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				htmltext = "31370-0.htm";
				st.exitQuest(true);
			}
		}
		else if ((cond == 1) && (st.getQuestItemsCount(HEAD_OF_SHADITH) == 0))
		{
			htmltext = "31370-2r.htm";
		}
		else if ((cond == 2) && (st.getQuestItemsCount(HEAD_OF_SHADITH) >= 1))
		{
			htmltext = "31370-3.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		if (npc.getId() == VARKAS_HERO_SHADITH)
		{
			if (player.getParty() != null)
			{
				for (L2PcInstance plr : player.getParty().getMembers())
				{
					QuestState qs = plr.getQuestState(qn);
					if (qs.getInt("cond") == 1)
					{
						qs.giveItems(HEAD_OF_SHADITH, 1);
						qs.set("cond", "2");
						qs.playSound("ItemSound.quest_itemget");
					}
				}
			}
			else
			{
				if (st.getInt("cond") == 1)
				{
					st.giveItems(HEAD_OF_SHADITH, 1);
					st.set("cond", "2");
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _607_ProveYourCourage(607, qn, "");
	}
}