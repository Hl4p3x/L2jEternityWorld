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
package l2e.gameserver.model.quest.AITasks;

import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.L2Skill;

public class SkillSee implements Runnable
{
	private final Quest _quest;
	private final L2Npc _npc;
	private final L2PcInstance _caster;
	private final L2Skill _skill;
	private final L2Object[] _targets;
	private final boolean _isSummon;
	
	public SkillSee(Quest quest, L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		_quest = quest;
		_npc = npc;
		_caster = caster;
		_skill = skill;
		_targets = targets;
		_isSummon = isSummon;
	}
	
	@Override
	public void run()
	{
		String res = null;
		try
		{
			res = _quest.onSkillSee(_npc, _caster, _skill, _targets, _isSummon);
		}
		catch (Exception e)
		{
			_quest.showError(_caster, e);
		}
		_quest.showResult(_caster, res);
	}
}