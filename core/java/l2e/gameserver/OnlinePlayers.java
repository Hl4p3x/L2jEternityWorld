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
package l2e.gameserver;

import l2e.Config;
import l2e.gameserver.customs.CustomMessage;
import l2e.gameserver.model.L2World;

/**
 * Created by LordWinter 11.02.2011
 */
public class OnlinePlayers
{
	private static OnlinePlayers _instance;
	
	class AnnounceOnline implements Runnable
	{
		@Override
		public void run()
		{
			if (Config.ONLINE_PLAYERS_AT_STARTUP)
			{
				CustomMessage msg = new CustomMessage("OnlinePlayers.ONLINE_ANNOUNCE", true);
				msg.add(L2World.getInstance().getAllPlayers().size() * Config.FAKE_ONLINE);
				Announcements.getInstance().announceToAll(msg);
				ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceOnline(), Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL);
			}
		}
	}
	
	public static OnlinePlayers getInstance()
	{
		if (_instance == null)
		{
			_instance = new OnlinePlayers();
		}
		return _instance;
	}
	
	private OnlinePlayers()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceOnline(), Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL);
	}
}