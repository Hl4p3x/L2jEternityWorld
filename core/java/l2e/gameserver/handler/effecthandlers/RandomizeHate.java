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

import java.util.ArrayList;
import java.util.List;

import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.util.Rnd;

public class RandomizeHate extends L2Effect
{
	public RandomizeHate(Env env, EffectTemplate template)
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
		if ((getEffected() == null) || (getEffected() == getEffector()) || !getEffected().isL2Attackable())
		{
			return false;
		}
		
		L2Attackable effectedMob = (L2Attackable) getEffected();
		final List<L2Character> targetList = new ArrayList<>();
		for (L2Character cha : getEffected().getKnownList().getKnownCharacters())
		{
			if ((cha != null) && (cha != effectedMob) && (cha != getEffector()))
			{
				if (cha.isL2Attackable() && (((L2Attackable) cha).getFactionId() != null) && ((L2Attackable) cha).getFactionId().equals(effectedMob.getFactionId()))
				{
					continue;
				}
				
				targetList.add(cha);
			}
		}

		if (targetList.isEmpty())
		{
			return true;
		}
		
		final L2Character target = targetList.get(Rnd.get(targetList.size()));
		final int hate = effectedMob.getHating(getEffector());
		effectedMob.stopHating(getEffector());
		effectedMob.addDamageHate(target, 0, hate);
		
		return true;
	}
}