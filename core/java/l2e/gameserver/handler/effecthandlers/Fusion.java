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

import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;

public class Fusion extends L2Effect
{
	public int _effect;
	public int _maxEffect;
	
	public Fusion(Env env, EffectTemplate template)
	{
		super(env, template);
		_effect = getSkill().getLevel();
		_maxEffect = SkillHolder.getInstance().getMaxLevel(getSkill().getId());
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FUSION;
	}
	
	@Override
	public void increaseEffect()
	{
		if (_effect < _maxEffect)
		{
			_effect++;
			updateBuff();
		}
	}
	
	@Override
	public void decreaseForce()
	{
		_effect--;
		if (_effect < 1)
		{
			exit();
		}
		else
		{
			updateBuff();
		}
	}
	
	private void updateBuff()
	{
		exit();
		SkillHolder.getInstance().getInfo(getSkill().getId(), _effect).getEffects(getEffector(), getEffected());
	}
}