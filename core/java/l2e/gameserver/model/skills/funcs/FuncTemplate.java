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
package l2e.gameserver.model.skills.funcs;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.model.conditions.Condition;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public final class FuncTemplate
{
	protected static final Logger _log = Logger.getLogger(FuncTemplate.class.getName());
	
	public Condition attachCond;
	public Condition applayCond;
	public final Class<?> func;
	public final Constructor<?> constructor;
	public final Stats stat;
	public final int order;
	public final Lambda lambda;
	
	public FuncTemplate(Condition pAttachCond, Condition pApplayCond, String pFunc, Stats pStat, int pOrder, Lambda pLambda)
	{
		attachCond = pAttachCond;
		applayCond = pApplayCond;
		stat = pStat;
		order = pOrder;
		lambda = pLambda;
		try
		{
			func = Class.forName("l2e.gameserver.model.skills.funcs.Func" + pFunc);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			constructor = func.getConstructor(new Class<?>[]
			{
				Stats.class,
				Integer.TYPE,
				Object.class,
				Lambda.class
			});
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public Func getFunc(Env env, Object owner)
	{
		if ((attachCond != null) && !attachCond.test(env))
		{
			return null;
		}
		try
		{
			Func f = (Func) constructor.newInstance(stat, order, owner, lambda);
			if (applayCond != null)
			{
				f.setCondition(applayCond);
			}
			return f;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
			return null;
		}
	}
}