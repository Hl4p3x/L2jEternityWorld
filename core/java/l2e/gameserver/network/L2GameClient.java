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
package l2e.gameserver.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.LoginServerThread;
import l2e.gameserver.LoginServerThread.SessionKey;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.instancemanager.AntiFeedManager;
import l2e.gameserver.model.CharSelectInfoPackage;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.L2Event;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.CharInfo;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.ServerClose;
import l2e.gameserver.security.SecondaryPasswordAuth;
import l2e.gameserver.util.FloodProtectors;
import l2e.gameserver.util.Util;
import l2e.protection.Protection;

import org.mmocore.network.MMOClient;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.ReceivablePacket;

public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>> implements Runnable
{
	protected static final Logger _log = Logger.getLogger(L2GameClient.class.getName());
	protected static final Logger _logAccounting = Logger.getLogger("accounting");
	
	public static enum GameClientState
	{
		CONNECTED,
		AUTHED,
		IN_GAME
	}
	
	private GameClientState _state;
	
	private final InetAddress _addr;
	private String _accountName;
	private SessionKey _sessionId;
	private L2PcInstance _activeChar;
	private final ReentrantLock _activeCharLock = new ReentrantLock();
	private SecondaryPasswordAuth _secondaryAuth;
	
	private boolean _isAuthedGG;
	private final long _connectionStartTime;
	private CharSelectInfoPackage[] _charSlotMapping = null;
	
	private final FloodProtectors _floodProtectors = new FloodProtectors(this);
	
	protected final ScheduledFuture<?> _autoSaveInDB;
	protected ScheduledFuture<?> _cleanupTask = null;
	
	private L2GameServerPacket _aditionalClosePacket;
	
	public GameCrypt _crypt = null;
	private String _hwid = "";
	private String _loginName = "";
	private String _playerName = "";
	private int _playerId = 0;
	private int revision = 0;
	private int playerID;
	private String login;
	private LockType lockType;
	
	public static enum LockType
	{
		PLAYER_LOCK,
		ACCOUNT_LOCK,
		NONE
	}
	
	private final ClientStats _stats;
	private boolean _isDetached = false;
	private boolean _protocol;
	
	private final ArrayBlockingQueue<ReceivablePacket<L2GameClient>> _packetQueue;
	private final ReentrantLock _queueLock = new ReentrantLock();
	
	private int[][] trace;
	
	public L2GameClient(MMOConnection<L2GameClient> con)
	{
		super(con);
		_state = GameClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();
		_stats = new ClientStats();
		_crypt = new GameCrypt();
		_packetQueue = new ArrayBlockingQueue<>(Config.CLIENT_PACKET_QUEUE_SIZE);
		
		if (Config.CHAR_STORE_INTERVAL > 0)
		{
			_autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), 300000L, (Config.CHAR_STORE_INTERVAL * 60000L));
		}
		else
		{
			_autoSaveInDB = null;
		}
		
		try
		{
			_addr = con != null ? con.getInetAddress() : InetAddress.getLocalHost();
		}
		catch (UnknownHostException e)
		{
			throw new Error("Unable to determine localhost address.");
		}
	}
	
	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		
		if (Protection.isProtectionOn())
		{
			key = Protection.getKey(key);
		}
		return key;
	}
	
	public GameClientState getState()
	{
		return _state;
	}
	
	public void setState(GameClientState pState)
	{
		if (_state != pState)
		{
			_state = pState;
			_packetQueue.clear();
		}
	}
	
	public ClientStats getStats()
	{
		return _stats;
	}
	
	public InetAddress getConnectionAddress()
	{
		return _addr;
	}
	
	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}
	
	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		_crypt.decrypt(buf.array(), buf.position(), size);
		return true;
	}
	
	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		_crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}
	
	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}
	
	public void setActiveChar(L2PcInstance pActiveChar)
	{
		_activeChar = pActiveChar;
	}
	
	public ReentrantLock getActiveCharLock()
	{
		return _activeCharLock;
	}
	
	public FloodProtectors getFloodProtectors()
	{
		return _floodProtectors;
	}
	
	public void setGameGuardOk(boolean val)
	{
		_isAuthedGG = val;
	}
	
	public boolean isAuthedGG()
	{
		return _isAuthedGG;
	}
	
	public void setAccountName(String pAccountName)
	{
		_accountName = pAccountName;
		
		if (Config.SECOND_AUTH_ENABLED)
		{
			_secondaryAuth = new SecondaryPasswordAuth(this);
		}
	}
	
	public String getAccountName()
	{
		return _accountName;
	}
	
	public void setSessionId(SessionKey sk)
	{
		_sessionId = sk;
	}
	
	public SessionKey getSessionId()
	{
		return _sessionId;
	}
	
	public void sendPacket(L2GameServerPacket gsp)
	{
		if (_isDetached || (gsp == null))
		{
			return;
		}
		
		if (gsp.isInvisible() && (getActiveChar() != null) && !getActiveChar().canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS))
		{
			return;
		}
		
		getConnection().sendPacket(gsp);
		gsp.runImpl();
	}
	
	public boolean isDetached()
	{
		return _isDetached;
	}
	
	public void setDetached(boolean b)
	{
		_isDetached = b;
	}
	
	public byte markToDeleteChar(int charslot)
	{
		int objid = getObjectIdForSlot(charslot);
		
		if (objid < 0)
		{
			return -1;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT clanId FROM characters WHERE charId=?"))
		{
			statement.setInt(1, objid);
			byte answer = 0;
			try (ResultSet rs = statement.executeQuery())
			{
				int clanId = rs.next() ? rs.getInt(1) : 0;
				if (clanId != 0)
				{
					L2Clan clan = ClanHolder.getInstance().getClan(clanId);
					
					if (clan == null)
					{
						answer = 0;
					}
					else if (clan.getLeaderId() == objid)
					{
						answer = 2;
					}
					else
					{
						answer = 1;
					}
				}
				
				if (answer == 0)
				{
					if (Config.DELETE_DAYS == 0)
					{
						deleteCharByObjId(objid);
					}
					else
					{
						try (PreparedStatement ps2 = con.prepareStatement("UPDATE characters SET deletetime=? WHERE charId=?"))
						{
							ps2.setLong(1, System.currentTimeMillis() + (Config.DELETE_DAYS * 86400000L));
							ps2.setInt(2, objid);
							ps2.execute();
						}
					}
					
					LogRecord record = new LogRecord(Level.WARNING, "Delete");
					record.setParameters(new Object[]
					{
						objid,
						this
					});
					_logAccounting.log(record);
				}
			}
			return answer;
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error updating delete time of character.", e);
			return -1;
		}
	}
	
	public void saveCharToDisk()
	{
		try
		{
			if (getActiveChar() != null)
			{
				getActiveChar().store();
				getActiveChar().storeRecommendations();
				if (Config.UPDATE_ITEMS_ON_CHAR_STORE)
				{
					getActiveChar().getInventory().updateDatabase();
					getActiveChar().getWarehouse().updateDatabase();
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error saving character..", e);
		}
	}
	
	public void markRestoredChar(int charslot)
	{
		final int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE charId=?"))
		{
			statement.setInt(1, objid);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error restoring character.", e);
		}
		
		final LogRecord record = new LogRecord(Level.WARNING, "Restore");
		record.setParameters(new Object[]
		{
			objid,
			this
		});
		_logAccounting.log(record);
	}
	
	public static void deleteCharByObjId(int objid)
	{
		if (objid < 0)
		{
			return;
		}
		
		CharNameHolder.getInstance().removeName(objid);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_contacts WHERE charId=? OR contactId=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_friends WHERE charId=? OR friendId=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_hennas WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_macroses WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_quest_global_data WHERE charId=?");
			statement.setInt(1, objid);
			statement.executeUpdate();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_skills WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM heroes WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM seven_signs WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_raid_points WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_reco_bonus WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_instance_time WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_variable WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM characters WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			if (Config.ALLOW_WEDDING)
			{
				statement = con.prepareStatement("DELETE FROM mods_wedding WHERE player1Id = ? OR player2Id = ?");
				statement.setInt(1, objid);
				statement.setInt(2, objid);
				statement.execute();
				statement.close();
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error deleting character.", e);
		}
	}
	
	public L2PcInstance loadCharFromDisk(int charslot)
	{
		final int objId = getObjectIdForSlot(charslot);
		if (objId < 0)
		{
			return null;
		}
		
		L2PcInstance character = L2World.getInstance().getPlayer(objId);
		if (character != null)
		{
			_log.severe("Attempt of double login: " + character.getName() + "(" + objId + ") " + getAccountName());
			if (character.getClient() != null)
			{
				character.getClient().closeNow();
			}
			else
			{
				character.deleteMe();
			}
			return null;
		}
		
		character = L2PcInstance.load(objId);
		if (character != null)
		{
			character.setRunning();
			character.standUp();
			
			character.refreshOverloaded();
			character.refreshExpertisePenalty();
			character.setOnlineStatus(true, false);
		}
		else
		{
			_log.severe("could not restore in slot: " + charslot);
		}
		return character;
	}
	
	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping = chars;
	}
	
	public CharSelectInfoPackage getCharSelection(int charslot)
	{
		if ((_charSlotMapping == null) || (charslot < 0) || (charslot >= _charSlotMapping.length))
		{
			return null;
		}
		return _charSlotMapping[charslot];
	}
	
	public SecondaryPasswordAuth getSecondaryAuth()
	{
		return _secondaryAuth;
	}
	
	public void close(L2GameServerPacket gsp)
	{
		if (getConnection() == null)
		{
			return;
		}
		if (_aditionalClosePacket != null)
		{
			getConnection().close(new L2GameServerPacket[]
			{
				_aditionalClosePacket,
				gsp
			});
		}
		else
		{
			getConnection().close(gsp);
		}
	}
	
	public void close(L2GameServerPacket[] gspArray)
	{
		if (getConnection() == null)
		{
			return;
		}
		getConnection().close(gspArray);
	}
	
	private int getObjectIdForSlot(int charslot)
	{
		final CharSelectInfoPackage info = getCharSelection(charslot);
		if (info == null)
		{
			_log.warning(toString() + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		return info.getObjectId();
	}
	
	@Override
	protected void onForcedDisconnection()
	{
		LogRecord record = new LogRecord(Level.WARNING, "Disconnected abnormally");
		record.setParameters(new Object[]
		{
			this
		});
		_logAccounting.log(record);
	}
	
	@Override
	protected void onDisconnection()
	{
		if (Config.DISCONNECT_SYSTEM_ENABLED)
		{
			try
			{
				if (getActiveChar() != null)
				{
					getActiveChar().getAppearance().setVisibleTitle(Config.DISCONNECT_TITLE);
					getActiveChar().getAppearance().setDisplayName(true);
					String color = String.valueOf(Config.DISCONNECT_TITLECOLOR.charAt(4)) + String.valueOf(Config.DISCONNECT_TITLECOLOR.charAt(5)) + String.valueOf(Config.DISCONNECT_TITLECOLOR.charAt(2)) + String.valueOf(Config.DISCONNECT_TITLECOLOR.charAt(3)) + String.valueOf(Config.DISCONNECT_TITLECOLOR.charAt(0)) + String.valueOf(Config.DISCONNECT_TITLECOLOR.charAt(1));
					getActiveChar().getAppearance().setTitleColor(Integer.decode("0x" + color));
					getActiveChar().broadcastPacket(new CharInfo(getActiveChar()));
				}
			}
			catch (Exception e)
			{
				_log.warning("onDisconnection " + e.getMessage());
			}
			finally
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new DisconnectTask(), Config.DISCONNECT_TIMEOUT * 1000);
				Protection.doDisconection(this);
			}
		}
		else
		{
			try
			{
				ThreadPoolManager.getInstance().executeTask(new DisconnectTask());
				Protection.doDisconection(this);
			}
			catch (RejectedExecutionException e)
			{
				_log.warning("onDisconnection " + e.getMessage());
			}
		}
	}
	
	public void closeNow()
	{
		_isDetached = true;
		close(ServerClose.STATIC_PACKET);
		synchronized (this)
		{
			if (_cleanupTask != null)
			{
				cancelCleanup();
			}
			_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), 0);
		}
	}
	
	@Override
	public String toString()
	{
		try
		{
			final InetAddress address = getConnection().getInetAddress();
			switch (getState())
			{
				case CONNECTED:
					return "[IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				case AUTHED:
					return "[Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				case IN_GAME:
					return "[Character: " + (getActiveChar() == null ? "disconnected" : getActiveChar().getName() + "[" + getActiveChar().getObjectId() + "]") + " - Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				default:
					throw new IllegalStateException("Missing state on switch");
			}
		}
		catch (NullPointerException e)
		{
			return "[Character read failed due to disconnect]";
		}
	}
	
	protected class DisconnectTask implements Runnable
	{
		@Override
		public void run()
		{
			boolean fast = true;
			try
			{
				if ((getActiveChar() != null) && !isDetached())
				{
					setDetached(true);
					if (offlineMode(getActiveChar()))
					{
						getActiveChar().leaveParty();
						
						if (getActiveChar().hasSummon())
						{
							getActiveChar().getSummon().setRestoreSummon(true);
							getActiveChar().getSummon().unSummon(getActiveChar());
							
							if (getActiveChar().getSummon() != null)
							{
								getActiveChar().getSummon().broadcastNpcInfo(0);
							}
						}
						
						if (Config.OFFLINE_SET_NAME_COLOR)
						{
							getActiveChar().getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
							getActiveChar().broadcastUserInfo();
						}
						
						if (getActiveChar().getOfflineStartTime() == 0)
						{
							getActiveChar().setOfflineStartTime(System.currentTimeMillis());
						}
						
						final LogRecord record = new LogRecord(Level.INFO, "Entering offline mode");
						record.setParameters(new Object[]
						{
							L2GameClient.this
						});
						_logAccounting.log(record);
						return;
					}
					fast = !getActiveChar().isInCombat() && !getActiveChar().isLocked();
				}
				cleanMe(fast);
			}
			catch (Exception e1)
			{
				_log.log(Level.WARNING, "Error while disconnecting client.", e1);
			}
		}
	}
	
	protected boolean offlineMode(L2PcInstance player)
	{
		boolean canSetShop = false;
		if (player.isInOlympiadMode() || player.isFestivalParticipant() || player.isBlockedFromExit() || player.isJailed() || (player.getVehicle() != null))
		{
			return false;
		}
		
		if (Config.OFFLINE_TRADE_ENABLE && ((player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL) || (player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY)))
		{
			canSetShop = true;
		}
		else if (Config.OFFLINE_CRAFT_ENABLE && (player.isInCraftMode() || (player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_MANUFACTURE)))
		{
			canSetShop = true;
		}
		
		if (Config.OFFLINE_MODE_IN_PEACE_ZONE && !player.isInsideZone(ZoneId.PEACE))
		{
			canSetShop = false;
		}
		return canSetShop;
	}
	
	public void cleanMe(boolean fast)
	{
		try
		{
			synchronized (this)
			{
				if (_cleanupTask == null)
				{
					_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), fast ? 5 : 15000L);
				}
			}
		}
		catch (Exception e1)
		{
			_log.log(Level.WARNING, "Error during cleanup.", e1);
		}
	}
	
	protected class CleanupTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (_autoSaveInDB != null)
				{
					_autoSaveInDB.cancel(true);
				}
				
				if (getActiveChar() != null)
				{
					if (getActiveChar().isLocked())
					{
						_log.log(Level.WARNING, "Player " + getActiveChar().getName() + " still performing subclass actions during disconnect.");
					}
					
					if (L2Event.isParticipant(getActiveChar()))
					{
						L2Event.savePlayerEventStatus(getActiveChar());
					}
					getActiveChar().setClient(null);
					
					if (getActiveChar().isOnline())
					{
						getActiveChar().deleteMe();
						AntiFeedManager.getInstance().onDisconnect(L2GameClient.this);
					}
				}
				setActiveChar(null);
			}
			catch (Exception e1)
			{
				_log.log(Level.WARNING, "Error while cleanup client.", e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}
	
	protected class AutoSaveTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				L2PcInstance player = getActiveChar();
				if ((player != null) && player.isOnline())
				{
					saveCharToDisk();
					if (player.hasSummon())
					{
						player.getSummon().store();
					}
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Error on AutoSaveTask.", e);
			}
		}
	}
	
	public boolean isProtocolOk()
	{
		return _protocol;
	}
	
	public void setProtocolOk(boolean b)
	{
		_protocol = b;
	}
	
	public boolean handleCheat(String punishment)
	{
		if (_activeChar != null)
		{
			Util.handleIllegalPlayerAction(_activeChar, toString() + ": " + punishment, Config.DEFAULT_PUNISH);
			return true;
		}
		
		Logger _logAudit = Logger.getLogger("audit");
		_logAudit.log(Level.INFO, "AUDIT: Client " + toString() + " kicked for reason: " + punishment);
		closeNow();
		return false;
	}
	
	public boolean dropPacket()
	{
		if (_isDetached)
		{
			return true;
		}
		
		if (getStats().countPacket(_packetQueue.size()))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		
		return getStats().dropPacket();
	}
	
	public void onBufferUnderflow()
	{
		if (getStats().countUnderflowException())
		{
			_log.severe("Client " + toString() + " - Disconnected: Too many buffer underflow exceptions.");
			closeNow();
			return;
		}
		if (_state == GameClientState.CONNECTED)
		{
			if (Config.PACKET_HANDLER_DEBUG)
			{
				_log.severe("Client " + toString() + " - Disconnected, too many buffer underflows in non-authed state.");
			}
			closeNow();
		}
	}
	
	public void onUnknownPacket()
	{
		if (getStats().countUnknownPacket())
		{
			_log.severe("Client " + toString() + " - Disconnected: Too many unknown packets.");
			closeNow();
			return;
		}
		if (_state == GameClientState.CONNECTED)
		{
			if (Config.PACKET_HANDLER_DEBUG)
			{
				_log.severe("Client " + toString() + " - Disconnected, too many unknown packets in non-authed state.");
			}
			closeNow();
		}
	}
	
	public void execute(ReceivablePacket<L2GameClient> packet)
	{
		if (getStats().countFloods())
		{
			_log.severe("Client " + toString() + " - Disconnected, too many floods:" + getStats().longFloods + " long and " + getStats().shortFloods + " short.");
			closeNow();
			return;
		}
		
		if (!_packetQueue.offer(packet))
		{
			if (getStats().countQueueOverflow())
			{
				_log.severe("Client " + toString() + " - Disconnected, too many queue overflows.");
				closeNow();
			}
			else
			{
				sendPacket(ActionFailed.STATIC_PACKET);
			}
			return;
		}
		
		if (_queueLock.isLocked())
		{
			return;
		}
		
		try
		{
			if (_state == GameClientState.CONNECTED)
			{
				if (getStats().processedPackets > 3)
				{
					if (Config.PACKET_HANDLER_DEBUG)
					{
						_log.severe("Client " + toString() + " - Disconnected, too many packets in non-authed state.");
					}
					closeNow();
					return;
				}
				ThreadPoolManager.getInstance().executeIOPacket(this);
			}
			else
			{
				ThreadPoolManager.getInstance().executePacket(this);
			}
		}
		catch (RejectedExecutionException e)
		{
			if (!ThreadPoolManager.getInstance().isShutdown())
			{
				_log.severe("Failed executing: " + packet.getClass().getSimpleName() + " for Client: " + toString());
			}
		}
	}
	
	@Override
	public void run()
	{
		if (!_queueLock.tryLock())
		{
			return;
		}
		
		try
		{
			int count = 0;
			ReceivablePacket<L2GameClient> packet;
			while (true)
			{
				packet = _packetQueue.poll();
				if (packet == null)
				{
					return;
				}
				
				if (_isDetached)
				{
					_packetQueue.clear();
					return;
				}
				
				try
				{
					packet.run();
				}
				catch (Exception e)
				{
					_log.severe("Exception during execution " + packet.getClass().getSimpleName() + ", client: " + toString() + "," + e.getMessage());
				}
				
				count++;
				if (getStats().countBurst(count))
				{
					return;
				}
			}
		}
		finally
		{
			_queueLock.unlock();
		}
	}
	
	public void setClientTracert(int[][] tracert)
	{
		trace = tracert;
	}
	
	public int[][] getTrace()
	{
		return trace;
	}
	
	private boolean cancelCleanup()
	{
		final Future<?> task = _cleanupTask;
		if (task != null)
		{
			_cleanupTask = null;
			return task.cancel(true);
		}
		return false;
	}
	
	public void setAditionalClosePacket(L2GameServerPacket aditionalClosePacket)
	{
		_aditionalClosePacket = aditionalClosePacket;
	}
	
	public final String getHWID()
	{
		return _hwid;
	}
	
	public void setHWID(final String hwid)
	{
		_hwid = hwid;
	}
	
	public int getRevision()
	{
		return revision;
	}
	
	public void setRevision(int revision)
	{
		this.revision = revision;
	}
	
	public int getPlayerId()
	{
		return _playerId;
	}
	
	public void setPlayerId(int plId)
	{
		_playerId = plId;
	}
	
	public final String getLoginName()
	{
		return _loginName;
	}
	
	public void setLoginName(final String name)
	{
		_loginName = name;
	}
	
	public final String getPlayerName()
	{
		return _playerName;
	}
	
	public void setPlayerName(final String name)
	{
		_playerName = name;
	}
	
	public LockType getLockType()
	{
		return lockType;
	}
	
	public void setLockType(LockType lockType)
	{
		this.lockType = lockType;
	}
	
	public String getLogin()
	{
		return login;
	}
	
	public void setLogin(String login)
	{
		this.login = login;
	}
	
	public int getPlayerID()
	{
		return playerID;
	}
	
	public void setPlayerID(int playerID)
	{
		this.playerID = playerID;
	}
}