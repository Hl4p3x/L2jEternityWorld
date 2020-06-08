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

import l2e.Config;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.holders.ItemsHolder;
import l2e.gameserver.model.itemcontainer.ClanWarehouse;
import l2e.gameserver.model.itemcontainer.ItemContainer;
import l2e.gameserver.model.itemcontainer.PcWarehouse;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.util.Util;

public final class SendWareHouseWithDrawList extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 12;
	
	private ItemsHolder _items[] = null;
	
	@Override
	protected void readImpl()
	{
		final int count = readD();
		if ((count <= 0) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}
		
		_items = new ItemsHolder[count];
		for (int i = 0; i < count; i++)
		{
			int objId = readD();
			long cnt = readQ();
			if ((objId < 1) || (cnt < 0))
			{
				_items = null;
				return;
			}
			_items[i] = new ItemsHolder(objId, cnt);
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_items == null)
		{
			return;
		}
		
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("withdraw"))
		{
			player.sendMessage("You are withdrawing items too fast.");
			return;
		}
		
		final ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
		{
			return;
		}
		
		final L2Npc manager = player.getLastFolkNPC();
		if (((manager == null) || !manager.isWarehouse() || !manager.canInteract(player)) && !player.isGM() && !player.isUsingAioWh())
		{
			return;
		}
		
		if (!(warehouse instanceof PcWarehouse) && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
			return;
		}
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (player.getKarma() > 0))
		{
			return;
		}
		
		if (Config.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH)
		{
			if ((warehouse instanceof ClanWarehouse) && ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE))
			{
				return;
			}
		}
		else
		{
			if ((warehouse instanceof ClanWarehouse) && !player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE);
				return;
			}
		}
		
		int weight = 0;
		int slots = 0;
		
		for (ItemsHolder i : _items)
		{
			L2ItemInstance item = warehouse.getItemByObjectId(i.getId());
			if ((item == null) || (item.getCount() < i.getCount()))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to withdraw non-existent item from warehouse.", Config.DEFAULT_PUNISH);
				return;
			}
			
			weight += i.getCount() * item.getItem().getWeight();
			if (!item.isStackable())
			{
				slots += i.getCount();
			}
			else if (player.getInventory().getItemByItemId(item.getId()) == null)
			{
				slots++;
			}
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}
		
		if (!player.getInventory().validateWeight(weight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}
		
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (ItemsHolder i : _items)
		{
			L2ItemInstance oldItem = warehouse.getItemByObjectId(i.getId());
			if ((oldItem == null) || (oldItem.getCount() < i.getCount()))
			{
				_log.warning("Error withdrawing a warehouse object for char " + player.getName() + " (olditem == null)");
				return;
			}
			final L2ItemInstance newItem = warehouse.transferItem(warehouse.getName(), i.getId(), i.getCount(), player.getInventory(), player, manager);
			if (newItem == null)
			{
				_log.warning("Error withdrawing a warehouse object for char " + player.getName() + " (newitem == null)");
				return;
			}
			
			if (playerIU != null)
			{
				if (newItem.getCount() > i.getCount())
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}
			}
		}
		
		if (playerIU != null)
		{
			player.sendPacket(playerIU);
		}
		else
		{
			player.sendPacket(new ItemList(player, false));
		}
		
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
}