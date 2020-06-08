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
import l2e.Config;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.CastleManorManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.SeedProduction;
import l2e.gameserver.model.actor.instance.L2ManorManagerInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

public class RequestBuySeed extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 12;
	
	private int _manorId;
	private Seed[] _seeds = null;
	
	@Override
	protected void readImpl()
	{
		_manorId = readD();
		
		int count = readD();
		if ((count <= 0) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}
		
		_seeds = new Seed[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			long cnt = readQ();
			if (cnt < 1)
			{
				_seeds = null;
				return;
			}
			_seeds[i] = new Seed(itemId, cnt);
		}
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getManor().tryPerformAction("BuySeed"))
		{
			return;
		}
		
		if (_seeds == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Object manager = player.getTarget();
		
		if (!(manager instanceof L2ManorManagerInstance))
		{
			manager = player.getLastFolkNPC();
		}
		
		if (!(manager instanceof L2ManorManagerInstance))
		{
			return;
		}
		
		if (!player.isInsideRadius(manager, INTERACTION_DISTANCE, true, false))
		{
			return;
		}
		
		Castle castle = CastleManager.getInstance().getCastleById(_manorId);
		
		long totalPrice = 0;
		int slots = 0;
		int totalWeight = 0;
		
		for (Seed i : _seeds)
		{
			if (!i.setProduction(castle))
			{
				return;
			}
			
			totalPrice += i.getPrice();
			
			if (totalPrice > MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}
			
			L2Item template = ItemHolder.getInstance().getTemplate(i.getSeedId());
			totalWeight += i.getCount() * template.getWeight();
			if (!template.isStackable())
			{
				slots += i.getCount();
			}
			else if (player.getInventory().getItemByItemId(i.getSeedId()) == null)
			{
				slots++;
			}
		}
		
		if (!player.getInventory().validateWeight(totalWeight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}
		
		if ((totalPrice < 0) || (player.getAdena() < totalPrice))
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}
		
		for (Seed i : _seeds)
		{
			if (!player.reduceAdena("Buy", i.getPrice(), player, false) || !i.updateProduction(castle))
			{
				totalPrice -= i.getPrice();
				continue;
			}
			player.addItem("Buy", i.getSeedId(), i.getCount(), manager, true);
		}
		
		if (totalPrice > 0)
		{
			castle.addToTreasuryNoTax(totalPrice);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA);
			sm.addItemNumber(totalPrice);
			player.sendPacket(sm);
		}
	}
	
	private static class Seed
	{
		private final int _seedId;
		private final long _count;
		SeedProduction _seed;
		
		public Seed(int id, long num)
		{
			_seedId = id;
			_count = num;
		}
		
		public int getSeedId()
		{
			return _seedId;
		}
		
		public long getCount()
		{
			return _count;
		}
		
		public long getPrice()
		{
			return _seed.getPrice() * _count;
		}
		
		public boolean setProduction(Castle c)
		{
			_seed = c.getSeed(_seedId, CastleManorManager.PERIOD_CURRENT);
			
			if (_seed.getPrice() <= 0)
			{
				return false;
			}
			
			if (_seed.getCanProduce() < _count)
			{
				return false;
			}
			
			if ((MAX_ADENA / _count) < _seed.getPrice())
			{
				return false;
			}
			
			return true;
		}
		
		public boolean updateProduction(Castle c)
		{
			synchronized (_seed)
			{
				long amount = _seed.getCanProduce();
				if (_count > amount)
				{
					return false;
				}
				_seed.setCanProduce(amount - _count);
			}
			
			if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
			{
				c.updateSeed(_seedId, _seed.getCanProduce(), CastleManorManager.PERIOD_CURRENT);
			}
			return true;
		}
	}
}