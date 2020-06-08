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
package l2e.gameserver.model;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.util.Util;
import l2e.util.Rnd;

public class L2DropCategory
{
	private final FastList<L2DropData> _drops;
	private int _categoryChance;
	private int _categoryBalancedChance;
	private final int _categoryType;
	
	public L2DropCategory(int categoryType)
	{
		_categoryType = categoryType;
		_drops = new FastList<>(0);
		_categoryChance = 0;
		_categoryBalancedChance = 0;
	}
	
	public void addDropData(L2DropData drop, boolean raid)
	{
		boolean found = false;
		
		if (drop.isQuestDrop())
		{
		}
		else
		{
			if (Config.CUSTOM_DROPLIST_TABLE)
			{
				for (L2DropData d : _drops)
				{
					if (d.getItemId() == drop.getItemId())
					{
						d.setMinDrop(drop.getMinDrop());
						d.setMaxDrop(drop.getMaxDrop());
						if (Math.abs(d.getChance() - drop.getChance()) >= 1e-6)
						{
							_categoryChance -= d.getChance();
							_categoryBalancedChance -= Math.min((d.getChance() * Config.RATE_DROP_ITEMS), L2DropData.MAX_CHANCE);
							d.setChance(drop.getChance());
							_categoryChance += d.getChance();
							_categoryBalancedChance += Math.min((d.getChance() * Config.RATE_DROP_ITEMS), L2DropData.MAX_CHANCE);
						}
						found = true;
						break;
					}
				}
			}
			
			if (!found)
			{
				_drops.add(drop);
				_categoryChance += drop.getChance();
				_categoryBalancedChance += Math.min((drop.getChance() * (raid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS)), L2DropData.MAX_CHANCE);
			}
		}
	}
	
	public FastList<L2DropData> getAllDrops()
	{
		return _drops;
	}
	
	public void clearAllDrops()
	{
		_drops.clear();
	}
	
	public boolean isSweep()
	{
		return (getCategoryType() == -1);
	}
	
	public int getCategoryChance()
	{
		if (getCategoryType() >= 0)
		{
			return _categoryChance;
		}
		return L2DropData.MAX_CHANCE;
	}
	
	public int getCategoryBalancedChance()
	{
		if (getCategoryType() >= 0)
		{
			return _categoryBalancedChance;
		}
		return L2DropData.MAX_CHANCE;
	}
	
	public int getCategoryType()
	{
		return _categoryType;
	}
	
	public synchronized L2DropData dropSeedAllowedDropsOnly()
	{
		FastList<L2DropData> drops = new FastList<>();
		int subCatChance = 0;
		for (L2DropData drop : getAllDrops())
		{
			if ((drop.getItemId() == PcInventory.ADENA_ID) || Util.contains(SevenSigns.SEAL_STONE_IDS, drop.getItemId()))
			{
				drops.add(drop);
				subCatChance += drop.getChance();
			}
		}
		
		int randomIndex = Rnd.get(subCatChance);
		int sum = 0;
		for (L2DropData drop : drops)
		{
			sum += drop.getChance();
			
			if (sum > randomIndex)
			{
				drops.clear();
				drops = null;
				return drop;
			}
		}
		return null;
	}
	
	public synchronized L2DropData dropOne(boolean raid)
	{
		int randomIndex = Rnd.get(getCategoryBalancedChance());
		int sum = 0;
		for (L2DropData drop : getAllDrops())
		{
			sum += Math.min((drop.getChance() * (raid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS)), L2DropData.MAX_CHANCE);
			
			if (sum >= randomIndex)
			{
				return drop;
			}
		}
		return null;
	}
}