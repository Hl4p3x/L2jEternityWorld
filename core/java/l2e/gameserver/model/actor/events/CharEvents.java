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
import java.util.logging.Logger;

import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.events.annotations.PlayerOnly;
import l2e.gameserver.model.actor.events.annotations.SkillId;
import l2e.gameserver.model.actor.events.annotations.SkillLevel;
import l2e.gameserver.model.actor.events.listeners.IAttackEventListener;
import l2e.gameserver.model.actor.events.listeners.IDamageDealtEventListener;
import l2e.gameserver.model.actor.events.listeners.IDamageReceivedEventListener;
import l2e.gameserver.model.actor.events.listeners.IDeathEventListener;
import l2e.gameserver.model.actor.events.listeners.ISkillUseEventListener;
import l2e.gameserver.model.actor.events.listeners.ITeleportedEventListener;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.util.Util;

public class CharEvents extends AbstractCharEvents
{
	protected static final Logger _log = Logger.getLogger(CharEvents.class.getName());
	
	private final L2Character _activeChar;
	
	public CharEvents(L2Character activeChar)
	{
		_activeChar = activeChar;
	}
	
	public boolean onAttack(L2Character target)
	{
		if (hasListeners())
		{
			for (IAttackEventListener listener : getEventListeners(IAttackEventListener.class))
			{
				try
				{
					if (listener.getClass().isAnnotationPresent(PlayerOnly.class) && !target.isPlayer())
					{
						continue;
					}
					
					if (!listener.onAttack(getActingPlayer(), target))
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
	
	public boolean onMagic(L2Skill skill, boolean simultaneously, L2Character target, L2Object[] targets)
	{
		if (hasListeners())
		{
			for (ISkillUseEventListener listener : getEventListeners(ISkillUseEventListener.class))
			{
				try
				{
					if (listener.getClass().isAnnotationPresent(PlayerOnly.class) && !target.isPlayer())
					{
						continue;
					}
					
					final SkillId skillIdA = listener.getClass().getAnnotation(SkillId.class);
					if ((skillIdA != null) && (!Util.contains(skillIdA.value(), skill.getId())))
					{
						continue;
					}
					
					final SkillLevel skillLevelA = listener.getClass().getAnnotation(SkillLevel.class);
					if ((skillLevelA != null) && (!Util.contains(skillLevelA.value(), skill.getLevel())))
					{
						continue;
					}
					
					if (!listener.onSkillUse(getActingPlayer(), skill, simultaneously, target, targets))
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
	
	public boolean onDeath(L2Character killer)
	{
		if (hasListeners())
		{
			for (IDeathEventListener listener : getEventListeners(IDeathEventListener.class))
			{
				try
				{
					if (listener.getClass().isAnnotationPresent(PlayerOnly.class) && !killer.isPlayer())
					{
						continue;
					}
					
					if (!listener.onDeath(killer, getActingPlayer()))
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
	
	public void onDamageDealt(double damage, L2Character target, L2Skill skill, boolean crit, boolean damageOverTime)
	{
		if (hasListeners())
		{
			for (IDamageDealtEventListener listener : getEventListeners(IDamageDealtEventListener.class))
			{
				try
				{
					if (listener.getClass().isAnnotationPresent(PlayerOnly.class) && !target.isPlayer())
					{
						continue;
					}
					
					listener.onDamageDealtEvent(getActingPlayer(), target, damage, skill, crit, damageOverTime);
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
	}
	
	public void onDamageReceived(double damage, L2Character attacker, L2Skill skill, boolean crit, boolean damageOverTime)
	{
		if (hasListeners())
		{
			for (IDamageReceivedEventListener listener : getEventListeners(IDamageReceivedEventListener.class))
			{
				try
				{
					if (listener.getClass().isAnnotationPresent(PlayerOnly.class) && !attacker.isPlayer())
					{
						continue;
					}
					
					listener.onDamageReceivedEvent(attacker, getActingPlayer(), damage, skill, crit, damageOverTime);
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
	}
	
	public void onTeleported()
	{
		if (hasListeners())
		{
			for (ITeleportedEventListener listener : getEventListeners(ITeleportedEventListener.class))
			{
				try
				{
					listener.onTeleported(getActingPlayer());
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
	}
	
	public L2Character getActingPlayer()
	{
		return _activeChar;
	}
}