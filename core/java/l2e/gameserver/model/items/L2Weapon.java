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
package l2e.gameserver.model.items;

import java.util.Collection;
import java.util.List;

import javolution.util.FastList;
import l2e.gameserver.handler.ISkillHandler;
import l2e.gameserver.handler.SkillHandler;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.conditions.Condition;
import l2e.gameserver.model.conditions.ConditionGameChance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.items.type.L2WeaponType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.Quest.QuestEventType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.util.StringUtil;

public final class L2Weapon extends L2Item
{
	private final L2WeaponType _type;
	private final boolean _isMagicWeapon;
	private final int _rndDam;
	private final int _soulShotCount;
	private final int _spiritShotCount;
	private final int _mpConsume;
	private SkillsHolder _enchant4Skill = null;
	private final int _changeWeaponId;
	private final SkillsHolder _unequipSkill = null;
	
	private SkillsHolder _skillsOnMagic;
	private Condition _skillsOnMagicCondition = null;
	private SkillsHolder _skillsOnCrit;
	private Condition _skillsOnCritCondition = null;
	
	private final int _reducedSoulshot;
	private final int _reducedSoulshotChance;
	
	private final int _reducedMpConsume;
	private final int _reducedMpConsumeChance;
	
	private final boolean _isForceEquip;
	private final boolean _isAttackWeapon;
	private final boolean _useWeaponSkillsOnly;
	
	public L2Weapon(StatsSet set)
	{
		super(set);
		_type = L2WeaponType.valueOf(set.getString("weapon_type", "none").toUpperCase());
		_type1 = L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE;
		_type2 = L2Item.TYPE2_WEAPON;
		_isMagicWeapon = set.getBool("is_magic_weapon", false);
		_soulShotCount = set.getInteger("soulshots", 0);
		_spiritShotCount = set.getInteger("spiritshots", 0);
		_rndDam = set.getInteger("random_damage", 0);
		_mpConsume = set.getInteger("mp_consume", 0);
		
		String[] reduced_soulshots = set.getString("reduced_soulshot", "").split(",");
		_reducedSoulshotChance = (reduced_soulshots.length == 2) ? Integer.parseInt(reduced_soulshots[0]) : 0;
		_reducedSoulshot = (reduced_soulshots.length == 2) ? Integer.parseInt(reduced_soulshots[1]) : 0;
		
		String[] reduced_mpconsume = set.getString("reduced_mp_consume", "").split(",");
		_reducedMpConsumeChance = (reduced_mpconsume.length == 2) ? Integer.parseInt(reduced_mpconsume[0]) : 0;
		_reducedMpConsume = (reduced_mpconsume.length == 2) ? Integer.parseInt(reduced_mpconsume[1]) : 0;
		
		String skill = set.getString("enchant4_skill", null);
		if (skill != null)
		{
			String[] info = skill.split("-");
			
			if ((info != null) && (info.length == 2))
			{
				int id = 0;
				int level = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
				}
				catch (Exception nfe)
				{
					_log.info(StringUtil.concat("> Couldnt parse ", skill, " in weapon enchant skills! item ", toString()));
				}
				if ((id > 0) && (level > 0))
				{
					_enchant4Skill = new SkillsHolder(id, level);
				}
			}
		}
		
		skill = set.getString("onmagic_skill", null);
		if (skill != null)
		{
			String[] info = skill.split("-");
			final int chance = set.getInteger("onmagic_chance", 100);
			if ((info != null) && (info.length == 2))
			{
				int id = 0;
				int level = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
				}
				catch (Exception nfe)
				{
					_log.info(StringUtil.concat("> Couldnt parse ", skill, " in weapon onmagic skills! item ", toString()));
				}
				if ((id > 0) && (level > 0) && (chance > 0))
				{
					_skillsOnMagic = new SkillsHolder(id, level);
					_skillsOnMagicCondition = new ConditionGameChance(chance);
				}
			}
		}
		
		skill = set.getString("oncrit_skill", null);
		if (skill != null)
		{
			String[] info = skill.split("-");
			final int chance = set.getInteger("oncrit_chance", 100);
			if ((info != null) && (info.length == 2))
			{
				int id = 0;
				int level = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
				}
				catch (Exception nfe)
				{
					_log.info(StringUtil.concat("> Couldnt parse ", skill, " in weapon oncrit skills! item ", toString()));
				}
				if ((id > 0) && (level > 0) && (chance > 0))
				{
					_skillsOnCrit = new SkillsHolder(id, level);
					_skillsOnCritCondition = new ConditionGameChance(chance);
				}
			}
		}
		
		skill = set.getString("unequip_skill", null);
		if (skill != null)
		{
			String[] info = skill.split("-");
			if ((info != null) && (info.length == 2))
			{
				int id = 0;
				int level = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
				}
				catch (Exception nfe)
				{
					_log.info(StringUtil.concat("> Couldnt parse ", skill, " in weapon unequip skills! item ", this.toString()));
				}
				if ((id > 0) && (level > 0))
				{
					setUnequipSkills(new SkillsHolder(id, level));
				}
			}
		}
		
		_changeWeaponId = set.getInteger("change_weaponId", 0);
		_isForceEquip = set.getBool("isForceEquip", false);
		_isAttackWeapon = set.getBool("isAttackWeapon", true);
		_useWeaponSkillsOnly = set.getBool("useWeaponSkillsOnly", false);
	}
	
	@Override
	public L2WeaponType getItemType()
	{
		return _type;
	}
	
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
	
	@Override
	public boolean isMagicWeapon()
	{
		return _isMagicWeapon;
	}
	
	public int getSoulShotCount()
	{
		return _soulShotCount;
	}
	
	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}
	
	public int getReducedSoulShot()
	{
		return _reducedSoulshot;
	}
	
	public int getReducedSoulShotChance()
	{
		return _reducedSoulshotChance;
	}
	
	public int getRandomDamage()
	{
		return _rndDam;
	}
	
	public int getMpConsume()
	{
		return _mpConsume;
	}
	
	public int getReducedMpConsume()
	{
		return _reducedMpConsume;
	}
	
	public int getReducedMpConsumeChance()
	{
		return _reducedMpConsumeChance;
	}
	
	@Override
	public L2Skill getEnchant4Skill()
	{
		if (_enchant4Skill == null)
		{
			return null;
		}
		return _enchant4Skill.getSkill();
	}
	
	public int getChangeWeaponId()
	{
		return _changeWeaponId;
	}
	
	public boolean isForceEquip()
	{
		return _isForceEquip;
	}
	
	public boolean isAttackWeapon()
	{
		return _isAttackWeapon;
	}
	
	public boolean useWeaponSkillsOnly()
	{
		return _useWeaponSkillsOnly;
	}
	
	public L2Effect[] getSkillEffects(L2Character caster, L2Character target, boolean crit)
	{
		if ((_skillsOnCrit == null) || !crit)
		{
			return _emptyEffectSet;
		}
		
		final List<L2Effect> effects = new FastList<>();
		final L2Skill onCritSkill = _skillsOnCrit.getSkill();
		if (_skillsOnCritCondition != null)
		{
			Env env = new Env();
			env.setCharacter(caster);
			env.setTarget(target);
			env.setSkill(onCritSkill);
			if (!_skillsOnCritCondition.test(env))
			{
				return _emptyEffectSet;
			}
		}
		
		if (!onCritSkill.checkCondition(caster, target, false))
		{
			return _emptyEffectSet;
		}
		
		final byte shld = Formulas.calcShldUse(caster, target, onCritSkill);
		if (!Formulas.calcSkillSuccess(caster, target, onCritSkill, shld, false, false, false))
		{
			return _emptyEffectSet;
		}
		if (target.getFirstEffect(onCritSkill.getId()) != null)
		{
			target.getFirstEffect(onCritSkill.getId()).exit();
		}
		for (L2Effect e : onCritSkill.getEffects(caster, target, new Env(shld, false, false, false)))
		{
			effects.add(e);
		}
		if (effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public L2Effect[] getSkillEffects(L2Character caster, L2Character target, L2Skill trigger)
	{
		if (_skillsOnMagic == null)
		{
			return _emptyEffectSet;
		}
		
		final L2Skill onMagicSkill = _skillsOnMagic.getSkill();
		
		if (trigger.isOffensive() && onMagicSkill.isOffensive())
		{
			return _emptyEffectSet;
		}
		
		if (!trigger.isMagic() && !onMagicSkill.isMagic())
		{
			return _emptyEffectSet;
		}
		
		if (_skillsOnMagicCondition != null)
		{
			Env env = new Env();
			env.setCharacter(caster);
			env.setTarget(target);
			env.setSkill(onMagicSkill);
			if (!_skillsOnMagicCondition.test(env))
			{
				return _emptyEffectSet;
			}
		}
		
		if (!onMagicSkill.checkCondition(caster, target, false))
		{
			return _emptyEffectSet;
		}
		
		final byte shld = Formulas.calcShldUse(caster, target, onMagicSkill);
		if (onMagicSkill.isOffensive() && !Formulas.calcSkillSuccess(caster, target, onMagicSkill, shld, false, false, false))
		{
			return _emptyEffectSet;
		}
		
		L2Character[] targets =
		{
			target
		};
		
		final ISkillHandler handler = SkillHandler.getInstance().getHandler(onMagicSkill.getSkillType());
		if (handler != null)
		{
			handler.useSkill(caster, onMagicSkill, targets);
		}
		else
		{
			onMagicSkill.useSkill(caster, targets);
		}
		
		if (caster instanceof L2PcInstance)
		{
			Collection<L2Object> objs = caster.getKnownList().getKnownObjects().values();
			for (L2Object spMob : objs)
			{
				if (spMob instanceof L2Npc)
				{
					L2Npc npcMob = (L2Npc) spMob;
					if (npcMob.getTemplate().getEventQuests(QuestEventType.ON_SKILL_SEE) != null)
					{
						for (Quest quest : npcMob.getTemplate().getEventQuests(QuestEventType.ON_SKILL_SEE))
						{
							quest.notifySkillSee(npcMob, caster.getActingPlayer(), onMagicSkill, targets, false);
						}
					}
				}
			}
		}
		return _emptyEffectSet;
	}
	
	public SkillsHolder getUnequipSkills()
	{
		return _unequipSkill;
	}
	
	public void setUnequipSkills(SkillsHolder unequipSkill)
	{
		unequipSkill = _unequipSkill;
	}
}