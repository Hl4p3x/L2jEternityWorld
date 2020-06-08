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
package l2e.gameserver.model.actor.templates;

import java.util.Arrays;
import java.util.Map;

import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.items.type.L2WeaponType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.stats.MoveType;

public class L2CharTemplate
{
	private int _baseSTR;
	private int _baseCON;
	private int _baseDEX;
	private int _baseINT;
	private int _baseWIT;
	private int _baseMEN;
	private float _baseHpMax;
	private float _baseCpMax;
	private float _baseMpMax;
	public float _baseHpReg;
	private float _baseMpReg;
	public int _basePAtk;
	public int _baseMAtk;
	public int _basePDef;
	public int _baseMDef;
	private int _basePAtkSpd;
	private int _baseMAtkSpd;
	private float _baseMReuseRate;
	private int _baseAttackRange;
	private L2WeaponType _baseAttackType;
	private int _baseShldDef;
	private int _baseShldRate;
	private int _baseCritRate;
	private int _baseMCritRate;

	private int _baseBreath;
	private int _baseAggression;
	private int _baseBleed;
	private int _basePoison;
	private int _baseStun;
	private int _baseRoot;
	private int _baseMovement;
	private int _baseConfusion;
	private int _baseSleep;
	private double _baseAggressionVuln;
	private double _baseBleedVuln;
	private double _basePoisonVuln;
	private double _baseStunVuln;
	private double _baseRootVuln;
	private double _baseMovementVuln;
	private double _baseSleepVuln;
	private double _baseCritVuln;
	private int _baseFire;
	private int _baseWind;
	private int _baseWater;
	private int _baseEarth;
	private int _baseHoly;
	private int _baseDark;
	private double _baseFireRes;
	private double _baseWindRes;
	private double _baseWaterRes;
	private double _baseEarthRes;
	private double _baseHolyRes;
	private double _baseDarkRes;
	private double _baseElementRes;
	
	private int _baseMpConsumeRate;
	private int _baseHpConsumeRate;
	
	public int _collisionRadius;
	
	private int _collisionHeight;
	
	private double _fCollisionRadius;
	private double _fCollisionHeight;

	private final float[] _moveType = new float[MoveType.values().length];
	
	public L2CharTemplate(StatsSet set)
	{
		set(set);
	}
	
	public void set(StatsSet set)
	{
		_baseSTR = set.getInteger("baseSTR", 0);
		_baseCON = set.getInteger("baseCON", 0);
		_baseDEX = set.getInteger("baseDEX", 0);
		_baseINT = set.getInteger("baseINT", 0);
		_baseWIT = set.getInteger("baseWIT", 0);
		_baseMEN = set.getInteger("baseMEN", 0);
		_baseHpMax = set.getFloat("baseHpMax", 0);
		_baseCpMax = set.getFloat("baseCpMax", 0);
		_baseMpMax = set.getFloat("baseMpMax", 0);
		_baseHpReg = set.getFloat("baseHpReg", 0);
		_baseMpReg = set.getFloat("baseMpReg", 0);
		_basePAtk = set.getInteger("basePAtk", 0);
		_baseMAtk = set.getInteger("baseMAtk", 0);
		_basePDef = set.getInteger("basePDef", 0);
		_baseMDef = set.getInteger("baseMDef", 0);
		_basePAtkSpd = set.getInteger("basePAtkSpd", 300);
		_baseMAtkSpd = set.getInteger("baseMAtkSpd", 333);
		_baseMReuseRate = set.getFloat("baseMReuseDelay", 1.f);
		_baseShldDef = set.getInteger("baseShldDef", 0);
		_baseAttackRange = set.getInteger("baseAtkRange", 40);
		_baseAttackType = L2WeaponType.findByName(set.getString("baseAtkType", "Fist"));
		_baseShldRate = set.getInteger("baseShldRate", 0);
		_baseCritRate = set.getInteger("baseCritRate", 4);
		_baseMCritRate = set.getInteger("baseMCritRate", 0);
		
		_baseBreath = set.getInteger("baseBreath", 100);
		_baseAggression = set.getInteger("baseAggression", 0);
		_baseBleed = set.getInteger("baseBleed", 0);
		_basePoison = set.getInteger("basePoison", 0);
		_baseStun = set.getInteger("baseStun", 0);
		_baseRoot = set.getInteger("baseRoot", 0);
		_baseMovement = set.getInteger("baseMovement", 0);
		_baseConfusion = set.getInteger("baseConfusion", 0);
		_baseSleep = set.getInteger("baseSleep", 0);
		_baseFire = set.getInteger("baseFire", 0);
		_baseWind = set.getInteger("baseWind", 0);
		_baseWater = set.getInteger("baseWater", 0);
		_baseEarth = set.getInteger("baseEarth", 0);
		_baseHoly = set.getInteger("baseHoly", 0);
		_baseDark = set.getInteger("baseDark", 0);
		_baseAggressionVuln = set.getInteger("baseAggressionVuln", 0);
		_baseBleedVuln = set.getInteger("baseBleedVuln", 0);
		_basePoisonVuln = set.getInteger("basePoisonVuln", 0);
		_baseStunVuln = set.getInteger("baseStunVuln", 0);
		_baseRootVuln = set.getInteger("baseRootVuln", 0);
		_baseMovementVuln = set.getInteger("baseMovementVuln", 0);
		_baseSleepVuln = set.getInteger("baseSleepVuln", 0);
		_baseCritVuln = set.getInteger("baseCritVuln", 1);
		_baseFireRes = set.getInteger("baseFireRes", 0);
		_baseWindRes = set.getInteger("baseWindRes", 0);
		_baseWaterRes = set.getInteger("baseWaterRes", 0);
		_baseEarthRes = set.getInteger("baseEarthRes", 0);
		_baseHolyRes = set.getInteger("baseHolyRes", 0);
		_baseDarkRes = set.getInteger("baseDarkRes", 0);
		_baseElementRes = set.getInteger("baseElementRes", 0);
		
		_baseMpConsumeRate = set.getInteger("baseMpConsumeRate", 0);
		_baseHpConsumeRate = set.getInteger("baseHpConsumeRate", 0);
		
		_fCollisionHeight = set.getDouble("collision_height", 0);
		_fCollisionRadius = set.getDouble("collision_radius", 0);
		_collisionRadius = (int) _fCollisionRadius;
		_collisionHeight = (int) _fCollisionHeight;

		Arrays.fill(_moveType, 1);
		setBaseMoveSpeed(MoveType.RUN, set.getInteger("baseRunSpd", 1));
		setBaseMoveSpeed(MoveType.WALK, set.getInteger("baseWalkSpd", 1));
		setBaseMoveSpeed(MoveType.FAST_SWIM, set.getInteger("baseSwimRunSpd", 1));
		setBaseMoveSpeed(MoveType.SLOW_SWIM, set.getInteger("baseSwimWalkSpd", 1));
	}
	
	public float getBaseHpMax()
	{
		return _baseHpMax;
	}
	
	public int getBaseFire()
	{
		return _baseFire;
	}
	
	public int getBaseWind()
	{
		return _baseWind;
	}
	
	public int getBaseWater()
	{
		return _baseWater;
	}
	
	public int getBaseEarth()
	{
		return _baseEarth;
	}
	
	public int getBaseHoly()
	{
		return _baseHoly;
	}
	
	public int getBaseDark()
	{
		return _baseDark;
	}
	
	public double getBaseFireRes()
	{
		return _baseFireRes;
	}
	
	public double getBaseWindRes()
	{
		return _baseWindRes;
	}
	
	public double getBaseWaterRes()
	{
		return _baseWaterRes;
	}
	
	public double getBaseEarthRes()
	{
		return _baseEarthRes;
	}
	
	public double getBaseHolyRes()
	{
		return _baseHolyRes;
	}
	
	public double getBaseDarkRes()
	{
		return _baseDarkRes;
	}

	public double getBaseElementRes()
	{
		return _baseElementRes;
	}
	
	public int getBaseSTR()
	{
		return _baseSTR;
	}
	
	public int getBaseCON()
	{
		return _baseCON;
	}
	
	public int getBaseDEX()
	{
		return _baseDEX;
	}
	
	public int getBaseINT()
	{
		return _baseINT;
	}
	
	public int getBaseWIT()
	{
		return _baseWIT;
	}
	
	public int getBaseMEN()
	{
		return _baseMEN;
	}
	
	public float getBaseCpMax()
	{
		return _baseCpMax;
	}
	
	public float getBaseMpMax()
	{
		return _baseMpMax;
	}
	
	public float getBaseHpReg()
	{
		return _baseHpReg;
	}
	
	public float getBaseMpReg()
	{
		return _baseMpReg;
	}
	
	public int getBasePAtk()
	{
		return _basePAtk;
	}
	
	public int getBaseMAtk()
	{
		return _baseMAtk;
	}
	
	public int getBasePDef()
	{
		return _basePDef;
	}

	public int getBaseMDef()
	{
		return _baseMDef;
	}
	
	public int getBasePAtkSpd()
	{
		return _basePAtkSpd;
	}
	
	public int getBaseMAtkSpd()
	{
		return _baseMAtkSpd;
	}
	
	public float getBaseMReuseRate()
	{
		return _baseMReuseRate;
	}
	
	public int getBaseShldDef()
	{
		return _baseShldDef;
	}
	
	public int getBaseShldRate()
	{
		return _baseShldRate;
	}
	
	public int getBaseCritRate()
	{
		return _baseCritRate;
	}
	
	public int getBaseMCritRate()
	{
		return _baseMCritRate;
	}
	
	public void setBaseMoveSpeed(MoveType type, float val)
	{
		_moveType[type.ordinal()] = val;
	}
	
	public float getBaseMoveSpeed(MoveType mt)
	{
		return _moveType[mt.ordinal()];
	}

	public int getBaseBreath()
	{
		return _baseBreath;
	}
	
	public int getBaseAggression()
	{
		return _baseAggression;
	}
	
	public int getBaseBleed()
	{
		return _baseBleed;
	}
	
	public int getBasePoison()
	{
		return _basePoison;
	}
	
	public int getBaseStun()
	{
		return _baseStun;
	}
	
	public int getBaseRoot()
	{
		return _baseRoot;
	}
	
	public int getBaseMovement()
	{
		return _baseMovement;
	}
	
	public int getBaseConfusion()
	{
		return _baseConfusion;
	}
	
	public int getBaseSleep()
	{
		return _baseSleep;
	}
	
	public double getBaseAggressionVuln()
	{
		return _baseAggressionVuln;
	}
	
	public double getBaseBleedVuln()
	{
		return _baseBleedVuln;
	}
	
	public double getBasePoisonVuln()
	{
		return _basePoisonVuln;
	}
	
	public double getBaseStunVuln()
	{
		return _baseStunVuln;
	}
	
	public double getBaseRootVuln()
	{
		return _baseRootVuln;
	}
	
	public double getBaseMovementVuln()
	{
		return _baseMovementVuln;
	}
	
	public double getBaseSleepVuln()
	{
		return _baseSleepVuln;
	}
	
	public double getBaseCritVuln()
	{
		return _baseCritVuln;
	}
	
	public int getBaseMpConsumeRate()
	{
		return _baseMpConsumeRate;
	}
	
	public int getBaseHpConsumeRate()
	{
		return _baseHpConsumeRate;
	}
	
	public int getCollisionRadius()
	{
		return _collisionRadius;
	}
	
	public int getCollisionHeight()
	{
		return _collisionHeight;
	}
	
	public double getfCollisionRadius()
	{
		return _fCollisionRadius;
	}
	
	public double getfCollisionHeight()
	{
		return _fCollisionHeight;
	}
	
	public void setBaseFire(int baseFire)
	{
		_baseFire = baseFire;
	}
	
	public void setBaseWater(int baseWater)
	{
		_baseWater = baseWater;
	}
	
	public void setBaseEarth(int baseEarth)
	{
		_baseEarth = baseEarth;
	}
	
	public void setBaseWind(int baseWind)
	{
		_baseWind = baseWind;
	}
	
	public void setBaseHoly(int baseHoly)
	{
		_baseHoly = baseHoly;
	}
	
	public void setBaseDark(int baseDark)
	{
		_baseDark = baseDark;
	}
	
	public void setBaseFireRes(double baseFireRes)
	{
		_baseFireRes = baseFireRes;
	}
	
	public void setBaseWaterRes(double baseWaterRes)
	{
		_baseWaterRes = baseWaterRes;
	}
	
	public void setBaseEarthRes(double baseEarthRes)
	{
		_baseEarthRes = baseEarthRes;
	}
	
	public void setBaseWindRes(double baseWindRes)
	{
		_baseWindRes = baseWindRes;
	}
	
	public void setBaseHolyRes(double baseHolyRes)
	{
		_baseHolyRes = baseHolyRes;
	}
	
	public void setBaseDarkRes(double baseDarkRes)
	{
		_baseDarkRes = baseDarkRes;
	}

	public void setBaseElementRes(double baseElementRes)
	{
		_baseElementRes = baseElementRes;
	}

	public L2WeaponType getBaseAttackType()
	{
		return _baseAttackType;
	}
	
	public void setBaseAttackType(L2WeaponType type)
	{
		_baseAttackType = type;
	}
	
	public int getBaseAttackRange()
	{
		return _baseAttackRange;
	}
	
	public void setBaseAttackRange(int val)
	{
		_baseAttackRange = val;
	}

	public Map<Integer, L2Skill> getSkills()
	{
		return null;
	}
}