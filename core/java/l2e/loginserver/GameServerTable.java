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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javolution.io.UTF8StreamReader;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamReaderImpl;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.loginserver.network.gameserverpackets.ServerStatus;
import l2e.util.IPSubnet;
import l2e.util.Rnd;

public final class GameServerTable
{
	private static final Logger _log = Logger.getLogger(GameServerTable.class.getName());

	private static final Map<Integer, String> _serverNames = new HashMap<>();
	private static final Map<Integer, GameServerInfo> _gameServerTable = new HashMap<>();
	private static final int KEYS_SIZE = 10;
	private KeyPair[] _keyPairs;
	
	public GameServerTable()
	{
		loadGameServerNames();
		_log.info(getClass().getSimpleName() + ": Loaded " + _serverNames.size() + " server names");
		
		loadRegisteredGameServers();
		_log.info(getClass().getSimpleName() + ": Loaded " + _gameServerTable.size() + " registered Game Servers");
		
		initRSAKeys();
		_log.info(getClass().getSimpleName() + ": Cached " + _keyPairs.length + " RSA keys for Game Server communication.");
	}

	private void loadGameServerNames()
	{
		final File xml = new File(Config.DATAPACK_ROOT, "data/servername.xml");
		try (InputStream in = new FileInputStream(xml);
			UTF8StreamReader utf8 = new UTF8StreamReader())
		{
			final XMLStreamReaderImpl xpp = new XMLStreamReaderImpl();
			xpp.setInput(utf8.setInput(in));
			for (int e = xpp.getEventType(); e != XMLStreamConstants.END_DOCUMENT; e = xpp.next())
			{
				if (e == XMLStreamConstants.START_ELEMENT)
				{
					if (xpp.getLocalName().toString().equals("server"))
					{
						Integer id = Integer.valueOf(xpp.getAttributeValue(null, "id").toString());
						String name = xpp.getAttributeValue(null, "name").toString();
						_serverNames.put(id, name);
					}
				}
			}
			xpp.close();
		}
		catch (Exception e)
		{
			_log.info(getClass().getSimpleName() + ": Cannot load " + xml.getAbsolutePath() + "!");
		}
	}

	private void initRSAKeys()
	{
		try
		{
			final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4));
			_keyPairs = new KeyPair[KEYS_SIZE];
			for (int i = 0; i < KEYS_SIZE; i++)
			{
				_keyPairs[i] = keyGen.genKeyPair();
			}
		}
		catch (Exception e)
		{
			_log.severe(getClass().getSimpleName() + ": Error loading RSA keys for Game Server communication!");
		}
	}

	private void loadRegisteredGameServers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement ps = con.createStatement();
			ResultSet rs = ps.executeQuery("SELECT * FROM gameservers"))
		{
			int id;
			while (rs.next())
			{
				id = rs.getInt("server_id");
				_gameServerTable.put(id, new GameServerInfo(id, stringToHex(rs.getString("hexid"))));
			}
		}
		catch (Exception e)
		{
			_log.severe(getClass().getSimpleName() + ": Error loading registered game servers!");
		}
	}

	public Map<Integer, GameServerInfo> getRegisteredGameServers()
	{
		return _gameServerTable;
	}

	public GameServerInfo getRegisteredGameServerById(int id)
	{
		return _gameServerTable.get(id);
	}

	public boolean hasRegisteredGameServerOnId(int id)
	{
		return _gameServerTable.containsKey(id);
	}

	public boolean registerWithFirstAvaliableId(GameServerInfo gsi)
	{
		synchronized (_gameServerTable)
		{
			for (Entry<Integer, String> entry : _serverNames.entrySet())
			{
				if (!_gameServerTable.containsKey(entry.getKey()))
				{
					_gameServerTable.put(entry.getKey(), gsi);
					gsi.setId(entry.getKey());
					return true;
				}
			}
		}
		return false;
	}

	public boolean register(int id, GameServerInfo gsi)
	{
		synchronized (_gameServerTable)
		{
			if (!_gameServerTable.containsKey(id))
			{
				_gameServerTable.put(id, gsi);
				gsi.setId(id);
				return true;
			}
		}
		return false;
	}

	public void registerServerOnDB(GameServerInfo gsi)
	{
		registerServerOnDB(gsi.getHexId(), gsi.getId(), gsi.getExternalHost());
	}

	public void registerServerOnDB(byte[] hexId, int id, String externalHost)
	{
		register(id, new GameServerInfo(id, hexId));
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)"))
		{
			ps.setString(1, hexToString(hexId));
			ps.setInt(2, id);
			ps.setString(3, externalHost);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			_log.severe(getClass().getSimpleName() + ": Error while saving gameserver!");
		}
	}

	public String getServerNameById(int id)
	{
		return _serverNames.get(id);
	}
	
	public Map<Integer, String> getServerNames()
	{
		return _serverNames;
	}

	public KeyPair getKeyPair()
	{
		return _keyPairs[Rnd.nextInt(10)];
	}

	private byte[] stringToHex(String string)
	{
		return new BigInteger(string, 16).toByteArray();
	}

	private String hexToString(byte[] hex)
	{
		if (hex == null)
		{
			return "null";
		}
		return new BigInteger(hex).toString(16);
	}

	public static class GameServerInfo
	{

		private int _id;
		private final byte[] _hexId;
		private boolean _isAuthed;

		private GameServerThread _gst;
		private int _status;

		private final ArrayList<GameServerAddress> _addrs = new ArrayList<>(5);
		private int _port;

		private final boolean _isPvp = true;
		private int _serverType;
		private int _ageLimit;
		private boolean _isShowingBrackets;
		private int _maxPlayers;

		public GameServerInfo(int id, byte[] hexId, GameServerThread gst)
		{
			_id = id;
			_hexId = hexId;
			_gst = gst;
			_status = ServerStatus.STATUS_DOWN;
		}

		public GameServerInfo(int id, byte[] hexId)
		{
			this(id, hexId, null);
		}

		public void setId(int id)
		{
			_id = id;
		}

		public int getId()
		{
			return _id;
		}

		public byte[] getHexId()
		{
			return _hexId;
		}

		public void setAuthed(boolean isAuthed)
		{
			_isAuthed = isAuthed;
		}

		public boolean isAuthed()
		{
			return _isAuthed;
		}

		public void setGameServerThread(GameServerThread gst)
		{
			_gst = gst;
		}

		public GameServerThread getGameServerThread()
		{
			return _gst;
		}

		public void setStatus(int status)
		{
			_status = status;
		}

		public int getStatus()
		{
			return _status;
		}

		public int getCurrentPlayerCount()
		{
			if (_gst == null)
			{
				return 0;
			}
			return _gst.getPlayerCount();
		}

		public String getExternalHost()
		{
			try
			{
				return getServerAddress(InetAddress.getByName("0.0.0.0"));
			}
			catch (Exception e)
			{
				
			}
			return null;
		}

		public int getPort()
		{
			return _port;
		}

		public void setPort(int port)
		{
			_port = port;
		}

		public void setMaxPlayers(int maxPlayers)
		{
			_maxPlayers = maxPlayers;
		}

		public int getMaxPlayers()
		{
			return _maxPlayers;
		}

		public boolean isPvp()
		{
			return _isPvp;
		}

		public void setAgeLimit(int val)
		{
			_ageLimit = val;
		}

		public int getAgeLimit()
		{
			return _ageLimit;
		}

		public void setServerType(int val)
		{
			_serverType = val;
		}

		public int getServerType()
		{
			return _serverType;
		}

		public void setShowingBrackets(boolean val)
		{
			_isShowingBrackets = val;
		}

		public boolean isShowingBrackets()
		{
			return _isShowingBrackets;
		}

		public void setDown()
		{
			setAuthed(false);
			setPort(0);
			setGameServerThread(null);
			setStatus(ServerStatus.STATUS_DOWN);
		}

		public void addServerAddress(String subnet, String addr) throws UnknownHostException
		{
			_addrs.add(new GameServerAddress(subnet, addr));
		}

		public String getServerAddress(InetAddress addr)
		{
			for (GameServerAddress a : _addrs)
			{
				if (a.equals(addr))
				{
					return a.getServerAddress();
				}
			}
			return null;
		}
		
		public String[] getServerAddresses()
		{
			String[] result = new String[_addrs.size()];
			for (int i = 0; i < result.length; i++)
			{
				result[i] = _addrs.get(i).toString();
			}
			
			return result;
		}

		public void clearServerAddresses()
		{
			_addrs.clear();
		}

		private class GameServerAddress extends IPSubnet
		{
			private final String _serverAddress;

			public GameServerAddress(String subnet, String address) throws UnknownHostException
			{
				super(subnet);
				_serverAddress = address;
			}

			public String getServerAddress()
			{
				return _serverAddress;
			}
			
			@Override
			public String toString()
			{
				return _serverAddress + super.toString();
			}
		}
	}
	
	public static GameServerTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final GameServerTable _instance = new GameServerTable();
	}
}