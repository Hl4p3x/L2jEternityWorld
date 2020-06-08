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
package l2e.gameserver.taskmanager.tasks;

import java.util.Calendar;

import l2e.Config;
import l2e.gameserver.data.sql.ClanHolder;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2ClanMember;
import l2e.gameserver.taskmanager.Task;
import l2e.gameserver.taskmanager.TaskManager;
import l2e.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2e.gameserver.taskmanager.TaskTypes;

public class TaskClanLeaderApply extends Task
{
	private static final String NAME = "clanleaderapply";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.DAY_OF_WEEK) == Config.ALT_CLAN_LEADER_DATE_CHANGE)
		{
			for (L2Clan clan : ClanHolder.getInstance().getClans())
			{
				if (clan.getNewLeaderId() != 0)
				{
					final L2ClanMember member = clan.getClanMember(clan.getNewLeaderId());
					if (member == null)
					{
						continue;
					}
					
					clan.setNewLeader(member);
				}
			}
			_log.info(getClass().getSimpleName() + ": launched.");
		}
	}
	
	@Override
	public void initializate()
	{
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", Config.ALT_CLAN_LEADER_HOUR_CHANGE, "");
	}
}