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

import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.data.xml.ExperienceParser;
import l2e.gameserver.data.xml.PetsParser;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2PetInstance;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.L2SwampZone;

public class PlayableStat extends CharStat
{
	protected static final Logger _log = Logger.getLogger(PlayableStat.class.getName());
	
	public PlayableStat(L2Playable activeChar)
	{
		super(activeChar);
	}
	
	public boolean addExp(long value)
	{
		if (((getExp() + value) < 0) || ((value > 0) && (getExp() == (getExpForLevel(getMaxLevel()) - 1))))
		{
			return true;
		}
		
		if ((getExp() + value) >= getExpForLevel(getMaxLevel()))
		{
			value = getExpForLevel(getMaxLevel()) - 1 - getExp();
		}
		
		if (!getActiveChar().getEvents().onExperienceReceived(value))
		{
			return false;
		}
		
		setExp(getExp() + value);
		
		byte minimumLevel = 1;
		if (getActiveChar() instanceof L2PetInstance)
		{
			minimumLevel = (byte) PetsParser.getInstance().getPetMinLevel(((L2PetInstance) getActiveChar()).getTemplate().getId());
		}
		
		byte level = minimumLevel;
		
		for (byte tmp = level; tmp <= getMaxLevel(); tmp++)
		{
			if (getExp() >= getExpForLevel(tmp))
			{
				continue;
			}
			level = --tmp;
			break;
		}
		if ((level != getLevel()) && (level >= minimumLevel))
		{
			addLevel((byte) (level - getLevel()));
		}
		
		return true;
	}
	
	public boolean removeExp(long value)
	{
		if ((getExp() - value) < 0)
		{
			value = getExp() - 1;
		}
		
		setExp(getExp() - value);
		
		byte minimumLevel = 1;
		if (getActiveChar() instanceof L2PetInstance)
		{
			minimumLevel = (byte) PetsParser.getInstance().getPetMinLevel(((L2PetInstance) getActiveChar()).getTemplate().getId());
		}
		byte level = minimumLevel;
		
		for (byte tmp = level; tmp <= getMaxLevel(); tmp++)
		{
			if (getExp() >= getExpForLevel(tmp))
			{
				continue;
			}
			level = --tmp;
			break;
		}
		if ((level != getLevel()) && (level >= minimumLevel))
		{
			addLevel((byte) (level - getLevel()));
		}
		return true;
	}
	
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		boolean expAdded = false;
		boolean spAdded = false;
		if (addToExp >= 0)
		{
			expAdded = addExp(addToExp);
		}
		if (addToSp >= 0)
		{
			spAdded = addSp(addToSp);
		}
		
		return expAdded || spAdded;
	}
	
	public boolean removeExpAndSp(long removeExp, int removeSp)
	{
		boolean expRemoved = false;
		boolean spRemoved = false;
		if (removeExp > 0)
		{
			expRemoved = removeExp(removeExp);
		}
		if (removeSp > 0)
		{
			spRemoved = removeSp(removeSp);
		}
		
		return expRemoved || spRemoved;
	}
	
	public boolean addLevel(byte value)
	{
		if ((getLevel() + value) > (getMaxLevel() - 1))
		{
			if (getLevel() < (getMaxLevel() - 1))
			{
				value = (byte) (getMaxLevel() - 1 - getLevel());
			}
			else
			{
				return false;
			}
		}
		
		boolean levelIncreased = ((getLevel() + value) > getLevel());
		value += getLevel();
		setLevel(value);
		
		if ((getExp() >= getExpForLevel(getLevel() + 1)) || (getExpForLevel(getLevel()) > getExp()))
		{
			setExp(getExpForLevel(getLevel()));
		}
		
		if (!levelIncreased && getActiveChar().isPlayer() && !((L2PcInstance) (getActiveChar())).isGM() && Config.DECREASE_SKILL_LEVEL)
		{
			((L2PcInstance) (getActiveChar())).checkPlayerSkills();
		}
		
		if (!levelIncreased)
		{
			return false;
		}
		
		getActiveChar().getStatus().setCurrentHp(getActiveChar().getStat().getMaxHp());
		getActiveChar().getStatus().setCurrentMp(getActiveChar().getStat().getMaxMp());
		
		return true;
	}
	
	public boolean addSp(int value)
	{
		if (value < 0)
		{
			_log.warning("wrong usage");
			return false;
		}
		int currentSp = getSp();
		if (currentSp == Integer.MAX_VALUE)
		{
			return false;
		}
		
		if (currentSp > (Integer.MAX_VALUE - value))
		{
			value = Integer.MAX_VALUE - currentSp;
		}
		
		setSp(currentSp + value);
		return true;
	}
	
	public boolean removeSp(int value)
	{
		int currentSp = getSp();
		if (currentSp < value)
		{
			value = currentSp;
		}
		setSp(getSp() - value);
		return true;
	}
	
	public long getExpForLevel(int level)
	{
		return level;
	}
	
	@Override
	public int getRunSpeed()
	{
		int val = super.getRunSpeed();
		if (getActiveChar().isInsideZone(ZoneId.SWAMP))
		{
			final L2SwampZone zone = ZoneManager.getInstance().getZone(getActiveChar(), L2SwampZone.class);
			int bonus = zone == null ? 0 : zone.getMoveBonus();
			val += val * (bonus / 100.0f);
		}
		return val;
	}
	
	@Override
	public int getWalkSpeed()
	{
		int val = super.getWalkSpeed();
		if (getActiveChar().isInsideZone(ZoneId.SWAMP))
		{
			final L2SwampZone zone = ZoneManager.getInstance().getZone(getActiveChar(), L2SwampZone.class);
			int bonus = zone == null ? 0 : zone.getMoveBonus();
			val += val * (bonus / 100.0f);
		}
		return val;
	}
	
	@Override
	public L2Playable getActiveChar()
	{
		return (L2Playable) super.getActiveChar();
	}
	
	public int getMaxLevel()
	{
		return ExperienceParser.getInstance().getMaxLevel();
	}
}