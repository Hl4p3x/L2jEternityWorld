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
package l2e.gameserver.model.actor.tasks.npc.trap;

import java.util.logging.Logger;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2TrapInstance;
import l2e.gameserver.network.serverpackets.SocialAction;

public class TrapTask implements Runnable
{
	private static final Logger _log = Logger.getLogger(TrapTask.class.getName());
	private static final int TICK = 500;
	private final L2TrapInstance _trap;
	
	public TrapTask(L2TrapInstance trap)
	{
		_trap = trap;
	}
	
	@Override
	public void run()
	{
		try
		{
			if (!_trap.isTriggered())
			{
				if (_trap.hasLifeTime())
				{
					_trap.setRemainingTime(_trap.getRemainingTime() - TICK);
					if (_trap.getRemainingTime() < (_trap.getLifeTime() - 15000))
					{
						_trap.broadcastPacket(new SocialAction(_trap.getObjectId(), 2));
					}
					if (_trap.getRemainingTime() < 0)
					{
						switch (_trap.getSkill().getTargetType())
						{
							case AURA:
							case FRONT_AURA:
							case BEHIND_AURA:
								_trap.triggerTrap(_trap);
								break;
							default:
								_trap.unSummon();
						}
						return;
					}
				}
				
				for (L2Character target : _trap.getKnownList().getKnownCharactersInRadius(_trap.getSkill().getAffectRange()))
				{
					if (!_trap.checkTarget(target))
					{
						continue;
					}
					_trap.triggerTrap(target);
					return;
				}
				
				ThreadPoolManager.getInstance().scheduleGeneral(new TrapTask(_trap), TICK);
			}
		}
		catch (Exception e)
		{
			_log.severe(L2TrapInstance.class.getSimpleName() + ": " + e.getMessage());
			_trap.unSummon();
		}
	}
}