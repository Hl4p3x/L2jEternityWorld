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
package l2e.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;

import l2e.L2DatabaseFactory;
import l2e.gameserver.ThreadPoolManager;

public class TriesResetTask
{
	public TriesResetTask()
	{
	}
	
	public static void getInstance()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement("UPDATE characters SET tries=?");
					
					statement.setInt(1, 3);
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
				}
			}
		}, getValidationTime());
	}
	
	private static long getValidationTime()
	{
		Calendar cld = Calendar.getInstance();
		cld.set(11, 12);
		cld.set(12, 1);
		long time = cld.getTimeInMillis();
		if ((System.currentTimeMillis() - time) <= 0)
		{
			long delay = cld.getTimeInMillis() - System.currentTimeMillis();
			return delay;
		}
		return 0;
	}
}