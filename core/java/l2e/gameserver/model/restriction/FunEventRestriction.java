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

import l2e.Config;
import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemSkills;
import l2e.gameserver.handler.itemhandlers.SummonItems;
import l2e.gameserver.instancemanager.FunEventsManager;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.serverpackets.ActionFailed;

final class FunEventRestriction extends AbstractRestriction
{
	@Override
	public final boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		if (activeChar.isFightingInEvent())
		{
			activeChar.sendMessage("You are participating in a fun event!");
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canRequestRevive(L2PcInstance activeChar)
	{
		if (activeChar.isFightingInEvent())
		{
			return false;
		}
		return true;
	}
	
	@Override
	public final boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		if ((activeChar.isFightingInEvent() || target.isFightingInEvent()) && !activeChar.isInSameEvent(target) && !activeChar.isGM())
		{
			if (((activeChar.getEventName().equals("CTF") || target.getEventName().equals("CTF")) && !Config.CTF_ALLOW_INTERFERENCE) || ((activeChar.getEventName().equals("BW") || target.getEventName().equals("BW")) && !Config.BW_ALLOW_INTERFERENCE) || ((activeChar.getEventName().equals("DM") || target.getEventName().equals("DM")) && !Config.DM_ALLOW_INTERFERENCE))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public final boolean canTarget(L2Character activeChar, L2Character target, boolean sendMessage, L2PcInstance attacker_, L2PcInstance target_)
	{
		if ((attacker_ == null) || (target_ == null) || (attacker_ == target_))
		{
			return true;
		}
		if ((attacker_.isFightingInEvent() || target_.isFightingInEvent()) && !attacker_.isInSameEvent(target_) && !attacker_.isGM())
		{
			if (((attacker_.getEventName().equals("CTF") || target_.getEventName().equals("CTF")) && !Config.CTF_ALLOW_INTERFERENCE) || ((attacker_.getEventName().equals("BW") || target_.getEventName().equals("BW")) && !Config.BW_ALLOW_INTERFERENCE) || ((attacker_.getEventName().equals("DM") || target_.getEventName().equals("DM")) && !Config.DM_ALLOW_INTERFERENCE))
			{
				if (sendMessage)
				{
					attacker_.sendMessage("You can't interact because of the fun event!");
				}
				return false;
			}
		}
		return true;
	}
	
	@Override
	public final boolean canTeleport(L2PcInstance activeChar)
	{
		if (activeChar.isFightingInEvent())
		{
			activeChar.sendMessage("You can't teleport during an event.");
			return false;
		}
		return true;
	}
	
	@Override
	public final boolean canUseItem(int itemId, L2PcInstance player, L2ItemInstance item)
	{
		if (player.isFightingInEvent())
		{
			if (player.getEventName().equals("CTF") && (player._CTFHaveFlagOfTeam > 0) && item.isEquipable() && item.isWeapon())
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public final boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar, L2ItemInstance item, L2PcInstance player)
	{
		if ((player != null) && player.isFightingInEvent())
		{
			if (clazz == SummonItems.class)
			{
				if ((player.getEventName().equals("CTF") && !Config.CTF_ALLOW_SUMMON) || (player.getEventName().equals("BW") && !Config.BW_ALLOW_SUMMON) || (player.getEventName().equals("DM") && !Config.DM_ALLOW_SUMMON))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			else if ((clazz == ItemSkills.class) && ((itemId == 5591) || (itemId == 5592)))
			{
				if ((player.getEventName().equals("BW") && !Config.BW_ALLOW_POTIONS) || (player.getEventName().equals("CTF") && !Config.CTF_ALLOW_POTIONS) || (player.getEventName().equals("DM") && !Config.DM_ALLOW_POTIONS))
				{
					player.sendMessage("�� �� ������ ������������ ����� �� ���� ������!");
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void levelChanged(L2PcInstance activeChar)
	{
		if (activeChar.getEventName() != null)
		{
			FunEventsManager.getInstance().notifyLevelChanged(activeChar);
		}
	}
	
	@Override
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		FunEventsManager.getInstance().notifyPlayerLogin(activeChar);
	}
	
	@Override
	public void playerDisconnected(L2PcInstance activeChar)
	{
		FunEventsManager.getInstance().notifyPlayerLogout(activeChar);
	}
	
	@Override
	public boolean playerKilled(L2Character activeChar, final L2PcInstance target, L2PcInstance killer)
	{
		if ((killer == null) || (!target.isFightingInEvent() && !target.isFightingInTW()))
		{
			return true;
		}
		return FunEventsManager.getInstance().notifyPlayerKilled(target, killer);
	}
}