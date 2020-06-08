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
package l2e.gameserver.model.entity.underground_coliseum;

public class UCRunningTask implements Runnable
{
	private UCArena _arena;

	public UCRunningTask(UCArena arena)
	{
		_arena = arena;
	}

	@Override
	public void run()
	{
		_arena.removeTeam();
		_arena.checkLost(false);

		UCTeam winnerTeam = null;
		for(UCTeam team : _arena.getTeams())
		{
			if(team.getStatus() == UCTeam.WIN)
				winnerTeam = team;
		}

		if(winnerTeam != null)
			winnerTeam.setStatus(UCTeam.NOT_DECIDED);

		UCPoint[] pointzor = _arena.getPoints();
		for (UCPoint point : pointzor)
			point.actionDoors(false);
		
		if(winnerTeam != null && _arena.getWaitingList().size() >= 1)
		{
			UCTeam other = winnerTeam.getOtherTeam();
			UCWaiting otherWaiting = _arena.getWaitingList().get(0);
			other.setParty(otherWaiting.getParty());
			other.setRegisterTime(otherWaiting.getRegisterMillis());
			_arena.getWaitingList().remove(0);
			winnerTeam.splitMembersAndTeleport();
			other.splitMembersAndTeleport();

			_arena.startFight();
			return;
		}

		while (true)
		{
			if(_arena.getWaitingList().size() >= 2)
			{
				int i = 0;
				UCWaiting teamWaiting = null;
				for(UCTeam team : _arena.getTeams())
				{
					teamWaiting = _arena.getWaitingList().get(i);
					team.setParty(teamWaiting.getParty());
					team.setRegisterTime(teamWaiting.getRegisterMillis());
					_arena.getWaitingList().remove(i);
					team.splitMembersAndTeleport();
					i++;
				}

				_arena.startFight();
				break;
			}
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				System.out.println("UC infinite while loop error.");
			}
		}
	}
}