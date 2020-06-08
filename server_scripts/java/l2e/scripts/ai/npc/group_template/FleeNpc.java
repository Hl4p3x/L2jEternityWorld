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

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.scripts.ai.npc.AbstractNpcAI;

public class FleeNpc extends AbstractNpcAI
{
	private static final int[] MOBS =
	{
		20432,
		22228,
		18150,
		18151,
		18152,
		18153,
		18154,
		18155,
		18156,
		18157
	};
	
	private FleeNpc(String name, String descr)
	{
		super(name, descr);
		
		addAttackId(MOBS);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if ((npc.getId() >= 18150) && (npc.getId() <= 18157))
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location((npc.getX() + getRandom(-40, 40)), (npc.getY() + getRandom(-40, 40)), npc.getZ(), npc.getHeading()));
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return null;
		}
		else if ((npc.getId() == 20432) || (npc.getId() == 22228))
		{
			if (getRandom(3) == 2)
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location((npc.getX() + getRandom(-200, 200)), (npc.getY() + getRandom(-200, 200)), npc.getZ(), npc.getHeading()));
			}
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return null;
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	public static void main(String[] args)
	{
		new FleeNpc(FleeNpc.class.getSimpleName(), "ai");
	}
}