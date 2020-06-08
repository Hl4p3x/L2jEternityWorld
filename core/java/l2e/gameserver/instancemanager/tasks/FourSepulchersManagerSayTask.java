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

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.FourSepulchersManager;

public final class FourSepulchersManagerSayTask implements Runnable
{
	@Override
	public void run()
	{
		if (FourSepulchersManager.getInstance().isAttackTime())
		{
			final Calendar tmp = Calendar.getInstance();
			tmp.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - FourSepulchersManager.getInstance().getWarmUpTimeEnd());
			if ((tmp.get(Calendar.MINUTE) + 5) < Config.FS_TIME_ATTACK)
			{
				FourSepulchersManager.getInstance().managerSay((byte) tmp.get(Calendar.MINUTE));
				ThreadPoolManager.getInstance().scheduleGeneral(new FourSepulchersManagerSayTask(), 5 * 60000);
			}
			else if ((tmp.get(Calendar.MINUTE) + 5) >= Config.FS_TIME_ATTACK)
			{
				FourSepulchersManager.getInstance().managerSay((byte) 90);
			}
		}
		else if (FourSepulchersManager.getInstance().isEntryTime())
		{
			FourSepulchersManager.getInstance().managerSay((byte) 0);
		}
	}
}