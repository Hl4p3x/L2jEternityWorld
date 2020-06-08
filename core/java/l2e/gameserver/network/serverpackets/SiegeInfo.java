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
package l2e.gameserver.network.serverpackets;

import java.util.Calendar;

import l2e.Config;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.ClanHall;

public class SiegeInfo extends L2GameServerPacket
{
	private Castle _castle;
	private ClanHall _hall;
	
	public SiegeInfo(Castle castle)
	{
		_castle = castle;
	}
	
	public SiegeInfo(ClanHall hall)
	{
		_hall = hall;
	}
	
	@Override
	protected final void writeImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		writeC(0xc9);
		if (_castle != null)
		{
			writeD(_castle.getId());
			
			final int ownerId = _castle.getOwnerId();
			
			writeD(((ownerId == activeChar.getClanId()) && (activeChar.isClanLeader())) ? 0x01 : 0x00);
			writeD(ownerId);
			if (ownerId > 0)
			{
				L2Clan owner = ClanHolder.getInstance().getClan(ownerId);
				if (owner != null)
				{
					writeS(owner.getName());
					writeS(owner.getLeaderName());
					writeD(owner.getAllyId());
					writeS(owner.getAllyName());
				}
				else
				{
					_log.warning("Null owner for castle: " + _castle.getName());
				}
			}
			else
			{
				writeS("");
				writeS("");
				writeD(0);
				writeS("");
			}
			writeD((int) (System.currentTimeMillis() / 1000));
			if (!_castle.getIsTimeRegistrationOver() && activeChar.isClanLeader() && (activeChar.getClanId() == _castle.getOwnerId()))
			{
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(_castle.getSiegeDate().getTimeInMillis());
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				
				writeD(0x00);
				writeD(Config.SIEGE_HOUR_LIST.size());
				for (int hour : Config.SIEGE_HOUR_LIST)
				{
					cal.set(Calendar.HOUR_OF_DAY, hour);
					writeD((int) (cal.getTimeInMillis() / 1000));
				}
			}
			else
			{
				writeD((int) (_castle.getSiegeDate().getTimeInMillis() / 1000));
				writeD(0x00);
			}
		}
		else
		{
			writeD(_hall.getId());
			
			final int ownerId = _hall.getOwnerId();
			
			writeD(((ownerId == activeChar.getClanId()) && (activeChar.isClanLeader())) ? 0x01 : 0x00);
			writeD(ownerId);
			if (ownerId > 0)
			{
				L2Clan owner = ClanHolder.getInstance().getClan(ownerId);
				if (owner != null)
				{
					writeS(owner.getName());
					writeS(owner.getLeaderName());
					writeD(owner.getAllyId());
					writeS(owner.getAllyName());
				}
				else
				{
					_log.warning("Null owner for siegable hall: " + _hall.getName());
				}
			}
			else
			{
				writeS("");
				writeS("");
				writeD(0);
				writeS("");
			}
			writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
			writeD((int) ((CHSiegeManager.getInstance().getSiegableHall(_hall.getId()).getNextSiegeTime()) / 1000));
			writeD(0x00);
		}
	}
}