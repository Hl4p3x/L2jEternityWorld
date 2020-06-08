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
package l2e.protection.network;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.customs.LocalizationStorage;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.serverpackets.GameGuardQuery;
import l2e.protection.ConfigProtection;
import l2e.protection.Protection;
import l2e.protection.hwidmanager.HWIDManager;
import l2e.protection.network.serverpackets.ProtectionPacket;
import l2e.protection.utils.Log;

public final class ProtectionManager
{
	protected static final Logger _log = Logger.getLogger(ProtectionManager.class.getName());
	
	protected static String _logFile = "ProtectManager";
	protected static String _logMainFile = "protection_logs";
	protected static ProtectionManager _instance;
	protected static ScheduledFuture<?> _GGTask = null;
	
	private class InfoSet
	{
		public String _playerName = "";
		public long _lastGGSendTime;
		public long _lastGGRecvTime;
		public int _attempts;
		public String _HWID = "";
		
		public InfoSet(final String name, final String HWID)
		{
			_playerName = name;
			_lastGGSendTime = System.currentTimeMillis();
			_lastGGRecvTime = _lastGGSendTime;
			_attempts = 0;
			_HWID = HWID;
		}
	}
	
	protected static ConcurrentHashMap<String, InfoSet> _objects = new ConcurrentHashMap<>();
	
	public static ProtectionManager getInstance()
	{
		if (_instance == null)
		{
			_log.info("Initializing ProtectionManager");
			_instance = new ProtectionManager();
		}
		return _instance;
	}
	
	public ProtectionManager()
	{
		startGGTask();
	}
	
	public static void SendSpecialSting(L2GameClient client)
	{
		if (Protection.isProtectionOn())
		{
			if (ConfigProtection.SHOW_PROTECTION_INFO_IN_CLIENT)
			{
				client.sendPacket(new ProtectionPacket(0, true, -1, ConfigProtection.PositionXProtectionInfoInClient, ConfigProtection.PositionYProtectionInfoInClient, ConfigProtection.ColorProtectionInfoInClient, "PROTECTION ON"));
			}
			if (ConfigProtection.SHOW_NAME_SERVER_IN_CLIENT)
			{
				client.sendPacket(new ProtectionPacket(1, true, -1, ConfigProtection.PositionXNameServerInfoInClient, ConfigProtection.PositionYNameServerInfoInClient, ConfigProtection.ColorNameServerInfoInClient, ("" + LocalizationStorage.getInstance().getString(client.getActiveChar().getLang(), "Protection.SERVER") + " ") + ConfigProtection.NameServerInfoInClient));
			}
			if (ConfigProtection.SHOW_REAL_TIME_IN_CLIENT)
			{
				client.sendPacket(new ProtectionPacket(15, true, -1, ConfigProtection.PositionXRealTimeInClient, ConfigProtection.PositionYRealTimeInClient, ConfigProtection.ColorRealTimeInClient, "" + LocalizationStorage.getInstance().getString(client.getActiveChar().getLang(), "Protection.REAL_TIME") + " "));
			}
			sendToClient(client.getActiveChar());
		}
	}
	
	public static void sendToClient(L2PcInstance client)
	{
		if (ConfigProtection.SHOW_ONLINE_IN_CLIENT)
		{
			client.sendPacket(new ProtectionPacket(2, true, -1, ConfigProtection.PositionXOnlineInClient, ConfigProtection.PositionYOnlineInClient, ConfigProtection.ColorOnlineInClient, ("" + LocalizationStorage.getInstance().getString(client.getLang(), "Protection.ONLINE") + " ") + (L2World.getInstance().getAllPlayers().size() * Config.FAKE_ONLINE)));
		}
		if (ConfigProtection.SHOW_SERVER_TIME_IN_CLIENT)
		{
			String strH, strM;
			int h = GameTimeController.getInstance().getGameHour();
			int m = GameTimeController.getInstance().getGameMinute();
			String nd;
			if (GameTimeController.getInstance().isNowNight())
			{
				nd = "" + LocalizationStorage.getInstance().getString(client.getLang(), "Protection.NIGHT") + "";
			}
			else
			{
				nd = "" + LocalizationStorage.getInstance().getString(client.getLang(), "Protection.DAY") + "";
			}
			if (h < 10)
			{
				strH = "0" + h;
			}
			else
			{
				strH = "" + h;
			}
			if (m < 10)
			{
				strM = "0" + m;
			}
			else
			{
				strM = "" + m;
			}
			client.sendPacket(new ProtectionPacket(3, true, -1, ConfigProtection.PositionXServerTimeInClient, ConfigProtection.PositionYServerTimeInClient, ConfigProtection.ColorServerTimeInClient, ("" + LocalizationStorage.getInstance().getString(client.getLang(), "Protection.GAME_TIME") + " ") + strH + ":" + strM + " (" + nd + ")"));
		}
		if (ConfigProtection.SHOW_PING_IN_CLIENT)
		{
			client.sendPacket(new ProtectionPacket(14, true, -1, ConfigProtection.PositionXPingInClient, ConfigProtection.PositionYPingInClient, ConfigProtection.ColorPingInClient, "" + LocalizationStorage.getInstance().getString(client.getLang(), "Protection.PING") + " "));
		}
		scheduleSendPacketToClient(ConfigProtection.TIME_REFRESH_SPECIAL_STRING, client);
	}
	
	public static void OffMessage(L2PcInstance client)
	{
		if (client != null)
		{
			client.sendPacket(new ProtectionPacket(0, false, -1, ConfigProtection.PositionXProtectionInfoInClient, ConfigProtection.PositionYProtectionInfoInClient, 0xFF00FF00, ""));
			client.sendPacket(new ProtectionPacket(1, false, -1, ConfigProtection.PositionXNameServerInfoInClient, ConfigProtection.PositionYNameServerInfoInClient, 0xFF00FF00, ""));
			client.sendPacket(new ProtectionPacket(2, false, -1, ConfigProtection.PositionXOnlineInClient, ConfigProtection.PositionYOnlineInClient, 0xFF00FF00, ""));
			client.sendPacket(new ProtectionPacket(3, false, -1, ConfigProtection.PositionXServerTimeInClient, ConfigProtection.PositionYServerTimeInClient, 0xFF00FF00, ""));
			client.sendPacket(new ProtectionPacket(14, false, -1, ConfigProtection.PositionXPingInClient, ConfigProtection.PositionYPingInClient, 0xFF00FF00, ""));
			client.sendPacket(new ProtectionPacket(15, false, -1, ConfigProtection.PositionXRealTimeInClient, ConfigProtection.PositionYRealTimeInClient, 0xFF00FF00, ""));
		}
		return;
	}
	
	public static void scheduleSendPacketToClient(long time, final L2PcInstance client)
	{
		if (time <= 0)
		{
			OffMessage(client);
			return;
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				sendToClient(client);
			}
		}, time);
	}
	
	class GGTask implements Runnable
	{
		@Override
		public void run()
		{
			long time = System.currentTimeMillis();
			for (final InfoSet object : _objects.values())
			{
				if ((time - object._lastGGSendTime) >= ConfigProtection.PROTECT_GG_SEND_INTERVAL)
				{
					try
					{
						L2World.getInstance().getPlayer(object._playerName).sendPacket(new GameGuardQuery());
						object._lastGGSendTime = time;
						object._lastGGRecvTime = time + ConfigProtection.PROTECT_GG_RECV_INTERVAL + 1;
					}
					catch (final Exception e)
					{
						removePlayer(object._playerName);
					}
				}
				
				if ((time - object._lastGGRecvTime) >= ConfigProtection.PROTECT_GG_RECV_INTERVAL)
				{
					try
					{
						final L2PcInstance player = L2World.getInstance().getPlayer(object._playerName);
						if (!player.getClient().isAuthedGG())
						{
							if (object._attempts < 3)
							{
								object._attempts++;
							}
							else
							{
								if (player != null)
								{
									final L2GameClient client = player.getClient();
									Log.add("Player was kicked because GG packet not receive (3 attempts)|" + client.toString(), _logMainFile);
								}
								player.logout();
							}
						}
						object._lastGGRecvTime = time;
					}
					catch (final Exception e)
					{
						removePlayer(object._playerName);
					}
				}
			}
			
			time = System.currentTimeMillis() - time;
			if (time > ConfigProtection.PROTECT_TASK_GG_INVERVAL)
			{
				Log.add("ALERT! TASK_SAVE_INTERVAL is too small, time to save characters in Queue = " + time + ", Config=" + ConfigProtection.PROTECT_TASK_GG_INVERVAL, _logMainFile);
			}
		}
	}
	
	public void startGGTask()
	{
		stopGGTask(true);
		if (ConfigProtection.PROTECT_ENABLE_GG_SYSTEM)
		{
			_GGTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new GGTask(), ConfigProtection.PROTECT_TASK_GG_INVERVAL, ConfigProtection.PROTECT_TASK_GG_INVERVAL);
		}
	}
	
	public static void stopGGTask(final boolean mayInterruptIfRunning)
	{
		if (_GGTask != null)
		{
			try
			{
				_GGTask.cancel(mayInterruptIfRunning);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			_GGTask = null;
		}
	}
	
	public void addPlayer(L2GameClient client)
	{
		HWIDManager.updateHWIDInfo(client, 1);
		_objects.put(client.getPlayerName(), new InfoSet(client.getPlayerName(), client.getHWID()));
		
		if (ConfigProtection.PROTECT_DEBUG)
		{
			Log.add(client.toString(), _logFile);
		}
	}
	
	public void removePlayer(final String name)
	{
		if (!_objects.containsKey(name))
		{
			if (ConfigProtection.PROTECT_DEBUG)
			{
				Log.add("trying to remove player that non exists : " + name, _logFile);
			}
		}
		else
		{
			_objects.remove(name);
		}
	}
	
	public int getCountByHWID(final String HWID)
	{
		int result = 0;
		for (final InfoSet object : _objects.values())
		{
			if (object._HWID.equals(HWID))
			{
				result++;
			}
		}
		return result;
	}
}