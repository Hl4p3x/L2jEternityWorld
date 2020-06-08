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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;
import java.util.logging.Logger;

import javolution.util.FastSet;

import l2e.Config;
import l2e.loginserver.GameServerTable.GameServerInfo;
import l2e.loginserver.network.L2JGameServerPacketHandler;
import l2e.loginserver.network.L2JGameServerPacketHandler.GameServerState;
import l2e.loginserver.network.loginserverpackets.ChangePasswordResponse;
import l2e.loginserver.network.loginserverpackets.InitLS;
import l2e.loginserver.network.loginserverpackets.KickPlayer;
import l2e.loginserver.network.loginserverpackets.LoginServerFail;
import l2e.loginserver.network.loginserverpackets.RequestCharacters;
import l2e.util.Util;
import l2e.util.crypt.NewCrypt;
import l2e.util.network.BaseSendablePacket;

public class GameServerThread extends Thread
{
	protected static final Logger _log = Logger.getLogger(GameServerThread.class.getName());
	private final Socket _connection;
	private InputStream _in;
	private OutputStream _out;
	private final RSAPublicKey _publicKey;
	private final RSAPrivateKey _privateKey;
	private NewCrypt _blowfish;
	private GameServerState _loginConnectionState = GameServerState.CONNECTED;
	
	private final String _connectionIp;
	
	private GameServerInfo _gsi;
	
	private final Set<String> _accountsOnGameServer = new FastSet<>();
	
	private String _connectionIPAddress;
	
	@Override
	public void run()
	{
		_connectionIPAddress   = _connection.getInetAddress().getHostAddress();
		if (GameServerThread.isBannedGameserverIP(_connectionIPAddress))
		{
			_log.info("GameServerRegistration: IP Address " + _connectionIPAddress + " is on Banned IP list.");
			forceClose(LoginServerFail.REASON_IP_BANNED);

			return;
		}
		
		InitLS startPacket = new InitLS(_publicKey.getModulus().toByteArray());
		try
		{
			sendPacket(startPacket);
			
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			for (;;)
			{
				lengthLo = _in.read();
				lengthHi = _in.read();
				length= lengthHi*256 + lengthLo;
				
				if (lengthHi < 0 || _connection.isClosed())
				{
					_log.finer("LoginServerThread: Login terminated the connection.");
					break;
				}
				
				byte[] data = new byte[length - 2];
				
				int receivedBytes = 0;
				int newBytes = 0;
				int left = length - 2;
				while (newBytes != -1 && receivedBytes < length - 2)
				{
					newBytes =  _in.read(data, receivedBytes, left);
					receivedBytes = receivedBytes + newBytes;
					left -= newBytes;
				}
				
				if (receivedBytes != length-2)
				{
					_log.warning("Incomplete Packet is sent to the server, closing connection.(LS)");
					break;
				}
				
				_blowfish.decrypt(data, 0, data.length);
				checksumOk = NewCrypt.verifyChecksum(data);
				if (!checksumOk)
				{
					_log.warning("Incorrect packet checksum, closing connection (LS)");
					return;
				}
				
				if (Config.DEBUG)
				{
					_log.warning("[C]" + Config.EOL + Util.printData(data));
				}
				
				L2JGameServerPacketHandler.handlePacket(data, this);
			}
		}
		catch (IOException e)
		{
			String serverName = (getServerId() != -1 ? "["+getServerId()+"] "+GameServerTable.getInstance().getServerNameById(getServerId()) : "("+_connectionIPAddress+")");
			String msg = "GameServer "+serverName+": Connection lost: "+e.getMessage();
			_log.info(msg);
			broadcastToTelnet(msg);
		}
		finally
		{
			if (isAuthed())
			{
				_gsi.setDown();
				_log.info("Server ["+getServerId()+"] "+GameServerTable.getInstance().getServerNameById(getServerId())+" is now set as disconnected");
			}
			L2LoginServer.getInstance().getGameServerListener().removeGameServer(this);
			L2LoginServer.getInstance().getGameServerListener().removeFloodProtection(_connectionIp);
		}
	}
	
	public boolean hasAccountOnGameServer(String account)
	{
		return _accountsOnGameServer.contains(account);
	}
	
	public int getPlayerCount()
	{
		return _accountsOnGameServer.size();
	}
	
	public void attachGameServerInfo(GameServerInfo gsi, int port, String[] hosts, int maxPlayers)
	{
		setGameServerInfo(gsi);
		gsi.setGameServerThread(this);
		gsi.setPort(port);
		setGameHosts(hosts);
		gsi.setMaxPlayers(maxPlayers);
		gsi.setAuthed(true);
	}
	
	public void forceClose(int reason)
	{
		sendPacket(new LoginServerFail(reason));
		
		try
		{
			_connection.close();
		}
		catch (IOException e)
		{
			_log.finer("GameServerThread: Failed disconnecting banned server, server already disconnected.");
		}
	}
	
	public static boolean isBannedGameserverIP(String ipAddress)
	{
		return false;
	}
	
	public GameServerThread(Socket con)
	{
		_connection = con;
		_connectionIp = con.getInetAddress().getHostAddress();
		try
		{
			_in = _connection.getInputStream();
			_out = new BufferedOutputStream(_connection.getOutputStream());
		}
		catch (IOException e)
		{
			_log.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
		KeyPair pair = GameServerTable.getInstance().getKeyPair();
		_privateKey = (RSAPrivateKey) pair.getPrivate();
		_publicKey = (RSAPublicKey) pair.getPublic();
		_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
		setName(getClass().getSimpleName()+"-"+getId()+"@"+_connectionIp);
		start();
	}
	
	public void sendPacket(BaseSendablePacket sl)
	{
		try
		{
			byte[] data = sl.getContent();
			NewCrypt.appendChecksum(data);
			if (Config.DEBUG)
			{
				_log.finest("[S] " + sl.getClass().getSimpleName() + ":" + Config.EOL + Util.printData(data));
			}
			_blowfish.crypt(data, 0, data.length);
			
			int len = data.length+2;
			synchronized(_out)
			{
				_out.write(len & 0xff);
				_out.write(len >> 8 &0xff);
				_out.write(data);
				_out.flush();
			}
		}
		catch (IOException e)
		{
			_log.severe("IOException while sending packet "+sl.getClass().getSimpleName());
		}
	}
	
	public void broadcastToTelnet(String msg)
	{
		if (L2LoginServer.getInstance().getStatusServer() != null)
		{
			L2LoginServer.getInstance().getStatusServer().sendMessageToTelnets(msg);
		}
	}
	
	public void kickPlayer(String account)
	{
		sendPacket(new KickPlayer(account));
	}
	
	public void requestCharacters(String account)
	{
		sendPacket(new RequestCharacters(account));
	}
	
	public void ChangePasswordResponse(byte successful, String characterName, String msgToSend)
	{
		sendPacket(new ChangePasswordResponse(successful, characterName, msgToSend));
	}
	
	public void setGameHosts(String[] hosts)
	{
		_log.info("Updated Gameserver ["+getServerId()+"] "+GameServerTable.getInstance().getServerNameById(getServerId())+" IP's:");
		
		_gsi.clearServerAddresses();
		for (int i = 0; i < hosts.length; i += 2)
		{
			try
			{
				_gsi.addServerAddress(hosts[i], hosts[i + 1]);
			}
			catch (Exception e)
			{
				_log.warning("Couldn't resolve hostname \""+e+"\"");
			}
		}
		
		for (String s : _gsi.getServerAddresses())
			_log.info(s);
	}
	
	public boolean isAuthed()
	{
		if (getGameServerInfo() == null)
			return false;
		return getGameServerInfo().isAuthed();
	}
	
	public void setGameServerInfo(GameServerInfo gsi)
	{
		_gsi = gsi;
	}
	
	public GameServerInfo getGameServerInfo()
	{
		return _gsi;
	}
	
	public String getConnectionIpAddress()
	{
		return _connectionIPAddress;
	}
	
	public int getServerId()
	{
		if (getGameServerInfo() != null)
		{
			return getGameServerInfo().getId();
		}
		return -1;
	}
	
	public RSAPrivateKey getPrivateKey()
	{
		return _privateKey;
	}
	
	public void SetBlowFish(NewCrypt blowfish)
	{
		_blowfish = blowfish;
	}
	
	public void addAccountOnGameServer(String account)
	{
		_accountsOnGameServer.add(account);
	}
	
	public void removeAccountOnGameServer(String account)
	{
		_accountsOnGameServer.remove(account);
	}
	
	public GameServerState getLoginConnectionState()
	{
		return _loginConnectionState;
	}
	
	public void setLoginConnectionState(GameServerState state)
	{
		_loginConnectionState = state;
	}
}