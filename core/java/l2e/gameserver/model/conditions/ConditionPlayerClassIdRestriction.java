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

public class ConditionPlayerClassIdRestriction extends Condition
{
	private final List<Integer> _classIds;
	
	public ConditionPlayerClassIdRestriction(List<Integer> classId)
	{
		_classIds = classId;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return (env.getPlayer() != null) && (_classIds.contains(env.getPlayer().getClassId().getId()));
	}
}