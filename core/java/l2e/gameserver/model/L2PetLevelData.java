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
package l2e.gameserver.model;

import l2e.gameserver.model.stats.MoveType;

public class L2PetLevelData
{
	private final int _ownerExpTaken;
	private final int _petFeedBattle;
	private final int _petFeedNormal;
	private final float _petMAtk;
	private final long _petMaxExp;
	private final int _petMaxFeed;
	private final float _petMaxHP;
	private final float _petMaxMP;
	private final float _petMDef;
	private final float _petPAtk;
	private final float _petPDef;
	private final float _petRegenHP;
	private final float _petRegenMP;
	private final short _petSoulShot;
	private final short _petSpiritShot;
	private final int _walkSpeedOnRide;
	private final int _runSpeedOnRide;
	private final int _slowSwimSpeedOnRide;
	private final int _fastSwimSpeedOnRide;
	private final int _slowFlySpeedOnRide;
	private final int _fastFlySpeedOnRide;
	
	public L2PetLevelData(StatsSet set)
	{
		_ownerExpTaken = set.getInteger("get_exp_type");
		_petMaxExp = set.getLong("exp");
		_petMaxHP = set.getFloat("org_hp");
		_petMaxMP = set.getFloat("org_mp");
		_petPAtk = set.getFloat("org_pattack");
		_petPDef = set.getFloat("org_pdefend");
		_petMAtk = set.getFloat("org_mattack");
		_petMDef = set.getFloat("org_mdefend");
		_petMaxFeed = set.getInteger("max_meal");
		_petFeedBattle = set.getInteger("consume_meal_in_battle");
		_petFeedNormal = set.getInteger("consume_meal_in_normal");
		_petRegenHP = set.getFloat("org_hp_regen");
		_petRegenMP = set.getFloat("org_mp_regen");
		_petSoulShot = set.getShort("soulshot_count");
		_petSpiritShot = set.getShort("spiritshot_count");
		_walkSpeedOnRide = set.getInteger("walkSpeedOnRide", 0);
		_runSpeedOnRide = set.getInteger("runSpeedOnRide", 0);
		_slowSwimSpeedOnRide = set.getInteger("slowSwimSpeedOnRide", 0);
		_fastSwimSpeedOnRide = set.getInteger("fastSwimSpeedOnRide", 0);
		_slowFlySpeedOnRide = set.getInteger("slowFlySpeedOnRide", 0);
		_fastFlySpeedOnRide = set.getInteger("fastFlySpeedOnRide", 0);
	}
	
	public int getOwnerExpTaken()
	{
		return _ownerExpTaken;
	}
	
	public int getPetFeedBattle()
	{
		return _petFeedBattle;
	}
	
	public int getPetFeedNormal()
	{
		return _petFeedNormal;
	}
	
	public float getPetMAtk()
	{
		return _petMAtk;
	}
	
	public long getPetMaxExp()
	{
		return _petMaxExp;
	}
	
	public int getPetMaxFeed()
	{
		return _petMaxFeed;
	}
	
	public float getPetMaxHP()
	{
		return _petMaxHP;
	}
	
	public float getPetMaxMP()
	{
		return _petMaxMP;
	}
	
	public float getPetMDef()
	{
		return _petMDef;
	}
	
	public float getPetPAtk()
	{
		return _petPAtk;
	}
	
	public float getPetPDef()
	{
		return _petPDef;
	}
	
	public float getPetRegenHP()
	{
		return _petRegenHP;
	}
	
	public float getPetRegenMP()
	{
		return _petRegenMP;
	}
	
	public short getPetSoulShot()
	{
		return _petSoulShot;
	}
	
	public short getPetSpiritShot()
	{
		return _petSpiritShot;
	}

	public int getSpeedOnRide(MoveType mt)
	{
		switch(mt)
		{
			case WALK:
				return _walkSpeedOnRide;
			case RUN:
				return _runSpeedOnRide;
			case SLOW_SWIM:
				return _slowSwimSpeedOnRide;
			case FAST_SWIM:
				return _fastSwimSpeedOnRide;
			case SLOW_FLY:
				return _slowFlySpeedOnRide;
			case FAST_FLY:
				return _fastFlySpeedOnRide;
		}
		return 0;
	}
}