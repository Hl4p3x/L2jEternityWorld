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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2e.gameserver.scripting.scriptengine.events;

import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.scripting.scriptengine.events.impl.L2Event;

public class SkillUseEvent implements L2Event
{
	private L2Character _caster;
	private L2Skill _skill;
	private L2Character _target;
	private L2Object[] _targets;
	
	public L2Character getCaster()
	{
		return _caster;
	}

	public void setCaster(L2Character caster)
	{
		_caster = caster;
	}

	public L2Object[] getTargets()
	{
		return _targets;
	}

	public void setTargets(L2Object[] targets)
	{
		_targets = targets;
	}

	public L2Skill getSkill()
	{
		return _skill;
	}

	public void setSkill(L2Skill skill)
	{
		_skill = skill;
	}

	public L2Character getTarget()
	{
		return _target;
	}

	public void setTarget(L2Character target)
	{
		_target = target;
	}
}