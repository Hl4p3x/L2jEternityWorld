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

import static l2e.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;
import l2e.Config;
import l2e.gameserver.data.xml.BuyListParser;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2MerchantInstance;
import l2e.gameserver.model.actor.instance.L2MerchantSummonInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.buylist.L2BuyList;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.ExBuySellList;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.util.Util;

public final class RequestRefundItem extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 4;
	
	private int _listId;
	private int[] _items = null;
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		final int count = readD();
		if ((count <= 0) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}
		
		_items = new int[count];
		for (int i = 0; i < count; i++)
		{
			_items[i] = readD();
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("refund"))
		{
			player.sendMessage("You are using refund too fast.");
			return;
		}
		
		if (_items == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!player.hasRefund())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Object target = player.getTarget();
		if (!player.isGM() && ((target == null) || !((target instanceof L2MerchantInstance) || (target instanceof L2MerchantSummonInstance)) || (player.getInstanceId() != target.getInstanceId()) || !player.isInsideRadius(target, INTERACTION_DISTANCE, true, false)))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Character merchant = null;
		if ((target instanceof L2MerchantInstance) || (target instanceof L2MerchantSummonInstance))
		{
			merchant = (L2Character) target;
		}
		else if (!player.isGM())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (merchant == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2BuyList buyList = BuyListParser.getInstance().getBuyList(_listId);
		if (buyList == null)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + _listId, Config.DEFAULT_PUNISH);
			return;
		}
		
		if (merchant instanceof L2MerchantInstance)
		{
			if (!buyList.isNpcAllowed(((L2MerchantInstance) merchant).getId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else
		{
			if (!buyList.isNpcAllowed(((L2MerchantSummonInstance) merchant).getId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		long weight = 0;
		long adena = 0;
		long slots = 0;
		
		L2ItemInstance[] refund = player.getRefund().getItems();
		int[] objectIds = new int[_items.length];
		
		for (int i = 0; i < _items.length; i++)
		{
			int idx = _items[i];
			if ((idx < 0) || (idx >= refund.length))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent invalid refund index", Config.DEFAULT_PUNISH);
				return;
			}
			
			for (int j = i + 1; j < _items.length; j++)
			{
				if (idx == _items[j])
				{
					Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent duplicate refund index", Config.DEFAULT_PUNISH);
					return;
				}
			}
			
			final L2ItemInstance item = refund[idx];
			final L2Item template = item.getItem();
			objectIds[i] = item.getObjectId();
			
			for (int j = 0; j < i; j++)
			{
				if (objectIds[i] == objectIds[j])
				{
					Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " has duplicate items in refund list", Config.DEFAULT_PUNISH);
					return;
				}
			}
			
			long count = item.getCount();
			weight += count * template.getWeight();
			adena += (count * template.getReferencePrice()) / 2;
			if (!template.isStackable())
			{
				slots += count;
			}
			else if (player.getInventory().getItemByItemId(template.getId()) == null)
			{
				slots++;
			}
		}
		
		if ((weight > Integer.MAX_VALUE) || (weight < 0) || !player.getInventory().validateWeight((int) weight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((slots > Integer.MAX_VALUE) || (slots < 0) || !player.getInventory().validateCapacity((int) slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((adena < 0) || !player.reduceAdena("Refund", adena, player.getLastFolkNPC(), false))
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		for (int i = 0; i < _items.length; i++)
		{
			L2ItemInstance item = player.getRefund().transferItem("Refund", objectIds[i], Long.MAX_VALUE, player.getInventory(), player, player.getLastFolkNPC());
			if (item == null)
			{
				_log.warning("Error refunding object for char " + player.getName() + " (newitem == null)");
				continue;
			}
		}
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ExBuySellList(player, true));
	}
}