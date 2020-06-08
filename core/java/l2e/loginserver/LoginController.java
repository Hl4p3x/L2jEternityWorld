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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import javolution.util.FastMap;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.loginserver.GameServerTable.GameServerInfo;
import l2e.loginserver.network.L2LoginClient;
import l2e.loginserver.network.gameserverpackets.ServerStatus;
import l2e.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import l2e.util.Base64;
import l2e.util.Rnd;
import l2e.util.crypt.ScrambledKeyPair;
import l2e.util.lib.Log;

public class LoginController
{
	protected static final Logger _log = Logger.getLogger(LoginController.class.getName());
	
	private static LoginController _instance;
	
	public static final int LOGIN_TIMEOUT = 60 * 1000;
	
	protected FastMap<String, L2LoginClient> _loginServerClients = new FastMap<String, L2LoginClient>().shared();
	
	private final Map<String, BanInfo> _bannedIps = new FastMap<String, BanInfo>().shared();
	
	private final Map<InetAddress, FailedLoginAttempt> _hackProtection;
	
	protected ScrambledKeyPair[] _keyPairs;
	
	private final Thread _purge;
	
	protected byte[][] _blowfishKeys;
	private static final int BLOWFISH_KEYS = 20;
	
	private static final String USER_INFO_SELECT = "SELECT password, IF(? > value OR value IS NULL, accessLevel, -1) AS accessLevel, lastServer FROM accounts LEFT JOIN (account_data) ON (account_data.account_name=accounts.login AND account_data.var=\"ban_temp\") WHERE login=?";
	private static final String AUTOCREATE_ACCOUNTS_INSERT = "INSERT INTO accounts (login, password, lastactive, accessLevel, lastIP) values (?, ?, ?, ?, ?)";
	private static final String ACCOUNT_INFO_UPDATE = "UPDATE accounts SET lastactive = ?, lastIP = ? WHERE login = ?";
	private static final String ACCOUNT_LAST_SERVER_UPDATE = "UPDATE accounts SET lastServer = ? WHERE login = ?";
	private static final String ACCOUNT_ACCESS_LEVEL_UPDATE = "UPDATE accounts SET accessLevel = ? WHERE login = ?";
	private static final String ACCOUNT_IPS_UPDATE = "UPDATE accounts SET pcIp = ?, hop1 = ?, hop2 = ?, hop3 = ?, hop4 = ? WHERE login = ?";
	private static final String ACCOUNT_IPAUTH_SELECT = "SELECT * FROM accounts_ipauth WHERE login = ?";

	public static void load() throws GeneralSecurityException
	{
		synchronized (LoginController.class)
		{
			if (_instance == null)
			{
				_instance = new LoginController();
			}
			else
			{
				throw new IllegalStateException("LoginController can only be loaded a single time.");
			}
		}
	}
	
	public static LoginController getInstance()
	{
		return _instance;
	}
	
	private LoginController() throws GeneralSecurityException
	{
		_log.info("Loading LoginController...");
		
		_hackProtection = new FastMap<>();
		
		_keyPairs = new ScrambledKeyPair[10];
		
		KeyPairGenerator keygen = null;
		
		keygen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
		keygen.initialize(spec);
		
		for (int i = 0; i < 10; i++)
		{
			_keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
		}
		_log.info("Cached 10 KeyPairs for RSA communication");
		
		testCipher((RSAPrivateKey) _keyPairs[0]._pair.getPrivate());
		
		generateBlowFishKeys();
		
		_purge = new PurgeThread();
		_purge.setDaemon(true);
		_purge.start();
	}
	
	private void testCipher(RSAPrivateKey key) throws GeneralSecurityException
	{
		Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
		rsaCipher.init(Cipher.DECRYPT_MODE, key);
	}
	
	private void generateBlowFishKeys()
	{
		_blowfishKeys = new byte[BLOWFISH_KEYS][16];
		
		for (int i = 0; i < BLOWFISH_KEYS; i++)
		{
			for (int j = 0; j < _blowfishKeys[i].length; j++)
			{
				_blowfishKeys[i][j] = (byte) (Rnd.nextInt(255) + 1);
			}
		}
		_log.info("Stored " + _blowfishKeys.length + " keys for Blowfish communication");
	}
	
	public byte[] getBlowfishKey()
	{
		return _blowfishKeys[(int) (Math.random() * BLOWFISH_KEYS)];
	}
	
	public SessionKey assignSessionKeyToClient(String account, L2LoginClient client)
	{
		SessionKey key;
		
		key = new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt());
		_loginServerClients.put(account, client);
		return key;
	}
	
	public void removeAuthedLoginClient(String account)
	{
		if (account == null)
			return;
		_loginServerClients.remove(account);
	}
	
	public L2LoginClient getAuthedClient(String account)
	{
		return _loginServerClients.get(account);
	}
	
	public static enum AuthLoginResult
	{
		INVALID_PASSWORD,
		ACCOUNT_BANNED,
		ALREADY_ON_LS,
		ALREADY_ON_GS,
		AUTH_SUCCESS
	}
	
	public AuthLoginResult tryAuthLogin(String account, String password, L2LoginClient client)
	{
		AuthLoginResult ret = AuthLoginResult.INVALID_PASSWORD;

		if (loginValid(account, password, client))
		{
			ret = AuthLoginResult.ALREADY_ON_GS;
			if (!isAccountInAnyGameServer(account))
			{
				ret = AuthLoginResult.ALREADY_ON_LS;
				
				if (_loginServerClients.putIfAbsent(account, client) == null)
				{
					ret = AuthLoginResult.AUTH_SUCCESS;
				}
			}
		}
		else
		{
			if (client.getAccessLevel() < 0)
			{
				ret = AuthLoginResult.ACCOUNT_BANNED;
			}
		}
		return ret;
	}
	
	public void addBanForAddress(String address, long expiration) throws UnknownHostException
	{
		InetAddress netAddress = InetAddress.getByName(address);
		if (!_bannedIps.containsKey(netAddress.getHostAddress()))
			_bannedIps.put(netAddress.getHostAddress(), new BanInfo(netAddress, expiration));
	}
	
	public void addBanForAddress(InetAddress address, long duration)
	{
		if (!_bannedIps.containsKey(address.getHostAddress()))
			_bannedIps.put(address.getHostAddress(), new BanInfo(address, System.currentTimeMillis() + duration));
	}
	
	public boolean isBannedAddress(InetAddress address)
	{
		String[] parts = address.getHostAddress().split("\\.");
		BanInfo bi = _bannedIps.get(address.getHostAddress());
		if (bi == null)
			bi = _bannedIps.get(parts[0] + "." + parts[1] + "." + parts[2] + ".0");
		if (bi == null)
			bi = _bannedIps.get(parts[0] + "." + parts[1] + ".0.0");
		if (bi == null)
			bi = _bannedIps.get(parts[0] + ".0.0.0");
		if (bi != null)
		{
			if (bi.hasExpired())
			{
				_bannedIps.remove(address.getHostAddress());
				return false;
			}
			return true;
		}
		return false;
	}
	
	public Map<String, BanInfo> getBannedIps()
	{
		return _bannedIps;
	}
	
	public boolean removeBanForAddress(InetAddress address)
	{
		return _bannedIps.remove(address.getHostAddress()) != null;
	}
	
	public boolean removeBanForAddress(String address)
	{
		try
		{
			return this.removeBanForAddress(InetAddress.getByName(address));
		}
		catch (UnknownHostException e)
		{
			return false;
		}
	}
	
	public SessionKey getKeyForAccount(String account)
	{
		L2LoginClient client = _loginServerClients.get(account);
		if (client != null)
		{
			return client.getSessionKey();
		}
		return null;
	}
	
	public boolean isAccountInAnyGameServer(String account)
	{
		Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
		for (GameServerInfo gsi : serverList)
		{
			GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
			{
				return true;
			}
		}
		return false;
	}
	
	public GameServerInfo getAccountOnGameServer(String account)
	{
		Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
		for (GameServerInfo gsi : serverList)
		{
			GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
			{
				return gsi;
			}
		}
		return null;
	}
	
	public void getCharactersOnAccount(String account)
	{
		Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
		for (GameServerInfo gsi : serverList)
		{
			if (gsi.isAuthed())
				gsi.getGameServerThread().requestCharacters(account);
		}
	}
	
	public boolean isLoginPossible(L2LoginClient client, int serverId)
	{
		GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
		int access = client.getAccessLevel();
		if ((gsi != null) && gsi.isAuthed())
		{
			boolean loginOk = ((gsi.getCurrentPlayerCount() < gsi.getMaxPlayers()) && (gsi.getStatus() != ServerStatus.STATUS_GM_ONLY)) || (access > 0);
			
			if (loginOk && (client.getLastServer() != serverId))
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement ps = con.prepareStatement(ACCOUNT_LAST_SERVER_UPDATE))
				{
					ps.setInt(1, serverId);
					ps.setString(2, client.getAccount());
					ps.executeUpdate();
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "Could not set lastServer: " + e.getMessage(), e);
				}
			}
			return loginOk;
		}
		return false;
	}
	
	public void setAccountAccessLevel(String account, int banLevel)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(ACCOUNT_ACCESS_LEVEL_UPDATE))
		{
			ps.setInt(1, banLevel);
			ps.setString(2, account);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not set accessLevel: " + e.getMessage(), e);
		}
	}
	
	public void setAccountLastTracert(String account, String pcIp, String hop1, String hop2, String hop3, String hop4)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(ACCOUNT_IPS_UPDATE))
		{
			ps.setString(1, pcIp);
			ps.setString(2, hop1);
			ps.setString(3, hop2);
			ps.setString(4, hop3);
			ps.setString(5, hop4);
			ps.setString(6, account);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not set last tracert: " + e.getMessage(), e);
		}
	}
	
	public void setCharactersOnServer(String account, int charsNum, long[] timeToDel, int serverId)
	{
		L2LoginClient client = _loginServerClients.get(account);
		
		if (client == null)
			return;
		
		if (charsNum > 0)
			client.setCharsOnServ(serverId, charsNum);
		
		if (timeToDel.length > 0)
			client.serCharsWaitingDelOnServ(serverId, timeToDel);
	}
	
	public ScrambledKeyPair getScrambledRSAKeyPair()
	{
		return _keyPairs[Rnd.nextInt(10)];
	}
	
	public boolean loginValid(String user, String password, L2LoginClient client)
	{
		boolean ok = false;
		InetAddress address = client.getConnection().getInetAddress();
		
		if (address == null || user == null)
		{
			return false;
		}
		
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] raw = password.getBytes("UTF-8");
			byte[] hash = md.digest(raw);
			
			byte[] expected = null;
			int access = 0;
			int lastServer = 1;
			List<InetAddress> ipWhiteList = new ArrayList<>();
			List<InetAddress> ipBlackList = new ArrayList<>();
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(USER_INFO_SELECT))
			{
				ps.setString(1, Long.toString(System.currentTimeMillis()));
				ps.setString(2, user);
				try (ResultSet rset = ps.executeQuery())
				{
					if (rset.next())
					{
						expected = Base64.decode(rset.getString("password"));
						access = rset.getInt("accessLevel");
						lastServer = rset.getInt("lastServer");
						if (lastServer <= 0)
						{
							lastServer = 1;
						}
						if (Config.DEBUG)
						{
							_log.fine("account exists");
						}
					}
				}
			}
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(ACCOUNT_IPAUTH_SELECT))
			{
				ps.setString(1, user);
				try (ResultSet rset = ps.executeQuery())
				{
					String ip, type;
					while (rset.next())
					{
						ip = rset.getString("ip");
						type = rset.getString("type");
						
						if (!isValidIPAddress(ip))
						{
							continue;
						}
						else if (type.equals("allow"))
						{
							ipWhiteList.add(InetAddress.getByName(ip));
						}
						else if (type.equals("deny"))
						{
							ipBlackList.add(InetAddress.getByName(ip));
						}
					}
				}
			}
			
			if (expected == null)
			{
				if (Config.AUTO_CREATE_ACCOUNTS)
				{
					if ((user.length() >= 2) && (user.length() <= 14))
					{
						try (Connection con = L2DatabaseFactory.getInstance().getConnection();
							PreparedStatement ps = con.prepareStatement(AUTOCREATE_ACCOUNTS_INSERT))
						{
							ps.setString(1, user);
							ps.setString(2, Base64.encodeBytes(hash));
							ps.setLong(3, System.currentTimeMillis());
							ps.setInt(4, 0);
							ps.setString(5, address.getHostAddress());
							ps.execute();
						}
						
						if (Config.LOG_LOGIN_CONTROLLER)
							Log.add("'" + user + "' " + address.getHostAddress() + " - OK : AccountCreate", "loginlog");
						
						_log.info("Created new account for " + user);
						return true;
						
					}
					if (Config.LOG_LOGIN_CONTROLLER)
						Log.add("'" + user + "' " + address.getHostAddress() + " - ERR : ErrCreatingACC", "loginlog");
					
					_log.warning("Invalid username creation/use attempt: " + user);
				}
				else
				{
					if (Config.LOG_LOGIN_CONTROLLER)
						Log.add("'" + user + "' " + address.getHostAddress() + " - ERR : AccountMissing", "loginlog");
					
					_log.warning("Account missing for user " + user);
					FailedLoginAttempt failedAttempt = _hackProtection.get(address);
					int failedCount;
					if (failedAttempt == null)
					{
						_hackProtection.put(address, new FailedLoginAttempt(address, password));
						failedCount = 1;
					}
					else
					{
						failedAttempt.increaseCounter();
						failedCount = failedAttempt.getCount();
					}
					
					if (failedCount >= Config.LOGIN_TRY_BEFORE_BAN)
					{
						_log.info("Banning '" + address.getHostAddress() + "' for " + Config.LOGIN_BLOCK_AFTER_BAN + " seconds due to " + failedCount + " invalid user name attempts");
						this.addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
					}
				}
				return false;
			}
			
			if (access < 0)
			{
				if (Config.LOG_LOGIN_CONTROLLER)
					Log.add("'" + user + "' " + address.getHostAddress() + " - ERR : AccountBanned", "loginlog");
				
				client.setAccessLevel(access);
				return false;
			}

			if (!ipWhiteList.isEmpty() || !ipBlackList.isEmpty())
			{
				if (!ipWhiteList.isEmpty() && !ipWhiteList.contains(address))
				{
					if (Config.LOG_LOGIN_CONTROLLER)
						Log.add("'" + user + "' " + address.getHostAddress() + " - ERR : INCORRECT IP", "loginlog");
					return false;
				}
				
				if (!ipBlackList.isEmpty() && ipBlackList.contains(address))
				{
					if (Config.LOG_LOGIN_CONTROLLER)
						Log.add("'" + user + "' " + address.getHostAddress() + " - ERR : BLACKLISTED IP", "loginlog");
					return false;
				}
			}

			ok = Arrays.equals(hash, expected);
			
			if (ok)
			{
				client.setAccessLevel(access);
				client.setLastServer(lastServer);
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement ps = con.prepareStatement(ACCOUNT_INFO_UPDATE))
				{
					ps.setLong(1, System.currentTimeMillis());
					ps.setString(2, address.getHostAddress());
					ps.setString(3, user);
					ps.execute();
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not check password:" + e.getMessage(), e);
			ok = false;
		}
		
		if (!ok)
		{
			if (Config.LOG_LOGIN_CONTROLLER)
				Log.add("'" + user + "' " + address.getHostAddress() + " - ERR : LoginFailed", "loginlog");
			
			FailedLoginAttempt failedAttempt = _hackProtection.get(address);
			int failedCount;
			if (failedAttempt == null)
			{
				_hackProtection.put(address, new FailedLoginAttempt(address, password));
				failedCount = 1;
			}
			else
			{
				failedAttempt.increaseCounter(password);
				failedCount = failedAttempt.getCount();
			}
			
			if (failedCount >= Config.LOGIN_TRY_BEFORE_BAN)
			{
				_log.info("Banning '" + address.getHostAddress() + "' for " + Config.LOGIN_BLOCK_AFTER_BAN + " seconds due to " + failedCount + " invalid user/pass attempts");
				this.addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
			}
		}
		else
		{
			_hackProtection.remove(address);
			if (Config.LOG_LOGIN_CONTROLLER)
				Log.add("'" + user + "' " + address.getHostAddress() + " - OK : LoginOk", "loginlog");
		}
		
		return ok;
	}

	public boolean isValidIPAddress(String  ipAddress)
	{
		String[] parts = ipAddress.split("\\.");
		if (parts.length != 4)
			return false;

		for (String s : parts)
		{
			int i = Integer.parseInt(s);
			if ((i < 0) || (i > 255))
				return false;
		}
		return true;
	}

	class FailedLoginAttempt
	{
		private int _count;
		private long _lastAttempTime;
		private String _lastPassword;
		
		public FailedLoginAttempt(InetAddress address, String lastPassword)
		{
			_count = 1;
			_lastAttempTime = System.currentTimeMillis();
			_lastPassword = lastPassword;
		}
		
		public void increaseCounter(String password)
		{
			if (!_lastPassword.equals(password))
			{
				if (System.currentTimeMillis() - _lastAttempTime < 300 * 1000)
				{
					_count++;
				}
				else
				{
					_count = 1;
					
				}
				_lastPassword = password;
				_lastAttempTime = System.currentTimeMillis();
			}
			else
			{
				_lastAttempTime = System.currentTimeMillis();
			}
		}
		
		public int getCount()
		{
			return _count;
		}
		
		public void increaseCounter()
		{
			_count++;
		}
		
	}
	
	class BanInfo
	{
		private final InetAddress _ipAddress;
		private final long _expiration;
		
		public BanInfo(InetAddress ipAddress, long expiration)
		{
			_ipAddress = ipAddress;
			_expiration = expiration;
		}
		
		public InetAddress getAddress()
		{
			return _ipAddress;
		}
		
		public boolean hasExpired()
		{
			return System.currentTimeMillis() > _expiration && _expiration > 0;
		}
	}
	
	class PurgeThread extends Thread
	{
		public PurgeThread()
		{
			setName("PurgeThread");
		}
		@Override
		public void run()
		{
			while (!isInterrupted())
			{
				for (L2LoginClient client : _loginServerClients.values())
				{
					if (client == null)
						continue;
					if ((client.getConnectionStartTime() + LOGIN_TIMEOUT) < System.currentTimeMillis())
					{
						client.close(LoginFailReason.REASON_ACCESS_FAILED);
					}
				}
				
				try
				{
					Thread.sleep(LOGIN_TIMEOUT / 2);
				}
				catch (InterruptedException e)
				{
					return;
				}
			}
		}
	}
}