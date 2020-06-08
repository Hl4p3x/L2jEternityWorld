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
package l2e.gameserver.handler.actionhandlers;

import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.handler.IActionHandler;
import l2e.gameserver.instancemanager.MercTicketManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Object.InstanceType;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;

public class L2ItemInstanceAction implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		final int castleId = MercTicketManager.getInstance().getTicketCastleId(((L2ItemInstance)target).getId());
		
		if (castleId > 0 &&  (!activeChar.isCastleLord(castleId) || activeChar.isInParty()))
		{
			if (activeChar.isInParty())
				activeChar.sendMessage("You cannot pickup mercenaries while in a party.");
			else
				activeChar.sendMessage("Only the castle lord can pickup mercenaries.");
			
			activeChar.setTarget(target);
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		else if (!activeChar.isFlying())
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, target);
		
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.L2ItemInstance;
	}
}