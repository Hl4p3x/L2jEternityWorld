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

import java.util.ArrayList;
import java.util.List;

public class Entry
{
	protected int _entryId;
	protected boolean _stackable = true;
	
	protected List<Ingredient> _products;
	protected List<Ingredient> _ingredients;
	
	public Entry(int entryId)
	{
		_entryId = entryId;
		_products = new ArrayList<>();
		_ingredients = new ArrayList<>();
	}
	
	protected Entry()
	{
	}
	
	public final void setEntryId(int id)
	{
		_entryId = id;
	}
	
	public final int getEntryId()
	{
		return _entryId;
	}
	
	public final void addProduct(Ingredient product)
	{
		_products.add(product);
		
		if (!product.isStackable())
			_stackable = false;
	}
	
	public final List<Ingredient> getProducts()
	{
		return _products;
	}
	
	public final void addIngredient(Ingredient ingredient)
	{
		_ingredients.add(ingredient);
	}
	
	public final List<Ingredient> getIngredients()
	{
		return _ingredients;
	}
	
	public final boolean isStackable()
	{
		return _stackable;
	}
	
	public long getTaxAmount()
	{
		return 0;
	}
}