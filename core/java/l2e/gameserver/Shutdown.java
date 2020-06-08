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

import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.data.sql.CharSchemesHolder;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.data.sql.OfflineTradersHolder;
import l2e.gameserver.data.xml.BotReportParser;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManorManager;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.GlobalVariablesManager;
import l2e.gameserver.instancemanager.GrandBossManager;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.instancemanager.ItemAuctionManager;
import l2e.gameserver.instancemanager.ItemsOnGroundManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.instancemanager.games.FishingChampionship;
import l2e.gameserver.instancemanager.leaderboards.ArenaLeaderboard;
import l2e.gameserver.instancemanager.leaderboards.CraftLeaderboard;
import l2e.gameserver.instancemanager.leaderboards.FishermanLeaderboard;
import l2e.gameserver.instancemanager.leaderboards.TvTLeaderboard;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Hero;
import l2e.gameserver.model.entity.events.phoenix.Interface;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.gameserverpackets.ServerStatus;
import l2e.gameserver.network.serverpackets.ServerClose;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Broadcast;
import gnu.trove.procedure.TObjectProcedure;

public class Shutdown extends Thread
{
	private static Logger _log = Logger.getLogger(Shutdown.class.getName());
	private static Shutdown _counterInstance = null;
	
	private int _secondsShut;
	private int _shutdownMode;
	public static final int SIGTERM = 0;
	public static final int GM_SHUTDOWN = 1;
	public static final int GM_RESTART = 2;
	public static final int ABORT = 3;
	private static final String[] MODE_TEXT =
	{
		"SIGTERM",
		"shutting down",
		"restarting",
		"aborting"
	};
	
	private void SendServerQuit(int seconds)
	{
		SystemMessage sysm = SystemMessage.getSystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS);
		sysm.addNumber(seconds);
		Broadcast.toAllOnlinePlayers(sysm);
	}
	
	public void startTelnetShutdown(String IP, int seconds, boolean restart)
	{
		_log.warning("IP: " + IP + " issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
		
		if (restart)
		{
			_shutdownMode = GM_RESTART;
		}
		else
		{
			_shutdownMode = GM_SHUTDOWN;
		}
		
		if (_shutdownMode > 0)
		{
			switch (seconds)
			{
				case 540:
				case 480:
				case 420:
				case 360:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
				case 30:
				case 10:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					break;
				default:
					SendServerQuit(seconds);
			}
		}
		
		if (_counterInstance != null)
		{
			_counterInstance._abort();
		}
		_counterInstance = new Shutdown(seconds, restart);
		_counterInstance.start();
	}
	
	public void autoRestart(int time)
	{
		_secondsShut = time;
		countdown();
		_shutdownMode = GM_RESTART;
		System.exit(2);
	}
	
	public void telnetAbort(String IP)
	{
		_log.warning("IP: " + IP + " issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
		
		if (_counterInstance != null)
		{
			_counterInstance._abort();
			Announcements _an = Announcements.getInstance();
			_an.announceToAll("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");
		}
	}
	
	protected Shutdown()
	{
		_secondsShut = -1;
		_shutdownMode = SIGTERM;
	}
	
	public Shutdown(int seconds, boolean restart)
	{
		if (seconds < 0)
		{
			seconds = 0;
		}
		_secondsShut = seconds;
		if (restart)
		{
			_shutdownMode = GM_RESTART;
		}
		else
		{
			_shutdownMode = GM_SHUTDOWN;
		}
	}
	
	@Override
	public void run()
	{
		if (this == getInstance())
		{
			TimeCounter tc = new TimeCounter();
			TimeCounter tc1 = new TimeCounter();
			try
			{
				if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
				{
					OfflineTradersHolder.getInstance().storeOffliners();
					_log.info("Offline Traders Table: Offline shops stored(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
				}
			}
			catch (Throwable t)
			{
				_log.log(Level.WARNING, "Error saving offline shops.", t);
			}
			
			try
			{
				disconnectAllCharacters();
				_log.info("All players disconnected and saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch (Throwable t)
			{
			}
			
			try
			{
				GameTimeController.getInstance().stopTimer();
				_log.info("Game Time Controller: Timer stopped(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch (Throwable t)
			{
				
			}
			
			try
			{
				ThreadPoolManager.getInstance().shutdown();
				_log.info("Thread Pool Manager: Manager has been shut down(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch (Throwable t)
			{
			}
			
			try
			{
				LoginServerThread.getInstance().interrupt();
				_log.info("Login Server Thread: Thread interruped(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch (Throwable t)
			{
			}
			saveData();
			tc.restartCounter();
			
			try
			{
				GameServer.gameServer.getSelectorThread().shutdown();
				_log.info("Game Server: Selector thread has been shut down(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch (Throwable t)
			{
			}
			
			try
			{
				L2DatabaseFactory.getInstance().shutdown();
				_log.info("L2Database Factory: Database connection has been shut down(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			}
			catch (Throwable t)
			{
				
			}
			
			if (getInstance()._shutdownMode == GM_RESTART)
			{
				Runtime.getRuntime().halt(2);
			}
			else
			{
				Runtime.getRuntime().halt(0);
			}
			_log.info("The server has been successfully shut down in " + (tc1.getEstimatedTime() / 1000) + "seconds.");
		}
		else
		{
			countdown();
			_log.warning("GM shutdown countdown is over. " + MODE_TEXT[_shutdownMode] + " NOW!");
			switch (_shutdownMode)
			{
				case GM_SHUTDOWN:
					getInstance().setMode(GM_SHUTDOWN);
					System.exit(0);
					break;
				case GM_RESTART:
					getInstance().setMode(GM_RESTART);
					System.exit(2);
					break;
				case ABORT:
					LoginServerThread.getInstance().setServerStatus(ServerStatus.STATUS_AUTO);
					break;
			}
		}
	}
	
	public void startShutdown(L2PcInstance activeChar, int seconds, boolean restart)
	{
		if (restart)
		{
			_shutdownMode = GM_RESTART;
		}
		else
		{
			_shutdownMode = GM_SHUTDOWN;
		}
		
		_log.warning("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown command. " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");
		
		if (_shutdownMode > 0)
		{
			switch (seconds)
			{
				case 540:
				case 480:
				case 420:
				case 360:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
				case 30:
				case 10:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					break;
				default:
					SendServerQuit(seconds);
			}
		}
		
		if (_counterInstance != null)
		{
			_counterInstance._abort();
		}
		_counterInstance = new Shutdown(seconds, restart);
		_counterInstance.start();
	}
	
	public void abort(L2PcInstance activeChar)
	{
		_log.warning("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown ABORT. " + MODE_TEXT[_shutdownMode] + " has been stopped!");
		if (_counterInstance != null)
		{
			_counterInstance._abort();
			Announcements _an = Announcements.getInstance();
			_an.announceToAll("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");
		}
	}
	
	private void setMode(int mode)
	{
		_shutdownMode = mode;
	}
	
	private void _abort()
	{
		_shutdownMode = ABORT;
	}
	
	private void countdown()
	{
		try
		{
			while (_secondsShut > 0)
			{
				
				switch (_secondsShut)
				{
					case 540:
						SendServerQuit(540);
						break;
					case 480:
						SendServerQuit(480);
						break;
					case 420:
						SendServerQuit(420);
						break;
					case 360:
						SendServerQuit(360);
						break;
					case 300:
						SendServerQuit(300);
						break;
					case 240:
						SendServerQuit(240);
						break;
					case 180:
						SendServerQuit(180);
						break;
					case 120:
						SendServerQuit(120);
						break;
					case 60:
						LoginServerThread.getInstance().setServerStatus(ServerStatus.STATUS_DOWN);
						SendServerQuit(60);
						break;
					case 30:
						SendServerQuit(30);
						break;
					case 10:
						SendServerQuit(10);
						break;
					case 5:
						SendServerQuit(5);
						break;
					case 4:
						SendServerQuit(4);
						break;
					case 3:
						SendServerQuit(3);
						break;
					case 2:
						SendServerQuit(2);
						break;
					case 1:
						SendServerQuit(1);
						break;
				}
				
				_secondsShut--;
				
				int delay = 1000;
				Thread.sleep(delay);
				
				if (_shutdownMode == ABORT)
				{
					break;
				}
			}
		}
		catch (InterruptedException e)
		{
		}
	}
	
	private void saveData()
	{
		switch (_shutdownMode)
		{
			case SIGTERM:
				_log.info("SIGTERM received. Shutting down NOW!");
				break;
			case GM_SHUTDOWN:
				_log.info("GM shutdown received. Shutting down NOW!");
				break;
			case GM_RESTART:
				_log.info("GM restart received. Restarting NOW!");
				break;
		
		}
		
		TimeCounter tc = new TimeCounter();
		
		if (Config.RANK_ARENA_ENABLED)
		{
			ArenaLeaderboard.getInstance().stopTask();
		}
		
		if (Config.RANK_FISHERMAN_ENABLED)
		{
			FishermanLeaderboard.getInstance().stopTask();
		}
		
		if (Config.RANK_CRAFT_ENABLED)
		{
			CraftLeaderboard.getInstance().stopTask();
		}
		
		if (Config.RANK_TVT_ENABLED)
		{
			TvTLeaderboard.getInstance().stopTask();
		}
		
		FishingChampionship.getInstance().shutdown();
		
		if (!SevenSigns.getInstance().isSealValidationPeriod())
		{
			SevenSignsFestival.getInstance().saveFestivalData(false);
			_log.info("SevenSignsFestival: Festival data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		}
		
		SevenSigns.getInstance().saveSevenSignsData();
		_log.info("SevenSigns: Seven Signs data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		SevenSigns.getInstance().saveSevenSignsStatus();
		_log.info("SevenSigns: Seven Signs status saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		RaidBossSpawnManager.getInstance().cleanUp();
		_log.info("RaidBossSpawnManager: All raidboss info saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		GrandBossManager.getInstance().cleanUp();
		_log.info("GrandBossManager: All Grand Boss info saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		HellboundManager.getInstance().cleanUp();
		_log.info("Hellbound Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		ItemAuctionManager.getInstance().shutdown();
		_log.info("Item Auction Manager: All tasks stopped(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		Olympiad.getInstance().saveOlympiadStatus();
		_log.info("Olympiad System: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		Hero.getInstance().shutdown();
		_log.info("Hero System: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		ClanHolder.getInstance().storeClanScore();
		_log.info("Clan System: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		CursedWeaponsManager.getInstance().saveData();
		_log.info("Cursed Weapons Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		CastleManorManager.getInstance().save();
		_log.info("Castle Manor Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		CHSiegeManager.getInstance().onServerShutDown();
		_log.info("CHSiegeManager: Siegable hall attacker lists saved!");
		QuestManager.getInstance().save();
		_log.info("Quest Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		GlobalVariablesManager.getInstance().storeMe();
		_log.info("Global Variables Manager: Variables saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		
		if (Config.ALLOW_COMMUNITY_BUFF && Config.BUFF_STORE_SCHEMES)
		{
			CharSchemesHolder.getInstance().onServerShutdown();
		}
		
		Interface.shutdown();
		
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().saveInDb();
			_log.info("Items On Ground Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
			ItemsOnGroundManager.getInstance().cleanUp();
			_log.info("Items On Ground Manager: Cleaned up(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
		}
		
		if (Config.BOTREPORT_ENABLE)
		{
			BotReportParser.getInstance().saveReportedCharData();
			_log.info("Bot Report Data: Sucessfully saved reports to database!");
		}
		
		try
		{
			int delay = 5000;
			Thread.sleep(delay);
		}
		catch (InterruptedException e)
		{
		}
	}
	
	private void disconnectAllCharacters()
	{
		L2World.getInstance().getAllPlayers().safeForEachValue(new DisconnectAllCharacters());
	}
	
	protected final class DisconnectAllCharacters implements TObjectProcedure<L2PcInstance>
	{
		private final Logger _log = Logger.getLogger(DisconnectAllCharacters.class.getName());
		
		@Override
		public final boolean execute(final L2PcInstance player)
		{
			if (player != null)
			{
				try
				{
					L2GameClient client = player.getClient();
					if ((client != null) && !client.isDetached())
					{
						client.close(ServerClose.STATIC_PACKET);
						client.setActiveChar(null);
						player.setClient(null);
					}
					player.deleteMe();
				}
				catch (Throwable t)
				{
					_log.log(Level.WARNING, "Failed logour char " + player, t);
				}
			}
			return true;
		}
	}
	
	private static final class TimeCounter
	{
		private long _startTime;
		
		protected TimeCounter()
		{
			restartCounter();
		}
		
		protected void restartCounter()
		{
			_startTime = System.currentTimeMillis();
		}
		
		protected long getEstimatedTimeAndRestartCounter()
		{
			final long toReturn = System.currentTimeMillis() - _startTime;
			restartCounter();
			return toReturn;
		}
		
		protected long getEstimatedTime()
		{
			return System.currentTimeMillis() - _startTime;
		}
	}
	
	public static Shutdown getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final Shutdown _instance = new Shutdown();
	}
}