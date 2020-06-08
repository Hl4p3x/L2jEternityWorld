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

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.L2Clan;
import l2e.gameserver.model.L2SiegeClan;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.status.SiegeFlagStatus;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.entity.Siegable;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFailed;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class L2SiegeFlagInstance extends L2Npc
{
	private L2Clan _clan;
	private L2PcInstance _player;
	private Siegable _siege;
	private final boolean _isAdvanced;
	private boolean _canTalk;
	
	public L2SiegeFlagInstance(L2PcInstance player, int objectId, L2NpcTemplate template, boolean advanced, boolean outPost)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2SiegeFlagInstance);
		
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			_clan = player.getClan();
			_player = player;
			_canTalk = false;
			if (_clan == null)
				deleteMe();
			if (outPost)
			{
				_isAdvanced = false;
				setIsInvul(true);
			}
			else
			{
				_isAdvanced = advanced;
				setIsInvul(false);
			}
			getStatus();
			return;
		}
		_clan = player.getClan();
		_player = player;
		_canTalk = true;
		_siege = SiegeManager.getInstance().getSiege(_player.getX(), _player.getY(), _player.getZ());
		if (_siege == null)
			_siege = FortSiegeManager.getInstance().getSiege(_player.getX(), _player.getY(), _player.getZ());
		if(_siege == null)
			_siege = CHSiegeManager.getInstance().getSiege(player);
		if (_clan == null || _siege == null)
		{
			throw new NullPointerException(getClass().getSimpleName()+": Initialization failed.");
		}
		
		L2SiegeClan sc = _siege.getAttackerClan(_clan);
		if (sc == null)
			throw new NullPointerException(getClass().getSimpleName()+": Cannot find siege clan.");
		
		sc.addFlag(this);
		_isAdvanced = advanced;
		getStatus();
		setIsInvul(false);
	}

	@Deprecated
	public L2SiegeFlagInstance(L2PcInstance player, int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_isAdvanced = false;
	}
	
	@Override
	public boolean isAttackable()
	{
		return !isInvul();
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !isInvul();
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		if (_siege != null && _clan != null)
		{
			L2SiegeClan sc = _siege.getAttackerClan(_clan);
			if (sc != null)
				sc.removeFlag(this);
		}
		else if (_clan != null)
			TerritoryWarManager.getInstance().removeClanFlag(_clan);
		return true;
	}
	
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
	
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (player == null || !canTarget(player))
			return;

		if (this != player.getTarget())
		{
			player.setTarget(this);
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	public boolean isAdvancedHeadquarter()
	{
		return _isAdvanced;
	}
	
	@Override
	public SiegeFlagStatus getStatus()
	{
		return (SiegeFlagStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new SiegeFlagStatus(this));
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, skill);
		if(canTalk())
		{
			if ((getCastle() != null && getCastle().getSiege().getIsInProgress())
				|| (getFort() != null && getFort().getSiege().getIsInProgress())
				|| (getConquerableHall() != null && getConquerableHall().isInSiege()))
			{
				if (_clan != null)
				{
					_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
					setCanTalk(false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTalkTask(), 20000);
				}
			}
		}
	}
	private class ScheduleTalkTask implements Runnable
	{
		
		public ScheduleTalkTask() {}
		
		@Override
		public void run()
		{
			setCanTalk(true);
		}
	}
	
	void setCanTalk(boolean val)
	{
		_canTalk = val;
	}
	
	private boolean canTalk()
	{
		return _canTalk;
	}
}