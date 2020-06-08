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
package l2e.gameserver.model.actor;

import l2e.Config;
import l2e.gameserver.ai.CtrlEvent;
import l2e.gameserver.instancemanager.InstanceManager;
import l2e.gameserver.model.actor.events.PlayableEvents;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.knownlist.PlayableKnownList;
import l2e.gameserver.model.actor.stat.PlayableStat;
import l2e.gameserver.model.actor.status.PlayableStatus;
import l2e.gameserver.model.actor.templates.L2CharTemplate;
import l2e.gameserver.model.effects.EffectFlag;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.entity.Instance;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.L2Skill;

public abstract class L2Playable extends L2Character
{
	private L2Character _lockedTarget = null;
	private L2PcInstance transferDmgTo = null;
	private int _hitmanTarget = 0;
	
	public L2Playable(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2Playable);
		setIsInvul(false);
	}
	
	@Override
	public PlayableKnownList getKnownList()
	{
		return (PlayableKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new PlayableKnownList(this));
	}
	
	@Override
	public PlayableStat getStat()
	{
		return (PlayableStat) super.getStat();
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new PlayableStat(this));
	}
	
	@Override
	public PlayableStatus getStatus()
	{
		return (PlayableStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new PlayableStatus(this));
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!getEvents().onDeath(killer))
		{
			return false;
		}
		
		synchronized (this)
		{
			if (isDead())
			{
				return false;
			}
			
			setCurrentHp(0);
			setIsDead(true);
		}
		
		setTarget(null);
		stopMove(null);
		getStatus().stopHpMpRegeneration();
		
		if (isPhoenixBlessed())
		{
			if (isCharmOfLuckAffected())
			{
				stopEffects(L2EffectType.CHARM_OF_LUCK);
			}
			if (isNoblesseBlessed())
			{
				stopEffects(L2EffectType.NOBLESSE_BLESSING);
			}
		}
		else if (!Config.TW_LOSE_BUFFS_ON_DEATH && isInTownWarEvent())
		{
		}
		else if (isNoblesseBlessed())
		{
			stopEffects(L2EffectType.NOBLESSE_BLESSING);
			if (isCharmOfLuckAffected())
			{
				stopEffects(L2EffectType.CHARM_OF_LUCK);
			}
		}
		else
		{
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}
		broadcastStatusUpdate();
		
		if (getWorldRegion() != null)
		{
			getWorldRegion().onDeath(this);
		}
		
		L2PcInstance actingPlayer = getActingPlayer();
		if (!actingPlayer.isNotifyQuestOfDeathEmpty())
		{
			for (QuestState qs : actingPlayer.getNotifyQuestOfDeath())
			{
				qs.getQuest().notifyDeath((killer == null ? this : killer), this, qs);
			}
		}
		
		if (getInstanceId() > 0)
		{
			final Instance instance = InstanceManager.getInstance().getInstance(getInstanceId());
			if (instance != null)
			{
				instance.notifyDeath(killer, this);
			}
		}
		
		if (killer != null)
		{
			L2PcInstance player = killer.getActingPlayer();
			
			if (player != null)
			{
				player.onKillUpdatePvPKarma(this);
			}
		}
		getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		
		return true;
	}
	
	public boolean checkIfPvP(L2Character target)
	{
		if (target == null)
		{
			return false;
		}
		if (target == this)
		{
			return false;
		}
		if (!target.isPlayable())
		{
			return false;
		}
		
		final L2PcInstance player = getActingPlayer();
		if (player == null)
		{
			return false;
		}
		if (player.getKarma() != 0)
		{
			return false;
		}
		
		final L2PcInstance targetPlayer = target.getActingPlayer();
		if (targetPlayer == null)
		{
			return false;
		}
		if (targetPlayer == this)
		{
			return false;
		}
		if (targetPlayer.getKarma() != 0)
		{
			return false;
		}
		if (targetPlayer.getPvpFlag() == 0)
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	public final boolean isNoblesseBlessed()
	{
		return isAffected(EffectFlag.NOBLESS_BLESSING);
	}
	
	public final boolean isPhoenixBlessed()
	{
		return isAffected(EffectFlag.PHOENIX_BLESSING);
	}
	
	public boolean isSilentMoving()
	{
		return isAffected(EffectFlag.SILENT_MOVE);
	}
	
	public final boolean isProtectionBlessingAffected()
	{
		return isAffected(EffectFlag.PROTECTION_BLESSING);
	}
	
	public final boolean isCharmOfLuckAffected()
	{
		return isAffected(EffectFlag.CHARM_OF_LUCK);
	}
	
	@Override
	public void updateEffectIcons(boolean partyOnly)
	{
		_effects.updateEffectIcons(partyOnly);
	}
	
	public boolean isLockedTarget()
	{
		return _lockedTarget != null;
	}
	
	public L2Character getLockedTarget()
	{
		return _lockedTarget;
	}
	
	public void setLockedTarget(L2Character cha)
	{
		_lockedTarget = cha;
	}
	
	public void setTransferDamageTo(L2PcInstance val)
	{
		transferDmgTo = val;
	}
	
	public L2PcInstance getTransferingDamageTo()
	{
		return transferDmgTo;
	}
	
	@Override
	public void initCharEvents()
	{
		setCharEvents(new PlayableEvents(this));
	}
	
	@Override
	public PlayableEvents getEvents()
	{
		return (PlayableEvents) super.getEvents();
	}
	
	public abstract int getKarma();
	
	public abstract byte getPvpFlag();
	
	public abstract boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove);
	
	public abstract void store();
	
	public abstract void storeEffect(boolean storeEffects);
	
	public abstract void restoreEffects();
	
	@Override
	public boolean isPlayable()
	{
		return true;
	}
	
	public void setHitmanTarget(int hitmanTarget)
	{
		_hitmanTarget = hitmanTarget;
	}
	
	public int getHitmanTarget()
	{
		return _hitmanTarget;
	}
}