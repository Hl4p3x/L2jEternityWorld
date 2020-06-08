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

import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.serverpackets.ExRegenMax;
import l2e.gameserver.network.serverpackets.StatusUpdate;

public class HealOverTime extends L2Effect
{
	public HealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	public HealOverTime(Env env, L2Effect effect)
	{
		super(env, effect);
	}
	
	@Override
	public boolean canBeStolen()
	{
		return true;
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HEAL_OVER_TIME;
	}
	
	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		
		if ((target.getFirstEffect(L2EffectType.INVINCIBLE) != null) || target.isInvul())
		{
			return false;
		}
		
		if (getEffected().isPlayer() && (getEffectTemplate().getTotalTickCount() > 0))
		{
			getEffected().sendPacket(new ExRegenMax(calc(), getEffectTemplate().getTotalTickCount() * getEffectTemplate().getAbnormalTime(), getEffectTemplate().getTotalTickCount()));
		}
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead() || getEffected().isDoor())
		{
			return false;
		}
		
		double hp = getEffected().getCurrentHp();
		double maxhp = getEffected().getMaxRecoverableHp();
		
		if (hp >= maxhp)
		{
			return false;
		}
		
		if (getSkill().isToggle())
		{
			hp += calc() * getEffectTemplate().getTotalTickCount();
		}
		else
		{
			hp += calc();
		}
		
		hp = Math.min(hp, maxhp);
		
		getEffected().setCurrentHp(hp);
		StatusUpdate suhp = new StatusUpdate(getEffected());
		suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
		getEffected().sendPacket(suhp);
		
		return getSkill().isToggle();
	}
}