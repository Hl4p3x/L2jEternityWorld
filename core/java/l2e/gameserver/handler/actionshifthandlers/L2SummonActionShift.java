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
package l2e.gameserver.handler.actionshifthandlers;

import l2e.gameserver.handler.AdminCommandHandler;
import l2e.gameserver.handler.IActionHandler;
import l2e.gameserver.handler.IAdminCommandHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Object.InstanceType;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class L2SummonActionShift implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if (activeChar.isGM())
		{
			if (activeChar.getTarget() != target)
			{
				activeChar.setTarget(target);
			}

			IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler("admin_summon_info");
			if (ach != null)
				ach.useAdminCommand("admin_summon_info", activeChar);
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.L2Summon;
	}
}