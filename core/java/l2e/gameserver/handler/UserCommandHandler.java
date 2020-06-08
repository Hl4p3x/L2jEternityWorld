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
package l2e.gameserver.handler;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.handler.usercommandhandlers.*;

public class UserCommandHandler
{
	private static Logger _log = Logger.getLogger(UserCommandHandler.class.getName());
	
	private final TIntObjectHashMap<IUserCommandHandler> _datatable;
	
	public static UserCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected UserCommandHandler()
	{
		_datatable = new TIntObjectHashMap<>();

		registerHandler(new ChannelDelete());
		registerHandler(new ChannelInfo());
		registerHandler(new ChannelLeave());
		registerHandler(new ClanPenalty());
		registerHandler(new ClanWarsList());
		registerHandler(new DisMount());
		registerHandler(new InstanceZone());
		registerHandler(new Loc());
		registerHandler(new Mount());
		registerHandler(new MyBirthday());
		registerHandler(new OlympiadStat());
		registerHandler(new PartyInfo());
		registerHandler(new SiegeStatus());
		registerHandler(new Time());
		registerHandler(new Unstuck());

		_log.info("Loaded " + _datatable.size() + " UserHandlers.");
	}
	
	public void registerHandler(IUserCommandHandler handler)
	{
		int[] ids = handler.getUserCommandList();
		for (int i = 0; i < ids.length; i++)
		{
			if (Config.DEBUG)
				_log.fine("Adding handler for user command " + ids[i]);
			_datatable.put(ids[i], handler);
		}
	}
	
	public IUserCommandHandler getHandler(int userCommand)
	{
		if (Config.DEBUG)
			_log.fine("getting handler for user command: " + userCommand);
		return _datatable.get(userCommand);
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final UserCommandHandler _instance = new UserCommandHandler();
	}
}