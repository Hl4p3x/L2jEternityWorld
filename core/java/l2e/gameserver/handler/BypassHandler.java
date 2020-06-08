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

import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.handler.bypasshandlers.*;

public class BypassHandler
{
	private static Logger _log = Logger.getLogger(BypassHandler.class.getName());
	
	private final TIntObjectHashMap<IBypassHandler> _datatable;
	
	public static BypassHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected BypassHandler()
	{
		_datatable = new TIntObjectHashMap<>();

		registerHandler(new Augment());
		registerHandler(new Buy());
		registerHandler(new BuyShadowItem());
		registerHandler(new ChatLink());
		registerHandler(new ClanWarehouse());
		registerHandler(new DropInfo());
		registerHandler(new ElcardiaBuff());
		registerHandler(new EventEngine());
		registerHandler(new Festival());
		registerHandler(new FortSiege());
		registerHandler(new Freight());
		registerHandler(new Henna());
		registerHandler(new ItemAuctionLink());
		registerHandler(new Leaderboard());
		registerHandler(new Link());
		registerHandler(new Loto());
		registerHandler(new ManorManager());
		registerHandler(new Multisell());
		registerHandler(new Observation());
		registerHandler(new OlympiadManagerLink());
		registerHandler(new OlympiadObservation());
		registerHandler(new PlayerHelp());
		registerHandler(new PrivateWarehouse());
		registerHandler(new QuestLink());
		registerHandler(new QuestList());
		registerHandler(new ReceivePremium());
		registerHandler(new ReleaseAttribute());
		registerHandler(new RentPet());
		registerHandler(new Rift());
		registerHandler(new SkillList());
		registerHandler(new SupportBlessing());
		registerHandler(new SupportMagic());
		registerHandler(new TerritoryStatus());
		registerHandler(new VoiceCommand());
		registerHandler(new Wear());

		_log.info("Loaded " + _datatable.size() + " BypassHandlers");
	}
	
	public void registerHandler(IBypassHandler handler)
	{
		for (String element : handler.getBypassList())
		{
			if (Config.DEBUG)
				_log.log(Level.FINE, "Adding handler for command " + element);
			
			_datatable.put(element.toLowerCase().hashCode(), handler);
		}
	}
	
	public IBypassHandler getHandler(String BypassCommand)
	{
		String command = BypassCommand;
		
		if (BypassCommand.indexOf(" ") != -1)
		{
			command = BypassCommand.substring(0, BypassCommand.indexOf(" "));
		}
		
		if (Config.DEBUG)
			_log.log(Level.FINE, "getting handler for command: " + command + " -> " + (_datatable.get(command.hashCode()) != null));
		
		return _datatable.get(command.toLowerCase().hashCode());
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final BypassHandler _instance = new BypassHandler();
	}
}