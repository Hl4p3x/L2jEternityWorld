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
package l2e.gameserver.taskmanager;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2CubicInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.serverpackets.AutoAttackStop;

public class AttackStanceTaskManager
{
	protected static final Logger _log = Logger.getLogger(AttackStanceTaskManager.class.getName());
	
	protected static final Map<L2Character, Long> _attackStanceTasks = new ConcurrentHashMap<>();
	
	protected AttackStanceTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FightModeScheduler(), 0, 1000);
	}
	
	public void addAttackStanceTask(L2Character actor)
	{
		if (actor != null)
		{
			if (actor.isPlayable())
			{
				final L2PcInstance player = actor.getActingPlayer();
				for (L2CubicInstance cubic : player.getCubics())
				{
					if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
					{
						cubic.doAction();
					}
				}
			}
			_attackStanceTasks.put(actor, System.currentTimeMillis());
		}
	}
	
	public void removeAttackStanceTask(L2Character actor)
	{
		if (actor != null)
		{
			if (actor.isSummon())
			{
				actor = actor.getActingPlayer();
			}
			_attackStanceTasks.remove(actor);
		}
	}
	
	public boolean hasAttackStanceTask(L2Character actor)
	{
		if (actor != null)
		{
			if (actor.isSummon())
			{
				actor = actor.getActingPlayer();
			}
			return _attackStanceTasks.containsKey(actor);
		}
		return false;
	}
	
	protected class FightModeScheduler implements Runnable
	{
		@Override
		public void run()
		{
			long current = System.currentTimeMillis();
			try
			{
				final Iterator<Entry<L2Character, Long>> iter = _attackStanceTasks.entrySet().iterator();
				Entry<L2Character, Long> e;
				L2Character actor;
				while (iter.hasNext())
				{
					e = iter.next();
					if ((current - e.getValue()) > 15000)
					{
						actor = e.getKey();
						if (actor != null)
						{
							actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
							actor.getAI().setAutoAttacking(false);
							if (actor.isPlayer() && actor.hasSummon())
							{
								actor.getSummon().broadcastPacket(new AutoAttackStop(actor.getSummon().getObjectId()));
							}
						}
						iter.remove();
					}
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Error in FightModeScheduler: " + e.getMessage(), e);
			}
		}
	}
	
	public static AttackStanceTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AttackStanceTaskManager _instance = new AttackStanceTaskManager();
	}
}