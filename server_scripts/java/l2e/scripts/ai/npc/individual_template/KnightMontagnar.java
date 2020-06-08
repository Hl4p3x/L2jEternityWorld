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

import java.util.ArrayList;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class KnightMontagnar extends L2AttackableAIScript
{
	private L2Npc Boss;
	private static final int duration = 300000;
	private static final int BOSS = 18568;
	private static final int FOLLOWER = 18569;
	private static long _LastAttack = 0;
	boolean successDespawn = false;
	boolean firstAttack = false;
	boolean follower1 = false;
	boolean follower2 = false;
	boolean follower3 = false;
	boolean follower4 = false;
	boolean follower5 = false;
	boolean follower6 = false;
	private ArrayList<L2Attackable> followers = null;
	private int instanceId = 0;
	
	public KnightMontagnar(int id, String name, String descr)
	{
		super(id, name, descr);
		
		int[] mobs =
		{
			BOSS,
			FOLLOWER
		};
		registerMobs(mobs);
		followers = new ArrayList<>();
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		L2PcInstance _target;
		
		if (event.equalsIgnoreCase("spawn_follower") && (npc != null))
		{
			L2Attackable follower = (L2Attackable) addSpawn(FOLLOWER, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			follower.setIsInvul(true);
			followers.add(follower);
			_target = setRandomPlayerTarget(follower);
			follower.setRunning();
			follower.addDamageHate(_target, 0, 999);
			follower.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _target);
		}
		else if (event.equalsIgnoreCase("set_target") && (npc != null))
		{
			_target = setRandomPlayerTarget(npc);
			
			if (_target != null)
			{
				NpcSay packet = new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.YOU_S1_ATTACK_THEM);
				packet.addStringParameter(_target.getName().toString());
				npc.broadcastPacket(packet);
				for (L2Attackable Follower : followers)
				{
					Follower.clearAggroList();
					Follower.setRunning();
					Follower.addDamageHate(_target, 0, 9999);
					Follower.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _target);
				}
			}
			startQuestTimer("set_target", 25000, npc, null);
		}
		else if (event.equalsIgnoreCase("despawn"))
		{
			if (InstanceManager.getInstance().getInstance(instanceId) == null)
			{
				cancelQuestTimer("despawn", npc, null);
				successDespawn = true;
			}
		}
		else if (!successDespawn && (npc != null))
		{
			if ((_LastAttack + 300000) < System.currentTimeMillis())
			{
				cancelQuestTimer("despawn", npc, null);
				Boss.deleteMe();
				for (L2Attackable Follower : followers)
				{
					Follower.deleteMe();
				}
				InstanceManager.getInstance().getInstance(instanceId).setDuration(duration);
				successDespawn = true;
			}
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getId() == BOSS)
		{
			_LastAttack = System.currentTimeMillis();
			startQuestTimer("despawn", 60000, npc, null, true);
			Boss = npc;
			instanceId = npc.getInstanceId();
			firstAttack = false;
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		_LastAttack = System.currentTimeMillis();
		
		if (npc.getId() == BOSS)
		{
			if (!firstAttack)
			{
				firstAttack = true;
			}
			if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.85)) && !follower1)
			{
				startQuestTimer("spawn_follower", 100, npc, null);
				cancelQuestTimer("set_target", npc, null);
				startQuestTimer("set_target", 200, npc, null);
				follower1 = true;
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.70)) && !follower2)
			{
				startQuestTimer("spawn_follower", 100, npc, null);
				cancelQuestTimer("set_target", npc, null);
				startQuestTimer("set_target", 200, npc, null);
				follower2 = true;
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.55)) && !follower3)
			{
				startQuestTimer("spawn_follower", 100, npc, null);
				cancelQuestTimer("set_target", npc, null);
				startQuestTimer("set_target", 200, npc, null);
				follower3 = true;
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.40)) && !follower4)
			{
				startQuestTimer("spawn_follower", 100, npc, null);
				cancelQuestTimer("set_target", npc, null);
				startQuestTimer("set_target", 200, npc, null);
				follower4 = true;
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.25)) && !follower5)
			{
				startQuestTimer("spawn_follower", 100, npc, null);
				cancelQuestTimer("set_target", npc, null);
				startQuestTimer("set_target", 200, npc, null);
				follower5 = true;
			}
			else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.10)) && !follower6)
			{
				startQuestTimer("spawn_follower", 100, npc, null);
				cancelQuestTimer("set_target", npc, null);
				startQuestTimer("set_target", 200, npc, null);
				follower6 = true;
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == BOSS)
		{
			cancelQuestTimer("despawn", npc, null);
			cancelQuestTimer("skill_use", npc, null);
			for (L2Attackable Follower : followers)
			{
				Follower.deleteMe();
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new KnightMontagnar(-1, "KnightMontagnar", "ai");
	}
}