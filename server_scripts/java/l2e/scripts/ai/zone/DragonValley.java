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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SpawnTable;
import l2e.gameserver.data.xml.SpawnParser;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class DragonValley extends AbstractNpcAI
{
	private static final int NECROMANCER_OF_THE_VALLEY = 22858;
	private static final int EXPLODING_ORC_GHOST = 22818;
	private static final int WRATHFUL_ORC_GHOST = 22819;
	private static final int DRAKOS_ASSASSIN = 22823;

	private static final int[] SUMMON_NPC =
	{
	                22822,
	                22824,
	                22862
	};

	private static final int[] SPAWN_ANIMATION =
	{
	                22826,
	                22823,
	                22828
	};
	private static final int[] SPOIL_REACT_MONSTER =
	{
	                22822,
	                22823,
	                22824,
	                22825,
	                22826,
	                22827,
	                22828,
	                22829,
	                22830,
	                22831,
	                22832,
	                22833,
	                22834,
	                22860,
	                22861,
	                22862
	};

	private static final int GREATER_HERB_OF_MANA = 8604;
	private static final int SUPERIOR_HERB_OF_MANA = 8605;

	private static final SkillsHolder SELF_DESTRUCTION = new SkillsHolder(6850, 1);

	private DragonValley(String name, String descr)
	{
		super(name, descr);

		addAttackId(NECROMANCER_OF_THE_VALLEY);
		addAttackId(SUMMON_NPC);
		addKillId(NECROMANCER_OF_THE_VALLEY);
		addKillId(SPOIL_REACT_MONSTER);
		addSpawnId(EXPLODING_ORC_GHOST);
		addSpawnId(SPOIL_REACT_MONSTER);

		for (int npcId : SPOIL_REACT_MONSTER)
		{
			for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
			{
				if (spawn.getId() == npcId)
				{
					onSpawn(spawn.getLastSpawn());
				}
			}
		}

		for (L2Spawn spawn : SpawnParser.getInstance().getSpawnData())
		{
			if (spawn != null)
			{
				if (spawn.getId() == NECROMANCER_OF_THE_VALLEY)
				{
					onSpawn(spawn.getLastSpawn());
				}
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("SelfDestruction") && !npc.isDead())
		{
			npc.abortAttack();
			npc.disableCoreAI(true);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			npc.doCast(SELF_DESTRUCTION.getSkill());
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == NECROMANCER_OF_THE_VALLEY)
		{
			spawnGhost(npc, killer, isSummon, 20);
		}
		else if (((L2Attackable) npc).isSweepActive())
		{
			((L2Attackable) npc).dropItem(killer, getRandom(GREATER_HERB_OF_MANA, SUPERIOR_HERB_OF_MANA), 1);
		}
		return super.onKill(npc, killer, isSummon);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.getId() == NECROMANCER_OF_THE_VALLEY)
		{
			spawnGhost(npc, attacker, isSummon, 1);
		}
		else
		{
			if ((npc.getCurrentHp() < (npc.getMaxHp() / 2)) && (getRandom(100) < 5) && npc.isScriptValue(0))
			{
				npc.setScriptValue(1);
				final int rnd = getRandom(3, 5);
				for (int i = 0; i < rnd; i++)
				{
					final L2Playable playable = isSummon ? attacker.getSummon() : attacker;
					final L2Attackable minion = (L2Attackable) addSpawn(DRAKOS_ASSASSIN, npc.getX(), npc.getY(), npc.getZ() + 10, npc.getHeading(), true, 0, true);
					attackPlayer(minion, playable);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		((L2Attackable) npc).setOnKillDelay(0);
		if (npc.getId() == EXPLODING_ORC_GHOST)
		{
			startQuestTimer("SelfDestruction", 3000, npc, null);
		}
		else if (Util.contains(SPAWN_ANIMATION, npc.getId()))
		{
			npc.setShowSummonAnimation(true);
		}
		return super.onSpawn(npc);
	}

	private void spawnGhost(L2Npc npc, L2PcInstance player, boolean isSummon, int chance)
	{
		if ((npc.getScriptValue() < 2) && (getRandom(100) < chance))
		{
			int val = npc.getScriptValue();
			final L2Playable attacker = isSummon ? player.getSummon() : player;
			final L2Attackable Ghost1 = (L2Attackable) addSpawn(getRandom(EXPLODING_ORC_GHOST, WRATHFUL_ORC_GHOST), npc.getX(), npc.getY(), npc.getZ() + 10, npc.getHeading(), false, 0, true);
			attackPlayer(Ghost1, attacker);
			val++;
			if ((val < 2) && (getRandom(100) < 10))
			{
				final L2Attackable Ghost2 = (L2Attackable) addSpawn(getRandom(EXPLODING_ORC_GHOST, WRATHFUL_ORC_GHOST), npc.getX(), npc.getY(), npc.getZ() + 20, npc.getHeading(), false, 0, false);
				attackPlayer(Ghost2, attacker);
				val++;
			}
			npc.setScriptValue(val);
		}
	}

	public static void main(String[] args)
	{
		new DragonValley(DragonValley.class.getSimpleName(), "ai");
	}
}
