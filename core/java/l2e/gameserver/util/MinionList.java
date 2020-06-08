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
package l2e.gameserver.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastSet;
import l2e.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.L2MinionData;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.instance.L2MonsterInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.util.Rnd;

public class MinionList
{
	private static Logger _log = Logger.getLogger(MinionList.class.getName());
	
	protected final L2MonsterInstance _master;
	private final List<L2MonsterInstance> _minionReferences;
	protected List<L2MonsterInstance> _reusedMinionReferences = null;
	
	public MinionList(L2MonsterInstance pMaster)
	{
		if (pMaster == null)
		{
			throw new NullPointerException("MinionList: master is null");
		}
		
		_master = pMaster;
		_minionReferences = new FastList<L2MonsterInstance>().shared();
	}
	
	public List<L2MonsterInstance> getSpawnedMinions()
	{
		return _minionReferences;
	}
	
	public final void spawnMinions()
	{
		if (_master.isAlikeDead())
		{
			return;
		}
		List<L2MinionData> minions = _master.getTemplate().getMinionData();
		if (minions == null)
		{
			return;
		}
		
		int minionCount, minionId, minionsToSpawn;
		for (L2MinionData minion : minions)
		{
			minionCount = minion.getAmount();
			minionId = minion.getMinionId();
			
			minionsToSpawn = minionCount - countSpawnedMinionsById(minionId);
			if (minionsToSpawn > 0)
			{
				for (int i = 0; i < minionsToSpawn; i++)
				{
					spawnMinion(minionId);
				}
			}
		}
		deleteReusedMinions();
	}
	
	public void deleteSpawnedMinions()
	{
		if (!_minionReferences.isEmpty())
		{
			for (L2MonsterInstance minion : _minionReferences)
			{
				if (minion != null)
				{
					minion.setLeader(null);
					minion.deleteMe();
					if (_reusedMinionReferences != null)
					{
						_reusedMinionReferences.add(minion);
					}
				}
			}
			_minionReferences.clear();
		}
	}
	
	public void deleteReusedMinions()
	{
		if (_reusedMinionReferences != null)
		{
			_reusedMinionReferences.clear();
		}
	}
	
	public void onMasterSpawn()
	{
		deleteSpawnedMinions();
		
		if ((_reusedMinionReferences == null) && (_master.getTemplate().getMinionData() != null) && (_master.getSpawn() != null) && _master.getSpawn().isRespawnEnabled())
		{
			_reusedMinionReferences = new FastList<L2MonsterInstance>().shared();
		}
	}
	
	public void onMinionSpawn(L2MonsterInstance minion)
	{
		_minionReferences.add(minion);
	}
	
	public void onMasterDie(boolean force)
	{
		if (_master.isRaid() || force)
		{
			deleteSpawnedMinions();
		}
	}
	
	public void onMinionDie(L2MonsterInstance minion, int respawnTime)
	{
		minion.setLeader(null);
		_minionReferences.remove(minion);
		if (_reusedMinionReferences != null)
		{
			_reusedMinionReferences.add(minion);
		}
		
		final int time = respawnTime < 0 ? _master.isRaid() ? (int) Config.RAID_MINION_RESPAWN_TIMER : 0 : respawnTime;
		if ((time > 0) && !_master.isAlikeDead())
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new MinionRespawnTask(minion), time);
		}
	}
	
	public void onAssist(L2Character caller, L2Character attacker)
	{
		if (attacker == null)
		{
			return;
		}
		
		if (!_master.isAlikeDead() && !_master.isInCombat())
		{
			_master.addDamageHate(attacker, 0, 1);
		}
		
		final boolean callerIsMaster = caller == _master;
		int aggro = callerIsMaster ? 10 : 1;
		if (_master.isRaid())
		{
			aggro *= 10;
		}
		
		for (L2MonsterInstance minion : _minionReferences)
		{
			if ((minion != null) && !minion.isDead() && (callerIsMaster || !minion.isInCombat()))
			{
				minion.addDamageHate(attacker, 0, aggro);
			}
		}
	}
	
	public void onMasterTeleported()
	{
		final int offset = 200;
		final int minRadius = (int) _master.getCollisionRadius() + 30;
		
		for (L2MonsterInstance minion : _minionReferences)
		{
			if ((minion != null) && !minion.isDead() && !minion.isMovementDisabled())
			{
				int newX = Rnd.get(minRadius * 2, offset * 2);
				int newY = Rnd.get(newX, offset * 2);
				newY = (int) Math.sqrt((newY * newY) - (newX * newX));
				if (newX > (offset + minRadius))
				{
					newX = (_master.getX() + newX) - offset;
				}
				else
				{
					newX = (_master.getX() - newX) + minRadius;
				}
				if (newY > (offset + minRadius))
				{
					newY = (_master.getY() + newY) - offset;
				}
				else
				{
					newY = (_master.getY() - newY) + minRadius;
				}
				
				minion.teleToLocation(newX, newY, _master.getZ());
			}
		}
	}
	
	private final void spawnMinion(int minionId)
	{
		if (minionId == 0)
		{
			return;
		}
		
		if ((_reusedMinionReferences != null) && !_reusedMinionReferences.isEmpty())
		{
			L2MonsterInstance minion;
			Iterator<L2MonsterInstance> iter = _reusedMinionReferences.iterator();
			while (iter.hasNext())
			{
				minion = iter.next();
				if ((minion != null) && (minion.getId() == minionId))
				{
					iter.remove();
					minion.refreshID();
					initializeNpcInstance(_master, minion);
					return;
				}
			}
		}
		spawnMinion(_master, minionId);
	}
	
	private final class MinionRespawnTask implements Runnable
	{
		private final L2MonsterInstance _minion;
		
		public MinionRespawnTask(L2MonsterInstance minion)
		{
			_minion = minion;
		}
		
		@Override
		public void run()
		{
			if (!_master.isAlikeDead() && _master.isVisible())
			{
				if (!_minion.isVisible())
				{
					if (_reusedMinionReferences != null)
					{
						_reusedMinionReferences.remove(_minion);
					}
					
					_minion.refreshID();
					initializeNpcInstance(_master, _minion);
				}
			}
		}
	}
	
	public static final L2MonsterInstance spawnMinion(L2MonsterInstance master, int minionId)
	{
		L2NpcTemplate minionTemplate = NpcTable.getInstance().getTemplate(minionId);
		if (minionTemplate == null)
		{
			return null;
		}
		
		L2MonsterInstance minion = new L2MonsterInstance(IdFactory.getInstance().getNextId(), minionTemplate);
		return initializeNpcInstance(master, minion);
	}
	
	protected static final L2MonsterInstance initializeNpcInstance(L2MonsterInstance master, L2MonsterInstance minion)
	{
		minion.stopAllEffects();
		minion.setIsDead(false);
		minion.setDecayed(false);
		minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp());
		minion.setHeading(master.getHeading());
		minion.setLeader(master);
		minion.setInstanceId(master.getInstanceId());
		
		final int offset = 200;
		final int minRadius = (int) master.getCollisionRadius() + 30;
		
		int newX = Rnd.get(minRadius * 2, offset * 2);
		int newY = Rnd.get(newX, offset * 2);
		newY = (int) Math.sqrt((newY * newY) - (newX * newX));
		if (newX > (offset + minRadius))
		{
			newX = (master.getX() + newX) - offset;
		}
		else
		{
			newX = (master.getX() - newX) + minRadius;
		}
		if (newY > (offset + minRadius))
		{
			newY = (master.getY() + newY) - offset;
		}
		else
		{
			newY = (master.getY() - newY) + minRadius;
		}
		
		minion.spawnMe(newX, newY, master.getZ());
		
		if (Config.DEBUG)
		{
			_log.info("Spawned minion template " + minion.getId() + " with objid: " + minion.getObjectId() + " to boss " + master.getObjectId() + " ,at: " + minion.getX() + " x, " + minion.getY() + " y, " + minion.getZ() + " z");
		}
		return minion;
	}
	
	private final int countSpawnedMinionsById(int minionId)
	{
		int count = 0;
		for (L2MonsterInstance minion : _minionReferences)
		{
			if ((minion != null) && (minion.getId() == minionId))
			{
				count++;
			}
		}
		return count;
	}
	
	public final int countSpawnedMinions()
	{
		return _minionReferences.size();
	}
	
	public final int lazyCountSpawnedMinionsGroups()
	{
		Set<Integer> seenGroups = new FastSet<>();
		for (L2MonsterInstance minion : _minionReferences)
		{
			if (minion == null)
			{
				continue;
			}
			
			seenGroups.add(minion.getId());
		}
		return seenGroups.size();
	}
}