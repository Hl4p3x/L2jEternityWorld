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
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.util.Util;

public class RebalanceHP extends L2Effect
{
	public RebalanceHP(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.REBALANCE_HP;
	}
	
	@Override
	public boolean onStart()
	{
		if (!getEffector().isPlayer() || !getEffector().isInParty())
		{
			return false;
		}
		
		double fullHP = 0;
		double currentHPs = 0;
		final L2Party party = getEffector().getParty();
		for (L2PcInstance member : party.getMembers())
		{
			if (member.isDead() || !Util.checkIfInRange(getSkill().getAffectRange(), getEffector(), member, true))
			{
				continue;
			}
			
			fullHP += member.getMaxHp();
			currentHPs += member.getCurrentHp();
		}
		
		double percentHP = currentHPs / fullHP;
		for (L2PcInstance member : party.getMembers())
		{
			if (member.isDead() || !Util.checkIfInRange(getSkill().getAffectRange(), getEffector(), member, true))
			{
				continue;
			}
			
			double newHP = member.getMaxHp() * percentHP;
			if (newHP > member.getCurrentHp())
			{
				if (member.getCurrentHp() > member.getMaxRecoverableHp())
				{
					newHP = member.getCurrentHp();
				}
				else if (newHP > member.getMaxRecoverableHp())
				{
					newHP = member.getMaxRecoverableHp();
				}
			}
			
			member.setCurrentHp(newHP);
			StatusUpdate su = new StatusUpdate(member);
			su.addAttribute(StatusUpdate.CUR_HP, (int) member.getCurrentHp());
			member.sendPacket(su);
		}
		return true;
	}
}