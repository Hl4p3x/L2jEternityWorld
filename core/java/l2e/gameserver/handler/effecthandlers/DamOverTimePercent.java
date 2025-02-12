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

import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class DamOverTimePercent extends L2Effect
{
	private final boolean _canKill;
	
	public DamOverTimePercent(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_canKill = template.hasParameters() && template.getParameters().getBool("canKill", false);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DMG_OVER_TIME_PERCENT;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
		{
			return false;
		}
		
		double damage;
		
		if (getSkill().isToggle())
		{
			damage = getEffected().getCurrentHp() * calc() * getEffectTemplate().getTotalTickCount();
		}
		else
		{
			damage = getEffected().getCurrentHp() * calc();
		}
		
		if (damage >= (getEffected().getCurrentHp() - 1))
		{
			if (getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_HP);
				return false;
			}
			
			if (!_canKill)
			{
				if (getEffected().getCurrentHp() <= 1)
				{
					return getSkill().isToggle();
				}
				damage = getEffected().getCurrentHp() - 1;
			}
		}
		getEffected().reduceCurrentHpByDOT(damage, getEffector(), getSkill());
		getEffected().notifyDamageReceived(damage, getEffector(), getSkill(), false, true);
		
		return getSkill().isToggle();
	}
}