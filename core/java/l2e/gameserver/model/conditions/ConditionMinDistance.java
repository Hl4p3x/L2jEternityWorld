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

import l2e.gameserver.model.stats.Env;

public class ConditionMinDistance extends Condition
{
	private final int _sqDistance;
	
	public ConditionMinDistance(int sqDistance)
	{
		_sqDistance = sqDistance;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return (env.getTarget() != null) && (env.getCharacter().getDistanceSq(env.getTarget()) >= _sqDistance);
	}
}