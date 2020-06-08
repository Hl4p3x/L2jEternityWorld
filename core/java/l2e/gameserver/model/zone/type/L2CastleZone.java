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

import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.zone.L2ZoneRespawn;
import l2e.gameserver.model.zone.ZoneId;

public class L2CastleZone extends L2ZoneRespawn
{
	private int _castleId;
	private Castle _castle = null;
	
	public L2CastleZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("castleId"))
			_castleId = Integer.parseInt(value);
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (getCastle() != null)
		{
			character.setInsideZone(ZoneId.CASTLE, true);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (getCastle() != null)
		{
			character.setInsideZone(ZoneId.CASTLE, false);
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
		TeleportWhereType type = TeleportWhereType.TOWN;
		for (L2PcInstance temp : getPlayersInside())
		{
			if (temp.getClanId() == owningClanId && owningClanId != 0)
				continue;
			
			temp.teleToLocation(type);
		}
	}
	
	public int getCastleId()
	{
		return _castleId;
	}
	
	private final Castle getCastle()
	{
		if (_castle == null)
		{
			_castle = CastleManager.getInstance().getCastleById(_castleId);
		}
		return _castle;
	}
}