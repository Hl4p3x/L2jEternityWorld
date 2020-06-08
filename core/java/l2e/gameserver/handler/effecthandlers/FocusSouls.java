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

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;

public class FocusSouls extends L2Effect
{
	public FocusSouls(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NONE;
	}
	
	@Override
	public boolean onStart()
	{
		if (!getEffected().isPlayer() || getEffected().isAlikeDead())
		{
			return false;
		}
		
		final L2PcInstance target = getEffected().getActingPlayer();
		final int maxSouls = (int) target.calcStat(Stats.MAX_SOULS, 0, null, null);
		if (maxSouls > 0)
		{
			int amount = (int) calc();
			if ((target.getChargedSouls() < maxSouls))
			{
				int count = ((target.getChargedSouls() + amount) <= maxSouls) ? amount : (maxSouls - target.getChargedSouls());
				target.increaseSouls(count);
			}
			else
			{
				target.sendPacket(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
				return false;
			}
		}
		return true;
	}
}