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
package l2e.gameserver.handler.bypasshandlers;

import java.util.logging.Level;

import l2e.gameserver.handler.IBypassHandler;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2OlympiadManagerInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.model.olympiad.OlympiadGameManager;
import l2e.gameserver.model.olympiad.OlympiadGameTask;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExOlympiadMatchList;

public class OlympiadObservation implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"watchmatch",
		"arenachange"
	};
	
	@Override
	public final boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		try
		{
			final L2Npc olymanager = activeChar.getLastFolkNPC();
			
			if (command.startsWith(COMMANDS[0]))
			{
				activeChar.sendPacket(new ExOlympiadMatchList());
			}
			else
			{
				if ((olymanager == null) || !(olymanager instanceof L2OlympiadManagerInstance))
				{
					return false;
				}
				
				if (!activeChar.inObserverMode() && !activeChar.isInsideRadius(olymanager, 300, false, false))
				{
					return false;
				}
				
				if (OlympiadManager.getInstance().isRegisteredInComp(activeChar))
				{
					activeChar.sendPacket(SystemMessageId.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
					return false;
				}

				if (!Olympiad.getInstance().inCompPeriod())
				{
					activeChar.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
					return false;
				}

				if (!TvTEvent.isInactive() && TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
				{
					activeChar.sendMessage("You can not observe games while registered for TvT");
					return false;
				}
				
				if (!TvTRoundEvent.isInactive() && TvTRoundEvent.isPlayerParticipant(activeChar.getObjectId()))
				{
					activeChar.sendMessage("You can not observe games while registered for TvT");
					return false;
				}
				
				final int arenaId = Integer.parseInt(command.substring(12).trim());
				final OlympiadGameTask nextArena = OlympiadGameManager.getInstance().getOlympiadTask(arenaId);
				if (nextArena != null)
				{
					activeChar.enterOlympiadObserverMode(nextArena.getZone().getSpawns().get(0), arenaId);
					activeChar.setInstanceId(OlympiadGameManager.getInstance().getOlympiadTask(arenaId).getZone().getInstanceId());
				}
			}
			return true;
			
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception in " + getClass().getSimpleName(), e);
		}
		return false;
	}
	
	@Override
	public final String[] getBypassList()
	{
		return COMMANDS;
	}
}