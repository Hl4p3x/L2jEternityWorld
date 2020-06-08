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

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.L2Henna;

public class HennaRemoveList extends L2GameServerPacket
{
	private final L2PcInstance _player;
	
	public HennaRemoveList(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xE6);
		writeQ(_player.getAdena());
		writeD(0x00);
		writeD(3 - _player.getHennaEmptySlots());
		
		for (L2Henna henna : _player.getHennaList())
		{
			if (henna != null)
			{
				writeD(henna.getDyeId());
				writeD(henna.getDyeItemId());
				writeD(henna.getCancelCount());
				writeD(0x00);
				writeD(henna.getCancelFee());
				writeD(0x00);
				writeD(0x01);
			}
		}
	}
}