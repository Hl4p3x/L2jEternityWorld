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
package l2e.gameserver.data.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.engines.DocumentEngine;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.skills.L2Skill;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;

public class SkillHolder
{
	private static Logger _log = Logger.getLogger(SkillHolder.class.getName());
	
	private final Map<Integer, L2Skill> _skills = new HashMap<>();
	private final TIntIntHashMap _skillMaxLevel = new TIntIntHashMap();
	private final TIntArrayList _enchantable = new TIntArrayList();
	
	protected SkillHolder()
	{
		load();
	}
	
	public void reload()
	{
		load();
		SkillTreesParser.getInstance().load();
	}
	
	private void load()
	{
		_skills.clear();
		DocumentEngine.getInstance().loadAllSkills(_skills);
		
		_skillMaxLevel.clear();
		for (final L2Skill skill : _skills.values())
		{
			final int skillId = skill.getId();
			final int skillLvl = skill.getLevel();
			if (skillLvl > 99)
			{
				if (!_enchantable.contains(skillId))
				{
					_enchantable.add(skillId);
				}
				continue;
			}
			final int maxLvl = _skillMaxLevel.get(skillId);
			if (skillLvl > maxLvl)
			{
				_skillMaxLevel.put(skillId, skillLvl);
			}
		}
		_enchantable.sort();
	}
	
	public static int getSkillHashCode(L2Skill skill)
	{
		return getSkillHashCode(skill.getId(), skill.getLevel());
	}
	
	public static int getSkillHashCode(int skillId, int skillLevel)
	{
		return (skillId * 1021) + skillLevel;
	}
	
	public final L2Skill getInfo(final int skillId, final int level)
	{
		final L2Skill result = _skills.get(getSkillHashCode(skillId, level));
		if (result != null)
		{
			return result;
		}
		
		final int maxLvl = _skillMaxLevel.get(skillId);
		if ((maxLvl > 0) && (level > maxLvl))
		{
			if (Config.DEBUG)
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": call to unexisting skill level id: " + skillId + " requested level: " + level + " max level: " + maxLvl, new Throwable());
			}
			return _skills.get(getSkillHashCode(skillId, maxLvl));
		}
		
		_log.warning(getClass().getSimpleName() + ": No skill info found for skill id " + skillId + " and skill level " + level + ".");
		return null;
	}
	
	public final int getMaxLevel(final int skillId)
	{
		return _skillMaxLevel.get(skillId);
	}
	
	public final boolean isEnchantable(final int skillId)
	{
		return _enchantable.binarySearch(skillId) >= 0;
	}
	
	public L2Skill[] getSiegeSkills(boolean addNoble, boolean hasCastle)
	{
		L2Skill[] temp = new L2Skill[2 + (addNoble ? 1 : 0) + (hasCastle ? 2 : 0)];
		int i = 0;
		temp[i++] = _skills.get(SkillHolder.getSkillHashCode(246, 1));
		temp[i++] = _skills.get(SkillHolder.getSkillHashCode(247, 1));
		
		if (addNoble)
		{
			temp[i++] = _skills.get(SkillHolder.getSkillHashCode(326, 1));
		}
		if (hasCastle)
		{
			temp[i++] = _skills.get(SkillHolder.getSkillHashCode(844, 1));
			temp[i++] = _skills.get(SkillHolder.getSkillHashCode(845, 1));
		}
		return temp;
	}
	
	public static enum FrequentSkill
	{
		RAID_CURSE(4215, 1),
		RAID_CURSE2(4515, 1),
		SEAL_OF_RULER(246, 1),
		BUILD_HEADQUARTERS(247, 1),
		WYVERN_BREATH(4289, 1),
		STRIDER_SIEGE_ASSAULT(325, 1),
		FAKE_PETRIFICATION(4616, 1),
		FIREWORK(5965, 1),
		LARGE_FIREWORK(2025, 1),
		BLESSING_OF_PROTECTION(5182, 1),
		VOID_BURST(3630, 1),
		VOID_FLOW(3631, 1),
		THE_VICTOR_OF_WAR(5074, 1),
		THE_VANQUISHED_OF_WAR(5075, 1),
		SPECIAL_TREE_RECOVERY_BONUS(2139, 1),
		WEAPON_GRADE_PENALTY(6209, 1),
		ARMOR_GRADE_PENALTY(6213, 1);
		
		private final SkillsHolder _holder;
		
		private FrequentSkill(int id, int level)
		{
			_holder = new SkillsHolder(id, level);
		}
		
		public int getId()
		{
			return _holder.getSkillId();
		}
		
		public int getLevel()
		{
			return _holder.getSkillLvl();
		}
		
		public L2Skill getSkill()
		{
			return _holder.getSkill();
		}
	}
	
	public static SkillHolder getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillHolder _instance = new SkillHolder();
	}
}