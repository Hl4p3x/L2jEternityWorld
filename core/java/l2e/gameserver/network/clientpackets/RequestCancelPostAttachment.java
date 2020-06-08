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
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Message;
import l2e.gameserver.model.itemcontainer.ItemContainer;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance.ItemLocation;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExChangePostState;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

public final class RequestCancelPostAttachment extends L2GameClientPacket
{
	private int _msgId;
	
	@Override
	protected void readImpl()
	{
		_msgId = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if ((activeChar == null) || !Config.ALLOW_MAIL || !Config.ALLOW_ATTACHMENTS)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("cancelpost"))
		{
			return;
		}
		
		Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
		{
			return;
		}
		if (msg.getSenderId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to cancel not own post!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!activeChar.isInsideZone(ZoneId.PEACE))
		{
			activeChar.sendPacket(SystemMessageId.CANT_CANCEL_NOT_IN_PEACE_ZONE);
			return;
		}
		
		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.sendPacket(SystemMessageId.CANT_CANCEL_DURING_EXCHANGE);
			return;
		}
		
		if (activeChar.isEnchanting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_CANCEL_DURING_ENCHANT);
			return;
		}
		
		if (activeChar.getPrivateStoreType() > L2PcInstance.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(SystemMessageId.CANT_CANCEL_PRIVATE_STORE);
			return;
		}
		
		if (!msg.hasAttachments())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANT_CANCEL_RECEIVED_MAIL);
			return;
		}
		
		final ItemContainer attachments = msg.getAttachments();
		if ((attachments == null) || (attachments.getSize() == 0))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANT_CANCEL_RECEIVED_MAIL);
			return;
		}
		
		int weight = 0;
		int slots = 0;
		
		for (L2ItemInstance item : attachments.getItems())
		{
			if (item == null)
			{
				continue;
			}
			
			if (item.getOwnerId() != activeChar.getObjectId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get not own item from cancelled attachment!", Config.DEFAULT_PUNISH);
				return;
			}
			
			if (item.getItemLocation() != ItemLocation.MAIL)
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get items not from mail !", Config.DEFAULT_PUNISH);
				return;
			}
			
			if (item.getLocationSlot() != msg.getId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get items from different attachment!", Config.DEFAULT_PUNISH);
				return;
			}
			
			weight += item.getCount() * item.getItem().getWeight();
			if (!item.isStackable())
			{
				slots += item.getCount();
			}
			else if (activeChar.getInventory().getItemByItemId(item.getId()) == null)
			{
				slots++;
			}
		}
		
		if (!activeChar.getInventory().validateCapacity(slots))
		{
			activeChar.sendPacket(SystemMessageId.CANT_CANCEL_INVENTORY_FULL);
			return;
		}
		
		if (!activeChar.getInventory().validateWeight(weight))
		{
			activeChar.sendPacket(SystemMessageId.CANT_CANCEL_INVENTORY_FULL);
			return;
		}
		
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (L2ItemInstance item : attachments.getItems())
		{
			if (item == null)
			{
				continue;
			}
			
			long count = item.getCount();
			final L2ItemInstance newItem = attachments.transferItem(attachments.getName(), item.getObjectId(), count, activeChar.getInventory(), activeChar, null);
			if (newItem == null)
			{
				return;
			}
			
			if (playerIU != null)
			{
				if (newItem.getCount() > count)
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}
			}
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_ACQUIRED_S2_S1);
			sm.addItemName(item.getId());
			sm.addItemNumber(count);
			activeChar.sendPacket(sm);
		}
		
		msg.removeAttachments();
		
		if (playerIU != null)
		{
			activeChar.sendPacket(playerIU);
		}
		else
		{
			activeChar.sendPacket(new ItemList(activeChar, false));
		}
		
		StatusUpdate su = new StatusUpdate(activeChar);
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
		
		final L2PcInstance receiver = L2World.getInstance().getPlayer(msg.getReceiverId());
		if (receiver != null)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANCELLED_MAIL);
			sm.addCharName(activeChar);
			receiver.sendPacket(sm);
			receiver.sendPacket(new ExChangePostState(true, _msgId, Message.DELETED));
		}
		MailManager.getInstance().deleteMessageInDb(_msgId);
		
		activeChar.sendPacket(new ExChangePostState(false, _msgId, Message.DELETED));
		activeChar.sendPacket(SystemMessageId.MAIL_SUCCESSFULLY_CANCELLED);
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}