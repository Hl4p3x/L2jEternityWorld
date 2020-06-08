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
package l2e.gameserver.model.zone.type;

import java.util.Map;

import javolution.util.FastMap;
import l2e.gameserver.GameServer;
import l2e.gameserver.instancemanager.GrandBossManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.L2Attackable;
import l2e.gameserver.model.actor.L2Character;
import l2e.gameserver.model.actor.L2Npc;
import l2e.gameserver.model.actor.L2Summon;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.zone.AbstractZoneSettings;
import l2e.gameserver.model.zone.L2ZoneType;
import l2e.util.L2FastList;

public class L2BossZone extends L2ZoneType
{
	private int _timeInvade;
	private boolean _enabled = true;
	
	private int[] _oustLoc =
	{
		0,
		0,
		0
	};
	
	private final class Settings extends AbstractZoneSettings
	{
		private final FastMap<Integer, Long> _playerAllowedReEntryTimes;
		
		private final L2FastList<Integer> _playersAllowed;
		
		private final L2FastList<L2Character> _raidList;
		
		public Settings()
		{
			_playerAllowedReEntryTimes = new FastMap<>();
			_playersAllowed = new L2FastList<>();
			_raidList = new L2FastList<>();
		}
		
		public FastMap<Integer, Long> getPlayerAllowedReEntryTimes()
		{
			return _playerAllowedReEntryTimes;
		}
		
		public L2FastList<Integer> getPlayersAllowed()
		{
			return _playersAllowed;
		}
		
		public L2FastList<L2Character> getRaidList()
		{
			return _raidList;
		}
		
		@Override
		public void clear()
		{
			_playerAllowedReEntryTimes.clear();
			_playersAllowed.clear();
			_raidList.clear();
		}
	}
	
	public L2BossZone(int id)
	{
		super(id);
		_oustLoc = new int[3];
		AbstractZoneSettings settings = ZoneManager.getSettings(getName());
		if (settings == null)
		{
			settings = new Settings();
		}
		setSettings(settings);
		GrandBossManager.getInstance().addZone(this);
	}
	
	@Override
	public Settings getSettings()
	{
		return (Settings) super.getSettings();
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("InvadeTime"))
		{
			_timeInvade = Integer.parseInt(value);
		}
		else if (name.equals("default_enabled"))
		{
			_enabled = Boolean.parseBoolean(value);
		}
		else if (name.equals("oustX"))
		{
			_oustLoc[0] = Integer.parseInt(value);
		}
		else if (name.equals("oustY"))
		{
			_oustLoc[1] = Integer.parseInt(value);
		}
		else if (name.equals("oustZ"))
		{
			_oustLoc[2] = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (_enabled)
		{
			if (character.isPlayer())
			{
				final L2PcInstance player = character.getActingPlayer();
				if (player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS))
				{
					return;
				}
				if (getSettings().getPlayersAllowed().contains(player.getObjectId()))
				{
					final Long expirationTime = getSettings().getPlayerAllowedReEntryTimes().get(player.getObjectId());
					
					if (expirationTime == null)
					{
						long serverStartTime = GameServer.dateTimeServerStarted.getTimeInMillis();
						if ((serverStartTime > (System.currentTimeMillis() - _timeInvade)))
						{
							return;
						}
					}
					else
					{
						getSettings().getPlayerAllowedReEntryTimes().remove(player.getObjectId());
						if (expirationTime.longValue() > System.currentTimeMillis())
						{
							return;
						}
					}
					getSettings().getPlayersAllowed().remove(getSettings().getPlayersAllowed().indexOf(player.getObjectId()));
				}
				
				if ((_oustLoc[0] != 0) && (_oustLoc[1] != 0) && (_oustLoc[2] != 0))
				{
					player.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2]);
				}
				else
				{
					player.teleToLocation(TeleportWhereType.TOWN);
				}
			}
			else if (character.isSummon())
			{
				final L2PcInstance player = character.getActingPlayer();
				if (player != null)
				{
					if (getSettings().getPlayersAllowed().contains(player.getObjectId()) || player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS))
					{
						return;
					}
					
					if ((_oustLoc[0] != 0) && (_oustLoc[1] != 0) && (_oustLoc[2] != 0))
					{
						player.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2]);
					}
					else
					{
						player.teleToLocation(TeleportWhereType.TOWN);
					}
				}
				((L2Summon) character).unSummon(player);
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (_enabled)
		{
			if (character.isPlayer())
			{
				final L2PcInstance player = character.getActingPlayer();
				if (player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS))
				{
					return;
				}
				
				if (!player.isOnline() && getSettings().getPlayersAllowed().contains(player.getObjectId()))
				{
					getSettings().getPlayerAllowedReEntryTimes().put(player.getObjectId(), System.currentTimeMillis() + _timeInvade);
				}
				else
				{
					if (getSettings().getPlayersAllowed().contains(player.getObjectId()))
					{
						getSettings().getPlayersAllowed().remove(getSettings().getPlayersAllowed().indexOf(player.getObjectId()));
					}
					getSettings().getPlayerAllowedReEntryTimes().remove(player.getObjectId());
				}
			}
			if (character.isPlayable())
			{
				if ((getCharactersInside() != null) && !getCharactersInside().isEmpty())
				{
					getSettings().getRaidList().clear();
					int count = 0;
					for (L2Character obj : getCharactersInside())
					{
						if (obj == null)
						{
							continue;
						}
						if (obj.isPlayable())
						{
							count++;
						}
						else if (obj.isL2Attackable() && obj.isRaid())
						{
							getSettings().getRaidList().add(obj);
						}
					}
					
					if ((count == 0) && !getSettings().getRaidList().isEmpty())
					{
						for (int i = 0; i < getSettings().getRaidList().size(); i++)
						{
							L2Attackable raid = (L2Attackable) getSettings().getRaidList().get(i);
							if ((raid == null) || (raid.getSpawn() == null) || raid.isDead())
							{
								continue;
							}
							if (!raid.isInsideRadius(raid.getSpawn().getX(), raid.getSpawn().getY(), 150, false))
							{
								raid.returnHome();
							}
						}
					}
				}
			}
		}
		if (character.isL2Attackable() && character.isRaid() && !character.isDead())
		{
			((L2Attackable) character).returnHome();
		}
	}
	
	public void setZoneEnabled(boolean flag)
	{
		if (_enabled != flag)
		{
			oustAllPlayers();
		}
		
		_enabled = flag;
	}
	
	public int getTimeInvade()
	{
		return _timeInvade;
	}
	
	public void setAllowedPlayers(L2FastList<Integer> players)
	{
		if (players != null)
		{
			getSettings().getPlayersAllowed().clear();
			getSettings().getPlayersAllowed().addAll(players);
		}
	}
	
	public L2FastList<Integer> getAllowedPlayers()
	{
		return getSettings().getPlayersAllowed();
	}
	
	public boolean isPlayerAllowed(L2PcInstance player)
	{
		if (player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS))
		{
			return true;
		}
		else if (getSettings().getPlayersAllowed().contains(player.getObjectId()))
		{
			return true;
		}
		else
		{
			if ((_oustLoc[0] != 0) && (_oustLoc[1] != 0) && (_oustLoc[2] != 0))
			{
				player.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2]);
			}
			else
			{
				player.teleToLocation(TeleportWhereType.TOWN);
			}
			return false;
		}
	}
	
	public void movePlayersTo(int x, int y, int z)
	{
		if (_characterList.isEmpty())
		{
			return;
		}
		
		for (L2Character character : getCharactersInside())
		{
			if ((character != null) && character.isPlayer())
			{
				L2PcInstance player = character.getActingPlayer();
				if (player.isOnline())
				{
					player.teleToLocation(x, y, z);
				}
			}
		}
	}
	
	public void oustAllPlayers()
	{
		if (_characterList.isEmpty())
		{
			return;
		}
		
		for (L2Character character : getCharactersInside())
		{
			if ((character != null) && character.isPlayer())
			{
				L2PcInstance player = character.getActingPlayer();
				if (player.isOnline())
				{
					if ((_oustLoc[0] != 0) && (_oustLoc[1] != 0) && (_oustLoc[2] != 0))
					{
						player.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2]);
					}
					else
					{
						player.teleToLocation(TeleportWhereType.TOWN);
					}
				}
			}
		}
		getSettings().getPlayerAllowedReEntryTimes().clear();
		getSettings().getPlayersAllowed().clear();
	}
	
	public void allowPlayerEntry(L2PcInstance player, int durationInSec)
	{
		if (!player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS))
		{
			if (!getSettings().getPlayersAllowed().contains(player.getObjectId()))
			{
				getSettings().getPlayersAllowed().add(player.getObjectId());
			}
			getSettings().getPlayerAllowedReEntryTimes().put(player.getObjectId(), System.currentTimeMillis() + (durationInSec * 1000));
		}
	}
	
	public void removePlayer(L2PcInstance player)
	{
		if (!player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS))
		{
			getSettings().getPlayersAllowed().remove(Integer.valueOf(player.getObjectId()));
			getSettings().getPlayerAllowedReEntryTimes().remove(player.getObjectId());
		}
	}
	
	public void updateKnownList(L2Npc npc)
	{
		if ((_characterList == null) || _characterList.isEmpty())
		{
			return;
		}
		
		Map<Integer, L2PcInstance> npcKnownPlayers = npc.getKnownList().getKnownPlayers();
		for (L2Character character : getCharactersInside())
		{
			if ((character != null) && character.isPlayer())
			{
				L2PcInstance player = character.getActingPlayer();
				if (player.isOnline())
				{
					npcKnownPlayers.put(player.getObjectId(), player);
				}
			}
		}
	}
}