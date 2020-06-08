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
package l2e.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.PetsParser;
import l2e.gameserver.model.L2PetData.L2PetSkillLearn;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.effects.L2Effect;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.util.Rnd;

public final class L2BabyPetInstance extends L2PetInstance
{
	private static final int BUFF_CONTROL = 5771;
	private static final int AWAKENING = 5753;
	
	protected List<SkillsHolder> _buffs = null;
	protected SkillsHolder _majorHeal = null;
	protected SkillsHolder _minorHeal = null;
	protected SkillsHolder _recharge = null;
	
	private Future<?> _castTask;
	
	protected boolean _bufferMode = true;
	
	public L2BabyPetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
	{
		super(objectId, template, owner, control);
		setInstanceType(InstanceType.L2BabyPetInstance);
	}
	
	public L2BabyPetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control, byte level)
	{
		super(objectId, template, owner, control, level);
		setInstanceType(InstanceType.L2BabyPetInstance);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		L2Skill skill;
		double healPower = 0;
		for (L2PetSkillLearn psl : PetsParser.getInstance().getPetData(getId()).getAvailableSkills())
		{
			int id = psl.getSkillId();
			int lvl = PetsParser.getInstance().getPetData(getId()).getAvailableLevel(id, getLevel());
			if (lvl == 0)
			{
				continue;
			}
			skill = SkillHolder.getInstance().getInfo(id, lvl);
			if (skill != null)
			{
				if ((skill.getId() == BUFF_CONTROL) || (skill.getId() == AWAKENING))
				{
					continue;
				}
				
				switch (skill.getSkillType())
				{
					case BUFF:
						if (_buffs == null)
						{
							_buffs = new ArrayList<>();
						}
						_buffs.add(new SkillsHolder(skill));
						break;
					case DUMMY:
						if (skill.hasEffectType(L2EffectType.MANAHEAL_BY_LEVEL))
						{
							_recharge = new SkillsHolder(skill);
						}
						else if (skill.hasEffectType(L2EffectType.HEAL))
						{
							if (healPower == 0)
							{
								_majorHeal = new SkillsHolder(skill);
								_minorHeal = _majorHeal;
								healPower = skill.getPower();
							}
							else
							{
								if (skill.getPower() > healPower)
								{
									_majorHeal = new SkillsHolder(skill);
								}
								else
								{
									_minorHeal = new SkillsHolder(skill);
								}
							}
						}
						break;
				}
			}
		}
		startCastTask();
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		stopCastTask();
		abortCast();
		return true;
	}
	
	@Override
	public synchronized void unSummon(L2PcInstance owner)
	{
		stopCastTask();
		abortCast();
		super.unSummon(owner);
	}
	
	@Override
	public void doRevive()
	{
		super.doRevive();
		startCastTask();
	}
	
	@Override
	public void onDecay()
	{
		super.onDecay();
		
		if (_buffs != null)
		{
			_buffs.clear();
		}
	}
	
	private final void startCastTask()
	{
		if ((_majorHeal != null) || (_buffs != null) || ((_recharge != null) && (_castTask == null) && !isDead()))
		{
			_castTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new CastTask(this), 3000, 2000);
		}
	}
	
	@Override
	public void switchMode()
	{
		_bufferMode = !_bufferMode;
	}
	
	public boolean isInSupportMode()
	{
		return _bufferMode;
	}
	
	private final void stopCastTask()
	{
		if (_castTask != null)
		{
			_castTask.cancel(false);
			_castTask = null;
		}
	}
	
	protected void castSkill(L2Skill skill)
	{
		final boolean previousFollowStatus = getFollowStatus();
		
		if (!previousFollowStatus && !isInsideRadius(getOwner(), skill.getCastRange(), true, true))
		{
			return;
		}
		
		setTarget(getOwner());
		useMagic(skill, false, false);
		
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1);
		msg.addSkillName(skill);
		sendPacket(msg);
		
		if (previousFollowStatus != getFollowStatus())
		{
			setFollowStatus(previousFollowStatus);
		}
	}
	
	private class CastTask implements Runnable
	{
		private final L2BabyPetInstance _baby;
		private final List<L2Skill> _currentBuffs = new ArrayList<>();
		
		public CastTask(L2BabyPetInstance baby)
		{
			_baby = baby;
		}
		
		@Override
		public void run()
		{
			L2PcInstance owner = _baby.getOwner();
			
			if ((owner != null) && !owner.isDead() && !owner.isInvul() && !_baby.isCastingNow() && !_baby.isBetrayed() && !_baby.isMuted() && !_baby.isOutOfControl() && _bufferMode && (_baby.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST))
			{
				L2Skill skill = null;
				
				if (_majorHeal != null)
				{
					final double hpPercent = owner.getCurrentHp() / owner.getMaxHp();
					final boolean isImprovedBaby = PetsParser.isUpgradeBabyPetGroup(getId());
					if ((isImprovedBaby && (hpPercent < 0.3)) || (!isImprovedBaby && (hpPercent < 0.15)))
					{
						skill = _majorHeal.getSkill();
						if (!_baby.isSkillDisabled(skill) && (Rnd.get(100) <= 75))
						{
							if (_baby.getCurrentMp() >= skill.getMpConsume())
							{
								castSkill(skill);
								return;
							}
						}
					}
					else if ((_majorHeal.getSkill() != _minorHeal.getSkill()) && ((isImprovedBaby && (hpPercent < 0.7)) || (!isImprovedBaby && (hpPercent < 0.8))))
					{
						skill = _minorHeal.getSkill();
						if (!_baby.isSkillDisabled(skill) && (Rnd.get(100) <= 25))
						{
							if (_baby.getCurrentMp() >= skill.getMpConsume())
							{
								castSkill(skill);
								return;
							}
						}
					}
				}
				
				if (_baby.getFirstEffect(BUFF_CONTROL) == null)
				{
					if ((_buffs != null) && !_buffs.isEmpty())
					{
						for (SkillsHolder i : _buffs)
						{
							skill = i.getSkill();
							if (_baby.isSkillDisabled(skill))
							{
								continue;
							}
							if (_baby.getCurrentMp() >= skill.getMpConsume())
							{
								_currentBuffs.add(skill);
							}
						}
					}
					
					if (!_currentBuffs.isEmpty())
					{
						L2Effect[] effects = owner.getAllEffects();
						Iterator<L2Skill> iter;
						L2Skill currentSkill;
						for (L2Effect e : effects)
						{
							if (e == null)
							{
								continue;
							}
							
							currentSkill = e.getSkill();
							
							if (currentSkill.isDebuff() || currentSkill.isPassive() || currentSkill.isToggle())
							{
								continue;
							}
							
							iter = _currentBuffs.iterator();
							while (iter.hasNext())
							{
								skill = iter.next();
								if ((currentSkill.getId() == skill.getId()) && (currentSkill.getLevel() >= skill.getLevel()))
								{
									iter.remove();
								}
								else
								{
									if (skill.hasEffects() && !"none".equals(skill.getEffectTemplates()[0].abnormalType) && e.getAbnormalType().equals(skill.getEffectTemplates()[0].abnormalType) && (e.getAbnormalLvl() >= skill.getEffectTemplates()[0].abnormalLvl))
									{
										iter.remove();
									}
								}
							}
							if (_currentBuffs.isEmpty())
							{
								break;
							}
						}
						if (!_currentBuffs.isEmpty())
						{
							castSkill(_currentBuffs.get(Rnd.get(_currentBuffs.size())));
							_currentBuffs.clear();
							return;
						}
					}
				}
				
				if ((_recharge != null) && owner.isInCombat() && ((owner.getCurrentMp() / owner.getMaxMp()) < 0.6) && (Rnd.get(100) <= 60))
				{
					skill = _recharge.getSkill();
					if (!_baby.isSkillDisabled(skill) && (_baby.getCurrentMp() >= skill.getMpConsume()))
					{
						castSkill(skill);
						return;
					}
				}
			}
		}
	}
}