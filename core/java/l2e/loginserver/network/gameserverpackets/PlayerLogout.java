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
package l2e.loginserver.network.gameserverpackets;

import java.util.logging.Logger;

import l2e.Config;
import l2e.loginserver.GameServerTable;
import l2e.loginserver.GameServerThread;
import l2e.util.network.BaseRecievePacket;

public class PlayerLogout extends BaseRecievePacket
{
	protected static Logger _log = Logger.getLogger(PlayerLogout.class.getName());
	
	public PlayerLogout(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		String account = readS();
		
		server.removeAccountOnGameServer(account);
		if (Config.DEBUG)
		{
			_log.info("Player "+account+" logged out from gameserver ["+server.getServerId()+"] "+GameServerTable.getInstance().getServerNameById(server.getServerId()));
		}
		server.broadcastToTelnet("Player "+account+" disconnected from GameServer "+server.getServerId());
	}
}