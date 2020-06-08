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
package l2e.gameserver.model.conditions;

import java.util.ArrayList;

import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetNpcId extends Condition
{
	private final ArrayList<Integer> _npcIds;
	
	public ConditionTargetNpcId(ArrayList<Integer> npcIds)
	{
		_npcIds = npcIds;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if ((env.getTarget() != null) && env.getTarget().isNpc())
		{
			return _npcIds.contains(((L2Npc) env.getTarget()).getId());
		}
		
		if ((env.getTarget() != null) && env.getTarget().isDoor())
		{
			return _npcIds.contains(((L2DoorInstance) env.getTarget()).getDoorId());
		}
		return false;
	}
}