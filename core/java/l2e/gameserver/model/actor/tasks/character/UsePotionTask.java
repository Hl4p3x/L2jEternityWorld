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
package l2e.gameserver.model.actor.tasks.character;

import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.skills.L2Skill;

public final class UsePotionTask implements Runnable
{
	private final L2Character _character;
	private final L2Skill _skill;
	
	public UsePotionTask(L2Character character, L2Skill skill)
	{
		_character = character;
		_skill = skill;
	}
	
	@Override
	public void run()
	{
		if (_character != null)
		{
			_character.doSimultaneousCast(_skill);
		}
	}
}