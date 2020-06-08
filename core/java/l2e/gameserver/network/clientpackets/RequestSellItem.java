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
import static l2e.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

import java.util.ArrayList;
import java.util.List;

import l2e.Config;
import l2e.gameserver.data.xml.BuyListParser;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2MerchantInstance;
import l2e.gameserver.model.actor.instance.L2MerchantSummonInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.buylist.L2BuyList;
import l2e.gameserver.model.holders.ItemsHolder;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.ExBuySellList;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.util.Util;

public final class RequestSellItem extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 16;
	
	private int _listId;
	private List<ItemsHolder> _items = null;
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		int size = readD();
		if ((size <= 0) || (size > Config.MAX_ITEM_IN_PACKET) || ((size * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}
		
		_items = new ArrayList<>(size);
		for (int i = 0; i < size; i++)
		{
			int objectId = readD();
			int itemId = readD();
			long count = readQ();
			if ((objectId < 1) || (itemId < 1) || (count < 1))
			{
				_items = null;
				return;
			}
			_items.add(new ItemsHolder(itemId, objectId, count));
		}
	}
	
	@Override
	protected void runImpl()
	{
		processSell();
	}
	
	protected void processSell()
	{
		L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("buy"))
		{
			player.sendMessage("You are buying too fast.");
			return;
		}
		
		if (_items == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && (player.getKarma() > 0))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Object target = player.getTarget();
		L2Character merchant = null;
		if (!player.isGM())
		{
			if ((target == null) || (!player.isInsideRadius(target, INTERACTION_DISTANCE, true, false)) || (player.getInstanceId() != target.getInstanceId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if ((target instanceof L2MerchantInstance) || (target instanceof L2MerchantSummonInstance))
			{
				merchant = (L2Character) target;
			}
			else
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if ((merchant == null) && !player.isGM())
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
		
		if (merchant != null)
		{
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
		}
		
		long totalPrice = 0;
		
		for (ItemsHolder i : _items)
		{
			L2ItemInstance item = player.checkItemManipulation(i.getObjectId(), i.getCount(), "sell");
			if ((item == null) || (!item.isSellable()))
			{
				continue;
			}
			
			long price = item.getReferencePrice() / 2;
			totalPrice += price * i.getCount();
			if (((MAX_ADENA / i.getCount()) < price) || (totalPrice > MAX_ADENA))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}
			
			if (Config.ALLOW_REFUND)
			{
				item = player.getInventory().transferItem("Sell", i.getObjectId(), i.getCount(), player.getRefund(), player, merchant);
			}
			else
			{
				item = player.getInventory().destroyItem("Sell", i.getObjectId(), i.getCount(), player, merchant);
			}
		}
		player.addAdena("Sell", totalPrice, merchant, false);
		
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ExBuySellList(player, true));
	}
}