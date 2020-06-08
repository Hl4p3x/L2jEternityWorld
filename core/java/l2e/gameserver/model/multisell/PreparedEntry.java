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
package l2e.gameserver.model.multisell;

import static l2e.gameserver.model.itemcontainer.PcInventory.ADENA_ID;

import java.util.ArrayList;

import l2e.gameserver.model.items.instance.L2ItemInstance;

public class PreparedEntry extends Entry
{
	private long _taxAmount = 0;
	
	public PreparedEntry(Entry template, L2ItemInstance item, boolean applyTaxes, boolean maintainEnchantment, double taxRate)
	{
		_entryId = template.getEntryId() * 100000;
		if (maintainEnchantment && (item != null))
		{
			_entryId += item.getEnchantLevel();
		}
		
		ItemInfo info = null;
		long adenaAmount = 0;
		
		_ingredients = new ArrayList<>(template.getIngredients().size());
		for (Ingredient ing : template.getIngredients())
		{
			if (ing.getItemId() == ADENA_ID)
			{
				if (ing.isTaxIngredient())
				{
					if (applyTaxes)
					{
						_taxAmount += Math.round(ing.getItemCount() * taxRate);
					}
				}
				else
				{
					adenaAmount += ing.getItemCount();
				}
				
				continue;
			}
			else if (maintainEnchantment && (item != null) && ing.isArmorOrWeapon())
			{
				info = new ItemInfo(item);
				final Ingredient newIngredient = ing.getCopy();
				newIngredient.setItemInfo(info);
				_ingredients.add(newIngredient);
			}
			else
			{
				final Ingredient newIngredient = ing.getCopy();
				_ingredients.add(newIngredient);
			}
			
		}
		adenaAmount += _taxAmount;
		if (adenaAmount > 0)
		{
			_ingredients.add(new Ingredient(ADENA_ID, adenaAmount, false, false));
		}
		
		_products = new ArrayList<>(template.getProducts().size());
		for (Ingredient ing : template.getProducts())
		{
			if (!ing.isStackable())
			{
				_stackable = false;
			}
			
			final Ingredient newProduct = ing.getCopy();
			if (maintainEnchantment && ing.isArmorOrWeapon())
			{
				newProduct.setItemInfo(info);
			}
			_products.add(newProduct);
		}
	}
	
	@Override
	public final long getTaxAmount()
	{
		return _taxAmount;
	}
}