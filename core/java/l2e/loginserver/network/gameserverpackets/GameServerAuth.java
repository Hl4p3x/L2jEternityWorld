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

import java.util.Arrays;
import java.util.logging.Logger;

import l2e.Config;
import l2e.loginserver.GameServerTable;
import l2e.loginserver.GameServerTable.GameServerInfo;
import l2e.loginserver.GameServerThread;
import l2e.loginserver.network.L2JGameServerPacketHandler.GameServerState;
import l2e.loginserver.network.loginserverpackets.AuthResponse;
import l2e.loginserver.network.loginserverpackets.LoginServerFail;
import l2e.util.network.BaseRecievePacket;

public class GameServerAuth extends BaseRecievePacket
{
	protected static Logger _log = Logger.getLogger(GameServerAuth.class.getName());

	GameServerThread _server;
	private final byte[] _hexId;
	private final int _desiredId;
	@SuppressWarnings("unused")
	private final boolean _hostReserved;
	private final boolean _acceptAlternativeId;
	private final int _maxPlayers;
	private final int _port;
	private final String[] _hosts;
	
	public GameServerAuth(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		_server = server;
		_desiredId = readC();
		_acceptAlternativeId = (readC() == 0 ? false : true);
		_hostReserved = (readC() == 0 ? false : true);
		_port = readH();
		_maxPlayers = readD();
		int size = readD();
		_hexId = readB(size);
		size = 2 * readD();
		_hosts = new String[size];
		for (int i = 0; i < size; i++)
			_hosts[i] = readS();
		
		if (Config.DEBUG)
			_log.info("Auth request received");
		
		if (handleRegProcess())
		{
			AuthResponse ar = new AuthResponse(server.getGameServerInfo().getId());
			server.sendPacket(ar);
			if (Config.DEBUG)
			{
				_log.info("Authed: id: "+server.getGameServerInfo().getId());
			}
			server.broadcastToTelnet("GameServer ["+server.getServerId()+"] "+GameServerTable.getInstance().getServerNameById(server.getServerId())+" is connected");
			server.setLoginConnectionState(GameServerState.AUTHED);
		}
	}
	
	private boolean handleRegProcess()
	{
		GameServerTable gameServerTable = GameServerTable.getInstance();
		
		int id = _desiredId;
		byte[] hexId = _hexId;
		
		GameServerInfo gsi = gameServerTable.getRegisteredGameServerById(id);

		if (gsi != null)
		{
			if (Arrays.equals(gsi.getHexId(), hexId))
			{
				synchronized (gsi)
				{
					if (gsi.isAuthed())
					{
						_server.forceClose(LoginServerFail.REASON_ALREADY_LOGGED8IN);
						return false;
					}
					_server.attachGameServerInfo(gsi, _port, _hosts, _maxPlayers);
				}
			}
			else
			{
				if (Config.ACCEPT_NEW_GAMESERVER && _acceptAlternativeId)
				{
					gsi = new GameServerInfo(id, hexId, _server);
					if (gameServerTable.registerWithFirstAvaliableId(gsi))
					{
						_server.attachGameServerInfo(gsi, _port, _hosts, _maxPlayers);
						gameServerTable.registerServerOnDB(gsi);
					}
					else
					{
						_server.forceClose(LoginServerFail.REASON_NO_FREE_ID);
						return false;
					}
				}
				else
				{
					_server.forceClose(LoginServerFail.REASON_WRONG_HEXID);
					return false;
				}
			}
		}
		else
		{
			if (Config.ACCEPT_NEW_GAMESERVER)
			{
				gsi = new GameServerInfo(id, hexId, _server);
				if (gameServerTable.register(id, gsi))
				{
					_server.attachGameServerInfo(gsi, _port, _hosts, _maxPlayers);
					gameServerTable.registerServerOnDB(gsi);
				}
				else
				{
					_server.forceClose(LoginServerFail.REASON_ID_RESERVED);
					return false;
				}
			}
			else
			{
				_server.forceClose(LoginServerFail.REASON_WRONG_HEXID);
				return false;
			}
		}
		return true;
	}
}