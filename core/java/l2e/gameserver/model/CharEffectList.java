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
package l2e.gameserver.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2e.Config;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.effects.EffectFlag;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.olympiad.OlympiadGameManager;
import l2e.gameserver.model.olympiad.OlympiadGameTask;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.L2SkillType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AbnormalStatusUpdate;
import l2e.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import l2e.gameserver.network.serverpackets.PartySpelled;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CharEffectList
{
	protected static final Logger _log = Logger.getLogger(CharEffectList.class.getName());
	private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];
	
	private FastList<L2Effect> _buffs;
	private FastList<L2Effect> _debuffs;
	private FastList<L2Effect> _passives;
	
	private Map<String, List<L2Effect>> _stackedEffects;
	
	private volatile boolean _hasBuffsRemovedOnAnyAction = false;
	private volatile boolean _hasBuffsRemovedOnDamage = false;
	private volatile boolean _hasDebuffsRemovedOnDamage = false;
	
	private boolean _queuesInitialized = false;
	private LinkedBlockingQueue<L2Effect> _addQueue;
	private LinkedBlockingQueue<L2Effect> _removeQueue;
	private final AtomicBoolean queueLock = new AtomicBoolean();
	private int _effectFlags;
	
	private boolean _partyOnly = false;
	private final L2Character _owner;
	
	private L2Effect[] _effectCache;
	private volatile boolean _rebuildCache = true;
	private final Object _buildEffectLock = new Object();
	
	public CharEffectList(L2Character owner)
	{
		_owner = owner;
	}
	
	public final L2Effect[] getAllEffects()
	{
		if (((_buffs == null) || _buffs.isEmpty()) && ((_debuffs == null) || _debuffs.isEmpty()))
		{
			return EMPTY_EFFECTS;
		}
		
		synchronized (_buildEffectLock)
		{
			if (!_rebuildCache)
			{
				return _effectCache;
			}
			
			_rebuildCache = false;
			
			FastList<L2Effect> temp = FastList.newInstance();
			
			if ((_buffs != null) && !_buffs.isEmpty())
			{
				temp.addAll(_buffs);
			}
			if ((_debuffs != null) && !_debuffs.isEmpty())
			{
				temp.addAll(_debuffs);
			}
			
			L2Effect[] tempArray = new L2Effect[temp.size()];
			temp.toArray(tempArray);
			return (_effectCache = tempArray);
		}
	}
	
	public final L2Effect getFirstEffect(L2EffectType tp)
	{
		L2Effect effectNotInUse = null;
		
		if ((_buffs != null) && !_buffs.isEmpty())
		{
			for (L2Effect e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getEffectType() == tp)
				{
					if (e.isInUse())
					{
						return e;
					}
					
					effectNotInUse = e;
				}
			}
		}
		
		if ((effectNotInUse == null) && (_debuffs != null) && !_debuffs.isEmpty())
		{
			for (L2Effect e : _debuffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getEffectType() == tp)
				{
					if (e.isInUse())
					{
						return e;
					}
					
					effectNotInUse = e;
				}
			}
		}
		return effectNotInUse;
	}
	
	public final L2Effect getFirstEffect(L2Skill skill)
	{
		L2Effect effectNotInUse = null;
		
		if (skill.isDebuff())
		{
			if ((_debuffs != null) && !_debuffs.isEmpty())
			{
				for (L2Effect e : _debuffs)
				{
					if (e == null)
					{
						continue;
					}
					
					if (e.getSkill() == skill)
					{
						if (e.isInUse())
						{
							return e;
						}
						
						effectNotInUse = e;
					}
				}
			}
		}
		else
		{
			if ((_buffs != null) && !_buffs.isEmpty())
			{
				for (L2Effect e : _buffs)
				{
					if (e == null)
					{
						continue;
					}
					
					if (e.getSkill() == skill)
					{
						if (e.isInUse())
						{
							return e;
						}
						
						effectNotInUse = e;
					}
				}
			}
		}
		return effectNotInUse;
	}
	
	public final L2Effect getFirstEffect(int skillId)
	{
		L2Effect effectNotInUse = null;
		
		if ((_buffs != null) && !_buffs.isEmpty())
		{
			for (L2Effect e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getSkill().getId() == skillId)
				{
					if (e.isInUse())
					{
						return e;
					}
					
					effectNotInUse = e;
				}
			}
		}
		
		if ((effectNotInUse == null) && (_debuffs != null) && !_debuffs.isEmpty())
		{
			for (L2Effect e : _debuffs)
			{
				if (e == null)
				{
					continue;
				}
				if (e.getSkill().getId() == skillId)
				{
					if (e.isInUse())
					{
						return e;
					}
					
					effectNotInUse = e;
				}
			}
		}
		return effectNotInUse;
	}
	
	public final L2Effect getFirstPassiveEffect(L2EffectType type)
	{
		if (_passives != null)
		{
			for (L2Effect e : _passives)
			{
				if ((e != null) && (e.getEffectType() == type))
				{
					if (e.isInUse())
					{
						return e;
					}
				}
			}
		}
		return null;
	}
	
	private boolean doesStack(L2Skill checkSkill)
	{
		if (((_buffs == null) || _buffs.isEmpty()) || (checkSkill._effectTemplates == null) || (checkSkill._effectTemplates.length < 1) || (checkSkill._effectTemplates[0].abnormalType == null) || "none".equals(checkSkill._effectTemplates[0].abnormalType))
		{
			return false;
		}
		
		String stackType = checkSkill._effectTemplates[0].abnormalType;
		
		for (L2Effect e : _buffs)
		{
			if ((e.getAbnormalType() != null) && e.getAbnormalType().equals(stackType))
			{
				return true;
			}
		}
		return false;
	}
	
	public int getBuffCount()
	{
		if ((_buffs == null) || _buffs.isEmpty())
		{
			return 0;
		}
		
		int buffCount = 0;
		for (L2Effect e : _buffs)
		{
			if ((e != null) && e.isIconDisplay() && !e.getSkill().isDance() && !e.getSkill().isTriggeredSkill() && !e.getSkill().is7Signs())
			{
				if (e.getSkill().getSkillType() == L2SkillType.BUFF)
				{
					buffCount++;
				}
			}
		}
		
		return buffCount;
	}
	
	public int getDanceCount()
	{
		if ((_buffs == null) || _buffs.isEmpty())
		{
			return 0;
		}
		
		int danceCount = 0;
		for (L2Effect e : _buffs)
		{
			if ((e != null) && e.getSkill().isDance() && e.isInUse())
			{
				danceCount++;
			}
		}
		
		return danceCount;
	}
	
	public int getTriggeredBuffCount()
	{
		if (_buffs == null)
		{
			return 0;
		}
		int activationBuffCount = 0;
		
		// synchronized(_buffs)
		{
			if (_buffs.isEmpty())
			{
				return 0;
			}
			
			for (L2Effect e : _buffs)
			{
				if ((e != null) && e.getSkill().isTriggeredSkill() && e.isInUse())
				{
					activationBuffCount++;
				}
			}
		}
		return activationBuffCount;
	}
	
	public final void stopAllEffects()
	{
		L2Effect[] effects = getAllEffects();
		
		for (L2Effect e : effects)
		{
			if (e != null)
			{
				e.exit(true);
			}
		}
	}
	
	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		L2Effect[] effects = getAllEffects();
		
		for (L2Effect e : effects)
		{
			if ((e != null) && !e.getSkill().isStayAfterDeath())
			{
				e.exit(true);
			}
		}
	}
	
	public void stopAllToggles()
	{
		if ((_buffs != null) && !_buffs.isEmpty())
		{
			for (L2Effect e : _buffs)
			{
				if ((e != null) && e.getSkill().isToggle())
				{
					e.exit();
				}
			}
		}
	}
	
	public final void stopEffects(L2EffectType type)
	{
		if ((_buffs != null) && !_buffs.isEmpty())
		{
			for (L2Effect e : _buffs)
			{
				if ((e != null) && (e.getEffectType() == type))
				{
					e.exit();
				}
			}
		}
		
		if ((_debuffs != null) && !_debuffs.isEmpty())
		{
			for (L2Effect e : _debuffs)
			{
				if ((e != null) && (e.getEffectType() == type))
				{
					e.exit();
				}
			}
		}
	}
	
	public final void stopSkillEffects(int skillId)
	{
		if ((_buffs != null) && !_buffs.isEmpty())
		{
			for (L2Effect e : _buffs)
			{
				if ((e != null) && (e.getSkill().getId() == skillId))
				{
					e.exit();
				}
			}
		}
		if ((_debuffs != null) && !_debuffs.isEmpty())
		{
			for (L2Effect e : _debuffs)
			{
				if ((e != null) && (e.getSkill().getId() == skillId))
				{
					e.exit();
				}
			}
		}
	}
	
	public void stopEffectsOnAction()
	{
		if (_hasBuffsRemovedOnAnyAction)
		{
			if ((_buffs != null) && !_buffs.isEmpty())
			{
				for (L2Effect e : _buffs)
				{
					if ((e != null) && e.getSkill().isRemovedOnAnyActionExceptMove())
					{
						e.exit(true);
					}
				}
			}
		}
	}
	
	public void stopEffectsOnDamage(boolean awake)
	{
		if (_hasBuffsRemovedOnDamage)
		{
			if ((_buffs != null) && !_buffs.isEmpty())
			{
				for (L2Effect e : _buffs)
				{
					if ((e != null) && e.getSkill().isRemovedOnDamage() && (awake || (e.getSkill().getSkillType() != L2SkillType.SLEEP)))
					{
						e.exit(true);
					}
				}
			}
		}
		if (_hasDebuffsRemovedOnDamage)
		{
			if ((_debuffs != null) && !_debuffs.isEmpty())
			{
				for (L2Effect e : _debuffs)
				{
					if ((e != null) && e.getSkill().isRemovedOnDamage() && (awake || (e.getSkill().getSkillType() != L2SkillType.SLEEP)))
					{
						e.exit(true);
					}
				}
			}
		}
	}
	
	public void updateEffectIcons(boolean partyOnly)
	{
		if ((_buffs == null) && (_debuffs == null))
		{
			return;
		}
		
		if (partyOnly)
		{
			_partyOnly = true;
		}
		
		queueRunner();
	}
	
	public void queueEffect(L2Effect effect, boolean remove)
	{
		if (effect == null)
		{
			return;
		}
		
		if (!_queuesInitialized)
		{
			init();
		}
		
		if (remove)
		{
			_removeQueue.offer(effect);
		}
		else
		{
			_addQueue.offer(effect);
		}
		
		queueRunner();
	}
	
	private synchronized void init()
	{
		if (_queuesInitialized)
		{
			return;
		}
		_addQueue = new LinkedBlockingQueue<>();
		_removeQueue = new LinkedBlockingQueue<>();
		_queuesInitialized = true;
	}
	
	private void queueRunner()
	{
		if (!queueLock.compareAndSet(false, true))
		{
			return;
		}
		
		try
		{
			L2Effect effect;
			do
			{
				while ((effect = _removeQueue.poll()) != null)
				{
					removeEffectFromQueue(effect);
					_partyOnly = false;
				}
				
				if ((effect = _addQueue.poll()) != null)
				{
					addEffectFromQueue(effect);
					_partyOnly = false;
				}
			}
			while (!_addQueue.isEmpty() || !_removeQueue.isEmpty());
			
			computeEffectFlags();
			updateEffectIcons();
		}
		finally
		{
			queueLock.set(false);
		}
	}
	
	protected void removeEffectFromQueue(L2Effect effect)
	{
		if (effect == null)
		{
			return;
		}
		
		if (effect.getSkill().isPassive())
		{
			if (effect.setInUse(false))
			{
				_owner.removeStatsOwner(effect.getStatFuncs());
				_passives.remove(effect);
			}
		}
		
		FastList<L2Effect> effectList;
		
		_rebuildCache = true;
		
		if (effect.getSkill().isDebuff())
		{
			if (_debuffs == null)
			{
				return;
			}
			effectList = _debuffs;
		}
		else
		{
			if (_buffs == null)
			{
				return;
			}
			effectList = _buffs;
		}
		
		if ("none".equals(effect.getAbnormalType()))
		{
			_owner.removeStatsOwner(effect);
		}
		else
		{
			if (_stackedEffects == null)
			{
				return;
			}
			
			List<L2Effect> stackQueue = _stackedEffects.get(effect.getAbnormalType());
			
			if ((stackQueue == null) || stackQueue.isEmpty())
			{
				return;
			}
			
			int index = stackQueue.indexOf(effect);
			
			if (index >= 0)
			{
				stackQueue.remove(effect);
				
				if (index == 0)
				{
					_owner.removeStatsOwner(effect);
					
					if (!stackQueue.isEmpty())
					{
						L2Effect newStackedEffect = listsContains(stackQueue.get(0));
						if (newStackedEffect != null)
						{
							if (newStackedEffect.setInUse(true))
							{
								_owner.addStatFuncs(newStackedEffect.getStatFuncs());
							}
						}
					}
				}
				if (stackQueue.isEmpty())
				{
					_stackedEffects.remove(effect.getAbnormalType());
				}
				else
				{
					_stackedEffects.put(effect.getAbnormalType(), stackQueue);
				}
			}
		}
		
		if (effectList.remove(effect) && _owner.isPlayer() && effect.isIconDisplay())
		{
			SystemMessage sm;
			if (effect.getSkill().isToggle())
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED);
			}
			sm.addSkillName(effect);
			_owner.sendPacket(sm);
		}
	}
	
	protected void addEffectFromQueue(L2Effect newEffect)
	{
		if (newEffect == null)
		{
			return;
		}
		
		L2Skill newSkill = newEffect.getSkill();
		
		if (newEffect.getSkill().isPassive())
		{
			if (_passives == null)
			{
				_passives = new FastList<L2Effect>().shared();
			}
			
			if ("none".equals(newEffect.getAbnormalType()))
			{
				if (newEffect.setInUse(true))
				{
					for (L2Effect eff : _passives)
					{
						if (eff == null)
						{
							continue;
						}
						
						if (eff.getEffectTemplate().equals(newEffect.getEffectTemplate()))
						{
							eff.exit();
						}
						
					}
					_owner.addStatFuncs(newEffect.getStatFuncs());
					_passives.add(newEffect);
				}
			}
			
			return;
		}
		_rebuildCache = true;
		
		if (newSkill.isDebuff())
		{
			if (_debuffs == null)
			{
				_debuffs = new FastList<L2Effect>().shared();
			}
			for (L2Effect e : _debuffs)
			{
				if ((e != null) && (e.getSkill().getId() == newEffect.getSkill().getId()) && (e.getEffectType() == newEffect.getEffectType()) && (e.getAbnormalLvl() == newEffect.getAbnormalLvl()) && e.getAbnormalType().equals(newEffect.getAbnormalType()))
				{
					newEffect.stopEffectTask();
					return;
				}
			}
			_debuffs.addLast(newEffect);
		}
		else
		{
			if (_buffs == null)
			{
				_buffs = new FastList<L2Effect>().shared();
			}
			
			for (L2Effect e : _buffs)
			{
				if ((e != null) && (e.getSkill().getId() == newEffect.getSkill().getId()) && (e.getEffectType() == newEffect.getEffectType()) && (e.getAbnormalLvl() == newEffect.getAbnormalLvl()) && e.getAbnormalType().equals(newEffect.getAbnormalType()))
				{
					e.exit();
				}
			}
			
			if (!doesStack(newSkill) && !newSkill.is7Signs())
			{
				int effectsToRemove;
				if (newSkill.isDance())
				{
					effectsToRemove = getDanceCount() - Config.DANCES_MAX_AMOUNT;
					if (effectsToRemove >= 0)
					{
						for (L2Effect e : _buffs)
						{
							if ((e == null) || !e.getSkill().isDance())
							{
								continue;
							}
							
							e.exit();
							effectsToRemove--;
							if (effectsToRemove < 0)
							{
								break;
							}
						}
					}
				}
				else if (newSkill.isTriggeredSkill())
				{
					effectsToRemove = getTriggeredBuffCount() - Config.TRIGGERED_BUFFS_MAX_AMOUNT;
					if (effectsToRemove >= 0)
					{
						for (L2Effect e : _buffs)
						{
							if ((e == null) || !e.getSkill().isTriggeredSkill())
							{
								continue;
							}
							
							e.exit();
							effectsToRemove--;
							if (effectsToRemove < 0)
							{
								break;
							}
						}
					}
				}
				else
				{
					effectsToRemove = getBuffCount() - _owner.getMaxBuffCount();
					if (effectsToRemove >= 0)
					{
						if (newSkill.getSkillType() == L2SkillType.BUFF)
						{
							for (L2Effect e : _buffs)
							{
								if ((e == null) || e.getSkill().isDance() || e.getSkill().isTriggeredSkill())
								{
									continue;
								}
								
								if (e.getSkill().getSkillType() == L2SkillType.BUFF)
								{
									e.exit();
									effectsToRemove--;
								}
								else
								{
									continue;
								}
								
								if (effectsToRemove < 0)
								{
									break;
								}
							}
						}
					}
				}
			}
			
			if (newSkill.isTriggeredSkill())
			{
				_buffs.addLast(newEffect);
			}
			else
			{
				int pos = 0;
				if (newSkill.isToggle())
				{
					for (L2Effect e : _buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().isDance())
						{
							break;
						}
						pos++;
					}
				}
				else if (newSkill.isDance())
				{
					for (L2Effect e : _buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().isTriggeredSkill())
						{
							break;
						}
						pos++;
					}
				}
				else
				{
					for (L2Effect e : _buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().isToggle() || e.getSkill().is7Signs() || e.getSkill().isDance() || e.getSkill().isTriggeredSkill())
						{
							break;
						}
						pos++;
					}
				}
				_buffs.add(pos, newEffect);
			}
		}
		
		if ("none".equals(newEffect.getAbnormalType()))
		{
			if (newEffect.setInUse(true))
			{
				_owner.addStatFuncs(newEffect.getStatFuncs());
			}
			return;
		}
		
		List<L2Effect> stackQueue;
		L2Effect effectToAdd = null;
		L2Effect effectToRemove = null;
		if (_stackedEffects == null)
		{
			_stackedEffects = new FastMap<>();
		}
		
		stackQueue = _stackedEffects.get(newEffect.getAbnormalType());
		
		if (stackQueue != null)
		{
			int pos = 0;
			if (!stackQueue.isEmpty())
			{
				effectToRemove = listsContains(stackQueue.get(0));
				
				Iterator<L2Effect> queueIterator = stackQueue.iterator();
				
				while (queueIterator.hasNext())
				{
					if (newEffect.getAbnormalLvl() < queueIterator.next().getAbnormalLvl())
					{
						pos++;
					}
					else
					{
						break;
					}
				}
				stackQueue.add(pos, newEffect);
				
				if (Config.EFFECT_CANCELING && !newEffect.getSkill().isStatic() && (stackQueue.size() > 1))
				{
					if (newSkill.isDebuff())
					{
						_debuffs.remove(stackQueue.remove(1));
					}
					else
					{
						_buffs.remove(stackQueue.remove(1));
					}
				}
			}
			else
			{
				stackQueue.add(0, newEffect);
			}
		}
		else
		{
			stackQueue = new FastList<>();
			stackQueue.add(0, newEffect);
		}
		_stackedEffects.put(newEffect.getAbnormalType(), stackQueue);
		
		if (!stackQueue.isEmpty())
		{
			effectToAdd = listsContains(stackQueue.get(0));
		}
		
		if (effectToRemove != effectToAdd)
		{
			if (effectToRemove != null)
			{
				_owner.removeStatsOwner(effectToRemove);
				effectToRemove.setInUse(false);
			}
			if (effectToAdd != null)
			{
				if (effectToAdd.setInUse(true))
				{
					_owner.addStatFuncs(effectToAdd.getStatFuncs());
				}
			}
		}
	}
	
	public void removePassiveEffects(int skillId)
	{
		if (_passives == null)
		{
			return;
		}
		
		for (L2Effect eff : _passives)
		{
			if (eff == null)
			{
				continue;
			}
			
			if (eff.getSkill().getId() == skillId)
			{
				eff.exit();
			}
		}
	}
	
	protected void updateEffectIcons()
	{
		if (_owner == null)
		{
			return;
		}
		
		if (!_owner.isPlayable())
		{
			updateEffectFlags();
			return;
		}
		
		AbnormalStatusUpdate mi = null;
		PartySpelled ps = null;
		PartySpelled psSummon = null;
		ExOlympiadSpelledInfo os = null;
		boolean isSummon = false;
		
		if (_owner.isPlayer())
		{
			if (_partyOnly)
			{
				_partyOnly = false;
			}
			else
			{
				mi = new AbnormalStatusUpdate();
			}
			
			if (_owner.isInParty())
			{
				ps = new PartySpelled(_owner);
			}
			
			if (_owner.getActingPlayer().isInOlympiadMode() && _owner.getActingPlayer().isOlympiadStart())
			{
				os = new ExOlympiadSpelledInfo(_owner.getActingPlayer());
			}
		}
		else if (_owner.isSummon())
		{
			isSummon = true;
			ps = new PartySpelled(_owner);
			psSummon = new PartySpelled(_owner);
		}
		
		boolean foundRemovedOnAction = false;
		boolean foundRemovedOnDamage = false;
		
		if ((_buffs != null) && !_buffs.isEmpty())
		{
			for (L2Effect e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getSkill().isRemovedOnAnyActionExceptMove())
				{
					foundRemovedOnAction = true;
				}
				if (e.getSkill().isRemovedOnDamage())
				{
					foundRemovedOnDamage = true;
				}
				
				if (!e.isIconDisplay())
				{
					continue;
				}
				
				if (e.getEffectType() == L2EffectType.SIGNET_GROUND)
				{
					continue;
				}
				
				if (e.isInUse())
				{
					if (mi != null)
					{
						e.addIcon(mi);
					}
					
					if (ps != null)
					{
						if (isSummon || (!e.getSkill().isToggle() && !(e.getSkill().isStatic() && ((e.getEffectType() == L2EffectType.HEAL_OVER_TIME) || (e.getEffectType() == L2EffectType.CPHEAL_OVER_TIME) || (e.getEffectType() == L2EffectType.MANA_HEAL_OVER_TIME)))))
						{
							e.addPartySpelledIcon(ps);
						}
					}
					
					if (psSummon != null)
					{
						if (!e.getSkill().isToggle() && !(e.getSkill().isStatic() && ((e.getEffectType() == L2EffectType.HEAL_OVER_TIME) || (e.getEffectType() == L2EffectType.CPHEAL_OVER_TIME) || (e.getEffectType() == L2EffectType.MANA_HEAL_OVER_TIME))))
						{
							e.addPartySpelledIcon(psSummon);
						}
					}
					
					if (os != null)
					{
						e.addOlympiadSpelledIcon(os);
					}
				}
			}
			
		}
		_hasBuffsRemovedOnAnyAction = foundRemovedOnAction;
		_hasBuffsRemovedOnDamage = foundRemovedOnDamage;
		foundRemovedOnDamage = false;
		
		if ((_debuffs != null) && !_debuffs.isEmpty())
		{
			for (L2Effect e : _debuffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getSkill().isRemovedOnAnyActionExceptMove())
				{
					foundRemovedOnAction = true;
				}
				if (e.getSkill().isRemovedOnDamage())
				{
					foundRemovedOnDamage = true;
				}
				
				if (!e.isIconDisplay())
				{
					continue;
				}
				
				switch (e.getEffectType())
				{
					case SIGNET_GROUND:
						continue;
				}
				
				if (e.isInUse())
				{
					if (mi != null)
					{
						e.addIcon(mi);
					}
					
					if (ps != null)
					{
						e.addPartySpelledIcon(ps);
					}
					
					if (psSummon != null)
					{
						e.addPartySpelledIcon(psSummon);
					}
					
					if (os != null)
					{
						e.addOlympiadSpelledIcon(os);
					}
				}
			}
			
		}
		_hasDebuffsRemovedOnDamage = foundRemovedOnDamage;
		
		if (mi != null)
		{
			_owner.sendPacket(mi);
		}
		
		if (ps != null)
		{
			if (_owner.isSummon())
			{
				L2PcInstance summonOwner = ((L2Summon) _owner).getOwner();
				
				if (summonOwner != null)
				{
					if (summonOwner.isInParty())
					{
						summonOwner.getParty().broadcastToPartyMembers(summonOwner, psSummon);
						summonOwner.sendPacket(ps);
					}
					else
					{
						summonOwner.sendPacket(ps);
					}
				}
			}
			else if (_owner.isPlayer() && _owner.isInParty())
			{
				_owner.getParty().broadcastPacket(ps);
			}
		}
		
		if (os != null)
		{
			final OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(_owner.getActingPlayer().getOlympiadGameId());
			if ((game != null) && game.isBattleStarted())
			{
				game.getZone().broadcastPacketToObservers(os);
			}
		}
	}
	
	protected void updateEffectFlags()
	{
		boolean foundRemovedOnAction = false;
		boolean foundRemovedOnDamage = false;
		
		if ((_buffs != null) && !_buffs.isEmpty())
		{
			for (L2Effect e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getSkill().isRemovedOnAnyActionExceptMove())
				{
					foundRemovedOnAction = true;
				}
				if (e.getSkill().isRemovedOnDamage())
				{
					foundRemovedOnDamage = true;
				}
			}
		}
		_hasBuffsRemovedOnAnyAction = foundRemovedOnAction;
		_hasBuffsRemovedOnDamage = foundRemovedOnDamage;
		foundRemovedOnDamage = false;
		
		if ((_debuffs != null) && !_debuffs.isEmpty())
		{
			for (L2Effect e : _debuffs)
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getSkill().isRemovedOnDamage())
				{
					foundRemovedOnDamage = true;
				}
			}
		}
		_hasDebuffsRemovedOnDamage = foundRemovedOnDamage;
	}
	
	private L2Effect listsContains(L2Effect effect)
	{
		if ((_buffs != null) && !_buffs.isEmpty() && _buffs.contains(effect))
		{
			return effect;
		}
		if ((_debuffs != null) && !_debuffs.isEmpty() && _debuffs.contains(effect))
		{
			return effect;
		}
		return null;
	}
	
	private final void computeEffectFlags()
	{
		int flags = 0;
		
		if (_buffs != null)
		{
			for (L2Effect e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				flags |= e.getEffectFlags();
			}
		}
		
		if (_debuffs != null)
		{
			for (L2Effect e : _debuffs)
			{
				if (e == null)
				{
					continue;
				}
				flags |= e.getEffectFlags();
			}
		}
		
		_effectFlags = flags;
	}
	
	public boolean isAffected(EffectFlag flag)
	{
		return (_effectFlags & flag.getMask()) != 0;
	}
	
	public void clear()
	{
		try
		{
			_addQueue = null;
			_removeQueue = null;
			_buffs = null;
			_debuffs = null;
			_stackedEffects = null;
			_queuesInitialized = false;
			_effectCache = null;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
	}
}