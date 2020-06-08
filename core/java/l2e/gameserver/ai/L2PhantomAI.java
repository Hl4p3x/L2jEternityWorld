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

import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_MOVE_TO;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_REST;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.xml.PhantomMessagesParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Character.AIAccessor;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.network.clientpackets.Say2;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.util.Rnd;

public class L2PhantomAI extends L2PlayableAI
{
	private boolean _thinking;
	private boolean _cpTask = false;
	private boolean _healTask = false;
	
	public L2PhantomAI(AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	protected synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		super.changeIntention(intention, arg0, arg1);
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		if (_actor.getTarget() == null)
		{
			setIntention(AI_INTENTION_IDLE);
		}
		else
		{
			if ((_actor.getTarget() != null) && ((L2Character) _actor.getTarget()).isAlikeDead())
			{
				setIntention(AI_INTENTION_IDLE);
			}
			else
			{
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, _actor.getTarget());
			}
		}
	}
	
	@Override
	protected void onIntentionRest()
	{
		if (getIntention() != AI_INTENTION_REST)
		{
			changeIntention(AI_INTENTION_REST, null, null);
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
		setIntention(AI_INTENTION_IDLE);
	}
	
	@Override
	protected void clientNotifyDead()
	{
		_clientMovingToPawnOffset = 0;
		_clientMoving = false;
		
		ThreadPoolManager.getInstance().scheduleAi(new ResurrectTask(), Rnd.get(3500, 6500));
		super.clientNotifyDead();
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		if (attacker instanceof L2PcInstance)
		{
			if ((attacker == null) || attacker.isDead())
			{
				setTarget(null);
				setIntention(AI_INTENTION_IDLE);
				return;
			}
			
			L2PcInstance player = (L2PcInstance) attacker;
			if (((player.getPvpFlag() > 0) && ((player.getLevel()) > _actor.getLevel()) && (player.getKarma() == 0)) || (((player.getLevel() - 10) > _actor.getLevel()) && (player.getKarma() == 0)))
			{
				_actor.setTarget(null);
				return;
			}
		}
		
		if (!_actor.isRunning())
		{
			_actor.setRunning();
		}
		
		setTarget(attacker);
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		
		super.onEvtAttacked(attacker);
	}
	
	private void thinkAttack()
	{
		L2Character target = getAttackTarget();
		if (target == null)
		{
			return;
		}
		
		if (target instanceof L2PcInstance)
		{
			if ((target == null) || target.isDead())
			{
				setTarget(null);
				setIntention(AI_INTENTION_IDLE);
				return;
			}
			
			L2PcInstance player = (L2PcInstance) target;
			if (((player.getPvpFlag() > 0) && ((player.getLevel()) > _actor.getLevel()) && (player.getKarma() == 0)) || (((player.getLevel() - 10) > _actor.getLevel()) && (player.getKarma() == 0)))
			{
				_actor.setTarget(null);
				return;
			}
		}
		
		if (!_actor.isDead())
		{
			if (Config.ALLOW_PHANTOM_USE_HEAL_POTION)
			{
				if ((!_healTask) && (_actor.getCurrentHp() < (_actor.getMaxHp() * 0.75)))
				{
					_healTask = true;
					ThreadPoolManager.getInstance().scheduleGeneral(new HealTask(), Config.PHANTOM_HEAL_REUSE_TIME);
				}
			}
			
			if (Config.ALLOW_PHANTOM_USE_CP_POTION)
			{
				if ((!_cpTask) && (_actor.getCurrentCp() < (_actor.getMaxCp() * 0.9)))
				{
					_cpTask = true;
					ThreadPoolManager.getInstance().scheduleGeneral(new CpTask(), Config.PHANTOM_PLAYERS_CP_REUSE_TIME);
				}
			}
		}
		
		if (checkTargetLostOrDead(target))
		{
			setAttackTarget(null);
			return;
		}
		if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange()))
		{
			return;
		}
		_accessor.doAttack(target);
	}
	
	private void thinkCast()
	{
		L2Character target = getCastTarget();
		
		if (target instanceof L2PcInstance)
		{
			if ((target == null) || target.isDead())
			{
				setTarget(null);
				setIntention(AI_INTENTION_IDLE);
				return;
			}
			
			L2PcInstance player = (L2PcInstance) target;
			if (((player.getPvpFlag() > 0) && ((player.getLevel()) > _actor.getLevel()) && (player.getKarma() == 0)) || (((player.getLevel() - 10) > _actor.getLevel()) && (player.getKarma() == 0)))
			{
				_actor.setTarget(null);
				return;
			}
		}
		
		if (checkTargetLost(target))
		{
			setCastTarget(null);
			return;
		}
		
		if (_actor.isCastingNow() && !_skill.isSimultaneousCast())
		{
			return;
		}
		
		if (maybeMoveToPawn(target, _actor.getMagicalAttackRange(_skill)))
		{
			return;
		}
		
		clientStopMoving(null);
		
		_accessor.doCast(_skill);
	}
	
	@Override
	protected void onIntentionMoveTo(Location loc)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}
		changeIntention(AI_INTENTION_MOVE_TO, loc, null);
		
		clientStopAutoAttack();
		
		_actor.abortAttack();
		
		moveTo(loc.getX(), loc.getY(), loc.getZ());
	}
	
	@Override
	protected void onEvtThink()
	{
		if (_thinking || _actor.isAllSkillsDisabled())
		{
			return;
		}
		
		_thinking = true;
		try
		{
			switch (getIntention())
			{
				case AI_INTENTION_ATTACK:
					thinkAttack();
					break;
				case AI_INTENTION_CAST:
					thinkCast();
					break;
			}
		}
		finally
		{
			_thinking = false;
		}
	}
	
	protected class CpTask implements Runnable
	{
		private CpTask()
		{
		}
		
		@Override
		public void run()
		{
			if (_actor.isDead())
			{
				return;
			}
			_actor.broadcastPacket(new MagicSkillUse(_actor, _actor, 2166, 1, 1, 0));
			_actor.setCurrentCp(200 + _actor.getCurrentCp());
			StatusUpdate su = new StatusUpdate(_actor);
			su.addAttribute(StatusUpdate.CUR_CP, (int) _actor.getCurrentCp());
			_actor.sendPacket(su);
			if (_actor.getCurrentCp() < (_actor.getMaxCp() * 0.9))
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new CpTask(), Config.PHANTOM_PLAYERS_CP_REUSE_TIME);
			}
			else
			{
				_cpTask = false;
			}
		}
	}
	
	protected class HealTask implements Runnable
	{
		private HealTask()
		{
		}
		
		@Override
		public void run()
		{
			if (_actor.isDead())
			{
				return;
			}
			
			_actor.broadcastPacket(new MagicSkillUse(_actor, _actor, 2038, 1, 1, 0));
			_actor.setCurrentHp(435 + _actor.getCurrentHp());
			StatusUpdate su = new StatusUpdate(_actor);
			su.addAttribute(StatusUpdate.CUR_HP, (int) _actor.getCurrentHp());
			_actor.sendPacket(su);
			if (_actor.getCurrentHp() < (_actor.getMaxHp() * 0.75))
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new HealTask(), Config.PHANTOM_HEAL_REUSE_TIME);
			}
			else
			{
				_healTask = false;
			}
		}
	}
	
	protected class ResurrectTask implements Runnable
	{
		public ResurrectTask()
		{
		}
		
		@Override
		public void run()
		{
			CreatureSay cs = new CreatureSay(_actor.getObjectId(), Say2.ALL, _actor.getName(), PhantomMessagesParser.getInstance().getRndDeadMessage());
			for (L2PcInstance player : _actor.getKnownList().getKnownPlayersInRadius(1000))
			{
				if (player != null)
				{
					player.sendPacket(cs);
				}
			}
			setIntention(AI_INTENTION_IDLE);
			_actor.teleToClosestTown();
		}
	}
}