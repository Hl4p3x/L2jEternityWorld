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

public class Kamaloka55_65 extends KamalokaSolo
{
	private static final int KANABION = 22473;
	private static final int[] APPEAR =
	{
		22474,
		22475
	};
	
	private static final int[] REW1 =
	{
		13002,
		12,
		13002,
		12,
		13002,
		12,
		13002,
		12,
		13002,
		12
	};
	private static final int[] REW2 =
	{
		10852,
		1,
		10853,
		1,
		10854,
		1,
		10855,
		1,
		12831,
		1
	};
	private static final int[] REW3 =
	{
		85,
		65,
		16,
		948,
		90,
		65,
		20,
		36,
		948,
		65,
		25,
		20,
		40,
		948
	};
	
	private final KamaParam param = new KamaParam();
	
	public Kamaloka55_65(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		param.qn = "Kamaloka55_65";
		param.minLev = 55;
		param.maxLev = 65;
		param.rewPosition = newCoord(16508, -205737, -8008);
		param.enterCoord = newCoord(15943, -205740, -8008);
		
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
		new Kamaloka55_65(-1, "Kamaloka55_65", "Kamaloka55_65");
	}
}