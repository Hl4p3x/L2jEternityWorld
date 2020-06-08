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
package l2e.gameserver.model.skills;

import java.lang.reflect.Constructor;

import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.skills.l2skills.*;

public enum L2SkillType
{
	CALCULATE_CHANCE,

	// Damage
	PDAM,
	MDAM,
	MANADAM,
	CPDAMPERCENT,
	DOT,
	MDOT,
	DRAIN(L2SkillDrain.class),
	DEATHLINK,
	FATAL,
	BLOW,
	SIGNET(L2SkillSignet.class),
	SIGNET_CASTTIME(L2SkillSignetCasttime.class),
	
	// Disablers
	BLEED,
	POISON,
	STUN,
	ROOT,
	CONFUSION,
	FEAR,
	SLEEP,
	CONFUSE_MOB_ONLY,
	MUTE,
	PARALYZE,
	DISARM,
	
	// Aggro
	AGGDAMAGE,
	AGGREDUCE,
	AGGREMOVE,
	AGGREDUCE_CHAR,
	AGGDEBUFF,
	
	// Fishing
	FISHING,
	PUMPING,
	REELING,
	
	// MISC
	UNLOCK,
	UNLOCK_SPECIAL,
	ENCHANT_ARMOR,
	ENCHANT_WEAPON,
	ENCHANT_ATTRIBUTE,
	SOULSHOT,
	SPIRITSHOT,
	SIEGEFLAG(L2SkillSiegeFlag.class),
	TAKEFORT,
	DELUXE_KEY_UNLOCK,
	SOW,
	GET_PLAYER,
	DETECTION,
	DUMMY,
	
	// Summons
	SUMMON(L2SkillSummon.class),
	FEED_PET,
	ERASE,
	BETRAY,
	
	BUFF,
	DEBUFF,
	CONT,
	FUSION,
	
	RESURRECT,
	CHARGEDAM(L2SkillChargeDmg.class),
	DETECT_TRAP,
	REMOVE_TRAP,
	
	// Skill is done within the core.
	COREDONE,
	
	// Nornil's Power (Nornil's Garden instance)
	NORNILS_POWER,
	
	// unimplemented
	NOTDONE,
	BALLISTA,
	BOMB,
	CAPTURE;
	
	private final Class<? extends L2Skill> _class;
	
	public L2Skill makeSkill(StatsSet set)
	{
		try
		{
			Constructor<? extends L2Skill> c = _class.getConstructor(StatsSet.class);
			
			return c.newInstance(set);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private L2SkillType()
	{
		_class = L2SkillDefault.class;
	}
	
	private L2SkillType(Class<? extends L2Skill> classType)
	{
		_class = classType;
	}
}