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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2e.gameserver.scripting.scriptengine.listeners.player;

import java.util.List;

import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.scripting.scriptengine.events.AddToInventoryEvent;
import l2e.gameserver.scripting.scriptengine.events.ItemDestroyEvent;
import l2e.gameserver.scripting.scriptengine.events.ItemDropEvent;
import l2e.gameserver.scripting.scriptengine.events.ItemTransferEvent;
import l2e.gameserver.scripting.scriptengine.impl.L2JListener;

public abstract class ItemTracker extends L2JListener
{
	private final List<Integer> _itemIds;
	
	public ItemTracker(List<Integer> itemIds)
	{
		_itemIds = itemIds;
		register();
	}
	
	public abstract void onDrop(ItemDropEvent event);
	
	public abstract void onAddToInventory(AddToInventoryEvent event);
	
	public abstract void onDestroy(ItemDestroyEvent event);
	
	public abstract void onTransfer(ItemTransferEvent event);
	
	@Override
	public void register()
	{
		PcInventory.addItemTracker(this);
	}
	
	@Override
	public void unregister()
	{
		PcInventory.removeItemTracker(this);
	}
	
	public boolean containsItemId(int itemId)
	{
		return _itemIds.contains(itemId);
	}
}