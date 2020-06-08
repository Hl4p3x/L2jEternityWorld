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
package l2e.gameserver.model.entity.events.phoenix.container;

import java.util.Random;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.gameserver.model.entity.events.phoenix.AbstractEvent;
import l2e.gameserver.model.entity.events.phoenix.Configuration;
import l2e.gameserver.model.entity.events.phoenix.events.Battlefield;
import l2e.gameserver.model.entity.events.phoenix.events.Bomb;
import l2e.gameserver.model.entity.events.phoenix.events.Domination;
import l2e.gameserver.model.entity.events.phoenix.events.DoubleDomination;
import l2e.gameserver.model.entity.events.phoenix.events.Lucky;
import l2e.gameserver.model.entity.events.phoenix.events.Mutant;
import l2e.gameserver.model.entity.events.phoenix.events.Russian;
import l2e.gameserver.model.entity.events.phoenix.events.Simon;
import l2e.gameserver.model.entity.events.phoenix.events.VIPTvT;
import l2e.gameserver.model.entity.events.phoenix.events.Zombie;

public class EventContainer
{
	private static class SingletonHolder
	{
		protected static final EventContainer _instance = new EventContainer();
	}
	
	public static EventContainer getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected Random rnd = new Random();
	
	private final FastMap<Integer, AbstractEvent> events;
	
	public FastList<Integer> eventIds;
	
	public EventContainer()
	{
		eventIds = new FastList<>();
		events = new FastMap<>();
		
		if (Domination.enabled && Configuration.getInstance().getBoolean(0, "eventEnabled_1"))
		{
			eventIds.add(1);
		}
		if (DoubleDomination.enabled && Configuration.getInstance().getBoolean(0, "eventEnabled_2"))
		{
			eventIds.add(2);
		}
		if (Lucky.enabled && Configuration.getInstance().getBoolean(0, "eventEnabled_3"))
		{
			eventIds.add(3);
		}
		if (Simon.enabled && Configuration.getInstance().getBoolean(0, "eventEnabled_4"))
		{
			eventIds.add(4);
		}
		if (VIPTvT.enabled && Configuration.getInstance().getBoolean(0, "eventEnabled_5"))
		{
			eventIds.add(5);
		}
		if (Zombie.enabled && Configuration.getInstance().getBoolean(0, "eventEnabled_6"))
		{
			eventIds.add(6);
		}
		if (Russian.enabled && Configuration.getInstance().getBoolean(0, "eventEnabled_7"))
		{
			eventIds.add(7);
		}
		if (Bomb.enabled && Configuration.getInstance().getBoolean(0, "eventEnabled_8"))
		{
			eventIds.add(8);
		}
		if (Mutant.enabled && Configuration.getInstance().getBoolean(0, "eventEnabled_9"))
		{
			eventIds.add(9);
		}
		if (Battlefield.enabled && Configuration.getInstance().getBoolean(0, "eventEnabled_10"))
		{
			eventIds.add(10);
		}
	}
	
	public AbstractEvent createEvent(int id)
	{
		if (!eventIds.contains(id))
		{
			return null;
		}
		
		for (AbstractEvent event : events.values())
		{
			if (event.eventId == id)
			{
				return null;
			}
		}
		
		switch (id)
		{
			case 1:
				events.put(events.size() + 1, new Domination(events.size() + 1));
				break;
			case 2:
				events.put(events.size() + 1, new DoubleDomination(events.size() + 1));
				break;
			case 3:
				events.put(events.size() + 1, new Lucky(events.size() + 1));
				break;
			case 4:
				events.put(events.size() + 1, new Simon(events.size() + 1));
				break;
			case 5:
				events.put(events.size() + 1, new VIPTvT(events.size() + 1));
				break;
			case 6:
				events.put(events.size() + 1, new Zombie(events.size() + 1));
				break;
			case 7:
				events.put(events.size() + 1, new Russian(events.size() + 1));
				break;
			case 8:
				events.put(events.size() + 1, new Bomb(events.size() + 1));
				break;
			case 9:
				events.put(events.size() + 1, new Mutant(events.size() + 1));
				break;
			case 10:
				events.put(events.size() + 1, new Battlefield(events.size() + 1));
				break;
		}
		events.get(events.size()).createStatus();
		
		return events.get(events.size());
	}
	
	public AbstractEvent createRandomEvent()
	{
		return createEvent(eventIds.get(rnd.nextInt(eventIds.size())));
	}
	
	public AbstractEvent getEvent(Integer id)
	{
		return events.get(id);
	}
	
	public FastMap<Integer, AbstractEvent> getEventMap()
	{
		return events;
	}
	
	protected FastList<String> getEventNames()
	{
		FastList<String> map = new FastList<>();
		for (AbstractEvent event : events.values())
		{
			map.add(Configuration.getInstance().getString(event.getId(), "eventName"));
		}
		return map;
	}
	
	protected int numberOfEvents()
	{
		return events.size();
	}
	
	public void removeEvent(Integer id)
	{
		events.remove(id);
	}
}