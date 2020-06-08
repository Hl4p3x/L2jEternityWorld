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

import javolution.util.FastList;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.model.FortSiegeSpawn;
import l2e.gameserver.model.entity.Fort;

public class ExShowFortressSiegeInfo extends L2GameServerPacket
{
	private final int _fortId;
	private final int _size;
	private final Fort _fort;
	private int _csize;
	private final int _csize2;
	
	public ExShowFortressSiegeInfo(Fort fort)
	{
		_fort = fort;
		_fortId = fort.getId();
		_size = fort.getFortSize();
		FastList<FortSiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(_fortId);
		if (commanders != null)
		{
			_csize = commanders.size();
		}
		_csize2 = _fort.getSiege().getCommanders().size();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x17);
		
		writeD(_fortId);
		writeD(_size);
		if (_csize > 0)
		{
			switch (_csize)
			{
				case 3:
					switch (_csize2)
					{
						case 0:
							writeD(0x03);
							break;
						case 1:
							writeD(0x02);
							break;
						case 2:
							writeD(0x01);
							break;
						case 3:
							writeD(0x00);
							break;
					}
					break;
				case 4:
					switch (_csize2)
					{
						case 0:
							writeD(0x05);
							break;
						case 1:
							writeD(0x04);
							break;
						case 2:
							writeD(0x03);
							break;
						case 3:
							writeD(0x02);
							break;
						case 4:
							writeD(0x01);
							break;
					}
					break;
			}
		}
		else
		{
			for (int i = 0; i < _size; i++)
			{
				writeD(0x00);
			}
		}
	}
}