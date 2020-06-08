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

import javolution.util.FastMap;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.L2AttackableAIScript;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Based on L2J Eternity-World
 */
public class BladeOtis extends L2AttackableAIScript
{
	private static final int BLADEO = 18562;
	private static final int GUARD = 18563;
	private final TIntObjectHashMap<Integer> _guardSpawns = new TIntObjectHashMap<>();
	private final FastMap<L2Npc, L2Npc> _guardMaster = new FastMap<>();
	
	public BladeOtis(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addAttackId(BLADEO);
		addKillId(BLADEO);
		addKillId(GUARD);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		int objId = npc.getObjectId();
		int x = player.getX();
		int y = player.getY();
		
		if ((_guardSpawns.get(objId) != null) && (_guardSpawns.get(objId) >= 6))
		{
			return null;
		}
		if (event.equalsIgnoreCase("time_to_spawn"))
		{
			L2Npc guard = addSpawn(GUARD, x + getRandom(-20, 50), y + getRandom(-20, 50), npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			if (_guardSpawns.get(objId) != null)
			{
				_guardSpawns.put(objId, _guardSpawns.get(objId) + 1);
			}
			else
			{
				_guardSpawns.put(objId, 1);
			}
			_guardMaster.put(guard, npc);
			guard.setTarget(player);
			((L2Attackable) npc).addDamageHate(player, 0, 999);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		int npcId = npc.getId();
		int objId = npc.getObjectId();
		int maxHp = npc.getMaxHp();
		double nowHp = npc.getStatus().getCurrentHp();
		
		if (npcId == BLADEO)
		{
			if (nowHp < (maxHp * 0.5))
			{
				if ((_guardSpawns.get(objId) == null) || (_guardSpawns.get(objId) == 0))
				{
					startQuestTimer("time_to_spawn", 1, npc, player);
				}
				else if (_guardSpawns.get(objId) < 6)
				{
					startQuestTimer("time_to_spawn", 10000, npc, player);
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		int npcId = npc.getId();
		int objId = npc.getObjectId();
		
		if (npcId == GUARD)
		{
			if (_guardMaster.get(npc) != null)
			{
				L2Npc master = _guardMaster.get(npc);
				if ((_guardSpawns.get(master.getObjectId()) != null) && (_guardSpawns.get(master.getObjectId()) > 0))
				{
					_guardSpawns.put(master.getObjectId(), _guardSpawns.get(master.getObjectId()) - 1);
				}
				_guardMaster.remove(npc);
			}
		}
		else if (npcId == BLADEO)
		{
			if (_guardSpawns.contains(objId))
			{
				_guardSpawns.remove(objId);
			}
			for (L2Npc i : _guardMaster.keySet())
			{
				if ((_guardMaster.get(i) != null) && (npc == _guardMaster.get(i)))
				{
					i.decayMe();
					_guardMaster.remove(i);
				}
			}
			this.cancelQuestTimer("time_to_spawn", npc, killer);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new BladeOtis(-1, "BladeOtis", "ai");
	}
}