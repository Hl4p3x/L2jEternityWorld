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

import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;

public class L2ArenaZone extends L2ZoneType
{
	public L2ArenaZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character.isPlayer())
		{
			if (!character.isInsideZone(ZoneId.PVP))
			{
				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
			}
		}
		character.setInsideZone(ZoneId.PVP, true);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character.isPlayer())
		{
			if (!character.isInsideZone(ZoneId.PVP))
			{
				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			}
		}
		character.setInsideZone(ZoneId.PVP, false);
	}
}