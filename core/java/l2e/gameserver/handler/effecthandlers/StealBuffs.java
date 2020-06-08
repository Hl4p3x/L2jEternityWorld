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

import java.util.List;

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.tasks.player.BuffsBackTask;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class StealBuffs extends L2Effect
{
	private final String _slot;
	private final int _rate;
	private final int _max;
	
	public StealBuffs(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_slot = template.getParameters().getString("slot", null);
		_rate = template.getParameters().getInteger("rate", 0);
		_max = template.getParameters().getInteger("max", 0);
	}
	
	@Override
	public boolean canBeStolen()
	{
		return false;
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NONE;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() != null) && getEffected().isPlayer() && (getEffector() != getEffected()))
		{
			final List<L2Effect> toSteal = Formulas.calcCancelStealEffects(getEffector(), getEffected(), getSkill(), _slot, _rate, _max);
			
			if (toSteal.size() == 0)
			{
				return false;
			}
			
			for (L2Effect eff : toSteal)
			{
				Env env = new Env();
				env.setCharacter(getEffected());
				env.setTarget(getEffector());
				env.setSkill(eff.getSkill());
				
				L2Effect effect = eff.getEffectTemplate().getStolenEffect(env, eff);
				if (effect != null)
				{
					effect.scheduleEffect();
					if (effect.isIconDisplay() && getEffector().isPlayer())
					{
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(effect);
						getEffector().sendPacket(sm);
					}
				}
				
				if (Config.RESTORE_DISPEL_SKILLS)
				{
					if (getSkill().hasEffectType(L2EffectType.HEAL_OVER_TIME) || getSkill().hasEffectType(L2EffectType.CPHEAL_OVER_TIME) || getSkill().hasEffectType(L2EffectType.MANA_HEAL_OVER_TIME))
					{
						continue;
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new BuffsBackTask(eff.getSkill(), getEffected().getActingPlayer()), Config.RESTORE_DISPEL_SKILLS_TIME * 1000);
				}
				eff.exit();
			}
			return true;
		}
		
		if (getSkill().hasSelfEffects())
		{
			L2Effect effect = getEffector().getFirstEffect(getSkill().getId());
			if ((effect != null) && effect.isSelfEffect())
			{
				effect.exit();
			}
			getSkill().getEffectsSelf(getEffector());
		}
		return false;
	}
}