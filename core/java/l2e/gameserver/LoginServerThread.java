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
package l2e.gameserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.L2GameClient.GameClientState;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.gameserverpackets.AuthRequest;
import l2e.gameserver.network.gameserverpackets.BlowFishKey;
import l2e.gameserver.network.gameserverpackets.ChangeAccessLevel;
import l2e.gameserver.network.gameserverpackets.ChangePassword;
import l2e.gameserver.network.gameserverpackets.PlayerAuthRequest;
import l2e.gameserver.network.gameserverpackets.PlayerInGame;
import l2e.gameserver.network.gameserverpackets.PlayerLogout;
import l2e.gameserver.network.gameserverpackets.PlayerTracert;
import l2e.gameserver.network.gameserverpackets.ReplyCharacters;
import l2e.gameserver.network.gameserverpackets.SendMail;
import l2e.gameserver.network.gameserverpackets.ServerStatus;
import l2e.gameserver.network.gameserverpackets.TempBan;
import l2e.gameserver.network.loginserverpackets.AuthResponse;
import l2e.gameserver.network.loginserverpackets.ChangePasswordResponse;
import l2e.gameserver.network.loginserverpackets.InitLS;
import l2e.gameserver.network.loginserverpackets.KickPlayer;
import l2e.gameserver.network.loginserverpackets.LoginServerFail;
import l2e.gameserver.network.loginserverpackets.PlayerAuthResponse;
import l2e.gameserver.network.loginserverpackets.RequestCharacters;
import l2e.gameserver.network.serverpackets.CharSelectionInfo;
import l2e.gameserver.network.serverpackets.LoginFail;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.Util;
import l2e.util.crypt.NewCrypt;
import l2e.util.network.BaseSendablePacket;

public class LoginServerThread extends Thread
{
	protected static final Logger _log = Logger.getLogger(LoginServerThread.class.getName());
	protected static final Logger _logAccounting = Logger.getLogger("accounting");
	
	private static final int REVISION = 0x0106;
	private RSAPublicKey _publicKey;
	private final String _hostname;
	private final int _port;
	private final int _gamePort;
	private Socket _loginSocket;
	private InputStream _in;
	private OutputStream _out;
	
	private NewCrypt _blowfish;
	private byte[] _blowfishKey;
	private byte[] _hexID;
	private final boolean _acceptAlternate;
	private int _requestID;
	private int _serverID;
	private final boolean _reserveHost;
	private int _maxPlayer;
	private final List<WaitingClient> _waitingClients;
	private final FastMap<String, L2GameClient> _accountsInGameServer = new FastMap<>();
	private int _status;
	private String _serverName;
	private final List<String> _subnets;
	private final List<String> _hosts;
	
	public static final boolean _demo = false;
	
	protected LoginServerThread()
	{
		super("LoginServerThread");
		_port = Config.GAME_SERVER_LOGIN_PORT;
		_gamePort = Config.PORT_GAME;
		_hostname = Config.GAME_SERVER_LOGIN_HOST;
		_hexID = Config.HEX_ID;
		if (_hexID == null)
		{
			_requestID = Config.REQUEST_ID;
			_hexID = Util.generateHex(16);
		}
		else
		{
			_requestID = Config.SERVER_ID;
		}
		_acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		_reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		_subnets = Config.GAME_SERVER_SUBNETS;
		_hosts = Config.GAME_SERVER_HOSTS;
		_waitingClients = new FastList<>();
		_accountsInGameServer.shared();
		_maxPlayer = Config.MAXIMUM_ONLINE_USERS;
	}
	
	public static LoginServerThread getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@Override
	public void run()
	{
		while (!isInterrupted())
		{
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			try
			{
				_log.info("Connecting to login on " + _hostname + ":" + _port);
				_loginSocket = new Socket(_hostname, _port);
				_in = _loginSocket.getInputStream();
				_out = new BufferedOutputStream(_loginSocket.getOutputStream());
				_blowfishKey = Util.generateHex(40);
				_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				while (!isInterrupted())
				{
					lengthLo = _in.read();
					lengthHi = _in.read();
					length = (lengthHi * 256) + lengthLo;
					
					if (lengthHi < 0)
					{
						_log.finer("LoginServerThread: Login terminated the connection.");
						break;
					}
					
					byte[] incoming = new byte[length - 2];
					
					int receivedBytes = 0;
					int newBytes = 0;
					int left = length - 2;
					while ((newBytes != -1) && (receivedBytes < (length - 2)))
					{
						newBytes = _in.read(incoming, receivedBytes, left);
						receivedBytes = receivedBytes + newBytes;
						left -= newBytes;
					}
					
					if (receivedBytes != (length - 2))
					{
						_log.warning("Incomplete Packet is sent to the server, closing connection.(LS)");
						break;
					}
					_blowfish.decrypt(incoming, 0, incoming.length);
					checksumOk = NewCrypt.verifyChecksum(incoming);
					
					if (!checksumOk)
					{
						_log.warning("Incorrect packet checksum, ignoring packet (LS)");
						break;
					}
					
					int packetType = incoming[0] & 0xff;
					switch (packetType)
					{
						case 0x00:
							InitLS init = new InitLS(incoming);
							if (init.getRevision() != REVISION)
							{
								_log.warning("/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							try
							{
								KeyFactory kfac = KeyFactory.getInstance("RSA");
								BigInteger modulus = new BigInteger(init.getRSAKey());
								RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
								_publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
							}
							
							catch (GeneralSecurityException e)
							{
								_log.warning("Troubles while init the public key send by login");
								break;
							}
							BlowFishKey bfk = new BlowFishKey(_blowfishKey, _publicKey);
							sendPacket(bfk);
							_blowfish = new NewCrypt(_blowfishKey);
							AuthRequest ar = new AuthRequest(_requestID, _acceptAlternate, _hexID, _gamePort, _reserveHost, _maxPlayer, _subnets, _hosts);
							sendPacket(ar);
							break;
						case 0x01:
							LoginServerFail lsf = new LoginServerFail(incoming);
							_log.info("Damn! Registeration Failed: " + lsf.getReasonString());
							break;
						case 0x02:
							AuthResponse aresp = new AuthResponse(incoming);
							_serverID = aresp.getServerId();
							_serverName = aresp.getServerName();
							Config.saveHexid(_serverID, hexToString(_hexID));
							_log.info("Registered on login as Server " + _serverID + " : " + _serverName);
							ServerStatus st = new ServerStatus();
							if (Config.SERVER_LIST_BRACKET)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.OFF);
							}
							st.addAttribute(ServerStatus.SERVER_TYPE, Config.SERVER_LIST_TYPE);
							if (Config.SERVER_GMONLY)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
							}
							if (Config.SERVER_LIST_AGE == 15)
							{
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_15);
							}
							else if (Config.SERVER_LIST_AGE == 18)
							{
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_18);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_ALL);
							}
							sendPacket(st);
							if (L2World.getInstance().getAllPlayersCount() > 0)
							{
								FastList<String> playerList = new FastList<>();
								for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
								{
									playerList.add(player.getAccountName());
								}
								PlayerInGame pig = new PlayerInGame(playerList);
								sendPacket(pig);
							}
							break;
						case 0x03:
							PlayerAuthResponse par = new PlayerAuthResponse(incoming);
							String account = par.getAccount();
							WaitingClient wcToRemove = null;
							synchronized (_waitingClients)
							{
								for (WaitingClient wc : _waitingClients)
								{
									if (wc.account.equals(account))
									{
										wcToRemove = wc;
									}
								}
							}
							if (wcToRemove != null)
							{
								if (par.isAuthed())
								{
									PlayerInGame pig = new PlayerInGame(par.getAccount());
									sendPacket(pig);
									wcToRemove.gameClient.setState(GameClientState.AUTHED);
									wcToRemove.gameClient.setSessionId(wcToRemove.session);
									CharSelectionInfo cl = new CharSelectionInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
									wcToRemove.gameClient.getConnection().sendPacket(cl);
									wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
								}
								else
								{
									_log.warning("Session key is not correct. Closing connection for account " + wcToRemove.account + ".");
									wcToRemove.gameClient.close(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
									_accountsInGameServer.remove(wcToRemove.account);
								}
								_waitingClients.remove(wcToRemove);
							}
							break;
						case 0x04:
							KickPlayer kp = new KickPlayer(incoming);
							doKickPlayer(kp.getAccount());
							break;
						case 0x05:
							RequestCharacters rc = new RequestCharacters(incoming);
							getCharsOnServer(rc.getAccount());
							break;
						case 0x06:
							new ChangePasswordResponse(incoming);
							break;
					}
				}
			}
			catch (UnknownHostException e)
			{
				_log.log(Level.WARNING, "", e);
			}
			catch (SocketException e)
			{
				_log.warning("LoginServer not avaible, trying to reconnect...");
			}
			catch (IOException e)
			{
				_log.log(Level.WARNING, "Disconnected from Login, Trying to reconnect: " + e.getMessage(), e);
			}
			finally
			{
				try
				{
					_loginSocket.close();
					if (isInterrupted())
					{
						return;
					}
				}
				catch (Exception e)
				{
				}
			}
			
			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				return;
			}
		}
	}
	
	public void addWaitingClientAndSendRequest(String acc, L2GameClient client, SessionKey key)
	{
		WaitingClient wc = new WaitingClient(acc, client, key);
		synchronized (_waitingClients)
		{
			_waitingClients.add(wc);
		}
		PlayerAuthRequest par = new PlayerAuthRequest(acc, key);
		try
		{
			sendPacket(par);
		}
		catch (IOException e)
		{
			_log.warning("Error while sending player auth request");
		}
	}
	
	public void removeWaitingClient(L2GameClient client)
	{
		WaitingClient toRemove = null;
		synchronized (_waitingClients)
		{
			for (WaitingClient c : _waitingClients)
			{
				if (c.gameClient == client)
				{
					toRemove = c;
				}
			}
			if (toRemove != null)
			{
				_waitingClients.remove(toRemove);
			}
		}
	}
	
	public void sendLogout(String account)
	{
		if (account == null)
		{
			return;
		}
		
		PlayerLogout pl = new PlayerLogout(account);
		try
		{
			sendPacket(pl);
		}
		catch (IOException e)
		{
			_log.warning("Error while sending logout packet to login");
		}
		finally
		{
			_accountsInGameServer.remove(account);
		}
	}
	
	public void addGameServerLogin(String account, L2GameClient client)
	{
		_accountsInGameServer.put(account, client);
	}
	
	public void sendAccessLevel(String account, int level)
	{
		ChangeAccessLevel cal = new ChangeAccessLevel(account, level);
		try
		{
			sendPacket(cal);
		}
		catch (IOException e)
		{
		}
	}
	
	public void sendClientTracert(String account, String[] address)
	{
		PlayerTracert ptc = new PlayerTracert(account, address[0], address[1], address[2], address[3], address[4]);
		try
		{
			sendPacket(ptc);
		}
		catch (IOException e)
		{
		}
	}
	
	public void sendMail(String account, String mailId, String... args)
	{
		SendMail sem = new SendMail(account, mailId, args);
		try
		{
			sendPacket(sem);
		}
		catch (IOException e)
		{
		}
	}
	
	public void sendTempBan(String account, String ip, long time)
	{
		TempBan tbn = new TempBan(account, ip, time);
		try
		{
			sendPacket(tbn);
		}
		catch (IOException e)
		{
		}
	}
	
	private String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}
	
	public void doKickPlayer(String account)
	{
		L2GameClient client = _accountsInGameServer.get(account);
		if (client != null)
		{
			LogRecord record = new LogRecord(Level.WARNING, "Kicked by login");
			record.setParameters(new Object[]
			{
				client
			});
			_logAccounting.log(record);
			client.setAditionalClosePacket(SystemMessage.getSystemMessage(SystemMessageId.ANOTHER_LOGIN_WITH_ACCOUNT));
			client.closeNow();
		}
	}
	
	private void getCharsOnServer(String account)
	{
		
		int chars = 0;
		List<Long> charToDel = new ArrayList<>();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT deletetime FROM characters WHERE account_name=?"))
		{
			ps.setString(1, account);
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					chars++;
					long delTime = rs.getLong("deletetime");
					if (delTime != 0)
					{
						charToDel.add(delTime);
					}
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "Exception: getCharsOnServer: " + e.getMessage(), e);
		}
		
		ReplyCharacters rec = new ReplyCharacters(account, chars, charToDel);
		try
		{
			sendPacket(rec);
		}
		catch (IOException e)
		{
		}
	}
	
	private void sendPacket(BaseSendablePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		_blowfish.crypt(data, 0, data.length);
		
		int len = data.length + 2;
		synchronized (_out)
		{
			_out.write(len & 0xff);
			_out.write((len >> 8) & 0xff);
			_out.write(data);
			_out.flush();
		}
	}
	
	public void setMaxPlayer(int maxPlayer)
	{
		sendServerStatus(ServerStatus.MAX_PLAYERS, maxPlayer);
		_maxPlayer = maxPlayer;
	}
	
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}
	
	public void sendServerStatus(int id, int value)
	{
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(id, value);
		try
		{
			sendPacket(ss);
		}
		catch (IOException e)
		{
		}
	}
	
	public void sendServerType()
	{
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(ServerStatus.SERVER_TYPE, Config.SERVER_LIST_TYPE);
		try
		{
			sendPacket(ss);
		}
		catch (IOException e)
		{
		}
	}
	
	public void sendChangePassword(String accountName, String charName, String oldpass, String newpass)
	{
		ChangePassword cp = new ChangePassword(accountName, charName, oldpass, newpass);
		try
		{
			sendPacket(cp);
		}
		catch (IOException e)
		{
		}
	}
	
	public String getStatusString()
	{
		return ServerStatus.STATUS_STRING[_status];
	}
	
	public String getServerName()
	{
		return _serverName;
	}
	
	public void setServerStatus(int status)
	{
		switch (status)
		{
			case ServerStatus.STATUS_AUTO:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
				_status = status;
				break;
			case ServerStatus.STATUS_DOWN:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_DOWN);
				_status = status;
				break;
			case ServerStatus.STATUS_FULL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_FULL);
				_status = status;
				break;
			case ServerStatus.STATUS_GM_ONLY:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
				_status = status;
				break;
			case ServerStatus.STATUS_GOOD:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GOOD);
				_status = status;
				break;
			case ServerStatus.STATUS_NORMAL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_NORMAL);
				_status = status;
				break;
			default:
				throw new IllegalArgumentException("Status does not exists:" + status);
		}
	}
	
	public L2GameClient getClient(String name)
	{
		return name != null ? _accountsInGameServer.get(name) : null;
	}
	
	public static class SessionKey
	{
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;
		
		public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
		{
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}
		
		@Override
		public String toString()
		{
			return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
		}
	}
	
	private static class WaitingClient
	{
		public String account;
		public L2GameClient gameClient;
		public SessionKey session;
		
		public WaitingClient(String acc, L2GameClient client, SessionKey key)
		{
			account = acc;
			gameClient = client;
			session = key;
		}
	}
	
	private static class SingletonHolder
	{
		protected static final LoginServerThread _instance = new LoginServerThread();
	}
}