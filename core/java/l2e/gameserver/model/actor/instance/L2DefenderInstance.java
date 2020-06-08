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

import l2e.Config;
import l2e.gameserver.ai.CtrlIntention;
import l2e.gameserver.ai.L2CharacterAI;
import l2e.gameserver.ai.L2FortSiegeGuardAI;
import l2e.gameserver.ai.L2SiegeGuardAI;
import l2e.gameserver.ai.L2SpecialSiegeGuardAI;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Playable;
import l2e.gameserver.model.actor.knownlist.DefenderKnownList;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.network.serverpackets.ActionFailed;

public class L2DefenderInstance extends L2Attackable
{
	private Castle _castle = null;
	private Fort _fort = null;
	private SiegableHall _hall = null;
	
	public L2DefenderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2DefenderInstance);
	}
	
	@Override
	public DefenderKnownList getKnownList()
	{
		return (DefenderKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new DefenderKnownList(this));
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
					if ((getConquerableHall() == null) && (getCastle(10000) == null))
					{
						_ai = new L2FortSiegeGuardAI(new AIAccessor());
					}
					else if (getCastle(10000) != null)
					{
						_ai = new L2SiegeGuardAI(new AIAccessor());
					}
					else
					{
						_ai = new L2SpecialSiegeGuardAI(new AIAccessor());
					}
				}
				return _ai;
			}
		}
		return _ai;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (!(attacker instanceof L2Playable))
		{
			return false;
		}
		
		L2PcInstance player = attacker.getActingPlayer();
		
		if (((_fort != null) && _fort.getZone().isActive()) || ((_castle != null) && _castle.getZone().isActive()) || ((_hall != null) && _hall.getSiegeZone().isActive()))
		{
			int activeSiegeId = (_fort != null ? _fort.getId() : (_castle != null ? _castle.getId() : (_hall != null ? _hall.getId() : 0)));
			
			if ((player != null) && (((player.getSiegeState() == 2) && !player.isRegisteredOnThisSiegeField(activeSiegeId)) || ((player.getSiegeState() == 1) && !TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId)) || (player.getSiegeState() == 0)))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public void returnHome()
	{
		if (getWalkSpeed() <= 0)
		{
			return;
		}
		if (getSpawn() == null)
		{
			return;
		}
		if (!isInsideRadius(getSpawn().getX(), getSpawn().getY(), 40, false))
		{
			if (Config.DEBUG)
			{
				_log.info(getObjectId() + ": moving home");
			}
			setisReturningToSpawnPoint(true);
			clearAggroList();
			
			if (hasAI())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, getSpawn().getLocation());
			}
		}
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		_fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
		_castle = CastleManager.getInstance().getCastle(getX(), getY(), getZ());
		_hall = getConquerableHall();
		if ((_fort == null) && (_castle == null) && (_hall == null))
		{
			_log.warning("L2DefenderInstance spawned outside of Fortress, Castle or Siegable hall Zone! NpcId: " + getId() + " x=" + getX() + " y=" + getY() + " z=" + getZ());
		}
	}
	
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (!canTarget(player))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (this != player.getTarget())
		{
			if (Config.DEBUG)
			{
				_log.info("new target selected:" + getObjectId());
			}
			player.setTarget(this);
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				if (Math.abs(player.getZ() - getZ()) < 600)
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
			if (!isAutoAttackable(player))
			{
				if (!canInteract(player))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
		{
			return;
		}
		
		if (!(attacker instanceof L2DefenderInstance))
		{
			if ((damage == 0) && (aggro <= 1) && (attacker instanceof L2Playable))
			{
				L2PcInstance player = attacker.getActingPlayer();
				
				if (((_fort != null) && _fort.getZone().isActive()) || ((_castle != null) && _castle.getZone().isActive()) || ((_hall != null) && _hall.getSiegeZone().isActive()))
				{
					int activeSiegeId = (_fort != null ? _fort.getId() : (_castle != null ? _castle.getId() : (_hall != null ? _hall.getId() : 0)));
					if ((player != null) && (((player.getSiegeState() == 2) && player.isRegisteredOnThisSiegeField(activeSiegeId)) || ((player.getSiegeState() == 1) && TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId))))
					{
						return;
					}
				}
			}
			super.addDamageHate(attacker, damage, aggro);
		}
	}
}