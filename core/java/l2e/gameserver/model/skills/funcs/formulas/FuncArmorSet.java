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

import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.data.xml.ArmorSetsParser;
import l2e.gameserver.model.L2ArmorSet;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncArmorSet extends Func
{
	private static final Map<Stats, FuncArmorSet> _fh_instance = new HashMap<>();
	
	public static Func getInstance(Stats st)
	{
		if (!_fh_instance.containsKey(st))
		{
			_fh_instance.put(st, new FuncArmorSet(st));
		}
		return _fh_instance.get(st);
	}
	
	private FuncArmorSet(Stats stat)
	{
		super(stat, 0x10, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2PcInstance player = env.getPlayer();
		if (player != null)
		{
			L2ItemInstance chest = player.getChestArmorInstance();
			if (chest != null)
			{
				L2ArmorSet set = ArmorSetsParser.getInstance().getSet(chest.getId());
				if ((set != null) && set.containAll(player))
				{
					switch (stat)
					{
						case STAT_STR:
							env.addValue(set.getSTR());
							break;
						case STAT_DEX:
							env.addValue(set.getDEX());
							break;
						case STAT_INT:
							env.addValue(set.getINT());
							break;
						case STAT_MEN:
							env.addValue(set.getMEN());
							break;
						case STAT_CON:
							env.addValue(set.getCON());
							break;
						case STAT_WIT:
							env.addValue(set.getWIT());
							break;
					}
				}
			}
		}
	}
}