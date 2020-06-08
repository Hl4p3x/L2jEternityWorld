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

import l2e.gameserver.instancemanager.AntiFeedManager;
import l2e.gameserver.model.actor.events.annotations.Message;
import l2e.gameserver.model.actor.events.annotations.UseAntiFeed;
import l2e.gameserver.model.actor.events.listeners.IDlgAnswerEventListener;
import l2e.gameserver.model.actor.events.listeners.IFamePointsChangeEventListener;
import l2e.gameserver.model.actor.events.listeners.IKarmaChangeEventListener;
import l2e.gameserver.model.actor.events.listeners.IPKPointsChangeEventListener;
import l2e.gameserver.model.actor.events.listeners.IPlayerLoginEventListener;
import l2e.gameserver.model.actor.events.listeners.IPlayerLogoutEventListener;
import l2e.gameserver.model.actor.events.listeners.IPvPKillEventListener;
import l2e.gameserver.model.actor.events.listeners.IPvPPointsEventChange;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.util.Util;

public class PlayerEvents extends PlayableEvents
{
	public PlayerEvents(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return (L2PcInstance) super.getActingPlayer();
	}
	
	public boolean onKarmaChange(int oldKarma, int newKarma)
	{
		if (hasListeners())
		{
			for (IKarmaChangeEventListener listener : getEventListeners(IKarmaChangeEventListener.class))
			{
				try
				{
					if (!listener.onKarmaChange(getActingPlayer(), oldKarma, newKarma))
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
	
	public boolean onPKChange(int oldPKPoints, int newPKPoints)
	{
		if (hasListeners())
		{
			for (IPKPointsChangeEventListener listener : getEventListeners(IPKPointsChangeEventListener.class))
			{
				try
				{
					if (!listener.onPKPointsChange(getActingPlayer(), oldPKPoints, newPKPoints))
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
	
	public boolean onPvPChange(int oldPvPPoints, int newPvPPoints)
	{
		if (hasListeners())
		{
			for (IPvPPointsEventChange listener : getEventListeners(IPvPPointsEventChange.class))
			{
				try
				{
					if (!listener.onPvPPointsChange(getActingPlayer(), oldPvPPoints, newPvPPoints))
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
	
	public boolean onFameChange(int oldFamePoints, int newFamePoints)
	{
		if (hasListeners())
		{
			for (IFamePointsChangeEventListener listener : getEventListeners(IFamePointsChangeEventListener.class))
			{
				try
				{
					if (!listener.onFamePointsChange(getActingPlayer(), oldFamePoints, newFamePoints))
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
	
	public boolean onDlgAnswer(int messageId, int answer, int requesterId)
	{
		if (hasListeners())
		{
			for (IDlgAnswerEventListener listener : getEventListeners(IDlgAnswerEventListener.class))
			{
				try
				{
					final Message messageA = listener.getClass().getAnnotation(Message.class);
					if ((messageA != null) && !Util.contains(messageA.value(), messageId))
					{
						continue;
					}
					
					if (!listener.onDlgAnswer(getActingPlayer(), messageId, answer, requesterId))
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

	public void onPlayerLogin()
	{
		if (hasListeners())
		{
			for (IPlayerLoginEventListener listener : getEventListeners(IPlayerLoginEventListener.class))
			{
				try
				{
					listener.onPlayerLogin(getActingPlayer());
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
	}

	public void onPlayerLogout()
	{
		if (hasListeners())
		{
			for (IPlayerLogoutEventListener listener : getEventListeners(IPlayerLogoutEventListener.class))
			{
				try
				{
					listener.onPlayerLogout(getActingPlayer());
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
	}

	public void onPvPKill(L2PcInstance target)
	{
		if (hasListeners())
		{
			for (IPvPKillEventListener listener : getEventListeners(IPvPKillEventListener.class))
			{
				try
				{
					final UseAntiFeed useAntiFeed = listener.getClass().getAnnotation(UseAntiFeed.class);
					if ((useAntiFeed != null) && !AntiFeedManager.getInstance().check(getActingPlayer(), target))
					{
						continue;
					}
					
					listener.onPvPKill(getActingPlayer(), target);
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
	}
}