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
package l2e.gameserver.model.quest;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.instance.L2PcInstance;

public class QuestTimer
{
	protected static final Logger _log = Logger.getLogger(QuestTimer.class.getName());
	
	public class ScheduleTimerTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!getIsActive())
			{
				return;
			}
			
			try
			{
				if (!getIsRepeating())
				{
					cancelAndRemove();
				}
				getQuest().notifyEvent(getName(), getNpc(), getPlayer());
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	private boolean _isActive = true;
	private final String _name;
	private final Quest _quest;
	private final L2Npc _npc;
	private final L2PcInstance _player;
	private final boolean _isRepeating;
	private ScheduledFuture<?> _schedular;
	
	public QuestTimer(Quest quest, String name, long time, L2Npc npc, L2PcInstance player, boolean repeating)
	{
		_name = name;
		_quest = quest;
		_player = player;
		_npc = npc;
		_isRepeating = repeating;
		if (repeating)
		{
			_schedular = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ScheduleTimerTask(), time, time); // Prepare auto end task
		}
		else
		{
			_schedular = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time); // Prepare auto end task
		}
	}
	
	public QuestTimer(Quest quest, String name, long time, L2Npc npc, L2PcInstance player)
	{
		this(quest, name, time, npc, player, false);
	}
	
	public QuestTimer(QuestState qs, String name, long time)
	{
		this(qs.getQuest(), name, time, null, qs.getPlayer(), false);
	}

	public void cancel()
	{
		_isActive = false;
		if (_schedular != null)
		{
			_schedular.cancel(false);
		}
	}

	public void cancelAndRemove()
	{
		cancel();
		_quest.removeQuestTimer(this);
	}

	public boolean isMatch(Quest quest, String name, L2Npc npc, L2PcInstance player)
	{
		if ((quest == null) || (name == null))
		{
			return false;
		}
		if ((quest != _quest) || !name.equalsIgnoreCase(getName()))
		{
			return false;
		}
		return ((npc == _npc) && (player == _player));
	}
	
	public final boolean getIsActive()
	{
		return _isActive;
	}
	
	public final boolean getIsRepeating()
	{
		return _isRepeating;
	}
	
	public final Quest getQuest()
	{
		return _quest;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public final L2Npc getNpc()
	{
		return _npc;
	}
	
	public final L2PcInstance getPlayer()
	{
		return _player;
	}
	
	@Override
	public final String toString()
	{
		return _name;
	}
}