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

import java.util.EmptyStackException;
import java.util.Stack;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.util.Util;
import l2e.util.Rnd;

public class L2PhantomArcherAI extends L2PhantomAI
{
	private boolean _thinking;
	
	static class IntentionCommand
	{
		protected CtrlIntention _crtlIntention;
		protected Object _arg0;
		protected Object _arg1;
		
		protected IntentionCommand(CtrlIntention pIntention, Object pArg0, Object pArg1)
		{
			this._crtlIntention = pIntention;
			this._arg0 = pArg0;
			this._arg1 = pArg1;
		}
	}
	
	private final Stack<IntentionCommand> _interuptedIntentions = new Stack<>();
	
	public L2PhantomArcherAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	protected synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (intention != CtrlIntention.AI_INTENTION_CAST)
		{
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		if ((intention == this._intention) && (arg0 == this._intentionArg0) && (arg1 == this._intentionArg1))
		{
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		this._interuptedIntentions.push(new IntentionCommand(this._intention, this._intentionArg0, this._intentionArg1));
		super.changeIntention(intention, arg0, arg1);
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		if ((this._skill != null) && (this._skill.isOffensive()))
		{
			this._interuptedIntentions.clear();
		}
		if (getIntention() == CtrlIntention.AI_INTENTION_CAST)
		{
			if (!this._interuptedIntentions.isEmpty())
			{
				IntentionCommand cmd = null;
				try
				{
					cmd = this._interuptedIntentions.pop();
				}
				catch (EmptyStackException ese)
				{
				}
				if ((cmd != null) && (cmd._crtlIntention != CtrlIntention.AI_INTENTION_CAST))
				{
					setIntention(cmd._crtlIntention, cmd._arg0, cmd._arg1);
				}
				else
				{
					setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
			}
			else
			{
				setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
	}
	
	@Override
	protected void onIntentionRest()
	{
		if (getIntention() != CtrlIntention.AI_INTENTION_REST)
		{
			changeIntention(CtrlIntention.AI_INTENTION_REST, null, null);
			setTarget(null);
			if (getAttackTarget() != null)
			{
				setAttackTarget(null);
			}
			clientStopMoving(null);
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		ThreadPoolManager.getInstance().scheduleAi(new ActiveTask(), 3000L);
	}
	
	private void thinkCast()
	{
		L2Character target = getCastTarget();
		if ((this._skill.getTargetType() == L2TargetType.GROUND) && (this._actor.isPlayer()))
		{
			if (!maybeMoveToPosition(this._actor.getActingPlayer().getCurrentSkillWorldPosition(), this._actor.getMagicalAttackRange(this._skill)))
			{
			}
		}
		else
		{
			if (checkTargetLost(target))
			{
				if ((this._skill.isOffensive()) && (getAttackTarget() != null))
				{
					setCastTarget(null);
				}
				ThreadPoolManager.getInstance().scheduleAi(new IdleTask(), 2000L);
				return;
			}
			if ((target != null) && (maybeMoveToPawn(target, this._actor.getMagicalAttackRange(this._skill))))
			{
				return;
			}
		}
		if (this._skill.getHitTime() > 50)
		{
			clientStopMoving(null);
		}
		L2Object oldTarget = this._actor.getTarget();
		if (oldTarget != null)
		{
			if ((target != null) && (oldTarget != target))
			{
				this._actor.setTarget(getCastTarget());
			}
			this._accessor.doCast(this._skill);
			if ((target != null) && (oldTarget != target))
			{
				this._actor.setTarget(oldTarget);
			}
		}
		else
		{
			this._accessor.doCast(this._skill);
		}
		ThreadPoolManager.getInstance().scheduleAi(new ActiveTask(), 700L);
	}
	
	private void thinkPickUp()
	{
		if ((this._actor.isAllSkillsDisabled()) || (this._actor.isMovementDisabled()))
		{
			return;
		}
		L2Object target = getTarget();
		if (checkTargetLost(target))
		{
			return;
		}
		if (maybeMoveToPawn(target, 36))
		{
			return;
		}
		setIntention(CtrlIntention.AI_INTENTION_IDLE);
		((L2PcInstance.AIAccessor) this._accessor).doPickupItem(target);
	}
	
	private void thinkInteract()
	{
		if (this._actor.isAllSkillsDisabled())
		{
			return;
		}
		L2Object target = getTarget();
		if (checkTargetLost(target))
		{
			return;
		}
		if (maybeMoveToPawn(target, 36))
		{
			return;
		}
		if (!(target instanceof L2StaticObjectInstance))
		{
			((L2PcInstance.AIAccessor) this._accessor).doInteract((L2Character) target);
		}
		setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}
	
	@Override
	protected void onEvtThink()
	{
		if ((this._thinking) || (this._actor.isAllSkillsDisabled()))
		{
			return;
		}
		this._thinking = true;
		try
		{
			if (getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			{
				radarOn();
			}
			else if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
			else if (getIntention() == CtrlIntention.AI_INTENTION_CAST)
			{
				thinkCast();
			}
			else if (getIntention() == CtrlIntention.AI_INTENTION_PICK_UP)
			{
				thinkPickUp();
			}
			else if (getIntention() == CtrlIntention.AI_INTENTION_INTERACT)
			{
				thinkInteract();
			}
		}
		finally
		{
			this._thinking = false;
		}
	}
	
	private boolean _cpTask = false;
	private long _atkTask = 0L;
	
	private void thinkAttack()
	{
		L2Character target = getAttackTarget();
		if (target == null)
		{
			return;
		}
		if (!this._actor.isDead())
		{
			if ((!this._cpTask) && (this._actor.getCurrentCp() < (this._actor.getMaxCp() * 0.9D)))
			{
				this._cpTask = true;
				ThreadPoolManager.getInstance().scheduleAi(new CpTask(), 200); // Config.CP_REUSE_TIME
			}
		}
		if (checkTargetLostOrDead(target))
		{
			if (target != null)
			{
				setAttackTarget(null);
			}
			ThreadPoolManager.getInstance().scheduleAi(new IdleTask(), 2000L);
			return;
		}
		if (maybeMoveToPawn(target, this._actor.getPhysicalAttackRange()))
		{
			return;
		}
		if ((this._atkTask < System.currentTimeMillis()) && (Rnd.get(100) < 45))
		{
			this._atkTask = (System.currentTimeMillis() + 5000L);
			switch (Rnd.get(10))
			{
				case 1:
					this._accessor.doCast(SkillHolder.getInstance().getInfo(101, 40));
					break;
				case 2:
					this._accessor.doCast(SkillHolder.getInstance().getInfo(19, 37));
					break;
				case 3:
					this._accessor.doCast(SkillHolder.getInstance().getInfo(354, 1));
					break;
				case 4:
					_actor.rndWalk();
			}
			this._actor.setTarget(target);
			ThreadPoolManager.getInstance().scheduleAi(new AttackTask(target), 900L);
			return;
		}
		this._accessor.doAttack(target);
	}
	
	private void radarOn()
	{
		if ((getAttackTarget() == null) && (Util.calculateDistance(this._actor.getX(), this._actor.getY(), this._actor.getZ(), this._actor.getFakeLoc()._x, this._actor.getFakeLoc()._y, this._actor.getFakeLoc()._z, true) > 2100.0D))
		{
			moveTo(this._actor.getFakeLoc()._x, this._actor.getFakeLoc()._y, this._actor.getFakeLoc()._z);
		}
		else if (Rnd.get(100) < 5)
		{
			this._accessor.doCast(SkillHolder.getInstance().getInfo(99, 2));
			this._actor.setTarget(this._actor);
		}
		else if (Rnd.get(100) < 10)
		{
			if (Util.calculateDistance(this._actor.getX(), this._actor.getY(), this._actor.getZ(), this._actor.getFakeLoc()._x, this._actor.getFakeLoc()._y, this._actor.getFakeLoc()._z, true) > 310.0D)
			{
				moveTo(this._actor.getFakeLoc()._x, this._actor.getFakeLoc()._y, this._actor.getFakeLoc()._z);
			}
			else
			{
				_actor.rndWalk();
			}
		}
		findTarget();
		ThreadPoolManager.getInstance().scheduleAi(new ActiveTask(), 6000L);
	}
	
	private void findTarget()
	{
		for (L2PcInstance target : this._actor.getKnownList().getKnownPlayersInRadius(1450))
		{
			if ((target != null) && (!target.isDead()))
			{
				if ((target.getKarma() > 0) || (target.getPvpFlag() > 0))
				{
					if (!this._actor.isRunning())
					{
						this._actor.setRunning();
					}
					if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
					{
						this._actor.setTarget(target);
						setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					}
					super.onEvtAttacked(target);
					break;
				}
			}
		}
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		if (!this._actor.isRunning())
		{
			this._actor.setRunning();
		}
		if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			this._actor.setTarget(attacker);
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		super.onEvtAttacked(attacker);
	}
	
	@Override
	protected void clientNotifyDead()
	{
		this._clientMovingToPawnOffset = 0;
		this._clientMoving = false;
		
		ThreadPoolManager.getInstance().scheduleAi(new ResurrectTask(), getRespawnDelay(0));
		super.clientNotifyDead();
	}
	
	private int getRespawnDelay(int delay)
	{
		delay = Rnd.get(3500, 6500);
		if ((delay > 4000) && (Rnd.get(100) < 25))
		{
			// this._actor.sayString(getLastWord(Rnd.get(13)), 0);
		}
		return delay;
	}
	
	// private String getLastWord(int word)
	// {
	// // return L2Phantom.getInstance().getRandomLastPhrase();
	// }
	
	protected String getRangeWord(int word)
	{
		switch (word)
		{
			case 0:
				return "������";
			case 1:
				return "���� ����";
			case 2:
				return "��� ����";
		}
		return "����� ����";
	}
	
	private class ResurrectTask implements Runnable
	{
		public ResurrectTask()
		{
		}
		
		@Override
		public void run()
		{
			_actor.teleToLocation(TeleportWhereType.TOWN);
			_actor.doRevive();
		}
	}
	
	private class IdleTask implements Runnable
	{
		public IdleTask()
		{
		}
		
		@Override
		public void run()
		{
			radarOn();
		}
	}
	
	private class ActiveTask implements Runnable
	{
		public ActiveTask()
		{
		}
		
		@Override
		public void run()
		{
			onEvtThink();
		}
	}
	
	protected class AttackTask implements Runnable
	{
		protected final L2Character _target;
		
		public AttackTask(L2Character target)
		{
			_target = target;
		}
		
		@Override
		public void run()
		{
			thinkAttack();
		}
	}
	
	private class CpTask implements Runnable
	{
		public CpTask()
		{
		}
		
		@Override
		public void run()
		{
			if (_actor.isDead())
			{
				return;
			}
			if (!_actor.isAllSkillsDisabled())
			{
				_actor.broadcastPacket(new MagicSkillUse(_actor, _actor, 2166, 1, 1, 0));
				if (_actor.getCurrentCp() != _actor.getMaxCp())
				{
					_actor.setCurrentCp(_actor.getCurrentCp() + 500.0D);
				}
			}
			if (_actor.getCurrentCp() < (_actor.getMaxCp() * 0.9D))
			{
				_cpTask = true;
				ThreadPoolManager.getInstance().scheduleAi(new CpTask(), 200); // Config.CP_REUSE_TIME
			}
			else
			{
				_cpTask = false;
			}
		}
	}
}
