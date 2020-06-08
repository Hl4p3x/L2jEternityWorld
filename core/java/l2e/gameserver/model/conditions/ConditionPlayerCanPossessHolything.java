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
package l2e.gameserver.model.conditions;

import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.util.Util;

public class ConditionPlayerCanPossessHolything extends Condition
{
	private final boolean _val;
	
	public ConditionPlayerCanPossessHolything(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		boolean canPossessHolything = true;
		if ((env.getPlayer() == null) || env.getPlayer().isAlikeDead() || env.getPlayer().isCursedWeaponEquipped())
		{
			canPossessHolything = false;
		}
		else if ((env.getPlayer().getClan() == null) || (env.getPlayer().getClan().getLeaderId() != env.getPlayer().getObjectId()))
		{
			canPossessHolything = false;
		}
		
		Castle castle = CastleManager.getInstance().getCastle(env.getPlayer());
		SystemMessage sm;
		if ((castle == null) || (castle.getId() <= 0) || !castle.getSiege().getIsInProgress() || (castle.getSiege().getAttackerClan(env.getPlayer().getClan()) == null))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(env.getSkill());
			env.getPlayer().sendPacket(sm);
			canPossessHolything = false;
		}
		else if (!castle.getArtefacts().contains(env.getTarget()))
		{
			env.getPlayer().sendPacket(SystemMessageId.INCORRECT_TARGET);
			canPossessHolything = false;
		}
		else if (!Util.checkIfInRange(200, env.getPlayer(), env.getTarget(), true))
		{
			env.getPlayer().sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
			canPossessHolything = false;
		}
		return (_val == canPossessHolything);
	}
}