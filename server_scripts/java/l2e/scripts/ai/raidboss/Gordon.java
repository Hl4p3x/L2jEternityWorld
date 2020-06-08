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

import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class Gordon extends AbstractNpcAI
{
	private static final int GORDON = 29095;
	private L2Npc _gordon;

	private Gordon(String name, String descr)
	{
		super(name, descr);

		addSpawnId(GORDON);
		addSeeCreatureId(GORDON);

		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn != null)
			{
				if (spawn.getId() == GORDON)
				{
					_gordon = spawn.getLastSpawn();
					if (_gordon != null)
					{
						onSpawn(_gordon);
					}
				}
			}
		}
	}

	@Override
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		if (creature.isPlayer() && ((L2PcInstance) creature).isCursedWeaponEquipped())
		{
			attackPlayer((L2Attackable) npc, (L2PcInstance) creature);
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		((L2Attackable) npc).setCanReturnToSpawnPoint(false);
		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new Gordon(Gordon.class.getSimpleName(), "ai");
	}
}
