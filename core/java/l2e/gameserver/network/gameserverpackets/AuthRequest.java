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

import java.util.List;

import l2e.util.network.BaseSendablePacket;

public class AuthRequest extends BaseSendablePacket
{
	public AuthRequest(int id, boolean acceptAlternate, byte[] hexid, int port, boolean reserveHost, int maxplayer, List<String> subnets, List<String> hosts)
	{
		writeC(0x01);
		writeC(id);
		writeC(acceptAlternate? 0x01 : 0x00);
		writeC(reserveHost? 0x01 : 0x00);
		writeH(port);
		writeD(maxplayer);
		writeD(hexid.length);
		writeB(hexid);
		writeD(subnets.size());
		for (int i = 0; i < subnets.size(); i++)
		{
			writeS(subnets.get(i));
			writeS(hosts.get(i));
		}
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}	
}