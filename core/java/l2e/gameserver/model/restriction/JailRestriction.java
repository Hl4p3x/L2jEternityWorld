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
package l2e.gameserver.model.restriction;

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.zone.ZoneId;

final class JailRestriction extends AbstractRestriction
{
	@Override
	public boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		if (isInJail(activeChar))
		{
			activeChar.sendMessage("You are in jail!");
			return true;
		}
		return false;
	}

	@Override
	public boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		if (isInJail(activeChar) || isInJail(target))
		{
			activeChar.sendMessage("Player is in jail!");
			return false;
		}
		return true;
	}

	@Override
	public boolean canTeleport(L2PcInstance activeChar)
	{
		if (isInJail(activeChar))
		{
			activeChar.sendMessage("You can't teleport in jail.");
			return false;
		}
		return true;
	}

	private boolean isInJail(L2PcInstance player)
	{
		return player.isJailed() || player.isInsideZone(ZoneId.JAIL);
	}
}