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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.handler.ISkillHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Pdam implements ISkillHandler
{
	private static final Logger _logDamage = Logger.getLogger("damage");
	
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.PDAM,
		L2SkillType.FATAL
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		if (((skill.getFlyRadius() > 0) || (skill.getFlyType() != null)) && activeChar.isMovementDisabled())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
			activeChar.sendPacket(sm);
			return;
		}
		
		int damage = 0;
		boolean ss = skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
		
		for (L2Character target : (L2Character[]) targets)
		{
			if (activeChar.isPlayer() && target.isPlayer() && target.getActingPlayer().isFakeDeath())
			{
				target.stopFakeDeath(true);
			}
			else if (target.isDead())
			{
				continue;
			}
			
			final byte shld = Formulas.calcShldUse(activeChar, target, skill);
			boolean crit = false;
			
			if (!skill.isStaticDamage() && (skill.getBaseCritRate() > 0))
			{
				crit = Formulas.calcCrit(skill.getBaseCritRate() * 10 * BaseStats.STR.calcBonus(activeChar), true, target);
			}
			
			if (!crit && ((skill.getCondition() & L2Skill.COND_CRIT) != 0))
			{
				damage = 0;
			}
			else
			{
				damage = skill.isStaticDamage() ? (int) skill.getPower() : (int) Formulas.calcPhysDam(activeChar, target, skill, shld, false, ss);
			}
			if (!skill.isStaticDamage() && (skill.getMaxSoulConsumeCount() > 0) && activeChar.isPlayer())
			{
				int chargedSouls = (activeChar.getActingPlayer().getChargedSouls() <= skill.getMaxSoulConsumeCount()) ? activeChar.getActingPlayer().getChargedSouls() : skill.getMaxSoulConsumeCount();
				damage *= 1 + (chargedSouls * 0.04);
			}
			if (crit)
			{
				damage *= 2;
			}
			
			final boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, skill);
			final byte reflect = Formulas.calcSkillReflect(target, skill);
			
			if (!skillIsEvaded)
			{
				if (skill.hasEffects())
				{
					L2Effect[] effects;
					if ((reflect & Formulas.SKILL_REFLECT_SUCCEED) != 0)
					{
						activeChar.stopSkillEffects(skill.getId());
						effects = skill.getEffects(target, activeChar);
						if ((effects != null) && (effects.length > 0))
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
					}
					else
					{
						target.stopSkillEffects(skill.getId());
						effects = skill.getEffects(activeChar, target, new Env(shld, false, false, false));
						if ((effects != null) && (effects.length > 0))
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							sm.addSkillName(skill);
							target.sendPacket(sm);
						}
					}
				}
				
				if (damage > 0)
				{
					activeChar.sendDamageMessage(target, damage, false, crit, false);
					
					if (Config.LOG_GAME_DAMAGE && activeChar.isPlayable() && (damage > Config.LOG_GAME_DAMAGE_THRESHOLD))
					{
						LogRecord record = new LogRecord(Level.INFO, "");
						record.setParameters(new Object[]
						{
							activeChar,
							" did damage ",
							damage,
							skill,
							" to ",
							target
						});
						record.setLoggerName("pdam");
						_logDamage.log(record);
					}
					Formulas.calcLethalHit(activeChar, target, skill);
					
					target.reduceCurrentHp(damage, activeChar, skill);
					
					if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
					{
						if (target.isPlayer())
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_C1_ATTACK);
							sm.addCharName(activeChar);
							target.sendPacket(sm);
						}
						if (activeChar.isPlayer())
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PERFORMING_COUNTERATTACK);
							sm.addCharName(target);
							activeChar.sendPacket(sm);
						}
						double vegdamage = ((1189 * target.getPAtk(activeChar)) / activeChar.getPDef(target));
						activeChar.reduceCurrentHp(vegdamage, target, skill);
					}
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.ATTACK_FAILED);
				}
			}
			else
			{
				if (activeChar.isPlayer())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DODGES_ATTACK);
					sm.addString(target.getName());
					activeChar.getActingPlayer().sendPacket(sm);
				}
				if (target.isPlayer())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_C1_ATTACK);
					sm.addString(activeChar.getName());
					target.getActingPlayer().sendPacket(sm);
				}
				Formulas.calcLethalHit(activeChar, target, skill);
			}
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
		activeChar.setChargedShot(ShotType.SOULSHOTS, false);
		
		if (skill.isSuicideAttack())
		{
			activeChar.doDie(activeChar);
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}