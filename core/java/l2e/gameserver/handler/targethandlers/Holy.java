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
package l2e.gameserver.handler.targethandlers;

import l2e.gameserver.handler.ITargetTypeHandler;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.targets.L2TargetType;

/**
 * @author UnAfraid
 */
public class Holy implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		if (!activeChar.isPlayer())
		{
			return EMPTY_TARGET_LIST;
		}
		
		final L2PcInstance player = activeChar.getActingPlayer();
		final Castle castle = CastleManager.getInstance().getCastle(player);
		if ((player.getClan() == null) || (castle == null) || !player.checkIfOkToCastSealOfRule(castle, true, skill, target))
		{
			return EMPTY_TARGET_LIST;
		}
		
		return new L2Object[]
		{
			activeChar.getTarget()
		};
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.HOLY;
	}
}