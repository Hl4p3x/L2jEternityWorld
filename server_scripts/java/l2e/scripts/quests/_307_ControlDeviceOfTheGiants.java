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

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;

/**
 * Created by LordWinter 01.12.2010 Based on L2J Eternity-World
 */
public class _307_ControlDeviceOfTheGiants extends Quest
{
	private static final String qn = "_307_ControlDeviceOfTheGiants";
	
	// NPCs
	private static int DROPH = 32711;
	
	private static int HEKATON_PRIME = 25687;
	private static final int[] RAIDBOSS =
	{
		HEKATON_PRIME
	};
	
	// ITEMS
	private static int CET_1_SHEET = 14829;
	private static int CET_2_SHEET = 14830;
	private static int CET_3_SHEET = 14831;
	private static int SUPPLY_BOX = 14850;
	protected int Resp = 0;
	
	public _307_ControlDeviceOfTheGiants(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(DROPH);
		addTalkId(DROPH);
		
		for (int mobId : RAIDBOSS)
		{
			addKillId(mobId);
		}
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		String htmltext = event;
		if (event.equalsIgnoreCase("32711-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		if (event.equalsIgnoreCase("dospawn"))
		{
			st.addSpawn(HEKATON_PRIME, 191975, 56959, -7616, 1800000);
			Resp = 1;
			return "32711-04.htm";
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		int id = st.getState();
		final int cond = st.getInt("cond");
		int npcId = npc.getId();
		if (npcId == DROPH)
		{
			if ((id == State.CREATED) && (cond == 0))
			{
				if (player.getLevel() >= 79)
				{
					htmltext = "32711-01.htm";
				}
				else
				{
					st.exitQuest(true);
					htmltext = "32711-00.htm";
				}
			}
			else if (id == State.STARTED)
			{
				if (npcId == DROPH)
				{
					if (cond == 2)
					{
						st.takeItems(CET_1_SHEET, 1);
						st.takeItems(CET_2_SHEET, 1);
						st.takeItems(CET_3_SHEET, 1);
						st.giveItems(SUPPLY_BOX, 1);
						st.exitQuest(true);
						st.playSound("ItemSound.quest_finish");
						return "32711-10.htm";
					}
					else if ((st.getQuestItemsCount(CET_1_SHEET) == 0) || (st.getQuestItemsCount(CET_2_SHEET) == 0) || (st.getQuestItemsCount(CET_3_SHEET) == 0))
					{
						return "32711-09.htm";
					}
					else if ((cond == 1) && (st.getQuestItemsCount(CET_1_SHEET) >= 1) && (st.getQuestItemsCount(CET_2_SHEET) >= 1) && (st.getQuestItemsCount(CET_3_SHEET) >= 1) && (Resp == 0))
					{
						return "32711-11.htm";
					}
					else if (Resp == 1)
					{
						return "32711-06.htm";
					}
				}
			}
			
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		final int cond = st.getInt("cond");
		int npcId = npc.getId();
		if (cond == 1)
		{
			if (npcId == HEKATON_PRIME)
			{
				st.set("cond", "2");
				ThreadPoolManager.getInstance().scheduleGeneral(new setresp(), 1000 * 60 * 60 * 3);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	protected class setresp implements Runnable
	{
		@Override
		public void run()
		{
			Resp = 0;
		}
	}
	
	public static void main(String[] args)
	{
		new _307_ControlDeviceOfTheGiants(307, qn, "");
	}
}