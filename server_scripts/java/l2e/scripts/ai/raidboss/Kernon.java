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
package l2e.scripts.ai.raidboss;

import l2e.scripts.ai.L2AttackableAIScript;

import l2e.gameserver.model.actor.instance.L2NpcInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;

/**
 * Based on L2J Eternity-World
 */
public class Kernon extends L2AttackableAIScript
{
	// Kernon NpcID
	private static final int KERNON = 25054;

	// Kernon Z coords
	private static final int z1 = 3900;
	private static final int z2 = 4300;

	public Kernon (int questId, String name, String descr)
	{
		super(questId,name,descr);

		int[] mobs = {KERNON};
		registerMobs(mobs);
	}

	public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		int npcId = npc.getId();

		if (npcId == KERNON)
		{
			int z = npc.getZ();
			if (z > z2 || z < z1)
			{
				npc.teleToLocation(113420,16424,3969);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	public static void main(String[] args)
	{
		new Kernon(-1, "Kernon", "ai");
	}
}