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

import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class MpByLevel extends L2Effect
{
	public MpByLevel(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffector() == null) || (getEffected() == null))
		{
			return false;
		}

		final int abs = (int) calc();
		final double absorb = ((getEffected().getCurrentMp() + abs) > getEffected().getMaxMp() ? getEffected().getMaxMp() : (getEffected().getCurrentMp() + abs));
		final int restored = (int) (absorb - getEffected().getCurrentMp());
		getEffected().setCurrentMp(absorb);

		final StatusUpdate su = new StatusUpdate(getEffected());
		su.addAttribute(StatusUpdate.CUR_MP, (int) absorb);
		getEffected().sendPacket(su);

		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
		sm.addNumber(restored);
		getEffected().sendPacket(sm);
		return true;
	}
	
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
}