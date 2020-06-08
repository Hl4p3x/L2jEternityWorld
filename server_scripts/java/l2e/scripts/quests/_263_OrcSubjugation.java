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
 * Created by LordWinter 06.07.2012
 * Based on L2J Eternity-World
 */
public class _263_OrcSubjugation extends Quest
{
	private final static String qn = "_263_OrcSubjugation";
	
	// Items
	private static final int ORC_AMULET = 1116;
	private static final int ORC_NECKLACE = 1117;
	
	public _263_OrcSubjugation(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(30346);
		addTalkId(30346);
		
		addKillId(20385, 20386, 20387, 20388);

		questItemIds = new int[] { ORC_AMULET, ORC_NECKLACE };
	}	
			
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30346-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30346-06.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}	
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if(st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getRace().ordinal() == 2)
				{
					if (player.getLevel() >= 8 && player.getLevel() <= 16)
						htmltext = "30346-02.htm";
					else
					{
						htmltext = "30346-01.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "30346-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				int amulet = (int) st.getQuestItemsCount(ORC_AMULET);
				int necklace = (int) st.getQuestItemsCount(ORC_NECKLACE);
				
				if (amulet == 0 && necklace == 0)
					htmltext = "30346-04.htm";
				else
				{
					htmltext = "30346-05.htm";
					st.rewardItems(57, amulet * 20 + necklace * 30);
					st.takeItems(ORC_AMULET, -1);
					st.takeItems(ORC_NECKLACE, -1);
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
		
		if (st.isStarted() && st.getRandom(10) > 4)
		{
			int item = ORC_NECKLACE;
			
			if (npc.getId() == 20385)
				item = ORC_AMULET;
			
			st.giveItems(item, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}

	public static void main(String[] args)
	{
		new _263_OrcSubjugation(263, qn, "");		
	}
}