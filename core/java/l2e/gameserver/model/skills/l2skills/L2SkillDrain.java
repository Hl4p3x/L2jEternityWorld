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
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2CubicInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class L2SkillDrain extends L2Skill
{
	private static final Logger _logDamage = Logger.getLogger("damage");
	
	private final float _absorbPart;
	private final int _absorbAbs;
	
	public L2SkillDrain(StatsSet set)
	{
		super(set);
		
		_absorbPart = set.getFloat("absorbPart", 0.f);
		_absorbAbs = set.getInteger("absorbAbs", 0);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}

		boolean ss = useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
		boolean sps = useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		boolean bss = useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		
		for (L2Character target : (L2Character[]) targets)
		{
			if (target.isAlikeDead() && (getTargetType() != L2TargetType.CORPSE_MOB))
			{
				continue;
			}
			
			if ((activeChar != target) && target.isInvul())
			{
				continue;
			}
			
			boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			byte shld = Formulas.calcShldUse(activeChar, target, this);
			int damage = isStaticDamage() ? (int) getPower() : (int) Formulas.calcMagicDam(activeChar, target, this, shld, sps, bss, mcrit);
			
			int _drain = 0;
			int _cp = (int) target.getCurrentCp();
			int _hp = (int) target.getCurrentHp();
			
			if (_cp > 0)
			{
				if (damage < _cp)
				{
					_drain = 0;
				}
				else
				{
					_drain = damage - _cp;
				}
			}
			else if (damage > _hp)
			{
				_drain = _hp;
			}
			else
			{
				_drain = damage;
			}
			
			double hpAdd = _absorbAbs + (_absorbPart * _drain);
			double hp = ((activeChar.getCurrentHp() + hpAdd) > activeChar.getMaxHp() ? activeChar.getMaxHp() : (activeChar.getCurrentHp() + hpAdd));
			
			activeChar.setCurrentHp(hp);
			
			StatusUpdate suhp = new StatusUpdate(activeChar);
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			activeChar.sendPacket(suhp);
			
			if ((damage > 0) && (!target.isDead() || (getTargetType() != L2TargetType.CORPSE_MOB)))
			{
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				activeChar.sendDamageMessage(target, damage, mcrit, false, false);
				
				if (Config.LOG_GAME_DAMAGE && activeChar.isPlayable() && damage > Config.LOG_GAME_DAMAGE_THRESHOLD)
				{
					LogRecord record = new LogRecord(Level.INFO, "");
					record.setParameters(new Object[]
					{
						activeChar,
						" did damage ",
						damage,
						this,
						" to ",
						target
					});
					record.setLoggerName("mdam");
					_logDamage.log(record);
				}
				
				if (hasEffects() && (getTargetType() != L2TargetType.CORPSE_MOB))
				{
					if ((Formulas.calcSkillReflect(target, this) & Formulas.SKILL_REFLECT_SUCCEED) > 0)
					{
						activeChar.stopSkillEffects(getId());
						getEffects(target, activeChar);
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(getId());
						activeChar.sendPacket(sm);
					}
					else
					{
						target.stopSkillEffects(getId());
						if (Formulas.calcSkillSuccess(activeChar, target, this, shld, ss, sps, bss))
						{
							getEffects(activeChar, target);
						}
						else
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(this);
							activeChar.sendPacket(sm);
						}
					}
				}
				
				target.reduceCurrentHp(damage, activeChar, this);
			}
			
			if (target.isDead() && getTargetType() == L2TargetType.CORPSE_MOB && target.isNpc())
			{
				((L2Npc) target).endDecayTask();
			}
		}

		L2Effect effect = activeChar.getFirstEffect(getId());
		if ((effect != null) && effect.isSelfEffect())
		{
			effect.exit();
		}
		getEffectsSelf(activeChar);
		activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
	}
	
	public void useCubicSkill(L2CubicInstance activeCubic, L2Object[] targets)
	{
		if (Config.DEBUG)
		{
			_log.info("L2SkillDrain: useCubicSkill()");
		}
		
		for (L2Character target : (L2Character[]) targets)
		{
			if (target.isAlikeDead() && (getTargetType() != L2TargetType.CORPSE_MOB))
			{
				continue;
			}
			
			boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, this));
			byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, this);
			
			int damage = (int) Formulas.calcMagicDam(activeCubic, target, this, mcrit, shld);
			if (Config.DEBUG)
			{
				_log.info("L2SkillDrain: useCubicSkill() -> damage = " + damage);
			}
			
			double hpAdd = _absorbAbs + (_absorbPart * damage);
			L2PcInstance owner = activeCubic.getOwner();
			double hp = ((owner.getCurrentHp() + hpAdd) > owner.getMaxHp() ? owner.getMaxHp() : (owner.getCurrentHp() + hpAdd));
			
			owner.setCurrentHp(hp);
			
			StatusUpdate suhp = new StatusUpdate(owner);
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			owner.sendPacket(suhp);
			
			if ((damage > 0) && (!target.isDead() || (getTargetType() != L2TargetType.CORPSE_MOB)))
			{
				target.reduceCurrentHp(damage, activeCubic.getOwner(), this);
				
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				owner.sendDamageMessage(target, damage, mcrit, false, false);
			}
		}
	}	
}