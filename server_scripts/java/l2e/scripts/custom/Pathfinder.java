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

public class Pathfinder extends Quest
{
	private static final String qn = "Pathfinder";
	
	final private static int Pathfinder = 32484;
	final private static int Rewarder = 32485;
	
	public Pathfinder(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addStartNpc(Pathfinder);
		addTalkId(Pathfinder);
		addFirstTalkId(Rewarder);
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getId();
		String htmltext = "";
		
		if (npcId == Pathfinder)
		{
			if (npc.isInsideRadius(-13948, 123819, -3112, 500, true, false))
			{
				htmltext = "gludio-list.htm";
			}
			else if (npc.isInsideRadius(18228, 146030, -3088, 500, true, false))
			{
				htmltext = "dion-list.htm";
			}
			else if (npc.isInsideRadius(108384, 221614, -3592, 500, true, false))
			{
				htmltext = "heine-list.htm";
			}
			else if (npc.isInsideRadius(80960, 56455, -1552, 500, true, false))
			{
				htmltext = "oren-list.htm";
			}
			else if (npc.isInsideRadius(85894, -142108, -1336, 500, true, false))
			{
				htmltext = "schuttgart-list.htm";
			}
			else if (npc.isInsideRadius(42674, -47909, -797, 500, true, false))
			{
				htmltext = "rune-list.htm";
			}
			else
			{
				return null;
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getId();
		String htmltext = "";
		
		if (npcId == Rewarder)
		{
			if (npc.isInsideRadius(9261, -219862, -8021, 1000, true, false))
			{
				htmltext = "20-30.htm";
			}
			else if (npc.isInsideRadius(16301, -219806, -8021, 1000, true, false))
			{
				htmltext = "25-35.htm";
			}
			else if (npc.isInsideRadius(23478, -220079, -7799, 1000, true, false))
			{
				htmltext = "30-40.htm";
			}
			else if (npc.isInsideRadius(9290, -212993, -7799, 1000, true, false))
			{
				htmltext = "35-45.htm";
			}
			else if (npc.isInsideRadius(16598, -212997, -7802, 1000, true, false))
			{
				htmltext = "40-50.htm";
			}
			else if (npc.isInsideRadius(23650, -213051, -8007, 1000, true, false))
			{
				htmltext = "45-55.htm";
			}
			else if (npc.isInsideRadius(9136, -205733, -8007, 1000, true, false))
			{
				htmltext = "50-60.htm";
			}
			else if (npc.isInsideRadius(16508, -205737, -8007, 1000, true, false))
			{
				htmltext = "55-65.htm";
			}
			else if (npc.isInsideRadius(23229, -206316, -7991, 1000, true, false))
			{
				htmltext = "60-70.htm";
			}
			else if (npc.isInsideRadius(42638, -219781, -8759, 1000, true, false))
			{
				htmltext = "65-75.htm";
			}
			else if (npc.isInsideRadius(49014, -219737, -8759, 1000, true, false))
			{
				htmltext = "70-80.htm";
			}
			else
			{
				return null;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Pathfinder(-1, qn, "custom");
	}
}