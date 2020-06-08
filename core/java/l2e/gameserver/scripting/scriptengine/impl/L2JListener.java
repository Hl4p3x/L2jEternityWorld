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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2e.gameserver.scripting.scriptengine.impl;

import java.util.logging.Logger;

import l2e.gameserver.model.actor.events.listeners.IEventListener;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public abstract class L2JListener implements IEventListener
{
	protected static Logger log = Logger.getLogger(L2JListener.class.getName());
	
	private L2PcInstance _player = null;
	
	public L2JListener()
	{
	}
	
	public L2JListener(L2PcInstance player)
	{
		_player = player;
	}
	
	public abstract void register();
	
	public abstract void unregister();
	
	public L2PcInstance getPlayer()
	{
		return _player;
	}
}