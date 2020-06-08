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
package l2e.gameserver.model.skills.l2skills;

import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.data.xml.ExperienceParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2MerchantSummonInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2ServitorInstance;
import l2e.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.skills.L2Skill;

public class L2SkillSummon extends L2Skill
{
	private final float _expPenalty;
	
	private final int _summonTotalLifeTime;
	private final int _summonTimeLostIdle;
	private final int _summonTimeLostActive;
	
	private final int _itemConsumeTime;
	private final int _itemConsumeOT;
	private final int _itemConsumeIdOT;
	private final int _itemConsumeSteps;
	private final boolean _inheritElementals;
	private final double _elementalSharePercent;
	
	public L2SkillSummon(StatsSet set)
	{
		super(set);
		
		_expPenalty = set.getFloat("expPenalty", 0.f);
		
		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000);
		_summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
		_summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);
		
		_itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
		_itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
		_itemConsumeTime = set.getInteger("itemConsumeTime", 0);
		_itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
		
		_inheritElementals = set.getBool("inheritElementals", false);
		_elementalSharePercent = set.getDouble("inheritPercent", 1);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if ((caster == null) || caster.isAlikeDead() || !caster.isPlayer())
		{
			return;
		}
		
		final L2PcInstance activeChar = caster.getActingPlayer();
		if (getNpcId() <= 0)
		{
			activeChar.sendMessage("Summon skill " + getId() + " not implemented yet.");
			return;
		}
		
		final L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(getNpcId());
		if (summonTemplate == null)
		{
			_log.warning("Summon attempt for nonexisting NPC ID:" + getNpcId() + ", skill ID:" + getId());
			return;
		}
		
		final int id = IdFactory.getInstance().getNextId();
		L2ServitorInstance summon;
		if (summonTemplate.isType("L2SiegeSummon"))
		{
			summon = new L2SiegeSummonInstance(id, summonTemplate, activeChar, this);
		}
		else if (summonTemplate.isType("L2MerchantSummon"))
		{
			summon = new L2MerchantSummonInstance(id, summonTemplate, activeChar, this);
		}
		else
		{
			summon = new L2ServitorInstance(id, summonTemplate, activeChar, this);
		}
		
		summon.setName(summonTemplate.getName());
		summon.setTitle(activeChar.getName());
		summon.setExpPenalty(_expPenalty);
		summon.setSharedElementals(_inheritElementals);
		summon.setSharedElementalsValue(_elementalSharePercent);
		
		if (summon.getLevel() >= ExperienceParser.getInstance().getMaxPetLevel())
		{
			summon.getStat().setExp(ExperienceParser.getInstance().getExpForLevel(ExperienceParser.getInstance().getMaxPetLevel() - 1));
			_log.warning("Summon (" + summon.getName() + ") NpcID: " + summon.getId() + " has a level above " + ExperienceParser.getInstance().getMaxPetLevel() + ". Please rectify.");
		}
		else
		{
			summon.getStat().setExp(ExperienceParser.getInstance().getExpForLevel(summon.getLevel() % ExperienceParser.getInstance().getMaxPetLevel()));
		}
		
		summon.setCurrentHp(summon.getMaxHp());
		summon.setCurrentMp(summon.getMaxMp());
		summon.setHeading(activeChar.getHeading());
		summon.setRunning();
		if (!(summon instanceof L2MerchantSummonInstance))
		{
			activeChar.setPet(summon);
		}
		summon.spawnMe(activeChar.getX() + 20, activeChar.getY() + 20, activeChar.getZ());
	}
	
	public final int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}
	
	public final int getTimeLostIdle()
	{
		return _summonTimeLostIdle;
	}
	
	public final int getTimeLostActive()
	{
		return _summonTimeLostActive;
	}
	
	public final int getItemConsumeOT()
	{
		return _itemConsumeOT;
	}
	
	public final int getItemConsumeIdOT()
	{
		return _itemConsumeIdOT;
	}
	
	public final int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}
	
	public final int getItemConsumeTime()
	{
		return _itemConsumeTime;
	}
	
	public final float getExpPenalty()
	{
		return _expPenalty;
	}
	
	public final L2NpcTemplate getSummonTemplate()
	{
		L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(getNpcId());
		if (summonTemplate == null)
		{
			_log.warning("Summon attempt for nonexisting NPC ID:" + getNpcId() + ", skill ID:" + this.getId());
		}
		return summonTemplate;
	}
	
	public final boolean getInheritElementals()
	{
		return _inheritElementals;
	}
	
	public final double getElementalSharePercent()
	{
		return _elementalSharePercent;
	}
}