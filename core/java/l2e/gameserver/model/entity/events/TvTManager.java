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

public class TvTManager
{
	protected static final Logger _log = Logger.getLogger(TvTManager.class.getName());
	
	private TvTStartTask _task;
	
	protected TvTManager()
	{
		if (Config.TVT_EVENT_ENABLED)
		{
			TvTEvent.init();
			
			scheduleEventStart();
			_log.info("TvTEventEngine[TvTManager.TvTManager()]: Started.");
		}
		else
		{
			_log.info("TvTEventEngine[TvTManager.TvTManager()]: Engine is disabled.");
		}
	}
	
	public static TvTManager getInstance()
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
			for (String timeOfDay : Config.TVT_EVENT_INTERVAL)
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
				_task = new TvTStartTask(nextStartTime.getTimeInMillis());
				ThreadPoolManager.getInstance().executeTask(_task);
			}
		}
		catch (Exception e)
		{
			_log.warning("TvTEventEngine[TvTManager.scheduleEventStart()]: Error figuring out a start time. Check TvTEventInterval in config file.");
		}
	}
	
	public void startReg()
	{
		if (!TvTEvent.startParticipation())
		{
			CustomMessage msg = new CustomMessage("TVTEvent.WAS_CANCEL", true);
			Announcements.getInstance().announceToAll(msg);
			_log.warning("TvTEventEngine[TvTManager.run()]: Error spawning event npc for participation.");
			
			scheduleEventStart();
		}
		else
		{
			CustomMessage msg = new CustomMessage("TVTEvent.OPEN_REG", true);
			msg.add(Config.TVT_EVENT_PARTICIPATION_TIME);
			Announcements.getInstance().announceToAll(msg);

			_task.setStartTime(System.currentTimeMillis() + 60000L * Config.TVT_EVENT_PARTICIPATION_TIME);
			ThreadPoolManager.getInstance().executeTask(_task);
		}
	}
	
	public void startEvent()
	{
		if (!TvTEvent.startFight())
		{
			CustomMessage msg = new CustomMessage("TVTEvent.EVENT_CANCEL", true);
			Announcements.getInstance().announceToAll(msg);
			_log.info("TvTEventEngine[TvTManager.run()]: Lack of registration, abort event.");
			
			scheduleEventStart();
		}
		else
		{
			CustomMessage msg = new CustomMessage("TVTEvent.TELE_TO_ARENA", true);
			msg.add(Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY);
			Announcements.getInstance().announceToAll(msg);

			_task.setStartTime(System.currentTimeMillis() + 60000L * Config.TVT_EVENT_RUNNING_TIME);
			ThreadPoolManager.getInstance().executeTask(_task);
		}
	}
	
	public void endEvent()
	{
		Announcements.getInstance().announceToAll(TvTEvent.calculateRewards());

		CustomMessage msg = new CustomMessage("TVTEvent.TELE_BACK", true);
		msg.add(Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY);
		TvTEvent.sysMsgToAllParticipants(msg.toString());

		TvTEvent.stopFight();
		scheduleEventStart();
	}
	
	public void skipDelay()
	{
		if (_task.nextRun.cancel(false))
		{
			_task.setStartTime(System.currentTimeMillis());
			ThreadPoolManager.getInstance().executeTask(_task);
		}
	}
	
	class TvTStartTask implements Runnable
	{
		private long _startTime;
		public ScheduledFuture<?> nextRun;
		
		public TvTStartTask(long startTime)
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
				if (TvTEvent.isInactive())
				{
					startReg();
				}
				else if (TvTEvent.isParticipating())
				{
					startEvent();
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
				if (TvTEvent.isParticipating())
				{
					CustomMessage msg = new CustomMessage("TVTEvent.UNTIL_HOURS", true);
					msg.add((time / 60 / 60));
					Announcements.getInstance().announceToAll(msg);
				}
				else if (TvTEvent.isStarted())
				{
					CustomMessage msg = new CustomMessage("TVTEvent.UNTIL_HOURS_FINISH", true);
					msg.add((time / 60 / 60));
					Announcements.getInstance().announceToAll(msg);
				}
			}
			else if (time >= 60)
			{
				if (TvTEvent.isParticipating())
				{
					CustomMessage msg = new CustomMessage("TVTEvent.UNTIL_MIN", true);
					msg.add((time / 60));
					Announcements.getInstance().announceToAll(msg);
				}
				else if (TvTEvent.isStarted())
				{
					CustomMessage msg = new CustomMessage("TVTEvent.UNTIL_MIN_FINISH", true);
					msg.add((time / 60));
					Announcements.getInstance().announceToAll(msg);
				}
			}
			else
			{
				if (TvTEvent.isParticipating())
				{
					CustomMessage msg = new CustomMessage("TVTEvent.UNTIL_SEC", true);
					msg.add(time);
					Announcements.getInstance().announceToAll(msg);
				}
				else if (TvTEvent.isStarted())
				{
					CustomMessage msg = new CustomMessage("TVTEvent.UNTIL_SEC_FINISH", true);
					msg.add(time);
					Announcements.getInstance().announceToAll(msg);
				}
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final TvTManager _instance = new TvTManager();
	}
}