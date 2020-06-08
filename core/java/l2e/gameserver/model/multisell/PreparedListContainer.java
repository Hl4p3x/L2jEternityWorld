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

import javolution.util.FastList;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2Armor;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.instance.L2ItemInstance;

public class PreparedListContainer extends ListContainer
{
	private int _npcObjectId = 0;
	
	public PreparedListContainer(ListContainer template, boolean inventoryOnly, L2PcInstance player, L2Npc npc)
	{
		super(template.getListId());
		_maintainEnchantment = template.getMaintainEnchantment();
		
		_applyTaxes = false;
		double taxRate = 0;
		if (npc != null)
		{
			_npcObjectId = npc.getObjectId();
			if (template.getApplyTaxes() && npc.getIsInTown() && (npc.getCastle().getOwnerId() > 0))
			{
				_applyTaxes = true;
				taxRate = npc.getCastle().getTaxRate();
			}
		}
		
		if (inventoryOnly)
		{
			if (player == null)
			{
				return;
			}
			
			final L2ItemInstance[] items;
			if (_maintainEnchantment)
			{
				items = player.getInventory().getUniqueItemsByEnchantLevel(false, false, false);
			}
			else
			{
				items = player.getInventory().getUniqueItems(false, false, false);
			}
			
			_entries = new FastList<>();
			for (L2ItemInstance item : items)
			{
				if (!item.isEquipped() && ((item.getItem() instanceof L2Armor) || (item.getItem() instanceof L2Weapon)))
				{
					for (Entry ent : template.getEntries())
					{
						for (Ingredient ing : ent.getIngredients())
						{
							if (item.getId() == ing.getItemId())
							{
								_entries.add(new PreparedEntry(ent, item, _applyTaxes, _maintainEnchantment, taxRate));
								break;
							}
						}
					}
				}
			}
		}
		else
		{
			_entries = new ArrayList<>(template.getEntries().size());
			for (Entry ent : template.getEntries())
			{
				_entries.add(new PreparedEntry(ent, null, _applyTaxes, false, taxRate));
			}
		}
	}
	
	public final boolean checkNpcObjectId(int npcObjectId)
	{
		return _npcObjectId != 0 ? _npcObjectId == npcObjectId : true;
	}
}