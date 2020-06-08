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

import java.util.HashMap;
import java.util.Map;

import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.interfaces.ISkillsHolder;
import l2e.gameserver.model.skills.L2Skill;

public class PlayerSkillHolder implements ISkillsHolder
{
	private final Map<Integer, L2Skill> _skills = new HashMap<>();
	
	public PlayerSkillHolder(L2PcInstance player)
	{
		for (L2Skill skill : player.getSkills().values())
		{
			if (SkillTreesParser.getInstance().isSkillAllowed(player, skill))
			{
				addSkill(skill);
			}
		}
	}
	
	@Override
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
	
	@Override
	public L2Skill addSkill(L2Skill skill)
	{
		return _skills.put(skill.getId(), skill);
	}
	
	@Override
	public int getSkillLevel(int skillId)
	{
		final L2Skill skill = getKnownSkill(skillId);
		return (skill == null) ? -1 : skill.getLevel();
	}
	
	@Override
	public L2Skill getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}
}