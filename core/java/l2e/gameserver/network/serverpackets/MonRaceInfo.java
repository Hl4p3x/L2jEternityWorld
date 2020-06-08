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

import l2e.gameserver.model.actor.L2Npc;

public class MonRaceInfo extends L2GameServerPacket
{
	private final int _unknown1;
	private final int _unknown2;
	private final L2Npc[] _monsters;
	private final int[][] _speeds;
	
	public MonRaceInfo(int unknown1, int unknown2, L2Npc[] monsters, int[][] speeds)
	{
		_unknown1 = unknown1;
		_unknown2 = unknown2;
		_monsters = monsters;
		_speeds = speeds;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe3);
		
		writeD(_unknown1);
		writeD(_unknown2);
		writeD(0x08);
		
		for (int i = 0; i < 8; i++)
		{
			writeD(_monsters[i].getObjectId());
			writeD(_monsters[i].getTemplate().getId() + 1000000);
			writeD(14107);
			writeD(181875 + (58 * (7 - i)));
			writeD(-3566);
			writeD(12080);
			writeD(181875 + (58 * (7 - i)));
			writeD(-3566);
			writeF(_monsters[i].getTemplate().getfCollisionHeight());
			writeF(_monsters[i].getTemplate().getfCollisionRadius());
			writeD(120);
			for (int j = 0; j < 20; j++)
			{
				if (_unknown1 == 0)
				{
					writeC(_speeds[i][j]);
				}
				else
				{
					writeC(0x00);
				}
			}
			writeD(0x00);
			writeD(0x00);
		}
	}
}