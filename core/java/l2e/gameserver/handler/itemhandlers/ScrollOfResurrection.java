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
package l2e.gameserver.handler.itemhandlers;

import java.util.logging.Level;

import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.events.TvTEvent;
import l2e.gameserver.model.entity.events.TvTRoundEvent;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.scripts.events.LastHero;

public class ScrollOfResurrection implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		if (!TvTEvent.onScrollUse(playable.getObjectId()))
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (!TvTRoundEvent.onScrollUse(playable.getObjectId()))
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (!LastHero.onScrollUse(playable.getObjectId()))
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return false;
		}
		
		if (activeChar.isMovementDisabled())
		{
			return false;
		}
		
		final int itemId = item.getId();
		final boolean petScroll = (itemId == 6387);
		final SkillsHolder[] skills = item.getItem().getSkills();
		
		if (skills == null)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": is missing skills!");
			return false;
		}
		
		final L2Character target = (L2Character) activeChar.getTarget();
		if ((target == null) || !target.isDead())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}
		
		L2PcInstance targetPlayer = null;
		if (target.isPlayer())
		{
			targetPlayer = (L2PcInstance) target;
		}
		
		L2PetInstance targetPet = null;
		if (target instanceof L2PetInstance)
		{
			targetPet = (L2PetInstance) target;
		}
		
		if ((targetPlayer != null) || (targetPet != null))
		{
			boolean condGood = true;
			
			Castle castle = null;
			
			if (targetPlayer != null)
			{
				castle = CastleManager.getInstance().getCastle(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
			}
			else if (targetPet != null)
			{
				castle = CastleManager.getInstance().getCastle(targetPet.getOwner().getX(), targetPet.getOwner().getY(), targetPet.getOwner().getZ());
			}
			
			if ((castle != null) && castle.getSiege().getIsInProgress())
			{
				condGood = false;
				activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
			}
			
			if (targetPet != null)
			{
				if (targetPet.getOwner() != activeChar)
				{
					if (targetPet.getOwner().isReviveRequested())
					{
						if (targetPet.getOwner().isRevivingPet())
						{
							activeChar.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); 
						}
						else
						{
							activeChar.sendPacket(SystemMessageId.CANNOT_RES_PET2);
						}
						condGood = false;
					}
				}
			}
			else if (targetPlayer != null)
			{
				if (targetPlayer.isFestivalParticipant())
				{
					condGood = false;
					activeChar.sendMessage("You may not resurrect participants in a festival.");
				}
				if (targetPlayer.isReviveRequested())
				{
					if (targetPlayer.isRevivingPet())
					{
						activeChar.sendPacket(SystemMessageId.MASTER_CANNOT_RES);
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
					}
					condGood = false;
				}
				else if (petScroll)
				{
					condGood = false;
					activeChar.sendMessage("You do not have the correct scroll");
				}
			}
			
			if (condGood)
			{
				if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
				{
					return false;
				}
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(item);
				activeChar.sendPacket(sm);
				
				for (SkillsHolder sk : skills)
				{
					activeChar.useMagic(sk.getSkill(), true, true);
				}
				return true;
			}
		}
		return false;
	}
}