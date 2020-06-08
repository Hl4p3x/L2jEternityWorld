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
package l2e.gameserver.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.GameTimeController;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentTask;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.network.L2GameClient;
import l2e.util.StringUtil;

public final class FloodProtectorAction
{
	private static final Logger _log = Logger.getLogger(FloodProtectorAction.class.getName());

	private final L2GameClient _client;
	private final FloodProtectorConfig _config;
	private volatile int _nextGameTick = GameTimeController.getInstance().getGameTicks();
	private AtomicInteger _count = new AtomicInteger(0);
	private boolean _logged;
	private volatile boolean _punishmentInProgress;
	
	public FloodProtectorAction(final L2GameClient client, final FloodProtectorConfig config)
	{
		super();

		_client = client;
		_config = config;
	}
	
	public boolean tryPerformAction(final String command)
	{
		final int curTick = GameTimeController.getInstance().getGameTicks();

		if (_client.getActiveChar() != null && _client.getActiveChar().canOverrideCond(PcCondOverride.FLOOD_CONDITIONS))
		{
			return true;
		}
		
		if (curTick < _nextGameTick || _punishmentInProgress)
		{
			if (_config.LOG_FLOODING && !_logged && _log.isLoggable(Level.WARNING))
			{
				log(" called command ", command, " ~", String.valueOf((_config.FLOOD_PROTECTION_INTERVAL - (_nextGameTick - curTick)) * GameTimeController.MILLIS_IN_TICK), " ms after previous command");
				_logged = true;
			}
			
			_count.incrementAndGet();
			
			if (!_punishmentInProgress && _config.PUNISHMENT_LIMIT > 0 && _count.get() >= _config.PUNISHMENT_LIMIT && _config.PUNISHMENT_TYPE != null)
			{
				_punishmentInProgress = true;
				
				if ("kick".equals(_config.PUNISHMENT_TYPE))
				{
					kickPlayer();
				}
				else if ("ban".equals(_config.PUNISHMENT_TYPE))
				{
					banAccount();
				}
				else if ("jail".equals(_config.PUNISHMENT_TYPE))
				{
					jailChar();
				}
				
				_punishmentInProgress = false;
			}
			
			return false;
		}
		
		if (_count.get() > 0)
		{
			if (_config.LOG_FLOODING && _log.isLoggable(Level.WARNING))
			{
				log(" issued ", String.valueOf(_count), " extra requests within ~", String.valueOf(_config.FLOOD_PROTECTION_INTERVAL * GameTimeController.MILLIS_IN_TICK), " ms");
			}
		}
		
		_nextGameTick = curTick + _config.FLOOD_PROTECTION_INTERVAL;
		_logged = false;
		_count.set(0);
		
		return true;
	}
	
	private void kickPlayer()
	{
		if (_client.getActiveChar() != null)
			_client.getActiveChar().logout(false);
		else
			_client.closeNow();
		
		if (_log.isLoggable(Level.WARNING))
		{
			log("kicked for flooding");
		}
	}
	
	private void banAccount()
	{
		PunishmentManager.getInstance().startPunishment(new PunishmentTask(_client.getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.BAN, System.currentTimeMillis() + _config.PUNISHMENT_TIME, "", getClass().getSimpleName()));
		if (_log.isLoggable(Level.WARNING))
		{
			log(" banned for flooding ", _config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + _config.PUNISHMENT_TIME + " mins");
		}
	}
	
	private void jailChar()
	{
		if (_client.getActiveChar() != null)
		{
			int charId = _client.getActiveChar().getObjectId();
			if (charId > 0)
			{
				PunishmentManager.getInstance().startPunishment(new PunishmentTask(charId, PunishmentAffect.CHARACTER, PunishmentType.JAIL, System.currentTimeMillis() + _config.PUNISHMENT_TIME, "", getClass().getSimpleName()));
			}
			
			if (_log.isLoggable(Level.WARNING))
			{
				log(" jailed for flooding ", _config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + _config.PUNISHMENT_TIME + " mins");
			}
		}
	}
	
	private void log(String... lines)
	{
		final StringBuilder output = StringUtil.startAppend(100, _config.FLOOD_PROTECTOR_TYPE, ": ");
		String address = null;
		try
		{
			if (!_client.isDetached())
				address = _client.getConnection().getInetAddress().getHostAddress();
		}
		catch (Exception e)
		{
		}
		
		switch (_client.getState())
		{
			case IN_GAME:
				if (_client.getActiveChar() != null)
				{
					StringUtil.append(output, _client.getActiveChar().getName());
					StringUtil.append(output, "(", String.valueOf(_client.getActiveChar().getObjectId()),") ");
				}
				break;
			case AUTHED:
				if (_client.getAccountName() != null)
					StringUtil.append(output, _client.getAccountName()," ");
				break;
			case CONNECTED:
				if (address != null)
					StringUtil.append(output, address);
				break;
			default:
				throw new IllegalStateException("Missing state on switch");
		}
		
		StringUtil.append(output, lines);
		_log.warning(output.toString());
	}
}