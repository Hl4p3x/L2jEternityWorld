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
package l2e.gameserver.handler.punishmenthandlers;

import l2e.gameserver.LoginServerThread;
import l2e.gameserver.handler.IPunishmentHandler;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.punishment.PunishmentTask;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.network.L2GameClient;

public class BanHandler implements IPunishmentHandler
{
	@Override
	public void onStart(PunishmentTask task)
	{
		switch (task.getAffect())
		{
			case CHARACTER:
			{
				int objectId = Integer.parseInt(String.valueOf(task.getKey()));
				final L2PcInstance player = L2World.getInstance().getPlayer(objectId);
				if (player != null)
				{
					applyToPlayer(player);
				}
				break;
			}
			case ACCOUNT:
			{
				String account = String.valueOf(task.getKey());
				final L2GameClient client = LoginServerThread.getInstance().getClient(account);
				if (client != null)
				{
					final L2PcInstance player = client.getActiveChar();
					if (player != null)
					{
						applyToPlayer(player);
					}
					else
					{
						client.closeNow();
					}
				}
				break;
			}
			case IP:
			{
				String ip = String.valueOf(task.getKey());
				for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
				{
					if (player.getIPAddress().equals(ip))
					{
						applyToPlayer(player);
					}
				}
				break;
			}
		}
	}
	
	@Override
	public void onEnd(PunishmentTask task)
	{
		
	}
	
	private static void applyToPlayer(L2PcInstance player)
	{
		player.logout();
	}
	
	@Override
	public PunishmentType getType()
	{
		return PunishmentType.BAN;
	}
}