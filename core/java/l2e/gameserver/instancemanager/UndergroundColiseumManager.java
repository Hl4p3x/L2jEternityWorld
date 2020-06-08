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
package l2e.gameserver.instancemanager;

import java.io.File;
import java.util.Calendar;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.entity.underground_coliseum.UCArena;
import l2e.gameserver.model.entity.underground_coliseum.UCPoint;
import l2e.gameserver.model.entity.underground_coliseum.UCTeam;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

public class UndergroundColiseumManager
{
	private final static Logger _log = Logger.getLogger(UndergroundColiseumManager.class.getName());
	
	private final TIntObjectHashMap<UCArena> _arenas = new TIntObjectHashMap<>(5);
	private boolean _isStarted;
	private final TIntArrayList _hourList = new TIntArrayList(Config.UC_END_HOUR - Config.UC_START_HOUR);
	
	public static UndergroundColiseumManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected UndergroundColiseumManager()
	{
		load();
		
		for (int i = Config.UC_START_HOUR; i < Config.UC_END_HOUR; i++)
		{
			_hourList.add(i);
		}
		
		_log.info("UndergroundColiseumManager: loaded " + _arenas.size() + " coliseum arenas");
		
		for (int day : Config.UC_WARDAYS)
		{
			runStartTask(day);
		}
	}
	
	private void load()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			File file = new File(Config.DATAPACK_ROOT + "/data/underground_coliseum.xml");
			if (!file.exists())
			{
				_log.info("The underground_coliseum.xml file is missing.");
				return;
			}
			Document doc = factory.newDocumentBuilder().parse(file);
			NamedNodeMap map;
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("arena".equalsIgnoreCase(d.getNodeName()))
						{
							map = d.getAttributes();
							int id = Integer.parseInt(map.getNamedItem("id").getNodeValue());
							int min_level = Integer.parseInt(map.getNamedItem("min_level").getNodeValue());
							int max_level = Integer.parseInt(map.getNamedItem("max_level").getNodeValue());
							int curator = Integer.parseInt(map.getNamedItem("curator").getNodeValue());
							
							UCArena arena = new UCArena(id, curator, min_level, max_level);
							int index = 0;
							int index2 = 0;
							
							for (Node und = d.getFirstChild(); und != null; und = und.getNextSibling())
							{
								if ("tower".equalsIgnoreCase(und.getNodeName()))
								{
									map = und.getAttributes();
									
									int npc_id = Integer.parseInt(map.getNamedItem("npc_id").getNodeValue());
									int x = Integer.parseInt(map.getNamedItem("x").getNodeValue());
									int y = Integer.parseInt(map.getNamedItem("y").getNodeValue());
									int z = Integer.parseInt(map.getNamedItem("z").getNodeValue());
									
									UCTeam team = new UCTeam(index, arena, x, y, z, npc_id);
									
									arena.setUCTeam(index, team);
									
									index++;
								}
								else if ("point".equalsIgnoreCase(und.getNodeName()))
								{
									map = und.getAttributes();
									
									int door1 = Integer.parseInt(map.getNamedItem("door1").getNodeValue());
									int door2 = Integer.parseInt(map.getNamedItem("door2").getNodeValue());
									
									int x = Integer.parseInt(map.getNamedItem("x").getNodeValue());
									int y = Integer.parseInt(map.getNamedItem("y").getNodeValue());
									int z = Integer.parseInt(map.getNamedItem("z").getNodeValue());
									
									UCPoint point = new UCPoint(door1, door2, x, y, z);
									arena.setUCPoint(index2, point);
									
									index2++;
								}
							}
							
							_arenas.put(arena.getId(), arena);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.info("Exception: " + e);
		}
	}
	
	public UCArena getArena(int id)
	{
		return _arenas.get(id);
	}
	
	public void setStarted(boolean started)
	{
		_isStarted = started;
		
		for (UCArena arena : getAllArenas())
		{
			arena.switchStatus(started);
		}
	}
	
	public boolean isStarted()
	{
		return _isStarted;
	}
	
	public UCArena[] getAllArenas()
	{
		return _arenas.values(new UCArena[_arenas.size()]);
	}
	
	public void runStartTask(int calendarDayOfWeek)
	{
		Calendar cal = Calendar.getInstance();
		
		if (cal.get(Calendar.DAY_OF_WEEK) == calendarDayOfWeek)
		{
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			
			if (_hourList.contains(hour))
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new UCRegistrationTask(true, calendarDayOfWeek), 0);
				return;
			}
		}
		
		Calendar startDate = Calendar.getInstance();
		startDate.set(Calendar.DAY_OF_WEEK, calendarDayOfWeek);
		startDate.set(Calendar.HOUR_OF_DAY, Config.UC_START_HOUR);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		
		while (startDate.getTimeInMillis() < System.currentTimeMillis())
		{
			startDate.add(Calendar.WEEK_OF_MONTH, 1);
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new UCRegistrationTask(true, calendarDayOfWeek), startDate.getTimeInMillis() - System.currentTimeMillis());
	}
	
	public void runEndTask(int calendarDayOfWeek)
	{
		Calendar cal = Calendar.getInstance();
		
		if (cal.get(Calendar.DAY_OF_WEEK) == calendarDayOfWeek)
		{
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			
			if (_hourList.contains(hour))
			{
				cal.set(Calendar.HOUR_OF_DAY, Config.UC_END_HOUR);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				
				long tms = cal.getTimeInMillis() - System.currentTimeMillis();
				
				ThreadPoolManager.getInstance().scheduleGeneral(new UCRegistrationTask(false, calendarDayOfWeek), tms);
				
				return;
			}
		}
		
		throw new IllegalArgumentException("Try run End Task from not active time");
	}
	
	public class UCRegistrationTask implements Runnable
	{
		private final boolean _start;
		private final int _calendarDayOfWeek;
		
		public UCRegistrationTask(boolean s, int a)
		{
			_start = s;
			_calendarDayOfWeek = a;
		}
		
		@Override
		public void run()
		{
			setStarted(_start);
			
			if (_start)
			{
				runEndTask(_calendarDayOfWeek);
			}
			else
			{
				runStartTask(_calendarDayOfWeek);
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final UndergroundColiseumManager _instance = new UndergroundColiseumManager();
	}
}