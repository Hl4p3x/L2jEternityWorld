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
package l2e.scripts.ai.zone;

import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.scripts.ai.npc.AbstractNpcAI;

/**
 * Based on L2J Eternity-World
 */
public final class MithrilMinesZone extends AbstractNpcAI
{
	private static final int GRAVE_ROBBER_SUMMONER = 22678;
	private static final int GRAVE_ROBBER_MAGICIAN = 22679;

	private L2Npc _summoner = null;
	private L2Npc _magician = null;

	private static final int[] SUMMONER_MINIONS =
	{
	                22683,
	                22684
	};

	private static final int[] MAGICIAN_MINIONS =
	{
	                22685,
	                22686
	};

	private MithrilMinesZone(String name, String descr)
	{
		super(name, descr);

		findNpcs();
		if ((_summoner == null) || (_magician == null))
		{
			throw new NullPointerException("Can`t find MithrilMines npcs!");
		}
		addSpawnId(GRAVE_ROBBER_SUMMONER, GRAVE_ROBBER_MAGICIAN);
	}

	private void findNpcs()
	{
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			switch (spawn.getId())
			{
				case GRAVE_ROBBER_SUMMONER:
					_summoner = spawn.getLastSpawn();
					break;
				case GRAVE_ROBBER_MAGICIAN:
					_magician = spawn.getLastSpawn();
					break;
			}
		}
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		int[] minions = MAGICIAN_MINIONS;
		if (npc.getId() == GRAVE_ROBBER_SUMMONER)
		{
			minions = SUMMONER_MINIONS;
		}
		addMinion((L2MonsterInstance) npc, minions[getRandom(minions.length)]);
		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new MithrilMinesZone(MithrilMinesZone.class.getSimpleName(), "ai");
	}
}
