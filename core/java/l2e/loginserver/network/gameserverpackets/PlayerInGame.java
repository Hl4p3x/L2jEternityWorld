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

public class PlayerInGame extends BaseRecievePacket
{
	private static Logger _log = Logger.getLogger(PlayerInGame.class.getName());
	
	public PlayerInGame(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		int size = readH();
		for (int i = 0; i < size; i++)
		{
			String account = readS();
			server.addAccountOnGameServer(account);
			if (Config.DEBUG)
			{
				_log.info("Account "+account+" logged in GameServer: ["+server.getServerId()+"] "+GameServerTable.getInstance().getServerNameById(server.getServerId()));
			}
			server.broadcastToTelnet("Account "+account+" logged in GameServer "+server.getServerId());
		}
	}
}