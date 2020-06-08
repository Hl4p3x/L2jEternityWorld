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

import java.util.Collection;
import java.util.Map;

import javolution.util.FastMap;
import javolution.util.FastSet;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.L2AttackableAIScript;

public class ReedFields extends L2AttackableAIScript
{
	private static FastSet<Integer> myTrackingSet = new FastSet<>();
	private static final Map<Integer, NpcStringId> MOBSTEXT = new FastMap<>();
	
	private static final int[] _devices =
	{
		18805,
		18806
	};
	
	private static final int[] _mobs =
	{
		22656,
		22657,
		22658,
		22659,
		18805,
		18806,
		22654
	};
	
	static
	{
		MOBSTEXT.put(Integer.valueOf(22656), NpcStringId.TARGET_RECOGNITION_ACHIEVED_ATTACK_SEQUENCE_COMMENCING);
		MOBSTEXT.put(Integer.valueOf(22657), NpcStringId.TARGET_THREAT_LEVEL_LAUNCHING_STRONGEST_COUNTERMEASURE);
		MOBSTEXT.put(Integer.valueOf(18805), NpcStringId.ALERT_ALERT_DAMAGE_DETECTION_RECOGNIZED_COUNTERMEASURES_ENABLED);
		MOBSTEXT.put(Integer.valueOf(22658), NpcStringId.PROTECT_THE_BRAZIERS_OF_PURITY_AT_ALL_COSTS);
		MOBSTEXT.put(Integer.valueOf(22659), NpcStringId.DEFEND_OUR_DOMAIN_EVEN_AT_RISK_OF_YOUR_OWN_LIFE);
		MOBSTEXT.put(Integer.valueOf(18806), NpcStringId.THE_PURIFICATION_FIELD_IS_BEING_ATTACKED_GUARDIAN_SPIRITS_PROTECT_THE_MAGIC_FORCE);
	}
	
	public ReedFields(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addAggroRangeEnterId(22654);
		
		for (int i : _devices)
		{
			addAttackId(i);
		}
		
		for (int id : _mobs)
		{
			addKillId(id);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("cleanup"))
		{
			myTrackingSet.remove(Integer.valueOf(npc.getObjectId()));
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (!myTrackingSet.contains(Integer.valueOf(npc.getObjectId())))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.NAIA_WAGANAGEL_PEUTAGUN));
			myTrackingSet.add(Integer.valueOf(npc.getObjectId()));
			startQuestTimer("cleanup", 30000L, npc, null);
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		L2Character target = isSummon ? player.getSummon() : player;
		if (!myTrackingSet.contains(Integer.valueOf(npc.getObjectId())))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), MOBSTEXT.get(Integer.valueOf(npc.getId()))));
			myTrackingSet.add(Integer.valueOf(npc.getObjectId()));
		}
		
		Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
		for (L2Object obj : objs)
		{
			if (obj != null)
			{
				if (obj instanceof L2MonsterInstance)
				{
					L2MonsterInstance monster = (L2MonsterInstance) obj;
					monster.setTarget(target);
					monster.setRunning();
					monster.addDamageHate(target, 0, 999);
					monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					monster.broadcastPacket(new NpcSay(monster.getObjectId(), 0, monster.getId(), MOBSTEXT.get(Integer.valueOf(monster.getId()))));
					myTrackingSet.add(Integer.valueOf(monster.getObjectId()));
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (myTrackingSet.contains(Integer.valueOf(npc.getObjectId())))
		{
			myTrackingSet.remove(Integer.valueOf(npc.getObjectId()));
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new ReedFields(-1, "ReedFields", "ai");
	}
}