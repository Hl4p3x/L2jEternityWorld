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

import java.util.List;

import javolution.util.FastList;

import l2e.gameserver.handler.ISkillHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.taskmanager.DecayTaskManager;
import l2e.scripts.events.LastHero;

public class Resurrect implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.RESURRECT
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2PcInstance player = null;
		if (activeChar.isPlayer())
			player = activeChar.getActingPlayer();
		
		L2PcInstance targetPlayer;
		List<L2Character> targetToRes = new FastList<>();
		
		for (L2Character target: (L2Character[]) targets)
		{
			if (target.isPlayer())
			{
				targetPlayer = target.getActingPlayer();
				
				if (skill.getTargetType() == L2TargetType.CORPSE_CLAN)
				{
					if ((player != null) && (player.getClanId() != targetPlayer.getClanId()))
					{
						continue;
					}
				}
				if (!LastHero.canBeRessurected(targetPlayer))
					continue;
			}
			if (target.isVisible())
				targetToRes.add(target);
		}
		
		if (targetToRes.isEmpty())
		{
			activeChar.abortCast();
			return;
		}
		
		for (L2Character cha : targetToRes)
		{
			if (activeChar.isPlayer())
			{
				if (cha.isPlayer())
					cha.getActingPlayer().reviveRequest(activeChar.getActingPlayer(), skill, false);
				else if (cha.isPet())
					((L2PetInstance) cha).getOwner().reviveRequest(activeChar.getActingPlayer(), skill, true);
			}
			else
			{
				DecayTaskManager.getInstance().cancelDecayTask(cha);
				cha.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
			}
		}
		activeChar.setChargedShot(activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS) ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}