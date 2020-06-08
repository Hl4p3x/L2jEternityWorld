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
package l2e.scripts.ai.npc.individual_template;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class HolyBrazier extends L2AttackableAIScript
{
	
	private static final int HolyBrazier = 32027;
	private static final int GuardianOfTheGrail = 22133;
	
	private L2Npc _guard = null;
	private L2Npc _brazier = null;
	
	public HolyBrazier(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] mobs =
		{
			HolyBrazier,
			GuardianOfTheGrail
		};
		registerMobs(mobs);
	}
	
	private void spawnGuard(L2Npc npc)
	{
		System.out.println("******* spawnGuard *******");
		System.out.println("_guard   = " + _guard);
		System.out.println("_brazier = " + _brazier);
		if ((_guard == null) && (_brazier != null))
		{
			System.out.println("******* addSpawn *******");
			_guard = addSpawn(GuardianOfTheGrail, _brazier.getX(), _brazier.getY(), _brazier.getZ(), 0, false, 0);
			_guard.setIsNoRndWalk(true);
		}
		System.out.println("******* return *******");
		return;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		System.out.println("******* onSpawn *******");
		System.out.println("npc = " + npc.getId());
		if (npc.getId() == HolyBrazier)
		{
			System.out.println("******* HolyBrazier *******");
			_brazier = npc;
			_guard = null;
			npc.setIsNoRndWalk(true);
			spawnGuard(npc);
		}
		System.out.println("******* return *******");
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if ((npc.getId() == GuardianOfTheGrail) && !npc.isInCombat() && (npc.getTarget() == null))
		{
			npc.setIsNoRndWalk(true);
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == GuardianOfTheGrail)
		{
			_guard = null;
			spawnGuard(npc);
		}
		else if (npc.getId() == HolyBrazier)
		{
			if (_guard != null)
			{
				_guard.deleteMe();
				_guard = null;
				
			}
			_brazier = null;
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new HolyBrazier(-1, "HolyBrazier", "ai");
	}
}