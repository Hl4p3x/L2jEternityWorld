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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.handler.EffectHandler;
import l2e.gameserver.model.ChanceCondition;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.conditions.Condition;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.skills.funcs.Lambda;
import l2e.gameserver.model.stats.Env;

public class EffectTemplate
{
	private static final Logger _log = Logger.getLogger(EffectTemplate.class.getName());
	
	private final Class<?> _handler;
	private final Constructor<?> _constructor;
	private final Condition _attachCond;
	private final Lambda _lambda;
	private final int _totalTickCount;
	
	private final int _abnormalTime;
	private final AbnormalEffect _abnormalEffect;
	private final AbnormalEffect[] _specialEffect;
	private final AbnormalEffect _eventEffect;
	public FuncTemplate[] funcTemplates;
	private final boolean _showIcon;
	private final String _name;
	private final double _effectPower;
	private final int _triggeredId;
	private final int _triggeredLevel;
	private final ChanceCondition _chanceCondition;
	private final StatsSet _parameters;
	
	public final String abnormalType;
	public final byte abnormalLvl;
	
	public EffectTemplate(Condition attachCond, Condition applyCond, Lambda lambda, String pAbnormalType, byte pAbnormalLvl, StatsSet set, StatsSet params)
	{
		_attachCond = attachCond;
		_lambda = lambda;
		_name = set.getString("name");
		_totalTickCount = set.getInteger("ticks", 0);
		_abnormalTime = set.getInteger("abnormalTime", 0);
		abnormalType = pAbnormalType;
		abnormalLvl = pAbnormalLvl;
		_abnormalEffect = AbnormalEffect.getByName(set.getString("abnormalVisualEffect", ""));
		final String[] specialEffects = set.getString("special", "").split(",");
		_specialEffect = new AbnormalEffect[specialEffects.length];
		for (int i = 0; i < specialEffects.length; i++)
		{
			_specialEffect[i] = AbnormalEffect.getByName(specialEffects[i]);
		}
		_eventEffect = AbnormalEffect.getByName(set.getString("event", ""));
		_showIcon = set.getInteger("noicon", 0) == 0;
		_effectPower = set.getDouble("effectPower", -1);
		_triggeredId = set.getInteger("triggeredId", 0);
		_triggeredLevel = set.getInteger("triggeredLevel", 1);
		_chanceCondition = ChanceCondition.parse(set.getString("chanceType", null), set.getInteger("activationChance", -1), set.getInteger("activationMinDamage", -1), set.getString("activationElements", null), set.getString("activationSkills", null), set.getBool("pvpChanceOnly", false));
		_parameters = params;
		_handler = EffectHandler.getInstance().getHandler(_name);
		if (_handler == null)
		{
			throw new RuntimeException(getClass().getSimpleName() + ": Requested unexistent effect handler: " + _name);
		}
		
		try
		{
			_constructor = _handler.getConstructor(Env.class, EffectTemplate.class);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public L2Effect getEffect(Env env)
	{
		return getEffect(env, false);
	}
	
	public L2Effect getEffect(Env env, boolean ignoreTest)
	{
		if (!ignoreTest && ((_attachCond != null) && !_attachCond.test(env)))
		{
			return null;
		}
		
		try
		{
			return (L2Effect) _constructor.newInstance(env, this);
		}
		catch (IllegalAccessException | InstantiationException e)
		{
			_log.log(Level.WARNING, "", e);
			return null;
		}
		catch (InvocationTargetException e)
		{
			_log.log(Level.WARNING, "Error creating new instance of Class " + _handler + " Exception was: " + e.getTargetException().getMessage(), e.getTargetException());
			return null;
		}
	}
	
	public L2Effect getStolenEffect(Env env, L2Effect stolen)
	{
		Constructor<?> stolenCons;
		try
		{
			stolenCons = _handler.getConstructor(Env.class, L2Effect.class);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
		
		try
		{
			final L2Effect effect = (L2Effect) stolenCons.newInstance(env, stolen);
			return effect;
		}
		catch (IllegalAccessException | InstantiationException e)
		{
			_log.log(Level.WARNING, "", e);
			return null;
		}
		catch (InvocationTargetException e)
		{
			_log.log(Level.WARNING, "Error creating new instance of Class " + _handler + " Exception was: " + e.getTargetException().getMessage(), e.getTargetException());
			return null;
		}
	}
	
	public void attach(FuncTemplate f)
	{
		if (funcTemplates == null)
		{
			funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			int len = funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			funcTemplates = tmp;
		}
	}
	
	public Lambda getLambda()
	{
		return _lambda;
	}
	
	public int getTotalTickCount()
	{
		return _totalTickCount;
	}
	
	public int getAbnormalTime()
	{
		return _abnormalTime;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public AbnormalEffect getAbnormalEffect()
	{
		return _abnormalEffect;
	}
	
	public AbnormalEffect[] getSpecialEffect()
	{
		return _specialEffect;
	}
	
	public AbnormalEffect getEventEffect()
	{
		return _eventEffect;
	}
	
	public FuncTemplate[] getFuncTemplates()
	{
		return funcTemplates;
	}
	
	public boolean isIconDisplay()
	{
		return _showIcon;
	}
	
	public double getEffectPower()
	{
		return _effectPower;
	}
	
	public int getTriggeredId()
	{
		return _triggeredId;
	}
	
	public int getTriggeredLevel()
	{
		return _triggeredLevel;
	}
	
	public ChanceCondition getChanceCondition()
	{
		return _chanceCondition;
	}
	
	public StatsSet getParameters()
	{
		return _parameters;
	}
	
	public boolean hasParameters()
	{
		return _parameters != null;
	}
	
	@Override
	public String toString()
	{
		return "Effect template[" + _handler + "]";
	}
}