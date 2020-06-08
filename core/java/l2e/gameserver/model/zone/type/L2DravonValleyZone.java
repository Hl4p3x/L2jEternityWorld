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
package l2e.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.L2Object.InstanceType;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.AbstractZoneSettings;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.gameserver.model.zone.TaskZoneSettings;
import l2e.gameserver.model.zone.ZoneId;
import l2e.util.Rnd;

/**
 * Created by LordWinter 06.06.2012 Fixed by L2J Eternity-World
 */
public class L2DravonValleyZone extends L2ZoneType
{
	private static final Map<ClassId, Double> weight = new HashMap<>();
	protected static List<L2Character> inzone = new ArrayList<>();
	
	private int _chance;
	private final int _initialDelay;
	private final int _reuse;
	private boolean _enabled;
	protected boolean _bypassConditions;
	
	public L2DravonValleyZone(int id)
	{
		super(id);
		
		_chance = 100;
		_initialDelay = 1000;
		_reuse = 60000;
		_enabled = true;
		setTargetType(InstanceType.L2Playable);
		_bypassConditions = false;
		AbstractZoneSettings settings = ZoneManager.getSettings(getName());
		if (settings == null)
		{
			settings = new TaskZoneSettings();
		}
		setSettings(settings);
	}
	
	static
	{
		weight.put(ClassId.duelist, 0.2);
		weight.put(ClassId.dreadnought, 0.7);
		weight.put(ClassId.phoenixKnight, 0.5);
		weight.put(ClassId.hellKnight, 0.5);
		weight.put(ClassId.sagittarius, 0.3);
		weight.put(ClassId.adventurer, 0.4);
		weight.put(ClassId.archmage, 0.3);
		weight.put(ClassId.soultaker, 0.3);
		weight.put(ClassId.arcanaLord, 1.);
		weight.put(ClassId.cardinal, -0.6);
		weight.put(ClassId.hierophant, 0.);
		weight.put(ClassId.evaTemplar, 0.8);
		weight.put(ClassId.swordMuse, 0.5);
		weight.put(ClassId.windRider, 0.4);
		weight.put(ClassId.moonlightSentinel, 0.3);
		weight.put(ClassId.mysticMuse, 0.3);
		weight.put(ClassId.elementalMaster, 1.);
		weight.put(ClassId.evaSaint, -0.6);
		weight.put(ClassId.shillienTemplar, 0.8);
		weight.put(ClassId.spectralDancer, 0.5);
		weight.put(ClassId.ghostHunter, 0.4);
		weight.put(ClassId.ghostSentinel, 0.3);
		weight.put(ClassId.stormScreamer, 0.3);
		weight.put(ClassId.spectralMaster, 1.);
		weight.put(ClassId.shillienSaint, -0.6);
		weight.put(ClassId.titan, 0.3);
		weight.put(ClassId.dominator, 0.1);
		weight.put(ClassId.grandKhavatari, 0.2);
		weight.put(ClassId.doomcryer, 0.1);
		weight.put(ClassId.fortuneSeeker, 0.9);
		weight.put(ClassId.maestro, 0.7);
		weight.put(ClassId.doombringer, 0.2);
		weight.put(ClassId.trickster, 0.5);
		weight.put(ClassId.judicator, 0.1);
		weight.put(ClassId.maleSoulhound, 0.3);
		weight.put(ClassId.femaleSoulhound, 0.3);
	}
	
	@Override
	public TaskZoneSettings getSettings()
	{
		return (TaskZoneSettings) super.getSettings();
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("chance"))
		{
			_chance = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		inzone.add(character);
		
		if (getSettings().getTask() == null)
		{
			synchronized (this)
			{
				if (getSettings().getTask() == null)
				{
					getSettings().setTask(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new BuffTask(), _initialDelay, _reuse));
				}
			}
		}
		
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.ALTERED, true);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character.isPlayer() && inzone.contains(character))
		{
			inzone.remove(character);
		}
		
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneId.ALTERED, false);
		}
		
		if (_characterList.isEmpty() && (getSettings().getTask() != null))
		{
			getSettings().clear();
		}
	}
	
	protected L2Skill getSkill(int skillId, int skillLvl)
	{
		return SkillHolder.getInstance().getInfo(skillId, skillLvl);
	}
	
	protected int getBuffLevel(L2Character character)
	{
		if (character.getParty() == null)
		{
			return 0;
		}
		
		L2Party party = character.getParty();
		
		if (party.getMemberCount() < 4)
		{
			return 0;
		}
		
		for (L2PcInstance p : party.getMembers())
		{
			if ((p.getLevel() < 80) || (p.getClassId().level() != 3))
			{
				return 0;
			}
		}
		
		double points = 0;
		int count = party.getMemberCount();
		
		for (L2PcInstance p : party.getMembers())
		{
			points += weight.get(p.getClassId());
		}
		
		return (int) Math.max(0, Math.min(3, Math.round(points * getCoefficient(count))));
	}
	
	private double getCoefficient(int count)
	{
		double cf;
		switch (count)
		{
			case 1:
				cf = 0.7;
				break;
			case 4:
				cf = 0.7;
				break;
			case 5:
				cf = 0.75;
				break;
			case 6:
				cf = 0.8;
				break;
			case 7:
				cf = 0.85;
				break;
			case 8:
				cf = 0.9;
				break;
			case 9:
				cf = 0.95;
				break;
			default:
				cf = 1;
		}
		return cf;
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	public int getChance()
	{
		return _chance;
	}
	
	public void setZoneEnabled(boolean val)
	{
		_enabled = val;
	}
	
	protected final class BuffTask implements Runnable
	{
		@Override
		public void run()
		{
			if (isEnabled())
			{
				for (L2Character izp : inzone)
				{
					if (getBuffLevel(izp) > 0)
					{
						if ((izp != null) && !izp.isDead())
						{
							if (Rnd.get(100) < getChance())
							{
								L2Skill skill = getSkill(6885, getBuffLevel(izp));
								if (skill != null)
								{
									if (izp.getFirstEffect(6885) == null)
									{
										skill.getEffects(izp, izp);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}