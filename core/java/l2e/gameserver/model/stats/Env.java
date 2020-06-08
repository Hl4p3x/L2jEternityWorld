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
package l2e.gameserver.model.stats;

import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2CubicInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;

public final class Env
{
	private double _baseValue;
	public boolean _blessedSpiritShot = false;
	public L2Character _character;
	private L2CubicInstance _cubic;
	private L2Effect _effect;
	private L2ItemInstance _item;
	public byte _shield = 0;
	public L2Skill _skill;
	private boolean _skillMastery = false;
	public boolean _soulShot = false;
	public boolean _spiritShot = false;
	public L2Character _target;
	public double _value;
	
	public Env()
	{
	}
	
	public Env(byte shield, boolean soulShot, boolean spiritShot, boolean blessedSpiritShot)
	{
		_shield = shield;
		_soulShot = soulShot;
		_spiritShot = spiritShot;
		_blessedSpiritShot = blessedSpiritShot;
	}

	public Env(L2Character character, L2Character target, L2Skill skill)
	{
		_character = character;
		_target = target;
		_skill = skill;
	}
	
	public double getBaseValue()
	{
		return _baseValue;
	}
	
	public L2Character getCharacter()
	{
		return _character;
	}
	
	public L2CubicInstance getCubic()
	{
		return _cubic;
	}
	
	public L2Effect getEffect()
	{
		return _effect;
	}
	
	public L2ItemInstance getItem()
	{
		return _item;
	}
	
	public L2PcInstance getPlayer()
	{
		return _character == null ? null : _character.getActingPlayer();
	}

	public byte getShield()
	{
		return _shield;
	}
	
	public L2Skill getSkill()
	{
		return _skill;
	}
	
	public L2Character getTarget()
	{
		return _target;
	}
	
	public double getValue()
	{
		return _value;
	}
	
	public boolean isBlessedSpiritShot()
	{
		return _blessedSpiritShot;
	}
	
	public boolean isSkillMastery()
	{
		return _skillMastery;
	}
	
	public boolean isSoulShot()
	{
		return _soulShot;
	}
	
	public boolean isSpiritShot()
	{
		return _spiritShot;
	}
	
	public void setBaseValue(double baseValue)
	{
		_baseValue = baseValue;
	}

	public void setBlessedSpiritShot(boolean blessedSpiritShot)
	{
		_blessedSpiritShot = blessedSpiritShot;
	}

	public void setCharacter(L2Character character)
	{
		_character = character;
	}

	public void setCubic(L2CubicInstance cubic)
	{
		_cubic = cubic;
	}

	public void setEffect(L2Effect effect)
	{
		_effect = effect;
	}

	public void setItem(L2ItemInstance item)
	{
		_item = item;
	}

	public void setShield(byte shield)
	{
		_shield = shield;
	}

	public void setSkill(L2Skill skill)
	{
		_skill = skill;
	}

	public void setSkillMastery(boolean skillMastery)
	{
		_skillMastery = skillMastery;
	}

	public void setSoulShot(boolean soulShot)
	{
		_soulShot = soulShot;
	}

	public void setSpiritShot(boolean spiritShot)
	{
		_spiritShot = spiritShot;
	}

	public void setTarget(L2Character target)
	{
		_target = target;
	}

	public void setValue(double value)
	{
		_value = value;
	}
	
	public void addValue(double value)
	{
		_value += value;
	}
	
	public void subValue(double value)
	{
		_value -= value;
	}
	
	public void mulValue(double value)
	{
		_value *= value;
	}
	
	public void divValue(double value)
	{
		_value /= value;
	}
}