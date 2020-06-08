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

import java.util.logging.Level;

import l2e.gameserver.cache.CrestCache;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.SystemMessageId;

public final class RequestExSetPledgeCrestLarge extends L2GameClientPacket
{
	private int _length;
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		_length = readD();
		if (_length > 2176)
		{
			return;
		}
		
		_data = new byte[_length];
		readB(_data);
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
		if (clan == null)
		{
			return;
		}
		
		if (_length < 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}
		if (_length > 2176)
		{
			activeChar.sendMessage("The insignia file size is greater than 2176 bytes.");
			return;
		}
		
		boolean updated = false;
		int crestLargeId = -1;
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_REGISTER_CREST) == L2Clan.CP_CL_REGISTER_CREST)
		{
			if ((_length == 0) || (_data == null))
			{
				if (clan.getCrestLargeId() == 0)
				{
					return;
				}
				
				crestLargeId = 0;
				activeChar.sendMessage("The insignia has been removed.");
				updated = true;
			}
			else
			{
				if ((clan.getCastleId() == 0) && (clan.getHideoutId() == 0))
				{
					activeChar.sendMessage("Only a clan that owns a clan hall or a castle can get their emblem displayed on clan related items"); // there is a system message for that but didnt found the id
					return;
				}
				
				crestLargeId = IdFactory.getInstance().getNextId();
				if (!CrestCache.getInstance().savePledgeCrestLarge(crestLargeId, _data))
				{
					_log.log(Level.INFO, "Error saving large crest for clan " + clan.getName() + " [" + clan.getId() + "]");
					return;
				}
				
				activeChar.sendPacket(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED);
				updated = true;
			}
		}
		
		if (updated && (crestLargeId != -1))
		{
			clan.changeLargeCrest(crestLargeId);
		}
	}
}