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
package l2e.gameserver.handler.effecthandlers;

import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.stats.Env;

public class SetSkill extends L2Effect
{
	private final int _skillId;
	private final int _skillLvl;
	
	public SetSkill(Env env, EffectTemplate template)
	{
		super(env, template);
		_skillId = template.getParameters().getInteger("skillId", 0);
		_skillLvl = template.getParameters().getInteger("skillLvl", 1);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NONE;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() == null) || !getEffected().isPlayer())
		{
			return false;
		}
		
		final L2Skill skill = SkillHolder.getInstance().getInfo(_skillId, _skillLvl);
		if (skill == null)
		{
			return false;
		}
		getEffected().getActingPlayer().addSkill(skill, true);
		return true;
	}
}