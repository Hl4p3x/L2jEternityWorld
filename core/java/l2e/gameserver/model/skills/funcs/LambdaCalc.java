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

import l2e.gameserver.model.stats.Env;

/**
 * @author mkizub
 */
public final class LambdaCalc extends Lambda
{
	public Func[] funcs;
	
	public LambdaCalc()
	{
		funcs = new Func[0];
	}
	
	@Override
	public double calc(Env env)
	{
		double saveValue = env.getValue();
		try
		{
			env.setValue(0);
			for (Func f : funcs)
			{
				f.calc(env);
			}
			return env.getValue();
		}
		finally
		{
			env.setValue(saveValue);
		}
	}
	
	public void addFunc(Func f)
	{
		int len = funcs.length;
		Func[] dest = new Func[len + 1];
		System.arraycopy(funcs, 0, dest, 0, len);
		dest[len] = f;
		funcs = dest;
	}
}
