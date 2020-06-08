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
package l2e.gameserver.model.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.model.TradeItem;
import l2e.gameserver.model.TradeList;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance.ItemLocation;
import l2e.gameserver.model.items.type.L2EtcItemType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.scripting.scriptengine.events.AddToInventoryEvent;
import l2e.gameserver.scripting.scriptengine.events.ItemDestroyEvent;
import l2e.gameserver.scripting.scriptengine.events.ItemDropEvent;
import l2e.gameserver.scripting.scriptengine.events.ItemTransferEvent;
import l2e.gameserver.scripting.scriptengine.listeners.player.ItemTracker;
import l2e.gameserver.util.Util;

public class PcInventory extends Inventory
{
	private static final Logger _log = Logger.getLogger(PcInventory.class.getName());
	
	public static final int ADENA_ID = 57;
	public static final int ANCIENT_ADENA_ID = 5575;
	public static final long MAX_ADENA = Config.MAX_ADENA;
	
	private final L2PcInstance _owner;
	private L2ItemInstance _adena;
	private L2ItemInstance _ancientAdena;
	
	private int[] _blockItems = null;
	
	private int _questSlots;
	
	private final Object _lock;
	private int _blockMode = -1;
	
	private static FastList<ItemTracker> itemTrackers = new FastList<ItemTracker>().shared();
	
	public PcInventory(L2PcInstance owner)
	{
		_owner = owner;
		_lock = new Object();
	}
	
	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.INVENTORY;
	}
	
	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PAPERDOLL;
	}
	
	public L2ItemInstance getAdenaInstance()
	{
		return _adena;
	}
	
	@Override
	public long getAdena()
	{
		return _adena != null ? _adena.getCount() : 0;
	}
	
	public L2ItemInstance getAncientAdenaInstance()
	{
		return _ancientAdena;
	}
	
	public long getAncientAdena()
	{
		return (_ancientAdena != null) ? _ancientAdena.getCount() : 0;
	}
	
	public L2ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena)
	{
		return getUniqueItems(allowAdena, allowAncientAdena, true);
	}
	
	public L2ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable)
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if ((!allowAdena && (item.getId() == ADENA_ID)))
			{
				continue;
			}
			if ((!allowAncientAdena && (item.getId() == ANCIENT_ADENA_ID)))
			{
				continue;
			}
			
			boolean isDuplicate = false;
			for (L2ItemInstance litem : list)
			{
				if (item == null)
				{
					continue;
				}
				
				if (litem.getId() == item.getId())
				{
					isDuplicate = true;
					break;
				}
			}
			if (!isDuplicate && (!onlyAvailable || ((item != null) && item.isSellable() && item.isAvailable(getOwner(), false, false))))
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	public L2ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena)
	{
		return getUniqueItemsByEnchantLevel(allowAdena, allowAncientAdena, true);
	}
	
	public L2ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable)
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if (item == null)
			{
				continue;
			}
			if ((!allowAdena && (item.getId() == ADENA_ID)))
			{
				continue;
			}
			if ((!allowAncientAdena && (item.getId() == ANCIENT_ADENA_ID)))
			{
				continue;
			}
			
			boolean isDuplicate = false;
			for (L2ItemInstance litem : list)
			{
				if ((litem.getId() == item.getId()) && (litem.getEnchantLevel() == item.getEnchantLevel()))
				{
					isDuplicate = true;
					break;
				}
			}
			if (!isDuplicate && (!onlyAvailable || (item.isSellable() && item.isAvailable(getOwner(), false, false))))
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	public L2ItemInstance[] getAllItemsByItemId(int itemId)
	{
		return getAllItemsByItemId(itemId, true);
	}
	
	public L2ItemInstance[] getAllItemsByItemId(int itemId, boolean includeEquipped)
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if (item == null)
			{
				continue;
			}
			
			if ((item.getId() == itemId) && (includeEquipped || !item.isEquipped()))
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	public L2ItemInstance[] getAllItemsByItemId(int itemId, int enchantment)
	{
		return getAllItemsByItemId(itemId, enchantment, true);
	}
	
	public L2ItemInstance[] getAllItemsByItemId(int itemId, int enchantment, boolean includeEquipped)
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if (item == null)
			{
				continue;
			}
			
			if ((item.getId() == itemId) && (item.getEnchantLevel() == enchantment) && (includeEquipped || !item.isEquipped()))
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	public L2ItemInstance[] getAvailableItems(boolean allowAdena, boolean allowNonTradeable, boolean feightable)
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if ((item == null) || !item.isAvailable(getOwner(), allowAdena, allowNonTradeable) || !canManipulateWithItemId(item.getId()))
			{
				continue;
			}
			else if (feightable)
			{
				if ((item.getItemLocation() == ItemLocation.INVENTORY) && item.isFreightable())
				{
					list.add(item);
				}
			}
			else
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	public L2ItemInstance[] getAugmentedItems()
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if ((item != null) && item.isAugmented())
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	public L2ItemInstance[] getElementItems()
	{
		FastList<L2ItemInstance> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if ((item != null) && (item.getElementals() != null))
			{
				list.add(item);
			}
		}
		
		L2ItemInstance[] result = list.toArray(new L2ItemInstance[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	public TradeItem[] getAvailableItems(TradeList tradeList)
	{
		FastList<TradeItem> list = FastList.newInstance();
		for (L2ItemInstance item : _items)
		{
			if ((item != null) && item.isAvailable(getOwner(), false, false))
			{
				TradeItem adjItem = tradeList.adjustAvailableItem(item);
				if (adjItem != null)
				{
					list.add(adjItem);
				}
			}
		}
		
		TradeItem[] result = list.toArray(new TradeItem[list.size()]);
		FastList.recycle(list);
		
		return result;
	}
	
	public void adjustAvailableItem(TradeItem item)
	{
		boolean notAllEquipped = false;
		for (L2ItemInstance adjItem : getItemsByItemId(item.getItem().getId()))
		{
			if (adjItem.isEquipable())
			{
				if (!adjItem.isEquipped())
				{
					notAllEquipped |= true;
				}
			}
			else
			{
				notAllEquipped |= true;
				break;
			}
		}
		if (notAllEquipped)
		{
			L2ItemInstance adjItem = getItemByItemId(item.getItem().getId());
			item.setObjectId(adjItem.getObjectId());
			item.setEnchant(adjItem.getEnchantLevel());
			
			if (adjItem.getCount() < item.getCount())
			{
				item.setCount(adjItem.getCount());
			}
			
			return;
		}
		
		item.setCount(0);
	}
	
	public void addAdena(String process, long count, L2PcInstance actor, Object reference)
	{
		if (count > 0)
		{
			addItem(process, ADENA_ID, count, actor, reference);
		}
	}
	
	public boolean reduceAdena(String process, long count, L2PcInstance actor, Object reference)
	{
		if (count > 0)
		{
			return destroyItemByItemId(process, ADENA_ID, count, actor, reference) != null;
		}
		return false;
	}
	
	public void addAncientAdena(String process, long count, L2PcInstance actor, Object reference)
	{
		if (count > 0)
		{
			addItem(process, ANCIENT_ADENA_ID, count, actor, reference);
		}
	}
	
	public boolean reduceAncientAdena(String process, long count, L2PcInstance actor, Object reference)
	{
		if (count > 0)
		{
			return destroyItemByItemId(process, ANCIENT_ADENA_ID, count, actor, reference) != null;
		}
		return false;
	}
	
	@Override
	public L2ItemInstance addItem(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		item = super.addItem(process, item, actor, reference);
		
		if ((item != null) && (item.getId() == ADENA_ID) && !item.equals(_adena))
		{
			_adena = item;
		}
		
		if ((item != null) && (item.getId() == ANCIENT_ADENA_ID) && !item.equals(_ancientAdena))
		{
			_ancientAdena = item;
		}
		
		fireTrackerEvents(TrackerEvent.ADD_TO_INVENTORY, actor, item, null);
		return item;
	}
	
	@Override
	public L2ItemInstance addItem(String process, int itemId, long count, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = super.addItem(process, itemId, count, actor, reference);
		
		if ((item != null) && (item.getId() == ADENA_ID) && !item.equals(_adena))
		{
			_adena = item;
		}
		
		if ((item != null) && (item.getId() == ANCIENT_ADENA_ID) && !item.equals(_ancientAdena))
		{
			_ancientAdena = item;
		}
		if ((item != null) && (actor != null))
		{
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate playerIU = new InventoryUpdate();
				playerIU.addItem(item);
				actor.sendPacket(playerIU);
			}
			else
			{
				actor.sendPacket(new ItemList(actor, false));
			}
			
			StatusUpdate su = new StatusUpdate(actor);
			su.addAttribute(StatusUpdate.CUR_LOAD, actor.getCurrentLoad());
			actor.sendPacket(su);
			
			fireTrackerEvents(TrackerEvent.ADD_TO_INVENTORY, actor, item, null);
		}
		return item;
	}
	
	@Override
	public L2ItemInstance transferItem(String process, int objectId, long count, ItemContainer target, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = super.transferItem(process, objectId, count, target, actor, reference);
		
		if ((_adena != null) && ((_adena.getCount() <= 0) || (_adena.getOwnerId() != getOwnerId())))
		{
			_adena = null;
		}
		
		if ((_ancientAdena != null) && ((_ancientAdena.getCount() <= 0) || (_ancientAdena.getOwnerId() != getOwnerId())))
		{
			_ancientAdena = null;
		}
		
		fireTrackerEvents(TrackerEvent.TRANSFER, actor, item, target);
		
		return item;
	}
	
	@Override
	public L2ItemInstance destroyItem(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		return this.destroyItem(process, item, item.getCount(), actor, reference);
	}
	
	@Override
	public L2ItemInstance destroyItem(String process, L2ItemInstance item, long count, L2PcInstance actor, Object reference)
	{
		item = super.destroyItem(process, item, count, actor, reference);
		
		if ((_adena != null) && (_adena.getCount() <= 0))
		{
			_adena = null;
		}
		
		if ((_ancientAdena != null) && (_ancientAdena.getCount() <= 0))
		{
			_ancientAdena = null;
		}
		
		fireTrackerEvents(TrackerEvent.DESTROY, actor, item, null);
		
		return item;
	}
	
	@Override
	public L2ItemInstance destroyItem(String process, int objectId, long count, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
		{
			return null;
		}
		return this.destroyItem(process, item, count, actor, reference);
	}
	
	@Override
	public L2ItemInstance destroyItemByItemId(String process, int itemId, long count, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = getItemByItemId(itemId);
		if (item == null)
		{
			return null;
		}
		return this.destroyItem(process, item, count, actor, reference);
	}
	
	@Override
	public L2ItemInstance dropItem(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		item = super.dropItem(process, item, actor, reference);
		
		if ((_adena != null) && ((_adena.getCount() <= 0) || (_adena.getOwnerId() != getOwnerId())))
		{
			_adena = null;
		}
		
		if ((_ancientAdena != null) && ((_ancientAdena.getCount() <= 0) || (_ancientAdena.getOwnerId() != getOwnerId())))
		{
			_ancientAdena = null;
		}
		
		fireTrackerEvents(TrackerEvent.DROP, actor, item, null);
		
		return item;
	}
	
	@Override
	public L2ItemInstance dropItem(String process, int objectId, long count, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = super.dropItem(process, objectId, count, actor, reference);
		
		if ((_adena != null) && ((_adena.getCount() <= 0) || (_adena.getOwnerId() != getOwnerId())))
		{
			_adena = null;
		}
		
		if ((_ancientAdena != null) && ((_ancientAdena.getCount() <= 0) || (_ancientAdena.getOwnerId() != getOwnerId())))
		{
			_ancientAdena = null;
		}
		
		fireTrackerEvents(TrackerEvent.DROP, actor, item, null);
		
		return item;
	}
	
	@Override
	protected boolean removeItem(L2ItemInstance item)
	{
		getOwner().removeItemFromShortCut(item.getObjectId());
		
		if (item.getObjectId() == getOwner().getActiveEnchantItemId())
		{
			getOwner().setActiveEnchantItemId(L2PcInstance.ID_NONE);
		}
		
		if (item.getId() == ADENA_ID)
		{
			_adena = null;
		}
		else if (item.getId() == ANCIENT_ADENA_ID)
		{
			_ancientAdena = null;
		}
		
		if (item.isQuestItem())
		{
			synchronized (_lock)
			{
				_questSlots--;
				if (_questSlots < 0)
				{
					_questSlots = 0;
					_log.warning(this + ": QuestInventory size < 0!");
				}
			}
		}
		return super.removeItem(item);
	}
	
	@Override
	public void refreshWeight()
	{
		super.refreshWeight();
		getOwner().refreshOverloaded();
	}
	
	@Override
	public void restore()
	{
		super.restore();
		_adena = getItemByItemId(ADENA_ID);
		_ancientAdena = getItemByItemId(ANCIENT_ADENA_ID);
	}
	
	public static int[][] restoreVisibleInventory(int objectId)
	{
		int[][] paperdoll = new int[31][3];
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement2 = con.prepareStatement("SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'"))
		{
			statement2.setInt(1, objectId);
			try (ResultSet invdata = statement2.executeQuery())
			{
				while (invdata.next())
				{
					int slot = invdata.getInt("loc_data");
					paperdoll[slot][0] = invdata.getInt("object_id");
					paperdoll[slot][1] = invdata.getInt("item_id");
					paperdoll[slot][2] = invdata.getInt("enchant_level");
					/*
					 * if (slot == Inventory.PAPERDOLL_RHAND) { paperdoll[Inventory.PAPERDOLL_RHAND][0] = invdata.getInt("object_id"); paperdoll[Inventory.PAPERDOLL_RHAND][1] = invdata.getInt("item_id"); paperdoll[Inventory.PAPERDOLL_RHAND][2] = invdata.getInt("enchant_level"); }
					 */
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore inventory: " + e.getMessage(), e);
		}
		return paperdoll;
	}
	
	public boolean checkInventorySlotsAndWeight(List<L2Item> itemList, boolean sendMessage, boolean sendSkillMessage)
	{
		int lootWeight = 0;
		int requiredSlots = 0;
		if (itemList != null)
		{
			for (L2Item item : itemList)
			{
				if (!item.isStackable() || (getInventoryItemCount(item.getId(), -1) <= 0))
				{
					requiredSlots++;
				}
				lootWeight += item.getWeight();
			}
		}
		
		boolean inventoryStatusOK = validateCapacity(requiredSlots) && validateWeight(lootWeight);
		if (!inventoryStatusOK && sendMessage)
		{
			_owner.sendPacket(SystemMessageId.SLOTS_FULL);
			if (sendSkillMessage)
			{
				_owner.sendPacket(SystemMessageId.WEIGHT_EXCEEDED_SKILL_UNAVAILABLE);
			}
		}
		return inventoryStatusOK;
	}
	
	public boolean validateCapacity(L2ItemInstance item)
	{
		int slots = 0;
		if (!item.isStackable() || (getInventoryItemCount(item.getId(), -1) <= 0) || (item.getItemType() != L2EtcItemType.HERB))
		{
			slots++;
		}
		return validateCapacity(slots, item.isQuestItem());
	}
	
	public boolean validateCapacityByItemId(int itemId)
	{
		int slots = 0;
		final L2ItemInstance invItem = getItemByItemId(itemId);
		if ((invItem == null) || !invItem.isStackable())
		{
			slots++;
		}
		return validateCapacity(slots, ItemHolder.getInstance().getTemplate(itemId).isQuestItem());
	}
	
	@Override
	public boolean validateCapacity(long slots)
	{
		return validateCapacity(slots, false);
	}
	
	public boolean validateCapacity(long slots, boolean questItem)
	{
		if (!questItem)
		{
			return (((_items.size() - _questSlots) + slots) <= _owner.getInventoryLimit());
		}
		return (_questSlots + slots) <= _owner.getQuestInventoryLimit();
	}
	
	@Override
	public boolean validateWeight(long weight)
	{
		if (_owner.isGM() && _owner.getDietMode() && _owner.getAccessLevel().allowTransaction())
		{
			return true;
		}
		return ((_totalWeight + weight) <= _owner.getMaxLoad());
	}
	
	public void setInventoryBlock(int[] items, int mode)
	{
		_blockMode = mode;
		_blockItems = items;
		
		_owner.sendPacket(new ItemList(_owner, false));
	}
	
	public void unblock()
	{
		_blockMode = -1;
		_blockItems = null;
		
		_owner.sendPacket(new ItemList(_owner, false));
	}
	
	public boolean hasInventoryBlock()
	{
		return ((_blockMode > -1) && (_blockItems != null) && (_blockItems.length > 0));
	}
	
	public void blockAllItems()
	{
		setInventoryBlock(new int[]
		{
			(ItemHolder.getInstance().getArraySize() + 2)
		}, 1);
	}
	
	public int getBlockMode()
	{
		return _blockMode;
	}
	
	public int[] getBlockItems()
	{
		return _blockItems;
	}
	
	public boolean canManipulateWithItemId(int itemId)
	{
		if (((_blockMode == 0) && Util.contains(_blockItems, itemId)) || ((_blockMode == 1) && !Util.contains(_blockItems, itemId)))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public void addItem(L2ItemInstance item)
	{
		if (item.isQuestItem())
		{
			synchronized (_lock)
			{
				_questSlots++;
			}
		}
		super.addItem(item);
	}
	
	public int getSize(boolean quest)
	{
		if (quest)
		{
			return _questSlots;
		}
		return getSize() - _questSlots;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + _owner + "]";
	}
	
	public void applyItemSkills()
	{
		for (L2ItemInstance item : _items)
		{
			item.giveSkillsToOwner();
			item.applyEnchantStats();
		}
	}
	
	private static enum TrackerEvent
	{
		DROP,
		ADD_TO_INVENTORY,
		DESTROY,
		TRANSFER
	}
	
	private void fireTrackerEvents(TrackerEvent tEvent, L2PcInstance actor, L2ItemInstance item, ItemContainer target)
	{
		if ((item != null) && (actor != null) && !itemTrackers.isEmpty())
		{
			switch (tEvent)
			{
				case ADD_TO_INVENTORY:
				{
					AddToInventoryEvent event = new AddToInventoryEvent();
					event.setItem(item);
					event.setPlayer(actor);
					for (ItemTracker tracker : itemTrackers)
					{
						if (tracker.containsItemId(item.getId()))
						{
							tracker.onAddToInventory(event);
						}
					}
					return;
				}
				case DROP:
				{
					ItemDropEvent event = new ItemDropEvent();
					event.setItem(item);
					event.setDropper(actor);
					event.setLocation(actor.getLocation());
					for (ItemTracker tracker : itemTrackers)
					{
						if (tracker.containsItemId(item.getId()))
						{
							tracker.onDrop(event);
						}
					}
					return;
				}
				case DESTROY:
				{
					ItemDestroyEvent event = new ItemDestroyEvent();
					event.setItem(item);
					event.setPlayer(actor);
					for (ItemTracker tracker : itemTrackers)
					{
						if (tracker.containsItemId(item.getId()))
						{
							tracker.onDestroy(event);
						}
					}
					return;
				}
				case TRANSFER:
				{
					if (target != null)
					{
						ItemTransferEvent event = new ItemTransferEvent();
						event.setItem(item);
						event.setPlayer(actor);
						event.setTarget(target);
						for (ItemTracker tracker : itemTrackers)
						{
							if (tracker.containsItemId(item.getId()))
							{
								tracker.onTransfer(event);
							}
						}
					}
					return;
				}
			}
		}
	}
	
	public static void addItemTracker(ItemTracker tracker)
	{
		if (!itemTrackers.contains(tracker))
		{
			itemTrackers.add(tracker);
		}
	}
	
	public static void removeItemTracker(ItemTracker tracker)
	{
		itemTrackers.remove(tracker);
	}
}