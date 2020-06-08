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

import java.util.Map;
import java.util.logging.Level;

import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.xml.BuyListParser;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2MerchantInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.buylist.L2BuyList;
import l2e.gameserver.model.buylist.Product;
import l2e.gameserver.model.itemcontainer.Inventory;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.items.L2Armor;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.type.L2ArmorType;
import l2e.gameserver.model.items.type.L2WeaponType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.ShopPreviewInfo;
import l2e.gameserver.network.serverpackets.UserInfo;
import l2e.gameserver.util.Util;

public final class RequestPreviewItem extends L2GameClientPacket
{
	protected L2PcInstance _activeChar;
	private Map<Integer, Integer> _item_list;
	protected int _unk;
	private int _listId;
	private int _count;
	private int[] _items;
	
	protected class RemoveWearItemsTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				_activeChar.sendPacket(SystemMessageId.NO_LONGER_TRYING_ON);
				_activeChar.sendPacket(new UserInfo(_activeChar));
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	@Override
	protected void readImpl()
	{
		_unk = readD();
		_listId = readD();
		_count = readD();
		
		if (_count < 0)
		{
			_count = 0;
		}
		if (_count > 100)
		{
			return;
		}
		
		_items = new int[_count];
		
		for (int i = 0; i < _count; i++)
		{
			_items[i] = readD();
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_items == null)
		{
			return;
		}
		
		_activeChar = getClient().getActiveChar();
		if (_activeChar == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("buy"))
		{
			_activeChar.sendMessage("You are buying too fast.");
			return;
		}
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && (_activeChar.getKarma() > 0))
		{
			return;
		}
		
		L2Object target = _activeChar.getTarget();
		if (!_activeChar.isGM() && ((target == null) || !((target instanceof L2MerchantInstance)) || !_activeChar.isInsideRadius(target, L2Npc.INTERACTION_DISTANCE, false, false)))
		{
			return;
		}
		
		if ((_count < 1) || (_listId >= 4000000))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2MerchantInstance merchant = (target instanceof L2MerchantInstance) ? (L2MerchantInstance) target : null;
		if (merchant == null)
		{
			_log.warning(getClass().getName() + " Null merchant!");
			return;
		}
		
		final L2BuyList buyList = BuyListParser.getInstance().getBuyList(_listId);
		if (buyList == null)
		{
			Util.handleIllegalPlayerAction(_activeChar, "Warning!! Character " + _activeChar.getName() + " of account " + _activeChar.getAccountName() + " sent a false BuyList list_id " + _listId, Config.DEFAULT_PUNISH);
			return;
		}
		
		long totalPrice = 0;
		_item_list = new FastMap<>();
		
		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i];
			
			final Product product = buyList.getProductByItemId(itemId);
			if (product == null)
			{
				Util.handleIllegalPlayerAction(_activeChar, "Warning!! Character " + _activeChar.getName() + " of account " + _activeChar.getAccountName() + " sent a false BuyList list_id " + _listId + " and item_id " + itemId, Config.DEFAULT_PUNISH);
				return;
			}
			
			L2Item template = product.getItem();
			if (template == null)
			{
				continue;
			}
			
			int slot = Inventory.getPaperdollIndex(template.getBodyPart());
			if (slot < 0)
			{
				continue;
			}
			
			if (template instanceof L2Weapon)
			{
				if (_activeChar.getRace().ordinal() == 5)
				{
					if (template.getItemType() == L2WeaponType.NONE)
					{
						continue;
					}
					else if ((template.getItemType() == L2WeaponType.RAPIER) || (template.getItemType() == L2WeaponType.CROSSBOW) || (template.getItemType() == L2WeaponType.ANCIENTSWORD))
					{
						continue;
					}
				}
			}
			else if (template instanceof L2Armor)
			{
				if (_activeChar.getRace().ordinal() == 5)
				{
					if ((template.getItemType() == L2ArmorType.HEAVY) || (template.getItemType() == L2ArmorType.MAGIC))
					{
						continue;
					}
				}
			}
			
			if (_item_list.containsKey(slot))
			{
				_activeChar.sendPacket(SystemMessageId.YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME);
				return;
			}
			
			_item_list.put(slot, itemId);
			totalPrice += Config.WEAR_PRICE;
			if (totalPrice > PcInventory.MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(_activeChar, "Warning!! Character " + _activeChar.getName() + " of account " + _activeChar.getAccountName() + " tried to purchase over " + PcInventory.MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}
		}
		
		if ((totalPrice < 0) || !_activeChar.reduceAdena("Wear", totalPrice, _activeChar.getLastFolkNPC(), true))
		{
			_activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}
		
		if (!_item_list.isEmpty())
		{
			_activeChar.sendPacket(new ShopPreviewInfo(_item_list));
			ThreadPoolManager.getInstance().scheduleGeneral(new RemoveWearItemsTask(), Config.WEAR_DELAY * 1000);
		}
	}
}