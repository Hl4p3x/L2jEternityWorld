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
package l2e.gameserver.model.actor.events;

import java.util.logging.Level;

import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.events.annotations.PlayerOnly;
import l2e.gameserver.model.actor.events.listeners.IExperienceReceivedEventListener;
import l2e.gameserver.model.actor.events.listeners.ILevelChangeEventListener;

public class PlayableEvents extends CharEvents
{
	public PlayableEvents(L2Playable activeChar)
	{
		super(activeChar);
	}
	
	public boolean onExperienceReceived(long exp)
	{
		if (hasListeners())
		{
			for (IExperienceReceivedEventListener listener : getEventListeners(IExperienceReceivedEventListener.class))
			{
				try
				{
					if (listener.getClass().isAnnotationPresent(PlayerOnly.class) && !getActingPlayer().isPlayer())
					{
						continue;
					}
					
					if (!listener.onExperienceReceived(getActingPlayer(), exp))
					{
						return false;
					}
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
		return true;
	}
	
	public boolean onLevelChange(byte levels)
	{
		if (hasListeners())
		{
			for (ILevelChangeEventListener listener : getEventListeners(ILevelChangeEventListener.class))
			{
				try
				{
					if (listener.getClass().isAnnotationPresent(PlayerOnly.class) && !getActingPlayer().isPlayer())
					{
						continue;
					}
					
					if (!listener.onLevelChange(getActingPlayer(), levels))
					{
						return false;
					}
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
		return true;
	}
	
	@Override
	public L2Playable getActingPlayer()
	{
		return (L2Playable) super.getActingPlayer();
	}
}