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
package l2e.gameserver.model.restriction;

import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemSkills;
import l2e.gameserver.handler.itemhandlers.SummonItems;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public final class OlympiadRestriction extends AbstractRestriction
{
	@Override
	public boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		if (activeChar.isInOlympiadMode() || (activeChar.getOlympiadGameId() != -1))
		{
			activeChar.sendMessage("You are registered on Grand Olympiad Games!");
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		if (activeChar.isInOlympiadMode() || target.isInOlympiadMode())
		{
			return false;
		}
		return true;
	}
	
	@Override
	public boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar, L2ItemInstance item, L2PcInstance player)
	{
		if ((player == null) || !player.isInOlympiadMode())
		{
			return true;
		}
		if (clazz == SummonItems.class)
		{
			player.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}
		else if (clazz == ItemSkills.class)
		{
			if (!((itemId >= 8193) && (itemId <= 8201)) && (itemId != 9702) && (itemId != 10410) && (itemId != 10412))
			{
				player.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean canTarget(L2Character activeChar, L2Character target, boolean sendMessage, L2PcInstance attacker_, L2PcInstance target_)
	{
		if ((attacker_ == null) || (target_ == null) || (attacker_ == target_) || attacker_.isGM())
		{
			return true;
		}
		if (attacker_.isInOlympiadMode() != target_.isInOlympiadMode())
		{
			return false;
		}
		if (attacker_.isInOlympiadMode() && target_.isInOlympiadMode())
		{
			if (attacker_.getOlympiadGameId() != target_.getOlympiadGameId())
			{
				return false;
			}
		}
		return true;
	}
}