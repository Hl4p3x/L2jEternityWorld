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

public final class KeyPacket extends L2GameServerPacket
{
	private final byte[] _key;
	private final int _id;
	
	public KeyPacket(byte[] key, int id)
	{
		_key = key;
		_id = id;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x2e);
		writeC(_id);
		for (int i = 0; i < 8; i++)
		{
			writeC(_key[i]);
		}
		writeD(0x01);
		writeD(0x01);
		writeC(0x01);
		writeD(0x00);
	}
}