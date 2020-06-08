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
package l2e.gameserver.model.conditions;

import java.util.ArrayList;

import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.stats.Env;

public final class ConditionPlayerHasClanHall extends Condition
{
	private final ArrayList<Integer> _clanHall;
	
	public ConditionPlayerHasClanHall(ArrayList<Integer> clanHall)
	{
		_clanHall = clanHall;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
		{
			return false;
		}
		
		final L2Clan clan = env.getPlayer().getClan();
		if (clan == null)
		{
			return ((_clanHall.size() == 1) && (_clanHall.get(0) == 0));
		}
		
		if ((_clanHall.size() == 1) && (_clanHall.get(0) == -1))
		{
			return clan.getHideoutId() > 0;
		}
		return _clanHall.contains(clan.getHideoutId());
	}
}