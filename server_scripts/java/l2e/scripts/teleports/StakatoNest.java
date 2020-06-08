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

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class StakatoNest extends Quest
{
	private final static Location[] _locs =
	{
		new Location(80456, -52322, -5640),
		new Location(88718, -46214, -4640),
		new Location(87464, -54221, -5120),
		new Location(80848, -49426, -5128),
		new Location(87682, -43291, -4128)
	};
	
	private final static int npcId = 32640;
	
	public StakatoNest(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(npcId);
		addTalkId(npcId);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);
		
		int index = Integer.parseInt(event) - 1;
		
		if (_locs.length > index)
		{
			Location loc = _locs[index];
			
			if (player.getParty() != null)
			{
				for (L2PcInstance partyMember : player.getParty().getMembers())
				{
					if (partyMember.isInsideRadius(player, 1000, true, true))
						partyMember.teleToLocation(loc, true);
				}
			}
			player.teleToLocation(loc, false);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState accessQuest = player.getQuestState("_240_ImTheOnlyOneYouCanTrust");
		if ((accessQuest != null) && accessQuest.isCompleted())
			htmltext = "32640.htm";
		else
			htmltext = "32640-no.htm";
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new StakatoNest(-1, "StakatoNest", "teleports");
	}
}