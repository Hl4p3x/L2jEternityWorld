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

public class L2NoStoreZone extends L2ZoneType
{
	public L2NoStoreZone(final int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.NO_STORE, true);
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.NO_STORE, false);
		}
	}
}