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
package l2e.scripts.ai.npc.group_template;

import javolution.util.FastSet;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.L2AttackableAIScript;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public class OlAriosh extends L2AttackableAIScript
{
	private static final int ARIOSH = 18555;
	private static final int GUARD = 18556;
	private static L2Npc _guard = null;
	private final FastSet<Integer> _lockedSpawns = new FastSet<>();
	private final TIntObjectHashMap<Integer> _spawnedGuards = new TIntObjectHashMap<>();
	
	public OlAriosh(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addAttackId(ARIOSH);
		addKillId(ARIOSH);
		addKillId(GUARD);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_to_spawn"))
		{
			final int objId = npc.getObjectId();
			if (!_spawnedGuards.contains(objId))
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.WHAT_ARE_YOU_DOING_HURRY_UP_AND_HELP_ME));
				_guard = addSpawn(GUARD, npc.getX() + 100, npc.getY() + 100, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
				_lockedSpawns.remove(objId);
				_spawnedGuards.put(_guard.getObjectId(), objId);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		if (npc.getId() == ARIOSH)
		{
			final int objId = npc.getObjectId();
			if (!_spawnedGuards.contains(objId))
			{
				if (!_lockedSpawns.contains(objId))
				{
					startQuestTimer("time_to_spawn", 60000, npc, player);
					_lockedSpawns.add(objId);
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		switch (npc.getId())
		{
			case GUARD:
				_spawnedGuards.remove(npc.getObjectId());
				break;
			case ARIOSH:
				_spawnedGuards.remove(_guard.getObjectId());
				_guard.decayMe();
				cancelQuestTimer("time_to_spawn", npc, killer);
				break;
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new OlAriosh(-1, "OlAriosh", "ai");
	}
}