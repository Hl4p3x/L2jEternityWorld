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
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.ai.L2CharacterAI;
import l2e.gameserver.ai.L2SummonAI;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.xml.CategoryParser;
import l2e.gameserver.data.xml.ExperienceParser;
import l2e.gameserver.handler.IItemHandler;
import l2e.gameserver.handler.ItemHandler;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.CategoryType;
import l2e.gameserver.model.L2Object;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.L2WorldRegion;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.L2Attackable.AggroInfo;
import l2e.gameserver.model.actor.events.SummonEvents;
import l2e.gameserver.model.actor.instance.L2MerchantSummonInstance;
import l2e.gameserver.model.actor.instance.L2NpcInstance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2e.gameserver.model.actor.knownlist.SummonKnownList;
import l2e.gameserver.model.actor.stat.SummonStat;
import l2e.gameserver.model.actor.status.SummonStatus;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.effects.L2EffectType;
import l2e.gameserver.model.itemcontainer.PetInventory;
import l2e.gameserver.model.items.L2EtcItem;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.items.type.L2ActionType;
import l2e.gameserver.model.olympiad.OlympiadGameManager;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.targets.L2TargetType;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AbstractNpcInfo.SummonInfo;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.ExPartyPetWindowAdd;
import l2e.gameserver.network.serverpackets.ExPartyPetWindowDelete;
import l2e.gameserver.network.serverpackets.ExPartyPetWindowUpdate;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.PetDelete;
import l2e.gameserver.network.serverpackets.PetInfo;
import l2e.gameserver.network.serverpackets.PetItemList;
import l2e.gameserver.network.serverpackets.PetStatusUpdate;
import l2e.gameserver.network.serverpackets.RelationChanged;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.TeleportToLocation;
import l2e.gameserver.taskmanager.DecayTaskManager;
import l2e.gameserver.util.Util;

public abstract class L2Summon extends L2Playable
{
	private L2PcInstance _owner;
	private int _attackRange = 36;
	private boolean _follow = true;
	private boolean _previousFollowStatus = true;
	
	protected boolean _restoreSummon = true;
	
	private int _shotsMask = 0;
	
	private static final int[] PASSIVE_SUMMONS =
	{
		12564,
		12621,
		14702,
		14703,
		14704,
		14705,
		14706,
		14707,
		14708,
		14709,
		14710,
		14711,
		14712,
		14713,
		14714,
		14715,
		14716,
		14717,
		14718,
		14719,
		14720,
		14721,
		14722,
		14723,
		14724,
		14725,
		14726,
		14727,
		14728,
		14729,
		14730,
		14731,
		14732,
		14733,
		14734,
		14735,
		14736
	};
	
	public class AIAccessor extends L2Character.AIAccessor
	{
		public L2Summon getSummon()
		{
			return L2Summon.this;
		}
		
		public boolean isAutoFollow()
		{
			return getFollowStatus();
		}
		
		public void doPickupItem(L2Object object)
		{
			L2Summon.this.doPickupItem(object);
		}
	}
	
	public L2Summon(int objectId, L2NpcTemplate template, L2PcInstance owner)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2Summon);
		
		setInstanceId(owner.getInstanceId());
		
		_showSummonAnimation = true;
		_owner = owner;
		_ai = new L2SummonAI(new AIAccessor());
		
		setXYZInvisible(owner.getX() + 20, owner.getY() + 20, owner.getZ() + 100);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		if (!(this instanceof L2MerchantSummonInstance))
		{
			if (Config.SUMMON_STORE_SKILL_COOLTIME && !isTeleporting())
			{
				restoreEffects();
			}
			
			this.setFollowStatus(true);
			updateAndBroadcastStatus(0);
			sendPacket(new RelationChanged(this, getOwner().getRelation(getOwner()), false));
			for (L2PcInstance player : getOwner().getKnownList().getKnownPlayersInRadius(800))
			{
				player.sendPacket(new RelationChanged(this, getOwner().getRelation(player), isAutoAttackable(player)));
			}
			L2Party party = this.getOwner().getParty();
			if (party != null)
			{
				party.broadcastToPartyMembers(getOwner(), new ExPartyPetWindowAdd(this));
			}
		}
		setShowSummonAnimation(false);
		_restoreSummon = false;
	}
	
	@Override
	public final SummonKnownList getKnownList()
	{
		return (SummonKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new SummonKnownList(this));
	}
	
	@Override
	public SummonStat getStat()
	{
		return (SummonStat) super.getStat();
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new SummonStat(this));
	}
	
	@Override
	public SummonStatus getStatus()
	{
		return (SummonStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new SummonStatus(this));
	}
	
	@Override
	public void initCharEvents()
	{
		setCharEvents(new SummonEvents(this));
	}
	
	@Override
	public SummonEvents getEvents()
	{
		return (SummonEvents) super.getEvents();
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2SummonAI(new L2Summon.AIAccessor());
				}
				return _ai;
			}
		}
		return _ai;
	}
	
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}
	
	public abstract int getSummonType();
	
	@Override
	public final void stopAllEffects()
	{
		super.stopAllEffects();
		updateAndBroadcastStatus(1);
	}
	
	@Override
	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		super.stopAllEffectsExceptThoseThatLastThroughDeath();
		updateAndBroadcastStatus(1);
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		for (L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			player.sendPacket(new SummonInfo(this, player, 1));
		}
	}
	
	public boolean isMountable()
	{
		return false;
	}
	
	public long getExpForThisLevel()
	{
		if (getLevel() >= ExperienceParser.getInstance().getMaxPetLevel())
		{
			return 0;
		}
		return ExperienceParser.getInstance().getExpForLevel(getLevel());
	}
	
	public long getExpForNextLevel()
	{
		if (getLevel() >= (ExperienceParser.getInstance().getMaxPetLevel() - 1))
		{
			return 0;
		}
		return ExperienceParser.getInstance().getExpForLevel(getLevel() + 1);
	}
	
	@Override
	public final int getKarma()
	{
		return getOwner() != null ? getOwner().getKarma() : 0;
	}
	
	@Override
	public final byte getPvpFlag()
	{
		return getOwner() != null ? getOwner().getPvpFlag() : 0;
	}
	
	@Override
	public final int getTeam()
	{
		return getOwner() != null ? getOwner().getTeam() : 0;
	}
	
	public final L2PcInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	public final int getId()
	{
		return getTemplate().getId();
	}
	
	public short getSoulShotsPerHit()
	{
		if (getTemplate().getAIDataStatic().getSoulShot() > 0)
		{
			return (short) getTemplate().getAIDataStatic().getSoulShot();
		}
		
		return 1;
	}
	
	public short getSpiritShotsPerHit()
	{
		if (getTemplate().getAIDataStatic().getSpiritShot() > 0)
		{
			return (short) getTemplate().getAIDataStatic().getSpiritShot();
		}
		
		return 1;
	}
	
	public void followOwner()
	{
		setFollowStatus(true);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (isNoblesseBlessed())
		{
			stopEffects(L2EffectType.NOBLESSE_BLESSING);
			storeEffect(true);
		}
		else
		{
			storeEffect(false);
		}
		
		if (!super.doDie(killer))
		{
			return false;
		}
		if (this instanceof L2MerchantSummonInstance)
		{
			return true;
		}
		L2PcInstance owner = getOwner();
		
		if (owner != null)
		{
			for (L2Character TgMob : getKnownList().getKnownCharacters())
			{
				if (TgMob instanceof L2Attackable)
				{
					if (((L2Attackable) TgMob).isDead())
					{
						continue;
					}
					
					AggroInfo info = ((L2Attackable) TgMob).getAggroList().get(this);
					if (info != null)
					{
						((L2Attackable) TgMob).addDamageHate(owner, info.getDamage(), info.getHate());
					}
				}
			}
		}
		
		if (isPhoenixBlessed() && (getOwner() != null))
		{
			getOwner().reviveRequest(getOwner(), null, true);
		}
		
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}
	
	public boolean doDie(L2Character killer, boolean decayed)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if (!decayed)
		{
			DecayTaskManager.getInstance().addDecayTask(this);
		}
		return true;
	}
	
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}
	
	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		super.broadcastStatusUpdate();
		updateAndBroadcastStatus(1);
	}
	
	public void deleteMe(L2PcInstance owner)
	{
		owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));
		
		if (getInventory() != null)
		{
			getInventory().destroyAllItems("pet deleted", getOwner(), this);
		}
		decayMe();
		getKnownList().removeAllKnownObjects();
		owner.setPet(null);
		super.deleteMe();
	}
	
	public void unSummon(L2PcInstance owner)
	{
		if (isVisible() && !isDead())
		{
			getAI().stopFollow();
			owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));
			L2Party party;
			if ((party = owner.getParty()) != null)
			{
				party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));
			}
			
			if ((getInventory() != null) && (getInventory().getSize() > 0))
			{
				getOwner().setPetInvItems(true);
				sendPacket(SystemMessageId.ITEMS_IN_PET_INVENTORY);
			}
			else
			{
				getOwner().setPetInvItems(false);
			}
			
			store();
			storeEffect(true);
			owner.setPet(null);
			
			if (hasAI())
			{
				getAI().stopAITask();
			}
			
			stopAllEffects();
			L2WorldRegion oldRegion = getWorldRegion();
			decayMe();
			if (oldRegion != null)
			{
				oldRegion.removeFromZones(this);
			}
			getKnownList().removeAllKnownObjects();
			setTarget(null);
			for (int itemId : owner.getAutoSoulShot())
			{
				String handler = ((L2EtcItem) ItemHolder.getInstance().getTemplate(itemId)).getHandlerName();
				if ((handler != null) && handler.contains("Beast"))
				{
					owner.disableAutoShot(itemId);
				}
			}
		}
	}
	
	public int getAttackRange()
	{
		return _attackRange;
	}
	
	public void setAttackRange(int range)
	{
		_attackRange = (range < 36) ? 36 : range;
	}
	
	public void setFollowStatus(boolean state)
	{
		_follow = state;
		if (_follow)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, getOwner());
		}
		else
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
		}
	}
	
	public boolean getFollowStatus()
	{
		return _follow;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _owner.isAutoAttackable(attacker);
	}
	
	public int getControlObjectId()
	{
		return 0;
	}
	
	public L2Weapon getActiveWeapon()
	{
		return null;
	}
	
	@Override
	public PetInventory getInventory()
	{
		return null;
	}
	
	protected void doPickupItem(L2Object object)
	{
	}
	
	public void setRestoreSummon(boolean val)
	{
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public boolean isInvul()
	{
		return super.isInvul() || getOwner().isSpawnProtected();
	}
	
	@Override
	public L2Party getParty()
	{
		if (_owner == null)
		{
			return null;
		}
		
		return _owner.getParty();
	}
	
	@Override
	public boolean isInParty()
	{
		return (_owner != null) && _owner.isInParty();
	}
	
	@Override
	public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if ((skill == null) || isDead() || (getOwner() == null))
		{
			return false;
		}
		
		if (skill.isPassive())
		{
			return false;
		}
		
		if (isCastingNow())
		{
			return false;
		}
		getOwner().setCurrentPetSkill(skill, forceUse, dontMove);
		
		L2Object target = null;
		
		switch (skill.getTargetType())
		{
			case OWNER_PET:
				target = getOwner();
				break;
			case PARTY:
			case AURA:
			case FRONT_AURA:
			case BEHIND_AURA:
			case SELF:
			case AURA_CORPSE_MOB:
				target = this;
				break;
			default:
				target = skill.getFirstOfTargetList(this);
				break;
		}
		
		if (target == null)
		{
			sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			return false;
		}
		
		if (isSkillDisabled(skill))
		{
			sendPacket(SystemMessageId.PET_SKILL_CANNOT_BE_USED_RECHARCHING);
			return false;
		}
		
		if (getCurrentMp() < (getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill)))
		{
			sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			return false;
		}
		
		if (getCurrentHp() <= skill.getHpConsume())
		{
			sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			return false;
		}
		
		if (skill.isOffensive())
		{
			if (getOwner() == target)
			{
				return false;
			}
			
			if (isInsidePeaceZone(this, target) && !getOwner().getAccessLevel().allowPeaceAttack())
			{
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				return false;
			}
			
			if (getOwner().isInOlympiadMode() && !getOwner().isOlympiadStart())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if ((target.getActingPlayer() != null) && (getOwner().getSiegeState() > 0) && getOwner().isInsideZone(ZoneId.SIEGE) && (target.getActingPlayer().getSiegeState() == getOwner().getSiegeState()) && (target.getActingPlayer() != getOwner()) && (target.getActingPlayer().getSiegeSide() == getOwner().getSiegeSide()))
			{
				if (TerritoryWarManager.getInstance().isTWInProgress())
				{
					sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
				}
				else
				{
					sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
				}
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if (target.isDoor())
			{
				if (!target.isAutoAttackable(getOwner()))
				{
					return false;
				}
			}
			else
			{
				if (!target.isAttackable() && !getOwner().getAccessLevel().allowPeaceAttack())
				{
					return false;
				}
				
				if (!target.isAutoAttackable(this) && !forceUse && !target.isNpc() && (skill.getTargetType() != L2TargetType.AURA) && (skill.getTargetType() != L2TargetType.FRONT_AURA) && (skill.getTargetType() != L2TargetType.BEHIND_AURA) && (skill.getTargetType() != L2TargetType.CLAN) && (skill.getTargetType() != L2TargetType.PARTY) && (skill.getTargetType() != L2TargetType.SELF))
				{
					return false;
				}
			}
		}
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		return true;
	}
	
	@Override
	public void setIsImmobilized(boolean value)
	{
		super.setIsImmobilized(value);
		
		if (value)
		{
			_previousFollowStatus = getFollowStatus();
			if (_previousFollowStatus)
			{
				setFollowStatus(false);
			}
		}
		else
		{
			setFollowStatus(_previousFollowStatus);
		}
	}
	
	public void setOwner(L2PcInstance newOwner)
	{
		_owner = newOwner;
	}
	
	@Override
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss || (getOwner() == null))
		{
			return;
		}
		
		if (target.getObjectId() != getOwner().getObjectId())
		{
			if (pcrit || mcrit)
			{
				if (isServitor())
				{
					sendPacket(SystemMessageId.CRITICAL_HIT_BY_SUMMONED_MOB);
				}
				else
				{
					sendPacket(SystemMessageId.CRITICAL_HIT_BY_PET);
				}
			}
			if (getOwner().isInOlympiadMode() && (target instanceof L2PcInstance) && ((L2PcInstance) target).isInOlympiadMode() && (((L2PcInstance) target).getOlympiadGameId() == getOwner().getOlympiadGameId()))
			{
				OlympiadGameManager.getInstance().notifyCompetitorDamage(getOwner(), damage);
			}
			
			final SystemMessage sm;
			
			if (target.isInvul() && !(target instanceof L2NpcInstance))
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.ATTACK_WAS_BLOCKED);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DONE_S3_DAMAGE_TO_C2);
				sm.addNpcName(this);
				sm.addCharName(target);
				sm.addNumber(damage);
			}
			sendPacket(sm);
		}
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, skill);
		if ((getOwner() != null) && (attacker != null))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RECEIVED_DAMAGE_OF_S3_FROM_C2);
			sm.addNpcName(this);
			sm.addCharName(attacker);
			sm.addNumber((int) damage);
			sendPacket(sm);
		}
	}
	
	@Override
	public void doCast(L2Skill skill)
	{
		final L2PcInstance actingPlayer = getActingPlayer();
		
		if (!actingPlayer.checkPvpSkill(getTarget(), skill, true) && !actingPlayer.getAccessLevel().allowPeaceAttack())
		{
			actingPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			actingPlayer.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		super.doCast(skill);
	}
	
	@Override
	public boolean isInCombat()
	{
		return (getOwner() != null) && getOwner().isInCombat();
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return getOwner();
	}
	
	@Override
	public final void broadcastPacket(L2GameServerPacket mov)
	{
		if (getOwner() != null)
		{
			mov.setInvisible(getOwner().isInvisible());
		}
		
		super.broadcastPacket(mov);
	}
	
	@Override
	public final void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		if (getOwner() != null)
		{
			mov.setInvisible(getOwner().isInvisible());
		}
		
		super.broadcastPacket(mov, radiusInKnownlist);
	}
	
	public void updateAndBroadcastStatus(int val)
	{
		if (getOwner() == null)
		{
			return;
		}
		
		sendPacket(new PetInfo(this, val));
		sendPacket(new PetStatusUpdate(this));
		if (isVisible())
		{
			broadcastNpcInfo(val);
		}
		L2Party party = getOwner().getParty();
		if (party != null)
		{
			party.broadcastToPartyMembers(getOwner(), new ExPartyPetWindowUpdate(this));
		}
		updateEffectIcons(true);
	}
	
	public void broadcastNpcInfo(int val)
	{
		for (L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if ((player == null) || ((player == getOwner()) && !(this instanceof L2MerchantSummonInstance)))
			{
				continue;
			}
			player.sendPacket(new SummonInfo(this, player, val));
		}
	}
	
	public boolean isHungry()
	{
		return false;
	}
	
	@Override
	public final boolean isAttackingNow()
	{
		return isInCombat();
	}
	
	public int getWeapon()
	{
		return 0;
	}
	
	public int getArmor()
	{
		return 0;
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (activeChar.equals(getOwner()) && !(this instanceof L2MerchantSummonInstance))
		{
			activeChar.sendPacket(new PetInfo(this, 0));
			updateEffectIcons(true);
			if (isPet())
			{
				activeChar.sendPacket(new PetItemList(getInventory().getItems()));
			}
		}
		else
		{
			activeChar.sendPacket(new SummonInfo(this, activeChar, 0));
		}
	}
	
	@Override
	public void onTeleported()
	{
		super.onTeleported();
		sendPacket(new TeleportToLocation(this, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading()));
	}
	
	@Override
	public String toString()
	{
		return super.toString() + "(" + getId() + ") Owner: " + getOwner();
	}
	
	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}
	
	public void switchMode()
	{
	}
	
	public void cancelAction()
	{
		if (!isMovementDisabled())
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
		}
	}
	
	public void doAttack()
	{
		if (getOwner() != null)
		{
			final L2Object target = getOwner().getTarget();
			if (target != null)
			{
				setTarget(target);
				getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
	}
	
	public final boolean canAttack(boolean ctrlPressed)
	{
		if (getOwner() == null)
		{
			return false;
		}
		
		final L2Object target = getOwner().getTarget();
		if ((target == null) || (this == target) || (getOwner() == target))
		{
			return false;
		}
		
		final int npcId = getId();
		if (Util.contains(PASSIVE_SUMMONS, npcId))
		{
			getOwner().sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (isBetrayed())
		{
			sendPacket(SystemMessageId.PET_REFUSING_ORDER);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (isAttackingDisabled())
		{
			if (getAttackEndTime() <= GameTimeController.getInstance().getGameTicks())
			{
				return false;
			}
			getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		
		if (isPet() && ((getLevel() - getOwner().getLevel()) > 20))
		{
			sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (getOwner().isInOlympiadMode() && !getOwner().isOlympiadStart())
		{
			getOwner().sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if ((target.getActingPlayer() != null) && (getOwner().getSiegeState() > 0) && getOwner().isInsideZone(ZoneId.SIEGE) && (target.getActingPlayer().getSiegeSide() == getOwner().getSiegeSide()))
		{
			if (TerritoryWarManager.getInstance().isTWInProgress())
			{
				sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
			}
			else
			{
				sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
			}
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (!getOwner().getAccessLevel().allowPeaceAttack() && getOwner().isInsidePeaceZone(this, target))
		{
			sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
			return false;
		}
		
		if (isLockedTarget())
		{
			sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
			return false;
		}
		
		if (!target.isAutoAttackable(getOwner()) && !ctrlPressed && !target.isNpc())
		{
			setFollowStatus(false);
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
			sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}
		
		if (!target.isDoor() && ((npcId == L2SiegeSummonInstance.SWOOP_CANNON_ID) || (npcId == L2SiegeSummonInstance.SIEGE_GOLEM_ID)))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public void sendPacket(L2GameServerPacket mov)
	{
		if (getOwner() != null)
		{
			getOwner().sendPacket(mov);
		}
	}
	
	@Override
	public void sendPacket(SystemMessageId id)
	{
		if (getOwner() != null)
		{
			getOwner().sendPacket(id);
		}
	}
	
	@Override
	public boolean isSummon()
	{
		return true;
	}
	
	@Override
	public L2Summon getSummon()
	{
		return this;
	}
	
	@Override
	public boolean isChargedShot(ShotType type)
	{
		return (_shotsMask & type.getMask()) == type.getMask();
	}
	
	@Override
	public void setChargedShot(ShotType type, boolean charged)
	{
		if (charged)
		{
			_shotsMask |= type.getMask();
		}
		else
		{
			_shotsMask &= ~type.getMask();
		}
	}
	
	@Override
	public void rechargeShots(boolean physical, boolean magic)
	{
		L2ItemInstance item;
		IItemHandler handler;
		
		if ((getOwner().getAutoSoulShot() == null) || getOwner().getAutoSoulShot().isEmpty())
		{
			return;
		}
		
		for (int itemId : getOwner().getAutoSoulShot())
		{
			item = getOwner().getInventory().getItemByItemId(itemId);
			
			if (item != null)
			{
				if (magic)
				{
					if (item.getItem().getDefaultAction() == L2ActionType.summon_spiritshot)
					{
						handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
						if (handler != null)
						{
							handler.useItem(getOwner(), item, false);
						}
					}
				}
				
				if (physical)
				{
					if (item.getItem().getDefaultAction() == L2ActionType.summon_soulshot)
					{
						handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
						if (handler != null)
						{
							handler.useItem(getOwner(), item, false);
						}
					}
				}
			}
			else
			{
				getOwner().removeAutoSoulShot(itemId);
			}
		}
	}
	
	@Override
	public int getClanId()
	{
		return (getOwner() != null) ? getOwner().getClanId() : 0;
	}
	
	@Override
	public int getAllyId()
	{
		return (getOwner() != null) ? getOwner().getAllyId() : 0;
	}
	
	@Override
	public boolean isInCategory(CategoryType type)
	{
		return CategoryParser.getInstance().isInCategory(type, getId());
	}
}