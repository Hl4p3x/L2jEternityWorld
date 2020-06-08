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

import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.npc.AbstractNpcAI;

/**
 * Based on L2J Eternity-World
 */
public class Sandstorm extends AbstractNpcAI
{
	private static final int SANDSTORM = 32350;

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		npc.setTarget(player);
		npc.doCast(SkillHolder.getInstance().getInfo(5435, 1));
		return super.onAggroRangeEnter(npc, player, isSummon);
	}

	public Sandstorm(String name, String descr)
	{
		super(name, descr);

		addAggroRangeEnterId(SANDSTORM);
	}

	public static void main(String[] args)
	{
		new Sandstorm(Sandstorm.class.getSimpleName(), "ai");
	}
}
