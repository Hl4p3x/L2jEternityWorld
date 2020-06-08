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

import javolution.util.FastMap;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.entity.events.BW;
import l2e.gameserver.model.entity.events.CTF;
import l2e.gameserver.model.entity.events.DM;
import l2e.gameserver.model.entity.events.FunEvent;
import l2e.gameserver.model.entity.events.FunEvent.State;
import l2e.gameserver.model.entity.events.TW;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class FunEventsManager
{
	private final FastMap<String, FunEvent> _events = new FastMap<>();
	
	protected FunEventsManager()
	{
		loadEvents();
	}
	
	public void loadEvents()
	{
		_events.clear();
		_events.put("CTF", new CTF());
		_events.put("BW", new BW());
		_events.put("DM", new DM());
		_events.put("TW", new TW());
	}
	
	public FunEvent getEvent(String eventName)
	{
		return _events.get(eventName);
	}
	
	public FunEvent getEvent(int eventId)
	{
		for (FunEvent event : _events.values())
		{
			if (event.EVENT_ID == eventId)
			{
				return event;
			}
		}
		return null;
	}
	
	public boolean isFightingInEvent(L2PcInstance player)
	{
		if (player.getEventName().equals(""))
		{
			return false;
		}
		for (FunEvent event : _events.values())
		{
			if (player.getEventName().equals(event.EVENT_NAME) && (event.getState() == State.FIGHTING))
			{
				return true;
			}
		}
		return false;
	}
	
	public void autoStartEvents()
	{
		for (FunEvent event : _events.values())
		{
			event.autoStart();
		}
	}
	
	public void abortEvents()
	{
		for (FunEvent event : _events.values())
		{
			event.abortEvent();
		}
	}
	
	public void abortEvent(String eventName)
	{
		FunEvent event = _events.get(eventName);
		if (event != null)
		{
			event.abortEvent();
		}
	}
	
	public void startEvent(String eventName)
	{
		FunEvent event = _events.get(eventName);
		if (event != null)
		{
			event.startEvent();
		}
	}
	
	public String getEventsInfo(String lang)
	{
		String info = "";
		for (FunEvent event : _events.values())
		{
			info += event.getInfo(lang);
		}
		return info;
	}
	
	public NpcHtmlMessage getChatWindow(L2PcInstance player, String eventName)
	{
		NpcHtmlMessage mes = null;
		FunEvent event = _events.get(eventName);
		if (event != null)
		{
			mes = event.getChatWindow(player);
		}
		return mes;
	}
	
	public void notifyJoinCursed(L2PcInstance player)
	{
		FunEvent event = _events.get(player.getEventName());
		if ((event != null) && (event.getState() == State.PARTICIPATING) && !event.EVENT_JOIN_CURSED)
		{
			event.removePlayer(player);
			player.sendMessage("Your registration in event " + event.EVENT_NAME + " canceled.");
		}
	}
	
	public void notifyLevelChanged(L2PcInstance player)
	{
		FunEvent event = _events.get(player.getEventName());
		if ((event != null) && (event.getState() == State.PARTICIPATING) && ((player.getLevel() > event.EVENT_PLAYER_LEVEL_MAX) || (player.getLevel() < event.EVENT_PLAYER_LEVEL_MIN)))
		{
			event.removePlayer(player);
			player.sendMessage("Your registration in event " + event.EVENT_NAME + " canceled.");
		}
	}
	
	public boolean notifyPlayerKilled(L2PcInstance player, L2PcInstance killer)
	{
		FunEvent event = null;
		if (player.isFightingInEvent() && player.isInSameEvent(killer) && !player.isInSameTeam(killer))
		{
			event = _events.get(player.getEventName());
			if ((event != null) && (event.getState() == State.FIGHTING))
			{
				return event.onPlayerDie(player, killer);
			}
		}
		else if (player.isFightingInTW() && killer.isFightingInTW())
		{
			event = _events.get("TW");
			if ((event != null) && (event.getState() == State.FIGHTING))
			{
				return event.onPlayerDie(player, killer);
			}
		}
		return true;
	}
	
	public void notifyPlayerLogout(L2PcInstance player)
	{
		FunEvent event = null;
		if (player.isFightingInEvent())
		{
			event = _events.get(player.getEventName());
			if ((event != null) && (event.getState() != State.INACTIVE))
			{
				event.onPlayerLogout(player);
			}
		}
		else if (player.isFightingInTW())
		{
			event = _events.get("TW");
			if ((event != null) && (event.getState() != State.INACTIVE))
			{
				event.onPlayerLogout(player);
			}
		}
	}
	
	public void notifyPlayerLogin(L2PcInstance player)
	{
		if (player.isFightingInTW())
		{
			FunEvent event = _events.get("TW");
			if ((event != null) && (event.getState() != State.INACTIVE))
			{
				event.onPlayerLogin(player);
			}
		}
		else
		{
			for (FunEvent event : _events.values())
			{
				event.onPlayerLogin(player);
			}
		}
	}
	
	public static FunEventsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FunEventsManager _instance = new FunEventsManager();
	}
}