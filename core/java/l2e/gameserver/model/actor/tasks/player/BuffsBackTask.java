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
package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;

public class BuffsBackTask implements Runnable
{
	L2PcInstance buffBackPlayer = null;
	L2Skill buffBackSkill = null;
	
	public BuffsBackTask(L2Skill skill, L2PcInstance player)
	{
		buffBackPlayer = player;
		buffBackSkill = skill;
	}
	
	@Override
	public void run()
	{
		if (!buffBackPlayer.isInOlympiadMode())
		{
			buffBackSkill.getEffects(buffBackPlayer, buffBackPlayer);
		}
	}
}