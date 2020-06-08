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
package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class TeleportTask implements Runnable
{
	private final L2PcInstance _activeChar;
	private final Location _loc;
	
	public TeleportTask(L2PcInstance player, Location loc)
	{
		_activeChar = player;
		_loc = loc;
	}
	
	@Override
	public void run()
	{
		if ((_activeChar != null) && _activeChar.isOnline())
		{
			_activeChar.teleToLocation(_loc, true);
		}
	}
}