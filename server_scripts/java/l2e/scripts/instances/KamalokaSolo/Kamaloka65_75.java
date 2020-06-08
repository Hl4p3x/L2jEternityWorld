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
package l2e.scripts.instances.KamalokaSolo;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class Kamaloka65_75 extends KamalokaSolo
{
	private static final int KANABION = 22479;
	private static final int[] APPEAR =
	{
		22480,
		22481
	};
	
	private static final int[] REW1 =
	{
		13002,
		15,
		13002,
		15,
		13002,
		15,
		13002,
		15,
		13002,
		15
	};
	private static final int[] REW2 =
	{
		10857,
		1,
		10858,
		1,
		10859,
		1,
		10860,
		1,
		12833,
		1
	};
	private static final int[] REW3 =
	{
		85,
		65,
		16,
		730,
		90,
		65,
		20,
		16,
		730,
		65,
		25,
		20,
		18,
		730
	};
	
	private final KamaParam param = new KamaParam();
	
	public Kamaloka65_75(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		param.qn = "Kamaloka65_75";
		param.minLev = 65;
		param.maxLev = 75;
		param.rewPosition = newCoord(42638, -219781, -8759);
		param.enterCoord = newCoord(41496, -219694, -8759);
		
		addStartNpc(ENTRANCE);
		addTalkId(ENTRANCE);
		addTalkId(REWARDER);
		addKillId(KANABION);
		for (int mob : APPEAR)
		{
			addKillId(mob);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		return onAdvEventTo(event, npc, player, param.qn, REW1, REW2);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getId() == ENTRANCE)
		{
			return onEnterTo(npc, player, param);
		}
		return onTalkTo(npc, player, param.qn);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		return onKillTo(npc, player, isPet, param.qn, KANABION, APPEAR, REW3);
	}
	
	public static void main(String[] args)
	{
		new Kamaloka65_75(-1, "Kamaloka65_75", "Kamaloka65_75");
	}
}