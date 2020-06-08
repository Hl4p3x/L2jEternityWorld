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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastSet;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.L2WorldRegion;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2EventMapGuardInstance;
import l2e.gameserver.model.actor.instance.L2GuardInstance;

public class KnownListUpdateTaskManager
{
	protected static final Logger _log = Logger.getLogger(KnownListUpdateTaskManager.class.getName());
	
	private static final int FULL_UPDATE_TIMER = 100;
	public static boolean updatePass = true;
	
	public static int _fullUpdateTimer = FULL_UPDATE_TIMER;
	
	protected static final FastSet<L2WorldRegion> _failedRegions = new FastSet<>(1);
	
	protected KnownListUpdateTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new KnownListUpdate(), 1000, Config.KNOWNLIST_UPDATE_INTERVAL);
	}
	
	private class KnownListUpdate implements Runnable
	{
		public KnownListUpdate()
		{
		}
		
		@Override
		public void run()
		{
			try
			{
				boolean failed;
				for (L2WorldRegion regions[] : L2World.getInstance().getAllWorldRegions())
				{
					for (L2WorldRegion r : regions)
					{
						try
						{
							failed = _failedRegions.contains(r);
							if (r.isActive())
							{
								updateRegion(r, ((_fullUpdateTimer == FULL_UPDATE_TIMER) || failed), updatePass);
							}
							if (failed)
							{
								_failedRegions.remove(r);
							}
						}
						catch (Exception e)
						{
							_log.log(Level.WARNING, "KnownListUpdateTaskManager: updateRegion(" + _fullUpdateTimer + "," + updatePass + ") failed for region " + r.getName() + ". Full update scheduled. " + e.getMessage(), e);
							_failedRegions.add(r);
						}
					}
				}
				updatePass = !updatePass;
				
				if (_fullUpdateTimer > 0)
				{
					_fullUpdateTimer--;
				}
				else
				{
					_fullUpdateTimer = FULL_UPDATE_TIMER;
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "", e);
			}
		}
	}
	
	public void updateRegion(L2WorldRegion region, boolean fullUpdate, boolean forgetObjects)
	{
		Collection<L2Object> vObj = region.getVisibleObjects().values();
		for (L2Object object : vObj)
		{
			if ((object == null) || !object.isVisible())
			{
				continue;
			}
			
			final boolean aggro = (Config.GUARD_ATTACK_AGGRO_MOB && (object instanceof L2GuardInstance)) || (object instanceof L2EventMapGuardInstance) || ((object instanceof L2Attackable) && (((L2Attackable) object).getEnemyClan() != null));
			
			if (forgetObjects)
			{
				object.getKnownList().forgetObjects(aggro || fullUpdate);
				continue;
			}
			for (L2WorldRegion regi : region.getSurroundingRegions())
			{
				if ((object instanceof L2Playable) || (aggro && regi.isActive()) || fullUpdate)
				{
					Collection<L2Object> inrObj = regi.getVisibleObjects().values();
					for (L2Object obj : inrObj)
					{
						if (obj != object)
						{
							object.getKnownList().addKnownObject(obj);
						}
					}
					
				}
				else if (object instanceof L2Character)
				{
					if (regi.isActive())
					{
						Collection<L2Playable> inrPls = regi.getVisiblePlayable().values();
						
						for (L2Object obj : inrPls)
						{
							if (obj != object)
							{
								object.getKnownList().addKnownObject(obj);
							}
							
						}
					}
				}
			}
		}
	}
	
	public static KnownListUpdateTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final KnownListUpdateTaskManager _instance = new KnownListUpdateTaskManager();
	}
}