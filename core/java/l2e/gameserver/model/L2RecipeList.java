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

public class L2RecipeList
{
	private L2RecipeInstance[] _recipes;
	private L2RecipeStatInstance[] _statUse;
	private L2RecipeStatInstance[] _altStatChange;
	private int _id;
	private int _level;
	private int _recipeId;
	private String _recipeName;
	private int _successRate;
	private int _itemId;
	private int _count;
	private int _rareItemId;
	private int _rareCount;
	private int _rarity;
	private boolean _isDwarvenRecipe;
	
	public L2RecipeList(StatsSet set, boolean haveRare)
	{
		_recipes = new L2RecipeInstance[0];
		_statUse = new L2RecipeStatInstance[0];
		_altStatChange = new L2RecipeStatInstance[0];
		_id = set.getInteger("id");
		_level = set.getInteger("craftLevel");
		_recipeId = set.getInteger("recipeId");
		_recipeName = set.getString("recipeName");
		_successRate = set.getInteger("successRate");
		_itemId = set.getInteger("itemId");
		_count = set.getInteger("count");
		if (haveRare)
		{
			_rareItemId = set.getInteger("rareItemId");
			_rareCount = set.getInteger("rareCount");
			_rarity = set.getInteger("rarity");
		}
		_isDwarvenRecipe = set.getBool("isDwarvenRecipe");
	}
	
	public void addRecipe(L2RecipeInstance recipe)
	{
		int len = _recipes.length;
		L2RecipeInstance[] tmp = new L2RecipeInstance[len+1];
		System.arraycopy(_recipes, 0, tmp, 0, len);
		tmp[len] = recipe;
		_recipes = tmp;
	}
	
	public void addStatUse(L2RecipeStatInstance statUse)
	{
		int len = _statUse.length;
		L2RecipeStatInstance[] tmp = new L2RecipeStatInstance[len+1];
		System.arraycopy(_statUse, 0, tmp, 0, len);
		tmp[len] = statUse;
		_statUse = tmp;
	}
	
	public void addAltStatChange(L2RecipeStatInstance statChange)
	{
		int len = _altStatChange.length;
		L2RecipeStatInstance[] tmp = new L2RecipeStatInstance[len+1];
		System.arraycopy(_altStatChange, 0, tmp, 0, len);
		tmp[len] = statChange;
		_altStatChange = tmp;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getRecipeId()
	{
		return _recipeId;
	}
	
	public String getRecipeName()
	{
		return _recipeName;
	}
	
	public int getSuccessRate()
	{
		return _successRate;
	}
	
	public boolean isConsumable()
	{
		return ((_itemId >= 1463 && _itemId <= 1467) // Soulshots
				|| (_itemId >= 2509 && _itemId <= 2514)
				|| (_itemId >= 3947 && _itemId <= 3952)
				|| (_itemId >= 1341 && _itemId <= 1345)
		);
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public int getRareItemId()
	{
		return _rareItemId;
	}
	
	public int getRareCount()
	{
		return _rareCount;
	}
	
	public int getRarity()
	{
		return _rarity;
	}
	
	public boolean isDwarvenRecipe()
	{
		return _isDwarvenRecipe;
	}
	
	public L2RecipeInstance[] getRecipes()
	{
		return _recipes;
	}
	
	public L2RecipeStatInstance[] getStatUse()
	{
		return _statUse;
	}
	
	public L2RecipeStatInstance[] getAltStatChange()
	{
		return _altStatChange;
	}
}