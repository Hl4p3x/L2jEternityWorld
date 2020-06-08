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

import java.util.List;

import l2e.gameserver.model.stats.Env;

public class ConditionPlayerServitorNpcId extends Condition
{
	private final List<Integer> _npcIds;
	
	public ConditionPlayerServitorNpcId(List<Integer> npcIds)
	{
		if ((npcIds.size() == 1) && (npcIds.get(0) == 0))
		{
			_npcIds = null;
		}
		else
		{
			_npcIds = npcIds;
		}
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if ((env.getPlayer() == null) || !env.getPlayer().hasSummon())
		{
			return false;
		}
		return (_npcIds == null) || _npcIds.contains(env.getPlayer().getSummon().getId());
	}
}