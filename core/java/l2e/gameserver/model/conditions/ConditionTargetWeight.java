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

public class ConditionTargetWeight extends Condition
{
	private final int _weight;
	
	public ConditionTargetWeight(int weight)
	{
		_weight = weight;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		final L2Character targetObj = env.getTarget();
		if ((targetObj != null) && targetObj.isPlayer())
		{
			final L2PcInstance target = targetObj.getActingPlayer();
			if (!target.getDietMode() && (target.getMaxLoad() > 0))
			{
				int weightproc = (((target.getCurrentLoad() - target.getBonusWeightPenalty()) * 100) / target.getMaxLoad());
				return (weightproc < _weight);
			}
		}
		return false;
	}
}