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
import java.util.concurrent.ScheduledFuture;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.l2skills.L2SkillSignet;
import l2e.gameserver.model.skills.l2skills.L2SkillSignetCasttime;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class Signet extends L2Effect
{
	private L2Skill _skill;
	private boolean _srcInArena;
	protected ScheduledFuture<?> timerTask;
	
	public Signet(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_EFFECT;
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
		if (getSkill() instanceof L2SkillSignet)
		{
			_skill = SkillHolder.getInstance().getInfo(getSkill().getEffectId(), getSkill().getLevel());
		}
		else if (getSkill() instanceof L2SkillSignetCasttime)
		{
			_skill = SkillHolder.getInstance().getInfo(getSkill().getEffectId(), getSkill().getLevel());
		}
		_srcInArena = (getEffector().isInsideZone(ZoneId.PVP) && !getEffector().isInsideZone(ZoneId.SIEGE));
		
		if (getSkill().getId() == 455)
		{
			timerTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TimerTask(), 2000, 2000);
		}
		
		if (getSkill().getId() != 1422)
		{
			timerTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TimerTask(), 3000, 9000);
		}
		return true;
	}
	
	protected class TimerTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_skill == null)
			{
				return;
			}
			
			int mpConsume = _skill.getMpConsume();
			if (mpConsume > getEffector().getCurrentMp())
			{
				getEffector().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
				return;
			}
			getEffector().reduceCurrentMp(mpConsume);
			
			List<L2Character> targets = new ArrayList<>();
			for (L2Character cha : getEffected().getKnownList().getKnownCharactersInRadius(getSkill().getAffectRange()))
			{
				if (cha == null)
				{
					continue;
				}
				
				if (_skill.isOffensive() && !L2Skill.checkForAreaOffensiveSkills(getEffector(), cha, _skill, _srcInArena))
				{
					continue;
				}
				
				getEffected().broadcastPacket(new MagicSkillUse(getEffected(), cha, _skill.getId(), _skill.getLevel(), 0, 0));
				targets.add(cha);
			}
			
			if (!targets.isEmpty())
			{
				getEffector().callSkill(_skill, targets.toArray(new L2Character[targets.size()]));
			}
			return;
		}
	}
}