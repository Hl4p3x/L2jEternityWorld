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
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.entity.Fort;

public class ExShowFortressMapInfo extends L2GameServerPacket
{
	private final Fort _fortress;
	
	public ExShowFortressMapInfo(Fort fortress)
	{
		_fortress = fortress;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x7D);
		
		writeD(_fortress.getId());
		writeD(_fortress.getSiege().getIsInProgress() ? 1 : 0);
		writeD(_fortress.getFortSize());
		
		FastList<FortSiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(_fortress.getId());
		if ((commanders != null) && (commanders.size() != 0) && _fortress.getSiege().getIsInProgress())
		{
			switch (commanders.size())
			{
				case 3:
				{
					for (FortSiegeSpawn spawn : commanders)
					{
						if (isSpawned(spawn.getId()))
						{
							writeD(0);
						}
						else
						{
							writeD(1);
						}
					}
					break;
				}
				case 4:
				{
					int count = 0;
					for (FortSiegeSpawn spawn : commanders)
					{
						count++;
						if (count == 4)
						{
							writeD(1);
						}
						if (isSpawned(spawn.getId()))
						{
							writeD(0);
						}
						else
						{
							writeD(1);
						}
					}
					break;
				}
			}
		}
		else
		{
			for (int i = 0; i < _fortress.getFortSize(); i++)
			{
				writeD(0);
			}
		}
	}
	
	private boolean isSpawned(int npcId)
	{
		boolean ret = false;
		for (L2Spawn spawn : _fortress.getSiege().getCommanders())
		{
			if (spawn.getId() == npcId)
			{
				ret = true;
				break;
			}
		}
		return ret;
	}
}