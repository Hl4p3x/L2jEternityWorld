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
 * Created by LordWinter 22.12.2010
 * Based on L2J Eternity-World
 */
public class _146_TheZeroHour extends Quest
{
	private static final String qn = "_146_TheZeroHour";

	private static final int KAHMAN = 31554;
	private static final int FANG = 14859;	
	private static final int Reward =  14849;
	private static final int QUEEN = 25671;

	private static final int[] ITEMS = {FANG};

	public _146_TheZeroHour(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(KAHMAN);
		addTalkId(KAHMAN);
		addKillId(QUEEN);
		questItemIds = ITEMS;
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
        	QuestState st = player.getQuestState(qn);
        	if (st == null)
			return htmltext;

		if(event.equalsIgnoreCase("31554-02.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("reward"))
		{
			if(st.getQuestItemsCount(FANG) >= 1)
			{
				htmltext = "31554-06.htm";
				st.takeItems(FANG, 1);
				st.giveItems(Reward, 1);
				st.exitQuest(true);
				st.playSound("ItemSound.quest_finish");
			}
			else
				htmltext = "31554-05.htm";	
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		final int cond = st.getInt("cond");
		int npcId = npc.getId();

		QuestState _prev = player.getQuestState("_109_InSearchOfTheNest");
		if(npcId == KAHMAN)
		{
			if(cond == 0)
			{
				if(player.getLevel() >= 81)
				{
					if (_prev != null && _prev.getState() == State.COMPLETED)
						htmltext = "31554-01.htm";
					else
						htmltext = "31554-00.htm";
				}
				else
					htmltext = "31554-03.htm";
			}
			else if(cond == 1)
				htmltext = "31554-04.htm";
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;
		int npcId = npc.getId();
		final int cond = st.getInt("cond");
		
		if(npcId == QUEEN && cond >= 1)
		{
			st.giveItems(FANG, 1);	
			st.playSound("ItemSound.quest_itemget");
		}
		return null;		
	}

	public static void main(String[] args)
	{
		new _146_TheZeroHour(146, qn, "");
	}
}