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

import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.util.Util;
import l2e.scripts.ai.L2AttackableAIScript;

/**
 * Based on L2J Eternity-World
 */
public class HotSpringDisease extends L2AttackableAIScript
{
	static final int[] disease1mobs =
	{
	                21314, 21316, 21317, 21319, 21321, 21322
	};
	static final int[] disease2mobs =
	{
	                21317, 21322
	};
	static final int[] disease3mobs =
	{
	                21316, 21319
	};
	static final int[] disease4mobs =
	{
	                21314, 21321
	};

	private static final int DISEASE_CHANCE = 5;

	public HotSpringDisease(int questId, String name, String descr)
	{
		super(questId, name, descr);

		registerMobs(disease1mobs);
		registerMobs(disease2mobs);
		registerMobs(disease3mobs);
		registerMobs(disease4mobs);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (Util.contains(disease1mobs, npc.getId()))
		{
			if (getRandom(100) < DISEASE_CHANCE)
			{
				npc.setTarget(attacker);
				npc.doCast(SkillHolder.getInstance().getInfo(4554, getRandom(10) + 1));
			}
		}
		if (Util.contains(disease2mobs, npc.getId()))
		{
			if (getRandom(100) < DISEASE_CHANCE)
			{
				npc.setTarget(attacker);
				npc.doCast(SkillHolder.getInstance().getInfo(4553, getRandom(10) + 1));
			}
		}
		if (Util.contains(disease3mobs, npc.getId()))
		{
			if (getRandom(100) < DISEASE_CHANCE)
			{
				npc.setTarget(attacker);
				npc.doCast(SkillHolder.getInstance().getInfo(4552, getRandom(10) + 1));
			}
		}
		if (Util.contains(disease4mobs, npc.getId()))
		{
			if (getRandom(100) < DISEASE_CHANCE)
			{
				npc.setTarget(attacker);
				npc.doCast(SkillHolder.getInstance().getInfo(4551, getRandom(10) + 1));
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	public static void main(String[] args)
	{
		new HotSpringDisease(-1, "HotSpringDisease", "ai");
	}
}
