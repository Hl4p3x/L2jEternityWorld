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
package l2e.gameserver.instancemanager.tasks;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.FourSepulchersManager;

public final class FourSepulchersChangeAttackTimeTask implements Runnable
{
	@Override
	public void run()
	{
		final FourSepulchersManager manager = FourSepulchersManager.getInstance();
		manager.setIsEntryTime(false);
		manager.setIsWarmUpTime(false);
		manager.setIsAttackTime(true);
		manager.setIsCoolDownTime(false);
		
		manager.locationShadowSpawns();
		
		manager.spawnMysteriousBox(31921);
		manager.spawnMysteriousBox(31922);
		manager.spawnMysteriousBox(31923);
		manager.spawnMysteriousBox(31924);
		
		if (!manager.isFirstTimeRun())
		{
			manager.setWarmUpTimeEnd(Calendar.getInstance().getTimeInMillis());
		}
		
		long interval = 0;

		if (manager.isFirstTimeRun())
		{
			for (double min = Calendar.getInstance().get(Calendar.MINUTE); min < manager.getCycleMin(); min++)
			{
				if ((min % 5) == 0)
				{
					final Calendar inter = Calendar.getInstance();
					inter.set(Calendar.MINUTE, (int) min);
					ThreadPoolManager.getInstance().scheduleGeneral(new FourSepulchersManagerSayTask(), inter.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
					break;
				}
			}
		}
		else
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FourSepulchersManagerSayTask(), 5 * 60400);
		}

		if (manager.isFirstTimeRun())
		{
			interval = manager.getAttackTimeEnd() - Calendar.getInstance().getTimeInMillis();
		}
		else
		{
			interval = Config.FS_TIME_ATTACK * 60000L;
		}
		
		manager.setChangeCoolDownTimeTask(ThreadPoolManager.getInstance().scheduleGeneral(new FourSepulchersChangeCoolDownTimeTask(), interval));
		final ScheduledFuture<?> changeAttackTimeTask = manager.getChangeAttackTimeTask();
		
		if (changeAttackTimeTask != null)
		{
			changeAttackTimeTask.cancel(true);
			manager.setChangeAttackTimeTask(null);
		}
	}
}