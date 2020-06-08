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
package l2e.gameserver.model.effects;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.ChanceCondition;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.interfaces.IChanceSkillTrigger;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.skills.funcs.Lambda;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AbnormalStatusUpdate;
import l2e.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import l2e.gameserver.network.serverpackets.MagicSkillLaunched;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.PartySpelled;
import l2e.gameserver.network.serverpackets.SystemMessage;

public abstract class L2Effect implements IChanceSkillTrigger
{
	protected static final Logger _log = Logger.getLogger(L2Effect.class.getName());
	
	private static final Func[] _emptyFunctionSet = new Func[0];
	
	private final L2Character _effector;
	private final L2Character _effected;
	private final L2Skill _skill;
	private final Lambda _lambda;
	private EffectState _state;
	protected int _periodStartTicks;
	protected int _periodFirstTime;
	private final EffectTemplate _template;
	private final FuncTemplate[] _funcTemplates;
	private int _tickCount;
	private final int _abnormalTime;
	private boolean _isSelfEffect = false;
	private boolean _isPassiveEffect = false;
	private boolean _preventExitUpdate;
	private volatile ScheduledFuture<?> _currentFuture;
	private boolean _inUse = false;
	private boolean _startConditionsCorrect = true;
	private boolean _isRemoved = false;
	
	private final String _abnormalType;
	private final byte _abnormalLvl;
	
	protected final class EffectTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				_periodFirstTime = 0;
				_periodStartTicks = GameTimeController.getInstance().getGameTicks();
				scheduleEffect();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	protected L2Effect(Env env, EffectTemplate template)
	{
		_state = EffectState.CREATED;
		_skill = env.getSkill();
		_template = template;
		_effected = env.getTarget();
		_effector = env.getCharacter();
		_lambda = template.getLambda();
		_funcTemplates = template.funcTemplates;
		_tickCount = 0;
		_abnormalTime = Formulas.calcEffectAbnormalTime(env, template);
		_periodStartTicks = GameTimeController.getInstance().getGameTicks();
		_periodFirstTime = 0;
		_abnormalType = template.abnormalType;
		_abnormalLvl = template.abnormalLvl;
	}
	
	protected L2Effect(Env env, L2Effect effect)
	{
		_template = effect._template;
		_state = EffectState.CREATED;
		_skill = env.getSkill();
		_effected = env.getTarget();
		_effector = env.getCharacter();
		_lambda = _template.getLambda();
		_funcTemplates = _template.funcTemplates;
		_tickCount = effect.getTickCount();
		_abnormalTime = effect.getAbnormalTime();
		_periodStartTicks = effect.getPeriodStartTicks();
		_periodFirstTime = effect.getTime();
		_abnormalType = _template.abnormalType;
		_abnormalLvl = _template.abnormalLvl;
	}
	
	public int getTickCount()
	{
		return _tickCount;
	}
	
	public void setCount(int newTickCount)
	{
		_tickCount = Math.min(newTickCount, _template.getTotalTickCount());
	}
	
	public void setFirstTime(int newFirstTime)
	{
		_periodFirstTime = Math.min(newFirstTime, _abnormalTime);
		_periodStartTicks -= _periodFirstTime * GameTimeController.TICKS_PER_SECOND;
	}
	
	public boolean isIconDisplay()
	{
		return _template.isIconDisplay();
	}
	
	public int getAbnormalTime()
	{
		return _abnormalTime;
	}
	
	public int getTime()
	{
		return (GameTimeController.getInstance().getGameTicks() - _periodStartTicks) / GameTimeController.TICKS_PER_SECOND;
	}
	
	public int getTimeLeft()
	{
		if (_template.getTotalTickCount() > 1)
		{
			if (getSkill().hasEffectType(L2EffectType.HEAL_OVER_TIME) || getSkill().hasEffectType(L2EffectType.CPHEAL_OVER_TIME) || getSkill().hasEffectType(L2EffectType.MANA_HEAL_OVER_TIME))
			{
				return (_template.getTotalTickCount()) - getTime();
			}
			return (((_template.getTotalTickCount() - _tickCount) + 1) * (_abnormalTime / _template.getTotalTickCount())) - getTime();
		}
		return _abnormalTime - getTime();
	}
	
	public boolean isInUse()
	{
		return _inUse;
	}
	
	public boolean setInUse(boolean inUse)
	{
		_inUse = inUse;
		if (_inUse)
		{
			_startConditionsCorrect = onStart();
		}
		else
		{
			onExit();
		}
		return _startConditionsCorrect;
	}
	
	public String getAbnormalType(String ngt)
	{
		return _abnormalType;
	}
	
	public String getAbnormalType()
	{
		return _abnormalType;
	}
	
	public byte getAbnormalLvl()
	{
		return _abnormalLvl;
	}
	
	public final L2Skill getSkill()
	{
		return _skill;
	}
	
	public final L2Character getEffector()
	{
		return _effector;
	}
	
	public final L2Character getEffected()
	{
		return _effected;
	}
	
	public boolean isSelfEffect()
	{
		return _isSelfEffect;
	}
	
	public void setSelfEffect()
	{
		_isSelfEffect = true;
	}
	
	public boolean isPassiveEffect()
	{
		return _isPassiveEffect;
	}
	
	public void setPassiveEffect()
	{
		_isPassiveEffect = true;
	}
	
	public final double calc()
	{
		final Env env = new Env();
		env.setCharacter(_effector);
		env.setTarget(_effected);
		env.setSkill(_skill);
		return _lambda.calc(env);
	}
	
	public boolean calcSuccess()
	{
		final Env env = new Env();
		env.setSkillMastery(Formulas.calcSkillMastery(getEffector(), getSkill()));
		env.setCharacter(getEffector());
		env.setTarget(getEffected());
		env.setSkill(getSkill());
		env.setEffect(this);
		return Formulas.calcEffectSuccess(env);
	}
	
	private final synchronized void startEffectTask()
	{
		stopEffectTask();
		
		final int delay = Math.max((_abnormalTime - _periodFirstTime) * 1000, 5);
		if (_template.getTotalTickCount() > 0)
		{
			if (getSkill().isToggle())
			{
				final int period = ((_abnormalTime > 1) ? Math.max(_abnormalTime / _template.getTotalTickCount(), 1) : _template.getTotalTickCount()) * 1000;
				_currentFuture = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new EffectTask(), delay / period, period);
			}
			else
			{
				_currentFuture = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new EffectTask(), delay, _abnormalTime * 1000);
			}
		}
		else
		{
			_currentFuture = ThreadPoolManager.getInstance().scheduleEffect(new EffectTask(), delay);
		}
		
		if (_state == EffectState.ACTING)
		{
			if (isSelfEffectType())
			{
				_effector.addEffect(this);
			}
			else
			{
				_effected.addEffect(this);
			}
		}
	}
	
	public final void exit()
	{
		exit(false);
	}
	
	public final void exit(boolean preventExitUpdate)
	{
		_preventExitUpdate = preventExitUpdate;
		_state = EffectState.FINISHING;
		scheduleEffect();
	}
	
	public final synchronized void stopEffectTask()
	{
		if (_currentFuture != null)
		{
			_currentFuture.cancel(false);
			_currentFuture = null;
			
			if (isSelfEffectType() && (getEffector() != null))
			{
				getEffector().removeEffect(this);
			}
			else if (getEffected() != null)
			{
				getEffected().removeEffect(this);
			}
		}
	}
	
	public abstract L2EffectType getEffectType();
	
	public boolean onStart()
	{
		if (_template.getAbnormalEffect() != AbnormalEffect.NULL)
		{
			getEffected().startAbnormalEffect(_template.getAbnormalEffect());
		}
		if (_template.getSpecialEffect() != null)
		{
			getEffected().startSpecialEffect(_template.getSpecialEffect());
		}
		if ((_template.getEventEffect() != AbnormalEffect.NULL) && getEffected().isPlayer())
		{
			getEffected().getActingPlayer().startEventEffect(_template.getEventEffect());
		}
		return true;
	}
	
	public void onExit()
	{
		if (_template.getAbnormalEffect() != AbnormalEffect.NULL)
		{
			getEffected().stopAbnormalEffect(_template.getAbnormalEffect());
		}
		if (_template.getSpecialEffect() != null)
		{
			getEffected().stopSpecialEffect(_template.getSpecialEffect());
		}
		if ((_template.getEventEffect() != AbnormalEffect.NULL) && getEffected().isPlayer())
		{
			getEffected().getActingPlayer().stopEventEffect(_template.getEventEffect());
		}
	}
	
	public boolean onActionTime()
	{
		return getAbnormalTime() < 0;
	}
	
	public final void scheduleEffect()
	{
		switch (_state)
		{
			case CREATED:
			{
				_state = EffectState.ACTING;
				
				if (_skill.isOffensive() && isIconDisplay() && getEffected().isPlayer())
				{
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					sm.addSkillName(_skill);
					getEffected().sendPacket(sm);
				}
				
				if (_abnormalTime != 0)
				{
					startEffectTask();
					return;
				}
				_startConditionsCorrect = onStart();
			}
			case ACTING:
			{
				if (isInUse())
				{
					_tickCount++;
					if (onActionTime() && _startConditionsCorrect)
					{
						return;
					}
				}
				
				if (_tickCount <= _template.getTotalTickCount())
				{
					return;
				}
				
				_state = EffectState.FINISHING;
			}
			case FINISHING:
			{
				if (getEffected().isPlayer() && isIconDisplay())
				{
					SystemMessageId smId = null;
					if (getSkill().isToggle())
					{
						smId = SystemMessageId.S1_HAS_BEEN_ABORTED;
					}
					else if (isRemoved())
					{
						smId = SystemMessageId.EFFECT_S1_DISAPPEARED;
					}
					else if (_tickCount >= _template.getTotalTickCount())
					{
						smId = SystemMessageId.S1_HAS_WORN_OFF;
					}
					
					if (smId != null)
					{
						final SystemMessage sm = SystemMessage.getSystemMessage(smId);
						sm.addSkillName(getSkill());
						getEffected().sendPacket(sm);
					}
				}
				
				if ((_currentFuture == null) && (getEffected() != null))
				{
					getEffected().removeEffect(this);
				}
				
				stopEffectTask();
				
				if (isInUse() || !((_tickCount > 1) || (_abnormalTime > 0)))
				{
					if (_startConditionsCorrect)
					{
						onExit();
					}
				}
				
				if (_skill.getAfterEffectId() > 0)
				{
					final L2Skill skill = SkillHolder.getInstance().getInfo(_skill.getAfterEffectId(), _skill.getAfterEffectLvl());
					if (skill != null)
					{
						getEffected().broadcastPacket(new MagicSkillUse(_effected, skill.getId(), skill.getLevel(), 0, 0));
						getEffected().broadcastPacket(new MagicSkillLaunched(_effected, skill.getId(), skill.getLevel()));
						skill.getEffects(getEffected(), getEffected());
					}
				}
			}
		}
	}
	
	public Func[] getStatFuncs()
	{
		if (_funcTemplates == null)
		{
			return _emptyFunctionSet;
		}
		
		final ArrayList<Func> funcs = new ArrayList<>(_funcTemplates.length);
		
		Env env = new Env();
		env.setCharacter(_effector);
		env.setTarget(_effected);
		env.setSkill(_skill);
		
		Func f;
		for (FuncTemplate t : _funcTemplates)
		{
			f = t.getFunc(env, this);
			if (f != null)
			{
				funcs.add(f);
			}
		}
		
		if (funcs.isEmpty())
		{
			return _emptyFunctionSet;
		}
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public final void addIcon(AbnormalStatusUpdate mi)
	{
		if (_state != EffectState.ACTING)
		{
			return;
		}
		
		if (_abnormalTime == -1)
		{
			mi.addEffect(getSkill(), -1);
		}
		else
		{
			mi.addEffect(getSkill(), getTimeLeft());
		}
	}
	
	public final void addPartySpelledIcon(PartySpelled ps)
	{
		if (_state != EffectState.ACTING)
		{
			return;
		}
		
		final ScheduledFuture<?> future = _currentFuture;
		if (future != null)
		{
			ps.addPartySpelledEffect(getSkill(), (int) future.getDelay(TimeUnit.SECONDS));
		}
		else if (_abnormalTime == -1)
		{
			ps.addPartySpelledEffect(getSkill(), -1);
		}
	}
	
	public final void addOlympiadSpelledIcon(ExOlympiadSpelledInfo os)
	{
		if (_state != EffectState.ACTING)
		{
			return;
		}
		
		final ScheduledFuture<?> future = _currentFuture;
		final L2Skill sk = getSkill();
		if (future != null)
		{
			os.addEffect(sk.getDisplayId(), sk.getDisplayLevel(), (int) future.getDelay(TimeUnit.MILLISECONDS));
		}
		else if (_abnormalTime == -1)
		{
			os.addEffect(sk.getDisplayId(), sk.getDisplayLevel(), _abnormalTime);
		}
	}
	
	public int getPeriodStartTicks()
	{
		return _periodStartTicks;
	}
	
	public EffectTemplate getEffectTemplate()
	{
		return _template;
	}
	
	public double getEffectPower()
	{
		return _template.getEffectPower();
	}
	
	public boolean canBeStolen()
	{
		return !getSkill().isPassive() && !getSkill().isToggle() && !getSkill().isDebuff() && !getSkill().isHeroSkill() && !getSkill().isGMSkill() && !(getSkill().isStatic() && (getSkill().getId() != 2341)) && getSkill().canBeDispeled();
	}
	
	public int getEffectFlags()
	{
		return EffectFlag.NONE.getMask();
	}
	
	@Override
	public String toString()
	{
		return "Effect " + getClass().getSimpleName() + ", " + _skill + ", State: " + _state + ", Time: " + _abnormalTime + ", Remaining: " + getTimeLeft();
	}
	
	public boolean isSelfEffectType()
	{
		return false;
	}
	
	public void decreaseForce()
	{
	}
	
	public void increaseEffect()
	{
	}
	
	public boolean checkCondition(Object obj)
	{
		return true;
	}
	
	@Override
	public boolean triggersChanceSkill()
	{
		return false;
	}
	
	@Override
	public int getTriggeredChanceId()
	{
		return 0;
	}
	
	@Override
	public int getTriggeredChanceLevel()
	{
		return 0;
	}
	
	@Override
	public ChanceCondition getTriggeredChanceCondition()
	{
		return null;
	}
	
	public boolean isPreventExitUpdate()
	{
		return _preventExitUpdate;
	}
	
	public void setPreventExitUpdate(boolean val)
	{
		_preventExitUpdate = val;
	}
	
	public boolean isInstant()
	{
		return false;
	}
	
	public boolean isRemoved()
	{
		return _isRemoved;
	}
	
	public void setRemoved(boolean val)
	{
		_isRemoved = val;
	}
}