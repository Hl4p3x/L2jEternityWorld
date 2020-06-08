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

import java.util.concurrent.ScheduledFuture;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.L2Spawn;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.knownlist.MonsterKnownList;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.util.MinionList;
import l2e.util.Rnd;

public class L2MonsterInstance extends L2Attackable
{
	protected boolean _enableMinions = true;
	
	private L2MonsterInstance _master = null;
	private volatile MinionList _minionList = null;
	
	private boolean _canAgroWhileMoving = false;
	private boolean _isAutoAttackable = true;
	private boolean _isPassive = false;
	private boolean _isMoveToSpawn = false;
	
	protected int _aggroRangeOverride = 0;
	
	protected ScheduledFuture<?> _maintenanceTask = null;
	protected ScheduledFuture<?> _returnToSpawnTask = null;
	
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
	
	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2MonsterInstance);
	}
	
	@Override
	public final MonsterKnownList getKnownList()
	{
		return (MonsterKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new MonsterKnownList(this));
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _isAutoAttackable && !isEventMob();
	}
	
	@Override
	public boolean isAggressive()
	{
		if (_isPassive)
		{
			return false;
		}
		return (getAggroRange() > 0) && !isEventMob();
	}
	
	@Override
	public void onSpawn()
	{
		if (!isTeleporting())
		{
			if (getLeader() != null)
			{
				setIsNoRndWalk(true);
				setIsRaidMinion(getLeader().isRaid());
				getLeader().getMinionList().onMinionSpawn(this);
			}
			
			if (hasMinions())
			{
				getMinionList().onMasterSpawn();
			}
			
			startMaintenanceTask();
		}
		super.onSpawn();
	}
	
	@Override
	public void onTeleported()
	{
		super.onTeleported();
		
		if (hasMinions())
		{
			getMinionList().onMasterTeleported();
		}
	}
	
	protected int getMaintenanceInterval()
	{
		return MONSTER_MAINTENANCE_INTERVAL;
	}
	
	protected void startMaintenanceTask()
	{
		if (getTemplate().getMinionData() == null)
		{
			return;
		}
		
		if (_maintenanceTask == null)
		{
			_maintenanceTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					if (_enableMinions)
					{
						getMinionList().spawnMinions();
					}
				}
			}, getMaintenanceInterval() + Rnd.get(1000));
		}
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (_returnToSpawnTask != null)
		{
			_returnToSpawnTask.cancel(true);
		}
		
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		
		return true;
	}
	
	@Override
	public void deleteMe()
	{
		if (_returnToSpawnTask != null)
		{
			_returnToSpawnTask.cancel(true);
		}
		
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		
		if (hasMinions())
		{
			getMinionList().onMasterDie(true);
		}
		
		if (getLeader() != null)
		{
			getLeader().getMinionList().onMinionDie(this, 0);
		}
		super.deleteMe();
	}
	
	@Override
	public L2MonsterInstance getLeader()
	{
		return _master;
	}
	
	public void setLeader(L2MonsterInstance leader)
	{
		_master = leader;
	}
	
	public void enableMinions(boolean b)
	{
		_enableMinions = b;
	}
	
	public boolean hasMinions()
	{
		return _minionList != null;
	}
	
	public MinionList getMinionList()
	{
		if (_minionList == null)
		{
			synchronized (this)
			{
				if (_minionList == null)
				{
					_minionList = new MinionList(this);
				}
			}
		}
		return _minionList;
	}
	
	@Override
	public boolean isMonster()
	{
		return true;
	}
	
	@Override
	public L2Npc getActingNpc()
	{
		return this;
	}
	
	public final boolean canAgroWhileMoving()
	{
		return _canAgroWhileMoving;
	}
	
	public final void setCanAgroWhileMoving()
	{
		_canAgroWhileMoving = true;
	}
	
	public void setClanOverride(String newClan)
	{
	}
	
	public void setIsAggresiveOverride(int aggroR)
	{
		_aggroRangeOverride = aggroR;
	}
	
	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (!_isPassive)
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}
	
	public void setPassive(boolean state)
	{
		_isPassive = state;
	}
	
	public boolean isPassive()
	{
		return _isPassive;
	}
	
	@Override
	public void setAutoAttackable(boolean state)
	{
		_isAutoAttackable = state;
	}
	
	public final boolean isMoveToSpawn()
	{
		return _isMoveToSpawn;
	}
	
	public final void setIsMoveToSpawn(boolean value)
	{
		_isMoveToSpawn = value;
	}
	
	public void returnToSpawn()
	{
		setIsMoveToSpawn(true);
		final L2Spawn spawn = getSpawn();
		if (spawn == null)
		{
			return;
		}
		
		final int spawnX = spawn.getX();
		final int spawnY = spawn.getY();
		final int spawnZ = spawn.getZ();
		_returnToSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if (!isInCombat() && !isAlikeDead() && !isDead())
				{
					clearAggroList();
					moveToLocation(spawnX, spawnY, spawnZ, 0);
				}
				setIsMoveToSpawn(false);
			}
		}, 15000);
	}
	
	@Override
	public boolean isWalker()
	{
		return ((getLeader() == null) ? super.isWalker() : getLeader().isWalker());
	}
	
	@Override
	public boolean isRunner()
	{
		return ((getLeader() == null) ? super.isRunner() : getLeader().isRunner());
	}
	
	@Override
	public boolean isEkimusFood()
	{
		return ((getLeader() == null) ? super.isEkimusFood() : getLeader().isEkimusFood());
	}

	@Override
	public boolean isSpecialCamera()
	{
		return ((getLeader() == null) ? super.isSpecialCamera() : getLeader().isSpecialCamera());
	}
	
	@Override
	public boolean giveRaidCurse()
	{
		return (isRaidMinion() && (getLeader() != null)) ? getLeader().giveRaidCurse() : super.giveRaidCurse();
	}
}