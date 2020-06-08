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

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.model.skills.L2Skill;
import l2e.util.Rnd;

public final class ChanceCondition
{
	private static final Logger _log = Logger.getLogger(ChanceCondition.class.getName());

	public static final int EVT_HIT = 1;
	public static final int EVT_CRIT = 2;
	public static final int EVT_CAST = 4;
	public static final int EVT_PHYSICAL = 8;
	public static final int EVT_MAGIC = 16;
	public static final int EVT_MAGIC_GOOD = 32;
	public static final int EVT_MAGIC_OFFENSIVE = 64;
	public static final int EVT_ATTACKED = 128;
	public static final int EVT_ATTACKED_HIT = 256;
	public static final int EVT_ATTACKED_CRIT = 512;
	public static final int EVT_HIT_BY_SKILL = 1024;
	public static final int EVT_HIT_BY_OFFENSIVE_SKILL = 2048;
	public static final int EVT_HIT_BY_GOOD_MAGIC = 4096;
	public static final int EVT_EVADED_HIT = 8192;
	public static final int EVT_ON_START = 16384;
	public static final int EVT_ON_ACTION_TIME = 32768;
	public static final int EVT_ON_EXIT = 65536;
	
	public static enum TriggerType
	{
		ON_HIT(1),
		ON_CRIT(2),
		ON_CAST(4),
		ON_PHYSICAL(8),
		ON_MAGIC(16),
		ON_MAGIC_GOOD(32),
		ON_MAGIC_OFFENSIVE(64),
		ON_ATTACKED(128),
		ON_ATTACKED_HIT(256),
		ON_ATTACKED_CRIT(512),
		ON_HIT_BY_SKILL(1024),
		ON_HIT_BY_OFFENSIVE_SKILL(2048),
		ON_HIT_BY_GOOD_MAGIC(4096),
		ON_EVADED_HIT(8192),
		ON_START(16384),
		ON_ACTION_TIME(32768),
		ON_EXIT(65536);
		
		private final int _mask;
		
		private TriggerType(int mask)
		{
			_mask = mask;
		}
		
		public final boolean check(int event)
		{
			return (_mask & event) != 0;
		}
	}
	
	private final TriggerType _triggerType;
	private final int _chance;
	private final int _mindmg;
	private final byte[] _elements;
	private final int[] _activationSkills;
	private final boolean _pvpOnly;
	
	private ChanceCondition(TriggerType trigger, int chance, int mindmg, byte[] elements, int[] activationSkills, boolean pvpOnly)
	{
		_triggerType = trigger;
		_chance = chance;
		_mindmg = mindmg;
		_elements = elements;
		_pvpOnly = pvpOnly;
		_activationSkills = activationSkills;
	}
	
	public static ChanceCondition parse(StatsSet set)
	{
		try
		{
			TriggerType trigger = set.getEnum("chanceType", TriggerType.class, null);
			int chance = set.getInteger("activationChance", -1);
			int mindmg = set.getInteger("activationMinDamage", -1);
			String elements = set.getString("activationElements", null);
			String activationSkills = set.getString("activationSkills", null);
			boolean pvpOnly = set.getBool("pvpChanceOnly", false);
			
			if (trigger != null)
				return new ChanceCondition(trigger, chance, mindmg, parseElements(elements), parseActivationSkills(activationSkills), pvpOnly);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
		return null;
	}
	
	public static ChanceCondition parse(String chanceType, int chance, int mindmg, String elements, String activationSkills, boolean pvpOnly)
	{
		try
		{
			if (chanceType == null)
				return null;
			
			TriggerType trigger = Enum.valueOf(TriggerType.class, chanceType);
			
			if (trigger != null)
				return new ChanceCondition(trigger, chance, mindmg, parseElements(elements), parseActivationSkills(activationSkills), pvpOnly);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
		
		return null;
	}
	
	public static final byte[] parseElements(String list)
	{
		if (list == null)
			return null;
		
		String[] valuesSplit = list.split(",");
		byte[] elements = new byte[valuesSplit.length];
		for (int i = 0; i < valuesSplit.length; i++)
			elements[i] = Byte.parseByte(valuesSplit[i]);
		
		Arrays.sort(elements);
		return elements;
	}
	
	public static final int[] parseActivationSkills(String list)
	{
		if (list == null)
			return null;
		
		String[] valuesSplit = list.split(",");
		int[] skillIds = new int[valuesSplit.length];
		for (int i = 0; i < valuesSplit.length; i++)
			skillIds[i] = Integer.parseInt(valuesSplit[i]);
		
		return skillIds;
	}
	
	public boolean trigger(int event, int damage, byte element, boolean playable, L2Skill skill)
	{
		if (_pvpOnly && !playable)
			return false;
		
		if (_elements != null && Arrays.binarySearch(_elements, element) < 0)
			return false;
		
		if (_activationSkills != null && skill != null && Arrays.binarySearch(_activationSkills, skill.getId()) < 0)
			return false;
		
		if (_mindmg > -1 && _mindmg > damage)
			return false;
		
		return _triggerType.check(event) && (_chance < 0 || Rnd.get(100) < _chance);
	}
	
	@Override
	public String toString()
	{
		return "Trigger["+_chance+";"+_triggerType.toString()+"]";
	}
}