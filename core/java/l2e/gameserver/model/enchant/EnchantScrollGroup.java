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
package l2e.gameserver.model.enchant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2e.gameserver.model.items.L2Item;

public final class EnchantScrollGroup
{
	private final int _id;
	private List<EnchantRateItem> _rateGroups;
	
	public EnchantScrollGroup(int id)
	{
		_id = id;
	}
	
	public int getId()
	{
		return _id;
	}

	public void addRateGroup(EnchantRateItem group)
	{
		if (_rateGroups == null)
		{
			_rateGroups = new ArrayList<>();
		}
		_rateGroups.add(group);
	}

	public List<EnchantRateItem> getRateGroups()
	{
		return _rateGroups != null ? _rateGroups : Collections.<EnchantRateItem> emptyList();
	}

	public EnchantRateItem getRateGroup(L2Item item)
	{
		for (EnchantRateItem group : getRateGroups())
		{
			if (group.validate(item))
			{
				return group;
			}
		}
		return null;
	}
}