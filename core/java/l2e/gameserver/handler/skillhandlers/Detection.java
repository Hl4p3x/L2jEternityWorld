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
package l2e.gameserver.handler.skillhandlers;

import l2e.gameserver.handler.ISkillHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;

public class Detection implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.DETECTION
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		final boolean hasParty;
		final boolean hasClan;
		final boolean hasAlly;
		final L2PcInstance player = activeChar.getActingPlayer();
		if (player != null)
		{
			hasParty = player.isInParty();
			hasClan = player.getClanId() > 0;
			hasAlly = player.getAllyId() > 0;
			
			for (L2PcInstance target : activeChar.getKnownList().getKnownPlayersInRadius(skill.getAffectRange()))
			{
				if ((target != null) && target.isInvisible())
				{
					if (hasParty && (target.getParty() != null) && (player.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId()))
					{
						continue;
					}
					if (hasClan && (player.getClanId() == target.getClanId()))
					{
						continue;
					}
					if (hasAlly && (player.getAllyId() == target.getAllyId()))
					{
						continue;
					}
					
					L2Effect eHide = target.getFirstEffect(L2EffectType.HIDE);
					if (eHide != null)
					{
						eHide.exit();
					}
				}
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}