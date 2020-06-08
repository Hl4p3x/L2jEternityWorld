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
package l2e.gameserver.network.gameserverpackets;

import javolution.util.FastList;

import l2e.util.network.BaseSendablePacket;

public class PlayerInGame extends BaseSendablePacket
{
	public PlayerInGame (String player)
	{
		writeC(0x02);
		writeH(1);
		writeS(player);
	}
	
	public PlayerInGame (FastList<String> players)
	{
		writeC(0x02);
		writeH(players.size());
		for(String pc : players)
			writeS(pc);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}