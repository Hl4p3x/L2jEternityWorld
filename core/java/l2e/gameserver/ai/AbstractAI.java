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

import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.AutoAttackStart;
import l2e.gameserver.network.serverpackets.AutoAttackStop;
import l2e.gameserver.network.serverpackets.Die;
import l2e.gameserver.network.serverpackets.MoveToLocation;
import l2e.gameserver.network.serverpackets.MoveToPawn;
import l2e.gameserver.network.serverpackets.StopMove;
import l2e.gameserver.network.serverpackets.StopRotation;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public abstract class AbstractAI implements Ctrl
{
	protected final Logger _log = Logger.getLogger(getClass().getName());
	
	private NextAction _nextAction;
	
	public NextAction getNextAction()
	{
		return _nextAction;
	}
	
	public void setNextAction(NextAction nextAction)
	{
		_nextAction = nextAction;
	}
	
	private class FollowTask implements Runnable
	{
		protected int _range = 70;
		
		public FollowTask()
		{
		}
		
		public FollowTask(int range)
		{
			_range = range;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_followTask == null)
				{
					return;
				}
				
				L2Character followTarget = _followTarget;
				if (followTarget == null)
				{
					if (_actor instanceof L2Summon)
					{
						((L2Summon) _actor).setFollowStatus(false);
					}
					setIntention(AI_INTENTION_IDLE);
					return;
				}
				
				if (!_actor.isInsideRadius(followTarget, _range, true, false))
				{
					if (!_actor.isInsideRadius(followTarget, 3000, true, false))
					{
						if (_actor instanceof L2Summon)
						{
							((L2Summon) _actor).setFollowStatus(false);
						}
						
						setIntention(AI_INTENTION_IDLE);
						return;
					}
					
					moveToPawn(followTarget, _range);
				}
			}
			catch (Exception e)
			{
				_log.warning(getClass().getSimpleName() + ": Error: " + e.getMessage());
			}
		}
	}
	
	protected final L2Character _actor;
	
	protected final L2Character.AIAccessor _accessor;
	
	protected CtrlIntention _intention = AI_INTENTION_IDLE;
	protected Object _intentionArg0 = null;
	protected Object _intentionArg1 = null;
	
	protected volatile boolean _clientMoving;
	protected volatile boolean _clientAutoAttacking;
	protected int _clientMovingToPawnOffset;
	
	private L2Object _target;
	private L2Character _castTarget;
	protected L2Character _attackTarget;
	protected L2Character _followTarget;
	
	L2Skill _skill;
	
	private int _moveToPawnTimeout;
	
	protected Future<?> _followTask = null;
	private static final int FOLLOW_INTERVAL = 1000;
	private static final int ATTACK_FOLLOW_INTERVAL = 500;
	
	protected AbstractAI(L2Character.AIAccessor accessor)
	{
		_accessor = accessor;
		
		_actor = accessor.getActor();
	}
	
	@Override
	public L2Character getActor()
	{
		return _actor;
	}
	
	@Override
	public CtrlIntention getIntention()
	{
		return _intention;
	}
	
	protected void setCastTarget(L2Character target)
	{
		_castTarget = target;
	}
	
	public L2Character getCastTarget()
	{
		return _castTarget;
	}
	
	protected void setAttackTarget(L2Character target)
	{
		_attackTarget = target;
	}
	
	@Override
	public L2Character getAttackTarget()
	{
		return _attackTarget;
	}
	
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_intention = intention;
		_intentionArg0 = arg0;
		_intentionArg1 = arg1;
	}
	
	@Override
	public final void setIntention(CtrlIntention intention)
	{
		setIntention(intention, null, null);
	}
	
	@Override
	public final void setIntention(CtrlIntention intention, Object arg0)
	{
		setIntention(intention, arg0, null);
	}
	
	@Override
	public final void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if ((intention != AI_INTENTION_FOLLOW) && (intention != AI_INTENTION_ATTACK))
		{
			stopFollow();
		}
		
		switch (intention)
		{
			case AI_INTENTION_IDLE:
				onIntentionIdle();
				break;
			case AI_INTENTION_ACTIVE:
				onIntentionActive();
				break;
			case AI_INTENTION_REST:
				onIntentionRest();
				break;
			case AI_INTENTION_ATTACK:
				onIntentionAttack((L2Character) arg0);
				break;
			case AI_INTENTION_CAST:
				onIntentionCast((L2Skill) arg0, (L2Object) arg1);
				break;
			case AI_INTENTION_MOVE_TO:
				onIntentionMoveTo((Location) arg0);
				break;
			case AI_INTENTION_FOLLOW:
				onIntentionFollow((L2Character) arg0);
				break;
			case AI_INTENTION_PICK_UP:
				onIntentionPickUp((L2Object) arg0);
				break;
			case AI_INTENTION_INTERACT:
				onIntentionInteract((L2Object) arg0);
				break;
		}
		
		if ((_nextAction != null) && _nextAction.getIntentions().contains(intention))
		{
			_nextAction = null;
		}
	}
	
	@Override
	public final void notifyEvent(CtrlEvent evt)
	{
		notifyEvent(evt, null, null);
	}
	
	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0)
	{
		notifyEvent(evt, arg0, null);
	}
	
	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
	{
		if ((!_actor.isVisible() && !_actor.isTeleporting()) || !_actor.hasAI())
		{
			return;
		}
		
		switch (evt)
		{
			case EVT_THINK:
				onEvtThink();
				break;
			case EVT_ATTACKED:
				onEvtAttacked((L2Character) arg0);
				break;
			case EVT_AGGRESSION:
				onEvtAggression((L2Character) arg0, ((Number) arg1).intValue());
				break;
			case EVT_STUNNED:
				onEvtStunned((L2Character) arg0);
				break;
			case EVT_PARALYZED:
				onEvtParalyzed((L2Character) arg0);
				break;
			case EVT_SLEEPING:
				onEvtSleeping((L2Character) arg0);
				break;
			case EVT_ROOTED:
				onEvtRooted((L2Character) arg0);
				break;
			case EVT_CONFUSED:
				onEvtConfused((L2Character) arg0);
				break;
			case EVT_MUTED:
				onEvtMuted((L2Character) arg0);
				break;
			case EVT_EVADED:
				onEvtEvaded((L2Character) arg0);
				break;
			case EVT_READY_TO_ACT:
				if (!_actor.isCastingNow() && !_actor.isCastingSimultaneouslyNow())
				{
					onEvtReadyToAct();
				}
				break;
			case EVT_USER_CMD:
				onEvtUserCmd(arg0, arg1);
				break;
			case EVT_ARRIVED:
				if (!_actor.isCastingNow() && !_actor.isCastingSimultaneouslyNow())
				{
					onEvtArrived();
				}
				break;
			case EVT_ARRIVED_REVALIDATE:
				if (_actor.isMoving())
				{
					onEvtArrivedRevalidate();
				}
				break;
			case EVT_ARRIVED_BLOCKED:
				onEvtArrivedBlocked((Location) arg0);
				break;
			case EVT_FORGET_OBJECT:
				onEvtForgetObject((L2Object) arg0);
				break;
			case EVT_CANCEL:
				onEvtCancel();
				break;
			case EVT_DEAD:
				onEvtDead();
				break;
			case EVT_FAKE_DEATH:
				onEvtFakeDeath();
				break;
			case EVT_FINISH_CASTING:
				onEvtFinishCasting();
				break;
		}
		
		if ((_nextAction != null) && _nextAction.getEvents().contains(evt))
		{
			_nextAction.doAction();
		}
	}
	
	protected abstract void onIntentionIdle();
	
	protected abstract void onIntentionActive();
	
	protected abstract void onIntentionRest();
	
	protected abstract void onIntentionAttack(L2Character target);
	
	protected abstract void onIntentionCast(L2Skill skill, L2Object target);
	
	protected abstract void onIntentionMoveTo(Location destination);
	
	protected abstract void onIntentionFollow(L2Character target);
	
	protected abstract void onIntentionPickUp(L2Object item);
	
	protected abstract void onIntentionInteract(L2Object object);
	
	protected abstract void onEvtThink();
	
	protected abstract void onEvtAttacked(L2Character attacker);
	
	protected abstract void onEvtAggression(L2Character target, int aggro);
	
	protected abstract void onEvtStunned(L2Character attacker);
	
	protected abstract void onEvtParalyzed(L2Character attacker);
	
	protected abstract void onEvtSleeping(L2Character attacker);
	
	protected abstract void onEvtRooted(L2Character attacker);
	
	protected abstract void onEvtConfused(L2Character attacker);
	
	protected abstract void onEvtMuted(L2Character attacker);
	
	protected abstract void onEvtEvaded(L2Character attacker);
	
	protected abstract void onEvtReadyToAct();
	
	protected abstract void onEvtUserCmd(Object arg0, Object arg1);
	
	protected abstract void onEvtArrived();
	
	protected abstract void onEvtArrivedRevalidate();
	
	protected abstract void onEvtArrivedBlocked(Location blocked_at_pos);
	
	protected abstract void onEvtForgetObject(L2Object object);
	
	protected abstract void onEvtCancel();
	
	protected abstract void onEvtDead();
	
	protected abstract void onEvtFakeDeath();
	
	protected abstract void onEvtFinishCasting();
	
	protected void clientActionFailed()
	{
		if (_actor instanceof L2PcInstance)
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	protected void moveToPawn(L2Object pawn, int offset)
	{
		if (!_actor.isMovementDisabled())
		{
			if (offset < 10)
			{
				offset = 10;
			}
			
			boolean sendPacket = true;
			if (_clientMoving && (_target == pawn))
			{
				if (_clientMovingToPawnOffset == offset)
				{
					if (GameTimeController.getInstance().getGameTicks() < _moveToPawnTimeout)
					{
						return;
					}
					sendPacket = false;
				}
				else if (_actor.isOnGeodataPath())
				{
					if (GameTimeController.getInstance().getGameTicks() < (_moveToPawnTimeout + 10))
					{
						return;
					}
				}
			}
			
			_clientMoving = true;
			_clientMovingToPawnOffset = offset;
			_target = pawn;
			_moveToPawnTimeout = GameTimeController.getInstance().getGameTicks();
			_moveToPawnTimeout += 1000 / GameTimeController.MILLIS_IN_TICK;
			
			if ((pawn == null) || (_accessor == null))
			{
				return;
			}
			
			_accessor.moveTo(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
			
			if (!_actor.isMoving())
			{
				clientActionFailed();
				return;
			}
			
			if (pawn instanceof L2Character)
			{
				if (_actor.isOnGeodataPath())
				{
					_actor.broadcastPacket(new MoveToLocation(_actor));
					_clientMovingToPawnOffset = 0;
				}
				else if (sendPacket)
				{
					_actor.broadcastPacket(new MoveToPawn(_actor, (L2Character) pawn, offset));
				}
			}
			else
			{
				_actor.broadcastPacket(new MoveToLocation(_actor));
			}
		}
		else
		{
			clientActionFailed();
		}
	}
	
	protected void moveTo(int x, int y, int z)
	{
		if (!_actor.isMovementDisabled())
		{
			_clientMoving = true;
			_clientMovingToPawnOffset = 0;
			
			_accessor.moveTo(x, y, z);
			_actor.broadcastPacket(new MoveToLocation(_actor));
			
		}
		else
		{
			clientActionFailed();
		}
	}
	
	protected void clientStopMoving(Location loc)
	{
		if (_actor.isMoving())
		{
			_accessor.stopMove(loc);
		}
		
		_clientMovingToPawnOffset = 0;
		
		if (_clientMoving || (loc != null))
		{
			_clientMoving = false;
			_actor.broadcastPacket(new StopMove(_actor));
			
			if (loc != null)
			{
				_actor.broadcastPacket(new StopRotation(_actor.getObjectId(), loc.getHeading(), 0));
			}
		}
	}
	
	protected void clientStoppedMoving()
	{
		if (_clientMovingToPawnOffset > 0)
		{
			_clientMovingToPawnOffset = 0;
			_actor.broadcastPacket(new StopMove(_actor));
		}
		_clientMoving = false;
	}
	
	public boolean isAutoAttacking()
	{
		return _clientAutoAttacking;
	}
	
	public void setAutoAttacking(boolean isAutoAttacking)
	{
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().setAutoAttacking(isAutoAttacking);
			}
			return;
		}
		_clientAutoAttacking = isAutoAttacking;
	}
	
	public void clientStartAutoAttack()
	{
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStartAutoAttack();
			}
			return;
		}
		if (!isAutoAttacking())
		{
			if (_actor.isPlayer() && _actor.hasSummon())
			{
				_actor.getSummon().broadcastPacket(new AutoAttackStart(_actor.getSummon().getObjectId()));
			}
			_actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
			setAutoAttacking(true);
		}
		AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
	}
	
	public void clientStopAutoAttack()
	{
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStopAutoAttack();
			}
			return;
		}
		if (_actor instanceof L2PcInstance)
		{
			if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor) && isAutoAttacking())
			{
				AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
			}
		}
		else if (isAutoAttacking())
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
			setAutoAttacking(false);
		}
	}
	
	protected void clientNotifyDead()
	{
		Die msg = new Die(_actor);
		_actor.broadcastPacket(msg);
		
		_intention = AI_INTENTION_IDLE;
		_target = null;
		_castTarget = null;
		_attackTarget = null;
		
		stopFollow();
	}
	
	public void describeStateToPlayer(L2PcInstance player)
	{
		if (getActor().isVisibleFor(player))
		{
			if (_clientMoving)
			{
				if ((_clientMovingToPawnOffset != 0) && (_followTarget != null))
				{
					player.sendPacket(new MoveToPawn(_actor, _followTarget, _clientMovingToPawnOffset));
				}
				else
				{
					player.sendPacket(new MoveToLocation(_actor));
				}
			}
		}
	}
	
	public synchronized void startFollow(L2Character target)
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
		_followTarget = target;
		_followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(), 5, FOLLOW_INTERVAL);
	}
	
	public synchronized void startFollow(L2Character target, int range)
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
		
		_followTarget = target;
		_followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(range), 5, ATTACK_FOLLOW_INTERVAL);
	}
	
	public synchronized void stopFollow()
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
		_followTarget = null;
	}
	
	protected L2Character getFollowTarget()
	{
		return _followTarget;
	}
	
	protected L2Object getTarget()
	{
		return _target;
	}
	
	protected void setTarget(L2Object target)
	{
		_target = target;
	}
	
	public void stopAITask()
	{
		stopFollow();
	}
	
	@Override
	public String toString()
	{
		if (_actor == null)
		{
			return "Actor: null";
		}
		return "Actor: " + _actor;
	}
}