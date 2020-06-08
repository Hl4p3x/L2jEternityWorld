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

import l2e.gameserver.model.ChanceCondition;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;

public class ChanceSkillTrigger extends L2Effect
{
	public ChanceSkillTrigger(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	public ChanceSkillTrigger(Env env, L2Effect effect)
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
		return L2EffectType.NONE;
	}

	@Override
	public ChanceCondition getTriggeredChanceCondition()
	{
		return getEffectTemplate().getChanceCondition();
	}

	@Override
	public int getTriggeredChanceId()
	{
		return getEffectTemplate().getTriggeredId();
	}

	@Override
	public int getTriggeredChanceLevel()
	{
		return getEffectTemplate().getTriggeredLevel();
	}

	@Override
	public boolean onActionTime()
	{
		getEffected().onActionTimeChanceEffect(getSkill().getElement());
		return getSkill().isPassive();
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().addChanceTrigger(this);
		getEffected().onStartChanceEffect(getSkill().getElement());
		return super.onStart();
	}
	
	@Override
	public void onExit()
	{
		if (isInUse() && (getTickCount() >= getEffectTemplate().getTotalTickCount()))
		{
			getEffected().onExitChanceEffect(getSkill().getElement());
		}
		getEffected().removeChanceEffect(this);
		super.onExit();
	}

	@Override
	public boolean triggersChanceSkill()
	{
		return getEffectTemplate().getTriggeredId() > 1;
	}
}