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
package l2e.scripts.ai.zone.LairOfAntharas;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Created by LordWinter 10.05.2012 Based on L2J Eternity-World
 */
public class DragonGuards extends L2AttackableAIScript
{
	private static final int DRAGON_GUARD = 22852;
	private static final int DRAGON_MAGE = 22853;
	
	private static final int[] WALL_MONSTERS =
	{
		DRAGON_GUARD,
		DRAGON_MAGE
	};
	
	public DragonGuards(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int mobId : WALL_MONSTERS)
		{
			addSpawnId(mobId);
			addAggroRangeEnterId(mobId);
			addAttackId(mobId);
		}
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc instanceof L2MonsterInstance)
		{
			for (int mobId : WALL_MONSTERS)
			{
				if (mobId == npc.getId())
				{
					final L2MonsterInstance monster = (L2MonsterInstance) npc;
					monster.setIsImmobilized(true);
					break;
				}
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if ((!npc.isCastingNow()) && (!npc.isAttackingNow()) && (!npc.isInCombat()) && (!player.isDead()))
		{
			npc.setIsImmobilized(false);
			npc.setRunning();
			((L2Attackable) npc).addDamageHate(player, 0, 999);
			((L2Attackable) npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		if (npc instanceof L2MonsterInstance)
		{
			for (int mobId : WALL_MONSTERS)
			{
				if (mobId == npc.getId())
				{
					final L2MonsterInstance monster = (L2MonsterInstance) npc;
					monster.setIsImmobilized(false);
					monster.setRunning();
					break;
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	public static void main(String[] args)
	{
		new DragonGuards(-1, DragonGuards.class.getSimpleName(), "ai");
	}
}