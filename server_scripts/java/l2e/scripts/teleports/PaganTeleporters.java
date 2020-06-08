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
package l2e.scripts.teleports;

import l2e.gameserver.data.xml.DoorParser;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class PaganTeleporters extends Quest
{
	private static final int[] NPCS =
	{
	                32034, 32035, 32036, 32037, 32039, 32040
	};

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("Close_Door1"))
		{
			DoorParser.getInstance().getDoor(19160001).closeMe();
		}
		else if (event.equalsIgnoreCase("Close_Door2"))
		{
			DoorParser.getInstance().getDoor(19160010).closeMe();
			DoorParser.getInstance().getDoor(19160011).closeMe();
		}

		return "";
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getId() == 32039)
		{
			player.teleToLocation(-12766, -35840, -10856);
		}
		else if (npc.getId() == 32040)
		{
			player.teleToLocation(36640, -51218, 718);
		}

		return "";
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());

		if (st == null)
		{
			return null;
		}

		switch (npc.getId())
		{
			case 32034:
				if (!st.hasQuestItems(8064) && !st.hasQuestItems(8065) && !st.hasQuestItems(8067))
				{
					htmltext = "noItem.htm";
				}
				else
				{
					htmltext = "FadedMark.htm";
					DoorParser.getInstance().getDoor(19160001).openMe();
					startQuestTimer("Close_Door1", 10000, null, null);
				}
				break;
			case 32035:
				DoorParser.getInstance().getDoor(19160001).openMe();
				startQuestTimer("Close_Door1", 10000, null, null);
				htmltext = "FadedMark.htm";
				break;
			case 32036:
				if (!st.hasQuestItems(8067))
				{
					htmltext = "noMark.htm";
				}
				else
				{
					htmltext = "openDoor.htm";
					startQuestTimer("Close_Door2", 10000, null, null);
					DoorParser.getInstance().getDoor(19160010).openMe();
					DoorParser.getInstance().getDoor(19160011).openMe();
				}
				break;
			case 32037:
				DoorParser.getInstance().getDoor(19160010).openMe();
				DoorParser.getInstance().getDoor(19160011).openMe();
				startQuestTimer("Close_Door2", 10000, null, null);
				htmltext = "FadedMark.htm";
				break;
		}

		st.exitQuest(true);

		return htmltext;
	}

	public PaganTeleporters(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int npcId : NPCS)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}

		addFirstTalkId(32039);
		addFirstTalkId(32040);
	}

	public static void main(String[] args)
	{
		new PaganTeleporters(-1, PaganTeleporters.class.getSimpleName(), "teleports");
	}
}
