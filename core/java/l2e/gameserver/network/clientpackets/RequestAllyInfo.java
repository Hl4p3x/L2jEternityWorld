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
package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.ClanInfo;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AllianceInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestAllyInfo extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		SystemMessage sm;
		final int allianceId = activeChar.getAllyId();
		if (allianceId > 0)
		{
			final AllianceInfo ai = new AllianceInfo(allianceId);
			activeChar.sendPacket(ai);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_INFO_HEAD);
			activeChar.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_NAME_S1);
			sm.addString(ai.getName());
			activeChar.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1);
			sm.addString(ai.getLeaderC());
			sm.addString(ai.getLeaderP());
			activeChar.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
			sm.addNumber(ai.getOnline());
			sm.addNumber(ai.getTotal());
			activeChar.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_CLAN_TOTAL_S1);
			sm.addNumber(ai.getAllies().length);
			activeChar.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_HEAD);
			for (final ClanInfo aci : ai.getAllies())
			{
				activeChar.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_NAME_S1);
				sm.addString(aci.getClan().getName());
				activeChar.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEADER_S1);
				sm.addString(aci.getClan().getLeaderName());
				activeChar.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEVEL_S1);
				sm.addNumber(aci.getClan().getLevel());
				activeChar.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
				sm.addNumber(aci.getOnline());
				sm.addNumber(aci.getTotal());
				activeChar.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_SEPARATOR);
			}
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_FOOT);
			activeChar.sendPacket(sm);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
		}
	}
}