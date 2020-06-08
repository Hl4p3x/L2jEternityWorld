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

import l2e.util.network.BaseSendablePacket;

public class TempBan extends BaseSendablePacket
{	
	public TempBan(String accountName, String ip, long time, String reason)
	{
		writeC(0x0A);
		writeS(accountName);
		writeS(ip);
		writeQ(System.currentTimeMillis()+(time*60000));
		if (reason != null)
		{
			writeC(0x01);
			writeS(reason);
		}
		else
			writeC(0x00);
	}
	
	public TempBan(String accountName, String ip, long time)
	{
		this(accountName, ip, time, null);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}