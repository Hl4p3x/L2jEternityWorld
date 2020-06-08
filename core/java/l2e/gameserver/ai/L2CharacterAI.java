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
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_MOVE_TO;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static l2e.gameserver.ai.CtrlIntention.AI_INTENTION_REST;

import java.util.List;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.geodata.GeoClient;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.instance.L2DoorInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.instance.L2ItemInstance.ItemLocation;
import l2e.gameserver.model.items.type.L2WeaponType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.AutoAttackStop;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;
import l2e.util.Rnd;

public class L2CharacterAI extends AbstractAI
{
	public static class IntentionCommand
	{
		protected final CtrlIntention _crtlIntention;
		protected final Object _arg0, _arg1;
		
		protected IntentionCommand(CtrlIntention pIntention, Object pArg0, Object pArg1)
		{
			_crtlIntention = pIntention;
			_arg0 = pArg0;
			_arg1 = pArg1;
		}
		
		public CtrlIntention getCtrlIntention()
		{
			return _crtlIntention;
		}
	}
	
	public static class CastTask implements Runnable
	{
		private final L2Character _activeChar;
		private final L2Object _target;
		private final L2Skill _skill;
		
		public CastTask(L2Character actor, L2Skill skill, L2Object target)
		{
			_activeChar = actor;
			_target = target;
			_skill = skill;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isAttackingNow())
			{
				_activeChar.abortAttack();
			}
			_activeChar.getAI().changeIntentionToCast(_skill, _target);
		}
	}
	
	public L2CharacterAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
	}
	
	public IntentionCommand getNextIntention()
	{
		return null;
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		if ((attacker instanceof L2Attackable) && !((L2Attackable) attacker).isCoreAIDisabled())
		{
			clientStartAutoAttack();
		}
	}
	
	@Override
	protected void onIntentionIdle()
	{
		changeIntention(AI_INTENTION_IDLE, null, null);
		
		setCastTarget(null);
		setAttackTarget(null);
		
		clientStopMoving(null);
		clientStopAutoAttack();
		
	}
	
	@Override
	protected void onIntentionActive()
	{
		if (getIntention() != AI_INTENTION_ACTIVE)
		{
			changeIntention(AI_INTENTION_ACTIVE, null, null);
			
			setCastTarget(null);
			setAttackTarget(null);
			
			clientStopMoving(null);
			clientStopAutoAttack();
			
			if (_actor instanceof L2Attackable)
			{
				((L2Npc) _actor).startRandomAnimationTimer();
			}
			
			onEvtThink();
		}
	}
	
	@Override
	protected void onIntentionRest()
	{
		setIntention(AI_INTENTION_IDLE);
	}
	
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		if (target == null)
		{
			clientActionFailed();
			return;
		}
		
		if (getIntention() == AI_INTENTION_REST)
		{
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAfraid())
		{
			clientActionFailed();
			return;
		}
		
		if (getIntention() == AI_INTENTION_ATTACK)
		{
			if (getAttackTarget() != target)
			{
				setAttackTarget(target);
				
				stopFollow();
				
				notifyEvent(CtrlEvent.EVT_THINK, null);
				
			}
			else
			{
				clientActionFailed();
			}
		}
		else
		{
			changeIntention(AI_INTENTION_ATTACK, target, null);
			
			setAttackTarget(target);
			
			stopFollow();
			
			notifyEvent(CtrlEvent.EVT_THINK, null);
		}
	}
	
	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		if ((getIntention() == AI_INTENTION_REST) && skill.isMagic())
		{
			clientActionFailed();
			_actor.setIsCastingNow(false);
			return;
		}
		
		if (_actor.getBowAttackEndTime() > GameTimeController.getInstance().getGameTicks())
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new CastTask(_actor, skill, target), (_actor.getBowAttackEndTime() - GameTimeController.getInstance().getGameTicks()) * GameTimeController.MILLIS_IN_TICK);
		}
		else
		{
			changeIntentionToCast(skill, target);
		}
	}
	
	protected void changeIntentionToCast(L2Skill skill, L2Object target)
	{
		setCastTarget((L2Character) target);
		_skill = skill;
		changeIntention(AI_INTENTION_CAST, skill, target);
		
		notifyEvent(CtrlEvent.EVT_THINK, null);
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
	protected void onIntentionFollow(L2Character target)
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
		
		if (_actor.isMovementDisabled())
		{
			clientActionFailed();
			return;
		}
		
		if (_actor.isDead())
		{
			clientActionFailed();
			return;
		}
		
		if (_actor == target)
		{
			clientActionFailed();
			return;
		}
		clientStopAutoAttack();
		changeIntention(AI_INTENTION_FOLLOW, target, null);
		startFollow(target);
	}
	
	@Override
	protected void onIntentionPickUp(L2Object object)
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
		clientStopAutoAttack();
		
		if ((object instanceof L2ItemInstance) && (((L2ItemInstance) object).getItemLocation() != ItemLocation.VOID))
		{
			return;
		}
		
		changeIntention(AI_INTENTION_PICK_UP, object, null);
		
		setTarget(object);
		if ((object.getX() == 0) && (object.getY() == 0))
		{
			_log.warning("Object in coords 0,0 - using a temporary fix");
			object.setXYZ(getActor().getX(), getActor().getY(), getActor().getZ() + 5);
		}
		moveToPawn(object, 20);
	}
	
	@Override
	protected void onIntentionInteract(L2Object object)
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
		clientStopAutoAttack();
		
		if (getIntention() != AI_INTENTION_INTERACT)
		{
			changeIntention(AI_INTENTION_INTERACT, object, null);
			setTarget(object);
			moveToPawn(object, 60);
		}
	}
	
	@Override
	protected void onEvtThink()
	{
	}
	
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
	}
	
	@Override
	protected void onEvtStunned(L2Character attacker)
	{
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}
		
		setAutoAttacking(false);
		clientStopMoving(null);
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtParalyzed(L2Character attacker)
	{
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}
		
		setAutoAttacking(false);
		clientStopMoving(null);
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtSleeping(L2Character attacker)
	{
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}
		
		setAutoAttacking(false);
		clientStopMoving(null);
	}
	
	@Override
	protected void onEvtRooted(L2Character attacker)
	{
		clientStopMoving(null);
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtConfused(L2Character attacker)
	{
		clientStopMoving(null);
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtMuted(L2Character attacker)
	{
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtEvaded(L2Character attacker)
	{
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
		onEvtThink();
	}
	
	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1)
	{
	}
	
	@Override
	protected void onEvtArrived()
	{
		_accessor.getActor().revalidateZone(true);
		
		if (_accessor.getActor().moveToNextRoutePoint())
		{
			return;
		}
		
		if (_accessor.getActor() instanceof L2Attackable)
		{
			((L2Attackable) _accessor.getActor()).setisReturningToSpawnPoint(false);
		}
		clientStoppedMoving();
		
		if (_actor instanceof L2Npc)
		{
			L2Npc npc = (L2Npc) _actor;
			WalkingManager.getInstance().onArrived(npc);
			
			if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_MOVE_FINISHED) != null)
			{
				for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_MOVE_FINISHED))
				{
					quest.notifyMoveFinished(npc);
				}
			}
		}
		
		if (getIntention() == AI_INTENTION_MOVE_TO)
		{
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		onEvtThink();
	}
	
	@Override
	protected void onEvtArrivedRevalidate()
	{
		onEvtThink();
	}
	
	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_loc)
	{
		if ((getIntention() == AI_INTENTION_MOVE_TO) || (getIntention() == AI_INTENTION_CAST))
		{
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		clientStopMoving(blocked_at_loc);
		onEvtThink();
	}
	
	@Override
	protected void onEvtForgetObject(L2Object object)
	{
		if (getTarget() == object)
		{
			setTarget(null);
			
			if ((getIntention() == AI_INTENTION_INTERACT) || (getIntention() == AI_INTENTION_PICK_UP))
			{
				setIntention(AI_INTENTION_ACTIVE);
			}
		}
		
		if (getAttackTarget() == object)
		{
			setAttackTarget(null);
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		if (getCastTarget() == object)
		{
			setCastTarget(null);
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		if (getFollowTarget() == object)
		{
			clientStopMoving(null);
			stopFollow();
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		if (_actor == object)
		{
			setTarget(null);
			setAttackTarget(null);
			setCastTarget(null);
			stopFollow();
			clientStopMoving(null);
			changeIntention(AI_INTENTION_IDLE, null, null);
		}
	}
	
	@Override
	protected void onEvtCancel()
	{
		_actor.abortCast();
		stopFollow();
		
		if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		}
		
		onEvtThink();
	}
	
	@Override
	protected void onEvtDead()
	{
		stopAITask();
		clientNotifyDead();
		
		if (!(_actor instanceof L2Playable))
		{
			_actor.setWalking();
		}
	}
	
	@Override
	protected void onEvtFakeDeath()
	{
		stopFollow();
		clientStopMoving(null);
		_intention = AI_INTENTION_IDLE;
		setTarget(null);
		setCastTarget(null);
		setAttackTarget(null);
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
	}
	
	protected boolean maybeMoveToPosition(Location worldPosition, int offset)
	{
		if (worldPosition == null)
		{
			_log.warning("maybeMoveToPosition: worldPosition == NULL!");
			return false;
		}
		
		if (offset < 0)
		{
			return false;
		}
		
		if (!_actor.isInsideRadius(worldPosition.getX(), worldPosition.getY(), offset + _actor.getTemplate().getCollisionRadius(), false))
		{
			if (_actor.isMovementDisabled())
			{
				return true;
			}
			
			if (!_actor.isRunning() && !(this instanceof L2PlayerAI) && !(this instanceof L2SummonAI))
			{
				_actor.setRunning();
			}
			
			stopFollow();
			
			int x = _actor.getX();
			int y = _actor.getY();
			
			double dx = worldPosition.getX() - x;
			double dy = worldPosition.getY() - y;
			
			double dist = Math.sqrt((dx * dx) + (dy * dy));
			
			double sin = dy / dist;
			double cos = dx / dist;
			
			dist -= offset - 5;
			
			x += (int) (dist * cos);
			y += (int) (dist * sin);
			
			moveTo(x, y, worldPosition.getZ());
			return true;
		}
		
		if (getFollowTarget() != null)
		{
			stopFollow();
		}
		
		return false;
	}
	
	protected boolean maybeMoveToPawn(L2Object target, int offset)
	{
		if (target == null)
		{
			_log.warning("maybeMoveToPawn: target == NULL!");
			return false;
		}
		if (offset < 0)
		{
			return false;
		}
		
		offset += _actor.getTemplate().getCollisionRadius();
		if (target instanceof L2Character)
		{
			offset += ((L2Character) target).getTemplate().getCollisionRadius();
		}
		
		if (!_actor.isInsideRadius(target, offset, false, false))
		{
			if (getFollowTarget() != null)
			{
				if (!_actor.isInsideRadius(target, offset + 100, false, false))
				{
					return true;
				}
				stopFollow();
				return false;
			}
			
			if (_actor.isMovementDisabled())
			{
				if (_actor.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				{
					_actor.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
				
				return true;
			}
			
			if ((_actor.getAI().getIntention() == CtrlIntention.AI_INTENTION_CAST) && (_actor instanceof L2PcInstance) && ((L2PcInstance) _actor).isTransformed())
			{
				if (!((L2PcInstance) _actor).getTransformation().isCombat())
				{
					_actor.sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
					_actor.sendPacket(ActionFailed.STATIC_PACKET);
					return true;
				}
			}
			
			if (!_actor.isRunning() && !(this instanceof L2PlayerAI) && !(this instanceof L2SummonAI))
			{
				_actor.setRunning();
			}
			
			stopFollow();
			if ((target instanceof L2Character) && !(target instanceof L2DoorInstance))
			{
				if (((L2Character) target).isMoving())
				{
					offset -= 100;
				}
				if (offset < 5)
				{
					offset = 5;
				}
				
				startFollow((L2Character) target, offset);
			}
			else
			{
				moveToPawn(target, offset);
			}
			return true;
		}
		
		if (getFollowTarget() != null)
		{
			stopFollow();
		}
		
		return false;
	}
	
	protected boolean checkTargetLostOrDead(L2Character target)
	{
		if ((target == null) || target.isAlikeDead())
		{
			if ((target instanceof L2PcInstance) && ((L2PcInstance) target).isFakeDeath())
			{
				target.stopFakeDeath(true);
				return false;
			}
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		return false;
	}
	
	protected boolean checkTargetLost(L2Object target)
	{
		if (target instanceof L2PcInstance)
		{
			L2PcInstance target2 = (L2PcInstance) target;
			
			if (target2.isFakeDeath())
			{
				target2.stopFakeDeath(true);
				return false;
			}
		}
		
		if (target == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		
		if ((_actor != null) && (_skill != null) && _skill.isOffensive() && (_skill.getAffectRange() > 0) && (Config.GEODATA) && !GeoClient.getInstance().canSeeTarget(_actor, target))
		{
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		return false;
	}
	
	protected class SelfAnalysis
	{
		public boolean isMage = false;
		public boolean isBalanced;
		public boolean isArcher = false;
		public boolean isHealer = false;
		public boolean isFighter = false;
		public boolean cannotMoveOnLand = false;
		public List<L2Skill> generalSkills = new FastList<>();
		public List<L2Skill> buffSkills = new FastList<>();
		public int lastBuffTick = 0;
		public List<L2Skill> debuffSkills = new FastList<>();
		public int lastDebuffTick = 0;
		public List<L2Skill> cancelSkills = new FastList<>();
		public List<L2Skill> healSkills = new FastList<>();
		public List<L2Skill> generalDisablers = new FastList<>();
		public List<L2Skill> sleepSkills = new FastList<>();
		public List<L2Skill> rootSkills = new FastList<>();
		public List<L2Skill> muteSkills = new FastList<>();
		public List<L2Skill> resurrectSkills = new FastList<>();
		public boolean hasHealOrResurrect = false;
		public boolean hasLongRangeSkills = false;
		public boolean hasLongRangeDamageSkills = false;
		public int maxCastRange = 0;
		
		public SelfAnalysis()
		{
		}
		
		public void init()
		{
			switch (((L2NpcTemplate) _actor.getTemplate()).getAIDataStatic().getAiType())
			{
				case FIGHTER:
					isFighter = true;
					break;
				case MAGE:
					isMage = true;
					break;
				case CORPSE:
				case BALANCED:
					isBalanced = true;
					break;
				case ARCHER:
					isArcher = true;
					break;
				case HEALER:
					isHealer = true;
					break;
				default:
					isFighter = true;
					break;
			}
			
			if (_actor instanceof L2Npc)
			{
				int npcId = ((L2Npc) _actor).getId();
				
				switch (npcId)
				{
					case 20314:
					case 20849:
						cannotMoveOnLand = true;
						break;
					default:
						cannotMoveOnLand = false;
						break;
				}
			}
			
			for (L2Skill sk : _actor.getAllSkills())
			{
				if (sk.isPassive())
				{
					continue;
				}
				int castRange = sk.getCastRange();
				boolean hasLongRangeDamageSkill = false;
				switch (sk.getSkillType())
				{
					case BUFF:
						buffSkills.add(sk);
						continue;
					case PARALYZE:
					case STUN:
						switch (sk.getId())
						{
							case 367:
							case 4111:
							case 4383:
							case 4616:
							case 4578:
								sleepSkills.add(sk);
								break;
							default:
								generalDisablers.add(sk);
								break;
						}
						break;
					case MUTE:
						muteSkills.add(sk);
						break;
					case SLEEP:
						sleepSkills.add(sk);
						break;
					case ROOT:
						rootSkills.add(sk);
						break;
					case FEAR:
					case CONFUSION:
					case DEBUFF:
						debuffSkills.add(sk);
						break;
					case RESURRECT:
						resurrectSkills.add(sk);
						hasHealOrResurrect = true;
						break;
					case NOTDONE:
					case COREDONE:
						continue;
					default:
						if (sk.hasEffectType(L2EffectType.CANCEL, L2EffectType.CANCEL_ALL, L2EffectType.NEGATE))
						{
							cancelSkills.add(sk);
						}
						else if (sk.hasEffectType(L2EffectType.HEAL, L2EffectType.HEAL_PERCENT))
						{
							healSkills.add(sk);
							hasHealOrResurrect = true;
						}
						else
						{
							generalSkills.add(sk);
							hasLongRangeDamageSkill = true;
						}
						break;
				}
				
				if (castRange > 70)
				{
					hasLongRangeSkills = true;
					if (hasLongRangeDamageSkill)
					{
						hasLongRangeDamageSkills = true;
					}
				}
				
				if (castRange > maxCastRange)
				{
					maxCastRange = castRange;
				}
				
			}
			
			if (!hasLongRangeDamageSkills && isMage)
			{
				isBalanced = true;
				isMage = false;
				isFighter = false;
			}
			
			if (!hasLongRangeSkills && (isMage || isBalanced))
			{
				isBalanced = false;
				isMage = false;
				isFighter = true;
			}
			
			if (generalSkills.isEmpty() && isMage)
			{
				isBalanced = true;
				isMage = false;
			}
		}
	}
	
	protected class TargetAnalysis
	{
		public L2Character character;
		public boolean isMage;
		public boolean isBalanced;
		public boolean isArcher;
		public boolean isFighter;
		public boolean isCanceled;
		public boolean isSlower;
		public boolean isMagicResistant;
		
		public TargetAnalysis()
		{
		}
		
		public void update(L2Character target)
		{
			if ((target == character) && (Rnd.nextInt(100) > 25))
			{
				return;
			}
			character = target;
			if (target == null)
			{
				return;
			}
			isMage = false;
			isBalanced = false;
			isArcher = false;
			isFighter = false;
			isCanceled = false;
			
			if (target.getMAtk(null, null) > (1.5 * target.getPAtk(null)))
			{
				isMage = true;
			}
			else if (((target.getPAtk(null) * 0.8) < target.getMAtk(null, null)) || ((target.getMAtk(null, null) * 0.8) > target.getPAtk(null)))
			{
				isBalanced = true;
			}
			else
			{
				L2Weapon weapon = target.getActiveWeaponItem();
				if ((weapon != null) && ((weapon.getItemType() == L2WeaponType.BOW) || (weapon.getItemType() == L2WeaponType.CROSSBOW)))
				{
					isArcher = true;
				}
				else
				{
					isFighter = true;
				}
			}
			if (target.getRunSpeed() < (_actor.getRunSpeed() - 3))
			{
				isSlower = true;
			}
			else
			{
				isSlower = false;
			}
			if ((target.getMDef(null, null) * 1.2) > _actor.getMAtk(null, null))
			{
				isMagicResistant = true;
			}
			else
			{
				isMagicResistant = false;
			}
			if (target.getBuffCount() < 4)
			{
				isCanceled = true;
			}
		}
	}
	
	public boolean canAura(L2Skill sk)
	{
		if ((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA) || (sk.getTargetType() == L2TargetType.AURA_CORPSE_MOB))
		{
			for (L2Object target : _actor.getKnownList().getKnownCharactersInRadius(sk.getAffectRange()))
			{
				if (target == getAttackTarget())
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean canAOE(L2Skill sk)
	{
		if (sk.hasEffectType(L2EffectType.CANCEL, L2EffectType.CANCEL_ALL, L2EffectType.NEGATE))
		{
			if ((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA) || (sk.getTargetType() == L2TargetType.AURA_CORPSE_MOB))
			{
				boolean cancast = true;
				for (L2Character target : _actor.getKnownList().getKnownCharactersInRadius(sk.getAffectRange()))
				{
					if (!GeoClient.getInstance().canSeeTarget(_actor, target))
					{
						continue;
					}
					if (target instanceof L2Attackable)
					{
						L2Npc targets = ((L2Npc) target);
						L2Npc actors = ((L2Npc) _actor);
						
						if ((targets.getEnemyClan() == null) || (actors.getClan() == null) || !targets.getEnemyClan().equals(actors.getClan()) || ((actors.getClan() == null) && (actors.getIsChaos() == 0)))
						{
							continue;
						}
					}
					
					L2Effect[] effects = target.getAllEffects();
					for (int i = 0; (effects != null) && (i < effects.length); i++)
					{
						L2Effect effect = effects[i];
						if (effect.getSkill() == sk)
						{
							cancast = false;
							break;
						}
					}
				}
				if (cancast)
				{
					return true;
				}
			}
			else if ((sk.getTargetType() == L2TargetType.AREA) || (sk.getTargetType() == L2TargetType.BEHIND_AREA) || (sk.getTargetType() == L2TargetType.FRONT_AREA))
			{
				boolean cancast = true;
				for (L2Character target : getAttackTarget().getKnownList().getKnownCharactersInRadius(sk.getAffectRange()))
				{
					if (!GeoClient.getInstance().canSeeTarget(_actor, target) || (target == null))
					{
						continue;
					}
					if (target instanceof L2Attackable)
					{
						L2Npc targets = ((L2Npc) target);
						L2Npc actors = ((L2Npc) _actor);
						if ((targets.getEnemyClan() == null) || (actors.getClan() == null) || !targets.getEnemyClan().equals(actors.getClan()) || ((actors.getClan() == null) && (actors.getIsChaos() == 0)))
						{
							continue;
						}
					}
					L2Effect[] effects = target.getAllEffects();
					if (effects.length > 0)
					{
						cancast = true;
					}
				}
				
				if (cancast)
				{
					return true;
				}
			}
		}
		else
		{
			if ((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA) || (sk.getTargetType() == L2TargetType.AURA_CORPSE_MOB))
			{
				boolean cancast = false;
				for (L2Character target : _actor.getKnownList().getKnownCharactersInRadius(sk.getAffectRange()))
				{
					if (!GeoClient.getInstance().canSeeTarget(_actor, target))
					{
						continue;
					}
					if (target instanceof L2Attackable)
					{
						L2Npc targets = ((L2Npc) target);
						L2Npc actors = ((L2Npc) _actor);
						if ((targets.getEnemyClan() == null) || (actors.getClan() == null) || !targets.getEnemyClan().equals(actors.getClan()) || ((actors.getClan() == null) && (actors.getIsChaos() == 0)))
						{
							continue;
						}
					}
					L2Effect[] effects = target.getAllEffects();
					if (effects.length > 0)
					{
						cancast = true;
					}
				}
				if (cancast)
				{
					return true;
				}
			}
			else if ((sk.getTargetType() == L2TargetType.AREA) || (sk.getTargetType() == L2TargetType.BEHIND_AREA) || (sk.getTargetType() == L2TargetType.FRONT_AREA))
			{
				boolean cancast = true;
				for (L2Character target : getAttackTarget().getKnownList().getKnownCharactersInRadius(sk.getAffectRange()))
				{
					if (!GeoClient.getInstance().canSeeTarget(_actor, target))
					{
						continue;
					}
					if (target instanceof L2Attackable)
					{
						L2Npc targets = ((L2Npc) target);
						L2Npc actors = ((L2Npc) _actor);
						if ((targets.getEnemyClan() == null) || (actors.getClan() == null) || !targets.getEnemyClan().equals(actors.getClan()) || ((actors.getClan() == null) && (actors.getIsChaos() == 0)))
						{
							continue;
						}
					}
					
					L2Effect[] effects = target.getAllEffects();
					for (int i = 0; (effects != null) && (i < effects.length); i++)
					{
						L2Effect effect = effects[i];
						if (effect.getSkill() == sk)
						{
							cancast = false;
							break;
						}
					}
				}
				
				if (cancast)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean canParty(L2Skill sk)
	{
		if (sk.getTargetType() == L2TargetType.PARTY)
		{
			int count = 0;
			int ccount = 0;
			for (L2Character target : _actor.getKnownList().getKnownCharactersInRadius(sk.getAffectRange()))
			{
				if (!(target instanceof L2Attackable) || !GeoClient.getInstance().canSeeTarget(_actor, target))
				{
					continue;
				}
				L2Npc targets = ((L2Npc) target);
				L2Npc actors = ((L2Npc) _actor);
				if ((actors.getFactionId() != null) && targets.getFactionId().equals(actors.getFactionId()))
				{
					count++;
					L2Effect[] effects = target.getAllEffects();
					for (int i = 0; (effects != null) && (i < effects.length); i++)
					{
						L2Effect effect = effects[i];
						if (effect.getSkill() == sk)
						{
							ccount++;
							break;
						}
					}
				}
			}
			if (ccount < count)
			{
				return true;
			}
			
		}
		return false;
	}
	
	public boolean isParty(L2Skill sk)
	{
		if (sk.getTargetType() == L2TargetType.PARTY)
		{
			return true;
		}
		return false;
	}
}