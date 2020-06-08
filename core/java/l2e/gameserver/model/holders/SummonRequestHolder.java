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
package l2e.gameserver.model.holders;

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;

public class SummonRequestHolder
{
	private final L2PcInstance _target;
	private final L2Skill _skill;
	
	public SummonRequestHolder(L2PcInstance destination, L2Skill skill)
	{
		_target = destination;
		_skill = skill;
	}
	
	public L2PcInstance getTarget()
	{
		return _target;
	}
	
	public L2Skill getSkill()
	{
		return _skill;
	}
}