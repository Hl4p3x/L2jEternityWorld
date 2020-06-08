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

import java.util.concurrent.ScheduledFuture;

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.tasks.player.BuffsBackTask;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;

public class SignetNoise extends L2Effect
{
	protected ScheduledFuture<?> timerTask;
	
	public SignetNoise(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_GROUND;
	}
	
	@Override
	public void onExit()
	{
		if (timerTask != null)
		{
			timerTask.cancel(false);
		}
		
		if (getEffected() != null)
		{
			getEffected().deleteMe();
		}
	}
	
	@Override
	public boolean onStart()
	{
		timerTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TimerTask(), 2000, 2000);
		return true;
	}
	
	protected class TimerTask implements Runnable
	{
		@Override
		public void run()
		{
			L2PcInstance caster = getEffector().getActingPlayer();
			for (L2Character target : getEffected().getKnownList().getKnownCharactersInRadius(getSkill().getAffectRange()))
			{
				if ((target == null) || (target == caster))
				{
					continue;
				}
				
				if (caster.canAttackCharacter(target))
				{
					for (L2Effect effect : target.getAllEffects())
					{
						if (effect.getSkill().isDance())
						{
							if (Config.RESTORE_DISPEL_SKILLS)
							{
								if (getSkill().hasEffectType(L2EffectType.HEAL_OVER_TIME) || getSkill().hasEffectType(L2EffectType.CPHEAL_OVER_TIME) || getSkill().hasEffectType(L2EffectType.MANA_HEAL_OVER_TIME))
								{
									continue;
								}
								ThreadPoolManager.getInstance().scheduleGeneral(new BuffsBackTask(effect.getSkill(), target.getActingPlayer()), Config.RESTORE_DISPEL_SKILLS_TIME * 1000);
							}
							effect.exit();
						}
					}
				}
			}
			return;
		}
	}
}