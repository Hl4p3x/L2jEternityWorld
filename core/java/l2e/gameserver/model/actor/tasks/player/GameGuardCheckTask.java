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

import java.util.logging.Logger;

import l2e.gameserver.data.xml.AdminParser;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.serverpackets.LeaveWorld;

public class GameGuardCheckTask implements Runnable
{
	private static final Logger _log = Logger.getLogger(GameGuardCheckTask.class.getName());
	
	private final L2PcInstance _player;
	
	public GameGuardCheckTask(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	public void run()
	{
		if ((_player != null))
		{
			L2GameClient client = _player.getClient();
			if ((client != null) && !client.isAuthedGG() && _player.isOnline())
			{
				AdminParser.getInstance().broadcastMessageToGMs("Client " + client + " failed to reply GameGuard query and is being kicked!");
				_log.info("Client " + client + " failed to reply GameGuard query and is being kicked!");
				
				client.close(LeaveWorld.STATIC_PACKET);
			}
		}
	}
}