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
public class NoRandomAnimation extends L2AttackableAIScript
{
	private final static int[] NO_ANIMATION_MOBS_LIST =
	{
	                13148, 18635, 18636, 18638, 18639, 18640, 18641, 18642, 18644, 18645, 18646, 18648,
	                18649, 18650, 18652, 18653, 18654, 18655, 18656, 18657, 18658, 18659, 18660, 18704,
	                18705, 18706, 18708, 18709, 18710, 18711, 18805, 18806, 18811, 22136, 29045, 29046,
	                29047, 29048, 29049, 29050, 29051, 29099, 29103, 29150, 29151, 29152, 29161, 29163,
	                29173, 29174, 29175, 30675, 30761, 30762, 30763, 30980, 31074, 31665, 32746, 31752,
	                32015, 32568, 32556, 32568, 29118, 29119
	};

	public NoRandomAnimation(int questId, String name, String descr)
	{
		super(questId, name, descr);

		final Collection<L2Spawn> spawns = SpawnTable.getInstance().getSpawnTable();
		for (L2Spawn npc : spawns)
		{
			if (Util.contains(NO_ANIMATION_MOBS_LIST, npc.getTemplate()._npcId))
			{
				if (npc.getLastSpawn() != null)
				{
					npc.getLastSpawn().setIsNoAnimation(true);
				}
			}
		}

		for (int npcid : NO_ANIMATION_MOBS_LIST)
		{
			addSpawnId(npcid);
		}
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (Util.contains(NO_ANIMATION_MOBS_LIST, npc.getId()))
		{
			npc.setIsNoAnimation(true);
		}

		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new NoRandomAnimation(-1, "NoRandomAnimation", "modifiers");
	}
}
