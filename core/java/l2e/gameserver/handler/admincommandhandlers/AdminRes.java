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
package l2e.gameserver.handler.admincommandhandlers;

import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2ControllableMobInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.taskmanager.DecayTaskManager;

public class AdminRes implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminRes.class.getName());
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_res",
		"admin_res_monster"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_res "))
			handleRes(activeChar, command.split(" ")[1]);
		else if (command.equals("admin_res"))
			handleRes(activeChar);
		else if (command.startsWith("admin_res_monster "))
			handleNonPlayerRes(activeChar, command.split(" ")[1]);
		else if (command.equals("admin_res_monster"))
			handleNonPlayerRes(activeChar);
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleRes(L2PcInstance activeChar)
	{
		handleRes(activeChar, null);
	}
	
	private void handleRes(L2PcInstance activeChar, String resParam)
	{
		L2Object obj = activeChar.getTarget();
		
		if (resParam != null)
		{
			L2PcInstance plyr = L2World.getInstance().getPlayer(resParam);
			
			if (plyr != null)
			{
				obj = plyr;
			}
			else
			{
				try
				{
					int radius = Integer.parseInt(resParam);
					
					for (L2PcInstance knownPlayer : activeChar.getKnownList().getKnownPlayersInRadius(radius))
						doResurrect(knownPlayer);
					
					activeChar.sendMessage("Resurrected all players within a " + radius + " unit radius.");
					return;
				}
				catch (NumberFormatException e)
				{
					activeChar.sendMessage("Enter a valid player name or radius.");
					return;
				}
			}
		}
		
		if (obj == null)
			obj = activeChar;
		
		if (obj instanceof L2ControllableMobInstance)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		doResurrect((L2Character) obj);
		
		if (Config.DEBUG)
			_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") resurrected character " + obj.getObjectId());
	}
	
	private void handleNonPlayerRes(L2PcInstance activeChar)
	{
		handleNonPlayerRes(activeChar, "");
	}
	
	private void handleNonPlayerRes(L2PcInstance activeChar, String radiusStr)
	{
		L2Object obj = activeChar.getTarget();
		
		try
		{
			int radius = 0;
			
			if (!radiusStr.isEmpty())
			{
				radius = Integer.parseInt(radiusStr);
				
				for (L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
					if (!(knownChar instanceof L2PcInstance) && !(knownChar instanceof L2ControllableMobInstance))
						doResurrect(knownChar);
				
				activeChar.sendMessage("Resurrected all non-players within a " + radius + " unit radius.");
			}
		}
		catch (NumberFormatException e)
		{
			activeChar.sendMessage("Enter a valid radius.");
			return;
		}
		
		if (obj instanceof L2PcInstance || obj instanceof L2ControllableMobInstance)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		doResurrect((L2Character) obj);
	}
	
	private void doResurrect(L2Character targetChar)
	{
		if (!targetChar.isDead())
			return;
		
		if (targetChar instanceof L2PcInstance)
			((L2PcInstance) targetChar).restoreExp(100.0);
		else
			DecayTaskManager.getInstance().cancelDecayTask(targetChar);
		
		targetChar.doRevive();
	}
}