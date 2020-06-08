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

public final class FourSepulchersChangeEntryTimeTask implements Runnable
{
	@Override
	public void run()
	{
		final FourSepulchersManager manager = FourSepulchersManager.getInstance();
		manager.setIsEntryTime(true);
		manager.setIsWarmUpTime(false);
		manager.setIsAttackTime(false);
		manager.setIsCoolDownTime(false);
		
		long interval = 0;

		if (manager.isFirstTimeRun())
		{
			interval = manager.getEntrytTimeEnd() - Calendar.getInstance().getTimeInMillis();
		}
		else
		{
			interval = Config.FS_TIME_ENTRY * 60000L;
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new FourSepulchersManagerSayTask(), 0);
		manager.setChangeWarmUpTimeTask(ThreadPoolManager.getInstance().scheduleEffect(new FourSepulchersChangeWarmUpTimeTask(), interval));
		final ScheduledFuture<?> changeEntryTimeTask = manager.getChangeEntryTimeTask();
		
		if (changeEntryTimeTask != null)
		{
			changeEntryTimeTask.cancel(true);
			manager.setChangeEntryTimeTask(null);
		}
	}
}