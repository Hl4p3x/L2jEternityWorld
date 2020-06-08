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

import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExShowDominionRegistry;

public final class RequestJoinDominionWar extends L2GameClientPacket
{
	private int _territoryId;
	private int _isClan;
	private int _isJoining;
	
	@Override
	protected void readImpl()
	{
		_territoryId = readD();
		_isClan = readD();
		_isJoining = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		L2Clan clan = activeChar.getClan();
		int castleId = _territoryId - 80;
		
		if (TerritoryWarManager.getInstance().getIsRegistrationOver())
		{
			activeChar.sendPacket(SystemMessageId.NOT_TERRITORY_REGISTRATION_PERIOD);
			return;
		}
		else if ((clan != null) && (TerritoryWarManager.getInstance().getTerritory(castleId).getOwnerClan() == clan))
		{
			activeChar.sendPacket(SystemMessageId.THE_TERRITORY_OWNER_CLAN_CANNOT_PARTICIPATE_AS_MERCENARIES);
			return;
		}
		
		if (_isClan == 0x01)
		{
			if ((activeChar.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) != L2Clan.CP_CS_MANAGE_SIEGE)
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			
			if (clan == null)
			{
				return;
			}
			
			if (_isJoining == 1)
			{
				if (System.currentTimeMillis() < clan.getDissolvingExpiryTime())
				{
					activeChar.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
					return;
				}
				else if (TerritoryWarManager.getInstance().checkIsRegistered(-1, clan))
				{
					activeChar.sendPacket(SystemMessageId.YOU_ALREADY_REQUESTED_TW_REGISTRATION);
					return;
				}
				TerritoryWarManager.getInstance().registerClan(castleId, clan);
			}
			else
			{
				TerritoryWarManager.getInstance().removeClan(castleId, clan);
			}
		}
		else
		{
			if ((activeChar.getLevel() < 40) || (activeChar.getClassId().level() < 2))
			{
				return;
			}
			if (_isJoining == 1)
			{
				if (TerritoryWarManager.getInstance().checkIsRegistered(-1, activeChar.getObjectId()))
				{
					activeChar.sendPacket(SystemMessageId.YOU_ALREADY_REQUESTED_TW_REGISTRATION);
					return;
				}
				else if ((clan != null) && TerritoryWarManager.getInstance().checkIsRegistered(-1, clan))
				{
					activeChar.sendPacket(SystemMessageId.YOU_ALREADY_REQUESTED_TW_REGISTRATION);
					return;
				}
				TerritoryWarManager.getInstance().registerMerc(castleId, activeChar);
			}
			else
			{
				TerritoryWarManager.getInstance().removeMerc(castleId, activeChar);
			}
		}
		activeChar.sendPacket(new ExShowDominionRegistry(castleId, activeChar));
	}
}