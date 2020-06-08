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
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.model.zone.ZoneId;

public class L2SwampZone extends L2ZoneType
{
	private int _move_bonus;
	
	private int _castleId;
	private Castle _castle;
	
	public L2SwampZone(int id)
	{
		super(id);
		
		_move_bonus = -50;
		_castleId = 0;
		_castle = null;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("move_bonus"))
		{
			_move_bonus = Integer.parseInt(value);
		}
		else if (name.equals("castleId"))
		{
			_castleId = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	private Castle getCastle()
	{
		if ((_castleId > 0) && (_castle == null))
		{
			_castle = CastleManager.getInstance().getCastleById(_castleId);
		}
		
		return _castle;
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (getCastle() != null)
		{
			if (!getCastle().getSiege().getIsInProgress() || !getCastle().getSiege().isTrapsActive())
			{
				return;
			}
			
			final L2PcInstance player = character.getActingPlayer();
			if ((player != null) && player.isInSiege() && (player.getSiegeState() == 2))
			{
				return;
			}
		}
		
		character.setInsideZone(ZoneId.SWAMP, true);
		if (character.isPlayer())
		{
			character.getActingPlayer().broadcastUserInfo();
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character.isInsideZone(ZoneId.SWAMP))
		{
			character.setInsideZone(ZoneId.SWAMP, false);
			if (character.isPlayer())
			{
				character.getActingPlayer().broadcastUserInfo();
			}
		}
	}
	
	public int getMoveBonus()
	{
		return _move_bonus;
	}
}