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

public class Kamaloka30_40 extends KamalokaSolo
{
	private static final int KANABION = 22458;
	private static final int[] APPEAR =
	{
		22459,
		22460
	};
	
	private static final int[] REW1 =
	{
		13002,
		3,
		13002,
		3,
		13002,
		3,
		13002,
		3,
		13002,
		3
	};
	private static final int[] REW2 =
	{
		10841,
		1,
		10842,
		1,
		10843,
		1,
		10844,
		1,
		12826,
		1
	};
	private static final int[] REW3 =
	{
		85,
		65,
		170,
		956,
		90,
		65,
		20,
		170,
		956,
		65,
		25,
		20,
		180,
		956
	};
	
	private final KamaParam param = new KamaParam();
	
	public Kamaloka30_40(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		param.qn = "Kamaloka30_40";
		param.minLev = 30;
		param.maxLev = 40;
		param.rewPosition = newCoord(23478, -220079, -7799);
		param.enterCoord = newCoord(22742, -220079, -7802);
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
		new Kamaloka30_40(-1, "Kamaloka30_40", "Kamaloka30_40");
	}
}