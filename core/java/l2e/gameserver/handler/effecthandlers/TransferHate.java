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

import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.util.Util;

public class TransferHate extends L2Effect
{
	private final int _chance;
	
	public TransferHate(Env env, EffectTemplate template)
	{
		super(env, template);
		_chance = template.hasParameters() ? template.getParameters().getInteger("chance", 100) : 100;
	}
	
	@Override
	public boolean calcSuccess()
	{
		return Formulas.calcProbability(_chance, getEffector(), getEffected(), getSkill());
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NONE;
	}
	
	@Override
	public boolean onStart()
	{
		if (Util.checkIfInRange(getSkill().getEffectRange(), getEffector(), getEffected(), true))
		{
			for (L2Character obj : getEffector().getKnownList().getKnownCharactersInRadius(getSkill().getAffectRange()))
			{
				if ((obj == null) || !obj.isL2Attackable() || obj.isDead())
				{
					continue;
				}
				
				final L2Attackable hater = ((L2Attackable) obj);
				final int hate = hater.getHating(getEffector());
				if (hate <= 0)
				{
					continue;
				}
				
				hater.reduceHate(getEffector(), -hate);
				hater.addDamageHate(getEffected(), 0, hate);
			}
			return true;
		}
		return false;
	}
}