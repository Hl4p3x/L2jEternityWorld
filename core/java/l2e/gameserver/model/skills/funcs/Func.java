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

import l2e.gameserver.model.conditions.Condition;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public abstract class Func
{
	public final Stats stat;
	public final int order;
	public final double value;
	public final Object funcOwner;
	public Condition cond;
	
	public Func(Stats stat, int order, Object funcOwner)
	{
		this(stat, order, funcOwner, 0.);
	}
	
	public Func(Stats stat, int order, Object owner, double value)
	{
		this.stat = stat;
		this.order = order;
		funcOwner = owner;
		this.value = value;
	}
	
	public void setCondition(Condition cond)
	{
		this.cond = cond;
	}
	
	public abstract void calc(Env env);
}