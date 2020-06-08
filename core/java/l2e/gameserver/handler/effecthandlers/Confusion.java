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

import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.effects.EffectFlag;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.util.Rnd;

public class Confusion extends L2Effect
{
	public Confusion(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public int getEffectFlags()
	{
		return EffectFlag.CONFUSED.getMask();
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NONE;
	}

	@Override
	public boolean onActionTime()
	{
		final List<L2Character> targetList = new ArrayList<>();
		for (L2Object obj : getEffected().getKnownList().getKnownObjects().values())
		{
			if (((getEffected().isMonster() && obj.isL2Attackable()) || (obj instanceof L2Character)) && (obj != getEffected()))
			{
				targetList.add((L2Character) obj);
			}
		}
		if (!targetList.isEmpty())
		{
			final L2Character target = targetList.get(Rnd.nextInt(targetList.size()));
			getEffected().setTarget(target);
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		return false;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (!getEffected().isPlayer())
		{
			getEffected().getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}
}