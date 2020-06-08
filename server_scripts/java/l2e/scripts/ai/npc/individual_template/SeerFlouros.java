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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class SeerFlouros extends L2AttackableAIScript
{
	private static L2Npc SeerFlouros, Follower;
	private static final int duration = 300000;
	private static final int SeerFlourosId = 18559;
	private static final int FollowerId = 18560;
	private static long _LastAttack = 0;
	private static boolean successDespawn = false;
	private static boolean minion = false;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("despawn"))
		{
			if (!successDespawn && (SeerFlouros != null) && ((_LastAttack + 300000) < System.currentTimeMillis()))
			{
				cancelQuestTimer("despawn", npc, null);
				SeerFlouros.deleteMe();
				InstanceManager.getInstance().getInstance(SeerFlouros.getInstanceId()).setDuration(duration);
				successDespawn = true;
				
				if (Follower != null)
				{
					Follower.deleteMe();
				}
			}
		}
		else if (event.equalsIgnoreCase("respMinion") && (SeerFlouros != null))
		{
			Follower = addSpawn(FollowerId, SeerFlouros.getX(), SeerFlouros.getY(), SeerFlouros.getZ(), SeerFlouros.getHeading(), false, 0);
			L2Attackable target = (L2Attackable) SeerFlouros;
			Follower.setRunning();
			((L2Attackable) Follower).addDamageHate(target.getMostHated(), 0, 999);
			Follower.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getId() == SeerFlourosId)
		{
			_LastAttack = System.currentTimeMillis();
			startQuestTimer("despawn", 60000, npc, null, true);
			SeerFlouros = npc;
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (!minion)
		{
			Follower = addSpawn(FollowerId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
			minion = true;
		}
		_LastAttack = System.currentTimeMillis();
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == SeerFlourosId)
		{
			cancelQuestTimer("despawn", npc, null);
			if (Follower != null)
			{
				Follower.deleteMe();
			}
		}
		else if ((npc.getId() == FollowerId) && (SeerFlouros != null))
		{
			startQuestTimer("respMinion", 30000, npc, null);
		}
		return null;
	}
	
	public SeerFlouros()
	{
		super(-1, "SeerFlouros", "ai");
		registerMobs(new int[]
		{
			SeerFlourosId,
			FollowerId
		});
	}
}