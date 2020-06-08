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

import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

/**
 * Create by LordWinter 29.10.2012 Based on L2J Eternity-World
 */
public class AbyssGaze extends Quest
{
	public AbyssGaze(int id, String name, String desc)
	{
		super(id, name, desc);

		addStartNpc(32540);
		addFirstTalkId(32540);
		addTalkId(32540);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;

		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);

		if (event.equalsIgnoreCase("request_permission"))
		{
			if(SoIManager.getCurrentStage() == 2 || SoIManager.getCurrentStage() == 5)
			{
				htmltext = "32540-2.htm";
			}
			else if(SoIManager.getCurrentStage() == 3 && SoIManager.isSeedOpen())
			{
				htmltext = "32540-3.htm";
			}
			else
			{
				htmltext = "32540-1.htm";
			}
		}
		else if(event.equalsIgnoreCase("enter_seed"))
		{
			if(SoIManager.getCurrentStage() == 3)
			{
				SoIManager.teleportInSeed(player);
				return null;
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);

		if (npc.getId() == 32540)
			return "32540.htm";
		return "";
	}

	public static void main(String[] args)
	{
		new AbyssGaze(-1, AbyssGaze.class.getSimpleName(), "custom");
	}
}