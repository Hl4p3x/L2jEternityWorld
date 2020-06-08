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

import java.util.List;

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import gnu.trove.map.hash.TIntObjectHashMap;

public class SummonEffects
{
	private final TIntObjectHashMap<TIntObjectHashMap<TIntObjectHashMap<List<SummonEffect>>>> _servitorEffects = new TIntObjectHashMap<>();
	
	public TIntObjectHashMap<TIntObjectHashMap<TIntObjectHashMap<List<SummonEffect>>>> getServitorEffectsOwner()
	{
		return _servitorEffects;
	}
	
	public TIntObjectHashMap<List<SummonEffect>> getServitorEffects(L2PcInstance owner)
	{
		final TIntObjectHashMap<TIntObjectHashMap<List<SummonEffect>>> servitorMap = _servitorEffects.get(owner.getObjectId());
		if (servitorMap == null)
		{
			return null;
		}
		return servitorMap.get(owner.getClassIndex());
	}
	
	private final TIntObjectHashMap<List<SummonEffect>> _petEffects = new TIntObjectHashMap<>();
	
	public TIntObjectHashMap<List<SummonEffect>> getPetEffects()
	{
		return _petEffects;
	}
	
	public class SummonEffect
	{
		L2Skill _skill;
		int _effectCount;
		int _effectCurTime;
		
		public SummonEffect(L2Skill skill, int effectCount, int effectCurTime)
		{
			_skill = skill;
			_effectCount = effectCount;
			_effectCurTime = effectCurTime;
		}
		
		public L2Skill getSkill()
		{
			return _skill;
		}
		
		public int getEffectCount()
		{
			return _effectCount;
		}
		
		public int getEffectCurTime()
		{
			return _effectCurTime;
		}
	}
	
	public static SummonEffects getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SummonEffects _instance = new SummonEffects();
	}
}