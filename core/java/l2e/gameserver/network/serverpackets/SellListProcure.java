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
package l2e.gameserver.network.serverpackets;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.CropProcure;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;

public class SellListProcure extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final long _money;
	private final Map<L2ItemInstance, Long> _sellList = new FastMap<>();
	private List<CropProcure> _procureList = new FastList<>();
	private final int _castle;
	
	public SellListProcure(L2PcInstance player, int castleId)
	{
		_money = player.getAdena();
		_activeChar = player;
		_castle = castleId;
		_procureList =  CastleManager.getInstance().getCastleById(_castle).getCropProcure(0);
		for(CropProcure c : _procureList)
		{
			L2ItemInstance item = _activeChar.getInventory().getItemByItemId(c.getId());
			if(item != null && c.getAmount() > 0)
			{
				_sellList.put(item,c.getAmount());
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xEF);
		writeQ(_money);
		writeD(0x00);
		writeH(_sellList.size());
		
		for(L2ItemInstance item : _sellList.keySet())
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getDisplayId());
			writeQ(_sellList.get(item));
			writeH(item.getItem().getType2());
			writeH(0);
			writeQ(0);
		}
	}
}