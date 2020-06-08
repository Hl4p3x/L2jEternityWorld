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

import java.util.logging.Level;

import l2e.Config;
import l2e.gameserver.data.xml.EnchantItemGroupsParser;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.util.Rnd;

public final class EnchantScroll extends EnchantItem
{
	private final boolean _isBlessed;
	private final boolean _isSafe;
	private final int _scrollGroupId;
	
	public EnchantScroll(StatsSet set)
	{
		super(set);
		
		_isBlessed = set.getBool("isBlessed", false);
		_isSafe = set.getBool("isSafe", false);
		_scrollGroupId = set.getInteger("scrollGroupId", 0);
	}
	
	public boolean isBlessed()
	{
		return _isBlessed;
	}
	
	public boolean isSafe()
	{
		return _isSafe;
	}
	
	public int getScrollGroupId()
	{
		return _scrollGroupId;
	}
	
	public boolean isValid(L2ItemInstance enchantItem, EnchantItem supportItem)
	{
		if ((supportItem != null) && (!supportItem.isValid(enchantItem) || isBlessed()))
		{
			return false;
		}
		
		return super.isValid(enchantItem);
	}
	
	public EnchantResultType calculateSuccess(L2PcInstance player, L2ItemInstance enchantItem, EnchantItem supportItem)
	{
		if (!isValid(enchantItem, supportItem))
		{
			return EnchantResultType.ERROR;
		}
		
		if (EnchantItemGroupsParser.getInstance().getScrollGroup(_scrollGroupId) == null)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Unexistent enchant scroll group specified for enchant scroll: " + getId());
			return EnchantResultType.ERROR;
		}
		
		final EnchantItemGroup group = EnchantItemGroupsParser.getInstance().getItemGroup(enchantItem.getItem(), _scrollGroupId);
		if (group == null)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't find enchant item group for scroll: " + getId() + " requested by: " + player);
			return EnchantResultType.ERROR;
		}
		
		double finalChance;
		
		final double chance = group.getChance(enchantItem.getEnchantLevel());
		final double bonusRate = getBonusRate();
		final double supportBonusRate = ((supportItem != null) && !_isBlessed) ? supportItem.getBonusRate() : 0;
		
		if (Config.CUSTOM_ENCHANT_ITEMS_ENABLED)
		{
			if (Config.ENCHANT_ITEMS_ID.containsKey(enchantItem.getItem().getId()))
			{
				if (enchantItem.getEnchantLevel() < 3)
				{
					finalChance = 100 + bonusRate + supportBonusRate;
				}
				else
				{
					finalChance = Config.ENCHANT_ITEMS_ID.get(enchantItem.getItem().getId()) + bonusRate + supportBonusRate;
				}
			}
			else
			{
				finalChance = chance + bonusRate + supportBonusRate;
			}
		}
		else
		{
			finalChance = chance + bonusRate + supportBonusRate;
		}
		
		final double random = 100 * Rnd.nextDouble();
		final boolean success = (random < finalChance);
		
		return success ? EnchantResultType.SUCCESS : EnchantResultType.FAILURE;
	}
}