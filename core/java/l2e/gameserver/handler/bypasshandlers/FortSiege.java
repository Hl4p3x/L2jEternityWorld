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

import l2e.gameserver.handler.IBypassHandler;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2FortSiegeNpcInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class FortSiege implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"fort_register", 
		"fort_unregister"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2FortSiegeNpcInstance))
		{
			return false;
		}
		
		if ((activeChar.getClanId() > 0) && ((activeChar.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) == L2Clan.CP_CS_MANAGE_SIEGE))
		{
			if (command.toLowerCase().startsWith(COMMANDS[0])) // register
			{
				if ((System.currentTimeMillis() < TerritoryWarManager.getInstance().getTWStartTimeInMillis()) && TerritoryWarManager.getInstance().getIsRegistrationOver())
				{
					activeChar.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
					return false;
				}
				else if ((System.currentTimeMillis() > TerritoryWarManager.getInstance().getTWStartTimeInMillis()) && TerritoryWarManager.getInstance().isTWChannelOpen())
				{
					activeChar.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
					return false;
				}
				else if (((L2Npc) target).getFort().getSiege().registerAttacker(activeChar, false))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REGISTERED_TO_S1_FORTRESS_BATTLE);
					sm.addString(((L2Npc) target).getFort().getName());
					activeChar.sendPacket(sm);
					((L2Npc) target).showChatWindow(activeChar, 7);
					return true;
				}
			}
			else if (command.toLowerCase().startsWith(COMMANDS[1])) // unregister
			{
				((L2Npc) target).getFort().getSiege().removeSiegeClan(activeChar.getClan());
				((L2Npc) target).showChatWindow(activeChar, 8);
				return true;
			}
			return false;
		}
		
		((L2Npc) target).showChatWindow(activeChar, 10);
		
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}