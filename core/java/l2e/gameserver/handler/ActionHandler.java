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

import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;

import l2e.gameserver.handler.actionhandlers.*;
import l2e.gameserver.model.L2Object.InstanceType;

public class ActionHandler
{
	private static Logger _log = Logger.getLogger(ActionHandler.class.getName());

	private final Map<InstanceType, IActionHandler> _actions;
	
	public static ActionHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ActionHandler()
	{
		_actions = new FastMap<>();

		registerHandler(new L2ArtefactInstanceAction());
		registerHandler(new L2DecoyAction());
		registerHandler(new L2DoorInstanceAction());
		registerHandler(new L2ItemInstanceAction());
		registerHandler(new L2NpcAction());
		registerHandler(new L2PcInstanceAction());
		registerHandler(new L2PetInstanceAction());
		registerHandler(new L2StaticObjectInstanceAction());
		registerHandler(new L2SummonAction());
		registerHandler(new L2TrapAction());

		_log.info("Loaded " + _actions.size() + " ActionHandlers");
	}
	
	public void registerHandler(IActionHandler handler)
	{
		_actions.put(handler.getInstanceType(), handler);
	}
	
	public IActionHandler getHandler(InstanceType iType)
	{
		IActionHandler result = null;
		for (InstanceType t = iType; t != null; t = t.getParent())
		{
			result = _actions.get(t);
			if (result != null)
				break;
		}
		return result;
	}
	
	public int size()
	{
		return _actions.size();
	}
	
	private static class SingletonHolder
	{
		protected static final ActionHandler _instance = new ActionHandler();
	}
}