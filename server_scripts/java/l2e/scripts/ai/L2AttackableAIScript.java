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
package l2e.scripts.ai;

import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import l2e.Config;
import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.instancemanager.DimensionalRiftManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2DecoyInstance;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2RiftInvaderInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.util.Util;

public class L2AttackableAIScript extends Quest
{
	public void registerMobs(int[] mobs)
	{
		addAttackId(mobs);
		addKillId(mobs);
		addSpawnId(mobs);
		addSpellFinishedId(mobs);
		addSkillSeeId(mobs);
		addAggroRangeEnterId(mobs);
		addFactionCallId(mobs);
	}

	public void registerMobs(int[] mobs, QuestEventType... types)
	{
		for (QuestEventType type : types)
		{
			addEventId(type, mobs);
		}
	}

	public void registerMobs(Collection<Integer> mobs, QuestEventType... types)
	{
		for (int id : mobs)
		{
			for (QuestEventType type : types)
			{
				addEventId(type, id);
			}
		}
	}

	public L2AttackableAIScript(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		return null;
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (caster == null)
		{
			return null;
		}
		if (!(npc instanceof L2Attackable))
		{
			return null;
		}

		L2Attackable attackable = (L2Attackable) npc;

		int skillAggroPoints = skill.getAggroPoints();

		if (caster.hasSummon())
		{
			if ((targets.length == 1) && Util.contains(targets, caster.getSummon()))
			{
				skillAggroPoints = 0;
			}
		}

		if (skillAggroPoints > 0)
		{
			if (attackable.hasAI() && (attackable.getAI().getIntention() == AI_INTENTION_ATTACK))
			{
				L2Object npcTarget = attackable.getTarget();
				for (L2Object skillTarget : targets)
				{
					if ((npcTarget == skillTarget) || (npc == skillTarget))
					{
						L2Character originalCaster = isSummon ? caster.getSummon() : caster;
						attackable.addDamageHate(originalCaster, 0, (skillAggroPoints * 150) / (attackable.getLevel() + 7));
					}
				}
			}
		}

		return null;
	}

	@Override
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isSummon)
	{
		if (attacker == null)
		{
			return null;
		}

		L2Character originalAttackTarget = (isSummon ? attacker.getSummon() : attacker);
		if (attacker.isInParty() && attacker.getParty().isInDimensionalRift())
		{
			byte riftType = attacker.getParty().getDimensionalRift().getType();
			byte riftRoom = attacker.getParty().getDimensionalRift().getCurrentRoom();

			if ((caller instanceof L2RiftInvaderInstance) && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
			{
				return null;
			}
		}
		npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, originalAttackTarget, 1);

		return null;
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (player == null)
		{
			return null;
		}

		L2Character target = isSummon ? player.getSummon() : player;

		((L2Attackable) npc).addDamageHate(target, 0, 1);

		if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		return null;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		return null;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if ((attacker != null) && (npc instanceof L2Attackable))
		{
			L2Attackable attackable = (L2Attackable) npc;

			L2Character originalAttacker = isSummon ? attacker.getSummon() : attacker;
			attackable.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, originalAttacker);
			attackable.addDamageHate(originalAttacker, damage, (damage * 100) / (attackable.getLevel() + 7));
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc instanceof L2MonsterInstance)
		{
			final L2MonsterInstance mob = (L2MonsterInstance) npc;
			if ((mob.getLeader() != null) && mob.getLeader().hasMinions())
			{
				final int respawnTime = Config.MINIONS_RESPAWN_TIME.containsKey(npc.getId()) ? Config.MINIONS_RESPAWN_TIME.get(mob.getId()) * 1000 : -1;
				mob.getLeader().getMinionList().onMinionDie(mob, respawnTime);
			}

			if (mob.hasMinions())
			{
				mob.getMinionList().onMasterDie(false);
			}
		}
		return null;
	}

	public L2Character setRandomTarget(L2Npc npc)
	{
		ArrayList<L2Character> result = new ArrayList<>();
		for (L2Object obj : npc.getKnownList().getKnownObjects().values())
		{
			if (((obj.isPlayer()) || (obj instanceof L2Summon) || (obj instanceof L2DecoyInstance)) && !(obj.getZ() < (npc.getZ() - 100)) && !(obj.getZ() > (npc.getZ() + 100)) && !((L2Character) obj).isDead())
			{
				result.add((L2Character) obj);
			}
		}
		if (!result.isEmpty() && (result.size() != 0))
		{
			return result.get(getRandom(result.size()));
		}
		return null;
	}

	public L2PcInstance setRandomPlayerTarget(L2Npc npc)
	{
		ArrayList<L2PcInstance> result = new ArrayList<>();
		for (L2Object obj : npc.getKnownList().getKnownObjects().values())
		{
			if ((obj.isPlayer()) && !(obj.getZ() < (npc.getZ() - 100)) && !(obj.getZ() > (npc.getZ() + 100)) && !((L2PcInstance) obj).isDead())
			{
				result.add((L2PcInstance) obj);
			}
		}
		if (!result.isEmpty() && (result.size() != 0))
		{
			return result.get(getRandom(result.size()));
		}
		return null;
	}

	public static void main(String[] args)
	{
		L2AttackableAIScript ai = new L2AttackableAIScript(-1, "L2AttackableAIScript", "L2AttackableAIScript");
		for (int level = 1; level < 100; level++)
		{
			final List<L2NpcTemplate> templates = NpcTable.getInstance().getAllOfLevel(level);
			for (L2NpcTemplate t : templates)
			{
				try
				{
					if (L2Attackable.class.isAssignableFrom(Class.forName("l2e.gameserver.model.actor.instance." + t.getType() + "Instance")))
					{
						ai.addEventId(Quest.QuestEventType.ON_ATTACK, t.getId());
						ai.addEventId(Quest.QuestEventType.ON_KILL, t.getId());
						ai.addEventId(Quest.QuestEventType.ON_SPAWN, t.getId());
						ai.addEventId(Quest.QuestEventType.ON_SKILL_SEE, t.getId());
						ai.addEventId(Quest.QuestEventType.ON_FACTION_CALL, t.getId());
						ai.addEventId(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER, t.getId());
					}
				}
				catch (ClassNotFoundException ex)
				{
					_log.info("Class not found " + t.getType() + "Instance");
				}
			}
		}
	}
}
