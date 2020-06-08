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
package l2e.gameserver.handler.itemhandlers;

import l2e.gameserver.data.xml.RecipeParser;
import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.model.L2RecipeList;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Recipes implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		
		if (activeChar.isInCraftMode())
		{
			activeChar.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
			return false;
		}
		
		final L2RecipeList rp = RecipeParser.getInstance().getRecipeByItemId(item.getId());
		if (rp == null)
		{
			return false;
		}
		
		if (activeChar.hasRecipeList(rp.getId()))
		{
			activeChar.sendPacket(SystemMessageId.RECIPE_ALREADY_REGISTERED);
			return false;
		}
		
		boolean canCraft = false;
		boolean recipeLevel = false;
		boolean recipeLimit = false;
		if (rp.isDwarvenRecipe())
		{
			canCraft = activeChar.hasDwarvenCraft();
			recipeLevel = (rp.getLevel() > activeChar.getDwarvenCraft());
			recipeLimit = (activeChar.getDwarvenRecipeBook().length >= activeChar.getDwarfRecipeLimit());
		}
		else
		{
			canCraft = activeChar.hasCommonCraft();
			recipeLevel = (rp.getLevel() > activeChar.getCommonCraft());
			recipeLimit = (activeChar.getCommonRecipeBook().length >= activeChar.getCommonRecipeLimit());
		}
		
		if (!canCraft)
		{
			activeChar.sendPacket(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
			return false;
		}
		
		if (recipeLevel)
		{
			activeChar.sendPacket(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER);
			return false;
		}
		
		if (recipeLimit)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
			sm.addNumber(rp.isDwarvenRecipe() ? activeChar.getDwarfRecipeLimit() : activeChar.getCommonRecipeLimit());
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (rp.isDwarvenRecipe())
		{
			activeChar.registerDwarvenRecipeList(rp, true);
		}
		else
		{
			activeChar.registerCommonRecipeList(rp, true);
		}
		
		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED);
		sm.addItemName(item);
		activeChar.sendPacket(sm);
		return true;
	}
}