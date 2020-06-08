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

import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerInstanceId extends Condition
{
	private final ArrayList<Integer> _instanceIds;

	public ConditionPlayerInstanceId(ArrayList<Integer> instanceIds)
	{
		_instanceIds = instanceIds;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
		{
			return false;
		}
		
		final int instanceId = env.getCharacter().getInstanceId();
		if (instanceId <= 0)
		{
			return false;
		}
		
		final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(env.getPlayer());
		if ((world == null) || (world.instanceId != instanceId))
		{
			return false;
		}
		return _instanceIds.contains(world.templateId);
	}
}