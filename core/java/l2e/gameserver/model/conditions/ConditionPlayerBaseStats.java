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

import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerBaseStats extends Condition
{	
	private final BaseStat _stat;
	private final int _value;
	
	public ConditionPlayerBaseStats(L2Character player, BaseStat stat, int value)
	{
		super();
		_stat = stat;
		_value = value;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
		{
			return false;
		}
		final L2PcInstance player = env.getPlayer();
		switch (_stat)
		{
			case Int:
				return player.getINT() >= _value;
			case Str:
				return player.getSTR() >= _value;
			case Con:
				return player.getCON() >= _value;
			case Dex:
				return player.getDEX() >= _value;
			case Men:
				return player.getMEN() >= _value;
			case Wit:
				return player.getWIT() >= _value;
		}
		return false;
	}
}

enum BaseStat
{
	Int,
	Str,
	Con,
	Dex,
	Men,
	Wit
}
