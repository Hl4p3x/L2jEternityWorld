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
package l2e.scripts.ai.modifiers;

import java.util.Collection;

import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class NoAttackingMobs extends L2AttackableAIScript
{
	// Add here the IDs of the mobs that should never be champion
	private final static int[] NO_ATTACKING_LIST =
	{
	                // Eye of Kasha
	                18812, 18813, 18814,
	                // Queen Ant Nurses
	                29003
	};

	public NoAttackingMobs(int questId, String name, String descr)
	{
		super(questId, name, descr);

		final Collection<L2Spawn> spawns = SpawnTable.getInstance().getSpawnTable();
		for (L2Spawn npc : spawns)
		{
			if (Util.contains(NO_ATTACKING_LIST, npc.getTemplate()._npcId))
			{
				if (npc.getLastSpawn() != null)
				{
					npc.getLastSpawn().setIsAttackDisabled(true);
				}
			}
		}

		for (int npcid : NO_ATTACKING_LIST)
		{
			addSpawnId(npcid);
		}
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (Util.contains(NO_ATTACKING_LIST, npc.getId()))
		{
			npc.setIsAttackDisabled(true);
		}

		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new NoAttackingMobs(-1, "NoAttackingMobs", "modifiers");
	}
}
