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

import java.util.Map;
import java.util.Map.Entry;

import l2e.gameserver.model.L2PremiumItem;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class ExGetPremiumItemList extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	
	private final Map<Integer, L2PremiumItem> _map;
	
	public ExGetPremiumItemList(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		_map = _activeChar.getPremiumItemList();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x86);
		if (!_map.isEmpty())
		{
			writeD(_map.size());
			for (Entry<Integer, L2PremiumItem> entry : _map.entrySet())
			{
				L2PremiumItem item = entry.getValue();
				writeD(entry.getKey());
				writeD(_activeChar.getObjectId());
				writeD(item.getItemId());
				writeQ(item.getCount());
				writeD(0x00);
				writeS(item.getSender());
			}
		}
		else
		{
			writeD(0x00);
		}
	}
}