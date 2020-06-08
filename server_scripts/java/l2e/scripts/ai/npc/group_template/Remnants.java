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

import l2e.scripts.ai.npc.AbstractNpcAI;

import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;

public class Remnants extends AbstractNpcAI
{
	private static final int[] NPCS =
	{
		18463,
		18464,
		18465
	};
	
	private static final int HOLY_WATER = 2358;
	
	private Remnants(String name, String descr)
	{
		super(name, descr);

		addSpawnId(NPCS);
		addSkillSeeId(NPCS);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setIsMortal(false);
		return super.onSpawn(npc);
	}
	
	@Override
	public final String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		if (skill.getId() == HOLY_WATER)
		{
			if (!npc.isDead())
			{
				if ((targets.length > 0) && (targets[0] == npc))
				{
					if (npc.getCurrentHp() < (npc.getMaxHp() * 0.02))
					{
						npc.doDie(caster);
					}
				}
			}
		}
		
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Remnants(Remnants.class.getSimpleName(), "ai");
	}
}