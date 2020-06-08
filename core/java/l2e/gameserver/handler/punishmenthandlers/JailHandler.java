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
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.cache.HtmCache;
import l2e.gameserver.handler.IPunishmentHandler;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.tasks.player.TeleportTask;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.model.punishment.PunishmentTask;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.L2JailZone;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.scripting.scriptengine.listeners.player.PlayerSpawnListener;

public class JailHandler extends PlayerSpawnListener implements IPunishmentHandler
{
	@Override
	public void onPlayerLogin(L2PcInstance activeChar)
	{
		if (activeChar.isJailed() && !activeChar.isInsideZone(ZoneId.JAIL))
		{
			applyToPlayer(null, activeChar);
		}
		else if (!activeChar.isJailed() && activeChar.isInsideZone(ZoneId.JAIL) && !activeChar.isGM())
		{
			removeFromPlayer(activeChar);
		}
	}
	
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
					applyToPlayer(task, player);
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
						applyToPlayer(task, player);
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
						applyToPlayer(task, player);
					}
				}
				break;
			}
		}
	}
	
	@Override
	public void onEnd(PunishmentTask task)
	{
		switch (task.getAffect())
		{
			case CHARACTER:
			{
				int objectId = Integer.parseInt(String.valueOf(task.getKey()));
				final L2PcInstance player = L2World.getInstance().getPlayer(objectId);
				if (player != null)
				{
					removeFromPlayer(player);
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
						removeFromPlayer(player);
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
						removeFromPlayer(player);
					}
				}
				break;
			}
		}
	}
	
	private static void applyToPlayer(PunishmentTask task, L2PcInstance player)
	{
		player.setInstanceId(0);
		player.setIsIn7sDungeon(false);
		
		if (!TvTEvent.isInactive() && TvTEvent.isPlayerParticipant(player.getObjectId()))
		{
			TvTEvent.removeParticipant(player.getObjectId());
		}
		
		if (!TvTRoundEvent.isInactive() && TvTRoundEvent.isPlayerParticipant(player.getObjectId()))
		{
			TvTEvent.removeParticipant(player.getObjectId());
		}
		
		if (OlympiadManager.getInstance().isRegisteredInComp(player))
		{
			OlympiadManager.getInstance().removeDisconnectedCompetitor(player);
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(player, L2JailZone.getLocationIn()), 2000);
		
		final NpcHtmlMessage msg = new NpcHtmlMessage(0);
		String content = HtmCache.getInstance().getHtm(player.getLang(), "data/html/jail_in.htm");
		if (content != null)
		{
			content = content.replaceAll("%reason%", task != null ? task.getReason() : "");
			content = content.replaceAll("%punishedBy%", task != null ? task.getPunishedBy() : "");
			msg.setHtml(content);
		}
		else
		{
			msg.setHtml("<html><body>You have been put in jail by an admin.</body></html>");
		}
		player.sendPacket(msg);
		if (task != null)
		{
			long delay = ((task.getExpirationTime() - System.currentTimeMillis()) / 1000);
			if (delay > 0)
			{
				player.sendMessage("You've been jailed for " + (delay > 60 ? ((delay / 60) + " minutes.") : delay + " seconds."));
			}
			else
			{
				player.sendMessage("You've been jailed forever.");
			}
		}
	}
	
	private static void removeFromPlayer(L2PcInstance player)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(player, L2JailZone.getLocationOut()), 2000);
		
		final NpcHtmlMessage msg = new NpcHtmlMessage(0);
		String content = HtmCache.getInstance().getHtm(player.getLang(), "data/html/jail_out.htm");
		if (content != null)
		{
			msg.setHtml(content);
		}
		else
		{
			msg.setHtml("<html><body>You are free for now, respect server rules!</body></html>");
		}
		player.sendPacket(msg);
	}
	
	@Override
	public PunishmentType getType()
	{
		return PunishmentType.JAIL;
	}
}