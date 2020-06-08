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
package l2e.gameserver.model.skills.l2skills;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class L2SkillChargeDmg extends L2Skill
{
	private static final Logger _logDamage = Logger.getLogger("damage");
	
	public L2SkillChargeDmg(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead())
		{
			return;
		}
		
		double modifier = 0;
		if (caster.isPlayer())
		{
			modifier = ((caster.getActingPlayer().getCharges() * 0.25) + 1);
		}
		boolean ss = useSoulShot() && caster.isChargedShot(ShotType.SOULSHOTS);

		for (L2Character target: (L2Character[]) targets)
		{
			if (target.isAlikeDead())
				continue;
			
			boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, this);
			if(skillIsEvaded)
			{
				if (caster.isPlayer())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DODGES_ATTACK);
					sm.addString(target.getName());
					caster.getActingPlayer().sendPacket(sm);
				}
				if (target.isPlayer())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_C1_ATTACK2);
					sm.addString(caster.getName());
					target.getActingPlayer().sendPacket(sm);
				}
				continue;
			}
			
			byte shld = Formulas.calcShldUse(caster, target, this);
			boolean crit = false;
			if (getBaseCritRate() > 0 && !isStaticDamage())
				crit = Formulas.calcCrit(this.getBaseCritRate() * 10 * BaseStats.STR.calcBonus(caster), true, target);

			double damage = isStaticDamage() ? getPower() : Formulas.calcPhysDam(caster, target, this, shld, false, ss);
			if (crit)
				damage *= 2;
			
			if (damage > 0)
			{
				byte reflect = Formulas.calcSkillReflect(target, this);
				if (hasEffects())
				{
					if ((reflect & Formulas.SKILL_REFLECT_SUCCEED) != 0)
					{
						caster.stopSkillEffects(getId());
						getEffects(target, caster);
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(this);
						caster.sendPacket(sm);
					}
					else
					{
						target.stopSkillEffects(getId());
						if (Formulas.calcSkillSuccess(caster, target, this, shld, false, false, true))
						{
							getEffects(caster, target, new Env(shld, false, false, false));
							
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							sm.addSkillName(this);
							target.sendPacket(sm);
						}
						else
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(this);
							caster.sendPacket(sm);
						}
					}
				}
				
				double finalDamage = isStaticDamage() ? damage : damage*modifier;
				
				if (Config.LOG_GAME_DAMAGE && caster.isPlayable() && damage > Config.LOG_GAME_DAMAGE_THRESHOLD)
				{
					LogRecord record = new LogRecord(Level.INFO, "");
					record.setParameters(new Object[]{caster, " did damage ", (int)damage, this, " to ", target});
					record.setLoggerName("pdam");
					_logDamage.log(record);
				}
				
				target.reduceCurrentHp(finalDamage, caster, this);

				if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
				{
					if (target.isPlayer())
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_C1_ATTACK);
						sm.addCharName(caster);
						target.sendPacket(sm);
					}
					if (caster.isPlayer())
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PERFORMING_COUNTERATTACK);
						sm.addCharName(target);
						caster.sendPacket(sm);
					}
					double vegdamage = (1189 * target.getPAtk(caster) / (double)caster.getPDef(target));
					caster.reduceCurrentHp(vegdamage, target, this);
				}
				
				caster.sendDamageMessage(target, (int)finalDamage, false, crit, false);
				
			}
			else
			{
				caster.sendDamageMessage(target, 0, false, false, true);
			}
		}
		
		if (hasSelfEffects())
		{
			L2Effect effect = caster.getFirstEffect(getId());
			if (effect != null && effect.isSelfEffect())
			{
				effect.exit();
			}
			getEffectsSelf(caster);
		}
		caster.setChargedShot(ShotType.SOULSHOTS, false);
	}
}