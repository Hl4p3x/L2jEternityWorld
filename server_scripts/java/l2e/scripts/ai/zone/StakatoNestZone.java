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

import java.util.List;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.util.Broadcast;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class StakatoNestZone extends AbstractNpcAI
{
	private static final int[] STAKATO_MOBS =
	{
	                18793,
	                18794,
	                18795,
	                18796,
	                18797,
	                18798,
	                22617,
	                22618,
	                22619,
	                22620,
	                22621,
	                22622,
	                22623,
	                22624,
	                22625,
	                22626,
	                22627,
	                22628,
	                22629,
	                22630,
	                22631,
	                22632,
	                22633,
	                25667
	};

	private static final int[] COCOONS =
	{
	                18793,
	                18794,
	                18795,
	                18796,
	                18797,
	                18798
	};

	private static final int STAKATO_LEADER = 22625;
	private static final int STAKATO_NURSE = 22630;
	private static final int STAKATO_NURSE_2 = 22631;
	private static final int STAKATO_BABY = 22632;
	private static final int STAKATO_CAPTAIN = 22629;
	private static final int STAKATO_FEMALE = 22620;
	private static final int STAKATO_MALE = 22621;
	private static final int STAKATO_MALE_2 = 22622;
	private static final int STAKATO_GUARD = 22619;
	private static final int STAKATO_CHIEF = 25667;
	private static final int GROWTH_ACCELERATOR = 2905;
	private static final int SMALL_COCOON = 14833;
	private static final int LARGE_COCOON = 14834;

	private StakatoNestZone(String name, String descr)
	{
		super(name, descr);

		registerMobs(STAKATO_MOBS);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		L2MonsterInstance mob = (L2MonsterInstance) npc;

		if ((mob.getId() == STAKATO_LEADER) && (getRandom(1000) < 100) && (mob.getCurrentHp() < (mob.getMaxHp() * 0.3)))
		{
			L2MonsterInstance _follower = checkMinion(npc);

			if (_follower != null)
			{
				double _hp = _follower.getCurrentHp();

				if (_hp > (_follower.getMaxHp() * 0.3))
				{
					mob.abortAttack();
					mob.abortCast();
					mob.setHeading(Util.calculateHeadingFrom(mob, _follower));
					mob.doCast(SkillHolder.getInstance().getInfo(4484, 1));
					mob.setCurrentHp(mob.getCurrentHp() + _hp);
					_follower.doDie(_follower);
					_follower.deleteMe();
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		L2MonsterInstance _minion = checkMinion(npc);

		if ((npc.getId() == STAKATO_NURSE) && (_minion != null))
		{
			Broadcast.toSelfAndKnownPlayers(npc, new MagicSkillUse(npc, 2046, 1, 1000, 0));
			for (int i = 0; i < 3; i++)
			{
				L2Npc _spawned = addSpawn(STAKATO_CAPTAIN, _minion, true);
				attackPlayer(killer, _spawned);
			}
		}
		else if (npc.getId() == STAKATO_BABY)
		{
			L2MonsterInstance leader = ((L2MonsterInstance) npc).getLeader();
			if ((leader != null) && !leader.isDead())
			{
				startQuestTimer("nurse_change", 5000, leader, killer);
			}
		}
		else if ((npc.getId() == STAKATO_MALE) && (_minion != null))
		{
			Broadcast.toSelfAndKnownPlayers(npc, new MagicSkillUse(npc, 2046, 1, 1000, 0));
			for (int i = 0; i < 3; i++)
			{
				L2Npc _spawned = addSpawn(STAKATO_GUARD, _minion, true);
				attackPlayer(killer, _spawned);
			}
		}
		else if (npc.getId() == STAKATO_FEMALE)
		{
			L2MonsterInstance leader = ((L2MonsterInstance) npc).getLeader();
			if ((leader != null) && !leader.isDead())
			{
				startQuestTimer("male_change", 5000, leader, killer);
			}
		}
		else if (npc.getId() == STAKATO_CHIEF)
		{
			if (killer.isInParty())
			{
				List<L2PcInstance> party = killer.getParty().getMembers();
				for (L2PcInstance member : party)
				{
					giveCocoon(member, npc);
				}
			}
			else
			{
				giveCocoon(killer, npc);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (Util.contains(COCOONS, npc.getId()) && Util.contains(targets, npc) && (skill.getId() == GROWTH_ACCELERATOR))
		{
			npc.doDie(caster);
			L2Npc spawned = addSpawn(STAKATO_CHIEF, npc.getX(), npc.getY(), npc.getZ(), Util.calculateHeadingFrom(npc, caster), false, 0, true);
			attackPlayer(caster, spawned);
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ((npc == null) || (player == null))
		{
			return null;
		}
		if (npc.isDead())
		{
			return null;
		}

		if (event.equalsIgnoreCase("nurse_change"))
		{
			npc.getSpawn().decreaseCount(npc);
			npc.deleteMe();
			L2Npc _spawned = addSpawn(STAKATO_NURSE_2, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, true);
			attackPlayer(player, _spawned);
		}
		else if (event.equalsIgnoreCase("male_change"))
		{
			npc.getSpawn().decreaseCount(npc);
			npc.deleteMe();
			L2Npc _spawned = addSpawn(STAKATO_MALE_2, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, true);
			attackPlayer(player, _spawned);
		}
		return null;
	}

	private static L2MonsterInstance checkMinion(L2Npc npc)
	{
		L2MonsterInstance mob = (L2MonsterInstance) npc;
		if (mob.hasMinions())
		{
			List<L2MonsterInstance> minion = mob.getMinionList().getSpawnedMinions();
			if ((minion != null) && !minion.isEmpty() && (minion.get(0) != null) && !minion.get(0).isDead())
			{
				return minion.get(0);
			}
		}
		return null;
	}

	private static void attackPlayer(L2PcInstance player, L2Npc npc)
	{
		if ((npc != null) && (player != null))
		{
			((L2Attackable) npc).setIsRunning(true);
			((L2Attackable) npc).addDamageHate(player, 0, 999);
			((L2Attackable) npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
	}

	private static void giveCocoon(L2PcInstance player, L2Npc npc)
	{
		if (getRandom(100) > 80)
		{
			player.addItem("StakatoCocoon", LARGE_COCOON, 1, npc, true);
		}
		else
		{
			player.addItem("StakatoCocoon", SMALL_COCOON, 1, npc, true);
		}
	}

	public static void main(String[] args)
	{
		new StakatoNestZone(StakatoNestZone.class.getSimpleName(), "ai");
	}
}
