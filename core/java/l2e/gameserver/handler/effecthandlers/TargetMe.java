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
package l2e.gameserver.handler.effecthandlers;

import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;

public class TargetMe extends L2Effect
{
	public TargetMe(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NONE;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected().isPlayable())
		{
			if (getEffected() instanceof L2SiegeSummonInstance)
			{
				return false;
			}
			
			if (getEffected().getTarget() != getEffector())
			{
				L2PcInstance effector = getEffector().getActingPlayer();
				if ((effector == null) || effector.checkPvpSkill(getEffected(), getSkill()))
				{
					getEffected().setTarget(getEffector());
				}
			}
			((L2Playable) getEffected()).setLockedTarget(getEffector());
			return true;
		}
		else if (getEffected().isL2Attackable() && !getEffected().isRaid())
		{
			return true;
		}
		return false;
	}
	
	@Override
	public void onExit()
	{
		if (getEffected().isPlayable())
		{
			((L2Playable) getEffected()).setLockedTarget(null);
		}
	}
}