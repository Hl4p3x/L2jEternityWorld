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
package l2e.gameserver.model.entity.events;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import l2e.Config;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.customs.CustomMessage;

public class TvTRoundManager
{
	protected static final Logger _log = Logger.getLogger(TvTRoundManager.class.getName());
	
	private TvTRoundStartTask _task;
	
	protected TvTRoundManager()
	{
		if (Config.TVT_ROUND_EVENT_ENABLED)
		{
			TvTRoundEvent.init();
			
			scheduleEventStart();
			_log.info("TvT Round Event Engine: Started.");
		}
		else
		{
			_log.info("TvT Round Event Engine: Disabled by config.");
		}
	}
	
	public static TvTRoundManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void scheduleEventStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;
			for (String timeOfDay : Config.TVT_ROUND_EVENT_INTERVAL)
			{
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				}
				if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
				{
					nextStartTime = testStartTime;
				}
			}
			if (nextStartTime != null)
			{
				_task = new TvTRoundStartTask(nextStartTime.getTimeInMillis());
				ThreadPoolManager.getInstance().executeTask(_task);
			}
		}
		catch (Exception e)
		{
			_log.warning("TvTRoundEventEngine[TvTRoundManager.scheduleEventStart()]: Error figuring out a start time. Check TvTRoundEventInterval in config file.");
		}
	}
	
	public void startReg()
	{
		if (!TvTRoundEvent.startParticipation())
		{
			CustomMessage msg = new CustomMessage("TvTRoundEvent.WAS_CANCEL", true);
			Announcements.getInstance().announceToAll(msg);
			_log.warning("TvTRoundEventEngine[TvTRoundManager.run()]: Error spawning event npc for participation.");
			
			scheduleEventStart();
		}
		else
		{
			CustomMessage msg = new CustomMessage("TvTRoundEvent.OPEN_REG", true);
			msg.add(Config.TVT_ROUND_EVENT_PARTICIPATION_TIME);
			Announcements.getInstance().announceToAll(msg);
			
			_task.setStartTime(System.currentTimeMillis() + 60000L * Config.TVT_ROUND_EVENT_PARTICIPATION_TIME);
			ThreadPoolManager.getInstance().executeTask(_task);
		}
	}
	
	public void startFirstRound()
	{
		if (!TvTRoundEvent.startEvent())
		{
			CustomMessage msg = new CustomMessage("TvTRoundEvent.EVENT_CANCEL", true);
			Announcements.getInstance().announceToAll(msg);
			_log.info("TvTRoundEventEngine[TvTRoundManager.run()]: Lack of registration, abort event.");
			
			scheduleEventStart();
		}
		else
		{
			CustomMessage msg1 = new CustomMessage("TvTRoundEvent.START", true);
			TvTRoundEvent.sysMsgToAllParticipants(msg1.toString());

			CustomMessage msg2 = new CustomMessage("TvTRoundEvent.TELE_TO_ARENA", true);
			msg2.add(Config.TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY);
			TvTRoundEvent.sysMsgToAllParticipants(msg2.toString());

			_task.setStartTime(System.currentTimeMillis() + 60000L * Config.TVT_ROUND_EVENT_FIRST_FIGHT_RUNNING_TIME);
			ThreadPoolManager.getInstance().executeTask(_task);
		}
	}
	
	public void startSecondRound()
	{
		if (!TvTRoundEvent.startFights())
		{
			CustomMessage msg = new CustomMessage("TvTRoundEvent.SECOND_ROUND_CANCEL", true);
			Announcements.getInstance().announceToAll(msg);
			_log.info("TvTRoundEventEngine[TvTRoundManager.run()]: There aren't enough participants, abort round.");
			TvTRoundEvent.setSecondRoundFinished();
			
			ThreadPoolManager.getInstance().executeTask(_task);
		}
		else
		{
			TvTRoundEvent.setInSecondRound();

			CustomMessage msg1 = new CustomMessage("TvTRoundEvent.START_SECOND", true);
			TvTRoundEvent.sysMsgToAllParticipants(msg1.toString());

			CustomMessage msg2 = new CustomMessage("TvTRoundEvent.TELE_TO_ARENA", true);
			msg2.add(Config.TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY);
			TvTRoundEvent.sysMsgToAllParticipants(msg2.toString());

			_task.setStartTime(System.currentTimeMillis() + 60000L * Config.TVT_ROUND_EVENT_SECOND_FIGHT_RUNNING_TIME);
			ThreadPoolManager.getInstance().executeTask(_task);
		}
	}
	
	public void startThirdRound()
	{
		if (!TvTRoundEvent.startFights())
		{
			CustomMessage msg1 = new CustomMessage("TvTRoundEvent.THIRD_ROUND_CANCEL", true);
			Announcements.getInstance().announceToAll(msg1);

			CustomMessage msg2 = new CustomMessage("TvTRoundEvent.NO_WINNERS", true);
			Announcements.getInstance().announceToAll(msg2);

			_log.info("TvTRoundEventEngine[TvTRoundManager.run()]: There aren't enough participants, abort event.");
			TvTRoundEvent.setIsWithoutWinners();
			
			ThreadPoolManager.getInstance().executeTask(_task);
		}
		else
		{
			TvTRoundEvent.setInThirdRound();

			CustomMessage msg1 = new CustomMessage("TvTRoundEvent.START_THIRD", true);
			TvTRoundEvent.sysMsgToAllParticipants(msg1.toString());

			CustomMessage msg2 = new CustomMessage("TvTRoundEvent.TELE_TO_ARENA", true);
			msg2.add(Config.TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY);
			TvTRoundEvent.sysMsgToAllParticipants(msg2.toString());

			_task.setStartTime(System.currentTimeMillis() + 60000L * Config.TVT_ROUND_EVENT_THIRD_FIGHT_RUNNING_TIME);
			ThreadPoolManager.getInstance().executeTask(_task);
		}
	}
	
	public void endFirstRound()
	{
		Announcements.getInstance().announceToAll(TvTRoundEvent.calculatePoints());
		if (Config.TVT_ROUND_EVENT_STOP_ON_TIE && TvTRoundEvent.getRoundTie() >= Config.TVT_ROUND_EVENT_MINIMUM_TIE)
		{
			TvTRoundEvent.stopEvent();

			CustomMessage msg = new CustomMessage("TvTRoundEvent.TO_INACTIVITY", true);
			Announcements.getInstance().announceToAll(msg);
			
			scheduleEventStart();
		}
		else
		{
			TvTRoundEvent.cleanTeamsPoints();
			TvTRoundEvent.setFirstRoundFinished();
			
			ThreadPoolManager.getInstance().executeTask(_task);
		}
	}
	
	public void endSecondRound()
	{
		Announcements.getInstance().announceToAll(TvTRoundEvent.calculatePoints());
		if (Config.TVT_ROUND_EVENT_STOP_ON_TIE && TvTRoundEvent.getRoundTie() >= Config.TVT_ROUND_EVENT_MINIMUM_TIE)
		{
			TvTRoundEvent.stopEvent();

			CustomMessage msg = new CustomMessage("TvTRoundEvent.TO_INACTIVITY", true);
			Announcements.getInstance().announceToAll(msg);
			
			scheduleEventStart();
		}
		else if (Config.TVT_ROUND_EVENT_REWARD_ON_SECOND_FIGHT_END && TvTRoundEvent.checkForPossibleWinner())
		{
			Announcements.getInstance().announceToAll(TvTRoundEvent.calculateRewards());

			CustomMessage msg = new CustomMessage("TvTRoundEvent.TELE_BACK", true);
			msg.add(Config.TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY);
			TvTRoundEvent.sysMsgToAllParticipants(msg.toString());

			TvTRoundEvent.stopEvent();
			scheduleEventStart();
		}
		else
		{
			TvTRoundEvent.cleanTeamsPoints();
			TvTRoundEvent.setSecondRoundFinished();
			
			ThreadPoolManager.getInstance().executeTask(_task);
		}
	}
	
	public void endThirdRound()
	{
		Announcements.getInstance().announceToAll(TvTRoundEvent.calculatePoints());
		if (Config.TVT_ROUND_EVENT_STOP_ON_TIE && TvTRoundEvent.getRoundTie() >= Config.TVT_ROUND_EVENT_MINIMUM_TIE)
		{
			TvTRoundEvent.stopEvent();

			CustomMessage msg = new CustomMessage("TvTRoundEvent.TO_INACTIVITY", true);
			Announcements.getInstance().announceToAll(msg);
			
			scheduleEventStart();
		}
		else
		{
			TvTRoundEvent.cleanTeamsPoints();
			TvTRoundEvent.setThirdRoundFinished();
			
			ThreadPoolManager.getInstance().executeTask(_task);
		}
	}
	
	public void endEvent()
	{
		if (TvTRoundEvent.isWithoutWinners())
		{
			TvTRoundEvent.stopEvent();
			scheduleEventStart();
		}
		else
		{
			Announcements.getInstance().announceToAll(TvTRoundEvent.calculateRewards());

			CustomMessage msg = new CustomMessage("TvTRoundEvent.TELE_BACK", true);
			msg.add(Config.TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY);
			TvTRoundEvent.sysMsgToAllParticipants(msg.toString());

			TvTRoundEvent.stopEvent();
			scheduleEventStart();
		}
	}
	
	public void skipDelay()
	{
		if (_task.nextRun.cancel(false))
		{
			_task.setStartTime(System.currentTimeMillis());
			ThreadPoolManager.getInstance().executeTask(_task);
		}
	}
	
	class TvTRoundStartTask implements Runnable
	{
		private long _startTime;
		public ScheduledFuture<?> nextRun;
		
		public TvTRoundStartTask(long startTime)
		{
			_startTime = startTime;
		}
		
		public void setStartTime(long startTime)
		{
			_startTime = startTime;
		}
		
		@Override
		public void run()
		{
			int delay = (int) Math.round((_startTime - System.currentTimeMillis()) / 1000.0);
			
			if (delay > 0)
			{
				announce(delay);
			}
			
			int nextMsg = 0;
			if (delay > 3600)
			{
				nextMsg = delay - 3600;
			}
			else if (delay > 1800)
			{
				nextMsg = delay - 1800;
			}
			else if (delay > 900)
			{
				nextMsg = delay - 900;
			}
			else if (delay > 600)
			{
				nextMsg = delay - 600;
			}
			else if (delay > 300)
			{
				nextMsg = delay - 300;
			}
			else if (delay > 60)
			{
				nextMsg = delay - 60;
			}
			else if (delay > 5)
			{
				nextMsg = delay - 5;
			}
			else if (delay > 0)
			{
				nextMsg = delay;
			}
			else
			{
				if (TvTRoundEvent.isInactive())
				{
					startReg();
				}
				else if (TvTRoundEvent.isParticipating())
				{
					startFirstRound();
				}
				else if (TvTRoundEvent.isInFirstRound())
				{
					endFirstRound();
				}
				else if (TvTRoundEvent.isFRoundFinished())
				{
					startSecondRound();
				}
				else if (TvTRoundEvent.isInSecondRound())
				{
					endSecondRound();
				}
				else if (TvTRoundEvent.isSRoundFinished())
				{
					startThirdRound();
				}
				else if (TvTRoundEvent.isInThirdRound())
				{
					endThirdRound();
				}
				else
				{
					endEvent();
				}
			}
			
			if (delay > 0)
			{
				nextRun = ThreadPoolManager.getInstance().scheduleGeneral(this, nextMsg * 1000);
			}
		}
		
		private void announce(long time)
		{
			if (time >= 3600 && time % 3600 == 0)
			{
				if (TvTRoundEvent.isParticipating())
				{
					CustomMessage msg = new CustomMessage("TvTRoundEvent.UNTIL_HOURS", true);
					msg.add((time / 60 / 60));
					Announcements.getInstance().announceToAll(msg);
				}
				else if (TvTRoundEvent.isStarted())
				{
					CustomMessage msg = new CustomMessage("TvTRoundEvent.UNTIL_HOURS_FINISH", true);
					msg.add((time / 60 / 60));
					Announcements.getInstance().announceToAll(msg);
				}
			}
			else if (time >= 60)
			{
				if (TvTRoundEvent.isParticipating())
				{
					CustomMessage msg = new CustomMessage("TvTRoundEvent.UNTIL_MIN", true);
					msg.add((time / 60));
					Announcements.getInstance().announceToAll(msg);
				}
				else if (TvTRoundEvent.isStarted())
				{
					CustomMessage msg = new CustomMessage("TvTRoundEvent.UNTIL_MIN_FINISH", true);
					msg.add((time / 60));
					Announcements.getInstance().announceToAll(msg);
				}
			}
			else
			{
				if (TvTRoundEvent.isParticipating())
				{
					CustomMessage msg = new CustomMessage("TvTRoundEvent.UNTIL_SEC", true);
					msg.add(time);
					Announcements.getInstance().announceToAll(msg);
				}
				else if (TvTRoundEvent.isStarted())
				{
					CustomMessage msg = new CustomMessage("TvTRoundEvent.UNTIL_SEC_FINISH", true);
					msg.add(time);
					Announcements.getInstance().announceToAll(msg);
				}
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final TvTRoundManager _instance = new TvTRoundManager();
	}
}