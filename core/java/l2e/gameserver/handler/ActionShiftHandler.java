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

import l2e.gameserver.handler.actionshifthandlers.*;
import l2e.gameserver.model.L2Object.InstanceType;

public class ActionShiftHandler
{
	private static Logger _log = Logger.getLogger(ActionShiftHandler.class.getName());

	private final Map<InstanceType, IActionHandler> _actionsShift;
	
	public static ActionShiftHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ActionShiftHandler()
	{
		_actionsShift = new FastMap<>();

		registerHandler(new L2DoorInstanceActionShift());
		registerHandler(new L2ItemInstanceActionShift());
		registerHandler(new L2NpcActionShift());
		registerHandler(new L2PcInstanceActionShift());
		registerHandler(new L2StaticObjectInstanceActionShift());
		registerHandler(new L2SummonActionShift());

		_log.info("Loaded " + _actionsShift.size() + " ActionShiftHandlers");
	}
	
	public void registerHandler(IActionHandler handler)
	{
		_actionsShift.put(handler.getInstanceType(), handler);
	}
	
	public IActionHandler getHandler(InstanceType iType)
	{
		IActionHandler result = null;
		for (InstanceType t = iType; t != null; t = t.getParent())
		{
			result = _actionsShift.get(t);
			if (result != null)
				break;
		}
		return result;
	}
	
	public int size()
	{
		return _actionsShift.size();
	}
	
	private static class SingletonHolder
	{
		protected static final ActionShiftHandler _instance = new ActionShiftHandler();
	}
}