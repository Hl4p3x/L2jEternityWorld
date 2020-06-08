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

import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.stats.Env;

public final class ConditionPlayerHasCastle extends Condition
{	
	private final int _castle;
	
	public ConditionPlayerHasCastle(int castle)
	{
		_castle = castle;
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
			return _castle == 0;
		}
		
		if (_castle == -1)
		{
			return clan.getCastleId() > 0;
		}
		return clan.getCastleId() == _castle;
	}
}