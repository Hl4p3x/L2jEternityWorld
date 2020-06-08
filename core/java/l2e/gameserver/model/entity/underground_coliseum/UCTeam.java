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
package l2e.gameserver.model.entity.underground_coliseum;

import java.util.logging.Logger;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.sql.NpcTable;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.L2Party;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.instance.L2UCTowerInstance;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPVPMatchUserDie;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.UserInfo;
import l2e.util.Rnd;

public class UCTeam
{
	protected static final Logger _log = Logger.getLogger(UCTeam.class.getName());
	
	public final static byte NOT_DECIDED = 0;
	public final static byte WIN = 1;
	public final static byte FAIL = 2;
	
	private final int _index;
	private final UCArena _baseArena;
	// Resurrection Tower Info
	protected final int _x;
	protected final int _y;
	protected final int _z;
	private final int _npcId;
	
	private L2UCTowerInstance _tower;
	
	private L2Party _party;
	
	private int _killCount;
	private byte _status;
	private L2Party _lastParty;
	
	private int _consecutiveWins;
	
	private long registerTime;
	
	public static L2PcInstance[] _team1 = new L2PcInstance[3];
	public static L2PcInstance[] _team2 = new L2PcInstance[3];
	public static L2PcInstance[] _team3 = new L2PcInstance[3];
	
	public UCTeam(int index, UCArena baseArena, int x, int y, int z, int npcId)
	{
		_index = index;
		_baseArena = baseArena;
		_x = x;
		_y = y;
		_z = z;
		_npcId = npcId;
		
		setStatus(NOT_DECIDED);
	}
	
	public long getRegisterTime()
	{
		return registerTime;
	}
	
	public void setLastParty(L2Party party)
	{
		_lastParty = party;
	}
	
	public void setRegisterTime(long time)
	{
		registerTime = time;
	}
	
	public void increaseConsecutiveWins()
	{
		_consecutiveWins++;
	}
	
	public int getConsecutiveWins()
	{
		return _consecutiveWins;
	}
	
	public void spawnTower()
	{
		if (_tower != null)
		{
			_log.info("Tower already exists. Can't spawn tower.");
			return;
		}
		
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(_npcId);
		
		if (template == null)
		{
			_log.info("Can't find Underground Coliseum tower template: " + _npcId);
			return;
		}
		
		_tower = new L2UCTowerInstance(this, IdFactory.getInstance().getNextId(), template);
		// _tower.setTeam(_index);
		_tower.setIsInvul(false);
		_tower.setCurrentHpMp(_tower.getMaxHp(), _tower.getMaxMp());
		_tower.spawnMe(_x, _y, _z);
	}
	
	public void deleteTower()
	{
		if (_tower == null)
		{
			return;
		}
		
		_tower.deleteMe();
		_tower = null;
	}
	
	public void splitMembersAndTeleport()
	{
		UCPoint[] pointzor = _baseArena.getPoints();
		for (L2PcInstance player : getParty().getMembers())
		{
			if (player == null)
			{
				continue;
			}
			
			// if (_team1[] < 3)
			// _team1[_team1[].length] = player;
			
			// else if (_team2[].length < 2 + count)
			// _team2[_team2[].length] = player;
			
			// else
			// _team3[_team3[].length] = player;
			player.setUCState(L2PcInstance.UC_STATE_POINT);
		}
		pointzor[1].teleportPeoples(_team1);
		pointzor[2].teleportPeoples(_team2);
		pointzor[3].teleportPeoples(_team3);
	}
	
	public boolean isKilledByThisTeam(final L2PcInstance killer)
	{
		if ((getParty() == null) || (killer == null))
		{
			return false;
		}
		
		for (L2PcInstance member : getParty().getMembers())
		{
			if ((member != null) && (member == killer))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void onKill(final L2PcInstance player, final L2PcInstance killer)
	{
		if ((player == null) || (killer == null) || (getParty() == null))
		{
			return;
		}
		
		if (!player.isDead())
		{
			return;
		}
		
		UCTeam otherTeam = getOtherTeam();
		UCPoint[] pointzor = _baseArena.getPoints();
		
		if (!otherTeam.isKilledByThisTeam(killer))
		{
			return;
		}
		
		otherTeam.increaseKillCount();
		player.increaseDeathCountUC();
		killer.increaseKillCountUC();
		
		_baseArena.broadcastToAll(new ExPVPMatchUserDie(_baseArena));
		
		if (player.getUCState() == L2PcInstance.UC_STATE_POINT)
		{
			boolean deadzor = true;
			for (L2PcInstance member1 : _team1)
			{
				if ((member1 != null) && !member1.isDead())
				{
					deadzor = false;
				}
			}
			if (deadzor)
			{
				pointzor[1].actionDoors(true);
				for (L2PcInstance memberzor1 : _team1)
				{
					memberzor1.setUCState(L2PcInstance.UC_STATE_ARENA);
				}
			}
			else
			{
				deadzor = true;
			}
			for (L2PcInstance member2 : _team2)
			{
				if ((member2 != null) && !member2.isDead())
				{
					deadzor = false;
				}
			}
			if (deadzor)
			{
				pointzor[2].actionDoors(true);
				for (L2PcInstance memberzor2 : _team2)
				{
					memberzor2.setUCState(L2PcInstance.UC_STATE_ARENA);
				}
			}
			else
			{
				deadzor = true;
			}
			for (L2PcInstance member3 : _team3)
			{
				if ((member3 != null) && !member3.isDead())
				{
					deadzor = false;
				}
			}
			if (deadzor)
			{
				pointzor[3].actionDoors(true);
				for (L2PcInstance memberzor3 : _team3)
				{
					memberzor3.setUCState(L2PcInstance.UC_STATE_ARENA);
				}
			}
			else
			{
				deadzor = true;
			}
		}
		
		if (_tower == null)
		{
			boolean flag = true;
			for (L2PcInstance member : getParty().getMembers())
			{
				if ((member != null) && !member.isDead())
				{
					flag = false;
				}
			}
			
			if (flag)
			{
				setStatus(FAIL);
				otherTeam.setStatus(WIN);
				_baseArena.runTaskNow();
				
			}
			return;
		}
		
		ThreadPoolManager.getInstance().executeTask(new Runnable()
		{
			@Override
			public void run()
			{
				resPlayer(player);
				
				player.teleToLocation(_x + Rnd.get(2, 50), _y + Rnd.get(10, 100), _z);
			}
		});
	}
	
	public void increaseKillCount()
	{
		_killCount++;
	}
	
	public static void resPlayer(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		player.restoreExp(100.0);
		player.doRevive();
		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
	}
	
	public void clean(boolean full)
	{
		deleteTower();
		
		if (full)
		{
			if (getParty() != null)
			{
				getParty().setUCState(null);
				_party = null;
			}
			
			_lastParty = null;
			_consecutiveWins = 0;
			setStatus(NOT_DECIDED);
		}
		
		_killCount = 0;
	}
	
	public byte getStatus()
	{
		return _status;
	}
	
	public UCArena getBaseArena()
	{
		return _baseArena;
	}
	
	public void computeReward()
	{
		if ((_lastParty == null) || (_lastParty != getOtherTeam().getParty()))
		{
			int reward = 0;
			switch (_consecutiveWins)
			{
				case 1:
					reward = 80;
					break;
				case 2:
					reward = 85;
					break;
				case 3:
					reward = 90;
					break;
				case 4:
					reward = 95;
					break;
				case 5:
					reward = 100;
					break;
				case 6:
					reward = 102;
					break;
				case 7:
					reward = 104;
					break;
				case 8:
					reward = 106;
					break;
				case 9:
					reward = 108;
					break;
				case 10:
					reward = 110;
					break;
				default:
					if (_consecutiveWins > 10)
					{
						reward = (110 + _consecutiveWins) - 10;
					}
					break;
			}
			
			for (L2PcInstance member : getParty().getMembers())
			{
				if (member != null)
				{
					member.setFame(member.getFame() + reward);
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_REPUTATION_SCORE);
					sm.addNumber(reward);
					member.sendPacket(sm);
					member.sendPacket(new UserInfo(member));
				}
			}
		}
	}
	
	public void setStatus(byte status)
	{
		_status = status;
		
		if (_status == WIN)
		{
			if (getIndex() == 0)
			{
				_baseArena.broadcastToAll(SystemMessage.getSystemMessage(SystemMessageId.THE_BLUE_TEAM_IS_VICTORIOUS));
			}
			else
			{
				_baseArena.broadcastToAll(SystemMessage.getSystemMessage(SystemMessageId.THE_RED_TEAM_IS_VICTORIOUS));
			}
		}
		
		switch (_status)
		{
			case NOT_DECIDED:
				break;
			case WIN:
				increaseConsecutiveWins();
				computeReward();
				clean(false);
				break;
			case FAIL:
				clean(true);
				break;
		}
	}
	
	public UCTeam getOtherTeam()
	{
		return _baseArena.getTeams()[getOtherTeamIndex()];
	}
	
	public int getOtherTeamIndex()
	{
		if (_index == 0)
		{
			return 1;
		}
		if (_index == 1)
		{
			return 0;
		}
		
		throw new IllegalArgumentException("Incorrect index: " + _index);
	}
	
	public int getKillCount()
	{
		return _killCount;
	}
	
	public void setParty(L2Party pa)
	{
		L2Party oldParty = _party;
		_party = pa;
		
		if (oldParty != null)
		{
			oldParty.setUCState(null);
		}
		
		if (_party != null)
		{
			_party.setUCState(this);
		}
	}
	
	public L2Party getParty()
	{
		return _party;
	}
	
	public int getIndex()
	{
		return _index;
	}
}