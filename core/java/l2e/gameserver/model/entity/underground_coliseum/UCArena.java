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

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.ExPVPMatchUserDie;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;

public class UCArena
{
	protected static final Logger _log = Logger.getLogger(UCArena.class.getName());
	
	private static final int MINUTES_IN_MILISECONDS = Config.UC_ROUND_TIME * 60 * 1000;
	
	private final int _id;
	private final int _minLevel;
	private final int _maxLevel;
	
	private final UCPoint[] _points = new UCPoint[4];
	private final UCTeam[] _teams = new UCTeam[2];
	
	private ScheduledFuture<?> _taskFuture = null;
	private final List<UCWaiting> _waitingPartys = new FastList<UCWaiting>().shared();
	
	public UCArena(int id, int curator, int min_level, int max_level)
	{
		_id = id;
		_minLevel = min_level;
		_maxLevel = max_level;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getMinLevel()
	{
		return _minLevel;
	}
	
	public int getMaxLevel()
	{
		return _maxLevel;
	}
	
	public void setUCPoint(int index, UCPoint point)
	{
		if (index > 4)
		{
			_log.info("Points can't be bigger than 4.");
			return;
		}
		_points[index] = point;
	}
	
	public void setUCTeam(int index, UCTeam team)
	{
		if (index > 2)
		{
			_log.info("There can't be more than 2 teams.");
			return;
		}
		_teams[index] = team;
	}
	
	public UCTeam[] getTeams()
	{
		return _teams;
	}
	
	public UCPoint[] getPoints()
	{
		return _points;
	}
	
	public List<UCWaiting> getWaitingList()
	{
		return _waitingPartys;
	}
	
	public void switchStatus(boolean start)
	{
		if ((_taskFuture == null) && start)
		{
			runNewTask();
		}
		else
		{
			_taskFuture.cancel(true);
			_taskFuture = null;
			
			checkLost(true);
			
			for (UCTeam team : _teams)
			{
				team.clean(true);
			}
		}
	}
	
	public void runNewTask()
	{
		_taskFuture = ThreadPoolManager.getInstance().scheduleGeneral(new UCRunningTask(this), MINUTES_IN_MILISECONDS);
	}
	
	public void runTaskNow()
	{
		_taskFuture.cancel(true);
		_taskFuture = null;
		UCRunningTask task = new UCRunningTask(this);
		task.run();
	}
	
	public void checkLost(boolean removeWinners)
	{
		UCTeam blueTeam = _teams[0];
		UCTeam redTeam = _teams[1];
		UCTeam winnerTeam = null;
		
		Continue:
		{
			if ((blueTeam.getStatus() == UCTeam.WIN) || (redTeam.getStatus() == UCTeam.WIN))
			{
				winnerTeam = blueTeam.getStatus() == UCTeam.WIN ? blueTeam : redTeam;
				break Continue;
			}
			
			if (blueTeam.getParty() == null)
			{
				redTeam.setStatus(UCTeam.WIN);
				winnerTeam = redTeam;
				break Continue;
			}
			else if (redTeam.getParty() == null)
			{
				blueTeam.setStatus(UCTeam.WIN);
				winnerTeam = blueTeam;
				break Continue;
			}
			
			if (blueTeam.getKillCount() > redTeam.getKillCount())
			{
				blueTeam.setStatus(UCTeam.WIN);
				redTeam.setStatus(UCTeam.FAIL);
				winnerTeam = blueTeam;
				break Continue;
			}
			else if (redTeam.getKillCount() > blueTeam.getKillCount())
			{
				blueTeam.setStatus(UCTeam.FAIL);
				redTeam.setStatus(UCTeam.WIN);
				winnerTeam = redTeam;
				break Continue;
			}
			else if (blueTeam.getKillCount() == redTeam.getKillCount())
			{
				if (blueTeam.getRegisterTime() > redTeam.getRegisterTime())
				{
					blueTeam.setStatus(UCTeam.FAIL);
					redTeam.setStatus(UCTeam.WIN);
					winnerTeam = redTeam;
				}
				else
				{
					blueTeam.setStatus(UCTeam.WIN);
					redTeam.setStatus(UCTeam.FAIL);
					winnerTeam = blueTeam;
				}
				break Continue;
			}
		}
		
		broadcastToAll(new ExPVPMatchUserDie(this));
		
		blueTeam.setLastParty(redTeam.getParty());
		redTeam.setLastParty(blueTeam.getParty());
		
		if (removeWinners && (winnerTeam != null))
		{
			UCPoint[] pointzor = getPoints();
			for (UCPoint point : pointzor)
			{
				point.actionDoors(false);
			}
			winnerTeam.clean(true);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void broadcastToAll(L2GameServerPacket packet)
	{
		for (UCTeam team : getTeams())
		{
			L2Party party = team.getParty();
			if (party != null)
			{
				party.broadcastToPartyMembers(packet);
			}
		}
	}
	
	public void startFight()
	{
		for (UCTeam team : _teams)
		{
			team.spawnTower();
			
			for (L2PcInstance player : team.getParty().getMembers())
			{
				if (player != null)
				{
					player.setTeam(team.getIndex() + 1);
				}
			}
		}
		runNewTask();
	}
	
	public TeleportWhereType getLocation()
	{
		return TeleportWhereType.TOWN;
	}
	
	public void removeTeam()
	{
		for (UCTeam team : _teams)
		{
			for (L2PcInstance player : team.getParty().getMembers())
			{
				if (player == null)
				{
					continue;
				}
				
				player.setTeam(0);
				player.cleanUCStats();
				player.setUCState(L2PcInstance.UC_STATE_NONE);
				if (player.isDead())
				{
					UCTeam.resPlayer(player);
				}
				
				player.teleToLocation(getLocation());
			}
		}
	}
}