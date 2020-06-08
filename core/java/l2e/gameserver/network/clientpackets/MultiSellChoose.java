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
package l2e.gameserver.network.clientpackets;

import java.util.ArrayList;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.data.xml.MultiSellParser;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.L2Augmentation;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.multisell.Entry;
import l2e.gameserver.model.multisell.Ingredient;
import l2e.gameserver.model.multisell.PreparedListContainer;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class MultiSellChoose extends L2GameClientPacket
{
	private int _listId;
	private int _entryId;
	private long _amount;
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		_entryId = readD();
		_amount = readQ();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getMultiSell().tryPerformAction("multisell choose"))
		{
			player.setMultiSell(null);
			return;
		}
		
		if ((_amount < 1) || (_amount > 5000))
		{
			player.setMultiSell(null);
			return;
		}
		
		PreparedListContainer list = player.getMultiSell();
		if ((list == null) || (list.getListId() != _listId))
		{
			player.setMultiSell(null);
			return;
		}
		
		L2Npc target = player.getLastFolkNPC();
		
		for (Entry entry : list.getEntries())
		{
			if (entry.getEntryId() == _entryId)
			{
				if (!entry.isStackable() && (_amount > 1))
				{
					_log.severe("Character: " + player.getName() + " is trying to set amount > 1 on non-stackable multisell, id:" + _listId + ":" + _entryId);
					player.setMultiSell(null);
					return;
				}
				
				final PcInventory inv = player.getInventory();
				
				int slots = 0;
				int weight = 0;
				for (Ingredient e : entry.getProducts())
				{
					if (e.getItemId() < 0)
					{
						continue;
					}
					
					if (!e.isStackable())
					{
						slots += e.getItemCount() * _amount;
					}
					else if (player.getInventory().getItemByItemId(e.getItemId()) == null)
					{
						slots++;
					}
					weight += e.getItemCount() * _amount * e.getWeight();
				}
				
				if (!inv.validateWeight(weight))
				{
					player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
					return;
				}
				
				if (!inv.validateCapacity(slots))
				{
					player.sendPacket(SystemMessageId.SLOTS_FULL);
					return;
				}
				
				ArrayList<Ingredient> ingredientsList = new ArrayList<>(entry.getIngredients().size());
				
				boolean newIng;
				for (Ingredient e : entry.getIngredients())
				{
					newIng = true;
					for (int i = ingredientsList.size(); --i >= 0;)
					{
						Ingredient ex = ingredientsList.get(i);
						if ((ex.getItemId() == e.getItemId()) && (ex.getEnchantLevel() == e.getEnchantLevel()))
						{
							if ((ex.getItemCount() + e.getItemCount()) > Integer.MAX_VALUE)
							{
								player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
								return;
							}
							final Ingredient ing = ex.getCopy();
							ing.setItemCount(ex.getItemCount() + e.getItemCount());
							ingredientsList.set(i, ing);
							newIng = false;
							break;
						}
					}
					if (newIng)
					{
						ingredientsList.add(e);
					}
				}
				
				for (Ingredient e : ingredientsList)
				{
					if ((e.getItemCount() * _amount) > Integer.MAX_VALUE)
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
						return;
					}
					if (e.getItemId() < 0)
					{
						if (!MultiSellParser.checkSpecialIngredient(e.getItemId(), e.getItemCount() * _amount, player))
						{
							return;
						}
					}
					else
					{
						final long required = ((Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMaintainIngredient()) ? (e.getItemCount() * _amount) : e.getItemCount());
						if (inv.getInventoryItemCount(e.getItemId(), list.getMaintainEnchantment() ? e.getEnchantLevel() : -1, false) < required)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_UNIT_OF_THE_ITEM_S1_REQUIRED);
							sm.addItemName(e.getTemplate());
							sm.addNumber((int) required);
							player.sendPacket(sm);
							return;
						}
					}
				}
				
				FastList<L2Augmentation> augmentation = FastList.newInstance();
				Elementals[] elemental = null;
				try
				{
					for (Ingredient e : entry.getIngredients())
					{
						if (e.getItemId() < 0)
						{
							if (!MultiSellParser.getSpecialIngredient(e.getItemId(), e.getItemCount() * _amount, player))
							{
								return;
							}
						}
						else
						{
							L2ItemInstance itemToTake = inv.getItemByItemId(e.getItemId()); // initialize and initial guess for the item to take.
							if (itemToTake == null)
							{
								_log.severe("Character: " + player.getName() + " is trying to cheat in multisell, id:" + _listId + ":" + _entryId);
								player.setMultiSell(null);
								return;
							}
							
							if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMaintainIngredient())
							{
								if (itemToTake.isStackable())
								{
									if (!player.destroyItem("Multisell", itemToTake.getObjectId(), (e.getItemCount() * _amount), player.getTarget(), true))
									{
										player.setMultiSell(null);
										return;
									}
								}
								else
								{
									if (list.getMaintainEnchantment())
									{
										L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), e.getEnchantLevel(), false);
										for (int i = 0; i < (e.getItemCount() * _amount); i++)
										{
											if (inventoryContents[i].isAugmented())
											{
												augmentation.add(inventoryContents[i].getAugmentation());
											}
											if (inventoryContents[i].getElementals() != null)
											{
												elemental = inventoryContents[i].getElementals();
											}
											if (!player.destroyItem("Multisell", inventoryContents[i].getObjectId(), 1, player.getTarget(), true))
											{
												player.setMultiSell(null);
												return;
											}
										}
									}
									else
									{
										for (int i = 1; i <= (e.getItemCount() * _amount); i++)
										{
											L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), false);
											
											itemToTake = inventoryContents[0];
											
											if (itemToTake.getEnchantLevel() > 0)
											{
												for (L2ItemInstance item : inventoryContents)
												{
													if (item.getEnchantLevel() < itemToTake.getEnchantLevel())
													{
														itemToTake = item;
														if (itemToTake.getEnchantLevel() == 0)
														{
															break;
														}
													}
												}
											}
											if (!player.destroyItem("Multisell", itemToTake.getObjectId(), 1, player.getTarget(), true))
											{
												player.setMultiSell(null);
												return;
											}
										}
									}
								}
							}
						}
					}
					
					for (Ingredient e : entry.getProducts())
					{
						if (e.getItemId() < 0)
						{
							MultiSellParser.addSpecialProduct(e.getItemId(), e.getItemCount() * _amount, player);
						}
						else
						{
							if (e.isStackable())
							{
								inv.addItem("Multisell", e.getItemId(), e.getItemCount() * _amount, player, player.getTarget());
							}
							else
							{
								L2ItemInstance product = null;
								for (int i = 0; i < (e.getItemCount() * _amount); i++)
								{
									product = inv.addItem("Multisell", e.getItemId(), 1, player, player.getTarget());
									if ((product != null) && list.getMaintainEnchantment())
									{
										if (i < augmentation.size())
										{
											product.setAugmentation(new L2Augmentation(augmentation.get(i).getAugmentationId()));
										}
										if (elemental != null)
										{
											for (Elementals elm : elemental)
											{
												product.setElementAttr(elm.getElement(), elm.getValue());
											}
										}
										product.setEnchantLevel(e.getEnchantLevel());
										product.updateDatabase();
									}
								}
							}
							SystemMessage sm;
							
							if ((e.getItemCount() * _amount) > 1)
							{
								sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
								sm.addItemName(e.getItemId());
								sm.addItemNumber(e.getItemCount() * _amount);
								player.sendPacket(sm);
								sm = null;
							}
							else
							{
								if (list.getMaintainEnchantment() && (e.getEnchantLevel() > 0))
								{
									sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2);
									sm.addItemNumber(e.getEnchantLevel());
									sm.addItemName(e.getItemId());
								}
								else
								{
									sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
									sm.addItemName(e.getItemId());
								}
								player.sendPacket(sm);
								sm = null;
							}
						}
					}
					player.sendPacket(new ItemList(player, false));
					
					StatusUpdate su = new StatusUpdate(player);
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
					su = null;
				}
				finally
				{
					FastList.recycle(augmentation);
				}
				
				if (entry.getTaxAmount() > 0)
				{
					target.getCastle().addToTreasury(entry.getTaxAmount() * _amount);
				}
				
				break;
			}
		}
	}
}