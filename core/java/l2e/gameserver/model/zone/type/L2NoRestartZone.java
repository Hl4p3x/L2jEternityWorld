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
package l2e.gameserver.model.zone.type;

import l2e.gameserver.GameServer;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.model.zone.ZoneId;

public class L2NoRestartZone extends L2ZoneType
{
	private int _restartAllowedTime = 0;
	private int _restartTime = 0;
	private boolean _enabled = true;
	
	public L2NoRestartZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equalsIgnoreCase("default_enabled"))
		{
			_enabled = Boolean.parseBoolean(value);
		}
		else if (name.equalsIgnoreCase("restartAllowedTime"))
		{
			_restartAllowedTime = Integer.parseInt(value) * 1000;
		}
		else if (name.equalsIgnoreCase("restartTime"))
		{
			_restartTime = Integer.parseInt(value) * 1000;
		}
		else if (name.equalsIgnoreCase("instanceId"))
		{
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (!_enabled)
		{
			return;
		}
		
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.NO_RESTART, true);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (!_enabled)
		{
			return;
		}
		
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.NO_RESTART, false);
		}
	}
	
	@Override
	public void onPlayerLoginInside(L2PcInstance player)
	{
		if (!_enabled)
		{
			return;
		}
		
		if (((System.currentTimeMillis() - player.getLastAccess()) > getRestartTime()) && ((System.currentTimeMillis() - GameServer.dateTimeServerStarted.getTimeInMillis()) > getRestartAllowedTime()))
		{
			player.teleToLocation(TeleportWhereType.TOWN);
		}
	}
	
	public int getRestartAllowedTime()
	{
		return _restartAllowedTime;
	}
	
	public void setRestartAllowedTime(int time)
	{
		_restartAllowedTime = time;
	}
	
	public int getRestartTime()
	{
		return _restartTime;
	}
	
	public void setRestartTime(int time)
	{
		_restartTime = time;
	}
}