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
package l2e.gameserver.model.entity.mods.conditions;

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.mods.base.Condition;

public class Castle extends Condition
{
	public Castle(Object value)
	{
		super(value);
		setName("Have Castle");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
		{
			return false;
		}
		
		if (player.getClan() != null)
		{
			if ((player.isCastleLord(5)) || (player.isCastleLord(3)) || (player.isCastleLord(7)))
			{
				return true;
			}
		}
		return false;
	}
}