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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.clientpackets.Say2;

public class SummonMinions extends AbstractNpcAI
{
	private static int HasSpawned;
	private static Set<Integer> myTrackingSet = new FastSet<Integer>().shared();
	private final FastMap<Integer, FastList<L2PcInstance>> _attackersList = new FastMap<Integer, FastList<L2PcInstance>>().shared();
	private static final Map<Integer, List<Integer>> MINIONS = new HashMap<>();
	
	static
	{
		// Timak Orc Troop
		MINIONS.put(20767, Arrays.asList(20768, 20769, 20770));
		// Blade of Splendor
		MINIONS.put(21524, Arrays.asList(21525));
		// Punishment of Splendor
		MINIONS.put(21531, Arrays.asList(21658));
		// Wailing of Splendor
		MINIONS.put(21539, Arrays.asList(21540));
		// Island Guardian
		MINIONS.put(22257, Arrays.asList(18364, 18364));
		// White Sand Mirage
		MINIONS.put(22258, Arrays.asList(18364, 18364));
		// Muddy Coral
		MINIONS.put(22259, Arrays.asList(18364, 18364));
		// Kleopora
		MINIONS.put(22260, Arrays.asList(18364, 18364));
		// Seychelles
		MINIONS.put(22261, Arrays.asList(18365, 18365));
		// Naiad
		MINIONS.put(22262, Arrays.asList(18365, 18365));
		// Sonneratia
		MINIONS.put(22263, Arrays.asList(18365, 18365));
		// Castalia
		MINIONS.put(22264, Arrays.asList(18366, 18366));
		// Chrysocolla
		MINIONS.put(22265, Arrays.asList(18366, 18366));
		// Pythia
		MINIONS.put(22266, Arrays.asList(18366, 18366));
		// Invader Soldier of Nightmare
		MINIONS.put(22715, Arrays.asList(22716,22716,22716,22716,22716,22716,22716,22716,22716));
		// Nihil Invader Soldier
		MINIONS.put(22726, Arrays.asList(22727,22727,22727,22727,22727,22727,22727,22727,22727));
		// Mutant Soldier
		MINIONS.put(22737, Arrays.asList(22738,22738,22738,22738,22738,22738,22738,22738,22738));
		// Tanta Lizardman Summoner
		MINIONS.put(22774, Arrays.asList(22768, 22768));
	}

	private static final NpcStringId[] ATTACK_LEADER_MSG =
	{
		NpcStringId.FORCES_OF_DARKNESS_FOLLOW_ME,
		NpcStringId.DESTROY_THE_ENEMY_MY_BROTHERS,
		NpcStringId.SHOW_YOURSELVES,
		NpcStringId.COME_OUT_YOU_CHILDREN_OF_DARKNESS
	};
	
	private SummonMinions(String name, String descr)
	{
		super(name, descr);

		registerMobs(MINIONS.keySet(), QuestEventType.ON_ATTACK, QuestEventType.ON_KILL);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		int npcId = npc.getId();
		int npcObjId = npc.getObjectId();
		
		if (!myTrackingSet.contains(npcObjId))
		{
			myTrackingSet.add(npcObjId);
			HasSpawned = npcObjId;
		}
		if (HasSpawned == npcObjId)
		{
			switch (npcId)
			{
				case 22030:
				case 22032:
				case 22038:
				{
					if (npc.getCurrentHp() < (npc.getMaxHp() / 2.0))
					{
						HasSpawned = 0;
						if (getRandom(100) < 33)
						{
							for (int val : MINIONS.get(npcId))
							{
								L2Attackable newNpc = (L2Attackable) addSpawn(val, (npc.getX() + getRandom(-150, 150)), (npc.getY() + getRandom(-150, 150)), npc.getZ(), 0, false, npc.getInstanceId());
								newNpc.setRunning();
								newNpc.addDamageHate(attacker, 0, 999);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
							}
						}
					}
					break;
				}
				case 22257:
				case 22258:
				case 22259:
				case 22260:
				case 22261:
				case 22262:
				case 22263:
				case 22264:
				case 22265:
				case 22266:
				{
					if (isSummon)
					{
						attacker = attacker.getSummon().getOwner();
					}
					if (attacker.getParty() != null)
					{
						for (L2PcInstance member : attacker.getParty().getMembers())
						{
							if (_attackersList.get(npcObjId) == null)
							{
								FastList<L2PcInstance> player = new FastList<>();
								player.add(member);
								_attackersList.put(npcObjId, player);
							}
							else if (!_attackersList.get(npcObjId).contains(member))
							{
								_attackersList.get(npcObjId).add(member);
							}
						}
					}
					else
					{
						if (_attackersList.get(npcObjId) == null)
						{
							FastList<L2PcInstance> player = new FastList<>();
							player.add(attacker);
							_attackersList.put(npcObjId, player);
						}
						else if (!_attackersList.get(npcObjId).contains(attacker))
						{
							_attackersList.get(npcObjId).add(attacker);
						}
					}
					if (((attacker.getParty() != null) && (attacker.getParty().getMemberCount() > 2)) || (_attackersList.get(npcObjId).size() > 2))
					{
						HasSpawned = 0;
						for (int val : MINIONS.get(npcId))
						{
							L2Attackable newNpc = (L2Attackable) addSpawn(val, npc.getX() + getRandom(-150, 150), npc.getY() + getRandom(-150, 150), npc.getZ(), 0, false, npc.getInstanceId());
							newNpc.setRunning();
							newNpc.addDamageHate(attacker, 0, 999);
							newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
						}
					}
					break;
				}
				default:
				{
					HasSpawned = 0;
					if (npcId != 20767)
					{
						for (int val : MINIONS.get(npcId))
						{
							L2Attackable newNpc = (L2Attackable) addSpawn(val, npc.getX() + getRandom(-150, 150), npc.getY() + getRandom(-150, 150), npc.getZ(), 0, false, npc.getInstanceId());
							newNpc.setRunning();
							newNpc.addDamageHate(attacker, 0, 999);
							newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
						}
					}
					else
					{
						broadcastNpcSay(npc, Say2.NPC_ALL, ATTACK_LEADER_MSG[getRandom(ATTACK_LEADER_MSG.length)]);
						for (int val : MINIONS.get(npcId))
						{
							addSpawn(val, (npc.getX() + getRandom(-100, 100)), (npc.getY() + getRandom(-100, 100)), npc.getZ(), 0, false, 0);
						}
					}

					if (((npcId == 22715) || (npcId == 22726) || (npcId == 22737)) && !npc.isDead())
					{
						onKill(npc, attacker, isSummon);
						npc.deleteMe();
					}
					break;
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		int npcObjId = npc.getObjectId();
		
		myTrackingSet.remove(npcObjId);
		
		if (_attackersList.containsKey(npcObjId))
		{
			_attackersList.get(npcObjId).clear();
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new SummonMinions(SummonMinions.class.getSimpleName(), "ai");
	}
}