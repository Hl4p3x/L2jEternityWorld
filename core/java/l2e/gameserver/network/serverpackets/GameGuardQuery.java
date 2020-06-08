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

public class GameGuardQuery extends L2GameServerPacket
{	
	//private GameGuardQuery()
	//{	
	//}
	
	@Override
	public void writeImpl()
	{
		writeC(0x74);
		writeD(0x27533DD9);
		writeD(0x2E72A51D);
		writeD(0x2017038B);
		writeD(0xC35B1EA3);
	}
}