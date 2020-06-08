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
package l2e.gameserver.model;

import l2e.Config;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.ItemList;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class TerritoryWard
{
	protected L2PcInstance _player = null;
	public int playerId = 0;
	private L2ItemInstance _item = null;
	private L2Npc _npc = null;
	
	private Location _location;
	private Location _oldLocation;
	
	private final int _itemId;
	private int _ownerCastleId;
	
	private final int _territoryId;
	
	public TerritoryWard(int territory_id, int x, int y, int z, int heading, int item_id, int castleId, L2Npc npc)
	{
		_territoryId = territory_id;
		_location = new Location(x, y, z, heading);
		_itemId = item_id;
		_ownerCastleId = castleId;
		_npc = npc;
	}
	
	public int getTerritoryId()
	{
		return _territoryId;
	}
	
	public int getOwnerCastleId()
	{
		return _ownerCastleId;
	}
	
	public void setOwnerCastleId(int newOwner)
	{
		_ownerCastleId = newOwner;
	}
	
	public L2Npc getNpc()
	{
		return _npc;
	}
	
	public void setNpc(L2Npc npc)
	{
		_npc = npc;
	}
	
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
	public synchronized void spawnBack()
	{
		if (_player != null)
		{
			dropIt();
		}
		
		_npc = TerritoryWarManager.getInstance().spawnNPC(36491 + _territoryId, _oldLocation);
	}
	
	public synchronized void spawnMe()
	{
		if (_player != null)
		{
			dropIt();
		}
		
		_npc = TerritoryWarManager.getInstance().spawnNPC(36491 + _territoryId, _location);
	}
	
	public synchronized void unSpawnMe()
	{
		if (_player != null)
		{
			dropIt();
		}
		if ((_npc != null) && !_npc.isDecayed())
		{
			_npc.deleteMe();
		}
	}
	
	public boolean activate(L2PcInstance player, L2ItemInstance item)
	{
		if (player.isMounted())
		{
			player.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
			player.destroyItem("CombatFlag", item, null, true);
			spawnMe();
			return false;
		}
		else if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(player) == 0)
		{
			player.sendMessage("Non participants can't pickup Territory Wards!");
			player.destroyItem("CombatFlag", item, null, true);
			spawnMe();
			return false;
		}
		_player = player;
		playerId = _player.getObjectId();
		_oldLocation = new Location(_npc.getX(), _npc.getY(), _npc.getZ(), _npc.getHeading());
		_npc = null;
		
		if (item == null)
		{
			_item = ItemHolder.getInstance().createItem("Combat", _itemId, 1, null, null);
		}
		else
		{
			_item = item;
		}
		_player.getInventory().equipItem(_item);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
		sm.addItemName(_item);
		_player.sendPacket(sm);
		
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(_item);
			_player.sendPacket(iu);
		}
		else
		{
			_player.sendPacket(new ItemList(_player, false));
		}
		
		_player.broadcastUserInfo();
		_player.setCombatFlagEquipped(true);
		_player.sendPacket(SystemMessageId.YOU_VE_ACQUIRED_THE_WARD);
		TerritoryWarManager.getInstance().giveTWPoint(player, _territoryId, 5);
		return true;
	}
	
	public void dropIt()
	{
		_player.setCombatFlagEquipped(false);
		int slot = _player.getInventory().getSlotFromItem(_item);
		_player.getInventory().unEquipItemInBodySlot(slot);
		_player.destroyItem("CombatFlag", _item, null, true);
		_item = null;
		_player.broadcastUserInfo();
		_location = new Location(_player.getX(), _player.getY(), _player.getZ(), _player.getHeading());
		_player = null;
		playerId = 0;
	}
}