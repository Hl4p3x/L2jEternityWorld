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
import l2e.gameserver.GameTimeController;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance.ItemLocation;

public abstract class ItemContainer
{
	protected static final Logger _log = Logger.getLogger(ItemContainer.class.getName());
	
	protected final List<L2ItemInstance> _items;
	
	protected ItemContainer()
	{
		_items = new FastList<L2ItemInstance>().shared();
	}
	
	protected abstract L2Character getOwner();
	
	protected abstract ItemLocation getBaseLocation();
	
	public String getName()
	{
		return "ItemContainer";
	}
	
	public int getOwnerId()
	{
		return getOwner() == null ? 0 : getOwner().getObjectId();
	}
	
	public int getSize()
	{
		return _items.size();
	}
	
	public L2ItemInstance[] getItems()
	{
		return _items.toArray(new L2ItemInstance[_items.size()]);
	}
	
	public L2ItemInstance getItemByItemId(int itemId)
	{
		for (L2ItemInstance item : _items)
		{
			if ((item != null) && (item.getId() == itemId))
			{
				return item;
			}
		}
		return null;
	}
	
	public List<L2ItemInstance> getItemsByItemId(int itemId)
	{
		List<L2ItemInstance> returnList = new FastList<>();
		for (L2ItemInstance item : _items)
		{
			if ((item != null) && (item.getId() == itemId))
			{
				returnList.add(item);
			}
		}
		
		return returnList;
	}
	
	public L2ItemInstance getItemByItemId(int itemId, L2ItemInstance itemToIgnore)
	{
		for (L2ItemInstance item : _items)
		{
			if ((item != null) && (item.getId() == itemId) && !item.equals(itemToIgnore))
			{
				return item;
			}
		}
		return null;
	}
	
	public L2ItemInstance getItemByObjectId(int objectId)
	{
		for (L2ItemInstance item : _items)
		{
			if ((item != null) && (item.getObjectId() == objectId))
			{
				return item;
			}
		}
		return null;
	}
	
	public long getInventoryItemCount(int itemId, int enchantLevel)
	{
		return getInventoryItemCount(itemId, enchantLevel, true);
	}
	
	public long getInventoryItemCount(int itemId, int enchantLevel, boolean includeEquipped)
	{
		long count = 0;
		
		for (L2ItemInstance item : _items)
		{
			if ((item.getId() == itemId) && ((item.getEnchantLevel() == enchantLevel) || (enchantLevel < 0)) && (includeEquipped || !item.isEquipped()))
			{
				if (item.isStackable())
				{
					count = item.getCount();
				}
				else
				{
					count++;
				}
			}
		}
		return count;
	}
	
	public L2ItemInstance addItem(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		L2ItemInstance olditem = getItemByItemId(item.getId());
		
		if ((olditem != null) && olditem.isStackable())
		{
			long count = item.getCount();
			olditem.changeCount(process, count, actor, reference);
			olditem.setLastChange(L2ItemInstance.MODIFIED);
			
			ItemHolder.getInstance().destroyItem(process, item, actor, reference);
			item.updateDatabase();
			item = olditem;
			
			if ((item.getId() == PcInventory.ADENA_ID) && (count < (10000 * Config.RATE_DROP_ITEMS_ID.get(PcInventory.ADENA_ID))))
			{
				if ((GameTimeController.getInstance().getGameTicks() % 5) == 0)
				{
					item.updateDatabase();
				}
			}
			else
			{
				item.updateDatabase();
			}
		}
		else
		{
			item.setOwnerId(process, getOwnerId(), actor, reference);
			item.setItemLocation(getBaseLocation());
			item.setLastChange((L2ItemInstance.ADDED));
			
			addItem(item);
			item.updateDatabase();
		}
		refreshWeight();
		return item;
	}
	
	public L2ItemInstance addItem(String process, int itemId, long count, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = getItemByItemId(itemId);
		
		if ((item != null) && item.isStackable())
		{
			item.changeCount(process, count, actor, reference);
			item.setLastChange(L2ItemInstance.MODIFIED);
			float adenaRate = Config.RATE_DROP_ITEMS_ID.containsKey(PcInventory.ADENA_ID) ? Config.RATE_DROP_ITEMS_ID.get(PcInventory.ADENA_ID) : 1;
			if ((itemId == PcInventory.ADENA_ID) && (count < (10000 * adenaRate)))
			{
				if ((GameTimeController.getInstance().getGameTicks() % 5) == 0)
				{
					item.updateDatabase();
				}
			}
			else
			{
				item.updateDatabase();
			}
		}
		else
		{
			for (int i = 0; i < count; i++)
			{
				L2Item template = ItemHolder.getInstance().getTemplate(itemId);
				if (template == null)
				{
					_log.log(Level.WARNING, (actor != null ? "[" + actor.getName() + "] " : "") + "Invalid ItemId requested: ", itemId);
					return null;
				}
				
				item = ItemHolder.getInstance().createItem(process, itemId, template.isStackable() ? count : 1, actor, reference);
				item.setOwnerId(getOwnerId());
				item.setItemLocation(getBaseLocation());
				item.setLastChange(L2ItemInstance.ADDED);
				
				addItem(item);
				item.updateDatabase();
				
				if (template.isStackable() || !Config.MULTIPLE_ITEM_DROP)
				{
					break;
				}
			}
		}
		refreshWeight();
		return item;
	}
	
	public L2ItemInstance transferItem(String process, int objectId, long count, ItemContainer target, L2PcInstance actor, Object reference)
	{
		if (target == null)
		{
			return null;
		}
		
		L2ItemInstance sourceitem = getItemByObjectId(objectId);
		if (sourceitem == null)
		{
			return null;
		}
		L2ItemInstance targetitem = sourceitem.isStackable() ? target.getItemByItemId(sourceitem.getId()) : null;
		
		synchronized (sourceitem)
		{
			if (getItemByObjectId(objectId) != sourceitem)
			{
				return null;
			}
			
			if (count > sourceitem.getCount())
			{
				count = sourceitem.getCount();
			}
			
			if ((sourceitem.getCount() == count) && (targetitem == null))
			{
				removeItem(sourceitem);
				target.addItem(process, sourceitem, actor, reference);
				targetitem = sourceitem;
			}
			else
			{
				if (sourceitem.getCount() > count)
				{
					sourceitem.changeCount(process, -count, actor, reference);
				}
				else
				{
					removeItem(sourceitem);
					ItemHolder.getInstance().destroyItem(process, sourceitem, actor, reference);
				}
				
				if (targetitem != null)
				{
					targetitem.changeCount(process, count, actor, reference);
				}
				else
				{
					targetitem = target.addItem(process, sourceitem.getId(), count, actor, reference);
				}
			}
			
			sourceitem.updateDatabase(true);
			if ((targetitem != sourceitem) && (targetitem != null))
			{
				targetitem.updateDatabase();
			}
			if (sourceitem.isAugmented())
			{
				sourceitem.getAugmentation().removeBonus(actor);
			}
			refreshWeight();
			target.refreshWeight();
		}
		return targetitem;
	}
	
	public L2ItemInstance destroyItem(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		return this.destroyItem(process, item, item.getCount(), actor, reference);
	}
	
	public L2ItemInstance destroyItem(String process, L2ItemInstance item, long count, L2PcInstance actor, Object reference)
	{
		synchronized (item)
		{
			if (item.getCount() > count)
			{
				item.changeCount(process, -count, actor, reference);
				item.setLastChange(L2ItemInstance.MODIFIED);
				
				if ((process != null) || ((GameTimeController.getInstance().getGameTicks() % 10) == 0))
				{
					item.updateDatabase();
				}
				refreshWeight();
			}
			else
			{
				if (item.getCount() < count)
				{
					return null;
				}
				
				boolean removed = removeItem(item);
				if (!removed)
				{
					return null;
				}
				
				ItemHolder.getInstance().destroyItem(process, item, actor, reference);
				
				item.updateDatabase();
				refreshWeight();
			}
		}
		return item;
	}
	
	public L2ItemInstance destroyItem(String process, int objectId, long count, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
		{
			return null;
		}
		return this.destroyItem(process, item, count, actor, reference);
	}
	
	public L2ItemInstance destroyItemByItemId(String process, int itemId, long count, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = getItemByItemId(itemId);
		if (item == null)
		{
			return null;
		}
		return this.destroyItem(process, item, count, actor, reference);
	}
	
	public void destroyAllItems(String process, L2PcInstance actor, Object reference)
	{
		for (L2ItemInstance item : _items)
		{
			if (item != null)
			{
				destroyItem(process, item, actor, reference);
			}
		}
	}
	
	public long getAdena()
	{
		long count = 0;
		for (L2ItemInstance item : _items)
		{
			if ((item != null) && (item.getId() == PcInventory.ADENA_ID))
			{
				count = item.getCount();
				return count;
			}
		}
		return count;
	}
	
	protected void addItem(L2ItemInstance item)
	{
		_items.add(item);
	}
	
	protected boolean removeItem(L2ItemInstance item)
	{
		return _items.remove(item);
	}
	
	protected void refreshWeight()
	{
	}
	
	public void deleteMe()
	{
		if (getOwner() != null)
		{
			for (L2ItemInstance item : _items)
			{
				if (item != null)
				{
					item.updateDatabase(true);
					item.deleteMe();
					L2World.getInstance().removeObject(item);
				}
			}
		}
		_items.clear();
	}
	
	public void updateDatabase()
	{
		if (getOwner() != null)
		{
			for (L2ItemInstance item : _items)
			{
				if (item != null)
				{
					item.updateDatabase(true);
				}
			}
		}
	}
	
	public void restore()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time FROM items WHERE owner_id=? AND (loc=?)"))
		{
			statement.setInt(1, getOwnerId());
			statement.setString(2, getBaseLocation().name());
			try (ResultSet inv = statement.executeQuery())
			{
				L2ItemInstance item;
				while (inv.next())
				{
					item = L2ItemInstance.restoreFromDb(getOwnerId(), inv);
					if (item == null)
					{
						continue;
					}
					
					L2World.getInstance().storeObject(item);
					
					L2PcInstance owner = getOwner() == null ? null : getOwner().getActingPlayer();
					
					if (item.isStackable() && (getItemByItemId(item.getId()) != null))
					{
						addItem("Restore", item, owner, null);
					}
					else
					{
						addItem(item);
					}
				}
			}
			refreshWeight();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not restore container:", e);
		}
	}
	
	public boolean validateCapacity(long slots)
	{
		return true;
	}
	
	public boolean validateWeight(long weight)
	{
		return true;
	}
	
	public boolean validateCapacityByItemId(int itemId, long count)
	{
		final L2Item template = ItemHolder.getInstance().getTemplate(itemId);
		return (template == null) || (template.isStackable() ? validateCapacity(1) : validateCapacity(count));
	}
	
	public boolean validateWeightByItemId(int itemId, long count)
	{
		final L2Item template = ItemHolder.getInstance().getTemplate(itemId);
		return (template == null) || validateWeight(template.getWeight() * count);
	}
}