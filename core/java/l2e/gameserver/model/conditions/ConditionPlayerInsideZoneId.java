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

import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.zone.L2ZoneType;

public class ConditionPlayerInsideZoneId extends Condition
{
	private final ArrayList<Integer> _zones;
	
	public ConditionPlayerInsideZoneId(ArrayList<Integer> zones)
	{
		_zones = zones;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
		{
			return false;
		}
		
		for (L2ZoneType zone : ZoneManager.getInstance().getZones(env.getCharacter()))
		{
			if (_zones.contains(zone.getId()))
			{
				return true;
			}
		}
		return false;
	}
}