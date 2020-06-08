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

import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.handler.ISkillHandler;
import l2e.gameserver.instancemanager.DuelManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2ClanHallManagerInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;

public class Continuous implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.BUFF,
		L2SkillType.DEBUFF,
		L2SkillType.DOT,
		L2SkillType.MDOT,
		L2SkillType.POISON,
		L2SkillType.BLEED,
		L2SkillType.FEAR,
		L2SkillType.CONT,
		L2SkillType.AGGDEBUFF,
		L2SkillType.FUSION
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		boolean acted = true;
		
		L2PcInstance player = null;
		if (activeChar.isPlayer())
		{
			player = activeChar.getActingPlayer();
		}
		
		if (skill.getEffectId() != 0)
		{
			L2Skill sk = SkillHolder.getInstance().getInfo(skill.getEffectId(), skill.getEffectLvl() == 0 ? 1 : skill.getEffectLvl());
			
			if (sk != null)
			{
				skill = sk;
			}
		}
		
		boolean ss = skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
		boolean sps = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		boolean bss = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		
		for (L2Character target : (L2Character[]) targets)
		{
			byte shld = 0;
			
			if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
			{
				target = activeChar;
			}
			
			if ((skill.getSkillType() == L2SkillType.BUFF) && !(activeChar instanceof L2ClanHallManagerInstance))
			{
				if (target != activeChar)
				{
					if (target.isPlayer())
					{
						L2PcInstance trg = target.getActingPlayer();
						if (trg.isCursedWeaponEquipped())
						{
							continue;
						}
						else if (trg.getBlockCheckerArena() != -1)
						{
							continue;
						}
					}
					else if ((player != null) && player.isCursedWeaponEquipped())
					{
						continue;
					}
				}
			}
			
			if (skill.isOffensive() || skill.isDebuff())
			{
				shld = Formulas.calcShldUse(activeChar, target, skill);
				acted = Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss);
			}
			
			if (acted)
			{
				if (skill.isToggle())
				{
					L2Effect[] effects = target.getAllEffects();
					if (effects != null)
					{
						for (L2Effect e : effects)
						{
							if (e != null)
							{
								if (e.getSkill().getId() == skill.getId())
								{
									e.exit();
									return;
								}
							}
						}
					}
				}
				
				if (target.isPlayer() && target.getActingPlayer().isInDuel() && ((skill.getSkillType() == L2SkillType.DEBUFF) || (skill.getSkillType() == L2SkillType.BUFF)) && (player != null) && (player.getDuelId() == target.getActingPlayer().getDuelId()))
				{
					DuelManager dm = DuelManager.getInstance();
					for (L2Effect buff : skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss)))
					{
						if (buff != null)
						{
							dm.onBuff(target.getActingPlayer(), buff);
						}
					}
				}
				else
				{
					L2Effect[] effects = skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
					L2Summon summon = target.getSummon();
					if ((summon != null) && (summon != activeChar) && summon.isServitor() && (effects.length > 0))
					{
						if (effects[0].canBeStolen() || skill.isHeroSkill() || skill.isStatic())
						{
							skill.getEffects(activeChar, target.getSummon(), new Env(shld, ss, sps, bss));
						}
					}
				}
				
				if (skill.getSkillType() == L2SkillType.AGGDEBUFF)
				{
					if (target.isL2Attackable())
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
					}
					else if (target.isPlayable())
					{
						if (target.getTarget() == activeChar)
						{
							target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
						}
						else
						{
							target.setTarget(activeChar);
						}
					}
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.ATTACK_FAILED);
			}
			Formulas.calcLethalHit(activeChar, target, skill);
		}
		
		if (skill.hasSelfEffects())
		{
			final L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if ((effect != null) && effect.isSelfEffect())
			{
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}
		activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}