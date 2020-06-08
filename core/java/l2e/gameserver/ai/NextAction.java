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
package l2e.gameserver.ai;

import java.util.ArrayList;

public class NextAction
{
	private ArrayList<CtrlEvent> _events;
	private ArrayList<CtrlIntention> _intentions;
	private NextActionCallback _callback;
	
	public interface NextActionCallback
	{
		public void doWork();
	}
	
	public NextAction(ArrayList<CtrlEvent> events, ArrayList<CtrlIntention> intentions, NextActionCallback callback)
	{
		_events = events;
		_intentions = intentions;
		setCallback(callback);
	}
	
	public NextAction(CtrlEvent event, CtrlIntention intention, NextActionCallback callback)
	{
		if (_events == null)
		{
			_events = new ArrayList<>();
		}
		
		if (_intentions == null)
		{
			_intentions = new ArrayList<>();
		}
		
		if (event != null)
		{
			_events.add(event);
		}
		
		if (intention != null)
		{
			_intentions.add(intention);
		}
		setCallback(callback);
	}
	
	public void doAction()
	{
		if (_callback != null)
		{
			_callback.doWork();
		}
	}
	
	public ArrayList<CtrlEvent> getEvents()
	{
		if (_events == null)
		{
			_events = new ArrayList<>();
		}
		return _events;
	}
	
	public void setEvents(ArrayList<CtrlEvent> event)
	{
		_events = event;
	}
	
	public void addEvent(CtrlEvent event)
	{
		if (_events == null)
		{
			_events = new ArrayList<>();
		}
		
		if (event != null)
		{
			_events.add(event);
		}
	}
	
	public void removeEvent(CtrlEvent event)
	{
		if (_events == null)
		{
			return;
		}
		_events.remove(event);
	}
	
	public NextActionCallback getCallback()
	{
		return _callback;
	}
	
	public void setCallback(NextActionCallback callback)
	{
		_callback = callback;
	}
	
	public ArrayList<CtrlIntention> getIntentions()
	{
		if (_intentions == null)
		{
			_intentions = new ArrayList<>();
		}
		return _intentions;
	}
	
	public void setIntentions(ArrayList<CtrlIntention> intentions)
	{
		_intentions = intentions;
	}
	
	public void addIntention(CtrlIntention intention)
	{
		if (_intentions == null)
		{
			_intentions = new ArrayList<>();
		}
		
		if (intention != null)
		{
			_intentions.add(intention);
		}
	}
	
	public void removeIntention(CtrlIntention intention)
	{
		if (_intentions == null)
		{
			return;
		}
		_intentions.remove(intention);
	}
}