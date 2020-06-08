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

import l2e.gameserver.handler.telnethandlers.*;

public class TelnetHandler
{
	private static final Logger _log = Logger.getLogger(TelnetHandler.class.getName());

	private final TIntObjectHashMap<ITelnetHandler> _telnetHandlers;
	
	public static TelnetHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected TelnetHandler()
	{
		_telnetHandlers = new TIntObjectHashMap<>();

		registerHandler(new ChatsHandler());
		registerHandler(new DebugHandler());
		registerHandler(new HelpHandler());
		registerHandler(new PlayerHandler());
		registerHandler(new ReloadHandler());
		registerHandler(new ServerHandler());
		registerHandler(new StatusHandler());
		registerHandler(new ThreadHandler());

		_log.info("Loaded " + _telnetHandlers.size() + " TelnetHandlers");
	}
	
	public void registerHandler(ITelnetHandler handler)
	{
		for (String element : handler.getCommandList())
		{
			_telnetHandlers.put(element.toLowerCase().hashCode(), handler);
		}
	}
	
	public ITelnetHandler getHandler(String BypassCommand)
	{
		String command = BypassCommand;
		
		if (BypassCommand.indexOf(" ") != -1)
		{
			command = BypassCommand.substring(0, BypassCommand.indexOf(" "));
		}
		
		return _telnetHandlers.get(command.toLowerCase().hashCode());
	}
	
	public int size()
	{
		return _telnetHandlers.size();
	}
	
	private static class SingletonHolder
	{
		protected static final TelnetHandler _instance = new TelnetHandler();
	}
}