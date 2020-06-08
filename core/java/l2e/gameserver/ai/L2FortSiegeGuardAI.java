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

import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.Collection;
import java.util.concurrent.Future;

import l2e.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2DefenderInstance;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2FortBallistaInstance;
import l2e.gameserver.model.actor.instance.L2FortCommanderInstance;
import l2e.gameserver.model.actor.instance.L2NpcInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.util.Util;
import l2e.util.Rnd;

public class L2FortSiegeGuardAI extends L2CharacterAI implements Runnable
{
	private static final int MAX_ATTACK_TIMEOUT = 300;
	
	private Future<?> _aiTask;
	
	private final SelfAnalysis _selfAnalysis = new SelfAnalysis();
	
	private int _attackTimeout;
	
	private int _globalAggro;
	
	private boolean _thinking;
	
	private final int _attackRange;
	
	public L2FortSiegeGuardAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		_selfAnalysis.init();
		_attackTimeout = Integer.MAX_VALUE;
		_globalAggro = -10;
		_attackRange = ((L2Attackable) _actor).getPhysicalAttackRange();
	}
	
	@Override
	public void run()
	{
		onEvtThink();
	}
	
	private boolean autoAttackCondition(L2Character target)
	{
		if ((target == null) || (target instanceof L2DefenderInstance) || (target instanceof L2NpcInstance) || (target instanceof L2DoorInstance) || target.isAlikeDead() || (target instanceof L2FortBallistaInstance) || (target instanceof L2FortCommanderInstance) || (target instanceof L2Playable))
		{
			L2PcInstance player = null;
			if (target instanceof L2PcInstance)
			{
				player = ((L2PcInstance) target);
			}
			else if (target instanceof L2Summon)
			{
				player = ((L2Summon) target).getOwner();
			}
			if ((player == null) || ((player.getClan() != null) && (player.getClan().getFortId() == ((L2Npc) _actor).getFort().getId())))
			{
				return false;
			}
		}
		
		if ((target != null) && target.isInvul())
		{
			if ((target instanceof L2PcInstance) && ((L2PcInstance) target).isGM())
			{
				return false;
			}
			if ((target instanceof L2Summon) && ((L2Summon) target).getOwner().isGM())
			{
				return false;
			}
		}
		
		if (target instanceof L2Summon)
		{
			L2PcInstance owner = ((L2Summon) target).getOwner();
			if (_actor.isInsideRadius(owner, 1000, true, false))
			{
				target = owner;
			}
		}
		
		if (target instanceof L2Playable)
		{
			if (((L2Playable) target).isSilentMoving() && !_actor.isInsideRadius(target, 250, false, false))
			{
				return false;
			}
		}
		return (_actor.isAutoAttackable(target) && GeoClient.getInstance().canSeeTarget(_actor, target));
	}
	
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (Config.DEBUG)
		{
			_log.warning(getClass().getSimpleName() + ": changeIntention(" + intention + ", " + arg0 + ", " + arg1 + ")");
		}
		
		if (intention == AI_INTENTION_IDLE)
		{
			if (!_actor.isAlikeDead())
			{
				L2Attackable npc = (L2Attackable) _actor;
				
				if (!npc.getKnownList().getKnownPlayers().isEmpty())
				{
					intention = AI_INTENTION_ACTIVE;
				}
				else
				{
					intention = AI_INTENTION_IDLE;
				}
			}
			
			if (intention == AI_INTENTION_IDLE)
			{
				super.changeIntention(AI_INTENTION_IDLE, null, null);
				
				if (_aiTask != null)
				{
					_aiTask.cancel(true);
					_aiTask = null;
				}
				_accessor.detachAI();
				
				return;
			}
		}
		super.changeIntention(intention, arg0, arg1);
		
		if (_aiTask == null)
		{
			_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
		}
	}
	
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getInstance().getGameTicks();
		
		super.onIntentionAttack(target);
	}
	
	private void thinkActive()
	{
		L2Attackable npc = (L2Attackable) _actor;
		
		if (_globalAggro != 0)
		{
			if (_globalAggro < 0)
			{
				_globalAggro++;
			}
			else
			{
				_globalAggro--;
			}
		}
		
		if (_globalAggro >= 0)
		{
			for (L2Character target : npc.getKnownList().getKnownCharactersInRadius(_attackRange))
			{
				if (target == null)
				{
					continue;
				}
				if (autoAttackCondition(target))
				{
					int hating = npc.getHating(target);
					
					if (hating == 0)
					{
						npc.addDamageHate(target, 0, 1);
					}
				}
			}
			
			L2Character hated;
			if (_actor.isConfused())
			{
				hated = getAttackTarget();
			}
			else
			{
				hated = npc.getMostHated();
			}
			
			if (hated != null)
			{
				int aggro = npc.getHating(hated);
				
				if ((aggro + _globalAggro) > 0)
				{
					if (!_actor.isRunning())
					{
						_actor.setRunning();
					}
					
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated, null);
				}
				return;
			}
			
		}
		
		if (_actor.getWalkSpeed() >= 0)
		{
			if (_actor instanceof L2DefenderInstance)
			{
				((L2DefenderInstance) _actor).returnHome();
			}
			else
			{
				((L2FortCommanderInstance) _actor).returnHome();
			}
		}
	}
	
	private void thinkAttack()
	{
		if (Config.DEBUG)
		{
			_log.warning(getClass().getSimpleName() + ": thinkAttack(); timeout=" + (_attackTimeout - GameTimeController.getInstance().getGameTicks()));
		}
		
		if (_attackTimeout < GameTimeController.getInstance().getGameTicks())
		{
			if (_actor.isRunning())
			{
				_actor.setWalking();
				_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getInstance().getGameTicks();
			}
		}
		
		L2Character attackTarget = getAttackTarget();
		if ((attackTarget == null) || attackTarget.isAlikeDead() || (_attackTimeout < GameTimeController.getInstance().getGameTicks()))
		{
			if (attackTarget != null)
			{
				L2Attackable npc = (L2Attackable) _actor;
				npc.stopHating(attackTarget);
			}
			_attackTimeout = Integer.MAX_VALUE;
			setAttackTarget(null);
			
			setIntention(AI_INTENTION_ACTIVE, null, null);
			
			_actor.setWalking();
			return;
		}
		factionNotifyAndSupport();
		attackPrepare();
	}
	
	private final void factionNotifyAndSupport()
	{
		L2Character target = getAttackTarget();
		
		if ((((L2Npc) _actor).getFactionId() == null) || (target == null))
		{
			return;
		}
		
		if (target.isInvul())
		{
			return;
		}
		
		String faction_id = ((L2Npc) _actor).getFactionId();
		
		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(1000))
		{
			if (cha == null)
			{
				continue;
			}
			
			if (!(cha instanceof L2Npc))
			{
				if (_selfAnalysis.hasHealOrResurrect && (cha instanceof L2PcInstance) && ((L2Npc) _actor).getFort().getSiege().checkIsDefender(((L2PcInstance) cha).getClan()))
				{
					if (!_actor.isAttackingDisabled() && (cha.getCurrentHp() < (cha.getMaxHp() * 0.6)) && (_actor.getCurrentHp() > (_actor.getMaxHp() / 2)) && (_actor.getCurrentMp() > (_actor.getMaxMp() / 2)) && cha.isInCombat())
					{
						for (L2Skill sk : _selfAnalysis.healSkills)
						{
							if (_actor.getCurrentMp() < sk.getMpConsume())
							{
								continue;
							}
							if (_actor.isSkillDisabled(sk))
							{
								continue;
							}
							if (!Util.checkIfInRange(sk.getCastRange(), _actor, cha, true))
							{
								continue;
							}
							
							int chance = 5;
							if (chance >= Rnd.get(100))
							{
								continue;
							}
							if (!GeoClient.getInstance().canSeeTarget(_actor, cha))
							{
								break;
							}
							
							L2Object OldTarget = _actor.getTarget();
							_actor.setTarget(cha);
							clientStopMoving(null);
							_accessor.doCast(sk);
							_actor.setTarget(OldTarget);
							return;
						}
					}
				}
				continue;
			}
			
			L2Npc npc = (L2Npc) cha;
			
			if (!faction_id.equals(npc.getFactionId()))
			{
				continue;
			}
			
			if (npc.getAI() != null)
			{
				if (!npc.isDead() && (Math.abs(target.getZ() - npc.getZ()) < 600) && ((npc.getAI()._intention == CtrlIntention.AI_INTENTION_IDLE) || (npc.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE)) && target.isInsideRadius(npc, 1500, true, false) && GeoClient.getInstance().canSeeTarget(npc, target))
				{
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 1);
					return;
				}
				
				if (_selfAnalysis.hasHealOrResurrect && !_actor.isAttackingDisabled() && (npc.getCurrentHp() < (npc.getMaxHp() * 0.6)) && (_actor.getCurrentHp() > (_actor.getMaxHp() / 2)) && (_actor.getCurrentMp() > (_actor.getMaxMp() / 2)) && npc.isInCombat())
				{
					for (L2Skill sk : _selfAnalysis.healSkills)
					{
						if (_actor.getCurrentMp() < sk.getMpConsume())
						{
							continue;
						}
						if (_actor.isSkillDisabled(sk))
						{
							continue;
						}
						if (!Util.checkIfInRange(sk.getCastRange(), _actor, npc, true))
						{
							continue;
						}
						
						int chance = 4;
						if (chance >= Rnd.get(100))
						{
							continue;
						}
						if (!GeoClient.getInstance().canSeeTarget(_actor, npc))
						{
							break;
						}
						
						L2Object OldTarget = _actor.getTarget();
						_actor.setTarget(npc);
						clientStopMoving(null);
						_accessor.doCast(sk);
						_actor.setTarget(OldTarget);
						return;
					}
				}
			}
		}
	}
	
	private void attackPrepare()
	{
		Collection<L2Skill> skills = null;
		double dist_2 = 0;
		int range = 0;
		L2DefenderInstance sGuard;
		if (_actor instanceof L2FortCommanderInstance)
		{
			sGuard = (L2FortCommanderInstance) _actor;
		}
		else
		{
			sGuard = (L2DefenderInstance) _actor;
		}
		L2Character attackTarget = getAttackTarget();
		
		try
		{
			_actor.setTarget(attackTarget);
			skills = _actor.getAllSkills();
			dist_2 = _actor.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY());
			range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius() + attackTarget.getTemplate().getCollisionRadius();
			if (attackTarget.isMoving())
			{
				range += 50;
			}
		}
		catch (NullPointerException e)
		{
			_actor.setTarget(null);
			setIntention(AI_INTENTION_IDLE, null, null);
			return;
		}
		
		if ((attackTarget instanceof L2PcInstance) && sGuard.getFort().getSiege().checkIsDefender(((L2PcInstance) attackTarget).getClan()))
		{
			sGuard.stopHating(attackTarget);
			_actor.setTarget(null);
			setIntention(AI_INTENTION_IDLE, null, null);
			return;
		}
		
		if (!GeoClient.getInstance().canSeeTarget(_actor, attackTarget))
		{
			sGuard.stopHating(attackTarget);
			_actor.setTarget(null);
			setIntention(AI_INTENTION_IDLE, null, null);
			return;
		}
		
		if (!_actor.isMuted() && (dist_2 > (range * range)))
		{
			for (L2Skill sk : skills)
			{
				int castRange = sk.getCastRange();
				
				if ((dist_2 <= (castRange * castRange)) && (castRange > 70) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() >= _actor.getStat().getMpConsume(sk)) && !sk.isPassive())
				{
					
					L2Object OldTarget = _actor.getTarget();
					if ((sk.getSkillType() == L2SkillType.BUFF) || (sk.hasEffectType(L2EffectType.HEAL)))
					{
						boolean useSkillSelf = true;
						if ((sk.hasEffectType(L2EffectType.HEAL)) && (_actor.getCurrentHp() > (int) (_actor.getMaxHp() / 1.5)))
						{
							useSkillSelf = false;
							break;
						}
						if (sk.getSkillType() == L2SkillType.BUFF)
						{
							L2Effect[] effects = _actor.getAllEffects();
							for (int i = 0; (effects != null) && (i < effects.length); i++)
							{
								L2Effect effect = effects[i];
								if (effect.getSkill() == sk)
								{
									useSkillSelf = false;
									break;
								}
							}
						}
						if (useSkillSelf)
						{
							_actor.setTarget(_actor);
						}
					}
					
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(OldTarget);
					return;
				}
			}
			
			if (!(_actor.isAttackingNow()) && (_actor.getRunSpeed() == 0) && (_actor.getKnownList().knowsObject(attackTarget)))
			{
				_actor.getKnownList().removeKnownObject(attackTarget);
				_actor.setTarget(null);
				setIntention(AI_INTENTION_IDLE, null, null);
			}
			else
			{
				double dx = _actor.getX() - attackTarget.getX();
				double dy = _actor.getY() - attackTarget.getY();
				double dz = _actor.getZ() - attackTarget.getZ();
				double homeX = attackTarget.getX() - sGuard.getSpawn().getX();
				double homeY = attackTarget.getY() - sGuard.getSpawn().getY();
				
				if ((((dx * dx) + (dy * dy)) > 10000) && (((homeX * homeX) + (homeY * homeY)) > 3240000) && (_actor.getKnownList().knowsObject(attackTarget)))
				{
					_actor.getKnownList().removeKnownObject(attackTarget);
					_actor.setTarget(null);
					setIntention(AI_INTENTION_IDLE, null, null);
				}
				else
				{
					if ((dz * dz) < (170 * 170))
					{
						if (_selfAnalysis.isMage)
						{
							range = _selfAnalysis.maxCastRange - 50;
						}
						if (_actor.getWalkSpeed() <= 0)
						{
							return;
						}
						if (attackTarget.isMoving())
						{
							moveToPawn(attackTarget, range - 70);
						}
						else
						{
							moveToPawn(attackTarget, range);
						}
					}
				}
			}
			return;
			
		}
		else if (_actor.isMuted() && (dist_2 > (range * range)))
		{
			double dz = _actor.getZ() - attackTarget.getZ();
			if ((dz * dz) < (170 * 170))
			{
				if (_selfAnalysis.isMage)
				{
					range = _selfAnalysis.maxCastRange - 50;
				}
				if (_actor.getWalkSpeed() <= 0)
				{
					return;
				}
				if (attackTarget.isMoving())
				{
					moveToPawn(attackTarget, range - 70);
				}
				else
				{
					moveToPawn(attackTarget, range);
				}
			}
			return;
		}
		else if (dist_2 <= (range * range))
		{
			L2Character hated = null;
			if (_actor.isConfused())
			{
				hated = attackTarget;
			}
			else
			{
				hated = ((L2Attackable) _actor).getMostHated();
			}
			
			if (hated == null)
			{
				setIntention(AI_INTENTION_ACTIVE, null, null);
				return;
			}
			if (hated != attackTarget)
			{
				attackTarget = hated;
			}
			
			_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getInstance().getGameTicks();
			
			if (!_actor.isMuted() && (Rnd.nextInt(100) <= 5))
			{
				for (L2Skill sk : skills)
				{
					int castRange = sk.getCastRange();
					
					if (((castRange * castRange) >= dist_2) && !sk.isPassive() && (_actor.getCurrentMp() >= _actor.getStat().getMpConsume(sk)) && !_actor.isSkillDisabled(sk))
					{
						L2Object OldTarget = _actor.getTarget();
						if ((sk.getSkillType() == L2SkillType.BUFF) || (sk.hasEffectType(L2EffectType.HEAL)))
						{
							boolean useSkillSelf = true;
							if ((sk.hasEffectType(L2EffectType.HEAL)) && (_actor.getCurrentHp() > (int) (_actor.getMaxHp() / 1.5)))
							{
								useSkillSelf = false;
								break;
							}
							if (sk.getSkillType() == L2SkillType.BUFF)
							{
								L2Effect[] effects = _actor.getAllEffects();
								for (int i = 0; (effects != null) && (i < effects.length); i++)
								{
									L2Effect effect = effects[i];
									if (effect.getSkill() == sk)
									{
										useSkillSelf = false;
										break;
									}
								}
							}
							if (useSkillSelf)
							{
								_actor.setTarget(_actor);
							}
						}
						clientStopMoving(null);
						_accessor.doCast(sk);
						_actor.setTarget(OldTarget);
						return;
					}
				}
			}
			_accessor.doAttack(attackTarget);
		}
	}
	
	@Override
	protected void onEvtThink()
	{
		if (_thinking || _actor.isCastingNow() || _actor.isAllSkillsDisabled())
		{
			return;
		}
		
		_thinking = true;
		
		try
		{
			if (getIntention() == AI_INTENTION_ACTIVE)
			{
				thinkActive();
			}
			else if (getIntention() == AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
		}
		finally
		{
			_thinking = false;
		}
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getInstance().getGameTicks();
		
		if (_globalAggro < 0)
		{
			_globalAggro = 0;
		}
		
		((L2Attackable) _actor).addDamageHate(attacker, 0, 1);
		
		if (!_actor.isRunning())
		{
			_actor.setRunning();
		}
		
		if (getIntention() != AI_INTENTION_ATTACK)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
		}
		super.onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		if (_actor == null)
		{
			return;
		}
		L2Attackable me = (L2Attackable) _actor;
		
		if (target != null)
		{
			me.addDamageHate(target, 0, aggro);
			
			aggro = me.getHating(target);
			
			if (aggro <= 0)
			{
				if (me.getMostHated() == null)
				{
					_globalAggro = -25;
					me.clearAggroList();
					setIntention(AI_INTENTION_IDLE, null, null);
				}
				return;
			}
			
			if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				if (!_actor.isRunning())
				{
					_actor.setRunning();
				}
				
				L2DefenderInstance sGuard;
				if (_actor instanceof L2FortCommanderInstance)
				{
					sGuard = (L2FortCommanderInstance) _actor;
				}
				else
				{
					sGuard = (L2DefenderInstance) _actor;
				}
				double homeX = target.getX() - sGuard.getSpawn().getX();
				double homeY = target.getY() - sGuard.getSpawn().getY();
				
				if (((homeX * homeX) + (homeY * homeY)) < 3240000)
				{
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
				}
			}
		}
		else
		{
			if (aggro >= 0)
			{
				return;
			}
			
			L2Character mostHated = me.getMostHated();
			if (mostHated == null)
			{
				_globalAggro = -25;
				return;
			}
			
			for (L2Character aggroed : me.getAggroList().keySet())
			{
				me.addDamageHate(aggroed, 0, aggro);
			}
			
			aggro = me.getHating(mostHated);
			if (aggro <= 0)
			{
				_globalAggro = -25;
				me.clearAggroList();
				setIntention(AI_INTENTION_IDLE, null, null);
			}
		}
	}
	
	@Override
	public void stopAITask()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
		_accessor.detachAI();
		super.stopAITask();
	}
}