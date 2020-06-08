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

public class PetitionVotePacket extends L2GameServerPacket
{
	public static final PetitionVotePacket STATIC_PACKET = new PetitionVotePacket();
	
	private PetitionVotePacket()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFC);
	}
}