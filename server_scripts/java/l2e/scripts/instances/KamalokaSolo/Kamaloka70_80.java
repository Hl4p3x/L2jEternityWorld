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

public class Kamaloka70_80 extends KamalokaSolo
{
	private static final int KANABION = 22482;
	private static final int[] APPEAR =
	{
		22483,
		22484
	};
	
	private static final int[] REW1 =
	{
		13002,
		17,
		13002,
		17,
		13002,
		17,
		13002,
		17,
		13002,
		17
	};
	private static final int[] REW2 =
	{
		10861,
		1,
		12834,
		1,
		10862,
		1,
		10863,
		1,
		10864,
		1
	};
	private static final int[] REW3 =
	{
		85,
		65,
		9,
		960,
		90,
		65,
		20,
		9,
		960,
		65,
		25,
		20,
		10,
		960
	};
	
	private final KamaParam param = new KamaParam();
	
	public Kamaloka70_80(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		param.qn = "Kamaloka70_80";
		param.minLev = 70;
		param.maxLev = 80;
		param.rewPosition = newCoord(49014, -219737, -8759);
		param.enterCoord = newCoord(48137, -219716, -8759);
		
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
		new Kamaloka70_80(-1, "Kamaloka70_80", "Kamaloka70_80");
	}
}