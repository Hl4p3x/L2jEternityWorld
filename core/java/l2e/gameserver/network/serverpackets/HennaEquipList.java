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

import l2e.gameserver.data.xml.HennaParser;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2Henna;

public class HennaEquipList extends L2GameServerPacket
{
	private final L2PcInstance _player;
	private final List<L2Henna> _hennaEquipList;
	
	public HennaEquipList(L2PcInstance player)
	{
		_player = player;
		_hennaEquipList = HennaParser.getInstance().getHennaList(player.getClassId());
	}
	
	public HennaEquipList(L2PcInstance player, List<L2Henna> list)
	{
		_player = player;
		_hennaEquipList = list;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xEE);
		writeQ(_player.getAdena());
		writeD(3);
		writeD(_hennaEquipList.size());
		
		for (L2Henna henna : _hennaEquipList)
		{
			if ((_player.getInventory().getItemByItemId(henna.getDyeItemId())) != null)
			{
				writeD(henna.getDyeId());
				writeD(henna.getDyeItemId());
				writeQ(henna.getWearCount());
				writeQ(henna.getWearFee());
				writeD(henna.isAllowedClass(_player.getClassId()) ? 0x01 : 0x00);
			}
		}
	}
}