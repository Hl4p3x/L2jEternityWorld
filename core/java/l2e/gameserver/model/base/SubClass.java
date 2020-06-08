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
package l2e.gameserver.model.base;

import l2e.Config;
import l2e.gameserver.data.xml.ExperienceParser;

public final class SubClass
{
	private static final byte _maxLevel = Config.MAX_SUBCLASS_LEVEL < ExperienceParser.getInstance().getMaxLevel() ? Config.MAX_SUBCLASS_LEVEL : (byte) (ExperienceParser.getInstance().getMaxLevel() - 1);
	
	private PlayerClass _class;
	private long _exp = ExperienceParser.getInstance().getExpForLevel(Config.BASE_SUBCLASS_LEVEL);
	private int _sp = 0;
	private byte _level = Config.BASE_SUBCLASS_LEVEL;
	private int _classIndex = 1;
	
	public SubClass(int classId, long exp, int sp, byte level, int classIndex)
	{
		_class = PlayerClass.values()[classId];
		_exp = exp;
		_sp = sp;
		_level = level;
		_classIndex = classIndex;
	}
	
	public SubClass(int classId, int classIndex)
	{
		_class = PlayerClass.values()[classId];
		_classIndex = classIndex;
	}
	
	public SubClass()
	{
	}
	
	public PlayerClass getClassDefinition()
	{
		return _class;
	}
	
	public int getClassId()
	{
		return _class.ordinal();
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
	public int getClassIndex()
	{
		return _classIndex;
	}
	
	public void setClassId(int classId)
	{
		_class = PlayerClass.values()[classId];
	}
	
	public void setExp(long expValue)
	{
		if (expValue > (ExperienceParser.getInstance().getExpForLevel(_maxLevel + 1) - 1))
		{
			expValue = ExperienceParser.getInstance().getExpForLevel(_maxLevel + 1) - 1;
		}
		
		_exp = expValue;
	}
	
	public void setSp(int spValue)
	{
		_sp = spValue;
	}
	
	public void setClassIndex(int classIndex)
	{
		_classIndex = classIndex;
	}
	
	public void setLevel(byte levelValue)
	{
		if (levelValue > _maxLevel)
		{
			levelValue = _maxLevel;
		}
		else if (levelValue < Config.BASE_SUBCLASS_LEVEL)
		{
			levelValue = Config.BASE_SUBCLASS_LEVEL;
		}
		
		_level = levelValue;
	}
	
	public void incLevel()
	{
		if (getLevel() == _maxLevel)
		{
			return;
		}
		
		_level++;
		setExp(ExperienceParser.getInstance().getExpForLevel(getLevel()));
	}
	
	public void decLevel()
	{
		if (getLevel() == Config.BASE_SUBCLASS_LEVEL)
		{
			return;
		}
		
		_level--;
		setExp(ExperienceParser.getInstance().getExpForLevel(getLevel()));
	}
}