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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.model.holders.RangeChanceHolder;

public final class EnchantItemGroup
{
	private static final Logger _log = Logger.getLogger(EnchantItemGroup.class.getName());
	private final List<RangeChanceHolder> _chances = new ArrayList<>();
	private final String _name;
	
	public EnchantItemGroup(String name)
	{
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void addChance(RangeChanceHolder holder)
	{
		_chances.add(holder);
	}

	public double getChance(int index)
	{
		if (!_chances.isEmpty())
		{
			for (RangeChanceHolder holder : _chances)
			{
				if ((holder.getMin() <= index) && (holder.getMax() >= index))
				{
					return holder.getChance();
				}
			}
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't match proper chance for item group: " + _name, new IllegalStateException());
			return _chances.get(_chances.size() - 1).getChance();
		}
		_log.log(Level.WARNING, getClass().getSimpleName() + ": item group: " + _name + " doesn't have any chances!");
		return -1;
	}
}