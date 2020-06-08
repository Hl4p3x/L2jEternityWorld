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
package l2e.gameserver.model;

import java.util.Date;
import java.util.List;

import javolution.util.FastList;
import l2e.gameserver.script.DateRange;
import l2e.gameserver.script.EventDrop;

public class EventDroplist
{
	private static final List<DateDrop> _allNpcDateDrops = new FastList<>();
	
	public static class DateDrop
	{
		protected final DateRange _dateRange;
		private final EventDrop _eventDrop;
		
		public DateDrop(DateRange dateRange, EventDrop eventDrop)
		{
			_dateRange = dateRange;
			_eventDrop = eventDrop;
		}
		
		public EventDrop getEventDrop()
		{
			return _eventDrop;
		}
		
		public DateRange getDateRange()
		{
			return _dateRange;
		}
	}
	
	public void addGlobalDrop(int[] itemIdList, int[] count, int chance, DateRange dateRange)
	{
		_allNpcDateDrops.add(new DateDrop(dateRange, new EventDrop(itemIdList, count[0], count[1], chance)));
	}
	
	public void addGlobalDrop(int itemId, int minCount, int maxCount, int chance, DateRange dateRange)
	{
		_allNpcDateDrops.add(new DateDrop(dateRange, new EventDrop(itemId, minCount, maxCount, chance)));
	}
	
	public void addGlobalDrop(DateRange dateRange, EventDrop eventDrop)
	{
		_allNpcDateDrops.add(new DateDrop(dateRange, eventDrop));
	}
	
	public List<DateDrop> getAllDrops()
	{
		final List<DateDrop> list = new FastList<>();
		final Date currentDate = new Date();
		for (DateDrop drop : _allNpcDateDrops)
		{
			if (drop._dateRange.isWithinRange(currentDate))
			{
				list.add(drop);
			}
		}
		return list;
	}
	
	public static EventDroplist getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventDroplist _instance = new EventDroplist();
	}
}