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

import java.util.ArrayList;
import java.util.List;

import l2e.gameserver.handler.ITargetTypeHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;

public class CorpsePlayer implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<>();
		if ((target != null) && target.isDead())
		{
			final L2PcInstance player;
			if (activeChar.isPlayer())
			{
				player = activeChar.getActingPlayer();
			}
			else
			{
				player = null;
			}
			
			final L2PcInstance targetPlayer;
			if (target.isPlayer())
			{
				targetPlayer = target.getActingPlayer();
			}
			else
			{
				targetPlayer = null;
			}
			
			final L2PetInstance targetPet;
			if (target.isPet())
			{
				targetPet = (L2PetInstance) target;
			}
			else
			{
				targetPet = null;
			}
			
			if ((player != null) && ((targetPlayer != null) || (targetPet != null)))
			{
				boolean condGood = true;
				
				if (skill.getSkillType() == L2SkillType.RESURRECT)
				{
					if (targetPlayer != null)
					{
						if (targetPlayer.isInsideZone(ZoneId.SIEGE) && !targetPlayer.isInSiege())
						{
							condGood = false;
							activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
						}
						
						if (targetPlayer.isFestivalParticipant())
						{
							condGood = false;
							activeChar.sendMessage("You may not resurrect participants in a festival.");
						}
						if (targetPlayer.isReviveRequested())
						{
							if (targetPlayer.isRevivingPet())
							{
								player.sendPacket(SystemMessageId.MASTER_CANNOT_RES);
							}
							else
							{
								player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
							}
							condGood = false;
						}
					}
					else if (targetPet != null)
					{
						if (targetPet.getOwner() != player)
						{
							if (targetPet.getOwner().isReviveRequested())
							{
								if (targetPet.getOwner().isRevivingPet())
								{
									player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
								}
								else
								{
									player.sendPacket(SystemMessageId.CANNOT_RES_PET2);
								}
								condGood = false;
							}
						}
					}
				}
				
				if (condGood)
				{
					if (!onlyFirst)
					{
						targetList.add(target);
						return targetList.toArray(new L2Object[targetList.size()]);
					}
					return new L2Character[]
					{
						target
					};
				}
			}
		}
		activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
		return EMPTY_TARGET_LIST;
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.CORPSE_PLAYER;
	}
}