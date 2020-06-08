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
package l2e.gameserver.model.zone.type;

import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.entity.clanhall.AuctionableHall;
import l2e.gameserver.model.zone.L2ZoneRespawn;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AgitDecoInfo;

public class L2ClanHallZone extends L2ZoneRespawn
{
	private int _clanHallId;
	
	public L2ClanHallZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("clanHallId"))
		{
			_clanHallId = Integer.parseInt(value);
			ClanHall hall = ClanHallManager.getInstance().getClanHallById(_clanHallId);
			if (hall == null)
				_log.warning("L2ClanHallZone: Clan hall with id " + _clanHallId + " does not exist!");
			else
				hall.setZone(this);
		}
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.CLAN_HALL, true);
			
			AuctionableHall clanHall = ClanHallManager.getInstance().getAuctionableHallById(_clanHallId);
			if (clanHall == null)
				return;
			
			AgitDecoInfo deco = new AgitDecoInfo(clanHall);
			character.sendPacket(deco);
			
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.CLAN_HALL, false);
		}
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
	
	public void banishForeigners(int owningClanId)
	{
		TeleportWhereType type = TeleportWhereType.CLANHALL_BANISH;
		for (L2PcInstance temp : getPlayersInside())
		{
			if (temp.getClanId() == owningClanId && owningClanId != 0)
				continue;
			
			temp.teleToLocation(type);
		}
	}
	
	public int getClanHallId()
	{
		return _clanHallId;
	}

	public void updateSiegeStatus()
	{
		if (_clanHallId == 35)
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					onEnter(character);
				}
				catch (Exception e)
				{
				}
			}	
		}
		else
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					character.setInsideZone(ZoneId.PVP, false);

					if (character.isPlayer())
						character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
				}
				catch (Exception e)
				{
				}
			}			
		}
	}
}