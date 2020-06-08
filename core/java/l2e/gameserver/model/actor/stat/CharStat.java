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
package l2e.gameserver.model.actor.stat;

import l2e.Config;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.stats.Calculator;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.MoveType;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.zone.ZoneId;

public class CharStat
{
	private L2Character _activeChar;
	private long _exp = 0;
	private int _sp = 0;
	private byte _level = 1;
	
	public CharStat(L2Character activeChar)
	{
		_activeChar = activeChar;
	}

	public final double calcStat(Stats stat, double init)
	{
		return calcStat(stat, init, null, null);
	}
	
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		if (stat == null)
		{
			return init;
		}
		
		final int id = stat.ordinal();
		
		final Calculator c = _activeChar.getCalculators()[id];
		
		if ((c == null) || (c.size() == 0))
		{
			return init;
		}

		if (getActiveChar().isPlayer() && getActiveChar().isTransformed())
		{
			double val = getActiveChar().getTransformation().getStat(getActiveChar().getActingPlayer(), stat);
			if (val > 0)
			{
				init = val;
			}
		}
		
		final Env env = new Env();
		env.setCharacter(_activeChar);
		env.setTarget(target);
		env.setSkill(skill);
		env.setValue(init);
		
		c.calc(env);
		if (env.getValue() <= 0)
		{
			switch(stat)
			{
				case MAX_HP:
				case MAX_MP:
				case MAX_CP:
				case MAGIC_DEFENCE:
				case POWER_DEFENCE:
				case POWER_ATTACK:
				case MAGIC_ATTACK:
				case POWER_ATTACK_SPEED:
				case MAGIC_ATTACK_SPEED:
				case SHIELD_DEFENCE:
				case STAT_CON:
				case STAT_DEX:
				case STAT_INT:
				case STAT_MEN:
				case STAT_STR:
				case STAT_WIT:
					env.setValue(1);
			}
		}
		return env.getValue();
	}
	
	public int getAccuracy()
	{
		if (_activeChar == null)
			return 0;
		return (int) Math.round(calcStat(Stats.ACCURACY_COMBAT, 0, null, null));
	}
	
	public L2Character getActiveChar()
	{
		return _activeChar;
	}
	
	public final float getAttackSpeedMultiplier()
	{
		if (_activeChar == null)
			return 1;
		
		return (float) ((1.1) * getPAtkSpd() / _activeChar.getTemplate().getBasePAtkSpd());
	}
	
	public final int getCON()
	{
		if (_activeChar == null)
			return 1;
		
		return (int) calcStat(Stats.STAT_CON, _activeChar.getTemplate().getBaseCON());
	}
	
	public final double getCriticalDmg(L2Character target, double init)
	{
		return calcStat(Stats.CRITICAL_DAMAGE, init, target, null);
	}
	
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
		{
			return 1;
		}
		int criticalHit = (int) calcStat(Stats.CRITICAL_RATE, _activeChar.getTemplate().getBaseCritRate(), target, skill);

		return Math.min(criticalHit, Config.MAX_PCRIT_RATE);
	}
	
	public final int getDEX()
	{
		if (_activeChar == null)
			return 1;
		
		return (int) calcStat(Stats.STAT_DEX, _activeChar.getTemplate().getBaseDEX());
	}
	
	public int getEvasionRate(L2Character target)
	{
		if (_activeChar == null)
			return 1;
		
		int val = (int) Math.round(calcStat(Stats.EVASION_RATE, 0, target, null));
		if (val > Config.MAX_EVASION && !_activeChar.canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
			val = Config.MAX_EVASION;
		return val;
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public void setExp(long value)
	{
		_exp = value;
	}
	
	public int getINT()
	{
		if (_activeChar == null)
			return 1;
		
		return (int) calcStat(Stats.STAT_INT, _activeChar.getTemplate().getBaseINT());
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
	public void setLevel(byte value)
	{
		_level = value;
	}
	
	public final int getMagicalAttackRange(L2Skill skill)
	{
		if (skill != null)
		{
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		}
		return _activeChar.getTemplate().getBaseAttackRange();
	}
	
	public int getMaxCp()
	{
		if (_activeChar == null)
			return 1;
		
		return (int) calcStat(Stats.MAX_CP, _activeChar.getTemplate().getBaseCpMax());
	}
	
	public int getMaxRecoverableCp()
	{
		if (_activeChar == null)
			return 1;
		
		return (int) calcStat(Stats.MAX_RECOVERABLE_CP, getMaxCp());
	}
	
	public int getMaxHp()
	{
		if (_activeChar == null)
			return 1;
		
		return (int) calcStat(Stats.MAX_HP, _activeChar.getTemplate().getBaseHpMax());
	}
	
	public int getMaxRecoverableHp()
	{
		if (_activeChar == null)
			return 1;
		
		return (int) calcStat(Stats.MAX_RECOVERABLE_HP, getMaxHp());
	}
	
	public int getMaxMp()
	{
		if (_activeChar == null)
			return 1;
		
		return (int) calcStat(Stats.MAX_MP, _activeChar.getTemplate().getBaseMpMax());
	}
	
	public int getMaxRecoverableMp()
	{
		if (_activeChar == null)
			return 1;
		
		return (int) calcStat(Stats.MAX_RECOVERABLE_MP, getMaxMp());
	}
	
	public int getMAtk(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
		{
			return 1;
		}
		
		float bonusAtk = 1;
		if (Config.CHAMPION_ENABLE && _activeChar.isChampion())
		{
			bonusAtk = Config.CHAMPION_ATK;
		}

		if (_activeChar.isRaid())
		{
			bonusAtk *= Config.RAID_MATTACK_MULTIPLIER;
		}
		return (int) calcStat(Stats.MAGIC_ATTACK, _activeChar.getTemplate().getBaseMAtk() * bonusAtk, target, skill);
	}
	
	public int getMAtkSpd()
	{
		if (_activeChar == null)
			return 1;
		float bonusSpdAtk = 1;
		if  (Config.CHAMPION_ENABLE && _activeChar.isChampion())
			bonusSpdAtk = Config.CHAMPION_SPD_ATK;
		double val = calcStat(Stats.MAGIC_ATTACK_SPEED, _activeChar.getTemplate().getBaseMAtkSpd() * bonusSpdAtk) * Config.MATK_SPEED_MULTI;
		if (val > Config.MAX_MATK_SPEED && !_activeChar.canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
			val = Config.MAX_MATK_SPEED;
		return (int) val;
	}
	
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
		{
			return 1;
		}
		double mrate = calcStat(Stats.MCRITICAL_RATE, 1, target, skill) * 10;

		return (int) Math.min(mrate, Config.MAX_MCRIT_RATE);
	}
	
	public int getMDef(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return 1;
		
		double defence = _activeChar.getTemplate().getBaseMDef();
		
		if (_activeChar.isRaid())
			defence *= Config.RAID_MDEFENCE_MULTIPLIER;
		
		return (int) calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
	}
	
	public final int getMEN()
	{
		if (_activeChar == null)
			return 1;
		
		return (int) calcStat(Stats.STAT_MEN, _activeChar.getTemplate().getBaseMEN());
	}
	
	public float getMovementSpeedMultiplier()
	{
		return (getRunSpeed() / getBaseMoveSpeed(MoveType.RUN));
	}

	public float getBaseMoveSpeed(MoveType type)
	{
		return _activeChar.getTemplate().getBaseMoveSpeed(type);
	}
	
	public float getMoveSpeed()
	{
		if (_activeChar == null)
			return 1;
		
		return  _activeChar.isRunning() ? getRunSpeed() : getWalkSpeed();
	}
	
	public final double getMReuseRate(L2Skill skill)
	{
		if (_activeChar == null)
			return 1;
		
		return calcStat(Stats.MAGIC_REUSE_RATE, _activeChar.getTemplate().getBaseMReuseRate(), null, skill);
	}
	
	public int getPAtk(L2Character target)
	{
		if (_activeChar == null)
		{
			return 1;
		}
		float bonusAtk = 1;
		if (Config.CHAMPION_ENABLE && _activeChar.isChampion())
		{
			bonusAtk = Config.CHAMPION_ATK;
		}
		if (_activeChar.isRaid())
		{
			bonusAtk *= Config.RAID_PATTACK_MULTIPLIER;
		}
		return (int) calcStat(Stats.POWER_ATTACK, _activeChar.getTemplate().getBasePAtk() * bonusAtk, target, null);
	}
	
	public final double getPAtkAnimals(L2Character target)
	{
		return calcStat(Stats.PATK_ANIMALS, 1, target, null);
	}
	
	public final double getPAtkDragons(L2Character target)
	{
		return calcStat(Stats.PATK_DRAGONS, 1, target, null);
	}
	
	public final double getPAtkInsects(L2Character target)
	{
		return calcStat(Stats.PATK_INSECTS, 1, target, null);
	}
	
	public final double getPAtkMonsters(L2Character target)
	{
		return calcStat(Stats.PATK_MONSTERS, 1, target, null);
	}
	
	public final double getPAtkPlants(L2Character target)
	{
		return calcStat(Stats.PATK_PLANTS, 1, target, null);
	}
	
	public final double getPAtkGiants(L2Character target)
	{
		return calcStat(Stats.PATK_GIANTS, 1, target, null);
	}
	
	public final double getPAtkMagicCreatures(L2Character target)
	{
		return calcStat(Stats.PATK_MCREATURES, 1, target, null);
	}

	public int getPAtkSpd()
	{
		if (_activeChar == null)
			return 1;
		float bonusAtk = 1;
		if  (Config.CHAMPION_ENABLE && _activeChar.isChampion())
			bonusAtk = Config.CHAMPION_SPD_ATK;
		int val = (int) Math.round(calcStat(Stats.POWER_ATTACK_SPEED, _activeChar.getTemplate().getBasePAtkSpd() * bonusAtk, null, null) * Config.PATK_SPEED_MULTI);
		return val;
	}
	
	public final double getPDefAnimals(L2Character target)
	{
		return calcStat(Stats.PDEF_ANIMALS, 1, target, null);
	}
	
	public final double getPDefDragons(L2Character target)
	{
		return calcStat(Stats.PDEF_DRAGONS, 1, target, null);
	}
	
	public final double getPDefInsects(L2Character target)
	{
		return calcStat(Stats.PDEF_INSECTS, 1, target, null);
	}
	
	public final double getPDefMonsters(L2Character target)
	{
		return calcStat(Stats.PDEF_MONSTERS, 1, target, null);
	}
	
	public final double getPDefPlants(L2Character target)
	{
		return calcStat(Stats.PDEF_PLANTS, 1, target, null);
	}
	
	public final double getPDefGiants(L2Character target)
	{
		return calcStat(Stats.PDEF_GIANTS, 1, target, null);
	}

	public final double getPDefMagicCreatures(L2Character target)
	{
		return calcStat(Stats.PDEF_MCREATURES, 1, target, null);
	}

	public int getPDef(L2Character target)
	{
		if (_activeChar == null)
		{
			return 1;
		}
		return (int) calcStat(Stats.POWER_DEFENCE, (_activeChar.isRaid()) ? _activeChar.getTemplate().getBasePDef() * Config.RAID_PDEFENCE_MULTIPLIER : _activeChar.getTemplate().getBasePDef(), target, null);
	}
	
	public final int getPhysicalAttackRange()
	{
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, _activeChar.getTemplate().getBaseAttackRange());
	}
	
	public final double getWeaponReuseModifier(L2Character target)
	{
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}
	
	public int getRunSpeed()
	{
		final float baseRunSpd = _activeChar.isInsideZone(ZoneId.WATER) ? getSwimRunSpeed() : getBaseMoveSpeed(MoveType.RUN);
		if (baseRunSpd == 0)
		{
			return 0;
		}
		return (int) Math.round(calcStat(Stats.MOVE_SPEED, baseRunSpd, null, null));
	}
	
	public int getSwimRunSpeed()
	{
		final float baseRunSpd = getBaseMoveSpeed(MoveType.FAST_SWIM);
		if (baseRunSpd == 0)
		{
			return 0;
		}
		return (int) Math.round(calcStat(Stats.MOVE_SPEED, baseRunSpd, null, null));
	}
	
	public final int getShldDef()
	{
		return (int) calcStat(Stats.SHIELD_DEFENCE, 0);
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public void setSp(int value)
	{
		_sp = value;
	}
	
	public final int getSTR()
	{
		if (_activeChar == null)
			return 1;
		
		return (int) calcStat(Stats.STAT_STR, _activeChar.getTemplate().getBaseSTR());
	}
	
	public int getWalkSpeed()
	{
		final float baseWalkSpd = _activeChar.isInsideZone(ZoneId.WATER) ? getSwimWalkSpeed() : getBaseMoveSpeed(MoveType.WALK);
		if (baseWalkSpd == 0)
		{
			return 0;
		}
		return (int) Math.round(calcStat(Stats.MOVE_SPEED, baseWalkSpd));
	}
	
	public int getSwimWalkSpeed()
	{
		final float baseWalkSpd = getBaseMoveSpeed(MoveType.SLOW_SWIM);
		if (baseWalkSpd == 0)
		{
			return 0;
		}
		return (int) Math.round(calcStat(Stats.MOVE_SPEED, baseWalkSpd));
	}
	
	public final int getWIT()
	{
		if (_activeChar == null)
			return 1;
		
		return (int) calcStat(Stats.STAT_WIT, _activeChar.getTemplate().getBaseWIT());
	}
	
	public final int getMpConsume(L2Skill skill)
	{
		if (skill == null)
			return 1;
		double mpConsume = skill.getMpConsume();
		double nextDanceMpCost = Math.ceil(skill.getMpConsume() / 2.);
		if (skill.isDance())
		{
			if (Config.DANCE_CONSUME_ADDITIONAL_MP && _activeChar != null && _activeChar.getDanceCount() > 0)
				mpConsume += _activeChar.getDanceCount() * nextDanceMpCost;
		}
		
		mpConsume = calcStat(Stats.MP_CONSUME, mpConsume, null, skill);
		
		if (skill.isDance())
			return (int)calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume);
		else if (skill.isMagic())
			return (int)calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume);
		else
			return (int)calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume);
	}

	public final int getMpInitialConsume(L2Skill skill)
	{
		if (skill == null)
			return 1;
		
		double mpConsume = calcStat(Stats.MP_CONSUME, skill.getMpInitialConsume(), null, skill);
		
		if (skill.isDance())
			return (int)calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume);
		else if (skill.isMagic())
			return (int)calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume);
		else
			return (int)calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume);
	}
	
	public byte getAttackElement()
	{
		L2ItemInstance weaponInstance = _activeChar.getActiveWeaponInstance();

		if (weaponInstance != null && weaponInstance.getAttackElementType() >= 0 )
			return weaponInstance.getAttackElementType();
		
		int tempVal =0, stats[] = { 0, 0, 0, 0, 0, 0 };
		
		byte returnVal = -2;
		stats[0] = (int) calcStat(Stats.FIRE_POWER, _activeChar.getTemplate().getBaseFire());
		stats[1] = (int) calcStat(Stats.WATER_POWER, _activeChar.getTemplate().getBaseWater());
		stats[2] = (int) calcStat(Stats.WIND_POWER, _activeChar.getTemplate().getBaseWind());
		stats[3] = (int) calcStat(Stats.EARTH_POWER, _activeChar.getTemplate().getBaseEarth());
		stats[4] = (int) calcStat(Stats.HOLY_POWER, _activeChar.getTemplate().getBaseHoly());
		stats[5] = (int) calcStat(Stats.DARK_POWER, _activeChar.getTemplate().getBaseDark());
		
		for (byte x = 0; x < 6; x++)
		{
			if (stats[x] > tempVal)
			{
				returnVal = x;
				tempVal = stats[x];
			}
		}
		
		return returnVal;
	}
	
	public int getAttackElementValue(byte attackAttribute)
	{
		switch (attackAttribute)
		{
			case Elementals.FIRE:
				return (int) calcStat(Stats.FIRE_POWER, _activeChar.getTemplate().getBaseFire());
			case Elementals.WATER:
				return (int) calcStat(Stats.WATER_POWER, _activeChar.getTemplate().getBaseWater());
			case Elementals.WIND:
				return (int) calcStat(Stats.WIND_POWER, _activeChar.getTemplate().getBaseWind());
			case Elementals.EARTH:
				return (int) calcStat(Stats.EARTH_POWER, _activeChar.getTemplate().getBaseEarth());
			case Elementals.HOLY:
				return (int) calcStat(Stats.HOLY_POWER, _activeChar.getTemplate().getBaseHoly());
			case Elementals.DARK:
				return (int) calcStat(Stats.DARK_POWER, _activeChar.getTemplate().getBaseDark());
			default:
				return 0;
		}
	}
	
	public int getDefenseElementValue(byte defenseAttribute)
	{
		switch (defenseAttribute)
		{
			case Elementals.FIRE:
				return (int) calcStat(Stats.FIRE_RES, _activeChar.getTemplate().getBaseFireRes());
			case Elementals.WATER:
				return (int) calcStat(Stats.WATER_RES, _activeChar.getTemplate().getBaseWaterRes());
			case Elementals.WIND:
				return (int) calcStat(Stats.WIND_RES, _activeChar.getTemplate().getBaseWindRes());
			case Elementals.EARTH:
				return (int) calcStat(Stats.EARTH_RES, _activeChar.getTemplate().getBaseEarthRes());
			case Elementals.HOLY:
				return (int) calcStat(Stats.HOLY_RES, _activeChar.getTemplate().getBaseHolyRes());
			case Elementals.DARK:
				return (int) calcStat(Stats.DARK_RES, _activeChar.getTemplate().getBaseDarkRes());
			default:
				return 0;
		}
	}

	public final double getRExp()
	{
		double val =  calcStat(Stats.RUNE_OF_EXP, 1);
		if (val > 1.5)
			val = 1.5;
		return  val;
	}

	public final double getRSp()
	{
		double val =  calcStat(Stats.RUNE_OF_SP, 1);
		if (val > 1.5)
			val = 1.5;
		return  val;
	}
}