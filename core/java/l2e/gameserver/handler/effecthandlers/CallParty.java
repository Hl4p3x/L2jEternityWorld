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

import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.stats.Env;

public class CallParty extends L2Effect
{
	public CallParty(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CALLPC;
	}

	@Override
	public boolean calcSuccess()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		L2Party party = getEffector().getParty();
		if (party != null)
		{
			for (L2PcInstance partyMember : party.getMembers())
			{
				if (CallPc.checkSummonTargetStatus(partyMember, getEffector().getActingPlayer()))
				{
					if (getEffector() != partyMember)
					{
						partyMember.teleToLocation(getEffector().getX(), getEffector().getY(), getEffector().getZ(), true);
					}
				}
			}
			return true;
		}
		return false;
	}
}