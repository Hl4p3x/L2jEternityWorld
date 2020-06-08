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
package l2e.gameserver.model.skills.funcs.formulas;

import l2e.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncGatesPDefMod extends Func
{
	private static final FuncGatesPDefMod _fmm_instance = new FuncGatesPDefMod();
	
	public static Func getInstance()
	{
		return _fmm_instance;
	}
	
	private FuncGatesPDefMod()
	{
		super(Stats.POWER_DEFENCE, 0x20, null);
	}
	
	@Override
	public void calc(Env env)
	{
		if (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DAWN)
		{
			env.mulValue(Config.ALT_SIEGE_DAWN_GATES_PDEF_MULT);
		}
		else if (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DUSK)
		{
			env.mulValue(Config.ALT_SIEGE_DUSK_GATES_PDEF_MULT);
		}
	}
}