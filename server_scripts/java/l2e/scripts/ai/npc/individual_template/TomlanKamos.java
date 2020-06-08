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

import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class TomlanKamos extends L2AttackableAIScript
{
	private static L2Npc Tomlan;
	private static final int duration = 300000;
	private static final int TOMLAN = 18554;
	private static long _LastAttack = 0;
	private static boolean successDespawn = false;
	
	public TomlanKamos(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addKillId(TOMLAN);
		addAttackId(TOMLAN);
		addSpawnId(TOMLAN);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("despawn"))
		{
			if (!successDespawn && (Tomlan != null) && ((_LastAttack + 300000) < System.currentTimeMillis()))
			{
				cancelQuestTimer("despawn", npc, null);
				Tomlan.deleteMe();
				if (InstanceManager.getInstance().getInstance(Tomlan.getInstanceId()) != null)
				{
					InstanceManager.getInstance().getInstance(Tomlan.getInstanceId()).setDuration(duration);
				}
				successDespawn = true;
			}
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		_LastAttack = System.currentTimeMillis();
		startQuestTimer("despawn", 60000, npc, null, true);
		Tomlan = npc;
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		_LastAttack = System.currentTimeMillis();
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		cancelQuestTimer("despawn", npc, null);
		return null;
	}
	
	public static void main(String[] args)
	{
		new TomlanKamos(-1, "TomlanKamos", "ai");
	}
}