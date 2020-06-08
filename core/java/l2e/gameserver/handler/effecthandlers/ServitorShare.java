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

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.effects.EffectFlag;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;

public class ServitorShare extends L2Effect
{
	private static final class ScheduledEffectExitTask implements Runnable
	{
		private final L2Character _effected;
		private final int _skillId;
		
		public ScheduledEffectExitTask(L2Character effected, int skillId)
		{
			_effected = effected;
			_skillId = skillId;
		}
		
		@Override
		public void run()
		{
			_effected.stopSkillEffects(_skillId);
		}
	}

	public ServitorShare(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean canBeStolen()
	{
		return false;
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.SERVITOR_SHARE.getMask();
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	@Override
	public void onExit()
	{
		final L2Character effected = getEffected().isPlayer() ? getEffected().getSummon() : getEffected().getActingPlayer();
		if (effected != null)
		{
			ThreadPoolManager.getInstance().scheduleEffect(new ScheduledEffectExitTask(effected, getSkill().getId()), 100);
		}
		super.onExit();
	}
}