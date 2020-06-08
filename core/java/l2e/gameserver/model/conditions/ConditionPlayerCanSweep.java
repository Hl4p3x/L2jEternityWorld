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

import l2e.Config;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class ConditionPlayerCanSweep extends Condition
{
	private final boolean _val;
	
	public ConditionPlayerCanSweep(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		boolean canSweep = false;
		if (env.getPlayer() != null)
		{
			final L2PcInstance sweeper = env.getPlayer();
			final L2Skill sweep = env.getSkill();
			if (sweep != null)
			{
				final L2Object[] targets = sweep.getTargetList(sweeper);
				if (targets != null)
				{
					L2Attackable target;
					for (L2Object objTarget : targets)
					{
						if (objTarget instanceof L2Attackable)
						{
							target = (L2Attackable) objTarget;
							if (target.isDead())
							{
								if (target.isSpoil())
								{
									canSweep = target.checkSpoilOwner(sweeper, true);
									canSweep &= !target.isOldCorpse(sweeper, Config.MAX_SWEEPER_TIME, true);
									canSweep &= sweeper.getInventory().checkInventorySlotsAndWeight(target.getSpoilLootItems(), true, true);
								}
								else
								{
									sweeper.sendPacket(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED);
								}
							}
						}
					}
				}
			}
		}
		return (_val == canSweep);
	}
}