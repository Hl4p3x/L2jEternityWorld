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
package l2e.loginserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import l2e.Config;

public abstract class FloodProtectedListener extends Thread
{
	private final Logger _log = Logger.getLogger(FloodProtectedListener.class.getName());
	private final Map<String, ForeignConnection> _floodProtection = new FastMap<>();
	private final String _listenIp;
	private final int _port;
	private ServerSocket _serverSocket;
	
	public FloodProtectedListener(String listenIp, int port) throws IOException
	{
		_port = port;
		_listenIp = listenIp;
		if (_listenIp.equals("*"))
		{
			_serverSocket = new ServerSocket(_port);
		}
		else
		{
			_serverSocket = new ServerSocket(_port, 50, InetAddress.getByName(_listenIp));
		}
	}
	
	@Override
	public void run()
	{
		Socket connection = null;
		
		while (!isInterrupted())
		{
			try
			{
				connection = _serverSocket.accept();
				if(Config.FLOOD_PROTECTION)
				{
					ForeignConnection fConnection = _floodProtection.get(connection.getInetAddress().getHostAddress());
					if(fConnection != null)
					{
						fConnection.connectionNumber += 1;
						if (((fConnection.connectionNumber > Config.FAST_CONNECTION_LIMIT) && ((System.currentTimeMillis() - fConnection.lastConnection) < Config.NORMAL_CONNECTION_TIME)) || ((System.currentTimeMillis() - fConnection.lastConnection) < Config.FAST_CONNECTION_TIME) || (fConnection.connectionNumber > Config.MAX_CONNECTION_PER_IP))
						{
							fConnection.lastConnection = System.currentTimeMillis();
							connection.close();
							fConnection.connectionNumber -= 1;
							if (!fConnection.isFlooding)
							{
								_log.warning("Potential Flood from " + connection.getInetAddress().getHostAddress());
							}
							fConnection.isFlooding = true;
							continue;
						}
						if (fConnection.isFlooding)
						{
							fConnection.isFlooding = false;
							_log.info(connection.getInetAddress().getHostAddress() + " is not considered as flooding anymore.");
						}
						fConnection.lastConnection = System.currentTimeMillis();
					}
					else
					{
						fConnection = new ForeignConnection(System.currentTimeMillis());
						_floodProtection.put(connection.getInetAddress().getHostAddress(),fConnection);
					}
				}
				addClient(connection);
			}
			catch (Exception e)
			{
				if (isInterrupted())
				{
					try
					{
						_serverSocket.close();
					}
					catch (IOException io)
					{
						_log.log(Level.INFO, "", io);
					}
					break;
				}
			}
		}
	}
	
	protected static class ForeignConnection
	{
		public int connectionNumber;
		public long lastConnection;
		public boolean isFlooding = false;
		
		public ForeignConnection(long time)
		{
			lastConnection = time;
			connectionNumber = 1;
		}
	}
	
	public abstract void addClient(Socket s);
	
	public void removeFloodProtection(String ip)
	{
		if(!Config.FLOOD_PROTECTION)
			return;
		ForeignConnection fConnection = _floodProtection.get(ip);
		if(fConnection != null)
		{
			fConnection.connectionNumber -= 1;
			if (fConnection.connectionNumber == 0)
			{
				_floodProtection.remove(ip);
			}
		}
		else
		{
			_log.warning("Removing a flood protection for a GameServer that was not in the connection map??? :"+ip);
		}
	}
	
	public void close()
	{
		try
		{
			_serverSocket.close();
		}
		catch (IOException e)
		{
			_log.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
}