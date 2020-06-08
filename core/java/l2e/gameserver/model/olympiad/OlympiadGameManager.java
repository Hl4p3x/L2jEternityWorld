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
package l2e.gameserver.model.olympiad;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.zone.type.L2OlympiadStadiumZone;

public class OlympiadGameManager implements Runnable
{
	private static final Logger _log = Logger.getLogger(OlympiadGameManager.class.getName());

	private volatile boolean _battleStarted = false;
	private final OlympiadGameTask[] _tasks;

	protected OlympiadGameManager()
	{
		final Collection<L2OlympiadStadiumZone> zones = ZoneManager.getInstance().getAllZones(L2OlympiadStadiumZone.class);
		if ((zones == null) || zones.isEmpty())
			throw new Error("No olympiad stadium zones defined !");

		_tasks = new OlympiadGameTask[zones.size()];
		int i = 0;
		for (L2OlympiadStadiumZone zone : zones)
			_tasks[i++] = new OlympiadGameTask(zone);

		_log.log(Level.INFO, "Olympiad System: Loaded " + _tasks.length + " stadiums.");
	}
	
	public static final OlympiadGameManager getInstance()
	{
		return SingletonHolder._instance;
	}

	protected final boolean isBattleStarted()
	{
		return _battleStarted;
	}

	protected final void startBattle()
	{
		_battleStarted = true;
	}

	@Override
	public final void run()
	{
		if (Olympiad.getInstance().isOlympiadEnd())
			return;
		
		if (Olympiad.getInstance().inCompPeriod())
		{
			OlympiadGameTask task;
			AbstractOlympiadGame newGame;

			List<List<Integer>> readyClassed = OlympiadManager.getInstance().hasEnoughRegisteredClassed();
			boolean readyNonClassed = OlympiadManager.getInstance().hasEnoughRegisteredNonClassed();
			boolean readyTeams = OlympiadManager.getInstance().hasEnoughRegisteredTeams();

			
			if ((readyClassed != null) || readyNonClassed || readyTeams)
			{
				for (int i = 0; i < _tasks.length; i++)
				{
					task = _tasks[i];
					synchronized (task)
					{
						if (!task.isRunning())
						{
							if ((readyClassed != null || readyTeams) && (i % 2) == 0)
							{
								if (readyTeams && (i % 4) == 0)
								{
									newGame = OlympiadGameTeams.createGame(i, OlympiadManager.getInstance().getRegisteredTeamsBased());
									if (newGame != null)
									{
										task.attachGame(newGame);
										continue;
									}
									readyTeams = false;
								}

								if (readyClassed != null)
								{
									newGame = OlympiadGameClassed.createGame(i, readyClassed);
									if (newGame != null)
									{
										task.attachGame(newGame);
										continue;
									}
									readyClassed = null;
								}
							}

							if (readyNonClassed)
							{
								newGame = OlympiadGameNonClassed.createGame(i, OlympiadManager.getInstance().getRegisteredNonClassBased());
								if (newGame != null)
								{
									task.attachGame(newGame);
									continue;
								}
								readyNonClassed = false;
							}
						}
					}

					if (readyClassed == null && !readyNonClassed && !readyTeams)
						break;
				}
			}
		}
		else
		{
			if (isAllTasksFinished())
			{
				OlympiadManager.getInstance().clearRegistered();				
				_battleStarted = false;
				_log.log(Level.INFO, "Olympiad System: All current games finished.");
			}
		}
	}

	public final boolean isAllTasksFinished()
	{
		for (OlympiadGameTask task : _tasks)
		{
			if (task.isRunning())
				return false;
		}
		return true;
	}

	public final OlympiadGameTask getOlympiadTask(int id)
	{
		if (id < 0 || id >= _tasks.length)
			return null;

		return _tasks[id];
	}

	public final int getNumberOfStadiums()
	{
		return _tasks.length;
	}

	public final void notifyCompetitorDamage(L2PcInstance player, int damage)
	{
		if (player == null)
			return;

		final int id = player.getOlympiadGameId();
		if ((id < 0) || (id >= _tasks.length))
			return;

		final AbstractOlympiadGame game = _tasks[id].getGame();
		if (game != null)
			game.addDamage(player, damage);
	}
	
	private static class SingletonHolder
	{
		protected static final OlympiadGameManager _instance = new OlympiadGameManager();
	}
}