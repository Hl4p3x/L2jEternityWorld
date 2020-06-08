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
package l2e.loginserver.network.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import l2e.loginserver.GameServerTable;
import l2e.loginserver.GameServerTable.GameServerInfo;
import l2e.loginserver.network.L2LoginClient;
import l2e.loginserver.network.gameserverpackets.ServerStatus;

public final class ServerList extends L2LoginServerPacket
{
	protected static final Logger _log = Logger.getLogger(ServerList.class.getName());
	
	private final List<ServerData> _servers;
	private final int _lastServer;
	private final Map<Integer, Integer> _charsOnServers;
	private final Map<Integer, long[]> _charsToDelete;
	
	class ServerData
	{
		protected byte[] _ip;
		protected int _port;
		protected int _ageLimit;
		protected boolean _pvp;
		protected int _currentPlayers;
		protected int _maxPlayers;
		protected boolean _brackets;
		protected boolean _clock;
		protected int _status;
		protected int _serverId;
		protected int _serverType;
		
		ServerData(L2LoginClient client, GameServerInfo gsi)
		{
			try
			{
				_ip = InetAddress.getByName(gsi.getServerAddress(client.getConnection().getInetAddress())).getAddress();
			}
			catch (UnknownHostException e)
			{
				_log.warning(getClass().getSimpleName() + ": " + e.getMessage());
				_ip = new byte[4];
				_ip[0] = 127;
				_ip[1] = 0;
				_ip[2] = 0;
				_ip[3] = 1;
			}
			
			_port = gsi.getPort();
			_pvp = gsi.isPvp();
			_serverType = gsi.getServerType();
			_currentPlayers = gsi.getCurrentPlayerCount();
			_maxPlayers = gsi.getMaxPlayers();
			_ageLimit = 0;
			_brackets = gsi.isShowingBrackets();
			_status = gsi.getStatus() != ServerStatus.STATUS_GM_ONLY ? gsi.getStatus() : client.getAccessLevel() > 0 ? gsi.getStatus() : ServerStatus.STATUS_DOWN;
			_serverId = gsi.getId();
		}
	}
	
	public ServerList(L2LoginClient client)
	{
		_servers = new ArrayList<>(GameServerTable.getInstance().getRegisteredGameServers().size());
		_lastServer = client.getLastServer();
		for (GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
		{
			_servers.add(new ServerData(client, gsi));
		}
		_charsOnServers = client.getCharsOnServ();
		_charsToDelete = client.getCharsWaitingDelOnServ();
	}
	
	@Override
	public void write()
	{
		writeC(0x04);
		writeC(_servers.size());
		writeC(_lastServer);
		for (ServerData server : _servers)
		{
			writeC(server._serverId);
			
			writeC(server._ip[0] & 0xff);
			writeC(server._ip[1] & 0xff);
			writeC(server._ip[2] & 0xff);
			writeC(server._ip[3] & 0xff);
			
			writeD(server._port);
			writeC(server._ageLimit);
			writeC(server._pvp ? 0x01 : 0x00);
			writeH(server._currentPlayers);
			writeH(server._maxPlayers);
			writeC(server._status == ServerStatus.STATUS_DOWN ? 0x00 : 0x01);
			writeD(server._serverType);
			writeC(server._brackets ? 0x01 : 0x00);
		}
		writeH(0x00);
		if (_charsOnServers != null)
		{
			writeC(_charsOnServers.size());
			for (int servId : _charsOnServers.keySet())
			{
				writeC(servId);
				writeC(_charsOnServers.get(servId));
				if ((_charsToDelete == null) || !_charsToDelete.containsKey(servId))
				{
					writeC(0x00);
				}
				else
				{
					writeC(_charsToDelete.get(servId).length);
					for (long deleteTime : _charsToDelete.get(servId))
					{
						writeD((int) ((deleteTime - System.currentTimeMillis()) / 1000));
					}
				}
			}
		}
		else
		{
			writeC(0x00);
		}
	}
}