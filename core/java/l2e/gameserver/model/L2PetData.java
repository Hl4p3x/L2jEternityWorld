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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.holders.SkillsHolder;

public class L2PetData
{
	private final Map<Integer, L2PetLevelData> _levelStats = new HashMap<>();
	private final List<L2PetSkillLearn> _skills = new ArrayList<>();
	
	private final int _npcId;
	private final int _itemId;
	private int _load = 20000;
	private int _hungryLimit = 1;
	private int _minlvl = Byte.MAX_VALUE;
	private boolean _syncLevel = false;
	private final List<Integer> _food = new ArrayList<>();
	
	public L2PetData(int npcId, int itemId)
	{
		_npcId = npcId;
		_itemId = itemId;
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public void addNewStat(int level, L2PetLevelData data)
	{
		if (_minlvl > level)
		{
			_minlvl = level;
		}
		_levelStats.put(level, data);
	}
	
	public L2PetLevelData getPetLevelData(int petLevel)
	{
		return _levelStats.get(petLevel);
	}
	
	public int getLoad()
	{
		return _load;
	}
	
	public int getHungryLimit()
	{
		return _hungryLimit;
	}
	
	public boolean isSynchLevel()
	{
		return _syncLevel;
	}
	
	public int getMinLevel()
	{
		return _minlvl;
	}
	
	public List<Integer> getFood()
	{
		return _food;
	}
	
	public void addFood(Integer foodId)
	{
		_food.add(foodId);
	}
	
	public void setLoad(int load)
	{
		_load = load;
	}
	
	public void setHungryLimit(int limit)
	{
		_hungryLimit = limit;
	}
	
	public void setSyncLevel(boolean val)
	{
		_syncLevel = val;
	}
	
	public void addNewSkill(int skillId, int skillLvl, int petLvl)
	{
		_skills.add(new L2PetSkillLearn(skillId, skillLvl, petLvl));
	}
	
	public int getAvailableLevel(int skillId, int petLvl)
	{
		int lvl = 0;
		for (L2PetSkillLearn temp : _skills)
		{
			if (temp.getSkillId() != skillId)
			{
				continue;
			}
			if (temp.getSkillLvl() == 0)
			{
				if (petLvl < 70)
				{
					lvl = (petLvl / 10);
					if (lvl <= 0)
					{
						lvl = 1;
					}
				}
				else
				{
					lvl = (7 + ((petLvl - 70) / 5));
				}
				
				int maxLvl = SkillHolder.getInstance().getMaxLevel(temp.getSkillId());
				if (lvl > maxLvl)
				{
					lvl = maxLvl;
				}
				break;
			}
			else if (temp.getMinLevel() <= petLvl)
			{
				if (temp.getSkillLvl() > lvl)
				{
					lvl = temp.getSkillLvl();
				}
			}
		}
		return lvl;
	}
	
	public List<L2PetSkillLearn> getAvailableSkills()
	{
		return _skills;
	}
	
	public static final class L2PetSkillLearn extends SkillsHolder
	{
		private final int _minLevel;
		
		public L2PetSkillLearn(int id, int lvl, int minLvl)
		{
			super(id, lvl);
			_minLevel = minLvl;
		}
		
		public int getMinLevel()
		{
			return _minLevel;
		}
	}
}