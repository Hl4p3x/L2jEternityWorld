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

public class Kamaloka60_70 extends KamalokaSolo
{
	private static final int KANABION = 22476;
	private static final int[] APPEAR =
	{
		22477,
		22478
	};
	
	private static final int[] REW1 =
	{
		13002,
		13,
		13002,
		13,
		13002,
		13,
		13002,
		13,
		13002,
		13
	};
	private static final int[] REW2 =
	{
		10856,
		1,
		10857,
		1,
		10858,
		1,
		10859,
		1,
		12832,
		1
	};
	private static final int[] REW3 =
	{
		85,
		65,
		14,
		730,
		90,
		65,
		20,
		14,
		730,
		65,
		25,
		20,
		16,
		730
	};
	
	private final KamaParam param = new KamaParam();
	
	public Kamaloka60_70(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		param.qn = "Kamaloka60_70";
		param.minLev = 60;
		param.maxLev = 70;
		param.rewPosition = newCoord(23229, -206316, -7991);
		param.enterCoord = newCoord(22343, -206237, -7991);
		
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
		new Kamaloka60_70(-1, "Kamaloka60_70", "Kamaloka60_70");
	}
}