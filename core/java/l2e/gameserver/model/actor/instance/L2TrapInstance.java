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
import java.util.List;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.knownlist.TrapKnownList;
import l2e.gameserver.model.actor.tasks.npc.trap.TrapTask;
import l2e.gameserver.model.actor.tasks.npc.trap.TrapTriggerTask;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.items.L2Weapon;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.olympiad.OlympiadGameManager;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.Quest.TrapAction;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.AbstractNpcInfo.TrapInfo;
import l2e.gameserver.network.serverpackets.L2GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.taskmanager.DecayTaskManager;

public final class L2TrapInstance extends L2Npc
{
	private static final int TICK = 1000;
	private boolean _hasLifeTime;
	private boolean _isInArena = false;
	private boolean _isTriggered;
	private final int _lifeTime;
	private L2PcInstance _owner;
	private final List<Integer> _playersWhoDetectedMe = new ArrayList<>();
	private L2Skill _skill;
	private int _remainingTime;
	
	public L2TrapInstance(int objectId, L2NpcTemplate template, int instanceId, int lifeTime)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2TrapInstance);
		setInstanceId(instanceId);
		setName(template.getName());
		setIsInvul(false);
		
		_owner = null;
		_isTriggered = false;
		for (L2Skill skill : template.getSkills().values())
		{
			_skill = skill;
		}
		_hasLifeTime = lifeTime >= 0;
		_lifeTime = lifeTime != 0 ? lifeTime : 30000;
		_remainingTime = _lifeTime;
		if (_skill != null)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new TrapTask(this), TICK);
		}
	}
	
	public L2TrapInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, int lifeTime)
	{
		this(objectId, template, owner.getInstanceId(), lifeTime);
		_owner = owner;
	}
	
	@Override
	public void broadcastPacket(L2GameServerPacket mov)
	{
		for (L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if ((player != null) && (_isTriggered || canBeSeen(player)))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	@Override
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		for (L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if ((player != null) && isInsideRadius(player, radiusInKnownlist, false, false) && (_isTriggered || canBeSeen(player)))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	public boolean canBeSeen(L2Character cha)
	{
		if ((cha != null) && _playersWhoDetectedMe.contains(cha.getObjectId()))
		{
			return true;
		}
		
		if ((_owner == null) || (cha == null))
		{
			return false;
		}
		if (cha == _owner)
		{
			return true;
		}
		
		if (cha.isPlayer())
		{
			if (((L2PcInstance) cha).inObserverMode())
			{
				return false;
			}
			
			if (_owner.isInOlympiadMode() && ((L2PcInstance) cha).isInOlympiadMode() && (((L2PcInstance) cha).getOlympiadSide() != _owner.getOlympiadSide()))
			{
				return false;
			}
		}
		
		if (_isInArena)
		{
			return true;
		}
		
		if (_owner.isInParty() && cha.isInParty() && (_owner.getParty().getLeaderObjectId() == cha.getParty().getLeaderObjectId()))
		{
			return true;
		}
		return false;
	}
	
	public boolean checkTarget(L2Character target)
	{
		if (!L2Skill.checkForAreaOffensiveSkills(this, target, _skill, _isInArena))
		{
			return false;
		}
		
		if ((target.isPlayer()) && ((L2PcInstance) target).inObserverMode())
		{
			return false;
		}
		
		if ((_owner != null) && _owner.isInOlympiadMode())
		{
			final L2PcInstance player = target.getActingPlayer();
			if ((player != null) && player.isInOlympiadMode() && (player.getOlympiadSide() == _owner.getOlympiadSide()))
			{
				return false;
			}
		}
		
		if (_isInArena)
		{
			return true;
		}
		
		if (_owner != null)
		{
			if (target instanceof L2Attackable)
			{
				return true;
			}
			
			final L2PcInstance player = target.getActingPlayer();
			if ((player == null) || ((player.getPvpFlag() == 0) && (player.getKarma() == 0)))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void deleteMe()
	{
		if (_owner != null)
		{
			_owner.setTrap(null);
			_owner = null;
		}
		super.deleteMe();
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return _owner;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	public int getKarma()
	{
		return _owner != null ? _owner.getKarma() : 0;
	}
	
	@Override
	public TrapKnownList getKnownList()
	{
		return (TrapKnownList) super.getKnownList();
	}
	
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	public byte getPvpFlag()
	{
		return _owner != null ? _owner.getPvpFlag() : 0;
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
	
	public L2Skill getSkill()
	{
		return _skill;
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new TrapKnownList(this));
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !canBeSeen(attacker);
	}
	
	@Override
	public boolean isTrap()
	{
		return true;
	}
	
	public boolean isTriggered()
	{
		return _isTriggered;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_isInArena = isInsideZone(ZoneId.PVP) && !isInsideZone(ZoneId.SIEGE);
		_playersWhoDetectedMe.clear();
	}
	
	@Override
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss || (_owner == null))
		{
			return;
		}
		
		if (_owner.isInOlympiadMode() && (target.isPlayer()) && ((L2PcInstance) target).isInOlympiadMode() && (((L2PcInstance) target).getOlympiadGameId() == _owner.getOlympiadGameId()))
		{
			OlympiadGameManager.getInstance().notifyCompetitorDamage(getOwner(), damage);
		}
		
		if (target.isInvul() && !(target instanceof L2NpcInstance))
		{
			_owner.sendPacket(SystemMessageId.ATTACK_WAS_BLOCKED);
		}
		else
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DONE_S3_DAMAGE_TO_C2);
			sm.addCharName(this);
			sm.addCharName(target);
			sm.addNumber(damage);
			_owner.sendPacket(sm);
		}
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (_isTriggered || canBeSeen(activeChar))
		{
			activeChar.sendPacket(new TrapInfo(this, activeChar));
		}
	}
	
	public void setDetected(L2Character detector)
	{
		if (_isInArena)
		{
			if (detector.isPlayable())
			{
				sendInfo(detector.getActingPlayer());
			}
			return;
		}
		
		if ((_owner != null) && (_owner.getPvpFlag() == 0) && (_owner.getKarma() == 0))
		{
			return;
		}
		
		_playersWhoDetectedMe.add(detector.getObjectId());
		
		if (getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION) != null)
		{
			for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION))
			{
				quest.notifyTrapAction(this, detector, TrapAction.TRAP_DETECTED);
			}
		}
		if (detector.isPlayable())
		{
			sendInfo(detector.getActingPlayer());
		}
	}
	
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}
	
	public void triggerTrap(L2Character target)
	{
		_isTriggered = true;
		broadcastPacket(new TrapInfo(this, null));
		setTarget(target);
		
		if (getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION) != null)
		{
			for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION))
			{
				quest.notifyTrapAction(this, target, TrapAction.TRAP_TRIGGERED);
			}
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new TrapTriggerTask(this), 300);
	}
	
	public void unSummon()
	{
		if (_owner != null)
		{
			_owner.setTrap(null);
			_owner = null;
		}
		
		if (isVisible() && !isDead())
		{
			if (getWorldRegion() != null)
			{
				getWorldRegion().removeFromZones(this);
			}
			
			deleteMe();
		}
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		
	}
	
	public boolean hasLifeTime()
	{
		return _hasLifeTime;
	}
	
	public void setHasLifeTime(boolean val)
	{
		_hasLifeTime = val;
	}
	
	public int getRemainingTime()
	{
		return _remainingTime;
	}
	
	public void setRemainingTime(int time)
	{
		_remainingTime = time;
	}
	
	public void setSkill(L2Skill _skill)
	{
		this._skill = _skill;
	}
	
	public int getLifeTime()
	{
		return _lifeTime;
	}
}