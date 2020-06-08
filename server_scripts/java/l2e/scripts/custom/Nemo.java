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
package l2e.scripts.custom;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

/**
 * Created by LordWinter 22.06.2012
 * Based on L2J Eternity-World
 */
public class Nemo extends Quest
{
	private static final String qn = "Nemo";

	// NPC
	private static final int _nemo = 32735;

	public Nemo(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_nemo);
		addFirstTalkId(_nemo);
		addTalkId(_nemo);
	}
		
	@Override
	@Deprecated
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);

		if (event.equalsIgnoreCase("request_collector"))
		{
			if(st.getQuestItemsCount(15487) > 0)
				htmltext = "32735-2.htm";
			else
			{
				player.addItem("Maguen", 15487, 1, null, true);
				return null;
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);

		if (npc.getId() == _nemo)
			return "32735.htm";
		return "";
	}
	
	public static void main(String[] args)
	{
		new Nemo(-1, qn, "custom");
	}
}