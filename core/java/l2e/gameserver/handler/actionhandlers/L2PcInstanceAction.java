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

import l2e.Config;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.handler.IActionHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Object.InstanceType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;

public class L2PcInstanceAction implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if (!TvTEvent.onAction(activeChar, target.getObjectId()))
		{
			return false;
		}
		
		if (!TvTRoundEvent.onAction(activeChar, target.getObjectId()))
		{
			return false;
		}
		
		if (activeChar.isOutOfControl())
		{
			return false;
		}
		
		if (activeChar.isLockedTarget() && (activeChar.getLockedTarget() != target))
		{
			activeChar.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
			return false;
		}
		
		if (activeChar.getTarget() != target)
		{
			activeChar.setTarget(target);
		}
		else if (interact)
		{
			if (((L2PcInstance) target).getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
			{
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
			}
			else
			{
				if (target.isAutoAttackable(activeChar))
				{
					if ((((L2PcInstance) target).isCursedWeaponEquipped() && (activeChar.getLevel() < 21)) || (activeChar.isCursedWeaponEquipped() && (((L2Character) target).getLevel() < 21)))
					{
						activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						if (Config.GEODATA)
						{
							if (GeoClient.getInstance().canSeeTarget(activeChar, target))
							{
								activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								activeChar.onActionRequest();
							}
						}
						else
						{
							activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
							activeChar.onActionRequest();
						}
					}
				}
				else
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					if (Config.GEODATA)
					{
						if (GeoClient.getInstance().canSeeTarget(activeChar, target))
						{
							activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
						}
					}
					else
					{
						activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.L2PcInstance;
	}
}