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

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2DefenderInstance;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.skills.L2Skill;

public class L2DoorAI extends L2CharacterAI
{
	public L2DoorAI(L2DoorInstance.AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	protected void onIntentionIdle()
	{
	}
	
	@Override
	protected void onIntentionActive()
	{
	}
	
	@Override
	protected void onIntentionRest()
	{
	}
	
	@Override
	protected void onIntentionAttack(L2Character target)
	{
	}
	
	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
	}
	
	@Override
	protected void onIntentionMoveTo(Location destination)
	{
	}
	
	@Override
	protected void onIntentionFollow(L2Character target)
	{
	}
	
	@Override
	protected void onIntentionPickUp(L2Object item)
	{
	}
	
	@Override
	protected void onIntentionInteract(L2Object object)
	{
	}
	
	@Override
	protected void onEvtThink()
	{
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		L2DoorInstance me = (L2DoorInstance) _actor;
		ThreadPoolManager.getInstance().executeTask(new onEventAttackedDoorTask(me, attacker));
	}
	
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
	}
	
	@Override
	protected void onEvtStunned(L2Character attacker)
	{
	}
	
	@Override
	protected void onEvtSleeping(L2Character attacker)
	{
	}
	
	@Override
	protected void onEvtRooted(L2Character attacker)
	{
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
	}
	
	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1)
	{
	}
	
	@Override
	protected void onEvtArrived()
	{
	}
	
	@Override
	protected void onEvtArrivedRevalidate()
	{
	}
	
	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_loc)
	{
	}
	
	@Override
	protected void onEvtForgetObject(L2Object object)
	{
	}
	
	@Override
	protected void onEvtCancel()
	{
	}
	
	@Override
	protected void onEvtDead()
	{
	}
	
	private class onEventAttackedDoorTask implements Runnable
	{
		private final L2DoorInstance _door;
		private final L2Character _attacker;
		
		public onEventAttackedDoorTask(L2DoorInstance door, L2Character attacker)
		{
			_door = door;
			_attacker = attacker;
		}
		
		@Override
		public void run()
		{
			for (L2DefenderInstance guard : _door.getKnownDefenders())
			{
				if (_actor.isInsideRadius(guard, guard.getFactionRange(), false, true) && (Math.abs(_attacker.getZ() - guard.getZ()) < 200))
				{
					guard.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _attacker, 15);
				}
			}
		}
	}
}