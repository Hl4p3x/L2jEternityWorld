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

import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerHasPet extends Condition
{
	private final ArrayList<Integer> _controlItemIds;
	
	public ConditionPlayerHasPet(ArrayList<Integer> itemIds)
	{
		if ((itemIds.size() == 1) && (itemIds.get(0) == 0))
		{
			_controlItemIds = null;
		}
		else
		{
			_controlItemIds = itemIds;
		}
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if ((env.getPlayer() == null) || (!(env.getPlayer().getSummon() instanceof L2PetInstance)))
		{
			return false;
		}
		
		if (_controlItemIds == null)
		{
			return true;
		}
		
		final L2ItemInstance controlItem = ((L2PetInstance) env.getPlayer().getSummon()).getControlItem();
		return (controlItem != null) && _controlItemIds.contains(controlItem.getId());
	}
}