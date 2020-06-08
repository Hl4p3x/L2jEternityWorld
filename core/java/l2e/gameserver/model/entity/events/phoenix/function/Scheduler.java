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
package l2e.gameserver.model.entity.events.phoenix.function;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import l2e.gameserver.model.entity.events.phoenix.Configuration;
import l2e.gameserver.model.entity.events.phoenix.container.EventContainer;
import l2e.gameserver.model.entity.events.phoenix.io.Out;
import l2e.gameserver.model.entity.events.phoenix.model.ManagerNpcHtml;

public class Scheduler
{
	protected class SchedulerTask implements Runnable
	{
		@Override
		public void run()
		{
			currentCal = Calendar.getInstance();
			Integer hour = currentCal.get(Calendar.HOUR_OF_DAY);
			Integer mins = currentCal.get(Calendar.MINUTE);
			
			for (Integer[] element : scheduleList)
			{
				if (element[0].equals(hour) && element[1].equals(mins))
				{
					if (element[2].equals(0))
					{
						EventContainer.getInstance().createRandomEvent();
					}
					else
					{
						EventContainer.getInstance().createEvent(element[2]);
					}
				}
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final Scheduler _instance = new Scheduler();
	}
	
	public static final Scheduler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public Calendar currentCal;
	
	public FastList<Integer[]> scheduleList;
	
	protected Scheduler()
	{
		scheduleList = new FastList<>();
		makeList();
		Out.tpmScheduleGeneralAtFixedRate(new SchedulerTask(), 1, 50000);
	}
	
	@SuppressWarnings("resource")
	private String loadFile()
	{
		String beolvasott = "";
		String str;
		try
		{
			BufferedReader bf = new BufferedReader(new FileReader("config/events/phoenix_events/EventScheduler.txt"));
			while ((str = bf.readLine()) != null)
			{
				beolvasott += str;
			}
		}
		catch (IOException e)
		{
			System.out.println("Error on reading the scheduler file!");
			return "";
		}
		return beolvasott;
	}
	
	private void makeList()
	{
		StringTokenizer st = new StringTokenizer(loadFile(), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer sti = new StringTokenizer(st.nextToken(), ":");
			Integer ora = Integer.parseInt(sti.nextToken());
			Integer perc = Integer.parseInt(sti.nextToken());
			Integer event = Integer.parseInt(sti.nextToken());
			scheduleList.add(new Integer[]
			{
				ora,
				perc,
				event
			});
		}
	}
	
	public void scheduleList(Integer player)
	{
		TextBuilder builder = new TextBuilder();
		
		int count = 0;
		
		builder.append("<center><table width=470 bgcolor=4f4f4f><tr><td width=70><font color=ac9775>Scheduler</font></td></tr></table><br>");
		
		for (Integer[] event : scheduleList)
		{
			count++;
			builder.append("<center><table width=270 " + ((count % 2) == 1 ? "" : "bgcolor=4f4f4f") + "><tr><td width=30><font color=ac9775>" + (event[0] < 10 ? "0" + event[0] : event[0]) + ":" + (event[1] < 10 ? "0" + event[1] : event[1]) + "</font></td><td width=210><font color=9f9f9f>" + Configuration.getInstance().getString(event[2], "eventName") + "</font></td></tr></table>");
		}
		Out.html(player, new ManagerNpcHtml(builder.toString()).string());
	}
}