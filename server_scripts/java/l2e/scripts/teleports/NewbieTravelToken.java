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

import java.util.Map;

import javolution.util.FastMap;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.util.Util;

public class NewbieTravelToken extends Quest
{
	private static final Map<Integer, Location> DATA = new FastMap<>();
	
	public NewbieTravelToken(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		DATA.put(30600, new Location(12160, 16554, -4583));
		DATA.put(30601, new Location(115594, -177993, -912));
		DATA.put(30599, new Location(45470, 48328, -3059));
		DATA.put(30602, new Location(-45067, -113563, -199));
		DATA.put(30598, new Location(-84053, 243343, -3729));
		DATA.put(32135, new Location(-119712, 44519, 368));
		
		for (int npcId : DATA.keySet())
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (Util.isDigit(event))
		{
			final int npcId = Integer.parseInt(event);
			if (DATA.keySet().contains(npcId))
			{
				player.teleToLocation(DATA.get(npcId), false);
				
				return super.onAdvEvent(event, npc, player);
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st != null)
		{
			if (player.getLevel() >= 20)
			{
				htmltext = "cant-travel.htm";
				st.exitQuest(true);
			}
			else
			{
				htmltext = npc.getId() + ".htm";
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new NewbieTravelToken(-1, "NewbieTravelToken", "teleports");
	}
}