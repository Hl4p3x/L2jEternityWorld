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
package l2e.gameserver.data.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.L2RecipeInstance;
import l2e.gameserver.model.L2RecipeList;
import l2e.gameserver.model.L2RecipeStatInstance;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class RecipeParser extends DocumentParser
{
	private static final Map<Integer, L2RecipeList> _recipes = new HashMap<>();
	
	protected RecipeParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_recipes.clear();
		parseDatapackFile("data/recipes.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _recipes.size() + " recipes.");
	}
	
	@Override
	protected void parseDocument()
	{
		final List<L2RecipeInstance> recipePartList = new ArrayList<>();
		final List<L2RecipeStatInstance> recipeStatUseList = new ArrayList<>();
		final List<L2RecipeStatInstance> recipeAltStatChangeList = new ArrayList<>();
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				recipesFile:
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						recipePartList.clear();
						recipeStatUseList.clear();
						recipeAltStatChangeList.clear();
						NamedNodeMap attrs = d.getAttributes();
						Node att;
						int id = -1;
						boolean haveRare = false;
						StatsSet set = new StatsSet();
						
						att = attrs.getNamedItem("id");
						if (att == null)
						{
							_log.severe(getClass().getSimpleName() + ": Missing id for recipe item, skipping");
							continue;
						}
						id = Integer.parseInt(att.getNodeValue());
						set.set("id", id);
						
						att = attrs.getNamedItem("recipeId");
						if (att == null)
						{
							_log.severe(getClass().getSimpleName() + ": Missing recipeId for recipe item id: " + id + ", skipping");
							continue;
						}
						set.set("recipeId", Integer.parseInt(att.getNodeValue()));
						
						att = attrs.getNamedItem("name");
						if (att == null)
						{
							_log.severe(getClass().getSimpleName() + ": Missing name for recipe item id: " + id + ", skipping");
							continue;
						}
						set.set("recipeName", att.getNodeValue());
						
						att = attrs.getNamedItem("craftLevel");
						if (att == null)
						{
							_log.severe(getClass().getSimpleName() + ": Missing level for recipe item id: " + id + ", skipping");
							continue;
						}
						set.set("craftLevel", Integer.parseInt(att.getNodeValue()));
						
						att = attrs.getNamedItem("type");
						if (att == null)
						{
							_log.severe(getClass().getSimpleName() + ": Missing type for recipe item id: " + id + ", skipping");
							continue;
						}
						set.set("isDwarvenRecipe", att.getNodeValue().equalsIgnoreCase("dwarven"));
						
						att = attrs.getNamedItem("successRate");
						if (att == null)
						{
							_log.severe(getClass().getSimpleName() + ": Missing successRate for recipe item id: " + id + ", skipping");
							continue;
						}
						set.set("successRate", Integer.parseInt(att.getNodeValue()));
						
						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if ("statUse".equalsIgnoreCase(c.getNodeName()))
							{
								String statName = c.getAttributes().getNamedItem("name").getNodeValue();
								int value = Integer.parseInt(c.getAttributes().getNamedItem("value").getNodeValue());
								try
								{
									recipeStatUseList.add(new L2RecipeStatInstance(statName, value));
								}
								catch (Exception e)
								{
									_log.severe(getClass().getSimpleName() + ": Error in StatUse parameter for recipe item id: " + id + ", skipping");
									continue recipesFile;
								}
							}
							else if ("altStatChange".equalsIgnoreCase(c.getNodeName()))
							{
								String statName = c.getAttributes().getNamedItem("name").getNodeValue();
								int value = Integer.parseInt(c.getAttributes().getNamedItem("value").getNodeValue());
								try
								{
									recipeAltStatChangeList.add(new L2RecipeStatInstance(statName, value));
								}
								catch (Exception e)
								{
									_log.severe(getClass().getSimpleName() + ": Error in AltStatChange parameter for recipe item id: " + id + ", skipping");
									continue recipesFile;
								}
							}
							else if ("ingredient".equalsIgnoreCase(c.getNodeName()))
							{
								int ingId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
								int ingCount = Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue());
								recipePartList.add(new L2RecipeInstance(ingId, ingCount));
							}
							else if ("production".equalsIgnoreCase(c.getNodeName()))
							{
								set.set("itemId", Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue()));
								set.set("count", Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue()));
							}
							else if ("productionRare".equalsIgnoreCase(c.getNodeName()))
							{
								set.set("rareItemId", Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue()));
								set.set("rareCount", Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue()));
								set.set("rarity", Integer.parseInt(c.getAttributes().getNamedItem("rarity").getNodeValue()));
								haveRare = true;
							}
						}
						
						L2RecipeList recipeList = new L2RecipeList(set, haveRare);
						for (L2RecipeInstance recipePart : recipePartList)
						{
							recipeList.addRecipe(recipePart);
						}
						for (L2RecipeStatInstance recipeStatUse : recipeStatUseList)
						{
							recipeList.addStatUse(recipeStatUse);
						}
						for (L2RecipeStatInstance recipeAltStatChange : recipeAltStatChangeList)
						{
							recipeList.addAltStatChange(recipeAltStatChange);
						}
						
						_recipes.put(id, recipeList);
					}
				}
			}
		}
	}
	
	public L2RecipeList getRecipeList(int listId)
	{
		return _recipes.get(listId);
	}
	
	public L2RecipeList getRecipeByItemId(int itemId)
	{
		for (L2RecipeList find : _recipes.values())
		{
			if (find.getRecipeId() == itemId)
			{
				return find;
			}
		}
		return null;
	}
	
	public int[] getAllItemIds()
	{
		int[] idList = new int[_recipes.size()];
		int i = 0;
		for (L2RecipeList rec : _recipes.values())
		{
			idList[i++] = rec.getRecipeId();
		}
		return idList;
	}
	
	public L2RecipeList getValidRecipeList(L2PcInstance player, int id)
	{
		L2RecipeList recipeList = _recipes.get(id);
		if ((recipeList == null) || (recipeList.getRecipes().length == 0))
		{
			player.sendMessage(getClass().getSimpleName() + ": No recipe for: " + id);
			player.isInCraftMode(false);
			return null;
		}
		return recipeList;
	}
	
	public static RecipeParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final RecipeParser _instance = new RecipeParser();
	}
}